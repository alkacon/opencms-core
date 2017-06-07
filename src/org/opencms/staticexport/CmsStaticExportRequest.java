/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import org.opencms.util.CmsRequestUtil;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Wrapper for static export requests, required for parameter based requests.<p>
 *
 * @since 6.0.0
 */
public class CmsStaticExportRequest extends HttpServletRequestWrapper {

    /** Map of parameters from the original request. */
    private Map<String, String[]> m_parameters;

    /**
     * Creates a new static export request wrapper.<p>
     *
     * @param req the request to wrap
     * @param data the data for the static export
     */
    public CmsStaticExportRequest(HttpServletRequest req, CmsStaticExportData data) {

        super(req);
        m_parameters = CmsRequestUtil.createParameterMap(data.getParameters());
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    @Override
    public long getDateHeader(String name) {

        // make sue "last modified since" optimization is NOT used for export requests
        if (CmsRequestUtil.HEADER_IF_MODIFIED_SINCE.equals(name)) {
            // return -1, this means "no header has been set" in servlet standard
            return -1;
        }
        return super.getDateHeader(name);
    }

    /**
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name) {

        String[] values = m_parameters.get(name);
        if (values != null) {
            return (values[0]);
        }
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {

        return m_parameters;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {

        return Collections.enumeration(m_parameters.keySet());
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name) {

        return m_parameters.get(name);
    }
}