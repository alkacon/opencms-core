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

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A pair consisting of the include name of a setting definition and the formatter key (possibly null) for which the setting should be used,
 * for use as a map key in a map of setting definitions.
 *
 * <p>
 * Shared settings can either be defined generally, or just for a specific list of formatter keys in a setting definition file. In the first case,
 * this corresponds to a single map key with the formatter key set to null, in the second case to a set of map keys with the same include name but
 * different formatter keys pointing to the same setting definition data.
 */
public class CmsSharedSettingKey {

    /** The formatter key (may be null). */
    private String m_formatterKey;

    /** The stored hash code. */
    private int m_hashCode;

    /** The include name of a setting definition. */
    private String m_name;

    /**
     * Creates a new instance.
     *
     * @param name the include name of the setting definition
     *
     * @param formatterKey the formatter key (may be null)
     */
    public CmsSharedSettingKey(String name, String formatterKey) {

        m_name = name;
        m_formatterKey = formatterKey;
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        m_hashCode = hashCodeBuilder.append(name).append(formatterKey).toHashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsSharedSettingKey)) {
            return false;
        }
        CmsSharedSettingKey other = (CmsSharedSettingKey)obj;
        return Objects.equals(this.m_name, other.m_name) && Objects.equals(this.m_formatterKey, other.m_formatterKey);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_hashCode;

    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "(" + m_name + "," + m_formatterKey + ")";

    }

}
