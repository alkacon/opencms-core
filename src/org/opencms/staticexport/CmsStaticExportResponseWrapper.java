/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.util.CmsRequestUtil;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrapper for static export requests, required to access the status code of the response.<p>
 *
 * The <code>{@link org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, jakarta.servlet.http.HttpServletRequest, HttpServletResponse)}</code>
 * method is called by the static export manager. Many implementations set the http status codes for the response.
 * This wrapper enables the export manager to return the status code set on the response
 * in <code>{@link org.opencms.staticexport.CmsStaticExportManager#export(jakarta.servlet.http.HttpServletRequest, HttpServletResponse, org.opencms.file.CmsObject, CmsStaticExportData)}</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsStaticExportResponseWrapper extends HttpServletResponseWrapper {

    /** The enforced cache control value, or {@code null} if no value is enforced. */
    private String m_cacheControl;

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
     * @see jakarta.servlet.http.HttpServletResponseWrapper#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String name, String value) {

        if ((m_cacheControl != null) && CmsRequestUtil.HEADER_CACHE_CONTROL.equalsIgnoreCase(name)) {
            super.setHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, m_cacheControl);
        } else {
            super.addHeader(name, value);
        }
    }

    /**
     * Enforces the given Cache-Control value for this response.<p>
     *
     * Later attempts by a resource loader to replace or add Cache-Control headers retain this value. If this method
     * is not called, the wrapper keeps its classic behavior and delegates all header changes unchanged.<p>
     *
     * @param cacheControl the Cache-Control value to enforce
     */
    public void enforceCacheControl(String cacheControl) {

        m_cacheControl = cacheControl;
        super.setHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, cacheControl);
    }

    /**
     * Returns the status code of this export response, if no status code was set so far,
     * <code>-1</code> is returned.<p>
     *
     * @return the status code of this export response
     */
    @Override
    public int getStatus() {

        return m_status;
    }

    /**
     * @see jakarta.servlet.ServletResponseWrapper#reset()
     */
    @Override
    public void reset() {

        super.reset();
        m_status = -1;
        if (m_cacheControl != null) {
            super.setHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, m_cacheControl);
        }
    }

    /**
     * @see jakarta.servlet.http.HttpServletResponse#sendError(int)
     */
    @Override
    public void sendError(int status) throws IOException {

        m_status = status;
        super.sendError(status);
    }

    /**
     * @see jakarta.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
     */
    @Override
    public void sendError(int status, String message) throws IOException {

        m_status = status;
        super.sendError(status, message);
    }

    /**
     * @see jakarta.servlet.http.HttpServletResponseWrapper#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String name, String value) {

        if ((m_cacheControl != null) && CmsRequestUtil.HEADER_CACHE_CONTROL.equalsIgnoreCase(name)) {
            super.setHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, m_cacheControl);
        } else {
            super.setHeader(name, value);
        }
    }

    /**
     * @see jakarta.servlet.http.HttpServletResponseWrapper#setStatus(int)
     */
    @Override
    public void setStatus(int status) {

        m_status = status;
        super.setStatus(status);
    }

}
