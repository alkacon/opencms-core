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

package org.opencms.ade.configuration;

import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class for formatter-related functionality.
 */
public class CmsFormatterUtils {

    /**
     * Gets the set of all formatter keys from the settings.
     *
     * @param configData the sitemap configuration
     * @param element the container element
     *
     * @return the set of all formatter keys
     */
    public static Set<String> getAllFormatterKeys(CmsADEConfigData configData, CmsContainerElementBean element) {

        Set<String> result = element.getIndividualSettings().entrySet().stream().filter(
            entry -> entry.getKey().startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)).map(
                entry -> entry.getValue()).collect(Collectors.toSet());
        return result;

    }

    /**
     * Gets the formatter key for the given container name from an element's settings.
     *
     * @param containerName the container name
     * @param element the element from which to get the formatter key
     *
     * @return the formatter key
     */
    public static String getFormatterKey(String containerName, CmsContainerElementBean element) {

        Map<String, String> settings = element.getSettings();
        return getFormatterKey(containerName, settings);
    }

    /**
     * Gets the formatter key for the given container name from an element's settings.
     *
     * @param containerName the container name
     * @param settings the settings from which to get the formatter key
     *
     * @return the formatter key
     */
    public static String getFormatterKey(String containerName, Map<String, String> settings) {

        String key1 = settings.get(CmsFormatterConfig.FORMATTER_SETTINGS_KEY + containerName);
        String key2 = settings.get(CmsFormatterConfig.FORMATTER_SETTINGS_KEY);
        for (String key : new String[] {key1, key2}) {
            if (key != null) {
                return key;
            }
        }
        return null;
    }

    /**
     * Gets the formatter key for the given container name from an element's settings.
     *
     * @param containerName the container name
     * @param settings the settings from which to remove the formatter key
     *
     * @return the formatter key
     */
    public static String removeFormatterKey(String containerName, Map<String, String> settings) {

        String result = null;
        for (String mapKey : Arrays.asList(
            CmsFormatterConfig.FORMATTER_SETTINGS_KEY + containerName,
            CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
            if (settings.get(mapKey) != null) {
                result = settings.remove(mapKey);
                break;
            }
        }
        for (String mapKey : new ArrayList<>(settings.keySet())) {
            if (mapKey.startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                settings.remove(mapKey);
            }
        }
        return result;
    }

}
