/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/A_CmsGroup.java,v $
 * Date   : $Date: 2000/04/06 12:39:03 $
 * Version: $Revision: 1.10 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.file;

import java.util.*;

/**
 * This abstract class describes a group in the Cms.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.10 $ $Date: 2000/04/06 12:39:03 $
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
     * Decides, if this group is disabled.
     * 
     * @return GROUP_FLAGS == C_FLAG_DISABLED
     */
	abstract public boolean getDisabled();   
	
	/**
     * Is the Flag Projectmanager set?
     * 
     * @return true if C_FLAG_GROUP_PROJECTMANAGER is set 
     */
	abstract public boolean getProjectmanager();   

	/**
     * Is the Flag ProjectCoWorker set?
     * 
     * @return true if C_FLAG_GROUP_PROJECTCOWORKER is set 
     */
	abstract public boolean getProjectCoWorker();   

	/**
     * Is the Flag Role set?
     * 
     * @return true if C_FLAG_GROUP_ROLE is set 
     */	abstract public boolean getRole();   
	
	/**
	 * Returns the GROUP_FLAGS.
	 * 
	 * @return the GROUP_FLAGS.
	 */
	abstract public int getFlags();

     /**
	 * Sets the GROUP_FLAGS.
	 */
	abstract void setFlags(int flags);
    
    /**
     * Disables the flags by setting them to C_FLAG_DISABLED.
     */
    abstract public void  setDisabled();
    
    /**
     * Enables the flags by setting them to C_FLAG_ENABLED.
     */
    abstract public void  setEnabled();
	
	abstract public void setProjectManager(boolean yes);
	abstract public void setProjectCoWorker(boolean yes); 
	abstract public void setRole(boolean yes);
	
	

	/**
	 * Returns the id of the parent group of the actual Cms group object, 
	 * or C_UNKNOWN_ID.
	 * 
	 * @return PARENT_GROUP_ID or C_UNKNOWN_ID
	 */
	abstract public int getParentId();

	/**
	 * Sets the id of the parent group of the actual Cms group object, 
	 * or C_UNKNOWN_ID.
	 * 
	 * @param id the id of the parent-group.
	 */
	abstract void setParentId(int id);
   
		
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
	 * Sets the description of this group.
	 * 
	 * @param description The description of this group.
	 */
	abstract public void setDescription(String description);

}
