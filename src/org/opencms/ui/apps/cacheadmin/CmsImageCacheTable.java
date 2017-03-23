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
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

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
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Table to show entries of image cache.<p>
 */
public class CmsImageCacheTable extends Table {

    /**
     * Column for dimensions of image.<p>
     */
    class DimensionColumn implements Table.ColumnGenerator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -4569513960107614645L;

        /**
         * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
         */
        public Object generateCell(Table source, Object itemId, Object columnId) {

            try {
                return m_cacheHelper.getSingleSize(A_CmsUI.getCmsObject(), (String)itemId);
            } catch (CmsException e) {
                LOG.error("Not able to read image.", e);
                return null;
            }
        }

    }

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

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsImageCacheTable.class.getName());

    /**column for image dimension.*/
    private static final String PROP_DIMENSIONS = "dimensions";

    /**Column for icon.*/
    private static final String PROP_ICON = "icon";

    /**column for name of image.*/
    private static final String PROP_NAME = "name";

    /**column for resource size.*/
    private static final String PROP_SIZE = "size";

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5559186186646954045L;

    /**instance of calling app.*/
    CmsCacheAdminApp m_app;

    /**Image cache helper instance. */
    CmsImageCacheHelper m_cacheHelper;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**Indexed container.*/
    private IndexedContainer m_container;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**CmsObject at root.*/
    private CmsObject m_rootCms;

    /**
     * public constructor.<p>
     *
     * @param app instance of calling app.
     */
    public CmsImageCacheTable(CmsCacheAdminApp app) {

        CmsVariationsDialog.resetHandler();

        m_app = app;

        //Set cachHelper
        m_cacheHelper = new CmsImageCacheHelper(A_CmsUI.getCmsObject(), false, false, false);

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

        m_container.addContainerProperty(PROP_ICON, Resource.class, new ExternalResource(getImageFileTypeIcon()));
        m_container.addContainerProperty(PROP_NAME, String.class, "");
        m_container.addContainerProperty(PROP_DIMENSIONS, String.class, "");
        m_container.addContainerProperty(PROP_SIZE, String.class, "");
        //ini Table
        setContainerDataSource(m_container);
        setColumnHeader(PROP_NAME, CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LIST_COLS_RESOURCE_0));
        setColumnHeader(PROP_SIZE, CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LIST_COLS_LENGTH_0));
        setColumnHeader(PROP_DIMENSIONS, CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LIST_COLS_SIZE_0));

        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setColumnWidth(null, 40);

        setSelectable(true);

        addGeneratedColumn(PROP_DIMENSIONS, new DimensionColumn());

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
        loadTable();
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
     * Shows dialog for variations of given resource.<p>
     *
     * @param resource to show variations for
     */
    void showVariationsWindow(String resource) {

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsVariationsDialog variationsDialog = new CmsVariationsDialog(resource, new Runnable() {

            public void run() {

                window.close();

            }

        }, m_app, CmsVariationsDialog.MODE_IMAGE);
        try {
            CmsResource resourceObject = getRootCms().readResource(resource);
            variationsDialog.displayResourceInfo(Collections.singletonList(resourceObject));
        } catch (CmsException e) {
            //
        }
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_VIEW_FLEX_VARIATIONS_1, resource));
        window.setContent(variationsDialog);
        UI.getCurrent().addWindow(window);
    }

    /**
     * Returns the path of the icon of the resource type image.<p>
     *
     * @return path to icon
     */
    private String getImageFileTypeIcon() {

        String result = "";
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
            CmsResourceTypeImage.getStaticTypeName());
        if (settings != null) {
            result = CmsWorkplace.RES_PATH_FILETYPES + settings.getBigIconIfAvailable();
        }

        return CmsWorkplace.getResourceUri(result);

    }

    /**
     * Fills table with entries from image cache helper.<p>
     */
    private void loadTable() {

        setVisibleColumns(PROP_NAME, PROP_DIMENSIONS, PROP_SIZE);

        List<String> resources = m_cacheHelper.getAllCachedImages();

        for (String res : resources) {
            Item item = m_container.addItem(res);
            item.getItemProperty(PROP_NAME).setValue(res);
            item.getItemProperty(PROP_SIZE).setValue(m_cacheHelper.getLength(res));
        }
    }

}
