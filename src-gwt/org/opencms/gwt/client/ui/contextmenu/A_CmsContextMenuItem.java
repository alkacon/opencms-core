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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A abstract implementation for a context menu item.<p>
 *
 * @since version 8.0.0
 */
public abstract class A_CmsContextMenuItem extends Composite
implements ClickHandler, MouseOutHandler, MouseOverHandler, HasClickHandlers, HasMouseOutHandlers,
HasMouseOverHandlers {

    /** Signals if the item should be disabled. */
    private boolean m_active;

    /** The handler registration for the click handler. */
    private HandlerRegistration m_clickRegistration;

    /** The handler registration for the click handler. */
    private HandlerRegistration m_mouseOutRegistration;

    /** The handler registration for the click handler. */
    private HandlerRegistration m_mouseOverRegistration;

    /** The parent menu of this menu item. */
    private CmsContextMenu m_parentMenu;

    /** The optional sub menu of this menu item. */
    private CmsContextMenu m_subMenu;

    /** The text for the menu item. */
    private String m_text;

    /**
     * Constructor.<p>
     *
     * @param text the text for the menu item
     */
    protected A_CmsContextMenuItem(String text) {

        m_active = true;
        m_text = text;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        m_clickRegistration = addDomHandler(handler, ClickEvent.getType());
        return m_clickRegistration;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        m_mouseOutRegistration = addDomHandler(handler, MouseOutEvent.getType());
        return m_mouseOutRegistration;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        m_mouseOverRegistration = addDomHandler(handler, MouseOverEvent.getType());
        return m_mouseOverRegistration;
    }

    /**
     * Deselects an item.<p>
     */
    public void deselectItem() {

        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().selected());
        m_parentMenu.setSelectedItem(null);
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
     * Returns the text of this menu item.<p>
     *
     * @return the text
     */
    public String getText() {

        return m_text;
    }

    /**
     * Returns <code>true</code> if this menu item has a sub menu, <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this menu item has a sub menu
     */
    public boolean hasSubmenu() {

        if (m_subMenu != null) {
            return true;
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the item is active <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if the item is active <code>false</code> otherwise
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * The action that is executed on click depends on the concrete implementation of a menu item.
     * So the onClick Method has to be implemented in the sub class.<p>
     *
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    public abstract void onClick(ClickEvent event);

    /**
     * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    public final void onMouseOut(MouseOutEvent event) {

        onHoverOut(event);
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    public final void onMouseOver(final MouseOverEvent event) {

        onHoverIn(event);
    }

    /**
     * Selects a item.<p>
     */
    public void selectItem() {

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().selected());
        m_parentMenu.setSelectedItem(this);
    }

    /**
     * Makes the menu item active or inactive.<p>
     *
     * If the item is inactive all handlers are removed.<p>
     *
     * @param active <code>true</code> if the item should be active, <code>false</code> otherwise
     * @param reason the reason for de-activation
     */
    public void setActive(boolean active, String reason) {

        if (!active) {
            m_clickRegistration.removeHandler();
            m_mouseOutRegistration.removeHandler();
            m_mouseOverRegistration.removeHandler();
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().disabled());
            getElement().setAttribute("title", reason);
        } else {
            addMouseOutHandler(this);
            addMouseOverHandler(this);
            addClickHandler(this);
            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().disabled());
        }
        m_active = active;
    }

    /**
     * Sets the text of the menu item.<p>
     *
     * @param text the text to set
     */
    public void setText(String text) {

        m_text = text;
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#initWidget(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    protected void initWidget(Widget widget) {

        super.initWidget(widget);
        addMouseOutHandler(this);
        addMouseOverHandler(this);
        addClickHandler(this);
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
     * If a menu item has no sub menu it will be deselected.<p>
     *
     * @param event the mouse out event
     */
    protected void onHoverOut(MouseOutEvent event) {

        if (!hasSubmenu()) {
            deselectItem();
        }
    }

    /**
     * Sets the parent menu of this menu item.<p>
     *
     * @param parentMenu the parent menu to set
     */
    protected void setParentMenu(CmsContextMenu parentMenu) {

        m_parentMenu = parentMenu;
    }

    /**
     * Sets the sub menu for the menu item.<p>
     *
     * @param subMenu the sub menu to set
     */
    protected void setSubMenu(CmsContextMenu subMenu) {

        m_subMenu = subMenu;
        subMenu.setParentItem(this);

    }
}