/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexCache.java,v $
 * Date   : $Date: 2003/09/15 13:30:42 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.cache.CmsLruHashMap;
import org.opencms.cache.I_CmsLruCacheObject;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.file.CmsObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import source.org.apache.java.util.Configurations;

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
 * @version $Revision: 1.2 $
 * 
 * @see com.opencms.flex.cache.CmsFlexCacheKey
 * @see com.opencms.flex.cache.CmsFlexCacheEntry
 * @see com.opencms.flex.util.CmsFlexLruCache
 * @see com.opencms.flex.util.I_CmsFlexLruCacheObject
 */
public class CmsFlexCache extends Object implements I_CmsEventListener {
    
    /** Initial Cache size, this should be a power of 2 because of the Java collections implementation */
    public static final int C_INITIAL_CAPACITY_CACHE = 512;
    // Some prime numbers: 127 257 509 1021 2039 4099 8191
    
    /** Initial size for variation lists, should be a power of 2 */
    public static final int C_INITIAL_CAPACITY_VARIATIONS = 8;
    // Some prime numbers: 2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71
    
    /** Suffix to append to online cache entries */
    public static String C_CACHE_ONLINESUFFIX = " [online]";
    
    /** Suffix to append to online cache entries */
    public static String C_CACHE_OFFLINESUFFIX = " [offline]";
    
    /** Suffix to append to workplace cache entries */
    public static String C_CACHE_WORKPLACESUFFIX = " [workplace]";
    
    /** Hashmap to store the Entries for fast lookup */
    private java.util.Map m_resourceMap;
    
    /** Counter for the size */
    private int m_size;
    
    /** Indicates if the cache is enabled or not */
    private boolean m_enabled;
    
    /** Indicates if offline resources should be cached or not */
    private boolean m_cacheOffline;
    
    /** Debug switch */
    private static final int DEBUG = 0;
    
    /** Static ints to trigger clearcache events */
    public static final int C_CLEAR_ALL = 0;
    public static final int C_CLEAR_ENTRIES = 1;
    public static final int C_CLEAR_ONLINE_ALL = 2;
    public static final int C_CLEAR_ONLINE_ENTRIES = 3;
    public static final int C_CLEAR_OFFLINE_ALL = 4;
    public static final int C_CLEAR_OFFLINE_ENTRIES = 5;
    public static final int C_CLEAR_WORKPLACE_ALL = 6;
        
    /** The LRU cache to organize the cached entries. */
    private CmsLruCache m_entryLruCache;
    
    /** The LRU cache to organize the cached resources. */
    private CmsLruCache m_variationCache;
    
    
    /**
     * Constructor for class CmsFlexCache.<p>
     *
     * The parameter "enabled" is used to control if the cache is
     * actually on or off. Even if you don't need the cache, you still
     * have to create an instance of it with enabled=false.
     * This is because you need some of the FlexCache data structures
     * for JSP inclusion buffering.<p>
     *
     * @param conf the OpenCms configuration
     */
    public CmsFlexCache(Configurations conf) {
        m_enabled = conf.getBoolean("flex.cache.enabled", true);
        m_cacheOffline = conf.getBoolean("flex.cache.offline", true);
        
        boolean forceGC = conf.getBoolean("flex.cache.forceGC", false);
        int maxCacheBytes = conf.getInteger("flex.cache.maxCacheBytes", 2000000);
        int avgCacheBytes = conf.getInteger("flex.cache.avgCacheBytes", 1500000);
        int maxEntryBytes = conf.getInteger("flex.cache.maxEntryBytes", 400000);  
        int maxVariations = conf.getInteger("flex.cache.maxEntries", 4000);
        int maxKeys = conf.getInteger("flex.cache.maxKeys", 4000);
     
        this.m_entryLruCache = new CmsLruCache(maxCacheBytes, avgCacheBytes, maxEntryBytes, forceGC);             
        this.m_variationCache = new CmsLruCache(maxVariations, (int)(maxVariations*0.75), -1, false);
        
        if (m_enabled) {
            this.m_resourceMap = java.util.Collections.synchronizedMap(new CmsLruHashMap(CmsFlexCache.C_INITIAL_CAPACITY_CACHE, maxKeys));     
            OpenCms.addCmsEventListener(this, new int[] {
                I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY,
                I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR       
            });
        }
        
        // make the flex cache available to other classes through the runtime properties
        OpenCms.setRuntimeProperty(I_CmsResourceLoader.C_LOADER_CACHENAME, this);
        
        if (DEBUG > 0) System.err.println("FlexCache: Initializing with parameters enabled=" + m_enabled + " cacheOffline=" + m_cacheOffline);
    }
    
    /**
     * Clears the cache for finalization.<p>
     * @throws Throwable if something goes wrong
     */
    protected void finalize() throws Throwable {
        this.clear();
        
        this.m_entryLruCache = null;
        this.m_variationCache = null;
        this.m_resourceMap = null;
        this.m_resourceMap = null;
        
        super.finalize();
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
     * Indicates if offline project resources are cached.<p>
     *
     * @return true if offline projects are cached, false if not
     */
    public boolean cacheOffline() {
        return m_cacheOffline;
    }
    
    /**
     * Clears all entries and all keys in the cache, online or offline.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param cms the CmsObject used for user authorization
     */
    private void clear(CmsObject cms) {
        if (! isEnabled()) return;
        if (! isAdmin(cms)) return;
        if (DEBUG > 0) System.err.println("FlexCache: Clearing complete cache");
        clear();
    }
    
    /**
     * Clears all entries in the cache, online or offline.<p>
     * 
     * The keys are not cleared.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param cms the CmsObject used for user authorization
     */
    private synchronized void clearEntries(CmsObject cms) {
        if (! isEnabled()) return;
        if (! isAdmin(cms)) return;
        if (DEBUG > 0) System.err.println("FlexCache: Clearing all entries");
        java.util.Iterator i = m_resourceMap.keySet().iterator();
        while (i.hasNext()) {
            CmsFlexCacheVariation v = (CmsFlexCacheVariation)m_resourceMap.get(i.next());
            java.util.Iterator allEntries = v.m_map.values().iterator();
            while (allEntries.hasNext()) {
                I_CmsLruCacheObject nextObject = (I_CmsLruCacheObject)allEntries.next();
                allEntries.remove();
                this.m_entryLruCache.remove(nextObject);
            }
            v.m_map = java.util.Collections.synchronizedMap(new HashMap(C_INITIAL_CAPACITY_VARIATIONS));
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
     *
     * @param cms the CmsObject used for user authorization
     */
    private void clearOffline(CmsObject cms) {
        if (! isEnabled()) return;
        if (! isAdmin(cms)) return;
        if (DEBUG > 0) System.err.println("FlexCache: Clearing offline keys & entries");
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
     *
     * @param cms the CmsObject used for user authorization
     */
    private void clearOfflineEntries(CmsObject cms) {
        if (! isEnabled()) return;
        if (! isAdmin(cms)) return;
        if (DEBUG > 0) System.err.println("FlexCache: Clearing offline entries");
        clearAccordingToSuffix(C_CACHE_OFFLINESUFFIX, true);
    }
    
    /**
     * Clears all entries and all keys from the online project in the cache.<p>
     * 
     * Cached resources from the offline projects are not touched.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param cms the CmsObject used for user authorization
     */
    private void clearOnline(CmsObject cms) {
        if (! isEnabled()) return;
        if (! isAdmin(cms)) return;
        if (DEBUG > 0) System.err.println("FlexCache: Clearing online keys & entries");
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
     *
     * @param cms the CmsObject used for user authorization
     */
    private void clearOnlineEntries(CmsObject cms) {
        if (! isEnabled()) return;
        if (! isAdmin(cms)) return;
        if (DEBUG > 0) System.err.println("FlexCache: Clearing online entries");
        clearAccordingToSuffix(C_CACHE_ONLINESUFFIX, true);
    }
    
    /**
     * Clears all entries and all keys from workplace in the cache.<p>
     * 
     * Cached resources outside the workplace are not touched.<p>
     *
     * Only users with administrator permissions are allowed
     * to perform this operation.<p>
     *
     * @param cms the CmsObject used for user authorization
     */    
    private void clearWorkplace(CmsObject cms) {
        if (!isEnabled()) return;
        if (!isAdmin(cms)) return;
        if (DEBUG > 0) System.err.println("FlexCache: Clearing online keys & entries");
        clearAccordingToSuffix(C_CACHE_WORKPLACESUFFIX, false);
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
     *
     * @param cms the CmsObject used for user authorization
     */
    private synchronized void purgeJspRepository(CmsObject cms) {
        if (!isAdmin(cms) && !cms.getRequestContext().isEventControlled()) return;
        if (DEBUG > 0) System.err.println("FlexCache.purgeJspRepository() purging JSP repositories!");

        File d;
        d = new java.io.File(org.opencms.loader.CmsJspLoader.getJspRepository() + "online" + java.io.File.separator);
        purgeDirectory(d);

        d = new java.io.File(org.opencms.loader.CmsJspLoader.getJspRepository() + "offline" + java.io.File.separator);
        purgeDirectory(d);
         
        clear();
        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) 
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "JSP repository purged - purgeJspRepository() called");
    }
    
    /**
     * Deletes a directory in the file system and all subfolders of the directory.<p>
     * 
     * @param d the directory to delete
     */
    private void purgeDirectory(File d) {
        if (d.canRead() && d.isDirectory()) {
            java.io.File[] files = d.listFiles();
            if (DEBUG > 0) {
                System.err.println("FlexCache.purgeDirectory() Deleting directory = " + d.getAbsolutePath());
                System.err.println("FlexCache.purgeDirectory() Files in directory = " + files.length);
            }                 
            for (int i = 0; i<files.length; i++) {
                File f = files[i];
                if (f.isDirectory()) {
                    purgeDirectory(f);
                }
                if (f.canWrite()) {
                    f.delete();
                } else if (DEBUG > 0) {
                    System.err.println("FlexCache.purgeDirectory() could not delete file = " + f);
                }
            }
        } else if (DEBUG > 0) {
            System.err.println("FlexCache.purgeDirectory() could not access directory: " + d);
            System.err.println("FlexCache.purgeDirectory() d.canWrite() = " + d.canWrite());
            System.err.println("FlexCache.purgeDirectory() d.canWrite() = " + d.canWrite());
            System.err.println("FlexCache.purgeDirectory() d.isDirectory() = " + d.isDirectory());
            
        }        
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
    public java.util.Set getCachedResources(CmsObject cms) {
        if (! isEnabled()) return null;
        if (! isAdmin(cms)) return null;
        return m_resourceMap.keySet();
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
    public java.util.Set getCachedVariations(String key, CmsObject cms) {
        if (! isEnabled()) return null;
        if (! isAdmin(cms)) return null;
        Object o = m_resourceMap.get(key);
        if (o != null) {
            CmsFlexCacheVariation v = (CmsFlexCacheVariation)o;
            this.m_variationCache.touch((I_CmsLruCacheObject)o);
            return v.m_map.keySet();
        }
        return null;
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
        if (! isEnabled()) return null;
        if (! isAdmin(cms)) return null;
        Object o = m_resourceMap.get(key);
        if (o != null) {
            CmsFlexCacheVariation v = (CmsFlexCacheVariation)o;
            this.m_variationCache.touch((I_CmsLruCacheObject)o);
            return v.m_key;
        }
        return null;
    }
    
    /**
     * Returns the total number of entries in the cache.<p>
     *
     * @return the number of entries in the cache
     */
    public int size() {
        return this.m_entryLruCache.size();
    }
    
    /**
     * Returns the total number of cached resource keys.
     *
     * @return the number of resource keys in the cache
     */
    public int keySize() {
        if (! isEnabled()) return 0;
        return m_resourceMap.size();
    }
    
    /**
     * This method checks if a given key
     * is already contained in the cache.<p>
     *
     * @return true if key is in the cache, false otherwise
     * @param key the key to look for
     */
    boolean containsKey(CmsFlexCacheKey key) {
        if (! isEnabled()) return false;
        return (get(key) != null);
    }
    
    /**
     * Implements the CmsEvent interface,
     * the FlexCache uses the events to clear itself in case a project is published.<p>
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(org.opencms.main.CmsEvent event) {
        if (! isEnabled()) return;
        
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                if (DEBUG > 0) System.err.println("FlexCache: Recieved event, clearing cache!");
                clear();
                break;
            case I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY:
                if (DEBUG > 0) System.err.println("FlexCache: Recieved event, purging JSP repository!");
                purgeJspRepository(event.getCmsObject());
                break;
            case I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR:
                if (DEBUG > 0) System.err.println("FlexCache: Recieved event, clearing part of cache!");
                java.util.Map m = event.getData();
                if (m == null) break;
                Integer it = null;
                try {
                    it = (Integer)m.get("action");
                } catch (Exception e) { }
                if (it == null) break;
                int i = it.intValue();
                switch (i) {
                    case C_CLEAR_ALL:
                        clear(event.getCmsObject());
                        break;
                    case C_CLEAR_ENTRIES:
                        clearEntries(event.getCmsObject());
                        break;
                    case C_CLEAR_ONLINE_ALL:
                        clearOnline(event.getCmsObject());
                        break;
                    case C_CLEAR_ONLINE_ENTRIES:
                        clearOnlineEntries(event.getCmsObject());
                        break;
                    case C_CLEAR_OFFLINE_ALL:
                        clearOffline(event.getCmsObject());
                        break;
                    case C_CLEAR_OFFLINE_ENTRIES:
                        clearOfflineEntries(event.getCmsObject());
                        break;
                    case C_CLEAR_WORKPLACE_ALL:
                        clearWorkplace(event.getCmsObject());
                        break;
                    default:
                        // no operation
                }
            default:
                // no operation
        }
    }
    
    /**
     * Looks up a specific entry in the cache.<p>
     * 
     * In case a found entry has a timeout set, it will be checked upon lookup.
     * In case the timeout of the entry has been reached, it will be removed from
     * the cache (and null will be returend in this case).<p>
     *
     * @param key The key to look for in the cache
     * @return the entry found for the key, or null if key is not in the cache
     */
    CmsFlexCacheEntry get(CmsFlexCacheKey key) {
        if (! isEnabled()) return null;
        if (DEBUG > 0) System.err.println("FlexCache: Trying to get entry for resource " + key.m_resource);
        Object o = m_resourceMap.get(key.m_resource);
        if (o != null) {
            CmsFlexCacheVariation v = (CmsFlexCacheVariation)o;
            String variation = v.m_key.matchRequestKey(key);
            
            this.m_variationCache.touch((I_CmsLruCacheObject)o);
            
            if (DEBUG > 0) {
                if (variation != null) {
                    CmsFlexCacheEntry e = (CmsFlexCacheEntry)v.m_map.get(variation);
                    if (e != null) {
                        System.err.println("FlexCache: Found entry for variation " + variation);
                    } else {
                        System.err.println("FlexCache: Did not find entry for variation " + variation);
                    }
                } else {
                    System.err.println("FlexCache: Found nothing because resource is not cachable for this request!");
                }
            }
            if (variation == null) return null;
            if (v.m_key.m_timeout < 0) {
                // No timeout for this resource is specified
                return (CmsFlexCacheEntry)v.m_map.get(variation);
            } else {
                // Check for possible timeout of entry
                CmsFlexCacheEntry e = (CmsFlexCacheEntry)v.m_map.get(variation);
                if (e == null) return null;
                if (DEBUG > 1) System.err.println("FlexCache: Checking timeout for resource " + key.m_resource);
                if (e.getTimeout() < key.m_timeout) {
                    if (DEBUG > 1) System.err.println("FlexCache: Resource has reached timeout, removing from cache!");
                    this.m_entryLruCache.remove(e);
                    return null;
                }
                if (DEBUG > 1) System.err.println("FlexCache: Resource timeout not reached!");
                return e;
            }
        } else if (DEBUG > 0) {
            System.err.println("FlexCache: Did not find any entry for resource");
            return null;
        } else return null;
    }
    
    /**
     * Returns the CmsFlexCacheKey data structure for a given resource name.<p>
     *
     * @param resource the resource name for which to look up the key for
     * @return the CmsFlexCacheKey data structure found for the resource
     */
    CmsFlexCacheKey getKey(String resource) {
        if (! isEnabled()) return null;
        Object o = m_resourceMap.get(resource);
        if (o != null) {
            if (DEBUG > 1) System.err.println("FlexCache: Found pre-calculated key for resource " + resource);
            this.m_variationCache.touch((I_CmsLruCacheObject)o);
            return ((CmsFlexCacheVariation)o).m_key;
        } else {
            if (DEBUG > 1) System.err.println("FlexCache: Did not find pre-calculated key for resource " + resource);
            return null;
        }
    }
    
    /**
     * Adds a key with a new, empty variation map to the cache.<p>
     *
     * @param key the key to add to the cache.
     */
    void putKey(CmsFlexCacheKey key) {
        if (! isEnabled()) return;
        Object o = m_resourceMap.get(key.m_resource);
        if (o == null) {
            // No variation map for this resource yet, so create one
            CmsFlexCacheVariation variationMap = new CmsFlexCacheVariation(key);
            m_resourceMap.put(key.m_resource, variationMap);
            this.m_variationCache.add(variationMap);
            if (DEBUG > 1) System.err.println("FlexCache: Added pre-calculated key for resource " + key.m_resource);
        }
        // If != null the key is already in the cache, so we just do nothing
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
     * @param key the key for the new value entry. Usually calculated from the response
     * @param entry the CmsFlexCacheEntry to store in the cache
     * @param variation the pre-calculated variation for the entry
     * @return true if the value was added to the cache, false otherwise
     */
    boolean put(CmsFlexCacheKey key, CmsFlexCacheEntry entry, String variation) {
        if (! isEnabled()) return false;
        if (DEBUG > 1) System.err.println("FlexCache: Trying to add entry for resource " + key.m_resource);
        if (variation != null) {
            // This is a cachable result
            key.m_variation = variation;
            if (DEBUG > 1) System.err.println("FlexCache: Adding entry for resource " + key.m_resource + " with variation:" + key.m_variation);
            put(key, entry);
            // Note that duplicates are NOT checked, it it assumed that this is done beforehand,
            // while checking if the entry is already in the cache or not.
            return true;
        } else {
            // Result is not cachable
            if (DEBUG > 1) System.err.println("FlexCache: Nothing added because resource is not cachable for this request!");
            return false;
        }
    }
    
    /**
     * Removes an entry from the cache.<p>
     *
     * @param key the key which describes the entry to remove from the cache
     */
    void remove(CmsFlexCacheKey key) {
        if (! isEnabled()) return;
        Object o = m_resourceMap.get(key.m_resource);
        if (o != null) {
            //Object old = ((HashMap)o).remove(key.Variation);
            Object old = ((HashMap)o).get(key.m_variation);
            if (old != null) {
                this.getEntryLruCache().remove((I_CmsLruCacheObject)old);
            }
        }
    }
    
    /**
     * Checks if the cache is empty or if at last one element is contained.<p>
     *
     * @return true if the cache is empty, false otherwise
     */
    boolean isEmpty() {
        if (! isEnabled()) return true;
        return m_resourceMap.isEmpty();
    }
    
    /**
     * Emptys the cache completely.<p>
     */
    private synchronized void clear() {
        if (! isEnabled()) return;
        
        m_resourceMap.clear();
        m_resourceMap = java.util.Collections.synchronizedMap(new CmsLruHashMap(C_INITIAL_CAPACITY_CACHE));
        
        m_size = 0;
        
        this.m_entryLruCache.clear();
        this.m_variationCache.clear();
        
        if (OpenCms.isLogging(I_CmsLogChannels.C_FLEX_CACHE)) 
            OpenCms.log(I_CmsLogChannels.C_FLEX_CACHE, "[FlexCache] Complete cache cleared - clear() called");
    }
    
    /**
     * Save a value to the cache.<p>
     *
     * @param key the key under shich the value is saved
     * @param theCacheEntry the entry to cache
     */
    private void put(CmsFlexCacheKey key, CmsFlexCacheEntry theCacheEntry) {
        Object o = m_resourceMap.get(key.m_resource);
        if (key.m_timeout > 0) theCacheEntry.setTimeout(key.m_timeout * 60000);
        if (o != null) {
            // We already have a variation map for this resource
            java.util.Map m = ((CmsFlexCacheVariation)o).m_map;
            boolean wasAdded = true;
            if (! m.containsKey(key.m_variation)) {
                wasAdded = this.m_entryLruCache.add(theCacheEntry);
            } else {
                wasAdded = this.m_entryLruCache.touch(theCacheEntry);
            }
            
            if (wasAdded) {
                theCacheEntry.setVariationData(key.m_variation, m);
                m.put(key.m_variation, theCacheEntry);
                this.m_variationCache.touch((I_CmsLruCacheObject)o);
            }
        } else {
            // No variation map for this resource yet, so create one
            CmsFlexCacheVariation list = new CmsFlexCacheVariation(key);

            boolean wasAdded = this.m_entryLruCache.add(theCacheEntry);
            
            if (wasAdded) {
                theCacheEntry.setVariationData(key.m_variation, list.m_map);
                list.m_map.put(key.m_variation, theCacheEntry);
                m_resourceMap.put(key.m_resource, list);
                this.m_variationCache.add(list);
            }
        }
        
        if (DEBUG > 0) System.err.println("FlexCache: Entry "  + m_size + " added for resource " + key.m_resource + " with variation " + key.m_variation);
        if (DEBUG > 2) System.err.println("FlexCache: Entry added was:\n" + theCacheEntry.toString());
    }
    
    /**
     * Internal method to determine if a user has Administration permissions.<p>
     * 
     * @param cms the context to check 
     * @return <code>true</code> if the current user has Administration permissions
     */
    private boolean isAdmin(CmsObject cms) {
        boolean result;
        try {
            result = cms.getRequestContext().isAdmin();
        } catch (Exception e) {
            result = false;
        }
        return result;
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
        java.util.Set keys = new java.util.HashSet(m_resourceMap.keySet());
        java.util.Iterator i = keys.iterator();
        while (i.hasNext()) {
            String s = (String)i.next();
            if (s.endsWith(suffix)) {
                CmsFlexCacheVariation v = (CmsFlexCacheVariation)m_resourceMap.get(s);
                if (entriesOnly) {
                    // Clear only entry
                    m_size -= v.m_map.size();
                    java.util.Iterator allEntries = v.m_map.values().iterator();
                    while (allEntries.hasNext()) {
                        I_CmsLruCacheObject nextObject = (I_CmsLruCacheObject)allEntries.next();
                        allEntries.remove();
                        this.m_entryLruCache.remove(nextObject);
                    }
                    v.m_map = java.util.Collections.synchronizedMap(new HashMap(C_INITIAL_CAPACITY_VARIATIONS));
                } else {
                    // Clear key and entry
                    m_size -= v.m_map.size();
                    java.util.Iterator allEntries = v.m_map.values().iterator();
                    while (allEntries.hasNext()) {
                        I_CmsLruCacheObject nextObject = (I_CmsLruCacheObject)allEntries.next();
                        allEntries.remove();
                        this.m_entryLruCache.remove(nextObject);
                    }
                    
                    this.m_variationCache.remove(v);
                    
                    v.m_map = null;
                    v.m_key = null;
                    m_resourceMap.remove(s);
                }
            }
        }
        if (OpenCms.isLogging(I_CmsLogChannels.C_FLEX_CACHE)) 
            OpenCms.log(I_CmsLogChannels.C_FLEX_CACHE, "[FlexCache] Part of the FlexCache cleared - clearOneHalf(" + suffix + ", " + entriesOnly + ") called");
    }
    
    /**
     * Returns the LRU cache where the CacheEntries are cached.<p>
     *
     * @return the LRU cache where the CacheEntries are cached
     */
    public CmsLruCache getEntryLruCache() {
        return this.m_entryLruCache;
    }
        
    /**
     * A simple data container class for the FlexCache variations.<p>
     * 
     * @see com.opencms.flex.cache.CmsFlexCache
     * @see com.opencms.flex.util.I_CmsFlexLruCacheObject
     * @author Alexander Kandzior (a.kandzior@alkacon.com)
     * @author Thomas Weckert (t.weckert@alkacon.com)
     * @version $Revision: 1.2 $ 
     */
    class CmsFlexCacheVariation extends Object implements org.opencms.cache.I_CmsLruCacheObject {
        
        /** Pointer to the next cache entry in the LRU cache */
        private I_CmsLruCacheObject m_next;
        
        /** Pointer to the previous cache entry in the LRU cache. */
        private I_CmsLruCacheObject m_previous;        
        
        /** The key belonging to the resource */
        public CmsFlexCacheKey m_key;
        
        /** Maps variations to CmsFlexCacheEntries */
        public Map m_map;
        
        /** Internal debug switch */
        private static final int DEBUG = 0;
                
        /**
         * Generates a new instance of CmsFlexCacheVariation.<p>
         *
         * @param theKey The (resource) key to contruct this variation list for
         */
        public CmsFlexCacheVariation(CmsFlexCacheKey theKey) {
            this.m_key = theKey;
            this.m_map = java.util.Collections.synchronizedMap(new HashMap(CmsFlexCache.C_INITIAL_CAPACITY_VARIATIONS));
        }
        
        // implementation of the com.opencms.flex.util.I_CmsFlexLruCacheObject interface methods
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#setNextLruObject(I_CmsFlexLruCacheObject)
         */
        public void setNextLruObject(I_CmsLruCacheObject theNextEntry) {
            this.m_next = theNextEntry;
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#getNextLruObject()
         */
        public I_CmsLruCacheObject getNextLruObject() {
            return this.m_next;
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#setPreviousLruObject(I_CmsFlexLruCacheObject)
         */
        public void setPreviousLruObject(I_CmsLruCacheObject thePreviousEntry) {
            this.m_previous = thePreviousEntry;
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#getPreviousLruObject()
         */
        public I_CmsLruCacheObject getPreviousLruObject() {
            return this.m_previous;
        }  
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#addToLruCache()
         */
        public void addToLruCache() {
            // NOOP
            if (DEBUG>0) System.out.println("Added resource " + this.m_key.m_resource + " to the LRU cache");
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#removeFromLruCache()
         */
        public void removeFromLruCache() {
            // NOOP            
            if (DEBUG>0) System.out.println("Removed resource " + this.m_key.m_resource + " from the LRU cache");
        }
        
        /**
         * @see com.opencms.flex.util.I_CmsFlexLruCacheObject#getLruCacheCosts()
         */
        public int getLruCacheCosts() {
            return this.m_map.size();
        }     
    }    
}

