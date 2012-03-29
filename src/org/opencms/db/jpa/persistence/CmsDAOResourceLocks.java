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
 * This data access object represents a resource lock entry inside the table "cms_resource_locks".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_RESOURCE_LOCKS")
public class CmsDAOResourceLocks {

    /** The lock type. */
    @Basic
    @Column(name = "LOCK_TYPE")
    private int m_lockType;

    /** The project id. */
    @Basic
    @Column(name = "PROJECT_ID", nullable = false, length = 36)
    private String m_projectId;

    /** The resource path. */
    @Id
    @Column(name = "RESOURCE_PATH", length = 1024)
    private String m_resourcePath;

    /** The user id. */
    @Basic
    @Column(name = "USER_ID", nullable = false, length = 36)
    private String m_userId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOResourceLocks() {

        // noop
    }

    /**
     * A public constructor for generating a new resource lock object with an unique id.<p>
     * 
     * @param resourcePath the resource path
     */
    public CmsDAOResourceLocks(String resourcePath) {

        m_resourcePath = resourcePath;
    }

    /**
     * Returns the lockType.<p>
     *
     * @return the lockType
     */
    public int getLockType() {

        return m_lockType;
    }

    /**
     * Returns the projectId.<p>
     *
     * @return the projectId
     */
    public String getProjectId() {

        return m_projectId;
    }

    /**
     * Returns the resourcePath.<p>
     *
     * @return the resourcePath
     */
    public String getResourcePath() {

        return m_resourcePath;
    }

    /**
     * Returns the userId.<p>
     *
     * @return the userId
     */
    public String getUserId() {

        return m_userId;
    }

    /**
     * Sets the lockType.<p>
     *
     * @param lockType the lockType to set
     */
    public void setLockType(int lockType) {

        m_lockType = lockType;
    }

    /**
     * Sets the projectId.<p>
     *
     * @param projectId the projectId to set
     */
    public void setProjectId(String projectId) {

        m_projectId = projectId;
    }

    /**
     * Sets the resourcePath.<p>
     *
     * @param resourcePath the resourcePath to set
     */
    public void setResourcePath(String resourcePath) {

        m_resourcePath = resourcePath;
    }

    /**
     * Sets the userId.<p>
     *
     * @param userId the userId to set
     */
    public void setUserId(String userId) {

        m_userId = userId;
    }

}