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
import java.net.URI;

import org.apache.commons.logging.Log;

import com.google.common.net.HttpHeaders;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Servlet filter to set the Content-Type response header based on the request path, according to the rules configured in opencms-vfs.xml.
 *
 * <p>This is meant to be used for static resources in the export folder.
 */
public class CmsExportContentTypeFilter implements Filter {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExportContentTypeFilter.class);

    /** Parameter to disable/enable wrapping of responses. */
    private static final String PARAM_WRAP_RESPONSES = "wrapResponses";

    /** True if responses should be wrapped. */
    private boolean m_wrapResponses = true;

    /**
     * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        ServletResponse filterResponse = response;
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;

            String mimeType = getMimeType(httpRequest);

            if (mimeType != null) {
                response.setContentType(mimeType);
                if (m_wrapResponses) {
                    // Jetty's servlet for serving static resources overrides the content type,
                    // but it does that by first removing them and then re-applying them based on its own mime type lookup table.
                    // The problem is, this table is incomplete and e.g. doesn't contain the mime type for .docx.
                    // So (unless wrapping is disabled) we wrap the response in a wrapper that prevents the clearing of the content type.
                    // It also tries to prevent duplication of the content type header.

                    filterResponse = new HttpServletResponseWrapper(httpResponse) {

                        @Override
                        public void addHeader(String name, String value) {

                            if (value != null) {
                                if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
                                    LOG.debug("Content-Type header was added with value: " + value);
                                    super.setHeader(name, value);
                                } else {
                                    super.addHeader(name, value);
                                }
                            }
                        }

                        @Override
                        public void setContentType(String type) {

                            if (type == null) {
                                LOG.debug(
                                    "setContentType was called with null value, ignoring it - OpenCmsContentType is "
                                        + mimeType
                                        + " [url: "
                                        + httpRequest.getRequestURL()
                                        + "]");
                            } else {
                                LOG.debug("setContentType was called with value: " + type);
                                super.setContentType(type);
                            }
                        }

                        @Override
                        public void setHeader(String name, String value) {

                            if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name) && (value == null)) {
                                LOG.debug(
                                    "Ignoring clearing of content type by servlet container - OpenCms content type is "
                                        + mimeType
                                        + " [url: "
                                        + httpRequest.getRequestURL()
                                        + "]");
                            } else {
                                super.setHeader(name, value);
                            }
                        }
                    };
                }
            }
        }
        chain.doFilter(request, filterResponse);

    }

    /**
     * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String wrapResponsesStr = filterConfig.getInitParameter(PARAM_WRAP_RESPONSES);
        if (wrapResponsesStr != null) {
            m_wrapResponses = Boolean.parseBoolean(wrapResponsesStr);
        }
    }

    /**
     * Tries to determine the MIME type to set for the given request (based on the path).
     *
     * @param httpRequest the current request
     * @return the MIME type to set (or null if no MIME type could be determined)
     */
    private String getMimeType(HttpServletRequest httpRequest) {

        try {
            URI uri = new URI(httpRequest.getRequestURI());
            String path = uri.getPath();
            if (!path.endsWith("/")) {
                int slashPos = path.lastIndexOf('/');
                String filename = "";
                if (slashPos == -1) {
                    filename = path;
                } else {
                    filename = path.substring(slashPos + 1);
                }
                String mimeType = OpenCms.getResourceManager().getMimeType(
                    filename,
                    OpenCms.getSystemInfo().getDefaultEncoding(),
                    null);
                return mimeType;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

}
