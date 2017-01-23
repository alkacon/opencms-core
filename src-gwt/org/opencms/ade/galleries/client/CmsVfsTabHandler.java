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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.ui.CmsVfsTab;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler class for the VFS tree tab.<p>
 *
 * @since 8.0.0
 */
public class CmsVfsTabHandler extends A_CmsTabHandler {

    /** The structure ids of open entries to save. */
    Collection<CmsUUID> m_openItemIds = new HashSet<CmsUUID>();

    /** The VFS tab which this handler belongs to. */
    CmsVfsTab m_tab;

    /** The site root to use for loading / saving tree state. */
    private String m_siteRoot;

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
     * Gets the sort list for the tab.<p>
     *
     * @return the sort list for the tab
     */
    public LinkedHashMap<String, String> getSortList() {

        if (!m_controller.isShowSiteSelector() || !(m_controller.getVfsSiteSelectorOptions().size() > 1)) {
            return null;
        }
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        for (CmsSiteSelectorOption option : m_controller.getVfsSiteSelectorOptions()) {
            options.put(option.getSiteRoot(), option.getMessage());
        }
        return options;

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
     * This method is called when the tree open state is changed.<p>
     *
     * @param openItemIds the structure ids of open tree items
     */
    public void onChangeTreeState(Collection<CmsUUID> openItemIds) {

        m_openItemIds = openItemIds;
        saveTreeState();
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

        if (m_tab.isInitialized()) {
            m_tab.onContentChange();
        } else {
            String siteRoot = m_controller.getPreselectOption(
                m_controller.getStartSiteRoot(),
                m_controller.getVfsSiteSelectorOptions());
            m_tab.setSortSelectBoxValue(siteRoot, true);
            if (siteRoot == null) {
                siteRoot = m_controller.getDefaultVfsTabSiteRoot();
            }
            m_siteRoot = siteRoot;
            m_controller.loadVfsEntryBean(siteRoot, null, new AsyncCallback<CmsVfsEntryBean>() {

                public void onFailure(Throwable caught) {

                    // will never be called
                }

                public void onSuccess(CmsVfsEntryBean result) {

                    m_tab.fillInitially(Collections.singletonList(result));
                    m_tab.onContentChange();

                }
            });

        }
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String,java.lang.String)
     */
    @Override
    public void onSort(final String sortParams, String filter) {

        m_controller.loadVfsEntryBean(sortParams, filter, new AsyncCallback<CmsVfsEntryBean>() {

            public void onFailure(Throwable caught) {

                // will never be called.
            }

            public void onSuccess(CmsVfsEntryBean result) {

                m_tab.fillInitially(Collections.singletonList(result));
                setSiteRoot(sortParams);
                m_openItemIds.clear();
                if (result != null) {
                    m_openItemIds.add(result.getStructureId());
                }
                saveTreeState();
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

    /**
     * Saves the tree state.<p>
     */
    protected void saveTreeState() {

        m_controller.saveTreeState(
            I_CmsGalleryProviderConstants.TREE_VFS,
            m_siteRoot,
            new HashSet<CmsUUID>(m_openItemIds));
    }

    /**
     * Sets the site root to use for loading/saving tree state.<p>
     *
     * @param siteRoot the site root to set
     */
    protected void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }

}
