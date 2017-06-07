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

package org.opencms.configuration.preferences;

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

/**
 * Start gallery preference.<p>
 */
public class CmsStartGallleryPreference extends A_CmsPreference {

    /** The default value. */
    private String m_value;

    /** Prefix used for editor preference settings. */
    public static final String GALLERY_PREFIX = "gallery.";

    /** The gallery type for which this preference controls the start gallery. */
    private String m_galleryType;

    /**
     * Creates a new instance.<o>
     * @param galleryType the gallery type
     * @param value the value
     */
    public CmsStartGallleryPreference(String galleryType, String value) {

        m_galleryType = galleryType;
        m_value = value;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getDefaultValue()
     */
    public String getDefaultValue() {

        return m_value;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getName()
     */
    public String getName() {

        return GALLERY_PREFIX + m_galleryType;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition() {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            null, //widget
            null, //widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            null, //nicename
            null, //description
            null, //error
            null//preferfolder
        );
        return prop;
    }

    /**
     * @see org.opencms.configuration.preferences.A_CmsPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition(CmsObject cms) {

        return getPropertyDefinition();
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getTab()
     */
    public String getTab() {

        return "hidden";
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getValue(org.opencms.configuration.CmsDefaultUserSettings)
     */
    public String getValue(CmsDefaultUserSettings userSettings) {

        return userSettings.getStartGallery(m_galleryType);
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#setValue(org.opencms.configuration.CmsDefaultUserSettings, java.lang.String)
     */
    public void setValue(CmsDefaultUserSettings settings, String value) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value) && value.contains("/") && !(value.endsWith("/"))) {
            value += "/";
        }
        settings.setStartGallery(m_galleryType, value);
    }

}
