/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cache/CmsLruCache.java,v $
 * Date   : $Date: 2003/11/08 10:32:44 $
 * Version: $Revision: 1.10 $
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
 
package org.opencms.cache;

import org.opencms.main.OpenCms;


/**
 * Implements an LRU (last recently used) cache.<p>
 * 
 * The idea of this cache to separate the caching policy from the data structure
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
 * implement the methods defined in the interface I_CmsLruCacheObject to be notified when they
 * are added/removed from the CmsFlexLruCache.
 *
 * @see org.opencms.cache.I_CmsLruCacheObject
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.10 $
 */
public class CmsLruCache extends java.lang.Object {
    
    /** The head of the list of double linked LRU cache objects. */
    private I_CmsLruCacheObject m_listHead;
    
    /** The tail of the list of double linked LRU cache objects. */
    private I_CmsLruCacheObject m_listTail;
    
    /** Force a finalization after down-sizing the cache? */
    private boolean m_forceFinalization;
    
    /** The max. sum of costs the cached objects might reach. */
    private int m_maxCacheCosts;
    
    /** The avg. sum of costs the cached objects. */
    private int m_avgCacheCosts;
    
    /** The max. costs of cacheable objects. */
    private int m_maxObjectCosts;
    
    /** The costs of all cached objects. */
    private int m_objectCosts;
    
    /** The sum of all cached objects. */
    private int m_objectCount;
    
    
    /**
     * The constructor with all options.<p>
     *
     * @param theMaxCacheCosts the max. cache costs of all cached objects
     * @param theAvgCacheCosts the avg. cache costs of all cached objects
     * @param theMaxObjectCosts the max. allowed cache costs per object. Set theMaxObjectCosts to -1 if you don't want to limit the max. allowed cache costs per object
     * @param forceFinalization should be true if a system wide garbage collection/finalization is forced after objects were removed from the cache
     */
    public CmsLruCache(int theMaxCacheCosts, int theAvgCacheCosts, int theMaxObjectCosts, boolean forceFinalization) {
        this.m_forceFinalization = forceFinalization;
        this.m_maxCacheCosts = theMaxCacheCosts;
        this.m_avgCacheCosts = theAvgCacheCosts;
        this.m_maxObjectCosts = theMaxObjectCosts;
        
        this.m_objectCosts = 0; 
        this.m_objectCount = 0;
        this.m_listHead = null; 
        this.m_listTail = null;
    }
    
    /**
     * Constructor for a LRU cache with forced garbage collection/finalization.<p>
     *
     * @param theMaxCacheCosts the max. cache costs of all cached objects
     * @param theAvgCacheCosts the avg. cache costs of all cached objects
     * @param theMaxObjectCosts the max. allowed cache costs per object. Set theMaxObjectCosts to -1 if you don't want to limit the max. allowed cache costs per object
     */
    // TODO: never used
    /*
    public CmsLruCache(int theMaxCacheCosts, int theAvgCacheCosts, int theMaxObjectCosts) {
        this(theMaxCacheCosts, theAvgCacheCosts, theMaxObjectCosts, false);
    }*/
    
    /**
     * Constructor for a LRU cache with forced garbage collection/finalization, the max. allowed costs of cacheable
     * objects is 1/4 of the max. costs of all cached objects.<p>
     *
     * @param theMaxCacheCosts the max. cache costs of all cached objects
     * @param theAvgCacheCosts the avg. cache costs of all cached objects
     */
    // TODO: never used
    /*
    public CmsLruCache(int theMaxCacheCosts, int theAvgCacheCosts) {
        this(theMaxCacheCosts, theAvgCacheCosts, theMaxCacheCosts/4, false);
    }*/
    
    /**
     * Constructor for a LRU cache where the max. allowed costs of cacheable objects is 1/4 of the max. costs of all cached objects.<p>
     *
     * @param theMaxCacheCosts the max. cache costs of all cached objects
     * @param theAvgCacheCosts the avg. cache costs of all cached objects
     * @param forceFinalization should be true if a system wide garbage collection/finalization is forced after objects were removed from the cache
     */
    // TODO: never used
    /*
    public CmsLruCache(int theMaxCacheCosts, int theAvgCacheCosts, boolean forceFinalization) {
        this(theMaxCacheCosts, theAvgCacheCosts, theMaxCacheCosts/4, forceFinalization);
    }*/
    
    /**
     * Returns a string representing the current state of the cache.<p>
     * @return a string representing the current state of the cache
     */
    public String toString() {
        String objectInfo = "";
        
        objectInfo += "max. cache costs of all cached objects: " + this.m_maxCacheCosts + "\n";
        objectInfo += "avg. cache costs of all cached objects: " + this.m_avgCacheCosts + "\n";
        objectInfo += "max. cache costs per object: " + this.m_maxObjectCosts + "\n";
        objectInfo += "costs of all cached objects: " + this.m_objectCosts + "\n";
        objectInfo += "sum of all cached objects: " + this.m_objectCount + "\n";
        
        if (!this.m_forceFinalization) {
            objectInfo += "no ";
        }
        objectInfo += "system garbage collection is forced during clean up\n";
        
        return objectInfo;
    }
    
    /**
     * Adds a new object to this cache.<p>
     * 
     * If add the same object more than once,
     * the object is touched instead.<p>
     *
     * @param theCacheObject the object being added to the cache
     * @return true if the object was added to the cache, false if the object was denied because its cache costs were higher than the allowed max. cache costs per object
     */
    public synchronized boolean add(I_CmsLruCacheObject theCacheObject) {
        if (theCacheObject == null) {
            // null can't be added or touched in the cache 
            return false;
        }
        
        // only objects with cache costs < the max. allowed object cache costs can be cached!
        if ((this.m_maxObjectCosts!=-1) && (theCacheObject.getLruCacheCosts() > this.m_maxObjectCosts)) {
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("Attempt to cache objects with cache costs " + theCacheObject.getLruCacheCosts() + " which is bigger than the max. allowed costs " + this.m_maxObjectCosts);
            }
            return false;
        }
        
        if (!this.isCached(theCacheObject)) {
            // add the object to the list of all cached objects in the cache
            this.addHead(theCacheObject);
        } else {
            this.touch(theCacheObject);
        }
        
        // check if the cache has to trash the last-recently-used objects before adding a new object
        if (this.m_objectCosts > this.m_maxCacheCosts) {
            this.gc();
        }
        
        return true;
    }
    
    /**
     * Test if a given object resides inside the cache.<p>
     *
     * @param theCacheObject the object to test 
     * @return true if the object is inside the cache, false otherwise
     */
    private boolean isCached(I_CmsLruCacheObject theCacheObject) {
        if (theCacheObject == null || m_objectCount == 0) {
            // the cache is empty or the object is null (which is never cached)
            return false;
        }
        
        I_CmsLruCacheObject nextObj;
        I_CmsLruCacheObject prevObj;
        
        if (((nextObj = theCacheObject.getNextLruObject()) != null) || ((prevObj = theCacheObject.getPreviousLruObject()) != null)) { 
            // the object has either a predecessor or successor in the linked 
            // list of all cached objects, so it is inside the cache
            return true;
        }
        
        if ((nextObj == null) && (prevObj == null)) {
            if ((m_objectCount == 1) 
            && (m_listHead != null) 
            && (m_listTail != null)
            && m_listHead.equals(theCacheObject) 
            && m_listTail.equals(theCacheObject)) {
                // the object is the one and only object in the cache
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Touch an existing object in this cache, in the sense that it's "last-recently-used" state
     * is updated.<p>
     *
     * @param theCacheObject the object being touched
     * @return true if an object was found and touched
     */
    public synchronized boolean touch(I_CmsLruCacheObject theCacheObject) {
        if (!isCached(theCacheObject)) {
            return false;
        }
        
        // only objects with cache costs < the max. allowed object cache costs can be cached!
        if ((m_maxObjectCosts!=-1) && (theCacheObject.getLruCacheCosts()>m_maxObjectCosts)) {
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("Attempt to cache objects with cache costs " + theCacheObject.getLruCacheCosts() + " which is bigger than the max. allowed costs " + this.m_maxObjectCosts);
            }
            remove(theCacheObject);
            return false;
        }
                
        // set the list pointers correct
        I_CmsLruCacheObject nextObj;
        I_CmsLruCacheObject prevObj;
        if ((nextObj = theCacheObject.getNextLruObject()) == null) {
            // case 1: the object is already at the head pos.
            return true;
        } else if ((prevObj = theCacheObject.getPreviousLruObject()) == null) {
            // case 2: the object at the tail pos., remove it from the tail to put it to the front as the new head
            I_CmsLruCacheObject newTail = nextObj;
            newTail.setPreviousLruObject(null);
            m_listTail = newTail;
        } else {
            // case 3: the object is somewhere within the list, remove it to put it the front as the new head
            prevObj.setNextLruObject(nextObj);
            nextObj.setPreviousLruObject(prevObj);
        }
        
        // set the touched object as the new head in the linked list:
        I_CmsLruCacheObject oldHead = this.m_listHead;
        if (oldHead != null) {
            oldHead.setNextLruObject(theCacheObject);
            theCacheObject.setNextLruObject(null);
            theCacheObject.setPreviousLruObject(oldHead);
        }
        this.m_listHead = theCacheObject;
        
        return true;
    }
    
    /**
     * Adds a cache object as the new haed to the list of all cached objects in this cache.<p>
     *
     * @param theCacheObject the object being added as the new head to the list of all cached objects
     */
    private void addHead(I_CmsLruCacheObject theCacheObject) {
        // set the list pointers correct
        if (this.m_objectCount>0) {
            // there is at least 1 object already in the list
            I_CmsLruCacheObject oldHead = this.m_listHead;
            oldHead.setNextLruObject(theCacheObject);
            theCacheObject.setPreviousLruObject(oldHead);
            this.m_listHead = theCacheObject;
        } else {
            // it is the first object to be added to the list
            this.m_listTail = theCacheObject;
            this.m_listHead = theCacheObject;
            theCacheObject.setPreviousLruObject(null);
        }
        theCacheObject.setNextLruObject(null);
        
        // update cache stats. and notify the cached object
        this.increaseCache(theCacheObject);
    }
    
    /**
     * Removes an object from the list of all cached objects in this cache,
     * no matter what position it has inside the list.<p>
     *
     * @param theCacheObject the object being removed from the list of all cached objects
     * @return a reference to the object that was removed
     */
    public synchronized I_CmsLruCacheObject remove(I_CmsLruCacheObject theCacheObject) {
        if (!this.isCached(theCacheObject)) {
            // theCacheObject is null or not inside the cache
            return null;
        }
        
        // set the list pointers correct
        if (theCacheObject.getNextLruObject()==null) {
            // remove the object from the head pos.
            I_CmsLruCacheObject newHead = theCacheObject.getPreviousLruObject();
            
            if (newHead!=null) {
                // if newHead is null, theCacheObject 
                // was the only object in the cache
                newHead.setNextLruObject(null);
            }
            
            this.m_listHead = newHead;
        } else if (theCacheObject.getPreviousLruObject()==null) {
            // remove the object from the tail pos.
            I_CmsLruCacheObject newTail = theCacheObject.getNextLruObject();
            
            if (newTail!=null) {
                // if newTail is null, theCacheObject 
                // was the only object in the cache                
                newTail.setPreviousLruObject(null);
            }
            
            this.m_listTail = newTail;
        } else {
            // remove the object from within the list
            theCacheObject.getPreviousLruObject().setNextLruObject(theCacheObject.getNextLruObject());
            theCacheObject.getNextLruObject().setPreviousLruObject(theCacheObject.getPreviousLruObject());
        }
        
        // update cache stats. and notify the cached object
        this.decreaseCache(theCacheObject);
        
        return theCacheObject;
    }
    
    /**
     * Removes the tailing object from the list of all cached objects.<p>
     */
    private synchronized void removeTail() {
        I_CmsLruCacheObject oldTail = null;
        
        if ((oldTail=this.m_listTail)!=null) {
            I_CmsLruCacheObject newTail = oldTail.getNextLruObject();
            
            // set the list pointers correct
            if (newTail!=null) {
                // there are still objects remaining in the list
                newTail.setPreviousLruObject(null);
                this.m_listTail = newTail;
            } else {
                // we removed the last object from the list
                this.m_listTail = null; 
                this.m_listHead = null;
            }
            
            // update cache stats. and notify the cached object
            this.decreaseCache(oldTail);
        }
    }
    
    /**
     * Decrease this caches statistics
     * and notify the cached object that it was removed from this cache.<p>
     *
     * @param theCacheObject the object being notified that it was removed from the cache
     */
    private void decreaseCache(I_CmsLruCacheObject theCacheObject) {
        // notify the object that it was now removed from the cache
        //theCacheObject.notify();
        theCacheObject.removeFromLruCache();
        
        // set the list pointers to null
        theCacheObject.setNextLruObject(null);
        theCacheObject.setPreviousLruObject(null);
        
        // update the cache stats.
        this.m_objectCosts -= theCacheObject.getLruCacheCosts();
        this.m_objectCount--;
    }
    
    /**
     * Increase this caches statistics 
     * and notify the cached object that it was added to this cache.<p>
     *
     * @param theCacheObject the object being notified that it was added to the cache
     */
    private void increaseCache(I_CmsLruCacheObject theCacheObject) {
        // notify the object that it was now added to the cache
        //theCacheObject.notify();
        theCacheObject.addToLruCache();
        
        // update the cache stats.
        this.m_objectCosts += theCacheObject.getLruCacheCosts();
        this.m_objectCount++;
    }
    
    /**
     * Removes the last recently used objects from the list of all cached objects as long
     * as the costs of all cached objects are higher than the allowed avg. costs of the cache.<p>
     */
    private void gc() {       
        I_CmsLruCacheObject currentObject = this.m_listTail;
        while (currentObject != null) {
            if (this.m_objectCosts < this.m_avgCacheCosts) {
                break;
            }
            currentObject = currentObject.getNextLruObject();
            this.removeTail();
        }
        
        if (m_forceFinalization) {
            // force a finalization/system garbage collection optionally
            System.runFinalization();
            Runtime.getRuntime().runFinalization();
            System.gc();
            Runtime.getRuntime().gc();            
        }
    }
    
    /**
     * Returns the count of all cached objects.<p>
     *
     * @return the count of all cached objects
     */
    public int size() {
        return this.m_objectCount;
    }
    
    /**
     * Clears this cache for finalization.<p>
     * @throws Throwable if something goes wring
     */
    protected void finalize() throws Throwable {
        this.clear();
        super.finalize();
    }
    
    /**
     * Removes all cached objects in this cache.<p>
     */
    public void clear() {
        // remove all objects from the linked list from the tail to the head:
        I_CmsLruCacheObject currentObject = this.m_listTail;
        while (currentObject!=null) {
            currentObject = currentObject.getNextLruObject();
            this.removeTail();
        }
        
        // reset the data structure
        this.m_objectCosts = 0;
        this.m_objectCount = 0;
        this.m_listHead = null; 
        this.m_listTail = null;
        
        if (m_forceFinalization) {
            // force a finalization/system garbage collection optionally
            Runtime.getRuntime().runFinalization();
            System.gc();
        }        
    }
    
    /**
     * Returns the average costs of all cached objects.<p>
     * 
     * @return the average costs of all cached objects
     */
    public int getAvgCacheCosts() {
        return this.m_avgCacheCosts;
    }

    /**
     * Returns the max costs of all cached objects.<p>
     * 
     * @return the max costs of all cached objects
     */
    public int getMaxCacheCosts() {
        return this.m_maxCacheCosts;
    }

    /**
     * Returns the max allowed costs per cached object.<p>
     * 
     * @return the max allowed costs per cached object
     */
    public int getMaxObjectCosts() {
        return this.m_maxObjectCosts;
    }

    /**
     * Returns the current costs of all cached objects.<p>
     * 
     * @return the current costs of all cached objects
     */
    public int getObjectCosts() {
        return this.m_objectCosts;
    }

}
