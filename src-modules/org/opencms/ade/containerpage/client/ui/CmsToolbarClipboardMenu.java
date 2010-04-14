/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarClipboardMenu.java,v $
 * Date   : $Date: 2010/04/14 06:45:01 $
 * Version: $Revision: 1.6 $
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

import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragTargetMenu;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsToolbarButton;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The clip-board tool-bar menu.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarClipboardMenu extends A_CmsToolbarMenu {

    /** The button name. */
    public static final String BUTTON_NAME = "clipboard";

    /** The main content widget. */
    private FlowPanel m_content;

    /** The favorite list drop-zone. */
    private CmsDragTargetMenu m_dropzone;

    /** The favorite and recent list tabs. */
    private CmsTabbedPanel m_tabs;

    /**
     * Constructor.<p>
     */
    public CmsToolbarClipboardMenu() {

        super(CmsToolbarButton.ButtonData.CLIPBOARD, BUTTON_NAME, true);

        //TODO: replace the following with the real menu content
        m_content = new FlowPanel();
        m_content.setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuContent());
        m_tabs = new CmsTabbedPanel();
        m_tabs.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().clipboardTabs());
        m_content.add(m_tabs);

        m_dropzone = new CmsDragTargetMenu();
        m_dropzone.setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().clipboardDropzone());

        // overriding overflow hidden set by AbsolutePanel
        m_dropzone.getElement().getStyle().clearOverflow();

        m_content.add(m_dropzone);
        //        Label menuContent = new Label("Menu content");
        //        menuContent.getElement().getStyle().setHeight(100, Unit.PX);
        //        menuContent.getElement().getStyle().setWidth(650, Unit.PX);
        setMenuWidget(m_content);
    }

    /**
     * Returns the tool-bar drop-zone.<p>
     *
     * @return the drop-zone
     */
    public CmsDragTargetMenu getDropzone() {

        return m_dropzone;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#hasPermissions(org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement)
     */
    public boolean hasPermissions(CmsDragContainerElement element) {

        // TODO: Auto-generated method stub
        return true;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#init()
     */
    public void init() {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        // TODO: Auto-generated method stub

    }

    /**
     * Opens the menu showing the favorite list drop-zone and hiding all other menu content.<p>
     * 
     * @param show <code>true</code> to show the drop-zone
     */
    public void showDropzone(boolean show) {

        if (show) {
            m_content.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().showDropzone());
            openMenu();
        } else {
            m_content.removeStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().showDropzone());
            closeMenu();
        }
    }

}
