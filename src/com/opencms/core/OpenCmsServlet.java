/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsServlet.java,v $
* Date   : $Date: 2001/02/12 08:52:13 $
* Version: $Revision: 1.75 $
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
        String classname = "com.opencms.core.OpenCmsHttpServlet";
        try {
            ClassLoader loader = new CmsClassLoader();
            Class c = loader.loadClass(classname);
            //Class c = CmsTemplateClassManager.class.getClassLoader().loadClass(classname);
            // Now we have to look for the constructor
            m_servlet = (HttpServlet)c.newInstance();
        } catch(Exception e) {
            String errorMessage = null;

            // Construct error message for the different exceptions
            if(e instanceof ClassNotFoundException) {
                errorMessage = "XXXCould not load template class " + classname + ". " + e.getMessage();
            }
            else {
                if(e instanceof InstantiationException) {
                    errorMessage = "XXXCould not instantiate template class " + classname;
                }
                else {
                    if(e instanceof NoSuchMethodException) {
                        errorMessage = "XXXCould not find constructor of template class " + classname;
                    }
                    else {
                        errorMessage = "XXXUnknown error while getting instance of template class " + classname;
                    }
                }
            }
            /*if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsTemplateClassManager] " + errorMessage);
            }*/
            throw new ServletException(e);
            //throw new CmsException(errorMessage, CmsException.C_CLASSLOADER_ERROR, e);
        }
        /*
        try{
            m_servlet = (HttpServlet) com.opencms.template.CmsTemplateClassManager.getClassInstance(null, "com.opencms.core.OpenCmsHttpServlet" );
        }catch(Exception e){
            throw new ServletException(e);
        }*/
        m_servlet.init(config);
    }

    /**
     * calls the service method of the real servlet.
     */
    public void service(ServletRequest p0, ServletResponse p1) throws ServletException, IOException {
        m_servlet.service(p0, p1);
    }
    /**
     * Destroys all running threads before closing the VM.
     */
    public void destroy() {
        m_servlet.destroy();
    }

}