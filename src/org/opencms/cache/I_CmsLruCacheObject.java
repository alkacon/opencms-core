/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cache/I_CmsLruCacheObject.java,v $
 * Date   : $Date: 2005/06/22 09:13:16 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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

/**
 * Defines the methods which an object being cached by CmsFlexLruCache must implement.<p>
 * 
 * CmsFlexLruCache is organized as a double linked list, that's why objects implementing this interface
 * need getters/setter for the next/previous nodes in the list of all cached objects.<p>
 *
 * @see CmsLruCache
 * 
 * @author Thomas Weckert 
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 6.0.0
 */
public interface I_CmsLruCacheObject {
    
    /** 
     * Set the next object in the double linked list of all cached objects.<p>
     *
     * @param theNextObject the next object
     */
    void setNextLruObject(I_CmsLruCacheObject theNextObject);
    
    /** 
     * Returns the next object in the double linked list of all cached objects.<p>
     *
     * @return the next object in the double linked list of all cached objects
     */
    I_CmsLruCacheObject getNextLruObject();
    
    /** 
     * Set the previous object in the double linked list of all cached objects.<p>
     * 
     * @param thePreviousObject the previous object
     */
    void setPreviousLruObject(I_CmsLruCacheObject thePreviousObject);
     
    /** 
     * Returns the previous object in the double linked list of all cached objects.<p>
     * 
     * @return the previous object in the double linked list of all cached objects 
     */
    I_CmsLruCacheObject getPreviousLruObject();
    
    /** 
     * Invoked after an object was added to the cache.<p>
     */
    void addToLruCache();
    
    /** 
     * Invoked after the object was removed to the cache.<p>
     */
    void removeFromLruCache();
    
    /** 
     * Returns the cache costs of this object, as for example it's byte size.<p>
     * 
     * @return the cache costs of this object
     */
    int getLruCacheCosts();
    
    /**
     * Returns the Object value.<p> 
     * 
     * @return the Object value
     */
    Object getValue();
}
