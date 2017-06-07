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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;

/**
 * Helper class to create "virtual" resources not existing in the vfs which are
 * based on existing resources.<p>
 *
 * It is not possible to change a {@link CmsResource} instance. This helper class
 * clones a <code>CmsResource</code> and can change some attributes of the
 * <code>CmsResource</code> like the path, the typeId or the length.<p>
 *
 * @since 6.2.4
 */
public class CmsWrappedResource {

    /** The resource this virtual resources is based on. */
    private CmsResource m_base;

    /** Indicates if the virtual resource is a folder or not. */
    private boolean m_isFolder;

    /** The size of the content of the virtual resource. */
    private int m_length;

    /** The root path of the virtual resource. */
    private String m_rootPath;

    /** The type id of the virtual resource. */
    private int m_typeId;

    /**
     * Creates a new virtual resource.<p>
     *
     * @param res the resource this virtual resource is based on
     */
    public CmsWrappedResource(CmsResource res) {

        m_base = res;

        m_rootPath = res.getRootPath();
        m_typeId = res.getTypeId();
        m_isFolder = res.isFolder();
        m_length = res.getLength();
    }

    /**
     * Returns the virtual resource as a file.<p>
     *
     * @return the virtual resource as a file
     */
    public CmsFile getFile() {

        if (m_base instanceof CmsFile) {
            CmsFile file = (CmsFile)m_base;

            return new CmsFile(
                file.getStructureId(),
                file.getResourceId(),
                m_rootPath,
                m_typeId,
                file.getFlags(),
                file.getProjectLastModified(),
                file.getState(),
                file.getDateCreated(),
                file.getUserCreated(),
                file.getDateLastModified(),
                file.getUserLastModified(),
                file.getDateReleased(),
                file.getDateExpired(),
                file.getSiblingCount(),
                file.getLength(),
                file.getDateContent(),
                file.getVersion(),
                file.getContents());
        }

        return new CmsFile(getResource());
    }

    /**
     * Returns the length.<p>
     *
     * @return the length
     */
    public int getLength() {

        return m_length;
    }

    /**
     * Returns the virtual resource.<p>
     *
     * @return the virtual resource
     */
    public CmsResource getResource() {

        return new CmsResource(
            m_base.getStructureId(),
            m_base.getResourceId(),
            m_rootPath,
            m_typeId,
            m_isFolder,
            m_base.getFlags(),
            m_base.getProjectLastModified(),
            m_base.getState(),
            m_base.getDateCreated(),
            m_base.getUserCreated(),
            m_base.getDateLastModified(),
            m_base.getUserLastModified(),
            m_base.getDateReleased(),
            m_base.getDateExpired(),
            m_base.getSiblingCount(),
            m_length,
            m_base.getDateContent(),
            m_base.getVersion());
    }

    /**
     * Returns the rootPath.<p>
     *
     * @return the rootPath
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Returns the typeId.<p>
     *
     * @return the typeId
     */
    public int getTypeId() {

        return m_typeId;
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
     * Sets the isFolder.<p>
     *
     * @param isFolder the isFolder to set
     */
    public void setFolder(boolean isFolder) {

        m_isFolder = isFolder;

        if ((m_isFolder) && (!m_rootPath.endsWith("/"))) {
            m_rootPath += "/";
        }
    }

    /**
     * Sets the length.<p>
     *
     * @param length the length to set
     */
    public void setLength(int length) {

        m_length = length;
    }

    /**
     * Sets the rootPath.<p>
     *
     * @param rootPath the rootPath to set
     */
    public void setRootPath(String rootPath) {

        m_rootPath = rootPath;
    }

    /**
     * Sets the typeId.<p>
     *
     * @param typeId the typeId to set
     */
    public void setTypeId(int typeId) {

        m_typeId = typeId;
    }
}
