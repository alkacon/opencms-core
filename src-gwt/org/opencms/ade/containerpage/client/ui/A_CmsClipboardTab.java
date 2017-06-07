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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsScrollPanel;

import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for clipboard tabs.<p>
 */
public abstract class A_CmsClipboardTab extends Composite {

    /**
     * Adds an item to the list.<p>
     *
     * @param item the item to add
     */
    public void addListItem(CmsListItem item) {

        getList().add(item);
    }

    /**
     * Clears the list.<p>
     */
    public void clearList() {

        getList().clear();
    }

    /**
     * Returns the item list.<p>
     *
     * @return the item list
     */
    public abstract CmsList<CmsListItem> getList();

    /**
     * Returns the height required by the tab content.<p>
     *
     * @return the tab content height
     */
    public int getRequiredHeight() {

        return getList().getElement().getClientHeight() + 17;
    }

    /**
     * Returns the scroll panel.<p>
     *
     * @return the scroll panel
     */
    public abstract CmsScrollPanel getScrollPanel();

    /**
     * Replaces the item with the same id if present.<p>
     *
     * @param item the new item
     */
    public void replaceItem(CmsListItem item) {

        CmsListItem oldItem = getList().getItem(item.getId());
        if (oldItem != null) {
            int index = getList().getWidgetIndex(oldItem);
            getList().removeItem(oldItem);
            if (index >= getList().getWidgetCount()) {
                getList().addItem(item);
            } else {
                getList().insertItem(item, index);
            }
        }
    }
}
