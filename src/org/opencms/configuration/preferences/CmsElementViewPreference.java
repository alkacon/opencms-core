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

import org.opencms.ade.configuration.CmsElementView;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collection;
import java.util.Locale;

/**
 * Element view preference configuration.<p>
 */
public class CmsElementViewPreference extends CmsBuiltinPreference {

    /** The preference name. */
    public static final String PREFERENCE_NAME = "elementView";

    /** The nice name. */
    private static final String NICE_NAME = "%(key."
        + org.opencms.workplace.commons.Messages.GUI_PREF_ELEMENT_VIEW_0
        + ")";

    /** Preference name for the explorer. */
    public static final String EXPLORER_PREFERENCE_NAME = "explorerElementView";

    /**
     * Constructor.<p>
     *
     * @param propName the name of the bean property used to access this preference
     */
    public CmsElementViewPreference(String propName) {

        super(propName);
        m_basic = false;
    }

    /**
     * Gets the nice name key.<p>
     *
     * @return the nice name key
     */
    public String getNiceName() {

        return NICE_NAME;
    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getPropertyDefinition()
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
            getDefaultValue(), //default
            getNiceName(), //nicename
            null, //description
            null, //error
            null //preferfolder
        );
        return prop;
    }

    /**
     * @see org.opencms.configuration.preferences.A_CmsPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition(CmsObject cms) {

        Collection<CmsElementView> views = OpenCms.getADEManager().getElementViews(cms).values();
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        StringBuffer resultBuffer = new StringBuffer();
        boolean first = true;
        for (CmsElementView view : views) {
            if (view.hasPermission(cms, null) && !view.isOther()) {
                if (!first) {
                    resultBuffer.append("|");
                }
                first = false;
                resultBuffer.append(view.getId().toString());
                resultBuffer.append(":");
                resultBuffer.append(view.getTitle(cms, wpLocale));
            }
        }
        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            "select_notnull", //widget
            resultBuffer.toString(), //widgetconfig
            null, //regex
            null, //ruletype
            getDefaultValue(), //default
            getNiceName(), //nicename
            null, //description
            null, //error
            null //preferfolder
        );
        return prop;
    }
}
