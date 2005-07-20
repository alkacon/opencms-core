/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/CmsModuleManager.java,v $
 * Date   : $Date: 2005/07/20 08:31:05 $
 * Version: $Revision: 1.32 $
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

package org.opencms.module;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsModuleConfiguration;
import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Manages the modules of an OpenCms installation.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.32 $ 
 * 
 * @since 6.0.0 
 */
public class CmsModuleManager {

    /** Indicates dependency check for module deletion. */
    public static final int DEPENDENCY_MODE_DELETE = 0;

    /** Indicates dependency check for module import. */
    public static final int DEPENDENCY_MODE_IMPORT = 1;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleManager.class);

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

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_MOD_MANAGER_CREATED_0));
        }

        m_modules = new Hashtable();
        for (int i = 0; i < configuredModules.size(); i++) {
            CmsModule module = (CmsModule)configuredModules.get(i);
            m_modules.put(module.getName(), module);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_MOD_CONFIGURED_1, module.getName()));
            }
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_NUM_MODS_CONFIGURED_1, new Integer(m_modules.size())));
        }
        m_moduleExportPoints = Collections.EMPTY_SET;
    }

    /**
     * Adds a new module to the module manager.<p>
     * 
     * @param cms must be initialized with "Admin" permissions 
     * @param module the module to add
     * 
     * @throws CmsSecurityException if the required permissions are not available (i.e. no "Admin" CmsObject has been provided)
     * @throws CmsConfigurationException if a module with this name is already configured 
     */
    public synchronized void addModule(CmsObject cms, CmsModule module)
    throws CmsSecurityException, CmsConfigurationException {

        // check the role permissions
        cms.checkRole(CmsRole.MODULE_MANAGER);

        if (m_modules.containsKey(module.getName())) {
            // module is currently configured, no create possible
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_MODULE_ALREADY_CONFIGURED_1,
                module.getName()));

        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_CREATE_NEW_MOD_1, module.getName()));
        }

        // initialize the module
        module.initialize(cms);

        m_modules.put(module.getName(), module);

        try {
            I_CmsModuleAction moduleAction = module.getActionInstance();
            // handle module action instance if initialized
            if (moduleAction != null) {
                moduleAction.moduleUpdate(module);
            }
        } catch (Throwable t) {
            LOG.error(Messages.get().key(Messages.LOG_MOD_UPDATE_ERR_1, module.getName()), t);
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
     * <dt>{@link #DEPENDENCY_MODE_DELETE}</dt>
     *      <dd>Check for module deleting, i.e. are other modules dependent on the 
     *          given module?</dd>
     * <dt>{@link #DEPENDENCY_MODE_IMPORT}</dt>
     *      <dd>Check for module importing, i.e. are all dependencies required by the given
     *          module available?</dd></dl>
     * 
     * @param module the module to check the dependencies for
     * @param mode the dependency check mode
     * @return a list of dependencies that are not fulfilled, if empty all dependencies are fulfilled
     */
    public List checkDependencies(CmsModule module, int mode) {

        List result = new ArrayList();

        if (mode == DEPENDENCY_MODE_DELETE) {
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

        } else if (mode == DEPENDENCY_MODE_IMPORT) {
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
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_CHECK_DEPENDENCY_INVALID_MODE_1,
                new Integer(mode)));
        }

        return result;
    }

    /**
     * Deletes a module from the configuration.<p>
     * 
     * @param cms must be initialized with "Admin" permissions 
     * @param moduleName the name of the module to delete
     * @param replace indicates if the module is replaced (true) or finally deleted (false)
     * @param report the report to print progesss messages to
     * 
     * @throws CmsRoleViolationException if the required module manager role permissions are not available 
     * @throws CmsConfigurationException if a module with this name is not available for deleting
     */
    public synchronized void deleteModule(CmsObject cms, String moduleName, boolean replace, I_CmsReport report)
    throws CmsRoleViolationException, CmsConfigurationException {

        // check for module manager role permissions
        cms.checkRole(CmsRole.MODULE_MANAGER);

        if (!m_modules.containsKey(moduleName)) {
            // module is not currently configured, no update possible
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_MODULE_NOT_CONFIGURED_1,
                moduleName));
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_DEL_MOD_1, moduleName));
        }

        CmsModule module;
        boolean removeResourceTypes = false;

        if (!replace) {
            // module is deleted, not replaced
            module = (CmsModule)m_modules.get(moduleName);
            // makr the resource manager to reinitialize if nescessary
            if (module.getResourceTypes() != Collections.EMPTY_LIST) {
                removeResourceTypes = true;
            }
            if (module.getExplorerTypes() != Collections.EMPTY_LIST) {
                OpenCms.getWorkplaceManager().removeExplorerTypeSettings(module);
            }

            // perform dependency check
            List dependencies = checkDependencies(module, DEPENDENCY_MODE_DELETE);
            if (!dependencies.isEmpty()) {
                StringBuffer message = new StringBuffer();
                Iterator it = dependencies.iterator();
                while (it.hasNext()) {
                    message.append("  ").append(((CmsModuleDependency)it.next()).getName()).append("\r\n");
                }
                throw new CmsConfigurationException(Messages.get().container(
                    Messages.ERR_MOD_DEPENDENCIES_2,
                    moduleName,
                    message.toString()));
            }
            try {
                I_CmsModuleAction moduleAction = module.getActionInstance();
                // handle module action instance if initialized
                if (moduleAction != null) {
                    moduleAction.moduleUninstall(module);
                }
            } catch (Throwable t) {
                LOG.error(Messages.get().key(Messages.LOG_MOD_UNINSTALL_ERR_1, moduleName), t);
            }
        }

        // now remove the module
        module = (CmsModule)m_modules.remove(moduleName);

        // move through all module resources and delete them
        for (int i = 0; i < module.getResources().size(); i++) {
            String currentResource = null;
            try {
                currentResource = (String)module.getResources().get(i);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_DEL_MOD_RESOURCE_1, currentResource));
                }
                // lock the resource
                cms.lockResource(currentResource);
                // delete the resource
                cms.deleteResource(currentResource, CmsResource.DELETE_PRESERVE_SIBLINGS);
                // update the report

                report.print(Messages.get().container(Messages.RPT_DELETE_0), I_CmsReport.FORMAT_NOTE);
                report.println(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    currentResource));
                // unlock the resource (so it gets deleted with next publish)
                cms.unlockResource(currentResource);
            } catch (CmsException e) {
                // ignore the exception and delete the next resource
                LOG.error(Messages.get().key(Messages.LOG_DEL_MOD_EXC_1, currentResource), e);
                report.println(e);
            }
        }

        // initialize the export points (removes export points from deleted module)
        initModuleExportPoints();

        // update the configuration
        updateModuleConfiguration();

        // reinit the manager is nescessary
        if (removeResourceTypes) {
            OpenCms.getResourceManager().initialize(cms);
        }
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

        synchronized (m_modules) {
            return new HashSet(m_modules.keySet());
        }
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
     * @param cms an initialized CmsObject with "manage modules" role permissions
     * @param configurationManager the initialized OpenCms configuration manager
     * 
     * @throws CmsRoleViolationException if the provided OpenCms context does not have "manage modules" role permissions
     */
    public synchronized void initialize(CmsObject cms, CmsConfigurationManager configurationManager)
    throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // certain test cases won't have an OpenCms context
            cms.checkRole(CmsRole.MODULE_MANAGER);
        }

        Iterator it;
        int count = 0;
        it = m_modules.keySet().iterator();
        while (it.hasNext()) {
            // get the module description
            CmsModule module = (CmsModule)m_modules.get(it.next());

            if (module.getActionClass() != null) {
                // create module instance class
                I_CmsModuleAction moduleAction = module.getActionInstance();
                if (module.getActionClass() != null) {
                    try {
                        moduleAction = (I_CmsModuleAction)Class.forName(module.getActionClass()).newInstance();
                    } catch (Exception e) {
                        CmsLog.INIT.info(
                            Messages.get().key(Messages.INIT_CREATE_INSTANCE_FAILED_1, module.getName()),
                            e);
                    }
                }
                if (moduleAction != null) {
                    count++;
                    module.setActionInstance(moduleAction);
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(Messages.get().key(
                            Messages.INIT_INITIALIZE_MOD_CLASS_1,
                            moduleAction.getClass().getName()));
                    }
                    try {
                        // create a copy of the adminCms so that each module instance does have 
                        // it's own context, a shared context might introduce side - effects
                        CmsObject adminCmsCopy = OpenCms.initCmsObject(cms);
                        // initialize the module
                        moduleAction.initialize(adminCmsCopy, configurationManager, module);
                    } catch (Throwable t) {
                        LOG.error(Messages.get().key(
                            Messages.LOG_INSTANCE_INIT_ERR_1,
                            moduleAction.getClass().getName()), t);
                    }
                }
            }
        }

        // initialize the export points
        initModuleExportPoints();

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_NUM_CLASSES_INITIALIZED_1, new Integer(count)));
        }
    }

    /**
     * Shuts down all module instance classes managed in this module manager.<p>
     */
    public synchronized void shutDown() {

        int count = 0;
        Iterator it = getModuleNames().iterator();
        while (it.hasNext()) {
            String moduleName = (String)it.next();
            // get the module
            CmsModule module = (CmsModule)m_modules.get(moduleName);
            if (module == null) {
                continue;
            }
            // get the module action instance            
            I_CmsModuleAction moduleAction = module.getActionInstance();
            if (moduleAction == null) {
                continue;
            }

            count++;
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(
                    Messages.INIT_SHUTDOWN_MOD_CLASS_1,
                    moduleAction.getClass().getName()));
            }
            try {
                // shut down the module
                moduleAction.shutDown(module);
            } catch (Throwable t) {
                LOG.error(
                    Messages.get().key(Messages.LOG_INSTANCE_SHUTDOWN_ERR_1, moduleAction.getClass().getName()),
                    t);
            }
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SHUTDOWN_NUM_MOD_CLASSES_1, new Integer(count)));
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SHUTDOWN_1, this.getClass().getName()));
        }
    }

    /**
     * Updates a already configured module with new values.<p>
     * 
     * @param cms must be initialized with "Admin" permissions 
     * @param module the module to update
     * 
     * @throws CmsRoleViolationException if the required module manager role permissions are not available 
     * @throws CmsConfigurationException if a module with this name is not available for updateing 
     */
    public synchronized void updateModule(CmsObject cms, CmsModule module)
    throws CmsRoleViolationException, CmsConfigurationException {

        // check for module manager role permissions
        cms.checkRole(CmsRole.MODULE_MANAGER);

        CmsModule oldModule = (CmsModule)m_modules.get(module.getName());

        if (oldModule == null) {
            // module is not currently configured, no update possible
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_OLD_MOD_ERR_1, module.getName()));
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_MOD_UPDATE_1, module.getName()));
        }

        if (oldModule.getVersion().compareTo(module.getVersion()) == 0) {
            // module version has not changed - auto increment version number
            module.getVersion().increment();
        }
        // indicate that the version number was recently updated
        module.getVersion().setUpdated(true);

        // initialize (freeze) the module
        module.initialize(cms);

        // replace old version of module with new version
        m_modules.put(module.getName(), module);

        try {
            I_CmsModuleAction moduleAction = oldModule.getActionInstance();
            // handle module action instance if initialized
            if (moduleAction != null) {
                moduleAction.moduleUpdate(module);
                // set the old action instance
                // the new action instance will be used after a system restart
                module.setActionInstance(moduleAction);
            }
        } catch (Throwable t) {
            LOG.error(Messages.get().key(Messages.LOG_INSTANCE_UPDATE_ERR_1, module.getName()), t);
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
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().key(Messages.LOG_DUPLICATE_EXPORT_POINT_2, point, module.getName()));
                    }
                } else {
                    exportPoints.add(point);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().key(Messages.LOG_ADD_EXPORT_POINT_2, point, module.getName()));
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