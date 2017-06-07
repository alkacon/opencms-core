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

import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * A implementation for a context menu.<p>
 *
 * @since version 8.0.0
 */
public class CmsContextMenu extends Composite implements ResizeHandler, I_CmsAutoHider {

    /** The menu auto hide parent. */
    private I_CmsAutoHider m_autoHideParent;

    /** A Flag indicating if the position of the menu should be fixed. */
    private boolean m_isFixed;

    /** The panel for the menu items. */
    private FlowPanel m_panel = new FlowPanel();

    /** The parent item. */
    private A_CmsContextMenuItem m_parentItem;

    /** The popup for a sub menu. */
    private CmsPopup m_popup;

    /** Stores the selected item. */
    private A_CmsContextMenuItem m_selectedItem;

    /**
     * Constructor.<p>
     *
     * @param menuData the data structure for the context menu
     * @param isFixed indicating if the position of the menu should be fixed.
     * @param autoHideParent the menu auto hide parent
     */
    public CmsContextMenu(List<I_CmsContextMenuEntry> menuData, boolean isFixed, I_CmsAutoHider autoHideParent) {

        initWidget(m_panel);
        m_isFixed = isFixed;
        m_popup = new CmsPopup();
        // clear the width and the padding of the popup content (needed for sub menus)
        m_popup.setWidth(0);
        m_popup.removePadding();
        m_popup.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().contextMenu());
        createContextMenu(menuData);
        setStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().cmsMenuBar());
        m_autoHideParent = autoHideParent;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsAutoHider#addAutoHidePartner(com.google.gwt.dom.client.Element)
     */
    public void addAutoHidePartner(com.google.gwt.dom.client.Element partner) {

        m_autoHideParent.addAutoHidePartner(partner);
    }

    /**
     * Adds a menu item to this menu.
     *
     * @param item the item to be added
     */
    public void addItem(A_CmsContextMenuItem item) {

        m_panel.add(item);
        item.setParentMenu(this);
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
     * @see org.opencms.gwt.client.ui.I_CmsAutoHider#hide()
     */
    public void hide() {

        m_autoHideParent.hide();
    }

    /**
     * Hides this menu and all its parent menus.<p>
     */
    public void hideAll() {

        CmsContextMenu currentMenu = this;
        int i = 0;
        while ((currentMenu != null) && (i < 10)) {
            currentMenu.hide();
            currentMenu = currentMenu.getParentMenu();
            i += 1;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsAutoHider#isAutoHideEnabled()
     */
    public boolean isAutoHideEnabled() {

        return m_autoHideParent.isAutoHideEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsAutoHider#isAutoHideOnHistoryEventsEnabled()
     */
    public boolean isAutoHideOnHistoryEventsEnabled() {

        return m_autoHideParent.isAutoHideOnHistoryEventsEnabled();
    }

    /**
     * If the browser's window is resized this method rearranges the sub menus of the selected item.<p>
     *
     * @see com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google.gwt.event.logical.shared.ResizeEvent)
     */
    public void onResize(final ResizeEvent event) {

        if ((m_selectedItem != null) && m_selectedItem.hasSubmenu()) {

            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    getSelectedItem().getSubMenu().setSubMenuPosition(getSelectedItem());
                    getSelectedItem().getSubMenu().onResize(event);
                }
            });
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsAutoHider#removeAutoHidePartner(com.google.gwt.dom.client.Element)
     */
    public void removeAutoHidePartner(com.google.gwt.dom.client.Element partner) {

        m_autoHideParent.removeAutoHidePartner(partner);

    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsAutoHider#setAutoHideEnabled(boolean)
     */
    public void setAutoHideEnabled(boolean autoHide) {

        m_autoHideParent.setAutoHideEnabled(autoHide);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsAutoHider#setAutoHideOnHistoryEventsEnabled(boolean)
     */
    public void setAutoHideOnHistoryEventsEnabled(boolean enabled) {

        m_autoHideParent.setAutoHideOnHistoryEventsEnabled(enabled);
    }

    /**
     * Sets the parent item.<p>
     *
     * @param parentItem the parent item
     */
    public void setParentItem(A_CmsContextMenuItem parentItem) {

        m_parentItem = parentItem;
    }

    /**
     * Returns the selected item of this menu.<p>
     *
     * @return the selected item of this menu
     */
    protected A_CmsContextMenuItem getSelectedItem() {

        return m_selectedItem;
    }

    /**
     * Action on close.<p>
     *
     * On close all sub menus should be hidden, the currently selected item should be deselected
     * and the popup will be closed.<p>
     */
    protected void onClose() {

        if ((m_selectedItem != null)) {
            if (m_selectedItem.hasSubmenu()) {
                m_selectedItem.getSubMenu().onClose();
            }
            m_selectedItem.deselectItem();
        }
        m_popup.hide();
    }

    /**
     * Opens a sub menu and sets its position.<p>
     *
     * @param item the item to show the sub menu of
     */
    protected void openPopup(final A_CmsContextMenuItem item) {

        m_popup.add(item.getSubMenu());
        m_popup.addAutoHidePartner(item.getElement());
        m_popup.setModal(false);
        m_popup.show();

        setSubMenuPosition(item);

        if (m_isFixed) {
            m_popup.setPositionFixed();
        }
    }

    /**
     * Sets the selected item of this menu.<p>
     *
     * @param selectedItem the item to select
     */
    protected void setSelectedItem(A_CmsContextMenuItem selectedItem) {

        m_selectedItem = selectedItem;
    }

    /**
     * Sets the position of the sub menu popup.<p>
     *
     * First calculates the best space where to show the popup.<p>
     *
     * The following list shows the possibilities, beginning
     * with the best and ending with the worst.<p>
     *
     * <ul>
     * <li>bottom-right</li>
     * <li>bottom-left</li>
     * <li>top-right</li>
     * <li>top-left</li>
     * </ul>
     *
     * Then the position (top and left coordinate) are calculated.<p>
     *
     * Finally the position of the sub menu popup is set to the calculated values.<p>
     *
     * @param item the item to show the sub menu of
     */
    protected void setSubMenuPosition(final A_CmsContextMenuItem item) {

        int scrollLeft = Window.getScrollLeft();
        int scrollTop = Window.getScrollTop();

        // calculate the left space
        // add 10 because of the shadow and for avoiding that the browser's right window border touches the sub menu
        int leftSpace = item.getAbsoluteLeft() - (scrollLeft + 10);
        // calculate the right space
        // add 10 because of the shadow and for avoiding that the browser's left window border touches the sub menu
        int rightSpace = Window.getClientWidth() - (item.getAbsoluteLeft() + item.getOffsetWidth() + 10);
        // if the width of the sub menu is smaller than the right space, show the sub menu on the right
        boolean showRight = item.getSubMenu().getOffsetWidth() < rightSpace;
        if (!showRight) {
            // if the width of the sub menu is larger than the right space, compare the left space with the right space
            // and show the sub menu on the right if on the right is more space than on the left
            showRight = leftSpace < rightSpace;
        }

        // calculate the top space
        // add 10 because of the shadow and for avoiding that the browser's top window border touches the sub menu
        int topSpace = (item.getAbsoluteTop() - scrollTop) + 10;
        // calculate the bottom space
        // add 10 because of the shadow and for avoiding that the browser's bottom window border touches the sub menu
        int bottomSpace = (Window.getClientHeight() + scrollTop) - (item.getAbsoluteTop() + 10);
        // if the height of the sub menu is smaller than the bottom space, show the sub menu on the bottom
        boolean showBottom = item.getSubMenu().getOffsetHeight() < bottomSpace;
        if (!showBottom) {
            // if the height of the sub menu is larger than the bottom space, compare the top space with
            // the bottom space and show the sub menu on the bottom if on the bottom is more space than on the top
            showBottom = topSpace < bottomSpace;
        }

        int left;
        int top;

        if (showBottom) {
            top = item.getAbsoluteTop() - 4;
        } else {
            top = ((item.getAbsoluteTop() - item.getSubMenu().getOffsetHeight()) + item.getOffsetHeight()) - 4;
        }

        if (showRight) {
            left = (item.getAbsoluteLeft() + item.getOffsetWidth()) - 4;
        } else {
            left = item.getAbsoluteLeft() - item.getSubMenu().getOffsetWidth() - 4;
        }

        // in case of fixed popup position, subtract the scroll position
        if (m_isFixed) {
            left -= scrollLeft;
            top -= scrollTop;
        }

        // finally set the position of the popup
        m_popup.setPopupPosition(left, top);
    }

    /**
     * Gets the parent menu.<p>
     *
     * @return the parent menu
     */
    CmsContextMenu getParentMenu() {

        if (m_parentItem != null) {
            return m_parentItem.getParentMenu();
        } else {
            return null;
        }
    }

    /**
     * Creates the context menu.<p>
     *
     * @param entries a list with all entries for the context menu
     */
    private void createContextMenu(List<I_CmsContextMenuEntry> entries) {

        Iterator<I_CmsContextMenuEntry> it = entries.iterator();
        while (it.hasNext()) {
            I_CmsContextMenuEntry entry = it.next();
            if (!entry.isVisible()) {
                continue;
            }
            if (entry.isSeparator()) {
                addSeparator();
            } else {
                A_CmsContextMenuItem item = entry.generateMenuItem();
                if (entry.hasSubMenu()) {
                    CmsContextMenu submenu = new CmsContextMenu(entry.getSubMenu(), m_isFixed, m_popup);
                    item.setSubMenu(submenu);
                    addItem(item);
                } else {
                    addItem(item);
                }
            }

        }
    }
}
