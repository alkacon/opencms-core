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

package org.opencms.gwt.client.ui.input.category;

import org.opencms.gwt.client.ui.CmsListItem;

import java.util.Comparator;

/**
 * Comparator for list items with a data value widget as there main widget.<p>
 */
public class CmsListItemDataComparator implements Comparator<CmsListItem> {

    /** The parameter index to compare. */
    private int m_paramIndex;

    /** Flag to indicate sorting ascending or descending. */
    private boolean m_ascending;

    /**
     * Default Constructor.<p>
     *
     * @param paramIndex the parameter index to compare
     * @param ascending the sort order
     */
    public CmsListItemDataComparator(int paramIndex, boolean ascending) {

        m_paramIndex = paramIndex;
        m_ascending = ascending;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(CmsListItem o1, CmsListItem o2) {

        int result = 0;
        String val1 = null;
        String val2 = null;
        try {
            CmsDataValue d1 = (CmsDataValue)o1.getMainWidget();
            CmsDataValue d2 = (CmsDataValue)o2.getMainWidget();
            if (m_paramIndex == 0) {
                val1 = d1.getLabel();
                val2 = d2.getLabel();
            } else {
                val1 = d1.getParameter(m_paramIndex);
                val2 = d2.getParameter(m_paramIndex);
            }
        } catch (@SuppressWarnings("unused") Exception e) {
            // ignore class cast exceptions
        }

        if (val1 != null) {
            result = val1.compareTo(val2);
        } else if (val2 != null) {
            result = 1;
        }
        return m_ascending ? result : -result;
    }

}
