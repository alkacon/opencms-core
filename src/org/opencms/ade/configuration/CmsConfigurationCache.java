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

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * This is the internal cache class used for storing configuration data. It is not public because it is only meant
 * for internal use.<p>
 * 
 * It stores an instance of {@link CmsADEConfigData} for each active configuration file in the sitemap,
 * and a single instance which represents the merged configuration from all the modules. When a sitemap configuration
 * file is updated, only the single instance for that configuration file is updated, whereas if a module configuration file
 * is changed, the configuration of all modules will be read again.<p>
 */
class CmsConfigurationCache {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConfigurationCache.class);

    /** The key that is used for the map entry which indicates that the module configuration needs to be read. */
    private static final String MODULE_CONFIG_KEY = "__MODULE_CONFIG_KEY__";

    /** The resource type for sitemap configurations. */
    protected I_CmsResourceType m_configType;

    /** The resource type for module configurations. */
    protected I_CmsResourceType m_moduleConfigType;

    /** The CMS context used for reading configuration data. */
    private CmsObject m_cms;

    /** The configuration files which have been changed but not read yet. */
    private Map<String, CmsUUID> m_configurationsToRead = new HashMap<String, CmsUUID>();

    /** The cached content types for folders. */
    private Map<String, String> m_folderTypes = new HashMap<String, String>();

    /** The merged configuration from all the modules. */
    private CmsADEConfigData m_moduleConfiguration;

    /** A cache which stores resources' paths by their structure IDs. */
    private Map<CmsUUID, String> m_pathCache = Collections.synchronizedMap(new HashMap<CmsUUID, String>());

    /** The configurations from the sitemap / VFS. */
    private Map<String, CmsADEConfigData> m_siteConfigurations = new HashMap<String, CmsADEConfigData>();

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
     * Gets the base path for a given sitemap configuration file.<p>
     * 
     * @param siteConfigFile the root path of the sitemap configuration file
     *  
     * @return the base path for the sitemap configuration file 
     */
    protected String getBasePath(String siteConfigFile) {

        if (siteConfigFile.endsWith(CmsADEManager.CONFIG_SUFFIX)) {
            return CmsResource.getParentFolder(CmsResource.getParentFolder(siteConfigFile));
        }
        return siteConfigFile;
    }

    /**
     * Gets all the detail pages for a given type.<p>
     * 
     * @param type the name of the type 
     * 
     * @return the detail pages for that type 
     */
    protected synchronized List<String> getDetailPages(String type) {

        readRemainingConfigurations();
        List<String> result = new ArrayList<String>();
        for (CmsADEConfigData configData : m_siteConfigurations.values()) {
            for (CmsDetailPageInfo pageInfo : configData.getDetailPagesForType(type)) {
                result.add(pageInfo.getUri());
            }
        }
        return result;
    }

    /**
     * Gets the merged module configuration.<p>
     * @return the merged module configuration instance
     */
    protected synchronized CmsADEConfigData getModuleConfiguration() {

        return m_moduleConfiguration;
    }

    /**
     * Helper method to retrieve the parent folder type.<p>
     * 
     * @param rootPath the path of a resource 
     * @return the parent folder content type 
     */
    protected synchronized String getParentFolderType(String rootPath) {

        readRemainingConfigurations();
        String parent = CmsResource.getParentFolder(rootPath);
        if (parent == null) {
            return null;
        }
        String type = m_folderTypes.get(parent);
        if (type == null) {
            return null;
        }
        return type;
    }

    /**
     * Helper method for getting the best matching sitemap configuration object for a given root path, ignoring the module 
     * configuration.<p>
     * 
     * For example, if there are configurations available for the paths /a, /a/b/c, /a/b/x and /a/b/c/d/e, then 
     * the method will return the configuration object for /a/b/c when passed the path /a/b/c/d.
     * 
     * If no configuration data is found for the path, null will be returned.<p> 
     * 
     * @param path a root path  
     * @return the configuration data for the given path, or null if none was found 
     */
    protected synchronized CmsADEConfigData getSiteConfigData(String path) {

        if (path == null) {
            return null;
        }
        readRemainingConfigurations();
        String normalizedPath = CmsStringUtil.joinPaths("/", path, "/");
        List<String> prefixes = new ArrayList<String>();
        for (String key : m_siteConfigurations.keySet()) {
            if (normalizedPath.startsWith(CmsStringUtil.joinPaths("/", key, "/"))) {
                prefixes.add(key);
            }
        }
        if (prefixes.size() == 0) {
            return null;
        }
        Collections.sort(prefixes);
        // for any two prefixes of a string, one is a prefix of the other. so the alphabetically last
        // prefix is the longest prefix of all.
        return m_siteConfigurations.get(prefixes.get(prefixes.size() - 1));
    }

    /**
     * Initializes the cache by reading in all the configuration files.<p>
     */
    protected synchronized void initialize() {

        m_siteConfigurations.clear();
        try {
            List<CmsResource> configFileCandidates = m_cms.readResources(
                "/",
                CmsResourceFilter.DEFAULT.addRequireType(m_configType.getTypeId()));
            for (CmsResource candidate : configFileCandidates) {
                if (isSitemapConfiguration(candidate.getRootPath(), candidate.getTypeId())) {
                    update(candidate);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        refreshModuleConfiguration();
        try {
            initializeFolderTypes();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Initializes the cached folder types.<p>
     * 
     * @throws CmsException if something goes wrong 
     */
    protected synchronized void initializeFolderTypes() throws CmsException {

        LOG.info("Computing folder types for detail pages...");
        m_folderTypes.clear();
        List<CmsADEConfigData> configDataObjects = new ArrayList<CmsADEConfigData>(m_siteConfigurations.values());
        for (CmsADEConfigData configData : configDataObjects) {
            Map<String, String> folderTypes = configData.getFolderTypes();
            m_folderTypes.putAll(folderTypes);
        }
        if (m_moduleConfiguration != null) {
            Map<String, String> folderTypes = m_moduleConfiguration.getFolderTypes();
            m_folderTypes.putAll(folderTypes);
        }
    }

    /**
     * Checks whether the given resource is configured as a detail page.<p>
     * 
     * @param cms the current CMS context  
     * @param resource the resource to test 
     * 
     * @return true if the resource is configured as a detail page 
     */
    protected synchronized boolean isDetailPage(CmsObject cms, CmsResource resource) {

        readRemainingConfigurations();
        CmsResource folder;
        if (resource.isFile()) {
            if (!CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
                return false;
            }
            try {
                folder = m_cms.readResource(CmsResource.getParentFolder(resource.getRootPath()));
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return false;
            }
        } else {
            folder = resource;
        }
        List<CmsDetailPageInfo> allDetailPages = new ArrayList<CmsDetailPageInfo>();
        // First collect all detail page infos 
        for (CmsADEConfigData configData : m_siteConfigurations.values()) {
            List<CmsDetailPageInfo> detailPageInfos = configData.getAllDetailPages();
            allDetailPages.addAll(detailPageInfos);
        }
        // First pass: check if the structure id or path directly match one of the configured detail pages.
        for (CmsDetailPageInfo info : allDetailPages) {
            if (folder.getStructureId().equals(info.getId())
                || folder.getRootPath().equals(info.getUri())
                || resource.getStructureId().equals(info.getId())
                || resource.getRootPath().equals(info.getUri())) {
                return true;
            }
        }
        // Second pass: configured detail pages may be actual container pages rather than folders 
        String normalizedFolderRootPath = CmsStringUtil.joinPaths(folder.getRootPath(), "/");
        for (CmsDetailPageInfo info : allDetailPages) {
            String parentPath = CmsResource.getParentFolder(info.getUri());
            String normalizedParentPath = CmsStringUtil.joinPaths(parentPath, "/");
            if (normalizedParentPath.equals(normalizedFolderRootPath)) {
                try {
                    CmsResource infoResource = m_cms.readResource(info.getId());
                    if (infoResource.isFile()) {
                        return true;
                    }
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        return false;
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
     * Reloads the module configuration.<p>
     */
    protected synchronized void refreshModuleConfiguration() {

        LOG.info("Refreshing module configuration.");
        CmsConfigurationReader reader = new CmsConfigurationReader(m_cms);
        m_moduleConfiguration = reader.readModuleConfigurations();
        m_moduleConfiguration.initialize(m_cms);
    }

    /**
     * Removes a published resource from the cache.<p>
     * 
     * @param res the published resource 
     */
    protected void remove(CmsPublishedResource res) {

        remove(res.getStructureId(), res.getRootPath(), res.getType());
    }

    /**
     * Removes a resource from the cache.<p>
     * 
     * @param res the resource to remove 
     */
    protected void remove(CmsResource res) {

        remove(res.getStructureId(), res.getRootPath(), res.getTypeId());
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
        try {
            updateFolderTypes(rootPath);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        m_pathCache.remove(structureId);
        if (isSitemapConfiguration(rootPath, type)) {
            synchronized (this) {
                String basePath = getBasePath(rootPath);
                removePath(basePath);
                LOG.info("Removing config file from cache: " + rootPath);
            }
        } else if (isModuleConfiguration(rootPath, type)) {
            LOG.info("Removing module configuration " + rootPath);
            synchronized (this) {
                m_configurationsToRead.put(MODULE_CONFIG_KEY, CmsUUID.getNullUUID());
            }
        }

    }

    /**
     * Updates the cache entry for the given published resource.<p>
     * 
     * @param res a published resource
     */
    protected void update(CmsPublishedResource res) {

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
    protected void update(CmsResource res) {

        try {
            update(res.getStructureId(), res.getRootPath(), res.getTypeId(), res.getState());
        } catch (CmsRuntimeException e) {
            // may happen during import of org.opencms.ade.configuration module
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Updates the cache entry for the given resource data.<p>
     * 
     * @param structureId the structure id of the resource  
     * @param rootPath the root path of the resource 
     * @param type the type id of the resource 
     * @param state the state of the resource 
     */
    protected void update(CmsUUID structureId, String rootPath, int type, CmsResourceState state) {

        if (CmsResource.isTemporaryFileName(rootPath)) {
            return;
        }

        try {
            updateFolderTypes(rootPath);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        synchronized (m_pathCache) {
            m_pathCache.remove(structureId);
            m_pathCache.put(structureId, rootPath);
        }
        if (isSitemapConfiguration(rootPath, type)) {
            synchronized (this) {
                // Do not update the configuration right now, because reading configuration files while handling 
                // an event may lead to cache problems. Instead, the configuration file is read when the configuration
                // is queried.
                LOG.info("Changed configuration file " + rootPath + "(" + structureId + "), will be read later");
                m_configurationsToRead.put(rootPath, structureId);
            }
        } else if (isModuleConfiguration(rootPath, type)) {
            LOG.info("Changed module configuration file " + rootPath + "(" + structureId + ")");
            synchronized (this) {
                m_configurationsToRead.put(MODULE_CONFIG_KEY, CmsUUID.getNullUUID());
            }
        }
    }

    /**
    * Updates the cached folder types.<p>
    * 
    * @param rootPath the folder root path 
    * @throws CmsException if something goes wrong 
    */
    protected synchronized void updateFolderTypes(String rootPath) throws CmsException {

        if (m_folderTypes.containsKey(rootPath)) {
            LOG.info("Updating folder types because of a change at " + rootPath);
            synchronized (this) {
                initializeFolderTypes();
            }
        }
    }

    /**
     * Reads the configuration files which have changed but not been read yet.<p>
     */
    private synchronized void readRemainingConfigurations() {

        if (m_configurationsToRead.isEmpty()) {
            // do not initialize folder types if there were no changes!
            return;
        }
        for (Map.Entry<String, CmsUUID> entry : m_configurationsToRead.entrySet()) {
            String rootPath = entry.getKey();
            CmsUUID structureId = entry.getValue();
            if (rootPath.equals(MODULE_CONFIG_KEY)) {
                refreshModuleConfiguration();
            } else {
                try {
                    // remove the original entry first, so that the configuration will be gone if reading the 
                    // configuration file fails.
                    m_siteConfigurations.remove(rootPath);
                    CmsResource configRes = m_cms.readResource(structureId);
                    CmsConfigurationReader reader = new CmsConfigurationReader(m_cms);
                    LOG.info("Reading configuration file " + rootPath + "(" + structureId + ")");
                    String basePath = getBasePath(rootPath);
                    CmsADEConfigData configData = reader.parseSitemapConfiguration(basePath, configRes);
                    configData.initialize(m_cms);
                    m_siteConfigurations.put(basePath, configData);
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                } catch (CmsRuntimeException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        m_configurationsToRead.clear();
        // Methods which recursively call this method must be called after this point,
        // because it will lead to an infinite recursion otherwise.

        try {
            initializeFolderTypes();
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        } catch (CmsRuntimeException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Remove a sitemap configuration from the cache by its base path.<p>
     * 
     * @param rootPath the base path for the sitemap configuration 
     */
    private void removePath(String rootPath) {

        m_configurationsToRead.remove(rootPath);
        m_siteConfigurations.remove(rootPath);
    }

}
