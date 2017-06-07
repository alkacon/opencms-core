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

package org.opencms.ui.util;

import org.opencms.util.CmsStringUtil;

import com.vaadin.ui.Component;

/**
 *
 */
public class CmsStyleVariable {

    /** The component to set the CSS class to. */
    private Component m_component;

    /** The current value. */
    private String m_style;

    /**
     * Constructor.<p>
     *
     * @param component the component
     */
    public CmsStyleVariable(Component component) {
        m_component = component;
    }

    /**
     * Returns the style.<p>
     *
     * @return the style
     */
    public String getStyle() {

        return m_style;
    }

    /**
     * Sets the style.<p>
     *
     * @param style the style to set
     */
    public void setStyle(String style) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_style)) {
            m_component.removeStyleName(m_style);
        }
        m_style = style;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_style)) {
            m_component.addStyleName(m_style);
        }
    }

}
