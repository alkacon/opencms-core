/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsContextMenu.java,v $
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
import org.opencms.gwt.client.ui.input.CmsLabel;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A implementation for a context menu.<p>
 * 
 * @author Ruediger Kurz
 */
public class CmsContextMenu extends Composite {

    /** Stores the selected item. */
    protected CmsContextMenuItem m_selectedItem;

    /** The panel for the menu items. */
    private FlowPanel m_panel = new FlowPanel();

    /** The popup for a sub menu. */
    private CmsPopup m_popup = new CmsPopup();

    /** A Flag indicating if the position of the menu should be fixed. */
    private boolean m_isFixed;

    /**
     * Constructor.<p>
     * 
     * @param isFixed indicating if the position of the menu should be fixed.
     */
    public CmsContextMenu(boolean isFixed) {

        initWidget(m_panel);
        m_isFixed = isFixed;
        setStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().cmsMenuBar());
        Element e = m_popup.getDialog().getWidget().getParent().getElement();
        DOM.removeElementAttribute(e, "style");
    }

    /**
     * Adds a menu item to the bar.
     * 
     * @param item the item to be added
     * @return the {@link CmsContextMenuItem} object
     */
    public CmsContextMenuItem addItem(CmsContextMenuItem item) {

        m_panel.add(item);
        item.setParentMenu(this);
        return item;
    }

    /**
     * Adds a menu item with a sub menu to this menu.<p>
     * 
     * @param text the text for the menu item
     * @param subMenu the sub menu to set
     * 
     * @return the new context menu item
     */
    public CmsContextMenuItem addItem(String text, CmsContextMenu subMenu) {

        return addItem(new CmsContextMenuItem(text, subMenu));
    }

    /**
     * Adds a menu item with a command to this menu.<p>
     * 
     * @param text the text for the menu item
     * @param cmd the command for the menu item
     * 
     * @return the new context menu item
     */
    public CmsContextMenuItem addItem(String text, Command cmd) {

        return addItem(new CmsContextMenuItem(text, cmd));
    }

    /**
     * Adds a separator to this menu.<p> 
     */
    public void addSeparator() {

        CmsLabel sparator = new CmsLabel();
        sparator.setStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuItemSeparator());
        m_panel.add(sparator);
    }

    /**
     * Returns the selected item.<p>
     * 
     * @return the selected item
     */
    public CmsContextMenuItem getSelectedItem() {

        return m_selectedItem;
    }

    /**
     * Action on close.<p>
     */
    public void onClose() {

        if ((m_selectedItem != null) && m_selectedItem.hasSubmenu()) {
            m_selectedItem.getSubMenu().onClose();
            m_selectedItem.deselectItem();
        }
        m_popup.hide();
    }

    /**
     * Opens a sub menu and sets its position.<p>
     * 
     * @param item the item to show the sub menu of
     */
    public void openPopup(final CmsContextMenuItem item) {

        m_popup.add(item.getSubMenu());
        m_popup.addAutoHidePartner(item.getElement());
        m_popup.setModal(false);
        m_popup.getDialog().getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().cmsContextMenuPopup());
        m_popup.show();

        int leftSpace = item.getAbsoluteLeft() - Window.getScrollLeft() - 5;
        int rightSpace = Window.getClientWidth() - (item.getAbsoluteLeft() + item.getOffsetWidth() - 5);
        boolean showRight = item.getSubMenu().getOffsetWidth() < rightSpace;
        if (!showRight) {
            showRight = leftSpace < rightSpace;
        }

        int topSpace = item.getAbsoluteTop() - Window.getScrollTop() + 4;
        int bottomSpace = Window.getClientHeight() + Window.getScrollTop() - (item.getAbsoluteTop() + 4);
        boolean showBottom = item.getSubMenu().getOffsetHeight() < bottomSpace;
        if (!showBottom) {
            showBottom = topSpace < bottomSpace;
        }

        int left;
        int top;
        if (showRight && showBottom) {
            left = item.getAbsoluteLeft() + item.getOffsetWidth() - 4;
            top = item.getAbsoluteTop() - Window.getScrollTop() - 4;
        } else if (showRight && !showBottom) {
            left = item.getAbsoluteLeft() + item.getOffsetWidth() - 4;
            top = item.getAbsoluteTop()
                - Window.getScrollTop()
                - item.getSubMenu().getOffsetHeight()
                + item.getOffsetHeight()
                - 4;
        } else if (!showRight && showBottom) {
            left = item.getAbsoluteLeft() - Window.getScrollLeft() - item.getSubMenu().getOffsetWidth() - 3;
            top = item.getAbsoluteTop() - Window.getScrollTop() - 4;
        } else {
            left = item.getAbsoluteLeft() - Window.getScrollLeft() - item.getSubMenu().getOffsetWidth() - 3;
            top = item.getAbsoluteTop()
                - Window.getScrollTop()
                - item.getSubMenu().getOffsetHeight()
                + item.getOffsetHeight()
                - 4;
        }
        m_popup.setPosition(left, top);
        if (m_isFixed) {
            m_popup.getDialog().getElement().getStyle().setPosition(Position.FIXED);
        }
    }

    /**
     * Sets the selected item of this menu.<p>
     * 
     * @param selectedItem the item to select
     */
    public void setSelectedItem(CmsContextMenuItem selectedItem) {

        m_selectedItem = selectedItem;
    }

}
