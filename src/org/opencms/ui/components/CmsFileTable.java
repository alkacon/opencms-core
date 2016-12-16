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

import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_CACHE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_COPYRIGHT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_CREATED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_EXPIRED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_MODIFIED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_RELEASED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_IN_NAVIGATION;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_IS_FOLDER;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_PERMISSIONS;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_PROJECT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RESOURCE_NAME;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RESOURCE_TYPE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_SIZE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_STATE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_STATE_NAME;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_TITLE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_TYPE_ICON;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_CREATED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_LOCKED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_MODIFIED;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsEditPropertyContext;
import org.opencms.ui.actions.I_CmsDefaultAction;
import org.opencms.ui.apps.CmsFileExplorerSettings;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsResourceContextMenuBuilder;
import org.opencms.ui.contextmenu.I_CmsContextMenuBuilder;
import org.opencms.ui.util.I_CmsItemSorter;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Table for displaying resources.<p>
 */
public class CmsFileTable extends CmsResourceTable {

    /**
     * File edit handler.<p>
     */
    public class FileEditHandler implements BlurListener {

        /** The serial version id. */
        private static final long serialVersionUID = -2286815522247807054L;

        /**
         * @see com.vaadin.event.FieldEvents.BlurListener#blur(com.vaadin.event.FieldEvents.BlurEvent)
         */
        public void blur(BlurEvent event) {

            stopEdit();
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
            if (itemId.equals(getEditItemId()) && isEditProperty((CmsResourceTableProperty)propertyId)) {
                result = super.createField(container, itemId, propertyId, uiContext);
                result.addStyleName(OpenCmsTheme.INLINE_TEXTFIELD);
                result.addValidator(m_editHandler);
                if (result instanceof TextField) {
                    ((TextField)result).setComponentError(null);
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
                    ((TextField)result).setTextChangeEventMode(TextChangeEventMode.LAZY);
                    ((TextField)result).addTextChangeListener(m_editHandler);
                }
                result.focus();
            }
            return result;
        }
    }

    /**
     * Extends the default sorting to differentiate between files and folder when sorting by name.<p>
     * Also allows sorting by navPos property for the Resource icon column.<p>
     */
    public static class FileSorter extends DefaultItemSorter implements I_CmsItemSorter {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see org.opencms.ui.util.I_CmsItemSorter#getSortableContainerPropertyIds(com.vaadin.data.Container)
         */
        public Collection<?> getSortableContainerPropertyIds(Container container) {

            Set<Object> result = new HashSet<Object>();
            for (Object propId : container.getContainerPropertyIds()) {
                Class<?> propertyType = container.getType(propId);
                if (Comparable.class.isAssignableFrom(propertyType)
                    || propertyType.isPrimitive()
                    || (propId.equals(CmsResourceTableProperty.PROPERTY_TYPE_ICON)
                        && container.getContainerPropertyIds().contains(
                            CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION))) {
                    result.add(propId);
                }
            }
            return result;
        }

        /**
         * @see com.vaadin.data.util.DefaultItemSorter#compareProperty(java.lang.Object, boolean, com.vaadin.data.Item, com.vaadin.data.Item)
         */
        @Override
        protected int compareProperty(Object propertyId, boolean sortDirection, Item item1, Item item2) {

            if (CmsResourceTableProperty.PROPERTY_RESOURCE_NAME.equals(propertyId)) {
                Boolean isFolder1 = (Boolean)item1.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_IS_FOLDER).getValue();
                Boolean isFolder2 = (Boolean)item2.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_IS_FOLDER).getValue();
                if (!isFolder1.equals(isFolder2)) {
                    int result = isFolder1.booleanValue() ? -1 : 1;
                    if (!sortDirection) {
                        result = result * (-1);
                    }
                    return result;
                }
            } else if ((CmsResourceTableProperty.PROPERTY_TYPE_ICON.equals(propertyId)
                || CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT.equals(propertyId))
                && (item1.getItemProperty(CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION) != null)) {
                int result;
                Float pos1 = (Float)item1.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION).getValue();
                Float pos2 = (Float)item2.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION).getValue();
                if (pos1 == null) {
                    result = pos2 == null
                    ? compareProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME, true, item1, item2)
                    : 1;
                } else {
                    result = pos2 == null ? -1 : Float.compare(pos1.floatValue(), pos2.floatValue());
                }
                if (!sortDirection) {
                    result = result * (-1);
                }
                return result;
            }
            return super.compareProperty(propertyId, sortDirection, item1, item2);
        }
    }

    /**
     * Handles folder selects in the file table.<p>
     */
    public interface I_FolderSelectHandler {

        /**
         * Called when the folder name is left clicked.<p>
         *
         * @param folderId the selected folder id
         */
        void onFolderSelect(CmsUUID folderId);
    }

    /** The logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsFileTable.class);

    /** The default file table columns. */
    private static final Map<CmsResourceTableProperty, Integer> DEFAULT_TABLE_PROPERTIES = new LinkedHashMap<CmsResourceTableProperty, Integer>();

    /** The serial version id. */
    private static final long serialVersionUID = 5460048685141699277L;

    /** The selected resources. */
    protected List<CmsResource> m_currentResources = new ArrayList<CmsResource>();

    /** The current file property edit handler. */
    I_CmsFilePropertyEditHandler m_editHandler;

    /** File edit event handler. */
    FileEditHandler m_fileEditHandler = new FileEditHandler();

    /** The context menu. */
    CmsContextMenu m_menu;

    /** The context menu builder. */
    I_CmsContextMenuBuilder m_menuBuilder;

    /** The dialog context provider. */
    private I_CmsContextProvider m_contextProvider;

    /** The edited item id. */
    private CmsUUID m_editItemId;

    /** The edited property id. */
    private CmsResourceTableProperty m_editProperty;

    /** The folder select handler. */
    private I_FolderSelectHandler m_folderSelectHandler;

    /** The original edit value. */
    private String m_originalEditValue;

    /** The default action column property. */
    CmsResourceTableProperty m_actionColumnProperty;

    {
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_TYPE_ICON, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_PROJECT, Integer.valueOf(COLLAPSED));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_RESOURCE_NAME, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_TITLE, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_NAVIGATION_TEXT, Integer.valueOf(COLLAPSED));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_NAVIGATION_POSITION, Integer.valueOf(INVISIBLE));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_IN_NAVIGATION, Integer.valueOf(INVISIBLE));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_COPYRIGHT, Integer.valueOf(COLLAPSED));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_CACHE, Integer.valueOf(COLLAPSED));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_RESOURCE_TYPE, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_SIZE, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_PERMISSIONS, Integer.valueOf(COLLAPSED));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_DATE_MODIFIED, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_USER_MODIFIED, Integer.valueOf(COLLAPSED));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_DATE_CREATED, Integer.valueOf(COLLAPSED));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_USER_CREATED, Integer.valueOf(COLLAPSED));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_DATE_RELEASED, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_DATE_EXPIRED, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_STATE_NAME, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_USER_LOCKED, Integer.valueOf(0));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_IS_FOLDER, Integer.valueOf(INVISIBLE));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_STATE, Integer.valueOf(INVISIBLE));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_INSIDE_PROJECT, Integer.valueOf(INVISIBLE));
        DEFAULT_TABLE_PROPERTIES.put(PROPERTY_RELEASED_NOT_EXPIRED, Integer.valueOf(INVISIBLE));
    }

    /**
     * Default constructor.<p>
     *
     * @param contextProvider the dialog context provider
     */
    public CmsFileTable(I_CmsContextProvider contextProvider) {
        super();
        m_actionColumnProperty = PROPERTY_RESOURCE_NAME;
        m_contextProvider = contextProvider;
        m_container.setItemSorter(new FileSorter());
        m_fileTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        m_fileTable.addStyleName(OpenCmsTheme.SIMPLE_DRAG);
        m_fileTable.setSizeFull();
        m_fileTable.setColumnCollapsingAllowed(true);
        m_fileTable.setSelectable(true);
        m_fileTable.setMultiSelect(true);

        m_fileTable.setTableFieldFactory(new FileFieldFactory());
        ColumnBuilder builder = new ColumnBuilder();
        for (Entry<CmsResourceTableProperty, Integer> entry : DEFAULT_TABLE_PROPERTIES.entrySet()) {
            builder.column(entry.getKey(), entry.getValue().intValue());
        }
        builder.buildColumns();

        m_fileTable.setSortContainerPropertyId(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME);
        m_menu = new CmsContextMenu();
        m_fileTable.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                @SuppressWarnings("unchecked")
                Set<CmsUUID> selectedIds = (Set<CmsUUID>)event.getProperty().getValue();
                List<CmsResource> selectedResources = new ArrayList<CmsResource>();
                for (CmsUUID id : selectedIds) {
                    try {
                        CmsResource resource = A_CmsUI.getCmsObject().readResource(id, CmsResourceFilter.ALL);
                        selectedResources.add(resource);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }

                }
                m_currentResources = selectedResources;

                if (!selectedIds.isEmpty() && (m_menuBuilder != null)) {
                    m_menu.removeAllItems();
                    m_menuBuilder.buildContextMenu(getContextProvider().getDialogContext(), m_menu);
                }
            }
        });

        m_fileTable.addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                handleFileItemClick(event);
            }
        });

        m_fileTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                Item item = m_container.getItem(itemId);
                String style = getStateStyle(item);
                if (m_actionColumnProperty == propertyId) {
                    style += " " + OpenCmsTheme.HOVER_COLUMN;
                } else if ((CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT == propertyId)
                    || (CmsResourceTableProperty.PROPERTY_TITLE == propertyId)) {
                    if ((item.getItemProperty(CmsResourceTableProperty.PROPERTY_IN_NAVIGATION) != null)
                        && ((Boolean)item.getItemProperty(
                            CmsResourceTableProperty.PROPERTY_IN_NAVIGATION).getValue()).booleanValue()) {
                        style += " " + OpenCmsTheme.IN_NAVIGATION;
                    }
                }
                return style;
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
            if ((resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT) == null)
                || ((Boolean)resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT).getValue()).booleanValue()) {

                CmsResourceState state = (CmsResourceState)resourceItem.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_STATE).getValue();
                result = getStateStyle(state);
            } else {
                result = OpenCmsTheme.PROJECT_OTHER;
            }
            if ((resourceItem.getItemProperty(PROPERTY_RELEASED_NOT_EXPIRED) != null)
                && !((Boolean)resourceItem.getItemProperty(PROPERTY_RELEASED_NOT_EXPIRED).getValue()).booleanValue()) {
                result += " " + OpenCmsTheme.EXPIRED;
            }
            if ((resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_DISABLED) != null)
                && ((Boolean)resourceItem.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_DISABLED).getValue()).booleanValue()) {
                result += " " + OpenCmsTheme.DISABLED;
            }
        }
        return result;
    }

    /**
     * Applies settings generally used within workplace app file lists.<p>
     */
    public void applyWorkplaceAppSettings() {

        // add site path property to container
        m_container.addContainerProperty(
            CmsResourceTableProperty.PROPERTY_SITE_PATH,
            CmsResourceTableProperty.PROPERTY_SITE_PATH.getColumnType(),
            CmsResourceTableProperty.PROPERTY_SITE_PATH.getDefaultValue());

        // replace the resource name column with the path column
        Object[] visibleCols = m_fileTable.getVisibleColumns();
        for (int i = 0; i < visibleCols.length; i++) {
            if (CmsResourceTableProperty.PROPERTY_RESOURCE_NAME.equals(visibleCols[i])) {
                visibleCols[i] = CmsResourceTableProperty.PROPERTY_SITE_PATH;
            }
        }
        m_fileTable.setVisibleColumns(visibleCols);
        m_fileTable.setColumnCollapsible(CmsResourceTableProperty.PROPERTY_SITE_PATH, false);
        m_fileTable.setColumnHeader(
            CmsResourceTableProperty.PROPERTY_SITE_PATH,
            CmsVaadinUtils.getMessageText(CmsResourceTableProperty.PROPERTY_SITE_PATH.getHeaderKey()));

        // update column visibility according to the latest file explorer settings
        CmsFileExplorerSettings settings;
        try {
            settings = OpenCms.getWorkplaceAppManager().getAppSettings(
                A_CmsUI.getCmsObject(),
                CmsFileExplorerSettings.class);

            setTableState(settings);
        } catch (Exception e) {
            LOG.error("Error while reading file explorer settings from user.", e);
        }
        m_fileTable.setSortContainerPropertyId(CmsResourceTableProperty.PROPERTY_SITE_PATH);
        setActionColumnProperty(CmsResourceTableProperty.PROPERTY_SITE_PATH);
        setMenuBuilder(new CmsResourceContextMenuBuilder());
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
                    new SimpleStringFilter(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME, search, true, false),
                    new SimpleStringFilter(CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT, search, true, false),
                    new SimpleStringFilter(CmsResourceTableProperty.PROPERTY_TITLE, search, true, false)));
        }
    }

    /**
     * Returns the index of the first visible item.<p>
     *
     * @return the first visible item
     */
    public int getFirstVisibleItemIndex() {

        return m_fileTable.getCurrentPageFirstItemIndex();
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
        fileTableState.setSortColumnId((CmsResourceTableProperty)m_fileTable.getSortContainerPropertyId());
        List<CmsResourceTableProperty> collapsedCollumns = new ArrayList<CmsResourceTableProperty>();
        Object[] visibleCols = m_fileTable.getVisibleColumns();
        for (int i = 0; i < visibleCols.length; i++) {
            if (m_fileTable.isColumnCollapsed(visibleCols[i])) {
                collapsedCollumns.add((CmsResourceTableProperty)visibleCols[i]);
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
    public boolean isEditProperty(CmsResourceTableProperty propertyId) {

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
     * Sets the default action column property.<p>
     *
     * @param actionColumnProperty the default action column property
     */
    public void setActionColumnProperty(CmsResourceTableProperty actionColumnProperty) {

        m_actionColumnProperty = actionColumnProperty;
    }

    /**
     * Sets the dialog context provider.<p>
     *
     * @param provider the dialog context provider
     */
    public void setContextProvider(I_CmsContextProvider provider) {

        m_contextProvider = provider;
    }

    /**
     * Sets the first visible item index.<p>
     *
     * @param i the item index
     */
    public void setFirstVisibleItemIndex(int i) {

        m_fileTable.setCurrentPageFirstItemIndex(i);
    }

    /**
     * Sets the folder select handler.<p>
     *
     * @param folderSelectHandler the folder select handler
     */
    public void setFolderSelectHandler(I_FolderSelectHandler folderSelectHandler) {

        m_folderSelectHandler = folderSelectHandler;
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
    public void startEdit(
        CmsUUID itemId,
        CmsResourceTableProperty propertyId,
        I_CmsFilePropertyEditHandler editHandler) {

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
     * @param id the resource structure id to update
     * @param remove true if the item should be removed only
     */
    public void update(CmsUUID id, boolean remove) {

        updateItem(id, remove);

    }

    /**
     * Updates the column widths.<p>
     *
     * The reason this is needed is that the Vaadin table does not support minimum widths for columns,
     * so expanding columns get squished when most of the horizontal space is used by other columns.
     * So we try to determine whether the expanded columns would have enough space, and if not, give them a
     * fixed width.
     *
     * @param estimatedSpace the estimated horizontal space available for the table.
     */
    public void updateColumnWidths(int estimatedSpace) {

        Object[] cols = m_fileTable.getVisibleColumns();
        List<CmsResourceTableProperty> expandCols = Lists.newArrayList();
        int nonExpandWidth = 0;
        int totalExpandMinWidth = 0;
        for (Object colObj : cols) {
            if (m_fileTable.isColumnCollapsed(colObj)) {
                continue;
            }
            CmsResourceTableProperty prop = (CmsResourceTableProperty)colObj;
            if (0 < m_fileTable.getColumnExpandRatio(prop)) {
                expandCols.add(prop);
                totalExpandMinWidth += getAlternativeWidthForExpandingColumns(prop);
            } else {
                nonExpandWidth += prop.getColumnWidth();
            }
        }
        if (estimatedSpace < (totalExpandMinWidth + nonExpandWidth)) {
            for (CmsResourceTableProperty expandCol : expandCols) {
                m_fileTable.setColumnWidth(expandCol, getAlternativeWidthForExpandingColumns(expandCol));
            }
        }
    }

    /**
     * Updates the file table sorting.<p>
     */
    public void updateSorting() {

        m_fileTable.sort();
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
     * Returns the dialog context provider.<p>
     *
     * @return the dialog context provider
     */
    I_CmsContextProvider getContextProvider() {

        return m_contextProvider;
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
    CmsResourceTableProperty getEditProperty() {

        return m_editProperty;
    }

    /**
     * Handles the file table item click.<p>
     *
     * @param event the click event
     */
    void handleFileItemClick(ItemClickEvent event) {

        if (isEditing()) {
            stopEdit();

        } else if (!event.isCtrlKey() && !event.isShiftKey()) {
            CmsUUID itemId = (CmsUUID)event.getItemId();
            boolean openedFolder = false;
            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT)) {
                handleSelection(itemId);
                openContextMenu(event);
            } else {
                if ((event.getPropertyId() == null)
                    || CmsResourceTableProperty.PROPERTY_TYPE_ICON.equals(event.getPropertyId())) {
                    openContextMenu(event);
                } else {
                    if (m_actionColumnProperty.equals(event.getPropertyId())) {
                        Boolean isFolder = (Boolean)event.getItem().getItemProperty(
                            CmsResourceTableProperty.PROPERTY_IS_FOLDER).getValue();
                        if ((isFolder != null) && isFolder.booleanValue()) {
                            if (m_folderSelectHandler != null) {
                                m_folderSelectHandler.onFolderSelect(itemId);
                            }
                            openedFolder = true;
                        } else {
                            try {
                                CmsObject cms = A_CmsUI.getCmsObject();
                                CmsResource res = cms.readResource(itemId, CmsResourceFilter.IGNORE_EXPIRATION);
                                m_currentResources = Collections.singletonList(res);
                                I_CmsDialogContext context = m_contextProvider.getDialogContext();
                                I_CmsDefaultAction action = OpenCms.getWorkplaceAppManager().getDefaultAction(context);
                                if (action != null) {
                                    action.executeAction(context);
                                    return;
                                }
                            } catch (CmsVfsResourceNotFoundException e) {
                                LOG.info(e.getLocalizedMessage(), e);
                            } catch (CmsException e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    } else {
                        I_CmsDialogContext context = m_contextProvider.getDialogContext();
                        if ((m_currentResources.size() == 1)
                            && m_currentResources.get(0).getStructureId().equals(itemId)
                            && (context instanceof I_CmsEditPropertyContext)
                            && ((I_CmsEditPropertyContext)context).isPropertyEditable(event.getPropertyId())) {

                            ((I_CmsEditPropertyContext)context).editProperty(event.getPropertyId());
                        }
                    }
                }
            }
            // update the item on click to show any available changes
            if (!openedFolder) {
                update(itemId, false);
            }
        }
    }

    /**
     * Clears the current edit process.<p>
     */
    private void clearEdit() {

        m_fileTable.setEditable(false);
        if (m_editItemId != null) {
            updateItem(m_editItemId, false);
        }
        m_editItemId = null;
        m_editProperty = null;
        m_editHandler = null;
        updateSorting();
    }

    /**
     * Gets alternative width for expanding table columns which is used when there is not enough space for
     * all visible columns.<p>
     *
     * @param prop the table property
     * @return the alternative column width
     */
    private int getAlternativeWidthForExpandingColumns(CmsResourceTableProperty prop) {

        if (prop.getId().equals(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME.getId())) {
            return 200;
        }
        if (prop.getId().equals(CmsResourceTableProperty.PROPERTY_TITLE.getId())) {
            return 300;
        }
        if (prop.getId().equals(CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT.getId())) {
            return 200;
        }
        return 200;
    }

    /**
     * Updates the given item in the file table.<p>
     *
     * @param itemId the item id
     * @param remove true if the item should be removed only
     */
    private void updateItem(CmsUUID itemId, boolean remove) {

        if (remove) {
            m_container.removeItem(itemId);
            return;
        }

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource resource = cms.readResource(itemId, CmsResourceFilter.ALL);
            fillItem(cms, resource, OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));

        } catch (CmsVfsResourceNotFoundException e) {
            m_container.removeItem(itemId);
            LOG.debug("Failed to update file table item, removing it from view.", e);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
