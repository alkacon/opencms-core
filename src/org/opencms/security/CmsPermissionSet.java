/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsPermissionSet.java,v $
 * Date   : $Date: 2004/06/04 09:06:42 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.security;

import org.opencms.main.I_CmsConstants;

import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A permission set contains both allowed and denied permissions as bitsets.<p>
 * 
 * Currently supported permissions are:<br>
 * <code>C_PERMISSION_READ</code> (r) the right to read the contents of a resource<br>
 * <code>C_PERMISSION_WRITE</code> (w) the right to write the contents of a resource<br>
 * <code>C_PERMISSION_VIEW</code> (v) the right to see a resource in listings (workplace)<br>
 * <code>C_PERMISSION_CONTROL</code> (c) the right to set permissions of a resource<br>
 * <code>C_PERMISSION_DIRECT_PUBLISH</code> (d) the right direct publish a resource even without publish project permissions<br>
 * 
 * @version $Revision: 1.13 $ $Date: 2004/06/04 09:06:42 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsPermissionSet {

    /**  Hashtable of all available permissions */
    static HashMap m_permissions = null;

    /**
     * Returns the message keys of each permission known in the system.<p>
     * 
     * @return Enumeration of message keys
     */
    public static Set getPermissionKeys() {

        return permissions().keySet();
    }

    /**
     * Returns the value of a single permission.<p>
     * 
     * @param key the key of the permission
     * @return the value of the given permission
     */
    public static int getPermissionValue(String key) {

        return ((Integer)permissions().get(key)).intValue();
    }

    /**
     * Initializes and returns the hashtable of all permissions known in the system.<p>
     * 
     * @return hastable with permission keys and values
     */
    private static HashMap permissions() {

        if (m_permissions == null) {
            m_permissions = new HashMap();
            m_permissions.put("security.permission.read", new Integer(I_CmsConstants.C_PERMISSION_READ));
            m_permissions.put("security.permission.write", new Integer(I_CmsConstants.C_PERMISSION_WRITE));
            m_permissions.put("security.permission.view", new Integer(I_CmsConstants.C_PERMISSION_VIEW));
            m_permissions.put("security.permission.control", new Integer(I_CmsConstants.C_PERMISSION_CONTROL));
            m_permissions.put("security.permission.direct_publish", new Integer(
                I_CmsConstants.C_PERMISSION_DIRECT_PUBLISH));
        }
        return m_permissions;
    }

    /** The set of allowed permissions */
    int m_allowed;

    /** The set of denied permissions */
    int m_denied;

    /**
     * Constructor to create an empty permission set.<p>
     */
    public CmsPermissionSet() {

        m_allowed = 0;
        m_denied = 0;
    }
    

    /**
     * Constructor to create a permission set with some preset allowed permissions.<p>
     * 
     * @param allowedPermissions bitset of allowed permissions
     */
    public CmsPermissionSet(int allowedPermissions) {

        m_allowed = allowedPermissions;
        m_denied = 0;
    }

    /**
     * Constructor to create a permission set with some preset allowed and denied permissions.<p>
     * 
     * @param allowedPermissions the set of permissions to allow
     * @param deniedPermissions the set of permissions to deny
     */
    public CmsPermissionSet(int allowedPermissions, int deniedPermissions) {

        m_allowed = allowedPermissions;
        m_denied = deniedPermissions;
    }

    /**
     * Constructor to create a permission set with preset allowed and denied permissions.<p>
     * The permissions are read from a string representation of permissions 
     * in the format {{+|-}{r|w|v|c|d}}*.
     * 
     * @param permissionString the string representation of allowed and denied permissions
     */
    public CmsPermissionSet(String permissionString) {

        StringTokenizer tok = new StringTokenizer(permissionString, "+-", true);
        m_allowed = 0;
        m_denied = 0;

        while (tok.hasMoreElements()) {
            String prefix = tok.nextToken();
            String suffix = tok.nextToken();
            switch (suffix.charAt(0)) {
                case 'R':
                case 'r':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= I_CmsConstants.C_PERMISSION_READ;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= I_CmsConstants.C_PERMISSION_READ;
                    }
                    break;
                case 'W':
                case 'w':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= I_CmsConstants.C_PERMISSION_WRITE;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= I_CmsConstants.C_PERMISSION_WRITE;
                    }
                    break;
                case 'V':
                case 'v':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= I_CmsConstants.C_PERMISSION_VIEW;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= I_CmsConstants.C_PERMISSION_VIEW;
                    }
                    break;
                case 'C':
                case 'c':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= I_CmsConstants.C_PERMISSION_CONTROL;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= I_CmsConstants.C_PERMISSION_CONTROL;
                    }
                    break;
                case 'D':
                case 'd':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= I_CmsConstants.C_PERMISSION_DIRECT_PUBLISH;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= I_CmsConstants.C_PERMISSION_DIRECT_PUBLISH;
                    }
                    break;
                default:
                    // ignore
                    break;
            }
        }
    }

    /**
     * Sets permissions from another permission set additionally both as allowed and denied permissions.<p>
     * 
     * @param permissionSet the set of permissions to set additionally.
     */
    public void addPermissions(CmsPermissionSet permissionSet) {

        m_allowed |= permissionSet.m_allowed;
        m_denied |= permissionSet.m_denied;
    }

    /**
     * Sets permissions additionally both as allowed and denied permissions.<p>
     * 
     * @param allowedPermissions bitset of permissions to allow
     * @param deniedPermissions  bitset of permissions to deny
     */
    public void addPermissions(int allowedPermissions, int deniedPermissions) {

        m_allowed |= allowedPermissions;
        m_denied |= deniedPermissions;
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        return new CmsPermissionSet(m_allowed, m_denied);
    }

    /**
     * Sets permissions additionally as denied permissions.<p>
     * 
     * @param permissions bitset of permissions to deny
     */
    public void denyPermissions(int permissions) {

        m_denied |= permissions;
    }
    
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        boolean equal = true;
        CmsPermissionSet perm = (CmsPermissionSet) obj;
        if ((perm.getAllowedPermissions() != m_allowed) || (perm.getDeniedPermissions() != m_denied)) {
            equal = false;
        }       
        return equal;
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

        if ((m_denied & I_CmsConstants.C_PERMISSION_READ) > 0) {
            p.append("-r");
        } else if ((m_allowed & I_CmsConstants.C_PERMISSION_READ) > 0) {
            p.append("+r");
        }

        if ((m_denied & I_CmsConstants.C_PERMISSION_WRITE) > 0) {
            p.append("-w");
        } else if ((m_allowed & I_CmsConstants.C_PERMISSION_WRITE) > 0) {
            p.append("+w");
        }

        if ((m_denied & I_CmsConstants.C_PERMISSION_VIEW) > 0) {
            p.append("-v");
        } else if ((m_allowed & I_CmsConstants.C_PERMISSION_VIEW) > 0) {
            p.append("+v");
        }

        if ((m_denied & I_CmsConstants.C_PERMISSION_CONTROL) > 0) {
            p.append("-c");
        } else if ((m_allowed & I_CmsConstants.C_PERMISSION_CONTROL) > 0) {
            p.append("+c");
        }

        if ((m_denied & I_CmsConstants.C_PERMISSION_DIRECT_PUBLISH) > 0) {
            p.append("-d");
        } else if ((m_allowed & I_CmsConstants.C_PERMISSION_DIRECT_PUBLISH) > 0) {
            p.append("+d");
        }

        return p.toString();
    }

    /**
     * Sets permissions additionally as allowed permissions.<p>
     * 
     * @param permissions bitset of permissions to allow
     */
    public void grantPermissions(int permissions) {

        m_allowed |= permissions;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return m_allowed * m_denied;
    }

    /**
     * Set permissions from another permission set both as allowed and denied permissions.<p>
     * Permissions formerly set are overwritten.
     * 
     * @param permissionSet the set of permissions
     */
    public void setPermissions(CmsPermissionSet permissionSet) {

        m_allowed = permissionSet.m_allowed;
        m_denied = permissionSet.m_denied;
    }

    /**
     * Sets permissions as allowed and denied permissions in the permission set.<p>
     * Permissions formerly set are overwritten.
     * 
     * @param allowedPermissions bitset of permissions to allow
     * @param deniedPermissions  bitset of permissions to deny
     */
    public void setPermissions(int allowedPermissions, int deniedPermissions) {

        m_allowed = allowedPermissions;
        m_denied = deniedPermissions;
    }

    /**
     * Returns the String representation of this permission set object.<p>
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return "[PermissionSet:] " + getPermissionString();
    }
}