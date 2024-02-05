/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAuthentificationException;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsUserDisabledException;
import org.opencms.security.Messages;
import org.opencms.util.CmsStringUtil;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Provides functions used to check the validity of a user login.<p>
 *
 * Stores invalid login attempts and disables a user account temporarily in case
 * the configured threshold of invalid logins is reached.<p>
 *
 * The invalid login attempt storage operates on a combination of user name, login remote IP address and
 * user type. This means that a user can be disabled for one remote IP, but still be enabled for
 * another remote IP.<p>
 *
 * Also allows to temporarily disallow logins (for example in case of maintenance work on the system).<p>
 *
 * @since 6.0.0
 */
public class CmsLoginManager {

    /**
     * Contains the data stored for each user in the storage for invalid login attempts.<p>
     */
    private class CmsUserData {

        /** The start time this account was disabled. */
        private long m_disableTimeStart;

        /** The count of the failed attempts. */
        private int m_invalidLoginCount;

        /**
         * Creates a new user data instance.<p>
         */
        protected CmsUserData() {

            // a new instance is creted only if there already was one failed attempt
            m_invalidLoginCount = 1;
        }

        /**
         * Returns the bad attempt count for this user.<p>
         *
         * @return the bad attempt count for this user
         */
        protected Integer getInvalidLoginCount() {

            return Integer.valueOf(m_invalidLoginCount);
        }

        /**
         * Returns the date this disabled user is released again.<p>
         *
         * @return the date this disabled user is released again
         */
        protected Date getReleaseDate() {

            return new Date(m_disableTimeStart + m_disableMillis + 1);
        }

        /**
         * Increases the bad attempt count, disables the data in case the
         * configured threshold is reached.<p>
         */
        protected void increaseInvalidLoginCount() {

            m_invalidLoginCount++;
            if (m_invalidLoginCount >= m_maxBadAttempts) {
                // threshold for bad login attempts has been reached for this user
                if (m_disableTimeStart == 0) {
                    // only disable in case this user has not already been disabled
                    m_disableTimeStart = System.currentTimeMillis();
                }
            }
        }

        /**
         * Returns <code>true</code> in case this user has been temporarily disabled.<p>
         *
         * @return <code>true</code> in case this user has been temporarily disabled
         */
        protected boolean isDisabled() {

            if (m_disableTimeStart > 0) {
                // check if the disable time is already over
                long currentTime = System.currentTimeMillis();
                if ((currentTime - m_disableTimeStart) > m_disableMillis) {
                    // disable time is over
                    m_disableTimeStart = 0;
                }
            }
            return m_disableTimeStart > 0;
        }

        /**
         * Reset disable time.<p>
         */
        protected void reset() {

            m_disableTimeStart = 0;
            m_invalidLoginCount = 0;
        }
    }

    /** Default token lifetime. */
    public static final long DEFAULT_TOKEN_LIFETIME = 3600 * 24 * 1000;

    /** Default lock time if treshold for bad login attempts is reached. */
    public static final int DISABLE_MINUTES_DEFAULT = 15;

    /** Default setting for the security option. */
    public static final boolean ENABLE_SECURITY_DEFAULT = false;

    /** Separator used for storage keys. */
    public static final String KEY_SEPARATOR = "_";

    /** Default for bad login attempts. */
    public static final int MAX_BAD_ATTEMPTS_DEFAULT = 3;

    /**Map holding usernames and userdata for user which are currently locked.*/
    protected static Map<String, Set<CmsUserData>> TEMP_DISABLED_USER;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLoginManager.class);

    /** The milliseconds to disable an account if the threshold is reached. */
    protected int m_disableMillis;

    /** The minutes to disable an account if the threshold is reached. */
    protected int m_disableMinutes;

    /** The flag to determine if the security option ahould be enabled on the login dialog. */
    protected boolean m_enableSecurity;

    /** The number of bad login attempts allowed before an account is temporarily disabled. */
    protected int m_maxBadAttempts;

    /** The storage for the bad login attempts. */
    protected Map<String, CmsUserData> m_storage;

    /** The token lifetime. */
    protected String m_tokenLifetimeStr;

    /** The before login message. */
    private CmsLoginMessage m_beforeLoginMessage;

    /** The login message, setting this may also disable logins for non-Admin users. */
    private CmsLoginMessage m_loginMessage;

    /** The logout URI. */
    private String m_logoutUri;

    /** Max inactivity time. */
    private String m_maxInactive;

    /** Password change interval. */
    private String m_passwordChangeInterval;

    /** Option which determines whether the login dialog should require an organizational unit. */
    private boolean m_requireOrgUnit;

    /** User data check interval. */
    private String m_userDateCheckInterval;

    /**
     * Creates a new storage for invalid logins.<p>
     *
     * @param disableMinutes the minutes to disable an account if the threshold is reached
     * @param maxBadAttempts the number of bad login attempts allowed before an account is temporarily disabled
     * @param enableSecurity flag to determine if the security option should be enabled on the login dialog
     * @param tokenLifetime the lifetime of authorization tokens, i.e. the time for which they are valid
     * @param maxInactive maximum inactivity time
     * @param passwordChangeInterval the password change interval
     * @param userDataCheckInterval the user data check interval
     * @param requireOrgUnit if true, should require organizational unit selection on login
     * @param logoutUri the alternative logout handler URI
     */
    public CmsLoginManager(
        int disableMinutes,
        int maxBadAttempts,
        boolean enableSecurity,
        String tokenLifetime,
        String maxInactive,
        String passwordChangeInterval,
        String userDataCheckInterval,
        boolean requireOrgUnit,
        String logoutUri) {

        m_maxBadAttempts = maxBadAttempts;
        if (TEMP_DISABLED_USER == null) {
            TEMP_DISABLED_USER = new Hashtable<String, Set<CmsUserData>>();
        }
        if (m_maxBadAttempts >= 0) {
            // otherwise the invalid login storage is sisabled
            m_disableMinutes = disableMinutes;
            m_disableMillis = disableMinutes * 60 * 1000;
            m_storage = new Hashtable<String, CmsUserData>();

        }
        m_enableSecurity = enableSecurity;
        m_tokenLifetimeStr = tokenLifetime;
        m_maxInactive = maxInactive;
        m_passwordChangeInterval = passwordChangeInterval;
        m_userDateCheckInterval = userDataCheckInterval;
        m_requireOrgUnit = requireOrgUnit;
        m_logoutUri = logoutUri;
    }

    /**
     * Returns the key to use for looking up the user in the invalid login storage.<p>
     *
     * @param userName the name of the user
     * @param remoteAddress the remore address (IP) from which the login attempt was made
     *
     * @return the key to use for looking up the user in the invalid login storage
     */
    private static String createStorageKey(String userName, String remoteAddress) {

        StringBuffer result = new StringBuffer();
        result.append(userName);
        result.append(KEY_SEPARATOR);
        result.append(remoteAddress);
        return result.toString();
    }

    /**
     * Checks whether a user account can be locked because of inactivity.
     *
     * @param cms the CMS context
     * @param user the user to check
     * @return true if the user may be locked after being inactive for too long
     */
    public boolean canLockBecauseOfInactivity(CmsObject cms, CmsUser user) {

        return !user.isManaged()
            && !user.isWebuser()
            && !OpenCms.getDefaultUsers().isDefaultUser(user.getName())
            && !OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ROOT_ADMIN);
    }

    /**
     * Checks whether the given user has been inactive for longer than the configured limit.<p>
     *
     * If no max inactivity time is configured, always returns false.
     *
     * @param user the user to check
     * @return true if the user has been inactive for longer than the configured limit
     */
    public boolean checkInactive(CmsUser user) {

        if (m_maxInactive == null) {
            return false;
        }
        try {
            if (user.getLastlogin() == 0) {
                return false;
            }
            long maxInactive = CmsStringUtil.parseDuration(m_maxInactive, Long.MAX_VALUE);
            return (System.currentTimeMillis() - user.getLastlogin()) > maxInactive;
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Checks if the threshold for the invalid logins has been reached for the given user.<p>
     *
     * In case the configured threshold is reached, an Exception is thrown.<p>
     *
     * @param userName the name of the user
     * @param remoteAddress the remote address (IP) from which the login attempt was made
     *
     * @throws CmsAuthentificationException in case the threshold of invalid login attempts has been reached
     */
    public void checkInvalidLogins(String userName, String remoteAddress) throws CmsAuthentificationException {

        if (m_maxBadAttempts < 0) {
            // invalid login storage is disabled
            return;
        }

        String key = createStorageKey(userName, remoteAddress);
        // look up the user in the storage
        CmsUserData userData = m_storage.get(key);
        if ((userData != null) && (userData.isDisabled())) {
            // threshold of invalid logins is reached
            Set<CmsUserData> data = TEMP_DISABLED_USER.get(userName);
            if (data == null) {
                data = new HashSet<CmsUserData>();
            }
            data.add(userData);
            TEMP_DISABLED_USER.put(userName, data);
            throw new CmsUserDisabledException(
                Messages.get().container(
                    Messages.ERR_LOGIN_FAILED_TEMP_DISABLED_4,
                    new Object[] {
                        userName,
                        remoteAddress,
                        userData.getReleaseDate(),
                        userData.getInvalidLoginCount()}));
        }
        if (TEMP_DISABLED_USER.containsKey(userName) & (userData != null)) {
            //User war disabled, but time is over -> remove from list
            if (TEMP_DISABLED_USER.get(userName).contains(userData)) {
                TEMP_DISABLED_USER.get(userName).remove(userData);
                if (TEMP_DISABLED_USER.get(userName).isEmpty()) {
                    TEMP_DISABLED_USER.remove(userName);
                }
            }
        }
    }

    /**
     * Checks if a login is currently allowed.<p>
     *
     * In case no logins are allowed, an Exception is thrown.<p>
     *
     * @throws CmsAuthentificationException in case no logins are allowed
     */
    public void checkLoginAllowed() throws CmsAuthentificationException {

        if ((m_loginMessage != null) && (m_loginMessage.isLoginCurrentlyForbidden())) {
            // login message has been set and is active
            throw new CmsAuthentificationException(
                Messages.get().container(Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1, m_loginMessage.getMessage()));
        }
    }

    /**
     * Returns the current before login message that is displayed on the login form.<p>
     *
     * if <code>null</code> is returned, no login message has been currently set.<p>
     *
     * @return  the current login message that is displayed if a user logs in
     */
    public CmsLoginMessage getBeforeLoginMessage() {

        return m_beforeLoginMessage;
    }

    /**
     * Returns the minutes an account gets disabled after too many failed login attempts.<p>
     *
     * @return the minutes an account gets disabled after too many failed login attempts
     */
    public int getDisableMinutes() {

        return m_disableMinutes;
    }

    /**
     * Returns the current login message that is displayed if a user logs in.<p>
     *
     * if <code>null</code> is returned, no login message has been currently set.<p>
     *
     * @return  the current login message that is displayed if a user logs in
     */
    public CmsLoginMessage getLoginMessage() {

        return m_loginMessage;
    }

    /**
     * Gets the logout URI.<p>
     *
     * If this is not null, users will be redirected to this JSP when logging out from the workplace
     * or page editor. The JSP is responsible for invalidating the user's session.
     *
     * @return the logout URI
     */
    public String getLogoutUri() {

        return m_logoutUri;
    }

    /**
     * Returns the number of bad login attempts allowed before an account is temporarily disabled.<p>
     *
     * @return the number of bad login attempts allowed before an account is temporarily disabled
     */
    public int getMaxBadAttempts() {

        return m_maxBadAttempts;
    }

    /**
     * Gets the max inactivity time.<p>
     *
     * @return the max inactivity time
     */
    public String getMaxInactive() {

        return m_maxInactive;
    }

    /**
     * Gets the password change interval.<p>
     *
     * @return the password change interval
     */
    public long getPasswordChangeInterval() {

        if (m_passwordChangeInterval == null) {
            return Long.MAX_VALUE;
        } else {
            return CmsStringUtil.parseDuration(m_passwordChangeInterval, Long.MAX_VALUE);
        }
    }

    /**
     * Gets the raw password change interval string.<p>
     *
     * @return the configured string for the password change interval
     */
    public String getPasswordChangeIntervalStr() {

        return m_passwordChangeInterval;
    }

    /**
     * Gets the authorization token lifetime in milliseconds.<p>
     *
     * @return the authorization token lifetime in milliseconds
     */
    public long getTokenLifetime() {

        if (m_tokenLifetimeStr == null) {
            return DEFAULT_TOKEN_LIFETIME;
        }
        return CmsStringUtil.parseDuration(m_tokenLifetimeStr, DEFAULT_TOKEN_LIFETIME);
    }

    /**
     * Gets the configured token lifetime as a string.<p>
     *
     * @return the configured token lifetime as a string
     */
    public String getTokenLifetimeStr() {

        return m_tokenLifetimeStr;
    }

    /**
     * Gets the user data check interval.<p>
     *
     * @return the user data check interval
     */
    public long getUserDataCheckInterval() {

        if (m_userDateCheckInterval == null) {
            return Long.MAX_VALUE;
        } else {
            return CmsStringUtil.parseDuration(m_userDateCheckInterval, Long.MAX_VALUE);
        }
    }

    /**
     * Gets the raw user data check interval string.<p>
     *
     * @return the configured string for the user data check interval
     */
    public String getUserDataCheckIntervalStr() {

        return m_userDateCheckInterval;
    }

    /**
     * Returns if the security option ahould be enabled on the login dialog.<p>
     *
     * @return <code>true</code> if the security option ahould be enabled on the login dialog, otherwise <code>false</code>
     */
    public boolean isEnableSecurity() {

        return m_enableSecurity;
    }

    /**
     * Checks if the user should be excluded from password reset.
     *
     * @param cms the CmsObject to use
     * @param user the user to check
     * @return true if the user should be excluded from password reset
     */
    public boolean isExcludedFromPasswordReset(CmsObject cms, CmsUser user) {

        return user.isManaged() || user.isWebuser() || OpenCms.getDefaultUsers().isDefaultUser(user.getName());
    }

    /**
     * Returns true if organizational unit selection should be required on login.
     *
     * @return true if org unit selection should be required
     */
    public boolean isOrgUnitRequired() {

        return m_requireOrgUnit;
    }

    /**
     * Checks if password has to be reset.<p>
     *
     * @param cms CmsObject
     * @param user CmsUser
     * @return true if password should be reset
     */
    public boolean isPasswordReset(CmsObject cms, CmsUser user) {

        if (isExcludedFromPasswordReset(cms, user)) {
            return false;
        }
        if (user.getAdditionalInfo().get(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET) != null) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a user is locked due to too many failed logins.<p>
     *
     * @param user the user to check
     *
     * @return true if the user is locked
     */
    public boolean isUserLocked(CmsUser user) {

        Set<String> keysForUser = getKeysForUser(user);
        for (String key : keysForUser) {
            CmsUserData data = m_storage.get(key);
            if ((data != null) && data.isDisabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if given user it temporarily locked.<p>
     *
     * @param username to check
     * @return true if user is locked
     */
    public boolean isUserTempDisabled(String username) {

        Set<CmsUserData> data = TEMP_DISABLED_USER.get(username);
        if (data == null) {
            return false;
        }
        for (CmsUserData userData : data) {
            if (!userData.isDisabled()) {
                data.remove(userData);
            }
        }
        if (data.size() > 0) {
            TEMP_DISABLED_USER.put(username, data);
            return true;
        } else {
            TEMP_DISABLED_USER.remove(username);
            return false;
        }
    }

    /**
     * Removes the current login message.<p>
     *
     * This operation requires that the current user has role permissions of <code>{@link CmsRole#ROOT_ADMIN}</code>.<p>
     *
     * @param cms the current OpenCms user context
     *
     * @throws CmsRoleViolationException in case the current user does not have the required role permissions
     */
    public void removeLoginMessage(CmsObject cms) throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        m_loginMessage = null;
    }

    /**
     * Checks if a user is required to change his password now.<p>
     *
     * @param cms the current CMS context
     * @param user the user to check
     *
     * @return true if the user should be asked to change his password
     */
    public boolean requiresPasswordChange(CmsObject cms, CmsUser user) {

        if (user.isManaged()
            || user.isWebuser()
            || OpenCms.getDefaultUsers().isDefaultUser(user.getName())
            || OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ROOT_ADMIN)) {
            return false;
        }
        String lastPasswordChangeStr = (String)user.getAdditionalInfo().get(
            CmsUserSettings.ADDITIONAL_INFO_LAST_PASSWORD_CHANGE);
        if (lastPasswordChangeStr == null) {
            return false;
        }
        long lastPasswordChange = Long.parseLong(lastPasswordChangeStr);
        if ((System.currentTimeMillis() - lastPasswordChange) > getPasswordChangeInterval()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a user is required to change his password now.<p>
     *
     * @param cms the current CMS context
     * @param user the user to check
     *
     * @return true if the user should be asked to change his password
     */
    public boolean requiresUserDataCheck(CmsObject cms, CmsUser user) {

        if (user.isManaged()
            || user.isWebuser()
            || OpenCms.getDefaultUsers().isDefaultUser(user.getName())
            || OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ROOT_ADMIN)) {
            return false;
        }

        String lastCheckStr = (String)user.getAdditionalInfo().get(
            CmsUserSettings.ADDITIONAL_INFO_LAST_USER_DATA_CHECK);
        if (lastCheckStr == null) {
            return !CmsStringUtil.isEmptyOrWhitespaceOnly(getUserDataCheckIntervalStr());
        }
        long lastCheck = Long.parseLong(lastCheckStr);
        if ((System.currentTimeMillis() - lastCheck) > getUserDataCheckInterval()) {
            return true;
        }
        return false;
    }

    /**
     * Resets lock from user.<p>
     *
     * @param username to reset lock for
     */
    public void resetUserTempDisable(String username) {

        Set<CmsUserData> data = TEMP_DISABLED_USER.get(username);
        if (data == null) {
            return;
        }
        for (CmsUserData userData : data) {
            userData.reset();
        }
        TEMP_DISABLED_USER.remove(username);
    }

    /**
     * Sets the before login message to display on the login form.<p>
     *
     * This operation requires that the current user has role permissions of <code>{@link CmsRole#ROOT_ADMIN}</code>.<p>
     *
     * @param cms the current OpenCms user context
     * @param message the message to set
     *
     * @throws CmsRoleViolationException in case the current user does not have the required role permissions
     */
    public void setBeforeLoginMessage(CmsObject cms, CmsLoginMessage message) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // during configuration phase no permission check id required
            OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        }
        m_beforeLoginMessage = message;

        if (m_beforeLoginMessage != null) {
            m_beforeLoginMessage.setFrozen();
        }
    }

    /**
     * Sets the login message to display if a user logs in.<p>
     *
     * This operation requires that the current user has role permissions of <code>{@link CmsRole#ROOT_ADMIN}</code>.<p>
     *
     * @param cms the current OpenCms user context
     * @param message the message to set
     *
     * @throws CmsRoleViolationException in case the current user does not have the required role permissions
     */
    public void setLoginMessage(CmsObject cms, CmsLoginMessage message) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // during configuration phase no permission check id required
            OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        }
        m_loginMessage = message;
        if (m_loginMessage != null) {
            m_loginMessage.setFrozen();
        }
    }

    /**
     * Unlocks a user who has exceeded his number of failed login attempts so that he can try to log in again.<p>
     * This requires the "account manager" role.
     *
     * @param cms the current CMS context
     * @param user the user to unlock
     *
     * @throws CmsRoleViolationException if the permission check fails
     */
    public void unlockUser(CmsObject cms, CmsUser user) throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(cms.getRequestContext().getOuFqn()));
        Set<String> keysToRemove = getKeysForUser(user);
        for (String keyToRemove : keysToRemove) {
            m_storage.remove(keyToRemove);
        }
    }

    /**
     * Adds an invalid attempt to login for the given user / IP to the storage.<p>
     *
     * In case the configured threshold is reached, the user is disabled for the configured time.<p>
     *
     * @param userName the name of the user
     * @param remoteAddress the remore address (IP) from which the login attempt was made
     */
    protected void addInvalidLogin(String userName, String remoteAddress) {

        if (m_maxBadAttempts < 0) {
            // invalid login storage is disabled
            return;
        }

        String key = createStorageKey(userName, remoteAddress);
        // look up the user in the storage
        CmsUserData userData = m_storage.get(key);
        if (userData != null) {
            // user data already contained in storage
            userData.increaseInvalidLoginCount();
        } else {
            // create an new data object for this user
            userData = new CmsUserData();
            m_storage.put(key, userData);
        }
    }

    /**
     * Removes all invalid attempts to login for the given user / IP.<p>
     *
     * @param userName the name of the user
     * @param remoteAddress the remore address (IP) from which the login attempt was made
     */
    protected void removeInvalidLogins(String userName, String remoteAddress) {

        if (m_maxBadAttempts < 0) {
            // invalid login storage is disabled
            return;
        }

        String key = createStorageKey(userName, remoteAddress);
        // just remove the user from the storage
        m_storage.remove(key);
    }

    /**
     * Helper method to get all the storage keys that match a user's name.<p>
     *
     * @param user the user for which to get the storage keys
     *
     * @return the set of storage keys
     */
    private Set<String> getKeysForUser(CmsUser user) {

        Set<String> keysToRemove = new HashSet<String>();
        for (Map.Entry<String, CmsUserData> entry : m_storage.entrySet()) {
            String key = entry.getKey();
            int separatorPos = key.lastIndexOf(KEY_SEPARATOR);
            String prefix = key.substring(0, separatorPos);
            if (user.getName().equals(prefix)) {
                keysToRemove.add(key);
            }
        }
        return keysToRemove;
    }
}