/*
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

import org.opencms.ade.configuration.formatters.CmsFormatterChangeSet;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCacheState;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A class which represents the accessible configuration data at a given point in a sitemap.<p>
 */
public class CmsADEConfigData {

    /**
     * Bean which contains the detail information for a single sub-sitemap and resource type.<p>
     *
     * This includes both  information about the detail page itself, as well as the path of the folder
     * which is used to store that content type in this subsitemap.<p>
     *
     */
    public class DetailInfo {

        /** The base path of the sitemap configuration where this information originates from. */
        private String m_basePath;

        /** The information about the detail page info itself. */
        private CmsDetailPageInfo m_detailPageInfo;

        /** The content folder path. */
        private String m_folderPath;

        /** The detail type. */
        private String m_type;

        /**
         * Creates a new instance.<p>
         *
         * @param folderPath the content folder path
         * @param detailPageInfo the detail page information
         * @param type the detail type
         * @param basePath the base path of the sitemap configuration
         */
        public DetailInfo(String folderPath, CmsDetailPageInfo detailPageInfo, String type, String basePath) {
            m_folderPath = folderPath;
            m_detailPageInfo = detailPageInfo;
            m_type = type;
            m_basePath = basePath;

        }

        /**
         * Gets the base path of the sitemap configuration from which this information is coming.<p>
         *
         * @return the base path
         */
        public String getBasePath() {

            return m_basePath;
        }

        /**
         * Gets the detail page information.<p>
         *
         * @return the detail page information
         */
        public CmsDetailPageInfo getDetailPageInfo() {

            return m_detailPageInfo;
        }

        /**
         * Gets the content folder path.<p>
         *
         * @return the content folder path
         */
        public String getFolderPath() {

            return m_folderPath;
        }

        /**
         * Gets the detail type.<p>
         *
         * @return the detail type
         */
        public String getType() {

            return m_type;
        }

        /**
         * Sets the base path.<p>
         *
         * @param basePath the new base path value
         */
        public void setBasePath(String basePath) {

            m_basePath = basePath;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }
    }

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEConfigData.class);

    /** The wrapped configuration bean containing the actual data. */
    protected CmsADEConfigDataInternal m_data;

    /** The cache state to which the wrapped configuration bean belongs. */
    private CmsADEConfigCacheState m_cache;

    /** The configuration sequence (contains the list of all sitemap configuration data beans to be used for inheritance). */
    private CmsADEConfigurationSequence m_configSequence;

    /**
     * Creates a new configuration data object, based on an internal configuration data bean and a
     * configuration cache state.<p>
     *
     * @param data the internal configuration data bean
     * @param cache the configuration cache state
     * @param configSequence the configuration sequence
     */
    public CmsADEConfigData(
        CmsADEConfigDataInternal data,
        CmsADEConfigCacheState cache,
        CmsADEConfigurationSequence configSequence) {

        m_data = data;
        m_cache = cache;
        m_configSequence = configSequence;
    }

    /**
     * Generic method to merge lists of named configuration objects.<p>
     *
     * The lists are merged such that the configuration objects from the child list rise to the front of the result list,
     * and two configuration objects will be merged themselves if they share the same name.<p>
     *
     * For example, if we have two lists of configuration objects:<p>
     *
     * parent: A1, B1, C1<p>
     * child: D2, B2<p>
     *
     * then the resulting list will look like:<p>
     *
     * D2, B3, A1, C1<p>
     *
     * where B3 is the result of merging B1 and B2.<p>
     *
     * @param <C> the type of configuration object
     * @param parentConfigs the parent configurations
     * @param childConfigs the child configurations
     * @param preserveDisabled if true, try to merge parents with disabled children instead of discarding them
     *
     * @return the merged configuration object list
     */
    public static <C extends I_CmsConfigurationObject<C>> List<C> combineConfigurationElements(
        List<C> parentConfigs,
        List<C> childConfigs,
        boolean preserveDisabled) {

        List<C> result = new ArrayList<C>();
        Map<String, C> map = new LinkedHashMap<String, C>();
        if (parentConfigs != null) {
            for (C parent : Lists.reverse(parentConfigs)) {
                map.put(parent.getKey(), parent);
            }
        }
        if (childConfigs == null) {
            childConfigs = Collections.emptyList();
        }
        for (C child : Lists.reverse(childConfigs)) {
            String childKey = child.getKey();
            if (child.isDisabled() && !preserveDisabled) {
                map.remove(childKey);
            } else {
                C parent = map.get(childKey);
                map.remove(childKey);
                C newValue;
                if (parent != null) {
                    newValue = parent.merge(child);
                } else {
                    newValue = child;
                }
                map.put(childKey, newValue);
            }
        }
        result = new ArrayList<C>(map.values());
        Collections.reverse(result);
        // those multiple "reverse" calls may a bit confusing. They are there because on the one hand we want to keep the
        // configuration items from one configuration in the same order as they are defined, on the other hand we want
        // configuration items from a child configuration to rise to the top of the configuration items.

        // so for example, if the parent configuration has items with the keys A,B,C,E
        // and the child configuration has items  with the keys C,B,D
        // we want the items of the combined configuration in the order C,B,D,A,E

        return result;
    }

    /**
     * Applies the formatter change sets of this and all parent configurations to a map of external (non-schema) formatters.<p>
     *
     * @param formatters the external formatter map which will be modified
     *
     * @param formatterCacheState the formatter cache state from which new external formatters should be fetched
     */
    public void applyAllFormatterChanges(
        Map<CmsUUID, I_CmsFormatterBean> formatters,
        CmsFormatterConfigurationCacheState formatterCacheState) {

        for (CmsFormatterChangeSet changeSet : getFormatterChangeSets()) {
            changeSet.applyToFormatters(formatters, formatterCacheState);
        }
    }

    /**
     * Gets the active external (non-schema) formatters for this sub-sitemap.<p>
     *
     * @return the map of active external formatters by structure id
     */
    public Map<CmsUUID, I_CmsFormatterBean> getActiveFormatters() {

        CmsFormatterConfigurationCacheState cacheState = getCachedFormatters();
        Map<CmsUUID, I_CmsFormatterBean> result = Maps.newHashMap(cacheState.getAutoEnabledFormatters());
        applyAllFormatterChanges(result, cacheState);
        return result;
    }

    /**
     * Gets the list of all detail pages.<p>
     *
     * @return the list of all detail pages
     */
    public List<CmsDetailPageInfo> getAllDetailPages() {

        return getAllDetailPages(true);
    }

    /**
     * Gets a list of all detail pages.<p>
     *
     * @param update if true, this method will try to correct the root paths in the returned objects if the corresponding resources have been moved
     *
     * @return the list of all detail pages
     */
    public List<CmsDetailPageInfo> getAllDetailPages(boolean update) {

        CmsADEConfigData parentData = parent();
        List<CmsDetailPageInfo> parentDetailPages;
        if (parentData != null) {
            parentDetailPages = parentData.getAllDetailPages(false);
        } else {
            parentDetailPages = Collections.emptyList();
        }
        List<CmsDetailPageInfo> result = mergeDetailPages(parentDetailPages, m_data.getOwnDetailPages());
        if (update) {
            result = updateUris(result);
        }
        return result;
    }

    /**
     * Gets the configuration base path.<p>
     *
     * For example, if the configuration file is located at /sites/default/.content/.config, the base path is /sites/default.<p>
     *
     * @return the base path of the configuration
     */
    public String getBasePath() {

        return m_data.getBasePath();
    }

    /**
     * Gets the cached formatters.<p>
     *
     * @return the cached formatters
     */
    public CmsFormatterConfigurationCacheState getCachedFormatters() {

        return OpenCms.getADEManager().getCachedFormatters(
            getCms().getRequestContext().getCurrentProject().isOnlineProject());
    }

    /**
     * Returns the names of the bundles configured as workplace bundles in any module configuration.
     * @return the names of the bundles configured as workplace bundles in any module configuration.
     */
    public Set<String> getConfiguredWorkplaceBundles() {

        Set<String> result = new HashSet<String>();
        for (CmsResourceTypeConfig config : internalGetResourceTypes(false)) {
            String bundlename = config.getConfiguredWorkplaceBundle();
            if (null != bundlename) {
                result.add(bundlename);
            }
        }
        return result;
    }

    /**
     * Gets the content folder path.<p>
     *
     * For example, if the configuration file is located at /sites/default/.content/.config, the content folder path is /sites/default/.content
     *
     * @return the content folder path
     */
    public String getContentFolderPath() {

        return CmsStringUtil.joinPaths(m_data.getBasePath(), CmsADEManager.CONTENT_FOLDER_NAME);

    }

    /**
     * Returns a list of the creatable resource types.<p>
     *
     * @param cms the CMS context used to check whether the resource types are creatable
     * @param pageFolderRootPath the root path of the current container page
     * @return the list of creatable resource type
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResourceTypeConfig> getCreatableTypes(CmsObject cms, String pageFolderRootPath) throws CmsException {

        List<CmsResourceTypeConfig> result = new ArrayList<CmsResourceTypeConfig>();
        for (CmsResourceTypeConfig typeConfig : getResourceTypes()) {
            if (typeConfig.checkCreatable(cms, pageFolderRootPath)) {
                result.add(typeConfig);
            }
        }
        return result;
    }

    /**
     * Returns the default model page.<p>
     *
     * @return the default model page
     */
    public CmsModelPageConfig getDefaultModelPage() {

        List<CmsModelPageConfig> modelPages = getModelPages();
        for (CmsModelPageConfig modelPageConfig : getModelPages()) {
            if (modelPageConfig.isDefault()) {
                return modelPageConfig;
            }
        }
        if (modelPages.isEmpty()) {
            return null;
        }
        return modelPages.get(0);
    }

    /**
     * Gets the detail information for this sitemap config data object.<p>
     *
     * @param cms the CMS context
     * @return the list of detail information
     */
    public List<DetailInfo> getDetailInfos(CmsObject cms) {

        List<DetailInfo> result = Lists.newArrayList();
        List<CmsDetailPageInfo> detailPages = getAllDetailPages(true);
        Collections.reverse(detailPages); // make sure primary detail pages come later in the list and override other detail pages for the same type
        Map<String, CmsDetailPageInfo> primaryDetailPageMapByType = Maps.newHashMap();
        for (CmsDetailPageInfo pageInfo : detailPages) {
            primaryDetailPageMapByType.put(pageInfo.getType(), pageInfo);
        }
        for (CmsResourceTypeConfig typeConfig : getResourceTypes()) {
            String typeName = typeConfig.getTypeName();
            if (((typeConfig.getFolderOrName() == null) || !typeConfig.getFolderOrName().isPageRelative())
                && primaryDetailPageMapByType.containsKey(typeName)) {
                String folderPath = typeConfig.getFolderPath(cms, null);
                CmsDetailPageInfo pageInfo = primaryDetailPageMapByType.get(typeName);
                result.add(new DetailInfo(folderPath, pageInfo, typeName, getBasePath()));
            }
        }
        return result;
    }

    /**
     * Gets the detail pages for a specific type.<p>
     *
     * @param type the type name
     *
     * @return the list of detail pages for that type
     */
    public List<CmsDetailPageInfo> getDetailPagesForType(String type) {

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        for (CmsDetailPageInfo detailpage : getAllDetailPages(true)) {
            if (detailpage.getType().equals(type)) {
                result.add(detailpage);
            }
        }
        return result;
    }

    /**
     * Returns all available display formatters.<p>
     *
     * @param cms the cms context
     *
     * @return the available display formatters
     */
    public List<I_CmsFormatterBean> getDisplayFormatters(CmsObject cms) {

        List<I_CmsFormatterBean> result = new ArrayList<I_CmsFormatterBean>();
        for (CmsResourceTypeConfig type : getResourceTypes()) {
            try {
                CmsFormatterConfiguration conf = getFormatters(cms, type.getType(), null);
                result.addAll(conf.getDisplayFormatters());
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns the formatter change sets for this and all parent sitemaps, ordered by increasing folder depth of the sitemap.<p>
     *
     * @return the formatter change sets for all ancestor sitemaps
     */
    public List<CmsFormatterChangeSet> getFormatterChangeSets() {

        CmsADEConfigData currentConfig = this;
        List<CmsFormatterChangeSet> result = Lists.newArrayList();
        while (currentConfig != null) {
            CmsFormatterChangeSet changes = currentConfig.getOwnFormatterChangeSet();
            if (changes != null) {
                result.add(changes);
            }
            currentConfig = currentConfig.parent();
        }
        Collections.reverse(result);
        return result;
    }

    /**
     * Gets the formatter configuration for a resource.<p>
     *
     * @param cms the current CMS context
     * @param res the resource for which the formatter configuration should be retrieved
     *
     * @return the configuration of formatters for the resource
     */
    public CmsFormatterConfiguration getFormatters(CmsObject cms, CmsResource res) {

        try {
            int resTypeId = res.getTypeId();
            return getFormatters(
                cms,
                OpenCms.getResourceManager().getResourceType(resTypeId),
                getFormattersFromSchema(cms, res));
        } catch (CmsLoaderException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Gets a named function reference.<p>
     *
     * @param name the name of the function reference
     *
     * @return the function reference for the given name
     */
    public CmsFunctionReference getFunctionReference(String name) {

        List<CmsFunctionReference> functionReferences = getFunctionReferences();
        for (CmsFunctionReference functionRef : functionReferences) {
            if (functionRef.getName().equals(name)) {
                return functionRef;
            }
        }
        return null;
    }

    /**
     * Gets the list of configured function references.<p>
     *
     * @return the list of configured function references
     */
    public List<CmsFunctionReference> getFunctionReferences() {

        return internalGetFunctionReferences();
    }

    /**
     * Gets the map of external (non-schema) formatters which are inactive in this sub-sitemap.<p>
     *
     * @return the map inactive external formatters
     */
    public Map<CmsUUID, I_CmsFormatterBean> getInactiveFormatters() {

        CmsFormatterConfigurationCacheState cacheState = getCachedFormatters();
        Map<CmsUUID, I_CmsFormatterBean> result = Maps.newHashMap(cacheState.getFormatters());
        result.keySet().removeAll(getActiveFormatters().keySet());
        return result;
    }

    /**
     * Gets the main detail page for a specific type.<p>
     *
     * @param type the type name
     *
     * @return the main detail page for that type
     */
    public CmsDetailPageInfo getMainDetailPage(String type) {

        List<CmsDetailPageInfo> detailPages = getDetailPagesForType(type);
        if ((detailPages == null) || detailPages.isEmpty()) {
            return null;
        }
        return detailPages.get(0);
    }

    /**
     * Gets the list of available model pages.<p>
     *
     * @return the list of available model pages
     */
    public List<CmsModelPageConfig> getModelPages() {

        return getModelPages(false);
    }

    /**
     * Gets the list of available model pages.<p>
     *
     * @param includeDisable <code>true</code> to include disabled model pages
     *
     * @return the list of available model pages
     */
    public List<CmsModelPageConfig> getModelPages(boolean includeDisable) {

        CmsADEConfigData parentData = parent();
        List<CmsModelPageConfig> parentModelPages;
        if ((parentData != null) && !m_data.isDiscardInheritedModelPages()) {
            parentModelPages = parentData.getModelPages();
        } else {
            parentModelPages = Collections.emptyList();
        }

        List<CmsModelPageConfig> result = combineConfigurationElements(
            parentModelPages,
            m_data.getOwnModelPageConfig(),
            includeDisable);
        return result;
    }

    /**
     * Gets the formatter changes for this sitemap configuration.<p>
     *
     * @return the formatter change set
     */
    public CmsFormatterChangeSet getOwnFormatterChangeSet() {

        return m_data.getFormatterChangeSet();
    }

    /**
     * Gets the configuration for the available properties.<p>
     *
     * @return the configuration for the available properties
     */
    public List<CmsPropertyConfig> getPropertyConfiguration() {

        CmsADEConfigData parentData = parent();
        List<CmsPropertyConfig> parentProperties;
        if ((parentData != null) && !m_data.isDiscardInheritedProperties()) {
            parentProperties = parentData.getPropertyConfiguration();
        } else {
            parentProperties = Collections.emptyList();
        }
        List<CmsPropertyConfig> result = combineConfigurationElements(
            parentProperties,
            m_data.getOwnPropertyConfigurations(),
            false);
        return result;
    }

    /**
     * Gets the property configuration as a map of CmsXmlContentProperty instances.<p>
     *
     * @return the map of property configurations
     */
    public Map<String, CmsXmlContentProperty> getPropertyConfigurationAsMap() {

        Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
        for (CmsPropertyConfig propConf : getPropertyConfiguration()) {
            result.put(propConf.getName(), propConf.getPropertyData());
        }
        return result;
    }

    /**
     * Returns the resource from which this configuration was read.<p>
     *
     * @return the resource from which this configuration was read
     */
    public CmsResource getResource() {

        return m_data.getResource();
    }

    /**
     * Returns the configuration for a specific resource type.<p>
     *
     * @param typeName the name of the type
     *
     * @return the resource type configuration for that type
     */
    public CmsResourceTypeConfig getResourceType(String typeName) {

        for (CmsResourceTypeConfig type : getResourceTypes()) {
            if (typeName.equals(type.getTypeName())) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets a list of all available resource type configurations.<p>
     *
     * @return the available resource type configurations
     */
    public List<CmsResourceTypeConfig> getResourceTypes() {

        List<CmsResourceTypeConfig> result = internalGetResourceTypes(true);
        for (CmsResourceTypeConfig config : result) {
            config.initialize(getCms());
        }
        return result;
    }

    /**
     * Gets the searchable resource type configurations.<p>
     *
     * @param cms the current CMS context
     * @return the searchable resource type configurations
     */
    public Collection<CmsResourceTypeConfig> getSearchableTypes(CmsObject cms) {

        return getResourceTypes();
    }

    /**
     * Gets the set of resource type names for which schema formatters can be enabled or disabled and which are not disabled in this sub-sitemap.<p>
     *
     * @return the set of types for which schema formatters are active
     */
    public Set<String> getTypesWithActiveSchemaFormatters() {

        Set<String> result = Sets.newHashSet(getTypesWithModifiableFormatters());
        for (CmsFormatterChangeSet changeSet : getFormatterChangeSets()) {
            changeSet.applyToTypes(result);
        }
        return result;
    }

    /**
     * Gets the set of names of resource types which have schema-based formatters that can be enabled or disabled.<p>
     *
     * @return the set of names of resource types which have schema-based formatters that can be enabled or disabled
     */
    public Set<String> getTypesWithModifiableFormatters() {

        Set<String> result = new HashSet<String>();
        for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
            if (type instanceof CmsResourceTypeXmlContent) {
                CmsXmlContentDefinition contentDef = null;
                try {
                    contentDef = CmsXmlContentDefinition.getContentDefinitionForType(getCms(), type.getTypeName());
                    if ((contentDef != null) && contentDef.getContentHandler().hasModifiableFormatters()) {
                        result.add(type.getTypeName());
                    }
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;

    }

    /**
     * Checks if there are any matching formatters for the given set of containers.<p>
     *
     * @param cms the current CMS context
     * @param resType the resource type for which the formatter configuration should be retrieved
     * @param containers the page containers
     *
     * @return if there are any matching formatters
     */
    public boolean hasFormatters(CmsObject cms, I_CmsResourceType resType, Collection<CmsContainer> containers) {

        try {
            if (CmsXmlDynamicFunctionHandler.TYPE_FUNCTION.equals(resType.getTypeName())) {
                // dynamic function may match any container
                return true;
            }
            CmsXmlContentDefinition def = CmsXmlContentDefinition.getContentDefinitionForType(
                cms,
                resType.getTypeName());
            CmsFormatterConfiguration schemaFormatters = def.getContentHandler().getFormatterConfiguration(cms, null);
            CmsFormatterConfiguration formatters = getFormatters(cms, resType, schemaFormatters);
            for (CmsContainer cont : containers) {
                if (cont.isEditable()
                    && (formatters.getAllMatchingFormatters(cont.getType(), cont.getWidth(), true).size() > 0)) {
                    return true;
                }
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);

        }
        return false;
    }

    /**
     * Returns the value of the "create contents locally" flag.<p>
     *
     * If this flag is set, contents of types configured in a super-sitemap will be created in the sub-sitemap (if the user
     * creates them from the sub-sitemap).
     *
     * @return the "create contents locally" flag
     */
    public boolean isCreateContentsLocally() {

        return m_data.isCreateContentsLocally();
    }

    /**
     * Returns the value of the "discard inherited model pages" flag.<p>
     *
     * If this flag is set, inherited model pages will be discarded for this sitemap.<p>
     *
     * @return the "discard inherited model pages" flag
     */
    public boolean isDiscardInheritedModelPages() {

        return m_data.isDiscardInheritedModelPages();
    }

    /**
     * Returns the value of the "discard inherited properties" flag.<p>
     *
     * If this is flag is set, inherited property definitions will be discarded for this sitemap.<p>
     *
     * @return the "discard inherited properties" flag.<p>
     */
    public boolean isDiscardInheritedProperties() {

        return m_data.isDiscardInheritedProperties();
    }

    /**
     * Returns the value of the "discard inherited types" flag.<p>
     *
     * If this flag is set, inherited resource types from a super-sitemap will be discarded for this sitemap.<p>
     *
     * @return the "discard inherited types" flag
     */
    public boolean isDiscardInheritedTypes() {

        return m_data.isDiscardInheritedTypes();
    }

    /**
     * Returns true if this is a module configuration instead of a normal sitemap configuration.<p>
     *
     * @return true if this is a module configuration
     */
    public boolean isModuleConfiguration() {

        return m_data.isModuleConfig();
    }

    /**
     * Returns true if detail pages from this sitemap should be preferred for links to contents in this sitemap.<p>
     *
     * @return true if detail pages from this sitemap should be preferred for links to contents in this sitemap
     */
    public boolean isPreferDetailPagesForLocalContents() {

        return m_data.isPreferDetailPagesForLocalContents();
    }

    /**
     * Fetches the parent configuration of this configuration.<p>
     *
     * If this configuration is a sitemap configuration with no direct parent configuration,
     * the module configuration will be returned. If this configuration already is a module configuration,
     * null will be returned.<p>
     *
     * @return the parent configuration
     */
    public CmsADEConfigData parent() {

        Optional<CmsADEConfigurationSequence> parentPath = m_configSequence.getParent();
        if (parentPath.isPresent()) {
            CmsADEConfigDataInternal internalData = parentPath.get().getConfig();
            return new CmsADEConfigData(internalData, m_cache, parentPath.get());
        } else {
            return null;
        }
    }

    /**
     * Creates the content directory for this configuration node if possible.<p>
     *
     * @throws CmsException if something goes wrong
     */
    protected void createContentDirectory() throws CmsException {

        if (!isModuleConfiguration()) {
            String contentFolder = getContentFolderPath();
            if (!getCms().existsResource(contentFolder)) {
                getCms().createResource(
                    contentFolder,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
            }
        }
    }

    /**
     * Gets the CMS object used for VFS operations.<p>
     *
     * @return the CMS object used for VFS operations
     */
    protected CmsObject getCms() {

        return m_cache.getCms();
    }

    /**
     * Gets the CMS object used for VFS operations.<p>
     *
     * @return the CMS object
     */
    protected CmsObject getCmsObject() {

        return getCms();
    }

    /**
     * Helper method to converts a list of detail pages to a map from type names to lists of detail pages for each type.<p>
     *
     * @param detailPages the list of detail pages
     *
     * @return the map of detail pages
     */
    protected Map<String, List<CmsDetailPageInfo>> getDetailPagesMap(List<CmsDetailPageInfo> detailPages) {

        Map<String, List<CmsDetailPageInfo>> result = Maps.newHashMap();
        for (CmsDetailPageInfo detailpage : detailPages) {
            String type = detailpage.getType();
            if (!result.containsKey(type)) {
                result.put(type, new ArrayList<CmsDetailPageInfo>());
            }
            result.get(type).add(detailpage);
        }
        return result;
    }

    /**
     * Collects the folder types in a map.<p>
     *
     * @return the map of folder types
     *
     * @throws CmsException if something goes wrong
     */
    protected Map<String, String> getFolderTypes() throws CmsException {

        Map<String, String> result = new HashMap<String, String>();
        CmsObject cms = OpenCms.initCmsObject(getCms());
        if (m_data.isModuleConfig()) {
            Set<String> siteRoots = OpenCms.getSiteManager().getSiteRoots();
            for (String siteRoot : siteRoots) {
                cms.getRequestContext().setSiteRoot(siteRoot);
                for (CmsResourceTypeConfig config : getResourceTypes()) {
                    String typeName = config.getTypeName();
                    if (!config.isPageRelative()) { // elements stored with container pages can not be used as detail contents
                        String folderPath = config.getFolderPath(cms, null);
                        result.put(CmsStringUtil.joinPaths(folderPath, "/"), typeName);
                    }
                }
            }
        } else {
            for (CmsResourceTypeConfig config : getResourceTypes()) {
                String typeName = config.getTypeName();
                if (!config.isPageRelative()) { // elements stored with container pages can not be used as detail contents
                    String folderPath = config.getFolderPath(getCms(), null);
                    result.put(CmsStringUtil.joinPaths(folderPath, "/"), typeName);
                }
            }
        }
        return result;
    }

    /**
     * Gets the formatter configuration for a resource type.<p>
     *
     * @param cms the current CMS context
     * @param resType the resource type
     * @param schemaFormatters the resource schema formatters
     *
     * @return the configuration of formatters for the resource type
     */
    protected CmsFormatterConfiguration getFormatters(
        CmsObject cms,
        I_CmsResourceType resType,
        CmsFormatterConfiguration schemaFormatters) {

        String typeName = resType.getTypeName();
        CmsFormatterConfigurationCacheState formatterCacheState = getCachedFormatters();
        List<I_CmsFormatterBean> formatters = new ArrayList<I_CmsFormatterBean>();
        Set<String> types = new HashSet<String>();
        types.add(typeName);
        for (CmsFormatterChangeSet changeSet : getFormatterChangeSets()) {
            if (changeSet != null) {
                changeSet.applyToTypes(types);
            }
        }
        if ((schemaFormatters != null) && types.contains(typeName)) {
            for (I_CmsFormatterBean formatter : schemaFormatters.getAllFormatters()) {
                formatters.add(formatter);
            }

        }
        Map<CmsUUID, I_CmsFormatterBean> externalFormattersById = Maps.newHashMap();
        for (I_CmsFormatterBean formatter : formatterCacheState.getFormattersForType(typeName, true)) {
            externalFormattersById.put(new CmsUUID(formatter.getId()), formatter);
        }
        applyAllFormatterChanges(externalFormattersById, formatterCacheState);
        for (I_CmsFormatterBean formatter : externalFormattersById.values()) {
            if (formatter.getResourceTypeNames().contains(typeName)) {
                formatters.add(formatter);
            }
        }
        return CmsFormatterConfiguration.create(cms, formatters);
    }

    /**
     * Gets the formatters from the schema.<p>
     *
     * @param cms the current CMS context
     * @param res the resource for which the formatters should be retrieved
     *
     * @return the formatters from the schema
     */
    protected CmsFormatterConfiguration getFormattersFromSchema(CmsObject cms, CmsResource res) {

        try {
            return OpenCms.getResourceManager().getResourceType(res.getTypeId()).getFormattersForResource(cms, res);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
        }
    }

    /**
     * Internal method for getting the function references.<p>
     *
     * @return the function references
     */
    protected List<CmsFunctionReference> internalGetFunctionReferences() {

        CmsADEConfigData parentData = parent();
        if ((parentData == null)) {
            if (m_data.isModuleConfig()) {
                return Collections.unmodifiableList(m_data.getFunctionReferences());
            } else {
                return Lists.newArrayList();
            }
        } else {
            return parentData.internalGetFunctionReferences();

        }
    }

    /**
     * Helper method for getting the list of resource types.<p>
     *
     * @param filterDisabled true if disabled types should be filtered from the result
     *
     * @return the list of resource types
     */
    protected List<CmsResourceTypeConfig> internalGetResourceTypes(boolean filterDisabled) {

        CmsADEConfigData parentData = parent();
        List<CmsResourceTypeConfig> parentResourceTypes = null;
        if (parentData == null) {
            parentResourceTypes = Lists.newArrayList();
        } else {
            parentResourceTypes = Lists.newArrayList();
            for (CmsResourceTypeConfig typeConfig : parentData.internalGetResourceTypes(false)) {
                CmsResourceTypeConfig copiedType = typeConfig.copy(m_data.isDiscardInheritedTypes());
                parentResourceTypes.add(copiedType);
            }
        }
        List<CmsResourceTypeConfig> result = combineConfigurationElements(
            parentResourceTypes,
            m_data.getOwnResourceTypes(),
            true);
        if (m_data.isCreateContentsLocally()) {
            for (CmsResourceTypeConfig typeConfig : result) {
                typeConfig.updateBasePath(
                    CmsStringUtil.joinPaths(m_data.getBasePath(), CmsADEManager.CONTENT_FOLDER_NAME));
            }
        }
        if (filterDisabled) {
            Iterator<CmsResourceTypeConfig> iter = result.iterator();
            while (iter.hasNext()) {
                CmsResourceTypeConfig typeConfig = iter.next();
                if (typeConfig.isDisabled()) {
                    iter.remove();
                }
            }
        }
        return result;
    }

    /**
     * Merges two lists of detail pages, one from a parent configuration and one from a child configuration.<p>
     *
     * @param parentDetailPages the parent's detail pages
     * @param ownDetailPages the child's detail pages
     *
     * @return the merged detail pages
     */
    protected List<CmsDetailPageInfo> mergeDetailPages(
        List<CmsDetailPageInfo> parentDetailPages,
        List<CmsDetailPageInfo> ownDetailPages) {

        List<CmsDetailPageInfo> parentDetailPageCopies = Lists.newArrayList();
        for (CmsDetailPageInfo info : parentDetailPages) {
            parentDetailPageCopies.add(info.copyAsInherited());
        }

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        Map<String, List<CmsDetailPageInfo>> resultDetailPageMap = Maps.newHashMap();
        resultDetailPageMap.putAll(getDetailPagesMap(parentDetailPageCopies));
        resultDetailPageMap.putAll(getDetailPagesMap(ownDetailPages));
        result = new ArrayList<CmsDetailPageInfo>();
        for (List<CmsDetailPageInfo> pages : resultDetailPageMap.values()) {
            result.addAll(pages);
        }
        return result;
    }

    /**
     * Helper method to correct paths in detail page beans if the corresponding resources have been moved.<p>
     *
     * @param detailPages the original list of detail pages
     *
     * @return the corrected list of detail pages
     */
    protected List<CmsDetailPageInfo> updateUris(List<CmsDetailPageInfo> detailPages) {

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        for (CmsDetailPageInfo page : detailPages) {
            CmsUUID structureId = page.getId();
            try {
                String rootPath = OpenCms.getADEManager().getRootPath(
                    structureId,
                    getCms().getRequestContext().getCurrentProject().isOnlineProject());
                CmsDetailPageInfo correctedPage = new CmsDetailPageInfo(structureId, rootPath, page.getType());
                result.add(page.isInherited() ? correctedPage.copyAsInherited() : correctedPage);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }
}
