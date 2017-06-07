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

import org.opencms.xml.content.CmsXmlContentProperty;

/**
 * Bean representing the configurable attributes for a preference.<p>
 */
public class CmsPreferenceData {

    /** The pref name. */
    private String m_name;

    /** The pref default value. */
    private String m_value;

    /** The pref tab. */
    private String m_tab;

    /** The preference widget configuration. */
    private CmsXmlContentProperty m_propDef;

    /**
     * Creates a new instance.<p>
     *
     * @param name the preference name
     * @param value the preference value
     * @param prop the preference configuration
     * @param tab the tab on which to display the preference
     */
    public CmsPreferenceData(String name, String value, CmsXmlContentProperty prop, String tab) {

        m_name = name;
        m_value = value;
        m_propDef = prop;
        m_tab = tab;
    }

    /**
     * Gets the default value for the preference.<p>
     *
     * @return the default value for the preference
     */
    public String getDefaultValue() {

        return m_value;
    }

    /**
     * Gets the name of the preference.<p>
     *
     * @return the preference name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the preference definition.<p>
     *
     * @return the preference definition
     */
    public CmsXmlContentProperty getPropertyDefinition() {

        return m_propDef;
    }

    /**
     * Gets the tab on which the preference should be displayed.<p>
     *
     * @return the tab on which the preference should be displayed
     */
    public String getTab() {

        return m_tab;
    }

}
