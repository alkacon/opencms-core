package com.opencms.file;

import java.util.*;

/**
 * This abstract class describes a group in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
 */
abstract public class A_CmsGroup { 
	
	/**
	 * Returns the name of this group.
	 * 
	 * @return name The name of the group.
	 */
	abstract public String getName();
	
	/**
	 * Returns the id of a group. This method has the package-visibility.
	 * 
	 * @return id The id of this group.
	 */
	abstract long getID();
	
	/**
	 * Returns the description of this group.
	 * 
	 * @return description The description of this group.
	 */
	abstract public String getDescription();
	
    /**
     * Desides, if this group is disabled.
     * 
     * @return GROUP_FLAGS == C_FLAG_DISABLED
     */
	abstract public boolean getDisabled();    
	
	/**
	 * Returns the GROUP_FLAGS.
	 * 
	 * @return the GROUP_FLAGS.
	 */
	abstract public int getFlags();
	
	/**
	 * Decides, if this group has a parent.
	 * 
	 * @return PARENT_GROUP_ID != null
	 */
	abstract public boolean hasParent();

	/**
	 * Decides, if this group has a child(s).
	 * 
	 * @return true, if this group has childs, else return false.
	 */
	abstract public boolean hasChild();
	
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
	
	// the following methods are not used, because the functionality is handled by
	// a I_CmsObjectBase:
	/*
    abstract public boolean isAdminGroup();
    abstract public Vector getUsers();
    abstract public boolean contains(I_CmsUser user);
	*/
}
