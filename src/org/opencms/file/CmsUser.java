/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsUser.java,v $
 * Date   : $Date: 2004/12/22 16:36:45 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.util.Map;

/**
 * A user in the OpenCms system.<p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.10 $
 */
public class CmsUser implements I_CmsPrincipal, Cloneable {

    /** A storage for additional user information. */
    private Map m_additionalInfo;

    /** The address of this user. */
    private String m_address;

    /** The description of the user. */
    private String m_description;

    /**  The email of the user. */
    private String m_email;

    /** The first name of this user. */
    private String m_firstname;

    /** The flags of the user. */
    private int m_flags;

    /** The id of this user. */
    private CmsUUID m_id;

    /** The last login date of this user. */
    private long m_lastlogin;

    /** The last name of this user. */
    private String m_lastname;

    /** The unique user name of this user. */
    private String m_name;

    /** The password of this user. */
    private String m_password;
    
    /** Boolean flag whether the last-login timestamp of this user was modified. */
    private boolean m_isTouched;    

    /**
     * Defines if the user is a webuser or a systemuser.<p>
     * 
     * C_USER_TYPE_SYSTEMUSER for systemuser (incl. guest).
     * C_USER_TYPE_WEBUSER for webuser.
     */
    private int m_type;

    /**
     * Creates a new Cms user object.<p>
     *
     * @param id the id of the new user
     * @param name the name of the new user
     * @param description the description of the new user
     */
    public CmsUser(CmsUUID id, String name, String description) {     
        m_id = id;
        m_name = name;
        m_description = description;        
        m_additionalInfo = null;
        m_address = "";
        m_email = "";
        m_firstname = "";
        m_flags = I_CmsConstants.C_FLAG_ENABLED;
        m_lastlogin = I_CmsConstants.C_UNKNOWN_LONG;            
        m_lastname = "";
        m_password = "";
        m_type = I_CmsConstants.C_UNKNOWN_INT;    
        m_isTouched = false;             
    }

    /**
     * Creates a new CmsUser object.<p>
     * @param id the id of the new user
     * @param name the name of the new user
     * @param password the password of the user
     * @param description the description of the new user
     * @param firstname the first name
     * @param lastname the last name
     * @param email the email address
     * @param lastlogin time stamp 
     * @param flags flags
     * @param additionalInfo user related information
     * @param address the address
     * @param type the type of this user
     */
    public CmsUser(
        CmsUUID id, 
        String name, 
        String password, 
        String description, 
        String firstname, 
        String lastname, 
        String email, 
        long lastlogin, 
        int flags, 
        Map additionalInfo, 
        String address, 
        int type
    ) {
        m_id = id;
        m_name = name;
        m_password = password;
        m_description = description;
        m_firstname = firstname;
        m_lastname = lastname;
        m_email = email;
        m_lastlogin = lastlogin;
        m_flags = flags;
        m_additionalInfo = additionalInfo;
        m_address = address;
        m_type = type;
    }
    
    /**
     * Returns <code>true</code> if the provided user type indicates a system user type.<p>
     * 
     * @param type the user type
     * @return true if the provided user type indicates a system user type
     */      
    public static boolean isSystemUser(int type) {
        return (type & 1) > 0;
    }
    
    /**
     * Returns <code>true</code> if the provided user type indicates a web user type.<p>
     * 
     * @param type the user type
     * @return true if the provided user type indicates a web user type
     */    
    public static boolean isWebUser(int type) {
        return (type & 2) > 0;
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {
        CmsUser user = new CmsUser(m_id, new String(m_name), new String(m_password), new String(m_description), new String(m_firstname), new String(m_lastname), new String(m_email), m_lastlogin, m_flags, getAdditionalInfo(), new String(m_address), m_type);
        return user;
    }

    
    /**
     * Delete additional information about the user. <p>
     * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
     *
     * @param key The key of the additional information to delete
     */
    public void deleteAdditionalInfo(String key) {
        m_additionalInfo.remove(key);
    }

    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // check if the object is a CmsUser object
        if (!(obj instanceof CmsUser)) {
            return false;
        }
        // same ID than the current user?
        return (((CmsUser)obj).getId().equals(m_id));
    }

    /**
     * Returns the complete Hashtable with additional information about the user.<p>
     * Additional infos are for example emailadress, adress or surname...
     *
     * The additional infos must be requested via the CmsObject.
     *
     * @return additional information about the user
     *
     */
    public Map getAdditionalInfo() {
        return m_additionalInfo;
    }

    /**
     * Returns additional information about the user which are usually set 
     * in the users preferences.<p>
     *
     * @param key the key to the additional information
     * @return additional information Object about the user, if the additional info
     * does not exists, it returns <code>null</code>
     */
    public Object getAdditionalInfo(String key) {
        Object value = m_additionalInfo.get(key);
       /* if (value == null && I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS.equals(key)) {
            Hashtable startSettings = new Hashtable();

            startSettings = OpenCms.getWorkplaceManager().getDefaultUserAdditionalInfos();

            m_additionalInfo.put(key, startSettings);
            value = startSettings;
        }*/
        return value;
    }

    /**
     * Gets the address.<p>
     *
     * @return the USER_ADDRESS, or null
     */
    public String getAddress() {
        return m_address;
    }

    /**
     * Gets the description of this user.<p>
     *
     * @return the description of this user
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * Decides if this user is disabled.<p>
     *
     * @return USER_FLAGS == C_FLAG_DISABLED
     */
    public boolean getDisabled() {
        boolean disabled = false;
        if (getFlags() == I_CmsConstants.C_FLAG_DISABLED) {
            disabled = true;
        }
        return disabled;
    }

    /**
     * Returns the email address of this user.<p>
     *
     * @return the email address of this user
     */
    public String getEmail() {
        return m_email;
    }

    /**
     * Returns the firstname of this user.<p>
     *
     * @return the firstname of this user
     */
    public String getFirstname() {
        return m_firstname;
    }

    /**
     * Returns the flags of this user.<p>
     *
     * @return the flags of this user
     */
    public int getFlags() {
        return m_flags;
    }

    /**
     * Returns the id of this user.<p>
     *
     * @return the id of this user
     */
    public CmsUUID getId() {
        return m_id;
    }

    /**
     * Returns the time of the last login of this user.<p>
     *
     * @return the time of the last login of this user, or C_UNKNOWN_LONG
     */
    public long getLastlogin() {
        return m_lastlogin;
    }

    /**
     * Returns the lastname of this user.<p>
     *
     * @return the lastname of this user
     */
    public String getLastname() {
        return m_lastname;
    }

    /**
     * Gets the (login) name of this user.<p>
     *
     * @return the (login) name of this user
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the encrypted user password.<p>
     *
     * @return the encrypted user password
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * Gets the type of the user (webuser or a systemuser).
     * C_USER_TYPE_SYSTEMUSER for systemuser (incl. guest).
     * C_USER_TYPE_WEBUSER for webuser.<p>
     *
     * @return the type, or C_UNKNOWN_INT
     */
    public int getType() {
        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (m_id != null) {
            return m_id.hashCode();
        } 
        return CmsUUID.getNullUUID().hashCode();
    }
    
    /**
     * Returns <code>true</code> if this user is the default guest user.<p>
     * 
     * @return true if this user is the default guest user
     */
    public boolean isGuestUser() {
        return OpenCms.getDefaultUsers().getUserGuest().equals(getName());
    }
    
    /**
     * Returns <code>true</code> if this user is a system user.<p>
     * 
     * @return true if this user is a system user
     */    
    public boolean isSystemUser() {
        return isSystemUser(m_type);
    }
    
    /**
     * Returns <code>true</code> if this user is a web user.<p>
     * 
     * @return true if this user is a web user
     */      
    public boolean isWebUser() {
        return isWebUser(m_type);
    }

    /**
     * Sets the  complete Hashtable with additional information about the user. <p>
     * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
     *
     * This method has package-visibility for security-reasons.
     * It is required to because of the use of two seprate databases for user data and
     * additional user data.
     * 
     * @param additionalInfo user-related additional information
     *
     */
    void setAdditionalInfo(Map additionalInfo) {
        m_additionalInfo = additionalInfo;
    }

    /**
     * Sets additional information about the user. <p>
     * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
     *
     *
     * @param key The key to the additional information
     * @param obj The additinoal information value
     *
     */
    public void setAdditionalInfo(String key, Object obj) {
        m_additionalInfo.put(key, obj);
    }

    /**
     * Sets the address.<p>
     *
     * @param value The user adress
     */
    public void setAddress(String value) {
        m_address = value;
    }

    /**
     * Sets the description of this user.<p>
     *
     * @param value the description of this user
     */
    public void setDescription(String value) {
        m_description = value;
    }

    /**
     * Disables the user flags by setting them to C_FLAG_DISABLED.<p>
     */
    public void setDisabled() {
        setFlags(I_CmsConstants.C_FLAG_DISABLED);
    }

    /**
     * Sets the email.<p>
     *
     * @param value The new email adress
     */
    public void setEmail(String value) {
        m_email = value;
    }

    /**
     * Enables the user flags by setting them to C_FLAG_ENABLED.<p>
     */
    public void setEnabled() {
        setFlags(I_CmsConstants.C_FLAG_ENABLED);
    }

    /**
     * Sets the firstname.<p>
     *
     * @param firstname the USER_FIRSTNAME
     */
    public void setFirstname(String firstname) {
        m_firstname = firstname;
    }

    /**
     * Sets the flags.<p>
     *
     * @param value The new user flags
     */
    void setFlags(int value) {
        m_flags = value;
    }

    /**
     * Sets the lastlogin.<p>
     *
     * @param value The new user section
     */
    public void setLastlogin(long value) {
        m_isTouched = true;
        m_lastlogin = value;
    }

    /**
     * Gets the lastname.<p>
     *
     * @param lastname the last name of the user
     */
    public void setLastname(String lastname) {
        m_lastname = lastname;
    }

    /**
     * Sets the password.<p>
     *
     * @param value The new password
     */
    public void setPassword(String value) {
        m_password = value;
    }

    /**
     * Sets the typ of this user.<p>
     *
     * @param value the type of this user
     */
    void setType(int value) {
        m_type = value;
    }

    /**
     * Sets the "touched" status of this user to "true".<p>
     */    
    public void touch() {
        
        m_isTouched = true;
    }    
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[User]");
        result.append(" name:");
        result.append(m_name);
        result.append(" id:");
        result.append(m_id);
        result.append(" flags:");
        result.append(getFlags());
        result.append(" type:");
        result.append(getType());
        result.append(" description:");
        result.append(m_description);
        return result.toString();
    }
    
    /**
     * Returns true if this user was touched, e.g. the last-login timestamp was changed.<p>
     * 
     * @return boolean true if this resource was touched
     */
    public boolean isTouched() {
        return m_isTouched;
    }

    /**
     * Returns the "full" name the given user in the format "{firstname} {lastname} ({username})",
     * or the empty String "" if the user is null.<p>
     * 
     * @param user the user to get the full name from
     * @return the "full" name the user
     * @see #getFullName() 
     */
    public static String getFullName(CmsUser user) {
        if (user == null) {
            return "";
        } else {
            return user.getFullName();
        }
    }
     
    /**
     * Returns the "full" name this user in the format "{firstname} {lastname} ({username})".<p>
     * 
     * @return the "full" name this user 
     */
    public String getFullName() {
        StringBuffer buf = new StringBuffer();
        String first = getFirstname();
        if ((null != first) && !"".equals(first.trim())) { 
            buf.append(first);
            buf.append(" ");
        }
        String last = getLastname();
        if ((null != last) && !"".equals(last.trim())) { 
            buf.append(getLastname());
            buf.append(" ");
        }
        buf.append("(");
        buf.append(getName());
        buf.append(")");
        return buf.toString();
    }    
    
}