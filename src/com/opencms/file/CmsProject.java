/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsProject.java,v $
 * Date   : $Date: 2000/06/06 16:55:17 $
 * Version: $Revision: 1.15 $
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

package com.opencms.file;

import com.opencms.core.*;
import java.sql.*;

/**
 * This class describes a project. A project is used to handle versions of 
 * one resource.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2000/06/06 16:55:17 $
 */
public class CmsProject implements I_CmsConstants,
                                                        Cloneable{
	
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
     * The group  of this resource.
     */
    private CmsGroup m_group;
  
     /**
     * The manager group  of this resource.
     */
    private CmsGroup m_managerGroup;
    
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
	 * The counter of locked resources in this project.
	 */
	private int m_countLockedResources = 0;

	/**
	 * The project type
	 */
	private int m_type = C_UNKNOWN_ID;
	

	CmsProject(int projectId, String name, String description, int taskId, 
			   int ownerId, CmsGroup group, CmsGroup managerGroup, int flags, Timestamp createdate, 
			   Timestamp publishingdate, int publishedBy, int countLockedResources, int type) {
		m_id = projectId;
		m_name = name;
		m_description = description;
		m_taskId = taskId;
		m_ownerId = ownerId;
		m_groupId = group.getId();
        m_group=group;
		m_managergroupId = managerGroup.getId();
        m_managerGroup=managerGroup;
		m_flags = flags;
		m_publishedBy = publishedBy;
		m_countLockedResources = countLockedResources;
		m_type = type;
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
	 * Returns the name of this project.
	 * 
	 * @return the name of this project.
	 */
	public String getName() {
		return(m_name);
	}

	/**
	 * Returns the description of this project.
	 * 
	 * @return description The description of this project.
	 */
	public String getDescription() {
		return(m_description);
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
	 * Sets the state of this project.<BR/>
	 * This may be C_PROJECT_STATE_UNLOCKED, C_PROJECT_STATE_LOCKED, 
	 * C_PROJECT_STATE_ARCHIVE.
	 * 
	 * @param flags The flag to bes set.
	 */
	void setFlags(int flags) {
		m_flags = flags;
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
	 * Returns the userid of the project owner.
	 * 
	 * @return the userid of the project owner.
	 */
	int getOwnerId() {
		return(m_ownerId);
	}
	
	/**
	 * Returns the groupid of this project.
	 * 
	 * @return the groupid of this project.
	 */
    int getGroupId() {
		return(m_groupId);
	}
	
	/**
	 * Returns the manager groupid of this project.
	 * 
	 * @return the manager groupid of this project.
	 */
	int getManagerGroupId() {
		return( m_managergroupId );
	}
	
	/**
	 * Returns the taskid of this project.
	 * 
	 * @return the taskid of this project.
	 */
    int getTaskId() {
		return(this.m_taskId);
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
	 * Returns the creation date of this project.
	 * 
	 * @return the creation date of this project.
	 */
	public long getCreateDate() {
		return(m_createdate);
	}
	
	/**
	 * Sets the publishing date of this project.
	 * 
	 * @param the publishing date of this project.
	 */
	void setPublishingDate(long publishingDate) {
		m_publishingdate = publishingDate;
	}
	
	/**
	 * Gets the published-by value.
	 * 
	 * @return the published-by value.
	 */
	int getPublishedBy() {
		return m_publishedBy;
	}
	
	/**
	 * Sets the published-by value.
	 * 
	 * @param the published-by value.
	 */
	void setPublishedBy(int id) {
		m_publishedBy = id;
	}
       
    /**
	 * Returns the group of this project.
	 * 
	 * @return the group of this resource.
	 */
     public CmsGroup getGroup() {
         return  (CmsGroup)m_group.clone();
     }

         
    /**
	 * Returns the manager group of this project.
	 * 
	 * @return the manager group of this resource.
	 */
     public CmsGroup getManagerGroup() {
         return  (CmsGroup)m_managerGroup.clone();
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
	 * Sets the type.
	 * 
	 * @param the type.
	 */
	void setType(int id) {
		m_type = id;
	}
	
	
	/**
	 * Gets the counter for locked resources in this project.
	 * 
	 * @return the counter for locked resources in this project.
	 */
	public int getCountLockedResources() {
		return m_countLockedResources;
	}
	
	/**
	 * Increments the counter for locked resources in this project.
	 */
	void incrementCountLockedResources() {
		m_countLockedResources ++;
	}
	
	/**
	 * Decrements the counter for locked resources in this project.
	 */
	void decrementCountLockedResources() {
		m_countLockedResources --;
		if( m_countLockedResources < 0 ) {
			m_countLockedResources = 0;
		}
	}
	
	/**
	 * Clears the counter for locked resources in this project.
	 */
	void clearCountLockedResources() {
		m_countLockedResources = 0;
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
    * Clones the CmsProject by creating a new CmsProject Object.
    * @return Cloned CmsProject.
    */
    public Object clone() {
        CmsProject project=new CmsProject(this.m_id,new String (this.m_name),
                                       new String(m_description),this.m_taskId,
                                       this.m_ownerId,this.getGroup(),this.getManagerGroup(),
                                       this.m_flags,new Timestamp(this.m_createdate),
                                       new Timestamp(this.m_publishingdate),this.m_publishedBy,
									   this.m_countLockedResources, this.m_type);
        return project;    
    }  
}
