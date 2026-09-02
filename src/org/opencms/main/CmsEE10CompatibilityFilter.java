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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.flex.CmsFlexRequest;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.util.CmsRequestUtil;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * This is a workaround for Jakarta EE 10 / Servlet 6.
 *
 * <p>With the new version of the standard, servlet containers are allowed to add custom wrappers for includes/forwards, which means the current response in the include or forward target will no longer be
 * a CmsFlexResponse. Specifically in the case of Jetty, it adds a wrapper that (for includes) will intercept all calls to methods that send headers and block them before they get to the CmsFlexResponse.
 * This breaks the way headers / redirects are handled by the flex cache. So for those types of requests, we check if the wrapper really wraps a CmsFlexResponse, and if so, pass a servlet response wrapper
 * to the filter chain which wraps the already wrapped response, but forwards all calls to setHeader, sendRedirect, etc. to the CmsFlexResponse, bypassing the servlet container wrapper.
 *
 * <p>The same applies to the request: the wrapper Jetty adds around the CmsFlexRequest reads the parameters of the wrapped request only once and then keeps that snapshot,
 * while OpenCms changes the parameters of the CmsFlexRequest during the request (includes, cms:addparams, forwards). So if the wrapper wraps a CmsFlexRequest, we pass
 * a request wrapper to the filter chain which forwards all parameter calls to the CmsFlexRequest, so the request seen by a JSP behaves like the CmsFlexRequest itself.
 */
public class CmsEE10CompatibilityFilter implements Filter {

    /**
     * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        HttpServletRequest filterRequest = request;
        if (!(req instanceof CmsFlexRequest)) {
            final CmsFlexRequest flexRequest = CmsRequestUtil.getFlexRequest(req);
            if (flexRequest != null) {
                filterRequest = new HttpServletRequestWrapper(request) {

                    @Override
                    public String getParameter(String name) {

                        return flexRequest.getParameter(name);
                    }

                    @Override
                    public Map<String, String[]> getParameterMap() {

                        return flexRequest.getParameterMap();
                    }

                    @Override
                    public Enumeration<String> getParameterNames() {

                        return flexRequest.getParameterNames();
                    }

                    @Override
                    public String[] getParameterValues(String name) {

                        return flexRequest.getParameterValues(name);
                    }
                };
            }
        }
        HttpServletResponse filterResponse = response;
        if ((res instanceof HttpServletResponseWrapper) && !(res instanceof CmsFlexResponse)) {
            HttpServletResponse wrappedResponse = (HttpServletResponse)((HttpServletResponseWrapper)res).getResponse();
            if (wrappedResponse instanceof CmsFlexResponse) {
                final CmsFlexResponse flexResponse = (CmsFlexResponse)wrappedResponse;
                filterResponse = new HttpServletResponseWrapper(response) {

                    @Override
                    public void addCookie(Cookie cookie) {

                        flexResponse.addCookie(cookie);
                    }

                    @Override
                    public void addDateHeader(String name, long date) {

                        flexResponse.addDateHeader(name, date);
                    }

                    @Override
                    public void addHeader(String name, String value) {

                        flexResponse.addHeader(name, value);
                    }

                    @Override
                    public void addIntHeader(String name, int value) {

                        flexResponse.addIntHeader(name, value);
                    }

                    @Override
                    public void sendRedirect(String location) throws IOException {

                        flexResponse.sendRedirect(location);

                    }

                    @Override
                    public void setDateHeader(String name, long date) {

                        flexResponse.setDateHeader(name, date);
                    }

                    @Override
                    public void setHeader(String name, String value) {

                        flexResponse.setHeader(name, value);
                    }

                    @Override
                    public void setIntHeader(String name, int value) {

                        flexResponse.setIntHeader(name, value);
                    }
                };

            }
        }
        chain.doFilter(filterRequest, filterResponse);

    }

}
