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

package org.opencms.xml.containerpage;

import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A bean containing formatter configuration data as strings.<p>
 * 
 * @since 8.0.0
 */
public class CmsFormatterBean {

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
    private Set<String> m_containerTypes;

    /** CSS Head includes. */
    private Set<String> m_cssHeadIncludes = new LinkedHashSet<String>();

    /** The id for this formatter. */
    private String m_id;

    /** Inline CSS snippets. */
    private String m_inlineCss;

    /** Inline Javascript snippets. */
    private String m_inlineJavascript;

    /** Is the formatter automatically enabled? */
    private boolean m_isAutoEnabled;

    /** Is the formatter from a formatter configuration file? */
    private boolean m_isFromFormatterConfigFile;

    /** Indicates if this formatter is to be used as preview in the ADE gallery GUI. */
    private boolean m_isPreviewFormatter;

    /** Javascript head includes. */
    private List<String> m_javascriptHeadIncludes = new ArrayList<String>();

    /** The formatter JSP. */
    private String m_jspRootPath;

    /** The UUID of the JSP resource for this formatter. */
    private CmsUUID m_jspStructureId;

    /** The location this formatter was configured in. */
    private String m_location;

    /** If true, will match any container/width combination. */
    private boolean m_matchAll;

    /** The formatter max width. */
    private int m_maxWidth;

    /** The formatter min width. */
    private int m_minWidth;

    /** The nice name. */
    private String m_niceName;

    /** The rank. */
    private int m_rank;

    /** The resource type name. */
    private String m_resourceTypeName;

    /** Indicates if the content should be searchable in the online index when this formatter is used. */
    private boolean m_search;

    /** The settings. */
    private Map<String, CmsXmlContentProperty> m_settings = new LinkedHashMap<String, CmsXmlContentProperty>();

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
     * 
     * @param cssHeadIncludes
     * @param inlineCss
     * @param javascriptHeadIncludes
     * @param inlineJavascript
     * @param niceName
     * @param resourceTypeName
     * @param rank
     * @param id
     * @param settings 
     * @param isFromConfigFile
     * @param isAutoEnabled 
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
        String resourceTypeName,
        int rank,
        String id,
        Map<String, CmsXmlContentProperty> settings,
        boolean isFromConfigFile,
        boolean isAutoEnabled) {

        m_jspRootPath = jspRootPath;
        m_jspStructureId = jspStructureId;

        m_containerTypes = containerTypes;
        m_minWidth = minWidth;
        m_maxWidth = maxWidth;

        m_isPreviewFormatter = preview;
        m_search = searchContent;
        m_location = location;

        m_id = id;
        m_niceName = niceName;
        m_resourceTypeName = resourceTypeName;
        m_rank = rank;
        m_inlineCss = inlineCss;
        m_inlineJavascript = inlineJavascript;
        m_javascriptHeadIncludes.addAll(javascriptHeadIncludes);
        m_cssHeadIncludes.addAll(cssHeadIncludes);
        m_settings.putAll(settings);
        m_isFromFormatterConfigFile = isFromConfigFile;
        m_isAutoEnabled = isAutoEnabled;
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
     * 
     *  
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
            rootPath,
            "",
            1000,
            null,
            Collections.<String, CmsXmlContentProperty> emptyMap(),
            false,
            false);

        // TODO Auto-generated constructor stub

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
            jspRootPath,
            "",
            DEFAULT_SCHEMA_RANK,
            null,
            Collections.<String, CmsXmlContentProperty> emptyMap(),
            false,
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
     * Returns the formatter container type.<p>
     * 
     * If this is "*", then the formatter is a width based formatter.<p>
     * 
     * @return the formatter container type 
     */
    public Set<String> getContainerTypes() {

        return m_containerTypes;
    }

    /**
     * Gets the CSS head includes.<p>
     * 
     * @return the CSS head includes 
     */
    public Set<String> getCssHeadIncludes() {

        return Collections.unmodifiableSet(m_cssHeadIncludes);
    }

    /**
     * Returns the id of this formatter.<p>
     * 
     * This method may return null because the id is not always defined for formatters, e.g. for those formatters declared in a schema.<p> 
     * 
     * @return the formatter id 
     */
    public String getId() {

        return m_id;
    }

    /**
     * Gets the inline CSS snippets.<p>
     * 
     * @return the inline CSS snippets 
     */
    public String getInlineCss() {

        return m_inlineCss;
    }

    /**
     * Gets the inline JS snippets.<p>
     * 
     * @return the inline JS snippets 
     */
    public String getInlineJavascript() {

        return m_inlineJavascript;
    }

    /**
     * Gets the Javascript head includes.<p>
     * 
     * @return the head includes 
     */
    public List<String> getJavascriptHeadIncludes() {

        return Collections.unmodifiableList(m_javascriptHeadIncludes);
    }

    /**
     * Returns the root path of the formatter JSP in the OpenCms VFS.<p>
     * 
     * @return the root path of the formatter JSP in the OpenCms VFS.<p>
     */
    public String getJspRootPath() {

        return m_jspRootPath;
    }

    /**
     * Returns the structure id of the JSP resource for this formatter.<p>
     * 
     * @return the structure id of the JSP resource for this formatter
     */
    public CmsUUID getJspStructureId() {

        return m_jspStructureId;
    }

    /**
     * Returns the location this formatter was defined in.<p>
     * 
     * This will be an OpenCms VFS root path, either to the XML schema XSD, or the
     * configuration file this formatter was defined in, or to the JSP that 
     * makes up this formatter.<p>
     * 
     * @return the location this formatter was defined in
     */
    public String getLocation() {

        return m_location;
    }

    /**
     * Returns the maximum formatter width.<p>
     * 
     * If this is not set, then {@link Integer#MAX_VALUE} is returned.<p>
     *  
     * @return the maximum formatter width 
     */
    public int getMaxWidth() {

        return m_maxWidth;
    }

    /**
     * Returns the minimum formatter width.<p>
     * 
     * If this is not set, then <code>-1</code> is returned.<p>
     * 
     * @return the minimum formatter width
     */
    public int getMinWidth() {

        return m_minWidth;
    }

    /**
     * Gets the nice name for this formatter.<p>
     * 
     * @return the nice name for this formatter 
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Gets the rank.<p>
     * 
     * @return the rank 
     */
    public int getRank() {

        return m_rank;
    }

    /**
     * Gets the resource type name.<p>
     * 
     * @return the resource type name 
     */
    public String getResourceTypeName() {

        return m_resourceTypeName;
    }

    /**
     * Gets the defined settings.<p>
     * 
     * @return the defined settings 
     */
    public Map<String, CmsXmlContentProperty> getSettings() {

        return Collections.unmodifiableMap(m_settings);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_containerTypes.hashCode() ^ ((m_minWidth * 33) ^ m_maxWidth);
    }

    /** 
     * Returns true if the formatter is automatically enabled.<p>
     * 
     * @return true if the formatter is automatically enabled 
     */
    public boolean isAutoEnabled() {

        return m_isAutoEnabled;
    }

    /**
     * Returns true if the formatter is from a formatter configuration file.<p>
     * 
     * @return formatter f 
     */
    public boolean isFromFormatterConfigFile() {

        return m_isFromFormatterConfigFile;
    }

    /** 
     * Returns true if this formatter should match all type/width combinations.<p>
     * 
     * @return true if this formatter should match all type/width combinations 
     */
    public boolean isMatchAll() {

        return m_matchAll;
    }

    /**
     * Indicates if this formatter is to be used as preview in the ADE gallery GUI.
     * 
     * @return <code>true</code> if this formatter is to be used as preview in the ADE gallery GUI
     */
    public boolean isPreviewFormatter() {

        return m_isPreviewFormatter;
    }

    /**
     * Returns <code>true</code> in case an XML content formatted with this formatter should be included in the 
     * online full text search.<p>
     * 
     * @return <code>true</code> in case an XML content formatted with this formatter should be included in the 
     * online full text search
     */
    public boolean isSearchContent() {

        return m_search;
    }

    /**
     * Returns <code>true</code> in case this formatter is based on type information.<p>
     * 
     * @return <code>true</code> in case this formatter is based on type information
     */
    public boolean isTypeFormatter() {

        return !m_containerTypes.isEmpty();
    }

    /**
     * Sets the structure id of the JSP for this formatter.<p>
     *
     * This is "package visible" as it should be only called from {@link CmsFormatterConfiguration#initialize(org.opencms.file.CmsObject)}.<p>
     * 
     * @param jspStructureId the structure id of the JSP for this formatter
     */
    void setJspStructureId(CmsUUID jspStructureId) {

        // package visibility is wanted
        m_jspStructureId = jspStructureId;
    }
}