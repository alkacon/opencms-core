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

/**
 * Represents the three possible display types (small, medium, wide).
 */
public enum CmsDisplayType {
    /** Wide. */
    wide(1241),
    /** Medium. *s*/
    medium(984),
    /** Small. **/
    small(0);

    /** The minimum width. */
    private int m_minWidth;

    /**
     * Creates a new instance.<p>
     *
     * @param width the minimum width
     */
    private CmsDisplayType(int width) {
        m_minWidth = width;
    }

    /**
     * Gets the display type corresponding to a given width.<p>
     *
     * @param width the window width
     *
     * @return the matching display type
     */
    public static CmsDisplayType getDisplayType(int width) {

        CmsDisplayType[] values = CmsDisplayType.values();
        for (CmsDisplayType type : values) {
            if (width >= type.minWidth()) {
                return type;
            }
        }

        return wide;
    }

    /**
     * Gets the minimum window width for this size.<p>
     *
     * @return the minimum window width
     */
    public int minWidth() {

        return m_minWidth;
    }

}
