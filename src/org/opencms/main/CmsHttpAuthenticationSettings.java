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

package org.opencms.main;

import org.opencms.workplace.CmsWorkplace;

/**
 * Contains the settings to handle HTTP basic authentication.<p>
 *
 * These settings control whether a browser-based pop-up dialog should be used for
 * authentication, or of the user should be redirected to an OpenCms URI for a
 * form-based authentication.<p>
 *
 * Since the URI for the form-based authentication is a system wide setting, users
 * are able to specify different authentication forms in a property "login-form" on
 * resources that require authentication.<p>
 *
 * @since 6.0.0
 */
public class CmsHttpAuthenticationSettings {

    /** The mechanism name for basic HTTP authentication. */
    public static final String AUTHENTICATION_BASIC = "BASIC";

    /** The mechanism name for form based authentication. */
    public static final String AUTHENTICATION_FORM = "FORM";

    /** The URI of the default authentication form. */
    public static final String DEFAULT_AUTHENTICATION_URI = CmsWorkplace.VFS_PATH_WORKPLACE
        + "action/authenticate.html";

    /** The mechanism used in browser-based HTTP authentication. */
    private String m_browserBasedAuthenticationMechanism;

    /** The URI of the system wide login form if browser-based HTTP basic authentication is disabled. */
    private String m_formBasedHttpAuthenticationUri;

    /** Boolean flag to enable or disable browser-based HTTP basic authentication. */
    private boolean m_useBrowserBasedHttpAuthentication;

    /**
     * Default constructor.<p>
     */
    public CmsHttpAuthenticationSettings() {

        super();
        m_useBrowserBasedHttpAuthentication = true;
        m_browserBasedAuthenticationMechanism = null;
        m_formBasedHttpAuthenticationUri = null;
    }

    /**
     * Returns the browser based authentication mechanism or <code>null</code> if unused.
     *
     * @return "BASIC" in case of browser based basic authentication, "FORM" in case of form based authentication or the alternative mechanism or <code>null</code> if unused.
     */
    public String getBrowserBasedAuthenticationMechanism() {

        if (m_useBrowserBasedHttpAuthentication) {
            return AUTHENTICATION_BASIC;
        } else if (m_browserBasedAuthenticationMechanism != null) {
            return m_browserBasedAuthenticationMechanism;
        } else if (m_formBasedHttpAuthenticationUri != null) {
            return AUTHENTICATION_FORM;
        } else {
            return null;
        }
    }

    /**
     * Returns the browser based authentication text for the configuration.<p>
     *
     * @return the browser based authentication text for the configuration
     */
    public String getConfigBrowserBasedAuthentication() {

        if (m_useBrowserBasedHttpAuthentication) {
            return Boolean.TRUE.toString();
        } else if (m_browserBasedAuthenticationMechanism != null) {
            return m_browserBasedAuthenticationMechanism;
        } else {
            return Boolean.FALSE.toString();
        }
    }

    /**
     * Returns the URI of the system wide login form if browser-based HTTP basic authentication is disabled.<p>
     *
     * @return the URI of the system wide login form if browser-based HTTP basic authentication is disabled
     */
    public String getFormBasedHttpAuthenticationUri() {

        return m_formBasedHttpAuthenticationUri;
    }

    /**
     * Sets the URI of the system wide login form if browser-based HTTP basic authentication is disabled.<p>
     *
     * @param uri the URI of the system wide login form if browser-based HTTP basic authentication is disabled to set
     */
    public void setFormBasedHttpAuthenticationUri(String uri) {

        m_formBasedHttpAuthenticationUri = uri;
    }

    /**
     * Sets if browser-based HTTP basic authentication is enabled or disabled.<p>
     *
     * @param value a boolean value to specifiy if browser-based HTTP basic authentication should be enabled
     */
    public void setUseBrowserBasedHttpAuthentication(boolean value) {

        m_useBrowserBasedHttpAuthentication = value;
        m_browserBasedAuthenticationMechanism = null;
    }

    /**
     * Sets if browser-based HTTP basic authentication is enabled or disabled.<p>
     *
     * @param value a string {<code>"true"</code>|<code>"false"</code>} to specify if browser-based HTTP basic authentication should be enabled;
     *        if another string is provided, the flag for browser based basic authentication is disabled and the value is stored as authentication mechanism.
     */
    public void setUseBrowserBasedHttpAuthentication(String value) {

        m_useBrowserBasedHttpAuthentication = Boolean.valueOf(value).booleanValue();
        if (!m_useBrowserBasedHttpAuthentication && !value.equalsIgnoreCase(Boolean.FALSE.toString())) {
            if (value.equalsIgnoreCase(AUTHENTICATION_BASIC)) {
                m_useBrowserBasedHttpAuthentication = true;
            } else {
                m_browserBasedAuthenticationMechanism = value;
                m_useBrowserBasedHttpAuthentication = false;
            }
        }
    }

    /**
     * Tests if browser-based HTTP basic authentication is enabled or disabled.<p>
     *
     * @return true if browser-based HTTP basic authentication is enabled
     */
    public boolean useBrowserBasedHttpAuthentication() {

        return m_useBrowserBasedHttpAuthentication;
    }

}
