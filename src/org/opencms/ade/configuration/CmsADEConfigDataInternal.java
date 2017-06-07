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

import org.opencms.ade.configuration.formatters.CmsFormatterChangeSet;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsResource;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    /** Should inherited properties be discard? */
    protected boolean m_discardInheritedProperties;
    /** Should inherited types be discarded? */
    protected boolean m_discardInheritedTypes;

    /** The configured formatter changes. */
    protected CmsFormatterChangeSet m_formatterChangeSet = new CmsFormatterChangeSet();

    /** True if this is a module configuration, not a normal sitemap configuration. */
    protected boolean m_isModuleConfig;
    /** The master configuration resource (possibly null). */
    protected CmsResource m_masterConfig;
    /** The base path of this configuration. */
    private String m_basePath;
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

    /** The resource from which the configuration data was read. */
    private CmsResource m_resource;

    /**
     * Creates a new configuration data instance.<p>

     * @param resource the resource from which this configuration data was read
     * @param isModuleConfig true if this is a module configuration
     * @param basePath the base path
     * @param masterConfig the master configuration resource (possibly null)
     * @param resourceTypeConfig the resource type configuration
     * @param discardInheritedTypes the "discard inherited types" flag
     * @param propertyConfig the property configuration
     * @param discardInheritedProperties the "discard inherited properties" flag
     * @param detailPageInfos the detail page configuration
     * @param modelPages the model page configuration
     * @param functionReferences the function reference configuration
     * @param discardInheritedModelPages the "discard  inherited model pages" flag
     * @param createContentsLocally the "create contents locally" flag
     * @param preferDetailPagesForLocalContents the "preferDetailPagesForLocalContents" flag
     * @param formatterChangeSet the formatter changes
     */
    public CmsADEConfigDataInternal(
        CmsResource resource,
        boolean isModuleConfig,
        String basePath,
        CmsResource masterConfig,
        List<CmsResourceTypeConfig> resourceTypeConfig,
        boolean discardInheritedTypes,
        List<CmsPropertyConfig> propertyConfig,
        boolean discardInheritedProperties,
        List<CmsDetailPageInfo> detailPageInfos,
        List<CmsModelPageConfig> modelPages,
        List<CmsFunctionReference> functionReferences,
        boolean discardInheritedModelPages,
        boolean createContentsLocally,
        boolean preferDetailPagesForLocalContents,
        CmsFormatterChangeSet formatterChangeSet) {

        m_resource = resource;
        m_basePath = basePath;
        m_ownResourceTypes = resourceTypeConfig;
        m_ownPropertyConfigurations = propertyConfig;
        m_ownModelPageConfig = modelPages;
        m_ownDetailPages = detailPageInfos;
        m_functionReferences = functionReferences;
        m_isModuleConfig = isModuleConfig;
        m_masterConfig = masterConfig;

        m_discardInheritedTypes = discardInheritedTypes;
        m_discardInheritedProperties = discardInheritedProperties;
        m_discardInheritedModelPages = discardInheritedModelPages;
        m_createContentsLocally = createContentsLocally;
        m_preferDetailPagesForLocalContents = preferDetailPagesForLocalContents;
        m_formatterChangeSet = formatterChangeSet;
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
     * Gets the master configuration resource (may be null).<p>
     *
     * @return the master configuration resource
     */
    public CmsResource getMasterConfig() {

        return m_masterConfig;
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

        return m_discardInheritedProperties;
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