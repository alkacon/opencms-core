/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsTemplateCache.java,v $
* Date   : $Date: 2003/07/12 11:29:22 $
* Version: $Revision: 1.1 $
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

package com.opencms.template;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.flex.util.CmsLruHashMap;

public class CmsTemplateCache implements I_CmsTemplateCache,I_CmsLogChannels {
    
    /** Hashtable to store the cached data */
    
    private CmsLruHashMap templateCache = new CmsLruHashMap(1000);
    
    /** Default constructor to create a template cache */
    public CmsTemplateCache() {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] Initialized successfully.");
        }
    }
    
    /** Deletes all documents from the template cache. */
    public void clearCache() {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
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
        if(key instanceof String) {
            templateCache.remove((String)key);
        }
        else {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] clearCache failed: " + key);
            }
        }
    }
    
    /**
     * Gets a previously cached template with the given key.
     * @param key Key of the requested template.
     * @return byte array with the cached template content or null if no cached value was found.
     */
    public byte[] get(Object key) {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] Getting " + key + " from cache.");
        }
        if(key instanceof String) {
            return (byte[])templateCache.get((String)key);
        }
        else {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] Getting " + key + " from cache failed.");
            }
            return null;
        }
    }
    
    /**
     * Checks if there exists a cached template content for
     * a given key.
     * @param key Key that should be checked.
     * @return <EM>true</EM> if a cached content was found, <EM>false</EM> otherwise.
     */
    public boolean has(Object key) {
        if(key instanceof String) {
            return templateCache.get((String)key) != null;
        }
        else {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] " + key + " is not instanceof String.");
            }
            return false;
        }
    }
    
    /**
     * Stores a template content in the cache using the given key.
     * @param key Key that should be used to store the template
     * @param content Template content to store.
     */
    public void put(Object key, byte[] content) {
        if(key instanceof String) {
            templateCache.put((String)key, content);
        }
        else {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CACHE, "[CmsTemplateCache] " + key + " is not instanceof String.");
            }
        }
    }
}
