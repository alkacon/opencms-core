/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsCache.java,v $
* Date   : $Date: 2001/12/20 10:46:48 $
* Version: $Revision: 1.16 $
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
 * This class implements a LRU cache storing CmsCachedObjects. It is used to  cache
 * data read from the File DB.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.16 $ $Date: 2001/12/20 10:46:48 $
 */

public class CmsCache implements I_CmsConstants {
    private Hashtable cache;
    private Hashtable index;
    private int max_objects;


    /**
     * Constructs a new cache.
     * If the cache Size has an illegal value (<= 0) it is set to the default value of 10.
     * @param cacheSize The size of the new cache
     */
    public CmsCache(int cacheSize) {
        // Illegal cach size? Set Default
        // If cacheSize=0 then cache is disabled (superflous)
        if (cacheSize < 0) {
            max_objects=10;
        } else {
            max_objects=cacheSize;
        }
        cache = new Hashtable(max_objects);
        index = new Hashtable(max_objects);
    }
     /**
     * Deletes all entries in a Cache.
     * This method is needed because of the problems caused by storing all subfolders and
     * files from a folder in a seperate subresource cache. Everytime a file or folder is
     * updated, read or deleted the subresource cache must be cleared.
     */
    public  void clear() {
      cache.clear();
      index.clear();
    }
    /**
     * Gets the contents of a CmsCachedObject form the cache.
     * If the object was found in the cache, it is updated to set its timestamp to the current
     * system time.
     *
     * @param id The id of the Object to be taken from the cache.
     *
     * @return Contents of the CmsCachedObject stored in the cache
     */
    public  Object get(int id) {

        CmsCachedObject cachedObject=null;
        CmsCachedObject ret=null;

        // get key for object
        String key = (String)index.get(new Integer(id));
        if (key == null) {

            return null;
        }

        // get object from cache
        cachedObject=(CmsCachedObject)cache.get(key);

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
     * Gets the contents of a CmsCachedObject form the cache.
     * If the object was found in the cache, it is updated to set its timestamp to the current
     * system time.
     *
     * @param key The key of the Object to be taken from the cache.
     * @param content Flag for getting the file content.
     * @return Contents of the CmsCachedObject stored in the cache
     */
    public  Object get(String key) {
        CmsCachedObject cachedObject=null;
        CmsCachedObject ret=null;

        // get object from cache
        cachedObject=(CmsCachedObject)cache.get(key);
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
     * Gets the Id of an object
     * @param value The object.
     * @return The Id of the object
     */
    private int getId(Object value){
        if(value instanceof CmsFile) {
            return ((CmsFile)value).getResourceId();
        } else if(value instanceof CmsFolder) {
            return ((CmsFolder)value).getResourceId();
        } else if(value instanceof CmsUser) {
            return ((CmsUser)value).getId();
        } else if(value instanceof CmsGroup) {
            return ((CmsGroup)value).getId();
        } else if(value instanceof CmsProject) {
            return ((CmsProject)value).getId();
        } else {
            return C_UNKNOWN_ID;
        }
    }
    /**
     * Gets the SringKey of an object
     * @param value The object.
     * @return The StringKey of the object
     */
    private String getStrKey(Object value){
        if(value instanceof CmsFile) {
            return C_FILE+((CmsFile)value).getProjectId()+((CmsFile)value).getAbsolutePath();
        } else if(value instanceof CmsFolder) {
            return C_FOLDER+((CmsFolder)value).getProjectId()+((CmsFolder)value).getAbsolutePath();
        } else if(value instanceof CmsUser) {
            return ((CmsUser)value).getName();
        } else if(value instanceof CmsGroup) {
            return ((CmsGroup)value).getName();
        } else if(value instanceof CmsProject) {
            return "p"+((CmsProject)value).getId();
        } else {
            return null;
        }
    }
    /**
     * Put a new key/value pair into the CmsCache.
     * If the cache is full, the least recently used cache object is removed.
     * Don't use this for projects.
     * @param key The key for the new object stroed in the cache.
     * @param value The value of the new object stroed in the cache.
     */
    public void put(int key, Object value) {

        String strKey = getStrKey(value);
        if (strKey != null){
            if (cache.size()<max_objects) {
                cache.put(strKey,(new CmsCachedObject(value)).clone());

            } else {
                removeLRU();
                cache.put(strKey,(new CmsCachedObject(value)).clone());
            }
            index.put(new Integer(key),strKey);
        }
    }
    /**
     * Put a new key/value pair into the CmsCache.
     * If the cache is full, the least recently used cache object is removed.
     * @param strKey The key for the new object stroed in the cache.
     * @param value The value of the new object stroed in the cache.
     */
    public void put(String strKey, Object value) {
        if (cache.size() < max_objects) {
            cache.put(strKey,(new CmsCachedObject(value)).clone());

        } else {
            removeLRU();
            cache.put(strKey,(new CmsCachedObject(value)).clone());
        }
        int id = getId(value);
        if (id != C_UNKNOWN_INT){
            index.put(new Integer(id),strKey);
        }
    }
    /**
     * Removes a CmsCachedObject from the cache.
     * @param id The Id of the Object to be removed from the cache.
     */
    public  void remove(int id) {
        if (max_objects > 0)
        {
            String key = (String)index.get(new Integer(id));
            index.remove(new Integer(id));
            cache.remove(key);
        }
    }
    /**
     * Removes a CmsCachedObject from the cache.
     * @param key The key of the Object to be removed from the cache.
     */
    public  void remove(String key) {
        if (max_objects > 0)
        {
            cache.remove(key);
        }
    }
    /**
     * Removes the least recent used object from the cache.
     */
    private void removeLRU() {
        long minTimestamp=-1;
        Object keyLRUObject = null;
        int indexKeyLRU = C_UNKNOWN_ID;
        // get the keys of all cache objets
        Enumeration keys = cache.keys();
        while (keys.hasMoreElements()) {
            Object key= keys.nextElement();
            CmsCachedObject value = (CmsCachedObject) cache.get(key);
            //actual object with a older timestamp than the current oldest?
            if ((minTimestamp == -1) || (minTimestamp > value.getTimestamp())) {
                // this is the new least recent used cache object
                minTimestamp = value.getTimestamp();
                keyLRUObject= key;
                indexKeyLRU = getId(value);
            }
        }
        // finally remove it from cache and if necessary from the index
        if (max_objects > 0)
        {
            cache.remove(keyLRUObject);
            index.remove(new Integer(indexKeyLRU));
        }
    }
    /**
    * Returnes the number of Elements in the cache
    **/
    public int size() {
        return cache.size();
    }
}
