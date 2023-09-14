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

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.formatters.CmsSettingConfiguration;
import org.opencms.ade.configuration.plugins.CmsTemplatePlugin;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import com.google.common.collect.ImmutableList;

/**
 * A bean containing formatter configuration data as strings.<p>
 *
 * @since 8.0.0
 */
public class CmsFormatterBean implements I_CmsFormatterBean, Cloneable {

    /** Default rank for formatters from formatter configuration files. */
    public static final int DEFAULT_CONFIGURATION_RANK = 1000;

    /** Default rank for formatters defined in schema. */
    public static final int DEFAULT_SCHEMA_RANK = 10000;

    /** Default formatter type constant. */
    public static final String PREVIEW_TYPE = "_PREVIEW_";

    /** The width of the preview window for the formatters. */
    public static final int PREVIEW_WIDTH = 640;

    /** Wildcard formatter type for width based formatters. */
    public static final String WILDCARD_TYPE = "*";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormatterBean.class);

    /** The formatter container type. */
    protected Set<String> m_containerTypes;

    /** CSS Head includes. */
    protected Set<String> m_cssHeadIncludes = new LinkedHashSet<String>();

    /** The description text for the formatter. */
    protected String m_description;

    /** Set of alias keys. */
    protected Set<String> m_aliasKeys = new HashSet<>();

    /** Set of all formatter keys (main + alias keys). */
    private Set<String> m_allKeys = new HashSet<>();

    /** Provides the display type. If empty if this formatter should not be used by the display tag. */
    protected String m_displayType;

    /** The id for this formatter. */
    protected String m_id;

    /** Inline CSS snippets. */
    protected String m_inlineCss;

    /** Inline Javascript snippets. */
    protected String m_inlineJavascript;

    /** Is the formatter automatically enabled? */
    protected boolean m_isAutoEnabled;

    /** True if this formatter can be used for detail views. */
    protected boolean m_isDetail;

    /** Is the formatter from a formatter configuration file? */
    protected boolean m_isFromFormatterConfigFile;

    /** Indicates if this formatter is to be used as preview in the ADE gallery GUI. */
    protected boolean m_isPreviewFormatter;

    /** JavaScript head includes. */
    protected List<String> m_javascriptHeadIncludes = new ArrayList<String>();

    /** The formatter JSP. */
    protected String m_jspRootPath;

    /** The UUID of the JSP resource for this formatter. */
    protected CmsUUID m_jspStructureId;

    /** The formatter key. */
    protected String m_key;

    /** The location this formatter was configured in. */
    protected String m_location;

    /** If true, will match any container/width combination. */
    protected boolean m_matchAll;

    /** The formatter max width. */
    protected int m_maxWidth;

    /** The meta mappings. */
    protected List<CmsMetaMapping> m_metaMappings;

    /** The formatter min width. */
    protected int m_minWidth;

    /** Indicates whether nested formatter settings should be displayed. */
    protected boolean m_nestedFormatterSettings;

    /** The nice name. */
    protected String m_niceName;

    /** The referenced plugins. */
    protected List<CmsTemplatePlugin> m_plugins = Collections.emptyList();

    /** The rank. */
    protected int m_rank;

    /** The resource type name. */
    protected Collection<String> m_resourceTypeNames;

    /** Indicates if the content should be searchable in the online index when this formatter is used. */
    protected boolean m_search;

    /** Indicating if this formatter will always render all nested containers. */
    protected boolean m_strictContainers;

    /** Indicates whether meta mappings should be applied for all elements. */
    protected boolean m_useMetaMappingsForNormalElements;

    /** Map of attributes. */
    private Map<String, String> m_attributes = Collections.emptyMap();

    /** Flag indicating this formatter allows settings to be edited in the content editor. */
    private boolean m_isAllowsSettingsInEditor;

    /** The setting configuration. */
    private CmsSettingConfiguration m_settingConfig;

    /**
     * Constructor for creating a new formatter configuration with resource structure id.<p>
     *
     * @param containerTypes the formatter container types
     * @param jspRootPath the formatter JSP VFS root path
     * @param jspStructureId the structure id of the formatter JSP
     * @param key the formatter key
     * @param aliasKeys the alias keys
     * @param minWidth the formatter min width
     * @param maxWidth the formatter max width
     * @param preview indicates if this formatter is to be used for the preview in the ADE gallery GUI
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     * @param cssHeadIncludes the CSS head includes
     * @param inlineCss the in-line CSS
     * @param javascriptHeadIncludes the JavaScript headincludes
     * @param inlineJavascript the in-line JavaScript
     * @param plugins the template plugins
     * @param niceName the configuration display name
     * @param description the description text for the formatter
     * @param resourceTypeNames the resource type names
     * @param rank the configuration rank
     * @param id the configuration id
     * @param settingConfig the settings configuration
     * @param isFromConfigFile <code>true</code> if configuration file based
     * @param isAutoEnabled <code>true</code> if auto enabled
     * @param isDetail <code>true</code> if detail formatter
     * @param displayType the display type
     * @param isAllowsSettingsInEditor whether this formatter allows settings to be edited in the content editor
     * @param strictContainers <code>true</code> if this formatter will always render all nested containers
     * @param nestedFormatterSettings indicates whether nested formatter settings should be displayed
     * @param metaMappings the meta mappings
     * @param attributes the formatter attributes
     * @param useMetaMappingsForNormalElements if true, meta mappings will be evaluated for normal container elements, not just detail elements
     */
    public CmsFormatterBean(
        Set<String> containerTypes,
        String jspRootPath,
        CmsUUID jspStructureId,
        String key,
        Set<String> aliasKeys,
        int minWidth,
        int maxWidth,
        boolean preview,
        boolean searchContent,
        String location,
        List<String> cssHeadIncludes,
        String inlineCss,
        List<String> javascriptHeadIncludes,
        String inlineJavascript,
        List<CmsTemplatePlugin> plugins,
        String niceName,
        String description,
        Collection<String> resourceTypeNames,
        int rank,
        String id,
        CmsSettingConfiguration settingConfig,
        boolean isFromConfigFile,
        boolean isAutoEnabled,
        boolean isDetail,
        String displayType,
        boolean isAllowsSettingsInEditor,
        boolean strictContainers,
        boolean nestedFormatterSettings,
        List<CmsMetaMapping> metaMappings,
        Map<String, String> attributes,
        boolean useMetaMappingsForNormalElements) {

        m_jspRootPath = jspRootPath;
        m_jspStructureId = jspStructureId;
        m_key = key;
        if (m_key != null) {
            m_key = m_key.trim();
            m_allKeys.add(m_key);
        }
        if (aliasKeys != null) {
            m_aliasKeys.addAll(aliasKeys);
            m_allKeys.addAll(aliasKeys);
        }

        m_containerTypes = containerTypes;
        m_minWidth = minWidth;
        m_maxWidth = maxWidth;

        m_isPreviewFormatter = preview;
        m_search = searchContent;
        m_location = location;
        m_description = description;

        m_id = id;
        m_niceName = niceName;
        m_resourceTypeNames = resourceTypeNames;
        m_rank = rank;
        m_inlineCss = inlineCss;
        m_inlineJavascript = inlineJavascript;
        m_javascriptHeadIncludes.addAll(javascriptHeadIncludes);
        m_cssHeadIncludes.addAll(cssHeadIncludes);
        m_plugins = new ArrayList<>(plugins);
        m_settingConfig = settingConfig;
        m_isFromFormatterConfigFile = isFromConfigFile;
        m_isAutoEnabled = isAutoEnabled;
        m_isDetail = isDetail;
        m_displayType = displayType;
        m_nestedFormatterSettings = nestedFormatterSettings;
        m_strictContainers = strictContainers;
        m_metaMappings = metaMappings;
        m_useMetaMappingsForNormalElements = useMetaMappingsForNormalElements;
        m_isAllowsSettingsInEditor = isAllowsSettingsInEditor;
        m_attributes = attributes != null ? attributes : Collections.emptyMap();
    }

    /**
     * Constructor for creating a new formatter configuration with resource structure id.<p>
     *
     * @param containerType the formatter container types
     * @param rootPath the formatter JSP VFS root path
     * @param structureId the structure id of the formatter JSP
     * @param minWidth the formatter min width
     * @param maxWidth the formatter max width
     * @param preview indicates if this formatter is to be used for the preview in the ADE gallery GUI
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     */
    public CmsFormatterBean(
        String containerType,
        String rootPath,
        CmsUUID structureId,
        int minWidth,
        int maxWidth,
        boolean preview,
        boolean searchContent,
        String location) {

        this(
            isWildcardType(containerType) ? Collections.<String> emptySet() : Collections.singleton(containerType),
            rootPath,
            structureId,
            null,
            new HashSet<String>(),
            minWidth,
            maxWidth,
            preview,
            searchContent,
            location,
            Collections.<String> emptyList(),
            "",
            Collections.<String> emptyList(),
            "",
            new ArrayList<CmsTemplatePlugin>(),
            null,
            rootPath,
            Collections.<String> emptySet(),
            1000,
            null,
            new CmsSettingConfiguration(),
            false,
            false,
            true,
            null,
            false,
            false,
            false,
            null,
            null,
            false);

    }

    /**
     * Constructor for creating a new formatter configuration without resource structure id.<p>
     *
     * @param containerType the formatter container type
     * @param jspRootPath the formatter JSP VFS root path
     * @param minWidthStr the formatter min width
     * @param maxWidthStr the formatter max width
     * @param preview indicates if this formatter is to be used for the preview in the ADE gallery GUI
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     */
    public CmsFormatterBean(
        String containerType,
        String jspRootPath,
        String minWidthStr,
        String maxWidthStr,
        String preview,
        String searchContent,
        String location) {

        m_jspRootPath = jspRootPath;
        m_containerTypes = Collections.singleton(containerType);
        if (isWildcardType(containerType)) {
            m_containerTypes = Collections.emptySet();
        }
        m_minWidth = -1;
        m_maxWidth = Integer.MAX_VALUE;

        if (m_containerTypes.isEmpty()) {
            // wildcard formatter; index by width
            // if no width available, use -1

            try {
                m_minWidth = Integer.parseInt(minWidthStr);
            } catch (NumberFormatException e) {
                //ignore; width will be -1
            }
            try {
                m_maxWidth = Integer.parseInt(maxWidthStr);
            } catch (NumberFormatException e) {
                //ignore; maxWidth will be max. integer
            }
        }

        m_isPreviewFormatter = Boolean.valueOf(preview).booleanValue();

        m_search = CmsStringUtil.isEmptyOrWhitespaceOnly(searchContent)
        ? true
        : Boolean.valueOf(searchContent).booleanValue();

        m_location = location;
        m_rank = DEFAULT_SCHEMA_RANK;
    }

    /**
     * Constructor for creating a formatter bean which matches all container/width combinations.<p>
     *
     * @param jspRootPath the jsp root path
     * @param jspStructureId the jsp structure id
     * @param location the formatter location
     * @param preview the preview formatter flag
     */
    CmsFormatterBean(String jspRootPath, CmsUUID jspStructureId, String location, boolean preview) {

        this(
            Collections.<String> emptySet(),
            jspRootPath,
            jspStructureId,
            null,
            new HashSet<String>(),
            -1,
            Integer.MAX_VALUE,
            preview,
            false,
            location,
            Collections.<String> emptyList(),
            "",
            Collections.<String> emptyList(),
            "",
            new ArrayList<>(),
            null,
            jspRootPath,
            Collections.<String> emptySet(),
            DEFAULT_SCHEMA_RANK,
            null,
            new CmsSettingConfiguration(),
            false,
            false,
            true,
            null,
            false,
            false,
            false,
            null,
            Collections.emptyMap(),
            false);
        m_matchAll = true;
    }

    /**
     * Checks if the given container type matches the ADE gallery preview type.<p>
     *
     * @param containerType the container type to check
     *
     * @return <code>true</code> if the given container type matches the ADE gallery preview type
     */
    public static boolean isPreviewType(String containerType) {

        return PREVIEW_TYPE.equals(containerType);
    }

    /**
     * Checks whether the container type is a wildcard.<p>
     *
     * @param containerType the container type
     *
     * @return true if the container type is a wildcard
     */
    private static boolean isWildcardType(String containerType) {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(containerType) || WILDCARD_TYPE.equals(containerType);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getAliasKeys()
     */
    public Set<String> getAliasKeys() {

        return Collections.unmodifiableSet(m_aliasKeys);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getAllKeys()
     */
    public Set<String> getAllKeys() {

        return Collections.unmodifiableSet(m_allKeys);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getAttributes()
     */
    public Map<String, String> getAttributes() {

        return m_attributes;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getContainerTypes()
     */
    @Override
    public Set<String> getContainerTypes() {

        return m_containerTypes == null
        ? Collections.<String> emptySet()
        : Collections.unmodifiableSet(m_containerTypes);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getCssHeadIncludes()
     */
    @Override
    public Set<String> getCssHeadIncludes() {

        return Collections.unmodifiableSet(m_cssHeadIncludes);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getDescription(Locale)
     */
    public String getDescription(Locale locale) {

        if (locale == null) {
            return m_description;
        }
        CmsMacroResolver resolver = new CmsMacroResolver() {

            @Override
            public String getMacroValue(String macro) {

                if (macro.startsWith(CmsMacroResolver.KEY_LOCALIZED_PREFIX)) {
                    String keyName = macro.substring(CmsMacroResolver.KEY_LOCALIZED_PREFIX.length());
                    return m_messages.keyWithParams(keyName, key -> "");
                } else {
                    return super.getMacroValue(macro);
                }
            }

            @Override
            public String resolveMacros(String input) {

                return StringUtils.trimToNull(super.resolveMacros(input));
            }

        };
        resolver.setMessages(OpenCms.getWorkplaceManager().getMessages(locale));
        return resolver.resolveMacros(m_description);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getDisplayType()
     */
    public String getDisplayType() {

        return m_displayType;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getId()
     */
    @Override
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getInlineCss()
     */
    @Override
    public String getInlineCss() {

        return m_inlineCss;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getInlineJavascript()
     */
    @Override
    public String getInlineJavascript() {

        return m_inlineJavascript;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getJavascriptHeadIncludes()
     */
    @Override
    public List<String> getJavascriptHeadIncludes() {

        return Collections.unmodifiableList(m_javascriptHeadIncludes);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getJspRootPath()
     */
    @Override
    public String getJspRootPath() {

        return m_jspRootPath;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getJspStructureId()
     */
    @Override
    public CmsUUID getJspStructureId() {

        return m_jspStructureId;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getKey()
     */
    public String getKey() {

        return m_key;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getLocation()
     */
    @Override
    public String getLocation() {

        return m_location;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getMaxWidth()
     */
    @Override
    public int getMaxWidth() {

        return m_maxWidth;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getMetaMappings()
     */
    public List<CmsMetaMapping> getMetaMappings() {

        return m_metaMappings;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getMinWidth()
     */
    @Override
    public int getMinWidth() {

        return m_minWidth;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getNiceName(Locale)
     */
    @Override
    public String getNiceName(Locale locale) {

        if (locale == null) {
            return m_niceName;
        }
        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setMessages(OpenCms.getWorkplaceManager().getMessages(locale));
        return resolver.resolveMacros(m_niceName);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getRank()
     */
    @Override
    public int getRank() {

        return m_rank;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getResourceTypeNames()
     */
    @Override
    public Collection<String> getResourceTypeNames() {

        return m_resourceTypeNames;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getSettings(org.opencms.ade.configuration.CmsADEConfigData)
     */
    @Override
    public Map<String, CmsXmlContentProperty> getSettings(CmsADEConfigData config) {

        ImmutableList<CmsUUID> sharedSettingOverrides = config.getSharedSettingOverrides();
        return m_settingConfig.getSettings(sharedSettingOverrides);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getTemplatePlugins()
     */
    public List<CmsTemplatePlugin> getTemplatePlugins() {

        return Collections.unmodifiableList(m_plugins);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getContainerTypes().hashCode() ^ ((m_minWidth * 33) ^ m_maxWidth);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#hasNestedFormatterSettings()
     */
    public boolean hasNestedFormatterSettings() {

        return m_nestedFormatterSettings;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isAllowsSettingsInEditor()
     */
    public boolean isAllowsSettingsInEditor() {

        return m_isAllowsSettingsInEditor;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isAutoEnabled()
     */
    @Override
    public boolean isAutoEnabled() {

        return m_isAutoEnabled;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isDetailFormatter()
     */
    public boolean isDetailFormatter() {

        return m_isDetail;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isDisplayFormatter()
     */
    public boolean isDisplayFormatter() {

        return m_displayType != null;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isFromFormatterConfigFile()
     */
    @Override
    public boolean isFromFormatterConfigFile() {

        return m_isFromFormatterConfigFile;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isMatchAll()
     */
    @Override
    public boolean isMatchAll() {

        return m_matchAll || ((m_containerTypes != null) && m_containerTypes.contains(WILDCARD_TYPE));
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isPreviewFormatter()
     */
    @Override
    public boolean isPreviewFormatter() {

        return m_isPreviewFormatter;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isSearchContent()
     */
    @Override
    public boolean isSearchContent() {

        return m_search;
    }

    /**
     * Returns whether this formatter will always render all nested containers.<p>
     *
     * @return <code>true</code> if this formatter will always render all nested containers
     */
    public boolean isStrictContainers() {

        return m_strictContainers;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isTypeFormatter()
     */
    @Override
    public boolean isTypeFormatter() {

        return !getContainerTypes().isEmpty();
    }

    /**
     * Sets the structure id of the JSP for this formatter.<p>
     *
     * This is "package visible" as it should be only called from {@link CmsFormatterConfiguration#initialize(org.opencms.file.CmsObject)}.<p>
     *
     * @param jspStructureId the structure id of the JSP for this formatter
     */
    public void setJspStructureId(CmsUUID jspStructureId) {

        // package visibility is wanted
        m_jspStructureId = jspStructureId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#useMetaMappingsForNormalElements()
     */
    @Override
    public boolean useMetaMappingsForNormalElements() {

        return m_useMetaMappingsForNormalElements;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#withKeys(java.util.Collection)
     */
    public Optional<I_CmsFormatterBean> withKeys(Collection<String> keys) {

        if ((getKey() != null) && !getAllKeys().equals(keys)) {
            Set<String> newAllKeys = new HashSet<>(keys);
            newAllKeys.add(getKey());
            Set<String> newAliases = new HashSet<>(keys);
            newAliases.remove(getKey());
            CmsFormatterBean clonedBean;
            try {
                clonedBean = (CmsFormatterBean)clone();
                clonedBean.m_aliasKeys = newAliases;
                clonedBean.m_allKeys = new HashSet<>(keys);
                return Optional.of(clonedBean);

            } catch (CloneNotSupportedException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return Optional.empty();
            }
        }
        return Optional.of(this);
    }
}