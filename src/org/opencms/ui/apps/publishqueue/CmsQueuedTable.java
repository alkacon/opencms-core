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
import org.opencms.publish.CmsPublishJobBase;
import org.opencms.publish.CmsPublishJobEnqueued;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Vaadin Table showing current jobs in queue.<p>
 */
public class CmsQueuedTable extends Table {

    /**
    * Menu entry for show-report option.<p>
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
    * Menu entry for show resources option.<p>
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

    /**File-count column id.*/
    public static final String PROP_FILESCOUNT = "files";

    /**icon property. */
    public static final String PROP_ICON = "icon";

    /**job type property. */
    public static final String PROP_ISRUNNING = "runjob";

    /**project column id.*/
    public static final String PROP_PROJECT = "project";

    /**resources column.*/
    public static final String PROP_RESOURCES = "resources";

    /**Start column id.*/
    public static final String PROP_START = "start";

    /**user column id.*/
    public static final String PROP_USER = "user";

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsQueuedTable.class.getName());

    /**generated id.*/
    private static final long serialVersionUID = -2229660370686867919L;

    /**indexed container. */
    IndexedContainer m_container;

    /** object which has called table.*/
    CmsPublishQueue m_manager;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**list of all current jobs.*/
    private List<CmsPublishJobBase> m_jobs;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * Public constructor.<p>
     *
     * @param manager object called this class
     */
    public CmsQueuedTable(CmsPublishQueue manager) {

        m_manager = manager;
        setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_PQUEUE_0));

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        //Read running and queued publish jobs
        m_jobs = new ArrayList<CmsPublishJobBase>();

        //a) running jobs
        if (OpenCms.getPublishManager().isRunning()) {
            m_jobs.add(OpenCms.getPublishManager().getCurrentPublishJob());
        }

        //b) queued jobs
        m_jobs.addAll(OpenCms.getPublishManager().getPublishQueue());

        if (m_jobs.size() > 0) {
            iniTable();
            addItemClickListener(new ItemClickListener() {

                /**vaadin serial id.*/
                private static final long serialVersionUID = -7394790444104979594L;

                public void itemClick(ItemClickEvent event) {

                    onItemClick(event, event.getItemId(), event.getPropertyId());
                }
            });
            loadJobs();
        } else {
            //No jobs there -> show empty table with message
            iniDummyTable();
        }
        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (PROP_PROJECT.equals(propertyId) | PROP_RESOURCES.equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }

                return null;
            }
        });

    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
        if (((Boolean)(getItem(getValue()).getItemProperty(PROP_ISRUNNING).getValue())).booleanValue()) {
            m_menuEntries.add(new EntryReport()); //Option for Report (only if job is running, not enqueued)
        } else {
            m_menuEntries.add(new EntryStop()); //Option for abort job (only if enqueued, not running)
        }
        m_menuEntries.add(new EntryResources()); //Option for Resources

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

        //Right click or click on icon column (=null) -> show menu
        if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == null)) {
            m_menu.setEntries(getMenuEntries(), Collections.singleton(((CmsUUID)getValue()).getStringValue()));
            m_menu.openForTable(event, itemId, propertyId, CmsQueuedTable.this);

            //Left click on resource column -> show resources
        } else if (event.getButton().equals(MouseButton.LEFT) && PROP_RESOURCES.equals(propertyId)) {
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(
                    CmsPublishQueue.PATH_RESOURCE,
                    CmsPublishQueue.JOB_ID,
                    ((CmsUUID)itemId).getStringValue()),
                true);

            //Left click on Project column -> show report
        } else if (event.getButton().equals(MouseButton.LEFT)
            && PROP_PROJECT.equals(propertyId)
            && ((Boolean)(CmsQueuedTable.this.getItem(itemId).getItemProperty(
                PROP_ISRUNNING).getValue())).booleanValue()) {
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(
                    CmsPublishQueue.PATH_REPORT,
                    CmsPublishQueue.JOB_ID,
                    ((CmsUUID)itemId).getStringValue()),
                true);
        }
    }

    /**
     * Init table in case of a empty queue.<p>
     */
    private void iniDummyTable() {

        m_container = new IndexedContainer();
        m_container.addContainerProperty(PROP_PROJECT, String.class, "");
        m_container.addContainerProperty(PROP_START, Date.class, null);
        m_container.addContainerProperty(PROP_USER, String.class, "");
        m_container.addContainerProperty(PROP_FILESCOUNT, Integer.class, null);
        setContainerDataSource(m_container);

        setColumnHeader(PROP_PROJECT, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_PROJECT_0));
        setColumnHeader(PROP_START, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_STARTDATE_0));
        setColumnHeader(PROP_USER, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_USER_0));
        setColumnHeader(PROP_FILESCOUNT, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_SIZE_0));

        Item item = m_container.addItem("dummy");
        item.getItemProperty(PROP_PROJECT).setValue(CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_NOJOB_0));
    }

    /**
     * Init table in case of having running jobs.<p>
     */
    private void iniTable() {

        m_container = new IndexedContainer();
        m_container.addContainerProperty(
            PROP_ICON,
            Resource.class,
            new ExternalResource(OpenCmsTheme.getImageLink(CmsPublishQueue.TABLE_ICON)));
        m_container.addContainerProperty(PROP_PROJECT, String.class, "");
        m_container.addContainerProperty(PROP_START, Date.class, null);
        m_container.addContainerProperty(PROP_USER, String.class, "");
        m_container.addContainerProperty(PROP_RESOURCES, List.class, null);
        m_container.addContainerProperty(PROP_FILESCOUNT, Integer.class, Integer.valueOf(1));
        m_container.addContainerProperty(PROP_ISRUNNING, Boolean.class, Boolean.FALSE);

        setContainerDataSource(m_container);
        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setColumnHeader(PROP_RESOURCES, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_RESOURCES_0));
        setColumnHeader(PROP_PROJECT, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_PROJECT_0));
        setColumnHeader(PROP_START, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_STARTDATE_0));
        setColumnHeader(PROP_USER, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_USER_0));
        setColumnHeader(PROP_FILESCOUNT, CmsVaadinUtils.getMessageText(Messages.GUI_PQUEUE_SIZE_0));

        setColumnWidth(null, 40);
        setColumnWidth(PROP_USER, 350);

        addGeneratedColumn(PROP_RESOURCES, new CmsResourcesCellGenerator(120));

        setSelectable(true);
    }

    /**
     * Interates through all jobs in the queue and display them in the table.<p>
     */
    private void loadJobs() {

        setVisibleColumns(PROP_PROJECT, PROP_START, PROP_USER, PROP_RESOURCES, PROP_FILESCOUNT);
        for (CmsPublishJobBase job : m_jobs) {

            Item item = m_container.addItem(job.getPublishHistoryId());
            item.getItemProperty(PROP_PROJECT).setValue(job.getProjectName().replace("&#47;", "/")); //TODO better way for unescaping..

            //distinguish between running and enqueued jobs
            if (job instanceof CmsPublishJobRunning) {
                item.getItemProperty(PROP_RESOURCES).setValue(
                    ((CmsPublishJobRunning)job).getPublishList().getAllResources());
                item.getItemProperty(PROP_START).setValue(new Date(((CmsPublishJobRunning)job).getStartTime()));
                item.getItemProperty(PROP_ISRUNNING).setValue(new Boolean(true));
            } else {
                item.getItemProperty(PROP_RESOURCES).setValue(
                    ((CmsPublishJobEnqueued)job).getPublishList().getAllResources());
                item.getItemProperty(PROP_ISRUNNING).setValue(new Boolean(false));
            }
            item.getItemProperty(PROP_USER).setValue(job.getUserName(A_CmsUI.getCmsObject()));
            item.getItemProperty(PROP_FILESCOUNT).setValue(Integer.valueOf(job.getSize()));
        }
    }
}
