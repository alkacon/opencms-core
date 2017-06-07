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
import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;
import java.util.Locale;

/**
 * Menu item which acts only as a container for nested menu items.<p>
 */
public class CmsSubmenu implements I_CmsContextMenuItem {

    /** The item id. */
    protected String m_id;

    /** The parent item id. */
    protected String m_parentId;

    /** The order. */
    protected float m_order;

    /** The priority. */
    protected int m_priority;

    /** The title (may contain localization macros). */
    private String m_title;

    /**
     * Creates a new instance.<p>
     *
     * @param id the id
     * @param parentId the parent id
     * @param title the title
     * @param order the order
     * @param priority the priority
     */
    public CmsSubmenu(String id, String parentId, String title, float order, int priority) {
        m_id = id;
        m_parentId = parentId;
        m_title = title;
        m_order = order;
        m_priority = priority;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    @Override
    public void executeAction(I_CmsDialogContext context) {
        // do nothing
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getId()
     */
    public String getId() {

        return m_id;
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

        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setMessages(OpenCms.getWorkplaceManager().getMessages(locale));
        if (m_title == null) {
            return "";
        }
        return resolver.resolveMacros(m_title);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#isLeafItem()
     */
    public boolean isLeafItem() {

        return false;
    }
}
