/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsGroup.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.6 $
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

import com.opencms.core.*;

/**
 * This class describes a Cms user group and the methods to access it.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2000/02/15 17:43:59 $
 */
 public class CmsGroup extends A_CmsGroup implements I_CmsConstants { 
    
    /**
     * The name of the user group.
     */
    private String m_name=null;
    
    /**
     * The id of the user group.
     */
    private int m_id=C_UNKNOWN_ID;
    
     /**
     * The parent id of the user group.
     */
    private int m_parentId=C_UNKNOWN_ID;
    
    /**
     * The description of the user group.
     */
	private String m_description=null;
    
    /**
     * The flags of the user group.
     */
	private int m_flags=C_UNKNOWN_INT;
	
     /**
     * Constructor, creates a new Cms group object.
     * 
     * @param id The id of the new group.
     * @param parent The parent group of the new group (or C_UNKNOWN_ID).
     * @param name The name of the new group.
     * @param description The description of the new group.
     * @param flags The flags of the new group.
     
     */
    public CmsGroup (int id, int parent,String name, String description, 
                     int flags) {
        m_id=id;
        m_name=name;
        m_description=description;
        m_flags=flags;
        m_parentId=parent;
    }  
    
    
	/**
	 * Returns the name of this group.
	 * 
	 * @return name The name of the group.
	 */
     public String getName(){
         return m_name;
     }
	
	/**
	 * Returns the id of a group. 
	 * 
	 * @return id The id of this group.
	 */
     public int getId(){
         return m_id;
     }
	
	/**
	 * Returns the description of this group.
	 * 
	 * @return description The description of this group.
	 */
     public String getDescription(){
         return m_description;
     }
	
    /**
     * Desides, if this group is disabled.
     * 
     * @return GROUP_FLAGS == C_FLAG_DISABLED
     */
     public boolean getDisabled() {
         boolean disabled=false;
         if (m_flags == C_FLAG_DISABLED) {
             disabled = true;
         }
         return disabled;
     }
	
	/**
	 * Returns the GROUP_FLAGS.
	 * 
	 * @return the GROUP_FLAGS.
	 */
     public int getFlags() {
         return m_flags;
     }

    /**
	 * Sets the GROUP_FLAGS.
	 * 
	 * @param flags The flags to be set.
	 * 
	 */
     void setFlags(int flags) {
         m_flags=flags;
     }
	 
    /**
     * Disables the flags by setting them to C_FLAG_DISABLED.
     */
    public void  setDisabled() {
        setFlags(C_FLAG_DISABLED);
    }
    
    /**
     * Enables the flags by setting them to C_FLAG_ENABLED.
     */
    public void  setEnabled() {
        setFlags(C_FLAG_ENABLED);
    }
	 
     
	/**
	 * Returns the id of the parent group of the actual Cms group object, 
	 * or C_UNKNOWN_ID.
	 * 
	 * @return PARENT_GROUP_ID or C_UNKNOWN_ID
	 */
     public int getParentId() {
      return m_parentId;         
     }

		
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
     public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[Group]:");
        output.append(m_name);
        output.append(" , Id=");
        output.append(m_id);
        output.append(" :");
        output.append(m_description);
        return output.toString();
      }
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
     public boolean equals(Object obj) {
         boolean equal=false;
        // check if the object is a CmsGroup object
        if (obj instanceof CmsGroup) {
            // same ID than the current user?
            if (((CmsGroup)obj).getId() == m_id){
                equal = true;
            }
        }
        return equal;
     }


}