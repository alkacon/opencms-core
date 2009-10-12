/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsTimeShiftPublish.java,v $
 * Date   : $Date: 2009/10/12 08:12:01 $
 * Version: $Revision: 1.3.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.commons;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.jobs.CmsTimeShiftPublishJob;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the resource timeshift publishing dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/timeshiftpublish.jsp
 * </ul>
 * <p>
 *
 * @author Mario Jaeger
 * 
 * @version $Revision: 1.3.2.1 $ 
 * 
 * @since 7.5.0
 */
public class CmsTimeShiftPublish extends CmsDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "timeshiftpublish";

    /** Request parameter name for the activation of the notification. */
    public static final String PARAM_ENABLE_NOTIFICATION = "enablenotification";

    /** Request parameter name for the reset publish date. */
    public static final String PARAM_RESETTIMESHIFTPUBLISH = "resettimeshiftpublish";

    /** Request parameter name for the publish date. */
    public static final String PARAM_TIMESHIFTPUBLISHDATE = "timeshiftpublishdate";

    /** The parameter for time shift publish date. */
    private String m_paramTimeshiftpublishdate;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsTimeShiftPublish(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsTimeShiftPublish(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * 
     * @see org.opencms.workplace.CmsDialog#actionCloseDialog()
     */
    public void actionCloseDialog() throws JspException {

        // so that the explorer will be shown, if dialog is opened from e-mail
        getSettings().getFrameUris().put("body", CmsWorkplace.VFS_PATH_VIEWS + "explorer/explorer_fs.jsp");
        super.actionCloseDialog();
    }

    /**
     * Performs the resource operation, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionUpdate() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);

        try {
            // prepare the time shift publish for the resource
            performDialogOperation();
            // close the dialog
            actionCloseDialog();
        } catch (Throwable e) {
            // show the error page
            includeErrorpage(this, e);
        }
    }

    /**
     * Returns the value of the time shift publish data.<p>
     * 
     * @return the value of the time shift publish data
     */
    public String getParamTimeshiftpublishdate() {

        return m_paramTimeshiftpublishdate;
    }

    /**
     * Sets the title of the dialog.<p>
     * 
     * @param singleKey the key for the single operation
     */
    public void setDialogTitle(String singleKey) {

        // generate title using the resource name as parameter for the key
        String resourceName = CmsStringUtil.formatResourceName(getParamResource(), 50);
        setParamTitle(key(singleKey, new Object[] {resourceName}));
    }

    /**
     * Sets the value of the reset expire parameter.<p>
     * 
     * @param paramTimeshiftpublishdate the value of the time shift publish date
     */
    public void setParamTimeshiftpublishdate(String paramTimeshiftpublishdate) {

        m_paramTimeshiftpublishdate = paramTimeshiftpublishdate;
    }

    /**
     * Returns a localized String for "Group", if the flag of a group ACE, and the localization for "User" otherwise.<p>
     * 
     * @param flags the flags of the ACE
     * 
     * @return localization for "Group", if the flag belongs to a group ACE
     */
    protected String getLocalizedType(int flags) {

        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_USER) > 0) {
            return key(Messages.GUI_LABEL_USER_0);
        } else {
            return key(Messages.GUI_LABEL_GROUP_0);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to modify the resource       
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_OK);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for dialog
            setDialogTitle(Messages.GUI_PUBLISH_SCHEDULED_SETTINGS_1);
        }
    }

    /**
     * Modifies the time shift publish date of a resource. <p>
     * 
     * @return true, if the operation was performed, otherwise false
     *
     * @throws CmsException if something goes wrong
     * @throws ParseException if something goes wrong
     */
    protected boolean performDialogOperation() throws CmsException, ParseException {

        // get the request parameters for resource and time shift publish date
        String resource = getParamResource();
        String timeshiftPublishDate = getParamTimeshiftpublishdate();
        String userName = getCms().getRequestContext().currentUser().getName();

        // get the java date format
        DateFormat dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            getCms().getRequestContext().getLocale());
        Date date = dateFormat.parse(timeshiftPublishDate);

        // check if the selected date is in the future
        if (date.getTime() < new Date().getTime()) {
            // the selected date in in the past, this is not possible
            throw new CmsException(Messages.get().container(Messages.ERR_PUBLISH_SCHEDULED_DATE_IN_PAST_1, date));
        }

        // make copies from the admin cmsobject and the user cmsobject
        // get the admin cms object
        CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
        CmsObject cmsAdmin = action.getCmsAdminObject();
        // get the user cms object
        CmsObject cms = OpenCms.initCmsObject(getCms());

        // set the current user site to the admin cms object
        cmsAdmin.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());

        // create the temporary project, which is deleted after publishing
        String projectName = "timeshiftpublish_" + date + "_" + resource;
        projectName = projectName.replace("/", "_");
        CmsProject tmpProject = cmsAdmin.createProject(
            projectName,
            "Timeshift publish project created for user "
                + cms.getRequestContext().currentUser().getName()
                + " for resource "
                + resource
                + ". Publish time is "
                + date,
            CmsRole.WORKPLACE_USER.getGroupName(),
            CmsRole.PROJECT_MANAGER.getGroupName(),
            CmsProject.PROJECT_TYPE_TEMPORARY);
        // make the project invisible
        tmpProject.setFlags(CmsProject.PROJECT_FLAG_HIDDEN);
        // set project as current project
        cmsAdmin.getRequestContext().setCurrentProject(tmpProject);
        cms.getRequestContext().setCurrentProject(tmpProject);

        // copy the resource to the project
        cmsAdmin.copyResourceToProject(resource);

        // lock the resource in the current project
        CmsLock lock = cms.getLock(resource);
        // prove is current lock from current but not in current project
        if ((lock != null)
            && lock.isOwnedBy(cms.getRequestContext().currentUser())
            && !lock.isOwnedInProjectBy(cms.getRequestContext().currentUser(), cms.getRequestContext().currentProject())) {
            // file is locked by current user but not in current project
            // change the lock from this file
            cms.changeLock(resource);
        }
        // lock resource from current user in current project
        cms.lockResource(resource);
        // get current lock
        lock = cms.getLock(resource);

        // create a new scheduled job
        CmsScheduledJobInfo job = new CmsScheduledJobInfo();
        // the job name
        String jobName = projectName;
        // set the job parameters
        job.setJobName(jobName);
        job.setClassName("org.opencms.scheduler.jobs.CmsTimeShiftPublishJob");
        // create the cron expression
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String cronExpr = ""
            + calendar.get(Calendar.SECOND)
            + " "
            + calendar.get(Calendar.MINUTE)
            + " "
            + calendar.get(Calendar.HOUR_OF_DAY)
            + " "
            + calendar.get(Calendar.DAY_OF_MONTH)
            + " "
            + (calendar.get(Calendar.MONTH) + 1)
            + " "
            + "?"
            + " "
            + calendar.get(Calendar.YEAR);
        // set the cron expression
        job.setCronExpression(cronExpr);
        // set the job active
        job.setActive(true);
        // create the context info
        CmsContextInfo contextInfo = new CmsContextInfo();
        contextInfo.setProjectName(projectName);
        contextInfo.setUserName(cmsAdmin.getRequestContext().currentUser().getName());
        // create the job schedule parameter
        SortedMap params = new TreeMap();
        // the user to send mail to
        params.put(CmsTimeShiftPublishJob.PARAM_USER, userName);
        // the job name
        params.put(CmsTimeShiftPublishJob.PARAM_JOBNAME, jobName);
        // the link check
        params.put(CmsTimeShiftPublishJob.PARAM_LINKCHECK, "true");
        // add the job schedule parameter
        job.setParameters(params);
        // add the context info to the scheduled job
        job.setContextInfo(contextInfo);
        // add the job to the scheduled job list
        OpenCms.getScheduleManager().scheduleJob(cmsAdmin, job);
        return true;
    }
}