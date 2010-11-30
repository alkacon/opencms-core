/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/jpa/persistence/CmsDAOOnlineResources.java,v $
 * Date   : $Date: 2010/11/30 09:33:54 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
 * This data access object represents a online resource entry inside the table "cms_online_resources".<p>
 * 
 * @author Georgi Naplatanov
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "cms_online_resources")
public class CmsDAOOnlineResources implements I_CmsDAOResources {

    /** The date content. */
    @Basic
    @Column(name = "date_content")
    private long m_dateContent;

    /** The date created. */
    @Basic
    @Column(name = "date_created")
    private long m_dateCreated;

    /** The date last modified. */
    @Basic
    @Column(name = "date_lastmodified")
    private long m_dateLastModified;

    /** The project last modified. */
    @Basic
    @Column(name = "project_lastmodified", nullable = false, length = 36)
    private String m_projectLastModified;

    /** The resource flags. */
    @Basic
    @Column(name = "resource_flags")
    private int m_resourceFlags;

    /** The resource id. */
    @Id
    @Column(name = "resource_id", length = 36)
    private String m_resourceId;

    /** The resource size. */
    @Basic
    @Column(name = "resource_size")
    private int m_resourceSize;

    /** The resource state. */
    @Basic
    @Column(name = "resource_state")
    private int m_resourceState;

    /** The resource type. */
    @Basic
    @Column(name = "resource_type")
    private int m_resourceType;

    /** The resource version. */
    @Basic
    @Column(name = "resource_version")
    private int m_resourceVersion;

    /** The sibling count. */
    @Basic
    @Column(name = "sibling_count")
    private int m_siblingCount;

    /** The user created. */
    @Basic
    @Column(name = "user_created", nullable = false, length = 36)
    private String m_userCreated;

    /** The user last modified. */
    @Basic
    @Column(name = "user_lastmodified", nullable = false, length = 36)
    private String m_userLastModified;

    /**
     * The default constructor.<p>
     */
    public CmsDAOOnlineResources() {

        // noop
    }

    /**
     * A public constructor for generating a new resource object with an unique id.<p>
     * 
     * @param resourceId the resource id
     */
    public CmsDAOOnlineResources(String resourceId) {

        m_resourceId = resourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getDateContent()
     */
    public long getDateContent() {

        return m_dateContent;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getDateCreated()
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getDateLastModified()
     */
    public long getDateLastModified() {

        return m_dateLastModified;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getProjectLastModified()
     */
    public String getProjectLastModified() {

        return m_projectLastModified;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getResourceFlags()
     */
    public int getResourceFlags() {

        return m_resourceFlags;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getResourceId()
     */
    public String getResourceId() {

        return m_resourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getResourceSize()
     */
    public int getResourceSize() {

        return m_resourceSize;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getResourceState()
     */
    public int getResourceState() {

        return m_resourceState;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getResourceType()
     */
    public int getResourceType() {

        return m_resourceType;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getResourceVersion()
     */
    public int getResourceVersion() {

        return m_resourceVersion;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getSiblingCount()
     */
    public int getSiblingCount() {

        return m_siblingCount;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getUserCreated()
     */
    public String getUserCreated() {

        return m_userCreated;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#getUserLastModified()
     */
    public String getUserLastModified() {

        return m_userLastModified;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setDateContent(long)
     */
    public void setDateContent(long dateContent) {

        m_dateContent = dateContent;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setDateCreated(long)
     */
    public void setDateCreated(long dateCreated) {

        m_dateCreated = dateCreated;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setDateLastModified(long)
     */
    public void setDateLastModified(long dateLastModified) {

        m_dateLastModified = dateLastModified;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setProjectLastModified(java.lang.String)
     */
    public void setProjectLastModified(String projectLastModified) {

        m_projectLastModified = projectLastModified;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setResourceFlags(int)
     */
    public void setResourceFlags(int resourceFlags) {

        m_resourceFlags = resourceFlags;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setResourceId(java.lang.String)
     */
    public void setResourceId(String resourceId) {

        m_resourceId = resourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setResourceSize(int)
     */
    public void setResourceSize(int resourceSize) {

        m_resourceSize = resourceSize;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setResourceState(int)
     */
    public void setResourceState(int resourceState) {

        m_resourceState = resourceState;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setResourceType(int)
     */
    public void setResourceType(int resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setResourceVersion(int)
     */
    public void setResourceVersion(int resourceVersion) {

        m_resourceVersion = resourceVersion;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setSiblingCount(int)
     */
    public void setSiblingCount(int siblingCount) {

        m_siblingCount = siblingCount;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setUserCreated(java.lang.String)
     */
    public void setUserCreated(String userCreated) {

        m_userCreated = userCreated;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResources#setUserLastModified(java.lang.String)
     */
    public void setUserLastModified(String userLastModified) {

        m_userLastModified = userLastModified;
    }
}