/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSystemInfo.java,v $
 * Date   : $Date: 2004/02/16 15:41:54 $
 * Version: $Revision: 1.6 $
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
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 * @since 5.3
 */
public class CmsSystemInfo {
    
    /** Default encoding */
    private static final String C_DEFAULT_ENCODING = "ISO-8859-1";

    /** Static version name to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NAME = "Ix";

    /** Static version number to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NUMBER = "5.3.x";
    
    /** The abolute path to the "opencms.properties" configuration file in the "real" file system */
    private String m_configurationFilePath;
    
    /** Default encoding, can be overwritten in "opencms.properties" */
    private String m_defaultEncoding;    
    
    /** The default web application (usually "ROOT") */
    private String m_defaultWebApplicationName;

    /** The filename of the log file */
    private String m_logFileName;
    
    /** The OpenCms context and servlet path, e.g. <code>/opencms/opencms</code> */
    private String m_openCmsContext;
    
    /** The name of the OpenCms server */
    private String m_serverName;
    
    /** The mapped servlet path for the OpenCms servlet */
    private String m_servletPath;    
    
    /** The startup time of this OpenCms instance */
    private long m_startupTime;
    
    /** The version name (including version number) of this OpenCms installation */
    private String m_versionName;

    /** The version number of this OpenCms installation */
    private String m_versionNumber;
    
    /** The web application name */
    private String m_webApplicationName;
    
    /** The OpenCms web application folder in the servlet container */
    private String m_webApplicationPath;
    
    /** The OpenCms web application "WEB-INF" directory path */
    private String m_webInfPath;    
    
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
     * Returns an absolute path (to a directory or a file) from a path relative to 
     * the web application folder of OpenCms.<p> 
     * 
     * If the provided path is already absolute, then it is returned unchanged.<p>
     * 
     * @param path the path (relative) to generate an absolute path from
     * @return an absolute path (to a directory or a file) from a path relative to the web application folder of OpenCms
     */    
    public String getAbsolutePathRelativeToWebApplication(String path) {
        if (path == null) {
            return null;
        }
        // check for absolute path is system depended, let's just use the standard check  
        File f = new File(path);
        if (f.isAbsolute()) {
            // apparently this is an absolute path already
            return f.getAbsolutePath();
        }
        return CmsLinkManager.normalizeRfsPath(getWebApplicationPath() + path);
    }

    /**
     * Returns an absolute path (to a directory or a file) from a path relative to 
     * the "WEB-INF" folder of the OpenCms web application.<p> 
     * 
     * If the provided path is already absolute, then it is returned unchanged.<p>
     * 
     * @param path the path (relative) to generate an absolute path from
     * @return an absolute path (to a directory or a file) from a path relative to the "WEB-INF" folder
     */
    public String getAbsolutePathRelativeToWebInf(String path) {
        if (path == null) {
            return null;
        }
        // check for absolute path is system depended, let's just use the standard check  
        File f = new File(path);
        if (f.isAbsolute()) {
            // apparently this is an absolute path already
            return f.getAbsolutePath();
        }
        return CmsLinkManager.normalizeRfsPath(m_webInfPath + path);
    }
    
    /**
     * Returns the abolute path to the "opencms.properties" configuration file in the "real" file system.<p>
     * 
     * @return the abolute path to the "opencms.properties" configuration file
     */
    public String getConfigurationFilePath() {
        if (m_configurationFilePath == null) {
            m_configurationFilePath = getAbsolutePathRelativeToWebInf(I_CmsConstants.C_CONFIGURATION_PROPERTIES_FILE);
        }
        return m_configurationFilePath;
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
     * Returns the filename of the logfile.<p>
     * 
     * @return the filename of the logfile
     */
    public String getLogFileName() {
        return m_logFileName;
    }    
    
    /**
     * Returns the OpenCms request context, e.g. /opencms/opencms.<p>
     * 
     * The context will always start with a "/" and never have a trailing "/".<p>
     * 
     * @return the OpenCms request context, e.g. /opencms/opencms
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
     * Returns the OpenCms server name.<p>
     * 
     * @return the OpenCms server name
     */
    public String getServerName() {
        return m_serverName;
    }
    
    /**
     * Returns the mapped OpenCms servlet path, e.g. "opencms" (no leading or trainling "/").<p>
     * 
     * @return the mapped OpenCms servlet path
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
     * @return the OpenCms web application name
     */    
    public String getWebApplicationName() {
        if (m_webApplicationName == null) {
            File path = new File(m_webInfPath);
            m_webApplicationName = path.getParentFile().getName();
        }
        return m_webApplicationName;
    }
    
    /**
     * Returns the OpenCms web application folder in the servlet container.<p>
     * 
     * @return the OpenCms web application folder in the servlet container
     */
    public String getWebApplicationPath() {
        if (m_webApplicationPath == null) {
            File path = new File(m_webInfPath);
            m_webApplicationPath = path.getParentFile().getAbsolutePath();
            if (! m_webApplicationPath.endsWith(File.separator)) {
                m_webApplicationPath += File.separator;
            }
        }
        return m_webApplicationPath;        
    }
    
    /** 
     * Returns the OpenCms web application "WEB-INF" directory path.<p>
     *
     * @return the OpenCms web application "WEB-INF" directory path
     */
    public String getWebInfPath() {
        return m_webInfPath;
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
     * Sets the default web application name (usually "ROOT").<p>
     * 
     * @param defaultWebApplicationName the default web application name to set
     */
    protected void setDefaultWebApplicationName(String defaultWebApplicationName) {
        // cut off all leading / trailing slashes 
        if (defaultWebApplicationName.endsWith("/")) {
            defaultWebApplicationName = defaultWebApplicationName.substring(0, defaultWebApplicationName.length()-1);
        }        
        if (defaultWebApplicationName.startsWith("/")) {
            defaultWebApplicationName = defaultWebApplicationName.substring(1);
        }             
        m_defaultWebApplicationName = defaultWebApplicationName;
    }
    
    /**
     * Sets the filename of the logfile.<p>
     *  
     * @param logFileName filename of the logfile to set
     */
    protected void setLogFileName(String logFileName) {
        m_logFileName = logFileName;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Log file is          : " + m_logFileName);
        }        
    }
    
    /**
     * Sets the OpenCms request context.<p>
     * 
     * @param context the OpenCms request context
     */
    protected void setOpenCmsContext(String context) {
        if ((context != null) && (context.startsWith("/" + getDefaultWebApplicationName()))) {
            context = context.substring(("/" + getDefaultWebApplicationName()).length());
        }
        if (context == null) {
            context = "";
        }
        m_openCmsContext = context;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("OpenCms: Context is " + m_openCmsContext);
        }        
    }    

    /**
     * Sets the server name.<p>
     * 
     * The server name is usefull in a cluster to distinguish the different servers.<p>
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
     * Sets the mapped OpenCms servlet path.<p>
     * 
     * @param servletPath the servlet path to set
     */
    protected void setServletPath(String servletPath) {
        // usually a mapping must be in the form "/opencms/*", cut off all slashes
        if (servletPath.endsWith("/*")) {
            servletPath = servletPath.substring(0, servletPath.length()-2);
        }        
        if (servletPath.startsWith("/")) {
            servletPath = servletPath.substring(1);
        }        
        m_servletPath = servletPath;
    }
    
    /** 
     * Sets the OpenCms web application "WEB-INF" directory path in the "real" file system.<p>
     *
     * @param basePath the OpenCms web application "WEB-INF" directory path to set
     */
    protected void setWebInfPath(String basePath) {
        // init base path
        basePath = basePath.replace('\\', '/');
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }
        m_webInfPath = CmsLinkManager.normalizeRfsPath(basePath);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("OpenCms: WEB-INF path is " + m_webInfPath);
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
