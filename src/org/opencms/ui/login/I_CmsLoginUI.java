/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.login.CmsLoginController.CmsLoginTargetInfo;

import java.util.List;

/**
 * Interface for the login user interface.<p>
 */
public interface I_CmsLoginUI {

    /**
     * Gets the selected org unit.<p>
     *
     * @return the selected org unit
     */
    String getOrgUnit();

    /**
     * Gets the password.<p>
     *
     * @return the password
     */
    String getPassword();

    /**
     * Gets the selected PC type.<p>
     *
     * @return the PC type
     */
    String getPcType();

    /**
     * Gets the user name.<p>
     *
     * @return the user name
     */
    String getUser();

    /**
     * Opens the login target for a logged in user.<p>
     *
     * @param targetInfo the login target information
     */
    void openLoginTarget(CmsLoginTargetInfo targetInfo);

    /**
     * Sets the org units which should be selectable by the user.<p>
     *
     * @param ous the selectable org units
     */
    void setSelectableOrgUnits(List<CmsOrganizationalUnit> ous);

    /**
     * Show notification that the user is already loogged in.<p>
     */
    void showAlreadyLoggedIn();

    /**
     * Shows the 'forgot password view'.<p>
     *
     * @param authToken the authorization token given as a request parameter
     */
    void showForgotPasswordView(String authToken);

    /**
     * Shows the given login error message.<p>
     *
     * @param messageHtml the message HTML
     */
    void showLoginError(String messageHtml);

    /**
     * Initializes the login view.<p>
     *
     * @param preselectedOu a potential preselected OU
     */
    void showLoginView(String preselectedOu);

    /**
     * Shows the password reset dialog.<p>
     */
    void showPasswordResetDialog();

}
