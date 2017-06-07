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

package org.opencms.main;

import org.opencms.jsp.util.CmsJspStatusBean;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This the error handler servlet of the OpenCms system.<p>
 *
 * This almost 1:1 extends the "standard" {@link org.opencms.main.OpenCmsServlet}.
 * By default, all errors are handled by this servlet, which is controlled by the
 * setting in the shipped <code>web.xml</code>.<p>
 *
 * This servlet is required because certain servlet containers (eg. BEA Weblogic)
 * can not handler the error with the same servlet that produced the error.<p>
 *
 * @since 6.2.0
 *
 * @see org.opencms.main.OpenCmsServlet
 * @see org.opencms.staticexport.CmsStaticExportManager
 */
public class OpenCmsServletErrorHandler extends OpenCmsServlet {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 5316004893684482816L;

    /**
     * OpenCms servlet main request handling method.<p>
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        // check the error status
        Integer errorStatus = (Integer)req.getAttribute(CmsJspStatusBean.ERROR_STATUS_CODE);
        if (errorStatus != null) {
            // only use super method if an error status code is set
            if (OpenCmsCore.getInstance().getRunLevel() > OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
                // use super method if servlet run level is available
                super.doGet(req, res);
            } else {
                // otherwise display a simple error page
                String errorMessage = (String)req.getAttribute(CmsJspStatusBean.ERROR_MESSAGE);
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(errorMessage)) {
                    errorMessage = "";
                }
                String output = "<html><body>"
                    + CmsStringUtil.escapeHtml(
                        Messages.get().getBundle().key(
                            Messages.ERR_OPENCMS_NOT_INITIALIZED_2,
                            errorStatus,
                            errorMessage))
                    + "</body></html>";
                res.setStatus(errorStatus.intValue());
                res.getWriter().println(output);
            }
        } else {
            // no status code set, this is an invalid request
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public synchronized void init(ServletConfig config) {

        // override super class to avoid default initialization
    }
}