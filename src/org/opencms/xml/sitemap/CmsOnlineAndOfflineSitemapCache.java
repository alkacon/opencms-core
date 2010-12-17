/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsOnlineAndOfflineSitemapCache.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
 * Version: $Revision: 1.10 $
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

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class wraps both the online and offline sitemap caches and delegates method calls
 * to them depending on the current project.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.10 $
 * 
 * @since 8.0.0
 */
public class CmsOnlineAndOfflineSitemapCache implements I_CmsSitemapCache {

    /** The offline sitemap cache. */
    private I_CmsSitemapCache m_offlineCache;

    /** The online sitemap cache. */
    private I_CmsSitemapCache m_onlineCache;

    /**
     * Initializes the cache. Only intended to be called during startup.<p>
     * 
     * @param adminCms the root admin CMS context for permission independent data retrieval 
     * @param memMonitor the memory monitor instance
     * @param structureIdCache a cache for memorizing the structure ids for resource paths
     * 
     * @see org.opencms.main.OpenCmsCore#initConfiguration
     */
    public CmsOnlineAndOfflineSitemapCache(
        CmsObject adminCms,
        CmsMemoryMonitor memMonitor,
        CmsVfsMemoryObjectCache structureIdCache) {

        m_offlineCache = new CmsSitemapStructureCache(adminCms, memMonitor, structureIdCache, true, false, "Offline");
        m_onlineCache = new CmsSitemapStructureCache(adminCms, memMonitor, structureIdCache, true, true, "Online");
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getActiveSitemaps(org.opencms.file.CmsObject)
     */
    public synchronized Map<String, String> getActiveSitemaps(CmsObject cms) throws CmsException {

        return getInternalCache(cms).getActiveSitemaps(cms);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getBestDetailPages(org.opencms.file.CmsObject, java.lang.String)
     */
    public List<CmsDetailPageInfo> getBestDetailPages(CmsObject cms, String type) throws CmsException {

        return getInternalCache(cms).getBestDetailPages(cms, type);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getDefaultProperties(org.opencms.file.CmsObject)
     */
    public Map<String, String> getDefaultProperties(CmsObject cms) {

        return getInternalCache(cms).getDefaultProperties(cms);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getEntriesByRootVfsPath(org.opencms.file.CmsObject, java.lang.String)
     */
    public List<CmsInternalSitemapEntry> getEntriesByRootVfsPath(CmsObject cms, String rootPath) throws CmsException {

        return getInternalCache(cms).getEntriesByRootVfsPath(cms, rootPath);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getEntriesByStructureId(org.opencms.file.CmsObject, org.opencms.util.CmsUUID)
     */
    public List<CmsInternalSitemapEntry> getEntriesByStructureId(CmsObject cms, CmsUUID structureId)
    throws CmsException {

        return getInternalCache(cms).getEntriesByStructureId(cms, structureId);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getEntryById(org.opencms.file.CmsObject, org.opencms.util.CmsUUID)
     */
    public CmsInternalSitemapEntry getEntryById(CmsObject cms, CmsUUID id) throws CmsException {

        return getInternalCache(cms).getEntryById(cms, id);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getEntryByUri(org.opencms.file.CmsObject, java.lang.String)
     */
    public CmsInternalSitemapEntry getEntryByUri(CmsObject cms, String uri) throws CmsException {

        return getInternalCache(cms).getEntryByUri(cms, uri);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getExportName(java.lang.String)
     */
    public String getExportName(String siteRoot) throws CmsException {

        return getInternalCache(true).getExportName(siteRoot);
    }

    /**
     * Returns either the online or offline sitemap cache depending on a flag.<p>
     * 
     * @param online if true, return the online sitemap cache, else the offline sitemap cache 
     * @return the online or offline sitemap cache 
     */
    public I_CmsSitemapCache getInternalCache(boolean online) {

        return online ? m_onlineCache : m_offlineCache;
    }

    /**
     * Returns either the online or offline sitemap cache depending on the current project of a {@link CmsObject}.<p>
     * 
     * @param cms the CMS context 
     * 
     * @return the online or offline sitemap cache 
     */
    public I_CmsSitemapCache getInternalCache(CmsObject cms) {

        boolean online = CmsProject.isOnlineProject(cms.getRequestContext().currentProject().getUuid());
        return getInternalCache(online);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getRuntimeInfoForSite(org.opencms.file.CmsObject, java.lang.String, java.util.Locale)
     */
    public CmsSitemapRuntimeInfo getRuntimeInfoForSite(CmsObject cms, String siteRoot, Locale locale)
    throws CmsException {

        return getInternalCache(cms).getRuntimeInfoForSite(cms, siteRoot, locale);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getSitemapForSiteRoot(org.opencms.file.CmsObject, java.lang.String)
     */
    public String getSitemapForSiteRoot(CmsObject cms, String siteRoot) throws CmsException {

        return getInternalCache(cms).getSitemapForSiteRoot(cms, siteRoot);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getSiteRootsForExportNames()
     */
    public Map<String, String> getSiteRootsForExportNames() throws CmsException {

        return getInternalCache(true).getSiteRootsForExportNames();
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#getSiteRootsWithSitemap(org.opencms.file.CmsObject)
     */
    public Set<String> getSiteRootsWithSitemap(CmsObject cms) throws CmsException {

        return getInternalCache(cms).getSiteRootsWithSitemap(cms);
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapCache#shutdown()
     */
    public void shutdown() {

        m_offlineCache.shutdown();
        m_onlineCache.shutdown();

    }

}
