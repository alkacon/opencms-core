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

package org.opencms.gwt.client.util;

import com.google.gwt.user.client.ui.UIObject;

/**
 * This is a helper class for changing GWT UIObjects' styles between a set of given values.<p>
 *
 * Every time a new value is set, the previous value of the style variable will be removed
 * from all associated UI objects, and the new style name will be added.
 *
 * @since 8.0.0
 *
 */
public class CmsStyleVariable {

    /** The ui objects associated with this style variable. */
    private final UIObject[] m_uis;

    /** The current style value. */
    private String m_style;

    /**
     * Creates a new instance.<p>
     *
     * @param uis the list of UI objects to associate with this style variable
     */
    public CmsStyleVariable(UIObject... uis) {

        m_uis = uis;
    }

    /**
     * Returns the current style value.<p>
     *
     * @return the current style value, or <code>null</code> if non is set
     */
    public String getValue() {

        return m_style;
    }

    /**
     * Removes the previous value of the style variable from all associated ui objects
     * and adds the new value as a style name to all of them.<p>
     *
     * @param newStyle the new style name
     */
    public void setValue(String newStyle) {

        for (UIObject ui : m_uis) {
            if (m_style != null) {
                ui.removeStyleName(m_style);
            }
            if (newStyle != null) {
                ui.addStyleName(newStyle);
            }
        }
        m_style = newStyle;
    }
}
