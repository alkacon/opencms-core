/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.security;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;

import java.util.Locale;

/**
 * Interface for plugging in a custom login button in the login dialog which redirects to some sort of third-party authentication page.
 */
public interface I_CmsCustomLogin extends I_CmsConfigurationParameterHandler {

    /**
     * Gets the page to redirect to after the user has clicked on the custom button.
     *
     * @param orgUnit the organizational unit that has been selected, or null if none was selected
     * @return the redirect URI
     */
    public String getRedirect(String orgUnit);

    /**
     * Gets the caption to display on the button for the given locale.
     *
     * @param locale the locale
     * @return the caption to display
     */
    String getLoginButtonCaption(Locale locale);

    /**
     * Sets the CmsObject needed for VFS operations or anything like that.
     *
     * @param cms a CmsObject with admin privileges
     */
    void initialize(CmsObject cms);

    /**
     * Only if this returns true should the custom login be enabled.
     *
     * @return true if the custom login should be enabled
     */
    boolean isEnabled();

    /**
     * Checks if the custom login method needs the user to select the OU in the login dialog.
     *
     * @return true if the custom login method needs the OU
     */
    boolean needsOrgUnit();

}
