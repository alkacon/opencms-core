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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;

import com.google.gwt.dom.client.Document;

/**
 * Abstract button class implementing common methods 
 * of {@link org.opencms.gwt.client.ui.I_CmsToolbarButton} 
 * for container-page tool-bar buttons.<p>
 * 
 * @param <HANDLER> the handler class to use for the button type 
 * 
 * @since 8.0.0
 */
public abstract class A_CmsToolbarButton<HANDLER extends I_CmsToolbarHandler> extends CmsToggleButton
implements I_CmsToolbarButton {

    /** The handler instance. */
    protected HANDLER m_handler;

    /** The CSS class responsible for displaying the proper icon. */
    private String m_iconClass;

    /** True if this button is active. */
    private boolean m_isActive;

    /**
     * Constructor.<p>
     * 
     * @param buttonData the button data to use
     * @param handler the container-page handler
     */
    protected A_CmsToolbarButton(I_CmsButton.ButtonData buttonData, HANDLER handler) {

        super(buttonData);
        setButtonStyle(ButtonStyle.IMAGE, null);
        setSize(Size.big);
        m_handler = handler;
        m_iconClass = buttonData.getIconClass();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#getIconClass()
     */
    public String getIconClass() {

        return m_iconClass;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#isActive()
     */
    public boolean isActive() {

        return isDown();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarClick()
     */
    public void onToolbarClick() {

        boolean active = isDown();

        if (active) {
            m_handler.deactivateCurrentButton();
            m_handler.setActiveButton(this);
            onToolbarActivate();
        } else {
            m_handler.deactivateCurrentButton();
            m_handler.activateSelection();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#setActive(boolean)
     */
    public void setActive(boolean active) {

        m_isActive = active;
        setDown(m_isActive);

        if (active) {
            if (m_handler.getActiveButton() != this) {
                m_handler.deactivateCurrentButton();
            }
            m_handler.setActiveButton(this);
            onToolbarActivate();
        } else {
            onToolbarDeactivate();
            m_handler.setActiveButton(null);
        }
    }

    /**
     * Toggle function. Shows of the element option buttons only the ones associated with this button.<p>
     * 
     * @param show <code>true</code> if to show the buttons
     */
    public void showSingleElementOption(boolean show) {

        if (show) {
            Document.get().getBody().addClassName(m_iconClass);
        } else {
            Document.get().getBody().removeClassName(m_iconClass);
        }
    }

    /**
     * Returns the container-page handler.<p>
     * 
     * @return the container-page handler
     */
    protected HANDLER getHandler() {

        return m_handler;
    }
}
