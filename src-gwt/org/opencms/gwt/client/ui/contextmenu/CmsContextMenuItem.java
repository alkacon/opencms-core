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

import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.HTML;

/**
 * An entry in a {@link org.opencms.gwt.client.ui.contextmenu.CmsContextMenu}. Menu items can either fire a
 * {@link com.google.gwt.user.client.Command} when they are clicked, or open a cascading sub-menu.<p>
 *
 * This implementation of the abstract context menu item provides a possible image in front of the text
 * and a arrow for a sub menu entry.<p>
 *
 * Furthermore constructs the HTML for such a menu entry.<p>
 *
 * @since version 8.0.0
 */
public final class CmsContextMenuItem extends A_CmsContextMenuItem {

    /** The command for this menu item. */
    private I_CmsContextMenuEntry m_entry;

    /** The panel containing the menu item text and optional the arrow and or a image in front of the text. */
    private HTML m_panel;

    /**
     * Constructs a context menu item.<p>
     *
     * @param entry the information for this item
     */
    public CmsContextMenuItem(I_CmsContextMenuEntry entry) {

        // call the super constructor
        super(entry.getLabel());
        m_entry = entry;

        // get the HTML for the menu item
        m_panel = new HTML(getMenuItemHtml(m_entry.hasSubMenu()));

        // initialize the widget with the panel and set the style name for the menu item
        initWidget(m_panel);
        setStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().cmsMenuItem());

        // now, if the widget is initialized, it's possible to set the item active or inactive,
        // because the mouse handlers for the item are added or removed
        if (!m_entry.isActive()) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_entry.getReason())) {
                setActive(m_entry.isActive(), m_entry.getReason());
            } else {
                setActive(m_entry.isActive(), "");
            }
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @Override
    public void onClick(ClickEvent event) {

        if (m_entry != null) {
            m_entry.execute();
        }
        // hide menu *after* executing the action, because hiding the menu may trigger mouseover events of the elements lying under it,
        // and executing the action first gives it the opportunity to add a 'blocking notification' to prevent this
        getParentMenu().hideAll();
    }

    /**
     * Generates the HTML for a menu item.<p>
     *
     * @param hasSubMenu signals if this menu has a sub menu
     *
     * @return the HTML for the menu item
     */
    protected String getMenuItemHtml(boolean hasSubMenu) {

        StringBuffer html = new StringBuffer();
        if (hasSubMenu) {
            // if this menu item has a sub menu show the arrow-icon behind the text of the icon
            html.append("<div class=\"");
            html.append(I_CmsLayoutBundle.INSTANCE.contextmenuCss().arrow()).append(" ").append(
                I_CmsButton.ICON_FONT).append(" ").append(I_CmsButton.TRIANGLE_RIGHT);
            html.append("\"></div>");
        }
        String iconClass = m_entry.getIconClass();
        if (iconClass == null) {
            iconClass = "";
        }
        String iconHtml = "<div class='" + iconClass + "'></div>";

        html.append(
            "<div class='" + I_CmsLayoutBundle.INSTANCE.contextmenuCss().iconBox() + "'>" + iconHtml + "</div>");
        // add the text to the item
        html.append("<div class=\"");
        html.append(I_CmsLayoutBundle.INSTANCE.contextmenuCss().label());
        html.append("\">" + getText() + "</div>");

        return html.toString();
    }
}
