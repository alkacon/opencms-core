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

import org.opencms.main.CmsLog;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.event.MouseEvents;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Table used to display scheduled jobs, together with buttons for modifying the jobs.<p>
 * The columns containing the buttons are implemented as generated columns.
 */
public class CmsJobTable extends Table {

    /**
     * Property columns of table, including their Messages for header.<p>
     */
    protected enum TableProperty {

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

    /**
     * Enum representing the actions for which buttons exist in the table rows.<p>
     */
    enum Action {
        /** Enable / disable. */
        activation(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_MACTIVATE_NAME_0,
        org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_MDEACTIVATE_NAME_0),

        /** Create new job from template. */
        copy(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_COPY_NAME_0, ""),

        /** Deletes the job. */
        delete(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_DELETE_NAME_0, ""),

        /** Edits the job. */
        /** Message constant for key in the resource bundle. */
        edit(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0, ""),

        /** Executes the job immediately. */
        run(org.opencms.workplace.tools.scheduler.Messages.GUI_JOBS_LIST_ACTION_EXECUTE_NAME_0, "");

        /** The message key. */
        private String m_key;

        /** The message key for activated case.*/
        private String m_keyActivated;

        /**
         * Creates a new action.<p>
         *
         * @param key the message key for the action
         * @param activatedKey an (optional) message key
         */
        private Action(String key, String activatedKey) {

            m_key = key;
            m_keyActivated = activatedKey;
        }

        /**
         * Returns an activated key.
         *
         * @return a message key
         */
        String getActivatedMessageKey() {

            return CmsStringUtil.isEmptyOrWhitespaceOnly(m_keyActivated) ? m_key : m_keyActivated;
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

            CmsScheduledJobInfo job = (((Set<CmsJobBean>)getValue()).iterator().next()).getJob();
            CmsScheduledJobInfo jobClone = job.clone();
            jobClone.setActive(!job.isActive());

            m_manager.writeElement(jobClone);
            reloadJobs();

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

            if ((data == null) || (data.size() > 1) || (m_manager.getElement(data.iterator().next()) == null)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            @SuppressWarnings("unchecked")
            CmsScheduledJobInfo job = (((Set<CmsJobBean>)getValue()).iterator().next()).getJob();

            return !job.isActive()
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

            m_manager.openEditDialog(data.iterator().next(), true);
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

            return (data != null) && (data.size() == 1) && (m_manager.getElement(data.iterator().next()) != null)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * The activate job context menu entry.<p>
     */
    class DeActivateEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            CmsScheduledJobInfo job = (((Set<CmsJobBean>)getValue()).iterator().next()).getJob();
            CmsScheduledJobInfo jobClone = job.clone();
            jobClone.setActive(!job.isActive());

            m_manager.writeElement(jobClone);
            reloadJobs();

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Action.activation.getActivatedMessageKey());
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if ((data == null) || (data.size() > 1) || (m_manager.getElement(data.iterator().next()) == null)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            @SuppressWarnings("unchecked")
            CmsScheduledJobInfo job = (((Set<CmsJobBean>)getValue()).iterator().next()).getJob();

            return job.isActive()
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
        @SuppressWarnings("unchecked")
        public void executeAction(Set<String> data) {

            String jobNames = "";
            final List<String> jobIds = new ArrayList<String>();
            List<CmsResourceInfo> jobInfos = new ArrayList<CmsResourceInfo>();
            for (CmsJobBean job : (Set<CmsJobBean>)getValue()) {
                jobIds.add(job.getJob().getId());
                jobNames += job.getName() + ", ";
                jobInfos.add(getJobInfo(job.getName(), job.getClassName()));
            }
            if (!jobNames.isEmpty()) {
                jobNames = jobNames.substring(0, jobNames.length() - 2);
            }

            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(Action.delete.getMessageKey()),
                CmsVaadinUtils.getMessageText(Messages.GUI_SCHEDULER_CONFIRM_DELETE_1, jobNames),
                new Runnable() {

                    public void run() {

                        m_manager.deleteElements(jobIds);
                        reloadJobs();
                    }
                }).displayResourceInfoDirectly(jobInfos);
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

            return (data != null) && (data.size() > 0) && (m_manager.getElement(data.iterator().next()) != null)
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

            m_manager.openEditDialog(data.iterator().next(), false);
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

            return (data != null) && (data.size() == 1) && (m_manager.getElement(data.iterator().next()) != null)
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

            @SuppressWarnings("unchecked")
            final CmsScheduledJobInfo job = ((Set<CmsJobBean>)getValue()).iterator().next().getJob();

            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(Action.run.getMessageKey()),
                CmsVaadinUtils.getMessageText(Messages.GUI_SCHEDULER_CONFIRM_EXECUTE_1, job.getJobName()),
                new Runnable() {

                    public void run() {

                        m_manager.runJob(job);
                    }
                }).displayResourceInfoDirectly(
                    Collections.singletonList(getJobInfo(job.getJobName(), job.getClassName())));
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

            return (data != null) && (data.size() == 1) && (m_manager.getElement(data.iterator().next()) != null)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsJobTable.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The job manager instance. */
    public CmsJobManagerApp m_manager;

    /** Bean container for the table. */
    protected BeanItemContainer<CmsJobBean> m_beanContainer;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * Creates a new instance.<p>
     *
     * @param manager the job manager instance
     */
    public CmsJobTable(CmsJobManagerApp manager) {

        m_manager = manager;
        m_beanContainer = new BeanItemContainer<CmsJobBean>(CmsJobBean.class);
        setContainerDataSource(m_beanContainer);
        setVisibleColumns(
            TableProperty.className.toString(),
            TableProperty.name.toString(),
            TableProperty.lastExecution.toString(),
            TableProperty.nextExecution.toString());
        setItemIconPropertyId(TableProperty.icon.toString());
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setColumnWidth(null, 40);

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
                @SuppressWarnings("unchecked")
                CmsScheduledJobInfo job = ((BeanItem<CmsJobBean>)source.getItem(itemId)).getBean().getJob();
                if (TableProperty.name.toString().equals(propertyId) & job.isActive()) {
                    return " " + OpenCmsTheme.IN_NAVIGATION;
                }
                return null;
            }
        });

    }

    /**
     * Returns the resource info box to the given job.<p>
     *
     * @param name the job name
     * @param className the job class
     *
     * @return the info box component
     */
    public static CmsResourceInfo getJobInfo(String name, String className) {

        return new CmsResourceInfo(name, className, new CmsCssIcon(OpenCmsTheme.ICON_JOB));
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    public List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new EditEntry());
            m_menuEntries.add(new ActivateEntry());
            m_menuEntries.add(new DeActivateEntry());
            m_menuEntries.add(new CopyEntry());
            m_menuEntries.add(new DeleteEntry());
            m_menuEntries.add(new RunEntry());
        }
        return m_menuEntries;
    }

    /**
     * Reloads the job table data.<p>
     */
    public void reloadJobs() {

        m_beanContainer.removeAllItems();
        for (CmsScheduledJobInfo job : m_manager.getAllElements()) {
            m_beanContainer.addBean(new CmsJobBean(job));
        }
        sort();
        refreshRowCache();

    }

    /**
     * Sets the menu entries.<p>
     *
     * @param newEntries to be set
     */
    public void setMenuEntries(List<I_CmsSimpleContextMenuEntry<Set<String>>> newEntries) {

        m_menuEntries = newEntries;
    }

    /**
     * Calls the edit formular to edit a job.<p>
     *
     * @param jobId to be edited.
     */
    void editJob(String jobId) {

        String stateEdit = CmsJobManagerApp.PATH_NAME_EDIT;
        stateEdit = A_CmsWorkplaceApp.addParamToState(stateEdit, CmsJobManagerApp.PARAM_JOB_ID, jobId);
        CmsAppWorkplaceUi.get().showApp(CmsScheduledJobsAppConfig.APP_ID, stateEdit);
    }

    /**
     * Handles the table item clicks, including clicks on images inside of a table item.<p>
     *
     * @param event the click event
     * @param itemId of the clicked row
     * @param propertyId column id
     */
    @SuppressWarnings("unchecked")
    void onItemClick(MouseEvents.ClickEvent event, Object itemId, Object propertyId) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {
            changeValueIfNotMultiSelect(itemId);
            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == null)) {
                Set<String> jobIds = new HashSet<String>();
                for (CmsJobBean job : (Set<CmsJobBean>)getValue()) {
                    jobIds.add(job.getJob().getId());
                }
                m_menu.setEntries(getMenuEntries(), jobIds);
                m_menu.openForTable(event, itemId, propertyId, this);
            } else if (event.getButton().equals(MouseButton.LEFT)
                && TableProperty.className.toString().equals(propertyId)) {

                String jobId = ((Set<CmsJobBean>)getValue()).iterator().next().getJob().getId();
                m_manager.defaultAction(jobId);
            }
        }
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
