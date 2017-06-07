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

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * The move button holding all move related methods.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarMoveButton extends A_CmsToolbarOptionButton {

    /** The drag and drop handler. */
    private CmsDNDHandler m_dndHandler;

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     * @param dndHandler the drag and drop handler
     */
    public CmsToolbarMoveButton(CmsContainerpageHandler handler, CmsDNDHandler dndHandler) {

        super(I_CmsButton.ButtonData.MOVE, handler);
        m_dndHandler = dndHandler;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#createOptionForElement(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public CmsElementOptionButton createOptionForElement(CmsContainerPageElementPanel element) {

        CmsElementOptionButton button = super.createOptionForElement(element);
        if (button != null) {
            button.addMouseDownHandler(m_dndHandler);
        }
        return button;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#isOptionAvailable(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public boolean isOptionAvailable(CmsContainerPageElementPanel element) {

        boolean disableButtons = CmsContainerpageController.get().isEditingDisabled();
        return !element.getParentTarget().isDetailView() && !disableButtons;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#onElementClick(com.google.gwt.event.dom.client.ClickEvent, org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public void onElementClick(ClickEvent event, CmsContainerPageElementPanel element) {

        // do nothing

    }
}
