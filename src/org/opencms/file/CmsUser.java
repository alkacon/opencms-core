/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsUser.java,v $
 * Date   : $Date: 2006/05/12 16:05:48 $
 * Version: $Revision: 1.33 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsUserSettings;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Map;

/**
 * A user principal in the OpenCms permission system.<p>
 *
 * A user in OpenCms is uniquely definded by its user named returned by
 * <code>{@link #getName()}</code>.<p>
 * 
 * Basic users in OpenCms are of type <code>{@link #USER_TYPE_SYSTEMUSER}</code>.
 * This means that the user can access the OpenCms Workplace.
 * Moreover, the user must be created by another user with the
 * <code>{@link org.opencms.security.CmsRole#ACCOUNT_MANAGER}</code>.
 * This user type is for "content managers" that actually have write permissions in 
 * at last some parts of the VFS.<p>
 * 
 * Another possible type of users is <code>{@link #USER_TYPE_WEBUSER}</code>.
 * These users do not have access to the OpenCms Workplace. 
 * However, a web user can be created by every user, for example the "Guest" user.
 * The main use case is that web users are used for users of the website that 
 * can generate their own accounts, in a "please register your account..." scenario. 
 * These web user accounts can then be used to 
 * build personalized web sites. A web user is created using 
 * <code>{@link org.opencms.file.CmsObject#addWebUser(String, String, String, String, Map)}</code>.<p>
 *
 * @author Alexander Kandzior 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.33 $
 * 
 * @since 6.0.0
 * 
 * @see CmsGroup 
 */
public class CmsUser extends CmsPrincipal implements I_CmsPrincipal, Cloneable {

    /** Identifies the system user type. */
    public static final int USER_TYPE_SYSTEMUSER = 0;

    /** Identifies the web user type. */
    public static final int USER_TYPE_WEBUSER = 2;

    /** Storage for additional user information. */
    private Map m_additionalInfo;

    /** The address of this user. */
    private String m_address;

    /**  The email of the user. */
    private String m_email;

    /** The first name of this user. */
    private String m_firstname;

    /** Boolean flag whether the last-login timestamp of this user was modified. */
    private boolean m_isTouched;

    /** The last login date of this user. */
    private long m_lastlogin;

    /** The last name of this user. */
    private String m_lastname;

    /** The password of this user. */
    private String m_password;

    /**
     * Defines if the user is of type "syetem user" or a "web user".<p>
     * 
     * Use {@link #USER_TYPE_SYSTEMUSER} for system users, or 
     * {@link #USER_TYPE_WEBUSER} for web usera.
     */
    private int m_type;

    /**
     * Creates a new, empty OpenCms user principal.<p>
     *
     * Mostly intented to be used with the {@link org.opencms.workplace.tools.accounts.A_CmsEditUserDialog}.<p>
     */
    public CmsUser() {

        this(null, "", "");
        setAdditionalInfo(new HashMap());
    }

    /**
     * Creates a new OpenCms user principal.<p>
     *
     * @param id the unique id of the new user
     * @param name the unique name of the new user
     * @param description the description of the new user
     */
    public CmsUser(CmsUUID id, String name, String description) {

        this(
            id,
            name,
            "",
            description,
            "",
            "",
            "",
            CmsDbUtil.UNKNOWN_ID,
            I_CmsPrincipal.FLAG_ENABLED,
            null,
            "",
            CmsDbUtil.UNKNOWN_ID);
    }

    /**
     * Creates a new OpenCms user principal.<p>
     * 
     * @param id the unique id of the new user
     * @param name the unique name of the new user
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
        int type) {

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
     * Returns the "full" name of the given user in the format <code>"{firstname} {lastname} ({username})"</code>,
     * or the empty String <code>""</code> if the user is null.<p>
     * 
     * @param user the user to get the full name from
     * @return the "full" name the user
     * 
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
     * Returns <code>true</code> if the provided user type indicates a system user.<p>
     * 
     * @param type the user type to check
     * 
     * @return true if the provided user type indicates a system user
     */
    public static boolean isSystemUser(int type) {

        return (type & 1) > 0;
    }

    /**
     * Returns <code>true</code> if the provided user type indicates a web user.<p>
     * 
     * @param type the user type to check
     * 
     * @return true if the provided user type indicates a web user
     */
    public static boolean isWebUser(int type) {

        return (type & 2) > 0;
    }

    /**
     * Checks if the provided user name is a valid user name and can be used as an argument value 
     * for {@link #setName(String)}.<p> 
     * 
     * @param name the user name to check
     * 
     * @throws CmsIllegalArgumentException if the check fails
     */
    public void checkName(String name) throws CmsIllegalArgumentException {

        OpenCms.getValidationHandler().checkUserName(name);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        return new CmsUser(
            m_id,
            m_name,
            m_password,
            m_description,
            m_firstname,
            m_lastname,
            m_email,
            m_lastlogin,
            m_flags,
            m_additionalInfo != null ? new HashMap(m_additionalInfo) : null,
            m_address,
            m_type);
    }

    /**
     * Deletes a value from this users "additional information" storage map.<p>
     *
     * @param key the additional user information to delete
     * 
     * @see #getAdditionalInfo()
     */
    public void deleteAdditionalInfo(String key) {

        m_additionalInfo.remove(key);
    }

    /**
     * Returns this users complete "additional information" storage map.<p>
     *
     * The "additional information" storage map is a simple {@link java.util#Map}
     * that can be used to store any key / value pairs for the user.
     * Some information parts of the users address are stored in this map
     * by default. The map is serialized when the user is stored in the database.<p>
     * 
     * @return this users complete "additional information" storage map
     */
    public Map getAdditionalInfo() {

        return m_additionalInfo;
    }

    /**
     * Returns a value from this users "additional information" storage map,
     * or <code>null</code> if no value for the given key is available.<p>
     * 
     * @param key selects the value to return from the "additional information" storage map
     * 
     * @return the selected value from this users "additional information" storage map
     * 
     * @see #getAdditionalInfo()
     */
    public Object getAdditionalInfo(String key) {

        return m_additionalInfo.get(key);
    }

    /**
     * Returns the address line of this user.<p>
     *
     * @return the address line of this user
     */
    public String getAddress() {

        return m_address;
    }

    /**
     * Returns the city information of this user.<p>
     * 
     * This informaion is stored in the "additional information" storage map
     * using the key <code>{@link CmsUserSettings#ADDITIONAL_INFO_CITY}</code>.<p>
     * 
     * @return the city information of this user
     */
    public String getCity() {

        return (String)getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_CITY);
    }

    /**
     * Returns the country information of this user.<p>
     *
     * This informaion is stored in the "additional information" storage map
     * using the key <code>{@link CmsUserSettings#ADDITIONAL_INFO_COUNTRY}</code>.<p>
     *
     * @return the country information of this user
     */
    public String getCountry() {

        return (String)getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_COUNTRY);
    }

    /**
     * Returns <code>true</code> if this user is disabled.<p>
     *
     * @return <code>true</code> if this user is disabled
     * 
     * @deprecated use {@link CmsPrincipal#isEnabled()} instead
     */
    public boolean getDisabled() {

        return !isEnabled();
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
     * Returns the "full" name of the this user in the format <code>"{firstname} {lastname} ({username})"</code>.<p>
     * 
     * @return the "full" name this user
     */
    public String getFullName() {

        StringBuffer buf = new StringBuffer();
        String first = getFirstname();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(first)) {
            buf.append(first);
            buf.append(" ");
        }
        String last = getLastname();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(last)) {
            buf.append(last);
            buf.append(" ");
        }
        buf.append("(");
        buf.append(getName());
        buf.append(")");
        return buf.toString();
    }

    /**
     * Returns the time of the last login of this user.<p>
     *
     * @return the time of the last login of this user
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
     * Returns the encrypted user password.<p>
     *
     * @return the encrypted user password
     */
    public String getPassword() {

        return m_password;
    }

    /**
     * Returns the type of this user.<p>
     * 
     * Possible options are
     * <code>{@link #USER_TYPE_SYSTEMUSER}</code> for a "system user",
     * or <code>{@link #USER_TYPE_WEBUSER}</code> for a "web user".<p>
     *
     * @return the type of this user
     */
    public int getType() {

        return m_type;
    }

    /**
     * Returns the zip code information of this user.<p>
     * 
     * This informaion is stored in the "additional information" storage map
     * using the key <code>{@link CmsUserSettings#ADDITIONAL_INFO_ZIPCODE}</code>.<p>
     *
     * @return the zip code information of this user 
     */
    public String getZipcode() {

        return (String)getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ZIPCODE);
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#isGroup()
     */
    public boolean isGroup() {

        return false;
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
     * Returns <code>true</code> if this user is a "system user".<p>
     * 
     * @return true if this user is a "system user"
     */
    public boolean isSystemUser() {

        return isSystemUser(m_type);
    }

    /**
     * Returns <code>true</code> if this user was touched.<p>
     * 
     * @return boolean true if this user was touched
     */
    public boolean isTouched() {

        return m_isTouched;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#isUser()
     */
    public boolean isUser() {

        return true;
    }

    /**
     * Returns <code>true</code> if this user is a "web user".<p>
     * 
     * @return true if this user is a "web user"
     */
    public boolean isWebUser() {

        return isWebUser(m_type);
    }

    /**
     * Sets this users complete "additional information" storage map to the given value.<p>
     * 
     * @param additionalInfo the complete "additional information" map to set
     * 
     * @see #getAdditionalInfo()
     */
    public void setAdditionalInfo(Map additionalInfo) {

        m_additionalInfo = additionalInfo;
    }

    /**
     * Stores a value in this users "additional information" storage map with the gicen access key.<p>
     * 
     * @param key the key to store the value under
     * @param value the value to store in the users "additional information" storage map
     * 
     * @see #getAdditionalInfo()
     */
    public void setAdditionalInfo(String key, Object value) {

        m_additionalInfo.put(key, value);
    }

    /**
     * Sets the address line of this user.<p>
     *
     * @param address the address line to set
     */
    public void setAddress(String address) {

        m_address = address;
    }

    /**
     * Sets the city information of this user.<p>
     * 
     * @param city the city information to set
     */
    public void setCity(String city) {

        setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_CITY, city);
    }

    /**
     * Sets the country information of this user.<p>
     * 
     * @param country the city information to set
     */
    public void setCountry(String country) {

        setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_COUNTRY, country);
    }

    /**
     * Disables this user.<p>
     * 
     * @deprecated use {@link CmsPrincipal#setEnabled(boolean)} instead
     */
    public void setDisabled() {

        setEnabled(false);
    }

    /**
     * Sets the email address of this user.<p>
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {

        OpenCms.getValidationHandler().checkEmail(email);
        m_email = email;
    }

    /**
     * Enables this user.<p>
     * 
     * @deprecated use {@link CmsPrincipal#setEnabled(boolean)} instead
     */
    public void setEnabled() {

        setEnabled(true);
    }

    /**
     * Sets the first name of this user.<p>
     *
     * @param firstname the name to set
     */
    public void setFirstname(String firstname) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(firstname)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_FIRSTNAME_EMPTY_0));
        }
        m_firstname = firstname;
    }

    /**
     * Sets the last login timestamp of this user.<p>
     *
     * @param value the last login timestamp to set
     */
    public void setLastlogin(long value) {

        m_isTouched = true;
        m_lastlogin = value;
    }

    /**
     * Sets the last name of this user.<p>
     *
     * @param lastname the name to set
     */
    public void setLastname(String lastname) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(lastname)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_LASTNAME_EMPTY_0));
        }
        m_lastname = lastname;
    }

    /**
     * Sets the password of this user.<p>
     *
     * @param value the password to set
     */
    public void setPassword(String value) {

        try {
            OpenCms.getPasswordHandler().validatePassword(value);
        } catch (CmsSecurityException e) {
            throw new CmsIllegalArgumentException(e.getMessageContainer());
        }
        m_password = value;
    }

    /**
     * Sets the zip code information of this user.<p>
     * 
     * @param zipcode the zip code information to set
     */
    public void setZipcode(String zipcode) {

        OpenCms.getValidationHandler().checkZipCode(zipcode);
        zipcode = zipcode.toUpperCase();
        setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ZIPCODE, zipcode);
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
     * Sets the "touched" status of this user to <code>true</code>.<p>
     */
    public void touch() {

        m_isTouched = true;
    }

    /**
     * Sets the type of this user.<p>
     * 
     * Possible options are
     * <code>{@link #USER_TYPE_SYSTEMUSER}</code> for a "system user",
     * or <code>{@link #USER_TYPE_WEBUSER}</code> for a "web user".<p>
     * 
     * @param value the type to set
     */
    protected void setType(int value) {

        m_type = value;
    }
}