/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsUser.java,v $
 * Date   : $Date: 2003/08/14 15:37:27 $
 * Version: $Revision: 1.46 $
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

package com.opencms.file;

import java.util.Hashtable;

import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;

/**
 * A user in the OpenCms system.<p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.46 $
 */
public class CmsUser implements I_CmsPrincipal, Cloneable {

    /** A storage for additional user information */
    private Hashtable m_additionalInfo;

    /** The address of this user */
    private String m_address;

    /** The default group of this user */
    private CmsGroup m_defaultGroup;

    /** The default group ID of this user */
    private CmsUUID m_defaultGroupId;

    /** The description of the user */
    private String m_description;

    /**  The email of the user */
    private String m_email;

    /** The firstname of this user */
    private String m_firstname;

    /** The flags of the user */
    private int m_flags;

    /** The Id of this user */
    private CmsUUID m_id;

    /** The ip address of the last login of this user */
    private String m_lastip;

    /** The last login of this user */
    private long m_lastlogin;

    /** The lastname of this user */
    private String m_lastname;

    /** The lastu sed date of this user */
    private long m_lastused;

    /** The login-name of this user */
    private String m_name;

    /** The password of this user */
    private String m_password;

    /** The recovery password of the user */
    private String m_recoveryPassword;

    /** The section of the user */
    private String m_section;

    /**
     * Defines if the user is a webuser or a systemuser.
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
        m_defaultGroup = null;
        m_defaultGroupId = CmsUUID.getNullUUID();
        m_email = "";
        m_firstname = "";
        m_flags = I_CmsConstants.C_FLAG_ENABLED;
        m_lastip = null;
        m_lastlogin = I_CmsConstants.C_UNKNOWN_LONG;            
        m_lastname = "";
        m_lastused = I_CmsConstants.C_UNKNOWN_LONG;
        m_password = "";
        m_recoveryPassword = "";
        m_section = null;
        m_type = I_CmsConstants.C_UNKNOWN_INT;                 
    }

    /**
     * Creates a new CmsUser object.<p>
     *
     * @param id the id of the new user
     * @param name the name of the new user
     * @param password the password of the user
     * @param recoveryPassword the recovery password
     * @param description the description of the new user
     * @param firstname the first name
     * @param lastname the last name
     * @param email the email address
     * @param lastlogin time stamp 
     * @param lastused time stamp (deprecated)
     * @param flags flags
     * @param additionalInfo user related information
     * @param defaultGroup default group of the user
     * @param address the address
     * @param section (deprecated)
     * @param type the type of this user
     */
    public CmsUser(CmsUUID id, String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfo, CmsGroup defaultGroup, String address, String section, int type) {

        m_id = id;
        m_name = name;
        m_password = password;
        m_recoveryPassword = recoveryPassword;
        m_description = description;
        m_firstname = firstname;
        m_lastname = lastname;
        m_email = email;
        m_lastlogin = lastlogin;
        m_lastused = lastused;
        m_flags = flags;
        m_defaultGroup = defaultGroup;
        m_defaultGroupId = (defaultGroup != null) ? defaultGroup.getId() : CmsUUID.getNullUUID();
        m_additionalInfo = additionalInfo;
        m_address = address;
        m_section = section;
        m_type = type;

        // TODO: initialize this with parameter
        m_lastip = null;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        CmsUser user = new CmsUser(m_id, new String(m_name), new String(m_password), new String(m_recoveryPassword), new String(m_description), new String(m_firstname), new String(m_lastname), new String(m_email), m_lastlogin, m_lastused, m_flags, getAdditionalInfo(), m_defaultGroup, new String(m_address), new String(m_section), m_type);
        return user;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // check if the object is a CmsUser object
        if (!(obj instanceof CmsUser))
            return false;
        // same ID than the current user?
        return (((CmsUser)obj).getId().equals(m_id));
    }

    /**
     * Returns the complete Hashtable with additional information about the user.<p>
     * Additional infos are for example emailadress, adress or surname...
     *
     * The additional infos must be requested via the CmsObject.
     *
     * @return additional information about the user.
     *
     */
    public Hashtable getAdditionalInfo() {
        return m_additionalInfo;
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
        if (value == null && I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS.equals(key)) {
            Hashtable startSettings = new Hashtable();
            startSettings.put(I_CmsConstants.C_START_LANGUAGE, OpenCms.getUserDefaultLanguage());
            startSettings.put(I_CmsConstants.C_START_PROJECT, new Integer(I_CmsConstants.C_PROJECT_ONLINE_ID));
            startSettings.put(I_CmsConstants.C_START_VIEW, I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/explorer.html");
            //            startSettings.put(I_CmsConstants.C_START_DEFAULTGROUP, this.getDefaultGroup().getName());
            startSettings.put(I_CmsConstants.C_START_LOCKDIALOG, "");
            startSettings.put(I_CmsConstants.C_START_ACCESSFLAGS, new Integer(OpenCms.getUserDefaultAccessFlags()));
            m_additionalInfo.put(key, startSettings);
            value = startSettings;
        }
        return value;
    }

    /**
     * Gets the address.<p>
     *
     * @return the USER_ADDRESS, or null.
     */
    public String getAddress() {
        return m_address;
    }

    /**
     * Returns the default group object of this user.<p>
     *
     * @return Default Group of the user
     */
    public CmsGroup getDefaultGroup() {
        return m_defaultGroup;
    }

    /**
    * Gets the defaultgroup id.<p>
    *
    * @return the USER_DEFAULTGROUP_ID, or null.
    */
    public CmsUUID getDefaultGroupId() {
        return m_defaultGroupId;
    }

    /**
     * Gets the description of this user.<p>
     *
     * @return the description of this user.
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * Decides, if this user is disabled.<p>
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
     * Gets the email.<p>
     *
     * @return the USER_EMAIL, or null.
     */
    public String getEmail() {
        return m_email;
    }

    /**
     * Gets the firstname.<p>
     *
     * @return the USER_FIRSTNAME, or null.
     */
    public String getFirstname() {
        return m_firstname;
    }

    /**
     * Gets the flags.<p>
     *
     * @return the USER_FLAGS, or C_UNKNOWN_INT.
     */
    public int getFlags() {
        return m_flags;
    }

    /**
     * Gets the id of this user.<p>
     *
     * @return the id of this user.
     */
    public CmsUUID getId() {
        return m_id;
    }

    /**
     * Gets the lastlogin.<p>
     *
     * @return the USER_LASTLOGIN, or C_UNKNOWN_LONG.
     */
    public long getLastlogin() {
        return m_lastlogin;
    }

    /**
     * Gets the lastname.<p>
     *
     * @return the USER_SURNAME, or null.
     */
    public String getLastname() {
        return m_lastname;
    }

    /**
     * Gets the ip address of the last login.<p>
     * 
     * @return the ip address of the last login
     */
    public String getLastRemoteAddress() {
        return m_lastip;
    }

    /**
     * Gets the lastlogin - DEPRECATED, don't use.<p>
     *
     * @return the USER_LASTLOGIN, or C_UNKNOWN_LONG.
     */
    public long getLastUsed() {
        return m_lastused;
    }

    /**
     * Gets the login-name of the user.<p>
     *
     * @return the login-name of the user.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets the password.<p>
     *
     * @return the USER_PASSWORD, or null.
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * Gets the recovery password.<p>
     *
     * @return the USER_RECOVERY_PASSWORD, or null.
     */
    public String getRecoveryPassword() {
        return m_recoveryPassword;
    }

    /**
     * Gets the section of the user - DEPRECATED, don't use..<p>
     *
     * @return the USER_SECTION, or null.
     */
    public String getSection() {
        return m_section;
    }

    /**
     * Gets the type of the user (webuser or a systemuser).
     * C_USER_TYPE_SYSTEMUSER for systemuser (incl. guest).
     * C_USER_TYPE_WEBUSER for webuser.<p>
     *
     * @return the type, or C_UNKNOWN_INT.
     */
    public int getType() {
        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return m_id.hashCode();
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
    void setAdditionalInfo(Hashtable additionalInfo) {
        m_additionalInfo = additionalInfo;
    }

    /**
     * Sets additional information about the user. <p>
     * Additional infos are for example emailadress, adress or surname...<BR/><BR/>
     *
     *
     * @param key The key to the additional information.
     * @param obj The additinoal information value.
     *
     */
    public void setAdditionalInfo(String key, Object obj) {
        m_additionalInfo.put(key, obj);
    }

    /**
     * Sets the address.<p>
     *
     * @param value The user adress.
     */
    public void setAddress(String value) {
        m_address = value;
    }

    /**
     * Sets the default group object of this user.<p>
     *
     * @param defaultGroup The default group of this user.
     */
    public void setDefaultGroup(CmsGroup defaultGroup) {
        m_defaultGroup = defaultGroup;
        m_defaultGroupId = defaultGroup.getId();
    }

    /**
     * Sets the description of this user.<p>
     *
     * @param value the description of this user.
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
     * @param value The new email adress.
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
     * @param firstname the USER_FIRSTNAME.
     */
    public void setFirstname(String firstname) {
        m_firstname = firstname;
    }

    /**
     * Sets the flags.<p>
     *
     * @param value The new user flags.
     */
    void setFlags(int value) {
        m_flags = value;
    }

    /**
     * Sets the lastlogin.<p>
     *
     * @param value The new user section.
     */
    public void setLastlogin(long value) {
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
     * Sets the ip address of the last login.<p>
     * 
     * @param address the ip address of the last login
     */
    public void setLastRemoteAddress(String address) {
        m_lastip = address;
    }

    /**
     * Sets the lastlogin - DEPRECATED, don't use.<p>
     *
     * @param value the time stamp
     */
    void setLastUsed(long value) {
        m_lastused = value;
    }

    /**
     * Sets the password.<p>
     *
     * @param value The new password.
     */
    public void setPassword(String value) {
        m_password = value;
    }

    /**
     * Sets the section of the user - DEPRECATED, don't use..<p>
     *
     * @param value The new user section.
     */
    public void setSection(String value) {
        m_section = value;
    }

    /**
     * Sets the typ.<p>
     *
     * @param value The new user typ.
     */
    void setType(int value) {
        m_type = value;
    }

    /**
     * Returns a string-representation for this object.<p>
     * This can be used for debugging.
     *
     * @return string-representation for this object.
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
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