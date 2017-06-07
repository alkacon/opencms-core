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

package org.opencms.workplace.tools.accounts;

/**
 * User additional information bean.<p>
 *
 * @since 6.5.6
 */
public class CmsUserAddInfoBean {

    /** The name of the additional info entry. */
    private String m_name;

    /** The type. */
    private Class<?> m_type;

    /** The value. */
    private String m_value;

    /**
     * Default constructor.<p>
     *
     * @param name the name of the additional info entry
     * @param value the value
     * @param type the type
     */
    public CmsUserAddInfoBean(String name, String value, Class<?> type) {

        m_name = name;
        m_value = value;
        m_type = type;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public Class<?> getType() {

        return m_type;
    }

    /**
     * Returns the value.<p>
     *
     * @return the value
     */
    public String getValue() {

        return m_value;
    }

    /**
     * Sets the value.<p>
     *
     * @param value the value to set
     */
    public void setValue(String value) {

        m_value = value;
    }
}
