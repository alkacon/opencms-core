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

import java.util.Locale;

/**
 * A cache policy for the shared cache delivery mode.<p>
 *
 * A policy without a content type is the default policy. Other policies match either an exact content type
 * or a top-level content type wildcard such as {@code image/*}.<p>
 */
public class CmsSharedCachePolicy {

    /** Value used for an optional duration which has not been configured. */
    public static final long DURATION_UNSET = -1;

    /** The maximum client cache age in seconds. */
    private long m_clientMaxAge = DURATION_UNSET;

    /** The optional content type selector. */
    private String m_contentType;

    /** The maximum shared cache age in seconds. */
    private long m_sharedMaxAge = DURATION_UNSET;

    /** The optional stale-if-error duration in seconds. */
    private long m_staleIfError = DURATION_UNSET;

    /**
     * Returns the maximum client cache age in seconds.<p>
     *
     * @return the maximum client cache age
     */
    public long getClientMaxAge() {

        return m_clientMaxAge;
    }

    /**
     * Returns the configured content type selector.<p>
     *
     * @return the content type selector, or {@code null} for the default policy
     */
    public String getContentType() {

        return m_contentType;
    }

    /**
     * Returns the maximum shared cache age in seconds.<p>
     *
     * @return the maximum shared cache age
     */
    public long getSharedMaxAge() {

        return m_sharedMaxAge;
    }

    /**
     * Returns the stale-if-error duration in seconds.<p>
     *
     * @return the stale-if-error duration, or {@link #DURATION_UNSET} if it is not configured
     */
    public long getStaleIfError() {

        return m_staleIfError;
    }

    /**
     * Returns whether this is the default policy.<p>
     *
     * @return {@code true} if this is the default policy
     */
    public boolean isDefault() {

        return m_contentType == null;
    }

    /**
     * Sets the maximum client cache age in seconds.<p>
     *
     * @param clientMaxAge the maximum client cache age
     */
    public void setClientMaxAge(String clientMaxAge) {

        m_clientMaxAge = parseDuration(clientMaxAge);
    }

    /**
     * Sets the content type selector.<p>
     *
     * @param contentType the content type selector
     */
    public void setContentType(String contentType) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(contentType)) {
            m_contentType = null;
        } else {
            m_contentType = contentType.trim().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Sets the maximum shared cache age in seconds.<p>
     *
     * @param sharedMaxAge the maximum shared cache age
     */
    public void setSharedMaxAge(String sharedMaxAge) {

        m_sharedMaxAge = parseDuration(sharedMaxAge);
    }

    /**
     * Sets the stale-if-error duration in seconds.<p>
     *
     * @param staleIfError the stale-if-error duration
     */
    public void setStaleIfError(String staleIfError) {

        m_staleIfError = parseDuration(staleIfError);
    }

    /**
     * Parses a non-negative duration in seconds.<p>
     *
     * @param value the value to parse
     * @return the parsed duration
     */
    private long parseDuration(String value) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            return DURATION_UNSET;
        }
        return Long.parseLong(value.trim());
    }
}
