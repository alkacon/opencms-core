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
import org.opencms.ade.configuration.plugins.CmsTemplatePlugin;
import org.opencms.file.types.CmsResourceTypeFunctionConfig;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A bean containing formatter configuration data as strings.<p>
 */
public class CmsFunctionFormatterBean extends CmsFormatterBean {

    /** The standard function formatter structure id. */
    private CmsUUID m_functionFormatterId;

    /** The request parameters to add for the included JSP. */
    private Map<String, String[]> m_parameters = new HashMap<>();

    /** The real path of the configured jsp. */
    private String m_realJspRootPath;

    /**
     * Constructor for creating a new formatter configuration with resource structure id.<p>
     *
     * @param containerTypes the formatter container types
     * @param jspRootPath the formatter JSP VFS root path
     * @param jspStructureId the structure id of the formatter JSP
     * @param key the key
     * @param aliasKeys the alias keys
     * @param functionFormatterId the standard function formatter structure id
     * @param minWidth the formatter min width
     * @param maxWidth the formatter max width
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     * @param cssHeadIncludes the CSS head includes
     * @param inlineCss the in-line CSS
     * @param javascriptHeadIncludes the JavaScript headincludes
     * @param inlineJavascript the in-line JavaScript
     * @param plugins the template plugins
     * @param niceName the configuration display name
     * @param description the description text for the formatter
     * @param id the configuration id
     * @param settingConfig the settings configuration
     * @param isAllowsSettingsInEditor whether this formatter allows settings to be edited in the content editor
     * @param isStrictContainers <code>true</code> if this formatter will always render all nested containers
     * @param parameters the request parameters to add for the included JSP
     */
    public CmsFunctionFormatterBean(
        Set<String> containerTypes,
        String jspRootPath,
        CmsUUID jspStructureId,
        String key,
        Set<String> aliasKeys,
        CmsUUID functionFormatterId,
        int minWidth,
        int maxWidth,
        String location,
        List<String> cssHeadIncludes,
        String inlineCss,
        List<String> javascriptHeadIncludes,
        String inlineJavascript,
        List<CmsTemplatePlugin> plugins,
        String niceName,
        String description,
        String id,
        CmsSettingConfiguration settingConfig,
        boolean isAllowsSettingsInEditor,
        boolean isStrictContainers,
        Map<String, String[]> parameters) {

        super(
            containerTypes,
            jspRootPath,
            jspStructureId,
            key,
            aliasKeys,
            minWidth,
            maxWidth,
            true, // preview
            false, // searchContent
            location,
            cssHeadIncludes,
            inlineCss,
            javascriptHeadIncludes,
            inlineJavascript,
            plugins,
            niceName,
            description,
            Arrays.asList(CmsResourceTypeFunctionConfig.TYPE_NAME),
            10099, // rank
            id,
            settingConfig,
            true, //isFromConfigFile
            true, // isAutoEnabled
            false, // detailType
            null, // isDisplay
            isAllowsSettingsInEditor,
            isStrictContainers,
            false, // nestedFormatterSettings
            Collections.<CmsMetaMapping> emptyList(),
            Collections.emptyMap(),
            false);
        m_realJspRootPath = jspRootPath;
        m_functionFormatterId = functionFormatterId;
        if (parameters != null) {
            m_parameters.putAll(parameters);
        }
    }

    /**
     * @see org.opencms.xml.containerpage.CmsFormatterBean#getJspRootPath()
     *
     * This is not the configured JSP, but the formatter JSP for the function_config type itself.
     */
    @Override
    public String getJspRootPath() {

        return CmsResourceTypeFunctionConfig.FORMATTER_PATH;
    }

    /**
     * @see org.opencms.xml.containerpage.CmsFormatterBean#getJspStructureId()
     *
     * This is not the configured JSP, but the formatter JSP for the function_config type itself.
     */
    @Override
    public CmsUUID getJspStructureId() {

        return m_functionFormatterId;
    }

    /**
     * Gets the parameters the dynamic function should set for the included JSP.<p>
     *
     * @return the map of parameters to add for the included JSP
     */
    public Map<String, String[]> getParameters() {

        return Collections.unmodifiableMap(m_parameters);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getRank()
     */
    @Override
    public int getRank() {

        return 1000;
    }

    /**
     * Gets the <em>actual</em> JSP id of the function.<p>
     *
     * @return the JSP structure id
     */
    public CmsUUID getRealJspId() {

        return super.getJspStructureId();
    }

    /**
     * Gets the root path of the configured JSP.<p>
     *
     * @return the root path of the configured JSP
     */
    public String getRealJspRootPath() {

        return m_realJspRootPath;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getResourceTypeNames()
     */
    @Override
    public Collection<String> getResourceTypeNames() {

        return Collections.singletonList(CmsResourceTypeFunctionConfig.TYPE_NAME);
    }

}