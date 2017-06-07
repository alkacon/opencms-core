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

package org.opencms.file.types;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.util.CmsStringUtil;

/**
 * Resource type descriptor for extended folder types (like for example the workplace galleries).<p>
 *
 * This type extends a folder but has a configurable type id and type name.
 * Optionally, a workplace class name for the type and a parameter String can be provided.<p>
 *
 * @since 7.6.0
 */
public class CmsRecourceTypeFolderGallery extends CmsResourceTypeFolderExtended {

    /** Configuration key for the optional folder class name. */
    public static final String CONFIGURATION_FOLDER_CONTENT_TYPES = "folder.content.types";

    /** The resource-types of the folder content. */
    private String m_folderContentTypes;

    /**
     * @see org.opencms.file.types.A_CmsResourceType#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        super.addConfigurationParameter(paramName, paramValue);
        if (CmsStringUtil.isNotEmpty(paramName) && CmsStringUtil.isNotEmpty(paramValue)) {
            if (CONFIGURATION_FOLDER_CONTENT_TYPES.equalsIgnoreCase(paramName)) {
                m_folderContentTypes = paramValue.trim();
            }

        }
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        CmsParameterConfiguration result = new CmsParameterConfiguration();
        if (CmsStringUtil.isNotEmpty(getFolderContentTypes())) {
            result.add(CONFIGURATION_FOLDER_CONTENT_TYPES, getFolderContentTypes());
        }
        CmsParameterConfiguration additional = super.getConfiguration();
        if ((additional != null) && (additional.size() > 0)) {
            result.putAll(additional);
        }
        return result;
    }

    /**
     * Returns the folder content types.<p>
     *
     * @return the folder content types
     */
    public String getFolderContentTypes() {

        return m_folderContentTypes;
    }

}
