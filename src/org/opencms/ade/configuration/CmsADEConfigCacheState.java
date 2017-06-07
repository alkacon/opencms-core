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

import org.opencms.ade.configuration.CmsADEConfigData.DetailInfo;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * An immutable object which represents the complete ADE configuration (sitemap and module configurations)
 * at a certain instant in time.<p>
 */
public class CmsADEConfigCacheState {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEConfigCacheState.class);

    /** The CMS context used for VFS operations. */
    private CmsObject m_cms;

    /** Cached detail page types. */
    private volatile Set<String> m_detailPageTypes;

    /** The available element views. */
    private Map<CmsUUID, CmsElementView> m_elementViews;

    /** The cached content types for folders. */
    private Map<String, String> m_folderTypes = new HashMap<String, String>();

    /** The merged configuration from all the modules. */
    private CmsADEConfigDataInternal m_moduleConfiguration;

    /** The list of module configurations. */
    private List<CmsADEConfigDataInternal> m_moduleConfigurations;

    /** The map of sitemap configurations by structure id. */
    private Map<CmsUUID, CmsADEConfigDataInternal> m_siteConfigurations = new HashMap<CmsUUID, CmsADEConfigDataInternal>();

    /** The configurations from the sitemap / VFS. */
    private Map<String, CmsADEConfigDataInternal> m_siteConfigurationsByPath = new HashMap<String, CmsADEConfigDataInternal>();

    /**
     * Creates a new configuration cache state.<p>
     *
     * @param cms the CMS context to use
     * @param siteConfigurations the map of sitemap configuration beans by structure id
     * @param moduleConfigs the complete list of module configurations
     * @param elementViews the available element views
     */
    public CmsADEConfigCacheState(
        CmsObject cms,
        Map<CmsUUID, CmsADEConfigDataInternal> siteConfigurations,
        List<CmsADEConfigDataInternal> moduleConfigs,
        Map<CmsUUID, CmsElementView> elementViews) {

        m_cms = cms;
        m_siteConfigurations = siteConfigurations;
        m_moduleConfigurations = moduleConfigs;
        m_elementViews = elementViews;
        for (CmsADEConfigDataInternal data : siteConfigurations.values()) {
            if (data.getBasePath() != null) {
                // In theory, the base path should never be null
                m_siteConfigurationsByPath.put(data.getBasePath(), data);
            } else {
                LOG.info("Empty base path for sitemap configuration: " + data.getResource().getRootPath());
            }
        }
        m_moduleConfiguration = mergeConfigurations(moduleConfigs);
        try {
            m_folderTypes = computeFolderTypes();
        } catch (Exception e) {
            m_folderTypes = Maps.newHashMap();
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Creates an empty ADE configuration cache state.<p>
     *
     * @param cms the CMS context
     * @return the empty configuration cache state
     */
    public static CmsADEConfigCacheState emptyState(CmsObject cms) {

        return new CmsADEConfigCacheState(
            cms,
            Collections.<CmsUUID, CmsADEConfigDataInternal> emptyMap(),
            Collections.<CmsADEConfigDataInternal> emptyList(),
            Collections.<CmsUUID, CmsElementView> emptyMap());
    }

    /**
     * Computes the map from folder paths to content types for this ADE configuration state.<p>
     *
     * @return the map of content types by folder root paths
     *
     * @throws CmsException if something goes wrong
     */
    public Map<String, String> computeFolderTypes() throws CmsException {

        Map<String, String> folderTypes = Maps.newHashMap();
        // do this first, since folder types from modules should be overwritten by folder types from sitemaps
        if (m_moduleConfiguration != null) {
            folderTypes.putAll(wrap(m_moduleConfiguration).getFolderTypes());
        }

        List<CmsADEConfigDataInternal> configDataObjects = new ArrayList<CmsADEConfigDataInternal>(
            m_siteConfigurationsByPath.values());
        for (CmsADEConfigDataInternal configData : configDataObjects) {
            folderTypes.putAll(wrap(configData).getFolderTypes());
        }
        return folderTypes;
    }

    /**
     * Creates a new object which represents the changed configuration state given some updates, without
     * changing the current configuration state (this object instance).
     *
     * @param sitemapUpdates a map containing changed sitemap configurations indexed by structure id (the map values are null if the corresponding sitemap configuration is not valid or could not be found)
     * @param moduleUpdates the list of *all* module configurations, or null if no module configuration update is needed
     * @param elementViewUpdates the updated element views, or null if no update needed
     *
     * @return the new configuration state
     */
    public CmsADEConfigCacheState createUpdatedCopy(
        Map<CmsUUID, CmsADEConfigDataInternal> sitemapUpdates,
        List<CmsADEConfigDataInternal> moduleUpdates,
        Map<CmsUUID, CmsElementView> elementViewUpdates) {

        Map<CmsUUID, CmsADEConfigDataInternal> newSitemapConfigs = Maps.newHashMap(m_siteConfigurations);
        if (sitemapUpdates != null) {
            for (Map.Entry<CmsUUID, CmsADEConfigDataInternal> entry : sitemapUpdates.entrySet()) {
                CmsUUID key = entry.getKey();
                CmsADEConfigDataInternal value = entry.getValue();
                if (value != null) {
                    newSitemapConfigs.put(key, value);
                } else {
                    newSitemapConfigs.remove(key);
                }
            }
        }
        List<CmsADEConfigDataInternal> newModuleConfigs = m_moduleConfigurations;
        if (moduleUpdates != null) {
            newModuleConfigs = moduleUpdates;
        }
        Map<CmsUUID, CmsElementView> newElementViews = m_elementViews;
        if (elementViewUpdates != null) {
            newElementViews = elementViewUpdates;
        }

        return new CmsADEConfigCacheState(m_cms, newSitemapConfigs, newModuleConfigs, newElementViews);
    }

    /**
     * Gets the detail page information for everything.<p>
     *
     * @param cms the current CMS context
     * @return the list containing all detail information
     */
    public List<DetailInfo> getDetailInfosForSubsites(CmsObject cms) {

        List<DetailInfo> result = Lists.newArrayList();
        for (CmsADEConfigDataInternal configData : m_siteConfigurationsByPath.values()) {
            List<DetailInfo> infosForSubsite = wrap(configData).getDetailInfos(cms);
            result.addAll(infosForSubsite);
        }
        return result;
    }

    /**
     * Gets the set of type names for which detail pages are configured in any sitemap configuration.<p>
     *
     * @return the set of type names with configured detail pages
     */
    public Set<String> getDetailPageTypes() {

        if (m_detailPageTypes != null) {
            return m_detailPageTypes;
        }
        Set<String> result = new HashSet<String>();
        for (CmsADEConfigDataInternal configData : m_siteConfigurationsByPath.values()) {
            List<CmsDetailPageInfo> detailPageInfos = configData.getOwnDetailPages();
            for (CmsDetailPageInfo info : detailPageInfos) {
                result.add(info.getType());
            }
        }
        m_detailPageTypes = result;
        return result;
    }

    /**
     * Returns the element views.<p>
     *
     * @return the element views
     */
    public Map<CmsUUID, CmsElementView> getElementViews() {

        return Collections.unmodifiableMap(m_elementViews);
    }

    /**
     * Gets the map of folder types.<p>
     *
     * @return the map of folder types
     */
    public Map<String, String> getFolderTypes() {

        return Collections.unmodifiableMap(m_folderTypes);
    }

    /**
     * Helper method to retrieve the parent folder type or <code>null</code> if none available.<p>
     *
     * @param rootPath the path of a resource
     * @return the parent folder content type
     */
    public String getParentFolderType(String rootPath) {

        String parent = CmsResource.getParentFolder(rootPath);
        if (parent == null) {
            return null;
        }
        String type = m_folderTypes.get(parent);
        // type may be null
        return type;
    }

    /**
     * Returns the root paths to all configured sites and sub sites.<p>
     *
     * @return the root paths to all configured sites and sub sites
     */
    public Set<String> getSiteConfigurationPaths() {

        return m_siteConfigurationsByPath.keySet();
    }

    /**
     * Looks up the sitemap configuration for a root path.<p>
     * @param rootPath the root path for which to look up the configuration
     *
     * @return the sitemap configuration for the given root path
     */
    public CmsADEConfigData lookupConfiguration(String rootPath) {

        CmsADEConfigDataInternal internalSiteConfig = getSiteConfigData(rootPath);
        CmsADEConfigData result;
        if (internalSiteConfig == null) {
            result = wrap(m_moduleConfiguration);
        } else {
            result = wrap(internalSiteConfig);
        }
        return result;
    }

    /**
     * Gets all detail page info beans which are defined anywhere in the configuration.<p>
     *
     * @return the list of detail page info beans
     */
    protected List<CmsDetailPageInfo> getAllDetailPages() {

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        for (CmsADEConfigDataInternal configData : m_siteConfigurationsByPath.values()) {
            result.addAll(wrap(configData).getAllDetailPages(true));
        }
        return result;
    }

    /**
     * Gets the CMS context used for VFS operations.<p>
     *
     * @return the CMS context used for VFS operations
     */
    protected CmsObject getCms() {

        return m_cms;
    }

    /**
     * Gets all the detail pages for a given type.<p>
     *
     * @param type the name of the type
     *
     * @return the detail pages for that type
     */
    protected List<String> getDetailPages(String type) {

        List<String> result = new ArrayList<String>();
        for (CmsADEConfigDataInternal configData : m_siteConfigurationsByPath.values()) {
            for (CmsDetailPageInfo pageInfo : wrap(configData).getDetailPagesForType(type)) {
                result.add(pageInfo.getUri());
            }
        }
        return result;
    }

    /**
     * Gets the merged module configuration.<p>
     * @return the merged module configuration instance
     */
    protected CmsADEConfigData getModuleConfiguration() {

        return wrap(m_moduleConfiguration);
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
    protected CmsADEConfigDataInternal getSiteConfigData(String path) {

        if (path == null) {
            return null;
        }
        List<String> prefixes = getSiteConfigPaths(path);
        if (prefixes.size() == 0) {
            return null;
        }
        // for any two prefixes of a string, one is a prefix of the other. so the alphabetically last
        // prefix is the longest prefix of all.
        return m_siteConfigurationsByPath.get(prefixes.get(prefixes.size() - 1));
    }

    /**
     * Finds the paths of sitemap configuration base paths above a given path.<p>
     *
     * @param path the path for which to find the base paths of all valid sitemap configurations
     *
     * @return the list of base paths
     */
    protected List<String> getSiteConfigPaths(String path) {

        String normalizedPath = CmsStringUtil.joinPaths("/", path, "/");
        List<String> prefixes = new ArrayList<String>();

        List<String> parents = new ArrayList<String>();
        String currentPath = normalizedPath;
        while (currentPath != null) {
            parents.add(currentPath);
            currentPath = CmsResource.getParentFolder(currentPath);
        }

        for (String parent : parents) {
            if (m_siteConfigurationsByPath.containsKey(parent)) {
                prefixes.add(parent);
            }
        }
        Collections.sort(prefixes);
        return prefixes;
    }

    /**
     * Checks whether the given resource is configured as a detail page.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource to test
     *
     * @return true if the resource is configured as a detail page
     */
    protected boolean isDetailPage(CmsObject cms, CmsResource resource) {

        CmsResource folder;
        if (resource.isFile()) {
            if (!CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
                return false;
            }
            try {
                folder = getCms().readResource(CmsResource.getParentFolder(resource.getRootPath()));
            } catch (CmsException e) {
                LOG.debug(e.getLocalizedMessage(), e);
                return false;
            }
        } else {
            folder = resource;
        }
        List<CmsDetailPageInfo> allDetailPages = new ArrayList<CmsDetailPageInfo>();
        // First collect all detail page infos
        for (CmsADEConfigDataInternal configData : m_siteConfigurationsByPath.values()) {
            List<CmsDetailPageInfo> detailPageInfos = wrap(configData).getAllDetailPages();
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
                    CmsResource infoResource = getCms().readResource(info.getId());
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
     * Merges a list of multiple configuration objects into a single configuration object.<p>
     *
     * @param configurations the list of configuration objects.<p>
     *
     * @return the merged configuration object
     */
    protected CmsADEConfigDataInternal mergeConfigurations(List<CmsADEConfigDataInternal> configurations) {

        if (configurations.isEmpty()) {
            return new CmsADEConfigDataInternal(null);
        }
        for (int i = 0; i < (configurations.size() - 1); i++) {
            configurations.get(i + 1).mergeParent(configurations.get(i));
        }
        CmsADEConfigDataInternal result = configurations.get(configurations.size() - 1);
        result.processModuleOrdering();
        return result;
    }

    /**
     * Wraps the internal config data into a bean which manages the lookup of inherited configurations.<p>
     *
     * @param data the config data to wrap
     *
     * @return the wrapper object
     */
    private CmsADEConfigData wrap(CmsADEConfigDataInternal data) {

        String path = data.getBasePath();
        List<CmsADEConfigDataInternal> configList = Lists.newArrayList();
        configList.add(m_moduleConfiguration);
        if (path != null) {
            List<String> siteConfigPaths = getSiteConfigPaths(path);
            for (String siteConfigPath : siteConfigPaths) {
                CmsADEConfigDataInternal currentConfig = m_siteConfigurationsByPath.get(siteConfigPath);
                CmsResource masterConfigResource = currentConfig.getMasterConfig();
                if (currentConfig.getMasterConfig() != null) {
                    CmsADEConfigDataInternal masterConfig = m_siteConfigurations.get(
                        masterConfigResource.getStructureId());
                    if (masterConfig != null) {
                        configList.add(masterConfig);
                    } else {
                        LOG.warn(
                            "Master configuration "
                                + masterConfigResource.getRootPath()
                                + " not found for sitemap configuration in "
                                + currentConfig.getBasePath());
                    }
                }
                configList.add(currentConfig);
            }
        }
        return new CmsADEConfigData(data, this, new CmsADEConfigurationSequence(configList));
    }
}
