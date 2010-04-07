/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/A_CmsToolbarMenu.java,v $
 * Date   : $Date: 2010/04/07 12:06:02 $
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

import org.opencms.ade.containerpage.client.CmsContainerpageEditor;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.gwt.client.ui.CmsMenuButton;
import org.opencms.gwt.client.ui.CmsToolbarButton;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * Abstract button class implementing common methods of {@link org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton} 
 * for all container-page tool-bar menu buttons.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsToolbarMenu extends CmsMenuButton implements I_CmsContainerpageToolbarButton {

    /** The CSS class responsible for displaying the proper icon. */
    private String m_iconClass;

    /** The button name. */
    private String m_name;

    /** The show left flag, default is <code>true</code>. */
    protected boolean m_showLeft = true;

    /**
     * @param icon
     * @param title
     * @param name
     * @param showLeft set <code>true</code> if the button should be displayed on the left side of the tool-bar
     */
    public A_CmsToolbarMenu(CmsToolbarButton.ButtonData buttonData, String name, boolean showLeft) {

        super(buttonData.getTitle(), buttonData.getIconClass());
        this.setTitle(buttonData.getTitle());
        m_name = name;
        m_iconClass = buttonData.getIconClass();
        m_showLeft = showLeft;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#createOptionForElement(org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement)
     */
    public CmsElementOptionButton createOptionForElement(CmsDragContainerElement element)
    throws UnsupportedOperationException {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#getIconClass()
     */
    public String getIconClass() {

        return m_iconClass;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#hasElementFunctions()
     */
    public boolean hasElementFunctions() {

        return false;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#isActive()
     */
    public boolean isActive() {

        return !isOpen();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#onElementClick(com.google.gwt.event.dom.client.ClickEvent, org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement)
     */
    public void onElementClick(ClickEvent event, CmsDragContainerElement element) {

        throw new UnsupportedOperationException();

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (active) {
            if (CmsContainerpageEditor.INSTANCE.getCurrentButton() != null) {
                CmsContainerpageEditor.INSTANCE.getCurrentButton().setActive(false);
            }
            onToolbarActivate();
            CmsContainerpageEditor.INSTANCE.setCurrentButton(this);
            this.openMenu();

        } else {
            onToolbarDeactivate();
            CmsContainerpageEditor.INSTANCE.setCurrentButton(null);
            this.closeMenu();
        }

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#showLeft()
     */
    public boolean showLeft() {

        return m_showLeft;
    }

}
