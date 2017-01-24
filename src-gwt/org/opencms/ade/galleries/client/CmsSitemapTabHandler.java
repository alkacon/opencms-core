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

import org.opencms.ade.galleries.client.ui.CmsSitemapTab;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Handler class for the sitemap tree tab.<p>
 *
 * @since 8.5.0
 */
public class CmsSitemapTabHandler extends A_CmsTabHandler {

    /** The site root used for loading / saving tree state data. */
    private String m_siteRoot;

    /**
     * Creates a new sitemap tab handler.<p>
     *
     * @param controller the gallery controller
     */
    public CmsSitemapTabHandler(CmsGalleryController controller) {

        super(controller);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#clearParams()
     */
    @Override
    public void clearParams() {

        // nothing to do, no parameters from this tab
    }

    /**
     * Gets the selected site root.<p>
     *
     * @return the selected site root
     */
    public String getDefaultSelectedSiteRoot() {

        return m_controller.getDefaultSitemapTabSiteRoot();
    }

    /**
     * Gets the path which is used when the sitemap entry is selected.<p>
     *
     * @param sitemapEntry the sitemap entry
     *
     * @return the path to use when the entry is selected
     */
    public String getSelectPath(CmsSitemapEntryBean sitemapEntry) {

        String normalizedSiteRoot = CmsStringUtil.joinPaths(CmsCoreProvider.get().getSiteRoot(), "/");
        String rootPath = sitemapEntry.getRootPath();
        if (rootPath.startsWith(normalizedSiteRoot)) {
            return rootPath.substring(normalizedSiteRoot.length() - 1);
        }
        return sitemapEntry.getRootPath();
    }

    /**
     * Gets the select options for the sort list.<p>
     *
     * @return the select options for the sort list
     */
    public LinkedHashMap<String, String> getSortList() {

        if (!m_controller.isShowSiteSelector() || !(m_controller.getSitemapSiteSelectorOptions().size() > 1)) {
            return null;
        }
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        for (CmsSiteSelectorOption option : m_controller.getSitemapSiteSelectorOptions()) {
            options.put(option.getSiteRoot(), option.getMessage());
        }
        return options;
    }

    /**
     * Loads the sub entries for the given path.<p>
     *
     * @param rootPath the root path
     * @param isRoot <code>true</code> if the requested entry is the root entry
     * @param callback the callback to execute with the result
     */
    public void getSubEntries(
        String rootPath,
        boolean isRoot,
        I_CmsSimpleCallback<List<CmsSitemapEntryBean>> callback) {

        m_controller.getSubEntries(rootPath, isRoot, null, callback);
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
     * Initializes the sitemap tab's content.<p>
     */
    public void initializeSitemapTab() {

        String siteRoot = m_controller.getPreselectOption(
            m_controller.getStartSiteRoot(),
            m_controller.getSitemapSiteSelectorOptions());
        getTab().setSortSelectBoxValue(siteRoot, true);
        m_controller.getDefaultScope();
        if (siteRoot == null) {
            siteRoot = m_controller.getDefaultSitemapTabSiteRoot();
        }
        m_siteRoot = siteRoot;
        getSubEntries(siteRoot, true, new I_CmsSimpleCallback<List<CmsSitemapEntryBean>>() {

            public void execute(List<CmsSitemapEntryBean> result) {

                getTab().fillDefault(result);
                getTab().onContentChange();
            }
        });

    }

    /**
     * This method is called when the tree open state changes.<p>
     *
     * @param openItemIds the structure ids of open entries
     */
    public void onChangeTreeState(Set<CmsUUID> openItemIds) {

        m_controller.saveTreeState(I_CmsGalleryProviderConstants.TREE_SITEMAP, m_siteRoot, openItemIds);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        if (getTab().isInitialized()) {
            getTab().onContentChange();
        } else {
            initializeSitemapTab();
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String, java.lang.String)
     */
    @Override
    public void onSort(final String sortParams, String filter) {

        m_controller.getSubEntries(sortParams, true, filter, new I_CmsSimpleCallback<List<CmsSitemapEntryBean>>() {

            public void execute(List<CmsSitemapEntryBean> entries) {

                getTab().fill(entries);
                setSiteRoot(sortParams);
                onChangeTreeState(new HashSet<CmsUUID>());
            }
        });

    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#removeParam(java.lang.String)
     */
    @Override
    public void removeParam(String paramKey) {

        // nothing to do, no parameters from this tab
    }

    /**
     * Returns the sitemap tab.<p>
     *
     * @return the sitemap tab
     */
    protected CmsSitemapTab getTab() {

        return m_controller.m_handler.m_galleryDialog.getSitemapTab();
    }

    /**
     * Setter for the site root attribute.<p>
     *
     * @param siteRoot the new value for the site root attribute
     */
    protected void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }
}
