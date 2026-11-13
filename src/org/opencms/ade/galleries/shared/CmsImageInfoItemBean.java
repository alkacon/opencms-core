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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries.shared;

import java.io.Serializable;

/**
 * An optional label/value item displayed in the image information box.<p>
 */
public class CmsImageInfoItemBean implements Serializable {

    /** The serialization version. */
    private static final long serialVersionUID = 1L;

    /** The display label, including any punctuation. */
    private String m_label;

    /** The display value. */
    private String m_value;

    /**
     * Creates an empty bean for serialization.<p>
     */
    public CmsImageInfoItemBean() {

        // For serialization.
    }

    /**
     * Creates an information item.<p>
     *
     * @param label the display label, including any punctuation
     * @param value the display value
     */
    public CmsImageInfoItemBean(String label, String value) {

        m_label = label;
        m_value = value;
    }

    /**
     * Returns the display label.<p>
     *
     * @return the display label
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * Returns the display value.<p>
     *
     * @return the display value
     */
    public String getValue() {

        return m_value;
    }

    /**
     * Sets the display label.<p>
     *
     * @param label the display label, including any punctuation
     */
    public void setLabel(String label) {

        m_label = label;
    }

    /**
     * Sets the display value.<p>
     *
     * @param value the display value
     */
    public void setValue(String value) {

        m_value = value;
    }
}
