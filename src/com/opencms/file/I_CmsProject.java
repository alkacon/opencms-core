package com.opencms.file;

/**
 * This interface describes a project. A project is used to handle versions of one
 * resource.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/07 17:25:04 $
 */
public interface I_CmsProject
{
	/**
	 * This constant defines the onlineproject. This is the project which
	 * is used to show the resources for guestusers
	 */
	public static final String C_ONLINE_PROJECTNAME	= "Onlineproject";
	
	/**
	 * This constant defines a unlocked project. 
	 * Resources may be changed in this project.
	 */
	public static final int C_STATE_UNLOCKED			= 0;

	/**
	 * This constant defines a locked project.
	 * Resources can't be changed in this project.
	 */
	public static final int C_STATE_LOCKED			= 1;

	/**
	 * This constant defines a project in a archive.
	 * Resources can't be changed in this project. Its state will never
	 * go back to the previos one.
	 */
	public static final int C_STATE_ARCHIVE			= 2;

	/**
	 * Returns the name of this project.
	 * 
	 * @return the name of this project.
	 */
    public String getName();

	/**
	 * Returns the description of this project.
	 * 
	 * @return description The description of this project.
	 */
	public String getDescription();

	/**
	 * Returns the state of this project.<BR>
	 * This may be C_STATE_UNLOCKED, C_STATE_LOCKED, C_STATE_ARCHIVE.
	 * 
	 * @return the state of this project.
	 */
	public int getState();

	/**
	 * Returns the id of this project.
	 * 
	 * @return the id of this project.
	 */	
    long getID();
	
	/**
	 * Returns the userid of the project owner.
	 * 
	 * @return the userid of the project owner.
	 */
	long getOwnerID();
	
	/**
	 * Returns the groupid of this project.
	 * 
	 * @return the groupid of this project.
	 */
    long getGroupID();
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
    public boolean equals(Object obj);

	/**
	 * Returns the hashcode for this object.
	 * 
	 * @return the hashcode for this object.
	 */
    public int hashCode();    
}
