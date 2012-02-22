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
import javax.persistence.Table;

/**
 * This data access object represents a online structure entry inside the table "cms_online_structure".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_ONLINE_STRUCTURE")
public class CmsDAOOnlineStructure implements I_CmsDAOStructure {

    /** The date expired. */
    @Basic
    @Column(name = "DATE_EXPIRED")
    private long m_dateExpired;

    /** The date released. */
    @Basic
    @Column(name = "DATE_RELEASED")
    private long m_dateReleased;

    /** The parent id. */
    @Basic
    @Column(name = "PARENT_ID", nullable = false, length = 36)
    private String m_parentId;

    /** The resource id. */
    @Basic
    @Column(name = "RESOURCE_ID", nullable = false, length = 36)
    private String m_resourceId;

    /** The resource path. */
    @Basic
    @Column(name = "RESOURCE_PATH", length = 1024)
    private String m_resourcePath;

    /** The id. */
    @Id
    @Column(name = "STRUCTURE_ID", length = 36)
    private String m_structureId;

    /** The structure state. */
    @Basic
    @Column(name = "STRUCTURE_STATE")
    private int m_structureState;

    /** The structure version. */
    @Basic
    @Column(name = "STRUCTURE_VERSION")
    private int m_structureVersion;

    /**
     * The default constructor.<p>
     */
    public CmsDAOOnlineStructure() {

        // noop
    }

    /**
     * A public constructor for generating a new structure object with an unique id.<p>
     * 
     * @param structureId the structure id
     */
    public CmsDAOOnlineStructure(String structureId) {

        m_structureId = structureId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#getDateExpired()
     */
    public long getDateExpired() {

        return m_dateExpired;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#getDateReleased()
     */
    public long getDateReleased() {

        return m_dateReleased;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#getParentId()
     */
    public String getParentId() {

        return m_parentId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#getResourceId()
     */
    public String getResourceId() {

        return m_resourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#getResourcePath()
     */
    public String getResourcePath() {

        return m_resourcePath;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#getStructureId()
     */
    public String getStructureId() {

        return m_structureId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#getStructureState()
     */
    public int getStructureState() {

        return m_structureState;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#getStructureVersion()
     */
    public int getStructureVersion() {

        return m_structureVersion;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#setDateExpired(long)
     */
    public void setDateExpired(long dateExpired) {

        m_dateExpired = dateExpired;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#setDateReleased(long)
     */
    public void setDateReleased(long dateReleased) {

        m_dateReleased = dateReleased;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#setParentId(java.lang.String)
     */
    public void setParentId(String parentId) {

        m_parentId = parentId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#setResourceId(java.lang.String)
     */
    public void setResourceId(String resourceId) {

        m_resourceId = resourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#setResourcePath(java.lang.String)
     */
    public void setResourcePath(String resourcePath) {

        m_resourcePath = resourcePath;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#setStructureId(java.lang.String)
     */
    public void setStructureId(String structureId) {

        m_structureId = structureId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#setStructureState(int)
     */
    public void setStructureState(int structureState) {

        m_structureState = structureState;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOStructure#setStructureVersion(int)
     */
    public void setStructureVersion(int structureVersion) {

        m_structureVersion = structureVersion;
    }

}