/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsTemplateCache.java,v $
* Date   : $Date: 2003/09/19 14:42:53 $
* Version: $Revision: 1.11 $
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

import org.opencms.cache.CmsLruHashMap;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;


public class CmsTemplateCache implements I_CmsTemplateCache {
    
    /** Hashtable to store the cached data */
    
    private CmsLruHashMap templateCache = new CmsLruHashMap(1000);
    
    /** Default constructor to create a template cache */
    public CmsTemplateCache() {
        if(OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled() ) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Template cache       : Initialized successfully");
        }
    }
    
    /** Deletes all documents from the template cache. */
    public void clearCache() {
        if(OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info("Clearing template cache");
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
            templateCache.remove(key);
        }
        else {
            if(OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("Could not clear key from cache: " + key);
            }
        }
    }
    
    /**
     * Gets a previously cached template with the given key.
     * @param key Key of the requested template.
     * @return byte array with the cached template content or null if no cached value was found.
     */
    public byte[] get(Object key) {
        if(OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info("Getting " + key + " from cache");
        }
        if(key instanceof String) {
            return (byte[])templateCache.get(key);
        }
        else {
            if(OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("Getting " + key + " from cache failed");
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
            return templateCache.get(key) != null;
        } else {
            if(OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn(key + " is not instanceof String");
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
            templateCache.put(key, content);
        }
        else {
            if(OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn(key + " is not instanceof String");
            }
        }
    }
}
