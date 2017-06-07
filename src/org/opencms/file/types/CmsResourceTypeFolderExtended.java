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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.types;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.util.CmsStringUtil;

/**
 * Resource type descriptor for extended folder types (like for example the workplace galleries).<p>
 *
 * This type extends a folder but has a configurable type id and type name.
 * Optionally, a workplace class name for the type and a parameter String can be provided.<p>
 *
 * @since 6.0.0
 */
public class CmsResourceTypeFolderExtended extends A_CmsResourceTypeFolderBase {

    /** Configuration key for the optional folder class name. */
    public static final String CONFIGURATION_FOLDER_CLASS = "folder.class";

    /** Configuration key for the optional folder class parameters. */
    public static final String CONFIGURATION_FOLDER_CLASS_PARAMS = "folder.class.params";

    /** The configured folder class name for this folder type. */
    private String m_folderClassName;

    /** The configured folder parameters for this folder type. */
    private String m_folderClassParams;

    /**
     * @see org.opencms.file.types.A_CmsResourceType#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        super.addConfigurationParameter(paramName, paramValue);
        if (CmsStringUtil.isNotEmpty(paramName) && CmsStringUtil.isNotEmpty(paramValue)) {
            if (CONFIGURATION_FOLDER_CLASS.equalsIgnoreCase(paramName)) {
                m_folderClassName = paramValue.trim();
            }
            if (CONFIGURATION_FOLDER_CLASS_PARAMS.equalsIgnoreCase(paramName)) {
                m_folderClassParams = paramValue.trim();
            }
        }
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        CmsParameterConfiguration result = new CmsParameterConfiguration();
        CmsParameterConfiguration additional = super.getConfiguration();
        if ((additional != null) && (additional.size() > 0)) {
            result.putAll(additional);
        }
        if (CmsStringUtil.isNotEmpty(getFolderClassName())) {
            result.put(CONFIGURATION_FOLDER_CLASS, getFolderClassName());
        }
        if (CmsStringUtil.isNotEmpty(getFolderClassParams())) {
            result.put(CONFIGURATION_FOLDER_CLASS_PARAMS, getFolderClassParams());
        }
        return result;
    }

    /**
     * Returns the (optional) configured folder class name for this folder.<p>
     *
     * @return the (optional) configured folder class name for this folder
     */
    public String getFolderClassName() {

        return m_folderClassName;
    }

    /**
     * Returns the (optional) configured folder class parameters name for this folder.<p>
     *
     * @return the (optional) configured folder class parameters for this folder
     */
    public String getFolderClassParams() {

        return m_folderClassParams;
    }
}