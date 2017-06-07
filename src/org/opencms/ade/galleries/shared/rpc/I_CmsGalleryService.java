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
import org.opencms.gwt.CmsRpcException;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Handles all RPC services related to the gallery dialog.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.ade.galleries.CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync
 */
public interface I_CmsGalleryService extends RemoteService {

    /**
     * Deletes the given resource.<p>
     *
     * @param resourcePath the resource path of the resource to delete
     *
     * @throws CmsRpcException if something goes wrong
     */
    void deleteResource(String resourcePath) throws CmsRpcException;

    /**
     * Loads the gallery configuration for the adeView mode.<p>
     *
     * @return the gallery configuration
     */
    CmsGalleryConfiguration getAdeViewModeConfiguration();

    /**
     * Returns the available galleries depending on the given resource types.<p>
     *
     * @param resourceTypes the resource types
     *
     * @return the galleries
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsGalleryFolderBean> getGalleries(List<String> resourceTypes) throws CmsRpcException;

    /**
     * Returns the resource info for a single resource.<p>
     *
     * @param path the resource path
     * @param locale the content locale
     *
     * @return the resource info
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsResultItemBean getInfoForResource(String path, String locale) throws CmsRpcException;

    /**
     * Returns the initial data for the given gallery mode.<p>
     *
     * @param conf the gallery configuration
     *
     * @return the data bean
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsGalleryDataBean getInitialSettings(CmsGalleryConfiguration conf) throws CmsRpcException;

    /**
     * Performs an initial search based on the given data bean and the available parameters of the request.<p>
     *
     * @param data the data bean
     *
     * @return the search result
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsGallerySearchBean getSearch(CmsGalleryDataBean data) throws CmsRpcException;

    /**
     * Returns the gallery search object containing search results and the currant search parameter.<p>
     *
     * @param searchObj the current search object
     * @return the search object containing search results
     * @throws CmsRpcException is something goes wrong
     */
    CmsGallerySearchBean getSearch(CmsGallerySearchBean searchObj) throws CmsRpcException;

    /**
     * Returns the sub entries to the given sitemap path.<p>
     *
     * @param rootPath the root path
     * @param isRoot <code>true</code> if the requested entry is the root entry
     * @param filter the search filter string
     *
     * @return the sub entries
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsSitemapEntryBean> getSubEntries(String rootPath, boolean isRoot, String filter) throws CmsRpcException;

    /**
     * Gets the sub-folders of a folder.<p>
     *
     * @param path the path of a folder
     *
     * @return beans representing the sub-folders of the folder
     *
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsVfsEntryBean> getSubFolders(String path) throws CmsRpcException;

    /**
     * Loads the root VFS entry bean for the given site root.
     *
     * @param path the site root
     * @param filter the filter string
     *
     * @return the root VFS entry bean for the given site root
     *
     *  @throws CmsRpcException if something goes wrong
     * */
    CmsVfsEntryBean loadVfsEntryBean(String path, String filter) throws CmsRpcException;

    /**
     * Stores the result view type with the user.<p>
     *
     * @param resultViewType the result view type
     */
    void saveResultViewType(String resultViewType);

    /**
     * Saves the tree open state for a tree tab.<p>
     *
     * @param treeName the tree name for which to save the tree state
     * @param treeToken the tree token for which to save the tree state
     * @param siteRoot the site root
     * @param openItems the set of structure ids of open tree items
     *
     * @throws CmsRpcException if something goes wrong
     */
    void saveTreeOpenState(String treeName, String treeToken, String siteRoot, Set<CmsUUID> openItems)
    throws CmsRpcException;

    /**
     * Updates the offline indices.<p>
     *
     * @throws CmsRpcException if something goes wrong
     */
    void updateIndex() throws CmsRpcException;

}