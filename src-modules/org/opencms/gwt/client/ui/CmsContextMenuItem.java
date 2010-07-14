/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsContextMenuItem.java,v $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;

/**
 * An entry in a 
 * {@link org.opencms.gwt.client.ui.CmsContextMenu}. Menu items can either fire a
 * {@link com.google.gwt.user.client.Command} when they are clicked, or open a
 * cascading sub-menu.
 * 
 * @author Ruediger Kurz
 */
public class CmsContextMenuItem extends Composite implements HasMouseOutHandlers, HasMouseOverHandlers {

    /** The command for this menu item. */
    private Command m_command;

    /** The panel containing the menu item text and optional the arrow. */
    private CmsContextMenuItemPanel m_panel;

    /** The parent menu of this menu item. */
    private CmsContextMenu m_parentMenu;

    /** The optional sub menu of this menu item. */
    private CmsContextMenu m_subMenu;

    /**
     * Constructs a new menu item that cascades to a sub-menu when it is selected.
     * 
     * @param text the item's text
     * @param subMenu the sub-menu to be displayed when it is selected
     */
    public CmsContextMenuItem(String text, CmsContextMenu subMenu) {

        this(text, true);
        m_subMenu = subMenu;
    }

    /**
     * Constructs a new menu item that fires a command when it is selected.
     * 
     * @param text the item's text
     * @param cmd the command to be fired when it is selected
     */
    public CmsContextMenuItem(String text, Command cmd) {

        this(text, false);
        m_command = cmd;
    }

    /**
     * Constructor.<p>
     * 
     * @param text the text for the menu item
     * @param hasSubmenu signals if the menu item has a sub menu
     */
    private CmsContextMenuItem(String text, boolean hasSubmenu) {

        m_panel = new CmsContextMenuItemPanel(this);
        m_panel.setHTML(getMenuItemHtml(text, hasSubmenu));
        initWidget(m_panel);
        setStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().cmsMenuItem());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return m_panel.addMouseOutHandler(handler);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    @Override
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return m_panel.addMouseOverHandler(handler);
    }

    /**
     * Returns the command of this menu item.<p>
     * 
     * @return the command
     */
    public Command getCommand() {

        return m_command;
    }

    /**
     * Returns the parent menu of this menu item.<p>
     *  
     * @return the parent menu
     */
    public CmsContextMenu getParentMenu() {

        return m_parentMenu;
    }

    /**
     * Returns the sub menu of this menu item.<p>
     * 
     * @return the sub menu
     */
    public CmsContextMenu getSubMenu() {

        return m_subMenu;
    }

    /**
     * Returns <code>true</code> if this menu item has a parent menu, <code>false</code> otherwise.<p>
     * 
     * @return <code>true</code> if this menu item has a parent menu
     */
    public boolean hasParentmenu() {

        if (m_parentMenu != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns <code>true</code> if this menu item has a sub menu, <code>false</code> otherwise.<p>
     * 
     * @return <code>true</code> if this menu item has a sub menu
     */
    public boolean hasSubmenu() {

        if (m_subMenu != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Selects a item.<p>
     */
    public void selectItem() {

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().selected());
        m_parentMenu.setSelectedItem(this);
    }

    /**
     * Sets the parent menu of this menu item.<p>
     * 
     * @param parentMenu the parent menu to set
     */
    public void setParentMenu(CmsContextMenu parentMenu) {

        m_parentMenu = parentMenu;
    }

    /**
     * Deselects a item.<p>
     */
    public void deselectItem() {

        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().selected());
        m_parentMenu.setSelectedItem(null);
    }

    /**
     * Implements the hover over action for a item.<p>
     * 
     * First closes all sub menus that are not required anymore.
     * And then reopens the necessary sub menus and activates the selected item.<p>
     * 
     * @param event the mouse over event
     */
    protected void onHoverIn(MouseOverEvent event) {

        if ((getParentMenu().getSelectedItem() != null)
            && (getParentMenu().getSelectedItem() != this)
            && getParentMenu().getSelectedItem().hasSubmenu()) {

            getParentMenu().getSelectedItem().getSubMenu().onClose();
            getParentMenu().getSelectedItem().deselectItem();
        }
        if (hasSubmenu()) {
            getSubMenu().openPopup(this);
        }
        selectItem();
    }

    /**
     * Implements the hover out action for a item.<p>
     * 
     * @param event the mouse out event
     */
    protected void onHoverOut(MouseOutEvent event) {

        if (!hasSubmenu()) {
            deselectItem();
        }
    }

    /**
     * Generates the HTML for a menu item.<p>
     * 
     * @param text the text of for the menu item
     * @param hasSubmenu signals if the menu item has a sub menu
     * 
     * @return the HTML for the menu item
     */
    protected String getMenuItemHtml(String text, boolean hasSubmenu) {

        StringBuffer html = new StringBuffer();
        if (hasSubmenu) {
            html.append("<div class=\"");
            html.append(I_CmsLayoutBundle.INSTANCE.contextmenuCss().arrow()
                + " "
                + I_CmsLayoutBundle.INSTANCE.iconsCss().uiIcon()
                + " "
                + I_CmsButton.UiIcon.triangle_1_e.name());
            html.append("\"></div>");
        }
        html.append("<div class=\"");
        html.append(I_CmsLayoutBundle.INSTANCE.contextmenuCss().label());
        html.append("\">" + text + "</div>");
        return html.toString();
    }

}
