package com.opencms.launcher;

import java.util.*;

/**
 * Common interface for the OpenCms template cache.
 * Classes and for a customized template cache have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/13 17:51:00 $
 */
public interface I_CmsTemplateCache { 

    /** Deletes all documents from the template cache. */
    public void clearCache();
    
    /**
     * Deletes the document with the given key from the
     * template cache.
     * @param key Key of the template that should be deleted.
     */
    public void clearCache(Object key);
	
    /**
     * Gets a previously cached template with the given key.
     * @param key Key of the requested template.
     * @return byte array with the cached template content or null if no cached value was found.
     */
    public byte[] get(Object key);

    /**
     * Stores a template content in the cache using the given key.
     * @param key Key that should be used to store the template
     * @param content Template content to store.
     */
    public void put(Object key, byte[] content);
    
    /**
     * Checks if there exists a cached template content for
     * a given key.
     * @param key Key that should be checked.
     * @return <EM>true</EM> if a cached content was found, <EM>false</EM> otherwise.
     */
    public boolean has(Object key);
}
