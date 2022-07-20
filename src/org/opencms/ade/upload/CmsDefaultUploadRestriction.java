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

package org.opencms.ade.upload;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsUploadRestrictionInfo;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;

import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Default implementation for upload restrictions uses restriction entries from opencms-workplace.xml.
 *
 * <p>This class directly takes parameters configured via param elements in opencms-workplace.xml and interprets them as restriction
 * entries. The parameter name is interpreted as the path, and the parameter value
 */
public class CmsDefaultUploadRestriction implements I_CmsUploadRestriction {

    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsDefaultUploadRestriction.class);

    /** The internal CmsObject. */
    private CmsObject m_cms;

    /** The configuration. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    public static I_CmsUploadRestriction unrestricted() {

        CmsDefaultUploadRestriction result = new CmsDefaultUploadRestriction();
        result.addConfigurationParameter("/", CmsUploadRestrictionInfo.UNRESTRICTED_UPLOADS);
        return result;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_config;
    }

    /**
     * @see org.opencms.ade.upload.I_CmsUploadRestriction#getUploadRestrictionInfo(org.opencms.file.CmsObject)
     */
    public CmsUploadRestrictionInfo getUploadRestrictionInfo(CmsObject cms) {

        if ((m_cms != null) && !OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN)) { // root admins may have to upload arbitrary files for things like updates / administration stuff
            CmsUploadRestrictionInfo.Builder builder = new CmsUploadRestrictionInfo.Builder();
            for (Map.Entry<String, String> entry : m_config.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.startsWith("/")) {
                    builder.add(key, value);
                }
            }
            return builder.build();
        }
        return new CmsUploadRestrictionInfo.Builder().add("/", CmsUploadRestrictionInfo.UNRESTRICTED_UPLOADS).build();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // do nothing
    }

    /**
     * @see org.opencms.configuration.I_CmsNeedsAdminCmsObject#setAdminCmsObject(org.opencms.file.CmsObject)
     */
    public void setAdminCmsObject(CmsObject adminCms) {

        m_cms = adminCms;
    }

}
