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

import org.opencms.configuration.CmsConfigurationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Configuration for the shared cache delivery mode.<p>
 */
public class CmsSharedCacheConfiguration {

    /** Pattern for an exact MIME type or a top-level wildcard. */
    private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(
        "[a-z0-9!#$&^_.+-]+/(?:\\*|[a-z0-9!#$&^_.+-]+)");

    /** If this configuration was explicitly configured. */
    private boolean m_configured;

    /** If shared cache delivery is enabled. */
    private boolean m_enabled;

    /** The configured cache policies. */
    private List<CmsSharedCachePolicy> m_policies = new ArrayList<CmsSharedCachePolicy>();

    /**
     * Adds a cache policy.<p>
     *
     * @param policy the cache policy
     */
    public void addCachePolicy(CmsSharedCachePolicy policy) {

        if (policy != null) {
            m_policies.add(policy);
            m_configured = true;
        }
    }

    /**
     * Returns the configured cache policies in configuration order.<p>
     *
     * @return the configured cache policies
     */
    public List<CmsSharedCachePolicy> getCachePolicies() {

        return Collections.unmodifiableList(m_policies);
    }

    /**
     * Returns the cache policy for a content type.<p>
     *
     * Exact content type policies take precedence over top-level wildcard policies. The default policy is returned
     * if no content type policy matches.<p>
     *
     * @param contentType the content type
     * @return the matching cache policy, or the default policy
     */
    public CmsSharedCachePolicy getCachePolicy(String contentType) {

        String normalizedContentType = normalizeContentType(contentType);
        CmsSharedCachePolicy wildcardPolicy = null;
        if (normalizedContentType != null) {
            int separator = normalizedContentType.indexOf('/');
            String wildcard = separator < 0 ? null : normalizedContentType.substring(0, separator) + "/*";
            for (CmsSharedCachePolicy policy : m_policies) {
                if (normalizedContentType.equals(policy.getContentType())) {
                    return policy;
                }
                if ((wildcard != null) && wildcard.equals(policy.getContentType())) {
                    wildcardPolicy = policy;
                }
            }
        }
        return wildcardPolicy != null ? wildcardPolicy : getDefaultCachePolicy();
    }

    /**
     * Returns the default cache policy.<p>
     *
     * @return the default cache policy, or {@code null} if none is configured
     */
    public CmsSharedCachePolicy getDefaultCachePolicy() {

        for (CmsSharedCachePolicy policy : m_policies) {
            if (policy.isDefault()) {
                return policy;
            }
        }
        return null;
    }

    /**
     * Returns whether shared cache settings have been configured explicitly.<p>
     *
     * @return {@code true} if shared cache settings have been configured
     */
    public boolean isConfigured() {

        return m_configured;
    }

    /**
     * Returns whether shared cache delivery is enabled.<p>
     *
     * @return {@code true} if shared cache delivery is enabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Sets whether shared cache delivery is enabled.<p>
     *
     * @param enabled if shared cache delivery is enabled
     */
    public void setEnabled(String enabled) {

        m_enabled = Boolean.valueOf(enabled).booleanValue();
        m_configured = true;
    }

    /**
     * Validates the shared cache configuration.<p>
     *
     * @throws CmsConfigurationException if the configuration is invalid
     */
    public void validate() throws CmsConfigurationException {

        if (!m_configured) {
            return;
        }
        int defaultPolicyCount = 0;
        Set<String> contentTypes = new HashSet<String>();
        for (CmsSharedCachePolicy policy : m_policies) {
            String policyName = policy.isDefault() ? "default" : policy.getContentType();
            if (policy.isDefault()) {
                defaultPolicyCount++;
            } else {
                if (!CONTENT_TYPE_PATTERN.matcher(policy.getContentType()).matches()) {
                    throw new CmsConfigurationException(
                        org.opencms.configuration.Messages.get().container(
                            org.opencms.configuration.Messages.ERR_SHAREDCACHE_POLICY_CONTENTTYPE_1,
                            policy.getContentType()));
                }
                if (!contentTypes.add(policy.getContentType())) {
                    throw new CmsConfigurationException(
                        org.opencms.configuration.Messages.get().container(
                            org.opencms.configuration.Messages.ERR_SHAREDCACHE_POLICY_DUPLICATE_1,
                            policy.getContentType()));
                }
            }
            validateDuration("clientmaxage", policyName, policy.getClientMaxAge(), false);
            validateDuration("sharedmaxage", policyName, policy.getSharedMaxAge(), false);
            validateDuration("staleiferror", policyName, policy.getStaleIfError(), true);
        }
        if (defaultPolicyCount != 1) {
            throw new CmsConfigurationException(
                org.opencms.configuration.Messages.get().container(
                    org.opencms.configuration.Messages.ERR_SHAREDCACHE_DEFAULT_POLICY_1,
                    Integer.valueOf(defaultPolicyCount)));
        }
    }

    /**
     * Normalizes a response content type for matching.<p>
     *
     * @param contentType the content type
     * @return the normalized content type
     */
    private String normalizeContentType(String contentType) {

        if (contentType == null) {
            return null;
        }
        int parameterSeparator = contentType.indexOf(';');
        String result = parameterSeparator < 0 ? contentType : contentType.substring(0, parameterSeparator);
        result = result.trim().toLowerCase(Locale.ROOT);
        return result.isEmpty() ? null : result;
    }

    /**
     * Validates a configured duration.<p>
     *
     * @param fieldName the XML field name
     * @param policyName the policy name
     * @param value the configured value
     * @param optional whether the duration is optional
     *
     * @throws CmsConfigurationException if the duration is invalid
     */
    private void validateDuration(String fieldName, String policyName, long value, boolean optional)
    throws CmsConfigurationException {

        if ((value < 0) && !(optional && (value == CmsSharedCachePolicy.DURATION_UNSET))) {
            throw new CmsConfigurationException(
                org.opencms.configuration.Messages.get().container(
                    org.opencms.configuration.Messages.ERR_SHAREDCACHE_POLICY_DURATION_2,
                    fieldName,
                    policyName));
        }
    }
}
