/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarContextButton.java,v $
 * Date   : $Date: 2010/07/14 12:42:17 $
 * Version: $Revision: 1.1 $
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
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * The gallery tool-bar menu.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarContextButton extends A_CmsToolbarMenu {

    /** The main content widget. */
    private FlexTable m_menuPanel;

    private boolean m_initialized;

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

                    Window.alert("Menu item have been selected");
                }
            };
            CmsContextMenu relations = new CmsContextMenu(true);
            relations.addItem("Link relation to ...", cmd);
            relations.addItem("Link relation from ...", cmd);
            relations.addSeparator();
            relations.addItem("Asign Categories", cmd);

            CmsContextMenu test = new CmsContextMenu(true);
            test.addItem("Touch", cmd);
            test.addItem("Availability", cmd);
            test.addSeparator();
            test.addItem("Secure/Export", cmd);
            test.addItem("Change type", cmd);
            test.addSeparator();
            test.addItem("Restore deleted", cmd);

            CmsContextMenu advanced = new CmsContextMenu(true);
            advanced.addItem("test", test);
            advanced.addSeparator();
            advanced.addItem("Touch", cmd);
            advanced.addItem("Availability", cmd);
            advanced.addSeparator();
            advanced.addItem("Secure/Export", cmd);
            advanced.addItem("Change type", cmd);
            advanced.addSeparator();
            advanced.addItem("Restore deleted", cmd);

            CmsContextMenu menu = new CmsContextMenu(true);
            menu.addItem("Lock", cmd);
            menu.addItem("Locked resources", cmd);
            menu.addSeparator();
            menu.addItem("Publish directly", cmd);
            menu.addSeparator();
            menu.addItem("Edit Metadata", cmd);
            menu.addSeparator();
            menu.addItem("Copy", cmd);
            menu.addItem("Rename/Move", cmd);
            menu.addItem("Delete", cmd);
            menu.addItem("Undo changes", cmd);
            menu.addSeparator();
            menu.addItem("Relations", relations);
            menu.addSeparator();
            menu.addItem("Permissions", cmd);
            menu.addItem("Change navigation", cmd);
            menu.addSeparator();
            menu.addItem("Advanced", advanced);
            menu.addSeparator();
            menu.addItem("History", cmd);
            menu.addItem("Properties", cmd);
            getPopupContent().addCloseHandler(new CmsContextMenuHandler(menu));

            Element e = getPopupContent().getWidget().getElement();
            DOM.removeElementAttribute(e, "style");

            m_menuPanel.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuPanel());
            m_menuPanel.setWidget(0, 0, menu);
            m_initialized = true;
        }

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        // nothing to do
    }

}