/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsUser.java,v $
 * Date   : $Date: 2000/07/20 11:52:19 $
 * Version: $Revision: 1.25 $
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
 * @version $Revision: 1.25 $ $Date: 2000/07/20 11:52:19 $
 */

public class CmsUser implements I_CmsConstants, Cloneable {

    /**
     * The login-name of the user.
     */
    private String m_name = "";
    
    /**
     * The Id of this user.
     */
    private int m_id=C_UNKNOWN_ID;
    
    /**
     * The password of the user.
     */
    private String m_password = "";
     
    
    /**
     * The password of the user.
     */
    private String m_recoveryPassword = "";
     
    /**
     * The description of the user.
     */
    private String m_description = "";
        
     /**
     * A storage for additional user information.
     */
    private Hashtable m_additionalInfo = null;
    
    /**
     * The default group of this user.
     */
    private CmsGroup m_defaultGroup= null;
	
	/**
     * The default group ID of this user.
     */
    private int m_defaultGroupId= C_UNKNOWN_INT;
	
	/**
     * The section of the user.
     */
    private String m_section=null;
    
	
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
	 * The address of the user.
	 */
	private String m_address = "";
	
	
	/**
	 * The last login of the user.
	 */
	private long m_lastlogin = C_UNKNOWN_LONG;
    
    /**
	 * The typ of the user.
	 */
	private int m_type = C_UNKNOWN_INT;
	
    
     /**
     * Constructor, creates a new Cms user object.
     * 
     * @param id The id of the new user.
     * @param name The name of the new user.
     * @param description The description of the new user.
     */
    public CmsUser(int id, String name,String description) {
        m_id=id;
        m_name=name;
        m_description= description;
    }
    	 
    /**
     * Constructor, creates a new Cms user object.
     * 
     * @param id The id of the new user.
     * @param name The name of the new user.
     * @param description The description of the new user.
     */
	public CmsUser (int id, String name, String password, String recoveryPassword, String description, String firstname,
					String lastname, String email, long lastlogin, long lastused, int flags,
					Hashtable additionalInfo, CmsGroup defaultGroup, String address,
					String section, int typ) {
            
        m_id=id;
        m_name=name;
        m_password = password;
        m_recoveryPassword = recoveryPassword;
        m_description= description;
        m_firstname = firstname;
        m_lastname = lastname;
        m_email = email;
        m_lastlogin = lastlogin;
        m_lastused = lastused;
        m_flags  = flags;
        this.setDefaultGroup(defaultGroup);
        m_additionalInfo=additionalInfo;
        m_address = address;
        m_section = section;
        m_type = typ;
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
	 * Sets the description of this user.
	 * 
	 * @param the description of this user.
	 */
	public void setDescription(String value) {
		m_description = value;
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
    public CmsGroup getDefaultGroup() {
        return m_defaultGroup;
    }
     
    
    /**
     * Sets the default group object of this user.
     * 
     * @param defaultGroup The default group of this user.
     */
    public void setDefaultGroup(CmsGroup defaultGroup) {
        m_defaultGroup = defaultGroup;
        m_defaultGroupId = defaultGroup.getId();
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
	 * Gets the password.
	 * 
	 * @return the USER_PASSWORD, or null.
	 */
	public String getPassword() {
        return m_password;
    }
     
    /**
     * Sets the password.
	 * 
	 * @param The new password.
     */
    public void setPassword(String value) {
        m_password = value;
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
	 * Gets the address.
	 * 
	 * @return the USER_ADDRESS, or null.
	 */
	public String getAddress() {
         return m_address;
    }
    
     /**
	 * Sets the address.
     *	 
     * @param value The user adress.
	 */
    public void setAddress(String value) {
        m_address = value;
    }

	/**
	 * Gets the section of the user.
	 * 
	 * @return the USER_SECTION, or null.
	 */
	public String getSection() {
        return m_section;
    }
    
     /**
	 * Sets the section of the user.
	 * 
	 * @param value The new user section.
	 */
    public void setSection(String value) {
        m_section = value;
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
    public void setLastlogin(long value) {
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
	 * Gets the defaultgroup id.
	 * 
	 * @return the USER_DEFAULTGROUP_ID, or null.
	 */
	public int getDefaultGroupId() {
        return m_defaultGroupId;
    }
    
     /**
	 * Gets the type.
	 * 
	 * @return the type, or C_UNKNOWN_INT.
	 */
	public int getType() {
        return m_type;
    }
 
    /**
	 * Sets the typ.
	 * 
	 * @param value The new user typ.
	 */
     void setType(int value) {
         m_type = value;
     }
     
    
    /** 
    * Clones the CmsResource by creating a new CmsUser Object.
    * @return Cloned CmsUser.
    */
    public Object clone() {
        CmsUser user= new CmsUser(m_id,new String(m_name),new String(m_password),new String(m_recoveryPassword),
								  new String (m_description),new String(m_firstname),
								  new String(m_lastname),new String(m_email),m_lastlogin,
								  m_lastused, m_flags, getAdditionalInfo(),
								  m_defaultGroup, new String(m_address), new String(m_section),m_type);
        return user;   
    }
}
