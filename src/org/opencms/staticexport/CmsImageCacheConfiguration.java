/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.util.CmsStringUtil;

/**
 * Configuration for generated image cache storage.<p>
 */
public class CmsImageCacheConfiguration {

    /** If this configuration was explicitly configured. */
    private boolean m_configured;

    /** The configured image cache implementation class name. */
    private String m_className;

    /** The configured image cache parameters. */
    private CmsParameterConfiguration m_parameters = new CmsParameterConfiguration();

    /**
     * Adds a configuration parameter.<p>
     *
     * @param paramName the parameter name
     * @param paramValue the parameter value
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_parameters.add(paramName, paramValue);
        m_configured = true;
    }

    /**
     * Returns the image cache implementation class name.<p>
     *
     * @return the image cache implementation class name
     */
    public String getClassName() {

        return m_className;
    }

    /**
     * Returns the configuration parameters.<p>
     *
     * @return the configuration parameters
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_parameters;
    }

    /**
     * Returns if image cache settings have been configured explicitly.<p>
     *
     * @return <code>true</code> if image cache settings have been configured
     */
    public boolean isConfigured() {

        return m_configured;
    }

    /**
     * Sets the image cache implementation class name.<p>
     *
     * @param className the image cache implementation class name
     */
    public void setClassName(String className) {

        m_className = CmsStringUtil.isEmptyOrWhitespaceOnly(className) ? null : className.trim();
        if (m_className != null) {
            m_configured = true;
        }
    }
}
