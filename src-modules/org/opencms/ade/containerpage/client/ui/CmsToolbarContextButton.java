/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarContextButton.java,v $
 * Date   : $Date: 2010/07/15 17:13:12 $
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
import org.opencms.gwt.client.ui.CmsContextMenu;
import org.opencms.gwt.client.ui.CmsContextMenuHandler;
import org.opencms.gwt.client.ui.CmsContextMenuItem;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * The gallery tool-bar menu.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarContextButton extends A_CmsToolbarMenu {

    /** The main content widget. */
    private FlexTable m_menuPanel;

    /** The handler resize registration. */
    private HandlerRegistration m_resizeRegistration;

    /** Signals whether the widget has been initialized or not.  */
    private boolean m_initialized;

    /** The menu. */
    private CmsContextMenu m_menu = new CmsContextMenu(true);

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     */
    public CmsToolbarContextButton(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.CONTEXT, handler);
        m_menuPanel = new FlexTable();
        setMenuWidget(m_menuPanel);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        if (!m_initialized) {
            Command cmd = new Command() {

                public void execute() {

                    Window.alert("Menu item has been selected");
                }
            };

            String imageClass = I_CmsLayoutBundle.INSTANCE.contextmenuCss().image()
                + " "
                + I_CmsLayoutBundle.INSTANCE.iconsCss().uiIcon()
                + " "
                + I_CmsButton.UiIcon.bookmark.name();

            String imagePath = "/opencms/opencms/system/workplace/resources/filetypes/xmlcontent.gif";

            CmsContextMenu relations = new CmsContextMenu(true);
            relations.addItem(CmsContextMenuItem.createItemWithImageClass("Link relation to ...", cmd, imageClass));
            relations.addItem(CmsContextMenuItem.createItemWithoutImage("Link relation from ...", cmd));
            relations.addSeparator();
            relations.addItem(CmsContextMenuItem.createItemWithoutImage("Asign Categories", cmd));

            CmsContextMenu test = new CmsContextMenu(true);
            test.addItem(CmsContextMenuItem.createItemWithoutImage("Touch", cmd));
            test.addItem(CmsContextMenuItem.createItemWithoutImage("Availability", cmd));
            test.addSeparator();
            test.addItem(CmsContextMenuItem.createItemWithoutImage("Secure/Export", cmd));
            test.addItem(CmsContextMenuItem.createItemWithoutImage("Change type", cmd));
            test.addSeparator();
            test.addItem(CmsContextMenuItem.createItemWithoutImage("Restore deleted", cmd));

            CmsContextMenu advanced = new CmsContextMenu(true);
            advanced.addItem(CmsContextMenuItem.createItemWithoutImage("test", test));
            advanced.addSeparator();
            advanced.addItem(CmsContextMenuItem.createItemWithoutImage("Touch", cmd));
            advanced.addItem(CmsContextMenuItem.createItemWithoutImage("Availability", cmd));
            advanced.addSeparator();
            advanced.addItem(CmsContextMenuItem.createItemWithoutImage("Secure/Export", cmd));
            advanced.addItem(CmsContextMenuItem.createItemWithoutImage("Change type", cmd));
            advanced.addSeparator();
            advanced.addItem(CmsContextMenuItem.createItemWithoutImage("Restore deleted", cmd));

            CmsContextMenuItem item = CmsContextMenuItem.createItemWithoutImage("Lock", cmd);
            item.setEnabled(false, "can't touch this!");
            m_menu.addItem(item);
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Locked resources", cmd));
            m_menu.addSeparator();
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Publish directly", cmd));
            m_menu.addSeparator();
            m_menu.addItem(CmsContextMenuItem.createItemWithImagePath("Edit Metadata", cmd, imagePath));
            m_menu.addSeparator();
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Copy", cmd));
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Rename/Move", cmd));
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Delete", cmd));
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Undo changes", cmd));
            m_menu.addSeparator();
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Relations", relations));
            m_menu.addSeparator();
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Permissions", cmd));
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Change navigation", cmd));
            m_menu.addSeparator();
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Advanced", advanced));
            m_menu.addSeparator();
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("History", cmd));
            m_menu.addItem(CmsContextMenuItem.createItemWithoutImage("Properties", cmd));

            getPopupContent().addCloseHandler(new CmsContextMenuHandler(m_menu));

            DOM.removeElementAttribute(getPopupContent().getWidget().getElement(), "style");
            m_menuPanel.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuPanel());
            m_menuPanel.setWidget(0, 0, m_menu);

            m_initialized = true;
        }

        m_resizeRegistration = Window.addResizeHandler(m_menu);
    }

    /**
     * Unregister the resize handler.<p>
     * 
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        if (m_resizeRegistration != null) {
            m_resizeRegistration.removeHandler();
            m_resizeRegistration = null;
        }
    }

}