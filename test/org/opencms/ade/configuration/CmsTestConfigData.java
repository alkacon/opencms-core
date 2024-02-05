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

import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReferenceMeta;
import org.opencms.ade.configuration.CmsConfigurationReader.DiscardPropertiesMode;
import org.opencms.ade.configuration.formatters.CmsFormatterChangeSet;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCacheState;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.galleries.CmsAddContentRestriction;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A configuration data class whose parent can be set directly for test purposes.<p>
 */
public class CmsTestConfigData extends CmsADEConfigData {

    /** The parent configuration object. */
    public CmsADEConfigData m_parent;

    /** CmsObject for testing. */
    private CmsObject m_cms2;

    /** The formatter cache state can be set directly for the test configuration data. */
    private CmsFormatterConfigurationCacheState m_formatters;

    /** The schema formatters can be set directly for the test configuration data. */
    private Map<Integer, CmsFormatterConfiguration> m_schemaFormatterConfiguration = new HashMap<Integer, CmsFormatterConfiguration>();

    /**
     * Creates a new instance.
     *
     * @param internalData the internal data
     */
    public CmsTestConfigData(CmsADEConfigDataInternal internalData) {

        super(internalData, null, null);
    }

    /**
     * Creates a new configuration data object.<p>
     *
     * @param basePath the base path
     * @param resourceTypeConfig the resource type configuration
     * @param propertyConfig the  property configuration
     * @param detailPageInfos the detail page configuration
     * @param modelPages the model page configuration
     */
    public CmsTestConfigData(
        String basePath,
        List<CmsResourceTypeConfig> resourceTypeConfig,
        List<CmsPropertyConfig> propertyConfig,
        List<CmsDetailPageInfo> detailPageInfos,
        List<CmsModelPageConfig> modelPages) {

        super(
            new CmsADEConfigDataInternal(
                null,
                false,
                basePath,
                null,
                resourceTypeConfig,
                false,
                propertyConfig,
                DiscardPropertiesMode.keep,
                detailPageInfos,
                modelPages,
                new ArrayList<CmsFunctionReference>(),
                false,
                false,
                false,
                false,
                false,
                new CmsFormatterChangeSet(),
                false,
                null),
            null,
            null);
    }

    public static CmsTestConfigData buildTestDataForPlugins(
        CmsObject cms,
        String basePath,
        boolean removeAllPlugins,
        Set<CmsUUID> pluginsToAdd,
        Set<CmsUUID> pluginsToRemove) {

        CmsADEConfigDataInternal internal;
        internal = new CmsADEConfigDataInternal(
            /*CmsObject*/ cms,
            /*CmsResource*/ null,
            /* boolean isModuleConfig*/false,
            /*String basePath*/basePath,
            /*List<CmsUUID> masterConfigs*/new ArrayList<>(),
            /*List<CmsResourceTypeConfig> resourceTypeConfig*/new ArrayList<>(),
            /*CmsGalleriesDisabledTypeMode*/null,
            /*CmsGalleriesDisabledTypeMode*/null,
            /*boolean discardInheritedTypes*/false,
            /*List<CmsPropertyConfig> propertyConfig*/new ArrayList<>(),
            /*DiscardPropertiesMode discardPropertiesMode*/null,
            /*List<CmsDetailPageInfo> detailPageInfos*/new ArrayList<>(),
            /*List<CmsModelPageConfigWithoutResource> modelPages*/new ArrayList<>(),
            /*List<CmsFunctionReference> functionReferences*/new ArrayList<>(),
            /*boolean discardInheritedModelPages*/false,
            /*boolean createContentsLocally*/true,
            /*boolean preferDetailPagesForLocalContents*/false,
            /*boolean excludeExternalDetailContents*/false,
            /*boolean includeInSiteSelector*/true,
            /*CmsFormatterChangeSet formatterChangeSet*/new CmsFormatterChangeSet(),
            /*boolean removeAllFunctions*/false,
            /*Set<CmsUUID> functionIds*/new HashSet<>(),
            /*Set<CmsUUID> functionsToRemove*/new HashSet<>(),
            /*boolean removeAllPlugins*/removeAllPlugins,
            /*Set<CmsUUID> addedPlugins*/pluginsToAdd,
            /*Set<CmsUUID> removedPlugins*/pluginsToRemove,
            /*Boolean useFormatterKeys*/null,
            /*CmsTypeOrderingMode orderingMode*/CmsTypeOrderingMode.byDisplayOrder,
            CmsAddContentRestriction.EMPTY,
            /*CmsUUID sharedSettingsOverride */ null,
            /*boolean removeAllSharedSettingsOverrides*/false,
            /*CmsUUID attributeEditorConfigId*/null,
            /*Map<String, String> attributes*/new HashMap<>());
        return new CmsTestConfigData(internal);

    }

    /**
     * @see org.opencms.ade.configuration.CmsADEConfigData#getCachedFormatters()
     */
    @Override
    public CmsFormatterConfigurationCacheState getCachedFormatters() {

        if (m_formatters != null) {
            return m_formatters;
        } else {
            return super.getCachedFormatters();
        }
    }

    /**
     * Sets the CmsObject for testing.<p>
     *
     * @param cms the CmsObject for testing
     */
    public void initialize(CmsObject cms) {

        m_cms2 = cms;
    }

    /**
     * @see org.opencms.ade.configuration.CmsADEConfigData#parent()
     */
    @Override
    public CmsADEConfigData parent() {

        return m_parent;
    }

    /**
     * Registers a schema formatter which will be returned from getSchemaFormatters if a resource with the matching type is passed in.<p>
     *
     * @param typeId  the resource type id
     * @param formatters the formatters for the resource type
     */
    public void registerSchemaFormatters(int typeId, CmsFormatterConfiguration formatters) {

        m_schemaFormatterConfiguration.put(Integer.valueOf(typeId), formatters);
    }

    /**
     * Sets the "create contents locally" flag.<p>
     *
     * @param createContentsLocally the flag to control whether contents from inherited resource types are stored in the local .content folder
     */
    public void setCreateContentsLocally(boolean createContentsLocally) {

        m_data.m_createContentsLocally = createContentsLocally;
    }

    /**
     * Sets the "discard inherited model pages" flag.<p>
     *
     * @param discardInheritedModelPages the flag to control whether inherited model pages are discarded
    */
    public void setDiscardInheritedModelPages(boolean discardInheritedModelPages) {

        m_data.m_discardInheritedModelPages = discardInheritedModelPages;
    }

    /**
     * Sets the 'discard properties' mode.
     *
     * @param mode the new value for the mode
     */
    public void setDiscardPropertiesMode(DiscardPropertiesMode mode) {

        m_data.m_discardPropertiesMode = mode;
    }

    /**
     * Sets the formatter change set.<p>
     *
     * @param changeSet the formatter change set
     */
    public void setFormatterChangeSet(CmsFormatterChangeSet changeSet) {

        m_data.m_formatterChangeSet = changeSet;
        clearCaches();
    }

    /**
     * Sets the formatter cache state.<p>
     *
     * @param formatters the formatter cache state
     */
    public void setFormatters(CmsFormatterConfigurationCacheState formatters) {

        m_formatters = formatters;
    }

    /**
     * Sets the "discard inherited types" flag.<p>
     *
     * @param discardInheritedTypes the flag to control whether inherited types are discarded
     */
    public void setIsDiscardInheritedTypes(boolean discardInheritedTypes) {

        m_data.m_discardInheritedTypes = discardInheritedTypes;
    }

    /**
     * Marks this as a module configuration or normal sitemap configuration.<p>
     *
     * @param isModuleConfig true if this is a module configuration
     */
    public void setIsModuleConfig(boolean isModuleConfig) {

        m_data.m_isModuleConfig = isModuleConfig;
    }

    /**
     * Sets the parent configuration object.<p>
     *
     * @param parent the parent configuration object
     */
    public void setParent(CmsADEConfigData parent) {

        m_parent = parent;
    }

    /**
     * @see org.opencms.ade.configuration.CmsADEConfigData#getCms()
     */
    @Override
    protected CmsObject getCms() {

        return m_cms2;
    }

    /**
     * @see org.opencms.ade.configuration.CmsADEConfigData#getFormattersFromSchema(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    protected CmsFormatterConfiguration getFormattersFromSchema(CmsObject cms, CmsResource res) {

        Integer key = Integer.valueOf(res.getTypeId());
        CmsFormatterConfiguration result = m_schemaFormatterConfiguration.get(key);
        if (result == null) {
            result = super.getFormattersFromSchema(cms, res);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.configuration.CmsADEConfigData#getMeta()
     */
    @Override
    protected ConfigReferenceMeta getMeta() {

        return new ConfigReferenceMeta();
    }

}
