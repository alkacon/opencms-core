/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.main;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Implements a servlet filter for URL rewriting.
 * It adds the servlet name (typically "/opencms") if not already present, but necessary.
 */
public class OpenCmsUrlServletFilter implements Filter {

    /**
     * Name of the init-param of the filter configuration that is used to provide
     * a pipe separated list of additional prefixes for which the URIs should not be adjusted.
     */
    private static final String INIT_PARAM_ADDITIONAL_EXCLUDEPREFIXES = "additionalExcludePrefixes";

    /** @see org.opencms.main.CmsSystemInfo#getContextPath() */
    private static final String CONTEXTPATH = OpenCms.getSystemInfo().getContextPath();

    /** @see org.opencms.main.CmsSystemInfo#getServletPath() */
    private static final String SERVLETPATH = OpenCms.getSystemInfo().getServletPath() + "/";

    /** @see org.opencms.staticexport.CmsStaticExportManager#getExportPathForConfiguration() */
    private static final String EXPORTPATH = "/" + OpenCms.getStaticExportManager().getExportPathForConfiguration();

    /**
     * The URI prefixes, for which the requested URI should not be rewritten,
     * i.e., the ones that should not be handled by the {@link org.opencms.main.OpenCmsServlet}.
     */
    private static final String[] DEFAULT_EXCLUDE_PREFIXES = new String[] {
        EXPORTPATH,
        "/workplace",
        "/VAADIN/",
        SERVLETPATH,
        "/resources/",
        "/webdav"};

    /**
     * A regex that matches if the requested URI starts with one of the {@link #DEFAULT_EXCLUDE_PREFIXES}
     * or a prefix listed in the value of the {@link #INIT_PARAM_ADDITIONAL_EXCLUDEPREFIXES} init-param.
     */
    private String m_regex;

    /**
     * Creates a regex that matches all URIs starting with one of the {@link #DEFAULT_EXCLUDE_PREFIXES}
     * or one of the <code>additionalExcludePrefixes</code>.
     *
     * @param additionalExcludePrefixes a pipe separated list of URI prefixes for which the URLs
     *  should not be adjusted - additionally to the default exclude prefixes
     *
     * @return a regex that matches all URIs starting with one of the {@link #DEFAULT_EXCLUDE_PREFIXES}
     *  or one of the <code>additionalExcludePrefixes</code>.
     */
    static String createRegex(String additionalExcludePrefixes) {

        StringBuffer regex = new StringBuffer();
        regex.append(CONTEXTPATH);
        regex.append('(');
        regex.append(DEFAULT_EXCLUDE_PREFIXES[0]);
        for (int i = 1; i < DEFAULT_EXCLUDE_PREFIXES.length; i++) {
            regex.append('|').append(DEFAULT_EXCLUDE_PREFIXES[i]);
        }
        if (!((null == additionalExcludePrefixes) || additionalExcludePrefixes.isEmpty())) {
            regex.append('|').append(additionalExcludePrefixes);
        }
        regex.append(')');
        regex.append(".*");
        return regex.toString();
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {

        // Nothing to do

    }

    /**
     * Adjusts the requested URIs by prepending the name of the {@link org.opencms.main.OpenCmsServlet},
     * if the request should be handled by that servlet.
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest)request;
            String uri = req.getRequestURI();
            if (!uri.matches(m_regex)) {
                String adjustedUri = uri.replaceFirst(CONTEXTPATH + "/", SERVLETPATH);
                req.getRequestDispatcher(adjustedUri).forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) {

        m_regex = createRegex(filterConfig.getInitParameter(INIT_PARAM_ADDITIONAL_EXCLUDEPREFIXES));
    }
}
