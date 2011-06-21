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

package org.opencms.ade.config;

import org.opencms.ade.config.CmsEntryPointCache.EntryPointFolder;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.CmsConfigurationItem;
import org.opencms.xml.containerpage.CmsLazyFolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A class which can be used to access the configuration data for the container page editor and sitemap editor.<p>
 * 
 * @since 8.0.0
 */
public class CmsADEConfigurationManager {

    /** The CMS context  to use. */
    private CmsObject m_cms;

    /** The configuration cache for container pages. */
    private CmsContainerPageConfigurationCache m_cntPageConfCache;

    /** The module configuration provider for container pages. */
    private CmsContainerPageModuleConfigProvider m_containerPageModuleConfig;

    /** The cache for entry points. */
    private CmsEntryPointCacheManager m_entryPointCacheManager;

    /** The configuration cache for sitemap configurations. */
    private CmsSitemapConfigurationCache m_sitemapConfCache;

    /** The module configuration provider for the sitemap. */
    private CmsSitemapModuleConfigProvider m_sitemapModuleConfig;

    /** Type name constant for sitemap configurations. */
    public static final String TYPE_SITEMAP_CONFIG = "sitemap_config";

    /**
     * Creates a new configuration manager instance.<p>
     * 
     * @param cms an admin CMS context
     *  
     * @throws CmsException if something goes wrong 
     */
    public CmsADEConfigurationManager(CmsObject cms)
    throws CmsException {

        m_cms = cms;
        m_entryPointCacheManager = new CmsEntryPointCacheManager(cms);
        m_sitemapConfCache = new CmsSitemapConfigurationCache(cms);
        m_cntPageConfCache = new CmsContainerPageConfigurationCache(cms);
        m_sitemapModuleConfig = new CmsSitemapModuleConfigProvider(cms);
        m_containerPageModuleConfig = new CmsContainerPageModuleConfigProvider(cms);

        m_folderTypesMaps = new HashMap<Boolean, Map<String, String>>();
        m_cntPageConfCache.addFlushHandler(new I_CmsCacheFlushHandler() {

            public void onFlushCache(boolean online) {

                clearFolderTypeCache(online);
            }
        });

        m_containerPageModuleConfig.addFlushHandler(new I_CmsCacheFlushHandler() {

            public void onFlushCache(boolean online) {

                clearFolderTypeCache(online);

            }
        });

        m_entryPointCacheManager.addFlushHandler(new I_CmsCacheFlushHandler() {

            public void onFlushCache(boolean online) {

                clearFolderTypeCache(online);
            }
        });

    }

    /**
     * Clears the folder type cache.<p>
     * 
     * @param online true if the online cache should be cleared, false if the offline cache should be cleared 
     */
    protected synchronized void clearFolderTypeCache(boolean online) {

        //System.out.println("Clearing folder type cache");
        m_folderTypesMaps.remove(new Boolean(online));
    }

    /**
     * Gets a map which collects the best detail pages for each type.<p>
     *  
     * @param cms the CMS context 
     * @return the map containing the detail page information 
     * 
     * @throws CmsException if something goes wrong 
     */
    public synchronized Map<String, List<CmsDetailPageInfo>> getAllDetailPages(CmsObject cms) throws CmsException {

        Map<String, List<CmsDetailPageInfo>> result = new HashMap<String, List<CmsDetailPageInfo>>();

        List<EntryPointFolder> entryPoints = m_entryPointCacheManager.getEntryPoints(cms);
        for (EntryPointFolder entryPoint : entryPoints) {
            CmsProperty sitemapConfigProp = entryPoint.getProperties().get(
                CmsPropertyDefinition.PROPERTY_CONFIG_SITEMAP);
            if ((sitemapConfigProp == null) || sitemapConfigProp.isNullProperty()) {
                continue;
            }

            String originalSiteRoot = cms.getRequestContext().getSiteRoot();
            String sitemapConfigStr = sitemapConfigProp.getValue();
            try {
                String resourceRootPath = entryPoint.getResource().getRootPath();
                String siteRoot = OpenCms.getSiteManager().getSiteRoot(resourceRootPath);
                cms.getRequestContext().setSiteRoot(siteRoot);
                sitemapConfigStr = cms.getRequestContext().addSiteRoot(sitemapConfigProp.getValue());
            } finally {
                cms.getRequestContext().setSiteRoot(originalSiteRoot);
            }
            CmsSitemapConfigurationData sitemapConfig = m_sitemapConfCache.getConfiguration(cms, sitemapConfigStr);
            Map<String, List<CmsDetailPageInfo>> detailPageInfos = sitemapConfig.getDetailPageInfo();
            for (Map.Entry<String, List<CmsDetailPageInfo>> entry : detailPageInfos.entrySet()) {
                String typeName = entry.getKey();
                List<CmsDetailPageInfo> detailList = entry.getValue();
                if (!result.containsKey(typeName)) {
                    result.put(typeName, new ArrayList<CmsDetailPageInfo>());
                }
                result.get(typeName).add(detailList.get(0));
            }
        }
        //TODO: add detail page from 
        return result;
    }

    /**
     * Gets the container page configuration.<p>
     * 
     * @param cms the CMS context
     * @return the container page configuration 
     * 
     * @throws CmsException if something goes wrong 
     */
    public synchronized CmsContainerPageConfigurationData getContainerPageConfiguration(CmsObject cms)
    throws CmsException {

        String rootPath = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
        return getContainerPageConfiguration(cms, rootPath);

    }

    /**
     * Fetches the container page configuration for the given root path.<p>
     * 
     * @param cms the current CMS context 
     * @param rootPath the root path to the container page
     *  
     * @return the configuration data for the root path
     *   
     * @throws CmsException if something goes wrong 
     */
    public synchronized CmsContainerPageConfigurationData getContainerPageConfiguration(CmsObject cms, String rootPath)
    throws CmsException {

        CmsContainerPageConfigurationData moduleData = m_containerPageModuleConfig.getConfiguration(cms);
        CmsContainerPageConfigurationData data = internalGetConfiguration(
            cms,
            rootPath,
            m_cntPageConfCache,
            new CmsContainerPageConfigurationData(),
            CmsPropertyDefinition.PROPERTY_CONFIG_SITEMAP);
        return moduleData.merge(data);
    }

    /**
     * Finds the nearest entry point to a given root path.<p>
     * 
     * @param cms the CMS context 
     * @param rootPath the root path for which the entry point should be found 
     * @return the nearest entry point as  a resource
     *  
     * @throws CmsException if something goes wrong 
     */
    public synchronized CmsResource getEntryPoint(CmsObject cms, String rootPath) throws CmsException {

        List<EntryPointFolder> entryPoints = m_entryPointCacheManager.lookup(cms, rootPath);
        if (entryPoints.isEmpty()) {
            return null;
        }
        return entryPoints.get(0).getResource();

    }

    /**
     * Gets the sitemap configuration.<p>
     * 
     * @param cms the CMS context 
     * @return the sitemap configuration data
     *  
     * @throws CmsException if something goes wrong 
     */
    public synchronized CmsSitemapConfigurationData getSitemapConfiguration(CmsObject cms) throws CmsException {

        String originalRootUri = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
        return getSitemapConfiguration(cms, originalRootUri);
    }

    /**
     * Fetches the sitemap configuration data for a given root path.<p>
     * 
     * @param cms the CMS context 
     * @param rootPath the root path for which the sitemap configuration should be retrieved 
     * 
     * @return the sitemap configuration data for the pat 
     * 
     * @throws CmsException if something goes wrong 
     */
    public synchronized CmsSitemapConfigurationData getSitemapConfiguration(CmsObject cms, String rootPath)
    throws CmsException {

        CmsObject acms = initCmsObject(cms);
        CmsSitemapConfigurationData moduleData = m_sitemapModuleConfig.getConfiguration(acms);
        CmsSitemapConfigurationData data = internalGetConfiguration(
            cms,
            rootPath,
            m_sitemapConfCache,
            new CmsSitemapConfigurationData(),
            CmsPropertyDefinition.PROPERTY_CONFIG_SITEMAP);
        return moduleData.merge(data);
    }

    /**
     * Initializes an admin CMS object with the site root and project from another CMS object.<p>
     * 
     * @param cms the CMS object whose project and site root  
     * @return the initialized CMS object 
     * @throws CmsException
     */
    protected CmsObject initCmsObject(CmsObject cms) throws CmsException {

        CmsObject result = OpenCms.initCmsObject(m_cms);
        result.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
        result.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        return result;
    }

    /**
     * Returns the list of configurations which may apply to a folder, with the configuration for the deepest sub-site coming first.<p>
     * 
     * @param <Config> the configuration data type 
     * 
     * @param cms the current CMS context 
     * @param rootPath the root path for which the configurations should be retrieved 
     * @param cache the cache from which the configuration should be read if available 
     * @param propName the name of the property which points to the configuration files 
     * 
     * @return the list of configurations for that folder
     *  
     * @throws CmsException if something goes wrong
     */
    protected <Config> List<Config> getConfigurationsForFolder(
        CmsObject cms,
        String rootPath,
        I_CmsConfigurationDataReader<Config> cache,
        String propName) throws CmsException {

        List<EntryPointFolder> entryPoints = getEntryPointsForFolder(cms, rootPath, propName);
        List<Config> configs = new ArrayList<Config>();
        for (EntryPointFolder entryPoint : entryPoints) {
            CmsProperty prop = entryPoint.getProperties().get(propName);
            // if prop were null, it would already be filtered out 
            String cntPageConf = prop.getValue();
            Config configuration = cache.getConfiguration(cms, cntPageConf);
            configs.add(configuration);
        }
        return configs;
    }

    /**
     * Returns the list of entry points for the given folder, with the deepest entry points coming first.<p>
     * 
     * @param cms the current CMS context  
     * @param rootPath the root path of the folder for which the entry points should be retrieved 
     * @param propName the name of the property which points to the configuration files
     *  
     * @return the list of entry points for the given root path 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected List<EntryPointFolder> getEntryPointsForFolder(CmsObject cms, String rootPath, String propName)
    throws CmsException {

        List<EntryPointFolder> entryPoints = m_entryPointCacheManager.lookup(cms, rootPath);
        List<EntryPointFolder> result = new ArrayList<EntryPointFolder>();
        for (EntryPointFolder entryPoint : entryPoints) {
            CmsProperty prop = entryPoint.getProperties().get(propName);
            if (prop != null) {
                result.add(entryPoint);
            }
        }
        return result;
    }

    /**
     * Internal helper method for combining the configurations objects along a given path.<p>
     * 
     * @param <Config> the configuration data type 
     * 
     * @param cms a CMS context 
     * @param rootPath the root path for which to read the configurations 
     * @param cache the cache  the cache from which to get the configurations 
     * @param empty an empty configuration object 
     * @param propName the property which contains the configuration paths 
     * 
     * @return the combined configuration object 
     *  
     * @throws CmsException if something goes wrong 
     */
    protected <Config extends I_CmsMergeable<Config>> Config internalGetConfiguration(
        CmsObject cms,
        String rootPath,
        I_CmsConfigurationDataReader<Config> cache,
        Config empty,
        String propName) throws CmsException {

        List<EntryPointFolder> entryPoints = getEntryPointsForFolder(cms, rootPath, propName);
        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        if (entryPoints.size() == 0) {
            return empty;
        }
        String nearestPath = entryPoints.get(0).getResource().getRootPath();
        Config combinedConfig = cache.getCombinedConfiguration(nearestPath, online);
        if (combinedConfig == null) {
            List<Config> configs = getConfigurationsForFolder(cms, rootPath, cache, propName);
            combinedConfig = mergeConfigurations(configs, empty);
            cache.setCombinedConfiguration(nearestPath, online, combinedConfig);
        }
        return combinedConfig;
    }

    /**
     * Merges a list of configurations.<p>
     * 
     * @param <Config> the configuration data type
     * @param configs the list of configuration objects to merge 
     * @param empty an empty configuration object 
     * 
     * @return the merged configuration 
     */
    protected <Config extends I_CmsMergeable<Config>> Config mergeConfigurations(List<Config> configs, Config empty) {

        Config result = empty;
        for (int i = configs.size() - 1; i >= 0; i--) {
            result = result.merge(configs.get(i));
        }
        return result;
    }

    /** The maps for the folder resource types, indexed by online/offline project. */
    protected Map<Boolean, Map<String, String>> m_folderTypesMaps;

    /**
     * Gets the map which, for a given folder, may contain the type of resources configured for it in the container page configuration.<p>
     * 
     * @param cms the current CMS context 
     * @return the resource type name for the folder, or null if there is no configured type 
     * 
     * @throws CmsException if something goes wrong 
     */
    public synchronized Map<String, String> getFolderTypes(CmsObject cms) throws CmsException {

        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        Boolean onlineObj = new Boolean(online);
        Map<String, String> folderTypes = m_folderTypesMaps.get(onlineObj);
        if (folderTypes == null) {
            //System.out.println("Reading folder types");
            folderTypes = Maps.newHashMap();
            CmsContainerPageConfigurationData moduleConfig = m_containerPageModuleConfig.getConfiguration(cms);
            addFolderTypesFromSingleConfiguration(cms, moduleConfig, folderTypes);
            List<EntryPointFolder> entryPoints = m_entryPointCacheManager.getEntryPoints(cms);
            for (EntryPointFolder entryPoint : entryPoints) {
                addFolderTypesForEntryPoint(cms, entryPoint, folderTypes);
            }
            m_folderTypesMaps.put(onlineObj, Collections.unmodifiableMap(folderTypes));
        }
        return folderTypes;
    }

    /**
     * Adds the folder types for a given entry point.<p>
     * 
     * @param cms the current CMS context 
     * @param entryPoint the entry point 
     * @param folderTypes the map to which the results should be added 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void addFolderTypesForEntryPoint(
        CmsObject cms,
        EntryPointFolder entryPoint,
        Map<String, String> folderTypes) throws CmsException {

        CmsResource folder = entryPoint.getResource();
        CmsProperty configProp = entryPoint.getProperties().get(CmsPropertyDefinition.PROPERTY_CONFIG_SITEMAP);
        if ((configProp == null) || configProp.isNullProperty()) {
            return;
        }
        String configFileSitePath = configProp.getValue();
        String site = OpenCms.getSiteManager().getSiteForRootPath(folder.getRootPath()).getSiteRoot();
        CmsObject siteCms = OpenCms.initCmsObject(cms);
        siteCms.getRequestContext().setSiteRoot(site);
        CmsContainerPageConfigurationData configData = m_cntPageConfCache.getConfiguration(siteCms, configFileSitePath);
        addFolderTypesFromSingleConfiguration(siteCms, configData, folderTypes);
    }

    /**
     * Adds the folder type data from a single configuration object to a map.<p>
     *  
     * @param cms the current CMS context
     * @param configData the single configuration data object 
     * @param folderTypes the map to which the results should be added 
     */
    protected void addFolderTypesFromSingleConfiguration(
        CmsObject cms,
        CmsContainerPageConfigurationData configData,
        Map<String, String> folderTypes) {

        Map<String, CmsConfigurationItem> typeConfig = configData.getTypeConfiguration();
        for (Map.Entry<String, CmsConfigurationItem> entry : typeConfig.entrySet()) {
            String typeName = entry.getKey();
            CmsConfigurationItem configItem = entry.getValue();
            CmsLazyFolder lf = configItem.getLazyFolder();
            String targetSitePath = lf.getSitePath(cms);
            String targetRootPath = cms.getRequestContext().addSiteRoot(targetSitePath);
            folderTypes.put(targetRootPath, typeName);
        }
    }
}
