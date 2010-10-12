/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/I_CmsSitemapCache.java,v $
 * Date   : $Date: 2010/10/12 15:02:41 $
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The interface for a sitemap cache.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision 1.0 $
 * 
 * @since 8.0.0
 */
public interface I_CmsSitemapCache {

    /**
     * Returns the active sitemap lookup table.<p>
     * 
     * This method is synchronized since it does not make any sense 
     * to concurrently initialize the look up table.<p>
     * 
     * @param cms the current CMS context
     * 
     * @return the active sitemap table, as localized entry point root path vs sitemap resource root path
     * 
     * @throws CmsException if something goes wrong
     */
    Map<String, String> getActiveSitemaps(CmsObject cms) throws CmsException;

    /**
     * Returns the default sitemap properties.<p>
     * 
     * @param cms the current cms context
     * 
     * @return the default sitemap properties
     */
    Map<String, String> getDefaultProperties(CmsObject cms);

    /**
     * Gets sitemap entries by root vfs path.<p>
     * 
     * @param cms the current CMS context 
     * @param rootPath the root path
     *  
     * @return a list of sitemap entries which point to a resource with the given root path 
     * @throws CmsException if something goes wrong 
     */
    List<CmsInternalSitemapEntry> getEntriesByRootVfsPath(CmsObject cms, String rootPath) throws CmsException;

    /**
     * Gets the entries of the sitemap which refer to a resource with a given structure id.<p>
     * 
     * @param cms the CMS context 
     * @param structureId the structure id 
     * 
     * @return the sitemap entries whi
     * 
     * @throws CmsException if something goes wrong
     */
    List<CmsInternalSitemapEntry> getEntriesByStructureId(CmsObject cms, CmsUUID structureId) throws CmsException;

    /**
     * Returns the sitemap entry for the given id and current project.<p>
     *
     * @param cms the current CMS context
     * @param id the id to look for
     * 
     * @return the sitemap entry, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong 
     */
    CmsInternalSitemapEntry getEntryById(CmsObject cms, CmsUUID id) throws CmsException;

    /**
     * Returns the sitemap entry for the given URI and current project.<p>
     *
     * @param cms the current CMS context
     * @param uri the URI to look for
     * 
     * @return the sitemap entry, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong 
     */
    CmsInternalSitemapEntry getEntryByUri(CmsObject cms, String uri) throws CmsException;

    /**
     * Returns a map from URIs of active sitemaps to their entry points.<p>
     * 
     * @param cms the current CMS context 
     * @return a map from sitemap URIs to entry points 
     * 
     * @throws CmsException if something goes wrong 
     */
    Map<String, String> getEntryPoints(CmsObject cms) throws CmsException;

    /**
     * Returns the configured export name for a given site root (or null, if no export name is configured).<p>
     * 
     * @param siteRoot the site root 
     * 
     * @return the export name for the given site root, or null 
     * 
     * @throws CmsException if something goes wrong 
     */
    String getExportName(String siteRoot) throws CmsException;

    /**
     * Returns the map from export names to site roots.<p>
     * 
     * @return a map from export names to site roots
     *  
     * @throws CmsException if something goes wrong 
     */
    Map<String, String> getSiteRootsForExportNames() throws CmsException;

    /**
     * Gets the roots of the sites which use a sitemap.<p>
     * 
     * @param cms the current CMS context 
     * 
     * @return a set of site roots 
     * 
     * @throws CmsException if something goes wrong 
     */
    Set<String> getSiteRootsWithSitemap(CmsObject cms) throws CmsException;

    /**
     * This is called when the system is shut down.<p>
     */
    void shutdown();

}