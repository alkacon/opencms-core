/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/rpc/Attic/I_CmsSitemapServiceAsync.java,v $
 * Date   : $Date: 2010/05/03 06:23:55 $
 * Version: $Revision: 1.8 $
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
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.I_CmsSitemapChange;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public interface I_CmsSitemapServiceAsync {

    /**
     * Creates a new sub-sitemap resource from the given sitemap and path.<p>
     * 
     * @param sitemapUri the super sitemap URI
     * @param path the starting path
     * @param callback the async callback
     */
    void createSubsitemap(String sitemapUri, String path, AsyncCallback<String> callback);

    /**
     * Executed when leaving the page.<p>
     * 
     * @param recentList the modified recent list, or <code>null</code> if it has not been modified
     * @param callback the async callback
     */
    @SynchronizedRpcRequest
    void exit(List<CmsClientSitemapEntry> recentList, AsyncCallback<Void> callback);

    /**
     * Returns the sitemap children for the given path.<p>
     * 
     * @param root the site relative root
     * @param callback the async callback
     */
    void getChildren(String root, AsyncCallback<List<CmsClientSitemapEntry>> callback);

    /**
     * Returns the sitemap entry for the given path.<p>
     * 
     * @param root the site relative root
     * @param callback the async callback
     */
    void getEntry(String root, AsyncCallback<CmsClientSitemapEntry> callback);

    /**
     * Returns the initialization data for the given sitemap.<p>
     * 
     * @param sitemapUri the site relative path
     * @param callback the async callback
     */
    void getInitData(String sitemapUri, AsyncCallback<CmsSitemapData> callback);

    /**
     * Returns the root sitemap entry for the given sitemap.<p>
     * 
     * @param sitemapUri the site relative path
     * @param callback the async callback
     */
    void getRoot(String sitemapUri, AsyncCallback<CmsClientSitemapEntry> callback);

    /**
     * Merges the given super sitemap with the sub-sitemap at the given path.<p>
     * 
     * @param sitemapUri the super sitemap URI
     * @param path the starting path
     * @param callback the async callback
     */
    void mergeSubsitemap(String sitemapUri, String path, AsyncCallback<Void> callback);

    /**
     * Saves the changes to the given sitemap.<p>
     * 
     * @param sitemapUri the sitemap URI 
     * @param changes the changes to save
     * @param callback the async callback
     */
    void save(String sitemapUri, List<I_CmsSitemapChange> changes, AsyncCallback<Void> callback);
}
