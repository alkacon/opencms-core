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

package org.opencms.ui.apps.searchindex;

import org.opencms.search.I_CmsSearchIndex;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.vaadin.event.MouseEvents;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Class for the vaadin table to show the indexes.<p>
 */
public class CmsSearchIndexTable extends Table {

    /**
     * The edit project context menu entry.<p>
     */
    class EntryRebuild implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            final Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
            CmsBasicDialog dialog = new CmsBasicDialog();

            dialog.setContent(new CmsSearchindexRebuild(m_manager, data));
            Button closeButton = new Button(CmsVaadinUtils.messageClose());
            closeButton.addClickListener(new ClickListener() {

                private static final long serialVersionUID = -1043776488459785433L;

                public void buttonClick(ClickEvent event) {

                    window.close();

                }

            });
            dialog.addButton(closeButton, true);
            window.setContent(dialog);

            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_ADMIN_TOOL_NAME_SHORT_0));
            A_CmsUI.get().addWindow(window);
            window.center();
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_REBUILD_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }
    }

    /**
     * Menu entry for show variations option.<p>
     */
    class EntrySources implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String resource = data.iterator().next();
            showSourcesWindow(resource);
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_INDEXSOURCE_SHOW_0);
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
     * All table properties.<p>
     */
    enum TableProperty {

        /**Field configuration column. */
        FieldConfig(Messages.GUI_SEARCHINDEX_COL_CONFIGURATION_0, String.class, "", false),

        /**Icon column.*/
        Icon(null, Resource.class, new CmsCssIcon(OpenCmsTheme.ICON_DATABASE), false),

        /**Locale column. */
        Locale(Messages.GUI_SEARCHINDEX_COL_LOCALE_0, String.class, "", false),

        /**name column. */
        Name(Messages.GUI_SEARCHINDEX_COL_NAME_0, String.class, "", false),

        /**Project column.*/
        Project(Messages.GUI_SEARCHINDEX_COL_PROJECT_0, String.class, "", false),

        /**Rebuild column.*/
        Rebuild(Messages.GUI_SEARCHINDEX_COL_REBUILDMODE_0, String.class, "", false);

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

    /**vaadin serial id.*/
    private static final long serialVersionUID = 5764331446498958798L;

    /**the calling instance.*/
    protected CmsSearchindexApp m_manager;

    /**Indexed container. */
    private IndexedContainer m_container;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * Constructor.<p>
     *
     * @param manager instance of the calling app
     */
    public CmsSearchIndexTable(CmsSearchindexApp manager) {

        m_manager = manager;
        m_container = new IndexedContainer();

        setContainerDataSource(m_container);

        for (TableProperty prop : TableProperty.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefaultValue());
            setColumnHeader(prop, prop.getLocalizedMessage());
        }

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);
        setVisibleColumns(
            TableProperty.Name,
            TableProperty.FieldConfig,
            TableProperty.Rebuild,
            TableProperty.Project,
            TableProperty.Locale);
        setItemIconPropertyId(TableProperty.Icon);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setColumnWidth(null, 40);

        addItemClickListener(new ItemClickListener() {

            /**vaadin serial id.*/
            private static final long serialVersionUID = -3595150969741628374L;

            public void itemClick(ItemClickEvent event) {

                onItemClick(event, event.getItemId(), event.getPropertyId());
            }

        });

        setSelectable(true);
        setMultiSelect(true);

        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (TableProperty.Name.equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }
                return null;
            }
        });

    }

    /**
     * (Re)loads the table.<p>
     */
    public void loadTable() {

        m_container.removeAllItems();
        List<I_CmsSearchIndex> indexes = m_manager.getAllElements();

        for (I_CmsSearchIndex index : indexes) {
            if (index.isEnabled()) {
                Item item = m_container.addItem(index);
                item.getItemProperty(TableProperty.Name).setValue(index.getName());
                item.getItemProperty(TableProperty.FieldConfig).setValue(index.getFieldConfiguration().getName());
                item.getItemProperty(TableProperty.Locale).setValue(index.getLocale().getDisplayName());
                item.getItemProperty(TableProperty.Project).setValue(index.getProject());
                item.getItemProperty(TableProperty.Rebuild).setValue(index.getRebuildMode());
            }
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
            m_menuEntries.add(new EntrySources()); //Option for show Sources of index
            m_menuEntries.add(new EntryRebuild());
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

        if (!event.isCtrlKey() && !event.isShiftKey()) {

            changeValueIfNotMultiSelect(itemId);

            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == null)) {
                m_menu.setEntries(getMenuEntries(), getSearchIndexNames());
                m_menu.openForTable(event, itemId, propertyId, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && TableProperty.Name.equals(propertyId)) {
                showSourcesWindow(((I_CmsSearchIndex)((Set<?>)getValue()).iterator().next()).getName());
            }
        }
    }

    /**
     * Shows dialog for variations of given resource.<p>
     *
     * @param resource to show variations for
     */
    void showSourcesWindow(String resource) {

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsSourceDialog sourceDialog = new CmsSourceDialog(m_manager, new Runnable() {

            public void run() {

                window.close();
            }
        });
        sourceDialog.setSource(resource);
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SEARCHINDEX_INDEXSOURCE_SHOW_1, resource));
        window.setContent(sourceDialog);
        UI.getCurrent().addWindow(window);
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

    /**
     * Returns a list with the names of all searchindexes.<p>
     *
     * @return List of search index names
     */
    private Set<String> getSearchIndexNames() {

        Set<String> names = new HashSet<String>();
        @SuppressWarnings("unchecked")
        Set<I_CmsSearchIndex> indexes = (Set<I_CmsSearchIndex>)getValue();
        for (I_CmsSearchIndex index : indexes) {
            names.add(index.getName());
        }
        return names;
    }
}