/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/CmsModuleManager.java,v $
 * Date   : $Date: 2005/02/20 18:33:03 $
 * Version: $Revision: 1.10 $
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

package org.opencms.module;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsModuleConfiguration;
import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the modules of an OpenCms installation.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3.6
 */
public class CmsModuleManager {

    /** Indicates dependency check for module deletion. */
    public static final int C_DEPENDENCY_MODE_DELETE = 0;

    /** Indicates dependency check for module import. */
    public static final int C_DEPENDENCY_MODE_IMPORT = 1;

    /** The map of initialized module action instances. */
    private Map m_moduleActionInstances;

    /** The list of module export points. */
    private Set m_moduleExportPoints;

    /** The map of configured modules. */
    private Map m_modules;
   
    /**
     * Basic constructor.<p>
     * 
     * @param configuredModules the list of configured modules 
     */
    public CmsModuleManager(List configuredModules) {

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Module configuration : created module manager");
        }

        m_modules = new HashMap();
        for (int i = 0; i < configuredModules.size(); i++) {
            CmsModule module = (CmsModule)configuredModules.get(i);
            m_modules.put(module.getName(), module);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                    ". Module configuration : configured module " + module.getName());
            }
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Module configuration : " + m_modules.size() + " modules configured");
        }
        m_moduleActionInstances = new HashMap();
        m_moduleExportPoints = Collections.EMPTY_SET;
    }

    /**
     * Adds a new module to the module manager.<p>
     * 
     * @param adminCms must be initialized with "Admin" permissions 
     * @param module the module to add
     * 
     * @throws CmsSecurityException if the required permissions are not available (i.e. no "Admin" CmsObject has been provided)
     * @throws CmsConfigurationException if a module with this name is already configured 
     */
    public synchronized void addModule(CmsObject adminCms, CmsModule module)
    throws CmsSecurityException, CmsConfigurationException {

        // this operation requires admin permissions
        if ((adminCms == null) || (!adminCms.isAdmin())) {
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        if (m_modules.containsKey(module.getName())) {
            // module is currently configured, no create possible
            throw new CmsConfigurationException(CmsConfigurationException.C_CONFIGURATION_ERROR);
        }

        if (OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info("Creating new module '" + module.getName() + "'");
        }

        m_modules.put(module.getName(), module);
        
        try {
            I_CmsModuleAction moduleAction = (I_CmsModuleAction)m_moduleActionInstances.get(module.getName());
            // handle module action instance if initialized
            if (moduleAction != null) {
                moduleAction.moduleUpdate(module);
            }    
        } catch (Throwable t) {
            OpenCms.getLog(this).error("Error during module action instance update for module '" + module.getName() + "'", t);
        }        

        // initialize the export points
        initModuleExportPoints();
        
        // update the configuration
        updateModuleConfiguration();
    }

    /**
     * Checks if a modules depedencies are fulfilled.<p> 
     * 
     * The possible values for the <code>mode</code> parameter are:<dl>
     * <dt>{@link #C_DEPENDENCY_MODE_DELETE}</dt>
     *      <dd>Check for module deleting, i.e. are other modules dependent on the 
     *          given module?</dd>
     * <dt>{@link #C_DEPENDENCY_MODE_IMPORT}</dt>
     *      <dd>Check for module importing, i.e. are all dependencies required by the given
     *          module available?</dd></dl>
     * 
     * @param module the module to check the dependencies for
     * @param mode the dependency check mode
     * @return a list of dependencies that are not fulfilled, if empty all dependencies are fulfilled
     */
    public List checkDependencies(CmsModule module, int mode) {

        List result = new ArrayList();

        if (mode == C_DEPENDENCY_MODE_DELETE) {
            // delete mode, check if other modules depend on this module
            Iterator i = m_modules.values().iterator();
            while (i.hasNext()) {
                CmsModule otherModule = (CmsModule)i.next();
                CmsModuleDependency dependency = otherModule.checkDependency(module);
                if (dependency != null) {
                    // dependency found, add to list
                    result.add(new CmsModuleDependency(otherModule.getName(), otherModule.getVersion()));
                }
            }

        } else if (mode == C_DEPENDENCY_MODE_IMPORT) {
            // import mode, check if all module dependencies are fulfilled            
            Iterator i = m_modules.values().iterator();
            // add all dependencies that must be found
            result.addAll(module.getDependencies());
            while (i.hasNext() && (result.size() > 0)) {
                CmsModule otherModule = (CmsModule)i.next();
                CmsModuleDependency dependency = module.checkDependency(otherModule);
                if (dependency != null) {
                    // dependency found, remove from list
                    result.remove(dependency);
                }
            }
        } else {
            // invalid mode selected
            throw new RuntimeException("checkDependencies() invalid mode parameter: " + mode);
        }

        return result;
    }
    
    /**
     * Deletes a module from the configuration.<p>
     * 
     * @param adminCms must be initialized with "Admin" permissions 
     * @param moduleName the name of the module to delete
     * @param replace indicates if the module is replaced (true) or finally deleted (false)
     * @param report the report to print progesss messages to
     * 
     * @throws CmsSecurityException if the required permissions are not available (i.e. no "Admin" CmsObject has been provided)
     * @throws CmsConfigurationException if a module with this name is not available for deleting
     */
    public synchronized void deleteModule(CmsObject adminCms, String moduleName, boolean replace, I_CmsReport report)
    throws CmsSecurityException, CmsConfigurationException {

        // this operation requires admin permissions
        if ((adminCms == null) || (!adminCms.isAdmin())) {
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        if (!m_modules.containsKey(moduleName)) {
            // module is not currently configured, no update possible
            throw new CmsConfigurationException(CmsConfigurationException.C_CONFIGURATION_ERROR);
        }

        if (OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info("Deleting module '" + moduleName + "'");
        }

        CmsModule module;
        boolean removeResourceTypes = false;
        
        if (! replace) {
            // module is deleted, not replaced
            module = (CmsModule)m_modules.get(moduleName);    
            // makr the resource manager to reinitialize if nescessary
            if (module.getResourceTypes() != Collections.EMPTY_LIST) {
                removeResourceTypes = true;          
            }   
            if (module.getExplorerTypes() != Collections.EMPTY_LIST) {
                OpenCms.getWorkplaceManager().removeExplorerTypeSettings(module.getExplorerTypes());
            }
            
            // perform dependency check
            List dependencies = checkDependencies(module, C_DEPENDENCY_MODE_DELETE);
            if (! dependencies.isEmpty()) {
                throw new CmsConfigurationException(CmsConfigurationException.C_CONFIGURATION_MODULE_DEPENDENCIES);                
            }
            try {
                I_CmsModuleAction moduleAction = (I_CmsModuleAction)m_moduleActionInstances.get(moduleName);
                // handle module action instance if initialized
                if (moduleAction != null) {
                    moduleAction.moduleUninstall(module);
                    // remove instance from list of configured instances
                    m_moduleActionInstances.remove(moduleName);
                }    
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error during module action instance uninstall for module '" + moduleName + "'", t);
            }
        }


        
        // now remove the module
        module = (CmsModule)m_modules.remove(moduleName);
        
        // move through all module resources and delete them
        for (int i = 0; i < module.getResources().size(); i++) {
            String currentResource = null;
            try {
                currentResource = (String)module.getResources().get(i);
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Deleting module resource '" + currentResource + "'");
                }
                // lock the resource
                adminCms.lockResource(currentResource);
                // delete the resource
                adminCms.deleteResource(currentResource, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
                // update the report
                report.print(report.key("report.deleting"), I_CmsReport.C_FORMAT_NOTE);
                report.println(currentResource);
                // unlock the resource (so it gets deleted with next publish)
                adminCms.unlockResource(currentResource);                
            } catch (CmsException e) {
                // ignore the exception and delete the next resource
                OpenCms.getLog(this).error("Exception deleting module resource '" + currentResource + "'", e);
                report.println(e);
            }
        }

        // initialize the export points (removes export points from deleted module)
        initModuleExportPoints();
        
        // update the configuration
        updateModuleConfiguration();        
        
        // reinit the manager is nescessary
        if (removeResourceTypes) {
            OpenCms.getResourceManager().initialize(adminCms);
        }
    }
    
    /**
     * Returns the module aciton instance of the module with the given name, or <code>null</code>
     * if no module action instance with that name is configured.<p>
     * 
     * @param name the module name to get the action instance for
     * @return the module aciton instance of the module with the given name
     */
    public I_CmsModuleAction getActionInstance(String name) {
        
        return (I_CmsModuleAction)m_moduleActionInstances.get(name);
    }
    
    /**
     * Returns an iterator that iterates the initialized module action instances.<p>
     * 
     * @return  an iterator that iterates the initialized module action instances
     */
    public Iterator getActionInstances() {
        
        return new ArrayList(m_moduleActionInstances.values()).iterator();
    }

    /**
     * Returns the (immutable) list of configured module export points.<p>
     * 
     * @return the (immutable) list of configured module export points
     * @see CmsExportPoint
     */
    public Set getExportPoints() {

        return m_moduleExportPoints;
    }

    /**
     * Returns the module with the given module name,
     * or <code>null</code> if no module with the given name is configured.<p>
     * 
     * @param name the name of the module to return
     * @return the module with the given module name
     */
    public CmsModule getModule(String name) {

        return (CmsModule)m_modules.get(name);
    }

    /**
     * Returns the set of names of all the installed modules.<p>
     * 
     * @return the set of names of all the installed modules
     */
    public Set getModuleNames() {

        return m_modules.keySet();
    }

    /**
     * Checks if this module manager has a module with the given name installed.<p>
     * 
     * @param name the name of the module to check
     * @return true if this module manager has a module with the given name installed
     */
    public boolean hasModule(String name) {

        return m_modules.containsKey(name);
    }

    /**
     * Initializes all module instance classes managed in this module manager.<p>
     * 
     * @param adminCms an initialized CmsObject with "Admin" permissions
     * @param configurationManager the initialized OpenCms configuration manager
     */
    public synchronized void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager) {

        if (((adminCms == null) && (OpenCms.getRunLevel() > 1)) || ((adminCms != null) && !adminCms.isAdmin())) {
            // null admin cms only allowed during test cases
            throw new RuntimeException("Admin permissions are required to initialize the module manager");
        }
        
        Iterator it;
        
        it = m_modules.keySet().iterator();
        while (it.hasNext()) {
            // get the module description
            CmsModule module = (CmsModule)m_modules.get(it.next());

            if (module.getActionClass() != null) {
                // create module instance class
                I_CmsModuleAction moduleAction = null;
                try {
                    moduleAction = (I_CmsModuleAction)Class.forName(module.getActionClass()).newInstance();
                } catch (InstantiationException e) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                        ". Module configuration : could not create instance for module " + module.getName(),
                        e);
                } catch (IllegalAccessException e) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                        ". Module configuration : could not create instance for module " + module.getName(),
                        e);
                } catch (ClassNotFoundException e) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                        ". Module configuration : could not create instance for module " + module.getName(),
                        e);
                } catch (ClassCastException e) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                        ". Module configuration : could not create instance for module " + module.getName(),
                        e);
                }

                if (moduleAction != null) {
                    // store and initialize module action class    
                    m_moduleActionInstances.put(module.getName(), moduleAction);                    
                    if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                        OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                            ". Module configuration : initializing module class " + moduleAction.getClass().getName());
                    }
                    try {
                        // create a copy of the adminCms so that each module instance does have 
                        // it's own context, a shared context might introduce side - effects
                        CmsContextInfo contextInfo = 
                            new CmsContextInfo(
                                adminCms.getRequestContext().currentUser(),
                                adminCms.getRequestContext().currentProject(),
                                adminCms.getRequestContext().getUri(),
                                adminCms.getRequestContext().getSiteRoot(),
                                adminCms.getRequestContext().getLocale(),
                                adminCms.getRequestContext().getEncoding(),
                                adminCms.getRequestContext().getRemoteAddress());
                        CmsObject adminCmsCopy = OpenCms.initCmsObject(adminCms, contextInfo);
                        // initialize the module
                        moduleAction.initialize(adminCmsCopy, configurationManager, module);
                    } catch (Throwable t) {
                        OpenCms.getLog(this).error("Error during module action instance initialize for class '" + moduleAction.getClass().getName() + "'", t);
                    }                    
                }
            }
        }

        // initialize the export points
        initModuleExportPoints();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Module configuration : " + m_moduleActionInstances.size() + " module classes initialized");
        }
    }

    /**
     * Shuts down all module instance classes managed in this module manager.<p>
     */
    public synchronized void shutDown() {

        Iterator it = m_moduleActionInstances.keySet().iterator();
        while (it.hasNext()) {
            String moduleName = (String)it.next();
            // get the module
            CmsModule module = (CmsModule)m_modules.get(moduleName);
            // get the module action instance            
            I_CmsModuleAction moduleAction = (I_CmsModuleAction)m_moduleActionInstances.get(moduleName);

            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                    ". Module configuration : shutting down module class " + moduleAction.getClass().getName());
            }
            try {
                // shut down the module
                moduleAction.shutDown(module);
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Error during module action instance shutDown for class '" + moduleAction.getClass().getName() + "'", t);
            }                  
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Module configuration : " + m_moduleActionInstances.size() + " module classes have been shut down");
        }
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Shutting down        : " + this.getClass().getName() + " ... ok!");
        }        
    }

    /**
     * Updates a already configured module with new values.<p>
     * 
     * @param adminCms must be initialized with "Admin" permissions 
     * @param module the module to update
     * 
     * @throws CmsSecurityException if the required permissions are not available (i.e. no "Admin" CmsObject has been provided)
     * @throws CmsConfigurationException if a module with this name is not available for updateing 
     */
    public synchronized void updateModule(CmsObject adminCms, CmsModule module)
    throws CmsSecurityException, CmsConfigurationException {

        // this operation requires admin permissions
        if ((adminCms == null) || (!adminCms.isAdmin())) {
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }

        CmsModule oldModule = (CmsModule)m_modules.get(module.getName());
        
        if (oldModule == null) {
            // module is not currently configured, no update possible
            throw new CmsConfigurationException(CmsConfigurationException.C_CONFIGURATION_ERROR);
        }

        if (OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info("Updating module '" + module.getName() + "'");
        }
        
        if (oldModule.getVersion().compareTo(module.getVersion()) == 0) {
            // module version has not changed - auto increment version number
            module.getVersion().increment();
        }
        // indicate that the version number was recently updated
        module.getVersion().setUpdated(true);

        // replace old version of module with new version
        m_modules.put(module.getName(), module);

        try {
            I_CmsModuleAction moduleAction = (I_CmsModuleAction)m_moduleActionInstances.get(module.getName());
            // handle module action instance if initialized
            if (moduleAction != null) {
                moduleAction.moduleUpdate(module);
            }    
        } catch (Throwable t) {
            OpenCms.getLog(this).error("Error during module action instance update for module '" + module.getName() + "'", t);
        }  
        
        // initialize the export points
        initModuleExportPoints();
        
        // update the configuration
        updateModuleConfiguration();        
    }

    /**
     * Initializes the list of esport points from all configured modules.<p>
     */
    private synchronized void initModuleExportPoints() {

        Set exportPoints = new HashSet();
        Iterator i = m_modules.values().iterator();
        while (i.hasNext()) {
            CmsModule module = (CmsModule)i.next();
            List moduleExportPoints = module.getExportPoints();
            for (int j = 0; j < moduleExportPoints.size(); j++) {
                CmsExportPoint point = (CmsExportPoint)moduleExportPoints.get(j);
                if (exportPoints.contains(point)) {
                    if (OpenCms.getLog(this).isWarnEnabled()) {
                        OpenCms.getLog(this).warn(
                            "Duplicate export point '" + point + "' in module '" + module.getName() + "'");
                    }
                } else {
                    exportPoints.add(point);
                    if (OpenCms.getLog(this).isDebugEnabled()) {
                        OpenCms.getLog(this).debug(
                            "Adding export point '" + point + "' from module '" + module.getName() + "'");
                    }
                }
            }
        }
        m_moduleExportPoints = Collections.unmodifiableSet(exportPoints);
    }

    /**
     * Updates the module configuration.<p>
     */
    private void updateModuleConfiguration() {
        
        OpenCms.writeConfiguration(CmsModuleConfiguration.class);
    }
    
}