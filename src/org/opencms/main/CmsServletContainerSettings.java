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

import org.opencms.util.A_CmsModeStringEnumeration;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;

/**
 * Stores specific servlet container options, that might influence OpenCms behavior.<p>
 *
 * @since 7.0.5
 */
public class CmsServletContainerSettings {

    /**
     *  Enumeration class for the configuration mode.<p>
     */
    public static final class CmsServletContainerCfgMode extends A_CmsModeStringEnumeration {

        /** Auto configuration mode. */
        protected static final CmsServletContainerCfgMode MODE_AUTO = new CmsServletContainerCfgMode("auto");

        /** Manual configuration mode. */
        protected static final CmsServletContainerCfgMode MODE_MANUAL = new CmsServletContainerCfgMode("manual");

        /** No set configuration mode. */
        protected static final CmsServletContainerCfgMode MODE_NONE = new CmsServletContainerCfgMode("none");

        /** Version id required for safe serialization. */
        private static final long serialVersionUID = -8191582624108081577L;

        /**
         * Private constructor.<p>
         *
         * @param mode the remote command execution return type integer representation
         */
        private CmsServletContainerCfgMode(String mode) {

            super(mode);
        }

        /**
         * Returns the parsed mode object if the string representation matches, or <code>null</code> if not.<p>
         *
         * @param mode the string representation to parse
         *
         * @return the parsed mode object
         */
        public static CmsServletContainerCfgMode valueOf(String mode) {

            if (mode == null) {
                return null;
            }
            if (mode.equalsIgnoreCase(MODE_NONE.getMode())) {
                return MODE_NONE;
            }
            if (mode.equalsIgnoreCase(MODE_MANUAL.getMode())) {
                return MODE_MANUAL;
            }
            if (mode.equalsIgnoreCase(MODE_AUTO.getMode())) {
                return MODE_AUTO;
            }
            return null;
        }

        /**
         * Checks if this is the auto mode.<p>
         *
         * @return <code>true</code> if this is the auto mode
         */
        public boolean isAuto() {

            return this == MODE_AUTO;
        }

        /**
         * Checks if this is the manual mode.<p>
         *
         * @return <code>true</code> if this is the manual mode
         */
        public boolean isManual() {

            return this == MODE_MANUAL;
        }

        /**
         * Checks if this is the none mode.<p>
         *
         * @return <code>true</code> if this is the none mode
         */
        public boolean isNone() {

            return this == MODE_NONE;
        }
    }

    /** String remote command execution return type. */
    public static final CmsServletContainerCfgMode CFG_MODE_AUTO = CmsServletContainerCfgMode.MODE_AUTO;

    /** Map remote command execution return type. */
    public static final CmsServletContainerCfgMode CFG_MODE_MANUAL = CmsServletContainerCfgMode.MODE_MANUAL;

    /** List remote command execution return type. */
    public static final CmsServletContainerCfgMode CFG_MODE_NONE = CmsServletContainerCfgMode.MODE_NONE;

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsServletContainerSettings.class);

    /** If the servlet can throw an exception if initialization fails, for instance, Weblogic and Resin have problems with the exception. */
    private static boolean m_servletThrowsException = true;

    /**
     * The replacement request attribute for the {@link javax.servlet.http.HttpServletRequest#getPathInfo()} method,
     * which is needed because this method is not properly implemented in BEA WLS 9.x.<p>
     */
    private static final String REQUEST_ERROR_PAGE_ATTRIBUTE_WEBLOGIC = "weblogic.servlet.errorPage";

    /** Constant name to identify GlassFish servers. */
    // 2.1: "Sun GlassFish Enterprise Server v2.1"
    private static final String SERVLET_CONTAINER_GLASSFISH = "GlassFish";

    /** Constant name to identify Resin servers. */
    private static final String SERVLET_CONTAINER_RESIN = "Resin";

    /** Constant name to identify BEA WebLogic servers. */
    private static final String SERVLET_CONTAINER_WEBLOGIC = "WebLogic Server";

    /** Constant name to identify IBM Websphere servers. */
    private static final String SERVLET_CONTAINER_WEBSPHERE = "IBM WebSphere Application Server";

    /** The context path of the web application. */
    private String m_contextPath;

    /** The default web application (usually "ROOT"). */
    private String m_defaultWebApplicationName;

    /** The configuration mode. */
    private CmsServletContainerCfgMode m_mode = CFG_MODE_NONE;

    /** The OpenCms context and servlet path, e.g. <code>/opencms/opencms</code>. */
    private String m_openCmsContext;

    /** If the flex response has to prevent buffer flushing, for instance, Websphere does not allow to set headers afterwards, so we have to prevent it. */
    private boolean m_preventResponseFlush;

    /** If the tags need to be released after ending, this has to be prevented when running with Resin, for example. */
    private boolean m_releaseTagsAfterEnd = true;

    /**
       * The request error page attribute to use if {@link javax.servlet.http.HttpServletRequest#getPathInfo()}
       * is not working properly, like in BEA WLS 9.x.
       */
    private String m_requestErrorPageAttribute;

    /** The name of the servlet container running OpenCms. */
    private String m_servletContainerName;

    /** The servlet path for the OpenCms servlet. */
    private String m_servletPath;

    /** The web application name. */
    private String m_webApplicationName;

    /** The OpenCms web application servlet container folder path (in the "real" file system). */
    private String m_webApplicationRfsPath;

    /** The OpenCms web application "WEB-INF" path (in the "real" file system). */
    private String m_webInfRfsPath;

    /**
     * Creates a new object.<p>
     *
     * @param context used to find out specifics of the servlet container
     */
    public CmsServletContainerSettings(ServletContext context) {

        // CmsSystemInfo<init> has to call this with null (for setup)
        if (context != null) {
            // check for OpenCms home (base) directory path
            String webInfRfsPath = context.getInitParameter(OpenCmsServlet.SERVLET_PARAM_OPEN_CMS_HOME);
            if (CmsStringUtil.isEmpty(webInfRfsPath)) {
                webInfRfsPath = CmsFileUtil.searchWebInfFolder(context.getRealPath("/"));
                if (CmsStringUtil.isEmpty(webInfRfsPath)) {
                    throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_FOLDER_0));
                }
            }

            // set the default web application name
            // read the the default name from the servlet context parameters
            String defaultWebApplication = context.getInitParameter("DefaultWebApplication");

            // read the the OpenCms servlet mapping from the servlet context parameters
            String servletMapping = context.getInitParameter(OpenCmsServlet.SERVLET_PARAM_OPEN_CMS_SERVLET);
            if (servletMapping == null) {
                throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_SERVLET_0));
            }

            // read the servlet container name
            String servletContainerName = context.getServerInfo();

            // web application context:
            // read it from the servlet context parameters
            //      this is needed in case an application server specific deployment descriptor is used to changed the webapp context
            String webApplicationContext = context.getInitParameter(
                OpenCmsServlet.SERVLET_PARAM_WEB_APPLICATION_CONTEXT);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(webApplicationContext)) {
                try {
                    URL contextRelativeUrl = context.getResource("/");
                    webApplicationContext = contextRelativeUrl.getPath();
                    String[] pathTokens = CmsStringUtil.splitAsArray(webApplicationContext, '/');
                    if (pathTokens.length == 1) {
                        /*
                         * There may be a "" context configured (e.g. in GlassFish).
                         */
                        webApplicationContext = "";
                    } else {

                        webApplicationContext = pathTokens[pathTokens.length - 1];
                    }
                } catch (MalformedURLException e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_INIT_CONTEXTNAME_0), e);
                }

            }
            // init values:
            init(webInfRfsPath, defaultWebApplication, servletMapping, servletContainerName, webApplicationContext);
            // finally care for the speciality of different servlet containers:
            initContainerSpecifics(context);
        }
    }

    /**
     * Creates an instance based on the given values.<p>
     *
     * This is intended for <code>{@link CmsShell}</code> access.<p>
     *
     * @param webInfRfsPath the OpenCms web application "WEB-INF" path in the "real" file system) to set
     * @param servletMapping the OpenCms servlet mapping  (e.g. "/opencms/*")
     * @param webApplicationContext the name/path of the OpenCms web application context (optional, will be calculated form the path if null)
     * @param defaultWebApplication the default web application name (usually "ROOT")
     * @param servletContainerName the name of the servlet container running OpenCms
     */
    protected CmsServletContainerSettings(
        String webInfRfsPath,
        String defaultWebApplication,
        String servletMapping,
        String servletContainerName,
        String webApplicationContext) {

        init(webInfRfsPath, defaultWebApplication, servletMapping, servletContainerName, webApplicationContext);
    }

    /**
     * Checks if the servlet can throw an exception if initialization fails.<p>
     *
     * @return <code>true</code> if the servlet can throw an exception if initialization fails
     */
    public static boolean isServletThrowsException() {

        return m_servletThrowsException;
    }

    /**
     * Returns the web application context path, e.g. "" (empty String) if the web application
     * is the default web application (usually "ROOT"), or "/opencms" if the web application
     * is called "opencms".<p>
     *
     * <i>From the Java Servlet Specification v2.4:</i><br>
     * <b>Context Path:</b> The path prefix associated with the ServletContext that this
     * servlet is a part of. If this context is the "default" context rooted at the base of
     * the web server's URL name space, this path will be an empty string. Otherwise,
     * if the context is not rooted at the root of the server's name space, the path starts
     * with a "/" character but does not end with a "/" character.<p>
     *
     * @return the web application context path
     * @see #getWebApplicationName()
     * @see #getServletPath()
     * @see #getOpenCmsContext()
     */
    public String getContextPath() {

        return m_contextPath;
    }

    /**
     * Returns the default web application name (usually "ROOT").<p>
     *
     * @return the default web application name
     */
    public String getDefaultWebApplicationName() {

        return m_defaultWebApplicationName;
    }

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public CmsServletContainerCfgMode getMode() {

        return m_mode;
    }

    /**
     * Returns the OpenCms request context, e.g. "/opencms/opencms".<p>
     *
     * The OpenCms context will always start with a "/" and never have a trailing "/".
     * The OpenCms context is identical to <code>getContexPath() + getServletPath()</code>.<p>
     *
     * @return the OpenCms request context, e.g. "/opencms/opencms"
     * @see #getContextPath()
     * @see #getServletPath()
     */
    public String getOpenCmsContext() {

        return m_openCmsContext;
    }

    /**
     * Returns the request error page attribute.<p>
     *
     * @return the request error page attribute
     */
    public String getRequestErrorPageAttribute() {

        return m_requestErrorPageAttribute;
    }

    /**
     * Returns the name of the servlet container running OpenCms.<p>
     *
     * @return the name of the servlet container running OpenCms
     */
    public String getServletContainerName() {

        return m_servletContainerName;
    }

    /**
     * Returns the OpenCms servlet path, e.g. "/opencms".<p>
     *
     * <i>From the Java Servlet Specification v2.4:</i><br>
     * <b>Servlet Path:</b> The path section that directly corresponds to the mapping
     * which activated this request. This path starts with a?/? character except in the
     * case where the request is matched with the ?/*? pattern, in which case it is the
     * empty string.<p>
     *
     * @return the OpenCms servlet path
     * @see #getContextPath()
     * @see #getWebApplicationName()
     * @see #getOpenCmsContext()
     */
    public String getServletPath() {

        return m_servletPath;
    }

    /**
     * Returns the OpenCms web application name, e.g. "opencms" or "ROOT" (no leading or trailing "/").<p>
     *
     * The web application name is stored for informational purposes only.
     * If you want to construct an URI, use either {@link #getContextPath()} and
     * {@link #getServletPath()}, or for links to the OpenCms VFS use {@link  #getOpenCmsContext()}.<p>
     *
     * @return the OpenCms web application name
     * @see #getContextPath()
     * @see #getServletPath()
     * @see #getOpenCmsContext()
     */
    public String getWebApplicationName() {

        return m_webApplicationName;
    }

    /**
     * Returns the OpenCms web application folder in the servlet container.<p>
     *
     * @return the OpenCms web application folder in the servlet container
     */
    public String getWebApplicationRfsPath() {

        return m_webApplicationRfsPath;
    }

    /**
     * Returns the OpenCms web application "WEB-INF" directory path.<p>
     *
     * @return the OpenCms web application "WEB-INF" directory path
     */
    public String getWebInfRfsPath() {

        return m_webInfRfsPath;
    }

    /**
     * Checks if the flex response has to prevent buffer flushing.<p>
     *
     * @return <code>true</code> if the flex response has to prevent buffer flushing
     */
    public boolean isPreventResponseFlush() {

        return m_preventResponseFlush;
    }

    /**
     * Checks if the tags need to be released after ending.<p>
     *
     * @return <code>true</code> if the tags need to be released after ending
     */
    public boolean isReleaseTagsAfterEnd() {

        return m_releaseTagsAfterEnd;
    }

    /**
     * Sets the mode from the configuration.<p>
     *
     * @param configValue the mode to set
     */
    public void setMode(String configValue) {

        m_mode = CmsServletContainerCfgMode.valueOf(configValue);
    }

    /**
     * Sets if the flex response has to prevent buffer flushing.<p>
     *
     * @param preventResponseFlush the flag to set
     */
    public void setPreventResponseFlush(boolean preventResponseFlush) {

        m_preventResponseFlush = preventResponseFlush;
    }

    /**
     * Sets if the tags need to be released after ending.<p>
     *
     * @param releaseTagsAfterEnd the flag to set
     */
    public void setReleaseTagsAfterEnd(boolean releaseTagsAfterEnd) {

        m_releaseTagsAfterEnd = releaseTagsAfterEnd;
    }

    /**
     * Sets the request error page attribute.<p>
     *
     * @param requestErrorPageAttribute the request error page attribute to set
     */
    public void setRequestErrorPageAttribute(String requestErrorPageAttribute) {

        m_requestErrorPageAttribute = requestErrorPageAttribute;
    }

    /**
     * Sets if the servlet can throw an exception if initialization fails.<p>
     *
     * @param servletThrowsException the flag to set
     */
    public void setServletThrowsException(boolean servletThrowsException) {

        m_servletThrowsException = servletThrowsException;
    }

    /**
     * Initialization code common to both constructors.<p>
     *
     * While the "webapplication - mode" constructor obtains all values from the
     * <code>{@link ServletContext}</code> the <code>{@link CmsShell}</code> constructor
     * accepts them as arguments and passes them here. <p>
     *
     * @param webInfRfsPath the OpenCms web application "WEB-INF" path in the "real" file system) to set
     * @param servletMapping the OpenCms servlet mapping  (e.g. "/opencms/*")
     * @param webApplicationContext the name/path of the OpenCms web application context (optional, will be calculated form the path if null)
     * @param defaultWebApplication the default web application name (usually "ROOT")
     * @param servletContainerName the name of the servlet container running OpenCms
     */
    private void init(
        String webInfRfsPath,
        String defaultWebApplication,
        String servletMapping,
        String servletContainerName,
        String webApplicationContext) {

        // WEB-INF RFS path

        webInfRfsPath = webInfRfsPath.replace('\\', '/');
        if (!webInfRfsPath.endsWith("/")) {
            webInfRfsPath = webInfRfsPath + "/";
        }
        m_webInfRfsPath = CmsFileUtil.normalizePath(webInfRfsPath);

        // default web application
        if (defaultWebApplication == null) {
            defaultWebApplication = "";
        }
        if (defaultWebApplication.endsWith("/")) {
            defaultWebApplication = defaultWebApplication.substring(0, defaultWebApplication.length() - 1);
        }
        if (defaultWebApplication.startsWith("/")) {
            defaultWebApplication = defaultWebApplication.substring(1);
        }
        m_defaultWebApplicationName = defaultWebApplication;

        // servlet mapping
        if (!servletMapping.startsWith("/")) {
            servletMapping = "/" + servletMapping;
        }
        if (servletMapping.endsWith("/*")) {
            // usually a mapping must be in the form "/opencms/*", cut off all slashes
            servletMapping = servletMapping.substring(0, servletMapping.length() - 2);
        }
        m_servletPath = servletMapping;

        // servlet container name
        if (servletContainerName == null) {
            m_servletContainerName = "";
        }
        m_servletContainerName = servletContainerName;

        // set the web application name
        File path = new File(m_webInfRfsPath);
        m_webApplicationName = path.getParentFile().getName();

        String contextPath = webApplicationContext;
        // whitespace is OK because e.g. on glassfish the "" context may be configured
        if (contextPath == null) {
            contextPath = m_webApplicationName;
            // this fixes an issue with context names in JBoss
            if (contextPath.endsWith(".war")) {
                contextPath = contextPath.substring(0, contextPath.length() - 4);
            }
        }
        // set the context path
        if (contextPath.equals(getDefaultWebApplicationName()) || "".equals(contextPath)) {
            m_contextPath = "";
        } else {
            m_contextPath = "/" + contextPath;
        }
        // set the OpenCms context
        m_openCmsContext = m_contextPath + m_servletPath;

        // set the web application rfs path
        m_webApplicationRfsPath = path.getParentFile().getAbsolutePath();
        if (!m_webApplicationRfsPath.endsWith(File.separator)) {
            m_webApplicationRfsPath += File.separator;
        }

        // fill some defaults:
        m_releaseTagsAfterEnd = false;
        m_requestErrorPageAttribute = null;
        m_servletThrowsException = true;
        m_preventResponseFlush = false;
    }

    /**
     * Initializes these container settings with container specific settings.<p>
     *
     * @param context
     *      the servlet context to find out information about the servlet container
     */
    private void initContainerSpecifics(ServletContext context) {

        // the tags behavior
        m_releaseTagsAfterEnd = !(m_servletContainerName.indexOf(SERVLET_CONTAINER_RESIN) > -1);

        // the request error page attribute
        if (m_servletContainerName.indexOf(SERVLET_CONTAINER_WEBLOGIC) > -1) {
            m_requestErrorPageAttribute = REQUEST_ERROR_PAGE_ATTRIBUTE_WEBLOGIC;
        }

        // the failed initialization behavior
        m_servletThrowsException = true;
        m_servletThrowsException &= (m_servletContainerName.indexOf(SERVLET_CONTAINER_RESIN) < 0);
        m_servletThrowsException &= (m_servletContainerName.indexOf(SERVLET_CONTAINER_WEBLOGIC) < 0);
        m_servletThrowsException &= (m_servletContainerName.indexOf(SERVLET_CONTAINER_GLASSFISH) < 0);

        // the flush flex response behavior
        m_preventResponseFlush = false;
        m_preventResponseFlush |= (m_servletContainerName.indexOf(SERVLET_CONTAINER_WEBSPHERE) > -1);
        m_preventResponseFlush |= (m_servletContainerName.indexOf(SERVLET_CONTAINER_RESIN) > -1);
    }
}
