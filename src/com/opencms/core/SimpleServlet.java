/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/SimpleServlet.java,v $
 * Date   : $Date: 2000/08/02 13:34:53 $
 * Version: $Revision: 1.1 $
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

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.file.*;
import com.opencms.util.*;


/**
* This class is the main servlet of the OpenCms system. 
* <p>
* From here, all other operations are invoked.
* It initializes the Servlet and processes all requests send to the OpenCms.
* Any incoming request is handled in multiple steps:
* <ul>
* <li>The requesting user is authenticated and a CmsObject with the user information
* is created. The CmsObject is needed to access all functions of the OpenCms, limited by
* the actual user rights. If the user is not identified, it is set to the default (guest)
* user. </li>
* <li>The requested document is loaded into the OpenCms and depending on its type and the
* users rights to display or modify it, it is send to one of the OpenCms launchers do
* display it. </li>
* <li>
* The document is forwared to a template class which is selected by the launcher and the
* output is generated.
* </li>
* </ul>
* <p>
* The class overloades the standard Servlet methods doGet and doPost to process 
* Http requests.
* 
* @author Michael Emmerich
* @version $Revision: 1.1 $ $Date: 2000/08/02 13:34:53 $  
* 
* */

public class SimpleServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException {	
		
		res.setContentType("text/plain");
		
		PrintWriter writer = res.getWriter();
		
		writer.println("req.getSession(false): " + req.getSession(false));
		
		HttpSession session = req.getSession(true);
		
		writer.println("Your session_id: " + session.getId());
		writer.println("Session is new?: " + session.isNew());
		writer.println("Your session creation time: " + session.getCreationTime());
		writer.println("Your requested session id was: " + req.getRequestedSessionId());
    }
}