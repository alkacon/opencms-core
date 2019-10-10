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

package org.opencms.jsp.userdata;

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;

/**
 * The stored information about a user data request.
 * <p>
 * This contains both the user data itself, as well as authentication information, for making sure the information can not just be automatically downloaded by someone
 * intercepting the email sent to the user.
 */
public class CmsUserDataRequestInfo {

    /** JSON key. */
    public static final String A_AUTH = "auth";

    /** JSON key. */
    public static final String A_CONTENT = "content";

    /** JSON key. */
    public static final String A_EMAIL = "email";

    /** JSON key. */
    public static final String A_EXPIRATION = "exp";

    /** JSON key. */
    public static final String A_ID = "id";

    /** JSON key. */
    public static final String A_TYPE = "type";

    /** JSON key. */
    public static final String A_USERIDS = "userids";

    /** JSON key. */
    private static final String A_HTML = "html";

    /** JSON key. */
    private static final String A_USER = "user";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDataRequestInfo.class);

    /** The JSON object used to store the information. */
    private JSONObject m_json = new JSONObject();

    /**
     * Creates a new user data request object.
     */
    public CmsUserDataRequestInfo() {

        setId(RandomStringUtils.randomAlphanumeric(20).toLowerCase());
        setAuth(RandomStringUtils.randomAlphanumeric(20).toLowerCase());
    }

    /**
     * Creates a new instance from the JSON data in a string.
     *
     * @param data the data as a string
     * @throws JSONException if parsing the JSON fails
     */
    public CmsUserDataRequestInfo(String data)
    throws JSONException {

        m_json = new JSONObject(data);
    }

    /**
     * Checks that the given authorization code matches the one stored in this object.
     *
     * @param auth the authorization code
     * @return true if the code matches
     */
    public boolean checkAuthCode(String auth) {

        String storedAuth = getAuthCode();
        return (storedAuth != null) && storedAuth.equals(auth);
    }

    /**
     * Gets the authorization code.
     *
     * @return the authorization code
     */
    public String getAuthCode() {

        return m_json.optString(A_AUTH);
    }

    /**
     * Gets the email address.
     *
     * @return the email address
     */
    public String getEmail() {

        return m_json.optString(A_EMAIL);
    }

    /**
     * Gets the expiration date as a long.
     *
     * @return the expiration date
     */
    public long getExpiration() {

        return m_json.optLong(A_EXPIRATION, -1l);
    }

    /**
     * Gets the id of the user data request.
     *
     * @return the id
     */
    public String getId() {

        return m_json.optString(A_ID);
    }

    /**
     * Gets the user data HTML text.
     *
     * @return the user data HTML text
     */
    public String getInfoHtml() {

        return m_json.optString(A_HTML);
    }

    /**
     * Gets the user data request type (single user or email).
     *
     * @return the user data request type
     */
    public CmsUserDataRequestType getType() {

        String strType = m_json.optString(A_TYPE);
        if (strType == null) {
            return null;
        }
        return CmsUserDataRequestType.valueOf(strType);
    }

    /**
     * Gets the full user name.<p>
     *
     * This is not set in case of email-only user data requests.
     *
     * @return the full user name
     */
    public String getUserName() {

        return m_json.optString(A_USER);
    }

    /**
     * Returns true if the user data request has expired.
     *
     * @return true if the user data request has expired
     */
    public boolean isExpired() {

        return System.currentTimeMillis() > getExpiration();
    }

    /**
     * Sets the authorization code.
     *
     * @param auth the authorization code.
     */
    public void setAuth(String auth) {

        setString(A_AUTH, auth);
    }

    /**
     * Sets the email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) {

        setString(A_EMAIL, email);
    }

    /**
     * Sets the expiration date.
     *
     * @param expiration the expiration date
     */
    public void setExpiration(long expiration) {

        try {
            m_json.put(A_EXPIRATION, expiration);
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(String id) {

        setString(A_ID, id);
    }

    /**
     * Sets the user data HTML.
     *
     * @param infoHtml the user data HTML
     */
    public void setInfoHtml(String infoHtml) {

        setString(A_HTML, infoHtml);
    }

    /**
     * Sets the user data request type.
     *
     * @param type the user data request type
     */
    public void setType(CmsUserDataRequestType type) {

        setString(A_TYPE, type.toString());
    }

    /**
     * Sets the full user name.
     *
     * @param user the full user name
     */
    public void setUser(String user) {

        setString(A_USER, user);
    }

    /**
     * Formats this object as JSON text.
     *
     * @return the JSON text
     */
    public String toJson() {

        return m_json.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return toJson();
    }

    /**
     * Helper method to set a string value in the JSON.
     *
     * @param key the attribute name
     * @param value the value
     */
    private void setString(String key, String value) {

        try {
            m_json.put(key, value);
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
