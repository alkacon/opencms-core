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

package org.opencms.ui.login;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Helper class for dealing with authorization tokens for the 'forgot password' functionality.<p>
 *
 * When a user requests a link to change his password, an authorization token is generated and also stored in the user's
 * additional info (in a slightly different form). When the user opens the 'change password' link sent to him, the authentication
 * token is validated by comparing it to the token in the user's additional info. Additionally, the system checks whether the age
 * of the token stored in the additional infos is older than the maximum age, which can also be configured.
 *
 */
public class CmsTokenValidator {

    /** Additional info key to store the authorization data. */
    public static final String ADDINFO_KEY = "RESET_AUTH";

    /** The user. */
    private CmsUser m_user;

    /**
     * Removes an authorization token from the user's additional information.<p>
     *
     * @param cms the CMS context
     * @param user the user
     * @throws CmsException if something goes wrong
     */
    public static void clearToken(CmsObject cms, CmsUser user) throws CmsException {

        user.getAdditionalInfo().remove(ADDINFO_KEY);
        cms.writeUser(user);
    }

    /**
     * Creates a new token for the given user and stores it in the user's additional info.<p>
     *
     * @param cms the CMS context
     * @param user the user
     * @param currentTime the current time
     * @return the authorization token
     * @throws CmsException if something goes wrong
     */
    public static String createToken(CmsObject cms, CmsUser user, long currentTime) throws CmsException {

        String randomKey = RandomStringUtils.randomAlphanumeric(8);
        String value = CmsEncoder.encodeStringsAsBase64Parameter(Arrays.asList(randomKey, "" + currentTime));
        user.setAdditionalInfo(ADDINFO_KEY, value);
        cms.writeUser(user);
        return CmsEncoder.encodeStringsAsBase64Parameter(Arrays.asList(user.getName(), randomKey));
    }

    /**
     * Gets the user.<p>
     *
     * @return the user
     */
    public CmsUser getUser() {

        return m_user;
    }

    /**
     * Validates the authentication token against the token stored in the user's additional info.<p>
     *
     * @param cms the CMS context
     * @param token the authentication token
     * @param maxAgeMillis the maximum token age in milliseconds
     *
     * @return null if the validation is succesfull, or a string containing the error message if not
     * @throws CmsException if something goes wrong
     */
    public String validateToken(CmsObject cms, String token, long maxAgeMillis) throws CmsException {

        try {
            List<String> tokenValues = CmsEncoder.decodeStringsFromBase64Parameter(token);

            if (tokenValues.size() != 2) {
                return "Invalid token";
            }
            CmsUser user = cms.readUser(tokenValues.get(0));
            String userValue = (String)user.getAdditionalInfo(ADDINFO_KEY);
            if (userValue == null) {
                return "no additional infos found";
            }
            List<String> userValues = CmsEncoder.decodeStringsFromBase64Parameter(userValue);
            if (userValues.get(0).equals(tokenValues.get(1))) {
                String strUserTime = userValues.get(1);
                long usertime = Long.valueOf(strUserTime).longValue();
                if ((System.currentTimeMillis() - usertime) > maxAgeMillis) {
                    return "Auth token too old";
                }
                m_user = user;
                return null;
            } else {
                return "Key mismatch";
            }
        } catch (IllegalArgumentException e) {
            return "Token parse error";
        } catch (CmsDataAccessException e) {
            return "User not found";
        }
    }

}
