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

package org.opencms.gwt.shared;

import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean for transferring user preference information between the client and the server.<p>
 */
public class CmsUserSettingsBean implements IsSerializable {

    /** Map of property definitions corresponding to the user settings. */
    private LinkedHashMap<String, CmsXmlContentProperty> m_settingConfiguration = new LinkedHashMap<String, CmsXmlContentProperty>();

    /** Map of the current values of the user settings. */
    private Map<String, String> m_values = new HashMap<String, String>();

    /** Set of keys of the "basic" (as opposed to "extended") options. */
    private Set<String> m_basicOptions = new HashSet<String>();

    /**
     * Default constructor.<p>
     */
    public CmsUserSettingsBean() {

        // do nothing

    }

    /**
     * Adds a user setting.<p>
     *
     * @param value the current value of the user setting
     * @param config the configuration for the user setting
     *
     * @param basic true if this is a basic user setting
     */
    public void addSetting(String value, CmsXmlContentProperty config, boolean basic) {

        m_values.put(config.getName(), value);
        m_settingConfiguration.put(config.getName(), config);
        if (basic) {
            m_basicOptions.add(config.getName());
        }
    }

    /**
     * Gets the map  with the configurations for the individual user settings.<p>
     *
     * @return the user setting configurations, indexed by user setting name
     */
    public Map<String, CmsXmlContentProperty> getConfiguration() {

        return Collections.unmodifiableMap(m_settingConfiguration);
    }

    /**
     * Gets the value for a given user setting.<p>
     *
     * @param key the user setting key
     *
     * @return the current value of the user setting
     */
    public String getValue(String key) {

        return m_values.get(key);
    }

    /**
     * Returns true if the user setting with the given name is a basic setting
     *
     * @param name the user setting name
     *
     * @return true if this is a basic setting
     */
    public boolean isBasic(String name) {

        return m_basicOptions.contains(name);
    }

}
