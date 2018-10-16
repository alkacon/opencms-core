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

import org.opencms.configuration.CmsSchedulerConfiguration;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.CmsSchedulerException;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.I_CmsCRUDApp;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * The scheduled jobs manager app.<p>
 */
public class CmsJobManagerApp extends A_CmsWorkplaceApp implements I_CmsCRUDApp<CmsScheduledJobInfo> {

    /** Parameter copy. */
    public static final String PARAM_COPY = "copy";

    /** Parameter job id. */
    public static final String PARAM_JOB_ID = "jobId";

    /** Path name edit. */
    public static final String PATH_NAME_EDIT = "edit";

    /** Table containing the jobs. */
    protected CmsJobTable m_jobTable;

    /** The current dialog window. */
    protected Window m_dialogWindow;

    /**
     * Closes the currently opened window.<p>
     *
     * @param updateTable <code>true</code> to update the jobs table
     */
    public void closeDialogWindow(boolean updateTable) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
            m_dialogWindow = null;
        }
        if (updateTable) {
            m_jobTable.reloadJobs();
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#createElement(java.lang.Object)
     */
    public void createElement(CmsScheduledJobInfo element) {

        writeElement(element);

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#defaultAction(java.lang.String)
     */
    public void defaultAction(String elelemntId) {

        openEditDialog(elelemntId, false);

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#deleteElements(java.util.List)
     */
    public void deleteElements(List<String> jobIds) {

        try {
            for (String jobId : jobIds) {
                OpenCms.getScheduleManager().unscheduleJob(A_CmsUI.getCmsObject(), jobId);
            }
            OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);
        } catch (CmsRoleViolationException e) {
            CmsErrorDialog.showErrorDialog(e);
        }

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#getAllElements()
     */
    public List<CmsScheduledJobInfo> getAllElements() {

        return OpenCms.getScheduleManager().getJobs();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#getElement(java.lang.String)
     */
    public CmsScheduledJobInfo getElement(String elementId) {

        return OpenCms.getScheduleManager().getJob(elementId);
    }

    /**
     * Creates the edit view for the given job id.<p>
     *
     * @param jobId the id of the job to edit, or null to create a new job
     * @param copy <code>true</code> to create a copy of the given job
     *
     * @return the edit view
     */
    public CmsJobEditView openEditDialog(String jobId, boolean copy) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
        }
        m_dialogWindow = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsScheduledJobInfo job = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(jobId)) {
            job = getElement(jobId);
        }
        CmsScheduledJobInfo jobCopy;
        if (job == null) {
            jobCopy = new CmsScheduledJobInfo();
            jobCopy.setContextInfo(new CmsContextInfo());
            m_dialogWindow.setCaption(
                CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.tools.scheduler.Messages.GUI_NEWJOB_ADMIN_TOOL_NAME_0));
        } else {
            jobCopy = job.clone();
            jobCopy.setActive(job.isActive());
            if (copy) {
                jobCopy.clearId();
                m_dialogWindow.setCaption(
                    CmsVaadinUtils.getMessageText(
                        org.opencms.ui.Messages.GUI_SCHEDULER_TITLE_COPY_1,
                        job.getJobName()));
            } else {
                m_dialogWindow.setCaption(
                    CmsVaadinUtils.getMessageText(
                        org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0));
            }
        }

        CmsJobEditView editPanel = new CmsJobEditView(this, jobCopy);
        editPanel.loadFromBean(jobCopy);
        m_dialogWindow.setContent(editPanel);
        A_CmsUI.get().addWindow(m_dialogWindow);
        m_dialogWindow.center();

        return editPanel;
    }

    /**
     * Restores the main view after leaving the editing mode.<p>
     */
    public void restoreMainView() {

        openSubView("", true);
    }

    /**
     * Executes the given schedule job.<p>
     *
     * @param job to be executed
     */
    public void runJob(CmsScheduledJobInfo job) {

        CmsScheduleManager scheduler = OpenCms.getScheduleManager();
        scheduler.executeDirectly(job.getId());

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#writeElement(java.lang.Object)
     */
    public void writeElement(CmsScheduledJobInfo jobInfo) {

        // schedule the edited job
        try {
            OpenCms.getScheduleManager().scheduleJob(A_CmsUI.getCmsObject(), jobInfo);
        } catch (CmsRoleViolationException | CmsSchedulerException e) {
            //
        }
        // update the XML configuration
        OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);

    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        crumbs.put(
            "",
            CmsVaadinUtils.getMessageText(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_ADMIN_TOOL_NAME_0));
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        Button addJob = CmsToolBar.createButton(
            FontOpenCms.WAND,
            CmsVaadinUtils.getMessageText(org.opencms.workplace.tools.scheduler.Messages.GUI_NEWJOB_ADMIN_TOOL_NAME_0));
        addJob.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openEditDialog(null, false);
            }
        });
        m_uiContext.addToolbarButton(addJob);
        m_rootLayout.setMainHeightFull(true);
        CmsJobTable table = getJobTable();
        table.reloadJobs();
        return table;
    }

    /**
     * Returns the job table instance.<p>
     *
     * @return the job table instance
     */
    protected CmsJobTable getJobTable() {

        if (m_jobTable == null) {
            m_jobTable = new CmsJobTable(this);
            m_jobTable.setWidth("100%");
        }
        return m_jobTable;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

}
