/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexCache.java,v $
 * Date   : $Date: 2005/04/22 14:38:35 $
 * Version: $Revision: 1.43 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.flex;

import org.opencms.cache.CmsLruCache;
import org.opencms.cache.I_CmsLruCacheObject;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

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
 * The whole cache is flushed if something is published.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @version $Revision: 1.43 $
 * 
 * @see org.opencms.flex.CmsFlexCacheKey
 * @see org.opencms.flex.CmsFlexCacheEntry
 * @see org.opencms.cache.CmsLruCache
 * @see org.opencms.cache.I_CmsLruCacheObject
 */
public class CmsFlexCache extends Object implements I_CmsEventListener {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexCache.class); 
    
    /**
     * A simple data container class for the FlexCache variations.<p>
     * 
     * @author Alexander Kandzior (a.kandzior@alkacon.com)
     */
    public class CmsFlexCacheVariation extends Object {

        /** The key belonging to the resource. */
        public CmsFlexCacheKey m_key;

        /** Maps variations to CmsFlexCacheEntries. */
        public Map m_map;

        /**
         * Generates a new instance of CmsFlexCacheVariation.<p>
         *
         * @param theKey The (resource) key to contruct this variation list for
         */
        public CmsFlexCacheVariation(CmsFlexCacheKey theKey) {

            m_key = theKey;
            m_map = Collections.synchronizedMap(new HashMap(CmsFlexCache.C_INITIAL_CAPACITY_VARIATIONS));
        }
    }

    /**
     * Extended LRUMap that handles the variations in case a key is removed.<p>
     * 
     * @author Alexander Kandzior (a.kandzior@alkacon.com)
     */
    class CmsFlexKeyMap extends LRUMap {

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
         * @see org.apache.commons.collections.map.LRUMap#removeLRU(org.apache.commons.collections.map.AbstractLinkedMap.LinkEntry)
         */
        protected boolean removeLRU(LinkEntry entry) {

            CmsFlexCacheVariation v = (CmsFlexCacheVariation)entry.getValue();
            if (v == null) {
                return true;
            }
            Map m = v.m_map;
            if ((m == null) || (m.size() == 0)) {
                return true;
            }
            Object[] entries = m.values().toArray();
            synchronized (m_variationCache) {
                for (int i = 0, s = entries.length; i < s; i++) {
                    CmsFlexCacheEntry e = (CmsFlexCacheEntry)entries[i];
                    m_variationCache.remove(e);
                }
                v.m_map.clear();
                v.m_map = null;
                v.m_key = null;
            }
            return true;
        }
    }

    /** Suffix to append to online cache entries. */
    public static final String C_CACHE_OFFLINESUFFIX = " [offline]";

    /** Suffix to append to online cache entries. */
    public static final String C_CACHE_ONLINESUFFIX = " [online]";

    /** Trigger for clearcache event: Clear complete cache. */
    public static final int C_CLEAR_ALL = 0;

    /** Trigger for clearcache event: Clear only entries. */
    public static final int C_CLEAR_ENTRIES = 1;

    /** Trigger for clearcache event: Clear complete offine cache. */
    public static final int C_CLEAR_OFFLINE_ALL = 4;

    /** Trigger for clearcache event: Clear only offline entries. */
    public static final int C_CLEAR_OFFLINE_ENTRIES = 5;

    /** Trigger for clearcache event: Clear complete online cache. */
    public static final int C_CLEAR_ONLINE_ALL = 2;

    /** Trigger for clearcache event: Clear only online entries. */
    public static final int C_CLEAR_ONLINE_ENTRIES = 3;

    /** Initial cache size, this should be a power of 2 because of the Java collections implementation. */
    public static final int C_INITIAL_CAPACITY_CACHE = 512;

    /** Initial size for variation lists, should be a power of 2. */
    public static final int C_INITIAL_CAPACITY_VARIATIONS = 8;

    /** The LRU cache to organize the cached entries. */
    protected CmsLruCache m_variationCache;

    /** Indicates if offline resources should be cached or not. */
    private boolean m_cacheOffline;

    /** Indicates if the cache is enabled or not. */
    private boolean m_enabled;

    /** Hashmap to store the entries for fast lookup. */
    private Map m_keyCache;

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
        
        int maxCacheBytes = configuration.getMaxCacheBytes();
        int avgCacheBytes = configuration.getAvgCacheBytes();
        int maxEntryBytes = configuration.getMaxEntryBytes();
        int maxKeys = configuration.getMaxKeys();
        

        this.m_variationCache = new CmsLruCache(maxCacheBytes, avgCacheBytes, maxEntryBytes);

        if (OpenCms.getMemoryMonitor().enabled()) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_entryLruCache", m_variationCache);
        }

        if (m_enabled) {
            CmsFlexKeyMap hashMap = new CmsFlexKeyMap(maxKeys);
            this.m_keyCache = Collections.synchronizedMap(hashMap);

            if (OpenCms.getMemoryMonitor().enabled()) {
                OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_resourceMap", hashMap);
            }

            OpenCms.addCmsEventListener(this, new int[] {
                I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY,
                I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR});
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.INIT_FLEXCACHE_CREATED_2, new Boolean(m_enabled), new Boolean(m_cacheOffline)));
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
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_RECEIVED_EVENT_CLEAR_CACHE_0));
                }
                clear();
                break;
            case I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY:
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_RECEIVED_EVENT_PURGE_REPOSITORY_0));
                }
                purgeJspRepository();
                break;
            case I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR:
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_RECEIVED_EVENT_CLEAR_CACHE_PARTIALLY_0));
                }
                Map m = event.getData();
                if (m == null) {
                    break;
                }
                Integer it = null;
                try {
                    it = (Integer)m.get("action");
                } catch (Exception e) {
                    // it will be null
                }
                if (it == null) {
                    break;
                }
                int i = it.intValue();
                switch (i) {
                    case C_CLEAR_ALL:
                        clear();
                        break;
                    case C_CLEAR_ENTRIES:
                        clearEntries();
                        break;
                    case C_CLEAR_ONLINE_ALL:
                        clearOnline();
                        break;
                    case C_CLEAR_ONLINE_ENTRIES:
                        clearOnlineEntries();
                        break;
                    case C_CLEAR_OFFLINE_ALL:
                        clearOffline();
                        break;
                    case C_CLEAR_OFFLINE_ENTRIES:
                        clearOfflineEntries();
                        break;
                    default:
                // no operation
                }
            default:
        // no operation
        }
    }

    /**
     * Returns the CmsFlexCacheKey data structure for a given
     * key (i.e. resource name).<p>
     * 
     * Usefull if you want to show the cache key for a resources,
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

        if (!isEnabled() || !cms.isAdmin()) {
            return null;
        }
        Object o = m_keyCache.get(key);
        if (o != null) {
            return ((CmsFlexCacheVariation)o).m_key;
        }
        return null;
    }

    /**
     * Returns a set of all cached resource names.
     * Usefull if you want to show a list of all cached resources,
     * like on the FlexCache administration page.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param cms the CmsObject used for user authorization
     * @return a Set of cached resource names (which are of type String)
     */
    public Set getCachedResources(CmsObject cms) {

        if (!isEnabled() || !cms.isAdmin()) {
            return null;
        }
        return m_keyCache.keySet();
    }

    /**
     * Returns all variations in the cache for a given resource name.
     * The variations are of type String.<p>
     * 
     * Usefull if you want to show a list of all cached entry - variations,
     * like on the FlexCache administration page.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param key the resource name for which to look up the variations for
     * @param cms the CmsObject used for user authorization
     * @return a Set of cached variations (which are of type String)
     */
    public Set getCachedVariations(String key, CmsObject cms) {

        if (!isEnabled() || !cms.isAdmin()) {
            return null;
        }
        Object o = m_keyCache.get(key);
        if (o != null) {
            return ((CmsFlexCacheVariation)o).m_map.keySet();
        }
        return null;
    }

    /**
     * Returns the LRU cache where the CacheEntries are cached.<p>
     *
     * @return the LRU cache where the CacheEntries are cached
     */
    public CmsLruCache getEntryLruCache() {

        return this.m_variationCache;
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

        return this.m_variationCache.size();
    }

    /**
     * Clears the cache for finalization.<p>
     * @throws Throwable if something goes wrong
     */
    protected void finalize() throws Throwable {

        try {
            this.clear();
            this.m_variationCache = null;
            this.m_keyCache = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
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
                this.m_variationCache.remove(entry);
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
                LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_FOUND_1, resource));
            }
            return ((CmsFlexCacheVariation)o).m_key;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_NOT_FOUND_1, resource));
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
     * @return true if the value was added to the cache, false otherwise
     */
    boolean put(CmsFlexCacheKey key, CmsFlexCacheEntry entry, String variation) {

        if (!isEnabled()) {
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_ADD_ENTRY_1, key.getResource()));
        }        
        if (variation != null) {
            // This is a cachable result
            key.setVariation(variation);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_ADD_ENTRY_WITH_VARIATION_2, key.getResource(), key.getVariation()));
            }            
            put(key, entry);
            // Note that duplicates are NOT checked, it it assumed that this is done beforehand,
            // while checking if the entry is already in the cache or not.
            return true;
        } else {
            // Result is not cachable
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_RESOURCE_NOT_CACHEABLE_0));
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
                LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_ADD_KEY_1, key.getResource()));
            }            
        }
        // If != null the key is already in the cache, so we just do nothing
    }

    /**
     * Removes an entry from the cache.<p>
     *
     * @param key the key which describes the entry to remove from the cache
     */
    void remove(CmsFlexCacheKey key) {

        if (!isEnabled()) {
            return;
        }
        Object o = m_keyCache.get(key.getResource());
        if (o != null) {
            //Object old = ((HashMap)o).remove(key.Variation);
            Object old = ((HashMap)o).get(key.getVariation());
            if (old != null) {
                this.getEntryLruCache().remove((I_CmsLruCacheObject)old);
            }
        }
    }

    /**
     * Emptys the cache completely.<p>
     */
    private synchronized void clear() {

        if (!isEnabled()) {
            return;
        }
        m_keyCache.clear();
        m_size = 0;

        this.m_variationCache.clear();

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_CLEAR_0));
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

        Set keys = new HashSet(m_keyCache.keySet());
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            String s = (String)i.next();
            if (s.endsWith(suffix)) {
                CmsFlexCacheVariation v = (CmsFlexCacheVariation)m_keyCache.get(s);
                if (entriesOnly) {
                    // Clear only entry
                    m_size -= v.m_map.size();
                    Iterator allEntries = v.m_map.values().iterator();
                    while (allEntries.hasNext()) {
                        I_CmsLruCacheObject nextObject = (I_CmsLruCacheObject)allEntries.next();
                        allEntries.remove();
                        this.m_variationCache.remove(nextObject);
                    }
                    v.m_map = Collections.synchronizedMap(new HashMap(C_INITIAL_CAPACITY_VARIATIONS));
                } else {
                    // Clear key and entry
                    m_size -= v.m_map.size();
                    Iterator allEntries = v.m_map.values().iterator();
                    while (allEntries.hasNext()) {
                        I_CmsLruCacheObject nextObject = (I_CmsLruCacheObject)allEntries.next();
                        allEntries.remove();
                        this.m_variationCache.remove(nextObject);
                    }

                    v.m_map = null;
                    v.m_key = null;
                    m_keyCache.remove(s);
                }
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_CLEAR_HALF_2, suffix, new Boolean(entriesOnly)));
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
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_CLEAR_ALL_0));
        }        
        // create new set to avoid ConcurrentModificationExceptions
        Set cacheKeys = new HashSet(m_keyCache.keySet());
        Iterator i = cacheKeys.iterator();
        while (i.hasNext()) {
            CmsFlexCacheVariation v = (CmsFlexCacheVariation)m_keyCache.get(i.next());
            Iterator allEntries = v.m_map.values().iterator();
            while (allEntries.hasNext()) {
                I_CmsLruCacheObject nextObject = (I_CmsLruCacheObject)allEntries.next();
                allEntries.remove();
                this.m_variationCache.remove(nextObject);
            }
            v.m_map = Collections.synchronizedMap(new HashMap(C_INITIAL_CAPACITY_VARIATIONS));
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
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_CLEAR_KEYS_AND_ENTRIES_0));
        }        
        clearAccordingToSuffix(C_CACHE_OFFLINESUFFIX, false);
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
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_CLEAR_OFFLINE_ENTRIES_0));
        }        
        clearAccordingToSuffix(C_CACHE_OFFLINESUFFIX, true);
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
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_CLEAR_ONLINE_KEYS_AND_ENTRIES_0));
        }        
        clearAccordingToSuffix(C_CACHE_ONLINESUFFIX, false);
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
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_CLEAR_ONLINE_ENTRIES_0));
        }        
        clearAccordingToSuffix(C_CACHE_ONLINESUFFIX, true);
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

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_WILL_PURGE_JSP_REPOSITORY_0));
        }

        File d;
        d = new File(org.opencms.loader.CmsJspLoader.getJspRepository() + "online" + File.separator);
        CmsFileUtil.purgeDirectory(d);

        d = new File(org.opencms.loader.CmsJspLoader.getJspRepository() + "offline" + File.separator);
        CmsFileUtil.purgeDirectory(d);

        clear();
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_FLEXCACHE_PURGED_JSP_REPOSITORY_0));
        }        
    }

    /**
     * Save a value to the cache.<p>
     *
     * @param key the key under shich the value is saved
     * @param theCacheEntry the entry to cache
     */
    private void put(CmsFlexCacheKey key, CmsFlexCacheEntry theCacheEntry) {

        Object o = m_keyCache.get(key.getResource());
        if (key.getTimeout() > 0) {
            theCacheEntry.setDateExpiresToNextTimeout(key.getTimeout());
        }
        if (o != null) {
            // We already have a variation map for this resource
            Map m = ((CmsFlexCacheVariation)o).m_map;
            boolean wasAdded = true;
            if (!m.containsKey(key.getVariation())) {
                wasAdded = this.m_variationCache.add(theCacheEntry);
            } else {
                wasAdded = this.m_variationCache.touch(theCacheEntry);
            }

            if (wasAdded) {
                theCacheEntry.setVariationData(key.getVariation(), m);
                m.put(key.getVariation(), theCacheEntry);
            }
        } else {
            // No variation map for this resource yet, so create one
            CmsFlexCacheVariation list = new CmsFlexCacheVariation(key);

            boolean wasAdded = this.m_variationCache.add(theCacheEntry);

            if (wasAdded) {
                theCacheEntry.setVariationData(key.getVariation(), list.m_map);
                list.m_map.put(key.getVariation(), theCacheEntry);
                m_keyCache.put(key.getResource(), list);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_ADDED_ENTRY_FOR_RESOURCE_WITH_VARIATION_3, new Integer(m_size), key.getResource(), key.getVariation()));
            LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHE_ADDED_ENTRY_1, theCacheEntry.toString()));
        }
    }
}

