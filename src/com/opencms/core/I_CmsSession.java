/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsSession.java,v $
* Date   : $Date: 2002/10/30 10:14:04 $
* Version: $Revision: 1.14 $
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

package com.opencms.core;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This interface defines an OpenCms session, a generic session object 
 * that is used by OpenCms and provides methods to access the current users
 * session data.
 * 
 * @author Michael Emmerich
 * @author Andreas Schouten
 * @version $Revision: 1.14 $ $Date: 2002/10/30 10:14:04 $  
 */
public interface I_CmsSession {
    
    /**
     * Gets a value from the session.
     * 
     * @param name the key.
     * @return the object for this key.
     */
    public Object getValue(String name);
    public String[] getValueNames();
    
    /**
     * Puts a value into the session
     * 
     * @param name the key.
     * @param value a object to store the value.
     */
    public void putValue(String name, Object value);
    
    /**
     * Removes a value from the session.
     * 
     * @param name the key for the value to remove.
     */
    public void removeValue(String name);
    
    /**
     * Invalidates the session.
     */
    public void invalidate();
}
