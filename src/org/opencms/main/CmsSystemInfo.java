/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSystemInfo.java,v $
 * Date   : $Date: 2005/04/18 21:21:18 $
 * Version: $Revision: 1.31 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.mail.CmsMailSettings;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.util.Properties;

/**
 * Provides access to system wide "read only" information.<p>
 * 
 * Regarding the naming conventions used, this comes straight from the Servlet Sepc v2.4:<p>
 *   
 * <i>SRV.3.1 Introduction to the ServletContext Interface<br>
 * [...] A ServletContext is rooted at a known path within a web server. For example
 * a servlet context could be located at http://www.mycorp.com/catalog. All
 * requests that begin with the /catalog request path, known as the <b>context path</b>, are
 * routed to the <b>web application</b> associated with the ServletContext.</i><p>   
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.31 $
 * @since 5.3
 */
public class CmsSystemInfo {

    /** Default encoding. */
    private static final String C_DEFAULT_ENCODING = CmsEncoder.C_UTF8_ENCODING;

    /** Static version name to use if version.properties can not be read. */
    private static final String C_DEFAULT_VERSION_NAME = "Corrin";

    /** Static version number to use if version.properties can not be read. */
    private static final String C_DEFAULT_VERSION_NUMBER = "6.0 development";

    /** The abolute path to the "opencms.properties" configuration file (in the "real" file system). */
    private String m_configurationFileRfsPath;

    /** The web application context path. */
    private String m_contextPath;

    /** Default encoding, can be overwritten in "opencms.properties". */
    private String m_defaultEncoding;

    /** The default web application (usually "ROOT"). */
    private String m_defaultWebApplicationName;

    /** The settings for the internal OpenCms email service. */
    private CmsMailSettings m_mailSettings;

    /** The OpenCms context and servlet path, e.g. <code>/opencms/opencms</code>. */
    private String m_openCmsContext;

    /** The abolute path to the "packages" folder (in the "real" file system). */
    private String m_packagesRfsPath;

    /** The name of the OpenCms server. */
    private String m_serverName;

    /** The servlet path for the OpenCms servlet. */
    private String m_servletPath;

    /** The startup time of this OpenCms instance. */
    private long m_startupTime;

    /** The settings for the synchronization. */
    private CmsSynchronizeSettings m_synchronizeSettings;
    
    /** The version identifier of this OpenCms installation, contains "OpenCms/" and the version number. */
    private String m_version;

    /** Indicates if the version history is enabled. */
    private boolean m_versionHistoryEnabled;

    /** The maximum number of entries in the version history (per resource). */
    private int m_versionHistoryMaxCount;

    /** The version name (including version number) of this OpenCms installation. */
    private String m_versionName;

    /** The version number of this OpenCms installation. */
    private String m_versionNumber;

    /** The web application name. */
    private String m_webApplicationName;

    /** The OpenCms web application servlet container folder path (in the "real" file system). */
    private String m_webApplicationRfsPath;

    /** The OpenCms web application "WEB-INF" path (in the "real" file system). */
    private String m_webInfRfsPath;

    /**
     * Creates a new system info container.<p>
     */
    public CmsSystemInfo() {

        // set startup time
        m_startupTime = System.currentTimeMillis();
        // init version onformation
        initVersion();
        // set default encoding (will be changed again later when properties have been read)
        m_defaultEncoding = C_DEFAULT_ENCODING.intern();
    }

    /**
     * Returns an absolute path (to a directory or a file in the "real" file system) from a path relative to 
     * the web application folder of OpenCms.<p> 
     * 
     * If the provided path is already absolute, then it is returned unchanged.
     * If the provided path is a folder, the result will always end with a folder separator.<p>
     * 
     * @param path the path (relative) to generate an absolute path from
     * @return an absolute path (to a directory or a file) from a path relative to the web application folder of OpenCms
     */
    public String getAbsoluteRfsPathRelativeToWebApplication(String path) {

        if (path == null) {
            return null;
        }
        // check for absolute path is system depended, let's just use the standard check  
        File f = new File(path);
        if (f.isAbsolute()) {
            // apparently this is an absolute path already
            path = f.getAbsolutePath();
            if (f.isDirectory() && !path.endsWith(File.separator)) {
                // make sure all folder paths end with a separator
                path = path.concat(File.separator);
            }
            return path;
        }
        return CmsFileUtil.normalizePath(getWebApplicationRfsPath() + path);
    }

    /**
     * Returns an absolute path (to a directory or a file in the "real" file system) from a path relative to 
     * the "WEB-INF" folder of the OpenCms web application.<p> 
     * 
     * If the provided path is already absolute, then it is returned unchanged.<p>
     * 
     * @param path the path (relative) to generate an absolute path from
     * @return an absolute path (to a directory or a file) from a path relative to the "WEB-INF" folder
     */
    public String getAbsoluteRfsPathRelativeToWebInf(String path) {

        if (path == null) {
            return null;
        }
        // check for absolute path is system depended, let's just use the standard check  
        File f = new File(path);
        if (f.isAbsolute()) {
            // apparently this is an absolute path already
            return f.getAbsolutePath();
        }
        return CmsFileUtil.normalizePath(getWebInfRfsPath() + path);
    }

    /**
     * Returns the abolute path to the "opencms.properties" configuration file (in the "real" file system).<p>
     * 
     * @return the abolute path to the "opencms.properties" configuration file
     */
    public String getConfigurationFileRfsPath() {

        if (m_configurationFileRfsPath == null) {
            m_configurationFileRfsPath = getAbsoluteRfsPathRelativeToWebInf("config/opencms.properties");
        }
        return m_configurationFileRfsPath;
    }

    /**
     * Returns the web application context path, e.g. "" (empty String) if the web application 
     * is the default web application (usually "ROOT"), or "/opencms" if the web application 
     * is called "opencms".<p>
     * 
     * <i>From the Java Servlet Sepcecification v2.4:</i><br>
     * <b>Context Path:</b> The path prefix associated with the ServletContext that this
     * servlet is a part of. If this context is the ?default? context rooted at the base of
     * the web server?s URL namespace, this path will be an empty string. Otherwise,
     * if the context is not rooted at the root of the server?s namespace, the path starts
     * with a?/? character but does not end with a?/? character.<p>
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
     * Return the OpenCms default character encoding.<p>
     * 
     * The default is set in the "opencms.properties" file.
     * If this is not set in "opencms.properties" the default 
     * is "UTF-8".<p>
     * 
     * @return the default encoding, e.g. "UTF-8" or "ISO-8859-1"
     */
    public String getDefaultEncoding() {

        return m_defaultEncoding;
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
     * Returns the filename of the logfile (in the "real" file system).<p>
     * 
     * If the method returns <code>null</code>, this means that the log
     * file is not managed by OpenCms.<p>
     * 
     * @return the filename of the logfile (in the "real" file system)
     */
    public String getLogFileRfsPath() {

        return CmsLog.getLogFileRfsPath();
    }

    /**
     * Returns the settings for the internal OpenCms email service.<p>
     * 
     * @return the settings for the internal OpenCms email service
     */
    public CmsMailSettings getMailSettings() {

        return m_mailSettings;
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
     * Returns the abolute path to the "packages" folder (in the "real" file system).<p>
     * 
     * @return the abolute path to the "packages" folder
     */
    public String getPackagesRfsPath() {

        if (m_packagesRfsPath == null) {
            m_packagesRfsPath = getAbsoluteRfsPathRelativeToWebInf(I_CmsConstants.C_PACKAGES_FOLDER);
        }
        return m_packagesRfsPath;
    }

    /**
     * Returns the time this OpenCms instance is running in miliseconds.<p>
     * 
     * @return the time this OpenCms instance is running in miliseconds
     */
    public long getRuntime() {

        return System.currentTimeMillis() - m_startupTime;
    }

    /**
     * Returns the OpenCms server name, e.g. "OpenCmsServer".<p>
     * 
     * The server name is set in <code>opencms.properties</code>.
     * It is not related to any DNS name the server might also have.
     * The server name is usefull e.g. in a cluster to distinguish different servers,
     * or if you compare logfiles from multiple servers.<p>
     * 
     * @return the OpenCms server name
     */
    public String getServerName() {

        return m_serverName;
    }

    /**
     * Returns the OpenCms servlet path, e.g. "/opencms".<p> 
     * 
     * <i>From the Java Servlet Sepcecification v2.4:</i><br>
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
     * Returns the time this OpenCms instance was started in miliseconds.<p>
     *
     * @return the time this OpenCms instance was started in miliseconds
     */
    public long getStartupTime() {

        return m_startupTime;
    }

    /**
     * Returns the current settings for the synchronization.<p>
     * 
     * @return the current settings for the synchronization
     */
    public CmsSynchronizeSettings getSynchronizeSettings() {

        return m_synchronizeSettings;
    }

    /**
     * Returns the identifier "OpenCms/" plus the OpenCms version number.<p>
     * 
     * This information is used for example to identify OpenCms in http response headers.<p>
     *
     * @return the identifier "OpenCms/" plus the OpenCms version number
     */
    public String getVersion() {
        
        return m_version;
    }

    /**
     * Returns the maximum number of versions that are kept per file in the VFS version history.<p>
     * 
     * If the version history is disabled, this setting has no effect.<p>
     * 
     * @return the maximum number of versions that are kept per file
     * @see #isVersionHistoryEnabled()
     */
    public int getVersionHistoryMaxCount() {

        return m_versionHistoryMaxCount;
    }

    /**
     * Returns the version name (including version number) of this OpenCms system.<p>
     *
     * @return the version name (including version number) of this OpenCms system
     */
    public String getVersionName() {

        return m_versionName;
    }
    
    /**
     * Returns the version number of this OpenCms system.<p>
     *
     * @return the version number of this OpenCms system
     */
    public String getVersionNumber() {

        return m_versionNumber;
    }

    /** 
     * Returns the OpenCms web application name, e.g. "opencms" or "ROOT" (no leading or trainling "/").<p> 
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
     * Returns if the VFS version history is enabled.<p> 
     * 
     * @return if the VFS version history is enabled
     */
    public boolean isVersionHistoryEnabled() {

        return m_versionHistoryEnabled;
    }

    /**
     * Returns if versions in the VFS version history should be kept 
     * after a resource is deleted.<p>
     * 
     * @return if versions in the VFS version history should be kept
     */
    public boolean keepVersionHistory() {

        // TODO: make configurable
        return true;
    }

    /**
     * Sets the settings for the synchronization.<p>
     * 
     * @param synchronizeSettings the settings for the synchronization to set
     */
    public void setSynchronizeSettings(CmsSynchronizeSettings synchronizeSettings) {

        m_synchronizeSettings = synchronizeSettings;
    }

    /**
     * VFS version history settings are set here.<p>
     * 
     * @param historyEnabled if true the history is enabled
     * @param historyMaxCount the maximum number of versions that are kept per VFS resource
     */
    public void setVersionHistorySettings(boolean historyEnabled, int historyMaxCount) {

        m_versionHistoryEnabled = historyEnabled;
        m_versionHistoryMaxCount = historyMaxCount;
    }

    /** 
     * Sets the OpenCms web application "WEB-INF" directory path (in the "real" file system).<p>
     * 
     * @param webInfRfsPath the OpenCms web application "WEB-INF" path in the "real" file system) to set
     * @param servletMapping the OpenCms servlet mapping  (e.g. "/opencms/*")
     * @param webApplicationContext the name/path of the OpenCms web application context (optional, will be calculated form the path if null)
     * @param defaultWebApplication the default web application name (usually "ROOT")
     */
    protected void init(
        String webInfRfsPath,
        String servletMapping,
        String webApplicationContext,
        String defaultWebApplication) {

        // init base path
        webInfRfsPath = webInfRfsPath.replace('\\', '/');
        if (!webInfRfsPath.endsWith("/")) {
            webInfRfsPath = webInfRfsPath + "/";
        }
        m_webInfRfsPath = CmsFileUtil.normalizePath(webInfRfsPath);

        // set the servlet paths
        if (servletMapping.endsWith("/*")) {
            // usually a mapping must be in the form "/opencms/*", cut off all slashes
            servletMapping = servletMapping.substring(0, servletMapping.length() - 2);
        }
        if (!servletMapping.startsWith("/")) {
            servletMapping = "/" + servletMapping;
        }
        m_servletPath = servletMapping;

        // set the default web application name
        if (defaultWebApplication.endsWith("/")) {
            defaultWebApplication = defaultWebApplication.substring(0, defaultWebApplication.length() - 1);
        }
        if (defaultWebApplication.startsWith("/")) {
            defaultWebApplication = defaultWebApplication.substring(1);
        }
        m_defaultWebApplicationName = defaultWebApplication;

        // set the web application name
        File path = new File(m_webInfRfsPath);
        m_webApplicationName = path.getParentFile().getName();

        String contextPath;
        if (webApplicationContext == null) {
            // default: use web application context calculated form RFS path (fine with Tomcat)
            contextPath = m_webApplicationName;
        } else {
            // optional: web application context was set in web.xml, required for certain 
            // runtime environments (e.g. Jboss) that do not use the same RFS and context path
            contextPath = webApplicationContext;
        }

        // set the context path
        if (contextPath.equals(getDefaultWebApplicationName())) {
            m_contextPath = "";
        } else {
            m_contextPath = "/" + contextPath;
        }

        // this fixes an issue with context names in Jboss
        if (m_contextPath.endsWith(".war")) {
            m_contextPath = m_contextPath.substring(0, m_contextPath.length() - 4);
        }

        // set the OpenCms context
        m_openCmsContext = m_contextPath + m_servletPath;

        // set the web application path
        m_webApplicationRfsPath = path.getParentFile().getAbsolutePath();
        if (!m_webApplicationRfsPath.endsWith(File.separator)) {
            m_webApplicationRfsPath += File.separator;
        }
    }

    /**
     * Sets the default encoding, called after the properties have been read.<p>
     *  
     * @param encoding the default encoding to set
     */
    protected void setDefaultEncoding(String encoding) {

        m_defaultEncoding = encoding.intern();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Encoding set to      : " + m_defaultEncoding);
        }
    }

    /**
     * Sets the settings for the internal OpenCms email service.<p>
     * 
     * @param mailSettings the settings for the internal OpenCms email service to set
     */
    protected void setMailSettings(CmsMailSettings mailSettings) {

        m_mailSettings = mailSettings;
    }

    /**
     * Sets the server name.<p>
     * 
     * The server name is set in <code>opencms.properties</code>.
     * It is not related to any DNS name the server might also have.
     * The server name is usefull e.g. in a cluster to distinguish different servers,
     * or if you compare logfiles from multiple servers.<p>
     *  
     * @param serverName the server name to set
     */
    protected void setServerName(String serverName) {

        m_serverName = serverName;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Server name is       : " + m_serverName);
        }
    }

    /**
     * Initializes the version for this OpenCms, will be called by 
     * CmsHttpServlet or CmsShell upon system startup.<p>
     */
    private void initVersion() {

        // init version information with static defaults
        m_versionName = C_DEFAULT_VERSION_NUMBER + " " + C_DEFAULT_VERSION_NAME;
        m_versionNumber = C_DEFAULT_VERSION_NUMBER;
        // set OpenCms version identifier with default values
        m_version = "OpenCms/" + m_versionNumber;
        // read the version-informations from properties
        Properties props = new Properties();
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("com/opencms/core/version.properties"));
        } catch (Throwable t) {
            // ignore this exception - no properties found
            return;
        }
        m_versionNumber = props.getProperty("version.number", C_DEFAULT_VERSION_NUMBER);
        m_versionName = m_versionNumber + " " + props.getProperty("version.name", C_DEFAULT_VERSION_NAME);
        // set OpenCms version identifier with propery values
        m_version = "OpenCms/" + m_versionNumber;
    }

}