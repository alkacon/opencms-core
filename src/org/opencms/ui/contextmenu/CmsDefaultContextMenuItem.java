/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;

/**
 * Base class for leaf context menu items.<p>
 */
public class CmsDefaultContextMenuItem implements I_CmsContextMenuItem {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultContextMenuItem.class);

    /** The item id. */
    protected String m_id;

    /** The parent item id. */
    protected String m_parentId;

    /** The order. */
    protected int m_order;

    /** The priority. */
    protected int m_priority;

    /** The title (may contain localization macros). */
    private String m_title;

    /** The action to execute when the item is clicked. */
    protected I_CmsContextMenuAction m_action;

    /** The visibility check for this item. */
    protected I_CmsHasMenuItemVisibility m_visibility;

    /** Default global id. */
    private String m_globalId = "" + new CmsUUID();

    /**
     * Creates a new instance.<p>
     *
     * @param id the id
     * @param parentId the parent item id
     * @param action the action to execute
     * @param title the title (may contain localization macros)
     * @param order the order
     * @param priority the priority
     * @param visibility the object used to check visibility
     */
    public CmsDefaultContextMenuItem(
        String id,
        String parentId,
        I_CmsContextMenuAction action,

        String title,
        int order,
        int priority,
        I_CmsHasMenuItemVisibility visibility) {
        m_id = id;
        m_parentId = parentId;
        m_title = title;
        m_order = order;
        m_priority = priority;
        m_action = action;
        m_visibility = visibility;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        if (m_action != null) {
            m_action.executeAction(context);
        } else {
            LOG.warn("Empty action in context menu item " + m_id + " . Configuration error?");
        }
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getClientAction()
     */
    public String getClientAction() {

        return null;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getGlobalId()
     */
    public String getGlobalId() {

        return m_globalId;
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
    public int getOrder() {

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

        if (m_visibility == null) {
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }
        return m_visibility.getVisibility(cms, resources);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#isLeafItem()
     */
    public boolean isLeafItem() {

        return true;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#setGlobalId(java.lang.String)
     */
    public void setGlobalId(String globalId) {

        m_globalId = globalId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }

}
