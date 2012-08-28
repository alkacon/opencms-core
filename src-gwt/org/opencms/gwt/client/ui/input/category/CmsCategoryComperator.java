/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;

import java.util.Comparator;

/***/
class CmsCategoryComperator implements Comparator<CmsDataValue> {

    private String m_param;

    /**
     * 
     */
    public CmsCategoryComperator(String param) {

        m_param = param;

    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(CmsDataValue o1, CmsDataValue o2) {

        int result = 0;
        SortParams sort = SortParams.valueOf(m_param);
        switch (sort) {
            case title_asc:
                result = o1.getLabel().compareTo(o2.getLabel());
                break;
            case title_desc:
                result = o1.getLabel().compareTo(o2.getLabel());
                result = -result;
                break;
            case path_asc:
                result = o1.getParameter(1).compareTo(o2.getParameter(1));
                break;
            case path_desc:
                result = o1.getParameter(1).compareTo(o2.getParameter(1));
                result = -result;
                break;
            default:
                // not supported
                return 0;

        }
        return result;
    }
}