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

import org.opencms.file.CmsObject;
import org.opencms.gwt.CmsCoreService;
import org.opencms.gwt.CmsPrefetchSerializationPolicy;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsRequestUtil;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.google.gwt.user.server.rpc.RPC;
import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;

/**
 * Servlet for workplace UI requests.<p>
 */
public class CmsUIServlet extends VaadinServlet {

    /** The static log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsUIServlet.class);

    /** Serialization id. */
    private static final long serialVersionUID = 8119684308154724518L;

    /** The current CMS context. */
    private ThreadLocal<CmsObject> m_perThreadCmsObject;

    /**
     * Returns the current cms context.<p>
     *
     * @return the current cms context
     */
    public CmsObject getCmsObject() {

        return m_perThreadCmsObject.get();
    }

    /**
     * Sets the current cms context.<p>
     *
     * @param cms the current cms context to set
     */
    public synchronized void setCms(CmsObject cms) {

        if (m_perThreadCmsObject == null) {
            m_perThreadCmsObject = new ThreadLocal<CmsObject>();
        }
        m_perThreadCmsObject.set(cms);
    }

    /**
     * @see com.vaadin.server.VaadinServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        // check to OpenCms runlevel
        int runlevel = OpenCmsCore.getInstance().getRunLevel();

        // write OpenCms server identification in the response header
        response.setHeader(CmsRequestUtil.HEADER_SERVER, OpenCmsCore.getInstance().getSystemInfo().getVersion());

        if (runlevel != OpenCms.RUNLEVEL_4_SERVLET_ACCESS) {
            // not the "normal" servlet runlevel
            if (runlevel == OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
                // we have shell runlevel only, upgrade to servlet runlevel (required after setup wizard)
                init(getServletConfig());
            } else {
                // illegal runlevel, we can't process requests
                // sending status code 403, indicating the server understood the request but refused to fulfill it
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                // goodbye
                return;
            }
        }

        try {
            OpenCmsCore.getInstance().initCmsContextForUI(request, response, this);
            super.service(request, response);
        } catch (CmsRoleViolationException rv) {
            // don't log these into the error channel
            LOG.debug(rv.getLocalizedMessage(), rv);
            // error code not set - set "internal server error" (500)
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            response.setStatus(status);
            try {
                response.sendError(status, rv.toString());
            } catch (IOException e) {
                // can be ignored
                LOG.error(e.getLocalizedMessage(), e);
            }
        } catch (Throwable t) {
            // error code not set - set "internal server error" (500)
            LOG.error(t.getLocalizedMessage(), t);
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            response.setStatus(status);
            try {
                response.sendError(status, t.toString());
            } catch (IOException e) {
                // can be ignored
                LOG.error(e.getLocalizedMessage(), e);
            }
        } finally {
            // remove the thread local cms context
            setCms(null);
        }
    }

    /**
     * @see com.vaadin.server.VaadinServlet#servletInitialized()
     */
    @Override
    protected void servletInitialized() throws ServletException {

        super.servletInitialized();
        getService().addSessionInitListener(new SessionInitListener() {

            private static final long serialVersionUID = -3191245142912338247L;

            public void sessionInit(SessionInitEvent event) {

                event.getSession().addBootstrapListener(new BootstrapListener() {

                    private static final long serialVersionUID = -6249561809984101044L;

                    public void modifyBootstrapFragment(BootstrapFragmentResponse response) {

                        // nothing to do
                    }

                    public void modifyBootstrapPage(BootstrapPageResponse response) {

                        CmsObject cms = ((CmsUIServlet)getCurrent()).getCmsObject();
                        // inserting prefetched data into the page
                        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
                        response.getDocument().head().appendElement("meta").attr("name", "gwt:property").attr(
                            "content",
                            wpLocale.toString());
                        try {
                            response.getDocument().head().appendElement("meta").attr("name", CmsCoreData.DICT_NAME).attr(
                                "content",
                                RPC.encodeResponseForSuccess(
                                    I_CmsCoreService.class.getMethod("prefetch"),
                                    CmsCoreService.prefetch((HttpServletRequest)response.getRequest()),
                                    CmsPrefetchSerializationPolicy.instance()));
                        } catch (Exception e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }

                        // inserting client messages into the page
                        Element script = new Element(Tag.valueOf("script"), "").attr("type", "text/javascript");
                        response.getDocument().head().appendChild(script);
                        script.appendChild(new DataNode("//<![CDATA[\n"
                            + org.opencms.gwt.ClientMessages.get().export(wpLocale, false)
                            + "\n//]]>", script.baseUri()));

                    }
                });
            }
        });
    }

}
