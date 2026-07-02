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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Configuration for direct delivery of externally stored VFS content.<p>
 */
public class CmsStoredContentDeliveryConfiguration {

    /** If direct stored content delivery is enabled. */
    private boolean m_enabled;

    /** Suffixes for which direct stored content delivery is enabled. */
    private Set<String> m_enabledSuffixes = new LinkedHashSet<String>();

    /** If this configuration was explicitly configured. */
    private boolean m_configured;

    /**
     * Adds a suffix for direct stored content delivery.<p>
     *
     * @param suffix the suffix
     */
    public void addEnabledSuffix(String suffix) {

        String normalizedSuffix = normalizeSuffix(suffix);
        if (normalizedSuffix != null) {
            m_enabledSuffixes.add(normalizedSuffix);
            m_configured = true;
        }
    }

    /**
     * Returns the enabled suffixes.<p>
     *
     * @return the enabled suffixes
     */
    public Set<String> getEnabledSuffixes() {

        return Collections.unmodifiableSet(m_enabledSuffixes);
    }

    /**
     * Returns if suffixes for direct stored content delivery have been configured.<p>
     *
     * @return <code>true</code> if suffixes have been configured
     */
    public boolean hasEnabledSuffixes() {

        return !m_enabledSuffixes.isEmpty();
    }

    /**
     * Returns if stored content delivery settings have been configured explicitly.<p>
     *
     * @return <code>true</code> if stored content delivery settings have been configured
     */
    public boolean isConfigured() {

        return m_configured;
    }

    /**
     * Returns if direct stored content delivery is enabled.<p>
     *
     * @return <code>true</code> if direct stored content delivery is enabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Returns if the suffix of the given resource name is enabled for direct stored content delivery.<p>
     *
     * @param resourceName the resource name
     * @return <code>true</code> if the suffix is enabled
     */
    public boolean isSuffixEnabled(String resourceName) {

        if (!hasEnabledSuffixes()) {
            return true;
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(resourceName)) {
            return false;
        }
        String normalizedName = resourceName.toLowerCase(Locale.ROOT);
        for (String suffix : m_enabledSuffixes) {
            if (normalizedName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets if direct stored content delivery is enabled.<p>
     *
     * @param enabled if direct stored content delivery is enabled
     */
    public void setEnabled(String enabled) {

        m_enabled = Boolean.valueOf(enabled).booleanValue();
        m_configured = true;
    }

    /**
     * Normalizes a suffix.<p>
     *
     * @param suffix the suffix
     * @return the normalized suffix
     */
    private String normalizeSuffix(String suffix) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(suffix)) {
            return null;
        }
        String result = suffix.trim().toLowerCase(Locale.ROOT);
        return result.startsWith(".") ? result : "." + result;
    }
}
