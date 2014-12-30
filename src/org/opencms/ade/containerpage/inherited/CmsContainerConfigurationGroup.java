/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.i18n.CmsLocaleManager;

import java.util.Locale;
import java.util.Map;

/**
 * A class which represents all the configuration entries which have been read from an inherited container
 * configuration file.<p>
 * 
 */
public class CmsContainerConfigurationGroup {

    /** The configurations grouped by locales. */
    private Map<Locale, Map<String, CmsContainerConfiguration>> m_configurations;

    /**
     * Creates a new instance.<p>
     * 
     * @param configurations the data contained by this configuration group
     */
    public CmsContainerConfigurationGroup(Map<Locale, Map<String, CmsContainerConfiguration>> configurations) {

        m_configurations = configurations;
    }

    /**
     * Gets the configuration for a given name and locale.<p>
     * 
     * @param name the configuration name 
     * 
     * @return the configuration for the name and locale 
     */
    public CmsContainerConfiguration getConfiguration(String name) {

        Map<String, CmsContainerConfiguration> configurationsForLocale = null;
        if (m_configurations.containsKey(CmsLocaleManager.MASTER_LOCALE)) {
            configurationsForLocale = m_configurations.get(CmsLocaleManager.MASTER_LOCALE);
        } else if (!m_configurations.isEmpty()) {
            configurationsForLocale = m_configurations.values().iterator().next();
        } else {
            return null;
        }
        return configurationsForLocale.get(name);
    }
}
