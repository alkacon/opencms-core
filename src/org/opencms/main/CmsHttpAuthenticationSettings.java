/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsHttpAuthenticationSettings.java,v $
 * Date   : $Date: 2005/04/26 13:20:51 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.main;

import org.opencms.workplace.I_CmsWpConstants;

/**
 * Contains the settings to handle HTTP basic authentication.<p>
 * 
 * These settings control wheter a browser-based popup dialog should be used for
 * authentication, or of the user should be redirected to an OpenCms URI for a
 * form-based authentication.<p>
 * 
 * Since the URI for the form-based authentication is a system wide setting, users
 * are able to specify different authentication forms in a property "login-form" on
 * resources that require authentication.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsHttpAuthenticationSettings {
    
    /** Boolean flag to enable or disable browser-based HTTP basic authentication. */
    private boolean m_useBrowserBasedHttpAuthentication;
    
    /** The URI of the system wide login form if browser-based HTTP basic authentication is disabled. */
    private String m_formBasedHttpAuthenticationUri;
    
    /** The URI of the default authentication form. */
    public static final String C_DEFAULT_AUTHENTICATION_URI = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/authenticate.html";

    /**
     * Default constructor.<p>
     */
    public CmsHttpAuthenticationSettings() {
        
        super();
        m_useBrowserBasedHttpAuthentication = true;
        m_formBasedHttpAuthenticationUri = null;
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
     * Tests if browser-based HTTP basic authentication is enabled or disabled.<p>
     *
     * @return true if browser-based HTTP basic authentication is enabled
     */
    public boolean useBrowserBasedHttpAuthentication() {

        return m_useBrowserBasedHttpAuthentication;
    }
    
    /**
     * Sets if browser-based HTTP basic authentication is enabled or disabled.<p>
     *
     * @param value a boolean value to specifiy if browser-based HTTP basic authentication should be enabled
     */
    public void setUseBrowserBasedHttpAuthentication(boolean value) {

        m_useBrowserBasedHttpAuthentication = value;
    }
    
    /**
     * Sets if browser-based HTTP basic authentication is enabled or disabled.<p>
     *
     * @param value a string {"true"|"false"} to specify if browser-based HTTP basic authentication should be enabled
     */
    public void setUseBrowserBasedHttpAuthentication(String value) {

        m_useBrowserBasedHttpAuthentication = Boolean.valueOf(value).booleanValue();
    }    
    
}
