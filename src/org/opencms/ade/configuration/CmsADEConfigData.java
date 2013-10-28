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
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A class which represents the accessible configuration data at a given point in a sitemap.<p>
 */
public class CmsADEConfigData {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEConfigData.class);

    /** The "create contents locally" flag. */
    protected boolean m_createContentsLocally;

    /** Should inherited model pages be discarded? */
    protected boolean m_discardInheritedModelPages;

    /** Should inherited properties be discard? */
    protected boolean m_discardInheritedProperties;

    /** Should inherited types be discarded? */
    protected boolean m_discardInheritedTypes;

    /** The base path of this configuration. */
    private String m_basePath;

    /** The cms context used for reading the configuration data. */
    private CmsObject m_cms;

    /** The configured formatter changes. */
    private CmsFormatterChangeSet m_formatterChangeSet;

    /** The list of configured function references. */
    private List<CmsFunctionReference> m_functionReferences = new ArrayList<CmsFunctionReference>();

    /** A flag which keeps track of whether this instance has already been initialized. */
    private boolean m_initialized;

    /** True if this is a module configuration, not a normal sitemap configuration. */
    private boolean m_isModuleConfig;

    /** The internal detail page configuration. */
    private List<CmsDetailPageInfo> m_ownDetailPages = new ArrayList<CmsDetailPageInfo>();

    /** The internal model page entries. */
    private List<CmsModelPageConfig> m_ownModelPageConfig = new ArrayList<CmsModelPageConfig>();

    /** The internal property configuration. */
    private List<CmsPropertyConfig> m_ownPropertyConfigurations = new ArrayList<CmsPropertyConfig>();

    /** The internal resource type entries. */
    private List<CmsResourceTypeConfig> m_ownResourceTypes = new ArrayList<CmsResourceTypeConfig>();

    /** The resource from which the configuration data was read. */
    private CmsResource m_resource;

    /** 
     * Default constructor to create an empty configuration.<p> 
     */
    public CmsADEConfigData() {

        // do nothing 
    }

    /**
     * Creates an empty configuration data object with a given base path.<p>
     * 
     * @param basePath the base path 
     */
    public CmsADEConfigData(String basePath) {

        m_basePath = basePath;
    }

    /**
     * Creates a new configuration data instance.<p>
     * 
     * @param basePath the base path 
     * @param resourceTypeConfig the resource type configuration
     * @param discardInheritedTypes the "discard inherited types" flag  
     * @param propertyConfig the property configuration
     * @param discardInheritedProperties the "discard inherited properties" flag  
     * @param detailPageInfos the detail page configuration
     * @param modelPages the model page configuration
     * @param functionReferences the function reference configuration 
     * @param discardInheritedModelPages the "discard  inherited model pages" flag 
     * @param createContentsLocally the "create contents locally" flag 
     * @param formatterChangeSet the formatter changes 
     */
    public CmsADEConfigData(
        String basePath,
        List<CmsResourceTypeConfig> resourceTypeConfig,
        boolean discardInheritedTypes,
        List<CmsPropertyConfig> propertyConfig,
        boolean discardInheritedProperties,
        List<CmsDetailPageInfo> detailPageInfos,
        List<CmsModelPageConfig> modelPages,
        List<CmsFunctionReference> functionReferences,
        boolean discardInheritedModelPages,
        boolean createContentsLocally,
        CmsFormatterChangeSet formatterChangeSet) {

        m_basePath = basePath;
        m_ownResourceTypes = resourceTypeConfig;
        m_ownPropertyConfigurations = propertyConfig;
        m_ownModelPageConfig = modelPages;
        m_ownDetailPages = detailPageInfos;
        m_functionReferences = functionReferences;

        m_discardInheritedTypes = discardInheritedTypes;
        m_discardInheritedProperties = discardInheritedProperties;
        m_discardInheritedModelPages = discardInheritedModelPages;
        m_createContentsLocally = createContentsLocally;
        m_formatterChangeSet = formatterChangeSet;
    }

    /**
     * Creates an empty configuration for a given base path.<p>
     * 
     * @param basePath the base path 
     * 
     * @return the empty configuration object 
     */
    public static CmsADEConfigData emptyConfiguration(String basePath) {

        return new CmsADEConfigData(basePath);
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
     * @return the merged configuration object list 
     */
    protected static <C extends I_CmsConfigurationObject<C>> List<C> combineConfigurationElements(
        List<C> parentConfigs,
        List<C> childConfigs) {

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
            if (child.isDisabled()) {
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

        CmsFormatterConfigurationCacheState cacheState = OpenCms.getADEManager().getCachedFormatters(
            m_cms.getRequestContext().getCurrentProject().isOnlineProject());
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

        checkInitialized();
        CmsADEConfigData parentData = parent();
        List<CmsDetailPageInfo> parentDetailPages;
        if (parentData != null) {
            parentDetailPages = parentData.getAllDetailPages(false);
        } else {
            parentDetailPages = Collections.emptyList();
        }
        List<CmsDetailPageInfo> result = mergeDetailPages(parentDetailPages, m_ownDetailPages);
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

        checkInitialized();
        return m_basePath;
    }

    /**
     * Gets the content folder path.<p>
     * 
     * For example, if the configuration file is located at /sites/default/.content/.config, the content folder path is /sites/default/.content
     * 
     * @return the content folder path 
     */
    public String getContentFolderPath() {

        return CmsStringUtil.joinPaths(m_basePath, CmsADEManager.CONTENT_FOLDER_NAME);

    }

    /**
     * Returns a list of the creatable resource types.<p>
     * 
     * @param cms the CMS context used to check whether the resource types are creatable 
     * @return the list of creatable resource type 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsResourceTypeConfig> getCreatableTypes(CmsObject cms) throws CmsException {

        checkInitialized();
        List<CmsResourceTypeConfig> result = new ArrayList<CmsResourceTypeConfig>();
        for (CmsResourceTypeConfig typeConfig : getResourceTypes()) {
            if (typeConfig.checkCreatable(cms)) {
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

        checkInitialized();
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
     * Returns the formatter change sets for this and all parent sitemaps, ordered by increasing folder depth of the sitemap.<p>
     * 
     * @return the formatter change sets for all ancestor sitemaps 
     */
    public List<CmsFormatterChangeSet> getFormatterChangeSets() {

        CmsADEConfigData currentConfig = this;
        List<CmsFormatterChangeSet> result = Lists.newArrayList();
        while (currentConfig != null) {
            CmsFormatterChangeSet changes = currentConfig.getOwnFormatterChangeSet();
            result.add(changes);
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

        int resTypeId = res.getTypeId();
        try {
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resTypeId);
            String typeName = resType.getTypeName();
            CmsFormatterConfigurationCacheState formatterCacheState = OpenCms.getADEManager().getCachedFormatters(
                cms.getRequestContext().getCurrentProject().isOnlineProject());
            CmsFormatterConfiguration schemaFormatters = getFormattersFromSchema(cms, res);
            List<I_CmsFormatterBean> formatters = new ArrayList<I_CmsFormatterBean>();
            Set<String> types = new HashSet<String>();
            types.add(typeName);
            for (CmsFormatterChangeSet changeSet : getFormatterChangeSets()) {
                changeSet.applyToTypes(types);
            }
            if (!types.isEmpty()) {
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
                if (typeName.equals(formatter.getResourceTypeName())) {
                    formatters.add(formatter);
                }
            }
            return CmsFormatterConfiguration.create(cms, formatters);
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

        CmsFormatterConfigurationCacheState cacheState = OpenCms.getADEManager().getCachedFormatters(
            m_cms.getRequestContext().getCurrentProject().isOnlineProject());
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

        CmsADEConfigData parentData = parent();
        List<CmsModelPageConfig> parentModelPages;
        if ((parentData != null) && !m_discardInheritedModelPages) {
            parentModelPages = parentData.getModelPages();
        } else {
            parentModelPages = Collections.emptyList();
        }

        List<CmsModelPageConfig> result = combineConfigurationElements(parentModelPages, m_ownModelPageConfig);
        return result;
    }

    /**
     * Gets the formatter changes for this sitemap configuration.<p>
     * 
     * @return the formatter change set 
     */
    public CmsFormatterChangeSet getOwnFormatterChangeSet() {

        return m_formatterChangeSet;
    }

    /**
     * Gets the configuration for the available properties.<p>
     * 
     * @return the configuration for the available properties
     */
    public List<CmsPropertyConfig> getPropertyConfiguration() {

        CmsADEConfigData parentData = parent();
        List<CmsPropertyConfig> parentProperties;
        if ((parentData != null) && !m_discardInheritedProperties) {
            parentProperties = parentData.getPropertyConfiguration();
        } else {
            parentProperties = Collections.emptyList();
        }
        List<CmsPropertyConfig> result = combineConfigurationElements(parentProperties, m_ownPropertyConfigurations);
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

        return m_resource;
    }

    /** 
     * Returns the configuration for a specific resource type.<p>
     * 
     * @param typeName the name of the type 
     * 
     * @return the resource type configuration for that type 
     */
    public CmsResourceTypeConfig getResourceType(String typeName) {

        checkInitialized();
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

        List<CmsResourceTypeConfig> result = internalGetResourceTypes();
        for (CmsResourceTypeConfig config : result) {
            config.initialize(m_cms);
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
                try {
                    CmsXmlContentDefinition contentDef = CmsXmlContentDefinition.getContentDefinitionForType(
                        m_cms,
                        type.getTypeName());
                    if (contentDef.getContentHandler().hasModifiableFormatters()) {
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
     * Initializes the configuration object.<p>
     * 
     * @param cms the CMS context to be used for VFS operations  
     */
    public void initialize(CmsObject cms) {

        m_cms = cms;
        m_initialized = true;
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

        return m_createContentsLocally;
    }

    /**
     * Returns the value of the "discard inherited model pages" flag.<p>
     * 
     * If this flag is set, inherited model pages will be discarded for this sitemap.<p>
     * 
     * @return the "discard inherited model pages" flag 
     */
    public boolean isDiscardInheritedModelPages() {

        return m_discardInheritedModelPages;
    }

    /**
     * Returns the value of the "discard inherited properties" flag.<p>
     * 
     * If this is flag is set, inherited property definitions will be discarded for this sitemap.<p>
     * 
     * @return the "discard inherited properties" flag.<p>
     */
    public boolean isDiscardInheritedProperties() {

        return m_discardInheritedProperties;
    }

    /**
     * Returns the value of the "discard inherited types" flag.<p>
     * 
     * If this flag is set, inherited resource types from a super-sitemap will be discarded for this sitemap.<p>
     * 
     * @return the "discard inherited types" flag 
     */
    public boolean isDiscardInheritedTypes() {

        return m_discardInheritedTypes;
    }

    /**
     * Returns true if this is a module configuration instead of a normal sitemap configuration.<p>
     * 
     * @return true if this is a module configuration 
     */
    public boolean isModuleConfiguration() {

        return m_isModuleConfig;
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

        if (m_basePath == null) {
            return null;
        }
        String parentPath = CmsResource.getParentFolder(m_basePath);
        if (OpenCms.getADEManager() == null) {
            return null;
        }
        CmsADEConfigData result = OpenCms.getADEManager().internalLookupConfiguration(m_cms, parentPath);
        return result;
    }

    /**
     * Sets the "module configuration" flag.<p>
     * 
     * @param isModuleConfig true if this configuration should be marked as a module configuration 
     */
    public void setIsModuleConfig(boolean isModuleConfig) {

        checkNotInitialized();
        m_isModuleConfig = isModuleConfig;
    }

    /**
     * Sets the configuration file resource.<p>
     * 
     * @param resource the configuration file resource 
     */
    public void setResource(CmsResource resource) {

        checkNotInitialized();
        m_resource = resource;
    }

    /**
     * Checks whether the configuration is initialized and throws an error otherwise.<p>
     */
    protected void checkInitialized() {

        if (!m_initialized) {
            throw new IllegalStateException();
        }
    }

    /**
     * Checks whether the configuration is *NOT* initialized and throws an error otherwise.<p>
     */
    protected void checkNotInitialized() {

        if (m_initialized) {
            throw new IllegalStateException();
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
            if (!m_cms.existsResource(contentFolder)) {
                m_cms.createResource(
                    contentFolder,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()).getTypeId());
            }
        }
    }

    /**
     * Gets the CMS object used for VFS operations.<p>
     * 
     * @return the CMS object 
     */
    protected CmsObject getCmsObject() {

        return m_cms;
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
        CmsObject cms = OpenCms.initCmsObject(m_cms);
        if (m_isModuleConfig) {
            Set<String> siteRoots = OpenCms.getSiteManager().getSiteRoots();
            for (String siteRoot : siteRoots) {
                cms.getRequestContext().setSiteRoot(siteRoot);
                for (CmsResourceTypeConfig config : getResourceTypes()) {
                    String typeName = config.getTypeName();
                    String folderPath = config.getFolderPath(cms);
                    result.put(CmsStringUtil.joinPaths(folderPath, "/"), typeName);
                }
            }
        } else {
            for (CmsResourceTypeConfig config : getResourceTypes()) {
                String typeName = config.getTypeName();
                String folderPath = config.getFolderPath(m_cms);
                result.put(CmsStringUtil.joinPaths(folderPath, "/"), typeName);
            }
        }
        return result;
    }

    /**
     * Gets the formatter configuration for a given type.<p>
     * 
     * @param type the type for which to get the formatters 
     * 
     * @return the formatter configuration for that type 
     */
    protected CmsFormatterConfiguration getFormatters(String type) {

        CmsResourceTypeConfig typeConfig = getResourceType(type);
        if ((typeConfig == null)
            || (typeConfig.getFormatterConfiguration() == null)
            || (typeConfig.getFormatterConfiguration().getAllFormatters().isEmpty())) {
            try {
                CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.getContentDefinitionForType(
                    m_cms,
                    type);
                if (contentDefinition == null) {
                    return null;
                }
                return contentDefinition.getContentHandler().getFormatterConfiguration(m_cms, null);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }
        }
        return typeConfig.getFormatterConfiguration();
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

        checkInitialized();
        CmsADEConfigData parentData = parent();
        if ((parentData == null)) {
            if (m_isModuleConfig) {
                return Collections.unmodifiableList(m_functionReferences);
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
     * @return the list of resource types 
     */
    protected List<CmsResourceTypeConfig> internalGetResourceTypes() {

        checkInitialized();
        CmsADEConfigData parentData = parent();
        List<CmsResourceTypeConfig> parentResourceTypes = null;
        if ((parentData == null) || m_discardInheritedTypes) {
            parentResourceTypes = Lists.newArrayList();
        } else {
            parentResourceTypes = Lists.newArrayList();
            for (CmsResourceTypeConfig typeConfig : parentData.internalGetResourceTypes()) {
                parentResourceTypes.add(typeConfig.copy());
            }
        }
        List<CmsResourceTypeConfig> result = combineConfigurationElements(parentResourceTypes, m_ownResourceTypes);
        if (m_createContentsLocally) {
            for (CmsResourceTypeConfig typeConfig : result) {
                typeConfig.updateBasePath(CmsStringUtil.joinPaths(m_basePath, CmsADEManager.CONTENT_FOLDER_NAME));
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

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        Map<String, List<CmsDetailPageInfo>> resultDetailPageMap = Maps.newHashMap();
        resultDetailPageMap.putAll(getDetailPagesMap(parentDetailPages));
        resultDetailPageMap.putAll(getDetailPagesMap(ownDetailPages));
        result = new ArrayList<CmsDetailPageInfo>();
        for (List<CmsDetailPageInfo> pages : resultDetailPageMap.values()) {
            result.addAll(pages);
        }
        return result;
    }

    /** 
     * Merges the parent's data into this object.<p>
     * 
     * @param parent the parent configuration data 
     */
    protected void mergeParent(CmsADEConfigData parent) {

        List<CmsResourceTypeConfig> parentTypes = null;
        if (parent != null) {
            parentTypes = parent.m_ownResourceTypes;
        } else {
            parentTypes = Collections.emptyList();
        }

        List<CmsPropertyConfig> parentProperties = null;
        if (parent != null) {
            parentProperties = parent.m_ownPropertyConfigurations;
        } else {
            parentProperties = Collections.emptyList();
        }

        List<CmsModelPageConfig> parentModelPages = null;
        if (parent != null) {
            parentModelPages = parent.m_ownModelPageConfig;
        } else {
            parentModelPages = Collections.emptyList();
        }

        List<CmsFunctionReference> parentFunctionRefs = null;
        if (parent != null) {
            parentFunctionRefs = parent.m_functionReferences;
        } else {
            parentFunctionRefs = Collections.emptyList();
        }

        m_ownResourceTypes = combineConfigurationElements(parentTypes, m_ownResourceTypes);
        m_ownPropertyConfigurations = combineConfigurationElements(parentProperties, m_ownPropertyConfigurations);
        m_ownModelPageConfig = combineConfigurationElements(parentModelPages, m_ownModelPageConfig);
        m_functionReferences = combineConfigurationElements(parentFunctionRefs, m_functionReferences);
    }

    /**
     * Handle the ordering from the module configurations.<p>
     */
    protected void processModuleOrdering() {

        Collections.sort(m_ownResourceTypes, new Comparator<CmsResourceTypeConfig>() {

            public int compare(CmsResourceTypeConfig a, CmsResourceTypeConfig b) {

                return ComparisonChain.start().compare(a.getOrder(), b.getOrder()).compare(
                    a.getTypeName(),
                    b.getTypeName()).result();
            }
        });

        Collections.sort(m_ownPropertyConfigurations, new Comparator<CmsPropertyConfig>() {

            public int compare(CmsPropertyConfig a, CmsPropertyConfig b) {

                return ComparisonChain.start().compare(a.getOrder(), b.getOrder()).compare(a.getName(), b.getName()).result();
            }
        });

        Collections.sort(m_functionReferences, new Comparator<CmsFunctionReference>() {

            public int compare(CmsFunctionReference a, CmsFunctionReference b) {

                return ComparisonChain.start().compare(a.getOrder(), b.getOrder()).compare(a.getName(), b.getName()).result();
            }
        });
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
                    m_cms.getRequestContext().getCurrentProject().isOnlineProject());
                CmsDetailPageInfo correctedPage = new CmsDetailPageInfo(structureId, rootPath, page.getType());
                result.add(correctedPage);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }
}
