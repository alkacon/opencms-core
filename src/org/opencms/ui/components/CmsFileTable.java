/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ui.components;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsCustomComponent;
import org.opencms.ui.I_CmsContextMenuBuilder;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorerSettings;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.vaadin.peter.contextmenu.ContextMenu;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.RowHeaderMode;

/**
 * Table for displaying resources.<p>
 */
public class CmsFileTable extends A_CmsCustomComponent {

    /**
     * Extends the default sorting to differentiate between files and folder when sorting by name.<p>
     */
    public static class FileSorter extends DefaultItemSorter {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.data.util.DefaultItemSorter#compareProperty(java.lang.Object, boolean, com.vaadin.data.Item, com.vaadin.data.Item)
         */
        @Override
        protected int compareProperty(Object propertyId, boolean sortDirection, Item item1, Item item2) {

            if (PROPERTY_RESOURCE_NAME.equals(propertyId)) {
                Boolean isFolder1 = (Boolean)item1.getItemProperty(PROPERTY_IS_FOLDER).getValue();
                Boolean isFolder2 = (Boolean)item2.getItemProperty(PROPERTY_IS_FOLDER).getValue();
                if (!isFolder1.equals(isFolder2)) {
                    int result = isFolder1.booleanValue() ? -1 : 1;
                    if (!sortDirection) {
                        result = result * (-1);
                    }
                    return result;
                }
            }
            return super.compareProperty(propertyId, sortDirection, item1, item2);
        }
    }

    /** File table property name. */
    public static final String PROPERTY_DATE_CREATED = "dateCreated";

    /** File table property name. */
    public static final String PROPERTY_DATE_EXPIRED = "dateExpired";

    /** File table property name. */
    public static final String PROPERTY_DATE_MODIFIED = "dateModified";

    /** File table property name. */
    public static final String PROPERTY_DATE_RELEASED = "dateReleased";

    /** File table property name. */
    public static final String PROPERTY_IS_FOLDER = "isFolder";

    /** File table property name. */
    public static final String PROPERTY_NAVIGATION_TEXT = "navigationText";

    /** File table property name. */
    public static final String PROPERTY_PERMISSIONS = "permissions";

    /** File table property name. */
    public static final String PROPERTY_RESOURCE_NAME = "resourceName";

    /** File table property name. */
    public static final String PROPERTY_RESOURCE_TYPE = "resourceType";

    /** File table property name. */
    public static final String PROPERTY_SIZE = "size";

    /** File table property name. */
    public static final String PROPERTY_STATE = "state";

    /** File table property name. */
    public static final String PROPERTY_STATE_NAME = "stateName";

    /** File table property name. */
    public static final String PROPERTY_TITLE = "title";

    /** File table property name. */
    public static final String PROPERTY_TYPE_ICON = "typeIcon";

    /** File table property name. */
    public static final String PROPERTY_USER_CREATED = "userCreated";

    /** File table property name. */
    public static final String PROPERTY_USER_LOCKED = "userLocked";

    /** File table property name. */
    public static final String PROPERTY_USER_MODIFIED = "userModified";

    /** The serial version id. */
    private static final long serialVersionUID = 5460048685141699277L;

    /** The resource data container. */
    IndexedContainer m_container;

    /** The table used to display the resource data. */
    private Table m_fileTable;

    private ContextMenu m_menu;

    private I_CmsContextMenuBuilder m_menuBuilder;

    /** The messages. */
    private CmsMessages messages;

    /**
     * Default constructor.<p>
     */
    public CmsFileTable() {

        super();
        m_container = new IndexedContainer();
        m_container.addContainerProperty(PROPERTY_TYPE_ICON, Resource.class, null);
        m_container.addContainerProperty(PROPERTY_RESOURCE_NAME, String.class, null);
        m_container.addContainerProperty(PROPERTY_TITLE, String.class, null);
        m_container.addContainerProperty(PROPERTY_NAVIGATION_TEXT, String.class, null);
        m_container.addContainerProperty(PROPERTY_RESOURCE_TYPE, String.class, null);
        m_container.addContainerProperty(PROPERTY_IS_FOLDER, Boolean.class, null);
        m_container.addContainerProperty(PROPERTY_SIZE, Integer.class, null);
        m_container.addContainerProperty(PROPERTY_PERMISSIONS, String.class, null);
        m_container.addContainerProperty(PROPERTY_DATE_MODIFIED, String.class, null);
        m_container.addContainerProperty(PROPERTY_USER_MODIFIED, String.class, null);
        m_container.addContainerProperty(PROPERTY_DATE_CREATED, String.class, null);
        m_container.addContainerProperty(PROPERTY_USER_CREATED, String.class, null);
        m_container.addContainerProperty(PROPERTY_DATE_RELEASED, String.class, "-");
        m_container.addContainerProperty(PROPERTY_DATE_EXPIRED, String.class, "-");
        m_container.addContainerProperty(PROPERTY_STATE_NAME, String.class, null);
        m_container.addContainerProperty(PROPERTY_STATE, CmsResourceState.class, null);
        m_container.addContainerProperty(PROPERTY_USER_LOCKED, String.class, null);
        m_container.setItemSorter(new FileSorter());
        m_fileTable = new Table();
        setCompositionRoot(m_fileTable);
        m_fileTable.addStyleName("borderless");
        m_fileTable.setSizeFull();
        m_fileTable.setColumnCollapsingAllowed(true);
        m_fileTable.setSelectable(true);
        m_fileTable.setMultiSelect(true);

        m_fileTable.setContainerDataSource(m_container);

        // following also sets the column order
        m_fileTable.setVisibleColumns(
            PROPERTY_RESOURCE_NAME,
            PROPERTY_TITLE,
            PROPERTY_NAVIGATION_TEXT,
            PROPERTY_RESOURCE_TYPE,
            PROPERTY_SIZE,
            PROPERTY_PERMISSIONS,
            PROPERTY_DATE_MODIFIED,
            PROPERTY_USER_MODIFIED,
            PROPERTY_DATE_CREATED,
            PROPERTY_USER_CREATED,
            PROPERTY_DATE_RELEASED,
            PROPERTY_DATE_EXPIRED,
            PROPERTY_STATE_NAME,
            PROPERTY_USER_LOCKED);

        messages = org.opencms.workplace.explorer.Messages.get().getBundle(CmsAppWorkplaceUi.get().getLocale());

        // using the same order as above
        m_fileTable.setColumnHeaders(
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_NAVTEXT_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0));

        m_fileTable.setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        m_fileTable.setItemIconPropertyId(PROPERTY_TYPE_ICON);
        m_fileTable.setSortContainerPropertyId(PROPERTY_RESOURCE_NAME);

        m_fileTable.setColumnCollapsed(PROPERTY_NAVIGATION_TEXT, true);
        m_fileTable.setColumnCollapsed(PROPERTY_PERMISSIONS, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_MODIFIED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_DATE_CREATED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_CREATED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_STATE_NAME, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_LOCKED, true);

        m_fileTable.addItemClickListener(new ItemClickListener() {

            public void itemClick(ItemClickEvent event) {

                if (event.getButton().equals(MouseButton.RIGHT)) {
                    Set<CmsUUID> selection = (Set<CmsUUID>)m_fileTable.getValue();
                    if (selection == null) {
                        m_fileTable.select(event.getItemId());
                    } else if (!selection.contains(event.getItemId())) {
                        m_fileTable.setValue(null);
                        m_fileTable.select(event.getItemId());
                    }

                }
            }
        });
        m_menu = new ContextMenu();
        m_fileTable.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                Set<CmsUUID> selectedIds = (Set<CmsUUID>)event.getProperty().getValue();
                if ((selectedIds != null) && !selectedIds.isEmpty()) {
                    m_menu.removeAllItems();
                    m_menuBuilder.buildContextMenu(selectedIds, m_menu);
                }
            }
        });

        m_fileTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                return getStateStyle(m_container.getItem(itemId));
            }
        });

        m_menu.setAsTableContextMenu(m_fileTable);
    }

    /**
     * Returns the resource state specific style name.<p>
     *
     * @param resourceItem the resource item
     *
     * @return the style name
     */
    public static String getStateStyle(Item resourceItem) {

        String result = "";
        if (resourceItem != null) {
            CmsResourceState state = (CmsResourceState)resourceItem.getItemProperty(PROPERTY_STATE).getValue();
            if (state != null) {
                if (state.isDeleted()) {
                    result = "state-deleted";
                } else if (state.isNew()) {
                    result = "state-new";
                } else if (state.isChanged()) {
                    result = "state-changed";
                }
            }
        }
        return result;
    }

    /**
     * Adds an item click listener to the table.<p>
     *
     * @param listener the listener
     */
    public void addItemClickListener(ItemClickListener listener) {

        m_fileTable.addItemClickListener(listener);
    }

    /**
     * Fills the resource table.<p>
     *
     * @param cms the current CMS context
     * @param resources the resources which should be displayed in the table
     */
    public void fillTable(CmsObject cms, List<CmsResource> resources) {

        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        m_container.removeAllItems();
        m_container.removeAllContainerFilters();
        CmsResourceUtil resUtil = new CmsResourceUtil(cms);
        for (CmsResource resource : resources) {
            try {
                resUtil.setResource(resource);
                Item resourceItem = m_container.addItem(resource.getStructureId());
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
                CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    type.getTypeName());
                resourceItem.getItemProperty(PROPERTY_TYPE_ICON).setValue(
                    new ExternalResource(
                        CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getIcon())));
                resourceItem.getItemProperty(PROPERTY_RESOURCE_NAME).setValue(resource.getName());
                resourceItem.getItemProperty(PROPERTY_TITLE).setValue(resUtil.getTitle());
                resourceItem.getItemProperty(PROPERTY_NAVIGATION_TEXT).setValue(resUtil.getNavText());
                resourceItem.getItemProperty(PROPERTY_RESOURCE_TYPE).setValue(
                    CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
                resourceItem.getItemProperty(PROPERTY_IS_FOLDER).setValue(Boolean.valueOf(resource.isFolder()));
                if (resource.isFile()) {
                    resourceItem.getItemProperty(PROPERTY_SIZE).setValue(Integer.valueOf(resource.getLength()));
                }
                resourceItem.getItemProperty(PROPERTY_PERMISSIONS).setValue(resUtil.getPermissionString());
                resourceItem.getItemProperty(PROPERTY_DATE_MODIFIED).setValue(
                    messages.getDateTime(resource.getDateLastModified()));
                resourceItem.getItemProperty(PROPERTY_USER_MODIFIED).setValue(resUtil.getUserLastModified());
                resourceItem.getItemProperty(PROPERTY_DATE_CREATED).setValue(
                    messages.getDateTime(resource.getDateCreated()));
                resourceItem.getItemProperty(PROPERTY_USER_CREATED).setValue(resUtil.getUserCreated());
                resourceItem.getItemProperty(PROPERTY_DATE_RELEASED).setValue(resUtil.getDateReleased());
                resourceItem.getItemProperty(PROPERTY_DATE_EXPIRED).setValue(resUtil.getDateExpired());
                resourceItem.getItemProperty(PROPERTY_STATE_NAME).setValue(resUtil.getStateName());
                resourceItem.getItemProperty(PROPERTY_STATE).setValue(resource.getState());
                resourceItem.getItemProperty(PROPERTY_USER_LOCKED).setValue(resUtil.getLockedByName());
            } catch (CmsException e) {
                e.printStackTrace();
                Notification.show(e.getMessage());
            }
        }
        m_fileTable.sort();
    }

    /**
     * Filters the displayed resources.<p>
     * Only resources where either the resource name, the title or the nav-text contains the given substring are shown.<p>
     *
     * @param search the search term
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(PROPERTY_RESOURCE_NAME, search, true, false),
                    new SimpleStringFilter(PROPERTY_NAVIGATION_TEXT, search, true, false),
                    new SimpleStringFilter(PROPERTY_TITLE, search, true, false)));
        }
    }

    /**
     * Gets the selected structure ids.<p>
     *
     * @return the set of selected structure ids
     */
    @SuppressWarnings("unchecked")
    public Set<CmsUUID> getSelectedIds() {

        return (Set<CmsUUID>)m_fileTable.getValue();
    }

    /**
     * Returns the current table state.<p>
     *
     * @return the table state
     */
    public CmsFileExplorerSettings getTableSettings() {

        CmsFileExplorerSettings fileTableState = new CmsFileExplorerSettings();

        fileTableState.setSortAscending(m_fileTable.isSortAscending());
        fileTableState.setSortColumnId((String)m_fileTable.getSortContainerPropertyId());
        List<String> collapsedCollumns = new ArrayList<String>();
        Object[] visibleCols = m_fileTable.getVisibleColumns();
        for (int i = 0; i < visibleCols.length; i++) {
            if (m_fileTable.isColumnCollapsed(visibleCols[i])) {
                collapsedCollumns.add((String)visibleCols[i]);
            }
        }
        fileTableState.setCollapsedColumns(collapsedCollumns);
        return fileTableState;
    }

    public Set<CmsUUID> getValue() {

        return (Set<CmsUUID>)m_fileTable.getValue();
    }

    public void setMenuBuilder(I_CmsContextMenuBuilder builder) {

        m_menuBuilder = builder;
    }

    /**
     * Sets the table state.<p>
     *
     * @param state the table state
     */
    public void setTableState(CmsFileExplorerSettings state) {

        if (state != null) {
            m_fileTable.setSortContainerPropertyId(state.getSortColumnId());
            m_fileTable.setSortAscending(state.isSortAscending());
            Object[] visibleCols = m_fileTable.getVisibleColumns();
            for (int i = 0; i < visibleCols.length; i++) {
                m_fileTable.setColumnCollapsed(visibleCols[i], state.getCollapsedColumns().contains(visibleCols[i]));
            }
        }
    }

    List<CmsResource> getSelectedItems() {

        List<CmsResource> result = Lists.newArrayList();
        return result;

    }
}
