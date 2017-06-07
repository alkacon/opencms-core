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

import java.util.List;

/**
 * Interface for a context menu entry.<p>
 *
 * @since version 8.0.0
 */
public interface I_CmsContextMenuEntry {

    /**
     * Executes the context menu action.<p>
     */
    void execute();

    /**
     * Generates the context menu item.<p>
     *
     * @return the context menu item
     */
    A_CmsContextMenuItem generateMenuItem();

    /**
     * Returns a CSS class which should be used to display an icon, or null if no icon is required.<p>
     *
     * @return the CSS class for the icon
     */
    String getIconClass();

    /**
     * Returns the JSP path for the command generation.<p>
     *
     * @return the JSP path
     */
    String getJspPath();

    /**
     * Returns the label (text) for the menu entry.<p>
     *
     * @return the label
     */
    String getLabel();

    /**
     * Returns the name of the entry.<p>
     *
     * @return the name of the entry
     */
    String getName();

    /**
     * Returns the reason if the entry is de-activated .<p>
     *
     * @return the reason
     */
    String getReason();

    /**
     * Returns a list of {@link I_CmsContextMenuEntry} objects.<p>
     *
     * @return the sub menu entries
     */
    List<I_CmsContextMenuEntry> getSubMenu();

    /**
     * Returns <code>true</code> if this menu entry has a sub menu <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this menu entry has a sub menu <code>false</code> otherwise
     */
    boolean hasSubMenu();

    /**
     * Returns <code>true</code> if this menu entry is active <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this menu entry is active <code>false</code> otherwise
     */
    boolean isActive();

    /**
     * Returns <code>true</code> if this menu entry is a separator <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this menu entry is a separator <code>false</code> otherwise
     */
    boolean isSeparator();

    /**
     * Returns <code>true</code> if this menu entry is visible <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if this menu entry is visible <code>false</code> otherwise
     */
    boolean isVisible();

}