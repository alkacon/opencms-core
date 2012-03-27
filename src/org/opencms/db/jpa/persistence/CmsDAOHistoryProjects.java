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
 * This data access object represents a history project entry 
 * inside the table "cms_history_projects".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_HISTORY_PROJECTS")
public class CmsDAOHistoryProjects {

    /** The date created. */
    @Basic
    @Column(name = "DATE_CREATED")
    private long m_dateCreated;

    /** The group id. */
    @Basic
    @Column(name = "GROUP_ID", nullable = false, length = 36)
    private String m_groupId;

    /** The managers group id. */
    @Basic
    @Column(name = "MANAGERGROUP_ID", nullable = false, length = 36)
    private String m_managerGroupId;

    /** The projects description. */
    @Basic
    @Column(name = "PROJECT_DESCRIPTION", nullable = false)
    private String m_projectDescription;

    /** The project id. */
    @Basic
    @Column(name = "PROJECT_ID", nullable = false, length = 36)
    private String m_projectId;

    /** The project name. */
    @Basic
    @Column(name = "PROJECT_NAME", nullable = false)
    private String m_projectName;

    /** The project ou. */
    @Basic
    @Column(name = "PROJECT_OU", nullable = false, length = 128)
    private String m_projectOu;

    /** The publish date of the project.*/
    @Basic
    @Column(name = "PROJECT_PUBLISHDATE")
    private long m_projectPublishDate;

    /** The user who published this project. */
    @Basic
    @Column(name = "PROJECT_PUBLISHED_BY", nullable = false, length = 36)
    private String m_projectPublishedBy;

    /** The project type. */
    @Basic
    @Column(name = "PROJECT_TYPE")
    private int m_projectType;

    /** The publish tag. */
    @Id
    @Column(name = "PUBLISH_TAG")
    private int m_publishTag;

    /** The user id. */
    @Basic
    @Column(name = "USER_ID", nullable = false, length = 36)
    private String m_userId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOHistoryProjects() {

        // noop
    }

    /**
     * This constructor creates a history project.<p>
     * 
     * @param publishTag the publish tag
     */
    public CmsDAOHistoryProjects(int publishTag) {

        m_publishTag = publishTag;
    }

    /**
     * Returns the dateCreated.<p>
     *
     * @return the dateCreated
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Returns the groupId.<p>
     *
     * @return the groupId
     */
    public String getGroupId() {

        return m_groupId;
    }

    /**
     * Returns the managerGroupId.<p>
     *
     * @return the managerGroupId
     */
    public String getManagerGroupId() {

        return m_managerGroupId;
    }

    /**
     * Returns the projectDescription.<p>
     *
     * @return the projectDescription
     */
    public String getProjectDescription() {

        return m_projectDescription;
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
     * Returns the projectName.<p>
     *
     * @return the projectName
     */
    public String getProjectName() {

        return m_projectName;
    }

    /**
     * Returns the projectOu.<p>
     *
     * @return the projectOu
     */
    public String getProjectOu() {

        return m_projectOu;
    }

    /**
     * Returns the projectPublishDate.<p>
     *
     * @return the projectPublishDate
     */
    public long getProjectPublishDate() {

        return m_projectPublishDate;
    }

    /**
     * Returns the projectPublishedBy.<p>
     *
     * @return the projectPublishedBy
     */
    public String getProjectPublishedBy() {

        return m_projectPublishedBy;
    }

    /**
     * Returns the projectType.<p>
     *
     * @return the projectType
     */
    public int getProjectType() {

        return m_projectType;
    }

    /**
     * Returns the publishTag.<p>
     *
     * @return the publishTag
     */
    public int getPublishTag() {

        return m_publishTag;
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
     * Sets the dateCreated.<p>
     *
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(long dateCreated) {

        m_dateCreated = dateCreated;
    }

    /**
     * Sets the groupId.<p>
     *
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {

        m_groupId = groupId;
    }

    /**
     * Sets the managerGroupId.<p>
     *
     * @param managerGroupId the managerGroupId to set
     */
    public void setManagerGroupId(String managerGroupId) {

        m_managerGroupId = managerGroupId;
    }

    /**
     * Sets the projectDescription.<p>
     *
     * @param projectDescription the projectDescription to set
     */
    public void setProjectDescription(String projectDescription) {

        m_projectDescription = projectDescription;
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
     * Sets the projectName.<p>
     *
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {

        m_projectName = projectName;
    }

    /**
     * Sets the projectOu.<p>
     *
     * @param projectOu the projectOu to set
     */
    public void setProjectOu(String projectOu) {

        m_projectOu = projectOu;
    }

    /**
     * Sets the projectPublishDate.<p>
     *
     * @param projectPublishDate the projectPublishDate to set
     */
    public void setProjectPublishDate(long projectPublishDate) {

        m_projectPublishDate = projectPublishDate;
    }

    /**
     * Sets the projectPublishedBy.<p>
     *
     * @param projectPublishedBy the projectPublishedBy to set
     */
    public void setProjectPublishedBy(String projectPublishedBy) {

        m_projectPublishedBy = projectPublishedBy;
    }

    /**
     * Sets the projectType.<p>
     *
     * @param projectType the projectType to set
     */
    public void setProjectType(int projectType) {

        m_projectType = projectType;
    }

    /**
     * Sets the publishTag.<p>
     *
     * @param publishTag the publishTag to set
     */
    public void setPublishTag(int publishTag) {

        m_publishTag = publishTag;
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