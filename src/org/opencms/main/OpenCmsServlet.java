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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.site.CmsSite;
import org.opencms.staticexport.CmsStaticExportData;
import org.opencms.staticexport.CmsStaticExportRequest;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsThreadLocalStack;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * This the main servlet of the OpenCms system.<p>
 *
 * From here, all operations that are results of HTTP requests are invoked.
 * Any incoming request is handled in multiple steps:
 *
 * <ol><li>The requesting <code>{@link org.opencms.file.CmsUser}</code> is authenticated
 * and a <code>{@link org.opencms.file.CmsObject}</code> with this users context information
 * is created. This <code>{@link org.opencms.file.CmsObject}</code> is used to access all functions of OpenCms, limited by
 * the authenticated users permissions. If the user is not identified, it is set to the default user, usually named "Guest".</li>
 *
 * <li>The requested <code>{@link org.opencms.file.CmsResource}</code> is loaded into OpenCms and depending on its type
 * (and the users persmissions to display or modify it),
 * it is send to one of the OpenCms <code>{@link org.opencms.loader.I_CmsResourceLoader}</code> implementations
 * do be processed.</li>
 *
 * <li>
 * The <code>{@link org.opencms.loader.I_CmsResourceLoader}</code> will then decide what to do with the
 * contents of the requested <code>{@link org.opencms.file.CmsResource}</code>.
 * In case of a JSP resource the JSP handling mechanism is invoked with the <code>{@link org.opencms.loader.CmsJspLoader}</code>,
 * in case of an image (or another static resource) this will be returned by the <code>{@link org.opencms.loader.CmsDumpLoader}</code>
 * etc.
 * </li></ol>
 *
 * @since 6.0.0
 *
 * @see org.opencms.main.CmsShell
 * @see org.opencms.file.CmsObject
 * @see org.opencms.main.OpenCms
 */
public class OpenCmsServlet extends HttpServlet implements I_CmsRequestHandler {

    /**
     * Context class for storing request-dependent caches etc.
     */
    public static class RequestCache {

        /** Cache for sitemap configurations. */
        private Map<String, CmsADEConfigData> m_configCache = new HashMap<>();

        /** Map of attributes. */
        private Map<String, Object> m_attributes = new HashMap<>();

        /** Buffer for log messages to write at the end of the request. */
        private CmsDuplicateRemovingLogBuffer m_logBuffer = new CmsDuplicateRemovingLogBuffer();

        /**
         * Adds a log message to the log buffer.
         *
         * @param channel the channel
         * @param level the log level
         * @param message the message
         */
        public void addLog(String channel, String level, String message) {

            m_logBuffer.add(channel, level, message);
        }

        /**
         * Called at the end of the request.
         */
        public void close() {

            m_logBuffer.flush();
        }

        /**
         * Gets the attribute for the given key.
         *
         * @param key the key
         * @return the attribute
         */
        public Object getAttribute(String key) {

            return m_attributes.get(key);
        }

        /**
         * Gets the stored attribute for the given key, or lazily initializes it with the given provider function if it doesn't exist yet.
         *
         * @param key the key
         * @param provider the function to lazily initialize the entry
         * @return the attribute
         */
        public Object getAttribute(String key, Function<String, Object> provider) {

            return m_attributes.computeIfAbsent(key, provider);
        }

        /**
         * Gets the cached sitemap configuration data.
         *
         * @param key the key
         * @return the sitemap config from the cache
         */
        public CmsADEConfigData getCachedConfig(String key) {

            return m_configCache.get(key);
        }

        /**
         * Sets the attribute for the given key.
         *
         * @param key the key
         * @param value the attribute value
         */
        public void setAttribute(String key, Object value) {

            m_attributes.put(key, value);
        }

        /**
         * Sets the cached sitemap configuration data.
         *
         * @param key the key
         * @param config the sitemap configuration to cache
         */
        public void setCachedConfig(String key, CmsADEConfigData config) {

            m_configCache.put(key, config);
        }

    }

    /**
     * Debugging information about currently running requests.
     */
    public static class RequestInfo {

        /** Start time of the request. */
        private long m_startTime;

        /** URI of the request. */
        private String m_uri;

        /** Thread id of the thread handling the request. */
        private long m_threadId;

        /**
         * Creates a new instance.
         *
         * @param uri the URI
         * @param startTime the start time
         */
        public RequestInfo(String uri, long startTime) {

            m_startTime = startTime;
            m_uri = uri;
            m_threadId = Thread.currentThread().getId();
        }

        /**
         * Gets the request start time.
         *
         * @return the start time
         */
        public long getStartTime() {

            return m_startTime;
        }

        /**
         * Gets the thread id
         *
         * @return the thread id
         */
        public long getThreadId() {

            return m_threadId;
        }

        /**
         * Gets the URI of the request.
         *
         * @return the URI of the request
         */
        public String getUri() {

            return m_uri;
        }
    }

    /** The current request in a threadlocal. */
    public static final CmsThreadLocalStack<HttpServletRequest> currentRequestStack = new CmsThreadLocalStack<HttpServletRequest>();

    /** Map containing beans with information about currently running requests. */
    public static final ConcurrentHashMap<CmsUUID, RequestInfo> activeRequests = new ConcurrentHashMap<>();

    /** The current thread context for the request. */
    private static final CmsThreadLocalStack<RequestCache> requestCacheStack = new CmsThreadLocalStack<RequestCache>();

    /** GWT RPC services suffix. */
    public static final String HANDLE_GWT = ".gwt";

    /** Handler prefix. */
    public static final String HANDLE_PATH = "/handle";

    /** Name of the <code>DefaultWebApplication</code> parameter in the <code>web.xml</code> OpenCms servlet configuration. */
    public static final String SERVLET_PARAM_DEFAULT_WEB_APPLICATION = "DefaultWebApplication";

    /** Name of the <code>OpenCmsHome</code> parameter in the <code>web.xml</code> OpenCms servlet configuration. */
    public static final String SERVLET_PARAM_OPEN_CMS_HOME = "OpenCmsHome";

    /** Name of the <code>OpenCmsServlet</code> parameter in the <code>web.xml</code> OpenCms servlet configuration. */
    public static final String SERVLET_PARAM_OPEN_CMS_SERVLET = "OpenCmsServlet";

    /** Name of the <code>WebApplicationContext</code> parameter in the <code>web.xml</code> OpenCms servlet configuration. */
    public static final String SERVLET_PARAM_WEB_APPLICATION_CONTEXT = "WebApplicationContext";

    /** Path to handler "error page" files in the VFS. */
    private static final String HANDLE_VFS_PATH = "/system/handler" + HANDLE_PATH;

    /** Handler "error page" file suffix. */
    private static final String HANDLE_VFS_SUFFIX = ".html";

    /** Handler implementation names. */
    private static final String[] HANDLER_NAMES = {"404"};

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(OpenCmsServlet.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 4729951599966070050L;

    /** URL prefix for the built-in service handler. */
    public static final String HANDLE_BUILTIN_SERVICE = "/handleBuiltinService/";

    /**
     * Gets a thread-local, request-specific context object for requests made to the OpenCms servlet.
     *
     * @return the thread context
     */
    public static RequestCache getRequestCache() {

        RequestCache result = requestCacheStack.top();
        return result;
    }

    /**
     * Helper method used to conveniently access the request cache, does nothing if the request cache isn't set.
     *
     * @param handler the handler to pass the request cache to
     */
    public static void withRequestCache(Consumer<RequestCache> handler) {

        RequestCache cache = getRequestCache();
        if (cache != null) {
            handler.accept(cache);
        }
    }

    /**
     * OpenCms servlet main request handling method.<p>
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        // we are using stacks for these because the doGet method may be called reentrantly, e.g. when using servlet forwarding.
        currentRequestStack.push(req);
        requestCacheStack.push(new RequestCache());
        final CmsUUID requestId = new CmsUUID();
        RequestInfo reqInfo = new RequestInfo(
            req.getRequestURL().toString() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""),
            System.currentTimeMillis());
        activeRequests.put(requestId, reqInfo);
        try {

            // check to OpenCms runlevel
            int runlevel = OpenCmsCore.getInstance().getRunLevel();

            // write OpenCms server identification in the response header
            res.setHeader(CmsRequestUtil.HEADER_SERVER, OpenCmsCore.getInstance().getSystemInfo().getVersion());

            if (runlevel != OpenCms.RUNLEVEL_4_SERVLET_ACCESS) {
                // not the "normal" servlet runlevel
                if (runlevel == OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
                    // we have shell runlevel only, upgrade to servlet runlevel (required after setup wizard)
                    init(getServletConfig());
                } else {
                    // illegal runlevel, we can't process requests
                    // sending status code 403, indicating the server understood the request but refused to fulfill it
                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                    // goodbye
                    return;
                }
            }

            String path = OpenCmsCore.getPathInfo(req);
            if (path.startsWith(HANDLE_BUILTIN_SERVICE)) {
                // built-in services are for small AJAX-related functionality in the core that doesn't need to be configurable
                String remainder = path.substring(HANDLE_BUILTIN_SERVICE.length() - 1); // we want a leading slash in remainder
                OpenCmsCore.getInstance().invokeBuiltinService(remainder, req, res);
            } else if (path.startsWith(HANDLE_PATH)) {
                // this is a request to an OpenCms handler URI
                invokeHandler(req, res);
            } else if (path.endsWith(HANDLE_GWT)) {
                // handle GWT rpc services
                String serviceName = CmsResource.getName(path);
                serviceName = serviceName.substring(0, serviceName.length() - HANDLE_GWT.length());
                OpenCmsCore.getInstance().invokeGwtService(serviceName, req, res, getServletConfig());
            } else {
                // standard request to a URI in the OpenCms VFS
                OpenCmsCore.getInstance().showResource(req, res);
            }
        } finally {
            currentRequestStack.pop();
            requestCacheStack.pop().close();
            activeRequests.remove(requestId);
        }
    }

    /**
     * OpenCms servlet POST request handling method,
     * will just call {@link #doGet(HttpServletRequest, HttpServletResponse)}.<p>
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        doGet(req, res);
    }

    /**
     * @see org.opencms.main.I_CmsRequestHandler#getHandlerNames()
     */
    public String[] getHandlerNames() {

        return HANDLER_NAMES;
    }

    /**
     * @see org.opencms.main.I_CmsRequestHandler#handle(HttpServletRequest, HttpServletResponse, String)
     */
    public void handle(HttpServletRequest req, HttpServletResponse res, String name)
    throws IOException, ServletException {

        int errorCode;
        try {
            errorCode = Integer.valueOf(name).intValue();
        } catch (NumberFormatException nf) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            LOG.debug("Error parsing handler name.", nf);
            return;
        }
        switch (errorCode) {
            case 404:
                CmsObject cms = null;
                CmsStaticExportData exportData = null;
                try {
                    // this will be set in the root site
                    cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
                    exportData = OpenCms.getStaticExportManager().getExportData(req, cms);
                } catch (CmsException e) {
                    // unlikely to happen
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_INIT_CMSOBJECT_IN_HANDLER_2,
                                name,
                                OpenCmsCore.getPathInfo(req)),
                            e);
                    }
                }
                if (exportData != null) {
                    try {
                        // generate a static export request wrapper
                        CmsStaticExportRequest exportReq = new CmsStaticExportRequest(req, exportData);
                        // export the resource and set the response status according to the result
                        res.setStatus(OpenCms.getStaticExportManager().export(exportReq, res, cms, exportData));
                    } catch (Throwable t) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().getBundle().key(Messages.LOG_ERROR_EXPORT_1, exportData), t);
                        }
                        openErrorHandler(req, res, errorCode);
                    }
                } else {
                    openErrorHandler(req, res, errorCode);
                }
                break;
            default:
                openErrorHandler(req, res, errorCode);
        }
    }

    /**
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public synchronized void init(ServletConfig config) throws ServletException {

        super.init(config);
        try {
            // upgrade the runlevel
            // usually this should have already been done by the context listener
            // however, after a fresh install / setup this will be done from here
            OpenCmsCore.getInstance().upgradeRunlevel(config.getServletContext());
            // finalize OpenCms initialization
            OpenCmsCore.getInstance().initServlet(this);
        } catch (CmsInitException e) {
            if (Messages.ERR_CRITICAL_INIT_WIZARD_0.equals(e.getMessageContainer().getKey())) {
                // if wizard is still enabled - allow retry of initialization (required for setup wizard)
                // this means the servlet init() call must be terminated by an exception
                if (CmsServletContainerSettings.isServletThrowsException()) {
                    throw new ServletException(e.getMessage());
                } else {
                    // this is needed since some servlet containers does not like the servlet to throw exceptions,
                    // like BEA WLS 9.x and Resin
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_ERROR_GENERIC_0), e);
                }
            }
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERROR_GENERIC_0), t);
        }
    }

    /**
     * Manages requests to internal OpenCms request handlers.<p>
     *
     * @param req the current request
     * @param res the current response
     * @throws ServletException in case an error occurs
     * @throws ServletException in case an error occurs
     * @throws IOException in case an error occurs
     */
    protected void invokeHandler(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        String pathInfo = OpenCmsCore.getPathInfo(req);
        String name = pathInfo.substring(HANDLE_PATH.length());
        I_CmsRequestHandler handler = OpenCmsCore.getInstance().getRequestHandler(name);
        if ((handler == null) && name.contains("/")) {
            // if the name contains a '/', also check for handlers matching the first path fragment only
            name = name.substring(0, name.indexOf("/"));
            handler = OpenCmsCore.getInstance().getRequestHandler(name);
        }
        if (handler != null) {
            handler.handle(req, res, name);
        } else {
            LOG.warn("Invalid request handler call: " + pathInfo);
            openErrorHandler(req, res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Displays an error code handler loaded from the OpenCms VFS,
     * or if such a page does not exist,
     * displays the default servlet container error code.<p>
     *
     * @param req the current request
     * @param res the current response
     * @param errorCode the error code to display
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    protected void openErrorHandler(HttpServletRequest req, HttpServletResponse res, int errorCode)
    throws IOException, ServletException {

        String handlerUri = (new StringBuffer(64)).append(HANDLE_VFS_PATH).append(errorCode).append(
            HANDLE_VFS_SUFFIX).toString();
        // provide the original error code in a request attribute
        req.setAttribute(CmsRequestUtil.ATTRIBUTE_ERRORCODE, Integer.valueOf(errorCode));
        CmsObject cms;
        CmsFile file;
        try {
            // create OpenCms context, this will be set in the root site
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            cms.getRequestContext().setSecureRequest(OpenCms.getSiteManager().usesSecureSite(req));
        } catch (CmsException e) {
            // unlikely to happen as the OpenCms "Guest" context can always be initialized
            CmsMessageContainer container = Messages.get().container(
                Messages.LOG_INIT_CMSOBJECT_IN_HANDLER_2,
                Integer.valueOf(errorCode),
                handlerUri);
            if (LOG.isWarnEnabled()) {
                LOG.warn(org.opencms.jsp.Messages.getLocalizedMessage(container, req), e);
            }
            // however, if it _does_ happen, then we really can't continue here
            if (!res.isCommitted()) {
                // since the handler file is not accessible, display the default error page
                res.sendError(errorCode, e.getLocalizedMessage());
            }
            return;
        }
        try {
            if (!tryCustomErrorPage(cms, req, res, errorCode)) {
                cms.getRequestContext().setUri(handlerUri);
                cms.getRequestContext().setSecureRequest(OpenCms.getSiteManager().usesSecureSite(req));
                // read the error handler file
                file = cms.readFile(handlerUri, CmsResourceFilter.IGNORE_EXPIRATION);
                OpenCms.getResourceManager().loadResource(cms, file, req, res);
            }
        } catch (CmsException e) {
            // unable to load error page handler VFS resource
            CmsMessageContainer container = Messages.get().container(
                Messages.ERR_SHOW_ERR_HANDLER_RESOURCE_2,
                Integer.valueOf(errorCode),
                handlerUri);
            throw new ServletException(org.opencms.jsp.Messages.getLocalizedMessage(container, req), e);
        }
    }

    /**
     * Tries to load the custom error page at the given rootPath.
     * @param cms {@link CmsObject} used for reading the resource (site root and uri get adjusted!)
     * @param req the current request
     * @param res the current response
     * @param rootPath the VFS root path to the error page resource
     * @return a flag, indicating if the error page could be loaded
     */
    private boolean loadCustomErrorPage(
        CmsObject cms,
        HttpServletRequest req,
        HttpServletResponse res,
        String rootPath) {

        try {

            // get the site of the error page resource
            CmsSite errorSite = OpenCms.getSiteManager().getSiteForRootPath(rootPath);
            cms.getRequestContext().setSiteRoot(errorSite.getSiteRoot());
            String relPath = cms.getRequestContext().removeSiteRoot(rootPath);
            if (cms.existsResource(relPath)) {
                cms.getRequestContext().setUri(relPath);
                OpenCms.getResourceManager().loadResource(cms, cms.readResource(relPath), req, res);
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            // something went wrong log the exception and return false
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Tries to load a site specific error page. If
     * @param cms {@link CmsObject} used for reading the resource (site root and uri get adjusted!)
     * @param req the current request
     * @param res the current response
     * @param errorCode the error code to display
     * @return a flag, indicating if the custom error page could be loaded.
     */
    private boolean tryCustomErrorPage(CmsObject cms, HttpServletRequest req, HttpServletResponse res, int errorCode) {

        String siteRoot = OpenCms.getSiteManager().matchRequest(req).getSiteRoot();
        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        if (site != null) {
            // store current site root and URI
            String currentSiteRoot = cms.getRequestContext().getSiteRoot();
            String currentUri = cms.getRequestContext().getUri();
            try {
                if (site.getErrorPage() != null) {
                    String rootPath = site.getErrorPage();
                    if (loadCustomErrorPage(cms, req, res, rootPath)) {
                        return true;
                    }
                }
                String rootPath = CmsStringUtil.joinPaths(siteRoot, "/.errorpages/handle" + errorCode + ".html");
                if (loadCustomErrorPage(cms, req, res, rootPath)) {
                    return true;
                }
            } finally {
                cms.getRequestContext().setSiteRoot(currentSiteRoot);
                cms.getRequestContext().setUri(currentUri);
            }
        }
        return false;
    }
}