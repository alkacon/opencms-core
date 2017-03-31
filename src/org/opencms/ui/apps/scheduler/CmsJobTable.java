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
import org.opencms.ui.Messages;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Image;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.themes.ValoTheme;

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

    /**
     * The activate job context menu entry.<p>
     */
    class ActivateEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String jobId = data.iterator().next();
            CmsScheduledJobInfo job = (((Set<CmsJobBean>)getValue()).iterator().next()).getJob();
            CmsScheduledJobInfo jobClone = (CmsScheduledJobInfo)job.clone();
            jobClone.setActive(!job.isActive());
            try {
                writeChangedJob(jobClone);
            } catch (CmsException e) {
                LOG.error("Error on activate job with id " + jobId, e);
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Action.activation.getMessageKey());
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return (data != null) && (data.size() == 1)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * The copy job context menu entry.<p>
     */
    class CopyEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String stateCopy = CmsScheduledJobsAppConfig.APP_ID + "/" + CmsJobManagerApp.PATH_NAME_EDIT;
            stateCopy = A_CmsWorkplaceApp.addParamToState(
                stateCopy,
                CmsJobManagerApp.PARAM_JOB_ID,
                data.iterator().next());
            stateCopy = A_CmsWorkplaceApp.addParamToState(
                stateCopy,
                CmsJobManagerApp.PARAM_COPY,
                Boolean.TRUE.toString());
            CmsAppWorkplaceUi.get().getNavigator().navigateTo(stateCopy);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Action.copy.getMessageKey());
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return (data != null) && (data.size() == 1)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * The delete job context menu entry.<p>
     */
    class DeleteEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String jobNames = "";
            final List<String> jobIds = new ArrayList<String>();

            for (CmsJobBean job : (Set<CmsJobBean>)getValue()) {
                jobIds.add(job.getJob().getId());
                jobNames += job.getName() + ", ";
            }
            if (!jobNames.isEmpty()) {
                jobNames = jobNames.substring(0, jobNames.length() - 2);
            }

            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(Action.delete.getMessageKey()),
                CmsVaadinUtils.getMessageText(Messages.GUI_SCHEDULER_CONFIRM_DELETE_1, jobNames),
                new Runnable() {

                    public void run() {

                        try {
                            for (String jobId : jobIds) {
                                OpenCms.getScheduleManager().unscheduleJob(A_CmsUI.getCmsObject(), jobId);
                            }
                            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
                            reloadJobs();
                        } catch (CmsRoleViolationException e) {
                            CmsErrorDialog.showErrorDialog(e);
                        }

                    }
                });
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Action.delete.getMessageKey());
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return (data != null) && (data.size() > 0)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * The edit job context menu entry.<p>
     */
    class EditEntry implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            editJob(data.iterator().next());
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry.I_HasCssStyles#getStyles()
         */
        public String getStyles() {

            return ValoTheme.LABEL_BOLD;
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Action.edit.getMessageKey());
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return (data != null) && (data.size() == 1)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * The delete job context menu entry.<p>
     */
    class RunEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            final CmsScheduledJobInfo job = ((Set<CmsJobBean>)getValue()).iterator().next().getJob();

            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(Action.run.getMessageKey()),
                CmsVaadinUtils.getMessageText(Messages.GUI_SCHEDULER_CONFIRM_EXECUTE_1, job.getJobName()),
                new Runnable() {

                    public void run() {

                        CmsScheduleManager scheduler = OpenCms.getScheduleManager();
                        scheduler.executeDirectly(job.getId());
                    }
                });
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Action.run.getMessageKey());
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return (data != null) && (data.size() == 1)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * Property columns of table, including their Messages for header.<p>
     */
    enum TableProperty {

        /**
         * Class name column.
         */
        className("className", org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_COL_CLASS_0),

        /**
         * icon column.
         */
        icon("icon", null),

        /**
         * last execution date column.
         */
        lastExecution("lastExecution", org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_COL_LASTEXE_0),

        /**
         * Name column.
         */
        name("name", org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_COL_NAME_0),

        /**
         * next execution date column.
         */
        nextExecution("nextExecution", org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_COL_NEXTEXE_0);

        /**Message for the header.*/
        private String m_header;

        /**Name of column.*/
        private String m_name;

        /**
         * private constructor.<p>
         *
         * @param propName name of property
         * @param header message
         */
        private TableProperty(String propName, String header) {
            m_header = header;
            m_name = propName;
        }

        /**
         * returns property from it's name used for table column ids.<p>
         *
         * @param propName to looked up
         * @return the TableProperty
         */
        static TableProperty get(String propName) {

            for (TableProperty prop : TableProperty.values()) {
                if (prop.toString().equals(propName)) {
                    return prop;
                }
            }
            return null;
        }

        /**
         * Returns all columns with header.<p>
         *
         * @return set of TableProperty
         */
        static Set<TableProperty> withHeader() {

            Set<TableProperty> ret = new HashSet<TableProperty>();

            for (TableProperty prop : TableProperty.values()) {
                if (prop.getMessageKey() != null) {
                    ret.add(prop);
                }
            }
            return ret;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            return m_name;
        }

        /**
         * Returns the message key.<p>
         *
         * @return message
         */
        String getMessageKey() {

            return m_header;
        }

    }

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsJobTable.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Bean container for the table. */
    private BeanItemContainer<CmsJobBean> m_beanContainer = new BeanItemContainer<CmsJobBean>(CmsJobBean.class);

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * Creates a new instance.<p>
     */
    public CmsJobTable() {
        setContainerDataSource(m_beanContainer);

        setVisibleColumns();

        addGeneratedColumn(TableProperty.icon.toString(), this);
        setColumnWidth(TableProperty.icon.toString(), 40);
        setColumnHeader(TableProperty.icon.toString(), "");

        setVisibleColumns(
            TableProperty.icon.toString(),
            TableProperty.className.toString(),
            TableProperty.name.toString(),
            TableProperty.lastExecution.toString(),
            TableProperty.nextExecution.toString());

        for (TableProperty prop : TableProperty.withHeader()) {
            setColumnExpandRatio(prop.toString(), 1);
            setColumnHeader(prop.toString(), CmsVaadinUtils.getMessageText(prop.getMessageKey()));
        }
        setSortContainerPropertyId(TableProperty.name.toString());
        getVisibleColumns();
        setSelectable(true);
        setMultiSelect(true);
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = -4738296706762013443L;

            public void itemClick(ItemClickEvent event) {

                onItemClick(event, event.getItemId(), event.getPropertyId());
            }
        });

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (TableProperty.className.toString().equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }
                CmsScheduledJobInfo job = ((BeanItem<CmsJobBean>)source.getItem(itemId)).getBean().getJob();
                if (TableProperty.name.toString().equals(propertyId) & job.isActive()) {
                    return " " + OpenCmsTheme.IN_NAVIGATION;
                }
                return null;
            }
        });

    }

    /**
     * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
     */
    public Object generateCell(Table source, final Object itemId, Object columnId) {

        Image image = new Image(
            String.valueOf(System.currentTimeMillis()),
            new ExternalResource(OpenCmsTheme.getImageLink("apps/scheduler.png")));

        image.addClickListener(new ClickListener() {

            /**vaadin serial id.*/
            private static final long serialVersionUID = 8006490459561309427L;

            public void click(ClickEvent event) {

                onItemClick(event, itemId, TableProperty.icon.toString());
            }
        });

        return image;
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
     * Calls the edit formular to edit a job.<p>
     *
     * @param jobId to be edited.
     */
    void editJob(String jobId) {

        String stateEdit = CmsScheduledJobsAppConfig.APP_ID + "/" + CmsJobManagerApp.PATH_NAME_EDIT;
        stateEdit = A_CmsWorkplaceApp.addParamToState(stateEdit, CmsJobManagerApp.PARAM_JOB_ID, jobId);
        CmsAppWorkplaceUi.get().getNavigator().navigateTo(stateEdit);
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
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new EditEntry());
            m_menuEntries.add(new ActivateEntry());
            m_menuEntries.add(new CopyEntry());
            m_menuEntries.add(new DeleteEntry());
            m_menuEntries.add(new RunEntry());
        }
        return m_menuEntries;
    }

    /**
     * Handles the table item clicks, including clicks on images inside of a table item.<p>
     *
     * @param event the click event
     * @param itemId of the clicked row
     * @param propertyId column id
     */
    void onItemClick(MouseEvents.ClickEvent event, Object itemId, Object propertyId) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {
            changeValueIfNotMultiSelect(itemId);
            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == TableProperty.icon.toString())) {
                Set<String> jobIds = new HashSet<String>();
                for (CmsJobBean job : (Set<CmsJobBean>)getValue()) {
                    jobIds.add(job.getJob().getId());
                }
                m_menu.setEntries(getMenuEntries(), jobIds);
                m_menu.openForTable(event, itemId, propertyId, this);
            } else if (event.getButton().equals(MouseButton.LEFT)
                && TableProperty.className.toString().equals(propertyId)) {

                String jobId = ((Set<CmsJobBean>)getValue()).iterator().next().getJob().getId();
                editJob(jobId);
            }
        }
    }

    /**
     * Writes a job to the configuration and reloads the table.<p>
     *
     * @param jobInfo the job bean
     *
     * @throws CmsException if something goes wrong
     */
    void writeChangedJob(CmsScheduledJobInfo jobInfo) throws CmsException {

        // schedule the edited job
        OpenCms.getScheduleManager().scheduleJob(A_CmsUI.getCmsObject(), jobInfo);
        // update the XML configuration
        OpenCms.writeConfiguration(CmsSystemConfiguration.class);

        reloadJobs();
    }

    /**
     * Checks value of table and sets it new if needed:<p>
     * if multiselect: new itemId is in current Value? -> no change of value<p>
     * no multiselect and multiselect, but new item not selected before: set value to new item<p>
     *
     * @param itemId if of clicked item
     */
    private void changeValueIfNotMultiSelect(Object itemId) {

        @SuppressWarnings("unchecked")
        Set<String> value = (Set<String>)getValue();
        if (value == null) {
            select(itemId);
        } else if (!value.contains(itemId)) {
            setValue(null);
            select(itemId);
        }
    }

}
