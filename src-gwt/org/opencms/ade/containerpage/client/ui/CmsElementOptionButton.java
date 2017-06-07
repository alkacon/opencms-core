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

import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;

/**
 * An optional container element button.<p>
 *
 * @since 8.0.0
 */
public class CmsElementOptionButton extends CmsPushButton implements I_CmsDragHandle {

    /** The associated container element. */
    private CmsContainerPageElementPanel m_dragElement;

    /** The associated tool-bar button. */
    private A_CmsToolbarOptionButton m_toolbarButton;

    /**
     * Constructor.<p>
     *
     * @param toolbarButton the tool-bar button associated with this button, providing all necessary information
     * @param element the element to create this button for
     */
    public CmsElementOptionButton(A_CmsToolbarOptionButton toolbarButton, CmsContainerPageElementPanel element) {

        super();
        setImageClass(toolbarButton.getButtonData().getSmallIconClass());
        setButtonStyle(ButtonStyle.FONT_ICON, null);
        setTitle(toolbarButton.getTitle());
        m_toolbarButton = toolbarButton;
        m_dragElement = element;
    }

    /**
     * Returns the dragElement.<p>
     *
     * @return the dragElement
     */
    public CmsContainerPageElementPanel getContainerElement() {

        return m_dragElement;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDragHandle#getDraggable()
     */
    public I_CmsDraggable getDraggable() {

        return m_dragElement;
    }

    /**
     * Returns the associated tool-bar button.<p>
     *
     * @return the associated tool-bar button
     */
    public A_CmsToolbarOptionButton getToolbarButton() {

        return m_toolbarButton;
    }

}
