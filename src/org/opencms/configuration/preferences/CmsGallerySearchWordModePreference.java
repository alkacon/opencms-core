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

package org.opencms.configuration.preferences;

import org.opencms.search.galleries.CmsGallerySearchParameters.CmsGallerySearchWordMode;
import org.opencms.xml.content.CmsXmlContentProperty;

/**
 * Class for the preference which controls how the search words entered in a gallery are pre-processed.
 */
public class CmsGallerySearchWordModePreference extends CmsBuiltinPreference {

    /** The preference name. */
    public static final String PREFERENCE_NAME = "gallerySearchWordMode";

    /** Widget configuration. */
    public static final String WIDGET_CONFIG = "plain:%(key.GUI_PREF_GALLERY_SEARCH_WORD_MODE_PLAIN_0)"
        + "|infix:%(key.GUI_PREF_GALLERY_SEARCH_WORD_MODE_INFIX_0)";

    /** The nice name. */
    private static final String NICE_NAME = "%(key."
        + org.opencms.workplace.commons.Messages.GUI_PREF_GALLERY_SEARCH_WORD_MODE_0
        + ")";

    /**
     * Creates a new instance.<p>
     *
     * @param propName the property name
     */
    public CmsGallerySearchWordModePreference(String propName) {

        super(propName);
        m_basic = true;

    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {

        return CmsGallerySearchWordMode.DEFAULT.name();
    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getPropertyDefinition()
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition() {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            "select_notnull", //widget
            WIDGET_CONFIG, //widgetconfig
            null, //regex
            null, //ruletype
            getDefaultValue(), //default
            NICE_NAME, //nicename
            null, //description
            null, //error
            null //preferfolder
        );
        return prop;

    }

}
