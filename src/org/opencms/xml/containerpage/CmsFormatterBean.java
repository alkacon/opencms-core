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

import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A bean containing formatter configuration data as strings.<p>
 *
 * @since 8.0.0
 */
public class CmsFormatterBean implements I_CmsFormatterBean {

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

    /** The formatter container type. */
    protected Set<String> m_containerTypes;

    /** CSS Head includes. */
    protected Set<String> m_cssHeadIncludes = new LinkedHashSet<String>();

    /** The description text for the formatter. */
    protected String m_description;

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

    /** True if this formatter should be used by the display tag. */
    protected boolean m_isDisplay;

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

    /** The rank. */
    protected int m_rank;

    /** The resource type name. */
    protected Collection<String> m_resourceTypeNames;

    /** Indicates if the content should be searchable in the online index when this formatter is used. */
    protected boolean m_search;

    /** The settings. */
    protected Map<String, CmsXmlContentProperty> m_settings = new LinkedHashMap<String, CmsXmlContentProperty>();

    /** Indicating if this formatter will always render all nested containers. */
    protected boolean m_strictContainers;

    /** Indicates whether meta mappings should be applied for all elements. */
    protected boolean m_useMetaMappingsForNormalElements;

    /**
     * Constructor for creating a new formatter configuration with resource structure id.<p>
     *
     * @param containerTypes the formatter container types
     * @param jspRootPath the formatter JSP VFS root path
     * @param jspStructureId the structure id of the formatter JSP
     * @param minWidth the formatter min width
     * @param maxWidth the formatter max width
     * @param preview indicates if this formatter is to be used for the preview in the ADE gallery GUI
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     * @param cssHeadIncludes the CSS head includes
     * @param inlineCss the in-line CSS
     * @param javascriptHeadIncludes the JavaScript headincludes
     * @param inlineJavascript the in-line JavaScript
     * @param niceName the configuration display name
     * @param description the description text for the formatter
     * @param resourceTypeNames the resource type names
     * @param rank the configuration rank
     * @param id the configuration id
     * @param settings the settings configuration
     * @param isFromConfigFile <code>true</code> if configuration file based
     * @param isAutoEnabled <code>true</code> if auto enabled
     * @param isDetail <code>true</code> if detail formatter
     * @param isDisplay the display flag
     * @param strictContainers <code>true</code> if this formatter will always render all nested containers
     * @param nestedFormatterSettings indicates whether nested formatter settings should be displayed
     * @param metaMappings the meta mappings
     * @param useMetaMappingsForNormalElements if true, meta mappings will be evaluated for normal container elements, not just detail elements
     */
    public CmsFormatterBean(
        Set<String> containerTypes,
        String jspRootPath,
        CmsUUID jspStructureId,
        int minWidth,
        int maxWidth,
        boolean preview,
        boolean searchContent,
        String location,
        List<String> cssHeadIncludes,
        String inlineCss,
        List<String> javascriptHeadIncludes,
        String inlineJavascript,
        String niceName,
        String description,
        Collection<String> resourceTypeNames,
        int rank,
        String id,
        Map<String, CmsXmlContentProperty> settings,
        boolean isFromConfigFile,
        boolean isAutoEnabled,
        boolean isDetail,
        boolean isDisplay,
        boolean strictContainers,
        boolean nestedFormatterSettings,
        List<CmsMetaMapping> metaMappings,
        boolean useMetaMappingsForNormalElements) {

        m_jspRootPath = jspRootPath;
        m_jspStructureId = jspStructureId;
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
        m_settings.putAll(settings);
        m_isFromFormatterConfigFile = isFromConfigFile;
        m_isAutoEnabled = isAutoEnabled;
        m_isDetail = isDetail;
        m_isDisplay = isDisplay;
        m_nestedFormatterSettings = nestedFormatterSettings;
        m_strictContainers = strictContainers;
        m_metaMappings = metaMappings;
        m_useMetaMappingsForNormalElements = useMetaMappingsForNormalElements;
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
            minWidth,
            maxWidth,
            preview,
            searchContent,
            location,
            Collections.<String> emptyList(),
            "",
            Collections.<String> emptyList(),
            "",
            null,
            rootPath,
            Collections.<String> emptySet(),
            1000,
            null,
            Collections.<String, CmsXmlContentProperty> emptyMap(),
            false,
            false,
            true,
            false,
            false,
            false,
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
            -1,
            Integer.MAX_VALUE,
            preview,
            false,
            location,
            Collections.<String> emptyList(),
            "",
            Collections.<String> emptyList(),
            "",
            null,
            jspRootPath,
            Collections.<String> emptySet(),
            DEFAULT_SCHEMA_RANK,
            null,
            Collections.<String, CmsXmlContentProperty> emptyMap(),
            false,
            false,
            true,
            false,
            false,
            false,
            null,
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
        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setMessages(OpenCms.getWorkplaceManager().getMessages(locale));
        return resolver.resolveMacros(m_description);
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
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getSettings()
     */
    @Override
    public Map<String, CmsXmlContentProperty> getSettings() {

        return Collections.unmodifiableMap(m_settings);
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

        return m_isDisplay;
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
}