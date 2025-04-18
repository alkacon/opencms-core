/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.formatters.CmsSettingConfiguration;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Formatter configuration for macro formatters.<p>
 */
public class CmsMacroFormatterBean extends CmsFormatterBean {

    /** The root path to the default content. */
    private String m_defaultContentRootPath;

    /** The UUID of the default content resource. */
    private CmsUUID m_defaultContentStructureId;

    /** The macro input. */
    private String m_macroInput;

    /** The online flag. */
    private boolean m_online;

    /** The placeholder macro input. */
    private String m_placeholderMacroInput;

    /** The referenced formatters. */
    private Map<String, CmsUUID> m_referencedFormatters;

    /**
     * Constructor for creating a new formatter configuration with resource structure id.<p>
     *
     * @param containerTypes the formatter container types
     * @param jspRootPath the formatter JSP VFS root path
     * @param jspStructureId the structure id of the formatter JSP
     * @param minWidth the formatter min width
     * @param maxWidth the formatter max width
     * @param searchContent indicates if the content should be searchable in the online index when this formatter is used
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     * @param niceName the configuration display name
     * @param description the description
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
     * @param macroInput the macro input
     * @param placeholderMacroInput the placeholder macro input
     * @param referencedFormatters the referenced formatters
     * @param online if this is the online version
     * @param metaMappings the meta mappings
     * @param useMetaMappingsForNormalElements if true, meta mappings will be evaluated for normal container elements, not just detail elements
     */
    public CmsMacroFormatterBean(
        Set<String> containerTypes,
        String jspRootPath,
        CmsUUID jspStructureId,
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
        String macroInput,
        String placeholderMacroInput,
        Map<String, CmsUUID> referencedFormatters,
        boolean online,
        List<CmsMetaMapping> metaMappings,
        boolean useMetaMappingsForNormalElements) {

        super(
            containerTypes,
            jspRootPath,
            jspStructureId,
            null,
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
        m_macroInput = macroInput;
        m_placeholderMacroInput = placeholderMacroInput;
        m_referencedFormatters = Collections.unmodifiableMap(referencedFormatters);
        m_online = online;
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
     * Returns the macro input.<p>
     *
     * @return the macro input
     */
    public String getMacroInput() {

        return m_macroInput;
    }

    /**
     * Returns the placeholder macro input.<p>
     *
     * @return the placeholder macro input
     */
    public String getPlaceholderMacroInput() {

        return m_placeholderMacroInput;
    }

    /**
     * The referenced formatters.<p>
     *
     * @return the reference formatters
     */
    public Map<String, CmsUUID> getReferencedFormatters() {

        return m_referencedFormatters;
    }

    /**
     * @see org.opencms.xml.containerpage.CmsFormatterBean#getSettings(org.opencms.ade.configuration.CmsADEConfigData)
     */
    @Override
    public Map<String, CmsXmlContentProperty> getSettings(CmsADEConfigData config) {

        LinkedHashMap<String, CmsXmlContentProperty> settings = new LinkedHashMap<String, CmsXmlContentProperty>(
            super.getSettings(config));
        for (CmsUUID formatterId : m_referencedFormatters.values()) {
            I_CmsFormatterBean formatter = OpenCms.getADEManager().getCachedFormatters(m_online).getFormatters().get(
                formatterId);
            if (formatter != null) {
                for (Entry<String, CmsXmlContentProperty> entry : formatter.getSettings(config).entrySet()) {
                    if (!settings.containsKey(entry.getKey())) {
                        settings.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return settings;
    }
}
