/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/util/Attic/I_CmsFlexLruCacheObject.java,v $
 * Date   : $Date: 2002/09/16 12:38:07 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
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

package com.opencms.flex.util;

/**
 * This interface defines the methods which an object being cached by CmsFlexLruCache has to implement.
 * CmsFlexLruCache is organized as a double linked list, that's why objects implementing this interface
 * need getters/setter for the next/previous nodes in the list of all cached objects.
 *
 * @see com.opencms.flex.util.CmsFlexLruCache
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $
 */
public interface I_CmsFlexLruCacheObject {
    
    /** Set the next object in the double linked list of all cached objects. */
    public void setNextLruObject( I_CmsFlexLruCacheObject theNextObject );
    
    /** Get the next object in the double linked list of all cached objects. */
    public I_CmsFlexLruCacheObject getNextLruObject();
    
    /** Set the previous object in the double linked list of all cached objects. */
    public void setPreviousLruObject( I_CmsFlexLruCacheObject thePreviousObject );
     
    /** Get the previous object in the double linked list of all cached objects. */
    public I_CmsFlexLruCacheObject getPreviousLruObject();
    
    /** This method is invoked after the object was added to the cache. */
    public void addToLruCache();
    
    /** This method is invoked after the object was removed to the cache. */
    public void removeFromLruCache();
    
    /** Returns the cache costs of this object, as for example it's byte size. */
    public int getLruCacheCosts();
}
