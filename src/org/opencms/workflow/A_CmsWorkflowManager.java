/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workflow;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Abstract class which provides common functionality for workflow managers, like initialization of
 * the configuration parameters.<p>
 *
 */
public abstract class A_CmsWorkflowManager implements I_CmsWorkflowManager {

    /** The CMS context with admin privileges. */
    protected CmsObject m_adminCms;

    /** The map of configuration parameters. */
    protected Map<String, String> m_parameters;

    /**
     * Gets the parameters of the workflow manager.<p>
     *
     * @return the configuration parameters of the workflow manager
     */
    public Map<String, String> getParameters() {

        return Collections.unmodifiableMap(m_parameters);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject adminCms) {

        m_adminCms = adminCms;
    }

    /**
     * Sets the configuration parameters of the workflow manager.<p>
     *
     * @param parameters the map of configuration parameters
     */
    public void setParameters(Map<String, String> parameters) {

        if (m_parameters != null) {
            throw new IllegalStateException();
        }
        m_parameters = parameters;
    }

    /**
     * Gets the locale to use for a given CMS context.<p>
     *
     * @param userCms the CMS context
     *
     * @return the locale to use
     */
    protected Locale getLocale(CmsObject userCms) {

        return OpenCms.getWorkplaceManager().getWorkplaceLocale(userCms);
    }

    /**
     * Gets the configuration parameter for a given key, and if it doesn't find one, returns a default value.<p>
     *
     * @param key the configuration key
     * @param defaultValue the default value to use when the configuration entry isn't found
     *
     * @return the configuration value
     */
    protected String getParameter(String key, String defaultValue) {

        String result = m_parameters.get(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

}
