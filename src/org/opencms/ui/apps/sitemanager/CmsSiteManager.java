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

import org.opencms.configuration.CmsSitesConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.letsencrypt.CmsLetsEncryptConfiguration.Trigger;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSSLMode;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsCRUDApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.sitemanager.CmsSitesTable.TableProperty;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsInfoButton;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.TextField;

/**
 * Manager class for the Site manager app.
 */

public class CmsSiteManager extends A_CmsWorkplaceApp implements I_CmsCRUDApp<CmsSite> {

    /**Bundel name for the sites which are used as templates for new sites.*/
    public static final String BUNDLE_NAME = "siteMacroBundle";

    /**Constant.*/
    public static final String FAVICON = "favicon.ico";

    /** Name of the macros folder for site templates.*/
    public static final String MACRO_FOLDER = ".macros";

    /** The add project path name. */
    public static final String PATH_NAME_ADD = "newSite";

    /** The edit project path name. */
    public static final String PATH_NAME_EDIT = "editSite";

    /**The global settings path name. */
    public static final String PATH_NAME_GLOBAL = "global";

    /**The webserver setting path name.  */
    public static final String PATH_NAME_WEBSERVER = "webserver";

    /**path attribute to transmit root of a site to be edited. */
    public static final String SITE_ROOT = "siteRoot";

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsSiteManager.class.getName());

    /**Path to the sites folder.*/
    static final String PATH_SITES = "/sites/";

    /** The currently opened dialog window. */
    protected Window m_dialogWindow;

    /** The site table. */
    protected CmsSitesTable m_sitesTable;

    /** The file table filter input. */
    protected TextField m_siteTableFilter;

    /**Info Button. */
    private CmsInfoButton m_infoButton;

    /**The publish button.*/
    private Button m_publishButton;

    /** The root cms object. */
    private CmsObject m_rootCms;

    /**
     * Method to check if a folder under given path contains a bundle for macro resolving.<p>
     *
     * @param cms CmsObject
     * @param folderPathRoot root path of folder
     * @return true if macros bundle found
     */
    public static boolean isFolderWithMacros(CmsObject cms, String folderPathRoot) {

        if (!CmsResource.isFolder(folderPathRoot)) {
            folderPathRoot = folderPathRoot.concat("/");
        }
        try {
            cms.readResource(folderPathRoot + MACRO_FOLDER);
            cms.readResource(folderPathRoot + MACRO_FOLDER + "/" + BUNDLE_NAME + "_desc");
        } catch (CmsException e) {
            return false;
        }
        return true;
    }

    /**
     * Check if LetsEncrypt updates are configured to be triggered by webserver configuration updates.<p>
     *
     * @return true if LetsEncrypt updates are configured to be triggered by webserver configuration updates
     */
    public static boolean isLetsEncryptConfiguredForWebserverThread() {

        return (OpenCms.getLetsEncryptConfig() != null)
            && OpenCms.getLetsEncryptConfig().isValidAndEnabled()
            && (OpenCms.getLetsEncryptConfig().getTrigger() == Trigger.webserverThread);
    }

    /**
     * Centers the currently open window.
     */
    public void centerWindow() {

        if (m_dialogWindow != null) {
            m_dialogWindow.center();
        }
    }

    /**
     * Closes the current dialog window and updates the sites table if requested.<p>
     *
     * @param updateTable <code>true</code> to update the sites table
     */
    public void closeDialogWindow(boolean updateTable) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
            m_dialogWindow = null;
        }
        if (updateTable) {
            A_CmsUI.get().reload();
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#createElement(java.lang.Object)
     */
    public void createElement(CmsSite element) {

        try {
            OpenCms.getSiteManager().addSite(getRootCmsObject(), element);
        } catch (CmsException e) {
            LOG.error("unable to save site", e);
        }

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#defaultAction(java.lang.String)
     */
    public void defaultAction(String elementId) {

        openEditDialog(elementId);

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#deleteElements(java.util.List)
     */
    public void deleteElements(List<String> elementId) {

        for (String siteRoot : elementId) {
            try {
                CmsSite site = getElement(siteRoot);
                if (site != null) {
                    OpenCms.getSiteManager().removeSite(getRootCmsObject(), site);
                }
            } catch (CmsException e) {
                LOG.error("Unable to delete site", e);
            }
        }
        updateInfo();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#getAllElements()
     */
    public List<CmsSite> getAllElements() {

        List<CmsSite> res = OpenCms.getSiteManager().getAvailableSites(getRootCmsObject(), false).stream().filter(
            site -> !site.isGenerated()).collect(Collectors.toList());
        return res;
    }

    /**
     * Get corrupted sites.<p>
     *
     * @return List<CmsSite>
     */
    public List<CmsSite> getCorruptedSites() {

        return OpenCms.getSiteManager().getAvailableCorruptedSites(getRootCmsObject(), true);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#getElement(java.lang.String)
     */
    public CmsSite getElement(String elementId) {

        return OpenCms.getSiteManager().getSiteForSiteRoot(elementId);
    }

    /**
     * Returns the fav icon path for the given site.<p>
     *
     * @param siteRoot the site root
     *
     * @return the icon path
     */
    public Resource getFavIcon(String siteRoot) {

        CmsResource iconResource = null;
        try {
            iconResource = getRootCmsObject().readResource(siteRoot + "/" + CmsSiteManager.FAVICON);
        } catch (CmsException e) {
            //no favicon there
        }
        if (iconResource != null) {
            return new ExternalResource(
                OpenCms.getLinkManager().getPermalink(getRootCmsObject(), iconResource.getRootPath()));
        }
        return new CmsCssIcon(OpenCmsTheme.ICON_SITE);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        context.addPublishButton(changes -> {
            A_CmsUI.get().reload();
        });
        super.initUI(context);
    }

    /**
     * Checks if site export is enabled.
     * @return true if site export is enabled
     */
    public boolean isExportEnabled() {

        // Classes that extend CmsSiteManager must explicitly override this method if the feature should be enabled for them
        return this.getClass() == CmsSiteManager.class;
    }

    /**
     * Opens the delete dialog for the given sites.<p>
     *
     * @param data the site roots
     */
    public void openDeleteDialog(Set<String> data) {

        CmsDeleteSiteDialog form = new CmsDeleteSiteDialog(this, data);
        openDialog(form, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_DELETE_0));
    }

    /**
     * Opens the edit site dialog.<p>
     *
     * @param siteRoot the site root of the site to edit, if <code>null</code>
     */
    public void openEditDialog(String siteRoot) {

        CmsEditSiteForm form;
        String caption;
        if (siteRoot != null) {
            form = new CmsEditSiteForm(m_rootCms, this, siteRoot);
            caption = CmsVaadinUtils.getMessageText(
                Messages.GUI_SITE_CONFIGURATION_EDIT_1,
                m_sitesTable.getContainer().getItem(siteRoot).getItemProperty(TableProperty.Title).getValue());
        } else {
            form = new CmsEditSiteForm(m_rootCms, this);
            caption = CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_0);
        }
        openDialog(form, caption);
    }

    /**
     * Opens the global settings dialog.<p>
     */
    public void openSettingsDailog() {

        CmsGlobalForm form = new CmsGlobalForm(this);
        openDialog(form, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_CONFIGURATION_0));
    }

    /**
     * Opens the update server configuration dialog.<p>
     */
    public void openUpdateServerConfigDailog() {

        CmsWebServerConfigForm form = new CmsWebServerConfigForm(this);
        openDialog(form, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVERCONFIG_0));
    }

    /**
     * Updates the general settings.<p>
     *
     * @param cms the cms to use
     * @param defaultUri the default URI
     * @param workplaceServers the workplace server URLs
     * @param sharedFolder the shared folder URI
     */
    public void updateGeneralSettings(
        CmsObject cms,
        String defaultUri,
        Map<String, CmsSSLMode> workplaceServers,
        String sharedFolder) {

        try {
            OpenCms.getSiteManager().updateGeneralSettings(cms, defaultUri, workplaceServers, sharedFolder);
            OpenCms.writeConfiguration(CmsSitesConfiguration.class);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#writeElement(java.lang.Object)
     */
    public void writeElement(CmsSite element) {

        try {
            OpenCms.getSiteManager().updateSite(m_rootCms, getElement(element.getSiteRoot()), element);
        } catch (CmsException e) {
            LOG.error("Unabel to update site", e);
        }
        //updateInfo();
        //m_sitesTable.loadSites();
    }

    /**
     * Creates the table holdings all available sites.
     * @return a vaadin table component
     */

    protected CmsSitesTable createSitesTable() {

        CmsSitesTable table = new CmsSitesTable(this);
        table.loadSites();
        return table;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SITE_MANAGER_TITLE_SHORT_0));
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_sitesTable = createSitesTable();

        m_rootLayout.setMainHeightFull(true);
        m_siteTableFilter = new TextField();
        m_siteTableFilter.setIcon(FontOpenCms.FILTER);
        m_siteTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_siteTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_siteTableFilter.setWidth("200px");
        m_siteTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                m_sitesTable.filter(event.getText());
            }
        });
        m_infoLayout.addComponent(m_siteTableFilter);
        addToolbarButtons();
        return m_sitesTable;
    }

    /**
     * Returns the root cms object.<p>
     *
     * @return the root cms object
     */
    protected CmsObject getRootCmsObject() {

        if (m_rootCms == null) {

            m_rootCms = getOfflineCmsObject(A_CmsUI.getCmsObject());
            m_rootCms.getRequestContext().setSiteRoot("");

        }
        return m_rootCms;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Opens a given dialog.<p>
     *
     * @param dialog to be shown
     * @param windowCaption caption of window
     */
    protected void openDialog(CmsBasicDialog dialog, String windowCaption) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
        }

        m_dialogWindow = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        m_dialogWindow.setContent(dialog);
        m_dialogWindow.setCaption(windowCaption);

        A_CmsUI.get().addWindow(m_dialogWindow);
        m_dialogWindow.center();
    }

    /**
     * Update the info button.<p>
     */
    protected void updateInfo() {

        m_infoButton.replaceData(getInfoMap());
    }

    /**
     * Adds the toolbar buttons.<p>
     */
    private void addToolbarButtons() {

        Button add = CmsToolBar.createButton(FontOpenCms.WAND, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_0));
        add.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openEditDialog(null);
            }
        });
        m_uiContext.addToolbarButton(add);

        Button settings = CmsToolBar.createButton(
            FontOpenCms.SETTINGS,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_0));
        settings.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openSettingsDailog();
            }
        });
        m_uiContext.addToolbarButton(settings);
        if (OpenCms.getSiteManager().isConfigurableWebServer() || isLetsEncryptConfiguredForWebserverThread()) {
            Button webServer = CmsToolBar.createButton(
                FontAwesome.SERVER,
                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVERCONFIG_0));
            webServer.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    openUpdateServerConfigDailog();
                }
            });
            m_uiContext.addToolbarButton(webServer);
        }

        m_infoButton = new CmsInfoButton(getInfoMap());

        m_infoButton.setWindowCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_STATISTICS_CAPTION_0));
        m_infoButton.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_STATISTICS_CAPTION_0));
        m_uiContext.addToolbarButton(m_infoButton);
    }

    /**
     * Get info map.<p>
     *
     * @return map of sites info
     */
    private Map<String, String> getInfoMap() {

        Map<String, String> infos = new LinkedHashMap<String, String>();
        int corruptedSites = getCorruptedSites().size();
        infos.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_STATISTICS_NUM_WEBSITES_0),
            String.valueOf(getAllElements().size() + corruptedSites));

        if (corruptedSites > 0) {
            infos.put(
                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_STATISTICS_NUM_CORRUPTED_WEBSITES_0),
                String.valueOf(corruptedSites));
        }

        return infos;
    }
}
