/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsCache.java,v $
 * Date   : $Date: 2000/04/18 14:13:27 $
 * Version: $Revision: 1.2 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.file;

import java.util.*;

/**
 * This class implements a LRU cache storing CmsCachedObjects. It is used to  cache 
 * data read from the File DB.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/04/18 14:13:27 $
 */

class CmsCache
{
	private Hashtable cache;
	private int max_objects;
	
	/**
	 * Constructs a new cache. 
	 * If the cache Size has an illegal value (<= 0) it is set to the default value of 10.
	 * @param cacheSize The size of the new cache
	 */
	public CmsCache(int cacheSize) {
        // illegal cach size? Set Default
		if (cacheSize <=0) {
			max_objects=10;    
		} else {
			max_objects=cacheSize;
		}
		cache = new Hashtable(max_objects);
	}
	
	/**
	 * Put a new key/value pair into the CmsCache.
	 * If the cache is full, the least recently used cache object is removed.
	 * @param key The key for the new object stroed in the cache.
	 * @param value The value of the new object stroed in the cache.
	 */
	public synchronized void put(Object key, Object value) {

		if (cache.size()<max_objects) {
			cache.put(key,new CmsCachedObject(value));
	
		} else {
			removeLRU();
			cache.put(key,new CmsCachedObject(value));		
		}
	}
	
	/**
	 * Removes the least recent used object from the cache.
	 */
	private void removeLRU() {
		long minTimestamp=-1;
		Object keyLRUObject = null;
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
			}
		}
        // finally remove it from cache
		cache.remove(keyLRUObject);  		
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
	public synchronized Object get(Object key) {
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
		} else {
		   return null;
		}
	}
	
	/**
	 * Removes a CmsCachedObject from the cache.
	 * @param key The key of the Object to be removed from the cache.
	 */
	public synchronized void remove(Object key)	{
	  cache.remove(key);
	}
	
	 /**
	 * Deletes all entries in a Cache.
	 * This method is needed because of the problems caused by storing all subfolders and
	 * files from a folder in a seperate subresource cache. Everytime a file or folder is 
	 * updated, read or deleted the subresource cache must be cleared.
	 */
	public synchronized void clear() {
      cache.clear();
    }	
}
