/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/A_CmsToolbarButton.java,v $
 * Date   : $Date: 2010/04/28 13:03:39 $
 * Version: $Revision: 1.2 $
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

import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.dom.client.Document;

/**
 * Abstract button class implementing common methods of {@link org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton} for container-page tool-bar buttons.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsToolbarButton extends CmsToggleButton implements I_CmsToolbarButton {

    /** The CSS class responsible for displaying the proper icon. */
    private String m_iconClass;

    private boolean m_isActive;

    private CmsContainerpageHandler m_handler;

    /**
     * Constructor.<p>
     * 
     * @param buttonData the button data to use
     * @param handler the container-page handler
     */
    protected A_CmsToolbarButton(I_CmsButton.ButtonData buttonData, CmsContainerpageHandler handler) {

        super(buttonData);
        m_handler = handler;
        m_iconClass = buttonData.getIconClass();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#createOptionForElement(org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement)
     */

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#getIconClass()
     */
    public String getIconClass() {

        return m_iconClass;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#isActive()
     */
    public boolean isActive() {

        return m_isActive;
        //return isDown();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#onToolbarClick()
     */
    public void onToolbarClick() {

        boolean active = isActive();

        if (!active) {
            m_handler.deactivateCurrentButton();
            m_handler.setActiveButton(this);
            setDown(true);
            onToolbarActivate();
        } else {
            m_handler.deactivateCurrentButton();
            m_handler.activateSelection();
        }

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#setActive(boolean)
     */
    public void setActive(boolean active) {

        m_isActive = active;
        setDown(m_isActive);

        if (active) {
            onToolbarActivate();
        } else {
            onToolbarDeactivate();
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
    protected CmsContainerpageHandler getHandler() {

        return m_handler;
    }
}
