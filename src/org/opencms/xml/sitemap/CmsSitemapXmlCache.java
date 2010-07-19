/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapXmlCache.java,v $
 * Date   : $Date: 2010/07/19 12:35:34 $
 * Version: $Revision: 1.1 $
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

import org.opencms.cache.CmsVfsCache;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A cache for XML sitemaps, i.e. {@link CmsXmlSitemap}  instances.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapXmlCache extends CmsVfsCache {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsSitemapXmlCache.class);

    /** The map of sitemap documents. */
    private Map<String, CmsXmlSitemap> m_documents;

    /** The name of the sitemap XML cache. */
    private String m_name;

    /** True if this is an online cache and false if this is an offline cache. */
    private boolean m_online;

    /**
     * Creates a new sitemap XML cache.<p>
     * 
     * @param name the name of the sitemap cache 
     * @param online true if it should be an online cache, false if it should be an offline cache 
     * @param cacheSize the maximum number of documents in the cache 
     */
    public CmsSitemapXmlCache(String name, boolean online, int cacheSize) {

        m_online = online;
        m_name = name;
        Map<String, CmsXmlSitemap> lruMapDocs = CmsCollectionsGenericWrapper.createLRUMap(cacheSize);
        m_documents = Collections.synchronizedMap(lruMapDocs);
        register("sitemapDocs", lruMapDocs);
        registerEventListener();
    }

    /**
     * Creates a cache key for a given structure id and a "keep encoding" flag.<p>
     * 
     * @param structureId the structure id
     * @param keepEncoding the flag which indicates whether the encoding should be kept 
     * 
     * @return the cache key for the structure id and the flag 
     */
    public String getCacheKey(CmsUUID structureId, boolean keepEncoding) {

        return structureId.toString() + "_" + keepEncoding;
    }

    /**
     * Returns the cached sitemap under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * 
     * @return the cached sitemap or <code>null</code> if not found
     */
    public CmsXmlSitemap getDocument(String key) {

        CmsXmlSitemap retValue;

        retValue = m_documents.get(key);
        if (LOG.isDebugEnabled()) {
            if (retValue == null) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_CACHE_MISSED_2, m_name, key));
            } else {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_CACHE_MATCHED_3, m_name, key, retValue));
            }
        }
        return retValue;
    }

    /**
     * Caches the given sitemap under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param sitemap the object to cache
     */
    public void setDocument(String key, CmsXmlSitemap sitemap) {

        Map<String, CmsXmlSitemap> docs = m_documents;
        if (docs.containsKey(key)) {
            // many false calls due to unmarshal method dependency
            return;
        }
        docs.put(key, sitemap);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_CACHE_SET_3, m_name, key, sitemap));
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getClass().getName() + " (" + m_name + ")";
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#flush(boolean)
     */
    @Override
    protected void flush(boolean online) {

        if (online == m_online) {
            m_documents.clear();
        }
    }

    /**
     * Registers a cached object in the memory monitor.<p>
     * 
     * This does nothing if the <code>useMemoryMonitor</code> parameter of the constructor was <code>false</code>.
     *  
     * @param key the key for registering the object in the memory monitor 
     * @param obj the object to register in the memory monitor 
     */
    protected void register(String key, Object obj) {

        String cacheKey = CmsSitemapXmlCache.class.getName() + "." + key + "." + m_name;
        OpenCms.getMemoryMonitor().register(cacheKey, obj);
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#uncacheResource(org.opencms.file.CmsResource)
     */
    @Override
    protected void uncacheResource(CmsResource resource) {

        if (resource == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }
        if (!m_online) {
            // flush docs
            m_documents.remove(getCacheKey(resource.getStructureId(), true));
            m_documents.remove(getCacheKey(resource.getStructureId(), false));
        }

    }

}
