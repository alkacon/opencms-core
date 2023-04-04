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

package org.opencms.ade.sitemap;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Performs attribute changes in a sitemap configuration file CmsXmlContent instance.
 */
public class CmsSitemapAttributeUpdater {

    /** The CmsObject to use. */
    private CmsObject m_cms;

    /** The XML content object containing the sitemap configuration. */
    private CmsXmlContent m_sitemapConfig;

    /**
     * Creates a new instance.
     *
     * @param cms the CmsObject to use
     * @param sitemapConfig the sitemap configuration XML content
     */
    public CmsSitemapAttributeUpdater(CmsObject cms, CmsXmlContent sitemapConfig) {

        m_cms = cms;
        m_sitemapConfig = sitemapConfig;
    }

    /**
     * Given a map of attribute values (usually from the attribute editor), this method computes the necessary updates to perform.
     *
     * <p>Attribute values that match the inherited attribute from a parent sitemap configuration should cause the attribute to be removed.
     *
     * @param newAttributes the map of attributes
     * @param config the sitemap config we are currently working on
     *
     * @return the map of updates (with null values indicating that the key should be removed)
     */
    public static Map<String, String> computeUpdatesRelativeToInheritedValues(
        Map<String, String> newAttributes,
        CmsADEConfigData config) {

        Map<String, String> updates = new HashMap<>();
        CmsADEConfigData parentConfig = config.parent();
        for (Map.Entry<String, String> entry : newAttributes.entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();
            String updateValue = attrValue;
            if (parentConfig != null) {
                String parentValue = parentConfig.getAttribute(attrName, null);
                if ((parentValue != null) && parentValue.equals(attrValue)) {
                    updateValue = null;
                }
            }
            if ("".equals(attrValue)) {
                updateValue = null;
            }
            updates.put(attrName, updateValue);
        }
        return updates;
    }

    /**
     * Gets the sitemap attributes that are currently stored in the XML content.
     *
     * @return the map of sitemap attributes
     */
    public Map<String, String> getAttributesFromContent() {

        Map<String, String> allValues = new LinkedHashMap<>();
        List<I_CmsXmlContentValue> attributeValues = m_sitemapConfig.getValues(
            CmsConfigurationReader.N_ATTRIBUTE,
            Locale.ENGLISH);
        for (I_CmsXmlContentValue attr : attributeValues) {
            I_CmsXmlContentValue keyValue = m_sitemapConfig.getValue(
                CmsXmlUtils.concatXpath(attr.getPath(), CmsConfigurationReader.N_KEY),
                Locale.ENGLISH);
            I_CmsXmlContentValue valueValue = m_sitemapConfig.getValue(
                CmsXmlUtils.concatXpath(attr.getPath(), CmsConfigurationReader.N_VALUE),
                Locale.ENGLISH);
            allValues.put(keyValue.getStringValue(m_cms), valueValue.getStringValue(m_cms));
        }
        return allValues;
    }

    /**
     * Gets the XML content which this object operates on.
     * @return the XML content
     */
    public CmsXmlContent getContent() {

        return m_sitemapConfig;
    }

    /**
     * Completely replaces the sitemap attributes in the XML content with the entries from the map passed as a parameter.
     *
     * @param allValues the new sitemap attributes that should replace the existing ones
     */
    public void replaceAttributes(Map<String, String> allValues) {

        while (m_sitemapConfig.hasValue(CmsConfigurationReader.N_ATTRIBUTE, Locale.ENGLISH)) {
            m_sitemapConfig.removeValue(CmsConfigurationReader.N_ATTRIBUTE, Locale.ENGLISH, 0);
        }
        CmsObject cms = m_cms;
        for (Map.Entry<String, String> entry : allValues.entrySet()) {
            I_CmsXmlContentValue newAttrValue = m_sitemapConfig.addValue(
                cms,
                CmsConfigurationReader.N_ATTRIBUTE,
                Locale.ENGLISH,
                0);
            I_CmsXmlContentValue newKeyValue = m_sitemapConfig.getValue(
                CmsXmlUtils.concatXpath(newAttrValue.getPath(), CmsConfigurationReader.N_KEY),
                Locale.ENGLISH);
            I_CmsXmlContentValue newValueValue = m_sitemapConfig.getValue(
                CmsXmlUtils.concatXpath(newAttrValue.getPath(), CmsConfigurationReader.N_VALUE),
                Locale.ENGLISH);
            newKeyValue.setStringValue(cms, entry.getKey());
            newValueValue.setStringValue(cms, entry.getValue());
        }
    }

    /**
     * Saves the attributes coming from the editor dialog.
     *
     * <p>This method replaces values that match values inherited from a parent configuration with null (not set).
     *
     * @param attributes the attributes to save
     * @return true if any changes were made
     */
    public boolean saveAttributesFromEditorDialog(Map<String, String> attributes) {

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
            m_cms,
            m_sitemapConfig.getFile().getRootPath());
        Map<String, String> updates = computeUpdatesRelativeToInheritedValues(attributes, config);
        return updateAttributes(updates);
    }

    /**
     * Performs a set of attribute changes.
     *
     * <p>The map given as an argument may contain null values. In that case, the corresponding attribute
     * is removed from the XML content.
     *
     * @param attributeUpdates a map of attributes
     * @return true if any attribute changes were necessary in the XML content
     */
    public boolean updateAttributes(Map<String, String> attributeUpdates) {

        Map<String, String> oldValues = getAttributesFromContent();
        Map<String, String> newValues = new LinkedHashMap<>(oldValues);
        for (Map.Entry<String, String> entry : attributeUpdates.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null) {
                newValues.put(key, value);
            } else {
                newValues.remove(key);
            }
        }
        boolean changed = !oldValues.equals(newValues);
        if (changed) {
            replaceAttributes(newValues);
        }
        return changed;

    }

}
