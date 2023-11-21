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

import org.opencms.ade.configuration.CmsConfigurationReader.DiscardPropertiesMode;
import org.opencms.ade.configuration.formatters.CmsFormatterChangeSet;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.galleries.CmsAddContentRestriction;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

/**
 * Represents a parsed sitemap or module configuration.<p>
 *
 * This is the internal representation stored in the cache. The configuration class
 * which is actually returned by CmsADEManager, and which contains most of the logic
 * related to sitemap configurations, is CmsADEConfigData.
 */
public class CmsADEConfigDataInternal {

    /**
     * Represents the value of an attribute, with additional information about where the value originated from.
     */
    public static class AttributeValue {

        /** The path of the configuration from which this attribute value originates. */
        private String m_origin;

        /** The value of the attribute. */
        private String m_value;

        /**
         * Creates a new instance.
         *
         * @param value the attribute value
         * @param origin the origin path
         */
        public AttributeValue(String value, String origin) {

            super();
            m_value = value;
            m_origin = origin;
        }

        /**
         * Gets the origin path.
         *
         * @return the origin path
         */
        public String getOrigin() {

            return m_origin;
        }

        /**
         * Gets the attribute string value.
         *
         * @return the attribute value
         */
        public String getValue() {

            return m_value;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return "[" + m_value + " (from: " + m_origin + ")]";
        }
    }

    /**
     * Represents a reference to a sitemap configuration with some associated metadata about that reference.
     */
    public static class ConfigReference {

        /** The id of the referenced configuration. */
        private CmsUUID m_config;

        /** The metadata associated with the reference. */
        private ConfigReferenceMeta m_meta = new ConfigReferenceMeta();

        /**
         * Creates a new instance.
         *
         * @param config the id of the target sitemap configuration
         */
        public ConfigReference(CmsUUID config) {

            m_config = config;
        }

        /**
         * Creates a new instance.
         *
         * @param config the id of the target sitemap configuration
         * @param meta the metadata associated with the reference
         */
        public ConfigReference(CmsUUID config, ConfigReferenceMeta meta) {

            m_config = config;
            m_meta = meta;

        }

        /**
         * Gets the id of the referenced sitemap configuration.
         *
         * @return the id of the referenced sitemap configuration
         */
        public CmsUUID getId() {

            return m_config;

        }

        /**
         * Gets the metadata for the configuration reference
         *
         * @return the metadata for the configuration reference
         */
        public ConfigReferenceMeta getMeta() {

            return m_meta;
        }

    }

    /**
     * Contains a sitemap configuration bean together with some metadata about how it was referenced from other sitemap configurations.
     */
    public static class ConfigReferenceInstance {

        /** The configuration object. */
        private CmsADEConfigDataInternal m_config;

        /** The metadata associated with the configuration reference. */
        private ConfigReferenceMeta m_meta = new ConfigReferenceMeta();

        /**
         * Creates a new instance.
         *
         * @param config the configuration
         */
        public ConfigReferenceInstance(CmsADEConfigDataInternal config) {

            m_config = config;
        }

        /**
         * Creates a new instance.
         *
         * @param config the configuration
         * @param meta the metadata associated with the reference to the configuration
         */
        public ConfigReferenceInstance(CmsADEConfigDataInternal config, ConfigReferenceMeta meta) {

            m_config = config;
            m_meta = meta;

        }

        /**
         * Gets the configuration instance.
         *
         * @return the configuration
         */
        public CmsADEConfigDataInternal getConfig() {

            return m_config;
        }

        /**
         * Gets the metadata associated with the configuration reference.
         *
         * @return the reference metadata
         */
        public ConfigReferenceMeta getMeta() {

            return m_meta;
        }

    }

    /**
     * Represents additional metadata from the query string of a master configuration link.
     */
    public static class ConfigReferenceMeta {

        /** The 'template' parameter. */
        public static final String PARAM_TEMPLATE = "template";

        /** The template identifier. */
        private String m_template;

        /**
         * Creates a new, empty instance.
         */
        public ConfigReferenceMeta() {
            // do nothing

        }

        /**
         * Creates a new instance from the parameters from a master configuration link.
         *
         * @param params the parameters for the metadata
         */
        public ConfigReferenceMeta(Map<String, String[]> params) {

            String[] templateVals = params.get(PARAM_TEMPLATE);
            if ((templateVals != null) && (templateVals.length > 0)) {
                m_template = templateVals[0];
            }
        }

        /**
         * If this object is the metadata for a link to a master configuration M, and 'next' is the metadata
         * for a link from M to some other master configuration N, combines the metadata into a single object and returns it.
         *
         * @param next the metadata to combine this object with
         * @return the combined metadata
         */
        public ConfigReferenceMeta combine(ConfigReferenceMeta next) {

            ConfigReferenceMeta result = new ConfigReferenceMeta();
            result.m_template = m_template != null ? m_template : next.m_template;
            return result;
        }

        /**
         * Gets the template identifier.
         *
         * <p>The template identifier should be one of the template context keys provided by a template provider.
         *
         * @return the template identifier
         */
        public String getTemplate() {

            return m_template;
        }

        /**
         * Returns true if 'remove all' settings should be ignored in the referenced master configuration.
         *
         * @return if true, 'remove all' settings are ignored in the referenced master configuration
         */
        public boolean isSkipRemovals() {

            return getTemplate() != null;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEConfigDataInternal.class);

    /** The "create contents locally" flag. */
    protected boolean m_createContentsLocally;

    /** Should inherited model pages be discarded? */
    protected boolean m_discardInheritedModelPages;

    /** Should inherited types be discarded? */
    protected boolean m_discardInheritedTypes;

    /** The 'discard properties' mode. */
    protected DiscardPropertiesMode m_discardPropertiesMode;

    /** The configured formatter changes. */
    protected CmsFormatterChangeSet m_formatterChangeSet = new CmsFormatterChangeSet();

    /** True if subsite should be included in site selector. */
    protected boolean m_includeInSiteSelector;

    /** True if this is a module configuration, not a normal sitemap configuration. */
    protected boolean m_isModuleConfig;

    /** The master configuration structure ids. */
    protected List<ConfigReference> m_masterConfigs;

    /** Mode for using formatter keys / the new container page format. */
    protected Boolean m_useFormatterKeys;

    /** The restrictions for the 'add content' dialog. */
    private CmsAddContentRestriction m_addContentRestriction = CmsAddContentRestriction.EMPTY;

    /** The set of ids of site plugins to add. */
    private Set<CmsUUID> m_addedPlugins;

    /** Structure id of the sitemap attribute editor configuration file. */
    private CmsUUID m_attributeEditorConfigId;

    /** The map of attributes. */
    private Map<String, AttributeValue> m_attributes = Collections.emptyMap();

    /** The base path of this configuration. */
    private String m_basePath;

    /** The CMS context. */
    private CmsObject m_cms;

    /** the dynamic functions available. */
    private Set<CmsUUID> m_dynamicFunctions;

    /** True if detail contents outside the sitemap should not be used with detail pages in the sitemap. */
    private boolean m_excludeExternalDetailContents;

    /** The list of configured function references. */
    private List<CmsFunctionReference> m_functionReferences = Lists.newArrayList();

    /** The functions to remove. */
    private Set<CmsUUID> m_functionsToRemove;

    /** The mode determining how to deal with disabled functions. */
    private CmsGalleryDisabledTypesMode m_galleryDisabledFunctionsMode;

    /** The display mode for deactivated types in the gallery dialog. */
    private CmsGalleryDisabledTypesMode m_galleryDisabledTypesMode;

    /** The internal detail page configuration. */
    private List<CmsDetailPageInfo> m_ownDetailPages = Lists.newArrayList();

    /** The internal model page entries. */
    private volatile List<CmsModelPageConfig> m_ownModelPageConfig = null;

    /** Model page data with no resources. */
    private List<CmsModelPageConfigWithoutResource> m_ownModelPageConfigRaw = new ArrayList<>();

    /** The internal property configuration. */
    private List<CmsPropertyConfig> m_ownPropertyConfigurations = Lists.newArrayList();

    /** The internal resource type entries. */
    private List<CmsResourceTypeConfig> m_ownResourceTypes = Lists.newArrayList();

    /** True if detail pages from this sitemap should be preferred when linking to contents inside this sitemap. */
    private boolean m_preferDetailPagesForLocalContents;

    /** Flag indicating whether all functions should be removed. */
    private boolean m_removeAllFunctions;

    /** If true, all site plugins inherited from parent sitemaps should be removed. */
    private boolean m_removeAllPlugins;

    /** The set of ids of site plugins to remove. */
    private Set<CmsUUID> m_removedPlugins;

    /** True if inherited shared setting overrides should be removed. */
    private boolean m_removeSharedSettingOverrides;

    /** The resource from which the configuration data was read. */
    private CmsResource m_resource;

    /** Shared setting override ID, may be null. */
    private CmsUUID m_sharedSettingOverride;

    /** The type ordering mode. */
    private CmsTypeOrderingMode m_typeOrderingMode;

    /**
     * Creates a new configuration data instance.<p>
     *
     * @param cms the CMS context
     * @param resource the resource from which this configuration data was read
     * @param isModuleConfig true if this is a module configuration
     * @param basePath the base path
     * @param masterConfigs structure ids of master configuration files
     * @param resourceTypeConfig the resource type configuration
     * @param galleryDisabledTypesMode  the display mode deactivated types in the gallery dialog
     * @param galleryDisabledFunctionsMode the mode controlling how to deal with disabled functions
     * @param discardInheritedTypes the "discard inherited types" flag
     * @param propertyConfig the property configuration
     * @param discardPropertiesMode the "discard inherited properties" mode
     * @param detailPageInfos the detail page configuration
     * @param modelPages the model page configuration
     * @param functionReferences the function reference configuration
     * @param discardInheritedModelPages the "discard  inherited model pages" flag
     * @param createContentsLocally the "create contents locally" flag
     * @param preferDetailPagesForLocalContents the "preferDetailPagesForLocalContents" flag
     * @param excludeExternalDetailContents the "excludeExternalDetailContents" flag
     * @param includeInSiteSelector the "includeInSiteSelector" flag
     * @param formatterChangeSet the formatter changes
     * @param removeAllFunctions flag indicating whether all functions should be removed
     * @param functionIds the dynamic functions available
     * @param functionsToRemove the function ids to remove
     * @param removeAllPlugins true all site plugins should be removed
     * @param removedPlugins the ids of site plugins to remove
     * @param addedPlugins the ids of site plugins to add
     * @param useFormatterKeys mode for using formatter keys / the new container page format
     * @param orderingMode the mode used to order the resource types
     * @param restriction the restrictions for the 'Add content' dialog
     * @param sharedSettingOverride shared setting override id, may be null
     * @param removeSharedSettingOverrides true if inherited shared setting overrides should be removed
     * @param attributeEditorConfigId the structure id of the attribute editor configuration file
     * @param attributes the map of attributes
     */
    public CmsADEConfigDataInternal(
        CmsObject cms,
        CmsResource resource,
        boolean isModuleConfig,
        String basePath,
        List<ConfigReference> masterConfigs,
        List<CmsResourceTypeConfig> resourceTypeConfig,
        CmsGalleryDisabledTypesMode galleryDisabledTypesMode,
        CmsGalleryDisabledTypesMode galleryDisabledFunctionsMode,
        boolean discardInheritedTypes,
        List<CmsPropertyConfig> propertyConfig,
        DiscardPropertiesMode discardPropertiesMode,
        List<CmsDetailPageInfo> detailPageInfos,
        List<CmsModelPageConfigWithoutResource> modelPages,
        List<CmsFunctionReference> functionReferences,
        boolean discardInheritedModelPages,
        boolean createContentsLocally,
        boolean preferDetailPagesForLocalContents,
        boolean excludeExternalDetailContents,
        boolean includeInSiteSelector,
        CmsFormatterChangeSet formatterChangeSet,
        boolean removeAllFunctions,
        Set<CmsUUID> functionIds,
        Set<CmsUUID> functionsToRemove,
        boolean removeAllPlugins,
        Set<CmsUUID> addedPlugins,
        Set<CmsUUID> removedPlugins,
        Boolean useFormatterKeys,
        CmsTypeOrderingMode orderingMode,
        CmsAddContentRestriction restriction,
        CmsUUID sharedSettingOverride,
        boolean removeSharedSettingOverrides,
        CmsUUID attributeEditorConfigId,
        Map<String, String> attributes) {

        m_cms = cms;
        m_resource = resource;
        m_basePath = basePath;
        m_ownResourceTypes = resourceTypeConfig;
        m_galleryDisabledTypesMode = galleryDisabledTypesMode;
        m_galleryDisabledFunctionsMode = galleryDisabledFunctionsMode;
        m_ownPropertyConfigurations = propertyConfig;
        m_ownModelPageConfigRaw = modelPages;
        m_ownDetailPages = detailPageInfos;
        m_functionReferences = functionReferences;
        m_isModuleConfig = isModuleConfig;
        m_masterConfigs = masterConfigs;
        if (m_masterConfigs == null) {
            m_masterConfigs = Collections.emptyList();
        }

        m_discardInheritedTypes = discardInheritedTypes;
        m_discardPropertiesMode = discardPropertiesMode;
        m_discardInheritedModelPages = discardInheritedModelPages;
        m_createContentsLocally = createContentsLocally;
        m_preferDetailPagesForLocalContents = preferDetailPagesForLocalContents;
        m_formatterChangeSet = formatterChangeSet;
        m_formatterChangeSet.setDebugPath(m_basePath);
        m_dynamicFunctions = functionIds;
        m_functionsToRemove = functionsToRemove;
        m_removeAllFunctions = removeAllFunctions;
        m_excludeExternalDetailContents = excludeExternalDetailContents;
        m_includeInSiteSelector = includeInSiteSelector;
        m_useFormatterKeys = useFormatterKeys;
        m_removeAllPlugins = removeAllPlugins;
        m_addedPlugins = Collections.unmodifiableSet(addedPlugins);
        m_removedPlugins = Collections.unmodifiableSet(removedPlugins);
        m_sharedSettingOverride = sharedSettingOverride;
        m_removeSharedSettingOverrides = removeSharedSettingOverrides;
        Map<String, AttributeValue> attributeObjects = new HashMap<>();
        String attributeOrigin = basePath;
        if (resource != null) {
            attributeOrigin = resource.getRootPath();
        }
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            attributeObjects.put(entry.getKey(), new AttributeValue(entry.getValue(), attributeOrigin));
        }
        m_attributes = Collections.unmodifiableMap(new HashMap<>(attributeObjects));

        m_typeOrderingMode = orderingMode;
        m_addContentRestriction = restriction;
        m_attributeEditorConfigId = attributeEditorConfigId;
    }

    /**
     * Creates an empty configuration data object with a given base path.<p>
     *
     * @param basePath the base path
     */
    public CmsADEConfigDataInternal(String basePath) {

        m_basePath = basePath;
    }

    /**
     * Creates a new configuration data instance.<p>

     * @param resource the resource from which this configuration data was read
     * @param isModuleConfig true if this is a module configuration
     * @param basePath the base path
     * @param masterConfigs structure ids of master configuration files
     * @param resourceTypeConfig the resource type configuration
     * @param discardInheritedTypes the "discard inherited types" flag
     * @param propertyConfig the property configuration
     * @param discardPropertiesMode the "discard inherited properties" mode
     * @param detailPageInfos the detail page configuration
     * @param modelPages the model page configuration
     * @param functionReferences the function reference configuration
     * @param discardInheritedModelPages the "discard  inherited model pages" flag
     * @param createContentsLocally the "create contents locally" flag
     * @param preferDetailPagesForLocalContents the "preferDetailPagesForLocalContents" flag
     * @param excludeExternalDetailContents the "excludeExternalDetailContents" flag
     * @param includeInSiteSelector the "includeInSiteSelector" flag
     * @param formatterChangeSet the formatter changes
     * @param removeAllFunctions flag indicating whether all functions should be removed
     * @param functionIds the dynamic functions available
     */
    protected CmsADEConfigDataInternal(
        CmsResource resource,
        boolean isModuleConfig,
        String basePath,
        List<ConfigReference> masterConfigs,
        List<CmsResourceTypeConfig> resourceTypeConfig,
        boolean discardInheritedTypes,
        List<CmsPropertyConfig> propertyConfig,
        DiscardPropertiesMode discardPropertiesMode,
        List<CmsDetailPageInfo> detailPageInfos,
        List<CmsModelPageConfig> modelPages,
        List<CmsFunctionReference> functionReferences,
        boolean discardInheritedModelPages,
        boolean createContentsLocally,
        boolean preferDetailPagesForLocalContents,
        boolean excludeExternalDetailContents,
        boolean includeInSiteSelector,
        CmsFormatterChangeSet formatterChangeSet,
        boolean removeAllFunctions,
        Set<CmsUUID> functionIds) {

        m_resource = resource;
        m_basePath = basePath;
        m_ownResourceTypes = resourceTypeConfig;
        m_ownPropertyConfigurations = propertyConfig;
        m_ownModelPageConfig = modelPages;
        m_ownDetailPages = detailPageInfos;
        m_functionReferences = functionReferences;
        m_isModuleConfig = isModuleConfig;
        m_masterConfigs = masterConfigs;
        if (m_masterConfigs == null) {
            m_masterConfigs = Collections.emptyList();
        }

        m_discardInheritedTypes = discardInheritedTypes;
        m_discardPropertiesMode = discardPropertiesMode;
        m_discardInheritedModelPages = discardInheritedModelPages;
        m_createContentsLocally = createContentsLocally;
        m_preferDetailPagesForLocalContents = preferDetailPagesForLocalContents;
        m_formatterChangeSet = formatterChangeSet;
        m_dynamicFunctions = functionIds;
        m_removeAllFunctions = removeAllFunctions;
        m_excludeExternalDetailContents = excludeExternalDetailContents;
        m_includeInSiteSelector = includeInSiteSelector;
    }

    /**
     * Creates an empty configuration for a given base path.<p>
     *
     * @param basePath the base path
     *
     * @return the empty configuration object
     */
    public static CmsADEConfigDataInternal emptyConfiguration(String basePath) {

        return new CmsADEConfigDataInternal(basePath);
    }

    /**
     * Gets the restrictions for the 'Add content' dialog.
     *
     * @return the restrictions for the 'Add content' dialog
     */
    public CmsAddContentRestriction getAddContentRestriction() {

        return m_addContentRestriction;
    }

    /**
     * Gets the set of ids of added site plugins.
     *
     * @return the set of ids of added site plugins
     */
    public Set<CmsUUID> getAddedPlugins() {

        return m_addedPlugins;
    }

    /**
     * Gets the structure id of the sitemap attribute editor configuration
     * @return the structure id of the sitemap attribute editor configuration
     */
    public CmsUUID getAttributeEditorConfigId() {

        return m_attributeEditorConfigId;
    }

    /**
     * Gets the map of attributes for this sitemap configuration.
     *
     * @return the map of attributes
     */
    public Map<String, AttributeValue> getAttributes() {

        return m_attributes;
    }

    /**
     * Gets the base path.<p>
     *
     * @return the base path
     */
    public String getBasePath() {

        return m_basePath;
    }

    /**
     * Gets the display mode for deactivated types in the sitemap dialog.
     *
     * @return the display mode for deactivated types
     */
    public CmsGalleryDisabledTypesMode getDisabledTypeMode() {

        return m_galleryDisabledTypesMode;
    }

    /**
     * Gets the 'discard properties' mode.
     *
     * @return the discard properties mode
     */
    public DiscardPropertiesMode getDiscardPropertiesMode() {

        return m_discardPropertiesMode;
    }

    /**
     * Returns the set of configured dynamic functions, regardless of whether the 'remove all formatters' option is enabled.
     *
     * @return the dynamic functions
     */
    public Collection<CmsUUID> getDynamicFunctions() {

        if (m_dynamicFunctions == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(m_dynamicFunctions);
    }

    /**
     * Gets the formatter change set.<p>
     *
     * @return the formatter change set.<p>
     */
    public CmsFormatterChangeSet getFormatterChangeSet() {

        return m_formatterChangeSet;

    }

    /**
     * Gets the dynamic function references.<p>
     *
     * @return the dynamic function references
     */
    public List<CmsFunctionReference> getFunctionReferences() {

        return m_functionReferences;
    }

    /**
     * Gets the ids of dynamic functions to remove.
     *
     * @return the ids of dynamic functions to remove
     */
    public Collection<CmsUUID> getFunctionsToRemove() {

        return m_functionsToRemove;
    }

    /**
     * Gets the mode for how disabled functions should be handled.
     *
     * @return the mode for disabled functions
     */
    public CmsGalleryDisabledTypesMode getGalleryDisabledFunctionsMode() {

        return m_galleryDisabledFunctionsMode;
    }

    /**
     * Gets the structure ids of the master configuration files.
     *
     * @return the structure ids of the master configurations
     */
    public List<ConfigReference> getMasterConfigs() {

        return Collections.unmodifiableList(m_masterConfigs);
    }

    /**
     * Returns the ownDetailPages.<p>
     *
     * @return the ownDetailPages
     */
    public List<CmsDetailPageInfo> getOwnDetailPages() {

        return m_ownDetailPages;
    }

    /**
     * Returns the ownModelPageConfig.<p>
     *
     * @return the ownModelPageConfig
     */
    public List<CmsModelPageConfig> getOwnModelPageConfig() {

        if (m_ownModelPageConfig == null) {
            List<CmsModelPageConfig> result = new ArrayList<>();
            for (CmsModelPageConfigWithoutResource modelPage : m_ownModelPageConfigRaw) {
                try {
                    CmsResource resource = m_cms.readResource(modelPage.getStructureId());
                    result.add(new CmsModelPageConfig(resource, modelPage.isDefault(), modelPage.isDisabled()));
                } catch (Exception e) {
                    LOG.warn("can't read model page for base path " + m_basePath + ": " + e.getLocalizedMessage(), e);
                }
            }
            m_ownModelPageConfig = result;
            return result;
        } else {
            return m_ownModelPageConfig;
        }
    }

    /**
     * Returns the ownPropertyConfigurations.<p>
     *
     * @return the ownPropertyConfigurations
     */
    public List<CmsPropertyConfig> getOwnPropertyConfigurations() {

        return m_ownPropertyConfigurations;
    }

    /**
     * Gets the resource types defined in this configuration.<p>
     *
     * @return the resource type configurations
     */
    public List<CmsResourceTypeConfig> getOwnResourceTypes() {

        return m_ownResourceTypes;
    }

    /**
     * Gets the set of ids of removed site plugins.
     *
     * @return the set of ids of removed site plugins
     */
    public Set<CmsUUID> getRemovedPlugins() {

        return m_removedPlugins;
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Gets the shared setting override ID (may be null).
     *
     * @return the shared setting override ID
     */
    public CmsUUID getSharedSettingOverride() {

        return m_sharedSettingOverride;
    }

    /**
     * Gets the type ordering mode.
     *
     * @return the type ordering mode
     */
    public CmsTypeOrderingMode getTypeOrderingMode() {

        return m_typeOrderingMode;
    }

    /**
     * Gets the 'use formatter keys' mode.<p>
     *
     * If true, container pages will be written in the new format, including using formatter keys when possible.
     *
     * @return the 'use formatter keys' mode
     */
    public Boolean getUseFormatterKeys() {

        return m_useFormatterKeys;
    }

    /**
     * Returns true if contents should be created in the sub-sitemap.<p>
     *
     * @return true if contents should be created in the sub-sitemap
     */
    public boolean isCreateContentsLocally() {

        return m_createContentsLocally;
    }

    /**
     * Returns true if inherited model pages should be discarded.<p>
     *
     * @return true if inherited model pages should be discarded.
     */
    public boolean isDiscardInheritedModelPages() {

        return m_discardInheritedModelPages;
    }

    /**
     * Returns true if inherited properties should be discarded.<p>
     *
     * @return true if inherited property configurations should be discardded.<p>
     */
    public boolean isDiscardInheritedProperties() {

        return m_discardPropertiesMode != DiscardPropertiesMode.keep;
    }

    /**
     * Returns true if inherited types should be discarded.<p>
     *
     * @return true if inherited types should be discarded
     */
    public boolean isDiscardInheritedTypes() {

        return m_discardInheritedTypes;
    }

    /**
     * Returns true if detail pages inside this subsite (and descendant subsites) should not be used for contents outside the subsite (and descendant subsites).<p>
     *
     * @return true if external detail contents should be excluded
     */
    public boolean isExcludeExternalDetailContents() {

        return m_excludeExternalDetailContents;
    }

    /**
     * Returns true if the subsite should be included in the site selector.
     *
     * @return true if the subsite should be included in the site selector
     */
    public boolean isIncludeInSiteSelector() {

        return m_includeInSiteSelector;
    }

    /**
     * Returns the isModuleConfig.<p>
     *
     * @return the isModuleConfig
     */
    public boolean isModuleConfig() {

        return m_isModuleConfig;
    }

    /**
     * Returns true if detail pages from this sitemap should be preferred for creating links to detail contents located inside this sitemap.<p>
     *
     * @return true if detail pages from this sitemap should be preferred
     */
    public boolean isPreferDetailPagesForLocalContents() {

        return m_preferDetailPagesForLocalContents;
    }

    /**
     * True if all functions should be removed by this sitemap configuration.
     *
     * @return true if all functions should be removed
     */
    public boolean isRemoveAllFunctions() {

        return m_removeAllFunctions;
    }

    /**
     * Returns true if all site plugins inherited from parent sitemaps should be removed.
     *
     * @return true if all site plugins should be removed
     */
    public boolean isRemoveAllPlugins() {

        return m_removeAllPlugins;
    }

    /**
     * Returns true if shared setting overrides inherited from other sitemap configurations should be discarded.
     *
     * @return true if inherited shared setting overrides should be discarded
     */
    public boolean isRemoveSharedSettingOverrides() {

        return m_removeSharedSettingOverrides;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        if (getBasePath() != null) {
            return "[" + getBasePath() + "]";
        } else {
            return super.toString();
        }

    }

    /**
     * Merges the parent's data into this object.<p>
     *
     * @param parent the parent configuration data
     */
    protected void mergeParent(CmsADEConfigDataInternal parent) {

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

        m_ownResourceTypes = CmsADEConfigData.combineConfigurationElements(parentTypes, m_ownResourceTypes, false);
        m_ownPropertyConfigurations = CmsADEConfigData.combineConfigurationElements(
            parentProperties,
            m_ownPropertyConfigurations,
            false);
        m_ownModelPageConfig = CmsADEConfigData.combineConfigurationElements(
            parentModelPages,
            m_ownModelPageConfig,
            false);
        m_functionReferences = CmsADEConfigData.combineConfigurationElements(
            parentFunctionRefs,
            m_functionReferences,
            false);

        // dynamic functions are not used in module configurations, so we do not need to merge them here
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

                return ComparisonChain.start().compare(a.getOrder(), b.getOrder()).compare(
                    a.getName(),
                    b.getName()).result();
            }
        });

        Collections.sort(m_functionReferences, new Comparator<CmsFunctionReference>() {

            public int compare(CmsFunctionReference a, CmsFunctionReference b) {

                return ComparisonChain.start().compare(a.getOrder(), b.getOrder()).compare(
                    a.getName(),
                    b.getName()).result();
            }
        });
    }

}