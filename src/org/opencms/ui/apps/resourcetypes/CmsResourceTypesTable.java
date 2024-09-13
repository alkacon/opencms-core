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

package org.opencms.ui.apps.resourcetypes;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.A_CmsResourceTypeFolderBase;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.CmsEditorConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.search.CmsSearchReplaceSettings;
import org.opencms.ui.apps.search.CmsSourceSearchApp;
import org.opencms.ui.apps.search.CmsSourceSearchAppConfiguration;
import org.opencms.ui.apps.search.CmsSourceSearchForm.SearchType;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Table for resource types on the system.<p>
 */
public class CmsResourceTypesTable extends Table {

    /**
     * The delete project context menu entry.<p>
     */
    class DeleteEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> data) {

            try {
                final Window window = CmsBasicDialog.prepareWindow();
                Iterator<String> it = data.iterator();
                boolean existResources = false;
                while (it.hasNext()) {
                    I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(it.next());
                    if (m_cms.readResources("", CmsResourceFilter.requireType(type), true).size() > 0) {
                        existResources = true;
                    }
                }

                if (existResources) {
                    CmsConfirmationDialog.show(
                        CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_DELETE_NOT_POSSIBLE_0),
                        CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_DELETE_NOT_POSSIBLE_LONG_0),
                        new Runnable() {

                            public void run() {

                                window.close();
                            }
                        },
                        null,
                        true);

                } else {
                    CmsConfirmationDialog.show(
                        CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_DELETE_CONFIRM_0),
                        CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_DELETE_CONFIRM_LONG_0),
                        new Runnable() {

                            public void run() {

                                try {
                                    Iterator<String> it = data.iterator();
                                    Map<String, CmsModule> modulesToBeUpdated = new HashMap<String, CmsModule>();
                                    while (it.hasNext()) {
                                        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                                            it.next());
                                        CmsModule module;
                                        if (!modulesToBeUpdated.containsKey(type.getModuleName())) {
                                            modulesToBeUpdated.put(
                                                type.getModuleName(),
                                                OpenCms.getModuleManager().getModule(type.getModuleName()).clone());
                                        }
                                        module = modulesToBeUpdated.get(type.getModuleName());

                                        List<CmsExplorerTypeSettings> typeSettings = Lists.newArrayList(
                                            module.getExplorerTypes());
                                        List<CmsExplorerTypeSettings> newTypeSettings = new ArrayList<CmsExplorerTypeSettings>();
                                        for (CmsExplorerTypeSettings setting : typeSettings) {
                                            if (!setting.getName().equals(type.getTypeName())) {
                                                newTypeSettings.add(setting);
                                            }
                                        }
                                        OpenCms.getWorkplaceManager().removeExplorerTypeSettings(module);

                                        List<I_CmsResourceType> types = new ArrayList<I_CmsResourceType>(
                                            module.getResourceTypes());

                                        types.remove(type);

                                        module.setResourceTypes(types);

                                        module.setExplorerTypes(newTypeSettings);

                                    }
                                    for (String moduleName : modulesToBeUpdated.keySet()) {
                                        OpenCms.getModuleManager().updateModule(
                                            m_cms,
                                            modulesToBeUpdated.get(moduleName));
                                        OpenCms.getResourceManager().initialize(m_cms);
                                        OpenCms.getWorkplaceManager().addExplorerTypeSettings(
                                            modulesToBeUpdated.get(moduleName));
                                    }
                                    // re-initialize the workplace
                                    OpenCms.getWorkplaceManager().initialize(m_cms);
                                } catch (CmsException e) {
                                    LOG.error("Unable to delete resource type", e);
                                }
                                window.close();
                                m_app.reload();
                            }
                        },
                        new Runnable() {

                            public void run() {

                                window.close();
                            }
                        });
                }

            } catch (CmsException e) {
                LOG.error("Unable to delete resource type", e);
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_DELETE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            try {
                Iterator<String> it = data.iterator();
                while (it.hasNext()) {
                    I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(it.next());
                    if (isCoreType(type)) {
                        return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                            Messages.GUI_RESOURCETYPE_APP_TABLE_NOT_AVAILABLE_CORE_0);
                    }
                }
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            } catch (CmsException e) {
                LOG.error("Unable to read resourcetype", e);
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
        }
    }

    /**
     * The delete project context menu entry.<p>
     */
    class EditEntry implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> data) {

            openEditDialog(data.iterator().next());
        }

        public String getStyles() {

            return ValoTheme.LABEL_BOLD;
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if (data.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                    Messages.GUI_RESOURCETYPE_APP_TABLE_NO_AVAILABLE_MULTIPLE_0);
            }
            String typeName = data.iterator().next();
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeName);
                return isCoreType(type)
                ? CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                    Messages.GUI_RESOURCETYPE_APP_TABLE_NOT_AVAILABLE_CORE_0)
                : CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            } catch (CmsLoaderException e) {
                LOG.error("Unable to read resource type by name", e);
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
        }
    }

    /**
     * The delete project context menu entry.<p>
     */
    class MoveEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> data) {

            try {
                Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(data.iterator().next());
                window.setContent(new CmsMoveResourceTypeDialog(window, type));
                String moduleName = type.getModuleName();
                window.setCaption(
                    CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_MOVE_WINDOW_CAPTION_1, moduleName));
                A_CmsUI.get().addWindow(window);
            } catch (CmsLoaderException e) {
                LOG.error("Unable to read resource type by name", e);
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_MOVE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if (data.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                    Messages.GUI_RESOURCETYPE_APP_TABLE_NO_AVAILABLE_MULTIPLE_0);
            }
            try {
                Iterator<String> it = data.iterator();
                while (it.hasNext()) {
                    I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(it.next());
                    if (isCoreType(type)) {
                        return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                            Messages.GUI_RESOURCETYPE_APP_TABLE_NOT_AVAILABLE_CORE_0);
                    }
                }
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            } catch (CmsException e) {
                LOG.error("Unable to read resourcetype", e);
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
        }

    }

    /**
     * The menu entry to switch to the explorer of concerning site.<p>
     */
    class SchemaEditorEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(data.iterator().next());
                if (type instanceof CmsResourceTypeXmlContent) {
                    CmsResourceTypeXmlContent typeXML = (CmsResourceTypeXmlContent)type;

                    CmsResource resource = m_cms.readResource(typeXML.getSchema());
                    String editState = CmsEditor.getEditState(
                        resource.getStructureId(),
                        false,
                        UI.getCurrent().getPage().getLocation().toString());

                    CmsAppWorkplaceUi.get().showApp(
                        OpenCms.getWorkplaceAppManager().getAppConfiguration(CmsEditorConfiguration.APP_ID),
                        editState);
                }

            } catch (CmsLoaderException e) {
                LOG.error("Unable to read resource type", e);
            } catch (CmsException e) {
                LOG.error("Unable to read schema file", e);
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_SCHEMA_DEFINITION_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if (data.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                    Messages.GUI_RESOURCETYPE_APP_TABLE_NO_AVAILABLE_MULTIPLE_0);
            }
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(data.iterator().next());
                if (type instanceof CmsResourceTypeXmlContent) {
                    CmsResourceTypeXmlContent typeXML = (CmsResourceTypeXmlContent)type;

                    try {
                        m_cms.readResource(typeXML.getSchema());
                    } catch (CmsException e) {
                        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                    }
                    return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
                }

            } catch (CmsLoaderException e) {
                LOG.error("Unable to read resource type", e);
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

    }

    /**
     * The menu entry to switch to the explorer of concerning site.<p>
     */
    class SearchEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            CmsSearchReplaceSettings settings = new CmsSearchReplaceSettings();
            settings.setPaths(Collections.singletonList("/"));
            settings.setSiteRoot(m_cms.getRequestContext().getSiteRoot());
            settings.setSearchpattern(".*");
            settings.setTypes(data.iterator().next());
            settings.setType(SearchType.fullText);
            CmsAppWorkplaceUi.get().showApp(
                CmsSourceSearchAppConfiguration.APP_ID,
                CmsSourceSearchApp.generateState(settings));

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_SEARCH_RESOURCES_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if (data.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                    Messages.GUI_RESOURCETYPE_APP_TABLE_NO_AVAILABLE_MULTIPLE_0);
            }
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(data.iterator().next());

                return type instanceof A_CmsResourceTypeFolderBase
                ? CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE
                : CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            } catch (CmsLoaderException e) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
        }

    }

    /**
     * All table properties.<p>
     */
    enum TableProperty {

        /**Icon.*/
        Icon(null, Resource.class, null, false),

        /**Is Broadcast send but not displayed.*/
        ID(Messages.GUI_RESOURCETYPE_ID_0, Integer.class, null, false),

        /**Icon column.*/
        Module(Messages.GUI_RESOURCETYPE_MODULE_0, String.class, "", false),

        /**Icon column.*/
        Name(Messages.GUI_RESOURCETYPE_EDIT_DISPLAY_NAME_0, String.class, "", false),

        /**Icon column.*/
        ShortName(Messages.GUI_RESOURCETYPE_EDIT_SHORT_NAME_0, String.class, "", false);

        /**Indicates if column is collapsable.*/
        private boolean m_collapsable;

        /**Default value for column.*/
        private Object m_defaultValue;

        /**Header Message key.*/
        private String m_headerMessage;

        /**Type of column property.*/
        private Class<?> m_type;

        /**
         * constructor.
         *
         * @param headerMessage key
         * @param type to property
         * @param defaultValue of column
         * @param collapsable should this column be collapsable?
         */
        TableProperty(String headerMessage, Class<?> type, Object defaultValue, boolean collapsable) {

            m_headerMessage = headerMessage;
            m_type = type;
            m_defaultValue = defaultValue;
            m_collapsable = collapsable;
        }

        /**
         * Returns list of all properties with non-empty header.<p>
         *
         * @return list of properties
         */
        static List<TableProperty> withHeader() {

            List<TableProperty> props = new ArrayList<TableProperty>();

            for (TableProperty prop : TableProperty.values()) {
                if (prop.m_headerMessage != null) {
                    props.add(prop);
                }
            }
            return props;
        }

        /**
         * Returns the default value of property.<p>
         *
         * @return object
         */
        Object getDefaultValue() {

            return m_defaultValue;
        }

        /**
         * Returns localized header.<p>
         *
         * @return string for header
         */
        String getLocalizedMessage() {

            if (m_headerMessage == null) {
                return "";
            }
            return CmsVaadinUtils.getMessageText(m_headerMessage);
        }

        /**
         * Returns tye of value for given property.<p>
         *
         * @return type
         */
        Class<?> getType() {

            return m_type;
        }

        /**
         * Indicates if column is collapsable.<p>
         *
         * @return boolean, true = is collapsable
         */
        boolean isCollapsable() {

            return m_collapsable;
        }

    }

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsResourceTypesTable.class);

    private static final long serialVersionUID = 1L;

    /**Resource type app instance. */
    CmsResourceTypeApp m_app;

    /** CmsObject.*/
    CmsObject m_cms;

    /**Container holding table data.*/
    private IndexedContainer m_container;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * Public constructor.<p>
     *
     * @param app instance
     */
    public CmsResourceTypesTable(CmsResourceTypeApp app) {

        m_app = app;
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
        } catch (CmsException e) {
            //
        }

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        setSizeFull();
        init();

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 7957778390938304845L;

            public void itemClick(ItemClickEvent event) {

                onItemClick(event, event.getItemId(), event.getPropertyId());
            }

        });

    }

    /**
     * Checks if the given type is a core type that shouldn't be edited.
     *
     * @param type the type to check
     * @return true if the type shouldn't be edited
     */
    private static boolean isCoreType(I_CmsResourceType type) {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(type.getModuleName())
            || CmsADEManager.MODULE_NAME_ADE_CONFIG.equals(type.getModuleName());
    }

    /**
     * Filters the table according to given string.<p>
     *
     * @param text to filter
     */
    public void filterTable(String text) {

        m_container.removeAllContainerFilters();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(text)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(TableProperty.Name, text, true, false),
                    new SimpleStringFilter(TableProperty.ShortName, text, true, false),
                    new SimpleStringFilter(TableProperty.Module, text, true, false)));
        }

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
            m_menuEntries.add(new SchemaEditorEntry());
            m_menuEntries.add(new SearchEntry());
            m_menuEntries.add(new MoveEntry());
            m_menuEntries.add(new DeleteEntry());
        }
        return m_menuEntries;
    }

    /**
     * Init the table.<p>
     */
    void init() {

        if (m_container == null) {
            m_container = new IndexedContainer();
            setContainerDataSource(m_container);
        } else {
            m_container.removeAllContainerFilters();
            m_container.removeAllItems();
        }
        for (TableProperty prop : TableProperty.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefaultValue());
            setColumnHeader(prop, prop.getLocalizedMessage());
        }

        setItemIconPropertyId(TableProperty.Icon);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setColumnWidth(null, 40);
        setSelectable(true);
        setMultiSelect(true);

        for (I_CmsResourceType type : CmsVaadinUtils.getResourceTypes()) {
            CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                type.getTypeName());
            Item item = m_container.addItem(type.getTypeName());
            item.getItemProperty(TableProperty.ID).setValue(Integer.valueOf(type.getTypeId()));
            item.getItemProperty(TableProperty.Icon).setValue(CmsResourceUtil.getBigIconResource(typeSetting, null));
            item.getItemProperty(TableProperty.Name).setValue(CmsVaadinUtils.getMessageText(typeSetting.getKey()));
            item.getItemProperty(TableProperty.ShortName).setValue(type.getTypeName());
            item.getItemProperty(TableProperty.Module).setValue(type.getModuleName());
        }

        setVisibleColumns(TableProperty.Name, TableProperty.ShortName, TableProperty.ID, TableProperty.Module);
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
                m_menu.setEntries(getMenuEntries(), (Set<String>)getValue());
                m_menu.openForTable(event, itemId, propertyId, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && TableProperty.Name.equals(propertyId)) {
                String typeName = (String)itemId;
                openEditDialog(typeName);
            }
        }
    }

    /**
     * Opens the edit dialog.<p>
     *
     * @param typeName type to be edited.
     */
    void openEditDialog(String typeName) {

        try {
            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

            window.setContent(
                new CmsEditResourceTypeDialog(window, m_app, OpenCms.getResourceManager().getResourceType(typeName)));
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_WINDOW_CAPTION_0));
            A_CmsUI.get().addWindow(window);
        } catch (CmsLoaderException e) {
            LOG.error("Unable to read resource type by name", e);
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
