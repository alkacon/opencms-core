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

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobFinished;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Image;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class for Vaadin Table showing history queue elements.<p>
 */
public class CmsHistoryQueuedTable extends Table {

    /**
     *Menu entry for showing report.<p>
     */
    class EntryReport implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String jobid = data.iterator().next();
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(CmsPublishQueue.PATH_REPORT, CmsPublishQueue.JOB_ID, jobid),
                true);
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

            String jobid = data.iterator().next();
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(CmsPublishQueue.PATH_RESOURCE, CmsPublishQueue.JOB_ID, jobid),
                true);
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
     * Column for status.<p>
     */
    class StatusColumn implements Table.ColumnGenerator {

        /**vaadin serial id. */
        private static final long serialVersionUID = -7953703707788778747L;

        /**
         * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
         */
        public Object generateCell(Table source, Object itemId, Object columnId) {

            Property<Object> prop = source.getItem(itemId).getItemProperty(PROP_STATUS);
            return getImageFromState((String)prop.getValue(), itemId);
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

    /** list column id constant. */
    private static final String LIST_COLUMN_ERRORS = "cse";

    /** list column id constant. */
    private static final String LIST_COLUMN_STATE = "ct";

    /** list column id constant. */
    private static final String LIST_COLUMN_WARNINGS = "csw";

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsHistoryQueuedTable.class.getName());

    /**table column. */
    private static final String PROP_FILESCOUNT = "files";

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

    /**Container. */
    IndexedContainer m_container;

    /**Instance of calling class.*/
    CmsPublishQueue m_manager;

    /** The context menu. */
    CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * Default constructor.<p>
     *
     * @param manager instance of calling class
     */
    public CmsHistoryQueuedTable(CmsPublishQueue manager) {
        m_manager = manager;
        setSizeFull();
        setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_PQUEUE_HIST_0));

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        m_container = new IndexedContainer();
        m_container.addContainerProperty(
            PROP_ICON,
            Resource.class,
            new ExternalResource(OpenCmsTheme.getImageLink(CmsPublishQueue.TABLE_ICON)));
        m_container.addContainerProperty(PROP_STATUS, String.class, null);
        m_container.addContainerProperty(PROP_PROJECT, String.class, "");
        m_container.addContainerProperty(PROP_START, Date.class, null);
        m_container.addContainerProperty(PROP_STOP, Date.class, null);
        m_container.addContainerProperty(PROP_USER, String.class, "");
        m_container.addContainerProperty(PROP_FILESCOUNT, Integer.class, Integer.valueOf(1));
        m_container.addContainerProperty(PROP_RESOURCES, List.class, null);

        setContainerDataSource(m_container);
        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setColumnHeader(PROP_STATUS, "");
        setColumnHeader(PROP_RESOURCES, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_RESOURCES_0));
        setColumnHeader(PROP_PROJECT, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_PROJECT_0));
        setColumnHeader(PROP_START, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_STARTDATE_0));
        setColumnHeader(PROP_STOP, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_ENDDATE_0));
        setColumnHeader(PROP_USER, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_USER_0));
        setColumnHeader(PROP_FILESCOUNT, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_SIZE_0));

        setColumnAlignment(PROP_STATUS, Align.CENTER);

        setColumnWidth(null, 40);
        setColumnWidth(PROP_STATUS, 40);
        setColumnWidth(PROP_START, 200);
        setColumnWidth(PROP_STOP, 200);
        setColumnWidth(PROP_RESOURCES, 550);

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

                if (PROP_PROJECT.equals(propertyId) | PROP_RESOURCES.equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }

                return null;
            }
        });

        addGeneratedColumn(PROP_RESOURCES, new CmsResourcesCellGenerator(50));
        addGeneratedColumn(PROP_STATUS, new StatusColumn());

        loadJobs();
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
     * Returns image for given state.<p>
     *
     * @param state state
     * @param itemId item id
     * @return image
     */
    Image getImageFromState(String state, final Object itemId) {

        String description = "";
        String path = ICON_OK;
        if (state.equals(STATE_ERROR)) {
            path = ICON_ERROR;
            description = CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_STATUS_ERROR_0);
        } else if (state.equals(STATE_WARNING)) {
            path = ICON_WARNINGS;
            description = CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_STATUS_WARNING_0);
        } else {
            description = CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_STATUS_OK_0);
        }

        Image ret = new Image(
            String.valueOf(System.currentTimeMillis()),
            new ExternalResource(OpenCmsTheme.getImageLink(path)));
        ret.setDescription(description);
        ret.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -8488944762446212367L;

            public void click(ClickEvent event) {

                onItemClick(event, itemId, PROP_STATUS);
            }
        });
        return ret;
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

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
            m_menu.setEntries(getMenuEntries(), Collections.singleton(((CmsUUID)getValue()).getStringValue()));
            m_menu.openForTable(event, itemId, propertyId, CmsHistoryQueuedTable.this);
        } else if (event.getButton().equals(MouseButton.LEFT) && PROP_RESOURCES.equals(propertyId)) {
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(
                    CmsPublishQueue.PATH_RESOURCE,
                    CmsPublishQueue.JOB_ID,
                    ((CmsUUID)itemId).getStringValue()),
                true);
        } else if (event.getButton().equals(MouseButton.LEFT) && PROP_STATUS.equals(propertyId)) {
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(
                    CmsPublishQueue.PATH_REPORT,
                    CmsPublishQueue.JOB_ID,
                    ((CmsUUID)itemId).getStringValue()),
                true);
        } else if (event.getButton().equals(MouseButton.LEFT) && PROP_PROJECT.equals(propertyId)) {
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(
                    CmsPublishQueue.PATH_REPORT,
                    CmsPublishQueue.JOB_ID,
                    ((CmsUUID)itemId).getStringValue()),
                true);
        }
    }

    /**
     * Returns the state of the given publish job.<p>
     *
     * @param publishJob the publish job to get the state for
     * @return the state of the given publish job
     */
    private Map<String, Object> getState(CmsPublishJobFinished publishJob) {

        Map<String, Object> result = new HashMap<String, Object>();
        byte[] reportBytes = null;
        try {
            reportBytes = OpenCms.getPublishManager().getReportContents(publishJob);
        } catch (CmsException e) {
            result.put(LIST_COLUMN_STATE, STATE_OK);
        }
        if ((reportBytes != null) && (result.get(LIST_COLUMN_STATE) == null)) {
            String report = new String(reportBytes);
            // see org.opencms.report.CmsHtmlReport#print(String, int)
            if (report.indexOf("<span class='err'>") > -1) {
                result.put(LIST_COLUMN_STATE, STATE_ERROR);
                result.put(
                    LIST_COLUMN_ERRORS,
                    new Integer(CmsStringUtil.splitAsList(report, "<span class='err'>").size() - 1));
                result.put(
                    LIST_COLUMN_WARNINGS,
                    new Integer(CmsStringUtil.splitAsList(report, "<span class='warn'>").size() - 1));
            } else if (report.indexOf("<span class='warn'>") > -1) {
                result.put(LIST_COLUMN_STATE, STATE_WARNING);
                result.put(
                    LIST_COLUMN_WARNINGS,
                    new Integer(CmsStringUtil.splitAsList(report, "<span class='warn'>").size() - 1));
            } else {
                result.put(LIST_COLUMN_STATE, STATE_OK);
            }
        }
        if (result.get(LIST_COLUMN_WARNINGS) == null) {
            result.put(LIST_COLUMN_WARNINGS, new Integer(0));
        }
        if (result.get(LIST_COLUMN_ERRORS) == null) {
            result.put(LIST_COLUMN_ERRORS, new Integer(0));
        }
        return result;
    }

    /**
     * Fills the table with finished publish jobs.<p>
     */
    private void loadJobs() {

        setVisibleColumns(PROP_STATUS, PROP_PROJECT, PROP_START, PROP_STOP, PROP_USER, PROP_RESOURCES, PROP_FILESCOUNT);

        List<CmsPublishJobFinished> publishJobs;
        if (OpenCms.getRoleManager().hasRole(A_CmsUI.getCmsObject(), CmsRole.ROOT_ADMIN)) {
            publishJobs = OpenCms.getPublishManager().getPublishHistory();
        } else {
            publishJobs = OpenCms.getPublishManager().getPublishHistory(
                A_CmsUI.getCmsObject().getRequestContext().getCurrentUser());
        }
        for (CmsPublishJobFinished job : publishJobs) {
            Map<String, Object> state = getState(job);
            Item item = m_container.addItem(job.getPublishHistoryId());
            item.getItemProperty(PROP_PROJECT).setValue(job.getProjectName().replace("&#47;", "/")); //TODO better way for unescaping..
            try {
                item.getItemProperty(PROP_RESOURCES).setValue(
                    A_CmsUI.getCmsObject().readPublishedResources(job.getPublishHistoryId()));
            } catch (com.vaadin.data.Property.ReadOnlyException | CmsException e) {
                LOG.error("Error while read published Resources", e);
            }
            item.getItemProperty(PROP_STATUS).setValue(state.get(LIST_COLUMN_STATE));
            item.getItemProperty(PROP_START).setValue(new Date(job.getStartTime()));
            item.getItemProperty(PROP_STOP).setValue(new Date(job.getFinishTime()));
            item.getItemProperty(PROP_USER).setValue(job.getUserName(A_CmsUI.getCmsObject()));
            item.getItemProperty(PROP_FILESCOUNT).setValue(Integer.valueOf(job.getSize()));

        }
        //Sort table according to start time of jobs
        m_container.sort(new String[] {PROP_START}, new boolean[] {false});
    }
}
