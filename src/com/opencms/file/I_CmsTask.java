package com.opencms.file;

/**
 * This interface describes a task in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/06 13:57:08 $
 */
public interface I_CmsTask
{
	/**
	 * This constant is used to order the tasks by date.
	 */
	public static int C_ORDER_BY_DATE =		1;

	/**
	 * This constant is used to order the tasks by name.
	 */
	public static int C_ORDER_BY_NAME =		2;
	
	// TODO: add order-criteria here.
	
	// TODO: add task-methods here.
	
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
