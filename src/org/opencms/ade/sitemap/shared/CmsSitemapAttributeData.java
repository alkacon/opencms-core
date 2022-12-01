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

package org.opencms.ade.sitemap.shared;

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The data needed by the sitemap attribute editor dialog in the sitemap editor.
 */
public class CmsSitemapAttributeData implements IsSerializable {

    /** The attribute definitions. */
    protected LinkedHashMap<String, CmsXmlContentProperty> m_attributeDefinitions;

    /** The attribute values. */
    protected Map<String, String> m_attributeValues;

    /** The list info bean for the sitemap configuration file. */
    protected CmsListInfoBean m_sitemapInfo;

    /** The URL to call to unlock the sitemap configuration file. */
    protected String m_unlockUrl;

    /**
     * Creates a new instance.
     *
     * @param sitemapInfo the list info bean for the sitemap configuration file
     * @param attributeDefinitions the attribute definitions
     * @param attributeValues the attribute values
     * @param unlockUrl the URL used to unlock the configuration file
     */
    public CmsSitemapAttributeData(
        CmsListInfoBean sitemapInfo,
        Map<String, CmsXmlContentProperty> attributeDefinitions,
        Map<String, String> attributeValues,
        String unlockUrl) {

        m_sitemapInfo = sitemapInfo;
        m_attributeDefinitions = new LinkedHashMap<>(attributeDefinitions);
        m_attributeValues = new HashMap<>(attributeValues);
        m_unlockUrl = unlockUrl;
    }

    /**
     * Default constructor for serialization.
     */
    protected CmsSitemapAttributeData() {

        // empty
    }

    /**
     * Gets the attribute definitions.
     *
     * @return the attribute definitions
     */
    public LinkedHashMap<String, CmsXmlContentProperty> getAttributeDefinitions() {

        return m_attributeDefinitions;
    }

    /**
     * Gets the attribute values
     *
     * @return the attribute values
     */
    public Map<String, String> getAttributeValues() {

        return m_attributeValues;
    }

    /**
     * Gets the list info for the sitemap configuration file.
     *
     * @return the list info for the sitemap configuration
     */
    public CmsListInfoBean getInfo() {

        return m_sitemapInfo;

    }

    /**
     * Gets the URL which should be called to unlock the sitemap configuration
     *
     * @return the URL to unlock the sitemap configuration
     */
    public String getUnlockUrl() {

        return m_unlockUrl;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "[CmsSitemapAttributeData: definitions="
            + m_attributeDefinitions
            + ", values="
            + m_attributeValues
            + "]";
    }

}
