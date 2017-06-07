/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.scheduler.jobs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.notification.CmsPublishNotification;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Scheduled job for time based publishing.<p>
 *
 * This class is called via the front end to publish scheduled a file at a given time.<p>
 *
 * Per default, it publishes all new, edited and deleted resources in the project which are locked in
 * the current project. For all resources in the project which are not locked by the current user is the
 * lock changed. You are able to perform a link validation before
 * publishing the project by adding the parameter <code>linkcheck=true</code>. It is possible to send
 * an email to a user in OpenCms in case somthing went wrong during this process. To do so specifiy
 * a parameter<code>mail-to-user=username_in_opencms</code>.
 * After running this job, the job is deleted. Therefore the job name is to set in the parameter <code>jobname</code><p>
 *
 * @since 7.5.1
 */
public class CmsPublishScheduledJob implements I_CmsScheduledJob {

    /** Job name parameter. */
    public static final String PARAM_JOBNAME = "jobname";

    /** Linkcheck parameter. */
    public static final String PARAM_LINKCHECK = "linkcheck";

    /** Mail to user parameter. */
    public static final String PARAM_USER = "mail-to-user";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishScheduledJob.class);

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public synchronized String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        Date jobStart = new Date();
        String finishMessage;
        String linkcheck = parameters.get(PARAM_LINKCHECK);
        String jobName = parameters.get(PARAM_JOBNAME);
        CmsProject project = cms.getRequestContext().getCurrentProject();
        CmsLogReport report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsPublishScheduledJob.class);

        try {

            // validate links if linkcheck parameter was given
            if (Boolean.valueOf(linkcheck).booleanValue()) {
                OpenCms.getPublishManager().validateRelations(
                    cms,
                    OpenCms.getPublishManager().getPublishList(cms),
                    report);
            }

            // change lock for the resources if necessary
            Iterator<String> iter = cms.readProjectResources(project).iterator();
            while (iter.hasNext()) {
                String resource = iter.next();
                // get current lock from file
                CmsLock lock = cms.getLock(resource);
                // prove is current lock from current but not in current project
                if ((lock != null)
                    && lock.isInProject(cms.getRequestContext().getCurrentProject())
                    && !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                    // file is locked in current project but not by current user
                    // unlock this file
                    cms.changeLock(resource);
                }
            }

            // publish the project, the publish output will be put in the logfile
            OpenCms.getPublishManager().publishProject(cms, report);
            OpenCms.getPublishManager().waitWhileRunning();
            finishMessage = Messages.get().getBundle().key(Messages.LOG_PUBLISH_FINISHED_1, project.getName());
        } catch (CmsException e) {
            // there was an error, so create an output for the logfile
            finishMessage = Messages.get().getBundle().key(
                Messages.LOG_PUBLISH_FAILED_2,
                project.getName(),
                e.getMessageContainer().key());

            // add error to report
            report.addError(finishMessage);
        } finally {
            //wait for other processes that may add entries to the report
            long lastTime = report.getLastEntryTime();
            long beforeLastTime = 0;
            while (lastTime != beforeLastTime) {
                wait(30000);
                beforeLastTime = lastTime;
                lastTime = report.getLastEntryTime();
            }

            // delete the job
            // the job id
            String jobId = "";
            // iterate over all jobs to find the current one
            Iterator<CmsScheduledJobInfo> iter = OpenCms.getScheduleManager().getJobs().iterator();
            while (iter.hasNext()) {
                CmsScheduledJobInfo jobInfo = iter.next();
                // the current job is found with the job name
                if (jobInfo.getJobName().equals(jobName)) {
                    // get the current job id
                    jobId = jobInfo.getId();
                }
            }
            // delete the current job
            OpenCms.getScheduleManager().unscheduleJob(cms, jobId);

            // send publish notification
            if (report.hasWarning() || report.hasError()) {
                try {
                    String userName = parameters.get(PARAM_USER);
                    CmsUser user = cms.readUser(userName);

                    CmsPublishNotification notification = new CmsPublishNotification(cms, user, report);

                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                    notification.addMacro("jobStart", df.format(jobStart));

                    notification.send();
                } catch (Exception e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISH_SEND_NOTIFICATION_FAILED_0), e);
                }
            }
        }

        return finishMessage;
    }
}