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

import org.opencms.file.CmsObject;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Filter access to statically exported resources while checking permissions.<p>
 */
public class OpenCmsProtectedExportFilter implements Filter {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(OpenCmsProtectedExportFilter.class);

    /** The protected export path prefix. */
    private String m_prefix;

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {

        m_prefix = null;
    }

    /**
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws IOException, ServletException {

        if ((OpenCms.getStaticExportManager().getProtectedExportPath() != null)
            && (req instanceof HttpServletRequest)) {
            HttpServletRequest request = (HttpServletRequest)req;
            String uri = request.getRequestURI();
            if (uri.startsWith(getPrefix())) {
                // direct access to the protected export folder is forbidden
                ((HttpServletResponse)res).sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            try {
                CmsObject cms = OpenCmsCore.getInstance().initCmsObject(request, (HttpServletResponse)res, false);
                if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {

                    String rootPath = OpenCms.getLinkManager().getRootPath(cms, uri);
                    if (rootPath != null) {
                        String rfsName = OpenCms.getStaticExportManager().getProtectedExportName(rootPath);
                        if (rfsName != null) {
                            cms = OpenCms.initCmsObject(cms);
                            cms.getRequestContext().setSiteRoot("");
                            if (cms.existsResource(rootPath)) {
                                req.getRequestDispatcher(rfsName).forward(request, res);
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        chain.doFilter(req, res);
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig arg0) {

        // nothing to do
    }

    /**
     * Returns the protected export path prefix.<p>
     *
     * @return the path prefix
     */
    private String getPrefix() {

        if (m_prefix == null) {
            m_prefix = OpenCms.getSystemInfo().getContextPath()
                + "/"
                + OpenCms.getStaticExportManager().getProtectedExportPath()
                + "/";
        }
        return m_prefix;
    }
}
