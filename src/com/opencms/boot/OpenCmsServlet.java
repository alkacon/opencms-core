/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/OpenCmsServlet.java,v $
* Date   : $Date: 2001/09/07 12:15:18 $
* Version: $Revision: 1.3 $
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

package com.opencms.boot;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Vector;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class OpenCmsServlet extends HttpServlet {

    private HttpServlet m_servlet;

    private CmsClassLoader m_loader;

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

        String base = config.getInitParameter("opencms.home");
            System.err.println("BASE: " + config.getServletContext().getRealPath("/"));
            System.err.println("BASE2: " + System.getProperty("user.dir"));
        if(base == null || "".equals(base)) {
            System.err.println("No OpenCms home folder given. Trying to guess...");
            base = CmsMain.searchBaseFolder(config.getServletContext().getRealPath("/"));
            if(base == null || "".equals(base)) {
                throw new ServletException("OpenCms base folder could not be guessed. Please define init parameter \"opencms.home\" in servlet engine configuration.");
            }
        }

        base = CmsBase.setBasePath(base);

        try {
            m_loader = new CmsClassLoader();
            // Search for jar files in the oclib folder.
            CmsMain.collectRepositories(base, m_loader);
            Class c = m_loader.loadClass(classname);
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
        if (m_loader.shouldReload()){
            /* Switching off this feature because of problems in
               running systems. TODO: refactore the automatic reload of classes.
               Now you have to restart your system manually after updating classes.


            System.err.println("[OpenCmsServlet] there are new Classes,"
                        +" we have to create a new http-servlet to throw away the old ones.");
            System.err.println("[OpenCmsServlet] first destroy the old http-servlet.");
            destroy();
            System.err.println("[OpenCmsServlet] now init the a new http-servlet.");
            init(m_config);
            System.err.println("[OpenCmsServlet] finaly call the service.");

               Log-message about "should reload classes"
            */

            System.err.println("[OpenCmsServlet] there are new Classes, you should restart the system...");

        }
        m_servlet.service(p0, p1);
    }
    /**
     * Destroys all running threads before closing the VM.
     */
    public void destroy() {
        m_servlet.destroy();
    }

    /**
     * Gives the usage-information to the user.
     */
    private static void usage() {
        System.err.println("Usage: java com.opencms.core.OpenCmsServlet properties-file");
    }

}