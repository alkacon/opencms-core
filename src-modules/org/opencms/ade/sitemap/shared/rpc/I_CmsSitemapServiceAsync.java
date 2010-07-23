/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/rpc/Attic/I_CmsSitemapServiceAsync.java,v $
 * Date   : $Date: 2010/07/23 11:38:26 $
 * Version: $Revision: 1.20 $
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

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapBrokenLinkBean;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;
import org.opencms.ade.sitemap.shared.CmsSubSitemapInfo;
import org.opencms.util.CmsUUID;
import org.opencms.xml.sitemap.I_CmsSitemapChange;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.20 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public interface I_CmsSitemapServiceAsync {

    /**
     * Returns a list of beans which represent the links which would be broken if the sitemap entries
     * passed as parameters were deleted.<p>
     * 
     * The "open" list entries will only be considered by themselves, while the sitemap entries with ids
     * in the "closed" list will be processed together with their descendants.<p>
     * 
     * This is necessary because the sitemap editor client code uses a lazily-loaded tree and thus does 
     * not have the full list of sitemap entries which are going to be deleted.<p>
     * 
     * @param open the list of sitemap entry ids which should be considered by themselves 
     * @param closed the list of sitemap entry ids which should be considedered together with their descendants
     * @param callback the asynchronous callback  
     * 
     */
    void getBrokenLinksToSitemapEntries(
        List<CmsUUID> open,
        List<CmsUUID> closed,
        AsyncCallback<List<CmsSitemapBrokenLinkBean>> callback);

    /**
     * Returns the sitemap children for the given path.<p>
     * 
     * @param sitemapUri the URI of the sitemap 
     * @param root the site relative root
     * @param callback the async callback
     */
    void getChildren(String sitemapUri, String root, AsyncCallback<List<CmsClientSitemapEntry>> callback);

    /**
     * Returns the sitemap entry for the given path.<p>
     * 
     * @param sitemapUri the URI of the sitemap 
     * @param root the site relative root
     * @param callback the async callback
     */
    void getEntry(String sitemapUri, String root, AsyncCallback<CmsClientSitemapEntry> callback);

    /**
     * Returns the initialization data for the given sitemap.<p>
     * 
     * @param sitemapUri the site relative path
     * @param callback the async callback
     */
    void prefetch(String sitemapUri, AsyncCallback<CmsSitemapData> callback);

    /**
     * Saves the changes to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param changes the changes to save
     * @param clipboardData the modified clipboard data, or <code>null</code> if it has not been modified
     * @param callback the async callback
     */
    void save(
        String sitemapUri,
        List<I_CmsSitemapChange> changes,
        CmsSitemapClipboardData clipboardData,
        AsyncCallback<Long> callback);

    /**
     * Saves a list of changes to a sitemap and then creates a sub-sitemap of the given sitemap starting from a path.<p>
     * 
     * @param sitemapUri the URI of the parent sitemap 
     * @param changes the changes which should be applied to the parent sitemap 
     * @param path the path in the parent sitemap from which the sub-sitemap should be created
     * @param callback the async callback  
     */
    void saveAndCreateSubSitemap(
        String sitemapUri,
        List<I_CmsSitemapChange> changes,
        String path,
        AsyncCallback<CmsSubSitemapInfo> callback);

    /**
     * Saves the current sitemap and merges one of its sub-sitemaps into it.<p>
     * 
     * @param sitemapUri the super sitemap URI
     * @param changes the list of changes to be saved 
     * @param path the path at which the sub-sitemap should be merged into the parent sitemap 
     * 
     * @param callback the async callback
     */
    void saveAndMergeSubSitemap(
        String sitemapUri,
        List<I_CmsSitemapChange> changes,
        String path,
        AsyncCallback<CmsSitemapMergeInfo> callback);

    /**
     * Saves the changes to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param changes the changes to save
     * @param clipboardData the modified clipboard data, or <code>null</code> if it has not been modified
     * @param callback the async callback
     */
    @SynchronizedRpcRequest
    void saveSync(
        String sitemapUri,
        List<I_CmsSitemapChange> changes,
        CmsSitemapClipboardData clipboardData,
        AsyncCallback<Long> callback);

}
