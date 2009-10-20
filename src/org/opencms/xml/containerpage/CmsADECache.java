/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsADECache.java,v $
 * Date   : $Date: 2009/10/20 07:38:54 $
 * Version: $Revision: 1.1.2.3 $
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Cache object instance for simultaneously cache online and offline items.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.3 $ 
 * 
 * @since 7.6 
 */
public final class CmsADECache implements I_CmsEventListener {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsADECache.class);

    /** Cache for ADE recent lists. */
    private Map<String, List<CmsContainerElementBean>> m_adeRecentLists;

    /** Cache for ADE search options. */
    private Map<String, CmsSearchOptions> m_adeSearchOptions;

    /** Cache for offline container elements. */
    private Map<String, CmsContainerElementBean> m_containerElementsOffline;

    /** Cache for offline container pages. */
    private Map<String, CmsXmlContainerPage> m_containerPagesOffline;

    /** Cache for online container pages. */
    private Map<String, CmsXmlContainerPage> m_containerPagesOnline;

    /**
     * Initializes the cache. Only intended to be called during startup.<p>
     * 
     * @param memMonitor the memory monitor instance
     * @param cacheSettings the system cache settings
     * 
     * @see org.opencms.main.OpenCmsCore#initConfiguration
     */
    public CmsADECache(CmsMemoryMonitor memMonitor, CmsADECacheSettings cacheSettings) {

        // container page caches
        Map<String, CmsXmlContainerPage> lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getContainerPageOfflineSize());
        m_containerPagesOffline = Collections.synchronizedMap(lruMapCntPage);
        memMonitor.register(CmsADECache.class.getName() + ".containerPagesOffline", lruMapCntPage);

        lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getContainerPageOnlineSize());
        m_containerPagesOnline = Collections.synchronizedMap(lruMapCntPage);
        memMonitor.register(CmsADECache.class.getName() + ".containerPagesOnline", lruMapCntPage);

        // container element cache
        Map<String, CmsContainerElementBean> lruMapCntElem = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getContainerElementOfflineSize());
        m_containerElementsOffline = Collections.synchronizedMap(lruMapCntElem);
        memMonitor.register(CmsADECache.class.getName() + ".containerElementsOffline", lruMapCntElem);

        // ADE search options
        Map<String, CmsSearchOptions> adeSearchOptions = new HashMap<String, CmsSearchOptions>();
        m_adeSearchOptions = Collections.synchronizedMap(adeSearchOptions);
        memMonitor.register(CmsADEManager.class.getName(), adeSearchOptions);

        // ADE recent lists
        Map<String, List<CmsContainerElementBean>> adeRecentList = new HashMap<String, List<CmsContainerElementBean>>();
        m_adeRecentLists = Collections.synchronizedMap(adeRecentList);
        memMonitor.register(CmsADEManager.class.getName(), adeRecentList);

        // add this class as an event handler to the cms event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCE_DELETED,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT,
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES,
            I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES});
    }

    /**
     * Takes care of cache synchronization and consistency.<p>
     * 
     * @param event the event to handle
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource = null;
        List<CmsResource> resources = null;

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                // a resource has been modified in a way that it *IS NOT* necessary also to clear 
                // lists of cached sub-resources where the specified resource might be contained inside.
                // all siblings are removed from the cache, too.
                resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                uncacheResource(resource);
                break;

            case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                // a list of resources and all of their properties have been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
            case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                // a list of resources has been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                flushContainerPages(true);
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                flushContainerPages(true);
                flushContainerPages(false);
                break;

            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                flushContainerPages(false);
                break;

            default:
                // noop
                break;
        }
    }

    /**
     * Flushes the ADE recent list cache.<p>
     */
    public void flushADERecentLists() {

        m_adeRecentLists.clear();
    }

    /**
     * Flushes the ADE search options cache.<p>
     */
    public void flushADESearchOptions() {

        m_adeSearchOptions.clear();
    }

    /**
     * Flushes the container elements cache.<p>
     */
    public void flushContainerElements() {

        m_containerElementsOffline.clear();

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
     * Returns the ADE recent list cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for, this may be the user's uuid
     * 
     * @return the cached recent list with the given cache key
     */
    public List<CmsContainerElementBean> getADERecentList(String key) {

        return m_adeRecentLists.get(key);
    }

    /**
     * Returns the ADE search options cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for, this may be the user's uuid
     * 
     * @return the cached search options with the given cache key
     */
    public CmsSearchOptions getADESearchOptions(String key) {

        return m_adeSearchOptions.get(key);
    }

    /**
     * Returns the cached container element under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * 
     * @return  the cached container element or <code>null</code> if not found
     */
    public CmsContainerElementBean getCacheContainerElement(String key) {

        return m_containerElementsOffline.get(key);
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
        return retValue;
    }

    /**
     * Caches the given ADE recent list under the given cache key.<p>
     * 
     * @param key the cache key
     * @param list the recent list to cache
     */
    public void setCacheADERecentList(String key, List<CmsContainerElementBean> list) {

        m_adeRecentLists.put(key, list);
    }

    /**
     * Caches the given ADE search options under the given cache key.<p>
     * 
     * @param key the cache key
     * @param opts the search options to cache
     */
    public void setCacheADESearchOptions(String key, CmsSearchOptions opts) {

        m_adeSearchOptions.put(key, opts);
    }

    /**
     * Caches the given container element under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param containerElement the object to cache
     */
    public void setCacheContainerElement(String key, CmsContainerElementBean containerElement) {

        m_containerElementsOffline.put(key, containerElement);

    }

    /**
     * Caches the given container page under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param containerPage the object to cache
     * @param online if to cache in online or offline project
     */
    public void setCacheContainerPages(String key, CmsXmlContainerPage containerPage, boolean online) {

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
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     * 
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    public void shutdown() {

        if (OpenCms.getMemoryMonitor() != null) {
            // prevent accidental calls
            return;

        }
        flushContainerPages(true);
        flushContainerPages(false);
        flushContainerElements();
        flushADERecentLists();
        flushADESearchOptions();
    }

    /**
     * Removes the container element identified by the given cache key from the cache.<p>
     * 
     * @param cacheKey the cache key to identify the container element to remove
     */
    public void uncacheContainerElement(String cacheKey) {

        m_containerElementsOffline.remove(cacheKey);

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
     * Removes a cached resource from the cache.<p>
     * 
     * The resource is removed both from the resource and sibling caches.
     * 
     * @param resource the resource
     */
    protected void uncacheResource(CmsResource resource) {

        if (resource == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }

        // remove the resource cached by it's structure ID
        uncacheContainerPage(resource.getStructureId(), false);
    }

    /**
     * Removes a bunch of cached resources from the offline cache, but keeps their properties
     * in the cache.<p>
     * 
     * @param resources a list of resources
     * 
     * @see #uncacheResource(CmsResource)
     */
    protected void uncacheResources(List<CmsResource> resources) {

        if (resources == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }

        for (int i = 0, n = resources.size(); i < n; i++) {
            // remove the resource
            uncacheResource(resources.get(i));
        }
    }

    /**
     * Returns the cache key for the given parameters.<p>
     * 
     * @param structureId the container page's structure id
     * @param keepEncoding if to keep the encoding while unmarshalling
     * 
     * @return the cached container page, or <code>null</code> if not found
     */
    public String getCacheKey(CmsUUID structureId, boolean keepEncoding) {

        return structureId.toString() + "_" + keepEncoding;
    }
}
