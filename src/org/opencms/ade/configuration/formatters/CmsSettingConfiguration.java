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

import org.opencms.gwt.shared.CmsGwtConstants;
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

    /** The key of the formatter using this configuration (may be null). */
    private String m_formatterKey;

    /** The settings configured in the formatter configuration. */
    private List<CmsXmlContentProperty> m_listedSettings;

    /** A map of all shared setting configurations in the system, with their structure ids as keys. */
    private Map<CmsUUID, Map<CmsSharedSettingKey, CmsXmlContentProperty>> m_sharedSettingConfigsById;

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
     * @param formatterKey the key of the formatter using this setting configuration (may be null)
     * @param displayType the display type
     */
    public CmsSettingConfiguration(
        List<CmsXmlContentProperty> listedSettings,
        Map<CmsUUID, Map<CmsSharedSettingKey, CmsXmlContentProperty>> sharedSettingConfigsById,
        List<CmsUUID> includeIds,
        String formatterKey,
        String displayType) {

        m_listedSettings = listedSettings;
        m_sharedSettingConfigsById = sharedSettingConfigsById;
        m_displayType = displayType;
        m_formatterKey = formatterKey;
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
     * Combines shard setting definitions from multiple shared setting files into a single map.
     *
     * @param ids the structure ids of shared setting files, in order of increasing specificity
     *
     * @return the combined map of shared setting definitions
     */
    private Map<CmsSharedSettingKey, CmsXmlContentProperty> combineSharedSettingDefinitionMaps(List<CmsUUID> ids) {

        Map<CmsSharedSettingKey, CmsXmlContentProperty> result = new HashMap<>();
        for (CmsUUID settingFileId : ids) {
            Map<CmsSharedSettingKey, CmsXmlContentProperty> sharedSettingsForLevel = m_sharedSettingConfigsById.get(
                settingFileId);
            if (sharedSettingsForLevel != null) {
                // since we have different map keys for each (includeName, formatterKey) combination,
                // putAll does the right thing here, i.e. setting definitions are overridden for their individual formatter keys
                result.putAll(sharedSettingsForLevel);
            } else {
                LOG.warn("Shared setting reference not found: " + settingFileId);
            }
        }
        return result;
    }

    /**
     * Helper method to get a shared setting for this formatter.
     *
     *  <p>Prioritizes shared settings with a formatter key matching this formatter's key.
     *
     * @param map the map of shared settings
     * @param includeName the effective include name of the setting definition to find
     *
     * @return the shared setting definition
     */
    private CmsXmlContentProperty getSharedSetting(
        Map<CmsSharedSettingKey, CmsXmlContentProperty> map,
        String includeName) {

        CmsXmlContentProperty result = null;

        // try formatter key specific entry first, if not found try the general entry

        if (m_formatterKey != null) {
            result = map.get(new CmsSharedSettingKey(includeName, m_formatterKey));
        }
        if (result == null) {
            result = map.get(new CmsSharedSettingKey(includeName, null));
        }
        return result;

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

        Map<CmsSharedSettingKey, CmsXmlContentProperty> sharedSettingDefinitions = combineSharedSettingDefinitionMaps(
            m_sharedSettingsIdsFromFormatter);
        Map<CmsSharedSettingKey, CmsXmlContentProperty> overrideSettingDefinitions = combineSharedSettingDefinitionMaps(
            overrideSharedSettingsIds);

        /*
         * For each setting listed in the formatter, try to find a matching setting definition in both the shared settings (referenced from the formatter)
         * and the setting overrides (configured in the sitemap/master configuration). These three setting definition objects (or less, if the setting override or shared
         * setting doesn't exist) are then merged, such that each individual field value for the setting definition is pulled from the first entry in the list
         * [overrideSetting, settingFromFormatter, settingFromSharedSettings] where it is defined. I.e. the field values defined in setting overrides have the highest
         * priority.
         */
        for (CmsXmlContentProperty settingDef : m_listedSettings) {
            String includeName = settingDef.getIncludeName(settingDef.getName());
            if (includeName == null) {
                continue;
            }

            CmsXmlContentProperty defaultSetting = getSharedSetting(sharedSettingDefinitions, includeName);
            CmsXmlContentProperty overrideSetting = getSharedSetting(overrideSettingDefinitions, includeName);
            CmsXmlContentProperty mergedSetting = settingDef;
            if (defaultSetting != null) {
                mergedSetting = mergedSetting.mergeDefaults(defaultSetting);
            }
            if (overrideSetting != null) {
                mergedSetting = overrideSetting.mergeDefaults(mergedSetting);
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
                CmsGwtConstants.HIDDEN_SETTINGS_WIDGET_NAME,
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
