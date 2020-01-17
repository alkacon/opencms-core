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

import org.opencms.ui.shared.components.CmsPasswordFieldState;

import com.vaadin.v7.ui.TextField;

/**
 * Password field for the login dialog.<p>
 */
public class CmsLoginPasswordField extends TextField {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p>
     */
    public CmsLoginPasswordField() {

        super();
        setImmediate(false);
        setWidth("100%");
    }

    /**
     * Checks if the password is visible.
     *
     * @return true if the password is visible
     */
    public boolean isPasswordVisible() {

        return getState(false).isPasswordVisible();
    }

    /**
     * Sets the visibility of the password.
     *
     * <p>If false, show the password field as a standard password field,
     * and if true, just as a text field.
     *
     * @param visible the password visibility
     */
    public void setPasswordVisible(boolean visible) {

        getState().setPasswordVisible(visible);

    }

    /**
     * @see com.vaadin.v7.ui.AbstractTextField#getState()
     */
    @Override
    protected CmsPasswordFieldState getState() {

        return (CmsPasswordFieldState)(super.getState());
    }

    /**
     * @see com.vaadin.v7.ui.AbstractTextField#getState(boolean)
     */
    @Override
    protected CmsPasswordFieldState getState(boolean dirty) {

        return (CmsPasswordFieldState)(super.getState(dirty));
    }

}
