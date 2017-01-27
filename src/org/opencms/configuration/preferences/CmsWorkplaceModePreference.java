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

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContentProperty;

/**
 * Workplace mode preference configuration.<p>
 */
public class CmsWorkplaceModePreference extends CmsBuiltinPreference {

    /** The preference name. */
    public static final String PREFERENCE_NAME = "workplaceMode";

    /** The nice name. */
    private static final String NICE_NAME = "%(key." + org.opencms.ui.Messages.GUI_PREF_WORKPLACE_MODE_0 + ")";

    /** Widget configuration. */
    public static final String WIDGET_CONFIG = "old:%(key.GUI_PREF_WORKPLACE_MODE_OLD_0)|new:%(key.GUI_PREF_WORKPLACE_MODE_NEW_0)";

    /**
     * Constructor.<p>
     *
     * @param propName the name of the bean property used to access this preference
     */
    public CmsWorkplaceModePreference(String propName) {

        super(propName);
        m_basic = false;
    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {

        return "new";
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
            "select_notnull", //widget
            WIDGET_CONFIG, //widgetconfig
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

        return getPropertyDefinition();
    }

    /**
     * @see org.opencms.configuration.preferences.A_CmsPreference#isDisabled(CmsObject)
     */
    @Override
    public boolean isDisabled(CmsObject cms) {

        return !OpenCms.getModuleManager().hasModule("org.opencms.workplace.traditional");
    }
}
