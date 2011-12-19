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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Content of the tool-bar menu recent tab.<p>
 * 
 * @since 8.0.0
 */
public class CmsRecentTab extends Composite {

    /** The ui-binder interface for this widget. */
    interface I_CmsRecentTabUiBinder extends UiBinder<Widget, CmsRecentTab> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder for this widget. */
    private static I_CmsRecentTabUiBinder uiBinder = GWT.create(I_CmsRecentTabUiBinder.class);

    /** The list panel holding the recent elements. */
    @UiField(provided = true)
    protected CmsList<CmsListItem> m_listPanel = new CmsList<CmsListItem>();

    /**
     * Constructor.<p>
     */
    public CmsRecentTab() {

        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Adds an item to the recent list.<p>
     * 
     * @param item the item to add
     */
    public void addListItem(CmsListItem item) {

        m_listPanel.add(item);
    }

    /**
     * Clears the recent list.<p>
     */
    public void clearList() {

        m_listPanel.clear();
    }

    /**
     * Replaces the item with the same id if present.<p>
     * 
     * @param item the new item
     */
    public void replaceItem(CmsListItem item) {

        CmsListItem oldItem = m_listPanel.getItem(item.getId());
        if (oldItem != null) {
            int index = m_listPanel.getWidgetIndex(oldItem);
            m_listPanel.removeItem(oldItem);
            if (index >= m_listPanel.getWidgetCount()) {
                m_listPanel.addItem(item);
            } else {
                m_listPanel.insertItem(item, index);
            }
        }
    }

}
