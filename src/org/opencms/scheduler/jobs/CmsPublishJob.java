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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.notification.CmsPublishNotification;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Scheduled job for time based publishing.<p>
 *
 * This class is called via the scheduled job backoffice to publish a project at a given time.<p>
 *
 * Per default, it publishes all new, edited and deleted resources in the project which are not locked.
 * To unlock all resources in the project before publishing, add the parameter <code>unlock=true</code>
 * in the scheduled job configuration. In addition you are able to perform a link validation before
 * publishing the project by adding the parameter <code>linkcheck=true</code>. It is possible to send
 * an email to a user in OpenCms in case somthing went wrong during this process. To do so specifiy
 * a parameter<code>mail-to-user=username_in_opencms</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsPublishJob implements I_CmsScheduledJob {

    /** Linkcheck parameter. */
    public static final String PARAM_LINKCHECK = "linkcheck";

    /** Unlock parameter. */
    public static final String PARAM_UNLOCK = "unlock";

    /** Mail to user parameter. */
    public static final String PARAM_USER = "mail-to-user";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishJob.class);

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public synchronized String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        Date jobStart = new Date();
        String finishMessage;
        String unlock = parameters.get(PARAM_UNLOCK);
        String linkcheck = parameters.get(PARAM_LINKCHECK);
        CmsProject project = cms.getRequestContext().getCurrentProject();

        CmsLogReport report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsPublishJob.class);

        try {

            // check if the unlock parameter was given
            if (Boolean.valueOf(unlock).booleanValue()) {
                cms.unlockProject(project.getUuid());
            }

            // validate links if linkcheck parameter was given
            if (Boolean.valueOf(linkcheck).booleanValue()) {
                OpenCms.getPublishManager().validateRelations(
                    cms,
                    OpenCms.getPublishManager().getPublishList(cms),
                    report);
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
                wait(300000);
                beforeLastTime = lastTime;
                lastTime = report.getLastEntryTime();
            }

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