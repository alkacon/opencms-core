/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/OpenCmsServlet.java,v $
 * Date   : $Date: 2003/09/25 16:07:45 $
 * Version: $Revision: 1.5 $
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

package org.opencms.main;

import org.opencms.staticexport.CmsStaticExportData;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This the main servlet of the OpenCms system.<p>
 * 
 * From here, all other operations are invoked.
 * Any incoming request is handled in multiple steps:
 * 
 * <ol><li>The requesting user is authenticated and a CmsObject with the user information
 * is created. The CmsObject is used to access all functions of OpenCms, limited by
 * the authenticated users permissions. If the user is not identified, it is set to the default (guest)
 * user.</li>
 * 
 * <li>The requested document is loaded into OpenCms and depending on its type 
 * (and the users persmissions to display or modify it), 
 * it is send to one of the OpenCms loaders do be processed.</li>
 * 
 * <li>
 * The loader will then decide what to do with the contents of the 
 * requested document. In case of an XMLTemplate the template mechanism will 
 * be started, in case of a JSP the JSP handling mechanism is invoked, 
 * in case of an image (or other static file) this will simply be returned etc.
 * </li></ol>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 */
public class OpenCmsServlet extends HttpServlet {
    
    /**
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public synchronized void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            new OpenCmsCore(config.getServletContext());
        } catch (CmsInitException e) {
            // log output will be provided by OpenCms
        }        
    }

    /**
     * Handler for OpenCms error requests.<p>
     * 
     * @param req the current request
     * @param res the current response 
     * @throws ServletException
     * @throws IOException in case an error occurs
     */
    private void handleError(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = req.getPathInfo();     
        int error = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        try {
            error = Integer.parseInt(path.substring("/handle".length()));
        } catch (NumberFormatException e) {
            // use default error
        }
        switch (error) {
            case 404:
                CmsObject cms = null;            
                CmsStaticExportData exportData = null;
                try {
                    cms = OpenCmsCore.getInstance().initCmsObject(req, res, OpenCms.getDefaultUsers().getUserExport(), null);            
                    exportData = OpenCms.getStaticExportManager().getExportData(req, cms);
                } catch (CmsException e) {
                    // unlikley to happen 
                    if (OpenCms.getLog(this).isWarnEnabled()) {                    
                        OpenCms.getLog(this).warn("Error initializing CmsObject in error handler for '" + path + "' code " + error, e);
                    }
                }
                if (exportData != null) {
                    synchronized (this) {
                        try {
                            OpenCms.getStaticExportManager().export(req, res, cms, exportData);
                        } catch (Throwable t) {
                            if (OpenCms.getLog(this).isWarnEnabled()) {                    
                                OpenCms.getLog(this).warn("Error exporting " + exportData, t);
                            }
                            res.sendError(HttpServletResponse.SC_NOT_FOUND);
                        }
                    }
                } else {
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                }    
                break;
            default:
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } 
    }
    
    /**
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy() {
    }

    /**
     * OpenCms servlet main request handling method.<p>
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getPathInfo();
        if ((path != null) && path.startsWith("/handle")) {
            handleError(req, res);                                     
        } else {
            OpenCmsCore.getInstance().showResource(req, res);
        }
    }

    /**
     * OpenCms servlet request handling method, 
     * will just call {@link #doGet(HttpServletRequest, HttpServletResponse)}.<p>
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {            
        doGet(req, res);
    }
    

}

