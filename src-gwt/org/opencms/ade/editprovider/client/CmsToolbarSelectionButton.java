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

package org.opencms.ade.editprovider.client;

import org.opencms.gwt.client.ui.A_CmsToolbarButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * The selection button for the direct edit provider.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarSelectionButton extends A_CmsToolbarButton<CmsDirectEditToolbarHandler> {

    /** The style variable which controls the direct edit button visibility. */
    CmsStyleVariable m_editButtonsVisibility;

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     */
    public CmsToolbarSelectionButton(CmsDirectEditToolbarHandler handler) {

        super(I_CmsButton.ButtonData.SELECTION, handler);
        addStyleName(I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarShow());
        m_editButtonsVisibility = new CmsStyleVariable(RootPanel.get());
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        // this is never called
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarClick()
     */
    @Override
    public void onToolbarClick() {

        if (areEditButtonsVisible()) {
            setDirectEditButtonsVisible(false);
        } else {
            setDirectEditButtonsVisible(true);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        // this is never called
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsToolbarButton#setActive(boolean)
     */
    @Override
    public void setActive(boolean active) {

        setDown(active);
        setDirectEditButtonsVisible(active);
    }

    /**
     * Sets the visibility of the direct edit buttons.<p>
     *
     * @param visible true if the buttons should be shown
     */
    protected void setDirectEditButtonsVisible(boolean visible) {

        m_editButtonsVisibility.setValue(
            visible
            ? I_CmsLayoutBundle.INSTANCE.directEditCss().showButtons()
            : I_CmsLayoutBundle.INSTANCE.directEditCss().hideButtons());
        getHandler().showToolbar(visible);
    }

    /**
     * Checks whether the direct edit buttons are visible.<p>
     *
     * @return true if the direct edit buttons are visible
     */
    private boolean areEditButtonsVisible() {

        return m_editButtonsVisibility.getValue().equals(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.directEditCss().showButtons());
    }
}
