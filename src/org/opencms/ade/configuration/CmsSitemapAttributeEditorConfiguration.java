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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Contains a set of attribute definitions for the sitemap attribute editor.
 */
public class CmsSitemapAttributeEditorConfiguration {

    /** A configuration with no entries. */
    public static final CmsSitemapAttributeEditorConfiguration EMPTY = new CmsSitemapAttributeEditorConfiguration(
        new HashMap<>());

    /** The actual attribute definitions. */
    private Map<String, CmsXmlContentProperty> m_attributeDefinitions;

    /**
     * Creates a new instance.
     *
     * @param attributeDefinitions the sitemap attribute definitions
     */
    public CmsSitemapAttributeEditorConfiguration(Map<String, CmsXmlContentProperty> attributeDefinitions) {

        super();
        m_attributeDefinitions = Collections.unmodifiableMap(new LinkedHashMap<>(attributeDefinitions));
    }

    /**
     * Reads the attribute definitions from an XML content.
     *
     * @param cms the CmsObject  to use
     * @param res the resource from which to read the attribute definitions
     * @return the sitemap attribute editor configuration which was read from the file
     * @throws CmsException if something goes wrong
     */
    public static CmsSitemapAttributeEditorConfiguration read(CmsObject cms, CmsResource res) throws CmsException {

        Map<String, CmsXmlContentProperty> resultMap = new LinkedHashMap<>();
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(res));
        CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, Locale.ENGLISH);
        for (I_CmsXmlContentValueLocation loc : root.getSubValues("Setting")) {
            CmsXmlContentProperty propDef = CmsConfigurationReader.parseProperty(cms, loc).getPropertyData();
            resultMap.put(propDef.getName(), propDef);
        }
        return new CmsSitemapAttributeEditorConfiguration(resultMap);
    }

    /**
     * Gets the attribute definitions.
     *
     * @return the attribute definitions
     */
    public Map<String, CmsXmlContentProperty> getAttributeDefinitions() {

        return m_attributeDefinitions;
    }

}
