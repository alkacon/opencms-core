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
 * A bean which represents a resource in the VFS.<p>
 *
 * @since 8.0.0
 */
public class CmsVfsEntryBean implements IsSerializable {

    /** True if the resource has children. */
    private boolean m_hasChildren;

    /** True if the resource is a folder. */
    private boolean m_isFolder;

    /** The name of the resource. */
    private String m_name;

    /** The path of the resource. */
    private String m_path;

    /** The resource type of the resource. */
    private String m_resourceType;

    /**
     * Constructs a new bean.<p>
     *
     * @param path the path of the resource
     * @param name the name of the resource
     * @param resourceType the resource type of the resource
     * @param isFolder true if the resource is a folder
     * @param hasChildren true if the resource is a folder which isn't empty
     */
    public CmsVfsEntryBean(String path, String name, String resourceType, boolean isFolder, boolean hasChildren) {

        m_isFolder = isFolder;
        m_path = path;
        m_name = name;
        m_resourceType = resourceType;
        m_hasChildren = hasChildren;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsVfsEntryBean() {

        // do nothing
    }

    /**
     * Returns the name of the resource.<p>
     *
     * @return the name of the resource
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the path of the resource.<p>
     *
     * @return the path of the resource
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the resource type.<p>
     *
     * @return the resource type
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns true if the resource has children, i.e. is a non-empty folder.<p>
     *
     * @return true if the resource has children
     */
    public boolean hasChildren() {

        return m_hasChildren;
    }

    /**
     * Returns true if the resource is a folder.<p>
     *
     * @return true if the resource is a folder
     */
    public boolean isFolder() {

        return m_isFolder;
    }

}
