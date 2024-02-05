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

package org.opencms.module;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsModuleConfiguration;
import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsImportExportManager;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Manages the modules of an OpenCms installation.<p>
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

    /** The import/export repository. */
    private CmsModuleImportExportRepository m_importExportRepository = new CmsModuleImportExportRepository();

    /** The list of module export points. */
    private Set<CmsExportPoint> m_moduleExportPoints;

    /** The map of configured modules. */
    private Map<String, CmsModule> m_modules;

    /** Whether incremental module updates are allowed (rather than deleting / reimporting the module). */
    private boolean m_moduleUpdateEnabled = true;

    /**
     * Basic constructor.<p>
     *
     * @param configuredModules the list of configured modules
     */
    public CmsModuleManager(List<CmsModule> configuredModules) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_MOD_MANAGER_CREATED_0));
        }

        m_modules = new Hashtable<String, CmsModule>();
        for (int i = 0; i < configuredModules.size(); i++) {
            CmsModule module = configuredModules.get(i);
            m_modules.put(module.getName(), module);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_MOD_CONFIGURED_1, module.getName()));
            }
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_NUM_MODS_CONFIGURED_1, Integer.valueOf(m_modules.size())));
        }
        m_moduleExportPoints = Collections.emptySet();
    }

    /**
     * Returns a map of dependencies.<p>
     *
     * The module dependencies are get from the installed modules or
     * from the module manifest.xml files found in the given FRS path.<p>
     *
     * Two types of dependency lists can be generated:<br>
     * <ul>
     *   <li>Forward dependency lists: a list of modules that depends on a module</li>
     *   <li>Backward dependency lists: a list of modules that a module depends on</li>
     * </ul>
     *
     * @param rfsAbsPath a RFS absolute path to search for modules, or <code>null</code> to use the installed modules
     * @param mode if <code>true</code> a list of forward dependency is build, is not a list of backward dependency
     *
     * @return a Map of module names as keys and a list of dependency names as values
     *
     * @throws CmsConfigurationException if something goes wrong
     */
    public static Map<String, List<String>> buildDepsForAllModules(String rfsAbsPath, boolean mode)
    throws CmsConfigurationException {

        Map<String, List<String>> ret = new HashMap<String, List<String>>();
        List<CmsModule> modules;
        if (rfsAbsPath == null) {
            modules = OpenCms.getModuleManager().getAllInstalledModules();
        } else {
            modules = new ArrayList<CmsModule>(getAllModulesFromPath(rfsAbsPath).keySet());
        }
        Iterator<CmsModule> itMods = modules.iterator();
        while (itMods.hasNext()) {
            CmsModule module = itMods.next();

            // if module a depends on module b, and module c depends also on module b:
            // build a map with a list containing "a" and "c" keyed by "b" to get a
            // list of modules depending on module "b"...
            Iterator<CmsModuleDependency> itDeps = module.getDependencies().iterator();
            while (itDeps.hasNext()) {
                CmsModuleDependency dependency = itDeps.next();
                // module dependency package name
                String moduleDependencyName = dependency.getName();

                if (mode) {
                    // get the list of dependent modules
                    List<String> moduleDependencies = ret.get(moduleDependencyName);
                    if (moduleDependencies == null) {
                        // build a new list if "b" has no dependent modules yet
                        moduleDependencies = new ArrayList<String>();
                        ret.put(moduleDependencyName, moduleDependencies);
                    }
                    // add "a" as a module depending on "b"
                    moduleDependencies.add(module.getName());
                } else {
                    List<String> moduleDependencies = ret.get(module.getName());
                    if (moduleDependencies == null) {
                        moduleDependencies = new ArrayList<String>();
                        ret.put(module.getName(), moduleDependencies);
                    }
                    moduleDependencies.add(dependency.getName());
                }
            }
        }
        itMods = modules.iterator();
        while (itMods.hasNext()) {
            CmsModule module = itMods.next();
            if (ret.get(module.getName()) == null) {
                ret.put(module.getName(), new ArrayList<String>());
            }
        }
        return ret;
    }

    /**
     * Returns a map of dependencies between the given modules.<p>
     *
     * The module dependencies are get from the installed modules or
     * from the module manifest.xml files found in the given FRS path.<p>
     *
     * Two types of dependency lists can be generated:<br>
     * <ul>
     *   <li>Forward dependency lists: a list of modules that depends on a module</li>
     *   <li>Backward dependency lists: a list of modules that a module depends on</li>
     * </ul>
     *
     * @param moduleNames a list of module names
     * @param rfsAbsPath a RFS absolute path to search for modules, or <code>null</code> to use the installed modules
     * @param mode if <code>true</code> a list of forward dependency is build, is not a list of backward dependency
     *
     * @return a Map of module names as keys and a list of dependency names as values
     *
     * @throws CmsConfigurationException if something goes wrong
     */
    public static Map<String, List<String>> buildDepsForModulelist(
        List<String> moduleNames,
        String rfsAbsPath,
        boolean mode)
    throws CmsConfigurationException {

        Map<String, List<String>> ret = buildDepsForAllModules(rfsAbsPath, mode);
        Iterator<CmsModule> itMods;
        if (rfsAbsPath == null) {
            itMods = OpenCms.getModuleManager().getAllInstalledModules().iterator();
        } else {
            itMods = getAllModulesFromPath(rfsAbsPath).keySet().iterator();
        }
        while (itMods.hasNext()) {
            CmsModule module = itMods.next();
            if (!moduleNames.contains(module.getName())) {
                Iterator<List<String>> itDeps = ret.values().iterator();
                while (itDeps.hasNext()) {
                    List<String> dependencies = itDeps.next();
                    dependencies.remove(module.getName());
                }
                ret.remove(module.getName());
            }
        }
        return ret;
    }

    /**
     * Returns a map of modules found in the given RFS absolute path.<p>
     *
     * @param rfsAbsPath the path to look for module distributions
     *
     * @return a map of <code>{@link CmsModule}</code> objects for keys and filename for values
     *
     * @throws CmsConfigurationException if something goes wrong
     */
    public static Map<CmsModule, String> getAllModulesFromPath(String rfsAbsPath) throws CmsConfigurationException {

        Map<CmsModule, String> modules = new HashMap<CmsModule, String>();
        if (rfsAbsPath == null) {
            return modules;
        }
        File folder = new File(rfsAbsPath);
        if (folder.exists()) {
            // list all child resources in the given folder
            File[] folderFiles = folder.listFiles();
            if (folderFiles != null) {
                for (int i = 0; i < folderFiles.length; i++) {
                    File moduleFile = folderFiles[i];
                    if (moduleFile.isFile() && !(moduleFile.getAbsolutePath().toLowerCase().endsWith(".zip"))) {
                        // skip non-ZIP files
                        continue;
                    }
                    if (moduleFile.isDirectory()) {
                        File manifest = new File(moduleFile, CmsImportExportManager.EXPORT_MANIFEST);
                        if (!manifest.exists() || !manifest.canRead()) {
                            // skip unused directories
                            continue;
                        }
                    }
                    modules.put(
                        CmsModuleImportExportHandler.readModuleFromImport(moduleFile.getAbsolutePath()),
                        moduleFile.getName());
                }
            }
        }
        return modules;
    }

    /**
     * Sorts a given list of module names by dependencies,
     * so that the resulting list can be imported in that given order,
     * that means modules without dependencies first.<p>
     *
     * The module dependencies are get from the installed modules or
     * from the module manifest.xml files found in the given FRS path.<p>
     *
     * @param moduleNames a list of module names
     * @param rfsAbsPath a RFS absolute path to search for modules, or <code>null</code> to use the installed modules
     *
     * @return a sorted list of module names
     *
     * @throws CmsConfigurationException if something goes wrong
     */
    public static List<String> topologicalSort(List<String> moduleNames, String rfsAbsPath)
    throws CmsConfigurationException {

        List<String> modules = new ArrayList<String>(moduleNames);
        List<String> retList = new ArrayList<String>();
        Map<String, List<String>> moduleDependencies = buildDepsForModulelist(moduleNames, rfsAbsPath, true);
        boolean finished = false;
        while (!finished) {
            finished = true;
            Iterator<String> itMods = modules.iterator();
            while (itMods.hasNext()) {
                String moduleName = itMods.next();
                List<String> deps = moduleDependencies.get(moduleName);
                if ((deps == null) || deps.isEmpty()) {
                    retList.add(moduleName);
                    Iterator<List<String>> itDeps = moduleDependencies.values().iterator();
                    while (itDeps.hasNext()) {
                        List<String> dependencies = itDeps.next();
                        dependencies.remove(moduleName);
                    }
                    finished = false;
                    itMods.remove();
                }
            }
        }
        if (!modules.isEmpty()) {
            throw new CmsIllegalStateException(
                Messages.get().container(Messages.ERR_MODULE_DEPENDENCY_CYCLE_1, modules.toString()));
        }
        Collections.reverse(retList);
        return retList;
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
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        if (m_modules.containsKey(module.getName())) {
            // module is currently configured, no create possible
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_MODULE_ALREADY_CONFIGURED_1, module.getName()));

        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_CREATE_NEW_MOD_1, module.getName()));
        }

        // initialize the module
        module.initialize(cms);

        m_modules.put(module.getName(), module);

        try {
            I_CmsModuleAction moduleAction = module.getActionInstance();
            String className = module.getActionClass();
            if ((moduleAction == null) && (className != null)) {
                Class<?> actionClass = Class.forName(className, false, getClass().getClassLoader());
                if (I_CmsModuleAction.class.isAssignableFrom(actionClass)) {
                    moduleAction = ((Class<? extends I_CmsModuleAction>)actionClass).newInstance();
                    module.setActionInstance(moduleAction);
                }
            }
            // handle module action instance if initialized
            if (moduleAction != null) {

                moduleAction.moduleUpdate(module);
            }
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_MOD_UPDATE_ERR_1, module.getName()), t);
        }

        // initialize the export points
        initModuleExportPoints();

        // update the configuration
        updateModuleConfiguration();

        // reinit the workplace CSS URIs
        if (!module.getParameters().isEmpty()) {
            OpenCms.getWorkplaceAppManager().initWorkplaceCssUris(this);
        }
    }

    /**
     * Checks if a modules dependencies are fulfilled.<p>
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
    public List<CmsModuleDependency> checkDependencies(CmsModule module, int mode) {

        List<CmsModuleDependency> result = new ArrayList<CmsModuleDependency>();

        if (mode == DEPENDENCY_MODE_DELETE) {
            // delete mode, check if other modules depend on this module
            Iterator<CmsModule> i = m_modules.values().iterator();
            while (i.hasNext()) {
                CmsModule otherModule = i.next();
                CmsModuleDependency dependency = otherModule.checkDependency(module);
                if (dependency != null) {
                    // dependency found, add to list
                    result.add(new CmsModuleDependency(otherModule.getName(), otherModule.getVersion()));
                }
            }

        } else if (mode == DEPENDENCY_MODE_IMPORT) {
            // import mode, check if all module dependencies are fulfilled
            Iterator<CmsModule> i = m_modules.values().iterator();
            // add all dependencies that must be found
            result.addAll(module.getDependencies());
            while (i.hasNext() && (result.size() > 0)) {
                CmsModule otherModule = i.next();
                CmsModuleDependency dependency = module.checkDependency(otherModule);
                if (dependency != null) {
                    // dependency found, remove from list
                    result.remove(dependency);
                }
            }
        } else {
            // invalid mode selected
            throw new CmsRuntimeException(
                Messages.get().container(Messages.ERR_CHECK_DEPENDENCY_INVALID_MODE_1, Integer.valueOf(mode)));
        }

        return result;
    }

    /**
     * Checks the module selection list for consistency, that means
     * that if a module is selected, all its dependencies are also selected.<p>
     *
     * The module dependencies are get from the installed modules or
     * from the module manifest.xml files found in the given FRS path.<p>
     *
     * @param moduleNames a list of module names
     * @param rfsAbsPath a RFS absolute path to search for modules, or <code>null</code> to use the installed modules
     * @param forDeletion there are two modes, one for installation of modules, and one for deletion.
     *
     * @throws CmsIllegalArgumentException if the module list is not consistent
     * @throws CmsConfigurationException if something goes wrong
     */
    public void checkModuleSelectionList(List<String> moduleNames, String rfsAbsPath, boolean forDeletion)
    throws CmsIllegalArgumentException, CmsConfigurationException {

        Map<String, List<String>> moduleDependencies = buildDepsForAllModules(rfsAbsPath, forDeletion);
        Iterator<String> itMods = moduleNames.iterator();
        while (itMods.hasNext()) {
            String moduleName = itMods.next();
            List<String> dependencies = moduleDependencies.get(moduleName);
            if (dependencies != null) {
                List<String> depModules = new ArrayList<String>(dependencies);
                depModules.removeAll(moduleNames);
                if (!depModules.isEmpty()) {
                    throw new CmsIllegalArgumentException(
                        Messages.get().container(
                            Messages.ERR_MODULE_SELECTION_INCONSISTENT_2,
                            moduleName,
                            depModules.toString()));
                }
            }
        }
    }

    /**
     * Deletes a module from the configuration.<p>
     *
     * @param cms must be initialized with "Admin" permissions
     * @param moduleName the name of the module to delete
     * @param replace indicates if the module is replaced (true) or finally deleted (false)
     * @param preserveLibs <code>true</code> to keep any exported file exported into the WEB-INF lib folder
     * @param report the report to print progress messages to
     *
     * @throws CmsRoleViolationException if the required module manager role permissions are not available
     * @throws CmsConfigurationException if a module with this name is not available for deleting
     * @throws CmsLockException if the module resources can not be locked
     */
    public synchronized void deleteModule(
        CmsObject cms,
        String moduleName,
        boolean replace,
        boolean preserveLibs,
        I_CmsReport report)
    throws CmsRoleViolationException, CmsConfigurationException, CmsLockException {

        // check for module manager role permissions
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        if (!m_modules.containsKey(moduleName)) {
            // module is not currently configured, no update possible
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_MODULE_NOT_CONFIGURED_1, moduleName));
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_DEL_MOD_1, moduleName));
        }

        CmsModule module = m_modules.get(moduleName);
        String importSite = module.getSite();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(importSite)) {
            CmsObject newCms;
            try {
                newCms = OpenCms.initCmsObject(cms);
                newCms.getRequestContext().setSiteRoot(importSite);
                cms = newCms;
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        if (!replace) {
            // module is deleted, not replaced

            // perform dependency check
            List<CmsModuleDependency> dependencies = checkDependencies(module, DEPENDENCY_MODE_DELETE);
            if (!dependencies.isEmpty()) {
                StringBuffer message = new StringBuffer();
                Iterator<CmsModuleDependency> it = dependencies.iterator();
                while (it.hasNext()) {
                    message.append("  ").append(it.next().getName()).append("\r\n");
                }
                throw new CmsConfigurationException(
                    Messages.get().container(Messages.ERR_MOD_DEPENDENCIES_2, moduleName, message.toString()));
            }
            try {
                I_CmsModuleAction moduleAction = module.getActionInstance();
                // handle module action instance if initialized
                if (moduleAction != null) {
                    moduleAction.moduleUninstall(module);
                }
            } catch (Throwable t) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_MOD_UNINSTALL_ERR_1, moduleName), t);
                report.println(
                    Messages.get().container(Messages.LOG_MOD_UNINSTALL_ERR_1, moduleName),
                    I_CmsReport.FORMAT_WARNING);
            }
        }

        boolean removeResourceTypes = !module.getResourceTypes().isEmpty();
        if (removeResourceTypes) {
            // mark the resource manager to reinitialize if necessary
            OpenCms.getWorkplaceManager().removeExplorerTypeSettings(module);
        }

        CmsProject previousProject = cms.getRequestContext().getCurrentProject();
        // try to create a new offline project for deletion
        CmsProject deleteProject = null;
        try {
            // try to read a (leftover) module delete project
            deleteProject = cms.readProject(
                Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                    Messages.GUI_DELETE_MODULE_PROJECT_NAME_1,
                    new Object[] {moduleName}));
        } catch (CmsException e) {
            try {
                // create a Project to delete the module
                deleteProject = cms.createProject(
                    Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                        Messages.GUI_DELETE_MODULE_PROJECT_NAME_1,
                        new Object[] {moduleName}),
                    Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                        Messages.GUI_DELETE_MODULE_PROJECT_DESC_1,
                        new Object[] {moduleName}),
                    OpenCms.getDefaultUsers().getGroupAdministrators(),
                    OpenCms.getDefaultUsers().getGroupAdministrators(),
                    CmsProject.PROJECT_TYPE_TEMPORARY);
            } catch (CmsException e1) {
                throw new CmsConfigurationException(e1.getMessageContainer(), e1);
            }
        }

        try {
            cms.getRequestContext().setCurrentProject(deleteProject);

            // check locks
            List<String> lockedResources = new ArrayList<String>();
            CmsLockFilter filter1 = CmsLockFilter.FILTER_ALL.filterNotLockableByUser(
                cms.getRequestContext().getCurrentUser());
            CmsLockFilter filter2 = CmsLockFilter.FILTER_INHERITED;
            List<String> moduleResources = module.getResources();
            for (int iLock = 0; iLock < moduleResources.size(); iLock++) {
                String resourceName = moduleResources.get(iLock);
                try {
                    lockedResources.addAll(cms.getLockedResources(resourceName, filter1));
                    lockedResources.addAll(cms.getLockedResources(resourceName, filter2));
                } catch (CmsException e) {
                    // may happen if the resource has already been deleted
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getMessageContainer(), e);
                    }
                    report.println(e.getMessageContainer(), I_CmsReport.FORMAT_WARNING);
                }
            }
            if (!lockedResources.isEmpty()) {
                CmsMessageContainer msg = Messages.get().container(
                    Messages.ERR_DELETE_MODULE_CHECK_LOCKS_2,
                    moduleName,
                    CmsStringUtil.collectionAsString(lockedResources, ","));
                report.addError(msg.key(cms.getRequestContext().getLocale()));
                report.println(msg);
                cms.getRequestContext().setCurrentProject(previousProject);
                try {
                    cms.deleteProject(deleteProject.getUuid());
                } catch (CmsException e1) {
                    throw new CmsConfigurationException(e1.getMessageContainer(), e1);
                }
                throw new CmsLockException(msg);
            }
        } finally {
            cms.getRequestContext().setCurrentProject(previousProject);
        }

        // now remove the module
        module = m_modules.remove(moduleName);

        if (preserveLibs) {
            // to preserve the module libs, remove the responsible export points, before deleting module resources
            Set<CmsExportPoint> exportPoints = new HashSet<CmsExportPoint>(m_moduleExportPoints);
            Iterator<CmsExportPoint> it = exportPoints.iterator();
            while (it.hasNext()) {
                CmsExportPoint point = it.next();
                if ((point.getUri().endsWith(module.getName() + "/lib/")
                    || point.getUri().endsWith(module.getName() + "/lib"))
                    && point.getConfiguredDestination().equals("WEB-INF/lib/")) {
                    it.remove();
                }
            }

            m_moduleExportPoints = Collections.unmodifiableSet(exportPoints);
        }

        try {
            cms.getRequestContext().setCurrentProject(deleteProject);

            // copy the module resources to the project
            List<CmsResource> moduleResources = CmsModule.calculateModuleResources(cms, module);
            for (CmsResource resource : moduleResources) {
                try {
                    cms.copyResourceToProject(resource);
                } catch (CmsException e) {
                    // may happen if the resource has already been deleted
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_MOVE_RESOURCE_FAILED_1,
                                cms.getSitePath(resource)));
                    }
                    report.println(e.getMessageContainer(), I_CmsReport.FORMAT_WARNING);
                }
            }

            report.print(Messages.get().container(Messages.RPT_DELETE_MODULE_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
            report.println(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_HTML_ITAG_1,
                    moduleName),
                I_CmsReport.FORMAT_HEADLINE);

            // move through all module resources and delete them
            for (CmsResource resource : moduleResources) {
                String sitePath = cms.getSitePath(resource);
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEL_MOD_RESOURCE_1, sitePath));
                    }
                    CmsLock lock = cms.getLock(resource);
                    if (lock.isUnlocked()) {
                        // lock the resource
                        cms.lockResource(resource);
                    } else if (lock.isLockableBy(cms.getRequestContext().getCurrentUser())) {
                        // steal the resource
                        cms.changeLock(resource);
                    }
                    if (!resource.getState().isDeleted()) {
                        // delete the resource
                        cms.deleteResource(sitePath, CmsResource.DELETE_PRESERVE_SIBLINGS);
                    }
                    // update the report
                    report.print(Messages.get().container(Messages.RPT_DELETE_0), I_CmsReport.FORMAT_NOTE);
                    report.println(
                        org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1,
                            sitePath));
                    if (!resource.getState().isNew()) {
                        // unlock the resource (so it gets deleted with next publish)
                        cms.unlockResource(resource);
                    }
                } catch (CmsException e) {
                    // ignore the exception and delete the next resource
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_DEL_MOD_EXC_1, sitePath), e);
                    report.println(e.getMessageContainer(), I_CmsReport.FORMAT_WARNING);
                }
            }

            if (moduleResources.size() > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_PROJECT_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
                // now unlock and publish the project
                cms.unlockProject(deleteProject.getUuid());
                OpenCms.getPublishManager().publishProject(cms, report);
                OpenCms.getPublishManager().waitWhileRunning();
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_PROJECT_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
                report.println(Messages.get().container(Messages.RPT_DELETE_MODULE_END_0), I_CmsReport.FORMAT_HEADLINE);
            }

        } catch (CmsException e) {
            throw new CmsConfigurationException(e.getMessageContainer(), e);
        } finally {
            cms.getRequestContext().setCurrentProject(previousProject);
        }

        // initialize the export points (removes export points from deleted module)
        initModuleExportPoints();

        // update the configuration
        updateModuleConfiguration();

        // reinit the manager is necessary
        if (removeResourceTypes) {
            OpenCms.getResourceManager().initialize(cms);
        }

        // reinit the workplace CSS URIs
        if (!module.getParameters().isEmpty()) {
            OpenCms.getWorkplaceAppManager().initWorkplaceCssUris(this);
        }
    }

    /**
     * Deletes a module from the configuration.<p>
     *
     * @param cms must be initialized with "Admin" permissions
     * @param moduleName the name of the module to delete
     * @param replace indicates if the module is replaced (true) or finally deleted (false)
     * @param report the report to print progress messages to
     *
     * @throws CmsRoleViolationException if the required module manager role permissions are not available
     * @throws CmsConfigurationException if a module with this name is not available for deleting
     * @throws CmsLockException if the module resources can not be locked
     */
    public synchronized void deleteModule(CmsObject cms, String moduleName, boolean replace, I_CmsReport report)
    throws CmsRoleViolationException, CmsConfigurationException, CmsLockException {

        deleteModule(cms, moduleName, replace, false, report);
    }

    /**
     * Returns a list of installed modules.<p>
     *
     * @return a list of <code>{@link CmsModule}</code> objects
     */
    public List<CmsModule> getAllInstalledModules() {

        return new ArrayList<CmsModule>(m_modules.values());
    }

    /**
     * Returns the (immutable) list of configured module export points.<p>
     *
     * @return the (immutable) list of configured module export points
     * @see CmsExportPoint
     */
    public Set<CmsExportPoint> getExportPoints() {

        return m_moduleExportPoints;
    }

    /**
     * Returns the importExportRepository.<p>
     *
     * @return the importExportRepository
     */
    public CmsModuleImportExportRepository getImportExportRepository() {

        return m_importExportRepository;
    }

    /**
     * Returns the module with the given module name,
     * or <code>null</code> if no module with the given name is configured.<p>
     *
     * @param name the name of the module to return
     * @return the module with the given module name
     */
    public CmsModule getModule(String name) {

        return m_modules.get(name);
    }

    /**
     * Returns the set of names of all the installed modules.<p>
     *
     * @return the set of names of all the installed modules
     */
    public Set<String> getModuleNames() {

        synchronized (m_modules) {
            return new HashSet<String>(m_modules.keySet());
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
            OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);
        }

        Iterator<String> it;
        int count = 0;
        it = m_modules.keySet().iterator();
        while (it.hasNext()) {
            // get the module description
            CmsModule module = m_modules.get(it.next());

            if (module.getActionClass() != null) {
                // create module instance class
                I_CmsModuleAction moduleAction = module.getActionInstance();
                if (module.getActionClass() != null) {
                    try {
                        moduleAction = (I_CmsModuleAction)Class.forName(module.getActionClass()).newInstance();
                    } catch (Exception e) {
                        CmsLog.INIT.info(
                            Messages.get().getBundle().key(Messages.INIT_CREATE_INSTANCE_FAILED_1, module.getName()),
                            e);
                    }
                }
                if (moduleAction != null) {
                    count++;
                    module.setActionInstance(moduleAction);
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(
                            Messages.get().getBundle().key(
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
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.LOG_INSTANCE_INIT_ERR_1,
                                moduleAction.getClass().getName()),
                            t);
                    }
                }
            }
        }

        // initialize the export points
        initModuleExportPoints();
        m_importExportRepository.initialize(cms);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_NUM_CLASSES_INITIALIZED_1, Integer.valueOf(count)));
        }
    }

    /**
     * Replaces an existing module with the one read from an import ZIP file.<p>
     *
     * If there is not already a module with the same name installed, then the module will just be imported normally.
     *
     * @param cms the CMS context
     * @param importFile the import file
     * @param report the report
     *
     * @return the module replacement status
     * @throws CmsException if something goes wrong
     */
    public CmsReplaceModuleInfo replaceModule(CmsObject cms, String importFile, I_CmsReport report)
    throws CmsException {

        CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(importFile);

        boolean hasModule = hasModule(module.getName());
        boolean usedNewUpdate = false;
        CmsUUID pauseId = OpenCms.getSearchManager().pauseOfflineIndexing();
        try {
            if (hasModule) {
                Optional<CmsModuleUpdater> optModuleUpdater;
                if (m_moduleUpdateEnabled) {
                    optModuleUpdater = CmsModuleUpdater.create(cms, importFile, report);
                } else {
                    optModuleUpdater = Optional.empty();
                }
                if (optModuleUpdater.isPresent()) {
                    usedNewUpdate = true;
                    optModuleUpdater.get().run();
                } else {
                    deleteModule(cms, module.getName(), true, report);
                    CmsImportParameters params = new CmsImportParameters(importFile, "/", true);
                    OpenCms.getImportExportManager().importData(cms, report, params);
                }

            } else {
                CmsImportParameters params = new CmsImportParameters(importFile, "/", true);
                OpenCms.getImportExportManager().importData(cms, report, params);
            }
        } finally {
            OpenCms.getSearchManager().resumeOfflineIndexing(pauseId);
        }
        return new CmsReplaceModuleInfo(module, usedNewUpdate);
    }

    /**
     * Enables / disables incremental module updates, for testing purposes.
     *
     * @param enabled if incremental module updating should be enabled
     */
    public void setModuleUpdateEnabled(boolean enabled) {

        m_moduleUpdateEnabled = enabled;
    }

    /**
     * Shuts down all module instance classes managed in this module manager.<p>
     */
    public synchronized void shutDown() {

        int count = 0;
        Iterator<String> it = getModuleNames().iterator();
        while (it.hasNext()) {
            String moduleName = it.next();
            // get the module
            CmsModule module = m_modules.get(moduleName);
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
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_SHUTDOWN_MOD_CLASS_1,
                        moduleAction.getClass().getName()));
            }
            try {
                // shut down the module
                moduleAction.shutDown(module);
            } catch (Throwable t) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_INSTANCE_SHUTDOWN_ERR_1,
                        moduleAction.getClass().getName()),
                    t);
            }
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_NUM_MOD_CLASSES_1, Integer.valueOf(count)));
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_1, this.getClass().getName()));
        }
    }

    /**
     * Updates a already configured module with new values.<p>
     *
     * @param cms must be initialized with "Admin" permissions
     * @param module the module to update
     *
     * @throws CmsRoleViolationException if the required module manager role permissions are not available
     * @throws CmsConfigurationException if a module with this name is not available for updating
     */
    public synchronized void updateModule(CmsObject cms, CmsModule module)
    throws CmsRoleViolationException, CmsConfigurationException {

        // check for module manager role permissions
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        CmsModule oldModule = m_modules.get(module.getName());

        if (oldModule == null) {
            // module is not currently configured, no update possible
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_OLD_MOD_ERR_1, module.getName()));
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_MOD_UPDATE_1, module.getName()));
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
            LOG.error(Messages.get().getBundle().key(Messages.LOG_INSTANCE_UPDATE_ERR_1, module.getName()), t);
        }

        // initialize the export points
        initModuleExportPoints();

        // update the configuration
        updateModuleConfiguration();

        // reinit the workplace CSS URIs
        if (!module.getParameters().isEmpty()) {
            OpenCms.getWorkplaceAppManager().initWorkplaceCssUris(this);
        }
    }

    /**
     * Updates the module configuration.<p>
     */
    public void updateModuleConfiguration() {

        OpenCms.writeConfiguration(CmsModuleConfiguration.class);
    }

    /**
     * Initializes the list of export points from all configured modules.<p>
     */
    private synchronized void initModuleExportPoints() {

        Set<CmsExportPoint> exportPoints = new HashSet<CmsExportPoint>();
        Iterator<CmsModule> i = m_modules.values().iterator();
        while (i.hasNext()) {
            CmsModule module = i.next();
            List<CmsExportPoint> moduleExportPoints = module.getExportPoints();
            for (int j = 0; j < moduleExportPoints.size(); j++) {
                CmsExportPoint point = moduleExportPoints.get(j);
                if (exportPoints.contains(point)) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_DUPLICATE_EXPORT_POINT_2,
                                point,
                                module.getName()));
                    }
                } else {
                    exportPoints.add(point);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(Messages.LOG_ADD_EXPORT_POINT_2, point, module.getName()));
                    }
                }
            }
        }
        m_moduleExportPoints = Collections.unmodifiableSet(exportPoints);
    }
}