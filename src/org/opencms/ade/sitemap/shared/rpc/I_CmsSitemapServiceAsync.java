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

package org.opencms.ade.sitemap.shared.rpc;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsVfsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public interface I_CmsSitemapServiceAsync {

    /**
     * Creates a sub-sitemap of the given sitemap starting from the given entry.<p>
     * 
     * @param entryId the structure id of the sitemap entry to create a sub sitemap of
     * @param callback the async callback  
     */
    void createSubSitemap(CmsUUID entryId, AsyncCallback<CmsSitemapChange> callback);

    /**
     * Returns the sitemap children for the given entry.<p>
     * 
     * @param entryPointUri the URI of the sitemap entry point
     * @param entryId the entry id
     * @param levels the count of child levels to read
     * @param callback the async callback
     */
    void getChildren(String entryPointUri, CmsUUID entryId, int levels, AsyncCallback<CmsClientSitemapEntry> callback);

    /**
     * Merges a sub-sitemap into it's parent sitemap.<p>
     * 
     * @param entryPoint the sitemap entry point
     * @param subSitemapId the structure id of the sub sitemap folder
     * 
     * @param callback the async callback
     */
    void mergeSubSitemap(String entryPoint, CmsUUID subSitemapId, AsyncCallback<CmsSitemapChange> callback);

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
    void save(String sitemapUri, CmsSitemapChange change, AsyncCallback<CmsSitemapChange> callback);

    /**
     * Save the change to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param change the change to save
     * @param callback the async callback
     */
    @SynchronizedRpcRequest
    void saveSync(String sitemapUri, CmsSitemapChange change, AsyncCallback<CmsSitemapChange> callback);
}
