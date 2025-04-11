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

package org.opencms.ui.shared.components;

import com.vaadin.v7.shared.ui.textfield.AbstractTextFieldState;

/**
 * Widget state for the special password field used by the login dialog.
 */
public class CmsPasswordFieldState extends AbstractTextFieldState {

    /** Flag indicating whether the password is visible or not. */
    private boolean m_passwordVisible;

    /**
     * Returns true if the password is visible.
     *
     * @return true if the password is visible
     */
    public boolean isPasswordVisible() {

        return m_passwordVisible;

    }

    /**
     * Sets the password visibility.
     *
     * @param visible true if the password should be visible
     */
    public void setPasswordVisible(boolean visible) {

        m_passwordVisible = visible;
    }

}
