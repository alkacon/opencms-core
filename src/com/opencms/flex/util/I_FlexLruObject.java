/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/util/Attic/I_FlexLruObject.java,v $
 * Date   : $Date: 2002/09/04 15:40:23 $
 * Version: $Revision: 1.1 $
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
 * This interface defines the methods which an object being cached by FlexLruCache has to implement.
 * FlexLruCache is organized as a double linked list, that's why objects implementing this interface
 * need getters/setter for the next/previous nodes in the list of all cached objects.
 *
 * @see com.opencms.flex.util.FlexLruCache
 * @author Thomas Weckert (<a href="mailto:t.weckert@alkacon.com">t.weckert@alkacon.com</a>)
 * @version $Revision: 1.1 $
 */
public interface I_FlexLruObject {
    
    /** Set the next object in the double linked list of all cached objects. */
    public void setNextLruObject( I_FlexLruObject theNextObject );
    
    /** Get the next object in the double linked list of all cached objects. */
    public I_FlexLruObject getNextLruObject();
    
    /** Set the previous object in the double linked list of all cached objects. */
    public void setPreviousLruObject( I_FlexLruObject thePreviousObject );
     
    /** Get the previous object in the double linked list of all cached objects. */
    public I_FlexLruObject getPreviousLruObject();
    
    /** This method is invoked after the object was added to the cache. */
    public void addToLruCache();
    
    /** This method is invoked after the object was removed to the cache. */
    public void removeFromLruCache();
    
    /** Returns the cache costs of this object, as for example it's byte size. */
    public int getLruCacheCosts();
}
