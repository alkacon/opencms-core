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

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.google.common.net.HttpHeaders;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Simple filter for setting caching headers for exported resources.
 *
 * <p>CSS and Javascript are assumed to be cacheable for 24 hours, everything else is assumed to be cacheable for 365 days.
 */
public class CmsExportExpiresFilter implements Filter {

    /**
     * Response wrapper which sets caching headers based on content type.
     */
    static class ResponseWrapper extends HttpServletResponseWrapper {

        /**
         * Creates a new instance.
         * @param response the response to wrap
         */
        public ResponseWrapper(HttpServletResponse response) {

            super(response);
        }

        /**
         * @see jakarta.servlet.ServletResponseWrapper#setContentType(java.lang.String)
         */
        @Override
        public void setContentType(String type) {

            super.setContentType(type);
            Duration day = Duration.of(1, ChronoUnit.DAYS);
            Duration year = Duration.of(365, ChronoUnit.DAYS);
            Duration duration = null;
            if (type != null) {
                if (type.contains("application/javascript")
                    || type.contains("text/css")
                    || type.contains("text/javascript")) {
                    duration = day;
                } else {
                    duration = year;
                }
                setDateHeader(HttpHeaders.EXPIRES, System.currentTimeMillis() + duration.toMillis());
                setHeader(HttpHeaders.CACHE_CONTROL, "max-age=" + duration.getSeconds());
            }
        }
    }

    /**
     * @see jakarta.servlet.Filter#destroy()
     */
    public void destroy() {

        // do nothing
    }

    /**
     * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse)response);
        chain.doFilter(request, wrappedResponse);
    }

    /**
     * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) {

        // does nothing
    }

}
