/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsAccessControlEntry.java,v $
 * Date   : $Date: 2005/09/16 08:51:57 $
 * Version: $Revision: 1.19.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.util.CmsUUID;

import java.util.StringTokenizer;

/**
 * An access control entry defines the permissions of a user or group for a distinct resource.<p>
 * 
 * Besides the <code>CmsPermissionSet</code> to define the permissions, the access control entry
 * contains the UUID of the resource and of the principal (user or group) who has the defined permissions.
 * Since the principal is identified by its UUID, any other entity may act as principal also.
 * 
 * <p>Additionally, the entry stores various flags:<br>
 * <code>ACCESS_FLAGS_DELETED</code> indicates that this entry is deleted<br>
 * <code>ACCESS_FLAGS_INHERIT</code> indicates that this entry should be inherited<br>
 * <code>ACCESS_FLAGS_OVERWRITE</code> indicates that this entry overwrites inherited settings<br>
 * <code>ACCESS_FLAGS_INHERITED</code> indicates that this entry is inherited<br>
 * <code>ACCESS_FLAGS_USER</code> indicates that the principal is a single user<br>
 * <code>ACCESS_FLAGS_GROUP</code> indicates that the principal is a group
 * </p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.19.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAccessControlEntry {

    /**
     * Flags of this access control entry.
     */
    private int m_flags;

    /**
     * The permission set.
     */
    private CmsPermissionSetCustom m_permissions;

    /**
     * Id of the principal.
     */
    private CmsUUID m_principal;

    /**
     * Id of the resource.
     */
    private CmsUUID m_resource;

    /** Flag to indicate that an access control entry is currently deleted. */
    public static final int ACCESS_FLAGS_DELETED = 1;

    /** Flag to indicate the pricipal type group. */
    public static final int ACCESS_FLAGS_GROUP = 32;

    /** Flag to indicate the principal type user. */
    public static final int ACCESS_FLAGS_USER = 16;

    /** Flag to indicate that an access control entry should be inherited. */
    public static final int ACCESS_FLAGS_INHERIT = 2;

    /** Flag to indicate that an access control entry was inherited (read only). */
    public static final int ACCESS_FLAGS_INHERITED = 8;

    /** Flag to indicate that an access control entry overwrites inherited entries. */
    public static final int ACCESS_FLAGS_OVERWRITE = 4;

    /** Flag to indicate that the principal is responsible for the resource. */
    public static final int ACCESS_FLAGS_RESPONSIBLE = 64;
    
    /**
     * Constructor to create a new access control entry for a given resource
     * based on an existing access control entry.<p>
     * 
     * @param resource the resource
     * @param base the base for the created access control entry
     */
    public CmsAccessControlEntry(CmsUUID resource, CmsAccessControlEntry base) {

        m_resource = resource;
        m_principal = base.m_principal;
        m_permissions = base.m_permissions;
        m_flags = base.m_flags;
    }

    /**
     * Constructor to create a new access control entry on a given resource and a given principal.<p>
     * Permissions are specified as permission set, flags as bitset.
     * 
     * @param resource the resource
     * @param principal the id of a principal (user or group)
     * @param permissions the set of allowed and denied permissions as permission set
     * @param flags additional flags of the access control entry
     */
    public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, CmsPermissionSet permissions, int flags) {

        m_resource = resource;
        m_principal = principal;
        m_permissions = new CmsPermissionSetCustom(permissions);
        m_flags = flags;
    }

    /**
     * Constructor to create a new access control entry on a given resource and a given principal.<p>
     * Permissions and flags are specified as bitsets.
     * 
     * @see CmsPermissionSet
     * 
     * @param resource the resource
     * @param principal the id of a principal (user or group)
     * @param allowed the set of allowed permissions
     * @param denied set set of explicitly denied permissions
     * @param flags additional flags of the access control entry
     */
    public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, int allowed, int denied, int flags) {

        m_resource = resource;
        m_principal = principal;
        m_permissions = new CmsPermissionSetCustom(allowed, denied);
        m_flags = flags;
    }

    /**
     * Constructor to create a new access control entry on a given resource and a given principal.<p>
     * Permission and flags are specified as string of the format {{+|-}{r|w|v|c|i}}*
     * 
     * @param resource the resource
     * @param principal the id of a principal (user or group)
     * @param acPermissionString allowed and denied permissions and also flags
     */
    public CmsAccessControlEntry(CmsUUID resource, CmsUUID principal, String acPermissionString) {

        m_resource = resource;
        m_principal = principal;
        m_flags = 0;

        StringTokenizer tok = new StringTokenizer(acPermissionString, "+-", true);
        StringBuffer permissionString = new StringBuffer();

        while (tok.hasMoreElements()) {
            String prefix = tok.nextToken();
            String suffix = tok.nextToken();
            switch (suffix.charAt(0)) {
                case 'I':
                case 'i':
                    if (prefix.charAt(0) == '+') {
                        m_flags |= CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;
                    }
                    break;
                case 'O':
                case 'o':
                    if (prefix.charAt(0) == '+') {
                        m_flags |= CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE;
                    }
                    break;
                case 'L':
                case 'l':
                    if (prefix.charAt(0) == '+') {
                        m_flags |= CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE;
                    }
                    if (prefix.charAt(0) == '-') {
                        m_flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE;
                    }
                    break;    
                default:
                    permissionString.append(prefix);
                    permissionString.append(suffix);
                    break;
            }
        }

        m_permissions = new CmsPermissionSetCustom(permissionString.toString());
    }

    /**
     * Returns the string representation of the "responsible" flag.<p>
     * 
     * @return string of the format {{+|-}s}*
     */
    public String getResponsibleString() {

        if (isResponsible()) {
            return "+l";
        } else  {
            return "+l";
        }
    }
    
    /**
     * Sets the explicitly denied permissions in the access control entry.<p>
     * 
     * @param denied the denied permissions as bitset
     */
    public void denyPermissions(int denied) {

        m_permissions.denyPermissions(denied);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsAccessControlEntry) {
            CmsAccessControlEntry other = (CmsAccessControlEntry)obj;
            if (other.m_flags != m_flags) {
                return false;
            }
            if (other.getPermissions().getAllowedPermissions() != getPermissions().getAllowedPermissions()) {
                return false;
            }
            if (other.getPermissions().getDeniedPermissions() != getPermissions().getDeniedPermissions()) {
                return false;
            }
            if (!other.m_resource.equals(m_resource)) {
                return false;
            }
            if (!other.m_principal.equals(m_principal)) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the currently allowed permissions as bitset.<p>
     * 
     * @return the allowed permissions
     */
    public int getAllowedPermissions() {

        return m_permissions.getAllowedPermissions();
    }

    /**
     * Returns the currently denied permissions as bitset.<p>
     * 
     * @return the denied permissions
     */
    public int getDeniedPermissions() {

        return m_permissions.getDeniedPermissions();
    }

    /**
     * Returns the current flags of the access control entry.<p>
     * 
     * @return bitset with flag values
     */
    public int getFlags() {

        return m_flags;
    }

    /**
     * Returns the current permission set (both allowed and denied permissions).<p>
     * 
     * @return the set of permissions
     */
    public CmsPermissionSet getPermissions() {

        return m_permissions;
    }

    /**
     * Returns the principal assigned with this access control entry.<p>
     * 
     * @return the principal
     */
    public CmsUUID getPrincipal() {

        return m_principal;
    }

    /**
     * Returns the resource assigned with this access control entry.<p>
     * 
     * @return the resource 
     */
    public CmsUUID getResource() {

        return m_resource;
    }

    /**
     * Sets the allowed permissions in the access control entry.<p>
     * 
     * @param allowed the allowed permissions as bitset
     */
    public void grantPermissions(int allowed) {

        m_permissions.grantPermissions(allowed);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        if (m_permissions != null) {
            return m_permissions.hashCode() * m_flags;
        }
        return CmsUUID.getNullUUID().hashCode();
    }

    /**
     * Returns if this access control entry has the inherited flag set.<p>
     * Note: to check if an access control entry is inherited, also the
     * resource id and the id of the current resource must be different.
     * 
     * @return true, if the inherited flag is set
     */
    public boolean isInherited() {

        return ((m_flags & CmsAccessControlEntry.ACCESS_FLAGS_INHERITED) > 0);
    }
    
    /**
     * Returns if the principal is responsible for the current resource.<p>
     * 
     * @return  true ,if the principal is responsible for the current resource
     */
    public boolean isResponsible() {

        return ((m_flags & CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE) > 0);
    }

    /**
     * Resets the given flags in the access control entry.<p>
     * 
     * @param flags bitset with flag values to reset
     */
    public void resetFlags(int flags) {

        m_flags &= ~flags;
    }

    /**
     * Sets the given flags in the access control entry.<p>
     * 
     * @param flags bitset with flag values to set
     */
    public void setFlags(int flags) {

        m_flags |= flags;
    }

    /**
     * Sets the allowed and denied permissions of the access control entry.<p>
     * 
     * @param permissions the set of permissions
     */
    public void setPermissions(CmsPermissionSet permissions) {

        m_permissions.setPermissions(permissions);
    }

    /**
     * Returns the String representation of this access control entry object.<p>
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return "[Ace:] "
            + "ResourceId="
            + m_resource
            + ", PrincipalId="
            + m_principal
            + ", Permissions="
            + m_permissions.toString()
            + ", Flags="
            + m_flags;
    }
}