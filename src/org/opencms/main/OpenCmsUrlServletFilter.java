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

import org.apache.commons.logging.Log;

/**
 * Implements a servlet filter for URL rewriting.
 * It adds the servlet name (typically "/opencms") if not already present, but necessary.
 */
public class OpenCmsUrlServletFilter implements Filter {

    /** The static log object for this class. */
    static final Log LOG = CmsLog.getLog(OpenCmsUrlServletFilter.class);

    /**
     * Name of the init-param of the filter configuration that is used to provide
     * a pipe separated list of additional prefixes for which the URIs should not be adjusted.
     */
    private static final String INIT_PARAM_ADDITIONAL_EXCLUDEPREFIXES = "additionalExcludePrefixes";

    /** A pipe separated list of URI prefixes, for which URIs should not be adjusted, additionally to the default prefixes. */
    private String m_additionalExcludePrefixes;

    /** The servlet context. */
    private String m_contextPath;
    /** Flag, indicating if the filter has been initialized. */
    private boolean m_isInitialized;

    /**
     * A regex that matches if the requested URI starts with one of the default exclude prefixes
     * or a prefix listed in the value of the {@link #INIT_PARAM_ADDITIONAL_EXCLUDEPREFIXES} init-param.
     */
    private String m_regex;

    /** The servlet name, prefixed by "/". */
    private String m_servletPath;

    /**
     * Creates a regex that matches all URIs starting with one of the <code>defaultExcludePrefixes</code>
     * or one of the <code>additionalExcludePrefixes</code>.
     *
     * @param contextPath The context path that every URI starts with.
     * @param defaultExcludePrefixes the default exclude prefixes.
     * @param additionalExcludePrefixes a pipe separated list of URI prefixes for which the URLs
     *  should not be adjusted - additionally to the default exclude prefixes
     *
     * @return a regex that matches all URIs starting with one of the <code>defaultExcludePrefixes</code>
     *  or one of the <code>additionalExcludePrefixes</code>.
     */
    static String createRegex(String contextPath, String[] defaultExcludePrefixes, String additionalExcludePrefixes) {

        StringBuffer regex = new StringBuffer();
        regex.append(contextPath);
        regex.append('(');
        regex.append(defaultExcludePrefixes[0]);
        for (int i = 1; i < defaultExcludePrefixes.length; i++) {
            regex.append('|').append(defaultExcludePrefixes[i]);
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

        m_additionalExcludePrefixes = null;

    }

    /**
     * Adjusts the requested URIs by prepending the name of the {@link org.opencms.main.OpenCmsServlet},
     * if the request should be handled by that servlet.
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        if (m_isInitialized || tryToInitialize()) {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest)request;
                String uri = req.getRequestURI();
                if (!uri.matches(m_regex)) {
                    String adjustedUri = uri.replaceFirst(m_contextPath + "/", m_servletPath);
                    req.getRequestDispatcher(adjustedUri).forward(request, response);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) {

        m_additionalExcludePrefixes = filterConfig.getInitParameter(INIT_PARAM_ADDITIONAL_EXCLUDEPREFIXES);
    }

    /**
     * Checks if OpenCms has reached run level 4 and if so, it initializes the member variables.
     * In particular {@link #m_regex}, {@link #m_contextPath} and {@link OpenCmsUrlServletFilter#m_servletPath} are initialized and sets {@link #m_isInitialized} to true.
     *
     * @return <code>true</code> if initialization was successful, <code>false</code> otherwise.
     */
    boolean tryToInitialize() {

        if (m_isInitialized) {
            return true;
        }
        if (OpenCms.getRunLevel() == 4) {
            m_contextPath = OpenCms.getSystemInfo().getContextPath();
            m_servletPath = OpenCms.getSystemInfo().getServletPath() + "/";

            /**
             * The URI prefixes, for which the requested URI should not be rewritten,
             * i.e., the ones that should not be handled by the {@link org.opencms.main.OpenCmsServlet}.
             */
            String[] defaultExcludePrefixes = new String[] {
                "/" + OpenCms.getStaticExportManager().getExportPathForConfiguration(),
                "/workplace",
                "/VAADIN/",
                m_servletPath,
                "/resources/",
                "/webdav",
                "/cmisatom",
                "/services"};
            StringBuffer regex = new StringBuffer();
            regex.append(m_contextPath);
            regex.append('(');
            regex.append(defaultExcludePrefixes[0]);
            for (int i = 1; i < defaultExcludePrefixes.length; i++) {
                regex.append('|').append(defaultExcludePrefixes[i]);
            }
            if (!((null == m_additionalExcludePrefixes) || m_additionalExcludePrefixes.isEmpty())) {
                regex.append('|').append(m_additionalExcludePrefixes);
            }
            regex.append(')');
            regex.append(".*");
            m_regex = regex.toString();
            m_isInitialized = true;
            return true;
        }
        return false;
    }
}
