/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsLoginManager.java,v $
 * Date   : $Date: 2005/06/15 15:54:09 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAuthentificationException;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.Messages;

import java.util.Date;
import java.util.Hashtable;

/**
 * Provides functions used to check the validity of a user login.<p>
 * 
 * Stores invalid login attempts and disables a user account temporarily in case 
 * the configured threshold of invalid logins is reached.<p>
 * 
 * The invalid login attempt storage operates on a combination of user name, login remote IP address and 
 * user type. This means that a user can be disabled for one remote IP, but still be enabled for
 * another rempte IP.<p>
 * 
 * Also allows to temporarily disallow logins (for example in case of maintainance work on the system).<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 6.0
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

            return new Integer(m_invalidLoginCount);
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
    }

    /** Default lock time if treshold for bad login attempts is reached. */
    public static final int DISABLE_MINUTES_DEFAULT = 15;

    /** Default for bad login attempts. */
    public static final int MAX_BAD_ATTEMPTS_DEFAULT = 5;

    /** The milliseconds to disable an account if the threshold is reached. */
    protected int m_disableMillis;

    /** The minutes to disable an account if the threshold is reached. */
    protected int m_disableMinutes;

    /** The number of bad login attempts allowed before an account is temporarily disabled. */
    protected int m_maxBadAttempts;

    /** The storage for the bad login attempts. */
    protected Hashtable m_storage;

    /** The login message, setting this may also disable logins for non-Admin users. */
    private CmsLoginMessage m_loginMessage;

    /**
     * Creates a new storage for invalid logins.<p>
     * 
     * @param disableMinutes the minutes to disable an account if the threshold is reached
     * @param maxBadAttempts the number of bad login attempts allowed before an account is temporarily disabled
     */
    public CmsLoginManager(int disableMinutes, int maxBadAttempts) {

        m_maxBadAttempts = maxBadAttempts;
        if (m_maxBadAttempts >= 0) {
            // otherwise the invalid login storage is sisabled
            m_disableMinutes = disableMinutes;
            m_disableMillis = disableMinutes * 60 * 1000;
            m_storage = new Hashtable();
        }
    }

    /**
     * Returns the key to use for looking up the user in the invalid login storage.<p>
     * 
     * @param userName the name of the user
     * @param type the type of the user
     * @param remoteAddress the remore address (IP) from which the login attempt was made
     * 
     * @return the key to use for looking up the user in the invalid login storage
     */
    private static String createStorageKey(String userName, int type, String remoteAddress) {

        StringBuffer result = new StringBuffer();
        result.append(userName);
        result.append('_');
        result.append(type);
        result.append('_');
        result.append(remoteAddress);
        return result.toString();
    }

    /**
     * Checks if the threshold for the invalid logins has been reached for the given user.<p>
     * 
     * In case the configured threshold is reached, an Exception is thrown.<p>
     * 
     * @param userName the name of the user
     * @param type the type of the user
     * @param remoteAddress the remore address (IP) from which the login attempt was made
     * 
     * @throws CmsAuthentificationException in case the threshold of invalid login attempts has been reached
     */
    public void checkInvalidLogins(String userName, int type, String remoteAddress) throws CmsAuthentificationException {

        if (m_maxBadAttempts < 0) {
            // invalid login storage is disabled
            return;
        }

        String key = createStorageKey(userName, type, remoteAddress);
        // look up the user in the storage
        CmsUserData userData = (CmsUserData)m_storage.get(key);
        if ((userData != null) && (userData.isDisabled())) {
            // threshold of invalid logins is reached
            throw new CmsAuthentificationException(Messages.get().container(
                Messages.ERR_LOGIN_FAILED_TEMP_DISABLED_5,
                new Object[] {
                    userName,
                    new Integer(type),
                    remoteAddress,
                    userData.getReleaseDate(),
                    userData.getInvalidLoginCount()}));
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
            throw new CmsAuthentificationException(Messages.get().container(
                Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1,
                m_loginMessage.getMessage()));
        }
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
     * Returns the number of bad login attempts allowed before an account is temporarily disabled.<p>
     *
     * @return the number of bad login attempts allowed before an account is temporarily disabled
     */
    public int getMaxBadAttempts() {

        return m_maxBadAttempts;
    }

    /**
     * Removes the current login message.<p>
     * 
     * This operation requires that the current user has role permissions of <code>{@link CmsRole#ADMINISTRATOR}</code>.<p>
     * 
     * @param cms the current OpenCms user context
     * 
     * @throws CmsRoleViolationException in case the current user does not have the required role permissions
     */
    public void removeLoginMessage(CmsObject cms) throws CmsRoleViolationException {

        cms.checkRole(CmsRole.ADMINISTRATOR);
        m_loginMessage = null;
    }

    /**
     * Sets the login message to display if a user logs in.<p>
     * 
     * This operation requires that the current user has role permissions of <code>{@link CmsRole#ADMINISTRATOR}</code>.<p>
     * 
     * @param cms the current OpenCms user context
     * @param message the message to set
     * 
     * @throws CmsRoleViolationException in case the current user does not have the required role permissions
     */
    public void setLoginMessage(CmsObject cms, CmsLoginMessage message) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // during configuration phase no permission check id required
            cms.checkRole(CmsRole.ADMINISTRATOR);
        }
        m_loginMessage = message;
        if (m_loginMessage != null) {
            m_loginMessage.setFrozen();
        }
    }

    /**
     * Adds an invalid attempt to login for the given user / IP to the storage.<p>
     * 
     * In case the configured threshold is reached, the user is disabled for the configured time.<p>
     * 
     * @param userName the name of the user
     * @param type the type of the user
     * @param remoteAddress the remore address (IP) from which the login attempt was made
     */
    protected void addInvalidLogin(String userName, int type, String remoteAddress) {

        if (m_maxBadAttempts < 0) {
            // invalid login storage is disabled
            return;
        }

        String key = createStorageKey(userName, type, remoteAddress);
        // look up the user in the storage
        CmsUserData userData = (CmsUserData)m_storage.get(key);
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
     * @param type the type of the user
     * @param remoteAddress the remore address (IP) from which the login attempt was made
     */
    protected void removeInvalidLogins(String userName, int type, String remoteAddress) {

        if (m_maxBadAttempts < 0) {
            // invalid login storage is disabled
            return;
        }

        String key = createStorageKey(userName, type, remoteAddress);
        // just remove the user from the storage
        m_storage.remove(key);
    }
}