package com.opencms.file;

import com.opencms.core.*;

/**
 * This class describes a Cms user group and the methods to access it.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 1999/12/14 18:02:13 $
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
     public long getId(){
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