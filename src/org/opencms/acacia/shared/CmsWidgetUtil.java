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

package org.opencms.acacia.shared;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Helper with methods commonly used in editor widgets. */
public class CmsWidgetUtil {

    /**
     * Generates a pipe separated option string from the map,
     * e.g., for {key1 -> value1, key2 -> null, key3 -> value3} the string  "key1=value1|key2|key3=value3" will be generated.
     * @param options the map with the config options.
     * @return the options as pipe separated string.
     */
    public static String generatePipeSeparatedConfigString(Map<String, String> options) {

        StringBuffer result = new StringBuffer();
        for (Entry<String, String> option : options.entrySet()) {
            result.append('|');
            result.append(option.getKey());
            if (option.getValue() != null) {
                result.append('=').append(option.getValue());
            }
        }
        return result.length() > 0 ? result.substring(1) : "";
    }

    /**
     * Returns a flag, indicating if a boolean option is set, i.e., it is in the map and has value null or (case-insensitive) "true".
     * @param configOptions the map with the config options.
     * @param optionKey the boolean option to check.
     * @return a flag, indicating if a boolean option is set
     */
    public static boolean getBooleanOption(Map<String, String> configOptions, String optionKey) {

        if (configOptions.containsKey(optionKey)) {
            String value = configOptions.get(optionKey);
            if ((value == null) || Boolean.valueOf(value).booleanValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value of an option, or the default if the value is null or the key is not part of the map.
     * @param configOptions the map with the config options.
     * @param optionKey the option to get the value of
     * @param defaultValue the default value to return if the option is not set.
     * @return the value of an option, or the default if the value is null or the key is not part of the map.
     */
    public static String getStringOption(Map<String, String> configOptions, String optionKey, String defaultValue) {

        String result = configOptions.get(optionKey);
        return null != result ? result : defaultValue;
    }

    /**
     * Parses a pipe-separated config string into a map, <i>converting all keys to lowercase</i>.
     * E.g., for "Key1=Value1|KEY2|key3=value3" the map {key1 -> Value1, key2 -> null, key3 -> value3} is returned
     * @param configString the config string to parse
     * @return the config options from the string as map
     */
    public static Map<String, String> parsePipeSeparatedConfigString(String configString) {

        Map<String, String> result = new HashMap<>();
        if (null != configString) {
            List<String> options = Arrays.asList(configString.split("\\|"));
            for (String option : options) {
                String optKey;
                String optValue;
                int firstEquals = option.indexOf("=");
                if (firstEquals >= 0) {
                    optKey = option.substring(0, firstEquals);
                    optValue = option.substring(firstEquals + 1);
                } else {
                    optKey = option.toLowerCase();
                    optValue = null;
                }
                if (optKey.length() > 0) {
                    result.put(optKey, optValue);
                }
            }
        }
        return result;
    }

}
