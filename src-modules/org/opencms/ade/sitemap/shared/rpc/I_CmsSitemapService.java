/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/rpc/Attic/I_CmsSitemapService.java,v $
 * Date   : $Date: 2011/01/21 11:09:42 $
 * Version: $Revision: 1.24 $
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
import org.opencms.gwt.CmsRpcException;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.24 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsVfsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
@RemoteServiceRelativePath("org.opencms.ade.sitemap.CmsVfsSitemapService.gwt")
public interface I_CmsSitemapService extends RemoteService {

    /**
     * Creates a sub-sitemap of the given sitemap starting from a path.<p>
     * 
     * @param sitemapUri the URI of the parent sitemap 
     * @param path the path in the parent sitemap from which the sub-sitemap should be created 
     * 
     * @return the sub-sitemap creation result 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsSubSitemapInfo createSubSitemap(String sitemapUri, String path) throws CmsRpcException;

    /**
     * Returns broken link data bean, containing a list of all not yet loaded sub elements and a list of beans which
     * represent the links which would be broken if the sitemap entries
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
     * 
     * @return a list of beans representing links which will be broken by deleting the sitemap entries 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsBrokenLinkData getBrokenLinksToSitemapEntries(
        CmsClientSitemapEntry deleteEntry,
        List<CmsUUID> open,
        List<CmsUUID> closed) throws CmsRpcException;

    /**
     * Returns the sitemap children for the given path.<p>
     * 
     * @param sitemapUri the URI of the sitemap 
     * @param root the site relative root
     *  
     * @return the sitemap children
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    List<CmsClientSitemapEntry> getChildren(String sitemapUri, String root) throws CmsRpcException;

    /**
     * Returns the sitemap entry for the given path.<p>
     * 
     * @param sitemapUri the URI of the sitemap 
     * @param root the site relative root
     *  
     * @return the sitemap entry
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsClientSitemapEntry getEntry(String sitemapUri, String root) throws CmsRpcException;

    /**
     * Merges one of its sub-sitemaps into it.<p>
     * 
     * @param sitemapUri the URI of the current sitemap
     * @param path the path at which the sub-sitemap should be merged into the parent sitemap 
     * 
     * @return the result of the merge operation
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsSitemapMergeInfo mergeSubSitemap(String sitemapUri, String path) throws CmsRpcException;

    /**
     * Returns the initialization data for the given sitemap.<p>
     * 
     * @param sitemapUri the site relative path
     *  
     * @return the initialization data
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsSitemapData prefetch(String sitemapUri) throws CmsRpcException;

    /**
     * Saves the change to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param change the change to save
     * 
     * @return the new timestamp
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    boolean save(String sitemapUri, CmsSitemapChange change) throws CmsRpcException;

    /**
     * Saves the change to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param change the change to save
     * 
     * @return the new timestamp
     * 
     * @throws CmsRpcException if something goes wrong
     */
    @SynchronizedRpcRequest
    boolean saveSync(String sitemapUri, CmsSitemapChange change) throws CmsRpcException;

}
