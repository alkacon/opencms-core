/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/I_CmsTemplateCache.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
* Version: $Revision: 1.4 $
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


/**
 * Common interface for the OpenCms template cache.
 * Classes and for a customized template cache have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2005/02/18 15:18:52 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
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
     * Checks if there exists a cached template content for
     * a given key.
     * @param key Key that should be checked.
     * @return <EM>true</EM> if a cached content was found, <EM>false</EM> otherwise.
     */
    public boolean has(Object key);
    
    /**
     * Stores a template content in the cache using the given key.
     * @param key Key that should be used to store the template
     * @param content Template content to store.
     */
    public void put(Object key, byte[] content);
}
