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
import org.opencms.xml.content.CmsXmlContentProperty;

/**
 * Interface describing a single preference value which can possibly be edited by the user.<p>
 */
public interface I_CmsPreference {

    /**
     * Creates the configuration for this preference.<p>
     *
     * @return the configuration for this preference
     */
    org.dom4j.Element createConfigurationItem();

    /**
     * Gets the default value for the preference.<p>
     *
     * @return the default value
     */
    String getDefaultValue();

    /**
     * Gets the preference name.<p>
     *
     * @return the preference name
     */
    String getName();

    /**
     * Gets the metadata describing how the setting should be edited.<p>
     *
     * @param cms the current CMS context
     *
     * @return the metadata for the client which describes how the setting should be edited
     */
    CmsXmlContentProperty getPropertyDefinition(CmsObject cms);

    /**
     * Gets the preference tab.<p>
     *
     * @return the preference tab
     */
    String getTab();

    /**
     * Reads the value of the preference from a CmsDefaultUserSettings instance .<p>
     *
     * @param userSettings the user settings from which to read the preference value
     *
     * @return the preference value
     */
    String getValue(CmsDefaultUserSettings userSettings);

    /**
     * Returns true if the preference is disabled.<p>
     * @param cms the current CMS context
     *
     * @return true if the preference is disabled
     */
    boolean isDisabled(CmsObject cms);

    /**
     * Sets the value of the preference in a CmsDefaultUserSettings instance.<p>
     *
     * @param settings the settings used to store the preference value
     * @param value the new value
     */
    void setValue(CmsDefaultUserSettings settings, String value);
}
