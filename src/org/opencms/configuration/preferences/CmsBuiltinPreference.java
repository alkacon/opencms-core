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

package org.opencms.configuration.preferences;

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContentProperty;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;

/**
 * Preference subclass for built-in preferences accessed with a getter/setter pair via reflection.<p>
 */
public class CmsBuiltinPreference extends A_CmsPreference {

    /** True if this is a basic preference. */
    protected boolean m_basic;

    /** True if this is a hidden preference. */
    protected boolean m_hidden;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsBuiltinPreference.class);

    /** The name of the bean property used to access this preference. */
    private String m_propName;

    /**
     * Creates a new instance.<p>
     * 
     * @param propName the name of the bean property used to access this preference
     */
    public CmsBuiltinPreference(String propName) {

        m_propName = propName;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getDefaultValue()
     */
    public String getDefaultValue() {

        CmsUserSettingsStringPropertyWrapper wrapper = new CmsUserSettingsStringPropertyWrapper(
            CmsDefaultUserSettings.CURRENT_DEFAULT_SETTINGS);
        try {
            return (BeanUtils.getProperty(wrapper, m_propName));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getName()
     */
    public String getName() {

        return m_propName;
    }

    /**
     * @see org.opencms.configuration.preferences.A_CmsPreference#getPropertyDefinition()
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition() {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(m_propName, // name
            "string", // type
            null, //widget
            null, //widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            null, //nicename
            null, //description
            null, //error
            null //preferfolder
        );
        return prop;
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getTab()
     */
    public String getTab() {

        return m_hidden ? "hidden" : (m_basic ? "basic" : "extended");
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#getValue(org.opencms.configuration.CmsDefaultUserSettings)
     */
    public String getValue(CmsDefaultUserSettings userSettings) {

        CmsUserSettingsStringPropertyWrapper wrapper = new CmsUserSettingsStringPropertyWrapper(userSettings);
        try {
            return (BeanUtils.getProperty(wrapper, m_propName));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * @see org.opencms.configuration.preferences.I_CmsPreference#setValue(org.opencms.configuration.CmsDefaultUserSettings, java.lang.String)
     */
    public void setValue(CmsDefaultUserSettings settings, String value) {

        CmsUserSettingsStringPropertyWrapper wrapper = new CmsUserSettingsStringPropertyWrapper(settings);
        try {
            BeanUtils.setProperty(wrapper, m_propName, value);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
