package com.opencms.file;

import com.opencms.core.*;

/**
 * This abstract class describes a task in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/23 16:49:21 $
 */
public class CmsTask extends A_CmsTask implements I_CmsConstants {
	
	// TODO: add task-methods here.
	
	/**
	 * Returns the id of this task.
	 * 
	 * @return the id of this task.
	 */
	int getId() {
		return( 1 ); // TODO: implement this
	}
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[Task]:");
        output.append(" Id=");
        output.append(getId());
        return output.toString();
	}
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
	public boolean equals(Object obj) {
        boolean equal=false;
        // check if the object is a CmsUser object
        if (obj instanceof CmsTask) {
            // same ID than the current user?
            if (((CmsTask)obj).getId() == this.getId()){
                equal = true;
            }
        }
        return equal;
	}

}
