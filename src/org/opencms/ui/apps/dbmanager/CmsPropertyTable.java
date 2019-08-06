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

package org.opencms.ui.apps.dbmanager;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.search.CmsSearchReplaceSettings;
import org.opencms.ui.apps.search.CmsSourceSearchApp;
import org.opencms.ui.apps.search.CmsSourceSearchAppConfiguration;
import org.opencms.ui.apps.search.CmsSourceSearchForm.SearchType;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
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
 * Class for the table containing all property definitions in the system.<p>
 */
public class CmsPropertyTable extends Table {

    /**Table columns.*/
    protected enum TableColumn {

        /**The icon column.*/
        Icon(null, Resource.class, new CmsCssIcon(OpenCmsTheme.ICON_DATABASE)),
        /**The Name column.*/
        Name(Messages.GUI_MESSAGES_BROADCAST_COLS_USER_0, String.class, "");

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
         */
        TableColumn(String headerMessage, Class<?> type, Object defaultValue) {

            m_headerMessage = headerMessage;
            m_type = type;
            m_defaultValue = defaultValue;
        }

        /**
         * Returns list of all properties with non-empty header.<p>
         *
         * @return list of properties
         */
        static List<TableColumn> withHeader() {

            List<TableColumn> props = new ArrayList<TableColumn>();

            for (TableColumn prop : TableColumn.values()) {
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

    }

    /**
     * Menu entry for showing resources.<p>
     */
    class EntryDelete implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_DELETE_0));
            window.setContent(new CmsPropertyDeleteDialog(data.iterator().next(), window, new Runnable() {

                public void run() {

                    init();
                }
            }));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_DELETE_0);
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
    class EntryResources
    implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            showResources(data.iterator().next());
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

    /**vaadin serial id.*/
    private static final long serialVersionUID = 8004865379309420561L;

    /**The container.*/
    private IndexedContainer m_container;

    /** The context menu. */
    CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * public constructor.<p>
     */
    public CmsPropertyTable() {

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        init();

        setColumnWidth(null, 40);
        setSelectable(true);
        setMultiSelect(false);

    }

    /**
     * Filters the table according to given search string.<p>
     *
     * @param search string to be looked for.
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(new Or(new SimpleStringFilter(TableColumn.Name, search, true, false)));
        }
    }

    /**
     * Fills table with items.<p>
     */
    public void init() {

        try {
            List<CmsPropertyDefinition> properties = A_CmsUI.getCmsObject().readAllPropertyDefinitions();

            m_container = new IndexedContainer();
            for (TableColumn col : TableColumn.values()) {
                m_container.addContainerProperty(col, col.getType(), col.getDefaultValue());
            }
            setContainerDataSource(m_container);
            setItemIconPropertyId(TableColumn.Icon);
            setRowHeaderMode(RowHeaderMode.ICON_ONLY);

            setVisibleColumns(TableColumn.Name);

            for (CmsPropertyDefinition prop : properties) {
                Item item = m_container.addItem(prop);
                item.getItemProperty(TableColumn.Name).setValue(prop.getName());
            }
            addItemClickListener(new ItemClickListener() {

                private static final long serialVersionUID = 4807195510202231174L;

                public void itemClick(ItemClickEvent event) {

                    setValue(null);
                    select(event.getItemId());
                    if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                        m_menu.setEntries(
                            getMenuEntries(),
                            Collections.singleton(((CmsPropertyDefinition)getValue()).getName()));
                        m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsPropertyTable.this);
                    } else if (TableColumn.Name.equals(event.getPropertyId())) {
                        showResources(((CmsPropertyDefinition)getValue()).getName());
                    }

                }

            });

            setCellStyleGenerator(new CellStyleGenerator() {

                private static final long serialVersionUID = 1L;

                public String getStyle(Table source, Object itemId, Object propertyId) {

                    if (TableColumn.Name.equals(propertyId)) {
                        return " " + OpenCmsTheme.HOVER_COLUMN;
                    }
                    return null;
                }
            });

        } catch (CmsException e) {
            //
        }
    }

    /**
     * Change to source search for showing resources for property definition.<p>
     *
     * @param propertyName to search resources for
     */
    protected void showResources(String propertyName) {

        CmsSearchReplaceSettings settings = new CmsSearchReplaceSettings();
        settings.setPaths(Collections.singletonList("/"));
        settings.setSiteRoot("");
        settings.setSearchpattern(".*");
        settings.setType(SearchType.properties);
        try {
            settings.setProperty(A_CmsUI.getCmsObject().readPropertyDefinition(propertyName));
        } catch (CmsException e) {
            //
        }
        CmsAppWorkplaceUi.get().showApp(
            CmsSourceSearchAppConfiguration.APP_ID,
            CmsSourceSearchApp.generateState(settings));
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new EntryResources());
            m_menuEntries.add(new EntryDelete());
        }
        return m_menuEntries;
    }
}
