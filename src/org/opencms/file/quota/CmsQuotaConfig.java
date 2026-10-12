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

package org.opencms.file.quota;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

/**
 * Shared sitemap attribute names and value handling for storage budgets.<p>
 *
 * Every website root forms an independent budget scope. A subsite forms a separate budget scope only
 * if its local {@link #ATTR_UPLOAD_QUOTA_SCOPE} attribute is true. Otherwise, its usage is charged to
 * the nearest enclosing budget scope. Local attributes include the subsite's master configurations
 * and connector values, but not attributes inherited from parent websites or subsites.<p>
 *
 * Individual budgets and defaults do not create budget scopes. Each scope's usage includes ordinary
 * subsites but excludes subordinate budget scopes.<p>
 */
public final class CmsQuotaConfig {

    /**
     * The individual budget for the current budget scope, overriding any applicable default.
     * This value is not inherited and has no effect on a subsite without its own budget scope.
     */
    public static final String ATTR_UPLOAD_QUOTA = "upload.quota";

    /**
     * The default for budget scopes at or below the website or subsite defining this attribute.
     * The nearest default applies unless the budget scope has an individual budget. An ordinary
     * subsite may supply a default for subordinate budget scopes without forming a scope itself.
     */
    public static final String ATTR_UPLOAD_QUOTA_DEFAULT = "upload.quota.default";

    /**
     * Marks a subsite as an independent budget scope when true; this marker is not inherited.
     * Website roots always form budget scopes, independently of this attribute.
     */
    public static final String ATTR_UPLOAD_QUOTA_SCOPE = "upload.quota.scope";

    /** The name of the central VFS configuration file. */
    public static final String CONFIG_FILE_NAME = "quotas.properties";

    /** The property for the system-wide default in the central configuration file. */
    public static final String PROPERTY_SYSTEM_DEFAULT = "upload.quota.systemdefault";

    /** An explicitly unlimited budget. */
    public static final String UNLIMITED = "unlimited";

    /**
     * Hidden constructor.<p>
     */
    private CmsQuotaConfig() {

        // Utility class.
    }

    /**
     * Validates and normalizes a configured budget.<p>
     *
     * @param value null to inherit, "unlimited", or a positive GB value with at most one decimal place
     *
     * @return the normalized value
     * @throws IllegalArgumentException if the value is invalid
     */
    public static String normalize(String value) {

        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (UNLIMITED.equals(trimmed)) {
            return UNLIMITED;
        }
        if (trimmed.matches("[0-9]+([.,][0-9])?")) {
            BigDecimal amount = new BigDecimal(trimmed.replace(',', '.'));
            if ((amount.signum() > 0) && (amount.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0)) {
                return amount.stripTrailingZeros().toPlainString();
            }
        }
        throw new IllegalArgumentException("Expected a positive GB quota with at most one decimal place: " + value);
    }

    /**
     * Converts a configured budget to bytes, retaining invalid settings as unavailable.<p>
     *
     * @param value the configured value, or null to inherit
     *
     * @return bytes, null to inherit, zero for unlimited, or a negative value if invalid
     */
    public static Long parse(String value) {

        try {
            String normalized = normalize(value);
            if (normalized == null) {
                return null;
            }
            if (UNLIMITED.equals(normalized)) {
                return CmsQuotaDefinition.UNLIMITED;
            }
            return Long.valueOf(
                new BigDecimal(normalized).multiply(
                    BigDecimal.valueOf(CmsQuotaDefinition.BYTES_PER_GB)).longValueExact());
        } catch (IllegalArgumentException e) {
            CmsLog.getLog(CmsQuotaConfig.class).warn("Invalid storage quota: " + value, e);
            return CmsQuotaDefinition.UNAVAILABLE;
        }
    }

    /**
     * Reads the system-wide default from a properties file.<p>
     *
     * @param contents the UTF-8 properties file contents
     *
     * @return bytes, null if not configured, zero for unlimited, or a negative value if invalid
     */
    public static Long parseSystemDefault(byte[] contents) {

        try {
            Properties properties = new Properties();
            properties.load(new StringReader(new String(contents, StandardCharsets.UTF_8)));
            String value = properties.getProperty(PROPERTY_SYSTEM_DEFAULT);
            return (value == null) || value.trim().isEmpty() ? null : parse(value);
        } catch (IOException | IllegalArgumentException e) {
            CmsLog.getLog(CmsQuotaConfig.class).warn("Invalid quota properties", e);
            return CmsQuotaDefinition.UNAVAILABLE;
        }
    }

    /**
     * Reads the central budget setting, preferring the VFS configuration override if present.<p>
     *
     * The selected file replaces the base file completely. Cached values are invalidated by VFS changes.<p>
     *
     * @param cms an administrative CMS context with an empty site root in the project to read
     *
     * @return bytes, null if not configured, zero for unlimited, or a negative value if unavailable
     */
    @SuppressWarnings("unchecked")
    public static Long readSystemDefault(CmsObject cms) {

        String path = OpenCms.getSystemInfo().getConfigFilePath(cms, CONFIG_FILE_NAME);
        CmsVfsMemoryObjectCache cache = OpenCms.getVfsMemoryObjectCache();
        Optional<Long> cached = (Optional<Long>)cache.getCachedObject(cms, path);
        if (cached != null) {
            return cached.orElse(null);
        }
        try {
            Long value = parseSystemDefault(cms.readFile(path).getContents());
            cache.putCachedObject(cms, path, Optional.ofNullable(value));
            return value;
        } catch (CmsVfsResourceNotFoundException e) {
            return null;
        } catch (CmsException e) {
            CmsLog.getLog(CmsQuotaConfig.class).warn("Could not read quota configuration " + path, e);
            return CmsQuotaDefinition.UNAVAILABLE;
        }
    }
}
