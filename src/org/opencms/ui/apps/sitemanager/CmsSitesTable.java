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

package org.opencms.ui.apps.sitemanager;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.user.I_CmsFilterableTable;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.event.MouseEvents;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;

/**
 *  Class to create Vaadin Table object with all available sites.<p>
 */
@SuppressWarnings("deprecation")
public class CmsSitesTable extends Table implements I_CmsFilterableTable {

    /**
     * The edit project context menu entry.<p>
     */
    public class EditEntry
    implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String siteRoot = data.iterator().next();
            m_manager.openEditDialog(siteRoot);
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_EDIT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if ((data == null) || (data.size() != 1) || (m_manager.getElement(data.iterator().next()) == null)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }
    }

    /**
     * Context menu entry for site export.
     */
    public class ExportEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * Creates a new entry.
         */
        public ExportEntry() {

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            CmsExportSiteForm form = new CmsExportSiteForm(
                A_CmsUI.getCmsObject(),
                m_manager,
                context.iterator().next());
            m_manager.openDialog(form, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_EXPORT_DIALOG_CAPTION_0));
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return Messages.get().getBundle(locale).key(Messages.GUI_SITE_EXPORT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if ((data == null) || (data.size() != 1)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            return m_manager.isExportEnabled()
                && OpenCms.getRoleManager().hasRole(A_CmsUI.getCmsObject(), CmsRole.DATABASE_MANAGER)
                ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
                : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

    }

    /**Table properties. */
    protected enum TableProperty {

        /**Status. */
        Changed("", Boolean.class, Boolean.valueOf(false)),
        /**Status. */
        CmsSite("", CmsSite.class, null),
        /**OU. */
        Favicon("", Image.class, null),
        /**Icon. */
        Icon(null, Label.class, new Label(new CmsCssIcon(OpenCmsTheme.ICON_SITE).getHtml(), ContentMode.HTML)),
        /**Last login. */
        Is_Webserver("", Boolean.class, Boolean.valueOf(true)),
        /**IsIndirect?. */
        New("", Boolean.class, Boolean.valueOf(false)),
        /**Is the user disabled? */
        OK("", Boolean.class, Boolean.valueOf(true)),
        /**Path. */
        Path(Messages.GUI_SITE_PATH_0, String.class, ""),
        /**Description. */
        Server(Messages.GUI_SITE_SERVER_0, String.class, ""),
        /**From Other ou?. */
        SSL("", Integer.class, Integer.valueOf(1)),
        /**Name. */
        Title(Messages.GUI_SITE_TITLE_0, String.class, ""),
        /**Is the user new? */
        Under_Other_Site("", Boolean.class, Boolean.valueOf(false));

        /**Default value for column.*/
        private Object m_defaultValue;

        /**Header Message key.*/
        private String m_headerMessage;

        /**Type of column property.*/
        private Class<?> m_type;

        /**
         * constructor.<p>
         *
         * @param name Name
         * @param type type
         * @param defaultValue value
         */
        TableProperty(String name, Class<?> type, Object defaultValue) {

            m_headerMessage = name;
            m_type = type;
            m_defaultValue = defaultValue;
        }

        /**
         * The default value.<p>
         *
         * @return the default value object
         */
        Object getDefault() {

            return m_defaultValue;
        }

        /**
         * Gets the name of the property.<p>
         *
         * @return a name
         */
        String getName() {

            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_headerMessage)) {
                return CmsVaadinUtils.getMessageText(m_headerMessage);
            }
            return "";
        }

        /**
         * Gets the type of property.<p>
         *
         * @return the type
         */
        Class<?> getType() {

            return m_type;
        }

    }

    /**
     * The delete project context menu entry.<p>
     */
    class DeleteEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> data) {

            m_manager.openDeleteDialog(data);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_DELETE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if (m_manager.getElement(data.iterator().next()) == null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
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

            String siteRoot = data.iterator().next();
            A_CmsUI.getCmsObject().getRequestContext().setSiteRoot(siteRoot);
            CmsAppWorkplaceUi.get().showApp(
                CmsFileExplorerConfiguration.APP_ID,
                A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getUuid()
                    + A_CmsWorkplaceApp.PARAM_SEPARATOR
                    + siteRoot
                    + A_CmsWorkplaceApp.PARAM_SEPARATOR);
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

            if (data.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            try {
                CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                cms.getRequestContext().setSiteRoot("");
                if (cms.existsResource(data.iterator().next())) {
                    return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
                }
            } catch (CmsException e) {
                LOG.error("Unable to inti OpenCms Object", e);
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;

        }

    }

    /**
     * Column with FavIcon.<p>
     */
    class FavIconColumn implements Table.ColumnGenerator {

        /**Serial version id.*/
        private static final long serialVersionUID = -3772456970393398685L;

        /**
         * @see com.vaadin.v7.ui.Table.ColumnGenerator#generateCell(com.vaadin.v7.ui.Table, java.lang.Object, java.lang.Object)
         */
        public Object generateCell(Table source, Object itemId, Object columnId) {

            return getImageFavIcon((String)itemId);
        }
    }

    /**
     * The menu entry to switch to the page editor of concerning site.<p>
     */
    class PageEditorEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String siteRoot = data.iterator().next();
            A_CmsUI.get().changeSite(siteRoot);

            CmsPageEditorConfiguration pageeditorApp = new CmsPageEditorConfiguration();
            pageeditorApp.getAppLaunchCommand().run();

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_PAGEEDITOR_TITLE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if (data == null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            if (data.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            String siteRoot = data.iterator().next();
            if (m_manager.getElement(siteRoot) == null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            if (!((Boolean)getItem(siteRoot).getItemProperty(TableProperty.OK).getValue()).booleanValue()) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * The menu entry to switch to the sitemap editor of concerning site.<p>
     */
    class SitemapEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String siteRoot = data.iterator().next();
            A_CmsUI.get().changeSite(siteRoot);

            CmsSitemapEditorConfiguration sitemapApp = new CmsSitemapEditorConfiguration();
            sitemapApp.getAppLaunchCommand().run();
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_SITEMAP_TITLE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if ((data == null) || (data.size() != 1)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            String siteRoot = data.iterator().next();
            if (m_manager.getElement(siteRoot) == null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /** The logger for this class. */
    protected static Log LOG = CmsLog.getLog(CmsSitesTable.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = 4655464609332605219L;

    /** The project manager instance. */
    public CmsSiteManager m_manager;

    /** The available menu entries. */
    protected List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /** The data container. */
    private IndexedContainer m_container;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /**Counter for valid sites.*/
    private int m_siteCounter;

    /**
     * Constructor.<p>
     *
     * @param manager the project manager
     */
    public CmsSitesTable(CmsSiteManager manager) {

        m_manager = manager;

        m_container = new IndexedContainer();
        for (TableProperty prop : TableProperty.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefault());
            setColumnHeader(prop, prop.getName());
        }

        setContainerDataSource(m_container);

        setColumnAlignment(TableProperty.Favicon, Align.CENTER);
        setColumnExpandRatio(TableProperty.Server, 2);
        setColumnExpandRatio(TableProperty.Title, 2);
        setColumnExpandRatio(TableProperty.Path, 2);
        setColumnWidth(TableProperty.Favicon, 40);
        setColumnWidth(TableProperty.SSL, 130);

        setSelectable(true);
        setMultiSelect(true);
        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                onItemClick(event, event.getItemId(), event.getPropertyId());
            }
        });
        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                String styles = "";

                if (TableProperty.SSL.equals(propertyId)) {
                    styles += " "
                        + getSSLStyle(
                            (CmsSite)source.getItem(itemId).getItemProperty(TableProperty.CmsSite).getValue());
                }

                if (TableProperty.Server.equals(propertyId)) {
                    styles += " " + OpenCmsTheme.HOVER_COLUMN;
                    if (!((Boolean)source.getItem(itemId).getItemProperty(
                        TableProperty.OK).getValue()).booleanValue()) {
                        if (((Boolean)source.getItem(itemId).getItemProperty(
                            TableProperty.Changed).getValue()).booleanValue()) {
                            styles += " " + OpenCmsTheme.STATE_CHANGED;
                        } else {
                            styles += " " + OpenCmsTheme.EXPIRED;
                        }
                    } else {
                        if (((Boolean)source.getItem(itemId).getItemProperty(
                            TableProperty.New).getValue()).booleanValue()) {
                            styles += " " + OpenCmsTheme.STATE_NEW;
                        }
                    }
                }
                if (TableProperty.Title.equals(propertyId)
                    & ((Boolean)source.getItem(itemId).getItemProperty(
                        TableProperty.Is_Webserver).getValue()).booleanValue()) {
                    styles += " " + OpenCmsTheme.IN_NAVIGATION;
                }
                if (styles.isEmpty()) {
                    return null;
                }

                return styles;
            }
        });

        addGeneratedColumn(TableProperty.SSL, new ColumnGenerator() {

            private static final long serialVersionUID = -2144476865774782965L;

            public Object generateCell(Table source, Object itemId, Object columnId) {

                return getSSLStatus((CmsSite)source.getItem(itemId).getItemProperty(TableProperty.CmsSite).getValue());

            }

        });
        addGeneratedColumn(TableProperty.Favicon, new FavIconColumn());

        setItemDescriptionGenerator(new ItemDescriptionGenerator() {

            private static final long serialVersionUID = 7367011213487089661L;

            public String generateDescription(Component source, Object itemId, Object propertyId) {

                if (TableProperty.Favicon.equals(propertyId)) {

                    return ((CmsSite)(((Table)source).getItem(itemId).getItemProperty(
                        TableProperty.CmsSite).getValue())).getSSLMode().getLocalizedMessage();
                }
                return null;
            }
        });

        setColumnCollapsingAllowed(false);

        setVisibleColumns(
            TableProperty.Icon,
            TableProperty.SSL,
            TableProperty.Favicon,
            TableProperty.Server,
            TableProperty.Title,
            TableProperty.Path);

        setColumnWidth(TableProperty.Icon, 40);
    }

    /**
     * Gets the style for ssl badget.<p>
     *
     * @param site to get style for
     * @return style string
     */
    public static String getSSLStyle(CmsSite site) {

        if ((site != null) && site.getSSLMode().isSecure()) {
            return OpenCmsTheme.TABLE_COLUMN_BOX_CYAN;
        }
        return OpenCmsTheme.TABLE_COLUMN_BOX_GRAY;
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsFilterableTable#filter(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public void filter(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(TableProperty.Title, search, true, false),
                    new SimpleStringFilter(TableProperty.Path, search, true, false),
                    new SimpleStringFilter(TableProperty.Server, search, true, false)));
        }
        if ((getValue() != null) & !((Set<String>)getValue()).isEmpty()) {
            setCurrentPageFirstItemId(((Set<String>)getValue()).iterator().next());
        }

    }

    /**
     * Get the container.<p>
     *
     * @return IndexedContainer
     */
    public IndexedContainer getContainer() {

        return m_container;
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsFilterableTable#getEmptyLayout()
     */
    public VerticalLayout getEmptyLayout() {

        return null;
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    public List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new EditEntry());
            m_menuEntries.add(new DeleteEntry());
            m_menuEntries.add(new ExplorerEntry());
            m_menuEntries.add(new PageEditorEntry());
            m_menuEntries.add(new ExportEntry());
        }
        return m_menuEntries;
    }

    /**
     * Returns number of correctly configured sites.<p>
     *
     * @return number of sites
     */
    public int getSitesCount() {

        return m_siteCounter;
    }

    /**
     *  Reads sites from Site Manager and adds them to table.<p>
     */
    public void loadSites() {

        m_container.removeAllItems();
        List<CmsSite> sites = m_manager.getAllElements();
        m_siteCounter = 0;
        CmsCssIcon icon = new CmsCssIcon(OpenCmsTheme.ICON_SITE);
        icon.setOverlay(OpenCmsTheme.STATE_CHANGED + " " + CmsResourceIcon.ICON_CLASS_CHANGED);
        for (CmsSite site : sites) {
            if (site.getSiteMatcher() != null) {
                m_siteCounter++;
                Item item = m_container.addItem(site.getSiteRoot());
                item.getItemProperty(TableProperty.CmsSite).setValue(site);
                item.getItemProperty(TableProperty.Server).setValue(site.getUrl());
                item.getItemProperty(TableProperty.Title).setValue(site.getTitle());
                item.getItemProperty(TableProperty.Is_Webserver).setValue(Boolean.valueOf(site.isWebserver()));
                item.getItemProperty(TableProperty.Path).setValue(site.getSiteRoot());
                if (OpenCms.getSiteManager().isOnlyOfflineSite(site)) {
                    item.getItemProperty(TableProperty.New).setValue(Boolean.valueOf(true));
                    item.getItemProperty(TableProperty.Icon).setValue(
                        new Label(icon.getHtmlWithOverlay(), ContentMode.HTML));
                } else {
                    item.getItemProperty(TableProperty.Icon).setValue(new Label(icon.getHtml(), ContentMode.HTML));
                }
                item.getItemProperty(TableProperty.OK).setValue(isNotNestedSite(site, sites));
            }
        }

        for (CmsSite site : m_manager.getCorruptedSites()) {

            Item item = m_container.addItem(site.getSiteRoot());

            //Make sure item doesn't exist in table yet.. should never happen
            if (item != null) {
                item.getItemProperty(TableProperty.CmsSite).setValue(site);
                item.getItemProperty(TableProperty.Icon).setValue(new Label(icon.getHtml(), ContentMode.HTML));
                item.getItemProperty(TableProperty.Server).setValue(site.getUrl());
                item.getItemProperty(TableProperty.Title).setValue(site.getTitle());
                item.getItemProperty(TableProperty.Is_Webserver).setValue(Boolean.valueOf(site.isWebserver()));
                item.getItemProperty(TableProperty.Path).setValue(site.getSiteRoot());
                item.getItemProperty(TableProperty.OK).setValue(Boolean.valueOf(false));
                if (!site.getSiteRootUUID().isNullUUID()) {
                    if (m_manager.getRootCmsObject().existsResource(site.getSiteRootUUID())) {
                        item.getItemProperty(TableProperty.Changed).setValue(Boolean.valueOf(true));
                        item.getItemProperty(TableProperty.Icon).setValue(
                            new Label(icon.getHtmlWithOverlay(), ContentMode.HTML));
                    }
                }
            }
        }
    }

    /**
     * Sets the menu entries.<p>
     *
     * @param newEntries to be set
     */
    public void setMenuEntries(List<I_CmsSimpleContextMenuEntry<Set<String>>> newEntries) {

        m_menuEntries = newEntries;
    }

    /**
     * Get the ssl status label.<p>
     *
     * @param site to get ssl status for
     * @return Label for status
     */
    protected String getSSLStatus(CmsSite site) {

        if ((site != null) && site.getSSLMode().isSecure()) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ENCRYPTED_0);
        }
        return CmsVaadinUtils.getMessageText(Messages.GUI_SITE_UNENCRYPTED_0);
    }

    /**
     * Returns an favicon image with click listener on right clicks.<p>
     *
     * @param itemId of row to put image in.
     * @return Vaadin Image.
     */
    Image getImageFavIcon(final String itemId) {

        Resource resource = getFavIconResource(itemId);

        if (resource != null) {

            Image favIconImage = new Image("", resource);
            favIconImage.setWidth("24px");
            favIconImage.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FAVICON_0));

            favIconImage.addClickListener(new MouseEvents.ClickListener() {

                private static final long serialVersionUID = 5954790734673665522L;

                public void click(com.vaadin.event.MouseEvents.ClickEvent event) {

                    onItemClick(event, itemId, TableProperty.Favicon);

                }
            });

            return favIconImage;
        } else {
            return null;
        }
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
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == TableProperty.Icon)) {

                m_menu.setEntries(getMenuEntries(), (Set<String>)getValue());
                m_menu.openForTable(event, itemId, propertyId, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && TableProperty.Server.equals(propertyId)) {
                String siteRoot = (String)itemId;
                m_manager.defaultAction(siteRoot);
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

    /**
     * Loads the FavIcon of a given site.<p>
     *
     * @param siteRoot of the given site.
     * @return the favicon as resource or default image if no faicon was found.
     */
    private Resource getFavIconResource(String siteRoot) {

        try {
            CmsResource favicon = m_manager.getRootCmsObject().readResource(siteRoot + "/" + CmsSiteManager.FAVICON);
            CmsFile faviconFile = m_manager.getRootCmsObject().readFile(favicon);
            final byte[] imageData = faviconFile.getContents();
            return new StreamResource(new StreamResource.StreamSource() {

                private static final long serialVersionUID = -8868657402793427460L;

                public InputStream getStream() {

                    return new ByteArrayInputStream(imageData);

                }
            }, "");
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * Is the given site NOT nested in any of the given sites?<p>
     *
     * @param site to check
     * @param sites to check
     *
     * @return TRUE if the site is NOT nested
     */
    private Boolean isNotNestedSite(CmsSite site, List<CmsSite> sites) {

        for (CmsSite s : sites) {
            if ((site.getSiteRoot().length() > s.getSiteRoot().length())
                & site.getSiteRoot().startsWith(CmsFileUtil.addTrailingSeparator(s.getSiteRoot()))) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
}