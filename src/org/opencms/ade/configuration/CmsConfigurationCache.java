/*
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsWaitHandle;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * This is the internal cache class used for storing configuration data. It is not public because it is only meant
 * for internal use.<p>
 * 
 * It stores an instance of {@link CmsADEConfigData} for each active configuration file in the sitemap,
 * and a single instance which represents the merged configuration from all the modules. When a sitemap configuration
 * file is updated, only the single instance for that configuration file is updated, whereas if a module configuration file
 * is changed, the configuration of all modules will be read again.<p>
 */
class CmsConfigurationCache implements I_CmsGlobalConfigurationCache {

    /** ID which is used to signal that the complete configuration should be reloaded. */
    public static final CmsUUID ID_UPDATE_ALL = CmsUUID.getConstantUUID("all");

    /** ID which is used to signal that the folder types should be updated. */
    public static final CmsUUID ID_UPDATE_FOLDERTYPES = CmsUUID.getConstantUUID("foldertypes");

    /** ID which is used to signal that the module configuration should be updated. */
    public static final CmsUUID ID_UPDATE_MODULES = CmsUUID.getNullUUID();

    /** The interval at which the tasks which checks for configuration updates runs, in milliseconds. */
    public static final int TASK_DELAY_MILLIS = 10 * 1000;

    /** Debug flag. */
    protected static boolean DEBUG = false;

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConfigurationCache.class);

    /** The resource type for sitemap configurations. */
    protected I_CmsResourceType m_configType;

    /** The resource type for module configurations. */
    protected I_CmsResourceType m_moduleConfigType;

    /** The CMS context used for reading configuration data. */
    private CmsObject m_cms;

    /** A cache which stores resources' paths by their structure IDs. */
    private ConcurrentHashMap<CmsUUID, String> m_pathCache = new ConcurrentHashMap<CmsUUID, String>();

    /** The current configuration state (immutable). */
    private volatile CmsADEConfigCacheState m_state;

    /** Scheduled future which is used to cancel the scheduled task. */
    private ScheduledFuture<?> m_taskFuture;

    /**
     *  A set of IDs which represent the configuration updates to perform. The IDs in this set
     * are either the structure IDs of sitemap configurations to reload, or special IDs which 
     * are not structure IDs but signal e.g. that the complete configuration should be reloaded. 
     */
    private CmsSynchronizedUpdateSet<CmsUUID> m_updateSet = new CmsSynchronizedUpdateSet<CmsUUID>();

    /** A wait handle which is used for waiting until the update task has run (e.g. for testing purposes). */
    private CmsWaitHandle m_waitHandle = new CmsWaitHandle();

    /** 
     * Creates a new cache instance.<p>
     * 
     * @param cms the CMS object used for reading the configuration data
     * @param configType the sitemap configuration file type 
     * @param moduleConfigType the module configuration file type 
     */
    public CmsConfigurationCache(CmsObject cms, I_CmsResourceType configType, I_CmsResourceType moduleConfigType) {

        m_cms = cms;
        m_configType = configType;
        m_moduleConfigType = moduleConfigType;
    }

    /** 
     * Gets the base path for a given sitemap configuration file.<p>
     * 
     * @param siteConfigFile the root path of the sitemap configuration file
     *  
     * @return the base path for the sitemap configuration file 
     */
    public static String getBasePath(String siteConfigFile) {

        if (siteConfigFile.endsWith(CmsADEManager.CONFIG_SUFFIX)) {
            return CmsResource.getParentFolder(CmsResource.getParentFolder(siteConfigFile));
        }
        return siteConfigFile;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#clear()
     */
    public void clear() {

        m_updateSet.add(ID_UPDATE_ALL);
    }

    /**
     * Looks up the root path for a given structure id.<p>
     *
     * This is used for correcting the paths of cached resource objects.<p>
     * 
     * @param structureId the structure id 
     * @return the root path for the structure id
     * 
     * @throws CmsException if the resource with the given id was not found or another error occurred 
     */
    public String getPathForStructureId(CmsUUID structureId) throws CmsException {

        String rootPath = m_pathCache.get(structureId);
        if (rootPath != null) {
            return rootPath;
        }
        CmsResource res = m_cms.readResource(structureId);
        m_pathCache.put(structureId, res.getRootPath());
        return res.getRootPath();
    }

    /** 
     * Gets the currently cached configuration state.<p>
     * 
     * @return the currently cached configuration state 
     */
    public CmsADEConfigCacheState getState() {

        return m_state;
    }

    /** 
     * Gets the wait handle which can be used to wait until the update task has run.<p>
     * 
     * @return the wait handle 
     */
    public CmsWaitHandle getWaitHandleForUpdateTask() {

        return m_waitHandle;
    }

    /**
     * Initializes the cache by reading in all the configuration files.<p>
     */
    public void initialize() {

        if (m_taskFuture != null) {
            // in case initialize has been called before on this object, cancel the existing task 
            m_taskFuture.cancel(false);
            m_taskFuture = null;
        }
        m_state = readCompleteConfiguration();
        // In debug mode, use a shorter delay to speed up the test cases 
        long delay = DEBUG ? 500 : TASK_DELAY_MILLIS;
        m_taskFuture = OpenCms.getExecutor().scheduleWithFixedDelay(new Runnable() {

            public void run() {

                performUpdate();
            }
        }, delay, delay, TimeUnit.MILLISECONDS);
    }

    /** 
     * Reads the complete configuration (sitemap and module configurations).<p>
     * 
     * @return an object representing the currently active configuration 
     */
    public CmsADEConfigCacheState readCompleteConfiguration() {

        long beginTime = System.currentTimeMillis();
        Map<CmsUUID, CmsADEConfigDataInternal> siteConfigurations = Maps.newHashMap();
        if (m_cms.existsResource("/")) {
            try {
                List<CmsResource> configFileCandidates = m_cms.readResources(
                    "/",
                    CmsResourceFilter.DEFAULT.addRequireType(m_configType.getTypeId()));
                for (CmsResource candidate : configFileCandidates) {
                    if (isSitemapConfiguration(candidate.getRootPath(), candidate.getTypeId())) {
                        try {
                            CmsConfigurationReader reader = new CmsConfigurationReader(m_cms);
                            String basePath = getBasePath(candidate.getRootPath());
                            CmsADEConfigDataInternal data = reader.parseSitemapConfiguration(basePath, candidate);
                            siteConfigurations.put(candidate.getStructureId(), data);
                        } catch (Exception e) {
                            LOG.error(
                                "Error processing sitemap configuration "
                                    + candidate.getRootPath()
                                    + ": "
                                    + e.getLocalizedMessage(),
                                e);
                        }

                    }
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        List<CmsADEConfigDataInternal> moduleConfigs = loadModuleConfiguration();
        CmsADEConfigCacheState result = new CmsADEConfigCacheState(m_cms, siteConfigurations, moduleConfigs);
        long endTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("readCompleteConfiguration took " + (endTime - beginTime) + "ms");
        }
        return result;

    }

    /**
     * Removes a published resource from the cache.<p>
     * 
     * @param res the published resource 
     */
    public void remove(CmsPublishedResource res) {

        remove(res.getStructureId(), res.getRootPath(), res.getType());
    }

    /**
     * Removes a resource from the cache.<p>
     * 
     * @param res the resource to remove 
     */
    public void remove(CmsResource res) {

        remove(res.getStructureId(), res.getRootPath(), res.getTypeId());
    }

    /**
     * Updates the cache entry for the given published resource.<p>
     * 
     * @param res a published resource
     */
    public void update(CmsPublishedResource res) {

        try {
            update(res.getStructureId(), res.getRootPath(), res.getType(), res.getState());
        } catch (CmsRuntimeException e) {
            // may happen during import of org.opencms.ade.configuration module
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /** 
     * Updates the cache entry for the given resource.<p>
     * 
     * @param res the resource for which the cache entry should be updated
     */
    public void update(CmsResource res) {

        try {
            update(res.getStructureId(), res.getRootPath(), res.getTypeId(), res.getState());
        } catch (CmsRuntimeException e) {
            // may happen during import of org.opencms.ade.configuration module
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Checks whether the given path/type combination belongs to a module configuration file.<p>
     * 
     * @param rootPath the root path of the resource 
     * @param type the type id of the resource 
     * 
     * @return true if the path/type combination belongs to a module configuration 
     */
    protected boolean isModuleConfiguration(String rootPath, int type) {

        return type == m_moduleConfigType.getTypeId();
    }

    /** 
     * Returns true if this an online configuration cache.<p>
     * 
     * @return true if this is an online cache, false if it is an offline cache 
     */
    protected boolean isOnline() {

        return m_cms.getRequestContext().getCurrentProject().isOnlineProject();
    }

    /**
     * Checks whether the given path/type combination belongs to a sitemap configuration.<p> 
     * 
     * @param rootPath the root path 
     * @param type the resource type id 
     * 
     * @return true if the path/type belong to an active sitemap configuration 
     */
    protected boolean isSitemapConfiguration(String rootPath, int type) {

        return rootPath.endsWith(CmsADEManager.CONFIG_SUFFIX) && (type == m_configType.getTypeId());
    }

    /** 
     * Loads a list of module configurations from the VFS.<p>
     * 
     * @return the module configurations 
     */
    protected List<CmsADEConfigDataInternal> loadModuleConfiguration() {

        if (m_cms.existsResource("/")) {
            CmsConfigurationReader reader = new CmsConfigurationReader(m_cms);
            List<CmsADEConfigDataInternal> moduleConfigs = reader.readModuleConfigurations();
            return moduleConfigs;
        } else {
            return Collections.emptyList();
        }
    }

    /** 
     * Checks if any configuration updates are required, and performs them if necessary.<p>
     * 
     * This should only be called from the scheduled update task.<p>
     */
    protected void performUpdate() {

        // Wrap a try-catch around everything, because an escaping exception would cancel the task from which this is called 
        try {
            Set<CmsUUID> updateIds = m_updateSet.removeAll();
            CmsADEConfigCacheState oldState = m_state;
            if (!updateIds.isEmpty() || (oldState == null)) {
                try {
                    // Although  the updates are performed in a scheduled task, it is still possible 
                    // that the task is scheduled immediately after a configuration update event. So
                    // here we ensure that there is at least a small delay between the event and the 
                    // actual update. This is required to prevent problems with other caches. 
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignore 
                }
                if (updateIds.contains(ID_UPDATE_ALL) || (oldState == null)) {
                    m_state = readCompleteConfiguration();
                } else {
                    boolean updateModules = updateIds.remove(ID_UPDATE_MODULES);
                    updateIds.remove(ID_UPDATE_FOLDERTYPES); // folder types are always updated when the update set is not empty, so at this point we don't care whether the id for folder type updates actually is in the update set 
                    Map<CmsUUID, CmsADEConfigDataInternal> updateMap = Maps.newHashMap();
                    for (CmsUUID structureId : updateIds) {
                        CmsADEConfigDataInternal sitemapConfig = parseSitemapConfiguration(structureId);
                        // sitemapConfig may be null at this point
                        updateMap.put(structureId, sitemapConfig);
                    }
                    List<CmsADEConfigDataInternal> moduleConfigs = null;
                    if (updateModules) {
                        moduleConfigs = loadModuleConfiguration();
                    }
                    m_state = oldState.createUpdatedCopy(updateMap, moduleConfigs);
                }
            }
        } catch (Exception e) {
            LOG.error("Could not perform configuration cache update: " + e.getMessage(), e);
        }
        m_waitHandle.release();
    }

    /**
     * Removes the cache entry for the given resource data.<p>
     * 
     * @param structureId the resource structure id 
     * @param rootPath the resource root path 
     * @param type the resource type 
     */
    protected void remove(CmsUUID structureId, String rootPath, int type) {

        if (CmsResource.isTemporaryFileName(rootPath)) {
            return;
        }
        m_pathCache.remove(structureId);
        if (isSitemapConfiguration(rootPath, type)) {
            m_updateSet.add(structureId);
        } else if (isModuleConfiguration(rootPath, type)) {
            m_updateSet.add(ID_UPDATE_MODULES);
        } else if (m_state.getFolderTypes().containsKey(rootPath)) {
            m_updateSet.add(ID_UPDATE_FOLDERTYPES);
        }
    }

    /**
     * Updates the cache entry for the given resource data.<p>
     * 
     * @param structureId the structure id of the resource  
     * @param rootPath the root path of the resource 
     * @param type the type id of the resource 
     * @param resState the state of the resource 
     */
    protected void update(CmsUUID structureId, String rootPath, int type, CmsResourceState resState) {

        if (CmsResource.isTemporaryFileName(rootPath)) {
            return;
        }
        m_pathCache.replace(structureId, rootPath);
        if (isSitemapConfiguration(rootPath, type)) {
            m_updateSet.add(structureId);
        } else if (isModuleConfiguration(rootPath, type)) {
            LOG.info("Changed module configuration file " + rootPath + "(" + structureId + ")");
            m_updateSet.add(ID_UPDATE_MODULES);
        } else if (m_state.getFolderTypes().containsKey(rootPath)) {
            m_updateSet.add(ID_UPDATE_FOLDERTYPES);
        }
    }

    /** 
     * Parses a sitemap configuration from a resource given its structure id, and either returns 
     * the parsed sitemap configuration, or null if reading or parsing the resource fails or if 
     * the resource is not a valid sitemap configuration.<p>
     * 
     * @param id the structure id of a resource 
     * @return the sitemap configuration parsed from the resource, or null on failure 
     */
    CmsADEConfigDataInternal parseSitemapConfiguration(CmsUUID id) {

        try {
            CmsResource configResource = m_cms.readResource(id);
            // Path or type may have changed in the meantime, so need to check if it's still a sitemap configuration 
            if (isSitemapConfiguration(configResource.getRootPath(), configResource.getTypeId())) {
                CmsConfigurationReader reader = new CmsConfigurationReader(m_cms);
                String basePath = getBasePath(configResource.getRootPath());
                CmsADEConfigDataInternal result = reader.parseSitemapConfiguration(basePath, configResource);
                return result;
            } else {
                LOG.info("Not a valid sitemap configuration anymore: " + configResource.getRootPath());
                return null;
            }
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return null;

        }
    }

}
