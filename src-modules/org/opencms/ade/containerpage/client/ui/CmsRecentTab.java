/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsRecentTab.java,v $
 * Date   : $Date: 2010/05/21 13:20:08 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragTargetList;
import org.opencms.gwt.client.ui.CmsListItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Content of the tool-bar menu recent tab.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
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
    @UiField
    protected CmsDragTargetList m_listPanel;

    /** The tab description label. */
    @UiField
    protected Label m_decriptionLabel;

    /**
     * Constructor.<p>
     */
    public CmsRecentTab() {

        initWidget(uiBinder.createAndBindUi(this));
        m_decriptionLabel.setText(Messages.get().key(Messages.GUI_TAB_RECENT_DESCRIPTION_0));
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

}
