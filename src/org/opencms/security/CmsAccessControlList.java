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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An access control list contains the permission sets of all principals for a distinct resource
 * that are calculated on the permissions defined by various access control entries.<p>
 *
 * <p>To each single resource, access control entries of type <code>CmsAccessControlEntry</code> can be assigned.
 * An access control entry defines the permissions (both allowed and explicitly denied) of a user or group for this resource.</p>
 *
 * <p>By calling the method <code>getAccessControlList</code> the list is generated on the resource. It contains the result of
 * merging both access control entries defined immediately on the resource and inherited along the folder hierarchie in the
 * OpenCms virtual file system (controlled by flags in the entry).</p>
 *
 * <p>To check the permissions of a user on a distinct resource, the method <code>hasPermissions</code> in the driver manager
 * is called in each operation. This method acts as access guard and matches the required permissions for the operation
 * against the allowed and denied permissions defined for the user or groups of this user.</p>
 *
 * @since 6.0.0
 */
public class CmsAccessControlList {

    /**
     * Collected permissions of a principal on this resource .
     */
    private Map<CmsUUID, CmsPermissionSetCustom> m_permissions;

    /**
     * Constructor to create an empty access control list for a given resource.<p>
     *
     */
    public CmsAccessControlList() {

        m_permissions = new HashMap<CmsUUID, CmsPermissionSetCustom>();
    }

    /**
     * Adds an access control entry to the access control list.<p>
     *
     * @param entry the access control entry to add
     */
    public void add(CmsAccessControlEntry entry) {

        CmsPermissionSetCustom p = m_permissions.get(entry.getPrincipal());
        if (p == null) {
            p = new CmsPermissionSetCustom();
            m_permissions.put(entry.getPrincipal(), p);
        }
        p.addPermissions(entry.getPermissions());
    }

    /**
     * Returns a clone of this Objects instance.<p>
     *
     * @return a clone of this instance
     */
    @Override
    public Object clone() {

        CmsAccessControlList acl = new CmsAccessControlList();
        Iterator<CmsUUID> i = m_permissions.keySet().iterator();
        while (i.hasNext()) {
            CmsUUID id = i.next();
            acl.m_permissions.put(id, (CmsPermissionSetCustom)(m_permissions.get(id)).clone());
        }
        return acl;
    }

    /**
     * Returns the permission map of this access control list.<p>
     *
     * @return permission map
     */
    public Map<CmsUUID, CmsPermissionSetCustom> getPermissionMap() {

        return m_permissions;
    }

    /**
     * Calculates the permissions of the given user and his groups from the access control list.<p>
     *
     * @param user the user
     * @param groups the groups of this user
     * @param roles the roles of this user
     *
     * @return the summarized permission set of the user
     */
    public CmsPermissionSetCustom getPermissions(CmsUser user, List<CmsGroup> groups, List<CmsRole> roles) {

        CmsPermissionSetCustom sum = new CmsPermissionSetCustom();
        boolean hasPermissions = false;
        CmsPermissionSet p = m_permissions.get(user.getId());
        if (p != null) {
            sum.addPermissions(p);
            hasPermissions = true;
        }
        if (groups != null) {
            int size = groups.size();
            for (int i = 0; i < size; i++) {
                I_CmsPrincipal principal = groups.get(i);
                p = m_permissions.get(principal.getId());
                if (p != null) {
                    sum.addPermissions(p);
                    hasPermissions = true;
                }
            }
        }
        if (roles != null) {
            int size = roles.size();
            for (int i = 0; i < size; i++) {
                CmsRole role = roles.get(i);
                p = m_permissions.get(role.getId());
                if (p != null) {
                    sum.addPermissions(p);
                    hasPermissions = true;
                }
            }
        }
        if (!hasPermissions) {
            // if no applicable entry is found check the 'all others' entry
            p = m_permissions.get(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID);
            if (p != null) {
                sum.addPermissions(p);
            }
        }
        return sum;
    }

    /**
     * Returns the permission set of a principal as stored in the access control list.<p>
     *
     * @param principalId the id of the principal (group or user)
     *
     * @return the current permissions of this single principal
     */
    public CmsPermissionSetCustom getPermissions(CmsUUID principalId) {

        return m_permissions.get(principalId);
    }

    /**
     * Calculates the permissions of the given user and his groups from the access control list.<p>
     * The permissions are returned as permission string in the format {{+|-}{r|w|v|c|i}}*.
     *
     * @param user the user
     * @param groups the groups of this user
     * @param roles the roles of this user
     *
     * @return a string that displays the permissions
     */
    public String getPermissionString(CmsUser user, List<CmsGroup> groups, List<CmsRole> roles) {

        return getPermissions(user, groups, roles).getPermissionString();
    }

    /**
     * Returns the principals with specific permissions stored in this access control list.<p>
     *
     * @return enumeration of principals (each group or user)
     */
    public List<CmsUUID> getPrincipals() {

        List<CmsUUID> principals = new ArrayList<CmsUUID>(m_permissions.keySet());
        Collections.sort(principals, CmsAccessControlEntry.COMPARATOR_PRINCIPALS);
        return principals;
    }

    /**
     * Sets the allowed permissions of a given access control entry as allowed permissions in the access control list.<p>
     * The denied permissions are left unchanged.
     *
     * @param entry the access control entry
     */
    public void setAllowedPermissions(CmsAccessControlEntry entry) {

        CmsPermissionSetCustom p = m_permissions.get(entry.getPrincipal());
        if (p == null) {
            p = new CmsPermissionSetCustom();
            m_permissions.put(entry.getPrincipal(), p);
        }
        p.setPermissions(entry.getAllowedPermissions(), p.getDeniedPermissions());
    }

    /**
     * Sets the denied permissions of a given access control entry as denied permissions in the access control list.<p>
     * The allowed permissions are left unchanged.
     *
     * @param entry the access control entry
     */
    public void setDeniedPermissions(CmsAccessControlEntry entry) {

        CmsPermissionSetCustom p = m_permissions.get(entry.getPrincipal());
        if (p == null) {
            p = new CmsPermissionSetCustom();
            m_permissions.put(entry.getPrincipal(), p);
        }
        p.setPermissions(p.getAllowedPermissions(), entry.getDeniedPermissions());
    }
}