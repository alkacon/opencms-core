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

import org.opencms.ade.configuration.formatters.CmsSettingConfiguration;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Formatter configuration for flex formatters.<p>
 */
public class CmsFlexFormatterBean extends CmsFormatterBean {

    /** The root path to the default content. */
    private String m_defaultContentRootPath;

    /** The UUID of the default content resource. */
    private CmsUUID m_defaultContentStructureId;

    /** The placeholder macro input. */
    private String m_placeholderStringTemplate;

    /** The macro input. */
    private String m_stringTemplate;

    /**
     * Constructor for creating a new formatter configuration with resource structure id.<p>
     *
     * @param containerTypes the formatter container types
     * @param jspRootPath the formatter JSP VFS root path
     * @param jspStructureId the structure id of the formatter JSP
     * @param key the formatter key
     * @param minWidth the formatter min width
     * @param maxWidth the formatter max width
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     * @param niceName the configuration display name
     * @param description the help text for the formatter
     * @param resourceTypeNames the resource type names
     * @param rank the configuration rank
     * @param id the configuration id
     * @param defaultContentRootPath the root path to the default content
     * @param defaultContentStructureId the UUID of the default content resource
     * @param settingConfig the settings configuration
     * @param isDetail <code>true</code> if detail formatter
     * @param isAutoEnabled <code>true</code> if auto enabled
     * @param displayType the display type
     * @param isAllowsSettingsInEditor whether this formatter allows settings to be edited in the content editor
     * @param stringTemplate the string template
     * @param placeholderStringTemplate the placeholder string template
     * @param metaMappings the meta mappings
     * @param useMetaMappingsForNormalElements if true, meta mappings will be evaluated for normal container elements, not just detail elements
     */
    public CmsFlexFormatterBean(
        Set<String> containerTypes,
        String jspRootPath,
        CmsUUID jspStructureId,
        String key,
        int minWidth,
        int maxWidth,
        boolean searchContent,
        String location,
        String niceName,
        String description,
        Collection<String> resourceTypeNames,
        int rank,
        String id,
        String defaultContentRootPath,
        CmsUUID defaultContentStructureId,
        CmsSettingConfiguration settingConfig,
        boolean isAutoEnabled,
        boolean isDetail,
        String displayType,
        boolean isAllowsSettingsInEditor,
        String stringTemplate,
        String placeholderStringTemplate,
        List<CmsMetaMapping> metaMappings,
        boolean useMetaMappingsForNormalElements) {

        super(
            containerTypes,
            jspRootPath,
            jspStructureId,
            key,
            new HashSet<>(),
            minWidth,
            maxWidth,
            false,
            searchContent,
            location,
            Collections.<String> emptyList(),
            "",
            Collections.<String> emptyList(),
            "",
            Collections.emptyList(),
            niceName,
            description,
            resourceTypeNames,
            rank,
            id,
            settingConfig,
            true,
            isAutoEnabled,
            isDetail,
            displayType,
            isAllowsSettingsInEditor,
            false,
            false,
            metaMappings,
            Collections.emptyMap(),
            useMetaMappingsForNormalElements);
        m_stringTemplate = stringTemplate;
        m_placeholderStringTemplate = placeholderStringTemplate;
        m_defaultContentRootPath = defaultContentRootPath;
        m_defaultContentStructureId = defaultContentStructureId;
    }

    /**
     * Returns the root path to the default content.<p>
     *
     * @return the root path to the default content
     */
    public String getDefaultContentRootPath() {

        return m_defaultContentRootPath;
    }

    /**
     * Returns the UUID of the default content.<p>
     *
     * @return the UUID of the default content
     */
    public CmsUUID getDefaultContentStructureId() {

        return m_defaultContentStructureId;
    }

    /**
     * Returns the placeholder string template.<p>
     *
     * @return the placeholder string template
     */
    public String getPlaceholderStringTemplate() {

        return m_placeholderStringTemplate;
    }

    /**
     * Returns the string template.<p>
     *
     * @return the string template
     */
    public String getStringTemplate() {

        return m_stringTemplate;
    }
}
