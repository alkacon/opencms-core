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

package org.opencms.ui.apps.publishqueue;

import org.opencms.db.CmsPublishList;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobBase;
import org.opencms.publish.CmsPublishJobEnqueued;
import org.opencms.publish.CmsPublishJobFinished;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.event.MouseEvents;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Class for Vaadin Table showing history queue elements.<p>
 */
public class CmsQueuedTable extends Table {

    /**
     * Row bean for the bean item container.
     */
    public class Row {

        /** The underlying job. */
        private CmsPublishJobBase m_job;

        /** Cached list of resources. */
        private List<?> m_resourceList;

        /** The icon. */
        private Resource m_icon;

        /** Cached state. */
        private String m_state;

        /** Used for initial sorting. */
        private int m_sortType;

        /**
         * Creates a new instance.
         *
         * @param job the underlying publish job
         */
        public Row(CmsPublishJobBase job, int sortType) {

            m_job = job;
            m_sortType = sortType;
            if (job == null) {
                throw new IllegalArgumentException("Job must not be null.");
            }
        }

        /**
         * Gets the file count.
         *
         * @return the file count
         */
        public int getFilesCount() {

            return m_job.getSize();
        }

        /**
         * Gets the icon.
         *
         * @return the icon
         */
        public Resource getIcon() {

            if (m_icon == null) {
                m_icon = new CmsCssIcon(OpenCmsTheme.ICON_PUBLISH);
            }
            return m_icon;
        }

        /**
         * Gets the publish job.
         *
         * @return the publish job
         */
        public CmsPublishJobBase getJob() {

            return m_job;
        }

        /**
         * Gets the project name.
         *
         * @return the project name
         */
        public String getProject() {

            return m_job.getProjectName().replace("&#47;", "/");
        }

        /**
         * Gets the resource list.
         *
         * @return the resource list
         */
        public List<?> getResourceList() {

            if (m_resourceList == null) {
                if (m_job instanceof CmsPublishJobFinished) {
                    try {
                        m_resourceList = A_CmsUI.getCmsObject().readPublishedResources(m_job.getPublishHistoryId());
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                } else if (m_job instanceof CmsPublishJobEnqueued) {
                    m_resourceList = ((CmsPublishJobEnqueued)m_job).getOriginalPublishList().getAllResources();
                } else if (m_job instanceof CmsPublishJobRunning) {
                    CmsPublishList publishList = ((CmsPublishJobRunning)m_job).getOriginalPublishList();
                    if (publishList == null) {
                        // publish job has already finished, can't get the publish list
                        m_resourceList = Collections.emptyList();
                    } else {
                        m_resourceList = publishList.getAllResources();
                    }
                }
            }
            return m_resourceList;
        }

        /**
         * Gets the resource list as a string.
         *
         * @return the resource list string representation
         */
        public String getResources() {

            List<?> resources = getResourceList();
            if (resources == null) {
                return "";
            }
            return CmsResourcesCellGenerator.formatResourcesForTable(resources, 50);
        }

        /**
         * Used for initial sorting.
         *
         * @return the sort type
         */
        public int getSortType() {

            return m_sortType;
        }

        /**
         * Gets the start date of the job.
         *
         * @return the start date
         */
        public Date getStart() {

            if (m_job instanceof CmsPublishJobFinished) {
                return new Date(((CmsPublishJobFinished)m_job).getStartTime());
            } else if (m_job instanceof CmsPublishJobRunning) {
                return new Date(((CmsPublishJobRunning)m_job).getStartTime());
            }
            return null;
        }

        /**
         * Gets the job state.
         *
         * @return the job state
         */
        @SuppressWarnings("synthetic-access")
        public String getStatus() {

            if (m_state == null) {
                if (m_job instanceof CmsPublishJobFinished) {
                    m_state = getState((CmsPublishJobFinished)m_job);
                } else if (m_job instanceof CmsPublishJobRunning) {
                    m_state = STATE_RUNNING;
                } else if (m_job instanceof CmsPublishJobEnqueued) {
                    m_state = STATE_ENQUEUE;
                } else {
                    m_state = STATE_ERROR;
                    LOG.error("Invalid job type: " + m_job);
                }
            }
            return m_state;
        }

        /**
         * Gets the user friendly label for the state.
         *
         * @return the user friendly state description
         */
        @SuppressWarnings("synthetic-access")
        public String getStatusLocale() {

            String state = getStatus();
            String key = STATUS_MESSAGES.get(state);
            if (key == null) {
                LOG.error("KEY is null for state " + state);
            }
            return CmsVaadinUtils.getMessageText(key);
        }

        /**
         * Gets the stop date for the job.
         *
         * @return the stop date
         */
        public Date getStop() {

            if (m_job instanceof CmsPublishJobFinished) {
                return new Date(((CmsPublishJobFinished)m_job).getFinishTime());
            }
            return null;
        }

        /**
         * Gets the name of the user who started the job.
         *
         * @return the user name for the job
         */
        public String getUser() {

            return m_job.getUserName(A_CmsUI.getCmsObject());
        }

    }

    /**
     *Menu entry for showing report.<p>
     */
    class EntryReport implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            showReportDialog(data.iterator().next());
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_REPORT_0);
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
     * Menu entry for showing resources.<p>
     */
    class EntryResources implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            showResourceDialog(data.iterator().next());

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_RESOURCES_0);
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
     * Menu entry for option to abort publish job.<p>
     */
    class EntryStop implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String jobid = data.iterator().next();
            CmsPublishJobBase job = OpenCms.getPublishManager().getJobByPublishHistoryId(new CmsUUID(jobid));
            if (job instanceof CmsPublishJobEnqueued) {
                try {
                    OpenCms.getPublishManager().abortPublishJob(
                        A_CmsUI.getCmsObject(),
                        (CmsPublishJobEnqueued)job,
                        true);
                    CmsAppWorkplaceUi.get().reload();
                } catch (CmsException e) {
                    LOG.error("Error on aborting publish job.", e);
                }
            }
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_STOP_0);
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

    /**Error status icon. */
    public static final String ICON_ERROR = "apps/publishqueue/state_error.png";

    /**Ok status icon. */
    public static final String ICON_OK = "apps/publishqueue/state_ok.png";

    /**Warning status icon. */
    public static final String ICON_WARNINGS = "apps/publishqueue/state_warning.png";

    /** list action id constant. */
    public static final String LIST_ACTION_COUNT = "ac";

    /** list action id constant. */
    public static final String LIST_ACTION_END = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_PROJECT = "ap";

    /** list action id constant. */
    public static final String LIST_ACTION_START = "as";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE_ERR = "ate";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE_OK = "ato";

    /** list action id constant. */
    public static final String LIST_ACTION_VIEW = "av";

    /** list id constant. */
    public static final String LIST_ID = "lppq";

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsQueuedTable.class.getName());

    /**table column. */
    private static final String PROP_FILESCOUNT = "filesCount";

    /**table column. */
    private static final String PROP_ICON = "icon";

    /**table column. */
    private static final String PROP_PROJECT = "project";

    /**resources column.*/
    private static final String PROP_RESOURCES = "resources";

    /**table column. */
    private static final String PROP_START = "start";

    /**table column. */
    private static final String PROP_STATUS = "status";

    private static final String PROP_SORT_TYPE = "sortType";

    /**table column. */
    private static final String PROP_STATUS_LOCALE = "statusLocale";

    /**table column. */
    private static final String PROP_STOP = "stop";

    /**table column. */
    private static final String PROP_USER = "user";

    /**vaadin serial id. */
    private static final long serialVersionUID = 7507300060974348158L;

    /** Publish job state constant. */
    private static final String STATE_ERROR = "error";

    /** Publish job state constant. */
    private static final String STATE_OK = "ok";

    /** Publish job state constant. */
    private static final String STATE_WARNING = "warning";

    /** Publish job state constant. */
    private static final String STATE_RUNNING = "running";

    /** Publish job state constant. */
    private static final String STATE_ENQUEUE = "queue";

    /** Publish job state constant. */
    private static final Map<String, String> STATUS_MESSAGES = getStatusMap();

    /**Container. */
    BeanItemContainer<Row> m_container;

    /**Instance of calling class.*/
    CmsPublishQueue m_manager;

    /** The context menu. */
    CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntriesEnq;

    /**
     * Default constructor.<p>
     *
     * @param manager instance of calling class
     */
    public CmsQueuedTable(CmsPublishQueue manager) {

        m_manager = manager;
        setSizeFull();
        setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_PQUEUE_HIST_0));

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        m_container = new BeanItemContainer<Row>(Row.class);

        setContainerDataSource(m_container);
        //        setItemIconPropertyId(PROP_ICON);
        //        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setColumnHeader(PROP_STATUS_LOCALE, "");
        setColumnHeader(PROP_RESOURCES, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_RESOURCES_0));
        setColumnHeader(PROP_PROJECT, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_PROJECT_0));
        setColumnHeader(PROP_START, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_STARTDATE_0));
        setColumnHeader(PROP_STOP, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_ENDDATE_0));
        setColumnHeader(PROP_USER, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_USER_0));
        setColumnHeader(PROP_FILESCOUNT, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_SIZE_0));

        setVisibleColumns(
            PROP_STATUS_LOCALE,
            PROP_PROJECT,
            PROP_START,
            PROP_STOP,
            PROP_USER,
            PROP_RESOURCES,
            PROP_FILESCOUNT);
        setColumnWidth(PROP_START, 200);
        setColumnWidth(PROP_STOP, 200);
        setColumnWidth(PROP_RESOURCES, 550);

        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setColumnWidth(null, 40);

        setSelectable(true);

        addItemClickListener(new ItemClickListener() {

            /**vaadin serial id. */
            private static final long serialVersionUID = -7394790444104979594L;

            public void itemClick(ItemClickEvent event) {

                onItemClick(event, event.getItemId(), event.getPropertyId());

            }

        });

        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (PROP_RESOURCES.equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }

                if (PROP_PROJECT.equals(propertyId) & !(itemId instanceof CmsPublishJobEnqueued)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }

                if (PROP_STATUS_LOCALE.equals(propertyId)) {
                    if (STATE_OK.equals(source.getItem(itemId).getItemProperty(PROP_STATUS).getValue())) {
                        return OpenCmsTheme.TABLE_COLUMN_BOX_GREEN;
                    }
                    if (STATE_WARNING.equals(source.getItem(itemId).getItemProperty(PROP_STATUS).getValue())) {
                        return OpenCmsTheme.TABLE_COLUMN_BOX_ORANGE;
                    }
                    if (STATE_ERROR.equals(source.getItem(itemId).getItemProperty(PROP_STATUS).getValue())) {
                        return OpenCmsTheme.TABLE_COLUMN_BOX_RED;
                    }
                    if (STATE_RUNNING.equals(source.getItem(itemId).getItemProperty(PROP_STATUS).getValue())) {
                        return OpenCmsTheme.TABLE_COLUMN_BOX_DARKGRAY;
                    }
                    if (STATE_ENQUEUE.equals(source.getItem(itemId).getItemProperty(PROP_STATUS).getValue())) {
                        return OpenCmsTheme.TABLE_COLUMN_BOX_GRAY;
                    }
                }

                return null;
            }
        });

        // addGeneratedColumn(PROP_RESOURCES, new CmsResourcesCellGenerator(50));
        loadJobs();
    }

    /**
     * Returns the status message map.<p>
     *
     * @return the status message map
     */
    private static Map<String, String> getStatusMap() {

        Map<String, String> map = new HashMap<String, String>();
        map.put(STATE_OK, Messages.GUI_PQUEUE_STATUS_OK_0);
        map.put(STATE_WARNING, Messages.GUI_PQUEUE_STATUS_WARNING_0);
        map.put(STATE_ERROR, Messages.GUI_PQUEUE_STATUS_ERROR_0);
        map.put(STATE_RUNNING, Messages.GUI_PQUEUE_STATUS_RUNNING_0);
        map.put(STATE_ENQUEUE, Messages.GUI_PQUEUE_STATUS_ENQUEUE_0);

        return map;
    }

    /**
     * Filters the table according to given search string.<p>
     *
     * @param search string to be looked for.
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(PROP_USER, search, true, false),
                    new SimpleStringFilter(PROP_RESOURCES, search, true, false),
                    new SimpleStringFilter(PROP_PROJECT, search, true, false)));
        }
    }

    /**
     * Show report dialog.<p>
     *
     * @param jobid to show report for
     */
    protected void showReportDialog(String jobid) {

        CmsPublishReport pReport = new CmsPublishReport(jobid);
        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsBasicDialog dialog = new CmsBasicDialog();
        dialog.addButton(
            new Button(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CLOSE_0),
                new com.vaadin.ui.Button.ClickListener() {

                    private static final long serialVersionUID = -4216949392648631634L;

                    public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {

                        window.close();

                    }
                }),
            true);
        dialog.setContent(pReport);
        window.setContent(dialog);
        window.setCaption(pReport.getCaption());
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Show resource dialog.<p>
     *
     * @param jobid to show resources for
     */
    protected void showResourceDialog(String jobid) {

        CmsPublishResources pResources = new CmsPublishResources(jobid);
        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsBasicDialog dialog = new CmsBasicDialog();
        dialog.addButton(
            new Button(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CLOSE_0),
                new com.vaadin.ui.Button.ClickListener() {

                    private static final long serialVersionUID = -4216949392648631634L;

                    public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {

                        window.close();

                    }
                }),
            true);
        dialog.setContent(pResources);
        window.setContent(dialog);
        window.setCaption(pResources.getCaption());
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (getValue() instanceof CmsPublishJobEnqueued) {
            if (m_menuEntriesEnq == null) {
                m_menuEntriesEnq = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
                m_menuEntriesEnq.add(new EntryStop());
                m_menuEntriesEnq.add(new EntryResources());
            }
            return m_menuEntriesEnq;
        }

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new EntryReport());
            m_menuEntries.add(new EntryResources());
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

        setValue(null);
        select(itemId);
        if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == null)) {
            m_menu.setEntries(
                getMenuEntries(),
                Collections.singleton(((((Row)getValue()).getJob()).getPublishHistoryId()).getStringValue()));
            m_menu.openForTable(event, itemId, propertyId, CmsQueuedTable.this);
        } else if (event.getButton().equals(MouseButton.LEFT) && PROP_RESOURCES.equals(propertyId)) {
            showResourceDialog((((Row)getValue()).getJob()).getPublishHistoryId().getStringValue());
        } else if (event.getButton().equals(MouseButton.LEFT) && PROP_PROJECT.equals(propertyId)) {
            if (!(getValue() instanceof CmsPublishJobEnqueued)) {
                showReportDialog(((((Row)getValue()).getJob()).getPublishHistoryId().getStringValue()));
            }
        }
    }

    /**
     * Returns the state of the given publish job.<p>
     *
     * @param publishJob the publish job to get the state for
     * @return the state of the given publish job
     */
    private String getState(CmsPublishJobFinished publishJob) {

        byte[] reportBytes = null;
        try {
            reportBytes = OpenCms.getPublishManager().getReportContents(publishJob);
        } catch (CmsException e) {
            //Can't read report -> error
            return STATE_ERROR;
        }
        if (reportBytes != null) {
            String report = new String(reportBytes);
            if (report.indexOf("<span class='err'>") > -1) {
                //Report contains error span
                return STATE_ERROR;

            }
            if (report.indexOf("<span class='warn'>") > -1) {
                //Report contains warning span
                return STATE_WARNING;

            }
        }
        //no warning or error state detected -> ok
        return STATE_OK;

    }

    /**
     * Fills the table with finished publish jobs.<p>
     */
    private void loadJobs() {

        List<CmsPublishJobFinished> publishJobs;
        if (OpenCms.getRoleManager().hasRole(A_CmsUI.getCmsObject(), CmsRole.ROOT_ADMIN)) {
            publishJobs = OpenCms.getPublishManager().getPublishHistory();
        } else {
            publishJobs = OpenCms.getPublishManager().getPublishHistory(
                A_CmsUI.getCmsObject().getRequestContext().getCurrentUser());
        }
        for (CmsPublishJobFinished job : publishJobs) {
            m_container.addBean(new Row(job, 0));
        }
        //Sort table according to start time of jobs
        m_container.sort(new String[] {PROP_START}, new boolean[] {false});

        List<CmsPublishJobBase> jobs = new ArrayList<CmsPublishJobBase>();

        //a) running jobs
        if (OpenCms.getPublishManager().isRunning()) {
            CmsPublishJobRunning currentJob = OpenCms.getPublishManager().getCurrentPublishJob();
            if (currentJob != null) {
                m_container.addBean(new Row(currentJob, 1));
            }
        }

        int i = 0;
        for (CmsPublishJobBase job : OpenCms.getPublishManager().getPublishQueue()) {
            m_container.addBean(new Row(job, 2 + i));
            i += 1;
        }
        m_container.sort(new String[] {PROP_SORT_TYPE, PROP_START}, new boolean[] {false, false});

    }
}
