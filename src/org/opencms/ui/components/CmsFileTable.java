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

import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_CREATED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_EXPIRED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_MODIFIED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_RELEASED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_IS_FOLDER;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_PERMISSIONS;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_PROJECT;
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
import org.opencms.ui.I_CmsContextMenuBuilder;
import org.opencms.ui.apps.CmsFileExplorerSettings;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

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
     */
    public static class FileSorter extends DefaultItemSorter {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

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
            }
            return super.compareProperty(propertyId, sortDirection, item1, item2);
        }
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFileTable.class);

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

    /** The edited item id. */
    private CmsUUID m_editItemId;

    /** The edited property id. */
    private CmsResourceTableProperty m_editProperty;

    /** The original edit value. */
    private String m_originalEditValue;

    /**
     * Default constructor.<p>
     */
    public CmsFileTable() {

        super();
        m_container.setItemSorter(new FileSorter());
        m_fileTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        m_fileTable.addStyleName(OpenCmsTheme.SIMPLE_DRAG);
        m_fileTable.setSizeFull();
        m_fileTable.setColumnCollapsingAllowed(true);
        m_fileTable.setSelectable(true);
        m_fileTable.setMultiSelect(true);
        m_fileTable.setTableFieldFactory(new FileFieldFactory());
        new ColumnBuilder() {

            {
                column(PROPERTY_TYPE_ICON);
                column(PROPERTY_PROJECT);
                column(PROPERTY_RESOURCE_NAME);
                column(PROPERTY_TITLE);
                column(PROPERTY_NAVIGATION_TEXT, COLLAPSED);
                column(PROPERTY_RESOURCE_TYPE);
                column(PROPERTY_SIZE);
                column(PROPERTY_PERMISSIONS, COLLAPSED);
                column(PROPERTY_DATE_MODIFIED);
                column(PROPERTY_USER_MODIFIED, COLLAPSED);
                column(PROPERTY_DATE_CREATED, COLLAPSED);
                column(PROPERTY_USER_CREATED, COLLAPSED);
                column(PROPERTY_DATE_RELEASED);
                column(PROPERTY_DATE_EXPIRED);
                column(PROPERTY_STATE_NAME);
                column(PROPERTY_USER_LOCKED);
                column(PROPERTY_IS_FOLDER, INVISIBLE);
                column(PROPERTY_STATE, INVISIBLE);
                column(PROPERTY_INSIDE_PROJECT, INVISIBLE);
            }

        }.buildColumns();

        m_fileTable.setSortContainerPropertyId(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME);
        m_menu = new CmsContextMenu();
        m_fileTable.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
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
                    m_menuBuilder.buildContextMenu(selectedResources, m_menu);
                }
            }
        });

        m_fileTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                return getStateStyle(m_container.getItem(itemId))
                    + (CmsResourceTableProperty.PROPERTY_RESOURCE_NAME == propertyId
                    ? " " + OpenCmsTheme.HOVER_COLUMN
                    : "");
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
            if (((Boolean)resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT).getValue()).booleanValue()) {

                CmsResourceState state = (CmsResourceState)resourceItem.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_STATE).getValue();
                result = getStateStyle(state);
            } else {
                result = OpenCmsTheme.PROJECT_OTHER;
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
