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

package org.opencms.ade.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Helper class for transforming a map of element settings based on the aliases/replacement rules in the setting definitions for a given formatter.
 * <p>
 * The translation method creates a new setting map according to the following rules:
 * <ul>
 * <li>If the setting map contains an entry whose key is the alias of a setting definition, the key is changed to the current setting name.
 * <li>If the setting map contains an entry whose key has the form ${formatterKey}_${settingAlias}, where ${settingAlias} is an alias of a setting with name ${newSetting} for the formatter
 * with key ${formatterKey}, the map key is replaced by ${formatterKey}_${newSetting} (these are nested settings used for list configuration elements)
 * <li>If the setting definition for an entry contains a value translation of the form 'newval_1:oldval_1|newval_2:oldval_2|....', and the value of the entry is one of the oldval_i values in that list,
 * the value will be replaced by the corresponding newval_i value.
 * <li>If none of these are the case, the setting entry will be copied as-is.
 * </ul>
 *
 * <p>
 * A single setting translator instance is used for processing the element settings of a single container page while it is being unmarshalled.
 *
 */
public class CmsSettingTranslator {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSettingTranslator.class);

    /** The active sitemap configuration. */
    private CmsADEConfigData m_config;

    /** A cache containing settings maps for various formatter keys. */
    private HashMap<String, Map<String, CmsXmlContentProperty>> m_settingsCache = new HashMap<>();

    /** A cache map whose keys are textual representations of setting value translations, and the values are the corresponding maps mapping old values to new values.*/
    private Map<String, Map<String, String>> m_translationMapCache = new HashMap<>();

    /**
     * Creates a new instance.
     *
     * @param config the active sitemap configuration
     */
    public CmsSettingTranslator(CmsADEConfigData config) {

        if (config == null) {
            throw new IllegalArgumentException("Sitemap configuration must  not be null.");
        }
        m_config = config;
    }

    /**
     * Parses a setting value translation of the form newval1:oldval1|newval2:oldval2|.... .
     *
     * @param translation the setting value translation
     * @return the setting translation map, with the old values as keys and the corresponding new values as values
     */
    public static Map<String, String> parseSettingTranslationMap(String translation) {

        if (translation == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        String[] parts = translation.split("\\|");
        for (String part : parts) {
            int colonPos = part.indexOf(":");
            if (colonPos != -1) {
                String left = part.substring(0, colonPos);
                left = left.trim();
                String right = part.substring(colonPos + 1);
                right = right.trim();
                result.put(right, left);
            }
        }
        return result;
    }

    /**
     * Translates the settings for the given formatter in the context of the current sitemap.
     *
     * @param formatter the formatter
     * @param settings the settings to translate
     * @return the map of translated settings
     */
    public Map<String, String> translateSettings(I_CmsFormatterBean formatter, Map<String, String> settings) {

        Map<String, String> result = new HashMap<>();
        Map<String, CmsXmlContentProperty> settingDefsWithAliases = getSettingsForFormatter(formatter.getKeyOrId());
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String targetKey = key;
            String targetValue = value;
            int underscorePos = key.indexOf("_");
            CmsXmlContentProperty matchingSetting = settingDefsWithAliases.get(key);
            if (matchingSetting != null) {
                targetKey = matchingSetting.getName();
                targetValue = translateValue(matchingSetting, value);
            } else if (underscorePos != -1) {
                String beforeUnderscore = key.substring(0, underscorePos);
                String afterUnderscore = key.substring(underscorePos + 1);
                CmsXmlContentProperty nestedSetting = getSettingsForFormatter(beforeUnderscore).get(afterUnderscore);
                if (nestedSetting != null) {
                    targetKey = beforeUnderscore + "_" + nestedSetting.getName(); // this only makes a difference if the alias name matched
                    targetValue = translateValue(nestedSetting, value);
                }
            }
            result.put(targetKey, targetValue);
        }
        return result;
    }

    /**
     * Helper method to get the map of settings for a given formatter key, where setting name aliases can also be used as keys.
     *
     * @param formatterKey the key of a formatter
     * @return the map of settings for the formatter key
     */
    protected Map<String, CmsXmlContentProperty> getSettingsForFormatter(String formatterKey) {

        return m_settingsCache.computeIfAbsent(formatterKey, k -> {

            I_CmsFormatterBean formatter = m_config.findFormatter(k, /*noWarn=*/true);
            if (formatter != null) {
                // build map for lookup via name or alias
                Map<String, CmsXmlContentProperty> settings = formatter.getSettings(m_config);
                Map<String, CmsXmlContentProperty> result = new HashMap<>();
                for (CmsXmlContentProperty settingDef : settings.values()) {
                    List<String> mapKeys = new ArrayList<>();
                    mapKeys.add(settingDef.getName());
                    if (settingDef.getAliasName() != null) {
                        for (String item : CmsStringUtil.splitAsList(settingDef.getAliasName(), "|")) {
                            mapKeys.add(item.trim());
                        }
                    }
                    for (String mapKey : mapKeys) {
                        if (mapKey != null) {
                            if (null != result.put(mapKey, settingDef)) {
                                LOG.warn(
                                    "Setting name collision for formatter " + formatterKey + ", setting " + mapKey);
                            }
                        }
                    }
                }
                return result;
            } else {
                return Collections.emptyMap();
            }

        });
    }

    /**
     * Translates a single setting value.
     *
     * @param settingDef the setting whose value should be translated
     * @param value the value to translate
     * @return the translated value
     */
    private String translateValue(CmsXmlContentProperty settingDef, String value) {

        if (settingDef.getTranslationStr() == null) {
            return value;
        }
        Map<String, String> translationMap = m_translationMapCache.computeIfAbsent(
            settingDef.getTranslationStr(),
            translationStr -> parseSettingTranslationMap(translationStr));
        String mappedValue = translationMap.get(value);
        if (mappedValue != null) {
            return mappedValue;
        } else {
            return value;
        }

    }
}
