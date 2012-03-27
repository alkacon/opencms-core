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
import javax.persistence.UniqueConstraint;

/**
 * This data access object represents a cms group entry inside the table "cms_groups".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_GROUPS", uniqueConstraints = @UniqueConstraint(columnNames = {"GROUP_NAME", "GROUP_OU"}))
public class CmsDAOGroups {

    /** The description of this group. */
    @Basic
    @Column(name = "GROUP_DESCRIPTION", nullable = false)
    private String m_groupDescription;

    /** The flags of this group. */
    @Basic
    @Column(name = "GROUP_FLAGS")
    private int m_groupFlags;

    /** The id of this group. */
    @Id
    @Column(name = "GROUP_ID", length = 36)
    private String m_groupId;

    /** The name of this group. */
    @Basic
    @Column(name = "GROUP_NAME", nullable = false, length = 128)
    private String m_groupName;

    /** The ou of this group. */
    @Basic
    @Column(name = "GROUP_OU", nullable = false, length = 128)
    private String m_groupOu;

    /** The parent group id of this group. */
    @Basic
    @Column(name = "PARENT_GROUP_ID", nullable = false, length = 36)
    private String m_parentGroupId;

    /**
     * The public constructor.<p>
     */
    public CmsDAOGroups() {

        // noop
    }

    /**
     * A public constructor for this DAO.<p>
     * 
     * @param groupId the id of the group
     */
    public CmsDAOGroups(String groupId) {

        m_groupId = groupId;
    }

    /**
     * Returns the description of this group.<p>
     * 
     * @return the description of this group
     */
    public String getGroupDescription() {

        return m_groupDescription;
    }

    /**
     * Returns the flag of this group.<p>
     * 
     * @return the flag of this group
     */
    public int getGroupFlags() {

        return m_groupFlags;
    }

    /**
     * Returns the id of this group.<p>
     * 
     * @return the id of this group
     */
    public String getGroupId() {

        return m_groupId;
    }

    /**
     * Returns the name of this group.<p>
     * 
     * @return the name of this group
     */
    public String getGroupName() {

        return m_groupName;
    }

    /**
     * Returns the OU of this group.<p>
     * 
     * @return the OU pf this group
     */
    public String getGroupOu() {

        return m_groupOu;
    }

    /**
     * Returns the ID of the parent group.<p>
     * 
     * @return the ID of the parent group
     */
    public String getParentGroupId() {

        return m_parentGroupId;
    }

    /**
     * Sets the description of this group.<p>
     * 
     * @param groupDescription the description to set
     */
    public void setGroupDescription(String groupDescription) {

        m_groupDescription = groupDescription;
    }

    /**
     * Sets the flag for this group.<p>
     * 
     * @param groupFlags the flag to set
     */
    public void setGroupFlags(int groupFlags) {

        m_groupFlags = groupFlags;
    }

    /**
     * Sets the id of this group.<p>
     * 
     * @param groupId the id to set
     */
    public void setGroupId(String groupId) {

        m_groupId = groupId;
    }

    /**
     * Sets the name of this group.<p>
     * 
     * @param groupName the name to set
     */
    public void setGroupName(String groupName) {

        m_groupName = groupName;
    }

    /**
     * Sets the OU of this group.<p>
     * 
     * @param groupOu the OU to set
     */
    public void setGroupOu(String groupOu) {

        m_groupOu = groupOu;
    }

    /**
     * Sets the ID of the parent group.<p>
     * 
     * @param parentGroupId the id to set
     */
    public void setParentGroupId(String parentGroupId) {

        m_parentGroupId = parentGroupId;
    }
}