/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.storage.policy;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.util.CmsStringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of the storage policy logic for binary blobs.<p>
 *
 * This class evaluates whether a file should be stored directly in the content
 * tables (CMS_CONTENTS, CMS_OFFLINE_CONTENTS) or offloaded to a deduplicated
 * storage. It uses a rule-based engine to check the configured resource type ids
 * and file size.<p>
 *
 * High-level evaluation order:
 * <ol>
 * <li><b>Whitelist:</b> If a file matches a whitelist rule (e.g., system paths), it is always stored locally.</li>
 * <li><b>Storage Rules:</b> If any storage rule matches (e.g., configured resource type ids above the configured threshold), it is offloaded.</li>
 * <li><b>Fallback:</b> If no rules match, the file is stored locally by default.</li>
 * </ol>
 */
public class CmsDefaultStoragePolicy implements I_CmsStoragePolicy, I_CmsConfigurationParameterHandler {

    /** The resource type ids parameter name. */
    public static final String PARAM_RESOURCE_TYPE_IDS = "resourceTypeIds";

    /** The threshold parameter name. */
    public static final String PARAM_THRESHOLD = "threshold";

    /** The default threshold, in bytes. The default value stores all non-empty binary and image files externally. */
    public static final int DEFAULT_THRESHOLD = 0;

    /** The configuration. */
    private final CmsParameterConfiguration m_configuration;

    /** The policy engine. */
    private final CmsStoragePolicyEngine m_engine;

    /** The configured threshold. */
    private int m_threshold = DEFAULT_THRESHOLD;

    /** The configured resource type ids. */
    private Set<Integer> m_resourceTypeIds = createDefaultResourceTypeIds();

    /**
     * Creates a new policy with predefined default rules.
     */
    public CmsDefaultStoragePolicy() {

        m_configuration = new CmsParameterConfiguration();
        m_engine = new CmsStoragePolicyEngine();
        setupDefaults();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        m_threshold = m_configuration.getInteger(PARAM_THRESHOLD, DEFAULT_THRESHOLD);
        m_resourceTypeIds = parseResourceTypeIds(
            m_configuration.getString(PARAM_RESOURCE_TYPE_IDS, null),
            createDefaultResourceTypeIds());
    }

    /**
     * @see org.opencms.db.storage.policy.I_CmsStoragePolicy#isExternalStorageRequired(CmsStoragePolicyContext)
     */
    @Override
    public boolean isExternalStorageRequired(CmsStoragePolicyContext context) {

        return m_engine.shouldStoreInStorage(context);
    }

    /**
     * Creates the default resource type id set.<p>
     *
     * @return the default resource type id set
     */
    private Set<Integer> createDefaultResourceTypeIds() {

        Set<Integer> result = new HashSet<Integer>();
        result.add(Integer.valueOf(CmsResourceTypeBinary.getStaticTypeId()));
        result.add(Integer.valueOf(CmsResourceTypeImage.getStaticTypeId()));
        return result;
    }

    /**
     * Parses a comma separated list of resource type ids.<p>
     *
     * @param value the configured value
     * @param defaultValue the default value
     *
     * @return the parsed resource type ids
     */
    private Set<Integer> parseResourceTypeIds(String value, Set<Integer> defaultValue) {

        if (value == null) {
            return defaultValue;
        }
        Set<Integer> result = new HashSet<Integer>();
        for (String typeId : CmsStringUtil.splitAsList(value, ',', true)) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(typeId)) {
                result.add(Integer.valueOf(typeId));
            }
        }
        return result;
    }

    /**
     * Initializes the engine with the default set of rules.
     */
    private void setupDefaults() {

        m_engine.addStorageRule(
            new CmsStoragePolicyEngine.AndRule(
                context -> Boolean.valueOf(
                    (context.getContent() != null) && (context.getContent().length > m_threshold)),
                context -> {
                    if (context.getResource() == null) {
                        return null;
                    }
                    int typeId = context.getResource().getTypeId();
                    return Boolean.valueOf(m_resourceTypeIds.contains(Integer.valueOf(typeId)));
                }));
    }
}
