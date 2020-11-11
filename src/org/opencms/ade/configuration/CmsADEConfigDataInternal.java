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
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
    protected List<CmsUUID> m_masterConfigs;

    /** The base path of this configuration. */
    private String m_basePath;

    /** the dynamic functions available. */
    private Set<CmsUUID> m_dynamicFunctions;

    /** True if detail contents outside the sitemap should not be used with detail pages in the sitemap. */
    private boolean m_excludeExternalDetailContents;

    /** The list of configured function references. */
    private List<CmsFunctionReference> m_functionReferences = Lists.newArrayList();

    /** The internal detail page configuration. */
    private List<CmsDetailPageInfo> m_ownDetailPages = Lists.newArrayList();

    /** The internal model page entries. */
    private List<CmsModelPageConfig> m_ownModelPageConfig = Lists.newArrayList();

    /** The internal property configuration. */
    private List<CmsPropertyConfig> m_ownPropertyConfigurations = Lists.newArrayList();

    /** The internal resource type entries. */
    private List<CmsResourceTypeConfig> m_ownResourceTypes = Lists.newArrayList();

    /** True if detail pages from this sitemap should be preferred when linking to contents inside this sitemap. */
    private boolean m_preferDetailPagesForLocalContents;

    /** Flag indicating whether all functions should be removed. */
    private boolean m_removeAllFunctions;

    /** The resource from which the configuration data was read. */
    private CmsResource m_resource;

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
    public CmsADEConfigDataInternal(
        CmsResource resource,
        boolean isModuleConfig,
        String basePath,
        List<CmsUUID> masterConfigs,
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
        m_formatterChangeSet.setDebugPath(m_basePath);
        m_dynamicFunctions = functionIds;
        m_removeAllFunctions = removeAllFunctions;
        m_excludeExternalDetailContents = excludeExternalDetailContents;
        m_includeInSiteSelector = includeInSiteSelector;
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
     * Gets the base path.<p>
     *
     * @return the base path
     */
    public String getBasePath() {

        return m_basePath;
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
     * Gets the structure ids of the master configuration files.
     *
     * @return the structure ids of the master configurations
     */
    public List<CmsUUID> getMasterConfigs() {

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

        return m_ownModelPageConfig;
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
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
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