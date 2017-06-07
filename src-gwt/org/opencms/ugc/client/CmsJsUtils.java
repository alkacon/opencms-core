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

package org.opencms.ugc.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Various utility functions.<p>
 */
public class CmsJsUtils {

    /** The random number generator. */
    public static final Random RANDOM = new Random();

    /**
     * Converts a Javascript object to a map from strings to strings, ignoring all properties
     * of the object whose value is not a string.<p>
     *
     * @param jso the Javascript object to convert
     * @return the map containing the string-valued properties of the Javascript object
     */
    public static Map<String, String> convertJsObjectToMap(JavaScriptObject jso) {

        Map<String, String> result = new HashMap<String, String>();
        JSONObject json = new JSONObject(jso);
        for (String key : json.keySet()) {
            if ((json.get(key) != null) && (json.get(key).isString() != null)) {
                result.put(key, json.get(key).isString().stringValue());
            } else {
                result.put(key, null);
            }
        }
        return result;
    }

    /**
     * Converts a map whose keys and values are strings to a Javascript object with the keys as attributes and the
     * corresponding values as the attribute values.<p>
     *
     * @param stringMap the map to convert
     * @return the Javascript object with the attributes defined by the map
     */
    public static JavaScriptObject convertMapToJsObject(Map<String, String> stringMap) {

        if (stringMap == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            json.put(key, new JSONString(value));
        }
        return json.getJavaScriptObject();
    }

    /**
     * Generates a random id.<p>
     *
     * @return the random id
     */
    public static String generateRandomId() {

        StringBuffer buffer = new StringBuffer();
        int base = 36;
        for (int i = 0; i < 16; i++) {
            buffer.append(Integer.toString(RANDOM.nextInt(base), base));
        }
        return buffer.toString();
    }

}
