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
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.ui.util.table.CmsTableUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Image;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;

/**
 *  Class to create Vaadin Table object with all available sites.
 */

public class CmsSitesTable extends Table {

    /**
     * The delete project context menu entry.<p>
     */
    class DeleteEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> data) {

            String message;
            final List<CmsSite> sitesToDelete = new ArrayList<CmsSite>();
            for (String siteRoot : data) {
                sitesToDelete.add(OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot));
            }
            if (sitesToDelete.size() == 1) {
                //                Item item = m_container.getItem(data.iterator().next());
                message = CmsVaadinUtils.getMessageText(
                    Messages.GUI_SITE_CONFIRM_DELETE_SITE_1,
                    sitesToDelete.get(0).getTitle());
            } else {
                message = "";
                for (CmsSite site : sitesToDelete) {
                    if (message.length() > 0) {
                        message += ", ";
                    }
                    message += site.getTitle();
                }
                message = CmsVaadinUtils.getMessageText(Messages.GUI_SITE_CONFIRM_DELETE_SITES_1, message);
            }

            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_DELETE_0),
                message,
                new Runnable() {

                    public void run() {

                        try {
                            for (CmsSite site : sitesToDelete) {
                                OpenCms.getSiteManager().removeSite(A_CmsUI.getCmsObject(), site);
                            }
                            CmsAppWorkplaceUi.get().reload();
                        } catch (CmsException e) {
                            LOG.error("Error deleting site " + sitesToDelete.get(0).getTitle(), e);
                        }
                    }
                });
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

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }
    }

    /**
     * The edit project context menu entry.<p>
     */
    class EditEntry implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String siteRoot = data.iterator().next();
            m_manager.openSubView(
                A_CmsWorkplaceApp.addParamToState(CmsSiteManager.PATH_NAME_EDIT, CmsSiteManager.SITE_ROOT, siteRoot),
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_PROJECTS_EDIT_0);
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
     *Cloumn with icon buttons.
     */

    class IconButtonColumn implements Table.ColumnGenerator {

        /**generated id. */
        private static final long serialVersionUID = 7732640709644021017L;

        /**
         * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
         */
        @SuppressWarnings("boxing")
        public Object generateCell(final Table source, final Object itemId, Object columnId) {

            Property<Object> prop = source.getItem(itemId).getItemProperty(PROP_WEBSERVER);

            FontOpenCms resource = (boolean)prop.getValue() ? FontOpenCms.CIRCLE_CHECK : FontOpenCms.CIRCLE_PAUSE;
            Button button = CmsTableUtil.createIconButton(
                resource,
                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVER_HELP_0));
            button.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 2665896145238141105L;

                public void buttonClick(ClickEvent event) {

                    String siteRoot = (String)itemId;
                    updateWebserver(siteRoot);

                }

            });
            return button;
        }

    }

    /**
     *
     */

    class ToggleWebServer implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            String siteRoot = data.iterator().next();
            updateWebserver(siteRoot);

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_SITE_TOGGLEWEBSERVER_0);
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
    protected static Log LOG = CmsLog.getLog(CmsSitesTable.class.getName());
    /**generated id.*/
    private static final long serialVersionUID = 4655464609332605219L;
    /** Favicon property. */
    private static final String PROP_FAVICON = "favicon";
    /**Site icon property. */
    private static final String PROP_ICON = "icon";
    /**Site server property.*/
    private static final String PROP_SERVER = "server";
    /**Site title property.*/
    private static final String PROP_TITLE = "title";
    /**Site path property  (is id for site row in table).*/
    private static final String PROP_PATH = "path";

    /**webserver-boolean property.*/
    private static final String PROP_WEBSERVER = "webserver";

    /**Site aliases property.*/
    private static final String PROP_ALIASES = "aliases";

    /**Site securesites property.*/
    private static final String PROP_SECURESITES = "securesites";

    /** The data container. */
    IndexedContainer m_container;

    /** The context menu. */
    CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /** The project manager instance. */
    CmsSiteManager m_manager;

    /**
     * Constructor.<p>
     *
     * @param manager the project manager
     */
    @SuppressWarnings("boxing")
    public CmsSitesTable(CmsSiteManager manager) {
        m_manager = manager;

        m_container = new IndexedContainer();
        m_container.addContainerProperty(
            PROP_ICON,
            Image.class,
            new Image("", new ExternalResource(OpenCmsTheme.getImageLink(CmsSiteManager.ICON))));
        m_container.addContainerProperty(
            PROP_FAVICON,
            Image.class,
            new Image("", new ExternalResource(OpenCmsTheme.getImageLink(CmsSiteManager.ICON))));
        m_container.addContainerProperty(PROP_WEBSERVER, Boolean.class, false);
        m_container.addContainerProperty(PROP_SERVER, String.class, "");
        m_container.addContainerProperty(PROP_TITLE, String.class, "");
        m_container.addContainerProperty(PROP_PATH, String.class, "");
        m_container.addContainerProperty(PROP_ALIASES, String.class, "");
        m_container.addContainerProperty(PROP_SECURESITES, String.class, "");

        setContainerDataSource(m_container);
        setColumnHeader(PROP_FAVICON, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_FAVICON_0));
        setColumnHeader(PROP_ICON, "");
        setColumnHeader(PROP_WEBSERVER, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVER_0));
        setColumnHeader(PROP_SERVER, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SERVER_0));
        setColumnHeader(PROP_TITLE, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_TITLE_0));
        setColumnHeader(PROP_PATH, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_PATH_0));
        setColumnHeader(PROP_ALIASES, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ALIASES_0));
        setColumnHeader(PROP_SECURESITES, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_SECURESERVER_0));
        setColumnAlignment(PROP_FAVICON, Align.CENTER);
        setColumnExpandRatio(PROP_SERVER, 2);
        setColumnExpandRatio(PROP_TITLE, 2);
        setColumnExpandRatio(PROP_PATH, 2);
        setColumnWidth(PROP_FAVICON, 70);
        setColumnWidth(PROP_ICON, 40);
        setColumnWidth(PROP_WEBSERVER, 100);
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

                if (PROP_SERVER.equals(propertyId)) {
                    return OpenCmsTheme.HOVER_COLUMN;
                }
                return null;
            }
        });

        addGeneratedColumn(PROP_WEBSERVER, new IconButtonColumn());

        setColumnCollapsingAllowed(true);
        setColumnCollapsible(PROP_ALIASES, true);
        setColumnCollapsible(PROP_SECURESITES, true);
        setColumnCollapsible(PROP_PATH, false);
        setColumnCollapsible(PROP_SERVER, false);
        setColumnCollapsible(PROP_TITLE, false);
        setColumnCollapsible(PROP_FAVICON, false);
        setColumnCollapsible(PROP_WEBSERVER, false);
        setColumnCollapsible(PROP_ICON, false);

    }

    /**
     * Filters the table according to given search string.
     * @param search string to be looked for.
     */

    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(PROP_TITLE, search, true, false),
                    new SimpleStringFilter(PROP_PATH, search, true, false),
                    new SimpleStringFilter(PROP_SERVER, search, true, false)));
        }
    }

    /**
     *
     */
    @SuppressWarnings("boxing")
    public void loadSites() {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_container.removeAllItems();

        setVisibleColumns(
            PROP_ICON,
            PROP_FAVICON,
            PROP_WEBSERVER,
            PROP_SERVER,
            PROP_TITLE,
            PROP_PATH,
            PROP_SECURESITES,
            PROP_ALIASES);

        setColumnCollapsed(PROP_ALIASES, true);
        setColumnCollapsed(PROP_SECURESITES, true);

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(cms, true);
        for (CmsSite site : sites) {
            if (site.getSiteMatcher() != null) {
                Item item = m_container.addItem(site.getSiteRoot());
                item.getItemProperty(PROP_WEBSERVER).setValue(site.isWebserver());

                item.getItemProperty(PROP_ICON).setValue(getImageIcon(site.getSiteRoot()));
                item.getItemProperty(PROP_FAVICON).setValue(getImageFavIcon(site.getSiteRoot()));
                item.getItemProperty(PROP_SERVER).setValue(site.getUrl());
                item.getItemProperty(PROP_TITLE).setValue(site.getTitle());
                item.getItemProperty(PROP_PATH).setValue(site.getSiteRoot());
                item.getItemProperty(PROP_ALIASES).setValue(getNiceStringFormList(site.getAliases()));
                if (site.hasSecureServer()) {
                    item.getItemProperty(PROP_SECURESITES).setValue(site.getSecureUrl());
                }
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
            m_menuEntries.add(new EditEntry());
            m_menuEntries.add(new ToggleWebServer());
            m_menuEntries.add(new DeleteEntry());
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
    @SuppressWarnings("unchecked")
    void onItemClick(MouseEvents.ClickEvent event, Object itemId, Object propertyId) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {

            changeValueIfNotMultiSelect(itemId);

            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == PROP_ICON)) {

                m_menu.setEntries(getMenuEntries(), (Set<String>)getValue());
                m_menu.openForTable(event, itemId, propertyId, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && PROP_SERVER.equals(propertyId)) {
                Item item = ((ItemClickEvent)event).getItem();
                String siteRoot = (String)item.getItemProperty(PROP_PATH).getValue();
                m_manager.openSubView(
                    A_CmsWorkplaceApp.addParamToState(
                        CmsSiteManager.PATH_NAME_EDIT,
                        CmsSiteManager.SITE_ROOT,
                        siteRoot),
                    true);
            }
        }
    }

    /**
     * Changes the boolean isWebserver of CmsSite triggered by click on IconButton or Menu entry.
     * @param siteRoot of site to be updated.
     */

    @SuppressWarnings("boxing")
    void updateWebserver(String siteRoot) {

        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);

        CmsSite newSite = (CmsSite)site.clone();
        newSite.setParameters(site.getParameters());
        newSite.setWebserver(!site.isWebserver());
        try {
            OpenCms.getSiteManager().updateSite(A_CmsUI.getCmsObject(), site, newSite);
            getItem(siteRoot).getItemProperty(PROP_WEBSERVER).setValue(newSite.isWebserver());
            refreshRowCache();
        } catch (CmsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks value of table and sets it new if needed:
     * if multiselect: new itemId is in current Value? -> no change of value
     * no multiselect and multiselect, but new item not selected before: set value to new item
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
     * Loads the FavIcon of a given site.
     * @param siteRoot of the given site.
     * @return the favicon as resource or default image if no faicon was found.
     */

    private Resource getFavIconResource(String siteRoot) {

        try {
            final CmsObject cms = A_CmsUI.getCmsObject();
            cms.getRequestContext().setSiteRoot("");
            CmsResource favicon = cms.readResource(siteRoot + "/" + CmsSiteManager.FAVICON);
            CmsFile faviconFile = cms.readFile(favicon);
            final byte[] imageData = faviconFile.getContents();
            return new StreamResource(new StreamResource.StreamSource() {

                private static final long serialVersionUID = -8868657402793427460L;

                public InputStream getStream() {

                    return new ByteArrayInputStream(imageData);

                }
            }, String.valueOf(System.currentTimeMillis()));
        } catch (@SuppressWarnings("unused") CmsException e) {
            return new ExternalResource(OpenCmsTheme.getImageLink(CmsSiteManager.ICON));
        }
    }

    /**
     * Returns an favicon image with click listener on right clicks.
     *
     * @param itemId of row to put image in.
     * @return Vaadin Image.
     */
    private Image getImageFavIcon(final String itemId) {

        Image favIconImage = new Image(String.valueOf(System.currentTimeMillis()), getFavIconResource(itemId));

        favIconImage.addClickListener(new MouseEvents.ClickListener() {

            private static final long serialVersionUID = 5954790734673665522L;

            public void click(com.vaadin.event.MouseEvents.ClickEvent event) {

                onItemClick(event, itemId, PROP_FAVICON);

            }
        });

        return favIconImage;

    }

    /**
     * Returns an image with default resource and with a click listener to open CmsContextMenu on left and right click.
     *
     * @param itemId of considered row in table.
     * @return vaadin image.
     *
     */
    private Image getImageIcon(final Object itemId) {

        Image imageIcon = new Image("", new ExternalResource(OpenCmsTheme.getImageLink(CmsSiteManager.ICON)));
        imageIcon.setResponsive(false);
        imageIcon.addClickListener(new com.vaadin.event.MouseEvents.ClickListener() {

            private static final long serialVersionUID = -1313614316848307224L;

            public void click(com.vaadin.event.MouseEvents.ClickEvent event) {

                onItemClick(event, itemId, PROP_ICON);

            }

        });
        return imageIcon;
    }

    /**
     * Makes a String from aliases (comma separated).
     *
     * @param aliases List of aliases.
     * @return nice string.
     */
    private String getNiceStringFormList(List<CmsSiteMatcher> aliases) {

        if (aliases.isEmpty()) {
            return "";
        }
        String ret = "";
        for (CmsSiteMatcher alias : aliases) {
            ret += alias.getServerName() + ", ";
        }
        return ret.substring(0, ret.length() - ", ".length());
    }
}
