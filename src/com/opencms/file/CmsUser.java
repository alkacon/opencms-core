package com.opencms.file;

import java.util.*;
import com.opencms.core.*;


 /**
 * This class describes the Cms user object and the methods to access it.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2000/01/03 18:51:36 $
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
     * A storage for additional user information.
     */
    private Hashtable m_additionalInfo = null;
    
    /**
     * The default group of this user.
     */
    private A_CmsGroup m_defaultGroup= null;
    
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
        m_additionalInfo=info;  
        m_defaultGroup=group;          
        
        //add aditional infos in the hashtable
        if (m_additionalInfo == null) {
            m_additionalInfo=new Hashtable();
        }
        m_additionalInfo.put(C_ADDITIONAL_INFO_DEFAULTGROUP_ID,new Integer(group.getId()));
        m_additionalInfo.put(C_ADDITIONAL_INFO_FLAGS,new Integer(flags));
        m_additionalInfo.put(C_ADDITIONAL_INFO_LASTLOGIN,new Long(0));
    }
    
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
    void  setDisabled() {
        setFlags(C_FLAG_DISABLED);
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
    void setAdditionalInfo(String key, Object obj)  {
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
     * This is a shortcut for: <pre>setAdditionalInfo(C_ADDITIONAL_INFO_EMAIL,value);</pre>
	 * 
	 * @param The new email adress.
     */
    void setEmail(String value) {
        m_additionalInfo.put(C_ADDITIONAL_INFO_EMAIL,value);
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
	 * This is a shortcut for: <pre>setAdditionalInfo(C_ADDITIONAL_INFO_ADDRESS,value);</pre>
     *	 
     * @param value The user adress.
	 */
    void setAddress(String value) {
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
    void setSection(String value) {
        m_additionalInfo.put(C_ADDITIONAL_INFO_SECTION,value);
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
	 * This is a shortcut for: <pre>setAdditionalInfo(C_ADDITIONAL_INFO_LASTLOGIN,new Long(value));</pre>
	 * 
	 * @param value The new user section.
	 */
    void setLastlogin(long value) {
        m_additionalInfo.put(C_ADDITIONAL_INFO_LASTLOGIN,new Long(value));
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
	 * This is a shortcut for: <pre>serAdditionalInfo(C_ADDITIONAL_INFO_FLAGS,new Integer(value));</pre>
	 * 
	 * @param value The new user flags.
	 */
     void setFlags(int value) {
         m_additionalInfo.put(C_ADDITIONAL_INFO_FLAGS, new Integer(value));
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

}

    

