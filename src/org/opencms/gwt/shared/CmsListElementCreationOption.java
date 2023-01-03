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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a single option (type) that can be selected in the list element creation dialog.
 */
public class CmsListElementCreationOption implements IsSerializable {

    /** The list info bean describing the type. */
    private CmsListInfoBean m_info;

    /** The string used to create the new resource by the content editor. */
    private String m_newLink;

    /** The type name. */
    private String m_type;

    /**
     * Creates a new instance.
     *
     * @param type the type name
     * @param info the list info bean describing the type
     * @param newLink the string used by the content editor to create the new resource
     */
    public CmsListElementCreationOption(String type, CmsListInfoBean info, String newLink) {

        super();
        m_type = type;
        m_info = info;
        m_newLink = newLink;
    }

    /**
     * Hidden default constructor.
     */
    protected CmsListElementCreationOption() {}

    /**
     * Gets the list info bean representing the type.
     *
     * @return the list info bean for the type
     */
    public CmsListInfoBean getInfo() {

        return m_info;
    }

    /**
     * Gets the string used by the content editor to create the new resource.
     *
     * @return the string used by the content editor to create the new resource
     */
    public String getNewLink() {

        return m_newLink;
    }

    /**
     * Gets the resource type name.
     *
     * @return the type name
     */
    public String getType() {

        return m_type;
    }

}
