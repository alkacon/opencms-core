/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/A_OpenCms.java,v $
 * Date   : $Date: 2003/08/11 11:00:11 $
 * Version: $Revision: 1.51 $
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

package com.opencms.core;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsDriverManager;
import org.opencms.loader.CmsLoaderManager;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.staticexport.CmsStaticExportProperties;

import com.opencms.boot.CmsBase;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.I_CmsRegistry;
import com.opencms.flex.CmsEvent;
import com.opencms.flex.I_CmsEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import source.org.apache.java.util.Configurations;

/**
 * Abstract class for the OpenCms "operating system" that provides 
 * public static methods which can be used by other classes to access 
 * basic features of OpenCms like logging etc.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.51 $
 */
public abstract class A_OpenCms {

    /** The default mimetype */
    protected static final String C_DEFAULT_MIMETYPE = "text/html";

    /** Static version name to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NAME = "Salusa Secundus";

    /** Static version number to use if version.properties can not be read */
    private static final String C_DEFAULT_VERSION_NUMBER = "5.1.x";

    /** Default encoding, can be overwritten in "opencms.properties" */
    private static String m_defaultEncoding = "ISO-8859-1";

    /** The default user and group names */
    private static CmsDefaultUsers m_defaultUsers;

    /** The driver manager to access the database */
    protected static CmsDriverManager m_driverManager = null;

    /** The object to store the properties from the opencms.property file for the static export */
    private static CmsStaticExportProperties m_exportProperties;
    
    /** The link manager to resolve links in &lt;link&gt; tags */
    private static CmsLinkManager m_linkManager;    

    /** List to save the event listeners in */
    private static java.util.ArrayList m_listeners = new ArrayList();

    /** The loader manager used for loading individual resources */
    private static CmsLoaderManager m_loaderManager;

    /** The filename of the log file */
    private static String m_logfile;
    
    /** The OpenCms map of configured mime types */
    private static Map m_mimeTypes;

    /** The OpenCms context and servlet path, e.g. <code>/opencms/opencms</code> */
    private static String m_openCmsContext = null;

    /** A Map for the storage of various runtime properties */
    private static Map m_runtimeProperties = null;
    
    /** The site manager contains information about all configured sites */
    private static CmsSiteManager m_siteManager;

    /** The default setting for the user access flags */
    private static int m_userDefaultaccessFlags = I_CmsConstants.C_ACCESS_DEFAULT_FLAGS;

    /** The default setting for the user language */
    private static String m_userDefaultLanguage = null;

    /** The version name (including version number) of this OpenCms installation */
    private static String m_versionName = C_DEFAULT_VERSION_NUMBER + " " + C_DEFAULT_VERSION_NAME;

    /** The version number of this OpenCms installation */
    private static String m_versionNumber = C_DEFAULT_VERSION_NUMBER;

    /** The OpenCms configuration read from <code>opencms.properties</code> */
    private Configurations m_conf = null;

    /**
     * Add a cms event listener.<p>
     *
     * @param listener the listener to add
     */
    public static void addCmsEventListener(I_CmsEventListener listener) {
        synchronized (m_listeners) {
            m_listeners.add(listener);
        }
    }

    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.<p>  
     * 
     * The default implementation performs
     * this notification synchronously using the calling thread.<p>
     *
     * @param event a CmsEvent
     */
    public static void fireCmsEvent(CmsEvent event) {
        if (m_listeners.size() < 1)
            return;
        I_CmsEventListener list[] = new I_CmsEventListener[0];
        synchronized (m_listeners) {
            list = (I_CmsEventListener[])m_listeners.toArray(list);
        }
        for (int i = 0; i < list.length; i++)
             list[i].cmsEvent(event);
    }

    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.<p>  
     * 
     * The default implementation performs
     * this notification synchronously using the calling thread.<p>
     *
     * @param cms an initialized CmsObject
     * @param type event type
     * @param data event data
     */
    public static void fireCmsEvent(CmsObject cms, int type, java.util.Map data) {
        A_OpenCms.fireCmsEvent(new CmsEvent(cms, type, data));
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
    public static String getDefaultEncoding() {
        return m_defaultEncoding;
    }
    
    /**
     * Returns the default user and group name configuration.<p>
     * 
     * @return the default user and group name configuration
     */
    public static CmsDefaultUsers getDefaultUsers() {
        return m_defaultUsers;
    }
    
    /**
     * Returns the link manager to resolve links in &lt;link&gt; tags.<p>
     * 
     * @return  the link manager to resolve links in &lt;link&gt; tags
     */
    public static CmsLinkManager getLinkManager() {
        return m_linkManager;        
    }

    /**
     * Returns the loader manager used for loading individual resources.<p>
     * 
     * @return the loader manager used for loading individual resources
     */
    public static CmsLoaderManager getLoaderManager() {
        return m_loaderManager;
    }

    /**
     * Returns the filename of the logfile.<p>
     * 
     * @return The filename of the logfile.
     */
    public static String getLogFileName() {
        return m_logfile;
    }
    
    /**
     * Returns the mime type for a specified file.<p>
     * 
     * @param filename the file name to check the mime type for
     * @param encoding default encoding in case of mime types is of type "text"
     * @return the mime type for a specified file
     */
    public static String getMimeType(String filename, String encoding) {        
        String mimetype = null;
        int lastDot = filename.lastIndexOf(".");
        // check if there was a file extension
        if ((lastDot > 0) && (lastDot < (filename.length() - 1))) {
            String ext = filename.substring(lastDot + 1);
            mimetype = (String)m_mimeTypes.get(ext);
            // was there a mimetype fo this extension?
            if (mimetype == null) {
                mimetype = C_DEFAULT_MIMETYPE;
            }
        } else {
            mimetype = C_DEFAULT_MIMETYPE;
        }
        mimetype = mimetype.toLowerCase();
        if ((encoding != null)         
        && mimetype.startsWith("text")
        && (mimetype.indexOf("charset") == -1)) {
            mimetype += "; charset=" + encoding;
        }
        
        return mimetype;                
    }

    /**
     * Returns the OpenCms request context, e.g. /opencms/opencms.<p>
     * 
     * The context will always start with a "/" and never have a trailing "/".<p>
     * 
     * @return String the OpenCms request context, e.g. /opencms/opencms
     */
    public static String getOpenCmsContext() {
        if (m_openCmsContext == null) {
            throw new RuntimeException("OpenCmsContext not initialised!");
        }
        return m_openCmsContext;
    }

    /**
     * Returns the registry to read values from it.<p>
     * 
     * You don't have the permissions to write values. 
     * This is useful for modules to read module-parameters.<p>
     *
     * @return the registry
     * @throws CmsException if the registry can not be returned
     */
    public static I_CmsRegistry getRegistry() throws CmsException {
        if (m_driverManager == null)
            return null;
        return m_driverManager.getRegistry(null);
    }

    /** 
     * Looks up a value in the runtime property Map.<p>
     *
     * @param key the key to look up in the runtime properties
     * @return the value for the key, or null if the key was not found
     */
    public static Object getRuntimeProperty(Object key) {
        if (m_runtimeProperties == null)
            return null;
        return m_runtimeProperties.get(key);
    }

    /**
     * Returns the initialized site manager, 
     * which contains information about all configured sites.<p> 
     * 
     * @return the initialized site manager
     */
    public static CmsSiteManager getSiteManager() {
        return m_siteManager;
    }

    /**
     * Returns the properties for the static export.<p>
     * 
     * @return the properties for the static export
     */
    public static CmsStaticExportProperties getStaticExportProperties() {
        return m_exportProperties;
    }

    /**
     * Returns the value for the default user access flags.<p>
     * 
     * @return the value for the default user access flags
     */
    public static int getUserDefaultAccessFlags() {
        return m_userDefaultaccessFlags;
    }

    /**
     * Returns the value of the user default language.<p>
     * 
     * @return the value of the user default language
     */
    public static String getUserDefaultLanguage() {
        return m_userDefaultLanguage;
    }

    /**
     * Returns a String containing the version information (version name and version number) 
     * of this OpenCms system.<p>
     *
     * @return version a String containing the version information
     */
    public static String getVersionName() {
        return m_versionName;
    }

    /**
     * Returns a String containing the version number 
     * of this OpenCms system.<p>
     *
     * @return version a String containing the version number
     */
    public static String getVersionNumber() {
        return m_versionNumber;
    }

    /**
     * Initializes the logging mechanism of OpenCms.<p>
     * 
     * @param config The configurations read from <code>opencms.properties</code>
     */
    public static void initializeServletLogging(Configurations config) {
        m_logfile = config.getString("log.file");
        CmsBase.initializeServletLogging(config);
    }

    /**
     * Initializes the version for this OpenCms, will be called by 
     * CmsHttpServlet or CmsShell upon system startup.<p>
     * 
     * @param o instance of calling object
     */
    static void initVersion(Object o) {
        // read the version-informations from properties, if not done
        Properties props = new Properties();
        try {
            props.load(o.getClass().getClassLoader().getResourceAsStream("com/opencms/core/version.properties"));
        } catch (Throwable t) {
            // ignore this exception - no properties found
            return;
        }
        m_versionNumber = props.getProperty("version.number", C_DEFAULT_VERSION_NUMBER);
        m_versionName = m_versionNumber + " " + props.getProperty("version.name", C_DEFAULT_VERSION_NAME);
    }

    /**
     * Checks if the system logging is active.<p>
     * 
     * @return <code>true</code> if the logging is active, <code>false</code> otherwise.
     */
    public static boolean isLogging() {
        return CmsBase.isLogging();
    }

    /**
     * Checks if the system logging is active for the selected channel.<p>
     * 
     * @param channel the channel where to log the message to
     * @return <code>true</code> if the logging is active for the channel, <code>false</code> otherwise.
     */
    public static boolean isLogging(String channel) {
        return CmsBase.isLogging(channel);
    }

    /**
     * Logs a message into the OpenCms logfile.<p>
     * 
     * If the logfile was not initialized (e.g. due tue a missing
     * ServletConfig while working with the console)
     * any log output will be written to <code>System.err</code>.
     * 
     * @param channel The channel the message is logged into
     * @param message The message to be logged.
     */
    public static void log(String channel, String message) {
        CmsBase.log(channel, message);
    }

    /**
     * Removes a cms event listener.<p>
     *
     * @param listener the listener to remove
     */
    public static void removeCmsEventListener(I_CmsEventListener listener) {
        synchronized (m_listeners) {
            m_listeners.remove(listener);
        }
    }

    /**
     * Sets the OpenCms request context.<p>
     * 
     * @param value the OpenCms request context
     */
    protected static void setOpenCmsContext(String value) {
        if ((value != null) && (value.startsWith("/ROOT"))) {
            value = value.substring("/ROOT".length());
        }
        if (value == null)
            value = "";
        m_openCmsContext = value;
    }

    /**       
     * This method adds an Object to the OpenCms runtime properties.
     * The runtime properties can be used to store Objects that are shared
     * in the whole system.<p>
     *
     * @param key the key to add the Object with
     * @param value the value of the Object to add
     */
    public static void setRuntimeProperty(Object key, Object value) {
        if (m_runtimeProperties == null) {
            m_runtimeProperties = Collections.synchronizedMap(new HashMap());
        }
        m_runtimeProperties.put(key, value);
    }

    /**
     * Sets the site manager, 
     * which contains information about all configured sites.<p> 
     * 
     * @param siteManager the site manager to set
     */
    protected static void setSiteManager(CmsSiteManager siteManager) {
        m_siteManager = siteManager;
    }
    
    /**
     * Sets the properties for the static export.<p>
     * 
     * @param exportProperties the properties for the static export
     */
    protected static void setStaticExportProperties(CmsStaticExportProperties exportProperties) {
        m_exportProperties = exportProperties;
    }

    /**
     * Seats the value of the user default access flags.
     * 
     * @param flags the new value of the user default access flags
     */
    protected static void setUserDefaultAccessFlags(int flags) {
        m_userDefaultaccessFlags = flags;
    }

    /**
     * Sets the value of the user default language.<p>
     * 
     * @param language the new value of the user default language
     */
    protected static void setUserDefaultLanguage(String language) {
        m_userDefaultLanguage = language;
    }

    /** 
     * Destructor method, to be called when the the class instance is shut down.<p>
     * 
     * @throws Throwable in case something goes wrong during shutdown 
     */
    abstract void destroy() throws Throwable;

    /** 
     * This method returns the runtime configuration.
     *
     * @return The runtime configuration.
     */
    public Configurations getConfiguration() {
        return m_conf;
    }

    /** 
     * Returns the complete runtime property Map.<p>
     *
     * @return the Map of runtime properties
     */
    public Map getRuntimePropertyMap() {
        return m_runtimeProperties;
    }

    /** 
     * Sets the runtime configuration.<p>
     * 
     * @param conf the configuration to set
     */
    public void setConfiguration(Configurations conf) {
        m_conf = conf;
    }

    /**
     * Sets the default encoding to the value specified.<p>
     * 
     * @param encoding the value to set, e.g. "UTF-8" or "ISO-8859-1".
     */
    protected void setDefaultEncoding(String encoding) {
        m_defaultEncoding = encoding;
    }
    
    /**
     * Sets the default user and group name configuration
     * 
     * @param defaultUsers the default user and group name configuration
     */
    protected void setDefaultUsers(CmsDefaultUsers defaultUsers) {
        m_defaultUsers = defaultUsers;
    }
    
    /**
     * Sets the link manager to resolve links in &lt;link&gt; tags.<p>
     * 
     * @param linkManager the link manager to resolve links in &lt;link&gt; tags
     */ 
    protected void setLinkManager(CmsLinkManager linkManager) {
        m_linkManager = linkManager;
    }

    /**
     * Sets the loader manager used for loading individual resources.<p>
     * 
     * @param loaderManager the loader manager used for loading individual resources
     */
    protected void setLoaderManager(CmsLoaderManager loaderManager) {
        m_loaderManager = loaderManager;
    }
    
    /**
     * Initilizes the map of available mime types.<p>
     * 
     * @param types the map of available mime types
     */
    protected void setMimeTypes(Hashtable types) {
        m_mimeTypes = new HashMap(types.size());
        m_mimeTypes.putAll(types);
    }

    /**
     * Sets the mimetype of the response.<p>
     * 
     * The mimetype is selected by the file extension of the requested document.
     * If no available mimetype is found, it is set to the default
     * "application/octet-stream".
     *
     * @param cms The actual OpenCms object.
     * @param file The requested document.
     */
    abstract void setResponse(CmsObject cms, CmsFile file);

    /**
     * Starts a schedule job with a correct instantiated CmsObject.
     * 
     * @param entry the CmsCronEntry to start
     */
    abstract void startScheduleJob(CmsCronEntry entry);

    /**
     * Reads the current crontable entries from the database and updates the
     * crontable with them.<p>
     */
    abstract void updateCronTable();
}
