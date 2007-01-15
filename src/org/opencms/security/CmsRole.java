/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsRole.java,v $
 * Date   : $Date: 2007/01/15 18:48:35 $
 * Version: $Revision: 1.11.4.4 $
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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A role is used in the OpenCms security system to check if a user has access to a certain system function.<p>
 * 
 * Roles are used to ensure access permissions to system function that are not file based. 
 * For example, roles are used to check permissions to functions like "the user can schedule a 
 * job in the <code>{@link org.opencms.scheduler.CmsScheduleManager}</code>" or "the user can export (or import) 
 * the OpenCms database".<p>
 * 
 * All roles are based on <code>{@link org.opencms.file.CmsGroup}</code>. This means to have access to a role,
 * the user has to be a member in a certain predefined system group. Each role has exactly one group that
 * contains all "direct" members of this role.<p>
 * 
 * All roles have (optional) parent roles. If a user not a member of the role group of a role, but he is
 * a member of at last one of the parent role groups, he/she also has full access to this role. This is called 
 * "indirect" membership to the role.<p>
 * 
 * Please note that "indirect" membership does grant the user the same full access to a role that "direct" 
 * membership does. For example, the <code>{@link #ROOT_ADMIN}</code> role is a parent group of all other roles. 
 * So all users that are members of <code>{@link #ROOT_ADMIN}</code> have access to the functions of all other roles.<p>
 * 
 * Please do not perform automated sorting of members on this compilation unit. That leads 
 * to NPE's<p>
 * 
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.11.4.4 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsRole {

    /** The "ACCOUNT_MANAGER" role. */
    public static final CmsRole ACCOUNT_MANAGER;

    /** The "ADMINISTRATOR" role, which is a parent to all organizational unit roles. */
    public static final CmsRole ADMINISTRATOR;

    /** The "EXPORT_DATABASE" role. */
    public static final CmsRole DATABASE_MANAGER;

    /** The "DEVELOPER" role. */
    public static final CmsRole DEVELOPER;

    /** The "DIRECT_EDIT_USER" role. */
    public static final CmsRole DIRECT_EDIT_USER;

    /** The "ORGUNIT_MANAGER" role. */
    public static final CmsRole ORGUNIT_MANAGER;

    /** The "PROJECT_MANAGER" role. */
    public static final CmsRole PROJECT_MANAGER;

    /** The "ROOT_ADMIN" role, which is a parent to all other roles. */
    public static final CmsRole ROOT_ADMIN;

    /** The "VFS_MANAGER" role. */
    public static final CmsRole VFS_MANAGER;

    /** The "WORKPLACE_MANAGER" role. */
    public static final CmsRole WORKPLACE_MANAGER;

    /** The "WORKPLACE_USER" role. */
    public static final CmsRole WORKPLACE_USER;

    /** The list of system roles. */
    private static final List SYSTEM_ROLES;

    /** The distinct group names of this role. */
    private String[] m_distictGroupNames;

    /** The name of the group this role is mapped to in the OpenCms database.*/
    private final String m_groupName;

    /** Indicates if this role is organizational unit dependent. */
    private final boolean m_ouDependent;

    /** The parent role of this role. */
    private final CmsRole m_parentRole;

    /** The name of this role. */
    private final String m_roleName;

    /** Indicates if this role is a system role or a user defined role. */
    private final boolean m_systemRole;

    /**
     * Creates a user defined role.<p>
     * 
     * @param roleName the name of this role
     * @param groupName the name of the group the members of this role are stored in
     * @param parentRole the parent role of this role
     * @param ouDependent if the role is organizational unit dependent
     */
    public CmsRole(String roleName, CmsRole parentRole, String groupName, boolean ouDependent) {

        m_roleName = roleName;
        m_groupName = groupName;
        m_parentRole = parentRole;
        m_ouDependent = ouDependent;
        m_systemRole = false;
        initialize();
    }

    /**
     * Creates a system role.<p>
     * 
     * @param roleName the name of this role
     * @param parentRole the parent role of this role
     * @param groupName the related group name
     */
    private CmsRole(String roleName, CmsRole parentRole, String groupName) {

        m_roleName = roleName;
        m_groupName = groupName;
        m_parentRole = parentRole;
        m_systemRole = true;
        m_ouDependent = !groupName.startsWith("/");
        initialize();
    }

    /**
     * Initializes the system roles with the configured OpenCms system group names.<p>
     */
    static {

        // TODO: replace default group names
        ROOT_ADMIN = new CmsRole("ROOT_ADMIN", null, "/Administrators");
        WORKPLACE_MANAGER = new CmsRole("WORKPLACE_MANAGER", CmsRole.ROOT_ADMIN, "/RoleWorkplaceManager");
        DATABASE_MANAGER = new CmsRole("DATABASE_MANAGER", CmsRole.ROOT_ADMIN, "/RoleDatabaseManager");
        ORGUNIT_MANAGER = new CmsRole("ORGUNIT_MANAGER", CmsRole.ROOT_ADMIN, "/RoleOrgUnitManager");

        ADMINISTRATOR = new CmsRole("ADMINISTRATOR", CmsRole.ROOT_ADMIN, "RoleAdministrators");
        PROJECT_MANAGER = new CmsRole("PROJECT_MANAGER", CmsRole.ADMINISTRATOR, "Projectmanagers");
        ACCOUNT_MANAGER = new CmsRole("ACCOUNT_MANAGER", CmsRole.ADMINISTRATOR, "RoleAccountManagers");
        VFS_MANAGER = new CmsRole("VFS_MANAGER", CmsRole.ADMINISTRATOR, "RoleVfsManagers");
        DEVELOPER = new CmsRole("DEVELOPER", CmsRole.VFS_MANAGER, "RoleDevelopers");
        WORKPLACE_USER = new CmsRole("WORKPLACE_USER", CmsRole.ADMINISTRATOR, "Users");
        DIRECT_EDIT_USER = new CmsRole("DIRECT_EDIT_USER", CmsRole.WORKPLACE_USER, "RoleDirectEditUsers");

        // create a lookup list for the system roles
        SYSTEM_ROLES = Collections.unmodifiableList(Arrays.asList(new CmsRole[] {
            ROOT_ADMIN,
            WORKPLACE_MANAGER,
            DATABASE_MANAGER,
            ADMINISTRATOR,
            PROJECT_MANAGER,
            ACCOUNT_MANAGER,
            VFS_MANAGER,
            DEVELOPER,
            WORKPLACE_USER,
            DIRECT_EDIT_USER}));

        // now initilaize all system roles
        for (int i = 0; i < SYSTEM_ROLES.size(); i++) {
            ((CmsRole)SYSTEM_ROLES.get(i)).initialize();
        }
    }

    /**
     * Returns the list of system defined roles (instances of <code>{@link CmsRole}</code>).<p> 
     * 
     * @return the list of system defined roles
     */
    public static List getSystemRoles() {

        return SYSTEM_ROLES;
    }

    /**
     * Returns the system role with the given name or <code>null</code>
     * if not found.<p>
     * 
     * @param roleName the name of the role
     * 
     * @return the role with the given name
     */
    public static CmsRole valueOf(String roleName) {

        Iterator it = SYSTEM_ROLES.iterator();
        while (it.hasNext()) {
            CmsRole role = (CmsRole)it.next();
            if (role.getRoleName().equals(roleName)) {
                return role;
            }
        }
        return null;
    }

    /**
     * Returns a set of all roles group names.<p>
     * 
     * @param role the role to begin with
     * 
     * @return a set of all roles group names
     */
    private static Set getAllGroupNames(CmsRole role) {

        Set distinctGroups = new HashSet();
        // add role group name
        distinctGroups.add(role.getGroupName());
        if (role.getParentRole() != null) {
            // add parent roles group names
            distinctGroups.addAll(getAllGroupNames(role.getParentRole()));
        }
        return distinctGroups;
    }

    /**
     * Returns a role violation exception configured with a localized, role specific message 
     * for this role.<p>
     * 
     * @param requestContext the current users OpenCms request context
     * @param orgUnitFqn the organizational unit used for the role check, it may be <code>null</code>
     * 
     * @return a role violation exception configured with a localized, role specific message 
     *      for this role
     */
    public CmsRoleViolationException createRoleViolationException(
        CmsRequestContext requestContext,
        String orgUnitFqn) {

        if (orgUnitFqn != null) {
            return new CmsRoleViolationException(Messages.get().container(
                Messages.ERR_USER_NOT_IN_ROLE_FOR_ORGUNIT_3,
                requestContext.currentUser().getName(),
                getName(requestContext.getLocale()),
                orgUnitFqn));
        } else {
            return new CmsRoleViolationException(Messages.get().container(
                Messages.ERR_USER_NOT_IN_ROLE_2,
                requestContext.currentUser().getName(),
                getName(requestContext.getLocale())));
        }
    }

    /**
     * Returns a role violation exception configured with a localized, role specific message 
     * for this role.<p>
     * 
     * @param requestContext the current users OpenCms request context
     * @param resource the resource used for the role check, it may be <code>null</code>
     * 
     * @return a role violation exception configured with a localized, role specific message 
     *      for this role
     */
    public CmsRoleViolationException createRoleViolationException(CmsRequestContext requestContext, CmsResource resource) {

        return new CmsRoleViolationException(Messages.get().container(
            Messages.ERR_USER_NOT_IN_ROLE_FOR_RESOURCE_3,
            requestContext.currentUser().getName(),
            getName(requestContext.getLocale()),
            requestContext.removeSiteRoot(resource.getRootPath())));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsRole) {
            return m_roleName.equals(((CmsRole)obj).m_roleName);
        }
        return false;
    }

    /**
     * Returns the name of the group this role is mapped to in the OpenCms database.<p>
     * 
     * @return the name of the group this role is mapped to in the OpenCms database
     */
    public String getGroupName() {

        return m_groupName;
    }

    /**
     * Returns a localized role name.<p>
     * 
     * @param locale the locale
     * 
     * @return the localized role name
     */
    public String getName(Locale locale) {

        if (m_systemRole) {
            // localize role names for system roles
            return Messages.get().getBundle(locale).key("GUI_ROLENAME_" + m_roleName + "_0");
        } else {
            return getRoleName();
        }
    }

    /**
     * Returns the parent role of this role.<p>
     *
     * @return the parent role of this role
     */
    public CmsRole getParentRole() {

        return m_parentRole;
    }

    /**
     * Returns the name of the role.<p>
     * 
     * @return the name of the role
     */
    public String getRoleName() {

        return m_roleName;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_roleName.hashCode();
    }

    /**
     * Returns <code>true</code> if at least one of the given <code>{@link CmsGroup}</code> instances is 
     * equal to a group of this role.<p>
     * 
     * This checks the given list against the role group of this role as well as against the role group 
     * of all parent roles.<p>
     * 
     * This method can only be used if the scope of this role is global, if not <code>false</code> is 
     * returned.<p>
     * 
     * @param groups a List of <code>{@link CmsGroup}</code> instances to match the role groups against
     * @param orgUnitFqn the organizational unit to check the role for
     * 
     * @return <code>true</code> if at least one of the given group names is equal to a group name
     *      of this role
     */
    public boolean hasRole(List groups, String orgUnitFqn) {

        String[] groupNames = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            groupNames[i] = ((CmsGroup)groups.get(i)).getName();
        }
        return hasRole(groupNames, orgUnitFqn);
    }

    /**
     * Returns <code>true</code> if at last one of the given group names is equal to a group name
     * of this role.<p>
     * 
     * This checks the given list against the role group of this role as well as against the role group 
     * of all parent roles.<p>
     * 
     * @param groupNames the group names to match the role groups against
     * @param orgUnitFqn the organizational unit to check the role for
     * 
     * @return <code>true</code> if at last one of the given group names is equal to a group name
     *      of this role
     */
    public boolean hasRole(String[] groupNames, String orgUnitFqn) {

        List orgUnits = new ArrayList();
        orgUnits.add("/"); // the root ou
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(orgUnitFqn) && !orgUnitFqn.equals("/")) {
            // add higher level ou's
            String parentOuFqn = orgUnitFqn;
            while (CmsStringUtil.isNotEmptyOrWhitespaceOnly(parentOuFqn) && !parentOuFqn.equals("/")) {
                orgUnits.add(parentOuFqn + "/");
                parentOuFqn = CmsOrganizationalUnit.getParentFqn(parentOuFqn);
            }
        }
        for (int i = 0; i < m_distictGroupNames.length; i++) {
            for (int j = 0; j < groupNames.length; j++) {
                if (groupNames[j].startsWith("/")) {
                    if (groupNames[j].equals(m_distictGroupNames[i])) {
                        return true;
                    }
                } else {
                    if (orgUnitFqn == null) {
                        if (groupNames[j].endsWith("/" + m_distictGroupNames[i])) {
                            return true;
                        }
                    } else {
                        Iterator it = orgUnits.iterator();
                        while (it.hasNext()) {
                            String ouFqn = (String)it.next();
                            if (groupNames[j].equals(ouFqn + m_distictGroupNames[i])) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if this role is organizational unit independent.<p>
     * 
     * @return <code>true</code> if this role is organizational unit independent
     */
    public boolean isOrganizationalUnitIndependent() {

        return !m_ouDependent;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();

        result.append("[");
        result.append(this.getClass().getName());
        result.append(", role: ");
        result.append(getRoleName());
        result.append(", group: ");
        result.append(getGroupName());
        result.append("]");

        return result.toString();
    }

    /**
     * Initializes this role, creating an optimized data structure for 
     * the lookup of the role group names.<p>
     */
    private void initialize() {

        // calculate the distinct groups of this role
        Set distinctGroups = new HashSet();
        distinctGroups.addAll(getAllGroupNames(this));
        m_distictGroupNames = new String[distinctGroups.size()];
        distinctGroups.toArray(m_distictGroupNames);
    }
}
