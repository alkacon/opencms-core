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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * Content of the tool-bar menu recent tab.<p>
 *
 * @since 8.0.0
 */
public class CmsRecentTab extends A_CmsClipboardTab {

    /** The ui-binder interface for this widget. */
    interface I_CmsRecentTabUiBinder extends UiBinder<Widget, CmsRecentTab> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder for this widget. */
    private static I_CmsRecentTabUiBinder uiBinder = GWT.create(I_CmsRecentTabUiBinder.class);

    /** The list panel holding the recent elements. */
    @UiField(provided = true)
    protected CmsList<CmsListItem> m_listPanel = new CmsList<CmsListItem>();

    /** The scroll panel. */
    @UiField
    protected CmsScrollPanel m_scrollPanel;

    /**
     * Constructor.<p>
     */
    public CmsRecentTab() {

        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsClipboardTab#getList()
     */
    @Override
    public CmsList<CmsListItem> getList() {

        return m_listPanel;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsClipboardTab#getScrollPanel()
     */
    @Override
    public CmsScrollPanel getScrollPanel() {

        return m_scrollPanel;
    }
}
