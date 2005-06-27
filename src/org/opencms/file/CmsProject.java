/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsProject.java,v $
 * Date   : $Date: 2005/06/27 23:22:15 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.util.CmsUUID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Describes an OpenCms project.<p>
 *
 * @author Alexander Kandzior 
 * @author Michael Emmerich 
 *
 * @version $Revision: 1.15 $
 * 
 * @since 6.0.0 
 */
public class CmsProject implements Cloneable {

    /** The id of the online project. */
    public static final int ONLINE_PROJECT_ID = 1;

    /** The name of the online project. */
    public static final String ONLINE_PROJECT_NAME = "Online";

    /** Indicates that a project is invisible in the workplace. */
    public static final int PROJECT_STATE_INVISIBLE = 3;

    /** Indicates an unlocked project. */
    public static final int PROJECT_STATE_UNLOCKED = 0;

    /** Indicates a normal project. */
    public static final int PROJECT_TYPE_NORMAL = 0;

    /** Indicates a temporary project that is deleted after it is published. */
    public static final int PROJECT_TYPE_TEMPORARY = 1;

    /** The creation date of this project. */
    private long m_dateCreated;

    /** The description of this project. */
    private String m_description;

    /** The state of this project. */
    private int m_flags;

    /** The manager group id of this project. */
    private CmsUUID m_groupManagersId;

    /** The id of the user group of this project. */
    private CmsUUID m_groupUsersId;

    /** The id of this project. */
    private int m_id;

    /** The name of this project. */
    private String m_name;

    /** The id of this projects owner. */
    private CmsUUID m_ownerId;

    /** The task id of this project. */
    private int m_taskId;

    /** The type of this project. */
    private int m_type;

    /**
     * Default constructor for gui usage.<p>
     */
    public CmsProject() {

        // noop
    }

    /**
     * Creates a new CmsProject.<p>
     *  
     * @param projectId the id to use for this project
     * @param name the name for this project
     * @param description the description for this project
     * @param taskId the task id for this project
     * @param ownerId the owner id for this project
     * @param groupId the group id for this project
     * @param managerGroupId the manager group id for this project
     * @param flags the flags for this project
     * @param dateCreated the creation date of this project
     * @param type the type of this project
     */
    public CmsProject(
        int projectId,
        String name,
        String description,
        int taskId,
        CmsUUID ownerId,
        CmsUUID groupId,
        CmsUUID managerGroupId,
        int flags,
        long dateCreated,
        int type) {

        m_id = projectId;
        m_name = name;
        m_description = description;
        m_taskId = taskId;
        m_ownerId = ownerId;
        m_groupUsersId = groupId;
        m_groupManagersId = managerGroupId;
        m_flags = flags;
        m_type = type;
        m_dateCreated = dateCreated;
    }

    /**
     * Construct a new CmsProject from a SQL ResultSet.<p>
     * 
     * @param res the result set to create a project from
     * @param sqlManager the SQL manager to use
     * @throws SQLException in case something goes wrong
     */
    public CmsProject(ResultSet res, org.opencms.db.generic.CmsSqlManager sqlManager)
    throws SQLException {

        this(
            res.getInt(sqlManager.readQuery("C_PROJECTS_PROJECT_ID")),
            res.getString(sqlManager.readQuery("C_PROJECTS_PROJECT_NAME")),
            res.getString(sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION")),
            res.getInt(sqlManager.readQuery("C_PROJECTS_TASK_ID")),
            new CmsUUID(res.getString(sqlManager.readQuery("C_PROJECTS_USER_ID"))),
            new CmsUUID(res.getString(sqlManager.readQuery("C_PROJECTS_GROUP_ID"))),
            new CmsUUID(res.getString(sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID"))),
            res.getInt(sqlManager.readQuery("C_PROJECTS_PROJECT_FLAGS")),
            res.getLong(sqlManager.readQuery("C_PROJECTS_DATE_CREATED")),
            res.getInt(sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE")));
    }

    /**
     * Checks if the full resource name (including the site root) of a resource matches
     * any of the project resources of a project.<p>
     * 
     * @param projectResources a List of project resources as Strings
     * @param resource the resource to check
     * @return true, if the resource is "inside" the project resources
     */
    public static boolean isInsideProject(List projectResources, CmsResource resource) {

        String resourcename = resource.getRootPath();
        return isInsideProject(projectResources, resourcename);
    }

    /**
     * Checks if the full resource name (including the site root) of a resource matches
     * any of the project resources of a project.<p>
     * 
     * @param projectResources a List of project resources as Strings
     * @param resourcename the resource to check
     * @return true, if the resource is "inside" the project resources
     */
    public static boolean isInsideProject(List projectResources, String resourcename) {

        for (int i = (projectResources.size() - 1); i >= 0; i--) {
            String projectResource = (String)projectResources.get(i);
            if (CmsResource.isFolder(projectResource)) {
                if (resourcename.startsWith(projectResource)) {
                    // folder - check only the prefix
                    return true;
                }
            } else {
                if (resourcename.equals(projectResource)) {
                    // file - check the full path
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the given project id is the online project id.<p>
     *  
     * @param projectId the project id to check
     * @return true if the given project id is the online project id
     */
    public static boolean isOnlineProject(int projectId) {

        return projectId == CmsProject.ONLINE_PROJECT_ID;
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        return new CmsProject(
            m_id,
            m_name,
            m_description,
            m_taskId,
            m_ownerId,
            m_groupUsersId,
            m_groupManagersId,
            m_flags,
            m_dateCreated,
            m_type);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsProject) {
            return ((CmsProject)obj).m_id == m_id;
        }
        return false;
    }

    /**
     * Returns the creation date of this project.<p>
     *
     * @return the creation date of this project
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Returns the description of this project.
     *
     * @return the description of this project
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the state of this project.<p>
     *
     * @return the state of this project
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * Returns the user group id of this project.<p>
     *
     * @return the user group id of this project
     */
    public CmsUUID getGroupId() {

        return m_groupUsersId;
    }

    /**
     * Returns the id of this project.<p>
     *
     * @return the id of this project
     */
    public int getId() {

        return m_id;
    }

    /**
     * Returns the manager group id of this project.<p>
     *
     * @return the manager group id of this project
     */
    public CmsUUID getManagerGroupId() {

        return m_groupManagersId;
    }

    /**
     * Returns the name of this project.<p>
     *
     * @return the name of this project
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the user id of the project owner.<p>
     *
     * @return the user id of the project owner
     */
    public CmsUUID getOwnerId() {

        return m_ownerId;
    }

    /**
     * Returns the task id of this project.<p>
     *
     * @return the task id of this project
     */
    public int getTaskId() {

        return m_taskId;
    }

    /**
     * Returns the type of this project.<p>
     *
     * @return the type of this project
     */
    public int getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        if (m_name != null) {
            return m_name.hashCode();
        }
        return 0;
    }

    /**
     * Returns the delete After Publishing flag.<p>
     *
     * @return the delete After Publishing flag
     * 
     * @see #getType()
     */
    public boolean isDeleteAfterPublishing() {

        return m_type == CmsProject.PROJECT_TYPE_TEMPORARY;
    }

    /**
     * Returns <code>true</code> if this project is the Online project.<p>
     * 
     * @return <code>true</code> if this project is the Online project
     */
    public boolean isOnlineProject() {

        return isOnlineProject(m_id);
    }

    /**
     * Sets the delete After Publishing flag.<p>
     *
     * @param deleteAfterPublishing the delete After Publishing flag to set
     * 
     * @see #setType(int)
     */
    public void setDeleteAfterPublishing(boolean deleteAfterPublishing) {

        m_type = deleteAfterPublishing ? CmsProject.PROJECT_TYPE_TEMPORARY : CmsProject.PROJECT_TYPE_NORMAL;
    }

    /**
     * Sets the description of this project.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the flags of this project.<p>
     *
     * @param flags the flag to set
     */
    public void setFlags(int flags) {

        m_flags = flags;
    }

    /**
     * Sets the user group id of this project.<p>
     *
     * @param id the user group id of this project
     */
    public void setGroupId(CmsUUID id) {

        CmsUUID.checkId(id, false);
        m_groupUsersId = id;
    }

    /**
     * Sets the manager group id of this project.<p>
     *
     * @param id the manager group id of this project
     */
    public void setManagerGroupId(CmsUUID id) {

        CmsUUID.checkId(id, false);
        m_groupManagersId = id;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[Project]:");
        result.append(m_name);
        result.append(" , Id=");
        result.append(m_id);
        result.append(" :");
        result.append(m_description);
        return result.toString();
    }

    /**
     * Sets the type of this project.<p>
     *
     * @param id the type to set
     */
    void setType(int id) {

        m_type = id;
    }
}
