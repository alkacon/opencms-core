/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsSession.java,v $
 * Date   : $Date: 2000/07/20 12:37:34 $
 * Version: $Revision: 1.9 $
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

package com.opencms.core;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class implements a cms-session. This session should be used instead of 
 * the HttpSession. In this session there will be implemented some mechanism for
 * session-failover in distributed-server environments.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.9 $ $Date: 2000/07/20 12:37:34 $  
 */
public class CmsSession {
	
	/**
	 * The original HttpSession
	 */
	private HttpSession m_session;

	/**
	 * Constructs a new CmsSession on base of a HttpSession.
	 * 
	 * @param originalSession the original session to use.
	 */
	public CmsSession(HttpSession originalSession) {
		m_session = originalSession;
	}
	
	public void putValue(String name, Object value) {
		m_session.putValue(name, value);
	}
	
	public Object getValue(String name) {
		return m_session.getValue(name);
	}
	
	public void removeValue(String name) {
		m_session.removeValue(name);
	}
}
