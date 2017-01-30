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

package org.opencms.ui.apps.projects;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The projects table.<p>
 */
public class CmsProjectsTable extends Table {

    /**
     * The delete project context menu entry.<p>
     */
    class DeleteEntry implements I_CmsSimpleContextMenuEntry<Set<CmsUUID>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<CmsUUID> data) {

            String message;
            if (data.size() == 1) {
                Item item = m_container.getItem(data.iterator().next());
                message = CmsVaadinUtils.getMessageText(
                    Messages.GUI_PROJECTS_CONFIRM_DELETE_PROJECT_1,
                    item.getItemProperty(PROP_NAME).getValue());
            } else {
                message = "";
                for (CmsUUID id : data) {
                    if (message.length() > 0) {
                        message += ", ";
                    }
                    Item item = m_container.getItem(id);
                    message += item.getItemProperty(PROP_NAME).getValue();
                }
                message = CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_CONFIRM_DELETE_PROJECTS_1, message);
            }

            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_DELETE_0),
                message,
                new Runnable() {

                    public void run() {

                        for (CmsUUID projectId : data) {
                            try {
                                A_CmsUI.getCmsObject().deleteProject(projectId);
                                CmsAppWorkplaceUi.get().reload();
                            } catch (CmsException e) {
                                LOG.error("Error deleting project " + projectId, e);
                                displayException(e);
                            }
                        }
                    }
                });
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_DELETE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<CmsUUID> data) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }
    }

    /**
     * The edit project context menu entry.<p>
     */
    class EditEntry implements I_CmsSimpleContextMenuEntry<Set<CmsUUID>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<CmsUUID> data) {

            CmsUUID id = data.iterator().next();
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(CmsProjectManager.PATH_NAME_EDIT, "projectId", id.toString()),
                true);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_EDIT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<CmsUUID> data) {

            return (data != null) && (data.size() == 1)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * The publish project context menu entry.<p>
     */
    class PublishEntry implements I_CmsSimpleContextMenuEntry<Set<CmsUUID>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<CmsUUID> data) {

            CmsUUID projectId = data.iterator().next();
            CmsAppWorkplaceUi.get().disableGlobalShortcuts();
            CmsGwtDialogExtension extension = new CmsGwtDialogExtension(A_CmsUI.get(), null);
            try {
                extension.openPublishDialog(A_CmsUI.getCmsObject().readProject(projectId));
            } catch (CmsException e) {
                LOG.error("Error reading project " + projectId, e);
                displayException(e);
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_PUBLISH_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<CmsUUID> data) {

            if ((data != null) && (data.size() == 1)) {
                CmsUUID projectId = data.iterator().next();
                try {
                    return A_CmsUI.getCmsObject().countLockedResources(projectId) == 0
                    ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
                    : CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
                } catch (CmsException e) {
                    LOG.error("Error reading locked resources on project " + projectId, e);
                }
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * The show project files context menu entry.<p>
     */
    class ShowFilesEntry
    implements I_CmsSimpleContextMenuEntry<Set<CmsUUID>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<CmsUUID> data) {

            CmsUUID id = data.iterator().next();
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(CmsProjectManager.PATH_NAME_FILES, "projectId", id.toString()),
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_SHOW_FILES_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<CmsUUID> data) {

            return (data != null) && (data.size() == 1)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * The unlock project context menu entry.<p>
     */
    class UnlockEntry implements I_CmsSimpleContextMenuEntry<Set<CmsUUID>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<CmsUUID> data) {

            for (CmsUUID projectId : data) {
                try {
                    A_CmsUI.getCmsObject().unlockProject(projectId);
                } catch (CmsException e) {
                    LOG.error("Error unlocking project " + projectId, e);
                    displayException(e);
                }
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_UNLOCK_FILES_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<CmsUUID> data) {

            if (data.size() == 1) {
                CmsUUID projectId = data.iterator().next();
                try {
                    if (A_CmsUI.getCmsObject().countLockedResources(projectId) == 0) {
                        return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
                    }
                } catch (CmsException e) {
                    LOG.error("Error reading locked resources on project " + projectId, e);
                }
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }
    }

    /** Project date created property. */
    public static final String PROP_DATE_CREATED = "dateCreated";

    /** Project description property. */
    public static final String PROP_DESCRIPTION = "descrition";

    /** Project icon property. */
    public static final String PROP_ICON = "icon";

    /** Project id property. */
    public static final String PROP_ID = "id";

    /** Project manager property. */
    public static final String PROP_MANAGER = "manager";

    /** Project name property. */
    public static final String PROP_NAME = "name";

    /** Project org unit property. */
    public static final String PROP_ORG_UNIT = "orgUnit";

    /** Project owner property. */
    public static final String PROP_OWNER = "owner";

    /** Project resources property. */
    public static final String PROP_RESOURCES = "resources";

    /** Project user property. */
    public static final String PROP_USER = "user";

    /** The logger for this class. */
    protected static Log LOG = CmsLog.getLog(CmsProjectsTable.class.getName());

    /** The serial version id. */
    private static final long serialVersionUID = 1540265836332964510L;

    /** The data container. */
    IndexedContainer m_container;

    /** The project manager instance. */
    CmsProjectManager m_manager;

    /** The context menu. */
    CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<CmsUUID>>> m_menuEntries;

    /**
     * Constructor.<p>
     *
     * @param manager the project manager
     */
    public CmsProjectsTable(CmsProjectManager manager) {
        m_manager = manager;

        m_container = new IndexedContainer();
        m_container.addContainerProperty(PROP_ID, CmsUUID.class, null);
        m_container.addContainerProperty(
            PROP_ICON,
            Resource.class,
            new ExternalResource(OpenCmsTheme.getImageLink(CmsProjectManager.ICON_PROJECT_SMALL)));
        m_container.addContainerProperty(PROP_NAME, String.class, "");
        m_container.addContainerProperty(PROP_DESCRIPTION, String.class, "");
        m_container.addContainerProperty(PROP_ORG_UNIT, String.class, "");
        m_container.addContainerProperty(PROP_OWNER, String.class, "");
        m_container.addContainerProperty(PROP_MANAGER, String.class, "");
        m_container.addContainerProperty(PROP_USER, String.class, "");
        m_container.addContainerProperty(PROP_DATE_CREATED, Date.class, "");
        m_container.addContainerProperty(PROP_RESOURCES, Label.class, "");

        setContainerDataSource(m_container);
        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setColumnHeader(PROP_NAME, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_NAME_0));
        setColumnHeader(PROP_DESCRIPTION, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_DESCRIPTION_0));
        setColumnHeader(PROP_ORG_UNIT, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_ORG_UNIT_0));
        setColumnHeader(PROP_OWNER, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_OWNER_0));
        setColumnHeader(PROP_MANAGER, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_MANAGER_GROUP_0));
        setColumnHeader(PROP_USER, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_USER_GROUP_0));
        setColumnHeader(PROP_DATE_CREATED, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_DATE_CREATED_0));
        setColumnHeader(PROP_RESOURCES, CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_RESOURCES_0));
        setColumnWidth(null, 40);
        setColumnExpandRatio(PROP_NAME, 2);
        setColumnExpandRatio(PROP_DESCRIPTION, 2);
        setColumnExpandRatio(PROP_RESOURCES, 2);
        setSelectable(true);
        setMultiSelect(true);
        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                onItemClick(event);
            }
        });
        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (PROP_NAME.equals(propertyId)) {
                    return OpenCmsTheme.HOVER_COLUMN;
                }
                return null;
            }
        });
    }

    /**
     * Filters the displayed projects.<p>
     *
     * @param filter the filter
     */
    public void filterTable(String filter) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(PROP_NAME, filter, true, false),
                    new SimpleStringFilter(PROP_DESCRIPTION, filter, true, false)));
        }
    }

    /**
     * Loads the projects table.<p>
     */
    public void loadProjects() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale locale = UI.getCurrent().getLocale();
        m_container.removeAllItems();
        boolean isMultiOU = false;
        // hide ou column if only one ou exists
        try {
            isMultiOU = !OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, "", true).isEmpty();
        } catch (CmsException e) {
            // noop
        }
        if (isMultiOU) {
            setVisibleColumns(
                PROP_NAME,
                PROP_DESCRIPTION,
                PROP_ORG_UNIT,
                PROP_OWNER,
                PROP_MANAGER,
                PROP_USER,
                PROP_DATE_CREATED,
                PROP_RESOURCES);
        } else {
            setVisibleColumns(
                PROP_NAME,
                PROP_DESCRIPTION,
                PROP_OWNER,
                PROP_MANAGER,
                PROP_USER,
                PROP_DATE_CREATED,
                PROP_RESOURCES);
        }

        // get content
        try {
            List<CmsProject> projects = OpenCms.getOrgUnitManager().getAllManageableProjects(cms, "", true);
            for (CmsProject project : projects) {
                Item item = m_container.addItem(project.getUuid());
                item.getItemProperty(PROP_ID).setValue(project.getUuid());
                item.getItemProperty(PROP_NAME).setValue(project.getSimpleName());
                item.getItemProperty(PROP_DESCRIPTION).setValue(project.getDescription());
                try {
                    item.getItemProperty(PROP_ORG_UNIT).setValue(
                        OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, project.getOuFqn()).getDisplayName(
                            locale));
                    item.getItemProperty(PROP_OWNER).setValue(cms.readUser(project.getOwnerId()).getName());
                    item.getItemProperty(PROP_MANAGER).setValue(
                        cms.readGroup(project.getManagerGroupId()).getSimpleName());
                    item.getItemProperty(PROP_USER).setValue(cms.readGroup(project.getGroupId()).getSimpleName());
                } catch (CmsException e) {
                    LOG.error("Error reading project properties for " + project.getSimpleName());
                }
                item.getItemProperty(PROP_DATE_CREATED).setValue(new Date(project.getDateCreated()));

                StringBuffer html = new StringBuffer(512);
                try {
                    for (String resource : cms.readProjectResources(project)) {
                        html.append(resource);
                        html.append("<br />");
                    }
                } catch (CmsException e) {
                    LOG.error("Error reading project resources for " + project.getSimpleName());
                }
                Label resLabel = new Label();
                resLabel.setContentMode(ContentMode.HTML);
                resLabel.setValue(html.toString());
                item.getItemProperty(PROP_RESOURCES).setValue(resLabel);

            }
        } catch (CmsException e) {
            LOG.error("Error reading manageable projects", e);
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Displays the given exception in the error dialog and reloads the UI on close.<p>
     *
     * @param e the exception
     */
    protected void displayException(Throwable e) {

        CmsErrorDialog.showErrorDialog(e, new Runnable() {

            public void run() {

                CmsAppWorkplaceUi.get().reload();
            }
        });
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<CmsUUID>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<CmsUUID>>>();
            m_menuEntries.add(new ShowFilesEntry());
            m_menuEntries.add(new UnlockEntry());
            m_menuEntries.add(new PublishEntry());
            m_menuEntries.add(new EditEntry());
            m_menuEntries.add(new DeleteEntry());
        }
        return m_menuEntries;
    }

    /**
     * Handles the table item clicks.<p>
     *
     * @param event the click event
     */
    @SuppressWarnings("unchecked")
    void onItemClick(ItemClickEvent event) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {
            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                CmsUUID itemId = (CmsUUID)event.getItemId();
                Set<CmsUUID> value = (Set<CmsUUID>)getValue();
                if (value == null) {
                    select(itemId);
                } else if (!value.contains(itemId)) {
                    setValue(null);
                    select(itemId);
                }
                m_menu.setEntries(getMenuEntries(), (Set<CmsUUID>)getValue());
                m_menu.openForTable(event, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && PROP_NAME.equals(event.getPropertyId())) {
                Item item = event.getItem();
                CmsUUID id = (CmsUUID)item.getItemProperty(PROP_ID).getValue();
                m_manager.openSubView(
                    A_CmsWorkplaceApp.addParamToState(CmsProjectManager.PATH_NAME_FILES, "projectId", id.toString()),
                    true);
            }
        }
    }
}
