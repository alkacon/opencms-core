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

package org.opencms.db.jpa.persistence;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * This data access object represents a offline contents entry inside the table "cms_offline_contents".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_OFFLINE_CONTENTS")
public class CmsDAOOfflineContents {

    /** The file contents. */
    @Basic
    @Lob
    @Column(name = "FILE_CONTENT", nullable = true)
    private byte[] m_fileContent;

    /** The resource id. */
    @Id
    @Column(name = "RESOURCE_ID", length = 36)
    private String m_resourceId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOOfflineContents() {

        // noop
    }

    /**
     * A public constructor for generating a new contents object with an unique id.<p>
     * 
     * @param resourceId the resource id
     */
    public CmsDAOOfflineContents(String resourceId) {

        m_resourceId = resourceId;
    }

    /**
     * Returns the fileContent.<p>
     *
     * @return the fileContent
     */
    public byte[] getFileContent() {

        return m_fileContent;
    }

    /**
     * Returns the resourceId.<p>
     *
     * @return the resourceId
     */
    public String getResourceId() {

        return m_resourceId;
    }

    /**
     * Sets the fileContent.<p>
     *
     * @param fileContent the fileContent to set
     */
    public void setFileContent(byte[] fileContent) {

        m_fileContent = fileContent;
    }

    /**
     * Sets the resourceId.<p>
     *
     * @param resourceId the resourceId to set
     */
    public void setResourceId(String resourceId) {

        m_resourceId = resourceId;
    }
}