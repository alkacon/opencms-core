package com.opencms.file;

import java.util.*;

/**
 * This interface describes a group in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/03 11:57:10 $
 */
public interface I_CmsGroup extends I_CmsFlags { 
	
	/**
	 * This is the group for guests.
	 */
	public static String C_GUESTGROUP = "Guests";
	
	/**
	 * This is the group for administrators.
	 */
	public static String C_ADMINGROUP = "Administrators";
	
	/**
	 * This is the group for projectleaders. It is the only group, which
	 * can create new projects.
	 */
	public static String C_PROJECTLEADERGROUP = "Projectleader";
	
	/**
	 * Returns the name of this group.
	 * 
	 * @return name The name of the group.
	 */
	public String getName();
	
	/**
	 * Returns the id of a group. This method has the package-visibility.
	 * 
	 * @return id The id of this group.
	 */
	int getID();
	
	/**
	 * Returns the description of this group.
	 * 
	 * @return description The description of this group.
	 */
	public String getDescription();
	
    /**
     * Desides, if this group is disabled.
     * 
     * @return GROUP_FLAGS == C_FLAG_DISABLED
     */
	public boolean getDisabled();    
	
	/**
	 * Returns the GROUP_FLAGS.
	 * 
	 * @return the GROUP_FLAGS.
	 */
	public int getFlags();
	
	/**
	 * Decides, if this group has a parent.
	 * 
	 * @return PARENT_GROUP_ID != null
	 */
	public boolean hasParent();

	/**
	 * Decides, if this group has a child(s).
	 * 
	 * @return true, if this group has childs, else return false.
	 */
	public boolean hasChild();
	
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
	
	// the following methods are not used, because the functionality is handled by
	// a I_CmsObjectBase:
	/*
    public boolean isAdminGroup();
    public Vector getUsers();
    public boolean contains(I_CmsUser user);
	*/
}
