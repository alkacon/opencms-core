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

package org.opencms.gwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * UiBinder widget for the create-mode selection dialog content. <p>
 */
public class CmsCreateModeSelectionView extends Composite {

    /** UiBinder interface. */
    interface I_CmsCreateModeSelectionViewUiBinder extends UiBinder<Widget, CmsCreateModeSelectionView> {
        // empty interface
    }

    /** UiBinder instance. */
    private static I_CmsCreateModeSelectionViewUiBinder uiBinder = GWT.create(
        I_CmsCreateModeSelectionViewUiBinder.class);

    /** UiBinder widget. */
    @UiField
    protected FlowPanel m_infoBox;

    /** UiBinder widget. */
    @UiField
    protected Label m_label;

    /**
     * Create a new instance.<p>
     */
    public CmsCreateModeSelectionView() {

        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Returns the infoBox.<p>
     *
     * @return the infoBox
     */
    public FlowPanel getInfoBox() {

        return m_infoBox;
    }

    /**
     * Returns the label.<p>
     *
     * @return the label
     */
    public Label getLabel() {

        return m_label;
    }

}
