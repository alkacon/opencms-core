package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsProject.java,v $
 * Date   : $Date: 2000/10/11 12:10:11 $
 * Version: $Revision: 1.26 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.opencms.core.*;
import java.sql.*;
import com.opencms.util.SqlHelper;

/**
 * This class describes a project. A project is used to handle versions of 
 * one resource.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Anders Fugmann
 * @author Jan Krag
 * @version $Revision: 1.26 $ $Date: 2000/10/11 12:10:11 $
 */
public class CmsProject implements I_CmsConstants, Cloneable{
	
	/**
	 * The id of this project.
	 */
	private int m_id = C_UNKNOWN_ID;
	
	/**
	 * The user_id of the owner.
	 */
	private int m_ownerId = C_UNKNOWN_ID;

	/**
	 * The group_id of the group, who may access the project.
	 */
	private int m_groupId = C_UNKNOWN_ID;
	
	/**
	 * The manager group_id of the group, who may manage the project.
	 */
	private int m_managergroupId = C_UNKNOWN_ID;
	
	/**
	 * The task_id for this project.
	 */
	private int m_taskId = C_UNKNOWN_ID;
	
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
	private int m_managerGroupId;
	
	/**
	 * The creation date of this project.
	 */
	private long m_createdate = C_UNKNOWN_LONG;
	
	/**
	 * The publishing date of this project.
	 */
	private long m_publishingdate = C_UNKNOWN_LONG;
	
	/**
	 * The state of this project.
	 */
	private int m_flags = C_PROJECT_STATE_UNLOCKED;
	
	/**
	 * The user-id of the publisher
	 */
	private int m_publishedBy = C_UNKNOWN_ID;
	
	/**
	 * The project type
	 */
	private int m_type = C_UNKNOWN_ID;

	/**
	 * The parent project, -1 if none.
	 */
	private int parentId;
	 
	 
	

	public CmsProject(int projectId, String name, String description, int taskId, 
					  int ownerId, int group, int managerGroup, int flags, 
					  Timestamp createdate, Timestamp publishingdate, int publishedBy, 
					  int type, int parentId) {
		
		m_id = projectId;
		m_name = name;
		m_description = description;
		m_taskId = taskId;
		m_ownerId = ownerId;
		m_groupId = group;
		m_groupId=group;
		m_managergroupId = managerGroup;
		m_managerGroupId=managerGroup;
		m_flags = flags;
		m_publishedBy = publishedBy;
		m_type = type;
		this.parentId = parentId;
		if( createdate != null) {
			m_createdate = createdate.getTime();
		} else {
			m_createdate = C_UNKNOWN_LONG;
		}
		if( publishingdate != null) {
			m_publishingdate = publishingdate.getTime();
		} else {
			m_publishingdate = C_UNKNOWN_LONG;
		}
	}
/**
 * Construct a new CmsProject, from a ResultSet.
 * Creation date: (10/02/00)
 * @param rs java.sql.ResultSet
 */
public CmsProject(ResultSet res, com.opencms.file.genericSql.CmsQueries m_cq) throws SQLException {
				 this(res.getInt(m_cq.C_PROJECTS_PROJECT_ID),
							res.getString(m_cq.C_PROJECTS_PROJECT_NAME),
							res.getString(m_cq.C_PROJECTS_PROJECT_DESCRIPTION),
							res.getInt(m_cq.C_PROJECTS_TASK_ID),
							res.getInt(m_cq.C_PROJECTS_USER_ID),
							res.getInt(m_cq.C_PROJECTS_GROUP_ID),
							res.getInt(m_cq.C_PROJECTS_MANAGERGROUP_ID),
							res.getInt(m_cq.C_PROJECTS_PROJECT_FLAGS),
							SqlHelper.getTimestamp(res,m_cq.C_PROJECTS_PROJECT_CREATEDATE),
							SqlHelper.getTimestamp(res,m_cq.C_PROJECTS_PROJECT_PUBLISHDATE),
							res.getInt(m_cq.C_PROJECTS_PROJECT_PUBLISHED_BY),
							res.getInt(m_cq.C_PROJECTS_PROJECT_TYPE),
							res.getInt(m_cq.C_PROJECTS_PARENT_ID) );
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
									   new Timestamp(this.m_publishingdate),this.m_publishedBy,
									   this.m_type,this.parentId);
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
			return "OpenCms Project";
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
	public int getGroupId() {
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
	 * Returns the manager groupid of this project.
	 * 
	 * @return the manager groupid of this project.
	 */
	public int getManagerGroupId() {
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
	public int getOwnerId() {
		return(m_ownerId);
	}
/**
 * Returns the parent of a project, skipping projects belonging to deleted sites.
 * Creation date: (10/10/00 11:22:21)
 * @author Finn Nielsen
 * @return The parent project
 * @param cms A CmsObject needed to make database access.
 */
public CmsProject getParent(CmsObject cms) throws CmsException
{
	// This is the default online master project.
	if (parentId == -1) return null;

	// get the first parent (will allways be an onlineproject, since offline projects will be leafs in the tree).
	//CmsSite site = cms.getSite(parentId);
	//CmsProject project = cms.readProject(site.getOnlineProjectId());

	// skip deleted sites
	//if (site.isDeleted())
	//	return project.getParent(cms);
	//else
	//	return project;
	return cms.readProject(parentId);
}
/**
 * return the id of the parent project.
 * Creation date: (10/02/00)
 * @return int the id of the parent project, -1 if none.
 */
public int getParentId() {
	return parentId;
}
	/**
	 * Gets the published-by value.
	 * 
	 * @return the published-by value.
	 */
	public int getPublishedBy() {
		return m_publishedBy;
	}
	/**
	 * Returns the publishing date of this project.
	 * 
	 * @return the publishing date of this project.
	 */
	public long getPublishingDate() {
		return(m_publishingdate);
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
	int getType() {
		return m_type;
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
 * Set the parent Id.
 * Creation date: (10/02/00)
 * @param newParentId int the parent Id.
 */
private void setParentId(int newParentId) {
	parentId = newParentId;
}
	/**
	 * Sets the published-by value.
	 * 
	 * @param the published-by value.
	 */
	public void setPublishedBy(int id) {
		m_publishedBy = id;
	}
	/**
	 * Sets the publishing date of this project.
	 * 
	 * @param the publishing date of this project.
	 */
	public void setPublishingDate(long publishingDate) {
		m_publishingdate = publishingDate;
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
}
