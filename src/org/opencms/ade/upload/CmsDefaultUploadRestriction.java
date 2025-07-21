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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Default implementation for upload restrictions uses restriction entries from opencms-workplace.xml.
 *
 * <p>This class directly takes parameters configured via param elements in opencms-workplace.xml and interprets them as restriction
 * entries. The parameter name is interpreted as the path, and the parameter value
 */
public class CmsDefaultUploadRestriction implements I_CmsUploadRestriction {

    /** Sitemap attribute to control the allowed upload extensions. */
    public static final String ATTR_UPLOAD_EXTENSIONS = "upload.extensions";

    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsDefaultUploadRestriction.class);

    /** The internal CmsObject. */
    private CmsObject m_cms;

    /** The configuration. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /** Cache for upload restrictions - caches just a single value. */
    private Cache<String, CmsUploadRestrictionInfo> m_cache = CacheBuilder.newBuilder().expireAfterWrite(
        1,
        TimeUnit.SECONDS).build();

    /**
     * Creates a new instance.
     */
    public CmsDefaultUploadRestriction() {

    }

    /**
     * Utility method for creating an upload restriction that doesn't restrict anything.
     *
     * @return the unrestricted upload restriction
     */
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

        if ((m_cms != null)
            && !OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN) // root admins may have to upload arbitrary files for things like updates / administration stuff
            && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            try {
                // The cache is just for a single value - use the empty string as a dummy key
                CmsUploadRestrictionInfo result = m_cache.get("", () -> computeUploadRestriction(cms));
                return result;
            } catch (ExecutionException e) {
                return new CmsUploadRestrictionInfo.Builder().add(
                    "/",
                    CmsUploadRestrictionInfo.UNRESTRICTED_UPLOADS).build();
            }
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

    /**
     * Calculates the upload restriction data based on the configuration in opencms-workplace.xml and the current sitemap config.
     *
     * @param cms the current CMS context
     * @return the upload restriction data
     */
    private CmsUploadRestrictionInfo computeUploadRestriction(CmsObject cms) {

        CmsUploadRestrictionInfo.Builder builder = new CmsUploadRestrictionInfo.Builder();
        for (Map.Entry<String, String> entry : m_config.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith("/")) {
                builder.add(key, value);
            }
        }
        Map<String, String> extensionsMap = OpenCms.getADEManager().getSitemapAttributeValuesByPath(
            cms,
            ATTR_UPLOAD_EXTENSIONS);
        for (Map.Entry<String, String> entry : extensionsMap.entrySet()) {
            String path = entry.getKey();
            String typesStr = entry.getValue();
            typesStr = typesStr.trim();
            if (!("".equals(typesStr))) {
                Set<String> extensions = Arrays.asList(typesStr.split(",")).stream().map(
                    token -> token.trim().replaceFirst("^\\.", "")).collect(Collectors.toSet());
                builder.add(path, Boolean.TRUE, extensions);
            } else {
                builder.add(path, Boolean.FALSE, new HashSet<>());
            }
        }
        CmsUploadRestrictionInfo result = builder.build();
        return result;
    }

}
