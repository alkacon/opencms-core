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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Servlet filter to set the Content-Type response header based on the request path, according to the rules configured in opencms-vfs.xml.
 *
 * <p>This is meant to be used for static resources in the export folder.
 */
public class CmsExportContentTypeFilter implements Filter {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExportContentTypeFilter.class);

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            String mimeType = getMimeType(httpRequest);
            if (mimeType != null) {
                response.setContentType(mimeType);
            }
        }
        chain.doFilter(request, response);

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
