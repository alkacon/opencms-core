
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsRequest.java,v $
* Date   : $Date: 2001/07/10 16:05:47 $
* Version: $Revision: 1.6 $
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

import java.util.*;

/**
 * This interface defines a CmsRequest.
 *
 * The CmsRequest is a genereic request object that is used in the CmsObject provinding
 * methods to read the data included in the request.
 *
 * Implementations of this interface use an existing request (e.g. HttpServletRequest) to
 * initialize a CmsRequest.
 *
 * @author Michael Emmerich
 * @author Alexander Kandzior
 * @version $Revision: 1.6 $ $Date: 2001/07/10 16:05:47 $
 */
public interface I_CmsRequest {

    /**
     * Returns the content of an uploaded file.
     * Returns null if no file with this name has been uploaded with this request.
     * Returns an empty byte[] if a file without content has been uploaded.
     *
     * @param name The name of the uploaded file.
     * @return The selected uploaded file content.
     */
    public byte[] getFile(String name);

    /**
     * Returns the names of all uploaded files in this request.
     * Returns an empty eumeration if no files were included in the request.
     *
     * @return An Enumeration of file names.
     */
    public Enumeration getFileNames();

    /**
     * Returns the original request that was used to create the CmsRequest.
     *
     * @return The original request of the CmsRequest.
     */
    public Object getOriginalRequest();

    /**
     * Returns the type of the request that was used to create the CmsRequest.
     * The returned int must be one of the constants defined above in this interface.
     *
     * @return The type of the CmsRequest.
     */
    public int getOriginalRequestType();

    /**
     * Returns the value of a named parameter as a String.
     * Returns null if the parameter does not exist or an empty string if the parameter
     * exists but without a value.
     *
     * @param name The name of the parameter.
     * @returns The value of the parameter.
     */
    public String getParameter(String name);

    /**
     * Returns all parameter names as an Enumeration of String objects.
     * Returns an empty Enumeratrion if no parameters were included in the request.
     *
     * @return Enumeration of parameter names.
     */
    public Enumeration getParameterNames();

    /**
     * Returns all parameter values of a parameter key.
     *
     * @return Aarray of String containing the parameter values.
     */
    public String[] getParameterValues(String key);

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
    public String getRequestedResource();

    /**
     * Returns the part of the Url that descibes the Web-Application.
     *
     * E.g: http://www.myserver.com/opencms/engine/index.html returns
     * http://www.myserver.com/opencms
     */
    public String getWebAppUrl();

    /**
     * Gets the part of the Url that describes the current servlet of this
     * Web-Application.
     */
    public String getServletUrl();
}
