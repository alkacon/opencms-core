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

package org.opencms.workplace.comparison;

import org.opencms.util.CmsStringUtil;

/**
 * Comparison of resource attributes.<p>
 */
public class CmsAttributeComparison {

    /** The name of the property.<p> */
    private String m_name;

    /** The type of the attribute comparison.<p> */
    private String m_status;

    /** The first value of the attribute.<p> */
    private String m_version1;

    /** The second value of the attribute.<p> */
    private String m_version2;

    /**
     * Constructs a new attribute object.<p>
     */
    public CmsAttributeComparison() {

        // empty
    }

    /**
     * Creates a new attribute comparison.<p>
     *
     * @param name the name to set
     * @param version1 the first value of the property
     * @param version2 the second value of the property
     */
    public CmsAttributeComparison(String name, String version1, String version2) {

        m_name = name;
        m_version1 = version1;
        m_version2 = version2;
        boolean v1Empty = CmsStringUtil.isEmptyOrWhitespaceOnly(version1);
        boolean v2Empty = CmsStringUtil.isEmptyOrWhitespaceOnly(version2);
        if (v1Empty && !v2Empty) {
            m_status = CmsResourceComparison.TYPE_ADDED;
        } else if (!v1Empty && v2Empty) {
            m_status = CmsResourceComparison.TYPE_REMOVED;
        } else if ((v1Empty && v2Empty) || version1.equals(version2)) {
            m_status = CmsResourceComparison.TYPE_UNCHANGED;
        } else {
            m_status = CmsResourceComparison.TYPE_CHANGED;
        }
    }

    /**
     * Creates a new attribute comparison.<p>
     *
     * @param name the name to set
     * @param version1 the first value of the property
     * @param version2 the second value of the property
     * @param type the type indicating if the element value has been added, removed, modified or is unchanged
     *
     * @see CmsResourceComparison#TYPE_ADDED
     * @see CmsResourceComparison#TYPE_CHANGED
     * @see CmsResourceComparison#TYPE_REMOVED
     * @see CmsResourceComparison#TYPE_UNCHANGED
     */
    public CmsAttributeComparison(String name, String version1, String version2, String type) {

        m_name = name;
        m_version1 = version1;
        m_version2 = version2;
        m_status = type;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getStatus() {

        return m_status;
    }

    /**
     * Returns the attribute.<p>
     *
     * @return the attribute
     */
    public String getVersion1() {

        return m_version1;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getVersion2() {

        return m_version2;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setStatus(String type) {

        m_status = type;
    }

    /**
     * Sets the version1.<p>
     *
     * @param version1 the version1 to set
     */
    public void setVersion1(String version1) {

        m_version1 = version1;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setVersion2(String type) {

        m_version2 = type;
    }
}
