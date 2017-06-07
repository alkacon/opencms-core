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

package org.opencms.security;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Creates and validates persisten login tokens for users.<p>
 *
 * When a token is created for a user, a special additional info item is stored on the user, such that
 * the token uniquely identifies that info item. The value of the info item is the expiration date of the token.
 * A token is validated by looking up the additional info item for the user and checking whether the token is still valid
 * according to the stored expiration date.<p>
 */
public class CmsPersistentLoginTokenHandler {

    /**
     * Bean representing the data encoded in a login token (user name and key).<p>
     */
    public static class Token {

        /** Separator to use for the encoded token string. */
        public static final String SEPARATOR = "|";

        /** The key. */
        private String m_key;

        /** The name. */
        private String m_name;

        /**
         * Creates a new token object from the encoded representation.<p>
         *
         * @param token a string containing the token data
         */
        public Token(String token) {

            if (token != null) {
                List<String> parts = CmsStringUtil.splitAsList(token, SEPARATOR);
                if (parts.size() == 2) {
                    m_name = decodeName(parts.get(0));
                    m_key = parts.get(1);
                }
            }
        }

        /**
         * Creates a token object from the given name and key.<p>
         *
         * @param name the name
         * @param key the key
         */
        public Token(String name, String key) {

            m_name = name;
            m_key = key;

        }

        /**
         * Gets the encoded token string  representation.<p>
         *
         * @return the token string
         */
        public String encode() {

            return encodeName(m_name) + SEPARATOR + m_key;
        }

        /**
         * Gets the additional info key to use for this token.<p>
         *
         * @return the additional info key
         */
        public String getAdditionalInfoKey() {

            return KEY_PREFIX + m_key;
        }

        /**
         * Gets the key for this token.<p>
         *
         * @return the key
         */
        public String getKey() {

            return m_key;

        }

        /**
         * Gets the user name for this token.<p>
         *
         * @return the user name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Checks if this token is valid.<p>
         *
         * @return true if this token is valid
         */
        public boolean isValid() {

            return (m_name != null) && (m_key != null);
        }

        /**
         * Decodes the user name from a hexadecimal string, and returns null if this is not possible.<p>
         *
         * @param nameHex the encoded name
         * @return the decoded name
         */
        @SuppressWarnings("synthetic-access")
        private String decodeName(String nameHex) {

            try {
                return new String(Hex.decodeHex(nameHex.toCharArray()), "UTF-8");
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
                return null;
            }
        }

        /**
         * Encodes a user name as a hex string for storing in the cookie.<p>
         *
         * @param name the user name
         *
         * @return the encoded name
         */
        private String encodeName(String name) {

            try {
                return Hex.encodeHexString(name.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // shouldn't happen
                throw new IllegalStateException("UTF8 not supported");
            }
        }
    }

    /** Default token lifetime. */
    public static final long DEFAULT_LIFETIME = 1000 * 60 * 60 * 8;

    /** Prefix used for the keys for the additional infos this class creates. */
    public static final String KEY_PREFIX = "logintoken_";

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPersistentLoginTokenHandler.class);

    /** Admin CMS context. */
    private static CmsObject m_adminCms;

    /** The lifetime for created tokens. */
    private long m_lifetime = DEFAULT_LIFETIME;

    /**
     * Creates a new instance.<p>
     */
    public CmsPersistentLoginTokenHandler() {

        // Default constructor, do nothing
    }

    /**
     * Static method used to give this class access to an admin cms context.<p>
     *
     * @param adminCms the admin cms context to set
     */
    public static void setAdminCms(CmsObject adminCms) {

        if (m_adminCms == null) {
            m_adminCms = adminCms;
        }
    }

    /**
     * Generates a new login token for a given user and registers the token in the user's additional info.<p>
     *
     * @param cms the CMS context for which to create a new token
     * @return the generated token
     *
     * @throws CmsException if something goes wrong
     */
    public String createToken(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        String key = RandomStringUtils.randomAlphanumeric(16);
        Token tokenObj = new Token(user.getName(), key);
        String token = tokenObj.encode();
        String addInfoKey = tokenObj.getAdditionalInfoKey();
        String value = "" + (System.currentTimeMillis() + m_lifetime);
        user.getAdditionalInfo().put(addInfoKey, value);
        removeExpiredTokens(user, System.currentTimeMillis());
        LOG.info("Generated token for user " + user.getName() + " using key " + key);
        m_adminCms.writeUser(user);

        return token;
    }

    /**
     * Invalidates all tokens for the given user.<p>
     *
     * @param user the user
     * @param token the token string
     *
     * @throws CmsException if something goes wrong
     */
    public void invalidateToken(CmsUser user, String token) throws CmsException {

        Token tokenObj = new Token(token);
        if (tokenObj.isValid()) {
            String addInfoKey = tokenObj.getAdditionalInfoKey();
            if (null != user.getAdditionalInfo().remove(addInfoKey)) {
                m_adminCms.writeUser(user);
            }
        }
    }

    /**
     * Removes  expired tokens from the user's additional infos.<p>
     *
     * This method does not write the user back to the database.
     *
     * @param user the user for which to remove the additional infos
     * @param now the current time
     */
    public void removeExpiredTokens(CmsUser user, long now) {

        List<String> toRemove = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : user.getAdditionalInfo().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(KEY_PREFIX)) {
                try {
                    long expiry = Long.parseLong((String)entry.getValue());
                    if (expiry < now) {
                        toRemove.add(key);
                    }
                } catch (NumberFormatException e) {
                    toRemove.add(key);
                }
            }
        }
        LOG.info("Removing " + toRemove.size() + " expired tokens for user " + user.getName());
        for (String removeKey : toRemove) {
            user.getAdditionalInfo().remove(removeKey);
        }
    }

    /**
     * Sets the token lifetime.<p>
     *
     * @param duration the number of milliseconds for which the token should be valid
     */
    public void setTokenLifetime(long duration) {

        m_lifetime = duration;
    }

    /**
     * Validates a token and returns the matching user for which the token is valid.<p>
     *
     * Returns null if no user matching the token is found, or if the token for the user is expired
     *
     * @param tokenString the token for which to find the matching user
     *
     * @return the matching user for the token, or null if no matching user was found or the token is expired
     */
    public CmsUser validateToken(String tokenString) {

        if (CmsStringUtil.isEmpty(tokenString)) {
            return null;
        }
        Token token = new Token(tokenString);
        if (!token.isValid()) {
            LOG.warn("Invalid token: " + tokenString);
            return null;
        }
        String name = token.getName();
        String key = token.getKey();
        String logContext = "[user=" + name + ",key=" + key + "] ";
        try {
            CmsUser user = m_adminCms.readUser(name);
            String infoKey = token.getAdditionalInfoKey();
            String addInfoValue = (String)user.getAdditionalInfo().get(infoKey);
            logContext = logContext + "[value=" + addInfoValue + "]";
            if (addInfoValue == null) {
                LOG.warn(logContext + " no matching additional info value found");
                return null;
            }
            try {
                long expirationDate = Long.parseLong(addInfoValue);
                if (System.currentTimeMillis() > expirationDate) {
                    LOG.warn(logContext + "Login token expired");
                    user.getAdditionalInfo().remove(infoKey);
                    try {
                        m_adminCms.writeUser(user);
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    return null;
                }
            } catch (NumberFormatException e) {
                LOG.warn(logContext + "Invalid format for login token additional info");
                return null;
            }
            return user;
        } catch (Exception e) {
            LOG.warn(logContext + "error validating token", e);
            return null;
        }
    }
}
