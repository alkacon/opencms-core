package com.opencms.file;

/**
 * This abstract class describes a task in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/16 18:55:53 $
 */
public abstract class A_CmsTask
{
	
	// TODO: add task-methods here.
	
	/**
	 * Returns the id of this task.
	 * 
	 * @return the id of this task.
	 */
	abstract int getId();
	
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
