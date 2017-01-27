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

import org.dom4j.Element;

/**
 * Wrapper used for built-in preferene which have also been configured in opencms-workplace.xml.<p>
 */
public class CmsWrapperPreference implements I_CmsPreference {

    /** The configured preference data. */
    private CmsPreferenceData m_prefData;

    /** The preference being wrapped. */
    private I_CmsPreference m_wrappedPreference;

    /**
     * Creates a new instance.<p>
     *
     * @param prefData the configured preference data
     *
     * @param wrappedPref the preference being wrapped
     */
    public CmsWrapperPreference(CmsPreferenceData prefData, I_CmsPreference wrappedPref) {

        m_prefData = prefData;
        m_wrappedPreference = wrappedPref;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#createConfigurationItem()
     */
    public Element createConfigurationItem() {

        Element elem = m_wrappedPreference.createConfigurationItem();
        CmsUserDefinedPreference.fillAttributes(m_prefData, elem);
        return elem;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getDefaultValue()
     */
    public String getDefaultValue() {

        return firstNotNull(m_prefData.getDefaultValue(), m_wrappedPreference.getDefaultValue());
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getName()
     */
    public String getName() {

        return m_wrappedPreference.getName();
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition(CmsObject cms) {

        CmsXmlContentProperty configProp = m_prefData.getPropertyDefinition();
        CmsXmlContentProperty wrappedProp = m_wrappedPreference.getPropertyDefinition(cms);

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            firstNotNull(configProp.getWidget(), wrappedProp.getWidget()), //widget
            firstNotNull(configProp.getWidgetConfiguration(), wrappedProp.getWidgetConfiguration()), //widgetconfig
            firstNotNull(configProp.getRuleRegex(), wrappedProp.getRuleRegex()), //regex
            firstNotNull(configProp.getRuleType(), wrappedProp.getRuleType()), //ruletype
            firstNotNull(configProp.getDefault(), wrappedProp.getDefault()),
            firstNotNull(configProp.getNiceName(), wrappedProp.getNiceName()), //nicename
            firstNotNull(configProp.getDescription(), wrappedProp.getDescription()), //description
            firstNotNull(configProp.getError(), wrappedProp.getError()), //error
            null//preferfolder
        );

        return prop;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getTab()
     */
    public String getTab() {

        return firstNotNull(m_prefData.getTab(), m_wrappedPreference.getTab());
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getValue(org.opencms.configuration.CmsDefaultUserSettings)
     */
    public String getValue(CmsDefaultUserSettings userSettings) {

        return m_wrappedPreference.getValue(userSettings);
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#isDisabled(CmsObject)
     */
    public boolean isDisabled(CmsObject cms) {

        return m_wrappedPreference.isDisabled(cms);
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#setValue(org.opencms.configuration.CmsDefaultUserSettings, java.lang.String)
     */
    public void setValue(CmsDefaultUserSettings settings, String value) {

        m_wrappedPreference.setValue(settings, value);
    }

    /**
     * Returns the first non-null value.<p>
     *
     * @param a a value
     * @param b another value
     *
     * @return the first non-null value
     */
    String firstNotNull(String a, String b) {

        return a != null ? a : b;
    }

}
