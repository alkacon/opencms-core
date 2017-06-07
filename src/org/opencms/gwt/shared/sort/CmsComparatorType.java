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

package org.opencms.gwt.shared.sort;

import java.util.Comparator;

/**
 * Comparator for objects with a type property.<p>
 *
 * @see I_CmsHasType
 *
 * @since 8.0.0
 */
public class CmsComparatorType implements Comparator<I_CmsHasType> {

    /** Sort order flag. */
    private boolean m_ascending;

    /**
     * Constructor.<p>
     *
     * @param ascending if <code>true</code> order is ascending
     */
    public CmsComparatorType(boolean ascending) {

        m_ascending = ascending;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(I_CmsHasType o1, I_CmsHasType o2) {

        int result = o1.getType().compareTo(o2.getType());
        return m_ascending ? result : -result;
    }
}
