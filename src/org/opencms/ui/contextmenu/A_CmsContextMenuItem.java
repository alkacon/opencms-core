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
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

/**
 * Abstract base class for context menu items.<p>
 */
public abstract class A_CmsContextMenuItem implements I_CmsContextMenuItem {

    protected String m_id;
    protected String m_parentId;
    protected int m_order;
    protected int m_priority;
    private String m_title;

    /** Default global id. */
    private String m_globalId = "" + new CmsUUID();

    public A_CmsContextMenuItem(String id, String parentId, String title, int order, int priority) {
        m_id = id;
        m_parentId = parentId;
        m_title = title;
        m_order = order;
        m_priority = priority;
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
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getTitle()
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
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

}
