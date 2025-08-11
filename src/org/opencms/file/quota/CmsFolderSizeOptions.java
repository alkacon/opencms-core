/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.quota;

/**
 * Options for reading folder size information.
 */
public class CmsFolderSizeOptions implements Cloneable {

    /** The root path for which to read folder size information. */
    private String m_rootPath;

    /** True if we want folder size information for all folders in the subtree. */
    private boolean m_tree;

    private boolean m_online;

    /**
     * Creates a new instance.
     *
     * @param rootPath the root path for which to read the folder size information
     * @param isTree true if we want folder size information for all folders in the subtree and not just the folder itself
     */
    public CmsFolderSizeOptions(String rootPath, boolean online, boolean isTree) {

        m_rootPath = rootPath;
        if (!m_rootPath.endsWith("/")) {
            m_rootPath += "/";
        }
        m_online = online;
        m_tree = isTree;
    }

    /**
     * Gets the root path for which we want folder size information.
     *
     * @return the root path for which we want folder size information
     */
    public String getRootPath() {

        return m_rootPath;
    }

    public boolean isOnline() {

        return m_online;
    }

    /**
     * True if we want to read folder size information for the complete subtree.
     *
     * @return true if we want to read folder size information for the complete subtree
     */
    public boolean isTree() {

        return m_tree;
    }

}
