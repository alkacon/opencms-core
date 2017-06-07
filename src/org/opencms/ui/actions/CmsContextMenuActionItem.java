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

package org.opencms.ui.actions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;
import java.util.Locale;

/**
 * A workplace action context menu item.<p>
 */
public class CmsContextMenuActionItem implements I_CmsContextMenuItem {

    /** The workplace action. */
    private I_CmsWorkplaceAction m_action;

    /** The order. */
    private float m_order;

    /** The parent item id. */
    private String m_parentId;

    /** The priority. */
    private int m_priority;

    /**
     * Creates a new instance.<p>
     *
     * @param action the action to execute
     * @param parentId the parent item id
     * @param order the order
     * @param priority the priority
     */
    public CmsContextMenuActionItem(I_CmsWorkplaceAction action, String parentId, float order, int priority) {
        m_parentId = parentId;
        m_order = order;
        m_priority = priority;
        m_action = action;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        CmsAppWorkplaceUi.get().disableGlobalShortcuts();
        m_action.executeAction(context);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getId()
     */
    public String getId() {

        return m_action.getId();
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getOrder()
     */
    public float getOrder() {

        return m_order;

    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getParentId()
     */
    public String getParentId() {

        return m_parentId;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getPriority()
     */
    public int getPriority() {

        return m_priority;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getTitle(java.util.Locale)
     */
    public String getTitle(Locale locale) {

        return m_action.getTitle();
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return m_action.getVisibility(cms, resources);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        return m_action.getVisibility(context);
    }

    /**
     * Returns the workplace action.<p>
     *
     * @return the workplace action
     */
    public I_CmsWorkplaceAction getWorkplaceAction() {

        return m_action;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#isLeafItem()
     */
    public boolean isLeafItem() {

        return true;
    }

}
