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

package org.opencms.flex;

import org.opencms.cache.CmsLruCache;
import org.opencms.cache.I_CmsLruCacheObject;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexBucketConfiguration.BucketSet;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * This class implements the FlexCache.<p>
 *
 * The data structure used is a two-level hashtable.
 * This is optimized for the structure of the keys that are used to describe the
 * caching behaviour of the entries.
 * The first hash-level is calculated from the resource name, i.e. the
 * name of the resource as it is referred to in the VFS of OpenCms.
 * The second hash-level is calculated from the cache-key of the resource,
 * which also is a String representing the specifc variation of the cached entry.<p>
 *
 * A suffix [online] or [offline] is appended to te resource name
 * to distinguish between the online and offline projects of OpenCms.
 * Also, for support of JSP based workplace pages, a suffix [workplace]
 * is appended. The same cached workplace pages are used both in the online and
 * all offline projects.<p>
 *
 * Entries in the first level of the cache are of type CmsFlexCacheVariation,
 * which is a sub-class of CmsFlexCache.
 * This class is a simple data type that contains of a Map of CmsFlexCacheEntries,
 * with variations - Strings as keys.<p>
 *
 * Here's a short summary of used terms:
 * <ul>
 * <li><b>key:</b>
 * A combination of a resource name and a variation.
 * The data structure used is CmsFlexCacheKey.
 * <li><b>resource:</b>
 * A String with the resource name and an appended [online] of [offline] suffix.
 * <li><b>variation:</b>
 * A String describing a variation of a cached entry in the CmsFlexCache language.
 * <li><b>entry:</b>
 * A CmsFlexCacheEntry data structure which is describes a cached OpenCms resource.
 * For every entry a key is saved which contains the resource name and the variation.
 * </ul>
 *
 * Cache clearing is handled using events.
 * The cache is fully flushed if an event {@link I_CmsEventListener#EVENT_PUBLISH_PROJECT}
 * or {@link I_CmsEventListener#EVENT_CLEAR_CACHES} is caught.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.flex.CmsFlexCacheKey
 * @see org.opencms.flex.CmsFlexCacheEntry
 * @see org.opencms.cache.CmsLruCache
 * @see org.opencms.cache.I_CmsLruCacheObject
 */
public class CmsFlexCache extends Object implements I_CmsEventListener {

    /**
     * A simple data container class for the FlexCache variations.<p>
     */
    public static class CmsFlexCacheVariation extends Object {

        /** The key belonging to the resource. */
        public CmsFlexCacheKey m_key;

        /** Maps variations to CmsFlexCacheEntries. */
        public Map<String, I_CmsLruCacheObject> m_map;

        /**
         * Generates a new instance of CmsFlexCacheVariation.<p>
         *
         * @param theKey The (resource) key to contruct this variation list for
         */
        public CmsFlexCacheVariation(CmsFlexCacheKey theKey) {

            m_key = theKey;
            m_map = new Hashtable<String, I_CmsLruCacheObject>(INITIAL_CAPACITY_VARIATIONS);
        }
    }

    /**
     * Extended LRUMap that handles the variations in case a key is removed.<p>
     */
    class CmsFlexKeyMap extends LRUMap {

        /** Serial version UID required for safe serialization. */
        private static final long serialVersionUID = 6931995916013396902L;

        /**
         * Initialize the map with the given size.<p>
         *
         * @param maxSize the maximum number of key to cache
         */
        public CmsFlexKeyMap(int maxSize) {

            super(maxSize);
        }

        /**
         * Ensures that all variations that referenced by this key are released
         * if the key is released.<p>
         *
         * @param entry the entry to remove
         *
         * @return <code>true</code> to actually delete the entry
         *
         * @see LRUMap#removeLRU(LinkEntry)
         */
        @Override
        protected boolean removeLRU(LinkEntry entry) {

            CmsFlexCacheVariation v = (CmsFlexCacheVariation)entry.getValue();
            if (v == null) {
                return true;
            }
            Map<String, I_CmsLruCacheObject> m = v.m_map;
            if ((m == null) || (m.size() == 0)) {
                return true;
            }

            // make a copy to safely iterate over because the line "m_variationCache.remove(e)" modifies the variation map for the key
            Collection<I_CmsLruCacheObject> entries = new ArrayList<I_CmsLruCacheObject>(m.values());
            synchronized (m_variationCache) {
                for (I_CmsLruCacheObject e : entries) {
                    m_variationCache.remove(e);
                }
                v.m_map.clear();
                v.m_map = null;
                v.m_key = null;
            }
            return true;
        }
    }

    /**Constant for distinguish cache action.*/
    public static final String CACHE_ACTION = "action";

    /** Suffix to append to online cache entries. */
    public static final String CACHE_OFFLINESUFFIX = " [offline]";

    /** Suffix to append to online cache entries. */
    public static final String CACHE_ONLINESUFFIX = " [online]";

    /** Trigger for clearcache event: Clear complete cache. */
    public static final int CLEAR_ALL = 0;

    /** Trigger for clearcache event: Clear only entries. */
    public static final int CLEAR_ENTRIES = 1;

    /** Trigger for clearcache event: Clear complete offine cache. */
    public static final int CLEAR_OFFLINE_ALL = 4;

    /** Trigger for clearcache event: Clear only offline entries. */
    public static final int CLEAR_OFFLINE_ENTRIES = 5;

    /** Trigger for clearcache event: Clear complete online cache. */
    public static final int CLEAR_ONLINE_ALL = 2;

    /** Trigger for clearcache event: Clear only online entries. */
    public static final int CLEAR_ONLINE_ENTRIES = 3;

    /** The configuration for the Flex cache buckets. */
    public static final String CONFIG_PATH = "/system/config/flexconfig.properties";

    /** Initial cache size, this should be a power of 2 because of the Java collections implementation. */
    public static final int INITIAL_CAPACITY_CACHE = 512;

    /** Initial size for variation lists, should be a power of 2. */
    public static final int INITIAL_CAPACITY_VARIATIONS = 8;

    /** Offline repository constant. */
    public static final String REPOSITORY_OFFLINE = "offline";

    /** Online repository constant. */
    public static final String REPOSITORY_ONLINE = "online";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexCache.class);

    /** The LRU cache to organize the cached entries. */
    protected CmsLruCache m_variationCache;

    /** The Flex bucket configuration. */
    private CmsFlexBucketConfiguration m_bucketConfiguration;

    /** Indicates if offline resources should be cached or not. */
    private boolean m_cacheOffline;

    /** The CMS object used for VFS operations. */
    private CmsObject m_cmsObject;

    /** Indicates if the cache is enabled or not. */
    private boolean m_enabled;

    /** Map to store the entries for fast lookup. */
    private Map<String, CmsFlexCacheVariation> m_keyCache;

    /** Counter for the size. */
    private int m_size;

    /**
     * Constructor for class CmsFlexCache.<p>
     *
     * The parameter "enabled" is used to control if the cache is
     * actually on or off. Even if you don't need the cache, you still
     * have to create an instance of it with enabled=false.
     * This is because you need some of the FlexCache data structures
     * for JSP inclusion buffering.<p>
     *
     * @param configuration the flex cache configuration
     */
    public CmsFlexCache(CmsFlexCacheConfiguration configuration) {

        m_enabled = configuration.isCacheEnabled();
        m_cacheOffline = configuration.isCacheOffline();

        long maxCacheBytes = configuration.getMaxCacheBytes();
        long avgCacheBytes = configuration.getAvgCacheBytes();
        int maxEntryBytes = configuration.getMaxEntryBytes();
        int maxKeys = configuration.getMaxKeys();

        m_variationCache = new CmsLruCache(maxCacheBytes, avgCacheBytes, maxEntryBytes);
        OpenCms.getMemoryMonitor().register(getClass().getName() + ".m_entryLruCache", m_variationCache);

        if (m_enabled) {
            CmsFlexKeyMap flexKeyMap = new CmsFlexKeyMap(maxKeys);
            m_keyCache = Collections.synchronizedMap(
                CmsCollectionsGenericWrapper.<String, CmsFlexCacheVariation> map(flexKeyMap));
            OpenCms.getMemoryMonitor().register(getClass().getName() + ".m_resourceMap", flexKeyMap);

            OpenCms.addCmsEventListener(
                this,
                new int[] {
                    I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                    I_CmsEventListener.EVENT_CLEAR_CACHES,
                    I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY,
                    I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR});
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.INIT_FLEXCACHE_CREATED_2,
                    Boolean.valueOf(m_enabled),
                    Boolean.valueOf(m_cacheOffline)));
        }
    }

    /**
     * Copies the key set of a map while synchronizing on the map.<p>
     *
     * @param map the map whose key set should be copied
     * @return the copied key set
     */
    private static <K, V> Set<K> synchronizedCopyKeys(Map<K, V> map) {

        if (map == null) {
            return new HashSet<K>();
        }
        synchronized (map) {
            return new HashSet<K>(map.keySet());
        }
    }

    /**
     * Copies a map while synchronizing on it.<p>
     *
     * @param map the map to copy
     * @return the copied map
     */
    private static <K, V> Map<K, V> synchronizedCopyMap(Map<K, V> map) {

        if (map == null) {
            return new HashMap<K, V>();
        }

        synchronized (map) {

            return new HashMap<K, V>(map);
        }
    }

    /**
     * Indicates if offline project resources are cached.<p>
     *
     * @return true if offline projects are cached, false if not
     */
    public boolean cacheOffline() {

        return m_cacheOffline;
    }

    /**
     * Implements the CmsEvent interface,
     * the FlexCache uses the events to clear itself in case a project is published.<p>
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(org.opencms.main.CmsEvent event) {

        if (!isEnabled()) {
            return;
        }

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("FlexCache: Received event PUBLISH_PROJECT");
                }
                String publishIdStr = (String)(event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                if (!CmsUUID.isValidUUID(publishIdStr)) {
                    clear();
                } else {
                    try {
                        CmsUUID publishId = new CmsUUID(publishIdStr);
                        List<CmsPublishedResource> publishedResources = m_cmsObject.readPublishedResources(publishId);
                        boolean updateConfiguration = false;
                        for (CmsPublishedResource res : publishedResources) {
                            if (res.getRootPath().equals(CONFIG_PATH)) {
                                updateConfiguration = true;
                                break;
                            }
                        }
                        CmsFlexBucketConfiguration bucketConfig = m_bucketConfiguration;
                        if (updateConfiguration) {
                            LOG.info("Flex bucket configuration was updated, re-initializing configuration...");
                            try {
                                m_bucketConfiguration = CmsFlexBucketConfiguration.loadFromVfsFile(
                                    m_cmsObject,
                                    CONFIG_PATH);
                            } catch (CmsException e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                            // Make sure no entries built for the old configuration remain in the cache
                            clear();
                        } else if (bucketConfig != null) {
                            boolean bucketClearOk = clearBucketsForPublishList(
                                bucketConfig,
                                publishId,
                                publishedResources);
                            if (!bucketClearOk) {
                                clear();
                            }
                        } else {
                            clear();
                        }

                    } catch (CmsException e1) {
                        LOG.error(e1.getLocalizedMessage(), e1);
                        clear();
                    }
                }
                break;
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_RECEIVED_EVENT_CLEAR_CACHE_0));
                }
                clear();
                break;
            case I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY:
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_RECEIVED_EVENT_PURGE_REPOSITORY_0));
                }
                purgeJspRepository();
                break;
            case I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR:
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_RECEIVED_EVENT_CLEAR_CACHE_PARTIALLY_0));
                }
                Map<String, ?> m = event.getData();
                if (m == null) {
                    break;
                }
                Integer it = null;
                try {
                    it = (Integer)m.get(CACHE_ACTION);
                } catch (Exception e) {
                    // it will be null
                }
                if (it == null) {
                    LOG.error("Flex cache clear event with no action parameter received");
                    break;
                }
                int i = it.intValue();
                switch (i) {
                    case CLEAR_ALL:
                        clear();
                        break;
                    case CLEAR_ENTRIES:
                        clearEntries();
                        break;
                    case CLEAR_ONLINE_ALL:
                        clearOnline();
                        break;
                    case CLEAR_ONLINE_ENTRIES:
                        clearOnlineEntries();
                        break;
                    case CLEAR_OFFLINE_ALL:
                        clearOffline();
                        break;
                    case CLEAR_OFFLINE_ENTRIES:
                        clearOfflineEntries();
                        break;
                    default:
                        // no operation
                }
                break;
            default:
                // no operation
        }
    }

    /**
     * Dumps keys and variations to a string buffer, for debug purposes.<p>
     *
     * @param buffer the buffer to which the key information should be written
     */
    public void dumpKeys(StringBuffer buffer) {

        synchronized (this) {
            for (Map.Entry<String, CmsFlexCacheVariation> entry : synchronizedCopyMap(m_keyCache).entrySet()) {
                String key = entry.getKey();
                CmsFlexCacheVariation variations = entry.getValue();
                Map<String, I_CmsLruCacheObject> variationMap = variations.m_map;
                for (Map.Entry<String, I_CmsLruCacheObject> varEntry : variationMap.entrySet()) {
                    String varKey = varEntry.getKey();
                    I_CmsLruCacheObject value = varEntry.getValue();
                    buffer.append(key + " VAR " + varKey + "\n");
                    if (value instanceof CmsFlexCacheEntry) {
                        CmsFlexCacheEntry singleCacheEntry = (CmsFlexCacheEntry)value;
                        BucketSet buckets = singleCacheEntry.getBucketSet();
                        if (buckets != null) {
                            buffer.append("buckets = " + buckets.toString() + "\n");
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the CmsFlexCacheKey data structure for a given
     * key (i.e. resource name).<p>
     *
     * Useful if you want to show the cache key for a resources,
     * like on the FlexCache administration page.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param key the resource name for which to look up the variation for
     * @param cms the CmsObject used for user authorization
     * @return the CmsFlexCacheKey data structure found for the resource
     */
    public CmsFlexCacheKey getCachedKey(String key, CmsObject cms) {

        if (!isEnabled() || !OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER)) {
            return null;
        }
        Object o = m_keyCache.get(key);
        if (o != null) {
            return ((CmsFlexCacheVariation)o).m_key;
        }
        return null;
    }

    /**
     * Returns a set of all cached resource names.<p>
     *
     * Useful if you want to show a list of all cached resources,
     * like on the FlexCache administration page.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param cms the CmsObject used for user authorization
     * @return a Set of cached resource names (which are of type String)
     */
    public Set<String> getCachedResources(CmsObject cms) {

        if (!isEnabled() || !OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER)) {
            return null;
        }
        return synchronizedCopyKeys(m_keyCache);
    }

    /**
     * Returns all variations in the cache for a given resource name.
     * The variations are of type String.<p>
     *
     * Useful if you want to show a list of all cached entry - variations,
     * like on the FlexCache administration page.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param key the resource name for which to look up the variations for
     * @param cms the CmsObject used for user authorization
     * @return a Set of cached variations (which are of type String)
     */
    public Set<String> getCachedVariations(String key, CmsObject cms) {

        if (!isEnabled() || !OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER)) {
            return null;
        }
        Object o = m_keyCache.get(key);
        if (o != null) {
            return synchronizedCopyKeys(((CmsFlexCacheVariation)o).m_map);
        }
        return null;
    }

    /**
     * Returns the LRU cache where the CacheEntries are cached.<p>
     *
     * @return the LRU cache where the CacheEntries are cached
     */
    public CmsLruCache getEntryLruCache() {

        return m_variationCache;
    }

    /**
     * Initializes the flex cache.<p>
     *
     * @param adminCms a CMS context with admin privileges
     */
    public void initializeCms(CmsObject adminCms) {

        try {
            m_cmsObject = adminCms;
            try {
                String path = CONFIG_PATH;
                if (m_cmsObject.existsResource(path)) {
                    LOG.info("Flex configuration found at " + CONFIG_PATH + ", initializing...");
                    m_bucketConfiguration = CmsFlexBucketConfiguration.loadFromVfsFile(m_cmsObject, path);
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Indicates if the cache is enabled (i.e. actually
     * caching entries) or not.<p>
     *
     * @return true if the cache is enabled, false if not
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Returns the total number of cached resource keys.
     *
     * @return the number of resource keys in the cache
     */
    public int keySize() {

        if (!isEnabled()) {
            return 0;
        }
        return m_keyCache.size();
    }

    /**
     * Returns the total number of entries in the cache.<p>
     *
     * @return the number of entries in the cache
     */
    public int size() {

        return m_variationCache.size();
    }

    /**
     * Looks up a specific entry in the cache.<p>
     *
     * In case a found entry has a timeout set, it will be checked upon lookup.
     * In case the timeout of the entry has been reached, it will be removed from
     * the cache (and null will be returned in this case).<p>
     *
     * @param key The key to look for in the cache
     * @return the entry found for the key, or null if key is not in the cache
     */
    CmsFlexCacheEntry get(CmsFlexRequestKey key) {

        if (!isEnabled()) {
            // cache is disabled
            return null;
        }
        Object o = m_keyCache.get(key.getResource());
        if (o != null) {
            // found a matching key in the cache
            CmsFlexCacheVariation v = (CmsFlexCacheVariation)o;
            String variation = v.m_key.matchRequestKey(key);

            if (CmsStringUtil.isEmpty(variation)) {
                // requested resource is not cacheable
                return null;
            }
            CmsFlexCacheEntry entry = (CmsFlexCacheEntry)v.m_map.get(variation);
            if (entry == null) {
                // no cache entry available for variation
                return null;
            }
            if (entry.getDateExpires() < System.currentTimeMillis()) {
                // cache entry avaiable but expired, remove entry
                m_variationCache.remove(entry);
                return null;
            }
            // return the found cache entry
            return entry;
        } else {
            return null;
        }
    }

    /**
     * Returns the CmsFlexCacheKey data structure for a given resource name.<p>
     *
     * @param resource the resource name for which to look up the key for
     * @return the CmsFlexCacheKey data structure found for the resource
     */
    CmsFlexCacheKey getKey(String resource) {

        if (!isEnabled()) {
            return null;
        }
        Object o = m_keyCache.get(resource);
        if (o != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_FOUND_1, resource));
            }
            return ((CmsFlexCacheVariation)o).m_key;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_NOT_FOUND_1, resource));
            }
            return null;
        }
    }

    /**
     * Checks if the cache is empty or if at last one element is contained.<p>
     *
     * @return true if the cache is empty, false otherwise
     */
    boolean isEmpty() {

        if (!isEnabled()) {
            return true;
        }
        return m_keyCache.isEmpty();
    }

    /**
     * This method adds new entries to the cache.<p>
     *
     * The key describes the conditions under which the value can be cached.
     * Usually the key belongs to the response.
     * The variation describes the conditions under which the
     * entry was created. This is usually calculated from the request.
     * If the variation is != null, the entry is cachable.<p>
     *
     * @param key the key for the new value entry
     * @param entry the CmsFlexCacheEntry to store in the cache
     * @param variation the pre-calculated variation for the entry
     * @param requestKey the request key from which the variation was determined
     * @return true if the value was added to the cache, false otherwise
     */
    boolean put(CmsFlexCacheKey key, CmsFlexCacheEntry entry, String variation, CmsFlexRequestKey requestKey) {

        if (!isEnabled()) {
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_ADD_ENTRY_1, key.getResource()));
        }
        if (variation != null) {
            // This is a cachable result
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_FLEXCACHE_ADD_ENTRY_WITH_VARIATION_2,
                        key.getResource(),
                        variation));
            }
            put(key, entry, variation);
            if (m_bucketConfiguration != null) {
                try {
                    List<String> paths = key.getPathsForBuckets(requestKey);
                    if (paths.size() > 0) {
                        BucketSet buckets = m_bucketConfiguration.getBucketSet(paths);
                        entry.setBucketSet(buckets);
                    } else {
                        entry.setBucketSet(null); // bucket set of null means entries will be deleted for every publish job
                    }
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            // Note that duplicates are NOT checked, it it assumed that this is done beforehand,
            // while checking if the entry is already in the cache or not.
            return true;
        } else {
            // Result is not cachable
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_RESOURCE_NOT_CACHEABLE_0));
            }
            return false;
        }
    }

    /**
     * Adds a key with a new, empty variation map to the cache.<p>
     *
     * @param key the key to add to the cache.
     */
    void putKey(CmsFlexCacheKey key) {

        if (!isEnabled()) {
            return;
        }
        Object o = m_keyCache.get(key.getResource());
        if (o == null) {
            // No variation map for this resource yet, so create one
            CmsFlexCacheVariation variationMap = new CmsFlexCacheVariation(key);
            m_keyCache.put(key.getResource(), variationMap);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_ADD_KEY_1, key.getResource()));
            }
        }
        // If != null the key is already in the cache, so we just do nothing
    }

    /**
     * Empties the cache completely.<p>
     */
    private synchronized void clear() {

        if (!isEnabled()) {
            return;
        }
        m_keyCache.clear();
        m_size = 0;

        m_variationCache.clear();

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_CLEAR_0));
        }
    }

    /**
     * Internal method to perform cache clearance.<p>
     *
     * It clears "one half" of the cache, i.e. either
     * the online or the offline parts.
     * A parameter is used to indicate if only
     * the entries or keys and entries are to be cleared.<p>
     *
     * @param suffix used to distinguish between "[Online]" and "[Offline]" entries
     * @param entriesOnly if <code>true</code>, only entries will be cleared, otherwise
     *         the entries and the keys will be cleared
     */
    private synchronized void clearAccordingToSuffix(String suffix, boolean entriesOnly) {

        Set<String> keys = synchronizedCopyKeys(m_keyCache);
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String s = i.next();
            if (s.endsWith(suffix)) {
                CmsFlexCacheVariation v = m_keyCache.get(s);
                if (entriesOnly) {
                    // Clear only entry
                    m_size -= v.m_map.size();
                    Iterator<I_CmsLruCacheObject> allEntries = v.m_map.values().iterator();
                    while (allEntries.hasNext()) {
                        I_CmsLruCacheObject nextObject = allEntries.next();
                        allEntries.remove();
                        m_variationCache.remove(nextObject);
                    }
                    v.m_map = new Hashtable<String, I_CmsLruCacheObject>(INITIAL_CAPACITY_VARIATIONS);
                } else {
                    // Clear key and entry
                    m_size -= v.m_map.size();
                    Iterator<I_CmsLruCacheObject> allEntries = v.m_map.values().iterator();
                    while (allEntries.hasNext()) {
                        I_CmsLruCacheObject nextObject = allEntries.next();
                        allEntries.remove();
                        m_variationCache.remove(nextObject);
                    }

                    v.m_map = null;
                    v.m_key = null;
                    m_keyCache.remove(s);
                }
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_FLEXCACHE_CLEAR_HALF_2,
                    suffix,
                    Boolean.valueOf(entriesOnly)));
        }
    }

    /**
     * Clears the Flex cache buckets matching the given publish list.<p>
     *
     * @param bucketConfig the bucket configuration to be used for checking which flex cache entry should be purged
     * @param publishId the publish id
     * @param publishedResources the published resources
     *
     * @return true if the flex buckets could be cleared successfully (if this returns false, the flex cache should fall back to the old behavior, i.e. clearing everything)
     */
    private boolean clearBucketsForPublishList(
        CmsFlexBucketConfiguration bucketConfig,
        CmsUUID publishId,
        List<CmsPublishedResource> publishedResources) {

        long startTime = System.currentTimeMillis();
        String p = "[" + publishId + "] "; // Prefix for log messages
        try {

            LOG.debug(p + "Trying bucket-based flex entry cleanup");
            if (bucketConfig.shouldClearAll(publishedResources)) {
                LOG.info(p + "Clearing Flex cache completely based on Flex bucket configuration.");
                return false;
            } else {
                long totalEntries = 0;
                long removedEntries = 0;
                List<String> paths = Lists.newArrayList();
                for (CmsPublishedResource pubRes : publishedResources) {
                    paths.add(pubRes.getRootPath());
                    LOG.info(p + "Published resource: " + pubRes.getRootPath());
                }
                BucketSet publishListBucketSet = bucketConfig.getBucketSet(paths);
                if (LOG.isInfoEnabled()) {
                    LOG.info(p + "Flex cache buckets for publish list: " + publishListBucketSet.toString());
                }
                synchronized (this) {
                    List<CmsFlexCacheEntry> entriesToDelete = Lists.newArrayList();
                    for (Map.Entry<String, CmsFlexCacheVariation> entry : synchronizedCopyMap(m_keyCache).entrySet()) {
                        CmsFlexCacheVariation variation = entry.getValue();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(p + "Processing entries for " + entry.getKey());
                        }
                        entriesToDelete.clear();

                        for (Map.Entry<String, I_CmsLruCacheObject> variationEntry : synchronizedCopyMap(
                            variation.m_map).entrySet()) {
                            CmsFlexCacheEntry flexEntry = (CmsFlexCacheEntry)(variationEntry.getValue());
                            totalEntries += 1;
                            BucketSet entryBucketSet = flexEntry.getBucketSet();
                            if (publishListBucketSet.matchForDeletion(entryBucketSet)) {
                                entriesToDelete.add(flexEntry);
                                if (LOG.isInfoEnabled()) {
                                    LOG.info(p + "Match: " + variationEntry.getKey());
                                }
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(p + "No match: " + variationEntry.getKey());
                                }
                            }
                        }
                        for (CmsFlexCacheEntry entryToDelete : entriesToDelete) {
                            m_variationCache.remove(entryToDelete);
                            removedEntries += 1;
                        }
                    }
                    long endTime = System.currentTimeMillis();
                    LOG.info(
                        p
                            + "Removed "
                            + removedEntries
                            + " of "
                            + totalEntries
                            + " Flex cache entries, took "
                            + (endTime - startTime)
                            + " milliseconds");
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error(p + "Exception while trying to selectively purge flex cache: " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Clears all entries in the cache, online or offline.<p>
     *
     * The keys are not cleared.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     */
    private synchronized void clearEntries() {

        if (!isEnabled()) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_CLEAR_ALL_0));
        }
        // create new set to avoid ConcurrentModificationExceptions
        Set<String> cacheKeys = synchronizedCopyKeys(m_keyCache);
        Iterator<String> i = cacheKeys.iterator();
        while (i.hasNext()) {
            CmsFlexCacheVariation v = m_keyCache.get(i.next());
            Iterator<I_CmsLruCacheObject> allEntries = v.m_map.values().iterator();
            while (allEntries.hasNext()) {
                I_CmsLruCacheObject nextObject = allEntries.next();
                allEntries.remove();
                m_variationCache.remove(nextObject);
            }
            v.m_map = new Hashtable<String, I_CmsLruCacheObject>(INITIAL_CAPACITY_VARIATIONS);
        }
        m_size = 0;
    }

    /**
     * Clears all entries and all keys from offline projects in the cache.<p>
     *
     * Cached resources from the online project are not touched.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     */
    private void clearOffline() {

        if (!isEnabled()) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_CLEAR_KEYS_AND_ENTRIES_0));
        }
        clearAccordingToSuffix(CACHE_OFFLINESUFFIX, false);
    }

    /**
     * Clears all entries from offline projects in the cache.<p>
     *
     * The keys from the offline projects are not cleared.
     * Cached resources from the online project are not touched.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     */
    private void clearOfflineEntries() {

        if (!isEnabled()) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_CLEAR_OFFLINE_ENTRIES_0));
        }
        clearAccordingToSuffix(CACHE_OFFLINESUFFIX, true);
    }

    /**
     * Clears all entries and all keys from the online project in the cache.<p>
     *
     * Cached resources from the offline projects are not touched.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     */
    private void clearOnline() {

        if (!isEnabled()) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_CLEAR_ONLINE_KEYS_AND_ENTRIES_0));
        }
        clearAccordingToSuffix(CACHE_ONLINESUFFIX, false);
    }

    /**
     * Clears all entries from the online project in the cache.<p>
     *
     * The keys from the online project are not cleared.
     * Cached resources from the offline projects are not touched.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     */
    private void clearOnlineEntries() {

        if (!isEnabled()) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_CLEAR_ONLINE_ENTRIES_0));
        }
        clearAccordingToSuffix(CACHE_ONLINESUFFIX, true);
    }

    /**
     * This method purges the JSP repository dirs,
     * i.e. it deletes all JSP files that OpenCms has written to the
     * real FS.<p>
     *
     * Obviously this method must be used with caution.
     * Purpose of this method is to allow
     * a complete purge of all JSP pages on a machine after
     * a major update of JSP templates was made.<p>
     */
    private synchronized void purgeJspRepository() {

        CmsJspLoader cmsJspLoader = (CmsJspLoader)OpenCms.getResourceManager().getLoader(
            CmsJspLoader.RESOURCE_LOADER_ID);

        cmsJspLoader.triggerPurge(new Runnable() {

            @SuppressWarnings("synthetic-access")
            public void run() {

                clear();
            }
        });
    }

    /**
     * Save a value to the cache.<p>
     *
     * @param key the key under which the value is saved
     * @param theCacheEntry the entry to cache
     * @param variation the variation string
     */
    private void put(CmsFlexCacheKey key, CmsFlexCacheEntry theCacheEntry, String variation) {

        CmsFlexCacheVariation o = m_keyCache.get(key.getResource());
        if (o != null) {
            // We already have a variation map for this resource
            Map<String, I_CmsLruCacheObject> m = o.m_map;
            boolean wasAdded = true;
            if (!m.containsKey(variation)) {
                wasAdded = m_variationCache.add(theCacheEntry);
            } else {
                wasAdded = m_variationCache.touch(theCacheEntry);
            }

            if (wasAdded) {
                theCacheEntry.setVariationData(variation, m);
                m.put(variation, theCacheEntry);
            }
        } else {
            // No variation map for this resource yet, so create one
            CmsFlexCacheVariation list = new CmsFlexCacheVariation(key);

            boolean wasAdded = m_variationCache.add(theCacheEntry);

            if (wasAdded) {
                theCacheEntry.setVariationData(variation, list.m_map);
                list.m_map.put(variation, theCacheEntry);
                m_keyCache.put(key.getResource(), list);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_FLEXCACHE_ADDED_ENTRY_FOR_RESOURCE_WITH_VARIATION_3,
                    Integer.valueOf(m_size),
                    key.getResource(),
                    variation));
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHE_ADDED_ENTRY_1, theCacheEntry.toString()));
        }
    }
}