/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/OpenCmsCore.java,v $
 * Date   : $Date: 2004/11/11 16:03:04 $
 * Version: $Revision: 1.152 $
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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsImportExportConfiguration;
import org.opencms.configuration.CmsModuleConfiguration;
import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexCache;
import org.opencms.flex.CmsFlexCacheConfiguration;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsI18nInfo;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.importexport.CmsImportExportManager;
import org.opencms.loader.CmsResourceManager;
import org.opencms.loader.I_CmsFlexCacheEnabledLoader;
import org.opencms.lock.CmsLockManager;
import org.opencms.module.CmsModuleManager;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.monitor.CmsMemoryMonitorConfiguration;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.search.CmsSearchManager;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.util.CmsPropertyUtils;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.xml.CmsXmlContentTypeManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;

/**
 * This class is the main class of the OpenCms system,
 * think of it as the "operating system" of OpenCms.<p>
 *  
 * Any request to an OpenCms resource will be processed by this class first.
 * The class will try to map the request to a VFS (Virtual File System) resource,
 * i.e. an URI. If the resource is found, it will be read and forwarded to
 * to a resource loader, which then genertates the output of the requested resource.<p>
 *
 * There will be only one instance of this object created for
 * any accessing class. This means that in the default configuration, where 
 * OpenCms is accessed through a servlet context, there will be only one instance of 
 * this class in that servlet context.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.152 $
 * @since 5.1
 */
public final class OpenCmsCore {
    
    /** Name of the property file containing HTML fragments for setup wizard and error dialog. */
    public static final String C_FILE_HTML_MESSAGES = "org/opencms/main/htmlmsg.properties";

    /** Prefix for a critical init error. */
    public static final String C_MSG_CRITICAL_ERROR = "Critical init error/";

    /** Prefix for error messages for initialization errors. */
    private static final String C_ERRORMSG = "OpenCms initialization error!\n\n";

    /** One instance to rule them all, one instance to find them... */
    private static OpenCmsCore m_instance;
    
    /** Lock object for synchronization. */
    private static Object m_lock = new Object();

    /** The session manager. */
    private static OpenCmsSessionManager m_sessionManager;

    /** URI of the authentication form (read from properties) in case of form based authentication. */
    private String m_authenticationFormURI;
    
    /** The configuration manager that contains the information from the XML configuration. */
    private CmsConfigurationManager m_configurationManager;

    /** List of configured directory default file names. */
    private List m_defaultFiles;

    /** The default user and group names. */
    private CmsDefaultUsers m_defaultUsers;
    
    /** List to save the event listeners in. */
    private Map m_eventListeners;

    /** The set of configured export points. */
    private Set m_exportPoints;

    /** The site manager contains information about the Cms import/export. */
    private CmsImportExportManager m_importExportManager;

    /** The link manager to resolve links in &lt;link&gt; tags. */
    private CmsLinkManager m_linkManager;

    /** The locale manager used for obtaining the current locale. */
    private CmsLocaleManager m_localeManager;

    /** The lock manager used for the locking mechanism. */
    private CmsLockManager m_lockManager;

    /** The OpenCms log to write all log messages to. */
    private CmsLog m_log;

    /** The memory monitor for collection memory statistics. */
    private CmsMemoryMonitor m_memoryMonitor;
    
    /** The module manager. */
    private CmsModuleManager m_moduleManager;
    
    /** The password handler used to digest and validate passwords. */
    private I_CmsPasswordHandler m_passwordHandler;
    
    /** Map of request handlers. */
    private Map m_requestHandlers;

    /** Member variable to store instances to modify resources. */
    private List m_resourceInitHandlers;

    /** The resource manager. */
    private CmsResourceManager m_resourceManager;

    /** The runlevel of this OpenCmsCore object instance. */
    private int m_runLevel;

    /** A Map for the storage of various runtime properties. */
    private Map m_runtimeProperties;

    /** The configured scheduler manager. */
    private CmsScheduleManager m_scheduleManager;

    /** The search manager provides indexing and searching. */
    private CmsSearchManager m_searchManager;
    
    /** The security manager to access the database and validate user permissions. */
    private CmsSecurityManager m_securityManager;

    /** The session info storage for all active users. */
    private CmsSessionInfoManager m_sessionInfoManager;

    /** The site manager contains information about all configured sites. */
    private CmsSiteManager m_siteManager;
    
    /** The static export manager. */
    private CmsStaticExportManager m_staticExportManager;

    /** The system information container for "read only" system settings. */
    private CmsSystemInfo m_systemInfo;

    /** The thread store. */
    private CmsThreadStore m_threadStore;

    /** Flag to indicate if basic or form based authentication is used. */
    private boolean m_useBasicAuthentication;

    /** The workplace manager contains information about the global workplace settings. */
    private CmsWorkplaceManager m_workplaceManager;
    
    /** The XML contnet type manager that contains the initialized XML content types. */
    private CmsXmlContentTypeManager m_xmlContentTypeManager;
    
    /**
     * Protected constructor that will initialize the singleton OpenCms instance with runlevel 1.<p>
     * @throws CmsInitException in case of errors during the initialization
     */
    private OpenCmsCore() throws CmsInitException {
        
        synchronized (m_lock) {
            if (m_instance != null && (m_instance.getRunLevel() > 0)) {
                throw new CmsInitException("OpenCms already initialized!");
            }
            initMembers();
            setRunLevel(1);
            m_instance = this;
        }
    }

    /**
     * Returns the initialized OpenCms instance.<p>
     * 
     * @return the initialized OpenCms instance
     */
    public static OpenCmsCore getInstance() {
        
        if (m_instance == null) {
            try {
                // create a new core object with runlevel 1
                new OpenCmsCore();
            } catch (CmsInitException e) {
                // already initialized, this all we need
            }
        }
        return m_instance;
    }
    
    /**
     * Returns the configured export points,
     * the returned set being an unmodifiable set.<p>
     * 
     * @return an unmodifiable set of the configured export points
     */
    public Set getExportPoints() {
        
        return m_exportPoints;
    }

    /**
     * Returns the session manager.<p>
     * 
     * @return the session manager
     */
    public OpenCmsSessionManager getSessionManager() {
        
        return m_sessionManager;
    }

    /**
     * Reads the requested resource from the OpenCms VFS,
     * in case a directory name is requested, the default files of the 
     * directory will be looked up and the first match is returned.<p>
     *
     * @param cms the current CmsObject
     * @param resourceName the requested resource
     * @param req the current request
     * @param res the current response
     * @return CmsFile the requested file read from the VFS
     * 
     * @throws CmsException in case the file does not exist or the user has insufficient access permissions 
     */
    public CmsResource initResource(CmsObject cms, String resourceName, HttpServletRequest req, HttpServletResponse res) throws CmsException {

        CmsResource resource = null;
        CmsException tmpException = null;

        try {
            // try to read the requested resource
            resource = cms.readResource(resourceName);
            // resource exists, lets check if we have a file or a folder
            if (resource.isFolder()) {
                // the resource is a folder, check if C_PROPERTY_DEFAULT_FILE is set on folder
                try {                
                    String defaultFileName = cms.readPropertyObject(CmsResource.getFolderPath(cms.getSitePath(resource)), I_CmsConstants.C_PROPERTY_DEFAULT_FILE, false).getValue();
                    if (defaultFileName != null) {
                        // property was set, so look up this file first
                        String tmpResourceName = CmsResource.getFolderPath(cms.getSitePath(resource)) + defaultFileName;
                        resource = cms.readResource(tmpResourceName);
                        // no exception? so we have found the default file                         
                        cms.getRequestContext().setUri(tmpResourceName);
                    } 
                } catch (CmsSecurityException se) {
                    // permissions deny access to the resource
                    throw se;
                } catch (CmsException e) {
                    // ignore all other exceptions and continue the lookup process
                }                
                if (resource.isFolder()) {
                    // resource is (still) a folder, check default files specified in configuration
                    for (int i = 0; i < m_defaultFiles.size(); i++) {
                        String tmpResourceName = CmsResource.getFolderPath(cms.getSitePath(resource)) + m_defaultFiles.get(i);
                        try {      
                            resource = cms.readResource(tmpResourceName);
                            // no exception? So we have found the default file                         
                            cms.getRequestContext().setUri(tmpResourceName);
                            // stop looking for default files   
                            break;
                        } catch (CmsSecurityException se) {
                            // permissions deny access to the resource
                            throw se;
                        } catch (CmsException e) {
                            // ignore all other exceptions and continue the lookup process
                        }     
                    }           
                }
            }
            if (resource.isFolder()) {
                // we only want files as a result for further processing
                resource = null;
            }
        } catch (CmsException e) {
            // file or folder with given name does not exist, store exception
            tmpException = e;
            resource = null;
        }
          
        if (resource != null) {
            // test if this file is only available for internal access operations
            if ((resource.getFlags() & I_CmsConstants.C_ACCESS_INTERNAL_READ) > 0) {
                throw new CmsException(CmsException.C_ERROR_DESCRIPTION[CmsException.C_INTERNAL_FILE] + cms.getRequestContext().getUri(), CmsException.C_INTERNAL_FILE);
            }
        }

        // test if this file has to be checked or modified
        Iterator i = m_resourceInitHandlers.iterator();
        while (i.hasNext()) {
            try {
                resource = ((I_CmsResourceInit)i.next()).initResource(resource, cms, req, res);
                // the loop has to be interrupted when the exception is thrown!
            } catch (CmsResourceInitException e) {
                break;
            }
        }

        // file is still null and not found exception was thrown, so throw original exception
        if (resource == null && tmpException != null) {
            throw tmpException;
        }

        // return the resource read from the VFS
        return resource;
    }

    /**
     * Add a cms event listener that listens to all events.<p>
     *
     * @param listener the listener to add
     */
    protected void addCmsEventListener(I_CmsEventListener listener) {
        
        addCmsEventListener(listener, null);
    }

    /**
     * Add a cms event listener.<p>
     *
     * @param listener the listener to add
     * @param eventTypes the events to listen for
     */
    protected void addCmsEventListener(I_CmsEventListener listener, int[] eventTypes) {
        synchronized (m_eventListeners) {
            if (eventTypes == null) {
                eventTypes = new int[] {I_CmsEventListener.LISTENERS_FOR_ALL_EVENTS.intValue()};
            }
            for (int i = 0; i < eventTypes.length; i++) {
                Integer eventType = new Integer(eventTypes[i]);
                List listeners = (List)m_eventListeners.get(eventType);
                if (listeners == null) {
                    listeners = new ArrayList();
                    m_eventListeners.put(eventType, listeners);
                }
                listeners.add(listener);
            }
        }
    }
    
    /**
     * Adds the specified request handler to the Map of OpenCms request handlers. <p>
     * 
     * @param handler the handler to add
     */
    protected void addRequestHandler(I_CmsRequestHandler handler) {
        
        if (handler == null) {
            return;
        }
        String[] names = handler.getHandlerNames();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (m_requestHandlers.get(name) != null) {
                if (getLog(this).isErrorEnabled()) {
                    getLog(this).error("Duplicate OpenCms request handler, ignoring '" + name + "'");
                }
                continue;
            }
            m_requestHandlers.put(name, handler);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Added RequestHandler : " + name + " (" + handler.getClass().getName() + ")");
            }
        }
    }

    /**
     * Destroys this OpenCms instance, called if the servlet (or shell) is shut down.<p> 
     */
    protected void destroy() {

        synchronized (m_lock) {
            if (m_runLevel > 1) {
                // runlevel 0 or 1 does not require any shutdown handling
                System.err.println("\n\nShutting down OpenCms, version " + getSystemInfo().getVersionName() + " in web application '" + getSystemInfo().getWebApplicationName() + "'");
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(".");
                    getLog(CmsLog.CHANNEL_INIT).info(".");
                    getLog(CmsLog.CHANNEL_INIT).info(".                      ...............................................................");
                    getLog(CmsLog.CHANNEL_INIT).info(". Performing shutdown  : OpenCms version " + getSystemInfo().getVersionName());
                    getLog(CmsLog.CHANNEL_INIT).info(". Shutdown time        : " + (new Date(System.currentTimeMillis())));
                }
                try {
                    if (m_threadStore != null) {
                        m_threadStore.shutDown();
                    }
                    if (m_scheduleManager != null) {
                        m_scheduleManager.shutDown();
                    }
                    if (m_securityManager != null) {
                        m_securityManager.destroy();
                    }
                } catch (Throwable e) {
                    if (getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                        getLog(CmsLog.CHANNEL_INIT).error(". Error during shutdown: " + e.toString(), e);
                    }
                }
        
                String runtime = CmsStringUtil.formatRuntime(getSystemInfo().getRuntime());
                if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).info(". OpenCms stopped!     : Total uptime was " + runtime);
                    getLog(CmsLog.CHANNEL_INIT).info(".                      ...............................................................");
                    getLog(CmsLog.CHANNEL_INIT).info(".");
                    getLog(CmsLog.CHANNEL_INIT).info(".");
                }
                System.err.println("Shutdown completed, total uptime was " + runtime + ".\n");
            }
            m_instance = null;
        }
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     *
     * @param event a CmsEvent
     */
    protected void fireCmsEvent(CmsEvent event) {

        fireCmsEventHandler((List)m_eventListeners.get(event.getTypeInteger()), event);
        fireCmsEventHandler((List)m_eventListeners.get(I_CmsEventListener.LISTENERS_FOR_ALL_EVENTS), event);
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     * 
     * @param type event type
     * @param data event data
     */
    protected void fireCmsEvent(int type, Map data) {

        fireCmsEvent(new CmsEvent(type, data));
    }

    /**
     * Returns the configured list of default directory file names.<p>
     *  
     * Caution: This list can not be modified.<p>
     * 
     * @return the configured list of default directory file names
     */
    protected List getDefaultFiles() {

        return m_defaultFiles;
    }

    /**
     * Returns the default user and group name configuration.<p>
     * 
     * @return the default user and group name configuration
     */
    protected CmsDefaultUsers getDefaultUsers() {

        return m_defaultUsers;
    }

    /**
     * Returns the initialized import/export manager,
     * which contains information about the Cms import/export.<p>
     * 
     * @return the initialized import/export manager
     */
    protected CmsImportExportManager getImportExportManager() {

        return m_importExportManager;
    }

    /**
     * Returns the link manager to resolve links in &lt;link&gt; tags.<p>
     * 
     * @return  the link manager to resolve links in &lt;link&gt; tags
     */
    protected CmsLinkManager getLinkManager() {

        return m_linkManager;
    }

    /**
     * Returns the locale manager used for obtaining the current locale.<p>
     * 
     * @return the locale manager
     */
    protected CmsLocaleManager getLocaleManager() {

        return m_localeManager;
    }

    /**
     * Returns the lock manager used for the locking mechanism.<p>
     * 
     * @return the lock manager used for the locking mechanism
     */
    protected CmsLockManager getLockManager() {

        return m_lockManager;
    }

    /**
     * Returns the log for the selected object.<p>
     * 
     * If the provided object is a String, this String will
     * be used as channel name. Otherwise the objects 
     * class name will be used as channel name.<p>
     *  
     * @param obj the object channel to use
     * @return the log for the selected object channel
     */
    protected Log getLog(Object obj) {

        if ((obj == null) || (!m_log.isInitialized())) {
            return m_log;
        }
        return m_log.getLogger(obj);
    }

    /**
     * Returns the memory monitor.<p>
     * 
     * @return the memory monitor
     */
    protected CmsMemoryMonitor getMemoryMonitor() {

        return m_memoryMonitor;
    }

    /**
     * Returns the module manager.<p>
     * 
     * @return the module manager
     */
    protected CmsModuleManager getModuleManager() {

        return m_moduleManager;
    }

    /**
     * Return the password handler.<p>
     * 
     * @return the password handler
     */
    protected I_CmsPasswordHandler getPasswordHandler() {

        return m_passwordHandler;
    }

    /**
     * Returns the handler instance for the specified name, 
     * or null if the name does not match any handler name.<p>
     * 
     * @param name the name of the handler instance to return
     * @return the handler instance for the specified name
     */
    protected I_CmsRequestHandler getRequestHandler(String name) {

        return (I_CmsRequestHandler)m_requestHandlers.get(name);
    }

    /**
     * Returns the resource manager.<p>
     * 
     * @return the resource manager
     */
    protected CmsResourceManager getResourceManager() {

        return m_resourceManager;
    }

    /** 
     * Returns the runlevel of this OpenCmsCore object instance.<p>
     * 
     * @return the runlevel of this OpenCmsCore object instance
     */
    protected int getRunLevel() {

        return m_runLevel;
    }

    /** 
     * Looks up a value in the runtime property Map.<p>
     *
     * @param key the key to look up in the runtime properties
     * @return the value for the key, or null if the key was not found
     */
    protected Object getRuntimeProperty(Object key) {

        if (m_runtimeProperties == null) {
            return null;
        }
        return m_runtimeProperties.get(key);
    }

    /** 
     * Returns the complete runtime property Map.<p>
     *
     * @return the Map of runtime properties
     */
    protected Map getRuntimePropertyMap() {

        return m_runtimeProperties;
    }

    /**
     * Returns the configured schedule manager.<p>
     *
     * @return the configured schedule manager
     */
    protected CmsScheduleManager getScheduleManager() {

        return m_scheduleManager;
    }

    /**
     * Returns the initialized search manager,
     * which provides indexing and searching operations.<p>
     * 
     * @return the initialized search manager
     */
    protected CmsSearchManager getSearchManager() {

        return m_searchManager;
    }

    /**
     * Returns the initialized OpenCms security manager.<p>
     * 
     * @return the initialized OpenCms security manager
     */
    protected CmsSecurityManager getSecurityManager() {

        return m_securityManager;
    }

    /**
     * Returns the session info storage for all active users.<p>
     * 
     * @return the session info storage for all active users
     */
    protected CmsSessionInfoManager getSessionInfoManager() {

        return m_sessionInfoManager;
    }

    /**
     * Returns the initialized site manager, 
     * which contains information about all configured sites.<p> 
     * 
     * @return the initialized site manager
     */
    protected CmsSiteManager getSiteManager() {

        return m_siteManager;
    }

    /**
     * Returns the properties for the static export.<p>
     * 
     * @return the properties for the static export
     */
    protected CmsStaticExportManager getStaticExportManager() {

        return m_staticExportManager;
    }

    /**
     * Returns the system information storage.<p> 
     * 
     * @return the system information storage
     */
    protected CmsSystemInfo getSystemInfo() {

        return m_systemInfo;
    }

    /**
     * Returns the OpenCms Thread store.<p>
     * 
     * @return the OpenCms Thread store
     */
    protected CmsThreadStore getThreadStore() {

        return m_threadStore;
    }

    /**
     * Returns the initialized workplace manager, 
     * which contains information about the global workplace settings.<p> 
     * 
     * @return the initialized workplace manager
     */
    protected CmsWorkplaceManager getWorkplaceManager() {

        return m_workplaceManager;
    }

    /**
     * Returns the XML content type manager.<p>
     * 
     * @return the XML content type manager
     */
    protected CmsXmlContentTypeManager getXmlContentTypeManager() {

        if (m_xmlContentTypeManager != null) {
            return m_xmlContentTypeManager;
        }
        if (m_runLevel < 2) {
            // this is only to enable test cases to run 
            m_xmlContentTypeManager = CmsXmlContentTypeManager.createTypeManagerForTestCases();
        }
        return m_xmlContentTypeManager;
    }
    
    /**
     * Returns an initialized CmsObject with the user and context initialized as provided.<p>
     * 
     * Note: Only if the provided <code>adminCms</code> CmsObject has admin permissions, 
     * this method allows the creation a CmsObject for any existing user. Otherwise
     * only the default users 'Guest' and 'Export' can initialized with 
     * this method, all other user names will throw an Exception.<p>
     * 
     * @param adminCms must either be initialized with "Admin" permissions, or null
     * @param contextInfo the context info to create a CmsObject for
     * 
     * @return an initialized CmsObject with the given users permissions
     * 
     * @throws CmsException if an invalid user name was provided, or if something else goes wrong
     * 
     * @see org.opencms.db.CmsDefaultUsers#getUserGuest()
     * @see org.opencms.db.CmsDefaultUsers#getUserExport()
     * @see OpenCms#initCmsObject(CmsObject, CmsContextInfo)
     * @see #initCmsObject(String)
     */    
    protected CmsObject initCmsObject(CmsObject adminCms, CmsContextInfo contextInfo) throws CmsException {
        
        String user = contextInfo.getUserName();
        
        if (adminCms == null || !adminCms.isAdmin()) {
            if (!user.equals(getDefaultUsers().getUserGuest()) 
             && !user.equals(getDefaultUsers().getUserExport())) {
             // if no admin object is provided, only "Guest" or "Export" user can be generated
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Invalid access to user '" + user + "' attempted" 
                        + ((adminCms!=null)?(" by " + adminCms.getRequestContext().currentUser().getName()):""));
                }                    
                throw new CmsSecurityException("Invalid user for default CmsObject initialization: " + user, 
                    CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
            }
        }
        
        return initCmsObject(contextInfo, null);        
    }

    /**
     * Returns an initialized CmsObject with the user initialized as provided,
     * with the "Online" project selected and "/" set as the current site root.<p>
     * 
     * Note: Only the default users 'Guest' and 'Export' can initialized with 
     * this method, all other user names will throw an Exception.<p>
     * 
     * @param user the user name to initialize, can only be 
     *        {@link org.opencms.db.CmsDefaultUsers#getUserGuest()} or
     *        {@link org.opencms.db.CmsDefaultUsers#getUserExport()}
     * 
     * @return an initialized CmsObject with the given users permissions
     * 
     * @throws CmsException if an invalid user name was provided, or if something else goes wrong
     * 
     * @see org.opencms.db.CmsDefaultUsers#getUserGuest()
     * @see org.opencms.db.CmsDefaultUsers#getUserExport()
     * @see OpenCms#initCmsObject(String)
     * @see #initCmsObject(CmsObject, CmsContextInfo)
     */
    protected CmsObject initCmsObject(String user) throws CmsException {

        return initCmsObject(null, new CmsContextInfo(user));
    }

    /**
     * Constructor to create a new OpenCms object.<p>
     * 
     * It reads the configurations from the <code>opencms.properties</code>
     * file in the <code>config/</code> subdirectory. With the information 
     * from this file is inits a ResourceBroker (Database access module),
     * various caching systems and other options.<p>
     * 
     * This will only be done once per accessing class.
     *
     * @param configuration the configurations from the <code>opencms.properties</code> file
     * @throws Exception in case of problems initializing OpenCms, this is usually fatal 
     */
    protected synchronized void initConfiguration(ExtendedProperties configuration) throws Exception {
                
        String systemEncoding = null;
        try {
            systemEncoding = System.getProperty("file.encoding");
        } catch (SecurityException se) {
            // security manager is active, but we will try other options before giving up
        }
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". System file.encoding : " + systemEncoding);
        }
        
        // read server ethernet address (MAC) and init UUID generator
        String ethernetAddress = configuration.getString("server.ethernet.address", CmsUUID.getDummyEthernetAddress());
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Ethernet address used: " + ethernetAddress);
        }
        CmsUUID.init(ethernetAddress);

        // set the server name
        String serverName = configuration.getString("server.name", "OpenCmsServer");
        getSystemInfo().setServerName(serverName);

        // initialize the lock manager
        m_lockManager = CmsLockManager.getInstance();

        // check the installed Java SDK
        try {
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                String jdkinfo = System.getProperty("java.vm.name") + " ";
                jdkinfo += System.getProperty("java.vm.version") + " ";
                jdkinfo += System.getProperty("java.vm.info") + " ";
                jdkinfo += System.getProperty("java.vm.vendor") + " ";
                getLog(CmsLog.CHANNEL_INIT).info(". Java VM in use       : " + jdkinfo);
                String osinfo = System.getProperty("os.name") + " ";
                osinfo += System.getProperty("os.version") + " ";
                osinfo += System.getProperty("os.arch") + " ";
                getLog(CmsLog.CHANNEL_INIT).info(". Operating sytem      : " + osinfo);
            }
        } catch (Exception e) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "2", e);
            }
            // any exception here is fatal and will cause a stop in processing
            throw e;
        }           
        
        // initialize the memory monitor
        m_memoryMonitor = new CmsMemoryMonitor();
        
        // create the configuration manager instance    
        m_configurationManager = new CmsConfigurationManager(getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("config/"));
        // now load the XML configuration
        m_configurationManager.loadXmlConfiguration();
        // store the configuration read from "opencms.properties" in the configuration manager 
        m_configurationManager.setConfiguration(configuration);
        
        // get the system configuration
        CmsSystemConfiguration systemConfiguration = (CmsSystemConfiguration)m_configurationManager.getConfiguration(CmsSystemConfiguration.class);
        
        // check the opencms.properties for the encoding setting
        String setEncoding = systemConfiguration.getDefaultContentEncoding();
        String defaultEncoding = CmsEncoder.lookupEncoding(setEncoding, null);
        if (defaultEncoding == null) {
            String msg = "OpenCms startup failure: Configured encoding '" + setEncoding + "' not supported by the Java VM";
            getLog(this).fatal(OpenCmsCore.C_MSG_CRITICAL_ERROR + "1: " + msg);
            throw new Exception(msg);            
        }
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms encoding     : " + defaultEncoding);
        }
        getSystemInfo().setDefaultEncoding(defaultEncoding);
        
        // initialize the memory monitor
        CmsMemoryMonitorConfiguration memoryMonitorConfiguration = systemConfiguration.getCmsMemoryMonitorConfiguration(); 
        m_memoryMonitor.initialize(memoryMonitorConfiguration);
                
        // set version history information        
        getSystemInfo().setVersionHistorySettings(systemConfiguration.isVersionHistoryEnabled(), systemConfiguration.getVersionHistoryMaxCount());
        // set mail configuration
        getSystemInfo().setMailSettings(systemConfiguration.getMailSettings());
        // set synchronize configuration
        getSystemInfo().setSynchronizeSettings(new CmsSynchronizeSettings());   
        // set the scheduler manager
        m_scheduleManager = systemConfiguration.getScheduleManager();                
        // set resource init classes
        m_resourceInitHandlers = systemConfiguration.getResourceInitHandlers();        
        // register request handler classes
        Iterator it = systemConfiguration.getRequestHandlers().iterator();
        while (it.hasNext()) {
            I_CmsRequestHandler handler = (I_CmsRequestHandler)it.next();
            addRequestHandler(handler);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". Request handler class: " + handler.getClass().getName() + " activated");
            }                    
        }    
        
        // read the default user configuration
        m_defaultUsers = systemConfiguration.getCmsDefaultUsers();
        
        // get Site Manager
        m_siteManager = systemConfiguration.getSiteManager();        
        
        // get the VFS / resource configuration
        CmsVfsConfiguration vfsConfiguation = (CmsVfsConfiguration)m_configurationManager.getConfiguration(CmsVfsConfiguration.class);
        m_resourceManager = vfsConfiguation.getResourceManager();        
        m_xmlContentTypeManager = vfsConfiguation.getXmlContentTypeManager();
        m_defaultFiles = vfsConfiguation.getDefaultFiles();

        // initialize translation engines
        m_resourceManager.setTranslators(vfsConfiguation.getFolderTranslator(), vfsConfiguation.getFileTranslator());

        // try to initialize the flex cache
        CmsFlexCache flexCache = null;
        try {
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Flex cache init      : starting");
            }
            // get the flex cache configuration from the SystemConfiguration
            CmsFlexCacheConfiguration flexCacheConfiguration = systemConfiguration.getCmsFlexCacheConfiguration();
            // pass configuration to flex cache for initialization
            flexCache = new CmsFlexCache(flexCacheConfiguration);
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Flex cache init      : finished");
            }
        } catch (Exception e) {
            if (getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).warn(". Flex cache init      : non-critical error " + e.toString());
            }
        }   
        
        if (flexCache != null) {
            // check all reasource loaders if they require the Flex cache
            Iterator i = m_resourceManager.getLoaders().iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof I_CmsFlexCacheEnabledLoader) {
                    // this resource loader requires the Flex cache
                    ((I_CmsFlexCacheEnabledLoader)o).setFlexCache(flexCache);                    
                }
            }
        }        
        
        // get the import/export configuration
        CmsImportExportConfiguration importExportConfiguration = (CmsImportExportConfiguration)m_configurationManager.getConfiguration(CmsImportExportConfiguration.class);
        m_importExportManager = importExportConfiguration.getImportExportManager();
        m_staticExportManager = importExportConfiguration.getStaticExportManager();
        
        // get the search configuration
        CmsSearchConfiguration searchConfiguration = (CmsSearchConfiguration)m_configurationManager.getConfiguration(CmsSearchConfiguration.class);
        m_searchManager = searchConfiguration.getSearchManager();                        
        
        // get the workplace configuration
        CmsWorkplaceConfiguration workplaceConfiguration = (CmsWorkplaceConfiguration)m_configurationManager.getConfiguration(CmsWorkplaceConfiguration.class);
        m_workplaceManager = workplaceConfiguration.getWorkplaceManager();
        // add the export points from the workplace
        addExportPoints(m_workplaceManager.getExportPoints());               
        
        // get the module configuration
        CmsModuleConfiguration moduleConfiguration = (CmsModuleConfiguration)m_configurationManager.getConfiguration(CmsModuleConfiguration.class); 
        m_moduleManager = moduleConfiguration.getModuleManager();

        // get the password handler
        m_passwordHandler = systemConfiguration.getPasswordHandler();
                
        try {
            // init the OpenCms security manager
            m_securityManager = CmsSecurityManager.newInstance(m_configurationManager, systemConfiguration.getRuntimeInfoFactory());
        } catch (Exception e) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error(OpenCmsCore.C_MSG_CRITICAL_ERROR + "3", e);
            }
            // any exception here is fatal and will cause a stop in processing
            throw new CmsException("Database init failed", CmsException.C_RB_INIT_ERROR, e);
        }      

        // initialize the Thread store
        m_threadStore = new CmsThreadStore();

        // initialize the link manager
        m_linkManager = new CmsLinkManager();
        
        if (m_runtimeProperties == null) {
            m_runtimeProperties = Collections.synchronizedMap(new HashMap());
        }
        m_runtimeProperties.putAll(systemConfiguration.getRuntimeProperties());

        // get an Admin cms context object with site root set to "/"
        CmsObject adminCms = initCmsObject(null, null, getDefaultUsers().getUserAdmin(), null);

        // initialize the scheduler
        m_scheduleManager.initialize(adminCms);
        
        // initialize the workplace manager
        m_workplaceManager.initialize(adminCms);
        
        // initialize the locale manager
        m_localeManager = systemConfiguration.getLocaleManager();
        m_localeManager.initialize(adminCms);
        
        // initialize the site manager
        m_siteManager.initialize(adminCms);
        
        // initialize the search manager
        m_searchManager.initialize(adminCms);
        
        // initialize the static export manager
        m_staticExportManager.initialize(adminCms);
        
        // initialize the XML content type manager
        m_xmlContentTypeManager.initialize(adminCms);

        // intialize the module manager
        m_moduleManager.initialize(adminCms, m_configurationManager);
        
        // initialize the resource manager
        m_resourceManager.initialize(adminCms);
    }

    /**
     * Initialization of the OpenCms runtime environment.<p>
     *
     * The connection information for the database is read 
     * from the <code>opencms.properties</code> configuration file and all 
     * driver manager are initialized via the initalizer, 
     * which usually will be an instance of a <code>OpenCms</code> class.
     *
     * @param context configuration of OpenCms from <code>web.xml</code>
     * @throws CmsInitException if initalization fails
     */
    protected synchronized void initContext(ServletContext context) throws CmsInitException {

        // read the the OpenCms servlet mapping from the servlet context parameters
        String servletMapping = context.getInitParameter("OpenCmsServlet");
        if (servletMapping == null) {
            m_instance = null;
            throw new CmsInitException("OpenCms servlet mapping not configured in 'web.xml', please set the 'OpenCmsServlet' parameter.");
        }

        // check for OpenCms home (base) directory path
        String base = context.getInitParameter("opencms.home");
        if (base == null || "".equals(base)) {
            base = searchWebInfFolder(context.getRealPath("/"));
            if (base == null || "".equals(base)) {
                throwInitException(new CmsInitException(C_ERRORMSG + "OpenCms base folder could not be guessed. Please define init parameter \"opencms.home\" in servlet engine configuration.\n\n"));
            }
        }
        
        // read the the default context name from the servlet context parameters
        String defaultWebApplication = context.getInitParameter("DefaultWebApplication");
        if (defaultWebApplication == null) {
            // not set in web.xml, so we use "ROOT" which should usually work since it is the (de-facto) standard 
            defaultWebApplication = "ROOT";
        }
        
        // read the the webapp context name from the servlet context parameters
        // this is needed in case an application server specific deployment descriptor is used to changed the webapp context
        String webApplicationContext = context.getInitParameter("WebApplicationContext");
        
        // now initialize the system info with the path and mapping information
        getSystemInfo().init(base, servletMapping, webApplicationContext, defaultWebApplication);

        // Collect the configurations 
        ExtendedProperties configuration = null;
        try {
            configuration = CmsPropertyUtils.loadProperties(getSystemInfo().getConfigurationFileRfsPath());
        } catch (Exception e) {
            throwInitException(new CmsInitException(C_ERRORMSG + "Trouble reading property file " + getSystemInfo().getConfigurationFileRfsPath() + ".\n\n", e));
        }
        
        // check if the wizard is enabled, if so stop initialization     
        if (configuration.getBoolean("wizard.enabled", true)) {
            m_instance = null;
            throw new CmsInitException("OpenCms setup wizard is enabled, unable to start OpenCms", CmsInitException.C_INIT_WIZARD_ENABLED);
        }
        
        // set path to log file
        String logfile = (String)configuration.get("log.file");
        if (logfile != null) {
            logfile = getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(logfile);
            configuration.put("log.file", logfile);
        }
        getSystemInfo().setLogFileRfsPath(logfile);

        // initialize the logging
        m_log.init(configuration, getSystemInfo().getConfigurationFileRfsPath());
        
        // output startup message to STDERR
        System.err.println("\n\nStarting OpenCms, version " + OpenCms.getSystemInfo().getVersionName() + " in web application '" + getSystemInfo().getWebApplicationName() + "'");
        for (int i = 0; i < I_CmsConstants.C_COPYRIGHT.length; i++) {
            System.err.println(I_CmsConstants.C_COPYRIGHT[i]);
        }
        System.err.println();

        // output startup message to logfile
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(".");
            getLog(CmsLog.CHANNEL_INIT).info(".");
            getLog(CmsLog.CHANNEL_INIT).info(".");
            getLog(CmsLog.CHANNEL_INIT).info(".");
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms version " + OpenCms.getSystemInfo().getVersionName());
            for (int i = 0; i < I_CmsConstants.C_COPYRIGHT.length; i++) {
                getLog(CmsLog.CHANNEL_INIT).info(". " + I_CmsConstants.C_COPYRIGHT[i]);
            }
            getLog(CmsLog.CHANNEL_INIT).info(".                      ...............................................................");
            getLog(CmsLog.CHANNEL_INIT).info(". Startup time         : " + (new Date(System.currentTimeMillis())));
            getLog(CmsLog.CHANNEL_INIT).info(". Servlet container    : " + context.getServerInfo());
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms version      : " + getSystemInfo().getVersionName());
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms webapp name  : " + getSystemInfo().getWebApplicationName());
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms servlet path : " + getSystemInfo().getServletPath());
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms context      : " + getSystemInfo().getOpenCmsContext());
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms WEB-INF path : " + getSystemInfo().getWebInfRfsPath());
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms property file: " + getSystemInfo().getConfigurationFileRfsPath());
            getLog(CmsLog.CHANNEL_INIT).info(". OpenCms log file     : " + getSystemInfo().getLogFileRfsPath());
        }

        try {
            // initialize the configuration
            initConfiguration(configuration);
        } catch (CmsException cmsex) {
            if (cmsex.getType() == CmsException.C_RB_INIT_ERROR) {
                throwInitException(new CmsInitException(C_ERRORMSG + "Could not connect to the database. Is the database up and running?\n\n", cmsex));
            }
        } catch (Exception exc) {
            throwInitException(new CmsInitException(C_ERRORMSG + "Trouble creating the com.opencms.core.CmsObject. Please check the root cause for more information.\n\n", exc));
        }       

        // initalize the session storage
        m_sessionInfoManager = new CmsSessionInfoManager();
        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info(". Session storage      : initialized");
        }

        // check if basic or form based authentication should be used      
        m_useBasicAuthentication = configuration.getBoolean("auth.basic", true);
        m_authenticationFormURI = configuration.getString("auth.form_uri", I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/authenticate.html");

        // check if the session manager is initialized (will be initialized from servlet listener)
        if (m_sessionManager != null) {
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". Session manager      : initialized");
            }
        } else {
            getLog(CmsLog.CHANNEL_INIT).error(". Session manager     : NOT initialized");
        }
    }
    
    /**
     * Initialize member variables.<p>
     */
    protected void initMembers() {

        synchronized (m_lock) {
            m_log = new CmsLog();
            m_resourceInitHandlers = new ArrayList();
            m_eventListeners = new HashMap();
            m_requestHandlers = new HashMap();
            m_systemInfo = new CmsSystemInfo();
            m_exportPoints = Collections.EMPTY_SET;
            m_defaultUsers = new CmsDefaultUsers();
            m_localeManager = new CmsLocaleManager(Locale.ENGLISH);
        }
    }

    /**
     * Initializes the system with the OpenCms servlet.<p>
     * 
     * This is the final step that is called on the servlets "init()" method.
     * It registers the servlets request handler and also outputs the final 
     * startup message. The servlet should auto-load since the &ltload-on-startup&gt;
     * parameter is set in the 'web.xml' by default.<p> 
     * 
     * @param servlet the OpenCms servlet
     */
    protected void initServlet(OpenCmsServlet servlet) {

        synchronized (m_lock) {
            // add the servlets request handler
            addRequestHandler(servlet);
            // output the final 'startup is finished' message
            if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                getLog(CmsLog.CHANNEL_INIT).info(". OpenCms is running!  : Total startup time was " + CmsStringUtil.formatRuntime(getSystemInfo().getRuntime()));
                getLog(CmsLog.CHANNEL_INIT).info(".                      ...............................................................");
                getLog(CmsLog.CHANNEL_INIT).info(".");
            }
        }
    }

    /**
     * Removes a cms event listener.<p>
     *
     * @param listener the listener to remove
     */
    protected void removeCmsEventListener(I_CmsEventListener listener) {

        synchronized (m_eventListeners) {
            Iterator it = m_eventListeners.keySet().iterator();
            while (it.hasNext()) {
                List listeners = (List)m_eventListeners.get(it.next());
                listeners.remove(listener);
            }
        }
    }

    /**
     * Searches for the OpenCms web application 'WEB-INF' folder during system startup.<p>
     * 
     * @param startFolder the folder where to start searching
     * @return String the path of the 'WEB-INF' folder in the 'real' file system
     */
    protected String searchWebInfFolder(String startFolder) {

        File f = new File(startFolder);
        if (!f.isDirectory()) {
            return null;
        }

        File configFile = new File(f, I_CmsConstants.C_CONFIGURATION_PROPERTIES_FILE.replace('/', File.separatorChar));
        if (configFile.exists() && configFile.isFile()) {
            return f.getAbsolutePath();
        }

        String webInfFolder = null;
        File[] subFiles = f.listFiles();
        for (int i = 0; i < subFiles.length; i++) {
            if (subFiles[i].isDirectory()) {
                webInfFolder = searchWebInfFolder(subFiles[i].getAbsolutePath());
                if (webInfFolder != null) {
                    break;
                }
            }
        }

        return webInfFolder;
    }

    /**       
     * This method adds an Object to the OpenCms runtime properties.
     * The runtime properties can be used to store Objects that are shared
     * in the whole system.<p>
     *
     * @param key the key to add the Object with
     * @param value the value of the Object to add
     */
    protected void setRuntimeProperty(Object key, Object value) {

        if (m_runtimeProperties == null) {
            m_runtimeProperties = Collections.synchronizedMap(new HashMap());
        }
        m_runtimeProperties.put(key, value);
    }

    /**
     * Sets the session manager.<p>
     * 
     * @param sessionManager the session manager to set
     */
    protected void setSessionManager(OpenCmsSessionManager sessionManager) {

        m_sessionManager = sessionManager;
    }

    /**
     * Displays a resource from the OpenCms by writing the result to the provided 
     * Servlet response output stream.<p>
     * 
     * @param req the current servlet request
     * @param res the current servlet response
     */
    protected void showResource(HttpServletRequest req, HttpServletResponse res) {

        CmsObject cms = null;
        try {
            cms = initCmsObject(req, res);
            // user is initialized, now deliver the requested resource
            CmsResource resource = initResource(cms, cms.getRequestContext().getUri(), req, res);
            if (resource != null) {
                // a file was read, go on process it
                m_resourceManager.loadResource(cms, resource, req, res);
                updateUser(cms, req);
            }

        } catch (Throwable t) {
            errorHandling(cms, req, res, t);
        }
    }

    /**
     * Upgrades to runlevel 2,
     * this is shell access but no Servlet context.<p>
     * 
     * @param configuration the configuration
     * @return the initialized OpenCmsCore
     */
    protected OpenCmsCore upgradeRunlevel(ExtendedProperties configuration) {
        
        synchronized (m_lock) {
            if ((m_instance != null) && (getRunLevel() == 2)) {
                // instance already in runlevel 2
                return m_instance;
            }
            setRunLevel(2);
            try {
                m_instance.initConfiguration(configuration);
            } catch (Throwable t) {
                if (getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).error("Critical error during OpenCms initialization", t);
                }
                m_instance = null;
            }
            return m_instance;
        }
    }

    /**
     * Upgrades to runlevel 3,
     * this is the final runlevel with an initialized Servlet context.<p>
     * 
     * @param context the context
     * @return the initialized OpenCmsCore
     */
    protected OpenCmsCore upgradeRunlevel(ServletContext context) {
        
        synchronized (m_lock) {
            if ((m_instance != null) && (getRunLevel() == 3)) {
                // instance already in runlevel 3
                return m_instance;
            }
            try {
                setRunLevel(3);
                m_instance.initContext(context);
            } catch (CmsInitException e) {
                if (e.getType() != CmsInitException.C_INIT_WIZARD_ENABLED) {
                    // do not output the "wizard enabled" message on the log
                    if (getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                        getLog(CmsLog.CHANNEL_INIT).error("Critical error during OpenCms initialization", e);
                    }
                }
                m_instance = null;
            } catch (Throwable t) {
                if (getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                    getLog(CmsLog.CHANNEL_INIT).error("Critical error during OpenCms initialization", t);
                }
                m_instance = null;
            }
            return m_instance;
        }
    }

    /**
     * Writes the XML configuration for the provided configuration class.<p>
     * 
     * @param clazz the configuration class to write the XML for
     */
    protected void writeConfiguration(Class clazz) {
        
        // exception handling is provided here to ensure identical log messages
        try {
            m_configurationManager.writeConfiguration(clazz);
        } catch (IOException e) {
            getLog(CmsConfigurationManager.class).error("Error writing configuration for class '" + clazz.getName() + "'", e);
        } catch (CmsConfigurationException e) {
            getLog(CmsConfigurationManager.class).error("Error writing configuration for class '" + clazz.getName() + "'", e);
        }
    }    
    
    /**
     * Adds the given set of export points to the list of all configured export points.<p> 
     * 
     * @param exportPoints the export points to add
     */
    private void addExportPoints(Set exportPoints) {

        // create a new immutable set of export points
        HashSet newSet = new HashSet(m_exportPoints.size() + exportPoints.size());
        newSet.addAll(exportPoints);
        newSet.addAll(m_exportPoints);
        m_exportPoints = Collections.unmodifiableSet(newSet);
    }

    /**
     * Checks if the current request contains http basic authentication information in 
     * the headers, if so tries to log in the user identified.<p>
     *  
     * @param cms the current cms context
     * @param req the current http request
     * @param res the current http response
     * @throws IOException in case of errors reading from the streams
     */
    private void checkBasicAuthorization(CmsObject cms, HttpServletRequest req, HttpServletResponse res) throws IOException {
        
        // no user identified from the session and basic authentication is enabled
        String auth = req.getHeader("Authorization");

        // user is authenticated, check password
        if (auth != null) {

            // only do basic authentification
            if (auth.toUpperCase().startsWith("BASIC ")) {

                // get encoded user and password, following after "BASIC "
                String base64Token = auth.substring(6);

                // decode it, using base 64 decoder
                String token = new String(Base64.decodeBase64(base64Token.getBytes()));
                String username = null;
                String password = null;
                int pos = token.indexOf(":");           
                if (pos != -1) {
                    username = token.substring(0, pos);
                    password = token.substring(pos + 1);
                }
                // authentication in the DB
                try {
                    try {
                        // try to login as a user first ...
                        cms.loginUser(username, password);
                    } catch (CmsException exc) {
                        // login as user failed, try as webuser ...
                        cms.loginWebUser(username, password);
                    }
                    // authentification was successful create a session
                    req.getSession(true);
                } catch (CmsSecurityException e) {
                    // authentification failed, so display a login screen
                    requestAuthorization(req, res);
                }
            }
        }
    }
    
    /**
     * Generates a formated exception output.<p>
     * 
     * Because the exception could be thrown while accessing the system files,
     * the complete HTML code must be added here!<p>
     * 
     * @param t the caught Exception
     * @param request the servlet request
     * @param cms the CmsObject
     * @return String containing the HTML code of the error message.
     */
    private String createErrorBox(Throwable t, HttpServletRequest request, CmsObject cms) {
        
        // load the property file that contains the html fragments for the dialog
        Properties htmlProps = new Properties();
        try {
            htmlProps.load(getClass().getClassLoader().getResourceAsStream(C_FILE_HTML_MESSAGES));
        } catch (Throwable thr) {
            if (getLog(this).isErrorEnabled()) {
                getLog(this).error("Could not load " + C_FILE_HTML_MESSAGES, thr);
            }
        }      
        
        // get localized message bundle
        CmsMessages messages = new CmsMessages(CmsWorkplaceMessages.C_BUNDLE_NAME, cms.getRequestContext().getLocale());
        
        // try to get the exception root cause
        Throwable cause = CmsFlexController.getThrowable(request);
        if (cause == null) {
            cause = t;
        }
        
        String errorHtml;
        // construct the error page
        errorHtml = htmlProps.getProperty("C_ERROR_DIALOG_START") 
                    + htmlProps.getProperty("C_STYLES") 
                    + htmlProps.getProperty("C_ERROR_DIALOG_END");
        
        
        errorHtml = CmsStringUtil.substitute(errorHtml, "${title}", messages.key("error.system.message"));
        errorHtml = CmsStringUtil.substitute(errorHtml, "${encoding}", getSystemInfo().getDefaultEncoding());
        errorHtml = CmsStringUtil.substitute(errorHtml, "${warnimageuri}", CmsWorkplace.getSkinUri() + "commons/error.gif");
        if (cause.getLocalizedMessage() != null) {
            errorHtml = CmsStringUtil.substitute(errorHtml, "${message}", "<p><b>" + CmsStringUtil.substitute(cause.getLocalizedMessage(), "\n", "\n<br>") + "</b></p>");
        } else {
            errorHtml = CmsStringUtil.substitute(errorHtml, "${message}", "<p><b>" + CmsStringUtil.substitute(cause.toString(), "\n", "\n<br>") + "</b></p>");
        }        
        errorHtml = CmsStringUtil.substitute(errorHtml, "${resource_key}", messages.key("error.system.resource"));
        errorHtml = CmsStringUtil.substitute(errorHtml, "${version_key}", messages.key("error.system.version"));
        errorHtml = CmsStringUtil.substitute(errorHtml, "${context_key}", messages.key("error.system.context"));
        String errorUri = CmsFlexController.getThrowableResourceUri(request);
        if (errorUri == null) {
            errorUri = cms.getRequestContext().getUri();
        }
        errorHtml = CmsStringUtil.substitute(errorHtml, "${resource}", errorUri);
        errorHtml = CmsStringUtil.substitute(errorHtml, "${version}", getSystemInfo().getVersionName());
        errorHtml = CmsStringUtil.substitute(errorHtml, "${context}", getSystemInfo().getOpenCmsContext());        
        errorHtml = CmsStringUtil.substitute(errorHtml, "${bt_close}", messages.key("button.close"));
        
        
        String exception = CmsException.getStackTraceAsString(cause);
        String details = "";
        
        if (exception == null || "".equals(exception.trim())) {
            // no stack trace available, do not show "details" button
            errorHtml = CmsStringUtil.substitute(errorHtml, "${button_details}", "");
        } else {
            // stack trace available, show the "details" button
            errorHtml = CmsStringUtil.substitute(errorHtml, "${button_details}", htmlProps.getProperty("C_BUTTON_DETAILS"));
            errorHtml = CmsStringUtil.substitute(errorHtml, "${bt_details}", messages.key("button.detail"));    
            exception = CmsStringUtil.escapeJavaScript(exception);
            exception = CmsStringUtil.substitute(exception, ">", "&gt;");
            exception = CmsStringUtil.substitute(exception, "<", "&lt;");
            details = "<html><body style='background-color: Window;'><pre>" + exception + "</pre></body></html>";
        }

        errorHtml = CmsStringUtil.substitute(errorHtml, "${details}", details);
        
        return errorHtml;
    }

    /**
     * This method performs the error handling for OpenCms.<p>
     *
     * @param cms the current cms context, might be null !
     * @param req the client request
     * @param res the client response
     * @param t the exception that occured
     */
    private void errorHandling(CmsObject cms, HttpServletRequest req, HttpServletResponse res, Throwable t) {

        boolean canWrite = !res.isCommitted() && !res.containsHeader("Location");
        int status = -1;
        boolean isNotGuest = false;

        if (t instanceof ServletException) {
            ServletException s = (ServletException)t;
            if (s.getRootCause() != null) {
                t = s.getRootCause();
            }
        }

        if (t instanceof CmsSecurityException) {
            CmsSecurityException e = (CmsSecurityException)t;

            // access error - display login dialog
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("[OpenCms] Access denied: " + e.getMessage());
            }
            if (canWrite) {
                try {
                    requestAuthorization(req, res);
                } catch (IOException ioe) {
                    // there is nothing we can do about this
                }
                return;
            }
        } else if (t instanceof CmsException) {
            CmsException e = (CmsException)t;

            int exceptionType = e.getType();
            switch (exceptionType) {

                case CmsException.C_NOT_FOUND:
                    // file not found - display 404 error.
                    status = HttpServletResponse.SC_NOT_FOUND;
                    break;

                case CmsException.C_SERVICE_UNAVAILABLE:
                    status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                    break;

                case CmsException.C_NO_USER:
                case CmsException.C_NO_GROUP:
                    status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                    isNotGuest = true;
                    break;

                case CmsException.C_HTTPS_PAGE_ERROR:
                    // http page and https request - display 404 error.
                    status = HttpServletResponse.SC_NOT_FOUND;
                    if (OpenCms.getLog(this).isInfoEnabled()) {
                        OpenCms.getLog(this).info("Trying to get a http page with a https request", e);
                    }
                    break;

                case CmsException.C_HTTPS_REQUEST_ERROR:
                    // https request and http page - display 404 error.
                    status = HttpServletResponse.SC_NOT_FOUND;
                    if (OpenCms.getLog(this).isInfoEnabled()) {
                        OpenCms.getLog(this).info("Trying to get a https page with a http request", e);
                    }
                    break;

                default:
                    // other CmsException
                    break;
            }

            if (e.getRootCause() != null) {
                t = e.getRootCause();
            }
        }

        if (status > 0) {
            res.setStatus(status);
        }

        try {
            isNotGuest = isNotGuest 
                || (cms != null 
                    && cms.getRequestContext().currentUser() != null 
                    && (! OpenCms.getDefaultUsers().getUserGuest().equals(cms.getRequestContext().currentUser().getName())) 
                    && ((cms.userInGroup(cms.getRequestContext().currentUser().getName(), OpenCms.getDefaultUsers().getGroupUsers())) 
                        || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), OpenCms.getDefaultUsers().getGroupProjectmanagers())) 
                        || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), OpenCms.getDefaultUsers().getGroupAdministrators()))));
        } catch (CmsException e) {
            // result is false
        }

        if (canWrite) {
            res.setContentType("text/HTML");
            res.setHeader(I_CmsConstants.C_HEADER_CACHE_CONTROL, I_CmsConstants.C_HEADER_VALUE_NO_CACHE);
            res.setHeader(I_CmsConstants.C_HEADER_PRAGMA, I_CmsConstants.C_HEADER_VALUE_NO_CACHE);
            if (isNotGuest && cms != null) {
                try {
                    res.getWriter().print(createErrorBox(t, req, cms));
                } catch (IOException e) {
                    // can be ignored
                }
            } else {
                if (status < 1) {
                    status = HttpServletResponse.SC_NOT_FOUND;
                }
                try {
                    res.sendError(status, t.toString());
                } catch (IOException e) {
                    // can be ignored
                }
            }
        }
    }

    /**
     * Fires the specified event to a list of event listeners.<p>
     * 
     * @param listeners the listeners to fire
     * @param event the event to fire
     */
    private void fireCmsEventHandler(List listeners, CmsEvent event) {

        if ((listeners != null) && (listeners.size() > 0)) {
            // handle all event listeners that listen only to this event type
            I_CmsEventListener list[] = new I_CmsEventListener[0];
            synchronized (listeners) {
                list = (I_CmsEventListener[])listeners.toArray(list);
            }
            for (int i = 0; i < list.length; i++) {
                list[i].cmsEvent(event);
            }
        }
    }
    
    /**
     * Initializes a CmsObject with the given context information.<p>
     * 
     * @param contextInfo the information for the CmsObject context to create
     * @param sessionStorage the session storage for this OpenCms instance
     * 
     * @return the initialized CmsObject
     * 
     * @throws CmsException if something goes wrong
     */    
    private CmsObject initCmsObject(
        CmsContextInfo contextInfo, 
        CmsSessionInfoManager sessionStorage
    ) throws CmsException {
        
        CmsUser user = contextInfo.getUser();        
        if (user == null) {
            user = m_securityManager.readUser(contextInfo.getUserName());
        }
                          
        CmsProject project = contextInfo.getProject();
        if (project == null) {
            project = m_securityManager.readProject(contextInfo.getProjectName());
        }
        
        // first create the request context
        CmsRequestContext context = 
            new CmsRequestContext(
                user,
                project,
                contextInfo.getRequestedUri(), 
                contextInfo.getSiteRoot(), 
                contextInfo.getLocale(), 
                contextInfo.getEncoding(), 
                contextInfo.getRemoteAddr(),
                m_resourceManager.getFolderTranslator(),
                m_resourceManager.getFileTranslator());

        // now initialize and return the CmsObject
        CmsObject cms = new CmsObject(m_securityManager, context, sessionStorage);
        return cms;
    }

    /**
     * This method handled the user authentification for each request sent to the
     * OpenCms. <p>
     *
     * User authentification is done in three steps:
     * <ul>
     * <li> Session Authentification: OpenCms stores all active sessions of authentificated
     * users in an internal storage. During the session authetification phase, it is checked
     * if the session of the active user is stored there. </li>
     * <li> HTTP Autheification: If session authentification fails, it is checked if the current
     * user has loged in using HTTP authentification. If this check is positive, the user account is
     * checked. </li>
     * <li> Default user: When both authentification methods fail, the current user is
     * set to the default (guest) user. </li>
     * </ul>
     *
     * @param req the current http request
     * @param res the current http response
     * @return the initialized cms context
     * @throws IOException if user authentication fails
     * @throws CmsException in case something goes wrong
     */
    private CmsObject initCmsObject(
        HttpServletRequest req, 
        HttpServletResponse res
    ) throws IOException, CmsException {
        
        CmsObject cms;

        // try to get the current session
        HttpSession session = req.getSession(false);
        String sessionId;
        
        // check if there is user data already stored in the session manager
        String user = null;
        if (session != null) {
            // session exists, try to reuse the user from the session
            sessionId = session.getId();
        } else {
            // special case for acessing a session from "outside" requests (e.g. upload applet)
            sessionId = req.getParameter("JSESSIONID");
        }
        if (sessionId != null) {
            user = m_sessionInfoManager.getUserName(sessionId);
        }        

        // initialize the requested site root
        CmsSite site = getSiteManager().matchRequest(req);

        if (user != null) {
            // a user name is found in the session manager, reuse this user information
            Integer project = m_sessionInfoManager.getCurrentProject(sessionId);

            // initialize site root from request
            String siteroot = null;
            // a dedicated workplace site is configured
            if ((getSiteManager().getWorkplaceSiteMatcher().equals(site.getSiteMatcher()))) {
                // if no dedicated workplace site is configured, 
                // or for the dedicated workplace site, use the site root from the session attribute
                siteroot = m_sessionInfoManager.getCurrentSite(sessionId);
            }
            if (siteroot == null) {
                siteroot = site.getSiteRoot();
            }
            cms = initCmsObject(req, user, siteroot, project.intValue(), m_sessionInfoManager);
        } else {
            // no user name found in session or no session, login the user as guest user
            cms = initCmsObject(req, OpenCms.getDefaultUsers().getUserGuest(), site.getSiteRoot(), I_CmsConstants.C_PROJECT_ONLINE_ID, null);
            if (m_useBasicAuthentication) {
                // check if basic authorization data was provided
                checkBasicAuthorization(cms, req, res);
            }
        }

        // return the initialized cms user context object
        return cms;
    }

    /**
     * Returns an initialized CmsObject with the given users permissions.<p>
     * 
     * In case the password is <code>null</code>, or the user is the <code>Guest</code> user,
     * no password check is done and the <code>Guest</code> user is initialized.<p>
     * 
     * @param req the current request
     * @param res the current response
     * @param user the user to initialize the CmsObject with
     * @param password the password of the user 
     * @return a cms context that has been initialized with "Guest" permissions
     * @throws CmsException in case the CmsObject could not be initialized
     */
    private CmsObject initCmsObject(
        HttpServletRequest req, 
        HttpServletResponse res,
        String user,
        String password
    ) throws CmsException {
        
        String siteroot = null;
        // gather information from request / response if provided
        if ((req != null) && (res != null)) {
            siteroot = OpenCms.getSiteManager().matchRequest(req).getSiteRoot();
        }
        // initialize the user        
        if (user == null) {
            user = getDefaultUsers().getUserGuest();
        }
        if (siteroot == null) {
            siteroot = "/";
        }
        CmsObject cms = initCmsObject(req, user, siteroot, I_CmsConstants.C_PROJECT_ONLINE_ID, null);
        // login the user if different from Guest
        if ((password != null) && !getDefaultUsers().getUserGuest().equals(user)) {
            cms.loginUser(user, password, I_CmsConstants.C_IP_LOCALHOST);            
        }
        return cms;
    }
    
    /**
     * Inits a CmsObject with the given users information.<p>
     * 
     * @param req the current http request (or null)
     * @param userName the name of the user to init
     * @param currentSite the users current site 
     * @param projectId the id of the users current project
     * @param sessionStorage the session storage for this OpenCms instance
     * @return the initialized CmsObject
     * @throws CmsException in case something goes wrong
     */
    private CmsObject initCmsObject(
        HttpServletRequest req, 
        String userName,
        String currentSite, 
        int projectId, 
        CmsSessionInfoManager sessionStorage
    ) throws CmsException {
        
        CmsUser user = m_securityManager.readUser(userName);
        CmsProject project = m_securityManager.readProject(projectId);
        
        // get requested resource uri
        String requestedResource = null;        
        if (req != null) {
            requestedResource = req.getPathInfo();
        }
        if (requestedResource == null) {
            // path info can be null, so no 'else'
            requestedResource = "/";
        }               
        
        // get remote IP address
        String remoteAddr;
        if (req != null) {
            remoteAddr = req.getHeader(I_CmsConstants.C_HEADER_X_FORWARDED_FOR);
            if (remoteAddr == null) {
                remoteAddr = req.getRemoteAddr();
            }
        } else {
            remoteAddr = I_CmsConstants.C_IP_LOCALHOST;
        }
        
        // get locale and encoding        
        CmsI18nInfo i18nInfo;
        if (m_localeManager.isInitialized()) {
            // locale manager is initialized
            // resolve locale and encoding
            String resourceName;
            if (requestedResource.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM)) {
                // add site root only if resource name does not start with "/system"
                resourceName = requestedResource;
            } else {
                resourceName = currentSite.concat(requestedResource);
            }  
            i18nInfo = m_localeManager.getI18nInfo(req, user, project, resourceName);
        } else {
            // locale manager not initialized, this will be true _only_ during system startup
            // the values set does not matter, no locale information form VFS is used on system startup
            // this is just to protect against null pointer exceptions
            i18nInfo = new CmsI18nInfo(Locale.ENGLISH, getSystemInfo().getDefaultEncoding());
        }
        
        // decode the requested resource, always using UTF-8
        requestedResource = CmsEncoder.decode(requestedResource);
        
        // initialize the context info
        CmsContextInfo contextInfo = new CmsContextInfo(
            user, 
            project,
            requestedResource, 
            currentSite,
            i18nInfo.getLocale(),
            i18nInfo.getEncoding(),
            remoteAddr);               

        // now generate and return the CmsObject
        return initCmsObject(contextInfo, sessionStorage);        
    }

    /**
     * This method sends a request to the client to display a login form,
     * it is needed for HTTP-Authentification.<p>
     *
     * @param req the client request
     * @param res the response
     * @throws IOException if something goes wrong
     */
    private void requestAuthorization(HttpServletRequest req, HttpServletResponse res) throws IOException {
        
        String servletPath = null;
        String redirectURL = null;
        
        if (m_useBasicAuthentication) {
            // HTTP basic authentication is used
            res.setHeader(I_CmsConstants.C_HEADER_WWW_AUTHENTICATE, "BASIC realm=\"" + getSystemInfo().getOpenCmsContext() + "\"");
            res.setStatus(401);
        } else {
            // form based authentication is used, redirect the user to
            // a page with a form to enter his username and password
            servletPath = req.getContextPath() + req.getServletPath();
            
            try {
                // get a Admin cms context object
                CmsObject adminCms = initCmsObject(req, res, getDefaultUsers().getUserAdmin(), null);
                CmsProperty propertyLoginForm = adminCms.readPropertyObject(I_CmsConstants.C_PROPERTY_LOGIN_FORM, req.getPathInfo(), true);

                if (org.opencms.main.OpenCms.getLog(this).isDebugEnabled()) {
                    org.opencms.main.OpenCms.getLog(this).debug("resource: " + req.getPathInfo());
                    org.opencms.main.OpenCms.getLog(this).debug("property: " + propertyLoginForm);
                }                
                             
                if (propertyLoginForm != CmsProperty.getNullProperty() && propertyLoginForm.getValue() != null && !"".equals(propertyLoginForm.getValue())) {
                    // build a redirect URL using the value of the property
                    // "__loginform" is a dummy request parameter that could be used in a JSP template to trigger
                    // if the template should display a login formular or not                       
                    redirectURL = servletPath + propertyLoginForm.getValue() + "?__loginform=true&requestedResource=" + req.getPathInfo();
                }
            } catch (CmsException e) {
                if (org.opencms.main.OpenCms.getLog(this).isErrorEnabled()) {
                    org.opencms.main.OpenCms.getLog(this).error("Error reading property \"" + I_CmsConstants.C_PROPERTY_LOGIN_FORM + "\"", e);
                }                
            } finally {
                if (redirectURL == null) {
                    // login-form property value not set- build a redirect URL to the 
                    // authentication redirect page configured in opencms.properties
                    redirectURL = servletPath + m_authenticationFormURI + "?requestedResource=" + req.getPathInfo();
                }
            }
            
            if (org.opencms.main.OpenCms.getLog(this).isDebugEnabled()) {
                org.opencms.main.OpenCms.getLog(this).debug("Redirecting response for authentication to: " + redirectURL);
            }            
            
            res.sendRedirect(redirectURL);
        }
    }

    /**       
     * Sets the init level of this OpenCmsCore object instance.<p>
     * 
     * @param level the level to set
     */
    private void setRunLevel(int level) {

        if (getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            getLog(CmsLog.CHANNEL_INIT).info("OpenCms: Changing runlevel from " + m_runLevel + " to " + level);
        }
        m_runLevel = level;
    }

    /**
     * Throws an exception that is also logged and written to the error output console.<p>
     * 
     * @param cause the original Exception
     * @throws CmsInitException the <code>cause</code> parameter
     */
    private void throwInitException(CmsInitException cause) throws CmsInitException {

        String message = cause.getMessage();
        if (message == null) {
            message = cause.toString();
        }
        System.err.println("\n--------------------\nCritical error during OpenCms context init phase:\n" + message);
        System.err.println("Giving up, unable to start OpenCms.\n--------------------");
        if (getLog(this).isFatalEnabled()) {
            getLog(this).fatal("Unable to start OpenCms", cause);
        }
        throw cause;
    }

    /**
     * Updates the the user data stored in the CmsSessionInfoManager after the requested document
     * is processed.<p>
     *
     * This is required if the user data (current group or project) was changed in
     * the requested document.<p>
     *
     * The user data is only updated if the user was authenticated to the system.
     *
     * @param cms the current CmsObject initialized with the user data
     * @param req the current request
     */
    private void updateUser(CmsObject cms, HttpServletRequest req) {
        
        if (!cms.getRequestContext().isUpdateSessionEnabled()) {
            return;
        }
        // get the session if it is there
        HttpSession session = req.getSession(false);

        // if the user was authenticated via sessions, update the information in the
        // session stroage
        if (session != null) {
            if (!cms.getRequestContext().currentUser().getName().equals(OpenCms.getDefaultUsers().getUserGuest())) {

                CmsSessionInfo sessionInfo = new CmsSessionInfo();

                // set necessary values in the CmsSessionInfo object
                sessionInfo.setUserName(cms.getRequestContext().currentUser().getName());
                sessionInfo.setUserId(cms.getRequestContext().currentUser().getId());
                sessionInfo.setProject(new Integer(cms.getRequestContext().currentProject().getId()));
                sessionInfo.setCurrentSite(cms.getRequestContext().getSiteRoot());
                sessionInfo.setSession(session);
                // update the session info user data
                m_sessionInfoManager.putUser(session.getId(), sessionInfo);

                // ensure that the session notify is set
                // this is required to remove the session from the internal storage on its destruction
                CmsSessionBindingListener notify = null;
                Object sessionValue = session.getAttribute("NOTIFY");
                if (sessionValue instanceof CmsSessionBindingListener) {
                    notify = (CmsSessionBindingListener)sessionValue;
                }
                if (notify == null) {
                    notify = new CmsSessionBindingListener(session.getId());
                    session.setAttribute("NOTIFY", notify);
                }
            }
        }
    }
    
}
