/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/util/Attic/CmsFlexLruCache.java,v $
 * Date   : $Date: 2002/09/05 12:48:02 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.flex.util;

import java.util.*;

/**
 * The idea of this cache is, to separate the caching policy from the data structure
 * where the cached objects are stored. The advantage of doing so is, that the CmsFlexLruCache
 * can identify the last-recently-used object in O(1), whereas you would need at least
 * O(n) to traverse the data structure that stores the cached objects. Second, you can
 * easily use the CmsFlexLruCache to get an LRU cache, no matter what data structure is used to
 * store your objects.
 * <p>
 * The cache policy is affected by the "costs" of the objects being cached. Valuable cache costs
 * might be the byte size of the cached objects for example.
 * <p>
 * To add/remove cached objects from the data structure that stores them, the objects have to
 * implement the methods defined in the interface I_CmsFlexLruCacheObject to be notified when they
 * are added/removed from the CmsFlexLruCache.
 *
 * @see com.opencms.flex.util.I_CmsFlexLruCacheObject
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsFlexLruCache extends java.lang.Object {
    
    /** The head of the list of double linked LRU cache objects. */
    private I_CmsFlexLruCacheObject m_LruCacheHead;
    
    /** The tail of the list of double linked LRU cache objects. */
    private I_CmsFlexLruCacheObject m_LruCacheTail;
    
    /** Force a finalization after down-sizing the cache? */
    private boolean m_ForceFinalization;
    
    /** The max. sum of costs the cached objects might reach. */
    private int m_MaxCacheCosts;
    
    /** The avg. sum of costs the cached objects. */
    private int m_AvgCacheCosts;
    
    /** The max. costs of cacheable objects. */
    private int m_MaxCacheObjectCosts;
    
    /** The costs of all cached objects. */
    private int m_CurrentCachedObjectCosts;
    
    /** The sum of all cached objects. */
    private int m_CurrentCachedObjectCount;
    
    private static final int DEBUG = 0;
    
    
    /**
     * This constructor reads the configuration parameters from the opencms.properties configuration file.
     *
     * @param theOpenCms the OpenCms instance
     */
    public CmsFlexLruCache( com.opencms.core.A_OpenCms theOpenCms ) {
        source.org.apache.java.util.Configurations opencmsProperties = theOpenCms.getConfiguration();
        
        this.m_ForceFinalization = opencmsProperties.getBoolean( "flex.cache.lru.forceGC", true );
        this.m_MaxCacheCosts = opencmsProperties.getInteger( "flex.cache.lru.maxCacheCosts", 1000000 );
        this.m_AvgCacheCosts = opencmsProperties.getInteger( "flex.cache.lru.avgCacheCosts", 750000 );
        this.m_MaxCacheObjectCosts = opencmsProperties.getInteger( "flex.cache.lru.maxCacheObjectCosts", 250000 );

        this.m_CurrentCachedObjectCosts = 0;
        this.m_CurrentCachedObjectCount = 0;
        
        this.m_LruCacheHead = null;
        this.m_LruCacheTail = null;        
    }
    
    /**
     * The constructor with all options.
     *
     * @param theMaxCacheCosts the max. cache costs of all cached objects
     * @param theAvgCacheCosts the avg. cache costs of all cached objects
     * @param theMaxCacheObjectCosts the max. allowed cache costs per object
     * @param forceFinalization should be true if a system wide garbage collection/finalization is forced after objects were removed from the cache
     */
    public CmsFlexLruCache( int theMaxCacheCosts, int theAvgCacheCosts, int theMaxCacheObjectCosts, boolean forceFinalization ) {
        this.m_MaxCacheCosts = theMaxCacheCosts;
        this.m_AvgCacheCosts = theAvgCacheCosts;
        this.m_MaxCacheObjectCosts = theMaxCacheObjectCosts;
        
        this.m_ForceFinalization = forceFinalization;
        
        this.m_CurrentCachedObjectCosts = 0;
        this.m_CurrentCachedObjectCount = 0;
        
        this.m_LruCacheHead = null;
        this.m_LruCacheTail = null;
    }
    
    /**
     * Constructor for a LRU cache with forced garbage collection/finalization.
     *
     * @param theMaxCacheCosts the max. cache costs of all cached objects
     * @param theAvgCacheCosts the avg. cache costs of all cached objects
     * @param theMaxCacheObjectCosts the max. allowed cache costs per object
     */
    public CmsFlexLruCache( int theMaxCacheCosts, int theAvgCacheCosts, int theMaxCacheObjectCosts ) {
        this( theMaxCacheCosts, theAvgCacheCosts, theMaxCacheObjectCosts, true );
    }
    
    /**
     * Constructor for a LRU cache with forced garbage collection/finalization, the max. allowed costs of cacheable
     * objects is 1/4 of the max. costs of all cached objects.
     *
     * @param theMaxCacheCosts the max. cache costs of all cached objects
     * @param theAvgCacheCosts the avg. cache costs of all cached objects
     */
    public CmsFlexLruCache( int theMaxCacheCosts, int theAvgCacheCosts ) {
        this( theMaxCacheCosts, theAvgCacheCosts, theMaxCacheCosts/4, true );
    }
    
    /**
     * Constructor for a LRU cache where the max. allowed costs of cacheable objects is 1/4 of the max. costs of all cached objects.
     *
     * @param theMaxCacheCosts the max. cache costs of all cached objects
     * @param theAvgCacheCosts the avg. cache costs of all cached objects
     * @param forceFinalization should be true if a system wide garbage collection/finalization is forced after objects were removed from the cache
     */
    public CmsFlexLruCache( int theMaxCacheCosts, int theAvgCacheCosts, boolean forceFinalization ) {
        this( theMaxCacheCosts, theAvgCacheCosts, theMaxCacheCosts/4, forceFinalization );
    }
    
    /**
     * Returns a string representing the current state of the cache.
     */
    public String toString() {
        String objectInfo = "\n";
        
        objectInfo += "max. cache costs of all cached objects: " + this.m_MaxCacheCosts + "\n";
        objectInfo += "avg. cache costs of all cached objects: " + this.m_AvgCacheCosts + "\n";
        objectInfo += "max. cache costs per object: " + this.m_MaxCacheObjectCosts + "\n";
        objectInfo += "costs of all cached objects: " + this.m_CurrentCachedObjectCosts + "\n";
        objectInfo += "sum of all cached objects: " + this.m_CurrentCachedObjectCount + "\n";
        
        if (!this.m_ForceFinalization) {
            objectInfo += "no ";
        }
        objectInfo += "system garbage collection is forced during clean up\n";
        
        return objectInfo;
    }
    
    /**
     * Add a new object to the cache.
     *
     * @param theCacheObject the object being added to the cache
     * @return true if the object was added to the cache, false if the object was denied because its cache costs were higher than the allowed max. cache costs per object
     */
    public boolean add( I_CmsFlexLruCacheObject theCacheObject ) {
        // only objects with cache costs < the max. allowed object cache costs can be cached!
        if (theCacheObject.getLruCacheCosts()>=this.m_MaxCacheObjectCosts) {
            this.log( "error: you are trying to cache objects with cache costs bigger than " + this.m_MaxCacheObjectCosts );
            return false;
        }
        
        // add the object to the list of all cached objects in the cache
        this.addHead( theCacheObject );
        
        // check if the cache has to trash the last-recently-used objects before adding a new object
        if (this.m_CurrentCachedObjectCosts>this.m_MaxCacheCosts) {
            this.gc();
        }
        
        return true;
    }
    
    /**
     * Touch an existing object in the cache in the sense that it' "last-recently-used" state
     * is updated.
     *
     * @param theCacheObject the object being touched
     */
    public boolean touch( I_CmsFlexLruCacheObject theCacheObject ) {
        // only objects with cache costs < the max. allowed object cache costs can be cached!
        if (theCacheObject.getLruCacheCosts()>=this.m_MaxCacheObjectCosts) {
            this.log( "error: you are trying to cache objects with cache costs bigger than " + this.m_MaxCacheObjectCosts );
            this.remove( theCacheObject );
            return false;
        }
                
        // set the list pointers correct
        if (theCacheObject.getNextLruObject()==null) {
            // case 1: the object is already at the head pos.
            return true;
        }
        else if (theCacheObject.getPreviousLruObject()==null) {
            // case 2: the object at the tail pos., remove it from the tail to put it to the front as the new head
            I_CmsFlexLruCacheObject newTail = theCacheObject.getNextLruObject();
            newTail.setPreviousLruObject( null );
            this.m_LruCacheTail = newTail;
        }
        else {
            // case 3: the object is somewhere within the list, remove it to put it the front as the new head
            theCacheObject.getPreviousLruObject().setNextLruObject( theCacheObject.getNextLruObject() );
            theCacheObject.getNextLruObject().setPreviousLruObject( theCacheObject.getPreviousLruObject() );
        }
        
        // set the touched object as the new head in the linked list:
        I_CmsFlexLruCacheObject oldHead = this.m_LruCacheHead;
        oldHead.setNextLruObject( theCacheObject );
        theCacheObject.setNextLruObject( null );
        theCacheObject.setPreviousLruObject( oldHead );
        this.m_LruCacheHead = theCacheObject;
        
        return true;
    }
    
    /**
     * Adds a cache object as the new haed to the list of all cached objects.
     *
     * @param theCacheObject the object being added as the new head to the list of all cached objects
     */
    private void addHead( I_CmsFlexLruCacheObject theCacheObject ) {
        // set the list pointers correct
        if (this.m_CurrentCachedObjectCount>0) {
            // there is at least 1 object already in the list
            I_CmsFlexLruCacheObject oldHead = this.m_LruCacheHead;
            oldHead.setNextLruObject( theCacheObject );
            theCacheObject.setPreviousLruObject( oldHead );
            this.m_LruCacheHead = theCacheObject;
        }
        else {
            // it is the first object to be added to the list
            this.m_LruCacheTail = this.m_LruCacheHead = theCacheObject;
            theCacheObject.setPreviousLruObject( null );
        }
        theCacheObject.setNextLruObject( null );
        
        // update cache stats. and notify the cached object
        this.increaseCache( theCacheObject );
    }
    
    /**
     * Removes an object from the list of all cached objects,
     * no matter what position it has inside the list.
     *
     * @param theCacheObject the object being removed from the list of all cached objects
     * @return a reference to the object that was removed
     */
    public synchronized I_CmsFlexLruCacheObject remove( I_CmsFlexLruCacheObject theCacheObject ) {
        // set the list pointers correct
        if (theCacheObject.getNextLruObject()==null) {
            // remove the object from the head pos.
            I_CmsFlexLruCacheObject newHead = theCacheObject.getPreviousLruObject();
            newHead.setNextLruObject( null );
            this.m_LruCacheHead = newHead;
        }
        else if (theCacheObject.getPreviousLruObject()==null) {
            // remove the object from the tail pos.
            I_CmsFlexLruCacheObject newTail = theCacheObject.getNextLruObject();
            newTail.setPreviousLruObject( null );
            this.m_LruCacheTail = newTail;
        }
        else {
            // remove the object from within the list
            theCacheObject.getPreviousLruObject().setNextLruObject( theCacheObject.getNextLruObject() );
            theCacheObject.getNextLruObject().setPreviousLruObject( theCacheObject.getPreviousLruObject() );
        }
        
        // update cache stats. and notify the cached object
        this.decreaseCache( theCacheObject );
        
        return theCacheObject;
    }
    
    /**
     * Removes the tailing object from the list of all cached objects.
     */
    private synchronized void removeTail() {
        I_CmsFlexLruCacheObject oldTail = this.m_LruCacheTail;
        I_CmsFlexLruCacheObject newTail = oldTail.getNextLruObject();
        
        // set the list pointers correct
        if (newTail!=null) {
            // there are still objects remaining in the list
            newTail.setPreviousLruObject( null );
            this.m_LruCacheTail = newTail;
        }
        else {
            // we removed the last object from the list
            this.m_LruCacheTail = this.m_LruCacheHead = null;
        }
        
        // update cache stats. and notify the cached object
        this.decreaseCache( oldTail );
    }
    
    /**
     * Decrease the cache stats. and notify the cached object that it was removed from the cache.
     *
     * @param theCacheObject the object being notified that it was removed from the cache
     */
    private void decreaseCache( I_CmsFlexLruCacheObject theCacheObject ) {
        // notify the object that it was now removed from the cache
        //theCacheObject.notify();
        theCacheObject.removeFromLruCache();
        
        // set the list pointers to null
        theCacheObject.setNextLruObject( null );
        theCacheObject.setPreviousLruObject( null );
        
        // update the cache stats.
        this.m_CurrentCachedObjectCosts -= theCacheObject.getLruCacheCosts();
        this.m_CurrentCachedObjectCount--;
    }
    
    /**
     * Increase the cache stats. and notify the cached object that it was added to the cache.
     *
     * @param theCacheObject the object being notified that it was added to the cache
     */
    private void increaseCache( I_CmsFlexLruCacheObject theCacheObject ) {
        // notify the object that it was now added to the cache
        //theCacheObject.notify();
        theCacheObject.addToLruCache();
        
        // update the cache stats.
        this.m_CurrentCachedObjectCosts += theCacheObject.getLruCacheCosts();
        this.m_CurrentCachedObjectCount++;
    }
    
    /**
     * Removes the last recently used objects from the list of all cached objects as long
     * as the costs of all cached objects are higher than the allowed avg. costs of the cache.
     */
    private void gc() {
        this.log( this.toString() );
        this.log( "free memory before garbage collection:\t" + Runtime.getRuntime().freeMemory() );
        
        I_CmsFlexLruCacheObject currentObject = this.m_LruCacheTail;
        while (currentObject!=null) {
            if (this.m_CurrentCachedObjectCosts<this.m_AvgCacheCosts) break;
            currentObject = currentObject.getNextLruObject();
            this.removeTail();
        }
        
        if (m_ForceFinalization) {
            // force a finalization/system garbage collection optionally
            Runtime.getRuntime().runFinalization();
            System.gc();
        }
        
        this.log( this.toString() );
        this.log( "free memory after garbage collection:\t" + Runtime.getRuntime().freeMemory() );
    }
    
    /**
     * Write a message to the log media.
     *
     * @param theLogMessage the message being logged
     */
    private void log( String theLogMessage ) {
        if (com.opencms.core.A_OpenCms.isLogging() && com.opencms.boot.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING) {
            com.opencms.boot.CmsBase.log(com.opencms.boot.CmsBase.C_FLEX_CACHE, "[" + this.getClass().getName() + "] " + theLogMessage );
        }
    }
    
    /**
     * Returns the count of all cached objects.
     *
     * @return the count of all cached objects
     */
    public int getSize() {
        return this.m_CurrentCachedObjectCount;
    }
    
    /**
     * Clears the cache for finalization.
     */
    protected void finalize() throws java.lang.Throwable {
        this.clear();
    }
    
    /**
     * Removes all cached objects and resets the internal cache.
     */
    public void clear() {
        // remove all objects from the linked list from the tail to the head:
        I_CmsFlexLruCacheObject currentObject = this.m_LruCacheTail;
        while (currentObject!=null) {
            currentObject = currentObject.getNextLruObject();
            this.removeTail();
        }
        
        this.m_CurrentCachedObjectCosts = this.m_CurrentCachedObjectCount = 0;
        this.m_LruCacheHead = this.m_LruCacheTail = null;
        
        if (m_ForceFinalization) {
            // force a finalization/system garbage collection optionally
            Runtime.getRuntime().runFinalization();
            System.gc();
        }        
    }
}
