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

/**
 * Abstract button class implementing common methods of {@link org.opencms.gwt.client.ui.I_CmsToolbarButton}
 * for all container-page tool-bar menu buttons.<p>
 *
 * @param <HANDLER> the handler class for the menu button
 *
 * @since 8.0.0
 */
public abstract class A_CmsToolbarMenu<HANDLER extends I_CmsToolbarHandler> extends CmsMenuButton
implements I_CmsToolbarButton {

    /** The handler instance. */
    private HANDLER m_handler;

    /**
     * Constructor.<p>
     *
     * @param buttonData the tool-bar button data
     * @param handler the container-page handler
     */
    public A_CmsToolbarMenu(I_CmsButton.ButtonData buttonData, HANDLER handler) {

        super(null, buttonData.getIconClass());
        setToolbarMode(true);
        setOpenRight(true);
        m_handler = handler;
        setTitle(buttonData.getTitle());
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#hideMenu()
     */
    @Override
    public void hideMenu() {

        super.hideMenu();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#isActive()
     */
    public boolean isActive() {

        return isOpen();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarClick()
     */
    public void onToolbarClick() {

        boolean active = isActive();

        setActive(!active);

    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (active) {
            m_handler.deactivateCurrentButton();
            m_handler.setActiveButton(this);
            m_popup.catchNotifications();
            onToolbarActivate();
            openMenu();
        } else {
            onToolbarDeactivate();
            closeMenu();
            m_handler.setActiveButton(null);
            m_handler.activateSelection();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#autoClose()
     */
    @Override
    protected void autoClose() {

        super.autoClose();
        onToolbarDeactivate();
        m_handler.setActiveButton(null);
        m_handler.activateSelection();
    }

    /**
     * Returns the container-page handler.<p>
     *
     * @return the container-page handler
     */
    protected HANDLER getHandler() {

        return m_handler;
    }

    /**
     * Sets the button handler.<p>
     *
     * @param handler the button handler
     */
    protected void setHandler(HANDLER handler) {

        m_handler = handler;
    }
}
