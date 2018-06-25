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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.A_CmsResourceTypeFolderBase;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.search.CmsSearchReplaceSettings;
import org.opencms.ui.apps.search.CmsSourceSearchApp;
import org.opencms.ui.apps.search.CmsSourceSearchAppConfiguration;
import org.opencms.ui.apps.search.CmsSourceSearchForm.SearchType;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Window;
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
                final I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(data.iterator().next());
                if (m_cms.readResources("", CmsResourceFilter.requireType(type), true).size() > 0) {
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

                                    CmsModule module = (CmsModule)OpenCms.getModuleManager().getModule(
                                        type.getModuleName()).clone();
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
                                    OpenCms.getModuleManager().updateModule(m_cms, module);
                                    OpenCms.getResourceManager().initialize(m_cms);
                                    OpenCms.getWorkplaceManager().addExplorerTypeSettings(module);
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

            String typeName = data.iterator().next();
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeName);
                return CmsStringUtil.isEmptyOrWhitespaceOnly(
                    (String)getItem(type).getItemProperty(TableProperty.Module).getValue())
                    ? CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE
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
    class EditEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> data) {

            try {
                Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

                window.setContent(
                    new CmsEditResourceTypeDialog(
                        window,
                        m_app,
                        OpenCms.getResourceManager().getResourceType(data.iterator().next())));
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_EDIT_WINDOW_CAPTION_0));
                A_CmsUI.get().addWindow(window);
            } catch (CmsLoaderException e) {
                LOG.error("Unable to read resource type by name", e);
            }
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

            String typeName = data.iterator().next();
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeName);
                return CmsStringUtil.isEmptyOrWhitespaceOnly(
                    (String)getItem(type).getItemProperty(TableProperty.Module).getValue())
                    ? CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE
                    : CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            } catch (CmsLoaderException e) {
                LOG.error("Unable to read resource type by name", e);
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
        }
    }

    /**
     * The menu entry to switch to the explorer of concerning site.<p>
     */
    class MessagesEditorEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(data.iterator().next());

                CmsModule module = OpenCms.getModuleManager().getModule(type.getModuleName());
                String messageFolder = CmsEditResourceTypeDialog.getMessageParentFolder(module.getName());
                //               if(m_cms.existsResource(messageFolder+"/"+CmsEditResourceTypeDialog.PATH_I18N+"/"+module.getName()+"_"+locale))

            } catch (CmsLoaderException e) {
                LOG.error("Unable to read resource type", e);
            }
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
     * The delete project context menu entry.<p>
     */
    class MoveEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> data) {

            try {
                Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

                window.setContent(
                    new CmsMoveResourceTypeDialog(
                        window,
                        OpenCms.getResourceManager().getResourceType(data.iterator().next())));
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_RESOURCETYPE_MOVE_WINDOW_CAPTION_0));
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

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
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
            CmsAppWorkplaceUi.get().getNavigator().navigateTo(
                CmsSourceSearchAppConfiguration.APP_ID + "/" + CmsSourceSearchApp.generateState(settings));

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

        /**Icon column.*/
        Name(Messages.GUI_RESOURCETYPE_NAME_0, String.class, "", false),

        /**Icon column.*/
        ShortName(Messages.GUI_RESOURCETYPE_SHORT_NAME_0, String.class, "", false),

        /**Is Broadcast send but not displayed.*/
        ID(Messages.GUI_RESOURCETYPE_ID_0, Integer.class, new Integer(0), false),

        /**Icon column.*/
        Module(Messages.GUI_RESOURCETYPE_MODULE_0, String.class, "", false);

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

    private static final long serialVersionUID = 1L;

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsResourceTypesTable.class);

    /** CmsObject.*/
    CmsObject m_cms;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**Container holding table data.*/
    private IndexedContainer m_container;

    /**Resource type app instance. */
    CmsResourceTypeApp m_app;

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
            Item item = m_container.addItem(type);
            item.getItemProperty(TableProperty.ID).setValue(type.getTypeId());
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
                Set<I_CmsResourceType> val = (Set<I_CmsResourceType>)getValue();
                m_menu.setEntries(getMenuEntries(), Collections.singleton(val.iterator().next().getTypeName()));
                m_menu.openForTable(event, itemId, propertyId, this);
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
