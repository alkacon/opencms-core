/*
* File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.legacy/src/com/opencms/core/Attic/I_CmsResponse.java,v $
* Date   : $Date: 2005/05/16 17:45:08 $
* Version: $Revision: 1.1 $
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

import java.io.*;

import javax.servlet.http.HttpServletResponse;

/**
 * This interface defines an OpenCms response, a generic response object that 
 * is used by OpenCms and provides methods to send processed data back to 
 * the requesting user.<p>
 * 
 * Implementations of this interface use an existing response 
 * (e.g. HttpServletResponse) to initialize an I_CmsResponse. 
 * 
 * @author Michael Emmerich
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.1 $ $Date: 2005/05/16 17:45:08 $  
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public interface I_CmsResponse {

    /** Request context attribute name. */  
    String C_CMS_RESPONSE = "__I_CmsResponse";
    
    /**
     * Returns the original response that was used to create the CmsResponse.
     * 
     * @return The original response of the CmsResponse.
     */
    HttpServletResponse getOriginalResponse();
    
    /**
     * Returns the type of the response that was used to create the CmsResponse.
     * The returned int must be one of the constants defined above in this interface.
     * 
     * @return The type of the CmsResponse.
     */
    int getOriginalResponseType();
    
    /**
     * Returns an OutputStream for writing the response data. 
     * 
     * @return OutputStream for writing data.
     * @throws IOException if something goes wrong
     */
    OutputStream getOutputStream() throws IOException;
    
    /**
     * Check if the output stream was requested previously.
     * @return <code>true</code> if getOutputStream() was called, <code>false</code> otherwise.
     */
    boolean isOutputWritten();
    
    /**
     * Check if the current request was redirected. In this case, the
     * servlet must not write any bytes to the output stream.
     * @return <code>true</code> if the request is redirected, <code>false</code> otherwise.
     */
    boolean isRedirected();
    
    /**
     * Sets a redirect to send the response to.<p> 
     * 
     * @param location the location the response is redirected to
     * @throws IOException if something goes wrong
     */
    void sendCmsRedirect(String location) throws IOException;
    
    /**
     * Sets the error code that is returnd by the response. The error code is specified
     * by a numeric value.
     * 
     * @param code The error code to be set.
     * @throws IOException if something goes wrong
     */
    void sendError(int code) throws IOException;
    
    /**
     * Sets the error code and a additional message that is returnd by the response. 
     * The error code is specified by a numeric value.
     * 
     * @param code The error code to be set.
     * @param msg Additional error message.
     * @throws IOException if something goes wrong
     */
    void sendError(int code, String msg) throws IOException;
    
    /**
     *  Helper function for a redirect to the cluster url. 
     * 
     * @param location a complete url, eg. <code>http://servername/servlets/opencms/index.html</code> 
     * @throws IOException if something goes wrong
     */
    void sendRedirect(String location) throws IOException;
    
    /**
     * Sets the length of the content being returned by the server.
     * 
     * @param len Number of bytes to be returned by the response.
     */
    void setContentLength(int len);
    
    /**
     * Sets the content type of the response to the specified type.
     * 
     * @param type The contnent type of the response.
     */
    void setContentType(String type);
    
    /**
     * Returns the content type of the response which has previously
     * been set using {@link #setContentType(String)}.
     * 
     * @return the content type of the response.
     */
    String getContentType();
        
    /**
     * Sets a header-field in the response.
     * 
     * @param key The key for the header.
     * @param value The value for the header.
     */
    void setHeader(String key, String value);

    /**
     * Add a header-field in the response.
     * 
     * @param key The key for the header.
     * @param value The value for the header.
     */
    void addHeader(String key, String value);
    
    /**
     * Sets the last modified header-field in the response.
     * 
     * @param time The last-modified time.
     */
    void setLastModified(long time);
    
    /**
     * Checks, if the header was set already.
     * @param key the header-key to check.
     * @return true if the header was set before else false.
     */
    boolean containsHeader(String key);
}
