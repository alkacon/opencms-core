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

package org.opencms.i18n;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * Loads message bundles from .properties files in the VFS.<p>
 * 
 * The paths of the properties files are formed from the base path in the bundle parameters, the locale, and the .properties suffix.
 */
public class CmsVfsBundleLoaderProperties implements CmsVfsResourceBundle.I_Loader {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsBundleLoaderProperties.class);

    /**
     * @see org.opencms.i18n.CmsVfsResourceBundle.I_Loader#loadData(org.opencms.file.CmsObject, org.opencms.i18n.CmsVfsBundleParameters)
     */
    public Map<Locale, Map<String, String>> loadData(CmsObject cms, CmsVfsBundleParameters params) throws Exception {

        CmsFile file = cms.readFile(params.getBasePath());
        String encoding = getEncoding(cms, file);
        Properties props = new Properties();
        // we do the decoding by ourselves using the encoding set on the resource, so we are not restricted to ISO 8859-1
        props.load(new InputStreamReader(new ByteArrayInputStream(file.getContents()), encoding));
        Map<String, String> messages = Maps.newHashMap();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            messages.put((String)entry.getKey(), (String)entry.getValue());
        }
        Map<Locale, Map<String, String>> result = Maps.newHashMap();
        result.put(params.getLocale(), messages);
        return result;
    }

    /**
     * Gets the encoding which should be used to read the properties file.<p>
     * 
     * @param cms the CMS context to use 
     * @param res the resource for which we want the encoding
     *  
     * @return the encoding value 
     */
    private String getEncoding(CmsObject cms, CmsResource res) {

        String defaultEncoding = OpenCms.getSystemInfo().getDefaultEncoding();
        try {
            CmsProperty encProp = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true);
            String encoding = encProp.getValue(defaultEncoding);
            return encoding;
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return defaultEncoding;
        }
    }

}
