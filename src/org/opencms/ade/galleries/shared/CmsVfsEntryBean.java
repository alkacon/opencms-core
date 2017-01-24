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

import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a single VFS resource entry for use by the VFS tab of the galleries.<p>
 */
public class CmsVfsEntryBean implements IsSerializable, I_CmsGalleryTreeEntry<CmsVfsEntryBean> {

    /** Flag to indicate if the user has write permissions to the folder. */
    private boolean m_editable;

    /** Flag indicating whether this is entry should be displayed at the top level of the tree. */
    private boolean m_isRoot;

    /** True if this is a filter search match. */
    private boolean m_isSearchMatch;

    /** The list of children. */
    private List<CmsVfsEntryBean> m_preloadedChildren;

    /** The root path of the VFS entry. */
    private String m_rootPath;

    /** The site root of the entry. */
    private String m_siteRoot;

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The folder title. */
    private String m_title;

    /**
     * Creates a new VFS entry bean.<p>
     *
     * @param rootPath the root path
     * @param structureId the structure id
     * @param title the folder title
     * @param isRoot flag indicating whether this is entry should be displayed at the top level of the tree
     * @param editable <code>true</code> if the user has write permissions to the folder
     * @param preloadedChildren the preloaded child nodes
     * @param isMatch true if this entry bean is a search match for the filter string the user entered
     */
    public CmsVfsEntryBean(
        String rootPath,
        CmsUUID structureId,
        String title,
        boolean isRoot,
        boolean editable,
        List<CmsVfsEntryBean> preloadedChildren,
        boolean isMatch) {

        m_rootPath = rootPath;

        m_structureId = structureId;
        m_isRoot = isRoot;
        m_editable = editable;
        m_title = title;
        m_isSearchMatch = isMatch;
        m_preloadedChildren = preloadedChildren;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsVfsEntryBean() {

        // do nothing
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryTreeEntry#addChild(java.lang.Object)
     */
    public void addChild(CmsVfsEntryBean child) {

        if (m_preloadedChildren == null) {
            m_preloadedChildren = new ArrayList<CmsVfsEntryBean>();
        }
        m_preloadedChildren.add(child);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryTreeEntry#getChildren()
     */
    public List<CmsVfsEntryBean> getChildren() {

        return m_preloadedChildren;
    }

    /**
     * Gets the name which should be displayed in the widget representing this VFS entry.<p>
     *
     * @return the name to display
     */
    public String getDisplayName() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_title)) {
            return m_title;
        }
        if (m_isRoot) {
            return getRootPath();
        } else {
            String fixedPath = getRootPath().replaceFirst("/$", "");
            int lastSlash = fixedPath.lastIndexOf('/');
            if (lastSlash == -1) {
                return fixedPath;
            }
            return fixedPath.substring(lastSlash + 1);
        }
    }

    /**
     * Gets the root path of the VFS entry.<p>
     *
     * @return the root path of the VFS entry
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Gets the site root of this tree entry.<p>
     *
     * @return the site root of this entry
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the structure id.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the editable flag. Indicate if the user has write permissions to the folder.<p>
     *
     * @return the editable flag
     */
    public boolean isEditable() {

        return m_editable;
    }

    /**
     * Returns true if this entry is a top-level entry.<p>
     *
     * @return true if this is a top-level entry
     */
    public boolean isRoot() {

        return m_isRoot;
    }

    /**
     * Returns true if this entry bean is a search match.<p>
     *
     * @return true if this is a search match
     */
    public boolean isSearchMatch() {

        return m_isSearchMatch;
    }

    /**
     * Sets the list of children.<p>
     *
     * @param children the list of children
     */
    public void setChildren(List<CmsVfsEntryBean> children) {

        m_preloadedChildren = children;
    }

    /**
     * Sets if the user has write permissions to the folder.<p>
     *
     * @param editable <code>true</code> if the user has write permissions to the folder
     */
    public void setEditable(boolean editable) {

        m_editable = editable;
    }

    /**
     * Sets the site root of this tree entry.<p>
     *
     * @param siteRoot the site root of this tree entry
     */
    public void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }

}
