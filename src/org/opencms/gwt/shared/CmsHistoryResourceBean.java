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

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean representing the data to be displayed in the history dialog for a single resource version.<p>
 */
public class CmsHistoryResourceBean implements IsSerializable {

    /** The description. */
    private String m_description;

    /** The modification date. */
    private CmsClientDateBean m_modificationDate;

    /** The publish date. */
    private CmsClientDateBean m_publishDate;

    /** The root path.<p> */
    private String m_rootPath;

    /** The size. */
    private int m_size;

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The title. */
    private String m_title;

    /**
     * The user who last modified the resource.<p>
     */
    private String m_userLastModified;

    /** The version. */
    private CmsHistoryVersion m_version;

    /**
     * Creates a new instance.<p>
     */
    public CmsHistoryResourceBean() {

    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Gets the modification date.<p>
     *
     * @return the modification date
     */
    public CmsClientDateBean getModificationDate() {

        return m_modificationDate;
    }

    /**
     * Gets the publish date.<p>
     *
     * @return the publish date
     */
    public CmsClientDateBean getPublishDate() {

        return m_publishDate;
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
     * Gets the size.<p>
     *
     * @return the size
     */
    public int getSize() {

        return m_size;
    }

    /**
     * Returns the structureId.<p>
     *
     * @return the structureId
     */
    public CmsUUID getStructureId() {

        return m_structureId;
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
     * Gets the user who last modified the history version.<p>+
     *
     * @return the user who last modified the resource
     */
    public String getUserLastModified() {

        return m_userLastModified;
    }

    /**
     * Returns the version.<p>
     *
     * @return the version
     */
    public CmsHistoryVersion getVersion() {

        return m_version;
    }

    /**
     * Sets the modification date.<p>
     *
     * @param formatDate the modification date
     */
    public void setDateLastModified(CmsClientDateBean formatDate) {

        m_modificationDate = formatDate;
    }

    /**
     * Sets the publish date.<p>
     *
     * @param formatDate the publish date
     */
    public void setDatePublished(CmsClientDateBean formatDate) {

        m_publishDate = formatDate;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description
     */
    public void setDescription(String description) {

        m_description = description;
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
     * Sets the size.<p>
     *
     * @param size the size
     */
    public void setSize(int size) {

        m_size = size;
    }

    /**
     * Sets the structure id
     * @param structureId the structure id to set
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the title.<p>
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;

    }

    /**
     * Sets the name of the user who last modified the resource.<p>
     *
     * @param userName the name of the user
     */
    public void setUserLastModified(String userName) {

        m_userLastModified = userName;
    }

    /**
     * Sets the version.<p>
     *
     * @param version the version to set
     */
    public void setVersion(CmsHistoryVersion version) {

        m_version = version;
    }

}
