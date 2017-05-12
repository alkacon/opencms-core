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

import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.linkvalidation.CmsInternalResources;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;

/**
 * Class for database manager app.<p>
 */
public class CmsDbManager extends A_CmsWorkplaceApp {

    /**App icon.*/
    protected static String APP_ICON = "apps/dbmanager/database_manager.png";

    /** Name of the manifest file used in upload files. */
    private static final String FILE_MANIFEST = "manifest.xml";

    /** Name of the sub-folder containing the OpenCms module packages. */
    private static final String FOLDER_MODULES = "modules";

    /**Icon to export tool. */
    private static final String ICON_EXPORT = "apps/dbmanager/data_export.png";

    /**Icon to http import tool. */
    private static final String ICON_IMPORT_HTTP = "apps/dbmanager/data_import_http.png";

    /**Icon to import server tool.*/
    private static final String ICON_IMPORT_SERVER = "apps/dbmanager/data_import_server.png";

    /**Icon to remove publish locks too. */
    private static final String ICON_REMOVE_PUBLISH_LOCKS = "apps/dbmanager/publishlocks.png";

    /**Icon to static export tool.*/
    private static final String ICON_STATIC_EXPORT = "apps/dbmanager/staticexport.png";

    /**Path to export tool. */
    private static final String PATH_EXPORT = "export";

    /**Path to http import tool. */
    private static final String PATH_IMPORT_HTTP = "importhttp";

    /**Path to import server tool.*/
    private static final String PATH_IMPORT_SERVER = "importserver";

    /**Path to remove publish locks too. */
    private static final String PATH_REMOVE_PUBLISH_LOCKS = "removepublishlocks";

    /**Path to static export tool.*/
    private static final String PATH_STATIC_EXPORT = "staticexport";

    /**
     * Returns the list of all uploadable zip files and uploadable folders available on the server.<p>
     *
     * @param includeFolders if true, the uploadable folders are included in the list
     * @return the list of all uploadable zip files and uploadable folders available on the server
     */
    protected static List<String> getFileListFromServer(boolean includeFolders) {

        List<String> result = new ArrayList<String>();

        // get the RFS package export path
        String exportpath = OpenCms.getSystemInfo().getPackagesRfsPath();
        File folder = new File(exportpath);

        // get a list of all files of the packages folder
        String[] files = folder.list();
        for (int i = 0; i < files.length; i++) {
            File diskFile = new File(exportpath, files[i]);
            // check this is a file and ends with zip -> this is a database upload file
            if (diskFile.isFile() && diskFile.getName().endsWith(".zip")) {
                result.add(diskFile.getName());
            } else if (diskFile.isDirectory()
                && includeFolders
                && (!diskFile.getName().equalsIgnoreCase(FOLDER_MODULES))
                && ((new File(diskFile + File.separator + FILE_MANIFEST)).exists())) {
                // this is an unpacked package, add it to uploadable files
                result.add(diskFile.getName());
            }
        }
        return result;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        //Deeper path
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_ADMIN_TOOL_NAME_SHORT_0));
            return crumbs;
        }

        crumbs.put(
            CmsDbManagerConfiguration.APP_ID,
            CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_ADMIN_TOOL_NAME_SHORT_0));
        if (state.startsWith(PATH_REMOVE_PUBLISH_LOCKS)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_DB_PUBLOCKS_ADMIN_TOOL_NAME_SHORT_0));
        }
        if (state.startsWith(PATH_STATIC_EXPORT)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_STATEXP_ADMIN_TOOL_NAME_SHORT_0));
        }
        if (state.startsWith(PATH_EXPORT)) {
            crumbs.put(
                "",
                CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORTSERVER_ADMIN_TOOL_NAME_SHORT_0));
        }
        if (state.startsWith(PATH_IMPORT_HTTP)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTHTTP_ADMIN_TOOL_NAME_SHORT_0));
        }
        if (state.startsWith(PATH_IMPORT_SERVER)) {
            crumbs.put(
                "",
                CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTSERVER_ADMIN_TOOL_NAME_SHORT_0));
        }
        if (crumbs.size() > 1) {
            return crumbs;
        } else {
            return new LinkedHashMap<String, String>(); //size==1 & state was not empty -> state doesn't match to known path
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (state.startsWith(PATH_REMOVE_PUBLISH_LOCKS)) {
            return getRemovePublishLocksView();
        }
        if (state.startsWith(PATH_STATIC_EXPORT)) {
            return new CmsDbStaticExportView();
        }
        if (state.startsWith(PATH_EXPORT)) {
            return new CmsDbExportView();
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        List<NavEntry> subNav = new ArrayList<NavEntry>();
        if (!state.startsWith(PATH_REMOVE_PUBLISH_LOCKS)) {
            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTSERVER_ADMIN_TOOL_NAME_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTSERVER_ADMIN_TOOL_HELP_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_IMPORT_SERVER)),
                    PATH_IMPORT_SERVER));

            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTHTTP_ADMIN_TOOL_NAME_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTHTTP_ADMIN_TOOL_HELP_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_IMPORT_HTTP)),
                    PATH_IMPORT_HTTP));

            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORTSERVER_ADMIN_TOOL_NAME_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_EXPORTSERVER_ADMIN_TOOL_HELP_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_EXPORT)),
                    PATH_EXPORT));

            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_STATEXP_ADMIN_TOOL_NAME_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_STATEXP_ADMIN_TOOL_HELP_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_STATIC_EXPORT)),
                    PATH_STATIC_EXPORT));

            subNav.add(
                new NavEntry(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_DB_PUBLOCKS_ADMIN_TOOL_NAME_0),
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_DB_PUBLOCKS_ADMIN_TOOL_HELP_0),
                    new ExternalResource(OpenCmsTheme.getImageLink(ICON_REMOVE_PUBLISH_LOCKS)),
                    PATH_REMOVE_PUBLISH_LOCKS));
        }
        return subNav;
    }

    /**
     * Component for remove publish lock surface.<p>
     *
     * @return HorizontalSplitPanel
     */
    private HorizontalSplitPanel getRemovePublishLocksView() {

        m_rootLayout.setMainHeightFull(true);
        HorizontalSplitPanel sp = new HorizontalSplitPanel();
        sp.setSizeFull();

        CmsDbRemovePublishLocks publishLocksView = new CmsDbRemovePublishLocks();

        sp.setFirstComponent(new CmsInternalResources(publishLocksView));
        sp.setSecondComponent(publishLocksView);
        sp.addStyleName("v-align-center");
        sp.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
        return sp;
    }
}
