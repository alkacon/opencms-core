/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsUser.java,v $
 * Date   : $Date: 2000/02/19 17:05:41 $
 * Version: $Revision: 1.11 $
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
import com.opencms.core.*;


 /**
 * This class describes the Cms user object and the methods to access it.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.11 $ $Date: 2000/02/19 17:05:41 $
 */

public class CmsUser extends A_CmsUser implements I_CmsConstants,
                                                  Cloneable {

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
     * A storage for additional user information.
     */
    private Hashtable m_additionalInfo = null;
    
    /**
     * The default group of this user.
     */
    private A_CmsGroup m_defaultGroup= null;
	
	/**
	 * The flags of the user.
	 */
	private int m_flags = C_FLAG_ENABLED;
	
	/**
	 * The email of the user.
	 */
	private String m_email = "";
	
	/**
	 * The lastused date.
	 */
	private long m_lastused = C_UNKNOWN_LONG;
	
	/**
	 * The firstname of the user.
	 */
	private String m_firstname = "";
	
	/**
	 * The lastname of the user.
	 */
	private String m_lastname = "";
	
	/**
	 * The last login of the user.
	 */
	private long m_lastlogin = C_UNKNOWN_LONG;
    	 
    /**
     * Constructor, creates a new Cms user object.
     * 
     * @param id The id of the new user.
     * @param name The name of the new user.
     * @param description The description of the new user.
     */
     CmsUser (int id, String name, String description) {
            
        m_id=id;
        m_name=name;
        m_description=description;
        m_defaultGroup=null;
        m_additionalInfo=null;
        m_additionalInfo=new Hashtable();
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
	public int getId() {
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
     * Disables the user flags by setting them to C_FLAG_DISABLED.
     */
    public void  setDisabled() {
        setFlags(C_FLAG_DISABLED);
    }
    
    /**
     * Enables the user flags by setting them to C_FLAG_ENABLED.
     */
    public void  setEnabled() {
        setFlags(C_FLAG_ENABLED);
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
        output.append(" , flags=");
        output.append(getFlags());
        output.append(" :");
        output.append(m_description);
        return output.toString();
    }
	
    /**
     * Returns the default group object of this user.
     * 
     * @return Default Group of the user
     */
    public A_CmsGroup getDefaultGroup() {
        return m_defaultGroup;
    }
     
    
    /**
     * Sets the default group object of this user.
     * 
     * @param defaultGroup The default group of this user.
     */
    void setDefaultGroup(A_CmsGroup defaultGroup) {
        m_defaultGroup = defaultGroup;
        m_additionalInfo.put(C_ADDITIONAL_INFO_DEFAULTGROUP_ID, 
							 new Integer(defaultGroup.getId()));

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
	 * Sets additional information about the user. <BR/>
	 * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
	 * 
	 * 
	 * @param key The key to the additional information.
	 * @param obj The additinoal information value.
	 * 
	 */
    public void setAdditionalInfo(String key, Object obj)  {
        m_additionalInfo.put(key,obj);
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
	 * Sets the  complete Hashtable with additional information about the user. <BR/>
	 * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
	 * 
	 * This method has package-visibility for security-reasons.
	 * It is required to because of the use of two seprate databases for user data and
	 * additional user data.
	 * 
	 */
    void setAdditionalInfo(Hashtable additionalInfo) {
        m_additionalInfo=additionalInfo;
    }
    
	/**
	 * Gets the email.
	 * 
	 * @return the USER_EMAIL, or null.
	 */
	public String getEmail() {
        return m_email;
    }
     
    /**
     * Sets the email.
	 * 
	 * @param The new email adress.
     */
    public void setEmail(String value) {
        m_email = value;
    }

	/**
	 * Gets the firstname.
	 * 
	 * @return the USER_FIRSTNAME, or null.
	 */
	public String getFirstname() {
		return m_firstname ;
    }

	/**
	 * Sets the firstname.
	 * 
	 * @param the USER_FIRSTNAME.
	 */
	public void setFirstname(String firstname) {
		m_firstname = firstname;
    }
	
	/**
	 * Gets the lastname.
	 * 
	 * @return the USER_SURNAME, or null.
	 */
	public String getLastname() {
        return m_lastname;
    }
	
	/**
	 * Gets the lastname.
	 * 
	 * @return the USER_SURNAME, or null.
	 */
	public void setLastname(String lastname) {
        m_lastname = lastname;
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
	 * This is a shortcut for: <pre>setAdditionalInfo(C_ADDITIONAL_INFO_ADDRESS,value);</pre>
     *	 
     * @param value The user adress.
	 */
    public void setAddress(String value) {
        m_additionalInfo.put(C_ADDITIONAL_INFO_ADDRESS,value);
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
	 * This is a shortcut for: <pre>setAdditionalInfo(C_ADDITIONAL_INFO_SECTION,value);</pre>
	 * 
	 * @param value The new user section.
	 */
    public void setSection(String value) {
        m_additionalInfo.put(C_ADDITIONAL_INFO_SECTION,value);
    }
    
    /**
	 * Gets the lastlogin.
	 * 
	 * @return the USER_LASTLOGIN, or C_UNKNOWN_LONG.
	 */
	public long getLastlogin() {
        return m_lastlogin;
    }

    /**
	 * Sets the lastlogin.
	 * 
	 * @param value The new user section.
	 */
    void setLastlogin(long value) {
        m_lastlogin = value;
    }
    
    /**
	 * Gets the lastlogin.
	 * 
	 * @return the USER_LASTLOGIN, or C_UNKNOWN_LONG.
	 */
	public long getLastUsed() {
        return m_lastused;
    }

    /**
	 * Sets the lastlogin.
	 * 
	 * @param value The new user section.
	 */
    void setLastUsed(long value) {
        m_lastused = value;
    }
	
     /**
	 * Gets the flags.
	 * 
	 * @return the USER_FLAGS, or C_UNKNOWN_INT.
	 */
	public int getFlags() {
        return m_flags;
    }
 
    /**
	 * Sets the flags.
	 * 
	 * @param value The new user flags.
	 */
     void setFlags(int value) {
         m_flags = value;
     }
       
     /**
	 * This is a shortcut for: <pre>getAdditionalInfo(C_ADDITIONAL_INFO_DEFAULTGROUP_ID);</pre>
	 * 
	 * @return the USER_DEFAULTGROUP_ID, or null.
	 */
	public int getDefaultGroupId() {
        int value=C_UNKNOWN_ID;
        value =((Integer)m_additionalInfo.get(C_ADDITIONAL_INFO_DEFAULTGROUP_ID)).intValue();
        return value;
    }
    
    /** 
    * Clones the CmsResource by creating a new CmsUser.
    * @return Cloned CmsUser.
    */
    public Object clone() {
        CmsUser user= new CmsUser(m_id,m_name,m_description);
        user.setDefaultGroup(this.getDefaultGroup());
        user.setAdditionalInfo(this.getAdditionalInfo());
        return user;   
    }
    
}
