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

package org.opencms.configuration;

import java.util.List;

/**
 * Bean containing additional log folders available for the log file viewer.<p>
 */
public class CmsAdditionalLogFolderConfig implements I_CmsConfigurationParameterHandler {

    /** XML element name for the folder list. */
    public static final String N_ADDITIONAL_LOG_FOLDERS = "additional-log-folders";

    /** XML element name for the individual log folder. */
    public static final String N_LOG_FOLDER = "log-folder";

    /**
     * The config helper instance.
     **/
    public static CmsElementWithSubElementsParamConfigHelper ADD_LOG_FOLDER_HELPER = new CmsElementWithSubElementsParamConfigHelper(
        "*/" + CmsWorkplaceConfiguration.N_WORKPLACE,
        N_ADDITIONAL_LOG_FOLDERS,
        CmsAdditionalLogFolderConfig.class,
        N_LOG_FOLDER);

    /** The parameter configuration used to store the config values. */
    CmsParameterConfiguration m_params = new CmsParameterConfiguration();

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_params.add(paramName, paramValue.trim());
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_params;

    }

    /**
     * Gets the log folders.<p>
     *
     * @return the log folders
     */
    public List<String> getLogFolders() {

        return m_params.getList(N_LOG_FOLDER);

    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {
        // do nothing
    }

}
