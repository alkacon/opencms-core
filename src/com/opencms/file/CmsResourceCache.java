/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceCache.java,v $
* Date   : $Date: 2001/07/31 15:50:13 $
* Version: $Revision: 1.2 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
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

package com.opencms.file;

import java.util.*;
import com.opencms.core.*;
/**
 * This class is used to cache resources read from the File DB.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.2 $ $Date: 2001/07/31 15:50:13 $
 */

public class CmsResourceCache implements I_CmsConstants {
    private Hashtable cache;
    private int max_objects;


    /**
     * Constructs a new cache.
     * If the cache Size has an illegal value (<= 0) it is set to the default value of 10.
     * @param cacheSize The size of the new cache
     */
    public CmsResourceCache(int cacheSize) {
        // Illegal cache size? Set Default
        // If cacheSize=0 then cache is disabled (superflous)
        if (cacheSize < 0) {
            max_objects=10;
        } else {
            max_objects=cacheSize;
        }
        cache = new Hashtable(max_objects);
    }
     /**
     * Deletes all entries in a Cache.
     * This method is needed because of the problems caused by storing all subfolders and
     * files from a folder in a seperate subresource cache. Everytime a file or folder is
     * updated, read or deleted the subresource cache must be cleared.
     */
    public  void clear() {
      cache.clear();
    }

    /**
     * Gets the contents of a CmsCachedObject form the cache.
     * If the object was found in the cache, it is updated to set its timestamp to the current
     * system time.
     *
     * @param resource The key of the Object to be taken from the resourcecache.
     * @param projectId The key of the Object to be taken from the cache in the resourcecache.
     * @param content Flag for getting the file content.
     * @return Contents of the CmsCachedObject stored in the cache
     */
    public  Object get(String resourcename, int projectId) {
        CmsCachedObject cacheValue = null;
        Hashtable resCache = null;
        CmsCachedObject cachedObject=null;
        CmsCachedObject ret=null;

        // get the hashtable with all objects for this resource
        cacheValue = (CmsCachedObject)cache.get(resourcename);
        if(cacheValue != null){
            resCache = (Hashtable)cacheValue.getContents();
        }
        // get object from cache
        if(resCache != null){
            cachedObject=(CmsCachedObject)resCache.get(""+projectId);
        }
        // not empty?
        if (cachedObject != null) {
            // update  timestamp
            cachedObject.setTimestamp();
            ret=(CmsCachedObject)cachedObject.clone();
            return (((CmsCachedObject)ret).getContents());
            //return null;

        } else {
           return null;
        }
    }

    /**
     * Put a new key/value pair into the CmsCache.
     * If the cache is full, the least recently used cache object is removed.
     * @param resourcename The key for the new object stored in the cache.
     * @param projectId The key for the object stored in the hashtable in the cache
     * @param value The value of the new object stored in the cache.
     */
    public void put(String resourceName, int projectId, Object value) {
        if (cache.size() > max_objects) {
            removeLRU(cache);
        }
        Hashtable innerCache = null;
        CmsCachedObject cacheValue = (CmsCachedObject)cache.get(resourceName);
        if (cacheValue != null) {
            innerCache = (Hashtable)cacheValue.getContents();
        }
        if (innerCache == null){
            innerCache = new Hashtable(max_objects);
            innerCache.put(""+projectId, new CmsCachedObject(value));
        } else {
            if(innerCache.size() > max_objects){
                removeLRU(innerCache);
            }
            innerCache.put(""+projectId, new CmsCachedObject(value));
        }
        cache.put(resourceName, new CmsCachedObject(innerCache));
    }

    /**
     * Removes a CmsCachedObject from the cache.
     * @param resourcename The key of the Object to be removed from the cache.
     */
    public  void remove(String resourcename)    {
        if (max_objects > 0)
        {
            cache.remove(resourcename);
        }
    }
    /**
     * Removes the least recent used object from the cache.
     */
    private void removeLRU(Hashtable theCache) {
        long minTimestamp=-1;
        Object keyLRUObject = null;
        int indexKeyLRU = C_UNKNOWN_ID;
        // get the keys of all cache objets
        Enumeration keys = theCache.keys();
        while (keys.hasMoreElements()) {
            Object key= keys.nextElement();
            CmsCachedObject value = (CmsCachedObject) theCache.get(key);
            //actual object with a older timestamp than the current oldest?
            if ((minTimestamp == -1) || (minTimestamp > value.getTimestamp())) {
                // this is the new least recent used cache object
                minTimestamp = value.getTimestamp();
                keyLRUObject= key;
            }
        }
        // finally remove it from cache and if necessary from the index
        if (max_objects > 0)
        {
            theCache.remove(keyLRUObject);
        }
    }
    /**
    * Returnes the number of Elements in the cache
    **/
    public int size() {
        return cache.size();
    }
}