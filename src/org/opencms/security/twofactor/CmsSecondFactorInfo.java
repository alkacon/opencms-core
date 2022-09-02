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

package org.opencms.security.twofactor;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Second factor information for login.
 */
public class CmsSecondFactorInfo {

    /** The verification code. */
    private String m_code;

    /** The shared secret. */
    private String m_secret;

    /**
     * Creates a new instance.
     *
     * @param code the verification code
     */
    public CmsSecondFactorInfo(String code) {

        m_code = StringUtils.trim(code);
    }

    /**
     * Creates a new instance.
     *
     * @param secret the secret
     * @param code the verification code
     */
    public CmsSecondFactorInfo(String secret, String code) {

        m_secret = StringUtils.trim(secret);
        m_code = StringUtils.trim(code);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsSecondFactorInfo)) {
            return false;
        }
        CmsSecondFactorInfo other = ((CmsSecondFactorInfo)obj);
        if (!Objects.equals(m_code, other.m_code)) {
            return false;
        }
        if (!Objects.equals(m_secret, other.m_secret)) {
            return false;
        }
        return true;

    }

    /**
     * Gets the verification code (normally generated and entered by the user).
     *
     * @return the verification code
     */
    public String getCode() {

        return m_code;
    }

    /**
     * Gets the secret (usually null, only used for second factor setup).
     *
     * @return the secret
     */
    public String getSecret() {

        return m_secret;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(m_code).append(m_secret).hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);

    }

}
