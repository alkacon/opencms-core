package com.opencms.launcher;

import com.opencms.core.*;
import java.util.*;


class CmsTemplateCache implements I_CmsTemplateCache, I_CmsLogChannels {

  	/** Hashtable to store the cached data */
    private Hashtable templateCache = new Hashtable();
    
    /** Default constructor to create a template cache */
    public CmsTemplateCache() {
        A_OpenCms.log(C_OPENCMS_INFO, "CmsTemplateCache initialized.");
    }

    /** Deletes all documents from the template cache. */
    public void clearCache() {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_INFO, "[CmsTemplateCache] clearing template cache.");
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
	    return (byte[])templateCache.get(key);
     //   return null;
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