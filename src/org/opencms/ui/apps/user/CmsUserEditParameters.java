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

package org.opencms.ui.apps.user;

/**
 * Parameters for the user edit dialog.<p>
 */
public class CmsUserEditParameters {

    /** True if editing is enabled. */
    private boolean m_editEnabled = true;

    /** True if password change is enabled. */
    private boolean m_passwordChangeEnabled = true;

    /**
     * Checks if editing is enabled.<p>
     *
     * @return true if editing is enabled
     */
    public boolean isEditEnabled() {

        return m_editEnabled;
    }

    /**
     * Checks if password change is enabled.<p>
     *
     * @return true if password change is enabled
     */
    public boolean isPasswordChangeEnabled() {

        return m_passwordChangeEnabled;
    }

    /**
     * Enables/disables editing.
     *
     * @param editEnabled the new value
     */
    public void setEditEnabled(boolean editEnabled) {

        m_editEnabled = editEnabled;
    }

    /**
     * Enables/disables password change.
     *
     * @param passwordChangeEnabled the new value
     */
    public void setPasswordChangeEnabled(boolean passwordChangeEnabled) {

        m_passwordChangeEnabled = passwordChangeEnabled;
    }

}
