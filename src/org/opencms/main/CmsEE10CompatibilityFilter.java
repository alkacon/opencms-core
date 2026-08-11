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

import org.opencms.flex.CmsFlexResponse;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * This is a workaround for Jakarta EE 10 / Servlet 6.
 *
 * <p>With the new version of the standard, servlet containers are allowed to add custom wrappers for includes/forwards, which means the current response in the include or forward target will no longer be
 * a CmsFlexResponse. Specifically in the case of Jetty, it adds a wrapper that (for includes) will intercept all calls to methods that send headers and block them before they get to the CmsFlexResponse.
 * This breaks the way headers / redirects are handled by the flex cache. So for those types of requests, we check if the wrapper really wraps a CmsFlexResponse, and if so, pass a servlet response wrapper
 * to the filter chain which wraps the already wrapped response, but forwards all calls to setHeader, sendRedirect, etc. to the CmsFlexResponse, bypassing the servlet container wrapper.
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
        chain.doFilter(request, filterResponse);

    }

}
