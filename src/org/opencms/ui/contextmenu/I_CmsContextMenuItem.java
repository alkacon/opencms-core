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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;
import java.util.Locale;

/**
 * Interface for workplace context menu items.<p>
 *
 */
public interface I_CmsContextMenuItem extends I_CmsHasMenuItemVisibility, I_CmsContextMenuAction {

    /**
     * Executes the context menu action given a dialog context.<p>
     *
     * @param context the dialog context
     */
    void executeAction(I_CmsDialogContext context);

    /**
     * Gets the id.<p>
     *
     * The id does not need to be unique among all context menu items which are in use in the system, but
     * when multiple menu items with the same id are available for a given context menu, only one of them
     * will be picked, based on the priority (a higher priority context menu item will be preferred to one with
     * a lower priority.<p>
     *
     * @return the id
     */
    String getId();

    /**
     * Integer attribute which is used to order menu items.<p>
     *
     * Items with a higher 'order' value will appear after items with a lower order at the same tree level.<p>
     *
     * @return the order
     */
    float getOrder();

    /**
     * Gets the id of the parent entry.<p>
     *
     * If this returns null, the context menu item will be inserted at the root level of the context menu,
     * otherwise, it will be added I_CmsContextMenuActionas a sub-entry of the context menu entry with the given id (if such an
     * entry exists; otherwise, the child entry will be ignored).
     *
     * @return the parent id
     */
    String getParentId();

    /**
     * Gets the priority.<p>
     *
     * If multiple context menu items with the same id are available for a menu, the one with the highest priority will be picked.<p>
     *
     * @return the priority
     */
    int getPriority();

    /**
     * Gets the title.<p>
     *
     * @param locale the locale
     *
     * @return the title
     */
    String getTitle(Locale locale);

    /**
     * Computes the visibility for this context menu items with the given CMS context and resources.<p>
     *
     * @param cms the current CMS context
     * @param resources the resources for which the context menu is being opened
     *
     * @return the visibility of the context menu item
     */
    CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources);

    /**
     * Returns true if this is a leaf item, i.e. an item which has no child items.<p>
     *
     * @return true if this is a leaf item
     */
    boolean isLeafItem();
}
