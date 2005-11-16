/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsAttributeComparison.java,v $
 * Date   : $Date: 2005/11/16 12:12:55 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
 * Comparison of resource properties.<p>
 * 
 * @author Jan Baudisch
 */
public class CmsAttributeComparison {

    /** The name of the property.<p> */
    private String m_name;

    /** The type of the property comparison.<p> */
    private String m_type;

    /** The first value of the property.<p> */
    private String m_version1;

    /** The second value of the property.<p> */
    private String m_version2;

    /** 
     * Constructs a new attribute object.<p>
     */
    public CmsAttributeComparison() {

        // no-op   
    }

    /** 
     * Creates a new attribute comparison.<p> 
     * 
     * @param name the name to set
     * @param version1 the first value of the property
     * @param version2 the seconf value of the property
     */
    public CmsAttributeComparison(String name, String version1, String version2) {

        m_name = name;
        m_version1 = version1;
        m_version2 = version2;
        boolean v1Empty = CmsStringUtil.isEmptyOrWhitespaceOnly(version1);
        boolean v2Empty = CmsStringUtil.isEmptyOrWhitespaceOnly(version2);
        if (v1Empty && !v2Empty) {
            m_type = CmsResourceComparison.TYPE_ADDED;
        } else if (!v1Empty && v2Empty) {
            m_type = CmsResourceComparison.TYPE_REMOVED;
        } else if ((v1Empty && v2Empty) || version1.equals(version2)) {
            m_type = CmsResourceComparison.TYPE_UNCHANGED;
        } else {
            m_type = CmsResourceComparison.TYPE_CHANGED;
        }
    }

    /** 
     * Creates a new attribute comparison.<p> 
     * 
     * @param name the name to set
     * @param version1 the first value of the property
     * @param version2 the seconf value of the property
     * @param type the type indicating if the element value has been added, removed, modified or is unchanged 
     * {@link CmsResourceComparison#TYPE_ADDED}.
     */
    public CmsAttributeComparison(String name, String version1, String version2, String type) {

        m_name = name;
        m_version1 = version1;
        m_version2 = version2;
        m_type = type;
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
    public String getType() {

        return m_type;
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
    public void setType(String type) {

        m_type = type;
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
