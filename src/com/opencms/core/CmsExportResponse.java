/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsExportResponse.java,v $
* Date   : $Date: 2002/10/30 10:07:28 $
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

import java.io.*;

/**
 * Implementation of the I_CmsResponse interface which is used as response 
 * buffer for pages during a static export.<p>
 * 
 * @version $Revision: 1.2 $ $Date: 2002/10/30 10:07:28 $
 */
public class CmsExportResponse implements I_CmsResponse {

    /**
     * The OutputStream to the discfile.
     */
    private OutputStream m_outStream;

    /**
     *
     */
    private boolean m_outputWritten = false;

    public CmsExportResponse() {

    }

    public Object getOriginalResponse() {
        return null;
    }

    public int getOriginalResponseType() {
        return 0;
    }

    public void putOutputStream(OutputStream outStream){
        m_outStream = outStream;
    }

    public OutputStream getOutputStream() throws IOException {
        m_outputWritten  = true;
        return m_outStream;
    }

    /**
     * Check if the output stream was requested previously.
     * @return <code>true</code> if getOutputStream() was called, <code>false</code> otherwise.
     */
    public boolean isOutputWritten() {
        return m_outputWritten;
    }

    public boolean isRedirected() {
        return false;
    }

    public void sendCmsRedirect(String location) throws IOException {
    }

    public void sendError(int code) throws IOException {
    }

    public void sendError(int code, String msg) throws IOException {
    }

    public void sendRedirect(String location) throws IOException {
    }

    public void setContentLength(int len) {
    }

    public void setContentType(String type) {
    }
    
    public String getContentType() {
        return null;
    }    

    public void setHeader(String key, String value) {
    }

    public void addHeader(String key, String value) {
    }

    public void setLastModified(long time) {
    }

    public boolean containsHeader(String key) {
        return false;
    }
}