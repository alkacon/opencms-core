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

package org.opencms.security;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An immutable permission set that contains both allowed and denied permissions as bitsets.<p>
 *
 * Currently supported permissions are:<ul>
 * <li><code>{@link CmsPermissionSet#PERMISSION_READ}</code> (r) the right to read the contents of a resource</li>
 * <li><code>{@link CmsPermissionSet#PERMISSION_WRITE}</code> (w) the right to write the contents of a resource</li>
 * <li><code>{@link CmsPermissionSet#PERMISSION_VIEW}</code> (v) the right to see a resource in listings (workplace)</li>
 * <li><code>{@link CmsPermissionSet#PERMISSION_CONTROL}</code> (c) the right to set permissions of a resource</li>
 * <li><code>{@link CmsPermissionSet#PERMISSION_DIRECT_PUBLISH}</code> (d) the right direct publish a resource even without publish project permissions</li></ul><p>
 *
 * @since 6.0.0
 */
public class CmsPermissionSet {

    /** Permission set to check control access. */
    public static final CmsPermissionSet ACCESS_CONTROL = new CmsPermissionSet(CmsPermissionSet.PERMISSION_CONTROL);

    /** Permission set to check direct publish permissions. */
    public static final CmsPermissionSet ACCESS_DIRECT_PUBLISH = new CmsPermissionSet(
        CmsPermissionSet.PERMISSION_DIRECT_PUBLISH);

    /** Permission set to check read access. */
    public static final CmsPermissionSet ACCESS_READ = new CmsPermissionSet(CmsPermissionSet.PERMISSION_READ);

    /** Permission set to check view access. */
    public static final CmsPermissionSet ACCESS_VIEW = new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW);

    /** Permission set to check write access. */
    public static final CmsPermissionSet ACCESS_WRITE = new CmsPermissionSet(CmsPermissionSet.PERMISSION_WRITE);

    /** The permission to control a resource. */
    public static final int PERMISSION_CONTROL = 8;

    /** The permission to direct publish a resource. */
    public static final int PERMISSION_DIRECT_PUBLISH = 16;

    /** No permissions for a resource (used especially for denied permissions). */
    public static final int PERMISSION_EMPTY = 0;

    /** All allowed permissions for a resource. */
    public static final int PERMISSION_FULL = CmsPermissionSet.PERMISSION_READ
        + CmsPermissionSet.PERMISSION_WRITE
        + CmsPermissionSet.PERMISSION_VIEW
        + CmsPermissionSet.PERMISSION_CONTROL
        + CmsPermissionSet.PERMISSION_DIRECT_PUBLISH;

    /** The permission to read a resource. */
    public static final int PERMISSION_READ = 1;

    /**  The permission to view a resource. */
    public static final int PERMISSION_VIEW = 4;

    /** The permission to write a resource. */
    public static final int PERMISSION_WRITE = 2;

    /** HashMap of all available permissions. */
    private static Map<String, Integer> m_permissions;

    /** The set of allowed permissions. */
    protected int m_allowed;

    /** The set of denied permissions. */
    protected int m_denied;

    /**
     * Constructor to create a permission set with preset allowed and denied permissions.<p>
     *
     * @param allowedPermissions the set of permissions to allow
     * @param deniedPermissions the set of permissions to deny
     */
    public CmsPermissionSet(int allowedPermissions, int deniedPermissions) {

        m_allowed = allowedPermissions;
        m_denied = deniedPermissions;
    }

    /**
     * Constructor to create an empty permission set.<p>
     */
    protected CmsPermissionSet() {

        // noop
    }

    /**
     * Constructor to create a permission set with preset allowed permissions.<p>
     *
     * @param allowedPermissions bitset of allowed permissions
     */
    protected CmsPermissionSet(int allowedPermissions) {

        m_allowed = allowedPermissions;
        m_denied = 0;
    }

    /**
     * Returns the message keys of each permission known in the system.<p>
     *
     * @return Enumeration of message keys
     */
    public static Set<String> getPermissionKeys() {

        return permissions().keySet();
    }

    /**
     * Returns the value of a single permission.<p>
     *
     * @param key the key of the permission
     * @return the value of the given permission
     */
    public static int getPermissionValue(String key) {

        return (permissions().get(key)).intValue();
    }

    /**
     * Initializes and returns the linked hash map of all permissions known in the system.<p>
     *
     * @return a linked hash map with permission keys and values
     */
    private static Map<String, Integer> permissions() {

        if (m_permissions == null) {
            LinkedHashMap<String, Integer> permissions = new LinkedHashMap<String, Integer>();
            permissions.put("GUI_PERMISSION_TYPE_READ_0", new Integer(CmsPermissionSet.PERMISSION_READ));
            permissions.put("GUI_PERMISSION_TYPE_WRITE_0", new Integer(CmsPermissionSet.PERMISSION_WRITE));
            permissions.put("GUI_PERMISSION_TYPE_VIEW_0", new Integer(CmsPermissionSet.PERMISSION_VIEW));
            permissions.put("GUI_PERMISSION_TYPE_CONTROL_0", new Integer(CmsPermissionSet.PERMISSION_CONTROL));
            permissions.put(
                "GUI_PERMISSION_TYPE_DIRECT_PUBLISH_0",
                new Integer(CmsPermissionSet.PERMISSION_DIRECT_PUBLISH));
            m_permissions = Collections.unmodifiableMap(permissions);
        }
        return m_permissions;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsPermissionSet) {
            CmsPermissionSet other = (CmsPermissionSet)obj;
            return (other.m_allowed == m_allowed) && (other.m_denied == m_denied);
        }
        return false;
    }

    /**
     * Returns the currently allowed permissions of ths permission set.<p>
     *
     * @return the allowed permissions as bitset
     */
    public int getAllowedPermissions() {

        return m_allowed;
    }

    /**
     * Returns the currently denied permissions of this permission set.<p>
     *
     * @return the denied permissions as bitset.
     */
    public int getDeniedPermissions() {

        return m_denied;
    }

    /**
     * Returns the permissions calculated from this permission set.<p>
     * These are all permissions allowed but not denied.
     *
     * @return the resulting permission set
     */
    public int getPermissions() {

        return m_allowed & ~m_denied;
    }

    /**
     * Returns the string representation of the current permissions in this permission set.<p>
     *
     * @return string of the format {{+|-}{r|w|v|c|d}}*
     */
    public String getPermissionString() {

        StringBuffer p = new StringBuffer("");

        if ((m_denied & CmsPermissionSet.PERMISSION_READ) > 0) {
            p.append("-r");
        } else if (requiresReadPermission()) {
            p.append("+r");
        }

        if ((m_denied & CmsPermissionSet.PERMISSION_WRITE) > 0) {
            p.append("-w");
        } else if (requiresWritePermission()) {
            p.append("+w");
        }

        if ((m_denied & CmsPermissionSet.PERMISSION_VIEW) > 0) {
            p.append("-v");
        } else if (requiresViewPermission()) {
            p.append("+v");
        }

        if ((m_denied & CmsPermissionSet.PERMISSION_CONTROL) > 0) {
            p.append("-c");
        } else if (requiresControlPermission()) {
            p.append("+c");
        }

        if ((m_denied & CmsPermissionSet.PERMISSION_DIRECT_PUBLISH) > 0) {
            p.append("-d");
        } else if (requiresDirectPublishPermission()) {
            p.append("+d");
        }

        return p.toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_allowed * m_denied;
    }

    /**
     * Returns true if control permissions (+c) are required by this permission set.<p>
     *
     * @return true if control permissions (+c) are required by this permission set
     */
    public boolean requiresControlPermission() {

        return 0 < (m_allowed & CmsPermissionSet.PERMISSION_CONTROL);
    }

    /**
     * Returns true if direct publish permissions (+d) are required by this permission set.<p>
     *
     * @return true if direct publish permissions (+d) are required by this permission set
     */
    public boolean requiresDirectPublishPermission() {

        return 0 < (m_allowed & CmsPermissionSet.PERMISSION_DIRECT_PUBLISH);
    }

    /**
     * Returns true if read permissions (+r) are required by this permission set.<p>
     *
     * @return true if read permissions (+r) are required by this permission set
     */
    public boolean requiresReadPermission() {

        return 0 < (m_allowed & CmsPermissionSet.PERMISSION_READ);
    }

    /**
     * Returns true if view permissions (+v) are required by this permission set.<p>
     *
     * @return true if view permissions (+v) are required by this permission set
     */
    public boolean requiresViewPermission() {

        return 0 < (m_allowed & CmsPermissionSet.PERMISSION_VIEW);
    }

    /**
     * Returns true if write permissions (+w) are required by this permission set.<p>
     *
     * @return true if write permissions (+w) are required by this permission set
     */
    public boolean requiresWritePermission() {

        return 0 < (m_allowed & CmsPermissionSet.PERMISSION_WRITE);
    }

    /**
     * Returns the String representation of this permission set object.<p>
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "[PermissionSet:] " + getPermissionString();
    }
}