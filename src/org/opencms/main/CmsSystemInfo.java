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

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.util.I_CmsJspDeviceSelector;
import org.opencms.mail.CmsMailSettings;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Provides access to system wide "read only" information about the running OpenCms instance.<p>
 *
 * Contains information about:
 * <ul>
 * <li>version and build number</li>
 * <li>server name</li>
 * <li>mail settings</li>
 * <li>configuration paths</li>
 * <li>default character encoding</li>
 * <li>...and more.</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class CmsSystemInfo {

    /**
     * Wrapper class used to access build information.<p>
     */
    public class BuildInfoItem {

        /** The key name. */
        private String m_keyName;

        /** The nice name for display. */
        private String m_niceName;

        /** The build information value. */
        private String m_value;

        /**
         * Creates a new instance wrapping a build info string array.<p>
         *
         * @param value the value
         * @param niceName the nice name
         * @param keyName the key name
         */
        public BuildInfoItem(String value, String niceName, String keyName) {

            m_value = value;
            m_niceName = niceName;
            m_keyName = keyName;
        }

        /**
         * Gets the key name for this build info item.<p>
         *
         * @return the value
         */
        public String getKeyName() {

            return m_keyName;
        }

        /**
         * Gets the nice name for this build info item.<p>
         *
         * @return the nice name
         */
        public String getNiceName() {

            return m_niceName;
        }

        /**
         * Gets the value for this build info item.<p>
         *
         * @return the value
         */
        public String getValue() {

            return m_value;
        }
    }

    /** Name of the config folder property provides as Java VM parameter -Dopencms.config=.*/
    public static final String CONFIG_FOLDER_PROPERTY = "opencms.config";

    /** Relative path to persistence.xml file. */
    public static final String FILE_PERSISTENCE = "classes"
        + File.separatorChar
        + "META-INF"
        + File.separatorChar
        + "persistence.xml";

    /** The name of the opencms.properties file. */
    public static final String FILE_PROPERTIES = "opencms.properties";

    /** The name of the opencms.tld file. */
    public static final String FILE_TLD = "opencms.tld";

    /** Path to the default "config" folder relative to the "WEB-INF" directory of the application. */
    public static final String FOLDER_CONFIG_DEFAULT = "config" + File.separatorChar;

    /** The name of the module folder in the package path. */
    public static final String FOLDER_MODULES = "modules" + File.separatorChar;

    /** Path to the "packages" folder relative to the "WEB-INF" directory of the application. */
    public static final String FOLDER_PACKAGES = "packages" + File.separatorChar;

    /** Path to the "WEB-INF" folder relative to the directory of the application. */
    public static final String FOLDER_WEBINF = "WEB-INF" + File.separatorChar;

    /** The workplace UI servlet name. */
    public static final String WORKPLACE_PATH = "/workplace";

    /** Default encoding. */
    private static final String DEFAULT_ENCODING = CmsEncoder.ENCODING_UTF_8;

    /** Static version id to use if version.properties can not be read. */
    private static final String DEFAULT_VERSION_ID = "Static";

    /** Static version number to use if version.properties can not be read. */
    private static final String DEFAULT_VERSION_NUMBER = "9.x.y";

    /** The list of additional version information that was contained in the version.properties file. */
    private Map<String, BuildInfoItem> m_buildInfo;

    /** The absolute path to the "opencms.properties" configuration file (in the "real" file system). */
    private String m_configurationFileRfsPath;

    /** Default encoding, can be set in opencms-system.xml. */
    private String m_defaultEncoding;

    /** The device selector instance. */
    private I_CmsJspDeviceSelector m_deviceSelector;

    /** Indicates if the version history is enabled. */
    private boolean m_historyEnabled;

    /** The maximum number of entries in the version history (per resource). */
    private int m_historyVersions;

    /** The maximum number of versions in the VFS version history for deleted resources. */
    private int m_historyVersionsAfterDeletion;

    /** The HTTP basic authentication settings. */
    private CmsHttpAuthenticationSettings m_httpAuthenticationSettings;

    /** The settings for the internal OpenCms email service. */
    private CmsMailSettings m_mailSettings;

    /** The project in which time stamps for the content notification are read. */
    private String m_notificationProject;

    /** The duration after which responsible resource owners will be notified about out-dated content (in days). */
    private int m_notificationTime;

    /** The absolute path to the "packages" folder (in the "real" file system). */
    private String m_packagesRfsPath;

    /** The absolute path to the persistence.xml file (in the "real" file system). */
    private String m_persistenceFileRfsPath;

    /** True if detail contents are restricted to detail pages from the same site. */
    private boolean m_restrictDetailContents;

    /** The name of the OpenCms server. */
    private String m_serverName;

    /** The servlet container specific settings. */
    private CmsServletContainerSettings m_servletContainerSettings;

    /** The startup time of this OpenCms instance. */
    private long m_startupTime;

    /** The static resource version parameter. */
    private String m_staticResourcePathFragment;

    /** The version identifier of this OpenCms installation, contains "OpenCms/" and the version number. */
    private String m_version;

    /** The version ID of this OpenCms installation, usually set by the build system. */
    private String m_versionId;

    /** The version number of this OpenCms installation. */
    private String m_versionNumber;

    /**
     * Creates a new system info container.<p>
     */
    public CmsSystemInfo() {

        // set startup time
        m_startupTime = System.currentTimeMillis();
        // init version information
        initVersion();
        // set default encoding (will be changed again later when properties have been read)
        m_defaultEncoding = DEFAULT_ENCODING.intern();
        // this may look odd, but initMembers in OpenCms core has to initialize this (e.g. for setup to avoid NPE)
        m_servletContainerSettings = new CmsServletContainerSettings(null);
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

        if ((path == null) || (getWebApplicationRfsPath() == null)) {
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
     * Returns the map of additional build information that was contained in the version.properties file.<p>
     *
     * The values are String arrays of length 2. First in this array is the actual value,
     * and second the "nice name" for the value that can be used to display the value somewhere.
     * In case no nice name was provided, the second value will repeat the key name.<p>
     *
     * @return the map of additional build information that was contained in the version.properties file
     *
     * @since 9.5.0
     */
    public Map<String, BuildInfoItem> getBuildInfo() {

        return m_buildInfo;
    }

    /**
     * Gets the path of the opencms config folder.<p>
     * Per default this is the "/WEB-INF/config/ folder.
     * If configured with the "-Dopencms.config=..." java startup parameter, OpenCms can access an external config
     * folder outside its webapplication.
     * @return complete rfs path to the config folder.
     */
    public String getConfigFolder() {

        // check if the system property is set and return its value
        if (CmsStringUtil.isNotEmpty(System.getProperty(CONFIG_FOLDER_PROPERTY))) {
            return System.getProperty(CONFIG_FOLDER_PROPERTY);
        } else {
            return getAbsoluteRfsPathRelativeToWebInf(FOLDER_CONFIG_DEFAULT);
        }
    }

    /**
     * Returns the absolute path to the "opencms.properties" configuration file (in the "real" file system).<p>
     *
     * @return the absolute path to the "opencms.properties" configuration file
     */
    public String getConfigurationFileRfsPath() {

        if (m_configurationFileRfsPath == null) {
            m_configurationFileRfsPath = getConfigFolder() + FILE_PROPERTIES;
        }
        return m_configurationFileRfsPath;
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

        return m_servletContainerSettings.getContextPath();
    }

    /**
     * Return the OpenCms default character encoding.<p>
     *
     * The default is set in the opencms-system.xml file.
     * If this is not set in opencms-system.xml the default
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

        return m_servletContainerSettings.getDefaultWebApplicationName();
    }

    /**
     * Gets the device selector.<p>
     *
     * @return the device selector
     */
    public I_CmsJspDeviceSelector getDeviceSelector() {

        return m_deviceSelector;
    }

    /**
     * Returns the maximum number of versions that are kept per file in the VFS version history.<p>
     *
     * If the version history is disabled, this setting has no effect.<p>
     *
     * @return the maximum number of versions that are kept per file
     * @see #isHistoryEnabled()
     */
    public int getHistoryVersions() {

        return m_historyVersions;
    }

    /**
     * Returns the number of versions in the VFS version history that should be
     * kept after a resource is deleted.<p>
     *
     * @return the number versions in the VFS version history for deleted resources
     */
    public int getHistoryVersionsAfterDeletion() {

        return m_historyVersionsAfterDeletion;
    }

    /**
     * Returns the HTTP authentication settings.<p>
     *
     * @return the HTTP authentication settings
     */
    public CmsHttpAuthenticationSettings getHttpAuthenticationSettings() {

        return m_httpAuthenticationSettings;
    }

    /**
     * Returns the filename of the log file (in the "real" file system).<p>
     *
     * If the method returns <code>null</code>, this means that the log
     * file is not managed by OpenCms.<p>
     *
     * @return the filename of the log file (in the "real" file system)
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
     * Returns the project in which time stamps for the content notification are read.<p>
     *
     * @return the project in which time stamps for the content notification are read
     */
    public String getNotificationProject() {

        return m_notificationProject;
    }

    /**
     * Returns the duration after which responsible resource owners will be notified about out-dated content (in days).<p>
     *
     * @return the duration after which responsible resource owners will be notified about out-dated content
     */
    public int getNotificationTime() {

        return m_notificationTime;
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

        return m_servletContainerSettings.getOpenCmsContext();
    }

    /**
     * Returns the absolute path to the "packages" folder (in the "real" file system).<p>
     *
     * @return the absolute path to the "packages" folder
     */
    public String getPackagesRfsPath() {

        if (m_packagesRfsPath == null) {
            m_packagesRfsPath = getAbsoluteRfsPathRelativeToWebInf(CmsSystemInfo.FOLDER_PACKAGES);
        }
        return m_packagesRfsPath;
    }

    /**
     * Returns the absolute path to the "persistence.xml" file (in the "real" file system).<p>
     *
     * @return the absolute path to the "persistence.xml" configuration file
     */
    public String getPersistenceFileRfsPath() {

        if (m_persistenceFileRfsPath == null) {
            m_persistenceFileRfsPath = getAbsoluteRfsPathRelativeToWebInf(FILE_PERSISTENCE);
        }
        return m_persistenceFileRfsPath;
    }

    /**
     * Returns the time this OpenCms instance is running in milliseconds.<p>
     *
     * @return the time this OpenCms instance is running in milliseconds
     */
    public long getRuntime() {

        return System.currentTimeMillis() - m_startupTime;
    }

    /**
     * Returns the OpenCms server name, e.g. "OpenCmsServer".<p>
     *
     * The server name is set in <code>opencms.properties</code>.
     * It is not related to any DNS name the server might also have.
     * The server name is useful e.g. in a cluster to distinguish different servers,
     * or if you compare log files from multiple servers.<p>
     *
     * @return the OpenCms server name
     */
    public String getServerName() {

        return m_serverName;
    }

    /**
     * Returns the servlet container specific settings.<p>
     *
     * @return the servlet container specific settings
     */
    public CmsServletContainerSettings getServletContainerSettings() {

        return m_servletContainerSettings;
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

        return m_servletContainerSettings.getServletPath();
    }

    /**
     * Returns the time this OpenCms instance was started in milliseconds.<p>
     *
     * @return the time this OpenCms instance was started in milliseconds
     */
    public long getStartupTime() {

        return m_startupTime;
    }

    /**
     * Returns the context for static resources served from the class path, e.g. "/opencms/opencms/handleStatic".<p>
     *
     * @return the static resource context
     */
    public String getStaticResourceContext() {

        if (m_staticResourcePathFragment == null) {
            m_staticResourcePathFragment = CmsStaticResourceHandler.getStaticResourceContext(
                OpenCms.getStaticExportManager().getVfsPrefix(),
                getVersionNumber());
        }
        return m_staticResourcePathFragment;
    }

    /**
     * Returns the identifier "OpenCms/" plus the OpenCms version number.<p>
     *
     * This information is used for example to identify OpenCms in HTTP response headers.<p>
     *
     * @return the identifier "OpenCms/" plus the OpenCms version number
     */
    public String getVersion() {

        return m_version;
    }

    /**
     * Returns the version ID of this OpenCms system.<p>
     *
     * The version ID is usually set dynamically by the build system.
     * It can be used to identify intermediate builds when the main
     * version number has not changed.<p>
     *
     * @return the version ID of this OpenCms system
     *
     * @since 9.5.0
     */
    public String getVersionId() {

        return m_versionId;
    }

    /**
     * Returns the version number of this OpenCms system, for example <code>9.5.0</code>.<p>
     *
     * @return the version number of this OpenCms system
     *
     * @since 7.0.2
     */
    public String getVersionNumber() {

        return m_versionNumber;
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

        return m_servletContainerSettings.getWebApplicationName();
    }

    /**
     * Returns the OpenCms web application folder in the servlet container.<p>
     *
     * @return the OpenCms web application folder in the servlet container
     */
    public String getWebApplicationRfsPath() {

        return m_servletContainerSettings.getWebApplicationRfsPath();
    }

    /**
     * Returns the OpenCms web application "WEB-INF" directory path.<p>
     *
     * @return the OpenCms web application "WEB-INF" directory path
     */
    public String getWebInfRfsPath() {

        return m_servletContainerSettings.getWebInfRfsPath();
    }

    /**
     * Returns the OpenCms workplace request context, e.g. "/opencms/workplace".<p>
     *
     * The OpenCms workplace context will always start with a "/" and never have a trailing "/".
     * The OpenCms context is identical to <code>getContexPath() + WORKPLACE_PATH</code>.<p>
     *
     * @return the OpenCms request context, e.g. "/opencms/workplace"
     * @see #getContextPath()
     * @see #WORKPLACE_PATH
     */
    public String getWorkplaceContext() {

        return getContextPath() + WORKPLACE_PATH;
    }

    /**
     * Returns if the VFS version history is enabled.<p>
     *
     * @return if the VFS version history is enabled
     */
    public boolean isHistoryEnabled() {

        return m_historyEnabled;
    }

    /**
     * Return true if detail contents are restricted to detail pages from the same site.<p>
     *
     * @return true if detail contents are restricted to detail pages from the same site
     */
    public boolean isRestrictDetailContents() {

        return m_restrictDetailContents;
    }

    /**
     * Sets the project in which time stamps for the content notification are read.<p>
     *
     * @param notificationProject the project in which time stamps for the content notification are read
     */
    public void setNotificationProject(String notificationProject) {

        m_notificationProject = notificationProject;
    }

    /**
     * Sets the duration after which responsible resource owners will be notified about out-dated content (in days).<p>
     *
     * @param notificationTime the duration after which responsible resource owners will be notified about out-dated content
     */
    public void setNotificationTime(int notificationTime) {

        m_notificationTime = notificationTime;
    }

    /**
     * VFS version history settings are set here.<p>
     *
     * @param historyEnabled if true the history is enabled
     * @param historyVersions the maximum number of versions that are kept per VFS resource
     * @param historyVersionsAfterDeletion the maximum number of versions that are kept for deleted resources
     */
    public void setVersionHistorySettings(
        boolean historyEnabled,
        int historyVersions,
        int historyVersionsAfterDeletion) {

        m_historyEnabled = historyEnabled;
        m_historyVersions = historyVersions;
        if (historyVersionsAfterDeletion < 0) {
            m_historyVersionsAfterDeletion = historyVersions;
        } else {
            m_historyVersionsAfterDeletion = historyVersionsAfterDeletion;
        }
    }

    /**
     * Sets the OpenCms web application "WEB-INF" directory path (in the "real" file system).<p>
     *
     * @param settings container specific information needed for this system info
     */
    protected void init(CmsServletContainerSettings settings) {

        m_servletContainerSettings = settings;
    }

    /**
     * Sets the default encoding, called after the configuration files have been read.<p>
     *
     * @param encoding the default encoding to set
     */
    protected void setDefaultEncoding(String encoding) {

        m_defaultEncoding = encoding.intern();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.LOG_SET_DEFAULT_ENCODING_1, m_defaultEncoding));
        }
    }

    /**
     * Sets the device selector.<p>
     *
     * @param selector the device selector to set
     */
    protected void setDeviceSelector(I_CmsJspDeviceSelector selector) {

        m_deviceSelector = selector;
    }

    /**
     * Sets the HTTP authentication settings.<p>
     *
     * @param httpAuthenticationSettings the HTTP authentication settings to set
     */
    protected void setHttpAuthenticationSettings(CmsHttpAuthenticationSettings httpAuthenticationSettings) {

        m_httpAuthenticationSettings = httpAuthenticationSettings;
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
     * Sets the value of the 'restrict detail contents' option.<p>
     *
     * @param restrictDetailContents the new value for the option
     */
    protected void setRestrictDetailContents(boolean restrictDetailContents) {

        m_restrictDetailContents = restrictDetailContents;
    }

    /**
     * Sets the server name.<p>
     *
     * The server name is set in <code>opencms.properties</code>.
     * It is not related to any DNS name the server might also have.
     * The server name is useful e.g. in a cluster to distinguish different servers,
     * or if you compare log files from multiple servers.<p>
     *
     * @param serverName the server name to set
     */
    protected void setServerName(String serverName) {

        m_serverName = serverName;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.LOG_SET_SERVERNAME_1, m_serverName));
        }
    }

    /**
     * Initializes the version for this OpenCms, will be called by
     * {@link OpenCmsServlet} or {@link CmsShell} upon system startup.<p>
     */
    private void initVersion() {

        // initialize version information with static defaults
        m_versionNumber = DEFAULT_VERSION_NUMBER;
        m_versionId = DEFAULT_VERSION_ID;
        m_version = "OpenCms/" + m_versionNumber;
        m_buildInfo = Collections.emptyMap();
        // read the version-informations from properties
        Properties props = new Properties();
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("org/opencms/main/version.properties"));
        } catch (Throwable t) {
            // no properties found - we just use the defaults
            return;
        }
        // initialize OpenCms version information from the property values
        m_versionNumber = props.getProperty("version.number", DEFAULT_VERSION_NUMBER);
        m_versionId = props.getProperty("version.id", DEFAULT_VERSION_ID);
        m_version = "OpenCms/" + m_versionNumber;
        m_buildInfo = new TreeMap<String, BuildInfoItem>();

        // iterate the properties and generate the build information from the entries
        for (String key : props.stringPropertyNames()) {
            if (!"version.number".equals(key) && !"version.id".equals(key) && !key.startsWith("nicename")) {
                String value = props.getProperty(key);
                String nicename = props.getProperty("nicename." + key, key);
                m_buildInfo.put(key, new BuildInfoItem(value, nicename, key));
            }
        }
        // make the map unmodifiable
        m_buildInfo = Collections.unmodifiableMap(m_buildInfo);
    }
}