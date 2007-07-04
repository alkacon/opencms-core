/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsUser.java,v $
 * Date   : $Date: 2007/07/04 16:57:12 $
 * Version: $Revision: 1.35 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A user principal in the OpenCms permission system.<p>
 *
 * A user in OpenCms is uniquely definded by its user named returned by
 * <code>{@link #getName()}</code>.<p>
 * 
 * Basic users in OpenCms are users that can access the OpenCms Workplace.
 * Moreover, the user must be created by another user with the
 * <code>{@link org.opencms.security.CmsRole#ACCOUNT_MANAGER}</code> role.
 * These users are "content managers" that actually have write permissions in 
 * at last some parts of the VFS.<p>
 * 
 * Another possibility is to have users in a 'Guests' group.
 * These users do not have access to the OpenCms Workplace. 
 * However, an user in a 'Guests' group can be created by every user, for example 
 * the "Guest" user. The main use case is that these users are used for users of 
 * the website that can generate their own accounts, in a "please register your 
 * account..." scenario. 
 * These user accounts can then be used to build personalized web sites.<p>
 *
 * @author Alexander Kandzior 
 * @author Michael Emmerich 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.35 $
 * 
 * @since 6.0.0
 * 
 * @see CmsGroup 
 */
public class CmsUser extends CmsPrincipal implements I_CmsPrincipal, Cloneable {

    /** Storage for additional user information. */
    private Map m_additionalInfo;

    /** The creation date. */
    private long m_dateCreated;

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
     * Creates a new, empty OpenCms user principal.<p>
     *
     * Mostly intented to be used with the <code>org.opencms.workplace.tools.accounts.A_CmsEditUserDialog</code>.<p>
     */
    public CmsUser() {

        this(
            null,
            "",
            "",
            "",
            "",
            "",
            0,
            I_CmsPrincipal.FLAG_ENABLED,
            System.currentTimeMillis(),
            Collections.singletonMap(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION, ""));
    }

    /**
     * Creates a new OpenCms user principal.<p>
     * 
     * @param id the unique id of the new user
     * @param name the fully qualified name of the new user
     * @param password the password of the user
     * @param firstname the first name
     * @param lastname the last name
     * @param email the email address
     * @param lastlogin time stamp 
     * @param flags flags
     * @param dateCreated the creation date
     * @param additionalInfo user related information
     */
    public CmsUser(
        CmsUUID id,
        String name,
        String password,
        String firstname,
        String lastname,
        String email,
        long lastlogin,
        int flags,
        long dateCreated,
        Map additionalInfo) {

        m_id = id;
        m_name = name;
        m_password = password;
        m_firstname = firstname;
        m_lastname = lastname;
        m_email = email;
        m_lastlogin = lastlogin;
        m_flags = flags;
        m_dateCreated = dateCreated;
        if (additionalInfo != null) {
            m_additionalInfo = new HashMap(additionalInfo);
        } else {
            m_additionalInfo = new HashMap();
        }
        if (m_additionalInfo.get(CmsUserSettings.ADDITIONAL_INFO_ADDRESS) == null) {
            m_additionalInfo.put(CmsUserSettings.ADDITIONAL_INFO_ADDRESS, "");
        }
        if (m_additionalInfo.get(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION) == null) {
            m_additionalInfo.put(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION, "");
        }
    }

    /**
     * Validates an email address.<p>
     * 
     * That means, the parameter should only be composed by digits and standard english letters, points, underscores and exact one "At" symbol.<p>
     * 
     * @param email the email to validate
     */
    public static void checkEmail(String email) {

        OpenCms.getValidationHandler().checkEmail(email);
    }

    /**
     * Validates a zip code.<p>
     * 
     * That means, the parameter should only be composed by digits and standard english letters.<p>
     * 
     * @param zipcode the zipcode to validate
     */
    public static void checkZipCode(String zipcode) {

        OpenCms.getValidationHandler().checkZipCode(zipcode);
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
            m_firstname,
            m_lastname,
            m_email,
            m_lastlogin,
            m_flags,
            m_dateCreated,
            m_additionalInfo);
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
     * The "additional information" storage map is a simple {@link java.util.Map}
     * that can be used to store any key / value pairs for the user.
     * Some information parts of the users address are stored in this map
     * by default.<p>
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

        return (String)getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ADDRESS);
    }

    /**
     * Returns the city information of this user.<p>
     * 
     * This information is stored in the "additional information" storage map
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
     * Returns the creation date.<p>
     *
     * @return the creation date
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * @see org.opencms.security.CmsPrincipal#getDescription()
     */
    public String getDescription() {

        return (String)getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION);
    }

    /**
     * Returns the description of this organizational unit.<p>
     *
     * @param locale the locale
     *
     * @return the description of this organizational unit
     */
    public String getDescription(Locale locale) {

        CmsMacroResolver macroResolver = new CmsMacroResolver();
        macroResolver.setMessages(org.opencms.db.generic.Messages.get().getBundle(locale));
        return macroResolver.resolveMacros((String)getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION));
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
     * @see org.opencms.security.CmsPrincipal#getDisplayName(org.opencms.file.CmsObject, java.util.Locale)
     */
    public String getDisplayName(CmsObject cms, Locale locale) throws CmsException {

        if (OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, "", true).size() > 0) {
            return org.opencms.security.Messages.get().getBundle(locale).key(
                org.opencms.security.Messages.GUI_PRINCIPAL_DISPLAY_NAME_2,
                getFullName(),
                OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, getOuFqn()).getDisplayName(locale));
        } else {
            return getFullName();
        }
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
        buf.append(getSimpleName());
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

        return OpenCms.getDefaultUsers().isUserGuest(getName());
    }

    /**
     * Returns <code>true</code> if this user is not able to manage itselfs.<p> 
     * 
     * @return <code>true</code> if this user is not able to manage itselfs 
     */
    public boolean isManaged() {

        return (getFlags() & I_CmsPrincipal.FLAG_USER_MANAGED) == I_CmsPrincipal.FLAG_USER_MANAGED;
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

        setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ADDRESS, address);
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
     * @see org.opencms.security.CmsPrincipal#setDescription(java.lang.String)
     */
    public void setDescription(String description) {

        setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION, description);
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

        checkEmail(email);
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

        OpenCms.getValidationHandler().checkFirstname(firstname);
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

        OpenCms.getValidationHandler().checkLastname(lastname);
        m_lastname = lastname;
    }

    /**
     * Sets the managed flag for this user to the given value.<p>
     * 
     * @param value the value to set
     */
    public void setManaged(boolean value) {

        if (isManaged() != value) {
            setFlags(getFlags() ^ I_CmsPrincipal.FLAG_USER_MANAGED);
        }
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

        checkZipCode(zipcode);
        if (CmsStringUtil.isNotEmpty(zipcode)) {
            zipcode = zipcode.toUpperCase();
        }
        setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ZIPCODE, zipcode);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[User]");
        result.append(" name:");
        result.append(getName());
        result.append(" id:");
        result.append(m_id);
        result.append(" flags:");
        result.append(getFlags());
        result.append(" description:");
        result.append(getDescription());
        return result.toString();
    }

    /**
     * Sets the "touched" status of this user to <code>true</code>.<p>
     */
    public void touch() {

        m_isTouched = true;
    }
}