/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsResponse.java,v $
 * Date   : $Date: 2000/02/15 17:53:48 $
 * Version: $Revision: 1.2 $
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

/**
 * This interface defines a CmsResponse.
 * 
 * The CmsResponse is a genereic response object that is used in the CmsObject provinding
 * methods to send data to the response.
 * 
 * Implementations of this interface use an existing responset (e.g. HttpServletResponse) to
 * initialize a CmsResponset. 
 * 
 * @author Michael Emmerich
 * @author Alexander Kandzior
 * @version $Revision: 1.2 $ $Date: 2000/02/15 17:53:48 $  
 */
public interface I_CmsResponse { 
    

    /**
     * Returns an OutputStream for writing the response data. 
     * 
     * @return OutputStream for writing data.
     * @exception Throws IOException if an error occurs.
     */
    public OutputStream getOutputStream()
        throws IOException;
    
    /**
     * Sets the length of the content being returned by the server.
     * 
     * @param len Number of bytes to be returned by the response.
     */
    public void setContentLength(int len);
    
    /**
     * Sets the content type of the response to the specified type.
     * 
     * @param type The contnent type of the response.
     */
    public void setContentType(String type);
    
    /**
     * Sets the error code that is returnd by the response. The error code is specified
     * by a numeric value.
     * 
     * @param code The error code to be set.
     * @exception Throws IOException if an error occurs.
     */
    public void sendError(int code) 
        throws IOException;
    
    /**
     * Sets the error code and a additional message that is returnd by the response. 
     * The error code is specified by a numeric value.
     * 
     * @param code The error code to be set.
     * @param msg Additional error message.
     * @exception Throws IOException if an error occurs.
     */
    public void sendError(int code, String msg)
        throws IOException;
    
    /**
     * Sets a redirect to send the responst to. 
     * 
     * @param location The location the response is send to.
     * @param msg Additional error message.
     * @exception Throws IOException if an error occurs.
     */
    public void sendCmsRedirect(String location)
        throws IOException;
    
    /**
     * Returns the type of the response that was used to create the CmsResponse.
     * The returned int must be one of the constants defined above in this interface.
     * 
     * @return The type of the CmsResponse.
     */
    public int getOriginalResponseType();

    /**
     * Returns the original response that was used to create the CmsResponse.
     * 
     * @return The original response of the CmsResponse.
     */
    public Object getOriginalResponse();
    
}
