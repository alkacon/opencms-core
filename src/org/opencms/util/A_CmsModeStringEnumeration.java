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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import java.io.Serializable;

/**
 * Base class for all string mode enumeration classes.<p>
 *
 * Like:<br>
 * <ul>
 *   <li>{@link org.opencms.db.CmsUserSettings.CmsSearchResultStyle}
 * </ul>
 *
 * @since 6.5.5
 */
public abstract class A_CmsModeStringEnumeration implements Serializable {

    /** Serialization id. */
    private static final long serialVersionUID = 6884841215348447781L;

    /** The internal mode descriptor. */
    private final String m_mode;

    /**
     * Default constructor.<p>
     *
     * @param mode the internal mode descriptor
     */
    protected A_CmsModeStringEnumeration(String mode) {

        m_mode = mode;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof A_CmsModeStringEnumeration) {
            if (obj.getClass().equals(this.getClass())) {
                A_CmsModeStringEnumeration eObj = (A_CmsModeStringEnumeration)obj;
                return eObj.getMode().equals(m_mode);
            }
        }
        return false;
    }

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public String getMode() {

        return m_mode;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_mode.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_mode;
    }
}