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

package org.opencms.ui.apps.publishqueue;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsResource;

import java.util.List;

import com.vaadin.ui.Table;

/**
*   Table column generator for published-resources. Shows list as comma separated string of setted maximal length.<p>
*   The Value of property have to be of type List<CmsResource> or List<CmsPublishedResource>.<p>
*/
class CmsResourcesCellGenerator implements Table.ColumnGenerator {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -3349782291910407616L;

    /**limit size for the string to be shown.*/
    private int m_charLimit;

    /**
     * Default public constructor.<p>
     *
     * @param charLimit maximal chars to output ".." instead of next list item.
     */
    public CmsResourcesCellGenerator(int charLimit) {
        m_charLimit = charLimit;
    }

    /**
    * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
    */
    public Object generateCell(Table source, Object itemId, Object columnId) {

        List<?> resources = (List<?>)source.getItem(itemId).getItemProperty(columnId).getValue();

        String out = "";
        if (!resources.isEmpty()) {
            out = getRootPath(resources.get(0));
            int i = 1;
            while ((resources.size() > i) & (out.length() < m_charLimit)) {
                out += ", " + getRootPath(resources.get(i));
            }
            if (resources.size() > i) {
                out += " ...";
            }
        }
        return out;
    }

    /**
     * Reads path of given resource.<p>
     *
     * @param resource CmsResource or CmsPublishedResource to get path of
     * @return path
     */
    private String getRootPath(Object resource) {

        if (resource instanceof CmsResource) {
            return ((CmsResource)resource).getRootPath();
        }
        if (resource instanceof CmsPublishedResource) {
            return ((CmsPublishedResource)resource).getRootPath();
        }
        throw new IllegalArgumentException("wrong format of resources"); //should never happen
    }

}