package com.opencms.file;

/**
 * This abstract class describes a project. A project is used to handle versions of 
 * one resource.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
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
	 * Returns the state of this project.<BR/>
	 * This may be C_STATE_UNLOCKED, C_STATE_LOCKED, C_STATE_ARCHIVE.
	 * 
	 * @return the state of this project.
	 */
	abstract public int getState();

	/**
	 * Returns the id of this project.
	 * 
	 * @return the id of this project.
	 */	
    abstract long getID();
	
	/**
	 * Returns the userid of the project owner.
	 * 
	 * @return the userid of the project owner.
	 */
	abstract long getOwnerID();
	
	/**
	 * Returns the groupid of this project.
	 * 
	 * @return the groupid of this project.
	 */
    abstract long getGroupID();
	
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

	/**
	 * Returns the hashcode for this object.
	 * 
	 * @return the hashcode for this object.
	 */
    abstract public int hashCode();    
}
