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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Manager class for the Site manager app.
 */

public class CmsSiteManager extends A_CmsWorkplaceApp {

    /**Constant.*/
    public static final String FAVICON = "favicon.ico";

    /**Bundel name for the sites which are used as templates for new sites.*/
    public static final String BUNDLE_NAME = "siteMacroBundle";

    /** The site icon path. */
    public static final String ICON = "apps/sitemanager/sites.png";

    /** The site icon path. */
    public static final String TABLE_ICON = "apps/sites.png";

    /**The icon for adding a new site. */
    public static final String ICON_ADD = "apps/sitemanager/site-new.png";

    /**Icon for site folder.*/
    public static final String ICON_FOLDER = "apps/sitemanager/folder.png";

    /**Icon for the global site settings. */
    public static final String ICON_SITES_GLOBAL = "apps/sitemanager/site-global.png";

    /**Icon for the webserver configuration. */
    public static final String ICON_SITES_WEBSERVER = "apps/sitemanager/sites-webserver.png";

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

    /**Path to the sites folder.*/
    static final String PATH_SITES = "/sites/";

    /** The file table filter input. */
    private TextField m_siteTableFilter;

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
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        //Check if state is empty -> start
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SITE_MANAGER_TITLE_SHORT_0));
            return crumbs;
        }

        //Deeper path
        crumbs.put(
            CmsSiteManagerConfiguration.APP_ID,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_MANAGER_TITLE_SHORT_0));
        if (state.equals(PATH_NAME_ADD)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_0));
        } else if (state.startsWith(PATH_NAME_EDIT)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SITE_EDIT_0));
        } else if (state.startsWith(PATH_NAME_GLOBAL)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_0));
        } else if (state.startsWith(PATH_NAME_WEBSERVER)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVERCONFIG_0));
        }
        if (crumbs.size() > 1) {
            return crumbs;
        } else {
            return new LinkedHashMap<String, String>();
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (m_siteTableFilter != null) {
            m_infoLayout.removeComponent(m_siteTableFilter);
            m_siteTableFilter = null;
        }

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            if (state.startsWith(PATH_NAME_EDIT)) {
                m_rootLayout.setMainHeightFull(false);
                return new CmsEditSiteForm(this, getSiteRootFromState(state));
            } else if (state.startsWith(PATH_NAME_ADD)) {
                m_rootLayout.setMainHeightFull(false);
                return new CmsEditSiteForm(this);
            } else if (state.startsWith(PATH_NAME_GLOBAL)) {
                m_rootLayout.setMainHeightFull(false);
                return new CmsGlobalForm(this);
            } else if (state.startsWith(PATH_NAME_WEBSERVER)) {
                m_rootLayout.setMainHeightFull(false);
                return new CmsWebServerConfigForm(this);
            }
            return null;
        }

        final CmsSitesTable sitesTable = (CmsSitesTable)getSitesTable();

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

                sitesTable.filterTable(event.getText());
            }
        });
        m_infoLayout.addComponent(m_siteTableFilter);

        return sitesTable;
    }

    /**
     * Creates the table holdings all available sites.
     * @return a vaadin table component
     */

    protected Component getSitesTable() {

        CmsSitesTable table = new CmsSitesTable(this);
        table.loadSites();
        return table;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            List<NavEntry> subNav = new ArrayList<NavEntry>();
            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_DESCRIPTION_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_ADD)),
                    PATH_NAME_ADD));
            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_HELP_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_SITES_GLOBAL)),
                    PATH_NAME_GLOBAL));

            if (OpenCms.getSiteManager().isConfigurableWebServer()) {
                subNav.add(
                    new NavEntry(
                        CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVERCONFIG_0),
                        CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVERCONFIG_HELP_0),
                        new ExternalResource(OpenCmsTheme.getImageLink(ICON_SITES_WEBSERVER)),
                        PATH_NAME_WEBSERVER));
            }

            return subNav;
        }
        return null;
    }

    /**
     * Returns the site-root of a site from the given state.<p>
     *
     * @param state the state
     *
     * @return the site root
     */
    private String getSiteRootFromState(String state) {

        return A_CmsWorkplaceApp.getParamFromState(state, SITE_ROOT);
    }

}
