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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.util.table.CmsTableUtil;
import org.opencms.workplace.CmsWorkplace;

import org.apache.commons.logging.Log;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

/**
 * Table used to display scheduled jobs, together with buttons for modifying the jobs.<p>
 * The columns containing the buttons are implemented as generated columns.
 */
public class CmsJobTable extends Table implements ColumnGenerator {

    /**
     * Enum representing the actions for which buttons exist in the table rows.<p>
     */
    enum Action {
        /** Enable / disable. */
        activation(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_MACTIVATE_NAME_0),

        /** Create new job from template. */
        copy(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_COPY_NAME_0),

        /** Deletes the job. */
        delete(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_DELETE_NAME_0),

        /** Edits the job. */
        /** Message constant for key in the resource bundle. */
        edit(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0),

        /** Executes the job immediately. */
        run(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_EXECUTE_NAME_0);

        /** The message key. */
        private String m_key;

        /**
         * Creates a new action.<p>
         *
         * @param key the message key for the action
         */
        private Action(String key) {
            m_key = key;
        }

        /**
         * Gets the message key for the action.<p>
         *
         * @return the message key
         */
        String getMessageKey() {

            return m_key;
        }
    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJobTable.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Bean container for the table. */
    private BeanItemContainer<CmsJobBean> m_beanContainer = new BeanItemContainer<CmsJobBean>(CmsJobBean.class);

    /**
     * Creates a new instance.<p>
     */
    public CmsJobTable() {

        setContainerDataSource(m_beanContainer);

        setVisibleColumns();
        for (Action action : Action.values()) {
            addGeneratedColumn(action, this);
            setColumnWidth(action, 26);
        }

        setVisibleColumns(
            Action.edit,
            Action.activation,
            Action.copy,
            Action.delete,
            Action.run,
            "name",
            "className",
            "lastExecution",
            "nextExecution");
        setColumnExpandRatio("name", 1);
        setColumnExpandRatio("className", 1);
        setColumnExpandRatio("lastExecution", 1);
        setColumnExpandRatio("nextExecution", 1);
        setSortContainerPropertyId("name");
        getVisibleColumns();

        setColumnHeader(Action.edit, "E");
        setColumnHeader(Action.activation, "A");
        setColumnHeader(Action.copy, "C");
        setColumnHeader(Action.delete, "D");
        setColumnHeader(Action.run, "X");

        setColumnHeader(
            "name",
            CmsVaadinUtils.getMessageText(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_COL_NAME_0));
        setColumnHeader(
            "className",
            CmsVaadinUtils.getMessageText(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_COL_CLASS_0));
        setColumnHeader(
            "lastExecution",
            CmsVaadinUtils.getMessageText(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_COL_LASTEXE_0));
        setColumnHeader(
            "nextExecution",
            CmsVaadinUtils.getMessageText(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_COL_NEXTEXE_0));
    }

    /**
     * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
     */
    public Object generateCell(Table source, Object itemId, Object columnId) {

        final Action action = (Action)columnId;
        CmsJobBean jobBean = (CmsJobBean)itemId;
        final CmsScheduledJobInfo job = jobBean.getJob();
        final CmsScheduledJobInfo jobClone = (CmsScheduledJobInfo)job.clone();

        Resource resource = null;
        String resPath = null;
        switch (action) {
            case activation:
                resource = job.isActive() ? FontOpenCms.CIRCLE_CHECK : FontOpenCms.CIRCLE_PAUSE;
                break;
            case copy:
                resource = FontOpenCms.CIRCLE_PLUS;
                break;
            case delete:
                resource = FontOpenCms.CIRCLE_MINUS;
                break;
            case edit:
                resPath = OpenCmsTheme.getImageLink("scheduler/scheduler.png");
                break;
            case run:
                resource = FontOpenCms.CIRCLE_PLAY;
                break;
            default:
        }
        if ((resource == null) && (resPath != null)) {
            resource = new ExternalResource(resPath);
        }

        Button button = CmsTableUtil.createIconButton(resource, CmsVaadinUtils.getMessageText(action.getMessageKey()));
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                try {
                    switch (action) {
                        case activation:
                            jobClone.setActive(!job.isActive());
                            writeChangedJob(jobClone);
                            break;
                        case copy:
                            String stateCopy = CmsScheduledJobsAppConfig.APP_ID + "/" + CmsJobManagerApp.PATH_NAME_EDIT;
                            stateCopy = A_CmsWorkplaceApp.addParamToState(
                                stateCopy,
                                CmsJobManagerApp.PARAM_JOB_ID,
                                job.getId());
                            stateCopy = A_CmsWorkplaceApp.addParamToState(
                                stateCopy,
                                CmsJobManagerApp.PARAM_COPY,
                                Boolean.TRUE.toString());
                            CmsAppWorkplaceUi.get().getNavigator().navigateTo(stateCopy);
                            //                            jobClone.setActive(job.isActive());
                            //                            jobClone.clearId();
                            //                            String title = CmsVaadinUtils.getMessageText(
                            //                                org.opencms.ui.Messages.GUI_SCHEDULER_TITLE_COPY_1,
                            //                                jobClone.getJobName());
                            //                            m_jobEditHandler.editJob(jobClone, title);

                            break;

                        case delete:
                            CmsConfirmationDialog.show(
                                CmsVaadinUtils.getMessageText(action.getMessageKey()),
                                CmsVaadinUtils.getMessageText(
                                    Messages.GUI_SCHEDULER_CONFIRM_DELETE_1,
                                    job.getJobName()),
                                new Runnable() {

                                    public void run() {

                                        try {
                                            OpenCms.getScheduleManager().unscheduleJob(
                                                A_CmsUI.getCmsObject(),
                                                job.getId());
                                            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
                                            reloadJobs();
                                        } catch (CmsRoleViolationException e) {
                                            CmsErrorDialog.showErrorDialog(e);
                                        }

                                    }
                                });
                            break;

                        case edit:
                            String stateEdit = CmsScheduledJobsAppConfig.APP_ID + "/" + CmsJobManagerApp.PATH_NAME_EDIT;
                            stateEdit = A_CmsWorkplaceApp.addParamToState(
                                stateEdit,
                                CmsJobManagerApp.PARAM_JOB_ID,
                                job.getId());
                            CmsAppWorkplaceUi.get().getNavigator().navigateTo(stateEdit);

                            //                            if (m_jobEditHandler == null) {
                            //                                break;
                            //                            }
                            //                            m_jobEditHandler.editJob(
                            //                                job,
                            //                                CmsVaadinUtils.getMessageText(
                            //                                    org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0));

                            break;
                        case run:
                            CmsConfirmationDialog.show(
                                CmsVaadinUtils.getMessageText(action.getMessageKey()),
                                CmsVaadinUtils.getMessageText(
                                    Messages.GUI_SCHEDULER_CONFIRM_EXECUTE_1,
                                    job.getJobName()),
                                new Runnable() {

                                    public void run() {

                                        CmsScheduleManager scheduler = OpenCms.getScheduleManager();
                                        scheduler.executeDirectly(job.getId());
                                    }
                                });

                            break;
                        default:
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    CmsErrorDialog.showErrorDialog(e, new Runnable() {

                        public void run() {
                            // do nothing

                        }
                    });

                }

            }
        });
        return button;
    }

    /**
     * Reloads the job table data.<p>
     */
    public void reloadJobs() {

        m_beanContainer.removeAllItems();
        for (CmsScheduledJobInfo job : OpenCms.getScheduleManager().getJobs()) {
            m_beanContainer.addBean(new CmsJobBean(job));
        }
        sort();
        refreshRowCache();

    }

    /**
     * Gets the icon resource for the given workplace resource path.<p>
     *
     * @param subPath the path relative to the workplace resources
     *
     * @return the icon resource
     */
    ExternalResource getIconResource(String subPath) {

        String resPath = CmsWorkplace.getResourceUri(subPath);
        return new ExternalResource(resPath);
    }

    /**
     * Writes a job to the configuration and reloads the table.<p>
     *
     * @param jobInfo the job bean
     *
     * @throws CmsException if something goes wrong
     */
    private void writeChangedJob(CmsScheduledJobInfo jobInfo) throws CmsException {

        // schedule the edited job
        OpenCms.getScheduleManager().scheduleJob(A_CmsUI.getCmsObject(), jobInfo);
        // update the XML configuration
        OpenCms.writeConfiguration(CmsSystemConfiguration.class);

        reloadJobs();
    }

}
