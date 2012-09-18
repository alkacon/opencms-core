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

import org.opencms.ade.galleries.client.ui.CmsSitemapTab;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler class for the sitemap tree tab.<p>
 * 
 * @since 8.5.0
 */
public class CmsSitemapTabHandler extends A_CmsTabHandler {

    /** The sitemap tab which this handler belongs to. */
    CmsSitemapTab m_tab;

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

        return m_controller.getSitemapSiteSelectorOptions().get(0).getSiteRoot();
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

        if (!m_controller.isShowSiteSelector()) {
            return null;
        }
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        int i = 0;
        for (CmsSiteSelectorOption option : m_controller.getSitemapSiteSelectorOptions()) {
            String key = "" + i;
            options.put(key, option.getMessage());
            i += 1;
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
    public void getSubEntries(String rootPath, boolean isRoot, AsyncCallback<List<CmsSitemapEntryBean>> callback) {

        m_controller.getSubEntries(rootPath, isRoot, callback);
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
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        if (getTab().isInitialized()) {
            getTab().onContentChange();
        } else {
            String key = m_controller.getPreselectOption(
                m_controller.getStartSiteRoot(),
                m_controller.getSitemapSiteSelectorOptions());
            getTab().setSortSelectBoxValue(key);
            getSubEntries(
                m_controller.getDefaultVfsTabSiteRoot(),
                true,
                new AsyncCallback<List<CmsSitemapEntryBean>>() {

                    public void onFailure(Throwable caught) {

                        // nothing to do
                    }

                    public void onSuccess(List<CmsSitemapEntryBean> result) {

                        getTab().fillInitially(result);
                        getTab().onContentChange();
                    }
                });
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String, java.lang.String)
     */
    @Override
    public void onSort(String sortParams, String filter) {

        int siteIndex = Integer.parseInt(sortParams);

        final CmsSiteSelectorOption option = m_controller.getSitemapSiteSelectorOptions().get(siteIndex);
        m_controller.getSubEntries(option.getSiteRoot(), true, new AsyncCallback<List<CmsSitemapEntryBean>>() {

            public void onFailure(Throwable caught) {

                // will never be called

            }

            public void onSuccess(List<CmsSitemapEntryBean> entries) {

                m_tab.fillInitially(entries);
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
     * Sets the tab for the handler.<p>
     * 
     * @param tab the tab for this handler 
     */
    public void setTab(CmsSitemapTab tab) {

        m_tab = tab;
    }

    /**
     * Returns the sitemap tab.<p>
     * 
     * @return the sitemap tab
     */
    protected CmsSitemapTab getTab() {

        return m_controller.m_handler.m_galleryDialog.getSitemapTab();
    }

}
