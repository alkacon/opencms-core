/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsProject.java,v $
* Date   : $Date: 2003/07/31 13:19:37 $
* Version: $Revision: 1.41 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
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

/**
 * Describes a project. A project is used to handle versions of
 * one resource.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Anders Fugmann
 * @author Jan Krag
 * @version $Revision: 1.41 $ $Date: 2003/07/31 13:19:37 $
 */
public class CmsProject implements Cloneable{

    /**
     * The id of this project.
     */
    private int m_id = I_CmsConstants.C_UNKNOWN_ID;

    /**
     * The user_id of the owner.
     */
    private CmsUUID m_ownerId;

    /**
     * The group_id of the group, who may access the project.
     */
    private CmsUUID m_groupId;

    /**
     * The manager group_id of the group, who may manage the project.
     */
    private CmsUUID m_managergroupId;

    /**
     * The task_id for this project.
     */
    private int m_taskId = I_CmsConstants.C_UNKNOWN_ID;

    /**
     * The name of this project.
     */
    private String m_name = null;

    /**
     * The description of this project.
     */
    private String m_description = null;

     /**
     * The manager group  of this resource.
     */
    private CmsUUID m_managerGroupId;

    /**
     * The creation date of this project.
     */
    private long m_createdate = I_CmsConstants.C_UNKNOWN_LONG;

    /**
     * The state of this project.
     */
    private int m_flags = I_CmsConstants.C_PROJECT_STATE_UNLOCKED;

    /**
     * The project type
     */
    private int m_type = I_CmsConstants.C_UNKNOWN_ID;

    /**
     * Construct a new CmsProject.
     */
    public CmsProject(int projectId, String name, String description, int taskId,
                      CmsUUID ownerId, CmsUUID groupId, CmsUUID managerGroupId, int flags,
                      Timestamp createdate, int type) {

        m_id = projectId;
        m_name = name;
        m_description = description;
        m_taskId = taskId;
        m_ownerId = ownerId;
        m_groupId = groupId;
        m_groupId=groupId;
        m_managergroupId = managerGroupId;
        m_managerGroupId=managerGroupId;
        m_flags = flags;
        m_type = type;
        if( createdate != null) {
            m_createdate = createdate.getTime();
        } else {
            m_createdate = I_CmsConstants.C_UNKNOWN_LONG;
        }
    }

    /**
     * Construct a new CmsProject that can be used to check if the provided id is the online project id.
     */
    public CmsProject(int projectId, int flags) {
        m_id = projectId;
        m_flags = flags;
    }
    
/**
 * Construct a new CmsProject, from a ResultSet.
 * Creation date: (10/02/00)
 * @param rs java.sql.ResultSet
 */
public CmsProject(ResultSet res, org.opencms.db.generic.CmsSqlManager m_cq) throws SQLException {
                 this(res.getInt(m_cq.get("C_PROJECTS_PROJECT_ID")),
                            res.getString(m_cq.get("C_PROJECTS_PROJECT_NAME")),
                            res.getString(m_cq.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                            res.getInt(m_cq.get("C_PROJECTS_TASK_ID")),
                            new CmsUUID(res.getString(m_cq.get("C_PROJECTS_USER_ID"))),
                            new CmsUUID(res.getString(m_cq.get("C_PROJECTS_GROUP_ID"))),
                            new CmsUUID(res.getString(m_cq.get("C_PROJECTS_MANAGERGROUP_ID"))),
                            res.getInt(m_cq.get("C_PROJECTS_PROJECT_FLAGS")),
                            SqlHelper.getTimestamp(res,m_cq.get("C_PROJECTS_PROJECT_CREATEDATE")),
                            res.getInt(m_cq.get("C_PROJECTS_PROJECT_TYPE")));
}
    /**
    * Clones the CmsProject by creating a new CmsProject Object.
    * @return Cloned CmsProject.
    */
    public Object clone() {
        CmsProject project=new CmsProject(this.m_id,new String (this.m_name),
                                       new String(m_description),this.m_taskId,
                                       this.m_ownerId,this.m_groupId,this.m_managerGroupId,
                                       this.m_flags,new Timestamp(this.m_createdate),
                                       this.m_type);
        return project;
    }
    /**
     * Compares the overgiven object with this object.
     *
     * @return true, if the object is identically else it returns false.
     */
    public boolean equals(Object obj) {
        boolean equal=false;
        // check if the object is a CmsProject object
        if (obj instanceof CmsProject) {
            // same ID than the current project?
            if (((CmsProject)obj).getId() == m_id){
                equal = true;
            }
        }
        return equal;
    }
    /**
     * Returns the creation date of this project.
     *
     * @return the creation date of this project.
     */
    public long getCreateDate() {
        return(m_createdate);
    }
    /**
     * Returns the description of this project.
     *
     * @return description The description of this project.
     */
    public String getDescription() {
        if ((m_description== null) || (m_description.length()<1) ) {
            return "(No project description)";
        } else {
            return m_description;
        }
    }
    /**
     * Returns the state of this project.<BR/>
     * This may be C_PROJECT_STATE_UNLOCKED, C_PROJECT_STATE_LOCKED,
     * C_PROJECT_STATE_ARCHIVE.
     *
     * @return the state of this project.
     */
    public int getFlags() {
        return(m_flags);
    }
    /**
     * Returns the groupid of this project.
     *
     * @return the groupid of this project.
     */
    public CmsUUID getGroupId() {
        return(m_groupId);
    }
    /**
     * Returns the id of this project.
     *
     * @return the id of this project.
     */
    public int getId() {
        return(m_id);
    }
    /**
     * Returns <code>true</code> if this project is the Online project.<p>
     * 
     * @return <code>true</code> if this project is the Online project
     */
    public boolean isOnlineProject() {
        return (m_id == I_CmsConstants.C_PROJECT_ONLINE_ID); 
    }    
    /**
     * Returns the manager groupid of this project.
     *
     * @return the manager groupid of this project.
     */
    public CmsUUID getManagerGroupId() {
        return( m_managergroupId );
    }
    /**
     * Returns the name of this project.
     *
     * @return the name of this project.
     */
    public String getName() {
        return(m_name);
    }
    /**
     * Returns the userid of the project owner.
     *
     * @return the userid of the project owner.
     */
    public CmsUUID getOwnerId() {
        return(m_ownerId);
    }

    /**
     * Returns the taskid of this project.
     *
     * @return the taskid of this project.
     */
   public  int getTaskId() {
        return(this.m_taskId);
    }
    /**
     * Gets the type.
     *
     * @return the type.
     */
    public int getType() {
        return m_type;
    }

    /**
     * Do static export after publish.
     *
     * @return true is static export is enabled.
     */
    public boolean doStaticExport(){
        if(m_type > 9){
            return true;
        } else {
            return false;
        }
    }
    /**
     * Sets the description of this project.
     *
     * @param description The description of this project.
     */
    public void setDescription(String description) {
        m_description = description;
    }

    /**
     * Sets the state of this project.<BR/>
     * This may be C_PROJECT_STATE_UNLOCKED, C_PROJECT_STATE_LOCKED,
     * C_PROJECT_STATE_ARCHIVE.
     *
     * @param flags The flag to bes set.
     */
    public void setFlags(int flags) {
        m_flags = flags;
    }

    /**
     * Sets the type.
     *
     * @param the type.
     */
    void setType(int id) {
        m_type = id;
    }
    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     *
     * @return string-representation for this object.
     */
    public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[Project]:");
        output.append(m_name);
        output.append(" , Id=");
        output.append(m_id);
        output.append(" :");
        output.append(m_description);
        return output.toString();
    }
    
    public static boolean isOnlineProject(CmsProject project) {
        return CmsProject.isOnlineProject(project.getId());
    }

    public static boolean isOnlineProject(int projectId) {
        return (projectId == I_CmsConstants.C_PROJECT_ONLINE_ID);
    }
        
}
