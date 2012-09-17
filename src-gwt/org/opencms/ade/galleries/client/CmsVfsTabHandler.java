/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.ui.CmsVfsTab;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler class for the VFS tree tab.<p>
 * 
 * @since 8.0.0
 */
public class CmsVfsTabHandler extends A_CmsTabHandler {

    /** The VFS tab which this handler belongs to. */
    CmsVfsTab m_tab;

    /**
     * Creates a new VFS tab handler.<p>
     * 
     * @param controller the gallery controller
     */
    public CmsVfsTabHandler(CmsGalleryController controller) {

        super(controller);

    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#clearParams()
     */
    @Override
    public void clearParams() {

        m_controller.clearFolders(false);
    }

    /**
     * Gets the path which should be set as a value when a VFS entry is selected in the VFS tab.<p>
     * 
     * @param vfsEntry the VFS entry which has been selected 
     * 
     * @return the selection path for the given VFS entry 
     */
    public String getSelectPath(CmsVfsEntryBean vfsEntry) {

        String normalizedSiteRoot = CmsStringUtil.joinPaths(CmsCoreProvider.get().getSiteRoot(), "/");
        String rootPath = vfsEntry.getRootPath();
        if (rootPath.startsWith(normalizedSiteRoot)) {
            return rootPath.substring(normalizedSiteRoot.length() - 1);
        }
        return vfsEntry.getRootPath();
    }

    /**
     * Gets the sub-folders of a given folder.<p>
     * 
     * @param path the path of the folder whose subfolders should be retrieved
     * @param callback the callback for processing the subfolders   
     */
    public void getSubFolders(String path, AsyncCallback<List<CmsVfsEntryBean>> callback) {

        m_controller.getSubFolders(path, callback);
    }

    /**
     * Returns if this tab should offer select resource buttons.<p>
     * 
     * @return <code>true</code> if this tab should offer select resource buttons
     */
    public boolean hasSelectResource() {

        return m_controller.hasSelectFolder();
    }

    /**
     * This method is called when a folder is selected or deselected in the VFS tab.<p>
     * 
     * @param folder the folder which is selected or deselected 
     * 
     * @param selected true if the folder has been selected, false if it has been deselected 
     */
    public void onSelectFolder(String folder, boolean selected) {

        if (selected) {
            m_controller.addFolder(folder);
        } else {
            m_controller.removeFolder(folder);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        m_controller.m_handler.m_galleryDialog.getVfsTab().onContentChange();
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String,java.lang.String)
     */
    @Override
    public void onSort(String sortParams, String filter) {

        int siteIndex = Integer.parseInt(sortParams);
        final CmsSiteSelectorOption option = m_controller.getSiteSelectorOptions().get(siteIndex);
        m_controller.loadVfsEntryBean(option, new AsyncCallback<CmsVfsEntryBean>() {

            public void onFailure(Throwable caught) {

                // TODO: Auto-generated method stub

            }

            public void onSuccess(CmsVfsEntryBean result) {

                m_tab.fillInitially(Collections.singletonList(result));
            }

        });
        m_controller.clearFolders(true);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#removeParam(java.lang.String)
     */
    @Override
    public void removeParam(String paramKey) {

        m_controller.removeFolderParam(paramKey);
    }

    /**
     * Sets the tab which this handler is bound to.<p>
     * 
     * @param tab the VFS tab 
     */
    public void setTab(CmsVfsTab tab) {

        m_tab = tab;
    }
}
