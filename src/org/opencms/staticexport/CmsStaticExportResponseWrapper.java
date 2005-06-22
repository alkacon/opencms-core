/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportResponseWrapper.java,v $
 * Date   : $Date: 2005/06/22 14:58:54 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrapper for static export requests, required to access the status code of the response.<p>
 * 
 * The <code>{@link org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, HttpServletResponse)}</code>
 * method is called by the static export manager. Many inmplementations set the http status codes for the response.
 * This wrapper enabled the export manager to return the status code set on the response 
 * in <code>{@link org.opencms.staticexport.CmsStaticExportManager#export(javax.servlet.http.HttpServletRequest, HttpServletResponse, org.opencms.file.CmsObject, CmsStaticExportData)}</code>.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsStaticExportResponseWrapper extends HttpServletResponseWrapper {

    /** The status code. */
    protected int m_status;

    /**
     * Creates a new export response wrapper.<p>
     * 
     * @param res the original response to wrap
     */
    public CmsStaticExportResponseWrapper(HttpServletResponse res) {

        super(res);
        m_status = -1;
    }

    /**
     * Returns the status code of this export response, if no status code was set so far,
     * <code>-1</code> is returned.<p>
     * 
     * @return the status code of this exportresponse
     */
    public int getStatus() {

        return m_status;
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    public void sendError(int status) throws IOException {

        m_status = status;
        super.sendError(status);
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
     */
    public void sendError(int status, String message) throws IOException {

        m_status = status;
        super.sendError(status, message);
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
     */
    public void setStatus(int status) {

        m_status = status;
        super.setStatus(status);
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int, java.lang.String)
     */
    public void setStatus(int status, String message) {

        m_status = status;
        super.setStatus(status, message);
    }
}