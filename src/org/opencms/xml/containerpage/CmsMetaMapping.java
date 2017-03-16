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

package org.opencms.xml.containerpage;

/**
 * Configuration bean for meta mappings.<p>
 */
public class CmsMetaMapping {

    /** The mapping key. */
    private String m_key;

    /** The mapped element xpath. */
    private String m_element;

    /** The mapping order. */
    private int m_order;

    /** The mapping default value. */
    private String m_defaultValue;

    /**
     * Constructor.<p>
     *
     * @param key the mapping key
     * @param element the mapped element xpath
     * @param order the mapping order
     * @param defaultValue the mapping default value
     */
    public CmsMetaMapping(String key, String element, int order, String defaultValue) {
        m_key = key;
        m_element = element;
        m_order = order;
        m_defaultValue = defaultValue;
    }

    /**
     * Returns the mapping default value.<p>
     *
     * @return the mapping default value
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * Returns the mapped element xpath.<p>
     *
     * @return the mapped element xpath
     */
    public String getElement() {

        return m_element;
    }

    /**
     * Returns the mapping key.<p>
     *
     * @return the mapping key
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the mapping order.<p>
     *
     * @return the mapping order
     */
    public int getOrder() {

        return m_order;
    }

}
