/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsResponseHttpServlet.java,v $
 * Date   : $Date: 2000/07/21 09:48:38 $
 * Version: $Revision: 1.7 $
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
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Implementation of the CmsResponse interface.
 * 
 * This implementation uses a HttpServletResponse as original response to create a
 * CmsResponseHttpServlet.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.7 $ $Date: 2000/07/21 09:48:38 $  
 */
public class CmsResponseHttpServlet implements I_CmsConstants,  
                                               I_CmsResponse{ 
	
	private static String C_LAST_MODIFIED = "Last-Modified";
    
     /**
     * The original response.
     */
    private HttpServletResponse m_res;
    
     /**
     * The original request.
     */
    private HttpServletRequest m_req;
    
    /**
     * The type of this CmsResponset.
     */
    private int m_type=C_RESPONSE_HTTP;
    
    
     /** 
     * Constructor, creates a new CmsResponseHttpServlet object.
     * It is nescessary to give the HttpServletRequest as well, because it is needed
     * to transform the CmsRedirect to a real Http redirect.
     * 
     * @param req The original HttpServletRequest used to create this CmsRequest.
     * @param res The original HttpServletResponse used to create this CmsResponse.
     */
     CmsResponseHttpServlet (HttpServletRequest req, HttpServletResponse res) {
        m_res=res;
        m_req=req;
    }

    /**
     * Returns an OutputStream for writing the response data. 
     * 
     * @return OutputStream for writing data.
     * @exception Throws IOException if an error occurs.
     */
    public OutputStream getOutputStream()
        throws IOException {
   
        return m_res.getOutputStream();
    }
    
    /**
     * Sets the length of the content being returned by the server.
     * 
     * @param len Number of bytes to be returned by the response.
     */
    public void setContentLength(int len) {
        m_res.setContentLength(len);        
    }
    
    /**
     * Sets the content type of the response to the specified type.
     * 
     * @param type The contnent type of the response.
     */
    public void setContentType(String type) {
        m_res.setContentType(type);
    }
    
    /**
     * Sets the error code that is returnd by the response. The error code is specified
     * by a numeric value.
     * 
     * @param code The error code to be set.
     * @exception Throws IOException if an error occurs.
     */
    public void sendError(int code) 
        throws IOException {
        m_res.sendError(code);
    }
    
    /**
     * Sets the error code and a additional message that is returnd by the response. 
     * The error code is specified by a numeric value.
     * 
     * @param code The error code to be set.
     * @param msg Additional error message.
     * @exception Throws IOException if an error occurs.
     */
    public void sendError(int code, String msg)
        throws IOException{
        m_res.sendError(code,msg);
    }
    
     /**
     * Sets a redirect to send the responst to. 
     * The original HttpServletResponse redirect is used here. Additional information
     * about the servlets location is taken from the original HttpServletRequest.
     * 
     * @param location The location the response is send to.
     * @param msg Additional error message.
     * @exception Throws IOException if an error occurs.
     */
    public void sendCmsRedirect(String location)
        throws IOException {
        String hostName = m_req.getScheme() + "://" + m_req.getHeader("HOST");       
        String servlet = m_req.getServletPath();
        m_res.sendRedirect(hostName + servlet + location);
    }
    
    /**
     * Sets the last modified header-field in the response.
     * 
     * @param time The last-modified time.
     */
	public void setLastModified(long time) {
		m_res.setDateHeader(C_LAST_MODIFIED, time);
	}
	
    /**
     * Returns the type of the response that was used to create the CmsResponse.
     * The returned int must be one of the constants defined above in this interface.
     * 
     * @return The type of the CmsResponse.
     */
    public int getOriginalResponseType() {
        return m_type;
    }

    /**
     * Returns the original response that was used to create the CmsResponse.
     * 
     * @return The original response of the CmsResponse.
     */
    public Object getOriginalResponse() {
        return m_res;
    }
    
}
