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

import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
import org.opencms.ade.configuration.plugins.CmsSitePlugin;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.db.generic.Messages;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsWaitHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

    /** ID to signal that plugins should be updated. */
    public static final CmsUUID ID_UPDATE_ATTRIBUTE_EDITOR_CONFIGURATIONS = CmsUUID.getConstantUUID(
        "attribute_editor_configurations");

    /** ID which is used to signal that the element views should be updated. */
    public static final CmsUUID ID_UPDATE_ELEMENT_VIEWS = CmsUUID.getConstantUUID("elementViews");

    /** ID which is used to signal that the folder types should be updated. */
    public static final CmsUUID ID_UPDATE_FOLDERTYPES = CmsUUID.getConstantUUID("foldertypes");

    /** ID which is used to signal that the module configuration should be updated. */
    public static final CmsUUID ID_UPDATE_MODULES = CmsUUID.getNullUUID();

    /** ID to signal that plugins should be updated. */
    public static final CmsUUID ID_UPDATE_SITE_PLUGINS = CmsUUID.getConstantUUID("site_plugins");

    /** The interval at which the tasks which checks for configuration updates runs, in milliseconds. */
    public static final int TASK_DELAY_MILLIS = 3 * 1000;

    /** Site plugin type name. */
    public static final String TYPE_SITE_PLUGIN = "site_plugin";

    /** The sitemap master config resource type name. */
    public static final String TYPE_SITEMAP_MASTER_CONFIG = "sitemap_master_config";

    /** Debug flag. */
    protected static boolean DEBUG;

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConfigurationCache.class);

    /** Resource type for attribute editor confiugrations. */
    private static final String TYPE_ATTRIBUTE_EDITOR_CONFIG = "attr_editor_config";

    /** The resource type for sitemap configurations. */
    protected I_CmsResourceType m_configType;

    /** The resource type for module configurations. */
    protected I_CmsResourceType m_moduleConfigType;

    /** The CMS context used for reading configuration data. */
    private CmsObject m_cms;

    /** Cache for keeping track of which pages are detail pages. */
    private LoadingCache<CmsResource, Boolean> m_detailPageIdCache = CacheBuilder.newBuilder().expireAfterWrite(
        60,
        TimeUnit.MINUTES).maximumSize(30000).concurrencyLevel(8).build(new CacheLoader<CmsResource, Boolean>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public Boolean load(CmsResource key) throws Exception {

                if (m_state == null) {
                    // this can only happen before the ADE manager is initialized
                    return Boolean.FALSE;
                }
                try {
                    return Boolean.valueOf(m_state.isDetailPage(m_cms, key));
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    return Boolean.FALSE;
                }
            }
        });

    /** The element view resource type. */
    private I_CmsResourceType m_elementViewType;

    /** A cache which stores resources' paths by their structure IDs. */
    private ConcurrentHashMap<CmsUUID, String> m_pathCache = new ConcurrentHashMap<CmsUUID, String>();

    /** The current configuration state (immutable). */
    private volatile CmsADEConfigCacheState m_state;

    /** Scheduled future which is used to cancel the scheduled task. */
    private ScheduledFuture<?> m_taskFuture;

    /** The work queue to keep of track of what needs to be done during the next cache update. */
    private LinkedBlockingQueue<Object> m_workQueue = new LinkedBlockingQueue<>();

    /**
     * Creates a new cache instance.<p>
     *
     * @param cms the CMS object used for reading the configuration data
     * @param configType the sitemap configuration file type
     * @param moduleConfigType the module configuration file type
     * @param elementViewType the element view resource type
     */
    public CmsConfigurationCache(
        CmsObject cms,
        I_CmsResourceType configType,
        I_CmsResourceType moduleConfigType,
        I_CmsResourceType elementViewType) {

        m_cms = cms;
        m_configType = configType;
        m_moduleConfigType = moduleConfigType;
        m_elementViewType = elementViewType;
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
        return null;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#clear()
     */
    public void clear() {

        m_workQueue.add(ID_UPDATE_ALL);
        m_detailPageIdCache.invalidateAll();
        m_pathCache.clear();
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

        String rootPath;
        if (structureId == null) {
            throw new CmsVfsResourceNotFoundException(
                Messages.get().container(Messages.ERR_READ_RESOURCE_WITH_ID_1, "null"));
        }
        rootPath = m_pathCache.get(structureId);
        if (rootPath != null) {
            return rootPath;
        }
        CmsResource res = m_cms.readResource(structureId);
        m_pathCache.put(structureId, res.getRootPath());
        return res.getRootPath();
    }

    /**
     * Gets the raw detail page information, with no existence checks or path corrections.
     *
     * @return the detail page information
     */
    public List<CmsDetailPageInfo> getRawDetailPages() {

        return m_state.getRawDetailPages();
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

        CmsWaitHandle handle = new CmsWaitHandle(true);
        m_workQueue.add(handle);
        return handle;
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
     * Checks if the given resource is a detail page.<p>
     * Delegates the actual work to the cache state, but also caches the result.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource to check
     * @return true if the given resource is a detail page
     */
    public boolean isDetailPage(CmsObject cms, CmsResource resource) {

        try {
            boolean result = m_detailPageIdCache.get(resource).booleanValue();
            if (!result) {
                // We want new detail pages to be available fast, so we don't cache negative results
                m_detailPageIdCache.invalidate(resource);
            }
            return result;
        } catch (ExecutionException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return true;
        }
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
                @SuppressWarnings("deprecation")
                List<CmsResource> configFileCandidates = m_cms.readResources(
                    "/",
                    CmsResourceFilter.DEFAULT.addRequireType(m_configType.getTypeId()));
                CmsLog.INIT.info(
                    ". Reading "
                        + configFileCandidates.size()
                        + " config resources of type: "
                        + m_configType.getTypeName()
                        + " from the "
                        + (m_cms.getRequestContext().getCurrentProject().isOnlineProject() ? "online" : "offline")
                        + " project.");
                if (OpenCms.getResourceManager().hasResourceType(TYPE_SITEMAP_MASTER_CONFIG)) {
                    List<CmsResource> masterCandidates = m_cms.readResources(
                        "/",
                        CmsResourceFilter.DEFAULT.addRequireType(
                            OpenCms.getResourceManager().getResourceType(TYPE_SITEMAP_MASTER_CONFIG)));
                    configFileCandidates.addAll(masterCandidates);
                }
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
        CmsLog.INIT.info(
            ". Reading "
                + (m_cms.getRequestContext().getCurrentProject().isOnlineProject() ? "online" : "offline")
                + " module configurations.");
        List<CmsADEConfigDataInternal> moduleConfigs = loadModuleConfiguration();
        CmsLog.INIT.info(
            ". Reading "
                + (m_cms.getRequestContext().getCurrentProject().isOnlineProject() ? "online" : "offline")
                + " element views.");
        Map<CmsUUID, CmsElementView> elementViews = loadElementViews();
        Map<CmsUUID, CmsSitePlugin> sitePlugins = loadSitePlugins();
        Map<CmsUUID, CmsSitemapAttributeEditorConfiguration> attributeEditorConfigs = loadAttributeEditorConfigurations();
        CmsADEConfigCacheState result = new CmsADEConfigCacheState(
            m_cms,
            siteConfigurations,
            moduleConfigs,
            elementViews,
            sitePlugins,
            attributeEditorConfigs);
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
            // may happen during import of org.opencms.base module
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
            // may happen during import of org.opencms.base module
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Checks whether the given type id matches the id of the macro formatter or flex formatter resource type.<p>
     * Also checks if the file is located in a '/.content/.formatters' folder.<p>
     *
     * @param type the type id
     * @param rootPath the root path
     *
     * @return <code>true</code> in case of a macro or flex formatter
     */
    protected boolean isMacroOrFlexFormatter(int type, String rootPath) {

        boolean result = false;
        try {
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(type);
            result = (CmsFormatterConfigurationCache.TYPE_MACRO_FORMATTER.equals(resType.getTypeName())
                || CmsFormatterConfigurationCache.TYPE_FLEX_FORMATTER.equals(resType.getTypeName()))
                && CmsResource.getParentFolder(rootPath).endsWith("/.content/.formatters");
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Checks whether the given path/type combination belongs to a module configuration file.<p>
     *
     * @param rootPath the root path of the resource
     * @param type the type id of the resource
     *
     * @return true if the path/type combination belongs to a module configuration
     */
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
    protected boolean isSitemapConfiguration(String rootPath, int type) {

        if (type == m_configType.getTypeId()) {
            return rootPath.endsWith(CmsADEManager.CONFIG_SUFFIX);
        } else {
            return OpenCms.getResourceManager().matchResourceType(TYPE_SITEMAP_MASTER_CONFIG, type);
        }
    }

    /**
     * Loads the available element views.<p>
     *
     * @return the element views
     */
    protected Map<CmsUUID, CmsElementView> loadElementViews() {

        List<CmsElementView> views = new ArrayList<CmsElementView>();
        if (m_cms.existsResource("/")) {
            views.add(CmsElementView.DEFAULT_ELEMENT_VIEW);
            try {
                @SuppressWarnings("deprecation")
                CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(
                    m_elementViewType.getTypeId());
                List<CmsResource> groups = m_cms.readResources("/", filter);
                for (CmsResource res : groups) {
                    try {
                        views.add(new CmsElementView(m_cms, res));
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            Collections.sort(views, new CmsElementView.ElementViewComparator());
            Map<CmsUUID, CmsElementView> elementViews = new LinkedHashMap<CmsUUID, CmsElementView>();
            for (CmsElementView view : views) {
                elementViews.put(view.getId(), view);
            }
            return elementViews;
        }
        return null;
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
        List<CmsWaitHandle> waitHandles = new ArrayList<>();
        try {
            ArrayList<Object> work = new ArrayList<>();
            m_workQueue.drainTo(work);
            Set<CmsUUID> updateIds = new HashSet<CmsUUID>();

            for (Object item : work) {
                if (item instanceof CmsUUID) {
                    updateIds.add((CmsUUID)item);
                } else if (item instanceof CmsWaitHandle) {
                    waitHandles.add((CmsWaitHandle)item);
                }
            }
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
                    boolean updateElementViews = updateIds.remove(ID_UPDATE_ELEMENT_VIEWS);
                    boolean updateSitePlugins = updateIds.remove(ID_UPDATE_SITE_PLUGINS);
                    boolean updateAttributeEditorConfigurations = updateIds.remove(
                        ID_UPDATE_ATTRIBUTE_EDITOR_CONFIGURATIONS);
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
                    Map<CmsUUID, CmsElementView> elementViews = null;
                    if (updateElementViews) {
                        elementViews = loadElementViews();
                    }
                    Map<CmsUUID, CmsSitePlugin> sitePlugins = null;
                    if (updateSitePlugins) {
                        sitePlugins = loadSitePlugins();
                    }

                    Map<CmsUUID, CmsSitemapAttributeEditorConfiguration> attributeEditorConfigurations = null;
                    if (updateAttributeEditorConfigurations) {
                        attributeEditorConfigurations = loadAttributeEditorConfigurations();
                    }
                    m_state = oldState.createUpdatedCopy(
                        updateMap,
                        moduleConfigs,
                        elementViews,
                        sitePlugins,
                        attributeEditorConfigurations);
                }
                try {
                    OpenCms.getADEManager().getCache().flushContainerPages(
                        m_cms.getRequestContext().getCurrentProject().isOnlineProject());
                } catch (Exception e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }

        } catch (Exception e) {
            LOG.error("Could not perform configuration cache update: " + e.getMessage(), e);
        } finally {
            for (CmsWaitHandle handle : waitHandles) {
                handle.release();
            }
        }
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
            m_workQueue.add(structureId);
        } else if (isModuleConfiguration(rootPath, type)) {
            m_workQueue.add(ID_UPDATE_MODULES);
        } else if (isElementView(type)) {
            m_workQueue.add(ID_UPDATE_ELEMENT_VIEWS);
        } else if (OpenCms.getResourceManager().matchResourceType(TYPE_SITE_PLUGIN, type)) {
            m_workQueue.add(ID_UPDATE_SITE_PLUGINS);
        } else if (OpenCms.getResourceManager().matchResourceType(TYPE_ATTRIBUTE_EDITOR_CONFIG, type)) {
            m_workQueue.add(ID_UPDATE_ATTRIBUTE_EDITOR_CONFIGURATIONS);
        } else if (m_state.getFolderTypes().containsKey(rootPath)) {
            m_workQueue.add(ID_UPDATE_FOLDERTYPES);
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
            m_workQueue.add(structureId);
        } else if (isModuleConfiguration(rootPath, type)) {
            LOG.info("Changed module configuration file " + rootPath + "(" + structureId + ")");
            m_workQueue.add(ID_UPDATE_MODULES);
        } else if (isElementView(type)) {
            m_workQueue.add(ID_UPDATE_ELEMENT_VIEWS);
        } else if (OpenCms.getResourceManager().matchResourceType(TYPE_SITE_PLUGIN, type)) {
            m_workQueue.add(ID_UPDATE_SITE_PLUGINS);
        } else if (OpenCms.getResourceManager().matchResourceType(TYPE_ATTRIBUTE_EDITOR_CONFIG, type)) {
            m_workQueue.add(ID_UPDATE_ATTRIBUTE_EDITOR_CONFIGURATIONS);
        } else if (m_state.getFolderTypes().containsKey(rootPath)) {
            m_workQueue.add(ID_UPDATE_FOLDERTYPES);
        } else if (isMacroOrFlexFormatter(type, rootPath)) {
            try {
                String path = CmsResource.getParentFolder(CmsResource.getParentFolder(rootPath));
                path = CmsStringUtil.joinPaths(path, ".config");
                CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION;
                if (m_cms.existsResource(path, filter)) {

                    CmsResource config = m_cms.readResource(path, filter);
                    m_workQueue.add(config.getStructureId());
                }
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
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
            if (e instanceof CmsVfsResourceNotFoundException) {
                LOG.info("Configuration file with ID " + id + " was not found.");
            } else {
                LOG.warn(e.getLocalizedMessage(), e);
            }
            return null;

        }
    }

    /**
     * Checks if the given type id is of the element view type.<p>
     *
     * @param type the type id to check
     *
     * @return <code>true</code> if the given type id is of the element view type
     */
    @SuppressWarnings("deprecation")
    private boolean isElementView(int type) {

        return type == m_elementViewType.getTypeId();
    }

    /**
     * Loads all sitemap attribute editor configurations.
     *
     * @return the map of all sitemap attribute editor configurations, with their structure IDs as keys
     */
    private Map<CmsUUID, CmsSitemapAttributeEditorConfiguration> loadAttributeEditorConfigurations() {

        Map<CmsUUID, CmsSitemapAttributeEditorConfiguration> result = new HashMap<>();
        if (OpenCms.getResourceManager().hasResourceType(TYPE_ATTRIBUTE_EDITOR_CONFIG) && m_cms.existsResource("/")) {
            try {
                @SuppressWarnings("deprecation")
                CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(
                    OpenCms.getResourceManager().getResourceType(TYPE_ATTRIBUTE_EDITOR_CONFIG).getTypeId());
                List<CmsResource> configResources = m_cms.readResources("/", filter);
                for (CmsResource res : configResources) {
                    try {
                        CmsSitemapAttributeEditorConfiguration config = CmsSitemapAttributeEditorConfiguration.read(
                            m_cms,
                            res);
                        if (config != null) {
                            result.put(res.getStructureId(), config);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return Collections.unmodifiableMap(result);

    }

    /**
     * Loads the site plugins from the VFS.
     *
     * @return the map of site plugins, with their structure ids as keys
     */
    private Map<CmsUUID, CmsSitePlugin> loadSitePlugins() {

        Map<CmsUUID, CmsSitePlugin> result = new HashMap<>();
        if (OpenCms.getResourceManager().hasResourceType(TYPE_SITE_PLUGIN) && m_cms.existsResource("/")) {
            try {
                @SuppressWarnings("deprecation")
                CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(
                    OpenCms.getResourceManager().getResourceType(TYPE_SITE_PLUGIN).getTypeId());
                List<CmsResource> pluginResources = m_cms.readResources("/", filter);
                for (CmsResource res : pluginResources) {
                    try {
                        CmsSitePlugin sitePlugin = CmsSitePlugin.read(m_cms, res);
                        result.put(res.getStructureId(), sitePlugin);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return Collections.unmodifiableMap(result);

    }

}
