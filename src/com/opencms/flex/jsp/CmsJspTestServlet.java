/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTestServlet.java,v $
 * Date   : $Date: 2002/12/06 23:16:58 $
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
 *
 * First created on 20. Mai 2002, 17:24
 */

package com.opencms.flex.jsp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is a servlet 
 * used to test basic JSP functionality for OpenCms/Flex.
 * It is especially useful when porting OpenCms/Flex to 
 * new Servlet environments.<p>
 *
 * In case you want to use this class, you must make an entry
 * in the web.xml file of the web application to enable 
 * this servlet.
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.de)
 * @version $Revision: 1.3 $
 */
public class CmsJspTestServlet extends HttpServlet {
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action != null) {
            if ("show".equals(action)) {
                String jsp = req.getParameter("jsp");
                if (jsp != null) {
                    // always using online project for testing
                    String jspuri = com.opencms.flex.CmsJspLoader.getJspUri(jsp, true);
                    req.getRequestDispatcher(jspuri).include(req, res);
                } else {
                    System.err.println("JspTestServlet: Error, action 'show' requires parameter 'jsp'!");
                }
            } else
            if ("forward".equals(action)) {
                String jsp = req.getParameter("jsp");
                if (jsp != null) {
                    // always using online project for testing
                    String jspuri = com.opencms.flex.CmsJspLoader.getJspUri(jsp, true);
                    req.getRequestDispatcher(jspuri).forward(req, res);
                } else {
                    System.err.println("JspTestServlet: Error, action 'forward' requires parameter 'jsp'!");
                }
            }            
        }        
    }
}
