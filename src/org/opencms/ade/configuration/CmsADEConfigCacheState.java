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
import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReference;
import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReferenceInstance;
import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReferenceMeta;
import org.opencms.ade.configuration.plugins.CmsSitePlugin;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
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

    /** Cache for detail page lists. */
    private Map<String, List<String>> m_detailPageCache;

    /** Memoized supplier for the cached detail page ids. */
    private Supplier<Set<CmsUUID>> m_detailPageIdCache;

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

    /** The sitemap attribute editor configurations. */
    private Map<CmsUUID, CmsSitemapAttributeEditorConfiguration> m_sitemapAttributeEditorConfigurations;

    /** Site plugins. */
    private Map<CmsUUID, CmsSitePlugin> m_sitePlugins;

    /** Cached list of subsites to be included in the site selector. */
    private volatile List<String> m_subsitesForSiteSelector;

    /** Cached set of names of content types anywhere in the configuration. */
    private volatile Set<String> m_contentTypes;

    /**
     * Creates a new configuration cache state.<p>
     *
     * @param cms the CMS context to use
     * @param siteConfigurations the map of sitemap configuration beans by structure id
     * @param moduleConfigs the complete list of module configurations
     * @param elementViews the available element views
     * @param sitePlugins the map of sitemap plugins
     * @param attributeEditorConfigurations the attribute editor configurations
     */
    public CmsADEConfigCacheState(
        CmsObject cms,
        Map<CmsUUID, CmsADEConfigDataInternal> siteConfigurations,
        List<CmsADEConfigDataInternal> moduleConfigs,
        Map<CmsUUID, CmsElementView> elementViews,
        Map<CmsUUID, CmsSitePlugin> sitePlugins,
        Map<CmsUUID, CmsSitemapAttributeEditorConfiguration> attributeEditorConfigurations) {

        m_cms = cms;
        m_siteConfigurations = siteConfigurations;
        m_moduleConfigurations = moduleConfigs;
        m_elementViews = elementViews;
        m_sitePlugins = sitePlugins;
        m_sitemapAttributeEditorConfigurations = attributeEditorConfigurations;
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
        CacheBuilder<?, ?> detailPageCacheBuilder = CacheBuilder.newBuilder().concurrencyLevel(8).expireAfterWrite(
            30,
            TimeUnit.SECONDS);
        m_detailPageCache = (Map<String, List<String>>)(detailPageCacheBuilder.build().asMap());
        m_detailPageIdCache = Suppliers.memoize(this::collectDetailPageIds);

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
            Collections.<CmsUUID, CmsElementView> emptyMap(),
            Collections.emptyMap(),
            Collections.emptyMap());
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
     * @param sitePluginUpdates the new map of site plugins, or null if no update needed
     * @param attributeEditorConfigurations the sitemap attribute editor configurations
     *
     * @return the new configuration state
     */
    public CmsADEConfigCacheState createUpdatedCopy(
        Map<CmsUUID, CmsADEConfigDataInternal> sitemapUpdates,
        List<CmsADEConfigDataInternal> moduleUpdates,
        Map<CmsUUID, CmsElementView> elementViewUpdates,
        Map<CmsUUID, CmsSitePlugin> sitePluginUpdates,
        Map<CmsUUID, CmsSitemapAttributeEditorConfiguration> attributeEditorConfigurations) {

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

        Map<CmsUUID, CmsSitePlugin> newSitePlugins = m_sitePlugins;
        if (sitePluginUpdates != null) {
            newSitePlugins = sitePluginUpdates;
        }

        Map<CmsUUID, CmsSitemapAttributeEditorConfiguration> newAttributeEditorConfigs = m_sitemapAttributeEditorConfigurations;
        if (attributeEditorConfigurations != null) {
            newAttributeEditorConfigs = attributeEditorConfigurations;
        }

        return new CmsADEConfigCacheState(
            m_cms,
            newSitemapConfigs,
            newModuleConfigs,
            newElementViews,
            newSitePlugins,
            newAttributeEditorConfigs);
    }

    /**
     * Gets the sitemap attribute editor configuration with the given id (or null, if there isn't one).
     *
     * @param id the structure id of an attribute editor configuration
     * @return the attribute editor configuration for the id
     */
    public CmsSitemapAttributeEditorConfiguration getAttributeEditorConfiguration(CmsUUID id) {

        return m_sitemapAttributeEditorConfigurations.get(id);
    }

    /**
     * Gets the set of content types configured anywhere in sitemap configurations.
     * 
     * @return the set of content types 
     */
    public Set<String> getContentTypes() {

        if (m_contentTypes == null) {
            Set<String> contentTypes = new HashSet<>();
            for (CmsADEConfigDataInternal config : m_siteConfigurations.values()) {
                for (CmsResourceTypeConfig typeConfig : config.getOwnResourceTypes()) {
                    contentTypes.add(typeConfig.getTypeName());
                }
            }
            for (CmsResourceTypeConfig typeConfig : m_moduleConfiguration.getOwnResourceTypes()) {
                contentTypes.add(typeConfig.getTypeName());
            }
            m_contentTypes = Collections.unmodifiableSet(contentTypes);
        }
        return m_contentTypes;
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
     * Gets the raw detail page information, with no existence checks or path corrections.
     *
     * @return the detail page information
     */
    public List<CmsDetailPageInfo> getRawDetailPages() {

        List<CmsDetailPageInfo> result = new ArrayList<>();
        for (CmsADEConfigDataInternal config : m_siteConfigurationsByPath.values()) {
            result.addAll(config.getOwnDetailPages());
        }
        return result;
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
     * The map of site plugins, by structure id.
     *
     * @return the map of site plugins
     */
    public Map<CmsUUID, CmsSitePlugin> getSitePlugins() {

        return m_sitePlugins;
    }

    /**
     * Gets subsites to be included in the site selector.
     *
     * @return the list of root paths of subsites that should be included in the site selector
     */
    public List<String> getSubsitesForSiteSelector() {

        if (m_subsitesForSiteSelector == null) {
            List<String> paths = m_siteConfigurations.values().stream().filter(
                conf -> conf.isIncludeInSiteSelector()).map(conf -> conf.getBasePath()).collect(Collectors.toList());
            m_subsitesForSiteSelector = Collections.unmodifiableList(paths);
        }
        return m_subsitesForSiteSelector;
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

        List<String> result = m_detailPageCache.get(type);
        if (result == null) {
            result = new ArrayList<>();
            for (CmsADEConfigDataInternal configData : m_siteConfigurationsByPath.values()) {
                for (CmsDetailPageInfo pageInfo : wrap(configData).getDetailPagesForType(type)) {
                    result.add(pageInfo.getUri());
                }
            }
            m_detailPageCache.put(type, result);
        }
        return Collections.unmodifiableList(result);
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

        if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            Set<CmsUUID> detailPageIds = m_detailPageIdCache.get();
            if (detailPageIds.contains(resource.getStructureId())) {
                // page may have been created/replaced after the detail page id cache was generated,
                // so we don't just return false if it doesn't contain the id.
                // instead we check the parent folder too in the next step.
                return true;
            }

            try {
                CmsResource parent = getCms().readResource(
                    CmsResource.getParentFolder(resource.getRootPath()),
                    CmsResourceFilter.ALL);
                return detailPageIds.contains(parent.getStructureId());

            } catch (Exception e) {
                LOG.info(e.getLocalizedMessage(), e);
                return false;
            }
        } else if (resource.isFolder()) {
            return m_detailPageIdCache.get().contains(resource.getStructureId());
        } else {
            return false;
        }
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
     * Internal method for collecting structure ids of all configured detail pages and their parent folders.
     *
     * @return the structure ids of configured detail pages and their parent folders
     */
    private Set<CmsUUID> collectDetailPageIds() {

        List<CmsDetailPageInfo> allDetailPages = new ArrayList<CmsDetailPageInfo>();
        // First collect all detail page infos
        for (CmsADEConfigDataInternal configData : m_siteConfigurationsByPath.values()) {
            List<CmsDetailPageInfo> detailPageInfos = configData.getOwnDetailPages();
            allDetailPages.addAll(detailPageInfos);
        }
        Set<CmsUUID> detailPageOrDetailPageFolderIds = new HashSet<>();
        for (CmsDetailPageInfo detailPageInfo : allDetailPages) {
            try {
                CmsResource detailPageRes = getCms().readResource(detailPageInfo.getId(), CmsResourceFilter.ALL);
                detailPageOrDetailPageFolderIds.add(detailPageInfo.getId());
                if (detailPageRes.isFile()) {
                    CmsResource parent = getCms().readParentFolder(detailPageInfo.getId());
                    detailPageOrDetailPageFolderIds.add(parent.getStructureId());
                } else {
                    CmsResource defaultfile = getCms().readDefaultFile("" + detailPageInfo.getId());
                    if (defaultfile != null) {
                        detailPageOrDetailPageFolderIds.add(defaultfile.getStructureId());
                    }
                }
            } catch (Exception e) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
        return Collections.unmodifiableSet(detailPageOrDetailPageFolderIds);
    }

    /**
     * For a given master configuration, lists all directly and indirectly referenced master configurations, in sitemap config inheritance order (i.e. referenced master configurations preceding the
     * configurations from which they are referenced).
     *
     * @param result the list to append the results to
     * @param current the configuration reference to start with
     * @param seen the set of structure ids of sitemap configurations already visited
     */
    private void fillMasterConfigurations(
        List<ConfigReferenceInstance> result,
        ConfigReferenceInstance current,
        Set<CmsUUID> seen) {

        CmsUUID currentId = current.getConfig().getResource().getStructureId();
        if (seen.contains(currentId)) {
            LOG.warn("Loop in sitemap configuration references, target = " + current.getConfig().getBasePath());
            return;
        }
        seen.add(currentId);
        // Recursively add the referenced master configurations before adding the current configuration in the end
        for (ConfigReference configRef : current.getConfig().getMasterConfigs()) {
            CmsADEConfigDataInternal config = m_siteConfigurations.get(configRef.getId());
            if (config != null) {
                ConfigReferenceMeta combinedMeta = current.getMeta().combine(configRef.getMeta());
                ConfigReferenceInstance combinedRef = new ConfigReferenceInstance(config, combinedMeta);
                fillMasterConfigurations(result, combinedRef, seen);
            } else {
                LOG.warn(
                    "Master configuration with id "
                        + configRef.getId()
                        + " not found, referenced by "
                        + current.getConfig().getResource().getRootPath());
            }
        }
        result.add(current);
        seen.remove(currentId);
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
        List<ConfigReferenceInstance> configList = Lists.newArrayList();
        configList.add(new ConfigReferenceInstance(m_moduleConfiguration));
        if (path != null) {
            List<String> siteConfigPaths = getSiteConfigPaths(path);
            for (String siteConfigPath : siteConfigPaths) {
                CmsADEConfigDataInternal currentConfig = m_siteConfigurationsByPath.get(siteConfigPath);
                fillMasterConfigurations(configList, new ConfigReferenceInstance(currentConfig), new HashSet<>());
            }
        }
        return new CmsADEConfigData(data, this, new CmsADEConfigurationSequence(configList));
    }
}
