/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsUser.java,v $
* Date   : $Date: 2003/05/15 12:39:34 $
* Version: $Revision: 1.35 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import com.opencms.core.A_OpenCms;
import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;

import java.util.Hashtable;


 /**
 * Describes the Cms user object and the methods to access it.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.35 $ $Date: 2003/05/15 12:39:34 $
 */

public class CmsUser implements I_CmsConstants, Cloneable {

    /**
     * The login-name of the user.
     */
    private String m_name = "";

    /**
     * The Id of this user.
     */
    private CmsUUID m_id;

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
    private CmsGroup m_defaultGroup = null;

    /**
     * The default group ID of this user.
     */
    private CmsUUID m_defaultGroupId;

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
     * Defines if the user is a webuser or a systemuser.
     * C_USER_TYPE_SYSTEMUSER for systemuser (incl. guest).
     * C_USER_TYPE_WEBUSER for webuser.
     */
    private int m_type = C_UNKNOWN_INT;


     /**
     * Constructor, creates a new Cms user object.
     *
     * @param id The id of the new user.
     * @param name The name of the new user.
     * @param description The description of the new user.
     */
    public CmsUser(CmsUUID id, String name,String description) {
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
    public CmsUser (CmsUUID id, String name, String password, String recoveryPassword, String description, String firstname,
                    String lastname, String email, long lastlogin, long lastused, int flags,
                    Hashtable additionalInfo, CmsGroup defaultGroup, String address,
                    String section, int type) {

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
        m_type = type;
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
    
    /**
     * Compares the given object with this user.<p>
     *
     * @return true if the object is equal, false otherwise
     */
    public boolean equals(Object obj) {
        // check if the object is a CmsUser object
        if (! (obj instanceof CmsUser)) return false;
        // same ID than the current user?
        return (((CmsUser)obj).getId().equals(m_id)); 
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
     * Returns additional information about the user which are usually set 
     * in the users preferences.<p>
     *
     * @param key the key to the additional information.
     * @return additional information Object about the user, if the additional info
     * does not exists, it returns <code>null</code>
     */
    public Object getAdditionalInfo(String key) {
        Object value = m_additionalInfo.get(key);
        if(value == null && C_ADDITIONAL_INFO_STARTSETTINGS.equals(key)){
            Hashtable startSettings = new Hashtable();
            startSettings.put(C_START_LANGUAGE, A_OpenCms.getUserDefaultLanguage());
            startSettings.put(C_START_PROJECT, new Integer(C_PROJECT_ONLINE_ID));
            startSettings.put(C_START_VIEW, "/system/workplace/action/explorer.html");
            startSettings.put(C_START_DEFAULTGROUP, this.getDefaultGroup().getName());
            startSettings.put(C_START_LOCKDIALOG, "");
            startSettings.put(C_START_ACCESSFLAGS, new Integer(A_OpenCms.getUserDefaultAccessFlags()));
            m_additionalInfo.put(key, startSettings);
            value = startSettings;
       }        
        return value;
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
     * Returns the default group object of this user.
     *
     * @return Default Group of the user
     */
    public CmsGroup getDefaultGroup() {
        return m_defaultGroup;
    }
     /**
     * Gets the defaultgroup id.
     *
     * @return the USER_DEFAULTGROUP_ID, or null.
     */
    public CmsUUID getDefaultGroupId() {
        return m_defaultGroupId;
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
     * Gets the email.
     *
     * @return the USER_EMAIL, or null.
     */
    public String getEmail() {
        return m_email;
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
     * Gets the flags.
     *
     * @return the USER_FLAGS, or C_UNKNOWN_INT.
     */
    public int getFlags() {
        return m_flags;
    }
    /**
     * Gets the id of this user.
     *
     * @return the id of this user.
     */
    public CmsUUID getId() {
        return m_id;
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
     * Gets the lastname.
     *
     * @return the USER_SURNAME, or null.
     */
    public String getLastname() {
        return m_lastname;
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
     * Gets the login-name of the user.
     *
     * @return the login-name of the user.
     */
    public String getName() {
        return m_name;
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
     * Gets the recovery password.
     *
     * @return the USER_RECOVERY_PASSWORD, or null.
     */
    public String getRecoveryPassword() {
        return m_recoveryPassword;
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
     * Gets the type of the user (webuser or a systemuser).
     * C_USER_TYPE_SYSTEMUSER for systemuser (incl. guest).
     * C_USER_TYPE_WEBUSER for webuser.
     *
     * @return the type, or C_UNKNOWN_INT.
     */
    public int getType() {
        return m_type;
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
     * Sets the address.
     *
     * @param value The user adress.
     */
    public void setAddress(String value) {
        m_address = value;
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
     * Sets the description of this user.
     *
     * @param the description of this user.
     */
    public void setDescription(String value) {
        m_description = value;
    }
    /**
     * Disables the user flags by setting them to C_FLAG_DISABLED.
     */
    public void  setDisabled() {
        setFlags(C_FLAG_DISABLED);
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
     * Enables the user flags by setting them to C_FLAG_ENABLED.
     */
    public void  setEnabled() {
        setFlags(C_FLAG_ENABLED);
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
     * Sets the flags.
     *
     * @param value The new user flags.
     */
     void setFlags(int value) {
         m_flags = value;
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
     * Gets the lastname.
     *
     * @return the USER_SURNAME, or null.
     */
    public void setLastname(String lastname) {
        m_lastname = lastname;
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
     * Sets the password.
     *
     * @param The new password.
     */
    public void setPassword(String value) {
        m_password = value;
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
     * Sets the typ.
     *
     * @param value The new user typ.
     */
     void setType(int value) {
         m_type = value;
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
        output.append(" , type=");
        output.append(getType());
        output.append(" :");
        output.append(m_description);
        return output.toString();
    }
    
}
