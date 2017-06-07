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

package org.opencms.loader;

import org.opencms.i18n.I_CmsMessageContainer;
import org.opencms.json.JSONObject;

import java.util.Locale;

/**
 * Simple JSON-based message container.<p>
 *
 * This message container will accept an Object which may either be a simple string (in which case
 * this string will always be returned as the message), or a JSONObject which has locale strings as
 * keys and the corresponding message strings as values.
 */
public class CmsJsonMessageContainer implements I_CmsMessageContainer {

    /** The JSON value for this message container. */
    private Object m_jsonValue;

    /**
     * Creates a new instance.<p>
     *
     * @param jsonValue the JSON value containing the message(s).<p>
     */
    public CmsJsonMessageContainer(Object jsonValue) {

        m_jsonValue = jsonValue;

    }

    /**
     * @see org.opencms.i18n.I_CmsMessageContainer#key(java.util.Locale)
     */
    public String key(Locale locale) {

        if (m_jsonValue instanceof String) {
            return (String)m_jsonValue;
        } else if (m_jsonValue instanceof JSONObject) {
            JSONObject localeMap = (JSONObject)m_jsonValue;
            for (String key : new String[] {
                locale.toString(),
                "en",
                localeMap.keySet().size() > 0 ? (localeMap.keySet().iterator().next()) : ""}) {
                if (localeMap.has(key)) {
                    return "" + localeMap.optString(key);
                }
            }
        }
        return "??? not found ???";
    }
}
