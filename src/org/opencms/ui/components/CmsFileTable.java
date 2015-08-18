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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsCustomComponent;
import org.opencms.ui.A_CmsUI;
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

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Table for displaying resources.<p>
 */
public class CmsFileTable extends A_CmsCustomComponent {

    /**
     * File edit handler.<p>
     */
    public class FileEditHandler implements BlurListener, Validator {

        /** The serial version id. */
        private static final long serialVersionUID = -2286815522247807054L;

        /**
         * @see com.vaadin.event.FieldEvents.BlurListener#blur(com.vaadin.event.FieldEvents.BlurEvent)
         */
        public void blur(BlurEvent event) {

            stopEdit();
        }

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (m_editHandler != null) {
                m_editHandler.validate((String)value);
            }
        }
    }

    /**
     * Field factory to enable inline editing of individual file properties.<p>
     */
    public class FileFieldFactory extends DefaultFieldFactory {

        /** The serial version id. */
        private static final long serialVersionUID = 3079590603587933576L;

        /**
         * @see com.vaadin.ui.DefaultFieldFactory#createField(com.vaadin.data.Container, java.lang.Object, java.lang.Object, com.vaadin.ui.Component)
         */
        @Override
        public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {

            Field<?> result = null;
            if (itemId.equals(getEditItemId()) && isEditProperty((String)propertyId)) {
                result = super.createField(container, itemId, propertyId, uiContext);
                result.addStyleName(OpenCmsTheme.INLINE_TEXTFIELD);
                result.addValidator(m_fileEditHandler);
                if (result instanceof TextField) {
                    ((TextField)result).addShortcutListener(new ShortcutListener("Cancel edit", KeyCode.ESCAPE, null) {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public void handleAction(Object sender, Object target) {

                            cancelEdit();
                        }
                    });
                    ((TextField)result).addShortcutListener(new ShortcutListener("Save", KeyCode.ENTER, null) {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public void handleAction(Object sender, Object target) {

                            stopEdit();
                        }
                    });
                    ((TextField)result).addBlurListener(m_fileEditHandler);
                }
                result.focus();
            }
            return result;
        }
    }

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
    public static final String PROPERTY_NAVIGATION_TEXT = CmsPropertyDefinition.PROPERTY_NAVTEXT;

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
    public static final String PROPERTY_TITLE = CmsPropertyDefinition.PROPERTY_TITLE;

    /** File table property name. */
    public static final String PROPERTY_TYPE_ICON = "typeIcon";

    /** File table property name. */
    public static final String PROPERTY_USER_CREATED = "userCreated";

    /** File table property name. */
    public static final String PROPERTY_USER_LOCKED = "userLocked";

    /** File table property name. */
    public static final String PROPERTY_USER_MODIFIED = "userModified";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFileTable.class);

    /** The serial version id. */
    private static final long serialVersionUID = 5460048685141699277L;

    /** The selected resources. */
    protected List<CmsResource> m_currentResources = Lists.newArrayList();

    /** The resource data container. */
    IndexedContainer m_container;

    /** The current file property edit handler. */
    I_CmsFilePropertyEditHandler m_editHandler;

    /** File edit event handler. */
    FileEditHandler m_fileEditHandler = new FileEditHandler();

    /** The table used to display the resource data. */
    Table m_fileTable;

    /** The context menu. */
    CmsContextMenu m_menu;

    /** The context menu builder. */
    I_CmsContextMenuBuilder m_menuBuilder;

    /** The edited item id. */
    private CmsUUID m_editItemId;

    /** The edited property id. */
    private String m_editProperty;

    /** The original edit value. */
    private String m_originalEditValue;

    /** The messages. */
    private CmsMessages messages;

    /**
     * Default constructor.<p>
     */
    public CmsFileTable() {

        super();
        m_container = new IndexedContainer();
        m_container.addContainerProperty(PROPERTY_TYPE_ICON, Component.class, null);
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
        m_fileTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        m_fileTable.setSizeFull();
        m_fileTable.setColumnCollapsingAllowed(true);
        m_fileTable.setSelectable(true);
        m_fileTable.setMultiSelect(true);
        m_fileTable.setTableFieldFactory(new FileFieldFactory());

        m_fileTable.setContainerDataSource(m_container);

        // following also sets the column order
        m_fileTable.setVisibleColumns(
            PROPERTY_TYPE_ICON,
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
            "", // icon column has no header
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

        m_fileTable.setRowHeaderMode(RowHeaderMode.HIDDEN);

        // setting icon column width explicitly
        m_fileTable.setColumnWidth(PROPERTY_TYPE_ICON, 40);

        m_fileTable.setSortContainerPropertyId(PROPERTY_RESOURCE_NAME);

        m_fileTable.setColumnCollapsed(PROPERTY_NAVIGATION_TEXT, true);
        m_fileTable.setColumnCollapsed(PROPERTY_PERMISSIONS, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_MODIFIED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_DATE_CREATED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_CREATED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_STATE_NAME, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_LOCKED, true);

        m_menu = new CmsContextMenu();
        m_fileTable.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                @SuppressWarnings("unchecked")
                Set<CmsUUID> selectedIds = (Set<CmsUUID>)event.getProperty().getValue();
                List<CmsResource> selectedResources = Lists.newArrayList();
                for (CmsUUID id : selectedIds) {
                    try {
                        A_CmsUI.get();
                        CmsResource resource = A_CmsUI.getCmsObject().readResource(id, CmsResourceFilter.ALL);
                        selectedResources.add(resource);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }

                }
                m_currentResources = selectedResources;

                if (!selectedIds.isEmpty()) {
                    m_menu.removeAllItems();
                    m_menuBuilder.buildContextMenu(selectedResources, m_menu);
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
                    result = OpenCmsTheme.STATE_DELETED;
                } else if (state.isNew()) {
                    result = OpenCmsTheme.STATE_NEW;
                } else if (state.isChanged()) {
                    result = OpenCmsTheme.STATE_CHANGED;
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
        for (CmsResource resource : resources) {
            fillItem(cms, resource, wpLocale);
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
     * Gets the list of selected resources.<p>
     *
     * @return the list of selected resources
     */
    public List<CmsResource> getSelectedResources() {

        return m_currentResources;
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

    /**
     * Handles the item selection.<p>
     *
     * @param itemId the selected item id
     */
    public void handleSelection(CmsUUID itemId) {

        Set<CmsUUID> selection = getSelectedIds();
        if (selection == null) {
            m_fileTable.select(itemId);
        } else if (!selection.contains(itemId)) {
            m_fileTable.setValue(null);
            m_fileTable.select(itemId);
        }
    }

    /**
     * Returns if a file property is being edited.<p>
     * @return <code>true</code> if a file property is being edited
     */
    public boolean isEditing() {

        return m_editItemId != null;
    }

    /**
     * Returns if the given property is being edited.<p>
     *
     * @param propertyId the property id
     *
     * @return <code>true</code> if the given property is being edited
     */
    public boolean isEditProperty(String propertyId) {

        return (m_editProperty != null) && m_editProperty.equals(propertyId);
    }

    /**
     * Opens the context menu.<p>
     *
     * @param event the click event
     */
    public void openContextMenu(ItemClickEvent event) {

        m_menu.openForTable(event, m_fileTable);
    }

    /**
     * Sets the menu builder.<p>
     *
     * @param builder the menu builder
     */
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

    /**
     * Starts inline editing of the given file property.<p>
     *
     * @param itemId the item resource structure id
     * @param propertyId the property to edit
     * @param editHandler the edit handler
     */
    public void startEdit(CmsUUID itemId, String propertyId, I_CmsFilePropertyEditHandler editHandler) {

        m_editItemId = itemId;
        m_editProperty = propertyId;
        m_originalEditValue = (String)m_container.getItem(m_editItemId).getItemProperty(m_editProperty).getValue();
        m_editHandler = editHandler;
        m_fileTable.setEditable(true);

    }

    /**
     * Stops the current edit process to save the changed property value.<p>
     */
    public void stopEdit() {

        if (m_editHandler != null) {
            String value = (String)m_container.getItem(m_editItemId).getItemProperty(m_editProperty).getValue();
            if (!value.equals(m_originalEditValue)) {
                m_editHandler.validate(value);
                m_editHandler.save(value);
            } else {
                // call cancel to ensure unlock
                m_editHandler.cancel();
            }
        }
        clearEdit();
    }

    /**
     * Updates all items with ids from the given list.<p>
     *
     * @param ids the list of resource structure ids to update
     */
    public void update(List<CmsUUID> ids) {

        for (CmsUUID id : ids) {
            updateItem(id);
        }
    }

    /**
     * Cancels the current edit process.<p>
     */
    void cancelEdit() {

        if (m_editHandler != null) {
            m_editHandler.cancel();
        }
        clearEdit();
    }

    /**
     * Returns the edit item id.<p>
     *
     * @return the edit item id
     */
    CmsUUID getEditItemId() {

        return m_editItemId;
    }

    /**
     * Returns the edit property id.<p>
     *
     * @return the edit property id
     */
    String getEditProperty() {

        return m_editProperty;
    }

    /**
     * Clears the current edit process.<p>
     */
    private void clearEdit() {

        m_fileTable.setEditable(false);
        if (m_editItemId != null) {
            updateItem(m_editItemId);
        }
        m_editItemId = null;
        m_editProperty = null;
        m_editHandler = null;

    }

    /**
     * Fills the file item data.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param locale the workplace locale
     */
    private void fillItem(CmsObject cms, CmsResource resource, Locale locale) {

        Item resourceItem = m_container.getItem(resource.getStructureId());
        if (resourceItem == null) {
            resourceItem = m_container.addItem(resource.getStructureId());
        }
        CmsResourceUtil resUtil = new CmsResourceUtil(cms);
        resUtil.setResource(resource);
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        resourceItem.getItemProperty(PROPERTY_TYPE_ICON).setValue(
            new CmsResourceIcon(
                CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getBigIcon()),
                resUtil.getLockState(),
                resource.getState()));
        resourceItem.getItemProperty(PROPERTY_RESOURCE_NAME).setValue(resource.getName());
        resourceItem.getItemProperty(PROPERTY_TITLE).setValue(resUtil.getTitle());
        resourceItem.getItemProperty(PROPERTY_NAVIGATION_TEXT).setValue(resUtil.getNavText());
        resourceItem.getItemProperty(PROPERTY_RESOURCE_TYPE).setValue(
            CmsWorkplaceMessages.getResourceTypeName(locale, type.getTypeName()));
        resourceItem.getItemProperty(PROPERTY_IS_FOLDER).setValue(Boolean.valueOf(resource.isFolder()));
        if (resource.isFile()) {
            resourceItem.getItemProperty(PROPERTY_SIZE).setValue(Integer.valueOf(resource.getLength()));
        }
        resourceItem.getItemProperty(PROPERTY_PERMISSIONS).setValue(resUtil.getPermissionString());
        resourceItem.getItemProperty(PROPERTY_DATE_MODIFIED).setValue(
            messages.getDateTime(resource.getDateLastModified()));
        resourceItem.getItemProperty(PROPERTY_USER_MODIFIED).setValue(resUtil.getUserLastModified());
        resourceItem.getItemProperty(PROPERTY_DATE_CREATED).setValue(messages.getDateTime(resource.getDateCreated()));
        resourceItem.getItemProperty(PROPERTY_USER_CREATED).setValue(resUtil.getUserCreated());
        resourceItem.getItemProperty(PROPERTY_DATE_RELEASED).setValue(resUtil.getDateReleased());
        resourceItem.getItemProperty(PROPERTY_DATE_EXPIRED).setValue(resUtil.getDateExpired());
        resourceItem.getItemProperty(PROPERTY_STATE_NAME).setValue(resUtil.getStateName());
        resourceItem.getItemProperty(PROPERTY_STATE).setValue(resource.getState());
        resourceItem.getItemProperty(PROPERTY_USER_LOCKED).setValue(resUtil.getLockedByName());
    }

    /**
     * Updates the given item in the file table.<p>
     *
     * @param itemId the item id
     */
    private void updateItem(CmsUUID itemId) {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource resource = cms.readResource(itemId, CmsResourceFilter.ALL);
            fillItem(cms, resource, OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (CmsVfsResourceNotFoundException e) {
            m_container.removeItem(itemId);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
