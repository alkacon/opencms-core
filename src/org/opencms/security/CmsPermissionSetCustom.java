/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsPermissionSetCustom.java,v $
 * Date   : $Date: 2004/08/23 15:37:02 $
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

package org.opencms.security;

import java.util.StringTokenizer;

/**
 * A custom permission set that can be modified during runtime and contains both allowed and denied permissions as bitsets.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsPermissionSetCustom extends CmsPermissionSet {

    /**
     * Constructor to create an empty permission set.<p>
     */
    public CmsPermissionSetCustom() {

        super();
    }

    /**
     * Constructor to create a permission set with preset allowed permissions.<p>
     * 
     * @param allowedPermissions bitset of allowed permissions
     */
    public CmsPermissionSetCustom(int allowedPermissions) {

        super(allowedPermissions);

    }

    /**
     * Constructor to create a permission set with preset allowed and denied permissions.<p>
     * 
     * @param allowedPermissions the set of permissions to allow
     * @param deniedPermissions the set of permissions to deny
     */
    public CmsPermissionSetCustom(int allowedPermissions, int deniedPermissions) {

        super(allowedPermissions, deniedPermissions);
    }

    /**
     * Constructor to create a permission set with preset allowed and denied permissions from a String.<p>
     * 
     * The permissions are read from a string representation of permissions 
     * in the format <code>{{+|-}{r|w|v|c|d}}*</code>.<p>
     * 
     * @param permissionString the string representation of allowed and denied permissions
     */
    public CmsPermissionSetCustom(String permissionString) {

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
                        m_allowed |= CmsPermissionSet.PERMISSION_READ;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= CmsPermissionSet.PERMISSION_READ;
                    }
                    break;
                case 'W':
                case 'w':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= CmsPermissionSet.PERMISSION_WRITE;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= CmsPermissionSet.PERMISSION_WRITE;
                    }
                    break;
                case 'V':
                case 'v':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= CmsPermissionSet.PERMISSION_VIEW;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= CmsPermissionSet.PERMISSION_VIEW;
                    }
                    break;
                case 'C':
                case 'c':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= CmsPermissionSet.PERMISSION_CONTROL;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= CmsPermissionSet.PERMISSION_CONTROL;
                    }
                    break;
                case 'D':
                case 'd':
                    if (prefix.charAt(0) == '+') {
                        m_allowed |= CmsPermissionSet.PERMISSION_DIRECT_PUBLISH;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_denied |= CmsPermissionSet.PERMISSION_DIRECT_PUBLISH;
                    }
                    break;
                default:
                    // ignore
                    break;
            }
        }
    }

    /**
     * Constructor to create a permission set with preset allowed and denied permissions from another permission set.<p>
     * 
     * The permissions are read from a string representation of permissions 
     * in the format <code>{{+|-}{r|w|v|c|d}}*</code>.<p>
     * 
     * @param permissions the set of allowed and denied permissions
     */
    public CmsPermissionSetCustom(CmsPermissionSet permissions) {

        m_allowed = permissions.m_allowed;
        m_denied = permissions.m_denied;
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
     * Sets permissions additionally as denied permissions.<p>
     * 
     * @param permissions bitset of permissions to deny
     */
    public void denyPermissions(int permissions) {

        m_denied |= permissions;
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
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        return new CmsPermissionSetCustom(m_allowed, m_denied);
    }
    
}