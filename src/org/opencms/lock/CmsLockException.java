/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/CmsLockException.java,v $
 * Date   : $Date: 2003/09/17 13:04:46 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.lock;

import com.opencms.core.CmsException;

/**
 * Signals that a particular action was invoked on resource with an insufficient lock state.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.7 $ $Date: 2003/09/17 13:04:46 $
 * @since 5.1.4
 */
public class CmsLockException extends CmsException {
    
    // the allowed type range for this exception is >=200 and <300
    
    /** A resource is locked, but a particular action requires the resource to be unlocked */
    public static final int C_RESOURCE_LOCKED = 200;
    
    /** A resource is locked by the current user, but a particular action requires that the resource is unlocked */
    public static final int C_RESOURCE_LOCKED_BY_CURRENT_USER = 202;
    
    /** A resource is locked by a user different from the current user, but a particular action requires that the resource is locked by the current user */
    public static final int C_RESOURCE_LOCKED_BY_OTHER_USER = 203;
    
    /** A resource has an inherited lock of a parent folder, but a particular action requires a non-inherited lock */
    public static final int C_RESOURCE_LOCKED_INHERITED = 204;
    
    /** A resource has a non-exclusive lock, but a particular action requires an exclusive lock */
    public static final int C_RESOURCE_LOCKED_NON_EXCLUSIVE = 205;
    
    /** A resource is unlocked, but a particular action requires the resource to be locked */
    public static final int C_RESOURCE_UNLOCKED = 201;
    
    /**
     * Default constructor for a CmsLockException.<p>
     */
    public CmsLockException() {
        super();
    }
    
    /**
     * Constructs a CmsLockException with the specified detail message and type.<p>
     * 
     * @param message the detail message
     * @param type the type of the exception
     */
    public CmsLockException(String message, int type) {
        super(message, type, null);
    }

}
