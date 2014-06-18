/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsImportExportConfiguration;
import org.opencms.configuration.CmsModuleConfiguration;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.db.CmsAliasManager;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsExportPoint;
import org.opencms.db.CmsLoginManager;
import org.opencms.db.CmsSecurityManager;
import org.opencms.db.CmsSqlManager;
import org.opencms.db.CmsSubscriptionManager;
import org.opencms.db.CmsUserSettings;
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
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsGwtServiceContext;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsI18nInfo;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsVfsBundleManager;
import org.opencms.importexport.CmsImportExportManager;
import org.opencms.jsp.util.CmsErrorBean;
import org.opencms.loader.CmsResourceManager;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.loader.I_CmsFlexCacheEnabledLoader;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.lock.CmsLockManager;
import org.opencms.module.CmsModuleManager;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.monitor.CmsMemoryMonitorConfiguration;
import org.opencms.publish.CmsPublishEngine;
import org.opencms.publish.CmsPublishManager;
import org.opencms.repository.CmsRepositoryManager;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.search.CmsSearchManager;
import org.opencms.security.CmsOrgUnitManager;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleManager;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsAuthorizationHandler;
import org.opencms.security.I_CmsCredentialsResolver;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsValidationHandler;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsDefaultWorkflowManager;
import org.opencms.workflow.I_CmsWorkflowManager;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.xml.CmsXmlContentTypeManager;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;

import java.io.IOException;
import java.security.Security;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

import cryptix.jce.provider.CryptixCrypto;

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
 * @since 6.0.0 
 */
public final class OpenCmsCore {

    /** Lock object for synchronization. */
    private static final Object LOCK = new Object();

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(OpenCmsCore.class);

    /** Indicates if the configuration was successfully finished or not. */
    private static CmsMessageContainer m_errorCondition;

    /** One instance to rule them all, one instance to find them... */
    private static OpenCmsCore m_instance;

    /** The ADE manager instance. */
    private CmsADEManager m_adeManager;

    /** The manager for page aliases. */
    private CmsAliasManager m_aliasManager;

    /** The configured authorization handler. */
    private I_CmsAuthorizationHandler m_authorizationHandler;

    /** The configuration manager that contains the information from the XML configuration. */
    private CmsConfigurationManager m_configurationManager;

    /** The object used for resolving database user credentials. */
    private I_CmsCredentialsResolver m_credentialsResolver;

    /** List of configured directory default file names. */
    private List<String> m_defaultFiles;

    /** The default user and group names. */
    private CmsDefaultUsers m_defaultUsers;

    /** The event manager for the event handling. */
    private CmsEventManager m_eventManager;

    /** The thread pool executor. */
    private ScheduledThreadPoolExecutor m_executor;

    /** The set of configured export points. */
    private Set<CmsExportPoint> m_exportPoints;

    /** The context objects for GWT services. */
    private Map<String, CmsGwtServiceContext> m_gwtServiceContexts;

    /** The site manager contains information about the Cms import/export. */
    private CmsImportExportManager m_importExportManager;

    /** The link manager to resolve links in &lt;cms:link&gt; tags. */
    private CmsLinkManager m_linkManager;

    /** The locale manager used for obtaining the current locale. */
    private CmsLocaleManager m_localeManager;

    /** The login manager. */
    private CmsLoginManager m_loginManager;

    /** The memory monitor for the collection of memory and runtime statistics. */
    private CmsMemoryMonitor m_memoryMonitor;

    /** The module manager. */
    private CmsModuleManager m_moduleManager;

    /** The organizational unit manager. */
    private CmsOrgUnitManager m_orgUnitManager;

    /** The password handler used to digest and validate passwords. */
    private I_CmsPasswordHandler m_passwordHandler;

    /** The publish engine. */
    private CmsPublishEngine m_publishEngine;

    /** The publish manager instance. */
    private CmsPublishManager m_publishManager;

    /** The repository manager. */
    private CmsRepositoryManager m_repositoryManager;

    /** The configured request handlers that handle "special" requests, for example in the static export on demand. */
    private Map<String, I_CmsRequestHandler> m_requestHandlers;

    /** Stores the resource init handlers that allow modification of the requested resource. */
    private List<I_CmsResourceInit> m_resourceInitHandlers;

    /** The resource manager. */
    private CmsResourceManager m_resourceManager;

    /** The role manager. */
    private CmsRoleManager m_roleManager;

    /** The runlevel of this OpenCmsCore object instance. */
    private int m_runLevel;

    /** The runtime properties allow storage of system wide accessible runtime information. */
    private Map<Object, Object> m_runtimeProperties;

    /** The configured scheduler manager. */
    private CmsScheduleManager m_scheduleManager;

    /** The search manager provides indexing and searching. */
    private CmsSearchManager m_searchManager;

    /** The security manager to access the database and validate user permissions. */
    private CmsSecurityManager m_securityManager;

    /** The session manager. */
    private CmsSessionManager m_sessionManager;

    /** The site manager contains information about all configured sites. */
    private CmsSiteManagerImpl m_siteManager;

    /** The static export manager. */
    private CmsStaticExportManager m_staticExportManager;

    /** The subscription manager. */
    private CmsSubscriptionManager m_subscriptionManager;

    /** The system information container for "read only" system settings. */
    private CmsSystemInfo m_systemInfo;

    /** The template context manager. */
    private CmsTemplateContextManager m_templateContextManager;

    /** The thread store. */
    private CmsThreadStore m_threadStore;

    /** The runtime validation handler. */
    private I_CmsValidationHandler m_validationHandler;

    /** The workflow manager instance. */
    private I_CmsWorkflowManager m_workflowManager;

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
            if ((m_instance != null) && (m_instance.getRunLevel() > OpenCms.RUNLEVEL_0_OFFLINE)) {
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
                m_instance = new OpenCmsCore();
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
                System.err.println(Messages.get().getBundle().key(
                    Messages.LOG_INIT_FAILURE_MESSAGE_1,
                    errorCondition.key()));
            }
            LOG.error(errorCondition.key(), new CmsException(errorCondition));
            m_instance = null;
        } else if (m_instance != null) {
            // OpenCms already was successful initialized
            LOG.warn(Messages.get().getBundle().key(
                Messages.LOG_INIT_INVALID_ERROR_2,
                new Integer(m_instance.getRunLevel()),
                errorCondition.key()));
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
                CmsLog.INIT.error(Messages.get().getBundle().key(Messages.LOG_DUPLICATE_REQUEST_HANDLER_1, name));
                continue;
            }
            m_requestHandlers.put(name, handler);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_ADDED_REQUEST_HANDLER_2,
                    name,
                    handler.getClass().getName()));
            }
        }
    }

    /**
     * Gets the ADE manager, and makes sure it is initialized.<p>
     * 
     * @return the initialized ADE manager
     */
    protected CmsADEManager getADEManager() {

        m_adeManager.initialize();
        return m_adeManager;
    }

    /**
     * Returns the alias manager.<p>
     * 
     * @return the alias manager
     */
    protected CmsAliasManager getAliasManager() {

        return m_aliasManager;
    }

    /**
     * Returns the configured authorization handler.<p>
     *
     * @return the configured authorization handler
     */
    protected I_CmsAuthorizationHandler getAuthorizationHandler() {

        return m_authorizationHandler;
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
     * Gets the configured credentials resolver instance.<p>
     * 
     * @return the credentials resolver 
     */
    protected I_CmsCredentialsResolver getCredentialsResolver() {

        return m_credentialsResolver;
    }

    /**
     * Returns the configured list of default directory file names.<p>
     *  
     * @return the configured list of default directory file names
     */
    protected List<String> getDefaultFiles() {

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
     * Returns the OpenCms event manager.<p>
     * 
     * @return the OpenCms event manager
     */
    protected CmsEventManager getEventManager() {

        return m_eventManager;
    }

    /** 
     * Gets the thread pool executor.<p>
     * 
     * @return the thread pool executor 
     */
    protected ScheduledThreadPoolExecutor getExecutor() {

        return m_executor;
    }

    /**
     * Returns the configured export points,
     * the returned set being an unmodifiable set.<p>
     * 
     * @return an unmodifiable set of the configured export points
     */
    protected Set<CmsExportPoint> getExportPoints() {

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

        return m_securityManager.getLockManager();
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
     * Returns the organizational unit manager.<p>
     * 
     * @return the organizational unit manager
     */
    protected CmsOrgUnitManager getOrgUnitManager() {

        return m_orgUnitManager;
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
     * Returns the path for the request.<p>
     * 
     * First checks the {@link HttpServletRequest#getPathInfo()}, then
     * the configured request error page attribute (if set), and then 
     * if still undefined the <code>/</code> is returned as path info.<p> 
     * 
     * This is only needed when the {@link HttpServletRequest#getPathInfo()}
     * is not really working as expected like in BEA WLS 9.x, where we have 
     * to use the 'weblogic.servlet.errorPage' request attribute.<p>
     * 
     * @param req the http request context
     * 
     * @return the path for the request
     */
    protected String getPathInfo(HttpServletRequest req) {

        String path = req.getPathInfo();
        if (path == null) {
            // if the HttpServletRequest#getPathInfo() method does not work properly  
            String requestErrorPageAttribute = getSystemInfo().getServletContainerSettings().getRequestErrorPageAttribute();
            if (requestErrorPageAttribute != null) {
                // use the proper page attribute
                path = (String)req.getAttribute(requestErrorPageAttribute);
                if (path != null) {
                    int pos = path.indexOf("/", 1);
                    if (pos > 0) {
                        // cut off the servlet name
                        path = path.substring(pos);
                    }
                }
            }
        }
        if (path == null) {
            path = "/";
        }
        return path;
    }

    /**
     * Returns the publish manager instance.<p>
     * 
     * @return the publish manager instance
     */
    protected CmsPublishManager getPublishManager() {

        return m_publishManager;
    }

    /**
     * Returns the repository manager.<p>
     * 
     * @return the repository manager
     */
    protected CmsRepositoryManager getRepositoryManager() {

        return m_repositoryManager;
    }

    /**
     * Returns the handler instance for the specified name, 
     * or null if the name does not match any handler name.<p>
     * 
     * @param name the name of the handler instance to return
     * @return the handler instance for the specified name
     */
    protected I_CmsRequestHandler getRequestHandler(String name) {

        return m_requestHandlers.get(name);
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
     * Returns the role manager.<p>
     * 
     * @return the role manager
     */
    protected CmsRoleManager getRoleManager() {

        return m_roleManager;
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
    protected CmsSiteManagerImpl getSiteManager() {

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
     * Returns the subscription manager.<p>
     * 
     * @return the subscription manager
     */
    protected CmsSubscriptionManager getSubscriptionManager() {

        return m_subscriptionManager;
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
     * Gets the template context manager instance.<p>
     * 
     * @return the template context manager instance 
     */
    protected CmsTemplateContextManager getTemplateContextManager() {

        return m_templateContextManager;

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
     * Returns the runtime validation handler.<p>
     * 
     * @return the validation handler
     */
    protected I_CmsValidationHandler getValidationHandler() {

        return m_validationHandler;
    }

    /**
     * Returns the workflow manager instance.<p>
     * 
     * @return the workflow manager
     */
    protected I_CmsWorkflowManager getWorkflowManager() {

        return m_workflowManager;
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
     * The request time (<code>{@link CmsRequestContext#getRequestTime()}</code>) 
     * is set to the current time.<p>
     * 
     * @param cms the CmsObject to create a copy of
     * 
     * @return an independent copy of the provided CmsObject
     * 
     * @throws CmsException in case the initialization failed
     * 
     * @see OpenCms#initCmsObject(CmsObject)
     * @see OpenCms#initCmsObject(CmsObject, CmsContextInfo)
     * @see OpenCms#initCmsObject(String)
     */
    protected CmsObject initCmsObject(CmsObject cms) throws CmsException {

        CmsContextInfo contextInfo = new CmsContextInfo(cms.getRequestContext());
        contextInfo.setRequestTime(CmsContextInfo.CURRENT_TIME);
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

        if ((adminCms == null) || !m_roleManager.hasRole(adminCms, CmsRole.ROOT_ADMIN)) {
            if (!userName.endsWith(getDefaultUsers().getUserGuest())
                && !userName.endsWith(getDefaultUsers().getUserExport())) {

                // if no admin object is provided, only "Guest" or "Export" user can be generated
                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_INVALID_INIT_USER_2,
                    userName,
                    ((adminCms != null) ? (adminCms.getRequestContext().getCurrentUser().getName()) : ""));
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
     * Initializes a new cms object from the session data of the request.<p>
     * 
     * If no session data is found, <code>null</code> is returned.<p>
     * 
     * @param req the request
     * 
     * @return the new initialized cms object
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsObject initCmsObjectFromSession(HttpServletRequest req) throws CmsException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to init cms object from session for request \"" + req.toString() + "\".");
        }
        // try to get an OpenCms user session info object for this request
        CmsSessionInfo sessionInfo = m_sessionManager.getSessionInfo(req);

        if (sessionInfo == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No session info found.");
            }
            return null;
        }

        // initialize the requested site root
        CmsSite site = getSiteManager().matchRequest(req);

        // a user name is found in the session manager, reuse this user information
        CmsUUID project = sessionInfo.getProject();

        // initialize site root from request
        String siteroot = null;

        // a dedicated workplace site is configured
        if ((getSiteManager().getWorkplaceSiteMatcher().equals(site.getSiteMatcher()))) {
            // if no dedicated workplace site is configured, 
            // or for the dedicated workplace site, use the site root from the session attribute
            siteroot = sessionInfo.getSiteRoot();
        } else if (site.hasSecureServer()
            && getSiteManager().getWorkplaceSiteMatcher().getUrl().equals(site.getSecureUrl())) {
            // if the workplace is using the secured site
            siteroot = sessionInfo.getSiteRoot();
        } else {
            siteroot = site.getSiteRoot();
        }

        // initialize user from request
        CmsUser user = m_securityManager.readUser(null, sessionInfo.getUserId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing cms object with user \"" + user.getName() + "\".");
        }
        return initCmsObject(req, user, siteroot, project, sessionInfo.getOrganizationalUnitFqn());
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
    protected synchronized void initConfiguration(CmsParameterConfiguration configuration) throws CmsInitException {

        String systemEncoding = null;
        try {
            systemEncoding = System.getProperty("file.encoding");
        } catch (SecurityException se) {
            // security manager is active, but we will try other options before giving up
        }
        Security.addProvider(new CryptixCrypto());
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_FILE_ENCODING_1, systemEncoding));
        }

        // read server ethernet address (MAC) and init UUID generator
        String ethernetAddress = configuration.getString("server.ethernet.address", CmsStringUtil.getEthernetAddress());
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ETHERNET_ADDRESS_1, ethernetAddress));
        }
        CmsUUID.init(ethernetAddress);

        // set the server name
        String serverName = configuration.getString("server.name", "OpenCmsServer");
        getSystemInfo().setServerName(serverName);

        // check the installed Java SDK
        try {
            if (CmsLog.INIT.isInfoEnabled()) {
                String jdkinfo = System.getProperty("java.vm.name") + " ";
                jdkinfo += System.getProperty("java.vm.version") + " ";
                jdkinfo += System.getProperty("java.vm.info") + " ";
                jdkinfo += System.getProperty("java.vm.vendor") + " ";
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_JAVA_VM_1, jdkinfo));
                String osinfo = System.getProperty("os.name") + " ";
                osinfo += System.getProperty("os.version") + " ";
                osinfo += System.getProperty("os.arch") + " ";
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_OPERATING_SYSTEM_1, osinfo));
            }
        } catch (Exception e) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_PROP_0), e);
        }

        // create the configuration manager instance    
        m_configurationManager = new CmsConfigurationManager(getSystemInfo().getConfigFolder());
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

        // initialize the memory monitor
        CmsMemoryMonitorConfiguration memoryMonitorConfiguration = systemConfiguration.getCmsMemoryMonitorConfiguration();
        // initialize the memory monitor
        try {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(memoryMonitorConfiguration.getClassName())) {
                m_memoryMonitor = (CmsMemoryMonitor)Class.forName(memoryMonitorConfiguration.getClassName()).newInstance();
            } else {
                m_memoryMonitor = new CmsMemoryMonitor();
            }
        } catch (Exception e) {
            // we can not start without a valid memory monitor
            throw new CmsInitException(Messages.get().container(
                Messages.ERR_CRITICAL_INIT_MEMORY_MONITOR_1,
                memoryMonitorConfiguration.getClassName()), e);
        }
        m_memoryMonitor.initialize(systemConfiguration);

        // get the event manager from the configuration and initialize it with the events already registered
        CmsEventManager configuredEventManager = systemConfiguration.getEventManager();
        configuredEventManager.initialize(m_eventManager);
        m_eventManager = configuredEventManager;

        // check if the encoding setting is valid
        String setEncoding = systemConfiguration.getDefaultContentEncoding();
        String defaultEncoding = CmsEncoder.lookupEncoding(setEncoding, null);
        if (defaultEncoding == null) {
            // we can not start without a valid encoding setting
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_ENCODING_1, setEncoding));
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_OPENCMS_ENCODING_1, defaultEncoding));
        }
        getSystemInfo().setDefaultEncoding(defaultEncoding);

        // set version history information        
        getSystemInfo().setVersionHistorySettings(
            systemConfiguration.isHistoryEnabled(),
            systemConfiguration.getHistoryVersions(),
            systemConfiguration.getHistoryVersionsAfterDeletion());
        // set mail configuration
        getSystemInfo().setMailSettings(systemConfiguration.getMailSettings());
        // set HTTP authentication settings
        getSystemInfo().setHttpAuthenticationSettings(systemConfiguration.getHttpAuthenticationSettings());

        // set content notification settings
        getSystemInfo().setNotificationTime(systemConfiguration.getNotificationTime());
        getSystemInfo().setNotificationProject(systemConfiguration.getNotificationProject());
        // set the scheduler manager
        m_scheduleManager = systemConfiguration.getScheduleManager();
        m_executor = new ScheduledThreadPoolExecutor(2);
        // set resource init classes
        m_resourceInitHandlers = systemConfiguration.getResourceInitHandlers();
        // register request handler classes
        Iterator<I_CmsRequestHandler> it = systemConfiguration.getRequestHandlers().iterator();
        while (it.hasNext()) {
            I_CmsRequestHandler handler = it.next();
            addRequestHandler(handler);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_REQUEST_HANDLER_CLASS_1,
                    handler.getClass().getName()));
            }
        }

        // read the default user configuration
        m_defaultUsers = systemConfiguration.getCmsDefaultUsers();

        // get the site manager from the configuration
        m_siteManager = systemConfiguration.getSiteManager();

        // get the VFS / resource configuration
        CmsVfsConfiguration vfsConfiguation = (CmsVfsConfiguration)m_configurationManager.getConfiguration(CmsVfsConfiguration.class);
        m_resourceManager = vfsConfiguation.getResourceManager();
        m_xmlContentTypeManager = vfsConfiguation.getXmlContentTypeManager();
        m_defaultFiles = vfsConfiguation.getDefaultFiles();

        // initialize translation engines
        m_resourceManager.setTranslators(
            vfsConfiguation.getFolderTranslator(),
            vfsConfiguation.getFileTranslator(),
            vfsConfiguation.getXsdTranslator());

        // try to initialize the flex cache
        CmsFlexCache flexCache = null;
        try {
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_FLEX_CACHE_STARTING_0));
            }
            // get the flex cache configuration from the SystemConfiguration
            CmsFlexCacheConfiguration flexCacheConfiguration = systemConfiguration.getCmsFlexCacheConfiguration();
            getSystemInfo().setDeviceSelector(flexCacheConfiguration.getDeviceSelector());
            // pass configuration to flex cache for initialization
            flexCache = new CmsFlexCache(flexCacheConfiguration);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_FLEX_CACHE_FINISHED_0));
            }
        } catch (Exception e) {
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_FLEX_CACHE_ERROR_1, e.getMessage()));
            }
        }

        if (flexCache != null) {
            // check all resource loaders if they require the Flex cache
            Iterator<I_CmsResourceLoader> i = m_resourceManager.getLoaders().iterator();
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
        m_repositoryManager = importExportConfiguration.getRepositoryManager();

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

        // get the validation handler
        m_validationHandler = systemConfiguration.getValidationHandler();

        // get the authorization handler
        m_authorizationHandler = systemConfiguration.getAuthorizationHandler();

        // get the login manager
        m_loginManager = systemConfiguration.getLoginManager();

        // initialize the publish engine
        m_publishEngine = new CmsPublishEngine(systemConfiguration.getRuntimeInfoFactory());

        // Credentials resolver - needs to be set before the driver manager is initialized 
        m_credentialsResolver = systemConfiguration.getCredentialsResolver();

        // init the OpenCms security manager
        m_securityManager = CmsSecurityManager.newInstance(
            m_configurationManager,
            systemConfiguration.getRuntimeInfoFactory(),
            m_publishEngine);

        // get the publish manager
        m_publishManager = systemConfiguration.getPublishManager();

        // get the subscription manager
        m_subscriptionManager = systemConfiguration.getSubscriptionManager();

        // initialize the role manager
        m_roleManager = new CmsRoleManager(m_securityManager);

        // initialize the organizational unit manager
        m_orgUnitManager = new CmsOrgUnitManager(m_securityManager);

        // initialize the Thread store
        m_threadStore = new CmsThreadStore(m_securityManager);

        // initialize the link manager
        m_linkManager = new CmsLinkManager(m_staticExportManager.getLinkSubstitutionHandler());

        m_aliasManager = new CmsAliasManager(m_securityManager);

        // store the runtime properties
        m_runtimeProperties.putAll(systemConfiguration.getRuntimeProperties());

        // initialize the session storage provider
        I_CmsSessionStorageProvider sessionStorageProvider = systemConfiguration.getSessionStorageProvider();

        // get an Admin cms context object with site root set to "/"
        CmsObject adminCms;
        try {
            adminCms = initCmsObject(null, null, getDefaultUsers().getUserAdmin(), (String)null, (String)null);
        } catch (CmsException e) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_ADMINCMS_0), e);
        }

        m_repositoryManager.initializeCms(adminCms);
        // now initialize the other managers
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

            // initialize the module manager
            m_moduleManager.initialize(initCmsObject(adminCms), m_configurationManager);

            // initialize the resource manager
            m_resourceManager.initialize(initCmsObject(adminCms));

            // initialize the publish manager
            m_publishManager.setPublishEngine(m_publishEngine);
            m_publishManager.setSecurityManager(m_securityManager);
            m_publishManager.initialize(initCmsObject(adminCms));

            // initialize the search manager
            m_searchManager.initialize(initCmsObject(adminCms));

            CmsVfsBundleManager vfsBundleManager = new CmsVfsBundleManager(adminCms);
            vfsBundleManager.reload(true);

            // initialize the workplace manager
            m_workplaceManager.initialize(initCmsObject(adminCms));

            // initialize the session manager
            m_sessionManager.initialize(sessionStorageProvider);
            m_sessionManager.setUserSessionMode(systemConfiguration.getUserSessionMode(true));

            // initialize the subscription manager
            m_subscriptionManager.setSecurityManager(m_securityManager);
            m_subscriptionManager.initialize(adminCms);

            // initialize ade manager
            // initialize the formatter configuration
            CmsFormatterConfiguration.initialize(adminCms);
            //m_adeManager = new CmsADEManager(initCmsObject(adminCms), m_memoryMonitor, systemConfiguration);
            m_adeManager = new CmsADEManager(adminCms, m_memoryMonitor, systemConfiguration);
            m_templateContextManager = new CmsTemplateContextManager(adminCms);
            m_workflowManager = systemConfiguration.getWorkflowManager();
            if (m_workflowManager == null) {
                m_workflowManager = new CmsDefaultWorkflowManager();
                m_workflowManager.setParameters(new HashMap<String, String>());
            }
            m_workflowManager.initialize(adminCms);
        } catch (CmsException e) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_MANAGERS_0), e);
        }
    }

    /**
     * Initialization of the OpenCms runtime environment.<p>
     *
     * The connection information for the database is read 
     * from the <code>opencms.properties</code> configuration file and all 
     * driver manager are initialized via the initializer, 
     * which usually will be an instance of a <code>OpenCms</code> class.
     *
     * @param context configuration of OpenCms from <code>web.xml</code>
     * @throws CmsInitException in case OpenCms can not be initialized
     */
    protected synchronized void initContext(ServletContext context) throws CmsInitException {

        m_gwtServiceContexts = new HashMap<String, CmsGwtServiceContext>();

        // automatic servlet container recognition and specific behavior:
        CmsServletContainerSettings servletContainerSettings = new CmsServletContainerSettings(context);
        getSystemInfo().init(servletContainerSettings);

        // Collect the configurations 
        CmsParameterConfiguration configuration;
        try {
            configuration = new CmsParameterConfiguration(getSystemInfo().getConfigurationFileRfsPath());
        } catch (Exception e) {
            throw new CmsInitException(Messages.get().container(
                Messages.ERR_CRITICAL_INIT_PROPFILE_1,
                getSystemInfo().getConfigurationFileRfsPath()), e);
        }

        String throwException = configuration.getString("servlet.exception.enabled", "auto");
        if (!throwException.equals("auto")) {
            // set the parameter is not automatic, the rest of the servlet container dependent parameters
            // will be set when reading the system configuration, if not set to auto
            boolean throwExc = Boolean.valueOf(throwException).booleanValue();
            getSystemInfo().getServletContainerSettings().setServletThrowsException(throwExc);
        }

        // check if the wizard is enabled, if so stop initialization
        if (configuration.getBoolean("wizard.enabled", true)) {
            throw new CmsInitException(Messages.get().container(Messages.ERR_CRITICAL_INIT_WIZARD_0));
        }
        // output startup message and copyright to STDERR
        System.err.println(Messages.get().getBundle().key(
            Messages.LOG_STARTUP_CONSOLE_NOTE_2,
            OpenCms.getSystemInfo().getVersionNumber(),
            getSystemInfo().getWebApplicationName()));
        for (int i = 0; i < Messages.COPYRIGHT_BY_ALKACON.length; i++) {
            System.err.println(Messages.COPYRIGHT_BY_ALKACON[i]);
        }
        System.err.println();

        // output startup message to log file
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
            for (int i = 0; i < Messages.COPYRIGHT_BY_ALKACON.length; i++) {
                CmsLog.INIT.info(". " + Messages.COPYRIGHT_BY_ALKACON[i]);
            }
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LINE_0));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_STARTUP_TIME_1,
                new Date(System.currentTimeMillis())));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_OPENCMS_VERSION_1,
                OpenCms.getSystemInfo().getVersionNumber()));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SERVLET_CONTAINER_1, context.getServerInfo()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_WEBAPP_NAME_1,
                getSystemInfo().getWebApplicationName()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_SERVLET_PATH_1,
                getSystemInfo().getServletPath()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_OPENCMS_CONTEXT_1,
                getSystemInfo().getOpenCmsContext()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_WEBINF_PATH_1,
                getSystemInfo().getWebInfRfsPath()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_PROPERTY_FILE_1,
                getSystemInfo().getConfigurationFileRfsPath()));
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_LOG_FILE_1,
                getSystemInfo().getLogFileRfsPath()));
        }

        // initialize the configuration
        initConfiguration(configuration);
    }

    /**
     * Initialize member variables.<p>
     */
    protected void initMembers() {

        synchronized (LOCK) {
            m_resourceInitHandlers = new ArrayList<I_CmsResourceInit>();
            m_requestHandlers = new HashMap<String, I_CmsRequestHandler>();
            m_systemInfo = new CmsSystemInfo();
            m_exportPoints = Collections.emptySet();
            m_defaultUsers = new CmsDefaultUsers();
            m_localeManager = new CmsLocaleManager(Locale.ENGLISH);
            m_sessionManager = new CmsSessionManager();
            m_runtimeProperties = new Hashtable<Object, Object>();
            // the default event manager must be available because the configuration already registers events 
            m_eventManager = new CmsEventManager();
            // default link manager is required for test cases
            m_linkManager = new CmsLinkManager(new CmsDefaultLinkSubstitutionHandler());
        }
    }

    /**
     * Reads the requested resource from the OpenCms VFS,
     * in case a directory name is requested, the default files of the 
     * directory will be looked up and the first match is returned.<p>
     *
     * The resource that is returned is always a <code>{@link org.opencms.file.CmsFile}</code>,
     * even though the content will usually not be loaded in the result. Folders are never returned since
     * the point of this method is really to load the default file if just a folder name is requested. If 
     * there is no default file in a folder, then the return value is null and no CmsException is thrown.<p>
     *
     * The URI stored in the given OpenCms user context will be changed to the URI of the resource 
     * that was found and returned.<p>
     * 
     * Implementing and configuring an <code>{@link I_CmsResourceInit}</code> handler 
     * allows to customize the process of default resource selection.<p>
     *
     * @param cms the current users OpenCms context
     * @param resourceName the path of the requested resource in the OpenCms VFS
     * @param req the current http request
     * @param res the current http response
     * 
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

        CmsException tmpException = null;
        CmsResource resource;

        try {
            // try to read the requested resource
            resource = cms.readDefaultFile(resourceName);
        } catch (CmsException e) {
            // file or folder with given name does not exist, store exception
            tmpException = e;
            resource = null;
        }

        if (resource != null) {
            // set the request uri to the right file
            cms.getRequestContext().setUri(cms.getSitePath(resource));
            // test if this file is only available for internal access operations
            if (resource.isInternal()) {
                throw new CmsException(Messages.get().container(
                    Messages.ERR_READ_INTERNAL_RESOURCE_1,
                    cms.getRequestContext().getUri()));
            }

            // check online project
            if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                // check if resource is secure
                boolean secure = Boolean.valueOf(
                    cms.readPropertyObject(cms.getSitePath(resource), CmsPropertyDefinition.PROPERTY_SECURE, true).getValue()).booleanValue();
                if (secure) {
                    // resource is secure, check site config
                    CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
                    // check the secure url
                    String secureUrl = null;
                    try {
                        secureUrl = site.getSecureUrl();
                    } catch (Exception e) {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.ERR_SECURE_SITE_NOT_CONFIGURED_1, resourceName),
                            e);
                        throw new CmsException(Messages.get().container(
                            Messages.ERR_SECURE_SITE_NOT_CONFIGURED_1,
                            resourceName), e);
                    }
                    boolean usingSec = true;
                    if (req != null) {
                        usingSec = req.getRequestURL().toString().toUpperCase().startsWith(secureUrl.toUpperCase());
                    }
                    if (site.isExclusiveUrl() && !usingSec) {
                        resource = null;
                        // secure resource without secure protocol, check error config
                        if (site.isExclusiveError()) {
                            // trigger 404 error
                            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                                Messages.ERR_REQUEST_SECURE_RESOURCE_0));
                        } else {
                            // redirect
                            String target = OpenCms.getLinkManager().getOnlineLink(
                                cms,
                                cms.getRequestContext().getUri());
                            try {
                                res.sendRedirect(target);
                            } catch (Exception e) {
                                // ignore, but should never happen
                            }
                        }
                    }
                }
            }
        }

        boolean clearErrors = false;
        // test if this file has to be checked or modified
        for (I_CmsResourceInit handler : m_resourceInitHandlers) {
            try {
                resource = handler.initResource(resource, cms, req, res);
                // the loop has to be interrupted when the exception is thrown!
            } catch (CmsResourceInitException e) {
                if (e.isClearErrors()) {
                    tmpException = null;
                    clearErrors = true;
                }
                break;
            } catch (CmsSecurityException e) {
                tmpException = e;
                break;
            }
        }

        // file is still null and not found exception was thrown, so throw original exception
        if (resource == null) {
            if (tmpException != null) {
                throw tmpException;
            } else if (!clearErrors) {
                throw new CmsVfsResourceNotFoundException(org.opencms.main.Messages.get().container(
                    org.opencms.main.Messages.ERR_PATH_NOT_FOUND_1,
                    resourceName));
            }
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
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_SYSTEM_RUNNING_1,
                    CmsStringUtil.formatRuntime(getSystemInfo().getRuntime())));
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LINE_0));
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
            }
        }
    }

    /**
     * Invokes the GWT servlet from within OpenCms.<p>
     * 
     * @param serviceName the GWT PRC service class name 
     * @param req the current servlet request
     * @param res the current servlet response
     * @param servletConfig the servlet configuration
     */
    protected void invokeGwtService(
        String serviceName,
        HttpServletRequest req,
        HttpServletResponse res,
        ServletConfig servletConfig) {

        CmsObject cms = null;
        try {
            // instantiate CMS context
            cms = initCmsObject(req, res);
            // instantiate GWT RPC service
            CmsGwtService rpcService = getGwtService(serviceName, servletConfig);
            // check permissions
            rpcService.checkPermissions(cms);
            // set runtime variables
            rpcService.setCms(cms);
            Object lock = req.getSession();
            if (lock == null) {
                lock = new Object();
            }
            synchronized (lock) {
                rpcService.service(req, res);
            }
            // update the session info
            m_sessionManager.updateSessionInfo(cms, req);
        } catch (CmsRoleViolationException rv) {
            // don't log these into the error channel
            LOG.debug(rv.getLocalizedMessage(), rv);
            // error code not set - set "internal server error" (500)
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            res.setStatus(status);
            try {
                res.sendError(status, rv.toString());
            } catch (IOException e) {
                // can be ignored
                LOG.error(e.getLocalizedMessage(), e);
            }
        } catch (Throwable t) {
            // error code not set - set "internal server error" (500)
            LOG.error(t.getLocalizedMessage(), t);
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            res.setStatus(status);
            try {
                res.sendError(status, t.toString());
            } catch (IOException e) {
                // can be ignored
                LOG.error(e.getLocalizedMessage(), e);
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

            if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                String uri = cms.getRequestContext().getUri();
                if (OpenCms.getStaticExportManager().isExportLink(cms, uri)) {
                    // if we used the request's query string for getRfsName, clients could cause an unlimited number 
                    // of files to be exported just by varying the request parameters! 
                    String url = OpenCms.getStaticExportManager().getRfsName(cms, uri);
                    String siteRoot = cms.getRequestContext().getSiteRoot();
                    url = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot).getUrl() + url;
                    res.sendRedirect(url);
                    return;
                }
            }

            // user is initialized, now deliver the requested resource
            CmsResource resource = initResource(cms, cms.getRequestContext().getUri(), req, res);
            if (resource != null) {
                // a file was read, go on process it
                m_resourceManager.loadResource(cms, resource, req, res);
                m_sessionManager.updateSessionInfo(cms, req);
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
                System.err.println(Messages.get().getBundle().key(
                    Messages.LOG_SHUTDOWN_CONSOLE_NOTE_2,
                    getSystemInfo().getVersionNumber(),
                    getSystemInfo().getWebApplicationName()));
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LINE_0));
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_SHUTDOWN_START_1,
                        getSystemInfo().getVersionNumber()));
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_CURRENT_RUNLEVEL_1,
                        new Integer(getRunLevel())));
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_SHUTDOWN_TIME_1,
                        new Date(System.currentTimeMillis())));
                }

                // take the system offline
                setRunLevel(OpenCms.RUNLEVEL_0_OFFLINE);

                if (LOG.isDebugEnabled()) {
                    // log exception to see which method did call the shutdown
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_SHUTDOWN_TRACE_0), new Exception());
                }

                try {
                    // the first thing we have to do is to wait until the current publish process finishes
                    m_publishEngine.shutDown();
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_PUBLISH_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                try {
                    // search manager must be shut down early since there may be background indexing still ongoing
                    if (m_searchManager != null) {
                        m_searchManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_SEARCH_MANAGER_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                try {
                    if (m_staticExportManager != null) {
                        m_staticExportManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_EXPORT_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                try {
                    if (m_moduleManager != null) {
                        m_moduleManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_MODULE_SHUTDOWN_1, e.getMessage()),
                        e);
                }

                try {
                    if (m_executor != null) {
                        m_executor.shutdownNow();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_MODULE_SHUTDOWN_1, e.getMessage()),
                        e);
                }

                try {
                    if (m_scheduleManager != null) {
                        m_scheduleManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_SCHEDULE_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                try {
                    if (m_resourceManager != null) {
                        m_resourceManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_RESOURCE_SHUTDOWN_1, e.getMessage()),
                        e);
                }

                try {
                    if (m_repositoryManager != null) {
                        m_repositoryManager.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(e.getLocalizedMessage(), e);
                }

                try {
                    // has to be stopped before the security manager, since this thread uses it
                    if (m_threadStore != null) {
                        m_threadStore.shutDown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_THREAD_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                try {
                    if (m_securityManager != null) {
                        m_securityManager.destroy();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_SECURITY_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                try {
                    if (m_sessionManager != null) {
                        m_sessionManager.shutdown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_SESSION_MANAGER_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                try {
                    if (m_memoryMonitor != null) {
                        m_memoryMonitor.shutdown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_MEMORY_MONITOR_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                try {
                    if (m_adeManager != null) {
                        m_adeManager.shutdown();
                    }
                } catch (Throwable e) {
                    CmsLog.INIT.error(
                        Messages.get().getBundle().key(Messages.LOG_ERROR_ADE_MANAGER_SHUTDOWN_1, e.getMessage()),
                        e);
                }
                String runtime = CmsStringUtil.formatRuntime(getSystemInfo().getRuntime());
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_OPENCMS_STOPPED_1, runtime));
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LINE_0));
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DOT_0));
                }
                System.err.println(Messages.get().getBundle().key(Messages.LOG_CONSOLE_TOTAL_RUNTIME_1, runtime));

            }
            m_instance = null;
        }
    }

    /**
     * This method updates the request context information.<p>
     * 
     * The update information is:<br>
     * <ul>
     *   <li>Requested Url</li>
     *   <li>Locale</li>
     *   <li>Encoding</li>
     *   <li>Remote Address</li>
     *   <li>Request Time</li>
     * </ul>
     * 
     * @param request the current request
     * @param cms the cms object to update the request context for
     * 
     * @return a new updated cms context
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsObject updateContext(HttpServletRequest request, CmsObject cms) throws CmsException {

        // get the right site for the request
        String siteRoot = null;
        if (cms.getRequestContext().getUri().startsWith("/system/workplace/")
            && getRoleManager().hasRole(cms, CmsRole.WORKPLACE_USER)) {
            // keep the site root for workplace requests
            siteRoot = cms.getRequestContext().getSiteRoot();
        } else {
            CmsSite site = OpenCms.getSiteManager().matchRequest(request);
            siteRoot = site.getSiteRoot();
        }
        return initCmsObject(
            request,
            cms.getRequestContext().getCurrentUser(),
            siteRoot,
            cms.getRequestContext().getCurrentProject().getUuid(),
            cms.getRequestContext().getOuFqn());
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
    protected OpenCmsCore upgradeRunlevel(CmsParameterConfiguration configuration) throws CmsInitException {

        synchronized (LOCK) {
            if ((m_instance != null) && (getRunLevel() >= OpenCms.RUNLEVEL_2_INITIALIZING)) {
                // instance already in runlevel 3 or 4
                return m_instance;
            }
            if (getRunLevel() != OpenCms.RUNLEVEL_1_CORE_OBJECT) {
                CmsLog.INIT.error(Messages.get().getBundle().key(
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

            afterUpgradeRunlevel();

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
                CmsLog.INIT.error(Messages.get().getBundle().key(
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

            afterUpgradeRunlevel();

            return m_instance;
        }
    }

    /**
     * Writes the XML configuration for the provided configuration class.<p>
     * 
     * @param clazz the configuration class to write the XML for
     */
    protected void writeConfiguration(Class<?> clazz) {

        // exception handling is provided here to ensure identical log messages
        try {
            m_configurationManager.writeConfiguration(clazz);
        } catch (IOException e) {
            CmsLog.getLog(CmsConfigurationManager.class).error(
                Messages.get().getBundle().key(Messages.LOG_ERROR_WRITING_CONFIG_1, clazz.getName()),
                e);
        } catch (CmsConfigurationException e) {
            CmsLog.getLog(CmsConfigurationManager.class).error(
                Messages.get().getBundle().key(Messages.LOG_ERROR_WRITING_CONFIG_1, clazz.getName()),
                e);
        }
    }

    /**
     * Adds the given set of export points to the list of all configured export points.<p> 
     * 
     * @param exportPoints the export points to add
     */
    private void addExportPoints(Set<CmsExportPoint> exportPoints) {

        // create a new immutable set of export points
        HashSet<CmsExportPoint> newSet = new HashSet<CmsExportPoint>(m_exportPoints.size() + exportPoints.size());
        newSet.addAll(exportPoints);
        newSet.addAll(m_exportPoints);
        m_exportPoints = Collections.unmodifiableSet(newSet);
    }

    /**
     * Finishes the startup sequence after last runlevel upgrade.<p>
     */
    private void afterUpgradeRunlevel() {

        try {
            // read the persistent locks
            m_instance.m_securityManager.readLocks();
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    org.opencms.lock.Messages.get().getBundle().key(org.opencms.lock.Messages.ERR_READ_LOCKS_0),
                    e);
            }
        }

        // everything is initialized, now start publishing
        m_publishManager.startPublishing();
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
     * @param t the exception that occurred
     */
    private void errorHandling(CmsObject cms, HttpServletRequest req, HttpServletResponse res, Throwable t) {

        // remove the controller attribute from the request
        CmsFlexController.removeController(req);

        boolean canWrite = (!res.isCommitted() && !res.containsHeader("Location"));
        int status = -1;
        boolean isGuest = true;

        if (t instanceof ServletException) {
            ServletException s = (ServletException)t;
            if (s.getRootCause() != null) {
                t = s.getRootCause();
            }
        } else if (t instanceof CmsSecurityException) {
            // access error - display login dialog
            if (canWrite) {
                try {
                    m_authorizationHandler.requestAuthorization(req, res, getLoginFormURL(req, res));
                } catch (IOException ioe) {
                    // there is nothing we can do about this
                }
                return;
            }
        } else if (t instanceof CmsDbEntryNotFoundException) {
            // user or group does not exist
            status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            isGuest = false;
        } else if (t instanceof CmsVfsResourceNotFoundException) {
            // file not found - display 404 error.
            status = HttpServletResponse.SC_NOT_FOUND;
        } else if (t instanceof CmsException) {
            if (t.getCause() != null) {
                t = t.getCause();
            }
            LOG.error(t.getLocalizedMessage() + " rendering URL " + req.getRequestURL(), t);
        } else if (t.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
            // only log to debug channel any exceptions caused by a client abort - this is tomcat specific 
            LOG.debug(t.getLocalizedMessage() + " rendering URL " + req.getRequestURL(), t);
        } else {
            LOG.error(t.getLocalizedMessage() + " rendering URL " + req.getRequestURL(), t);
        }

        if (status < 1) {
            // error code not set - set "internal server error" (500)
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        res.setStatus(status);

        try {
            if ((cms != null) && (cms.getRequestContext().getCurrentUser() != null)) {
                isGuest = isGuest
                    && (cms.getRequestContext().getCurrentUser().isGuestUser() || cms.userInGroup(
                        cms.getRequestContext().getCurrentUser().getName(),
                        OpenCms.getDefaultUsers().getGroupGuests()));
            }
        } catch (CmsException e) {
            // result is false
            LOG.error(e.getLocalizedMessage(), e);
        }

        if (canWrite) {
            res.setContentType("text/html");
            CmsRequestUtil.setNoCacheHeaders(res);
            if (!isGuest && (cms != null) && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                try {
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.getWriter().print(createErrorBox(t, req, cms));
                } catch (IOException e) {
                    // can be ignored
                    LOG.error(e.getLocalizedMessage(), e);
                }
            } else {
                try {
                    res.sendError(status, t.toString());
                } catch (IOException e) {
                    // can be ignored
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * 
     * 
     * @param serviceName the GWT PRC service class name 
     * @param servletConfig the servlet configuration
     * 
     * @return the GWT service instance
     * 
     * @throws Throwable if something goes wrong
     */
    private synchronized CmsGwtService getGwtService(String serviceName, ServletConfig servletConfig) throws Throwable {

        CmsGwtServiceContext context = m_gwtServiceContexts.get(serviceName);
        if (context == null) {
            context = new CmsGwtServiceContext(serviceName);
            m_gwtServiceContexts.put(serviceName, context);
        }
        CmsGwtService gwtService = (CmsGwtService)Class.forName(serviceName).newInstance();
        gwtService.init(servletConfig);
        gwtService.setContext(context);
        return gwtService;
    }

    /**
     * Reads the login form which should be used for authenticating the current request.<p>
     * 
     * @param req current request
     * @param res current response
     * 
     * @return the URL of the login form or <code>null</code> if not set
     * 
     * @throws IOException
     */
    private String getLoginFormURL(HttpServletRequest req, HttpServletResponse res) throws IOException {

        CmsHttpAuthenticationSettings httpAuthenticationSettings = OpenCms.getSystemInfo().getHttpAuthenticationSettings();
        String loginFormURL = null;

        // this will create an admin user with the "right" site root already set
        CmsObject adminCms;
        try {
            adminCms = initCmsObject(req, res, OpenCms.getDefaultUsers().getUserAdmin(), null, null);
        } catch (CmsException e) {
            // this should never happen, if it does we can't continue
            throw new IOException(Messages.get().getBundle().key(
                Messages.ERR_INVALID_INIT_USER_2,
                OpenCms.getDefaultUsers().getUserAdmin(),
                null));
        }
        // get the requested resource
        String path = adminCms.getRequestContext().getUri();
        CmsProperty propertyLoginForm = null;
        try {
            propertyLoginForm = adminCms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_LOGIN_FORM, true);
        } catch (Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_ERROR_READING_AUTH_PROP_2,
                        CmsPropertyDefinition.PROPERTY_LOGIN_FORM,
                        path),
                    t);
            }
        }

        String params = null;
        if ((propertyLoginForm != null)
            && (propertyLoginForm != CmsProperty.getNullProperty())
            && CmsStringUtil.isNotEmpty(propertyLoginForm.getValue())) {
            // login form property value was found            
            // build a redirect URL using the value of the property
            // "__loginform" is a dummy request parameter that could be used in a JSP template to trigger
            // if the template should display a login formular or not
            loginFormURL = propertyLoginForm.getValue();
            params = "__loginform=true";
        } else if (!httpAuthenticationSettings.useBrowserBasedHttpAuthentication()
            && CmsStringUtil.isNotEmpty(httpAuthenticationSettings.getFormBasedHttpAuthenticationUri())) {
            // login form property value not set, but form login set in configuration
            // build a redirect URL to the default login form URI configured in opencms.properties
            loginFormURL = httpAuthenticationSettings.getFormBasedHttpAuthenticationUri();
        }

        String callbackURL = CmsRequestUtil.encodeParamsWithUri(path, req);
        if (loginFormURL != null) {
            if (!loginFormURL.startsWith("http")) {
                loginFormURL = m_linkManager.substituteLink(adminCms, loginFormURL, null, true);
            } else {
                callbackURL = m_linkManager.getServerLink(adminCms, path);
                callbackURL = CmsRequestUtil.encodeParamsWithUri(callbackURL, req);
            }
        }

        return m_authorizationHandler.getLoginFormURL(loginFormURL, params, callbackURL);
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
            user = m_securityManager.readUser(null, contextInfo.getUserName());
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
            contextInfo.getRequestTime(),
            m_resourceManager.getFolderTranslator(),
            m_resourceManager.getFileTranslator(),
            contextInfo.getOuFqn());
        context.setDetailResource(contextInfo.getDetailResource());

        // now initialize and return the CmsObject
        return new CmsObject(m_securityManager, context);
    }

    /**
     * Initializes a {@link CmsObject} with the given users information.<p>
     * 
     * @param request the current http request (or <code>null</code>)
     * @param user the initialized user
     * @param siteRoot the users current site 
     * @param projectId the id of the users current project
     * @param ouFqn the organizational unit
     * 
     * @return the initialized CmsObject
     * 
     * @throws CmsException in case something goes wrong
     */
    private CmsObject initCmsObject(
        HttpServletRequest request,
        CmsUser user,
        String siteRoot,
        CmsUUID projectId,
        String ouFqn) throws CmsException {

        CmsProject project = null;
        try {
            project = m_securityManager.readProject(projectId);
        } catch (CmsDbEntryNotFoundException e) {
            // project not found, switch to online project
            project = m_securityManager.readProject(CmsProject.ONLINE_PROJECT_ID);
        }

        // get requested resource uri and remote IP address, as well as time for "time warp" browsing
        String requestedResource = null;
        Long requestTimeAttr = null;
        String remoteAddr;

        if (request != null) {
            // get path info from request
            requestedResource = getPathInfo(request);

            // check for special header for remote address
            remoteAddr = request.getHeader(CmsRequestUtil.HEADER_X_FORWARDED_FOR);
            if (remoteAddr == null) {
                // if header is not available, use default remote address
                remoteAddr = request.getRemoteAddr();
            }

            // check for special "time warp" browsing
            HttpSession session = request.getSession(false);
            if (session != null) {
                // no new session must be created here
                requestTimeAttr = (Long)session.getAttribute(CmsContextInfo.ATTRIBUTE_REQUEST_TIME);
            }
        } else {
            // if no request is available, the IP is always set to localhost
            remoteAddr = CmsContextInfo.LOCALHOST;
        }
        if (requestedResource == null) {
            // path info can still be null
            requestedResource = "/";
        }

        // calculate the request time
        long requestTime;
        if (requestTimeAttr == null) {
            requestTime = System.currentTimeMillis();
        } else {
            requestTime = requestTimeAttr.longValue();
        }

        // get locale and encoding   
        CmsI18nInfo i18nInfo;
        if (m_localeManager.isInitialized()) {
            // locale manager is initialized
            // resolve locale and encoding
            if (requestedResource.endsWith(OpenCmsServlet.HANDLE_GWT) && (request != null)) {
                // GWT RPC call, always keep the request encoding and use the default locale
                i18nInfo = new CmsI18nInfo(CmsLocaleManager.getDefaultLocale(), request.getCharacterEncoding());
            } else {
                String resourceName;
                if (requestedResource.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
                    // add site root only if resource name does not start with "/system"
                    resourceName = requestedResource;
                } else if (OpenCms.getSiteManager().startsWithShared(requestedResource)) {
                    resourceName = requestedResource;
                } else {
                    resourceName = siteRoot.concat(requestedResource);
                }
                i18nInfo = m_localeManager.getI18nInfo(request, user, project, resourceName);
            }
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
            siteRoot,
            i18nInfo.getLocale(),
            i18nInfo.getEncoding(),
            remoteAddr,
            requestTime,
            ouFqn);

        // now generate and return the CmsObject
        return initCmsObject(contextInfo);
    }

    /**
     * Handles the user authentification for each request sent to OpenCms.<p>
     *
     * User authentification is done in three steps:
     * <ol>
     * <li>Session authentification: OpenCms stores information of all authentificated
     *      users in an internal storage based on the users session.</li>
     * <li>Authorization handler authentification: If the session authentification fails, 
     *      the current configured authorization handler is called.</li>
     * <li>Default user: When both authentification methods fail, the user is set to 
     *      the default (Guest) user.</li>
     * </ol>
     *
     * @param req the current http request
     * @param res the current http response
     * 
     * @return the initialized cms context
     * 
     * @throws IOException if user authentication fails
     * @throws CmsException in case something goes wrong
     */
    private CmsObject initCmsObject(HttpServletRequest req, HttpServletResponse res) throws IOException, CmsException {

        // first try to restore a stored session
        CmsObject cms = initCmsObjectFromSession(req);
        if (cms != null) {
            return cms;
        }

        // if does not work, try to authorize the request
        I_CmsAuthorizationHandler.I_PrivilegedLoginAction loginAction = new I_CmsAuthorizationHandler.I_PrivilegedLoginAction() {

            private CmsObject m_adminCms;

            /**
             * @see org.opencms.security.I_CmsAuthorizationHandler.I_PrivilegedLoginAction#doLogin(javax.servlet.http.HttpServletRequest, java.lang.String)
             */
            public CmsObject doLogin(HttpServletRequest request, String principal) throws CmsException {

                try {
                    CmsUser user = m_adminCms.readUser(principal);
                    if (!user.isEnabled()) {
                        throw new CmsException(Messages.get().container(
                            Messages.ERR_INVALID_INIT_USER_2,
                            user.getName(),
                            "-"));
                    }

                    // initialize the new cms object
                    CmsContextInfo contextInfo = new CmsContextInfo(m_adminCms.getRequestContext());
                    contextInfo.setUserName(principal);
                    CmsObject newCms = initCmsObject(m_adminCms, contextInfo);

                    if (contextInfo.getRequestedUri().startsWith("/system/workplace/")
                        && getRoleManager().hasRole(newCms, CmsRole.WORKPLACE_USER)) {
                        // set the default project of the user for workplace users
                        CmsUserSettings settings = new CmsUserSettings(newCms);
                        // set the configured start site
                        newCms.getRequestContext().setSiteRoot(settings.getStartSite());
                        try {
                            CmsProject project = newCms.readProject(settings.getStartProject());
                            if (getOrgUnitManager().getAllAccessibleProjects(newCms, project.getOuFqn(), false).contains(
                                project)) {
                                // user has access to the project, set this as current project
                                newCms.getRequestContext().setCurrentProject(project);
                            }
                        } catch (CmsException e) {
                            // unable to set the startup project, bad but not critical
                        }
                    }
                    // fire the login user event
                    OpenCms.fireCmsEvent(
                        I_CmsEventListener.EVENT_LOGIN_USER,
                        Collections.<String, Object> singletonMap("data", user));
                    return newCms;
                } finally {
                    m_adminCms = null;
                }
            }

            /**
             * @see org.opencms.security.I_CmsAuthorizationHandler.I_PrivilegedLoginAction#getCmsObject()
             */
            public CmsObject getCmsObject() {

                return m_adminCms;
            }

            /**
             * @see org.opencms.security.I_CmsAuthorizationHandler.I_PrivilegedLoginAction#setCmsObject(org.opencms.file.CmsObject)
             */
            public void setCmsObject(CmsObject adminCms) {

                m_adminCms = adminCms;
            }
        };
        loginAction.setCmsObject(initCmsObject(req, res, OpenCms.getDefaultUsers().getUserAdmin(), null, null));
        cms = m_authorizationHandler.initCmsObject(req, loginAction);
        if (cms != null) {
            return cms;
        }

        // authentification failed or not enough permissions, so display a login screen
        m_authorizationHandler.requestAuthorization(req, res, getLoginFormURL(req, res));

        cms = initCmsObject(
            req,
            m_securityManager.readUser(null, OpenCms.getDefaultUsers().getUserGuest()),
            getSiteManager().matchRequest(req).getSiteRoot(),
            CmsProject.ONLINE_PROJECT_ID,
            "");
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
     * @param ouFqn the organizational unit, if <code>null</code> the users ou is used
     * 
     * @return a cms context that has been initialized with "Guest" permissions
     * 
     * @throws CmsException in case the CmsObject could not be initialized
     */
    private CmsObject initCmsObject(
        HttpServletRequest req,
        HttpServletResponse res,
        String user,
        String password,
        String ouFqn) throws CmsException {

        String siteroot = null;
        // gather information from request if provided
        if (req != null) {
            siteroot = OpenCms.getSiteManager().matchRequest(req).getSiteRoot();
        }
        // initialize the user        
        if (user == null) {
            user = getDefaultUsers().getUserGuest();
        }
        if (siteroot == null) {
            siteroot = "/";
        }
        CmsObject cms = initCmsObject(
            req,
            m_securityManager.readUser(null, user),
            siteroot,
            CmsProject.ONLINE_PROJECT_ID,
            ouFqn);
        // login the user if different from Guest and password was provided
        if ((password != null) && !getDefaultUsers().isUserGuest(user)) {
            cms.loginUser(user, password, CmsContextInfo.LOCALHOST);
        }
        return cms;
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
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_RUNLEVEL_CHANGE_2,
                        new Integer(m_instance.m_runLevel),
                        new Integer(level)));
                }
            }
            m_instance.m_runLevel = level;
        }
    }

}
