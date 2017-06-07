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

/**
 * Container which contains at most one {@link org.opencms.gwt.client.util.I_CmsUniqueActiveItem}.<p>
 *
 * If a new value is set while this container already contains an item, the previous item is removed and notified of that removal.<p>
 */
public class CmsUniqueActiveItemContainer {

    /** The current item. */
    private I_CmsUniqueActiveItem m_activeItem;

    /**
     * Removes the current item.
     */
    public void clear() {

        if (m_activeItem != null) {
            m_activeItem.onDeactivate();
            m_activeItem = null;
        }
    }

    /**
     * Removes the current item only if it is the same object as the given parameter.<p>
     *
     * @param item the item to match
     */
    public void clearIfMatches(I_CmsUniqueActiveItem item) {

        if (item == m_activeItem) {
            clear();
        }
    }

    /**
     * Sets the active item.<p>
     *
     * If this container already contains an item, it is replaced with the given item, and its onDeactivate() method is called.<p>
     *
     * @param item the new item
     */
    public void setActiveItem(I_CmsUniqueActiveItem item) {

        clear();
        if (item != null) {
            m_activeItem = item;
        }
    }

}
