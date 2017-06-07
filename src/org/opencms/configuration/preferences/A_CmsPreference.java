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

import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.xml.content.CmsXmlContentProperty;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Abstract superclass for preferences.<p>
 */
public abstract class A_CmsPreference implements I_CmsPreference {

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#createConfigurationItem()
     */
    public Element createConfigurationItem() {

        CmsXmlContentProperty prop = getPropertyDefinition();
        Element elem = DocumentHelper.createElement(CmsWorkplaceConfiguration.N_PREFERENCE);
        for (String[] attrToSet : new String[][] {
            {I_CmsXmlConfiguration.A_NAME, getName()},
            {I_CmsXmlConfiguration.A_VALUE, getDefaultValue()},
            {CmsWorkplaceConfiguration.A_NICE_NAME, prop.getNiceName()},
            {CmsWorkplaceConfiguration.A_DESCRIPTION, prop.getDescription()},
            {CmsWorkplaceConfiguration.A_WIDGET, prop.getWidget()},
            {CmsWorkplaceConfiguration.A_WIDGET_CONFIG, prop.getWidgetConfiguration()},
            {CmsWorkplaceConfiguration.A_RULE_REGEX, prop.getRuleRegex()},
            {CmsWorkplaceConfiguration.A_ERROR, prop.getError()}}) {
            String attrName = attrToSet[0];
            String value = attrToSet[1];
            if (value != null) {
                elem.addAttribute(attrName, value);
            }
        }
        return elem;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    public CmsXmlContentProperty getPropertyDefinition(CmsObject cms) {

        return getPropertyDefinition();
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#isDisabled(CmsObject)
     */
    public boolean isDisabled(CmsObject cms) {

        return false;
    }

    /**
     * Gets the user-independent property configuration.<p>
     *
     * This is what is used to write the preference back to the workplace configuration.
     *
     * @return the property configuration
     */
    protected abstract CmsXmlContentProperty getPropertyDefinition();

}
