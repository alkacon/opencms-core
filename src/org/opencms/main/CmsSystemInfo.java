/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSystemInfo.java,v $
 * Date   : $Date: 2004/02/21 13:33:20 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.staticexport.CmsLinkManager;


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
 * @version $Revision: 1.8 $
 * @since 5.3
 */
public class CmsSystemInfo {
    
    /** Default encoding */
    private static final String C_DEFAULT_ENCODING = "ISO-8859-1";

    /** Static version name to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NAME = "Ix";

    /** Static version number to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NUMBER = "5.3.x";
    
    /** The abolute path to the "opencms.properties" configuration file (in the "real" file system) */
    private String m_configurationFileRfsPath;
    
    /** The web application context path */
    private String m_contextPath;
    
    /** Default encoding, can be overwritten in "opencms.properties" */
    private String m_defaultEncoding;    
    
    /** The default web application (usually "ROOT") */
    private String m_defaultWebApplicationName;

    /** The  abolute path to the OpenCms log file (in the "real" file system) */
    private String m_logFileRfsPath;
    
    /** The OpenCms context and servlet path, e.g. <code>/opencms/opencms</code> */
    private String m_openCmsContext;
    
    /** The name of the OpenCms server */
    private String m_serverName;
    
    /** The servlet path for the OpenCms servlet */
    private String m_servletPath;
    
    /** The startup time of this OpenCms instance */
    private long m_startupTime;
    
    /** The version name (including version number) of this OpenCms installation */
    private String m_versionName;

    /** The version number of this OpenCms installation */
    private String m_versionNumber;
    
    /** The web application name */
    private String m_webApplicationName;
    
    /** The OpenCms web application servlet container folder path (in the "real" file system) */
    private String m_webApplicationRfsPath;
    
    /** The OpenCms web application "WEB-INF" path (in the "real" file system) */
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
        m_defaultEncoding = C_DEFAULT_ENCODING;
    }
    
    /**
     * Returns an absolute path (to a directory or a file in the "real" file system) from a path relative to 
     * the web application folder of OpenCms.<p> 
     * 
     * If the provided path is already absolute, then it is returned unchanged.<p>
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
            return f.getAbsolutePath();
        }
        return CmsLinkManager.normalizeRfsPath(getWebApplicationRfsPath() + path);
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
        return CmsLinkManager.normalizeRfsPath(m_webInfRfsPath + path);
    }
    
    /**
     * Returns the abolute path to the "opencms.properties" configuration file (in the "real" file system).<p>
     * 
     * @return the abolute path to the "opencms.properties" configuration file
     */
    public String getConfigurationFileRfsPath() {
        if (m_configurationFileRfsPath == null) {
            m_configurationFileRfsPath = getAbsoluteRfsPathRelativeToWebInf(I_CmsConstants.C_CONFIGURATION_PROPERTIES_FILE);
        }
        return m_configurationFileRfsPath;
    }

    /**
     * Returns the web application context path, e.g. "" (empty String) if the web application 
     * is the default web application (usually "ROOT"), or "/opencms" if the web application 
     * is called "opencms".<p>
     * 
     * <i>From the Java Servlet Sepcecification v2.4:</i><br>
     * <b>Context Path:<b> The path prefix associated with the ServletContext that this
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
     * is "ISO-8859-1".<p>
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
     * @return the filename of the logfile (in the "real" file system)
     */ 
    public String getLogFileRfsPath() {
        return m_logFileRfsPath;
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
     * Returns a String containing the version information (version name and version number) 
     * of this OpenCms system.<p>
     *
     * @return version a String containing the version information
     */
    public String getVersionName() {
        return m_versionName;
    }

    /**
     * Returns a String containing the version number 
     * of this OpenCms system.<p>
     *
     * @return version a String containing the version number
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
     * Sets the default encoding, called after the properties have been read.<p>
     *  
     * @param encoding the default encoding to set
     */
    protected void setDefaultEncoding(String encoding) {
        m_defaultEncoding = encoding;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Encoding set to      : " + m_defaultEncoding);
        }        
    }
    
    /**
     * Sets the absolute path to the OpenCms logfile (in the "real" file system).<p>
     *  
     * @param logFileRfsPath the absolute path to the OpenCms logfile
     */
    protected void setLogFileRfsPath(String logFileRfsPath) {
        m_logFileRfsPath = logFileRfsPath;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Log file is          : " + m_logFileRfsPath);
        }        
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
     * Sets the OpenCms web application "WEB-INF" directory path (in the "real" file system).<p>
     *
     * @param webInfRfsPath the OpenCms web application "WEB-INF" path in the "real" file system) to set
     * @param servletMapping the OpenCms servlet mapping  (e.g. "/opencms/*")
     * @param defaultWebApplication the default web application name (usually "ROOT")
     */
    protected void init(String webInfRfsPath, String servletMapping, String defaultWebApplication) {
        // init base path
        webInfRfsPath = webInfRfsPath.replace('\\', '/');
        if (!webInfRfsPath.endsWith("/")) {
            webInfRfsPath = webInfRfsPath + "/";
        }
        m_webInfRfsPath = CmsLinkManager.normalizeRfsPath(webInfRfsPath);
 
        // set the servlet paths
        if (servletMapping.endsWith("/*")) {
            // usually a mapping must be in the form "/opencms/*", cut off all slashes
            servletMapping = servletMapping.substring(0, servletMapping.length()-2);
        } 
        if (! servletMapping.startsWith("/")) {
            servletMapping = "/" + servletMapping;
        }
        m_servletPath = servletMapping;  
        
        // set the default web application name
        if (defaultWebApplication.endsWith("/")) {
            defaultWebApplication = defaultWebApplication.substring(0, defaultWebApplication.length()-1);
        }        
        if (defaultWebApplication.startsWith("/")) {
            defaultWebApplication = defaultWebApplication.substring(1);
        }             
        m_defaultWebApplicationName = defaultWebApplication;
        
        // set the web application name
        File path = new File(m_webInfRfsPath);
        m_webApplicationName = path.getParentFile().getName();
        
        // set the context path
        if (m_webApplicationName.equals(getDefaultWebApplicationName())) {
            m_contextPath = "";
        } else {
            m_contextPath = "/" + m_webApplicationName;
        }

        // set the OpenCms context
        m_openCmsContext = m_contextPath + m_servletPath;
        
        // set the web application path
        m_webApplicationRfsPath = path.getParentFile().getAbsolutePath();
        if (! m_webApplicationRfsPath.endsWith(File.separator)) {
            m_webApplicationRfsPath += File.separator;
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
        // read the version-informations from properties, if not done
        Properties props = new Properties();
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("com/opencms/core/version.properties"));
        } catch (Throwable t) {
            // ignore this exception - no properties found
            return;
        }
        m_versionNumber = props.getProperty("version.number", C_DEFAULT_VERSION_NUMBER);
        m_versionName = m_versionNumber + " " + props.getProperty("version.name", C_DEFAULT_VERSION_NAME);
    }  
}
