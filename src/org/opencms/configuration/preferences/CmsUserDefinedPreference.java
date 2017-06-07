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
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.xml.content.CmsXmlContentProperty;

import org.dom4j.Element;

/**
 * Subclass for user-defined preferences.<p>
 */
public class CmsUserDefinedPreference extends A_CmsPreference {

    /** The configured preference data. */
    private CmsPreferenceData m_preferenceData;

    /**
     * Creates a new instance.<p>
     *
     * @param name the preference name
     * @param value the preference value
     * @param prop the bean containing the widget configuration for the preference
     * @param tab the tab on which to display the widget
     */
    public CmsUserDefinedPreference(String name, String value, CmsXmlContentProperty prop, String tab) {

        m_preferenceData = new CmsPreferenceData(name, value, prop, tab);
    }

    /**
     * Helper method used to create the configuration attributes for a CmsPreferenceData bean.<p>
     *
     * @param pref the preference data
     * @param elem the element in which the attributes should be created
     */
    public static void fillAttributes(CmsPreferenceData pref, Element elem) {

        CmsXmlContentProperty prop = pref.getPropertyDefinition();
        for (String[] attrToSet : new String[][] {
            {I_CmsXmlConfiguration.A_VALUE, pref.getDefaultValue()},
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
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getDefaultValue()
     */
    public String getDefaultValue() {

        return m_preferenceData.getDefaultValue();
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getName()
     */
    public String getName() {

        return m_preferenceData.getName();
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition(CmsObject cms) {

        return getPropertyDefinition();
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getTab()
     */
    public String getTab() {

        return m_preferenceData.getTab();
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getValue(org.opencms.configuration.CmsDefaultUserSettings)
     */
    public String getValue(CmsDefaultUserSettings userSettings) {

        return userSettings.getAdditionalPreference(getName(), true);
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#setValue(org.opencms.configuration.CmsDefaultUserSettings, java.lang.String)
     */
    public void setValue(CmsDefaultUserSettings settings, String value) {

        settings.setAdditionalPreference(getName(), value);
    }

    /**
     * @see org.opencms.configuration.preferences.A_CmsPreference#getPropertyDefinition()
     */
    @Override
    protected CmsXmlContentProperty getPropertyDefinition() {

        return m_preferenceData.getPropertyDefinition();
    }

}
