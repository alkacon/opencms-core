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

import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A sitemap entry bean.<p>
 */
public class CmsSitemapEntryBean implements IsSerializable, I_CmsGalleryTreeEntry<CmsSitemapEntryBean> {

    /** The entry children. */
    private List<CmsSitemapEntryBean> m_children;

    /** The is folder flag. */
    private boolean m_isFolder;

    /** The hidden entry flag. */
    private boolean m_isHiddenEntry;

    /** Flag which indicates whether this bean belongs to a navigation level. */
    private boolean m_isNavLevel;

    /** Flag indicating whether this is entry should be displayed at the top level of the tree. */
    private boolean m_isRoot;

    /** True if this is a search match. */
    private boolean m_isSearchMatch;

    /** The root path. */
    private String m_rootPath;

    /** The site path of this VFS entry. */
    private String m_sitePath;

    /** The site root of the site to which this entry bean belongs. */
    private String m_siteRoot;

    /** The entry id. */
    private CmsUUID m_structureId;

    /** The folder title. */
    private String m_title;

    /** The resource type. */
    private String m_type;

    /**
     * Constructor.<p>
     *
     * @param rootPath the root path
     * @param sitePath the site path
     * @param structureId the entry id
     * @param title the title
     * @param type the resource type
     * @param isFolder <code>true</code> if this entry represents a folder
     * @param isRoot <code>true</code> if this is a site root entry
     * @param isHiddenEntry <code>true</code> if this is a hidden entry
     * @param isNavLevel <code>true</code> if this is a navigation level
     *
     */
    public CmsSitemapEntryBean(
        String rootPath,
        String sitePath,
        CmsUUID structureId,
        String title,
        String type,
        boolean isFolder,
        boolean isRoot,
        boolean isHiddenEntry,
        boolean isNavLevel) {

        m_rootPath = rootPath;
        m_sitePath = sitePath;
        m_structureId = structureId;
        m_title = title;
        m_type = type;
        m_isFolder = isFolder;
        m_isRoot = isRoot;
        m_isHiddenEntry = isHiddenEntry;
        m_isNavLevel = isNavLevel;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsSitemapEntryBean() {

        // nothing to do
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryTreeEntry#addChild(java.lang.Object)
     */
    public void addChild(CmsSitemapEntryBean child) {

        if (m_children == null) {
            m_children = new ArrayList<CmsSitemapEntryBean>();
        }
        m_children.add(child);
    }

    /**
     * Returns the children of this entry or <code>null</code> if not loaded.<p>
     *
     * @return the children of the entry
     */
    public List<CmsSitemapEntryBean> getChildren() {

        return m_children;
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
            return m_sitePath;
        } else {
            String fixedPath = m_sitePath.replaceFirst("/$", "");
            int lastSlash = fixedPath.lastIndexOf('/');
            if (lastSlash == -1) {
                return fixedPath;
            }
            return fixedPath.substring(lastSlash + 1);
        }
    }

    /**
     * Returns the type name used to determine the icon (may be different from the actual type.<p>
     *
     * @return the type name used for the icon
     */
    public String getImageType() {

        if (m_isNavLevel) {
            return CmsGwtConstants.TYPE_NAVLEVEL;
        } else {
            return getType();
        }
    }

    /**
     * Gets the root path of the sitemap entry.<p>
     *
     * @return the root path of the sitemap entry
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Returns the site path of this VFS tree.
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Gets the site root.<p>
     *
     * @return the site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the entry structure id.<p>
     *
     * @return the entry structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns if the children of this entry have been loaded.<p>
     *
     * @return <code>true</code> if the children of this entry have been loaded
     */
    public boolean hasChildren() {

        return m_children != null;
    }

    /**
     * Returns the isFolder.<p>
     *
     * @return the isFolder
     */
    public boolean isFolder() {

        return m_isFolder;
    }

    /**
     * Returns if this is a hidden entry.<p>
     *
     * @return <code>true</code> if this is a hidden entry
     */
    public boolean isHiddenEntry() {

        return m_isHiddenEntry;
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
     * Returns true if this is a search match.<p>
     *
     * @return true if this is a search match
     */
    public boolean isSearchMatch() {

        return m_isSearchMatch;
    }

    /**
     * Sets the children of this entry.<p>
     *
     * @param children the children
     */
    public void setChildren(List<CmsSitemapEntryBean> children) {

        m_children = children;
    }

    /**
     * Marks this entry as a search match.<p>
     *
     * @param isSearchMatch true if this is a search match
     */
    public void setSearchMatch(boolean isSearchMatch) {

        m_isSearchMatch = isSearchMatch;
    }

    /**
     * Sets the site root for this bean.<p>
     *
     * @param siteRoot the site root
     */
    public void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "CmsSitemapEntryBean(rootpath=" + m_rootPath + ",sitepath=" + m_sitePath + ")";
    }

}
