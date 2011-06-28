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

package org.opencms.xml.containerpage;

import org.opencms.cache.CmsVfsCache;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsLog;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Cache object instance for simultaneously cache online and offline items.<p>
 * 
 * @since 7.6 
 */
public final class CmsADECache extends CmsVfsCache {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsADECache.class);

    /** Cache for offline container pages. */
    private Map<String, CmsXmlContainerPage> m_containerPagesOffline;

    /** Cache for online container pages. */
    private Map<String, CmsXmlContainerPage> m_containerPagesOnline;

    /** Cache for offline group containers. */
    private Map<String, CmsXmlGroupContainer> m_groupContainersOffline;

    /** Cache for online group containers. */
    private Map<String, CmsXmlGroupContainer> m_groupContainersOnline;

    /**
     * Initializes the cache. Only intended to be called during startup.<p>
     * 
     * @param memMonitor the memory monitor instance
     * @param cacheSettings the system cache settings
     * 
     * @see org.opencms.main.OpenCmsCore#initConfiguration
     */
    public CmsADECache(CmsMemoryMonitor memMonitor, CmsADECacheSettings cacheSettings) {

        initialize(memMonitor, cacheSettings);
        registerEventListener();
    }

    /**
     * Flushes the container pages cache.<p>
     * 
     * @param online if to flush the online or offline cache
     */
    public void flushContainerPages(boolean online) {

        if (online) {
            m_containerPagesOnline.clear();
        } else {
            m_containerPagesOffline.clear();
        }
    }

    /**
     * Flushes the group containers cache.<p>
     * 
     * @param online if to flush the online or offline cache
     */
    public void flushGroupContainers(boolean online) {

        if (online) {
            m_groupContainersOnline.clear();
        } else {
            m_groupContainersOffline.clear();
        }
    }

    /**
     * Returns the cached container page under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param online if cached in online or offline project
     * 
     * @return the cached container page or <code>null</code> if not found
     */
    public CmsXmlContainerPage getCacheContainerPage(String key, boolean online) {

        CmsXmlContainerPage retValue;
        if (online) {
            retValue = m_containerPagesOnline.get(key);
            if (LOG.isDebugEnabled()) {
                if (retValue == null) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MISSED_ONLINE_1,
                        new Object[] {key}));

                } else {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MATCHED_ONLINE_2,
                        new Object[] {key, retValue}));
                }
            }
        } else {
            retValue = m_containerPagesOffline.get(key);
            if (LOG.isDebugEnabled()) {
                if (retValue == null) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MISSED_OFFLINE_1,
                        new Object[] {key}));

                } else {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MATCHED_OFFLINE_2,
                        new Object[] {key, retValue}));
                }
            }
        }
        if (retValue != null) {
            //System.out.println("got cached page: " + retValue.getFile().getRootPath());
        }
        return retValue;
    }

    /**
     * Returns the cached group container under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param online if cached in online or offline project
     * 
     * @return the cached group container or <code>null</code> if not found
     */
    public CmsXmlGroupContainer getCacheGroupContainer(String key, boolean online) {

        CmsXmlGroupContainer retValue;
        if (online) {
            retValue = m_groupContainersOnline.get(key);
            if (LOG.isDebugEnabled()) {
                if (retValue == null) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MISSED_ONLINE_1,
                        new Object[] {key}));

                } else {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MATCHED_ONLINE_2,
                        new Object[] {key, retValue}));
                }
            }
        } else {
            retValue = m_groupContainersOffline.get(key);
            if (LOG.isDebugEnabled()) {
                if (retValue == null) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MISSED_OFFLINE_1,
                        new Object[] {key}));

                } else {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MATCHED_OFFLINE_2,
                        new Object[] {key, retValue}));
                }
            }
        }
        return retValue;
    }

    /**
     * Returns the cache key for the given parameters.<p>
     * 
     * @param structureId the container page's structure id
     * @param keepEncoding if to keep the encoding while unmarshalling
     * 
     * @return the cache key for the given container page and parameters
     */
    public String getCacheKey(CmsUUID structureId, boolean keepEncoding) {

        return structureId.toString() + "_" + keepEncoding;
    }

    /**
     * Caches the given container page under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param containerPage the object to cache
     * @param online if to cache in online or offline project
     */
    public void setCacheContainerPage(String key, CmsXmlContainerPage containerPage, boolean online) {

        //System.out.println("caching page:" + containerPage.getFile().getRootPath());

        if (online) {
            m_containerPagesOnline.put(key, containerPage);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_SET_ONLINE_2,
                    new Object[] {key, containerPage}));
            }
        } else {
            m_containerPagesOffline.put(key, containerPage);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_SET_OFFLINE_2,
                    new Object[] {key, containerPage}));
            }
        }
    }

    /**
     * Caches the given group container under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param groupContainer the object to cache
     * @param online if to cache in online or offline project
     */
    public void setCacheGroupContainer(String key, CmsXmlGroupContainer groupContainer, boolean online) {

        if (online) {
            m_groupContainersOnline.put(key, groupContainer);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_SET_ONLINE_2,
                    new Object[] {key, groupContainer}));
            }
        } else {
            m_groupContainersOffline.put(key, groupContainer);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_SET_OFFLINE_2,
                    new Object[] {key, groupContainer}));
            }
        }
    }

    /**
     * Removes the container page identified by its structure id from the cache.<p>
     * 
     * @param structureId the container page's structure id
     * @param online if online or offline
     */
    public void uncacheContainerPage(CmsUUID structureId, boolean online) {

        if (online) {
            m_containerPagesOnline.remove(getCacheKey(structureId, true));
            m_containerPagesOnline.remove(getCacheKey(structureId, false));
        } else {
            m_containerPagesOffline.remove(getCacheKey(structureId, true));
            m_containerPagesOffline.remove(getCacheKey(structureId, false));
        }
    }

    /**
     * Removes the group container identified by its structure id from the cache.<p>
     * 
     * @param structureId the group container's structure id
     * @param online if online or offline
     */
    public void uncacheGroupContainer(CmsUUID structureId, boolean online) {

        if (online) {
            m_groupContainersOnline.remove(getCacheKey(structureId, true));
            m_groupContainersOnline.remove(getCacheKey(structureId, false));
        } else {
            m_groupContainersOffline.remove(getCacheKey(structureId, true));
            m_groupContainersOffline.remove(getCacheKey(structureId, false));
        }
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#flush(boolean)
     */
    @Override
    protected void flush(boolean online) {

        flushContainerPages(online);
        flushGroupContainers(online);
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
        if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            // remove the resource cached by it's structure ID
            //System.out.println("uncaching page: " + resource.getRootPath());
            uncacheContainerPage(resource.getStructureId(), false);
        } else {
            uncacheGroupContainer(resource.getStructureId(), false);
        }
    }

    /**
     * Initializes the caches.<p>
     * 
     * @param memMonitor the memory monitor instance
     * @param cacheSettings the system cache settings
     */
    private void initialize(CmsMemoryMonitor memMonitor, CmsADECacheSettings cacheSettings) {

        // container page caches
        Map<String, CmsXmlContainerPage> lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getContainerPageOfflineSize());
        m_containerPagesOffline = Collections.synchronizedMap(lruMapCntPage);
        memMonitor.register(CmsADECache.class.getName() + ".containerPagesOffline", lruMapCntPage);

        lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getContainerPageOnlineSize());
        m_containerPagesOnline = Collections.synchronizedMap(lruMapCntPage);
        memMonitor.register(CmsADECache.class.getName() + ".containerPagesOnline", lruMapCntPage);

        // container page caches
        Map<String, CmsXmlGroupContainer> lruMapGroupContainer = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getGroupContainerOfflineSize());
        m_groupContainersOffline = Collections.synchronizedMap(lruMapGroupContainer);
        memMonitor.register(CmsADECache.class.getName() + ".groupContainersOffline", lruMapGroupContainer);

        lruMapGroupContainer = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getGroupContainerOnlineSize());
        m_groupContainersOnline = Collections.synchronizedMap(lruMapGroupContainer);
        memMonitor.register(CmsADECache.class.getName() + ".groupContainersOnline", lruMapGroupContainer);
    }
}
