package com.opencms.file;

import java.util.*;
import com.opencms.core.*;


 /**
 * This class describes the Cms user object and the methods to access it.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 1999/12/14 18:02:14 $
 */

public class CmsUser extends A_CmsUser implements I_CmsConstants {

    /**
     * The login-name of the user.
     */
    private String m_name = null;
    
    /**
     * The Id of this user.
     */
    private int m_id=C_UNKNOWN_ID;
    
    /**
     * The description of the user.
     */
    private String m_description=null;
        
    /**
     * The current group of the user.
     */
	private A_CmsGroup m_currentGroup = null;
    
    /**
     * A storage for additional user information.
     */
    private Hashtable m_additionalInfo = null;
    
    
    /**
     * Constructor, creates a new Cms user object.
     * 
     * @param id The id of the new user.
     * @param name The name of the new user.
     * @param description The description of the new user.
     * @param flags The flags of the new user.
     * @param group The default user group of the new user.
     * @param info A Hashtable with additional user information.
     */
    public CmsUser (int id, String name, String description, int flags,
                    A_CmsGroup group, Hashtable info) {
        m_id=id;
        m_name=name;
        m_description=description;
        m_currentGroup=group;
        m_additionalInfo=info;  
          
        //add aditional infos in the hastable
        if (m_additionalInfo == null) {
            m_additionalInfo=new Hashtable();
        }
        m_additionalInfo.put(C_ADDITIONAL_INFO_DEFAULTGROUP,group);
        m_additionalInfo.put(C_ADDITIONAL_INFO_FLAGS,new Integer(flags));
        m_additionalInfo.put(C_ADDITIONAL_INFO_LASTLOGIN,new Long(0));
    }
    
    
	/**
	 * Gets the login-name of the user.
	 * 
	 * @return the login-name of the user.
	 */
    public String getName() {
        return m_name;
    }
                                                     
	
	/**
	 * Gets the id of this user.
	 * 
	 * @return the id of this user.
	 */
	public long getId() {
        return m_id;
    }
	
	/**
	 * Gets the description of this user.
	 * 
	 * @return the description of this user.
	 */
	public String getDescription() {
    return m_description;
    }
	
    /**
     * Decides, if this user is disabled.
     * 
     * @return USER_FLAGS == C_FLAG_DISABLED
     */
    public boolean getDisabled() {
        boolean disabled=false;
        if (getFlags() == C_FLAG_DISABLED) {
            disabled=true;
        }
        return disabled;
    }
	

	/**
	 * Returns the current group for this user.
	 * 
	 * @return the current group for this user.
	 */
	public A_CmsGroup getCurrentGroup() {
        return m_currentGroup;
    }

	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[User]:");
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
        // check if the object is a CmsUser object
        if (obj instanceof CmsUser) {
            // same ID than the current user?
            if (((CmsUser)obj).getId() == m_id){
                equal = true;
            }
        }
        return equal;
    }

		
	/**
	 * Returns additional information about the user. <BR/>
	 * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
	 * 
	 * The additional infos must be requested via the CmsObject.
	 * 
	 * 
	 * @param key the key to the additional information.
	 * 
	 * Returns additional information about the user. If the additional info
	 * does not exists, it returns null.
	 * 
	 */
	public Object getAdditionalInfo(String key) {
        Object value=null;
        value =m_additionalInfo.get(key);
        return value;
    }
	
     /**
	 * Returns the complete Hashtable with additional information about the user. <BR/>
	 * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
	 * 
	 * The additional infos must be requested via the CmsObject.
	 * 
	 * 
	 * Returns additional information about the user. 
	 * 
	 */
    public Hashtable getAdditionalInfo() {
        return  m_additionalInfo;
    }
         
    
	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_EMAIL);</pre>
	 * 
	 * @return the USER_EMAIL, or null.
	 */
	public String getEmail() {
        String value=null;
        value =(String)m_additionalInfo.get(C_ADDITIONAL_INFO_EMAIL);
        return value;
    }

	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_FIRSTNAME);</pre>
	 * 
	 * @return the USER_FIRSTNAME, or null.
	 */
	public String getFirstname() {
        String value=null;
        value =(String)m_additionalInfo.get(C_ADDITIONAL_INFO_FIRSTNAME);
        return value;
    }

	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_LASTNAME);</pre>
	 * 
	 * @return the USER_SURNAME, or null.
	 */
	public String getLastname() {
        String value=null;
        value =(String)m_additionalInfo.get(C_ADDITIONAL_INFO_LASTNAME);
        return value;
    }
	
	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_ADDRESS);</pre>
	 * 
	 * @return the USER_ADDRESS, or null.
	 */
	public String getAddress() {
        String value=null;
        value =(String)m_additionalInfo.get(C_ADDITIONAL_INFO_ADDRESS);
        return value;
    }

	/**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_SECTION);</pre>
	 * 
	 * @return the USER_SECTION, or null.
	 */
	public String getSection() {
        String value=null;
        value =(String)m_additionalInfo.get(C_ADDITIONAL_INFO_SECTION);
        return value;
    }
    
    /**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_LASTLOGIN);</pre>
	 * 
	 * @return the USER_LASTLOGIN, or C_UNKNOWN_LONG.
	 */
	public long getLastlogin() {
        long value=C_UNKNOWN_LONG;
        Long lastlogin=null;
        lastlogin =((Long)m_additionalInfo.get(C_ADDITIONAL_INFO_LASTLOGIN));
        if (lastlogin != null) {
            value=lastlogin.longValue();
        }
        return value;
    }

    
     /**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_FLAGS);</pre>
	 * 
	 * @return the USER_FLAGS, or C_UNKNOWN_INT.
	 */
	public int getFlags() {
        int value=C_UNKNOWN_INT;
        Integer flags=null;
        flags =((Integer)m_additionalInfo.get(C_ADDITIONAL_INFO_FLAGS));
        if (flags != null) {
            value=flags.intValue();
        }
        return value;
    }
 
       
     /**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_DEFAULTGROUP);</pre>
	 * 
	 * @return the USER_DEFAULTGROUP, or null.
	 */
	public A_CmsGroup getDefaultGroup() {
        A_CmsGroup value=null;
        value =(A_CmsGroup)m_additionalInfo.get(C_ADDITIONAL_INFO_DEFAULTGROUP);
        return value;
    }

}

    

