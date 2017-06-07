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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.cache;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/**
 * Implements a memory cache, that stores objects related to VFS files,
 * providing a cache for the "online" and another for the "offline" project.<p>
 *
 * @since 6.1.3
 */
public class CmsVfsMemoryObjectCache extends CmsVfsCache {

    /** Counts the number of instances created. */
    private static int instanceCounter;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsMemoryObjectCache.class);

    /** A cache that maps VFS resource names to Objects. */
    private static CmsVfsMemoryObjectCache m_vfsMemoryObjectCache;

    /** The id for this instance, which is used for distinguishing cache keys of different instances. */
    private int m_id;

    /**
     * Constructor, creates a new CmsVfsMemoryObjectCache.<p>
     */
    public CmsVfsMemoryObjectCache() {

        m_id = instanceCounter;
        instanceCounter += 1;
        // register the event listeners
        registerEventListener();
    }

    /**
     * Returns the VFS memory Object cache.<p>
     *
     * @return the VFS memory Object cache
     */
    public static CmsVfsMemoryObjectCache getVfsMemoryObjectCache() {

        if (m_vfsMemoryObjectCache == null) {
            m_vfsMemoryObjectCache = new CmsVfsMemoryObjectCache();
        }
        return m_vfsMemoryObjectCache;
    }

    /**
     * Return an object from the cache.<p>
     *
     * @param cms the current users OpenCms context
     * @param rootPath the rootPath of the VFS resource to get the object for
     * @return object form cache or null
     */
    public Object getCachedObject(CmsObject cms, String rootPath) {

        String key = getCacheKeyForCurrentProject(cms, rootPath);
        return OpenCms.getMemoryMonitor().getCachedVfsObject(key);
    }

    /**
     * Uses a transformer for loading an object from a path if it has not already been cached, and then caches it.<p>
     *
     * @param cms the CMS context
     * @param rootPath the root path from which the object should be loaded
     * @param function the function which should load the object from VFS if it isn't already cached
     *
     * @return the loaded object
     */
    public Object loadVfsObject(CmsObject cms, String rootPath, Transformer function) {

        Object result = getCachedObject(cms, rootPath);
        if (result == null) {
            result = function.transform(rootPath);
            putCachedObject(cms, rootPath, result);
        }
        return result;
    }

    /**
     * Puts an object into the cache.<p>
     *
     * @param cms the CmsObject
     * @param rootPath the rootPath of the VFS resource to store the object for
     * @param value the object to store
     */
    public void putCachedObject(CmsObject cms, String rootPath, Object value) {

        String key = getCacheKeyForCurrentProject(cms, rootPath);
        OpenCms.getMemoryMonitor().cacheVfsObject(key, value);
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#flush(boolean)
     */
    @Override
    protected void flush(boolean online) {

        OpenCms.getMemoryMonitor().flushCache(CmsMemoryMonitor.CacheType.VFS_OBJECT);
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#uncacheResource(org.opencms.file.CmsResource)
     */
    @Override
    protected void uncacheResource(CmsResource resource) {

        uncacheSystemId(resource.getRootPath());
    }

    /**
     * Returns a cache key for the given system id (filename) based on the status
     * of the given project flag.<p>
     *
     * @param systemId the system id (filename) to get the cache key for
     * @param online indicates if this key is generated for the online project
     *
     * @return the cache key for the system id
     */
    private String getCacheKey(String systemId, boolean online) {

        if (online) {
            return "online_(" + m_id + ")_" + systemId;
        }
        return "offline_(" + m_id + ")_" + systemId;
    }

    /**
     * Returns a cache key for the given root path based on the status
     * of the given CmsObject.<p>
     *
     * @param cms the cms context
     * @param rootPath the filename to get the cache key for
     *
     * @return the cache key for the system id
     */
    private String getCacheKeyForCurrentProject(CmsObject cms, String rootPath) {

        // check the project
        boolean project = (cms != null) ? cms.getRequestContext().getCurrentProject().isOnlineProject() : false;
        return getCacheKey(rootPath, project);
    }

    /**
     * Uncaches a system id (filename) from the internal offline temporary and content definitions caches.<p>
     *
     * The online resources cached for the online project are only flushed when a project is published.<p>
     *
     * @param systemId the system id (filename) to uncache
     */
    private void uncacheSystemId(String systemId) {

        String key = getCacheKey(systemId, false);
        Object o = OpenCms.getMemoryMonitor().getCachedVfsObject(key);
        OpenCms.getMemoryMonitor().uncacheVfsObject(key);
        if ((null != o) && LOG.isDebugEnabled()) {
            LOG.debug(
                org.opencms.xml.Messages.get().getBundle().key(
                    org.opencms.xml.Messages.LOG_ERR_UNCACHED_SYS_ID_1,
                    key));
        }
    }
}
