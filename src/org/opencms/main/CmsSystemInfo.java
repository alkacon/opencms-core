/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSystemInfo.java,v $
 * Date   : $Date: 2004/02/12 10:17:45 $
 * Version: $Revision: 1.2 $
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
 * @version $Revision: 1.2 $
 * @since 5.3
 */
public class CmsSystemInfo {
    
    /** Default encoding */
    private static final String C_DEFAULT_ENCODING = "ISO-8859-1";

    /** Static version name to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NAME = "Ix";

    /** Static version number to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NUMBER = "5.3.x";
    
    /** The OpenCms application base path */
    private String m_basePath;    
    
    /** Default encoding, can be overwritten in "opencms.properties" */
    private String m_defaultEncoding;    
        
    /** The version name (including version number) of this OpenCms installation */
    private String m_versionName;

    /** The version number of this OpenCms installation */
    private String m_versionNumber;
    
    /** The name of the OpenCms server */
    private String m_serverName;

    /** The filename of the log file */
    private String m_logFileName;
    
    /** The OpenCms context and servlet path, e.g. <code>/opencms/opencms</code> */
    private String m_openCmsContext;
    
    
    
    /**
     * Creates a new system info container.<p>
     */    
    public CmsSystemInfo() {
        // init version onformation
        initVersion();
        // set default encoding (will be changed again later when properties have been read)
        m_defaultEncoding = C_DEFAULT_ENCODING;
    }
    
    /** 
     * Returns the OpenCms web application name, e.g. "opencms".<p> 
     * 
     * @return the OpenCms web application name
     */
    public static String getWebAppName() {
        File basePath = new File(OpenCms.getSystemInfo().getBasePath());
        String webAppName = basePath.getParentFile().getName();
        return webAppName;
    }    
    
    /** 
     * Returns the OpenCms web application base path.<p>
     *
     * @return the OpenCms web application base path
     */
    public String getBasePath() {
        return m_basePath;
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
    
    /** 
     * Sets the OpenCms web application base path.<p>
     *
     * @param basePath the OpenCms web application base path to set
     */
    protected void setBasePath(String basePath) {
        // init base path
        basePath = basePath.replace('\\', '/');
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }
        m_basePath = CmsLinkManager.normalizeRfsPath(basePath);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info("OpenCms: Base application path is " + m_basePath);
        }        
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
     * Returns the filename of the logfile.<p>
     * 
     * @return the filename of the logfile
     */
    public String getLogFileName() {
        return m_logFileName;
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
     * Sets the OpenCms request context.<p>
     * 
     * @param context the OpenCms request context
     */
    protected void setOpenCmsContext(String context) {
        if ((context != null) && (context.startsWith("/ROOT"))) {
            context = context.substring("/ROOT".length());
        }
        if (context == null) {
            context = "";
        }
        m_openCmsContext = context;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". OpenCms context is   : " + m_openCmsContext);
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
     * Returns the OpenCms server name.<p>
     * 
     * @return the OpenCms server name
     */
    public String getServerName() {
        return m_serverName;
    }    
}
