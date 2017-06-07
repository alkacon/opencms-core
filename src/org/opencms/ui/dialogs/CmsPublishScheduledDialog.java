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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.dialogs;

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.jobs.CmsPublishScheduledJob;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsDateField;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.commons.Messages;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;

/**
 * The publish schedule dialog.<p>
 */
public class CmsPublishScheduledDialog extends CmsBasicDialog {

    /**
     * Validates the date input.<p>
     */
    class DateValidator implements Validator {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            Date date = (Date)value;
            if (date == null) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_PUBLISH_SCHEDULED_DATEEMPTY_0));
            }
            if (date.getTime() < new Date().getTime()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_PUBLISH_SCHEDULED_DATENOTFUTURE_0));
            }

        }

    }

    /** The serial version id. */
    private static final long serialVersionUID = 7488454443783670970L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishScheduledDialog.class);

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The date selection field. */
    private CmsDateField m_dateField;

    /** The OK button. */
    private Button m_okButton;

    /** The cancel button. */
    private Button m_cancelButton;

    /** Include sub-resources check box. */
    private CheckBox m_includeSubResources;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context
     */
    public CmsPublishScheduledDialog(I_CmsDialogContext context) {
        m_context = context;
        displayResourceInfo(context.getResources());
        FormLayout form = initForm();
        setContent(form);
        m_okButton = new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });
        addButton(m_okButton);
        m_cancelButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }
        });
        addButton(m_cancelButton);

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsPublishScheduledDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });

        m_dateField.addValidator(new DateValidator());

    }

    /**
     * Cancels the dialog action.<p>
     */
    void cancel() {

        m_context.finish(Collections.<CmsUUID> emptyList());
    }

    /**
     * Submits the dialog action.<p>
     */
    void submit() {

        if (!m_dateField.isValid()) {
            return;
        }
        long current = System.currentTimeMillis();
        Date dateValue = m_dateField.getValue();
        long publishTime = m_dateField.getValue().getTime();
        if (current > publishTime) {
            m_context.error(
                new CmsException(Messages.get().container(Messages.ERR_PUBLISH_SCHEDULED_DATE_IN_PAST_1, dateValue)));
        } else {
            try {
                // make copies from the admin cmsobject and the user cmsobject
                // get the admin cms object
                CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
                CmsObject cmsAdmin = action.getCmsAdminObject();
                // get the user cms object
                CmsObject cms = OpenCms.initCmsObject(m_context.getCms());

                // set the current user site to the admin cms object
                cmsAdmin.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
                CmsProject tmpProject = createTempProject(cmsAdmin, m_context.getResources(), dateValue);
                // set project as current project
                cmsAdmin.getRequestContext().setCurrentProject(tmpProject);
                cms.getRequestContext().setCurrentProject(tmpProject);

                Set<CmsUUID> changeIds = new HashSet<CmsUUID>();
                for (CmsResource resource : m_context.getResources()) {
                    addToTempProject(cmsAdmin, cms, resource, tmpProject);
                    if (resource.isFolder() && m_includeSubResources.getValue().booleanValue()) {
                        List<CmsResource> subResources = cms.readResources(
                            resource,
                            CmsResourceFilter.ONLY_VISIBLE.addExcludeState(CmsResourceState.STATE_UNCHANGED),
                            true);
                        for (CmsResource sub : subResources) {
                            // check publish permissions on sub resource
                            if (cms.hasPermissions(
                                sub,
                                CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                                false,
                                CmsResourceFilter.ALL)) {
                                addToTempProject(cmsAdmin, cms, sub, tmpProject);
                            }
                        }
                    }

                    changeIds.add(resource.getStructureId());
                }
                // create a new scheduled job
                CmsScheduledJobInfo job = new CmsScheduledJobInfo();
                // the job name
                String jobName = tmpProject.getName();
                jobName = jobName.replace("&#47;", "/");
                // set the job parameters
                job.setJobName(jobName);
                job.setClassName("org.opencms.scheduler.jobs.CmsPublishScheduledJob");
                // create the cron expression
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateValue);
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
                contextInfo.setProjectName(tmpProject.getName());
                contextInfo.setUserName(cmsAdmin.getRequestContext().getCurrentUser().getName());
                // create the job schedule parameter
                SortedMap<String, String> params = new TreeMap<String, String>();
                // the user to send mail to
                String userName = m_context.getCms().getRequestContext().getCurrentUser().getName();
                params.put(CmsPublishScheduledJob.PARAM_USER, userName);
                // the job name
                params.put(CmsPublishScheduledJob.PARAM_JOBNAME, jobName);
                // the link check
                params.put(CmsPublishScheduledJob.PARAM_LINKCHECK, "true");
                // add the job schedule parameter
                job.setParameters(params);
                // add the context info to the scheduled job
                job.setContextInfo(contextInfo);
                // add the job to the scheduled job list
                OpenCms.getScheduleManager().scheduleJob(cmsAdmin, job);
                // update the XML configuration
                OpenCms.writeConfiguration(CmsSystemConfiguration.class);
                m_context.finish(changeIds);
            } catch (CmsException ex) {
                LOG.error("Error performing publish scheduled dialog operation.", ex);
                m_context.error(ex);
            }
        }

    }

    /**
     * Adds the given resource to the temporary project.<p>
     *
     * @param adminCms the admin cms context
     * @param userCms the user cms context
     * @param resource the resource
     * @param tmpProject the temporary project
     * @throws CmsException in case something goes wrong
     */
    private void addToTempProject(CmsObject adminCms, CmsObject userCms, CmsResource resource, CmsProject tmpProject)
    throws CmsException {

        // copy the resource to the project
        adminCms.copyResourceToProject(resource);

        // lock the resource in the current project
        CmsLock lock = userCms.getLock(resource);
        // prove is current lock from current but not in current project
        if ((lock != null)
            && lock.isOwnedBy(userCms.getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                userCms.getRequestContext().getCurrentUser(),
                userCms.getRequestContext().getCurrentProject())) {
            // file is locked by current user but not in current project
            // change the lock from this file
            userCms.changeLock(resource);
        }
        // lock resource from current user in current project
        userCms.lockResource(resource);
        // get current lock
        lock = userCms.getLock(resource);
    }

    /**
     * Creates the publish project's name for a given root path and publish date.<p>
     *
     * @param rootPath the publish resource's root path
     * @param date the publish date
     *
     * @return the publish project name
     */
    private String computeProjectName(String rootPath, Date date) {

        // create the temporary project, which is deleted after publishing
        // the publish scheduled date in project name
        String dateTime = CmsDateUtil.getDateTime(date, DateFormat.SHORT, getLocale());
        // the resource name to publish scheduled
        String projectName = CmsVaadinUtils.getMessageText(
            Messages.GUI_PUBLISH_SCHEDULED_PROJECT_NAME_2,
            rootPath,
            dateTime);
        // the HTML encoding for slashes is necessary because of the slashes in english date time format
        // in project names slahes are not allowed, because these are separators for organizaional units
        projectName = projectName.replace("/", "&#47;");
        while (projectName.length() > 190) {
            rootPath = "..." + rootPath.substring(5, rootPath.length());
            projectName = computeProjectName(rootPath, date);
        }
        return projectName;
    }

    /**
     * Creates the temporary project.<p>
     *
     * @param adminCms the admin cms context
     * @param resources the resources
     * @param date the publish date
     *
     * @return the project
     *
     * @throws CmsException in case writing the project fails
     */
    private CmsProject createTempProject(CmsObject adminCms, List<CmsResource> resources, Date date)
    throws CmsException {

        CmsProject tmpProject;

        String rootPath = resources.get(0).getRootPath();
        if (resources.size() > 1) {
            rootPath = CmsResource.getParentFolder(rootPath);
        }
        String projectName = computeProjectName(rootPath, date);

        try {
            // create the project
            tmpProject = adminCms.createProject(
                projectName,
                "",
                CmsRole.WORKPLACE_USER.getGroupName(),
                CmsRole.PROJECT_MANAGER.getGroupName(),
                CmsProject.PROJECT_TYPE_TEMPORARY);
        } catch (CmsException e) {
            String resName = CmsResource.getName(rootPath);
            if (resName.length() > 64) {
                resName = resName.substring(0, 64) + "...";
            }
            // use UUID to make sure the project name is still unique
            projectName = computeProjectName(resName, date) + " [" + new CmsUUID() + "]";
            // create the project
            tmpProject = adminCms.createProject(
                projectName,
                "",
                CmsRole.WORKPLACE_USER.getGroupName(),
                CmsRole.PROJECT_MANAGER.getGroupName(),
                CmsProject.PROJECT_TYPE_TEMPORARY);
        }
        // make the project invisible for all users
        tmpProject.setHidden(true);
        // write the project to the database
        adminCms.writeProject(tmpProject);
        return tmpProject;
    }

    /**
     * Checks whether the resources list contains any folders.<p>
     *
     * @return <code>true</code> if the resources list contains any folders
     */
    private boolean hasFolders() {

        for (CmsResource resource : m_context.getResources()) {
            if (resource.isFolder()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes the form fields.<p>
     *
     * @return the form component
     */
    private FormLayout initForm() {

        FormLayout form = new FormLayout();
        form.setWidth("100%");
        m_dateField = new CmsDateField();
        m_dateField.setCaption(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_DATE_PUBLISH_SCHEDULED_0));
        form.addComponent(m_dateField);
        m_includeSubResources = new CheckBox(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_PUBLISH_MULTI_SUBRESOURCES_0));
        if (hasFolders()) {
            form.addComponent(m_includeSubResources);
        }

        return form;
    }
}
