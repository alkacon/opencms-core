/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsProject.java,v $
 * Date   : $Date: 2003/09/10 07:20:04 $
 * Version: $Revision: 1.46 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.file;

import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.SqlHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

/**
 * Describes an OpenCms project.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 *
 * @version $Revision: 1.46 $
 */
public class CmsProject implements Cloneable {

    /** The creation date of this project */
    private long m_dateCreated;

    /** The description of this project */
    private String m_description;

    /** The state of this project */
    private int m_flags;

    /** The manager group id of this project */
    private CmsUUID m_groupManagersId;

    /** The id of the user group of this project */
    private CmsUUID m_groupUsersId;

    /** The id of this project */
    private int m_id;

    /** The name of this project */
    private String m_name;

    /** The id of this projects owner */
    private CmsUUID m_ownerId;
    
    /** The task id of this project */
    private int m_taskId;

    /** The type of this project */
    private int m_type;

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
        Timestamp dateCreated, 
        int type
    ) {
        m_id = projectId;
        m_name = name;
        m_description = description;
        m_taskId = taskId;
        m_ownerId = ownerId;
        m_groupUsersId = groupId;
        m_groupUsersId=groupId;
        m_groupManagersId = managerGroupId;
        m_flags = flags;
        m_type = type;
        
        if (dateCreated != null) {
            m_dateCreated = dateCreated.getTime();
        } else {
            m_dateCreated = I_CmsConstants.C_UNKNOWN_LONG;
        }
    }

    /**
     * Construct a new CmsProject from a SQL ResultSet.<p>
     * 
     * @param res the result set to create a project from
     * @param sqlManager the SQL manager to use
     * @throws SQLException in case something goes wrong
     */
    public CmsProject(ResultSet res, org.opencms.db.generic.CmsSqlManager sqlManager) throws SQLException {
        this(
            res.getInt(sqlManager.get("C_PROJECTS_PROJECT_ID")),
            res.getString(sqlManager.get("C_PROJECTS_PROJECT_NAME")),
            res.getString(sqlManager.get("C_PROJECTS_PROJECT_DESCRIPTION")),
            res.getInt(sqlManager.get("C_PROJECTS_TASK_ID")),
            new CmsUUID(res.getString(sqlManager.get("C_PROJECTS_USER_ID"))),
            new CmsUUID(res.getString(sqlManager.get("C_PROJECTS_GROUP_ID"))),
            new CmsUUID(res.getString(sqlManager.get("C_PROJECTS_MANAGERGROUP_ID"))),
            res.getInt(sqlManager.get("C_PROJECTS_PROJECT_FLAGS")),
            SqlHelper.getTimestamp(res, sqlManager.get("C_PROJECTS_PROJECT_CREATEDATE")),
            res.getInt(sqlManager.get("C_PROJECTS_PROJECT_TYPE"))
        );
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
        String resourcename = resource.getFullResourceName();        
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
        Iterator i = projectResources.iterator();  
        while (i.hasNext()) {
            String projectResource = (String)i.next();
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
        return projectId == I_CmsConstants.C_PROJECT_ONLINE_ID;
    }
    
    /**
     * @see java.lang.Object#clone()
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
            new Timestamp(m_dateCreated),
            m_type
        );
    }
        
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // check if the object is a CmsProject object
        if (obj instanceof CmsProject) {
            // same ID than the current project?
            return ((CmsProject)obj).getId() == m_id;
        }
        return false;
    }
    
    /**
     * Returns the creation date of this project.<p>
     *
     * @return the creation date of this project
     */
    public long getCreateDate() {
        return m_dateCreated;
    }
    
    /**
     * Returns the description of this project.
     *
     * @return the description of this project
     */
    public String getDescription() {
        if ((m_description == null) || (m_description.length() < 1)) {
            return "(No project description entered)";
        } else {
            return m_description;
        }
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
    public  int getTaskId() {
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
     * Returns <code>true</code> if this project is the Online project.<p>
     * 
     * @return <code>true</code> if this project is the Online project
     */
    public boolean isOnlineProject() {
        return isOnlineProject(m_id); 
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
     * Sets the type of this project.<p>
     *
     * @param id the type to set
     */
    void setType(int id) {
        m_type = id;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer result=new StringBuffer();
        result.append("[Project]:");
        result.append(m_name);
        result.append(" , Id=");
        result.append(m_id);
        result.append(" :");
        result.append(m_description);
        return result.toString();
    }
        
}
