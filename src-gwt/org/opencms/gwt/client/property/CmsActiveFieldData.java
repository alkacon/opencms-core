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

package org.opencms.gwt.client.property;

import org.opencms.gwt.client.ui.input.I_CmsFormField;

/**
 * Represents information about which field in the property editor dialog is active.<p>
 */
public class CmsActiveFieldData {

    /** The tab. */
    public String m_page;

    /** The property name. */
    public String m_property;

    /** The selected field. */
    private I_CmsFormField m_field;

    /**
     * Creates a new instance.<p>
     *
     * @param field the field
     * @param tab the tab
     * @param property the property name
     */
    public CmsActiveFieldData(I_CmsFormField field, String tab, String property) {
        m_field = field;
        m_page = tab;
        m_property = property;
    }

    /**
     * Gets the field.<p>
     *
     * @return the field
     */
    public I_CmsFormField getField() {

        return m_field;
    }

    public String getFieldId() {

        return (m_field != null) ? m_field.getId() : "(null)";
    }

    /**
     * Gets the property name.<p>
     *
     * @return the property name
     */
    public String getProperty() {

        return m_property;
    }

    /**
     * Gets the tab name .<p>
     *
     * @return the tab
     */
    public String getTab() {

        return m_page;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_page + ":" + m_property;
    }

}
