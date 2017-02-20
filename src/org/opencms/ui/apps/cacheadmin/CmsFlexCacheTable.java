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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.flex.CmsFlexCache;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Table showong content of flex cache.<p>
 */
public class CmsFlexCacheTable extends Table {

    /**
     * Menu entry for show variations option.<p>
     */
    class EntryVariations
    implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String resource = data.iterator().next();
            m_app.openSubView(
                A_CmsWorkplaceApp.addParamToState(
                    CmsCacheAdminApp.PATH_VIEW_FLEX_VARIATIONS,
                    CmsCacheAdminApp.RESOURCE,
                    resource),
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_STATS_VARIATIONS_0);
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

    /**icon for first column.*/
    private static final String ICON = "apps/cacheAdmin/cache_table.png";

    /**Column for icon.*/
    private static final String PROP_ICON = "icon";

    /**Column for key.*/
    private static final String PROP_KEY = "key";

    /**Column for project name.*/
    private static final String PROP_PROJECT = "project";

    /**Column for resource-name.*/
    private static final String PROP_RESOURCENAME = "name";

    /**vaadin serial id.*/
    private static final long serialVersionUID = 836377854954208442L;

    /**Instance of calling class.*/
    CmsCacheAdminApp m_app;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**Current flex cache.*/
    private CmsFlexCache m_cache;

    /**Indexed Container.*/
    private IndexedContainer m_container;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * public constructor. <p>
     *
     * @param app calling instance
     */
    public CmsFlexCacheTable(CmsCacheAdminApp app) {
        m_app = app;

        m_cache = OpenCms.getFlexCache();

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        m_container = new IndexedContainer();

        m_container.addContainerProperty(
            PROP_ICON,
            Resource.class,
            new ExternalResource(OpenCmsTheme.getImageLink(ICON)));
        m_container.addContainerProperty(PROP_RESOURCENAME, String.class, "");
        m_container.addContainerProperty(PROP_PROJECT, String.class, "");
        m_container.addContainerProperty(PROP_KEY, String.class, "");

        setContainerDataSource(m_container);

        setColumnHeader(PROP_RESOURCENAME, CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LIST_COLS_RESOURCE_0));
        setColumnHeader(PROP_PROJECT, CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LIST_COLS_PROJECT_0));
        setColumnHeader(PROP_KEY, CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LIST_COLS_KEY_0));

        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setColumnWidth(null, 40);

        setSelectable(true);

        loadTableEntries();

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = -4738296706762013443L;

            public void itemClick(ItemClickEvent event) {

                setValue(null);
                select(event.getItemId());

                //Right click or click on icon column (=null) -> show menu
                if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                    m_menu.setEntries(getMenuEntries(), Collections.singleton(((String)getValue())));
                    m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsFlexCacheTable.this);
                }
            }
        });
    }

    /**
     * Filters the table according to given search string.<p>
     *
     * @param search string to be looked for.
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(PROP_RESOURCENAME, search, true, false),
                    new SimpleStringFilter(PROP_KEY, search, true, false)));
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
            m_menuEntries.add(new EntryVariations()); //Option for Variations
        }
        return m_menuEntries;
    }

    /**
     * Reads flex cache entries and puts them to table.<p>
     */
    private void loadTableEntries() {

        setVisibleColumns(PROP_RESOURCENAME, PROP_PROJECT, PROP_KEY);

        Iterator<String> itResources = new ArrayList<String>(
            m_cache.getCachedResources(A_CmsUI.getCmsObject())).iterator();
        while (itResources.hasNext()) {
            String resource = itResources.next();
            String resName = resource;
            String project = "";
            String key = "";
            if (resource.endsWith(CmsFlexCache.CACHE_OFFLINESUFFIX)) {
                resName = resource.substring(0, resource.length() - CmsFlexCache.CACHE_OFFLINESUFFIX.length());
                project = "Offline";
            }
            if (resource.endsWith(CmsFlexCache.CACHE_ONLINESUFFIX)) {
                resName = resource.substring(0, resource.length() - CmsFlexCache.CACHE_ONLINESUFFIX.length());
                project = "Online";
            }
            key = m_cache.getCachedKey(resource, A_CmsUI.getCmsObject()).toString();

            Item item = m_container.addItem(resource);
            item.getItemProperty(PROP_RESOURCENAME).setValue(resName);
            item.getItemProperty(PROP_PROJECT).setValue(project);
            item.getItemProperty(PROP_KEY).setValue(key);
        }
    }
}
