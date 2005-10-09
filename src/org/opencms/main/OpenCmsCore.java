/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/OpenCmsCore.java,v $
 * Date   : $Date: 2005/10/09 07:15:20 $
 * Version: $Revision: 1.216.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsLoginManager;
import org.opencms.db.CmsSecurityManager;
import org.opencms.db.CmsSqlManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.flex.CmsFlexCache;
import org.opencms.flex.CmsFlexCacheConfiguration;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsI18nInfo;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsImportExportManager;
import org.opencms.jsp.util.CmsErrorBean;
import org.opencms.loader.CmsResourceManager;
import org.opencms.loader.I_CmsFlexCacheEnabledLoader;
import org.opencms.lock.CmsLockManager;
import org.opencms.module.CmsModuleManager;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.monitor.CmsMemoryMonitorConfiguration;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.search.CmsSearchManager;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPropertyUtils;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.xml.CmsXmlContentTypeManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;

/**
 * The internal implementation of the core OpenCms "operating system" functions.<p>
 * 
 * All access to this class must be done through the public static methods
 * of the <code>{@link org.opencms.main.OpenCms}</code> object.
 * Under no circumstances should you ever try to access this class directly.<p>
 * 
 * This class is so OpenCms internal you should not even be reading this documentation ;-)<p>
 * 
 * Any request to the <code>{@link org.opencms.main.OpenCmsServlet}</code> will be forwarded to this core class.
 * The core will then try to map the request to a VFS (Virtual File System) URI,
 * that is a <code>{@link org.opencms.file.CmsResource}</code> in the OpenCms database. 
 * If a resource is found, it will be read and forwarded to
 * to the corresponding <code>{@link org.opencms.loader.I_CmsResourceLoader}</code>, 
 * which will then generate the output for the requested resource and return it to the requesting client.<p>
 *
 * There will be only one singleton instance of this object created for
 * this core class. This means that in the default configuration, where 
 * OpenCms is accessed through a servlet context, there will be only one instance of 
 * the core in that servlet context.<p>
 * 
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.216.2.4 $ 
 * 
 * @since 6.0.0 
 */
public final class OpenCmsCore {

    /** Required as template for event list generation. */
    private static final I_CmsEventListener[] EVENT_LIST = new I_CmsEventListener[0];

    /** Lock object for synchronization. */
    private static final Object LOCK = new Object();

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(OpenCmsCore.class);

    /** Indicates if the configuration was sucessfully finished or not. */
    private static CmsMessageContainer m_errorCondition;

    /** One instance to rule them all, one instance to find them... */
    private static OpenCmsCore m_instance;

    /** The configuration manager that contains the information from the XML configuration. */
    private CmsConfigurationManager m_configurationManager;

    /** List of configured directory default file names. */
    private List m_defaultFiles;

    /** The default user and group names. */
    private CmsDefaultUsers m_defaultUsers;

    /** Stores the active event listeners. */
    private Map m_eventListeners;

    /** The set of configured export points. */
    private Set m_exportPoints;

    /** The site manager contains information about the Cms import/export. */
    private CmsImportExportManager m_importExportManager;

    /** The link manager to resolve links in &lt;cms:link&gt; tags. */
    private CmsLinkManager m_linkManager;

    /** The locale manager used for obtaining the current locale. */
    private CmsLocaleManager m_localeManager;

    /** The lock manager used for the locking mechanism. */
    private CmsLockManager m_lockManager;

    /** The login manager. */
    private CmsLoginManager m_loginManager;

    /** The memory monitor for the collection of memory and runtime statistics. */
    private CmsMemoryMonitor m_memoryMonitor;

    /** The module manager. */
    private CmsModuleManager m_moduleManager;

    /** The password handler used to digest and validate passwords. */
    private I_CmsPasswordHandler m_passwordHandler;

    /** The configured request handlers that handle "special" requests, for example in the static export on demand. */
    private Map m_requestHandlers;

    /** Stores the resource init handlers that allow modification of the requested resource. */
    private List m_resourceInitHandlers;

    /** The resource manager. */
    private CmsResourceManager m_resourceManager;

    /** The runlevel of this OpenCmsCore object instance. */
    private int m_runLevel;

    /** The runtime properties allow storage of system wide accessible runtime information. */
    private Map m_runtimeProperties;

    /** The configured scheduler manager. */
    private CmsScheduleManager m_scheduleManager;

    /** The search manager provides indexing and searching. */
    private CmsSearchManager m_searchManager;

    /** The security manager to access the database and validate user permissions. */
    private CmsSecurityManager m_securityManager;

    /** The session manager. */
    private CmsSessionManager m_sessionManager;

    /** The site manager contains information about all configured sites. */
    private CmsSiteManager m_siteManager;

    /** The static export manager. */
    private CmsStaticExportManager m_staticExportManager;

    /** The system information container for "read only" system settings. */
    private CmsSystemInfo m_systemInfo;

    /** The thread store. */
    private CmsThreadStore m_threadStore;

    /** The workplace manager contains information about the global workplace settings. */
    private CmsWorkplaceManager m_workplaceManager;

    /** The XML content type manager that contains the initialized XML content types. */
    private CmsXmlContentTypeManager m_xmlContentTypeManager;

    /**
     * Protected constructor that will initialize the singleton OpenCms instance 
     * with runlevel {@link OpenCms#RUNLEVEL_1_CORE_OBJECT}.<p>
     * 
     * @throws CmsInitException in case of errors during the initialization
     */
    private OpenCmsCore()
    throws CmsInitException {

        synchronized (LOCK) {
            if (m_instance != null && (m_instance.getRunLevel() > OpenCms.RUNLEVEL_0_OFFLINE)) {
                throw new CmsInitException(Messages.get().container(Messages.ERR_ALREADY_INITIALIZED_0));
            }
            initMembers();
            m_instance = this;
            setRunLevel(OpenCms.RUNLEVEL_1_CORE_OBJECT);
        }
    }

    /**
     * Returns the initialized OpenCms singleton instance.<p>
     * 
     * @return the initialized OpenCms singleton instance
     */
    protected static OpenCmsCore getInstance() {

        if (m_errorCondition != null) {
            // OpenCms is not properly initialized
            throw new CmsInitException(m_errorCondition, false);
        }
        if (m_instance == null) {
            try {
                // create a new core object with runlevel 1
                new OpenCmsCore();
            } catch (CmsInitException e) {
                // already initialized, this is all we need
            }
        }
        return m_instance;
    }

    /**
     * Sets the error condition.<p>
     *
     * @param errorCondition the error condition to set
     */
    protected static void setErrorCondition(CmsMessageContainer errorCondition) {

        // init exceptions should only be thrown during setup process
        if ((m_instance != null) && (m_instance.getRunLevel() < OpenCms.RUNLEVEL_3_SHELL_ACCESS)) {
            if (!Messages.ERR_CRITICAL_INIT_WIZARD_0.equals(errorCondition.getKey())) {
                // if wizard is still enabled allow retry of initialization (for setup wizard)
                m_errorCondition = errorCondition;
                // output an error message to the console
                System.err.println(Messages.get().key(Messages.LOG_INIT_FAILURE_MESSAGE_1, errorCondition.key()));
            }
            LOG.error(errorCondition.key());
            m_instance = null;
        } else if (m_instance != null) {
            // OpenCms already was successfull initialized
            LOG.warn(Messages.get().key(
                Messages.LOG_INIT_INVALID_ERROR_2,
                new Integer(m_instance.getRunLevel()),
                errorCondition.key()));
        }
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
                CmsLog.INIT.error(Messages.get().key(Messages.LOG_DUPLICATE_REQUEST_HANDLER_1, name));
                continue;
            }
            m_requestHandlers.put(name, handler);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(
                    Messages.INIT_ADDED_REQUEST_HANDLER_2,
                    name,
                    handler.getClass().getName()));
            }
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
     * Returns the configured export points,
     * the returned set being an unmodifiable set.<p>
     * 
     * @return an unmodifiable set of the configured export points
     */
    protected Set getExportPoints() {

        return m_exportPoints;
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
     * Returns the login manager used to check the validity of a login.<p>
     * 
     * @return the login manager
     */
    protected CmsLoginManager getLoginManager() {

        return m_loginManager;
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
     * For a detailed description about the possible run levels, 
     * please see {@link OpenCms#getRunLevel()}.<p>
     * 
     * @return the runlevel of this OpenCmsCore object instance
     * 
     * @see OpenCms#getRunLevel()
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

        return m_runtimeProperties.get(key);
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
     * Returns the initialized OpenCms configuration manager.<p>
     * 
     * @return the initialized OpenCms configuration manager
     */
    protected CmsConfigurationManager getConfigurationManager() {

        return m_configurationManager;
    }

    /**
     * Returns the session manager.<p>
     * 
     * @return the session manager
     */
    protected CmsSessionManager getSessionManager() {

        return m_sessionManager;
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
     * Returns an instance of the common sql manager.<p>
     * 
     * @return an instance of the common sql manager
     */
    protected CmsSqlManager getSqlManager() {

        return m_securityManager.getSqlManager();
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
        if (getRunLevel() == OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // this is only to enable test cases to run 
            m_xmlContentTypeManager = CmsXmlContentTypeManager.createTypeManagerForTestCases();
        }
        return m_xmlContentTypeManager;
    }

    /**
     * Returns an independent copy of the provided CmsObject.<p>
     * 
     * This can be useful in case a permanent reference to a CmsObject is stored.
     * Changing the request context values (for example project, siteroot) in the new CmsObject 
     * will have no side effects to the CmsObject it was copied form.<p>  
     * 
     * @param cms the CmsObject to create a copy of
     * 
     * @return an independent copy of the provided CmsObject
     * 
     * @throws CmsException in case the intialization failed
     * 
     * @see OpenCms#initCmsObject(CmsObject)
     * @see OpenCms#initCmsObject(CmsObject, CmsContextInfo)
     * @see OpenCms#initCmsObject(String)
     */
    protected CmsObject initCmsObject(CmsObject cms) throws CmsException {

        CmsContextInfo contextInfo = new CmsContextInfo(cms.getRequestContext());
        return initCmsObject(contextInfo);
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
     * @throws CmsException if an invalid user name was provided
     * @throws CmsRoleViolationException if the current user does not have the role permissions to create a context for the requested user
     * 
     * @see org.opencms.db.CmsDefaultUsers#getUserGuest()
     * @see org.opencms.db.CmsDefaultUsers#getUserExport()
     * @see OpenCms#initCmsObject(CmsObject)
     * @see OpenCms#initCmsObject(CmsObject, CmsContextInfo)
     * @see OpenCms#initCmsObject(String)
     */
    protected CmsObject initCmsObject(CmsObject adminCms, CmsContextInfo contextInfo)
    throws CmsRoleViolationException, CmsException {

        String userName = contextInfo.getUserName();

        if (adminCms == null || !adminCms.hasRole(CmsRole.ADMINISTRATOR)) {
            if (!userName.equals(getDefaultUsers().getUserGuest())
                && !userName.equals(getDefaultUsers().getUserExport())) {

                // if no admin object is provided, only "Guest" or "Export" user can be generated
                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_INVALID_INIT_USER_2,
                    userName,
                    ((adminCms != null) ? (adminCms.getRequestContext().currentUser().getName()) : ""));
                if (LOG.isWarnEnabled()) {
                    LOG.warn(message.key());
                }
                throw new CmsRoleViolationException(message);
            }
        }

        return initCmsObject(contextInfo);
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
     * @throws CmsInitException in case OpenCms can not be initialized
     */
    protected synchronized void initConfiguration(ExtendedProperties configuration) throws CmsInitException {

        String systemEncoding = null;
        try {
            systemEncoding = System.getProperty("file.encoding");
        } catch (SecurityException se) {
            // security manager is active, but we will try other options before giving up
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_FILE_ENCODING_1, systemEncoding));
        }

        // read server ethernet address (MAC) and init UUID generator
        String ethernetAddress = configuration.getString("server.ethernet.address", CmsUUID.getDummyEthernetAddress());
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_ETHERNET_ADDRESS_1, ethernetAddress));
        }
        CmsUUID.init(ethernetAddress);

        // set the server name
        String serverName = configuration.getString("server.name", "OpenCmsServer");
        getSystemInfo().setServerName(serverName);

        // initialize the lock manager
        m_lockManager = CmsLockManager.getInstance();

        // check the installed Java SDK
        try {
            if (CmsLog.INIT.isInfoEnabled()) {
                String jdkinfo = System.getProperty("java.vm.name") + " ";
                jdkinfo += System.getProperty("java.vm.version") + " ";
                jdkinfo += System.getProperty("java.vm.info") + " ";
                jdkinfo += System.getProperty("java.vm.vendor") + " ";
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_JAVA_VM_1, jdkinfo));
                String osinfo = System.getProperty("os.name") + " ";
                osinfo += System.getProperty("os.version") + " ";
                osinfo += System.getProperty("os.arch") + " ";
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_OPERATING_SYSTEM_1, osinfo));
            }
        } catch (Exception e) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_PROP_0), e);
        }

        // initialize the memory monitor
        m_memoryMonitor = new CmsMemoryMonitor();

        // create the configuration manager instance    
        m_configurationManager = new CmsConfigurationManager(getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "config/"));
        // store the configuration read from "opencms.properties" in the configuration manager 
        m_configurationManager.setConfiguration(configuration);

        // now load the XML configuration
        try {
            m_configurationManager.loadXmlConfiguration();
        } catch (Exception e) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_XML_0), e);
        }

        // get the system configuration
        CmsSystemConfiguration systemConfiguration = (CmsSystemConfiguration)m_configurationManager.getConfiguration(CmsSystemConfiguration.class);

        // check if the encoding setting is valid
        String setEncoding = systemConfiguration.getDefaultContentEncoding();
        String defaultEncoding = CmsEncoder.lookupEncoding(setEncoding, null);
        if (defaultEncoding == null) {
            // we can not start without a valid encoding setting
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_ENCODING_1, setEncoding));
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_OPENCMS_ENCODING_1, defaultEncoding));
        }
        getSystemInfo().setDefaultEncoding(defaultEncoding);

        // initialize the memory monitor
        CmsMemoryMonitorConfiguration memoryMonitorConfiguration = systemConfiguration.getCmsMemoryMonitorConfiguration();
        m_memoryMonitor.initialize(memoryMonitorConfiguration);

        // set version history information        
        getSystemInfo().setVersionHistorySettings(
            systemConfiguration.isVersionHistoryEnabled(),
            systemConfiguration.getVersionHistoryMaxCount());
        // set mail configuration
        getSystemInfo().setMailSettings(systemConfiguration.getMailSettings());
        // set HTTP authentication settings
        getSystemInfo().setHttpAuthenticationSettings(systemConfiguration.getHttpAuthenticationSettings());
        
        // set content notification settings
        getSystemInfo().setNotificationTime(systemConfiguration.getNotificationTime());
        getSystemInfo().setNotificationProject(systemConfiguration.getNotificationProject());
        // set the scheduler manager
        m_scheduleManager = systemConfiguration.getScheduleManager();
        // set resource init classes
        m_resourceInitHandlers = systemConfiguration.getResourceInitHandlers();
        // register request handler classes
        Iterator it = systemConfiguration.getRequestHandlers().iterator();
        while (it.hasNext()) {
            I_CmsRequestHandler handler = (I_CmsRequestHandler)it.next();
            addRequestHandler(handler);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.warn(Messages.get().key(Messages.INIT_REQUEST_HANDLER_CLASS_1, handler.getClass().getName()));
            }
        }

        // read the default user configuration
        m_defaultUsers = systemConfiguration.getCmsDefaultUsers();

        try {
            // initialize the group names for the system roles 
            CmsRole.initialize(m_defaultUsers);
        } catch (CmsSecurityException e) {
            // this should never happen
            throw new CmsInitException(
                Messages.get().container(Messages.ERR_CRITICAL_INIT_GENERIC_1, e.getMessage()),
                e);
        }

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
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_FLEX_CACHE_STARTING_0));
            }
            // get the flex cache configuration from the SystemConfiguration
            CmsFlexCacheConfiguration flexCacheConfiguration = systemConfiguration.getCmsFlexCacheConfiguration();
            // pass configuration to flex cache for initialization
            flexCache = new CmsFlexCache(flexCacheConfiguration);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_FLEX_CACHE_FINISHED_0));
            }
        } catch (Exception e) {
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(Messages.get().key(Messages.INIT_FLEX_CACHE_ERROR_1, e.getMessage()));
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

        // get the login manager
        m_loginManager = systemConfiguration.getLoginManager();

        // init the OpenCms security manager
        m_securityManager = CmsSecurityManager.newInstance(
            m_configurationManager,
            systemConfiguration.getRuntimeInfoFactory());

        // initialize the Thread store
        m_threadStore = new CmsThreadStore();

        // initialize the link manager
        m_linkManager = new CmsLinkManager();

        // store the runtime properties
        m_runtimeProperties.putAll(systemConfiguration.getRuntimeProperties());

        // get an Admin cms context object with site root set to "/"
        CmsObject adminCms;
        try {
            adminCms = initCmsObject(null, null, getDefaultUsers().getUserAdmin(), null);
        } catch (CmsException e) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_ADMINCMS_0), e);
        }

        // now initialize the managers
        try {
            // initialize the scheduler
            m_scheduleManager.initialize(initCmsObject(adminCms));

            // initialize the locale manager
            m_localeManager = systemConfiguration.getLocaleManager();
            m_localeManager.initialize(initCmsObject(adminCms));

            // initialize the site manager
            m_siteManager.initialize(initCmsObject(adminCms));

            // initialize the static export manager
            m_staticExportManager.initialize(initCmsObject(adminCms));

            // initialize the XML content type manager
            m_xmlContentTypeManager.initialize(initCmsObject(adminCms));

            // intialize the module manager
            m_moduleManager.initialize(initCmsObject(adminCms), m_configurationManager);

            // initialize the resource manager
            m_resourceManager.initialize(initCmsObject(adminCms));

            // initialize the search manager
            m_searchManager.initialize(initCmsObject(adminCms));
            
            // initialize the workplace manager
            m_workplaceManager.initialize(initCmsObject(adminCms));
        } catch (CmsException e) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_MANAGERS_0), e);
        }
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
     * @throws CmsInitException in case OpenCms can not be initialized
     */
    protected synchronized void initContext(ServletContext context) throws CmsInitException {

        // read the the OpenCms servlet mapping from the servlet context parameters
        String servletMapping = context.getInitParameter(OpenCmsServlet.SERVLET_PARAM_OPEN_CMS_SERVLET);
        if (servletMapping == null) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_SERVLET_0));
        }

        // check for OpenCms home (base) directory path
        String webInfPath = context.getInitParameter(OpenCmsServlet.SERVLET_PARAM_OPEN_CMS_HOME);
        if (CmsStringUtil.isEmpty(webInfPath)) {
            webInfPath = CmsFileUtil.searchWebInfFolder(context.getRealPath("/"));
            if (CmsStringUtil.isEmpty(webInfPath)) {
                throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_FOLDER_0));
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
        String webApplicationContext = context.getInitParameter(OpenCmsServlet.SERVLET_PARAM_WEB_APPLICATION_CONTEXT);

        // now initialize the system info with the path and mapping information
        getSystemInfo().init(webInfPath, servletMapping, webApplicationContext, defaultWebApplication);

        // Collect the configurations 
        ExtendedProperties configuration = null;
        try {
            configuration = CmsPropertyUtils.loadProperties(getSystemInfo().getConfigurationFileRfsPath());
        } catch (Exception e) {
            throw new CmsInitException(Messages.get().container(
                Messages.ERR_CRITICAL_INIT_PROPFILE_1,
                getSystemInfo().getConfigurationFileRfsPath()), e);
        }

        // check if the wizard is enabled, if so stop initialization
        if (configuration.getBoolean("wizard.enabled", true)) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_WIZARD_0));
        }
        // output startup message and copyright to STDERR
        System.err.println(Messages.get().key(
            Messages.LOG_STARTUP_CONSOLE_NOTE_2,
            OpenCms.getSystemInfo().getVersionName(),
            getSystemInfo().getWebApplicationName()));
        for (int i = 0; i < Messages.COPYRIGHT_BY_ALKACON.length; i++) {
            System.err.println(Messages.COPYRIGHT_BY_ALKACON[i]);
        }
        System.err.println();

        // output startup message to logfile
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
            for (int i = 0; i < Messages.COPYRIGHT_BY_ALKACON.length; i++) {
                CmsLog.INIT.info(". " + Messages.COPYRIGHT_BY_ALKACON[i]);
            }
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_LINE_0));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_STARTUP_TIME_1, new Date(System.currentTimeMillis())));
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_OPENCMS_VERSION_1,
                OpenCms.getSystemInfo().getVersionName()));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SERVLET_CONTAINER_1, context.getServerInfo()));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_WEBAPP_NAME_1, getSystemInfo().getWebApplicationName()));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SERVLET_PATH_1, getSystemInfo().getServletPath()));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_OPENCMS_CONTEXT_1, getSystemInfo().getOpenCmsContext()));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_WEBINF_PATH_1, getSystemInfo().getWebInfRfsPath()));
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_PROPERTY_FILE_1,
                getSystemInfo().getConfigurationFileRfsPath()));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_LOG_FILE_1, getSystemInfo().getLogFileRfsPath()));
        }

        // initialize the configuration
        initConfiguration(configuration);
    }

    /**
     * Initialize member variables.<p>
     */
    protected void initMembers() {

        synchronized (LOCK) {
            m_resourceInitHandlers = new ArrayList();
            m_eventListeners = new HashMap();
            m_requestHandlers = new HashMap();
            m_systemInfo = new CmsSystemInfo();
            m_exportPoints = Collections.EMPTY_SET;
            m_defaultUsers = new CmsDefaultUsers();
            m_localeManager = new CmsLocaleManager(Locale.ENGLISH);
            m_sessionManager = new CmsSessionManager();
            m_runtimeProperties = new Hashtable();
        }
    }

    /**
     * Reads the requested resource from the OpenCms VFS,
     * in case a directory name is requested, the default files of the 
     * directory will be looked up and the first match is returned.<p>
     *
     * The resource that is returned is always a <code>{@link org.opencms.file.CmsFile}</code>,
     * even though the content will usually not be loaded in the result. Folders are never returned since
     * the point of this method is really to load the default file if just a folder name is requested.<p>
     *
     * The URI stored in the given OpenCms user context will be changed to the URI of the resource 
     * that was found and returned.<p>
     * 
     * Implementing and configuring an <code>{@link I_CmsResourceInit}</code> handler 
     * allows to customize the process of default resouce selection.<p>
     *
     * @param cms the current users OpenCms context
     * @param resourceName the path of the requested resource in the OpenCms VFS
     * @param req the current http request
     * @param res the current http response
     * @return the requested resource read from the VFS
     * 
     * @throws CmsException in case the requested file does not exist or the user has insufficient access permissions
     * 
     * @see OpenCms#initResource(CmsObject, String, HttpServletRequest, HttpServletResponse)
     */
    protected CmsResource initResource(
        CmsObject cms,
        String resourceName,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException {

        CmsResource resource = null;
        CmsException tmpException = null;

        try {
            // try to read the requested resource
            resource = cms.readResource(resourceName);
            // resource exists, lets check if we have a file or a folder
            if (resource.isFolder()) {
                // the resource is a folder, check if PROPERTY_DEFAULT_FILE is set on folder
                try {
                    String defaultFileName = cms.readPropertyObject(
                        CmsResource.getFolderPath(cms.getSitePath(resource)),
                        CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                        false).getValue();
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
                        String tmpResourceName = CmsResource.getFolderPath(cms.getSitePath(resource))
                            + m_defaultFiles.get(i);
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
            if ((resource.getFlags() & CmsResource.FLAG_INTERNAL) > 0) {
                throw new CmsException(Messages.get().container(
                    Messages.ERR_READ_INTERNAL_RESOURCE_1,
                    cms.getRequestContext().getUri()));
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

        synchronized (LOCK) {
            // add the servlets request handler
            addRequestHandler(servlet);
            // output the final 'startup is finished' message
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(
                    Messages.INIT_SYSTEM_RUNNING_1,
                    CmsStringUtil.formatRuntime(getSystemInfo().getRuntime())));
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_LINE_0));
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
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
     * This method adds an Object to the OpenCms runtime properties.
     * The runtime properties can be used to store Objects that are shared
     * in the whole system.<p>
     *
     * @param key the key to add the Object with
     * @param value the value of the Object to add
     */
    protected void setRuntimeProperty(Object key, Object value) {

        m_runtimeProperties.put(key, value);
    }

    /**
     * Sets the session manager.<p>
     * 
     * @param sessionManager the session manager to set
     */
    protected void setSessionManager(CmsSessionManager sessionManager) {

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
                updateUserSessionData(cms, req);
            }

        } catch (Throwable t) {
            errorHandling(cms, req, res, t);
        }
    }

    /**
     * Destroys this OpenCms instance, called if the servlet (or shell) is shut down.<p> 
     */
    protected void shutDown() {

        synchronized (LOCK) {
            if (getRunLevel() > OpenCms.RUNLEVEL_0_OFFLINE) {

                System.err.println(Messages.get().key(
                    Messages.LOG_SHUTDOWN_CONSOLE_NOTE_2,
                    getSystemInfo().getVersionName(),
                    getSystemInfo().getWebApplicationName()));
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_LINE_0));
                    CmsLog.INIT.info(Messages.get().key(
                        Messages.INIT_SHUTDOWN_START_1,
                        getSystemInfo().getVersionName()));
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_CURRENT_RUNLEVEL_1, new Integer(getRunLevel())));
                    CmsLog.INIT.info(Messages.get().key(
                        Messages.INIT_SHUTDOWN_TIME_1,
                        new Date(System.currentTimeMillis())));
                }

                // take the system offline
                setRunLevel(OpenCms.RUNLEVEL_0_OFFLINE);

                if (LOG.isDebugEnabled()) {
                    // log exception to see which method did call the shutdown
                    LOG.debug(Messages.get().key(Messages.LOG_SHUTDOWN_TRACE_0), new Exception());
                }

                try {
                    if (m_staticExportManager != null) {
                        m_staticExportManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(Messages.get().key(Messages.LOG_ERROR_EXPORT_SHUTDOWN_1, e.getMessage()), e);
                }
                try {
                    if (m_moduleManager != null) {
                        m_moduleManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(Messages.get().key(Messages.LOG_ERROR_MODULE_SHUTDOWN_1, e.getMessage()), e);
                }
                try {
                    if (m_scheduleManager != null) {
                        m_scheduleManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(Messages.get().key(Messages.LOG_ERROR_SCHEDULE_SHUTDOWN_1, e.getMessage()), e);
                }
                try {
                    if (m_resourceManager != null) {
                        m_resourceManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(Messages.get().key(Messages.LOG_ERROR_RESOURCE_SHUTDOWN_1, e.getMessage()), e);
                }
                try {
                    if (m_securityManager != null) {
                        m_securityManager.destroy();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(Messages.get().key(Messages.LOG_ERROR_SECURITY_SHUTDOWN_1, e.getMessage()), e);
                }
                try {
                    if (m_threadStore != null) {
                        m_threadStore.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(Messages.get().key(Messages.LOG_ERROR_THREAD_SHUTDOWN_1, e.getMessage()), e);
                }
                String runtime = CmsStringUtil.formatRuntime(getSystemInfo().getRuntime());
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_OPENCMS_STOPPED_1, runtime));
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_LINE_0));
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_DOT_0));
                }
                System.err.println(Messages.get().key(Messages.LOG_CONSOLE_TOTAL_RUNTIME_1, runtime));

            }
            m_instance = null;
        }
    }

    /**
     * Upgrades to runlevel {@link OpenCms#RUNLEVEL_3_SHELL_ACCESS},
     * this is shell access to the database but no Servlet context.<p>
     * 
     * To upgrade the runlevel, the system must be in runlevel {@link OpenCms#RUNLEVEL_1_CORE_OBJECT},
     * otherwise an exception is thrown.<p>
     * 
     * @param configuration the configuration
     * @throws CmsInitException in case OpenCms can not be initialized
     * @return the initialized OpenCmsCore
     */
    protected OpenCmsCore upgradeRunlevel(ExtendedProperties configuration) throws CmsInitException {

        synchronized (LOCK) {
            if ((m_instance != null) && (getRunLevel() >= OpenCms.RUNLEVEL_2_INITIALIZING)) {
                // instance already in runlevel 3 or 4
                return m_instance;
            }
            if (getRunLevel() != OpenCms.RUNLEVEL_1_CORE_OBJECT) {
                CmsLog.INIT.error(Messages.get().key(
                    Messages.LOG_WRONG_INIT_SEQUENCE_2,
                    new Integer(3),
                    new Integer(getRunLevel())));
                return m_instance;
            }

            // set the runlevel to "initializing OpenCms"
            setRunLevel(OpenCms.RUNLEVEL_2_INITIALIZING);
            // initialize the configuration
            m_instance.initConfiguration(configuration);
            // upgrade the runlevel - OpenCms shell is available
            setRunLevel(OpenCms.RUNLEVEL_3_SHELL_ACCESS);

            return m_instance;
        }
    }

    /**
     * Upgrades to runlevel {@link OpenCms#RUNLEVEL_4_SERVLET_ACCESS},
     * this is the final runlevel with an initialized database and Servlet context.<p>
     * 
     * To upgrade the runlevel, the system must be in runlevel {@link OpenCms#RUNLEVEL_1_CORE_OBJECT},
     * otherwise an exception is thrown.<p>
     * 
     * @param context the current servlet context
     * @throws CmsInitException in case OpenCms can not be initialized
     * @return the initialized OpenCmsCore
     */
    protected OpenCmsCore upgradeRunlevel(ServletContext context) throws CmsInitException {

        synchronized (LOCK) {
            if ((m_instance != null) && (getRunLevel() >= OpenCms.RUNLEVEL_4_SERVLET_ACCESS)) {
                // instance already in runlevel 5 or 6
                return m_instance;
            }
            if (getRunLevel() != OpenCms.RUNLEVEL_1_CORE_OBJECT) {
                CmsLog.INIT.error(Messages.get().key(
                    Messages.LOG_WRONG_INIT_SEQUENCE_2,
                    new Integer(4),
                    new Integer(getRunLevel())));
                return m_instance;
            }

            // set the runlevel to "initializing OpenCms"
            setRunLevel(OpenCms.RUNLEVEL_2_INITIALIZING);
            // initialize the servlet context
            m_instance.initContext(context);
            // initialization successfully finished - OpenCms servlet is online
            // the runlevel will change from 2 directly to 4, this is on purpose
            setRunLevel(OpenCms.RUNLEVEL_4_SERVLET_ACCESS);

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
            CmsLog.getLog(CmsConfigurationManager.class).error(
                Messages.get().key(Messages.LOG_ERROR_WRITING_CONFIG_1, clazz.getName()),
                e);
        } catch (CmsConfigurationException e) {
            CmsLog.getLog(CmsConfigurationManager.class).error(
                Messages.get().key(Messages.LOG_ERROR_WRITING_CONFIG_1, clazz.getName()),
                e);
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
    private void checkBasicAuthorization(CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws IOException {

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
                } catch (CmsException e) {
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
     * @return String containing the HTML code of the error message
     */
    private String createErrorBox(Throwable t, HttpServletRequest request, CmsObject cms) {

        String errorUri = CmsFlexController.getThrowableResourceUri(request);
        if (errorUri == null) {
            errorUri = cms.getRequestContext().getUri();
        }
        // try to get the exception root cause
        Throwable cause = CmsFlexController.getThrowable(request);
        if (cause == null) {
            cause = t;
        }
        CmsErrorBean errorBean = new CmsErrorBean(cms, cause);
        errorBean.setParamAction(errorUri);
        return errorBean.toHtml();
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

        // remove the controller attribute from the request
        CmsFlexController.removeController(req);

        boolean canWrite = !res.isCommitted() && !res.containsHeader("Location");
        int status = -1;
        boolean isNotGuest = false;

        if (t instanceof ServletException) {
            ServletException s = (ServletException)t;
            if (s.getRootCause() != null) {
                t = s.getRootCause();
            }
        } else if (t instanceof CmsSecurityException) {
            // access error - display login dialog
            if (canWrite) {
                try {
                    requestAuthorization(req, res);
                } catch (IOException ioe) {
                    // there is nothing we can do about this
                }
                return;
            }
        } else if (t instanceof CmsDbEntryNotFoundException) {
            // user or group does not exist
            status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            isNotGuest = true;
        } else if (t instanceof CmsVfsResourceNotFoundException) {
            // file not found - display 404 error.
            status = HttpServletResponse.SC_NOT_FOUND;
        } else if (t instanceof CmsException) {
            if (t.getCause() != null) {
                t = t.getCause();
            }
        }

        if (status < 1) {
            // error code not set - set "internal server error" (500)
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        res.setStatus(status);

        try {
            isNotGuest = isNotGuest
                || (cms != null
                    && cms.getRequestContext().currentUser() != null
                    && (!OpenCms.getDefaultUsers().getUserGuest().equals(
                        cms.getRequestContext().currentUser().getName())) && ((cms.userInGroup(
                    cms.getRequestContext().currentUser().getName(),
                    OpenCms.getDefaultUsers().getGroupUsers()))
                    || (cms.userInGroup(
                        cms.getRequestContext().currentUser().getName(),
                        OpenCms.getDefaultUsers().getGroupProjectmanagers())) || (cms.userInGroup(
                    cms.getRequestContext().currentUser().getName(),
                    OpenCms.getDefaultUsers().getGroupAdministrators()))));
        } catch (CmsException e) {
            // result is false
        }

        if (canWrite) {
            res.setContentType("text/html");
            CmsRequestUtil.setNoCacheHeaders(res);
            if (isNotGuest && cms != null && !cms.getRequestContext().currentProject().isOnlineProject()) {
                try {
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.getWriter().print(createErrorBox(t, req, cms));
                } catch (IOException e) {
                    // can be ignored
                }
            } else {
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
            I_CmsEventListener[] list;
            synchronized (listeners) {
                list = (I_CmsEventListener[])listeners.toArray(EVENT_LIST);
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
     * 
     * @return the initialized CmsObject
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsObject initCmsObject(CmsContextInfo contextInfo) throws CmsException {

        CmsUser user = contextInfo.getUser();
        if (user == null) {
            user = m_securityManager.readUser(contextInfo.getUserName());
        }

        CmsProject project = contextInfo.getProject();
        if (project == null) {
            project = m_securityManager.readProject(contextInfo.getProjectName());
        }

        // first create the request context
        CmsRequestContext context = new CmsRequestContext(
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
        CmsObject cms = new CmsObject(m_securityManager, context);
        return cms;
    }

    /**
     * This method handled the user authentification for each request sent to the
     * OpenCms. <p>
     *
     * User authentification is done in three steps:
     * <ol>
     * <li> Session authentification: OpenCms stores information of all authentificated
     * users in an internal storage based on the users session.</li>
     * <li> HTTP authentification: If the session authentification fails, it is checked if the current
     * user is providing data for HTTP BASIC authentification. If this check is positive, the user 
     * is tried to log in with this data, and on success a session is generated.</li>
     * <li> Default user: When both authentification methods fail, the user is
     * set to the default (Guest) user. </li>
     * </ol>
     *
     * @param req the current http request
     * @param res the current http response
     * @return the initialized cms context
     * @throws IOException if user authentication fails
     * @throws CmsException in case something goes wrong
     */
    private CmsObject initCmsObject(HttpServletRequest req, HttpServletResponse res) throws IOException, CmsException {

        CmsObject cms;

        // try to get the current session
        HttpSession session = req.getSession(false);
        String sessionId;

        // check if there is user data already stored in the session manager
        if (session != null) {
            // session exists, try to reuse the user from the session
            sessionId = session.getId();
        } else {
            // special case for acessing a session from "outside" requests (e.g. upload applet)
            sessionId = req.getHeader(CmsRequestUtil.HEADER_JSESSIONID);
        }
        CmsSessionInfo sessionInfo = null;
        if (sessionId != null) {
            sessionInfo = m_sessionManager.getSessionInfo(sessionId);
        }

        // initialize the requested site root
        CmsSite site = getSiteManager().matchRequest(req);

        if (sessionInfo != null) {
            // a user name is found in the session manager, reuse this user information
            int project = sessionInfo.getProject();

            // initialize site root from request
            String siteroot = null;
            // a dedicated workplace site is configured
            if ((getSiteManager().getWorkplaceSiteMatcher().equals(site.getSiteMatcher()))) {
                // if no dedicated workplace site is configured, 
                // or for the dedicated workplace site, use the site root from the session attribute
                siteroot = sessionInfo.getSiteRoot();
            }
            if (siteroot == null) {
                siteroot = site.getSiteRoot();
            }
            cms = initCmsObject(req, sessionInfo.getUser().getName(), siteroot, project);
        } else {
            // no user name found in session or no session, login the user as guest user
            cms = initCmsObject(
                req,
                OpenCms.getDefaultUsers().getUserGuest(),
                site.getSiteRoot(),
                CmsProject.ONLINE_PROJECT_ID);
            // check if "basic" authentification data is provided
            checkBasicAuthorization(cms, req, res);
        }

        // return the initialized cms user context object
        return cms;
    }

    /**
     * Returns an initialized CmsObject with the given users permissions.<p>
     * 
     * In case the password is <code>null</code>, or the user is the <code>Guest</code> user,
     * no password check is done. Therefore you can initialize all users without knowing their passwords 
     * by just supplying <code>null</code> as password. This is intended only for 
     * internal operation in the core.<p>
     * 
     * @param req the current request
     * @param res the current response
     * @param user the user to initialize the CmsObject with
     * @param password the password of the user 
     * @return a cms context that has been initialized with "Guest" permissions
     * @throws CmsException in case the CmsObject could not be initialized
     */
    private CmsObject initCmsObject(HttpServletRequest req, HttpServletResponse res, String user, String password)
    throws CmsException {

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
        CmsObject cms = initCmsObject(req, user, siteroot, CmsProject.ONLINE_PROJECT_ID);
        // login the user if different from Guest and password was provided
        if ((password != null) && !getDefaultUsers().getUserGuest().equals(user)) {
            cms.loginUser(user, password, CmsContextInfo.LOCALHOST);
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
     * @return the initialized CmsObject
     * @throws CmsException in case something goes wrong
     */
    private CmsObject initCmsObject(HttpServletRequest req, String userName, String currentSite, int projectId)
    throws CmsException {

        CmsUser user = m_securityManager.readUser(userName);
        CmsProject project = null;
        try {
            project = m_securityManager.readProject(projectId);
        } catch (CmsDbEntryNotFoundException e) {
            // project not found, switch to online project
            project = m_securityManager.readProject(CmsProject.ONLINE_PROJECT_ID);
        }

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
            remoteAddr = req.getHeader(CmsRequestUtil.HEADER_X_FORWARDED_FOR);
            if (remoteAddr == null) {
                remoteAddr = req.getRemoteAddr();
            }
        } else {
            remoteAddr = CmsContextInfo.LOCALHOST;
        }

        // get locale and encoding        
        CmsI18nInfo i18nInfo;
        if (m_localeManager.isInitialized()) {
            // locale manager is initialized
            // resolve locale and encoding
            String resourceName;
            if (requestedResource.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
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
        return initCmsObject(contextInfo);
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

        // this will create an admin user with the "right" site root already set
        CmsObject adminCms;
        try {
            adminCms = initCmsObject(req, res, getDefaultUsers().getUserAdmin(), null);
        } catch (CmsException e) {
            // this should never happen, if it does we can't continue
            throw new IOException(Messages.get().key(
                Messages.ERR_INVALID_INIT_USER_2,
                getDefaultUsers().getUserAdmin(),
                null));
        }
        // get the requested resource
        String path = adminCms.getRequestContext().getUri();
        CmsProperty propertyLoginForm = null;
        String redirectURL = null;
        try {
            propertyLoginForm = adminCms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_LOGIN_FORM, true);
        } catch (Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(
                    Messages.LOG_ERROR_READING_AUTH_PROP_2,
                    CmsPropertyDefinition.PROPERTY_LOGIN_FORM,
                    path), t);
            }
        }

        CmsHttpAuthenticationSettings httpAuthenticationSettings = getSystemInfo().getHttpAuthenticationSettings();
        String pathWithParams = CmsRequestUtil.encodeParamsWithUri(path, req);
        if (propertyLoginForm != null
            && propertyLoginForm != CmsProperty.getNullProperty()
            && CmsStringUtil.isNotEmpty(propertyLoginForm.getValue())) {
            // login form property value was found            
            // build a redirect URL using the value of the property
            // "__loginform" is a dummy request parameter that could be used in a JSP template to trigger
            // if the template should display a login formular or not  
            redirectURL = propertyLoginForm.getValue()
                + "?__loginform=true&"
                + CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE
                + "="
                + pathWithParams;
        } else if (!httpAuthenticationSettings.useBrowserBasedHttpAuthentication()
            && CmsStringUtil.isNotEmpty(httpAuthenticationSettings.getFormBasedHttpAuthenticationUri())) {
            // login form property value not set, but form login set in configuration
            // build a redirect URL to the default login form URI configured in opencms.properties
            redirectURL = httpAuthenticationSettings.getFormBasedHttpAuthenticationUri()
                + "?"
                + CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE
                + "="
                + pathWithParams;
        }

        if (redirectURL == null) {
            // HTTP basic authentication is used
            res.setHeader(CmsRequestUtil.HEADER_WWW_AUTHENTICATE, "BASIC realm=\""
                + getSystemInfo().getOpenCmsContext()
                + "\"");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            // resolve the login form link using the link manager
            redirectURL = m_linkManager.substituteLink(adminCms, redirectURL);
            if (LOG.isDebugEnabled()) {
                Messages.get().key(Messages.LOG_AUTHENTICATE_PROPERTY_2, redirectURL, path);
            }
            // finally redirect to the login form
            res.sendRedirect(redirectURL);
        }
    }

    /**       
     * Sets the init level of this OpenCmsCore object instance.<p>
     * 
     * For a detailed description about the possible run levels, 
     * please see {@link OpenCms#getRunLevel()}.<p>
     * 
     * @param level the level to set
     */
    private void setRunLevel(int level) {

        if (m_instance != null) {
            if (m_instance.m_runLevel >= OpenCms.RUNLEVEL_1_CORE_OBJECT) {
                // otherwise the log is not available
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().key(
                        Messages.INIT_RUNLEVEL_CHANGE_2,
                        new Integer(m_instance.m_runLevel),
                        new Integer(level)));
                }
            }
            m_instance.m_runLevel = level;
        }
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
    private void updateUserSessionData(CmsObject cms, HttpServletRequest req) {

        if (!cms.getRequestContext().isUpdateSessionEnabled()) {
            // this request must not update the user session info
            // this is true for long running "thread" requests, e.g. during project publish
            return;
        }
        // get the session if it is available
        HttpSession session = req.getSession(false);
        // if the user was authenticated via sessions, 
        // update the information in the session info manager
        if (session != null) {
            if (!cms.getRequestContext().currentUser().isGuestUser()) {
                // get the session info object for the user
                CmsSessionInfo sessionInfo = m_sessionManager.getSessionInfo(session.getId());
                if (sessionInfo != null) {
                    // update the users session information
                    sessionInfo.update(cms.getRequestContext());
                } else {
                    // create a new session info for the user
                    sessionInfo = new CmsSessionInfo(
                        cms.getRequestContext(),
                        session.getId(),
                        session.getMaxInactiveInterval());
                    // update the session info user data
                    m_sessionManager.addSessionInfo(sessionInfo);
                }
            }
        }
    }
}