/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsExportRequest.java,v $
* Date   : $Date: 2001/12/20 15:29:37 $
* Version: $Revision: 1.2 $
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

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class CmsExportRequest implements I_CmsRequest {

    /**
     * The resource requested.
     */
    private String m_resourcePath;

    /**
     * the parameters for this request.
     */
    private Hashtable m_parameters = null;

    public CmsExportRequest(HttpServletRequest req) {
    }
    public CmsExportRequest() {
    }
    public byte[] getFile(String name) {
        return null;
    }
    public Enumeration getFileNames() {
        Enumeration enu = (new Vector()).elements();
        return enu;
    }
    public Object getOriginalRequest() {
        throw new java.lang.UnsupportedOperationException("Method getOriginalRequest not supported in StaticExport.");
    }
    public int getOriginalRequestType() {
        throw new java.lang.UnsupportedOperationException("Method getOriginalRequestType not supported in StaticExport.");
    }
    public String getParameter(String name) {
        if(m_parameters != null){
            String[] res = (String[])m_parameters.get(name);
            if(res != null){
                return res[0];
            }
        }
        return null;
    }
    public Enumeration getParameterNames() {
        if(m_parameters == null){
            Enumeration enu = (new Vector()).elements();
            return enu;
        }else{
            return m_parameters.keys();
        }
    }
    public String[] getParameterValues(String key) {
        if(m_parameters != null){
            return (String[])m_parameters.get(key);
        }
        return null;
    }
    /**
     * sets the parameters for this static export.
     * @param parameters The Hashtable with the parameters (contains String[])
     */
    public void setParameters(Hashtable parameters){
        m_parameters = parameters;
    }
    public String getRequestedResource() {
        return m_resourcePath;
    }
    public void setRequestedResource(String res){
        m_resourcePath = res;
    }

    /**
     * Returns the part of the Url that descibes the Web-Application.
     *
     * E.g: http://www.myserver.com/opencms/engine/index.html returns
     * http://www.myserver.com/opencms
     */
    public String getWebAppUrl() {
        throw new java.lang.UnsupportedOperationException("Method getWebAppUrl not supported in StaticExport.");
    }

    /**
     * Gets the part of the Url that describes the current servlet of this
     * Web-Application.
     */
    public String getServletUrl() {
        throw new java.lang.UnsupportedOperationException("Method getServletUrl not supported in StaticExport.");
    }
}