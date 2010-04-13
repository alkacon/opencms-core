/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarClipboardMenu.java,v $
 * Date   : $Date: 2010/04/13 14:29:43 $
 * Version: $Revision: 1.5 $
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
import org.opencms.gwt.client.ui.CmsToolbarButton;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The clip-board tool-bar menu.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarClipboardMenu extends A_CmsToolbarMenu {

    private CmsDragTargetMenu m_dropZone;

    /** The button name. */
    public static final String BUTTON_NAME = "clipboard";

    /**
     * Constructor.<p>
     */
    public CmsToolbarClipboardMenu() {

        super(CmsToolbarButton.ButtonData.CLIPBOARD, BUTTON_NAME, true);

        //TODO: replace the following with the real menu content
        FlowPanel content = new FlowPanel();
        content.setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuContent());
        //        CmsTabbedPanel tabs=new CmsTabbedPanel();
        //        content.add(tabs);

        m_dropZone = new CmsDragTargetMenu();
        m_dropZone.setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuDropzone());

        // overriding overflow hidden set by AbsolutePanel
        m_dropZone.getElement().getStyle().clearOverflow();

        content.add(m_dropZone);
        //        Label menuContent = new Label("Menu content");
        //        menuContent.getElement().getStyle().setHeight(100, Unit.PX);
        //        menuContent.getElement().getStyle().setWidth(650, Unit.PX);
        setMenuWidget(content);
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
     * Returns the tool-bar drop-zone.<p>
     *
     * @return the drop-zone
     */
    public CmsDragTargetMenu getDropZone() {

        return m_dropZone;
    }

}
