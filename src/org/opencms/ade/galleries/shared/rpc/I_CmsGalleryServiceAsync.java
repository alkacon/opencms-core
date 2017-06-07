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

package org.opencms.ade.galleries.shared.rpc;

import org.opencms.ade.galleries.shared.CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handles all RPC services related to the gallery dialog.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.ade.galleries.CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync
 */
public interface I_CmsGalleryServiceAsync {

    /**
     * Deletes the given resource.<p>
     *
     * @param resourcePath the resource path of the resource to delete
     * @param callback the callback
     */
    void deleteResource(String resourcePath, AsyncCallback<Void> callback);

    /**
     * Loads the gallery configuration for the adeView mode.<p>
     *
     * @param callback the callback for the result
     */
    void getAdeViewModeConfiguration(AsyncCallback<CmsGalleryConfiguration> callback);

    /**
     * Returns the available galleries depending on the given resource types.<p>
     *
     * @param resourceTypes the resource types
     * @param callback the callback
     */
    void getGalleries(List<String> resourceTypes, AsyncCallback<List<CmsGalleryFolderBean>> callback);

    /**
     * Returns the resource info for a single resource.<p>
     *
     * @param path the resource path
     * @param locale the content locale
     * @param callback the callback
     */
    void getInfoForResource(String path, String locale, AsyncCallback<CmsResultItemBean> callback);

    /**
     * Returns the initial data for the given gallery mode.<p>
     *
     * @param conf the gallery configuration
     * @param callback the callback
     */
    void getInitialSettings(CmsGalleryConfiguration conf, AsyncCallback<CmsGalleryDataBean> callback);

    /**
     * Performs an initial search based on the given data bean and the available parameters of the request.<p>
     *
     * @param data the data bean
     * @param callback the callback
     */
    void getSearch(CmsGalleryDataBean data, AsyncCallback<CmsGallerySearchBean> callback);

    /**
     * Returns the gallery search object containing search results and the currant search parameter.<p>
     *
     * @param searchObj the current search object
     * @param callback the callback
     */
    void getSearch(CmsGallerySearchBean searchObj, AsyncCallback<CmsGallerySearchBean> callback);

    /**
     * Returns the sub entries to the given sitemap path.<p>
     *
     * @param rootPath the root path
     * @param isRoot <code>true</code> if the requested entry is the root entry
     * @param filter the search filter, only relevant when isRoot is true
     * @param callback the asynchronous callback
     */
    void getSubEntries(
        String rootPath,
        boolean isRoot,
        String filter,
        AsyncCallback<List<CmsSitemapEntryBean>> callback);

    /**
     * Gets the sub-folders of a folder.<p>
     *
     * @param path the path of a folder
     * @param callback the asynchronous callback
     */
    void getSubFolders(String path, AsyncCallback<List<CmsVfsEntryBean>> callback);

    /**
     * Loads the root VFS entry bean for the given site root.<p>
     *
     * @param path the site root
     * @param filter the search filter
     *
     * @param resultCallback the callback for the result
     * */
    void loadVfsEntryBean(String path, String filter, AsyncCallback<CmsVfsEntryBean> resultCallback);

    /**
     * Stores the result view type with the user.<p>
     *
     * @param resultViewType the result view type
     * @param callback the callback
     */
    void saveResultViewType(String resultViewType, AsyncCallback<Void> callback);

    /**
     * Saves the tree open state.<p>
     *
     * @param treeName the tree name
     * @param treeToken the tree token
     * @param siteRoot the site root
     * @param openItems the open items
     * @param callback the result callback
     */
    void saveTreeOpenState(
        String treeName,
        String treeToken,
        String siteRoot,
        Set<CmsUUID> openItems,
        AsyncCallback<Void> callback);

    /**
     * Updates the offline indices.<p>
     *
     * @param callback  the callback
     */
    void updateIndex(AsyncCallback<Void> callback);
}
