/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsRequest.java,v $
* Date   : $Date: 2005/02/18 14:23:16 $
* Version: $Revision: 1.18 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
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

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * This interface defines an OpenCms request, a generic request object that is used 
 * by OpenCms and provides methods to read the data included in the request.<p>
 *
 * Implementations of this interface use an existing request 
 * (e.g. HttpServletRequest) to
 * initialize an I_CmsRequest.
 *
 * @author Michael Emmerich
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.18 $ $Date: 2005/02/18 14:23:16 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public interface I_CmsRequest {

    /** Request context attribute name. */
    String C_CMS_REQUEST = "__I_CmsRequest";
    
    /**
     * Returns the content of an uploaded file.
     * Returns null if no file with this name has been uploaded with this request.
     * Returns an empty byte[] if a file without content has been uploaded.
     *
     * @param name The name of the uploaded file.
     * @return The selected uploaded file content.
     */
    byte[] getFile(String name);

    /**
     * Returns the names of all uploaded files in this request.
     * Returns an empty eumeration if no files were included in the request.
     *
     * @return An Enumeration of file names.
     */
    Enumeration getFileNames();

    /**
     * Returns the original request that was used to create the CmsRequest.
     *
     * @return The original request of the CmsRequest.
     */
    HttpServletRequest getOriginalRequest();
    
    /**
     * Sets the original request to another value.
     * 
     * @param request the request 
     */
    void setOriginalRequest(HttpServletRequest request);

    /**
     * Returns the type of the request that was used to create the CmsRequest.
     * The returned int must be one of the constants defined above in this interface.
     *
     * @return The type of the CmsRequest.
     */
    int getOriginalRequestType();

    /**
     * Returns the value of a named parameter as a String.
     * Returns null if the parameter does not exist or an empty string if the parameter
     * exists but without a value.
     *
     * @param name The name of the parameter.
     * @return The value of the parameter.
     */
    String getParameter(String name);

    /**
     * Returns all parameter names as an Enumeration of String objects.
     * Returns an empty Enumeratrion if no parameters were included in the request.
     *
     * @return Enumeration of parameter names.
     */
    Enumeration getParameterNames();

    /**
     * Returns all parameter values of a parameter key.
     *
     * @param key the parameter key
     * @return Aarray of String containing the parameter values.
     */
    String[] getParameterValues(String key);

    /**
     * This funtion returns the name of the requested resource.
     * <P>
     * For a http request, the name of the resource is extracted as follows:
     * <CODE>http://{servername}/{servletpath}/{path to the cms resource}</CODE>
     * In the following example:
     * <CODE>http://my.work.server/servlet/opencms/system/def/explorer</CODE>
     * the requested resource is <CODE>/system/def/explorer</CODE>.
     * </P>
     *
     * @return The path to the requested resource.
     */
    String getRequestedResource();
    
    /**
     * Set the name returned by getRequestedResource().
     * This is required in case there was a folder name requested and 
     * a default file (e.g. index.html) has to be used instead of the folder.
     * 
     * @param resourceName The name to set the requested resource name to 
     */
    void setRequestedResource(String resourceName);

    /**
     * Returns the part of the Url that descibes the Web-Application.
     *
     * E.g: http://www.myserver.com/opencms/engine/index.html returns
     * http://www.myserver.com/opencms
     * 
     * @return the webapp url
     */
    String getWebAppUrl();

    /**
     * Gets the part of the Url that describes the current servlet of this
     * Web-Application.
     * 
     * @return the servlet url
     */
    String getServletUrl();

    /**
     * Methods to get the data from the original request.
     * 
     * @return the server name
     */
    String getServerName();
    
    /**
     * Methods to get the data from the original request.
     * 
     * @return the scheme
     */    
    String getScheme();
    
    /**
     * Methods to get the data from the original request.
     * 
     * @return the server port
     */
    int getServerPort();
}
