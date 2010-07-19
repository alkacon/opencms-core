/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarContextButton.java,v $
 * Date   : $Date: 2010/07/19 14:11:43 $
 * Version: $Revision: 1.3 $
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
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsContextMenu;
import org.opencms.gwt.client.ui.CmsContextMenuHandler;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * The gallery tool-bar menu.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarContextButton extends A_CmsToolbarMenu {

    /** The menu. */
    protected CmsContextMenu m_menu;

    /** Signals whether the widget has been initialized or not. */
    private boolean m_initialized;

    /** The main content widget. */
    private FlexTable m_menuPanel;

    /** The handler resize registration. */
    private HandlerRegistration m_resizeRegistration;

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
            getHandler().loadContextMenu(CmsCoreProvider.get().getUri());
            m_initialized = true;
        }

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

    /**
     * Creates the menu and adds it to the panel.<p>
     * 
     * @param menuEntries the menu entries 
     */
    public void showMenu(List<I_CmsContextMenuEntry> menuEntries) {

        if ((menuEntries != null) && !menuEntries.isEmpty()) {
            m_menu = new CmsContextMenu(menuEntries, true);
            m_resizeRegistration = Window.addResizeHandler(m_menu);
        }
        getPopupContent().addCloseHandler(new CmsContextMenuHandler(m_menu));

        DOM.removeElementAttribute(getPopupContent().getWidget().getElement(), "style");
        m_menuPanel.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuPanel());
        m_menuPanel.setWidget(0, 0, m_menu);
    }
}