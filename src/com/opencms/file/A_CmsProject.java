package com.opencms.file;

/**
 * This abstract class describes a project. A project is used to handle versions of 
 * one resource.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.5 $ $Date: 1999/12/17 17:25:36 $
 */
abstract public class A_CmsProject
{
	/**
	 * Returns the name of this project.
	 * 
	 * @return the name of this project.
	 */
    abstract public String getName();

	/**
	 * Returns the description of this project.
	 * 
	 * @return description The description of this project.
	 */
	abstract public String getDescription();

	/**
	 * Sets the description of this project.
	 * 
	 * @param description The description of this project.
	 */
	abstract public void setDescription(String description);
	
	/**
	 * Returns the state of this project.<BR/>
	 * This may be C_PROJECT_STATE_UNLOCKED, C_PROJECT_STATE_LOCKED, 
	 * C_PROJECT_STATE_ARCHIVE.
	 * 
	 * @return the state of this project.
	 */
	abstract public int getFlags();

	/**
	 * Returns the id of this project.
	 * 
	 * @return the id of this project.
	 */	
    abstract int getId();
	
	/**
	 * Returns the userid of the project owner.
	 * 
	 * @return the userid of the project owner.
	 */
	abstract int getOwnerId();
	
	/**
	 * Returns the groupid of this project.
	 * 
	 * @return the groupid of this project.
	 */
    abstract int getGroupId();
	
	/**
	 * Returns the taskid of this project.
	 * 
	 * @return the taskid of this project.
	 */
    abstract int getTaskId();
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	abstract public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
    abstract public boolean equals(Object obj);

}
