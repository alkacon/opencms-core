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
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler class for the sitemap tree tab.<p>
 * 
 * @since 8.5.0
 */
public class CmsSitemapTabHandler extends A_CmsTabHandler {

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
     * Loads the sub entries for the given path.<p>
     * 
     * @param path the site path
     * @param siteRoot the site root, if requesting from another site
     * @param isRoot <code>true</code> if the requested entry is the root entry
     * @param callback the callback to execute with the result
     */
    public void getSubEntries(
        String path,
        String siteRoot,
        boolean isRoot,
        AsyncCallback<List<CmsSitemapEntryBean>> callback) {

        m_controller.getSubEntries(path, siteRoot, isRoot, callback);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        if (getTab().isInitialized()) {
            getTab().onContentChange();
        } else {
            getSubEntries("/", null, true, new AsyncCallback<List<CmsSitemapEntryBean>>() {

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

        // nothing to do, no sorting this one
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

}
