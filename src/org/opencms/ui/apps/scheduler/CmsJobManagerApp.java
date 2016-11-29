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

package org.opencms.ui.apps.scheduler;

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

/**
 * The scheduled jobs manager app.<p>
 */
public class CmsJobManagerApp extends A_CmsWorkplaceApp {

    /** Parameter copy. */
    public static final String PARAM_COPY = "copy";

    /** Parameter job id. */
    public static final String PARAM_JOB_ID = "jobId";

    /** Path name edit. */
    public static final String PATH_NAME_EDIT = "edit";

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJobManagerApp.class);

    /** Table containing the jobs. */
    protected CmsJobTable m_jobTable;

    /**
     * Restores the main view after leaving the editing mode.<p>
     */
    public void restoreMainView() {

        openSubView("", true);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put(
                "",
                CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_ADMIN_TOOL_NAME_0));
        } else if (state.startsWith(PATH_NAME_EDIT)) {
            crumbs.put(
                CmsScheduledJobsAppConfig.APP_ID,
                CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_ADMIN_TOOL_NAME_0));
            String viewName;
            String jobId = A_CmsWorkplaceApp.getParamFromState(state, "jobId");
            CmsScheduledJobInfo info = OpenCms.getScheduleManager().getJob(jobId);
            if (info == null) {
                viewName = CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.tools.scheduler.Messages.GUI_NEWJOB_ADMIN_TOOL_NAME_0);
            } else if (Boolean.valueOf(A_CmsWorkplaceApp.getParamFromState(state, PARAM_COPY)).booleanValue()) {
                viewName = CmsVaadinUtils.getMessageText(
                    org.opencms.ui.Messages.GUI_SCHEDULER_TITLE_COPY_1,
                    info.getJobName());
            } else {
                viewName = CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0);
            }

            crumbs.put("", viewName);
        }
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            m_rootLayout.setMainHeightFull(true);
            CmsJobTable table = getJobTable();
            table.reloadJobs();
            return table;
        } else if (state.startsWith(PATH_NAME_EDIT)) {
            m_rootLayout.setMainHeightFull(false);
            String jobId = A_CmsWorkplaceApp.getParamFromState(state, PARAM_JOB_ID);
            String copyMode = A_CmsWorkplaceApp.getParamFromState(state, PARAM_COPY);
            return getJobEditView(jobId, Boolean.valueOf(copyMode).booleanValue());
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            List<NavEntry> subNav = new ArrayList<NavEntry>();
            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(
                        org.opencms.workplace.tools.scheduler.Messages.GUI_NEWJOB_ADMIN_TOOL_NAME_0),
                    CmsVaadinUtils.getMessageText(
                        org.opencms.workplace.tools.scheduler.Messages.GUI_NEWJOB_ADMIN_TOOL_HELP_0),
                    new ExternalResource(OpenCmsTheme.getImageLink("scheduler/scheduler_big_add.png")),
                    PATH_NAME_EDIT));
            return subNav;
        }
        return null;
    }

    /**
     * Creates the edit view for the given job id.<p>
     *
     * @param jobId the id of the job to edit, or null to create a new job
     * @param copy <code>true</code> to create a copy of the given job
     *
     * @return the edit view
     */
    private CmsJobEditView getJobEditView(String jobId, boolean copy) {

        CmsScheduledJobInfo job = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(jobId)) {
            job = OpenCms.getScheduleManager().getJob(jobId);
        }

        if (job == null) {
            job = new CmsScheduledJobInfo();
            job.setContextInfo(new CmsContextInfo());
        }
        final CmsScheduledJobInfo jobCopy = (CmsScheduledJobInfo)job.clone();
        jobCopy.setActive(job.isActive());
        if (copy) {
            jobCopy.clearId();
        }
        final CmsJobEditView editPanel = new CmsJobEditView(jobCopy);
        editPanel.loadFromBean(jobCopy);

        Button saveButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));

        Button cancelButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        editPanel.setButtons(saveButton, cancelButton);
        saveButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings({"synthetic-access"})
            public void buttonClick(ClickEvent event) {

                try {
                    if (editPanel.trySaveToBean()) {
                        OpenCms.getScheduleManager().scheduleJob(A_CmsUI.getCmsObject(), jobCopy);
                        OpenCms.writeConfiguration(CmsSystemConfiguration.class);
                        restoreMainView();
                    }

                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    CmsErrorDialog.showErrorDialog(e, new Runnable() {

                        public void run() {

                            restoreMainView();

                        }
                    });
                }

            }

        });

        cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                restoreMainView();

            }
        });
        return editPanel;
    }

    /**
     * Returns the job table instance.<p>
     *
     * @return the job table instance
     */
    private CmsJobTable getJobTable() {

        if (m_jobTable == null) {
            m_jobTable = new CmsJobTable();
            m_jobTable.setWidth("100%");
        }
        return m_jobTable;
    }

}
