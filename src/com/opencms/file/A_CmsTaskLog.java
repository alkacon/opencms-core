package com.opencms.file;

import java.util.*;


/**
 * This abstract class describes a tasklog in the Cms.
 * 
 * @author Ruediger Gutfleisch
 * @version $Revision: 1.2 $ $Date: 2000/01/28 18:46:41 $
 */
public abstract class A_CmsTaskLog
{
	/**
	 * Returns the id of this task.
	 * 
	 * @return the id of this task.
	 */
	abstract int getId();
	

	abstract String getComment();
	abstract int getUser();
	abstract int getType();
	abstract java.sql.Timestamp getStartTime();

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
