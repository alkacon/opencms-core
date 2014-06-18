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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsVfsBundleParameters;
import org.opencms.i18n.CmsVfsResourceBundle;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Loads message bundles from the different locales of a single XML content.<p>
 */
public class CmsVfsBundleLoaderXml implements CmsVfsResourceBundle.I_Loader {

    /** Node name. */
    public static final String N_KEY = "Key";

    /** Node name. */
    public static final String N_MESSAGE = "Message";

    /** Node name. */
    public static final String N_VALUE = "Value";

    /**
     * @see org.opencms.i18n.CmsVfsResourceBundle.I_Loader#loadData(org.opencms.file.CmsObject, org.opencms.i18n.CmsVfsBundleParameters)
     */
    public Map<Locale, Map<String, String>> loadData(CmsObject cms, CmsVfsBundleParameters params) throws Exception {

        CmsFile file = cms.readFile(params.getBasePath());
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
        Map<Locale, Map<String, String>> result = Maps.newHashMap();
        for (Locale locale : content.getLocales()) {
            List<I_CmsXmlContentValue> messages = content.getValues(N_MESSAGE, locale);
            Map<String, String> currentLocale = new HashMap<String, String>();
            for (I_CmsXmlContentValue messageValue : messages) {
                String path = messageValue.getPath();
                I_CmsXmlContentValue keyValue = content.getValue(CmsXmlUtils.concatXpath(path, N_KEY), locale);
                String keyStr = keyValue.getStringValue(cms);
                // Ignore leading/trailing spaces in the key to protect from user error  
                keyStr = keyStr.trim();
                I_CmsXmlContentValue valueValue = content.getValue(CmsXmlUtils.concatXpath(path, N_VALUE), locale);
                String valueStr = valueValue.getStringValue(cms);
                currentLocale.put(keyStr, valueStr);
            }
            result.put(locale, currentLocale);
        }
        return result;
    }
}
