/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/rpc/Attic/I_CmsSitemapServiceAsync.java,v $
 * Date   : $Date: 2011/02/01 15:25:05 $
 * Version: $Revision: 1.25 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared.rpc;

import org.opencms.ade.sitemap.shared.CmsBrokenLinkData;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;
import org.opencms.ade.sitemap.shared.CmsSubSitemapInfo;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.25 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsVfsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public interface I_CmsSitemapServiceAsync {

    /**
     * Saves a list of changes to a sitemap and then creates a sub-sitemap of the given sitemap starting from a path.<p>
     * 
     * @param sitemapUri the URI of the parent sitemap 
     * @param path the path in the parent sitemap from which the sub-sitemap should be created
     * @param callback the async callback  
     */
    void createSubSitemap(String sitemapUri, String path, AsyncCallback<CmsSubSitemapInfo> callback);

    /**
     * Returns broken link data bean, containing a list of all not yet loaded sub elements and a list of beans
     * which represent the links which would be broken if the sitemap entries
     * passed as parameters were deleted.<p>
     * 
     * The "open" list entries will only be considered by themselves, while the sitemap entries with ids
     * in the "closed" list will be processed together with their descendants.<p>
     * 
     * This is necessary because the sitemap editor client code uses a lazily-loaded tree and thus does 
     * not have the full list of sitemap entries which are going to be deleted.<p>
     * 
     * @param deleteEntry the entry to delete 
     * @param open the list of sitemap entry ids which should be considered by themselves 
     * @param closed the list of sitemap entry ids which should be considedered together with their descendants
     * @param callback the asynchronous callback  
     * 
     */
    void getBrokenLinksToSitemapEntries(
        CmsClientSitemapEntry deleteEntry,
        List<CmsUUID> open,
        List<CmsUUID> closed,
        AsyncCallback<CmsBrokenLinkData> callback);

    /**
     * Returns the sitemap children for the given path.<p>
     * 
     * @param entryPointUri the URI of the sitemap entry point
     * @param root the site relative root
     * @param levels the count of child levels to read
     * @param callback the async callback
     */
    void getChildren(String entryPointUri, String root, int levels, AsyncCallback<List<CmsClientSitemapEntry>> callback);

    /**
     * Saves the current sitemap and merges one of its sub-sitemaps into it.<p>
     * 
     * @param entryPoint the sitemap entry point
     * @param path the path at which the sub-sitemap should be merged into the parent sitemap 
     * 
     * @param callback the async callback
     */
    void mergeSubSitemap(String entryPoint, String path, AsyncCallback<CmsSitemapMergeInfo> callback);

    /**
     * Returns the initialization data for the given sitemap.<p>
     * 
     * @param sitemapUri the site relative path
     * @param callback the async callback
     */
    void prefetch(String sitemapUri, AsyncCallback<CmsSitemapData> callback);

    /**
     * Saves the change to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param change the change to save
     * @param callback the async callback
     */
    void save(String sitemapUri, CmsSitemapChange change, AsyncCallback<Boolean> callback);

    /**
     * Save the change to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param change the change to save
     * @param callback the async callback
     */
    @SynchronizedRpcRequest
    void saveSync(String sitemapUri, CmsSitemapChange change, AsyncCallback<Boolean> callback);

}
