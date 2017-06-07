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

package org.opencms.workplace.editors.directedit;

/**
 * Constants to indicate the direct edit permissions of a user for a VFS resource,
 * used to describe if and how to show the direct edit buttons for the resource.<p>
 *
 * @since 6.2.3
 */
public final class CmsDirectEditPermissions {

    /**
     * Describes the "disabled" permission.<p>
     *
     * User has general permissions to direct edit a resource, but this is currently not possible
     * because for example another user has locked the resource.<p>
     *
     * Direct edit buttons are displayed, but "grayed out".
     */
    public static final CmsDirectEditPermissions DISABLED = new CmsDirectEditPermissions(1);

    /**
     * Describes the "enabled" permission.<p>
     *
     * User has permissions to direct edit a resource, the resource is also available for direct edit.<p>
     *
     * Direct edit buttons are displayed and active.
     */
    public static final CmsDirectEditPermissions ENABLED = new CmsDirectEditPermissions(2);

    /**
     * Describes the "inactive" permission.<p>
     *
     * User has no permissions to direct edit a resource.
     * This may be because of write permissions, or because the resource is not part of the current
     * project, or for other reasons.<p>
     *
     * Direct edit buttons are not displayed at all.
     */
    public static final CmsDirectEditPermissions INACTIVE = new CmsDirectEditPermissions(0);

    /** String constant for {@link #DISABLED}. */
    public static final String VALUE_DISABLED = "disabled";

    /** String constant for {@link #ENABLED}. */
    public static final String VALUE_ENABLED = "enabled";

    /** String constant for {@link #INACTIVE}. */
    public static final String VALUE_INACTIVE = "inactive";

    /** The direct edit permission value. */
    int m_permission;

    /**
     * Hides the public constructor.<p>
     *
     * @param value the direct edit mode
     */
    private CmsDirectEditPermissions(int value) {

        m_permission = value;
    }

    /**
     * Returns the direct edit permission int value.<p>
     *
     * The possible value are:
     * <ul>
     * <li>0: Permission is {@link #INACTIVE}.
     * <li>1: Permission is {@link #DISABLED}.
     * <li>2: Permission is {@link #ENABLED}.
     * </ul>
     *
     * @return the direct edit permission int value
     */
    public int getPermission() {

        return m_permission;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String result;
        switch (m_permission) {
            case 1:
                result = VALUE_DISABLED;
                break;
            case 2:
                result = VALUE_ENABLED;
                break;
            default:
                result = VALUE_INACTIVE;
        }
        return result;
    }
}