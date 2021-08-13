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

import org.opencms.crypto.CmsEncryptionException;
import org.opencms.file.CmsObject;
import org.opencms.gwt.CmsCoreService;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.i18n.CmsMessages;
import org.opencms.security.CmsDefaultAuthorizationHandler;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.ui.Messages;
import org.opencms.ui.login.CmsLoginUI;
import org.opencms.ui.shared.CmsVaadinConstants;
import org.opencms.util.CmsRequestUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceLoginHandler;
import org.opencms.workplace.CmsWorkplaceManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ApplicationConstants;
import com.vaadin.ui.UI;

/**
 * Servlet for workplace UI requests.<p>
 */
public class CmsUIServlet extends VaadinServlet implements SystemMessagesProvider, SessionInitListener {

    /** The bootstrap listener. */
    static final BootstrapListener BOOTSTRAP_LISTENER = new BootstrapListener() {

        private static final long serialVersionUID = -6249561809984101044L;

        public void modifyBootstrapFragment(BootstrapFragmentResponse response) {

            // nothing to do
        }

        public void modifyBootstrapPage(BootstrapPageResponse response) {

            CmsCoreService svc = new CmsCoreService();
            HttpServletRequest request = (HttpServletRequest)VaadinService.getCurrentRequest();
            svc.setRequest(request);
            CmsObject cms = ((CmsUIServlet)getCurrent()).getCmsObject();
            svc.setCms(cms);

            Document doc = response.getDocument();
            Elements appLoadingElements = doc.getElementsByClass("v-app-loading");
            if (appLoadingElements.size() > 0) {
                for (Node node : appLoadingElements.get(0).childNodes()) {
                    node.remove();

                }

                appLoadingElements.get(0).append(CmsVaadinConstants.LOADING_INDICATOR_HTML);
            }

            if (shouldShowLogin()) {
                try {
                    String html = CmsLoginUI.generateLoginHtmlFragment(cms, response.getRequest());
                    Element el = new Element(Tag.valueOf("div"), "").html(html);
                    doc.body().appendChild(el);
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            Locale currentLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            // Inject CmsCoreData etc. for GWT dialogs
            try {
                doc.head().append(CmsGwtActionElement.exportCommon(cms, svc.prefetch()));
                doc.head().append(org.opencms.ade.publish.ClientMessages.get().export(currentLocale, true));
                doc.head().append(org.opencms.ade.upload.ClientMessages.get().export(currentLocale, true));
                doc.head().append(org.opencms.ade.galleries.ClientMessages.get().export(currentLocale, true));
                for (String cssURI : OpenCms.getWorkplaceAppManager().getWorkplaceCssUris()) {
                    doc.head().append("<link rel=\"stylesheet\" href=\"" + CmsWorkplace.getResourceUri(cssURI) + "\">");
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    };

    /** The static log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsUIServlet.class);

    /** The login UI provider, overrides the default UI to display the login dialog when required. */
    static final UIProvider LOGIN_UI_PROVIDER = new UIProvider() {

        private static final long serialVersionUID = 9154828335594149982L;

        @Override
        public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {

            if (shouldShowLogin() || isLoginUIRequest(event.getRequest())) {
                return CmsLoginUI.class;
            }
            return null;
        }
    };

    /** The login redirect handler. */
    static final RequestHandler REQUEST_AUTHORIZATION_HANDLER = new RequestHandler() {

        private static final long serialVersionUID = 1L;

        public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
        throws IOException {

            if (shouldShowLogin() && !isLoginUIRequest(request)) {

                String link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                    ((CmsUIServlet)getCurrent()).getCmsObject(),
                    CmsWorkplaceLoginHandler.LOGIN_FORM);
                String requestedUri = ((HttpServletRequest)request).getRequestURI();
                if (!requestedUri.endsWith(OpenCms.getSystemInfo().getWorkplaceContext())) {
                    try {
                        link += "?"
                            + CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE
                            + "="
                            + URLEncoder.encode(requestedUri, "UTF-8")
                            + "&"
                            + CmsDefaultAuthorizationHandler.PARAM_ENCRYPTED_REQUESTED_RESOURCE
                            + "="
                            + OpenCms.getDefaultTextEncryption().encrypt(requestedUri);
                    } catch (CmsEncryptionException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
                OpenCms.getAuthorizationHandler().requestAuthorization(
                    (HttpServletRequest)request,
                    (HttpServletResponse)response,
                    link);
                return true;
            }
            return false;
        }
    };

    /** Serialization id. */
    private static final long serialVersionUID = 8119684308154724518L;

    // install the slf4j bridge to pipe vaadin logging to log4j
    static {
        SLF4JBridgeHandler.install();
    }

    /** The VAADIN heartbeat request path prefix. */
    private static final String HEARTBEAT_PREFIX = '/' + ApplicationConstants.HEARTBEAT_PATH + '/';

    /** The current CMS context. */
    private ThreadLocal<CmsObject> m_perThreadCmsObject = new ThreadLocal<>();

    /** Map of stored system messages objects. */
    private Map<Locale, SystemMessages> m_systemMessages = new ConcurrentHashMap<Locale, SystemMessages>();

    /** Stores whether the current request is a broadcast poll. */
    private ThreadLocal<Boolean> m_perThreadBroadcastPoll = new ThreadLocal<>();

    /**
     * Checks whether the given request was referred from the login page.<p>
     *
     * @param request the request
     *
     * @return <code>true</code> in case of login ui requests
     */
    static boolean isLoginUIRequest(VaadinRequest request) {

        String referrer = request.getHeader("referer");
        return (referrer != null) && referrer.contains(CmsWorkplaceLoginHandler.LOGIN_HANDLER);
    }

    /**
     * Returns whether the login dialog should be shown.<p>
     *
     * @return <code>true</code> if the login dialog should be shown
     */
    static boolean shouldShowLogin() {

        return ((CmsUIServlet)getCurrent()).getCmsObject().getRequestContext().getCurrentUser().isGuestUser();
    }

    /**
     * Returns the current cms context.<p>
     *
     * @return the current cms context
     */
    public CmsObject getCmsObject() {

        return m_perThreadCmsObject.get();
    }

    /**
     * @see com.vaadin.server.SystemMessagesProvider#getSystemMessages(com.vaadin.server.SystemMessagesInfo)
     */
    public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {

        Locale locale = systemMessagesInfo.getLocale();
        if (!m_systemMessages.containsKey(locale)) {
            m_systemMessages.put(locale, createSystemMessages(locale));
        }
        return m_systemMessages.get(locale);
    }

    /**
     * @see com.vaadin.server.SessionInitListener#sessionInit(com.vaadin.server.SessionInitEvent)
     */
    public void sessionInit(final SessionInitEvent event) {

        // set the locale to the users workplace locale
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        event.getSession().setLocale(wpLocale);
        event.getSession().addRequestHandler(REQUEST_AUTHORIZATION_HANDLER);
        event.getSession().addUIProvider(LOGIN_UI_PROVIDER);
        event.getSession().addBootstrapListener(BOOTSTRAP_LISTENER);
    }

    /**
     * Sets that the current request is a broadcast call.<p>
     */
    public void setBroadcastPoll() {

        m_perThreadBroadcastPoll.set(Boolean.TRUE);
    }

    /**
     * Sets the current cms context.<p>
     *
     * @param cms the current cms context to set
     */
    public synchronized void setCms(CmsObject cms) {

        m_perThreadCmsObject.set(cms);
    }

    /**
     * @see com.vaadin.server.VaadinServlet#createServletService(com.vaadin.server.DeploymentConfiguration)
     */
    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration)
    throws ServiceException {

        CmsVaadinServletService service = new CmsVaadinServletService(this, deploymentConfiguration);
        service.init();
        return service;
    }

    /**
     * @see com.vaadin.server.VaadinServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        CmsRequestUtil.disableCrossSiteFrameEmbedding(response);
        if (request.getRequestURI().contains("/VAADIN")) {
            super.service(request, response);
            return;
        }
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

        // check if the given request matches the workplace site
        if ((OpenCms.getSiteManager().getSites().size() > 1) && !OpenCms.getSiteManager().isWorkplaceRequest(request)) {

            // do not send any redirects to the workplace site for security reasons
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            OpenCmsCore.getInstance().initCmsContextForUI(request, response, this);
            super.service(request, response);
            OpenCms.getSessionManager().updateSessionInfo(getCmsObject(), request, isHeartbeatRequest(request));
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
        } catch (IOException io) {
            // probably connection aborted by client, no need to write to the ERROR channel
            LOG.warn(io.getLocalizedMessage(), io);
            // try so set status and send error in any case
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            response.setStatus(status);
            try {
                response.sendError(status, io.toString());
            } catch (Exception e) {
                // can be ignored
            }
        } catch (Throwable t) {
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
            clearThreadLocal();
        }
    }

    /**
     * @see com.vaadin.server.VaadinServlet#servletInitialized()
     */
    @Override
    protected void servletInitialized() throws ServletException {

        super.servletInitialized();
        getService().setSystemMessagesProvider(this);
        getService().addSessionInitListener(this);
    }

    /**
     * Clears the thread local storage.<p>
     */
    private void clearThreadLocal() {

        m_perThreadCmsObject.set(null);
        m_perThreadBroadcastPoll.remove();
    }

    /**
     * Returns a system messages instance for the given locale.<p>
     *
     * @param locale the locale
     *
     * @return the system messages
     */
    private SystemMessages createSystemMessages(Locale locale) {

        CmsMessages messages = Messages.get().getBundle(locale);
        CustomizedSystemMessages systemMessages = new CustomizedSystemMessages();
        systemMessages.setCommunicationErrorCaption(messages.key(Messages.GUI_SYSTEM_COMMUNICATION_ERROR_CAPTION_0));
        systemMessages.setCommunicationErrorMessage(messages.key(Messages.GUI_SYSTEM_COMMUNICATION_ERROR_MESSAGE_0));
        systemMessages.setCommunicationErrorNotificationEnabled(true);
        systemMessages.setAuthenticationErrorCaption(messages.key(Messages.GUI_SYSTEM_AUTHENTICATION_ERROR_CAPTION_0));
        systemMessages.setAuthenticationErrorMessage(messages.key(Messages.GUI_SYSTEM_AUTHENTICATION_ERROR_MESSAGE_0));
        systemMessages.setAuthenticationErrorNotificationEnabled(true);
        systemMessages.setSessionExpiredCaption(messages.key(Messages.GUI_SYSTEM_SESSION_EXPIRED_ERROR_CAPTION_0));
        systemMessages.setSessionExpiredMessage(messages.key(Messages.GUI_SYSTEM_SESSION_EXPIRED_ERROR_MESSAGE_0));
        systemMessages.setSessionExpiredNotificationEnabled(true);
        systemMessages.setInternalErrorCaption(messages.key(Messages.GUI_SYSTEM_INTERNAL_ERROR_CAPTION_0));
        systemMessages.setInternalErrorMessage(messages.key(Messages.GUI_SYSTEM_INTERNAL_ERROR_MESSAGE_0));
        systemMessages.setInternalErrorNotificationEnabled(true);
        return systemMessages;
    }

    /**
     * Checks whether the given request is a heartbeat request.<p>
     *
     * @param request the request
     *
     * @return <code>true</code> in case of VAADIN heartbeat requests
     */
    private boolean isHeartbeatRequest(HttpServletRequest request) {

        if ((m_perThreadBroadcastPoll.get() != null) && m_perThreadBroadcastPoll.get().booleanValue()) {
            return true;
        }
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) && pathInfo.startsWith(HEARTBEAT_PREFIX);
    }
}
