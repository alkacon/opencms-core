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
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.ui.Component;

/**
 * Class for database manager app.<p>A_CmsAttributeAwareApp
 */
public class CmsDbManager extends A_CmsAttributeAwareApp {

    /** Name of the manifest file used in upload files. */
    private static final String FILE_MANIFEST = "manifest.xml";

    /** Name of the sub-folder containing the OpenCms module packages. */
    private static final String FOLDER_MODULES = "modules";

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
        if (files != null) {
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
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_STATS_TITLE_0));
            return crumbs;
        }

        return new LinkedHashMap<String, String>(); //size==1 & state was not empty -> state doesn't match to known path

    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            return new CmsResourceTypeStatsView();
        }

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

}
