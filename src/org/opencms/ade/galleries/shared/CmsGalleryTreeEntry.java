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

package org.opencms.ade.galleries.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Gallery tree entry class. To organize gallery folders as a tree.<p>
 *
 * @since 8.0.1
 */
public class CmsGalleryTreeEntry extends CmsGalleryFolderBean {

    /** List of child entries. */
    private List<CmsGalleryTreeEntry> m_children;

    /** The parent entry. */
    private CmsGalleryTreeEntry m_parent;

    /**
     * Constructor.<p>
     * Copy the fields of the given master.<p>
     *
     * @param master master to copy
     */
    public CmsGalleryTreeEntry(CmsGalleryFolderBean master) {

        setContentTypes(master.getContentTypes());
        setEditable(master.isEditable());
        setPath(master.getPath());
        setTitle(master.getTitle());
        setType(master.getType());
    }

    /**
     * Adds a new child entry.<p>
     *
     * @param child the child entry to add
     */
    public void addChild(CmsGalleryTreeEntry child) {

        if (m_children == null) {
            m_children = new ArrayList<CmsGalleryTreeEntry>();
        }
        m_children.add(child);
        child.setParent(this);
    }

    /**
     * Returns the list of child entries.<p>
     *
     * @return the list of child entries
     */
    public List<CmsGalleryTreeEntry> getChildren() {

        return m_children;
    }

    /**
     * Returns the parent entry or <code>null</code> if there is none.<p>
     *
     * @return the parent entry
     */
    public CmsGalleryTreeEntry getParent() {

        return m_parent;
    }

    /**
     * Sets the child entry list.<p>
     *
     * @param children the list of child entries
     */
    public void setChildren(List<CmsGalleryTreeEntry> children) {

        m_children = children;
        for (CmsGalleryTreeEntry child : m_children) {
            child.setParent(this);
        }
    }

    /**
     * Sets the parent entry.<p>
     *
     * @param parent the parent entry
     */
    protected void setParent(CmsGalleryTreeEntry parent) {

        m_parent = parent;
    }
}
