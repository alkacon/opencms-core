/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsTemplateCache.java,v $
 * Date   : $Date: 2000/04/04 09:59:54 $
 * Version: $Revision: 1.8 $
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

package com.opencms.launcher;

import com.opencms.core.*;
import java.util.*;


class CmsTemplateCache implements I_CmsTemplateCache, I_CmsLogChannels {

    /** Hashtable to store the cached data */
    private Hashtable templateCache = new Hashtable();
    
    /** Default constructor to create a template cache */
    public CmsTemplateCache() {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] Initialized successfully.");
        }
    }

    /** Deletes all documents from the template cache. */
    public void clearCache() {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] clearing template cache.");
        }
        templateCache.clear();
	}
    
    /**
     * Deletes the document with the given key from the
     * template cache.
     * @param key Key of the template that should be deleted.
     */    
    public void clearCache(Object key) {
        templateCache.remove(key);
    }

    /**
     * Gets a previously cached template with the given key.
     * @param key Key of the requested template.
     * @return byte array with the cached template content or null if no cached value was found.
     */    
	public byte[] get(Object key) {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] Getting " + key + " from cache.");            
        }
        return (byte[])templateCache.get(key);
    }
    
    /**
     * Stores a template content in the cache using the given key.
     * @param key Key that should be used to store the template
     * @param content Template content to store.
     */
    public void put(Object key, byte[] content) {
         templateCache.put(key, content);
    }
    
    /**
     * Checks if there exists a cached template content for
     * a given key.
     * @param key Key that should be checked.
     * @return <EM>true</EM> if a cached content was found, <EM>false</EM> otherwise.
     */
    public boolean has(Object key) {
        return templateCache.containsKey(key);
       // return false;
    }
}