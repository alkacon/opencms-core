/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsContextMenuItem.java,v $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HTML;

/**
 * An entry in a {@link org.opencms.gwt.client.ui.CmsContextMenu}. Menu items can either fire a
 * {@link com.google.gwt.user.client.Command} when they are clicked, or open a cascading sub-menu.<p>
 * 
 * This implementation of the abstract context menu item provides a possible image in front of the text
 * and a arrow for a sub menu entry.<p>
 * 
 * Furthermore constructs the HTML for such a menu entry.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.3 $
 * 
 * @since version 8.0.0
 */
public final class CmsContextMenuItem extends A_CmsContextMenuItem {

    /** The panel containing the menu item text and optional the arrow and or a image in front of the text. */
    protected HTML m_panel;

    /** The command for this menu item. */
    private Command m_command;

    /** The image class. */
    private String m_imageClass;

    /** The image path. */
    private String m_imagePath;

    /**
     * @param entry
     */
    public CmsContextMenuItem(I_CmsContextMenuEntry entry) {

        super(entry.getLabel());
        m_panel = new HTML(getMenuItemHtml(entry.hasSubMenu()));
        initWidget(m_panel);
        setStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().cmsMenuItem());

        setImagePath(entry.getImagePath());
        setImageClass(entry.getImageClass());
        setCommand(entry.getCommand());

        if (!entry.isActive()) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(entry.getReason())) {
                setEnabled(entry.isActive(), entry.getReason());
            } else {
                setEnabled(entry.isActive(), "");
            }
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @Override
    public void onClick(ClickEvent event) {

        if (m_command != null) {
            m_command.execute();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsContextMenuItem#getMenuItemHtml(boolean)
     */
    @Override
    protected String getMenuItemHtml(boolean hasSubMenu) {

        StringBuffer html = new StringBuffer();
        if (hasSubMenu) {
            // if this menu item has a sub menu show the arrow-icon behind the text of the icon
            html.append("<div class=\"");
            html.append(I_CmsLayoutBundle.INSTANCE.contextmenuCss().arrow()
                + " "
                + I_CmsLayoutBundle.INSTANCE.iconsCss().uiIcon()
                + " "
                + I_CmsButton.UiIcon.triangle_1_e.name());
            html.append("\"></div>");
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_imageClass)) {
            // if an image class is set to the menu item show the image in front of the text
            html.append("<div class=\"");
            html.append(m_imageClass);
            html.append("\"></div>");
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_imagePath)) {
            // if an image path is set to the menu item show the image in front of the text
            html.append("<div class=\"");
            html.append(I_CmsLayoutBundle.INSTANCE.contextmenuCss().image());
            html.append("\" ");
            html.append("style=\"background: transparent url('" + m_imagePath + "') no-repeat scroll 0 0\"");
            html.append("\"");
            html.append("></div>");
        }
        // add the text to the item
        html.append("<div class=\"");
        html.append(I_CmsLayoutBundle.INSTANCE.contextmenuCss().label());
        html.append("\">" + getText() + "</div>");

        return html.toString();
    }

    /**
     * Sets the command.<p>
     * 
     * @param command the command to set
     */
    private void setCommand(Command command) {

        m_command = command;

    }

    /**
     * Sets the image class.<p>
     * 
     * @param imageClass the image class to set
     */
    private void setImageClass(String imageClass) {

        m_imageClass = imageClass;
    }

    /**
     * Sets the image path.<p>
     * 
     * @param imagePath the path to set
     */
    private void setImagePath(String imagePath) {

        m_imagePath = imagePath;
    }
}
