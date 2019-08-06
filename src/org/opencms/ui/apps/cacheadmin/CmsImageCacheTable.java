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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.cacheadmin.CmsCacheViewApp.Mode;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

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
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Table to show entries of image cache.<p>
 */
public class CmsImageCacheTable extends Table {

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
            showVariationsWindow(resource);
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_STATS_VARIATIONS_0);
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
     * The menu entry to switch to the explorer of concerning site.<p>
     */
    class ExplorerEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            CmsResource res;
            try {
                res = getRootCms().readResource(data.iterator().next());
                openExplorerForParent(res.getRootPath(), res.getStructureId().getStringValue());
            } catch (CmsException e) {
                e.printStackTrace();
            }

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return Messages.get().getBundle(locale).key(Messages.GUI_EXPLORER_TITLE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if (data == null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            String res = data.iterator().next();

            if (!A_CmsUI.getCmsObject().getRequestContext().getSiteRoot().equals("")) {
                if (!res.startsWith(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot())) {
                    return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
                }
            }

            return data.size() == 1
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

    }

    /**
     * Column for dimensions of image.<p>
     */
    class VariationsColumn implements Table.ColumnGenerator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -4569513960107614645L;

        /**
         * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
         */
        public Object generateCell(Table source, Object itemId, Object columnId) {

            return Integer.valueOf(HELPER.getVariationsCount((String)itemId));
        }

    }

    /**Image cache helper instance. */
    protected static CmsImageCacheHolder HELPER;

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsImageCacheTable.class.getName());

    /**Column for icon.*/
    private static final String PROP_ICON = "icon";

    /**column for name of image.*/
    private static final String PROP_NAME = "name";

    /**column for image dimension.*/
    private static final String PROP_VARIATIONS = "variations";

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5559186186646954045L;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**Indexed container.*/
    private IndexedContainer m_container;

    /**intro result view.*/
    private VerticalLayout m_intro;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**null result view. */
    private VerticalLayout m_nullResult;

    /**CmsObject at root.*/
    private CmsObject m_rootCms;

    /**Filter text field for table. */
    private TextField m_siteTableFilter;

    /**
     * public constructor.<p>
     * @param nullResult vaadin component
     * @param intro vaadin component
     * @param siteTableFilter vaadin component
     */
    public CmsImageCacheTable(VerticalLayout intro, VerticalLayout nullResult, TextField siteTableFilter) {

        m_intro = intro;
        m_nullResult = nullResult;
        m_siteTableFilter = siteTableFilter;

        //Set menu
        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        //Setup container, sortable only for Name because other properties are loaded in background
        m_container = new IndexedContainer() {

            private static final long serialVersionUID = -8679153149897733835L;

            @Override
            public Collection<?> getSortableContainerPropertyIds() {

                return Collections.singleton(PROP_NAME);
            }
        };

        m_container.addContainerProperty(
            PROP_ICON,
            Resource.class,
            CmsResourceUtil.getBigIconResource(
                OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeImage.getStaticTypeName()),
                null));
        m_container.addContainerProperty(PROP_NAME, String.class, "");
        m_container.addContainerProperty(PROP_VARIATIONS, Integer.class, "");
        //ini Table
        setContainerDataSource(m_container);
        setColumnHeader(PROP_NAME, CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LIST_COLS_RESOURCE_0));
        setColumnHeader(
            PROP_VARIATIONS,
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LIST_COLS_VARIATIONS_0));

        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setColumnWidth(null, 40);

        setSelectable(true);

        addGeneratedColumn(PROP_VARIATIONS, new VariationsColumn());

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = -4738296706762013443L;

            public void itemClick(ItemClickEvent event) {

                setValue(null);
                select(event.getItemId());

                //Right click or click on icon column (=null) -> show menu
                if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                    m_menu.setEntries(getMenuEntries(), Collections.singleton((String)getValue()));
                    m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsImageCacheTable.this);
                }

                if (event.getButton().equals(MouseButton.LEFT) & PROP_NAME.equals(event.getPropertyId())) {
                    showVariationsWindow(((String)getValue()));
                }
            }
        });

        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (PROP_NAME.equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }

                return null;
            }
        });

        setColumnWidth(PROP_VARIATIONS, 100);

    }

    /**
     * Filters the table according to given search string.<p>
     *
     * @param search string to be looked for.
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(new Or(new SimpleStringFilter(PROP_NAME, search, true, false)));
        }
        if ((getValue() != null)) {
            setCurrentPageFirstItemId(getValue());
        }
    }

    /**
     * Loads the table.<p>
     *
     * @param search searchstring to be considered
     */
    public void load(String search) {

        HELPER = new CmsImageCacheHolder(search);
        loadTable();
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
            m_menuEntries.add(new ExplorerEntry());
        }
        return m_menuEntries;
    }

    /**
     * Returns a cms object at root-site.<p>
     *
     * @return cmsobject
     */
    CmsObject getRootCms() {

        try {
            if (m_rootCms == null) {

                m_rootCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                m_rootCms.getRequestContext().setSiteRoot("");
            }
        } catch (CmsException e) {
            //
        }
        return m_rootCms;

    }

    /**
     * Opens the explorer for given path and selected resource.<p>
     *
     * @param rootPath to be opened
     * @param uuid to be selected
     */
    void openExplorerForParent(String rootPath, String uuid) {

        String parentPath = CmsResource.getParentFolder(rootPath);
        CmsAppWorkplaceUi.get().showApp(
            CmsFileExplorerConfiguration.APP_ID,
            A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getUuid()
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + A_CmsUI.getCmsObject().getRequestContext().getSiteRoot()
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + parentPath.substring(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot().length())
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + uuid
                + A_CmsWorkplaceApp.PARAM_SEPARATOR);
    }

    /**
     * Shows dialog for variations of given resource.<p>
     *
     * @param resource to show variations for
     */
    void showVariationsWindow(String resource) {

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
        CmsVariationsDialog variationsDialog = new CmsVariationsDialog(resource, new Runnable() {

            public void run() {

                window.close();

            }

        }, Mode.ImageCache);
        try {
            CmsResource resourceObject = getRootCms().readResource(resource);
            variationsDialog.displayResourceInfo(Collections.singletonList(resourceObject));
        } catch (CmsException e) {
            //
        }
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_VIEW_FLEX_VARIATIONS_1, resource));
        window.setContent(variationsDialog);
        A_CmsUI.get().addWindow(window);
        window.center();
    }

    /**
     * Fills table with entries from image cache helper.<p>
     */
    private void loadTable() {

        m_nullResult.setVisible(false);
        m_intro.setVisible(false);
        setVisible(true);
        m_siteTableFilter.setVisible(true);

        m_container.removeAllItems();

        setVisibleColumns(PROP_NAME, PROP_VARIATIONS);

        List<String> resources = HELPER.getAllCachedImages();

        for (String res : resources) {
            Item item = m_container.addItem(res);
            item.getItemProperty(PROP_NAME).setValue(res);
        }

        if (resources.size() == 0) {
            m_nullResult.setVisible(true);
            setVisible(false);
            m_siteTableFilter.setVisible(false);
        }

    }

}
