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

package org.opencms.ui.contextmenu;

import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.Locale;

/**
 * Interface for generic context menu entris.<p>
 *
 * @param <T> the item data type
 */
public interface I_CmsSimpleContextMenuEntry<T> {

    /**
     * This interface allows special styles for certain entries.<p>
     */
    public interface I_HasCssStyles {

        /**
         * Returns the styles to use for this menu entry.<p>
         *
         * @return the styles to use for this menu entry
         */
        String getStyles();
    }

    /**
     * Executes the entry action.<p>
     *
     * @param context the item data
     */
    void executeAction(T context);

    /**
     * Returns the entry title for the given locale.<p>
     *
     * @param locale the locale
     *
     * @return the title
     */
    String getTitle(Locale locale);

    /**
     * Returns the entry visibility matching the given item data.<p>
     *
     * @param context the item data
     *
     * @return the visibility mode
     */
    CmsMenuItemVisibilityMode getVisibility(T context);
}
