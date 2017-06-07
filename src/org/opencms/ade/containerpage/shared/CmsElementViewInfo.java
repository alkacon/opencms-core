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

package org.opencms.ade.containerpage.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Element view info.<p>
 */
public class CmsElementViewInfo implements IsSerializable {

    /** The element view id. */
    private CmsUUID m_elementViewId;

    /** The parent view (may be null). */
    private CmsElementViewInfo m_parent;

    /** The title. */
    private String m_title;

    /**
     * Constructor.<p>
     *
     * @param title the title
     * @param elementViewId the element view id
     */
    public CmsElementViewInfo(String title, CmsUUID elementViewId) {

        m_title = title;
        m_elementViewId = elementViewId;
    }

    /**
     * Constructor, for serialization only.<p>
     */
    protected CmsElementViewInfo() {

    }

    /**
     * Returns the element view id.<p>
     *
     * @return the element view id
     */
    public CmsUUID getElementViewId() {

        return m_elementViewId;
    }

    /**
     * Gets the id of the root view of this view.<p>
     *
     * The root view is either this view itself if it doesn't have a parent view, or the parent view if it does.
     *
     * @return the root view
     */
    public CmsUUID getRootViewId() {

        if (m_parent != null) {
            return m_parent.getElementViewId();
        } else {
            return getElementViewId();
        }
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns true if this is a root view.<p>
     *
     * @return true if this is a root view
     */
    public boolean isRoot() {

        return m_parent == null;
    }

    /**
     * Sets the parent view bean.<p>
     *
     * @param parent the parent view bean
     */
    public void setParent(CmsElementViewInfo parent) {

        m_parent = parent;
    }

}
