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

package org.opencms.ade.configuration.formatters;

import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

/**
 * Contains the setting-related data for a formatter bean.
 */
public class CmsSettingConfiguration {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSettingConfiguration.class);

    /** Cache for calculating and storing the setting definition maps for various combinations of override shared setting configuration file ids. */
    private LoadingCache<ImmutableList<CmsUUID>, Map<String, CmsXmlContentProperty>> m_cache = CacheBuilder.newBuilder().concurrencyLevel(
        4).build(new CacheLoader<ImmutableList<CmsUUID>, Map<String, CmsXmlContentProperty>>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public Map<String, CmsXmlContentProperty> load(ImmutableList<CmsUUID> sharedSettingOverrides)
            throws Exception {

                return resolveSettings(sharedSettingOverrides);

            }

        });

    /** The display type. */
    private String m_displayType;

    /** The settings configured in the formatter configuration. */
    private List<CmsXmlContentProperty> m_listedSettings;

    /** A map of all shared setting configurations in the system, with their structure ids as keys. */
    private Map<CmsUUID, List<CmsXmlContentProperty>> m_sharedSettingConfigsById;

    /** The list of structure ids of shared settings files configured in the formatter. The last entry has the highest priority. */
    private List<CmsUUID> m_sharedSettingsIdsFromFormatter;

    /**
     * Creates an empty configuration.
     */
    public CmsSettingConfiguration() {

        m_listedSettings = new ArrayList<>();
        m_sharedSettingConfigsById = new HashMap<>();
        m_displayType = null;
        m_sharedSettingsIdsFromFormatter = new ArrayList<>();
    }

    /**
     *
     * @param listedSettings  the setting entries configured in the formatter configuration
     * @param sharedSettingConfigsById the map of shared setting configurations, with their structure ids as keys
     * @param includeIds the list of structure ids of shared setting configurations referenced from the formatter configuration
     * @param displayType the display type
     */
    public CmsSettingConfiguration(
        List<CmsXmlContentProperty> listedSettings,
        Map<CmsUUID, List<CmsXmlContentProperty>> sharedSettingConfigsById,
        List<CmsUUID> includeIds,
        String displayType) {

        m_listedSettings = listedSettings;
        m_sharedSettingConfigsById = sharedSettingConfigsById;
        m_displayType = displayType;
        m_sharedSettingsIdsFromFormatter = new ArrayList<>(includeIds);
    }

    /**
     * Gets the setting map by looking up the configured settings' include names in either the shared settings files
     * configured in the formatter configuration, or the override shared settings files whose ids are passed as parameter.
     *
     *  <p>
     *  Setting definitions from Override shared settings files have higher priority than those referenced in the formatter
     *  configuration, and later entries in both lists have higher prioritiy than earlier ones.
     *
     * @param sharedSettingOverrides the structure ids of Override shared setting configurations (highest priority last)
     *
     * @return the setting definition map
     */
    public Map<String, CmsXmlContentProperty> getSettings(ImmutableList<CmsUUID> sharedSettingOverrides) {

        try {
            return m_cache.get(sharedSettingOverrides);
        } catch (ExecutionException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Computes the finished map of settings for the given combination of shared setting overrides.+
     *
     * @param overrideSharedSettingsIds the structure ids of shared setting overrides active in the current context, with the most specific override last
     *
     * @return the finished map of settings
     */
    private Map<String, CmsXmlContentProperty> resolveSettings(ImmutableList<CmsUUID> overrideSharedSettingsIds) {

        Map<String, CmsXmlContentProperty> result = new LinkedHashMap<>();
        Map<String, CmsXmlContentProperty> sharedSettingsByIncludeNames = new HashMap<>();
        List<CmsUUID> allSharedSettingsIds = new ArrayList<>();

        // Shared setting configurations from the formatter are processed first, then shared setting overrides from the sitemap configuration.
        // This means the latter are prioritized, since the entries from them are put into sharedSettingsByIncludeName last, and thus replace
        // existing entries.

        allSharedSettingsIds.addAll(m_sharedSettingsIdsFromFormatter);
        allSharedSettingsIds.addAll(overrideSharedSettingsIds);

        // first merge all shared setting definitions into a single map, with include names as keys

        for (CmsUUID includeOverride : allSharedSettingsIds) {
            List<CmsXmlContentProperty> settingDefs = m_sharedSettingConfigsById.get(includeOverride);
            if (settingDefs != null) {
                for (CmsXmlContentProperty settingDef : settingDefs) {
                    String includeName = settingDef.getIncludeName(settingDef.getName());
                    sharedSettingsByIncludeNames.put(includeName, settingDef);
                }
            } else {
                LOG.warn("Shared setting reference not found: " + includeOverride);
            }
        }

        // then build the actual setting map, while using the combined shared setting definition from above to look
        // up IncludeNames.

        for (CmsXmlContentProperty settingDef : m_listedSettings) {
            String includeName = settingDef.getIncludeName(settingDef.getName());
            if (includeName == null) {
                continue;
            }
            CmsXmlContentProperty defaultSetting = sharedSettingsByIncludeNames.get(includeName);
            CmsXmlContentProperty mergedSetting;
            if (defaultSetting != null) {
                mergedSetting = settingDef.mergeDefaults(defaultSetting);
            } else {
                mergedSetting = settingDef;
            }
            if (mergedSetting.getName() == null) {
                continue;
            }
            result.put(mergedSetting.getName(), mergedSetting);
        }
        if ((m_displayType != null) && !result.containsKey(CmsFormatterBeanParser.SETTING_DISPLAY_TYPE)) {
            CmsXmlContentProperty displayType = new CmsXmlContentProperty(
                CmsFormatterBeanParser.SETTING_DISPLAY_TYPE,
                "string",
                "hidden",
                null,
                null,
                null,
                m_displayType,
                null,
                null,
                null,
                null);
            result.put(displayType.getName(), displayType);
        }
        return Collections.unmodifiableMap(result);
    }
}
