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

package org.opencms.ade.containerpage.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Element view info.<p>
 */
public class CmsElementViewInfo implements IsSerializable {

    /** The element view id. */
    private CmsUUID m_elementViewId;

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
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

}
