package com.opencms.core;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsSession.java,v $
 * Date   : $Date: 2000/08/28 14:02:39 $
 * Version: $Revision: 1.10 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class implements a cms-session. This session should be used instead of 
 * the HttpSession. In this session there will be implemented some mechanism for
 * session-failover in distributed-server environments.
 * 
 * @author Michael Emmerich
 * @author Andreas Schouten
 * @version $Revision: 1.10 $ $Date: 2000/08/28 14:02:39 $  
 */
public interface I_CmsSession  {

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
}
