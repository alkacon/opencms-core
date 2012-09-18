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

package org.opencms.ade.galleries.shared.rpc;

import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsSiteSelectorOption;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.CmsRpcException;

import java.util.List;

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
     * Returns the initial data for the given gallery mode.<p>
     * 
     * @param galleryMode the gallery mode
     * @param referencePath the reference path
     * @param galleryPath the start gallery path
     * @param currentElement the current element
     * @param resourceTypes the available resource types (comma separated list)
     * @param galleryTypes the gallery types (comma separated list)
     * 
     * @return the data bean
     * 
     * @throws CmsRpcException if something goes wrong
     */
    CmsGalleryDataBean getInitialSettings(
        GalleryMode galleryMode,
        String referencePath,
        String galleryPath,
        String currentElement,
        String resourceTypes,
        String galleryTypes) throws CmsRpcException;

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
     * 
     * @return the sub entries 
     * 
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsSitemapEntryBean> getSubEntries(String rootPath, boolean isRoot) throws CmsRpcException;

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
     * Loads the root VFS entry bean for the given site selector option.
     * 
     * @param option the site selector option
     * 
     * @return the root VFS entry bean for the given option
     * 
     *  @throws CmsRpcException if something goes wrong 
     * */
    CmsVfsEntryBean loadVfsEntryBean(CmsSiteSelectorOption option) throws CmsRpcException;

    /**
     * Updates the offline indices.<p>
     * 
     * @throws CmsRpcException if something goes wrong
     */
    void updateIndex() throws CmsRpcException;

}