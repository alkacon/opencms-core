package com.opencms.file;

import com.opencms.core.*;

/**
 * This class describes a project. A project is used to handle versions of 
 * one resource.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.4 $ $Date: 2000/01/13 12:27:38 $
 */
public class CmsProject extends A_CmsProject implements I_CmsConstants {
	
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
	 * The globetask_id for this project.
	 */
	private int m_globetaskId = C_UNKNOWN_ID;
	
	/**
	 * The name of this project.
	 */
	private String m_name = null;
	
	/**
	 * The description of this project.
	 */
	private String m_description = null;
	
	/**
	 * The state of this project.
	 */
	private int m_flags = C_PROJECT_STATE_UNLOCKED;

	CmsProject(int projectId, String name, String description, int globetaskId, 
			   int ownerId, int groupId, int flags) {
		m_id = projectId;
		m_name = name;
		m_description = description;
		m_globetaskId = globetaskId;
		m_ownerId = ownerId;
		m_groupId = groupId;
		m_flags = flags;
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
    int getId() {
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
	 * Returns the taskid of this project.
	 * 
	 * @return the taskid of this project.
	 */
    int getTaskId() {
		return(this.m_globetaskId);
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
}
