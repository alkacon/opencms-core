package com.opencms.file;

/**
 * This abstract class describes a task in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
 */
public abstract class A_CmsTask
{
	
	// TODO: add task-methods here.
	
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
