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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Additional info bean.<p>
 *
 * @since 8.0.1
 */
public class CmsAdditionalInfoBean implements IsSerializable {

    /** The info name. */
    private String m_name;

    /** The info style name. */
    private String m_styleName;

    /** The info value. */
    private String m_value;

    /**
     * Constructor.<p>
     *
     * @param name the info name
     * @param value the info value
     * @param styleName the info style name
     */
    public CmsAdditionalInfoBean(String name, String value, String styleName) {

        m_name = name;
        m_value = value;
        m_styleName = styleName;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsAdditionalInfoBean() {

        // nothing to do
    }

    /**
     * Returns the info name.<p>
     *
     * @return the info name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the info style name.<p>
     *
     * @return the info style name
     */
    public String getStyle() {

        return m_styleName;
    }

    /**
     * Returns the info value.<p>
     *
     * @return the info value
     */
    public String getValue() {

        return m_value;
    }

    /**
     * Sets the info name.<p>
     *
     * @param name the info name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the info style name.<p>
     *
     * @param styleName the info style name to set
     */
    public void setStyle(String styleName) {

        m_styleName = styleName;
    }

    /**
     * Sets the info value.<p>
     *
     * @param value the info value to set
     */
    public void setValue(String value) {

        m_value = value;
    }

}
