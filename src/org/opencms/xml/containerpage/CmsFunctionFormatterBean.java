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

import org.opencms.file.types.CmsResourceTypeFunctionConfig;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

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
     * @param minWidth the formatter min width
     * @param maxWidth the formatter max width
     * @param location the location where this formatter was defined, should be an OpenCms VFS resource path
     * @param cssHeadIncludes the CSS head includes
     * @param inlineCss the in-line CSS
     * @param javascriptHeadIncludes the JavaScript headincludes
     * @param inlineJavascript the in-line JavaScript
     * @param niceName the configuration display name
     * @param description the description text for the formatter
     * @param id the configuration id
     * @param settings the settings configuration
     * @param hasNestedContainers <code>true</code> if this formatter has nested containers
     * @param isStrictContainers <code>true</code> if this formatter will always render all nested containers
     * @param parameters the request parameters to add for the included JSP
     */
    public CmsFunctionFormatterBean(
        Set<String> containerTypes,
        String jspRootPath,
        CmsUUID jspStructureId,
        int minWidth,
        int maxWidth,
        String location,
        List<String> cssHeadIncludes,
        String inlineCss,
        List<String> javascriptHeadIncludes,
        String inlineJavascript,
        String niceName,
        String description,
        String id,
        Map<String, CmsXmlContentProperty> settings,
        boolean hasNestedContainers,
        boolean isStrictContainers,
        Map<String, String[]> parameters) {

        super(
            containerTypes,
            jspRootPath,
            jspStructureId,
            minWidth,
            maxWidth,
            true, // preview
            false, // searchContent
            location,
            cssHeadIncludes,
            inlineCss,
            javascriptHeadIncludes,
            inlineJavascript,
            niceName,
            description,
            Arrays.asList(CmsResourceTypeFunctionConfig.TYPE_NAME),
            10099, // rank
            id,
            settings,
            true, //isFromConfigFile
            true, // isAutoEnabled
            false, // isDetail
            false, // isDisplay
            hasNestedContainers,
            isStrictContainers,
            false, // nestedFormatterSettings
            Collections.<CmsMetaMapping> emptyList());
        m_realJspRootPath = jspRootPath;
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

        return CmsResourceTypeFunctionConfig.FORMATTER_ID;
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