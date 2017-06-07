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

import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * Implements an LRU (last recently used) cache.<p>
 *
 * The idea of this cache is to separate the caching policy from the data structure
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
 * are added/removed from the CmsFlexLruCache.<p>
 *
 * @see org.opencms.cache.I_CmsLruCacheObject
 *
 * @since 6.0.0
 */
public class CmsLruCache extends java.lang.Object {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLruCache.class);

    /** The average sum of costs the cached objects. */
    private long m_avgCacheCosts;

    /** The head of the list of double linked LRU cache objects. */
    private I_CmsLruCacheObject m_listHead;

    /** The tail of the list of double linked LRU cache objects. */
    private I_CmsLruCacheObject m_listTail;

    /** The maximum sum of costs the cached objects might reach. */
    private long m_maxCacheCosts;

    /** The maximum costs of cacheable objects. */
    private int m_maxObjectCosts;

    /** The costs of all cached objects. */
    private int m_objectCosts;

    /** The sum of all cached objects. */
    private int m_objectCount;

    /**
     * The constructor with all options.<p>
     *
     * @param theMaxCacheCosts the maximum cache costs of all cached objects
     * @param theAvgCacheCosts the average cache costs of all cached objects
     * @param theMaxObjectCosts the maximum allowed cache costs per object. Set theMaxObjectCosts to -1 if you don't want to limit the max. allowed cache costs per object
     */
    public CmsLruCache(long theMaxCacheCosts, long theAvgCacheCosts, int theMaxObjectCosts) {

        m_maxCacheCosts = theMaxCacheCosts;
        m_avgCacheCosts = theAvgCacheCosts;
        m_maxObjectCosts = theMaxObjectCosts;
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
        if ((m_maxObjectCosts != -1) && (theCacheObject.getLruCacheCosts() > m_maxObjectCosts)) {
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_CACHE_COSTS_TOO_HIGH_2,
                        new Integer(theCacheObject.getLruCacheCosts()),
                        new Integer(m_maxObjectCosts)));
            }
            return false;
        }

        if (!isCached(theCacheObject)) {
            // add the object to the list of all cached objects in the cache
            addHead(theCacheObject);
        } else {
            touch(theCacheObject);
        }

        // check if the cache has to trash the last-recently-used objects before adding a new object
        if (m_objectCosts > m_maxCacheCosts) {
            gc();
        }

        return true;
    }

    /**
     * Removes all cached objects in this cache.<p>
     */
    public synchronized void clear() {

        // remove all objects from the linked list from the tail to the head:
        I_CmsLruCacheObject currentObject = m_listTail;
        while (currentObject != null) {
            currentObject = currentObject.getNextLruObject();
            removeTail();
        }

        // reset the data structure
        m_objectCosts = 0;
        m_objectCount = 0;
        m_listHead = null;
        m_listTail = null;
    }

    /**
     * Returns the average costs of all cached objects.<p>
     *
     * @return the average costs of all cached objects
     */
    public long getAvgCacheCosts() {

        return m_avgCacheCosts;
    }

    /**
     * Returns the max costs of all cached objects.<p>
     *
     * @return the max costs of all cached objects
     */
    public long getMaxCacheCosts() {

        return m_maxCacheCosts;
    }

    /**
     * Returns the max allowed costs per cached object.<p>
     *
     * @return the max allowed costs per cached object
     */
    public int getMaxObjectCosts() {

        return m_maxObjectCosts;
    }

    /**
     * Returns the current costs of all cached objects.<p>
     *
     * @return the current costs of all cached objects
     */
    public int getObjectCosts() {

        return m_objectCosts;
    }

    /**
     * Removes an object from the list of all cached objects in this cache,
     * no matter what position it has inside the list.<p>
     *
     * @param theCacheObject the object being removed from the list of all cached objects
     * @return a reference to the object that was removed
     */
    public synchronized I_CmsLruCacheObject remove(I_CmsLruCacheObject theCacheObject) {

        if (!isCached(theCacheObject)) {
            // theCacheObject is null or not inside the cache
            return null;
        }

        // set the list pointers correct
        if (theCacheObject.getNextLruObject() == null) {
            // remove the object from the head pos.
            I_CmsLruCacheObject newHead = theCacheObject.getPreviousLruObject();

            if (newHead != null) {
                // if newHead is null, theCacheObject
                // was the only object in the cache
                newHead.setNextLruObject(null);
            }

            m_listHead = newHead;
        } else if (theCacheObject.getPreviousLruObject() == null) {
            // remove the object from the tail pos.
            I_CmsLruCacheObject newTail = theCacheObject.getNextLruObject();

            if (newTail != null) {
                // if newTail is null, theCacheObject
                // was the only object in the cache
                newTail.setPreviousLruObject(null);
            }

            m_listTail = newTail;
        } else {
            // remove the object from within the list
            theCacheObject.getPreviousLruObject().setNextLruObject(theCacheObject.getNextLruObject());
            theCacheObject.getNextLruObject().setPreviousLruObject(theCacheObject.getPreviousLruObject());
        }

        // update cache stats. and notify the cached object
        decreaseCache(theCacheObject);

        return theCacheObject;
    }

    /**
     * Returns the count of all cached objects.<p>
     *
     * @return the count of all cached objects
     */
    public int size() {

        return m_objectCount;
    }

    /**
     * Returns a string representing the current state of the cache.<p>
     *
     * @return a string representing the current state of the cache
     */
    @Override
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append("max. costs: " + m_maxCacheCosts).append(", ");
        buf.append("avg. costs: " + m_avgCacheCosts).append(", ");
        buf.append("max. costs/object: " + m_maxObjectCosts).append(", ");
        buf.append("costs: " + m_objectCosts).append(", ");
        buf.append("count: " + m_objectCount);
        return buf.toString();
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
        if ((m_maxObjectCosts != -1) && (theCacheObject.getLruCacheCosts() > m_maxObjectCosts)) {
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_CACHE_COSTS_TOO_HIGH_2,
                        new Integer(theCacheObject.getLruCacheCosts()),
                        new Integer(m_maxObjectCosts)));
            }
            remove(theCacheObject);
            return false;
        }

        // set the list pointers correct
        I_CmsLruCacheObject nextObj = theCacheObject.getNextLruObject();
        if (nextObj == null) {
            // case 1: the object is already at the head pos.
            return true;
        }
        I_CmsLruCacheObject prevObj = theCacheObject.getPreviousLruObject();
        if (prevObj == null) {
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
        I_CmsLruCacheObject oldHead = m_listHead;
        if (oldHead != null) {
            oldHead.setNextLruObject(theCacheObject);
            theCacheObject.setNextLruObject(null);
            theCacheObject.setPreviousLruObject(oldHead);
        }
        m_listHead = theCacheObject;

        return true;
    }

    /**
     * Adds a cache object as the new haed to the list of all cached objects in this cache.<p>
     *
     * @param theCacheObject the object being added as the new head to the list of all cached objects
     */
    private void addHead(I_CmsLruCacheObject theCacheObject) {

        // set the list pointers correct
        if (m_objectCount > 0) {
            // there is at least 1 object already in the list
            I_CmsLruCacheObject oldHead = m_listHead;
            oldHead.setNextLruObject(theCacheObject);
            theCacheObject.setPreviousLruObject(oldHead);
            m_listHead = theCacheObject;
        } else {
            // it is the first object to be added to the list
            m_listTail = theCacheObject;
            m_listHead = theCacheObject;
            theCacheObject.setPreviousLruObject(null);
        }
        theCacheObject.setNextLruObject(null);

        // update cache stats. and notify the cached object
        increaseCache(theCacheObject);
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
        m_objectCosts -= theCacheObject.getLruCacheCosts();
        m_objectCount--;
    }

    /**
     * Removes the last recently used objects from the list of all cached objects as long
     * as the costs of all cached objects are higher than the allowed avg. costs of the cache.<p>
     */
    private void gc() {

        I_CmsLruCacheObject currentObject = m_listTail;
        while (currentObject != null) {
            if (m_objectCosts < m_avgCacheCosts) {
                break;
            }
            currentObject = currentObject.getNextLruObject();
            removeTail();
        }
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
        m_objectCosts += theCacheObject.getLruCacheCosts();
        m_objectCount++;
    }

    /**
     * Test if a given object resides inside the cache.<p>
     *
     * @param theCacheObject the object to test
     * @return true if the object is inside the cache, false otherwise
     */
    private boolean isCached(I_CmsLruCacheObject theCacheObject) {

        if ((theCacheObject == null) || (m_objectCount == 0)) {
            // the cache is empty or the object is null (which is never cached)
            return false;
        }

        I_CmsLruCacheObject nextObj = theCacheObject.getNextLruObject();
        I_CmsLruCacheObject prevObj = theCacheObject.getPreviousLruObject();

        if ((nextObj != null) || (prevObj != null)) {
            // the object has either a predecessor or successor in the linked
            // list of all cached objects, so it is inside the cache
            return true;
        }

        // both nextObj and preObj are null
        if ((m_objectCount == 1)
            && (m_listHead != null)
            && (m_listTail != null)
            && m_listHead.equals(theCacheObject)
            && m_listTail.equals(theCacheObject)) {
            // the object is the one and only object in the cache
            return true;
        }

        return false;
    }

    /**
     * Removes the tailing object from the list of all cached objects.<p>
     */
    private synchronized void removeTail() {

        I_CmsLruCacheObject oldTail = m_listTail;
        if (oldTail != null) {
            I_CmsLruCacheObject newTail = oldTail.getNextLruObject();

            // set the list pointers correct
            if (newTail != null) {
                // there are still objects remaining in the list
                newTail.setPreviousLruObject(null);
                m_listTail = newTail;
            } else {
                // we removed the last object from the list
                m_listTail = null;
                m_listHead = null;
            }

            // update cache stats. and notify the cached object
            decreaseCache(oldTail);
        }
    }
}