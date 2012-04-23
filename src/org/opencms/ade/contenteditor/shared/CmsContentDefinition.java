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

package org.opencms.ade.contenteditor.shared;

import com.alkacon.acacia.shared.AttributeConfiguration;
import com.alkacon.acacia.shared.ContentDefinition;
import com.alkacon.acacia.shared.Entity;
import com.alkacon.vie.shared.I_Type;

import java.util.List;
import java.util.Map;

/**
 * Contains all information needed for editing an XMLContent.<p>
 */
public class CmsContentDefinition extends ContentDefinition {

    /** The available locales. */
    private Map<String, String> m_availableLocales;

    /** The content locales. */
    private List<String> m_contentLocales;

    /** The resource type name. */
    private String m_resourceType;

    /** The site path. */
    private String m_sitePath;

    /** The content title. */
    private String m_title;

    /**
     * Constructor.<p>
     * 
     * @param entity the entity
     * @param configurations the attribute configurations
     * @param types the types
     * @param locale the content locale
     * @param contentLocales the content locales
     * @param availableLocales the available locales
     * @param title the content title
     * @param sitePath the site path
     * @param resourceType the resource type name
     */
    public CmsContentDefinition(
        Entity entity,
        Map<String, AttributeConfiguration> configurations,
        Map<String, I_Type> types,
        String locale,
        List<String> contentLocales,
        Map<String, String> availableLocales,
        String title,
        String sitePath,
        String resourceType) {

        super(entity, configurations, types, locale);
        m_contentLocales = contentLocales;
        m_availableLocales = availableLocales;
        m_title = title;
        m_sitePath = sitePath;
        m_resourceType = resourceType;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsContentDefinition() {

        super();
    }

    /**
     * Returns the available locales.<p>
     *
     * @return the available locales
     */
    public Map<String, String> getAvailableLocales() {

        return m_availableLocales;
    }

    /**
     * Returns the content locales.<p>
     *
     * @return the content locales
     */
    public List<String> getContentLocales() {

        return m_contentLocales;
    }

    /**
     * Returns the resource type.<p>
     *
     * @return the resource type
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

}
