package com.opencms.file;

import java.util.*;

/**
 * This abstract class describes a group in the Cms.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.5 $ $Date: 1999/12/15 16:43:21 $
 */
abstract public class A_CmsGroup { 
	
	/**
	 * Returns the name of this group.
	 * 
	 * @return name The name of the group.
	 */
	abstract public String getName();
	
	/**
	 * Returns the id of a group. 
	 * 
	 * @return id The id of this group.
	 */
	abstract public int getId();
	
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
	 * Returns the id of the parent group of the actual Cms group object, 
	 * or C_UNKNOWN_ID.
	 * 
	 * @return PARENT_GROUP_ID or C_UNKNOWN_ID
	 */
	abstract public int getParentId();

   
		
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
