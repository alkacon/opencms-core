/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsServlet.java,v $
* Date   : $Date: 2001/02/20 15:20:17 $
* Version: $Revision: 1.77 $
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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class OpenCmsServlet extends HttpServlet {

    private HttpServlet m_servlet;

    private ServletConfig m_config;

    /**
     * Initialization of the OpenCms HttpServlet.
     * Used instead of a constructor (Overloaded Servlet API method)
     * <p>
     * The connection information for the property database will be read from the configuration
     * file and all resource brokers will be initialized via the initalizer.
     *
     * @param config Configuration of OpenCms.
     * @exception ServletException Thrown when sevlet initalization fails.
     */
    public void init(ServletConfig config) throws ServletException {
        m_config = config;
        String classname = "com.opencms.core.OpenCmsHttpServlet";
        try {
            ClassLoader loader = new CmsClassLoader();
            Class c = loader.loadClass(classname);
            // Now we have to look for the constructor
            m_servlet = (HttpServlet)c.newInstance();
        } catch(Exception e) {
            throw new ServletException(e);
        }
        m_servlet.init(config);
    }

    /**
     * calls the service method of the real servlet.
     */
    public void service(ServletRequest p0, ServletResponse p1) throws ServletException, IOException {
        // test if we must create a new http-servlet
        if (    false){ //m_servlet.shouldReloadClasses()){
            System.err.println("[OpenCmsServlet] there are new Classes,"
                        +" we have to create a new http-servlet to throw away the old ones.");
            System.err.println("[OpenCmsServlet] first destroy the old http-servlet.");
            destroy();
            System.err.println("[OpenCmsServlet] now init the a new http-servlet.");
            init(m_config);
            System.err.println("[OpenCmsServlet] finaly call the service.");
        }
        m_servlet.service(p0, p1);
    }
    /**
     * Destroys all running threads before closing the VM.
     */
    public void destroy() {
        m_servlet.destroy();
    }

}