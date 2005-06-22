/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsRole.java,v $
 * Date   : $Date: 2005/06/22 14:58:54 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
 * membership does. For example, the <code>{@link #ADMINISTRATOR}</code> role is a parent group of all other roles. 
 * So all users that are members of <code>{@link #ADMINISTRATOR}</code> have access to the functions of all other roles.<p>
 * 
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsRole {

    /** The "ADMINISTRATOR" role, which is a parent to all other roles.<p> */
    public static final CmsRole ADMINISTRATOR = new CmsRole("ADMINISTRATOR", new CmsRole[0]);

    /** The "DEVELOPER" role. */
    public static final CmsRole DEVELOPER = new CmsRole("DEVELOPER", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The "EXPORT_DATABASE" role. */
    public static final CmsRole EXPORT_DATABASE = new CmsRole("EXPORT_DATABASE", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The "HISTORY_MANAGER" role. */
    public static final CmsRole HISTORY_MANAGER = new CmsRole("HISTORY_MANAGER", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The "IMPORT_DATABASE" role. */
    public static final CmsRole IMPORT_DATABASE = new CmsRole("IMPORT_DATABASE", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The "MODULE_MANAGER" role. */
    public static final CmsRole MODULE_MANAGER = new CmsRole("MODULE_MANAGER", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The "PROJECT_MANAGER" role. */
    public static final CmsRole PROJECT_MANAGER = new CmsRole("PROJECT_MANAGER", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The "PROPERTY_MANAGER" role. */
    public static final CmsRole PROPERTY_MANAGER = new CmsRole(
        "PROPERTY_MANAGER",
        new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** 
     * The "RESOURCE_TYPE_MANAGER" role.<p>
     *
     * Additional parent: <code>{@link CmsRole#MODULE_MANAGER}</code>.<p>
     */
    public static final CmsRole RESOURCE_TYPE_MANAGER = new CmsRole("RESOURCE_TYPE_MANAGER", new CmsRole[] {
        CmsRole.ADMINISTRATOR,
        CmsRole.MODULE_MANAGER});

    /** The "ROOT_FOLDER_ACCESS" role. */
    public static final CmsRole ROOT_FOLDER_ACCESS = new CmsRole(
        "ROOT_FOLDER_ACCESS",
        new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The "SCHEDULER_MANAGER" role. */
    public static final CmsRole SCHEDULER_MANAGER = new CmsRole(
        "SCHEDULER_MANAGER",
        new CmsRole[] {CmsRole.ADMINISTRATOR});
    /** 
     * The "SYSTEM_USER" role.<p>
     *
     * Additional parents: <code>{@link CmsRole#WORKPLACE_USER}</code>, <code>{@link CmsRole#PROJECT_MANAGER},</code>
     * <code>{@link CmsRole#DEVELOPER}</code>.<p>
     */
    public static final CmsRole SYSTEM_USER = new CmsRole("SYSTEM_USER", new CmsRole[] {
    // important: this role must be defined _after_ all other roles it referes to
        CmsRole.ADMINISTRATOR, CmsRole.WORKPLACE_USER, CmsRole.PROJECT_MANAGER, CmsRole.DEVELOPER});

    /** The "USER_MANAGER" role. */
    public static final CmsRole USER_MANAGER = new CmsRole("USER_MANAGER", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The "VFS_MANAGER" role. */
    public static final CmsRole VFS_MANAGER = new CmsRole("VFS_MANAGER", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** 
     * The "WORKPLACE_MANAGER" role.<p>
     *
     * Additional parents: <code>{@link CmsRole#MODULE_MANAGER}</code>, <code>{@link CmsRole#DEVELOPER}</code>.<p>
     */
    public static final CmsRole WORKPLACE_MANAGER = new CmsRole("WORKPLACE_MANAGER", new CmsRole[] {
        CmsRole.ADMINISTRATOR,
        CmsRole.MODULE_MANAGER,
        CmsRole.DEVELOPER});

    /** The "WORKPLACE_USER" role. */
    public static final CmsRole WORKPLACE_USER = new CmsRole("WORKPLACE_USER", new CmsRole[] {CmsRole.ADMINISTRATOR});

    /** The list of system roles. */
    private static List m_systemRoles;

    /** The distinct group names of this role. */
    private Object[] m_distictGroupNames;

    /** The name of the group this role is mapped to in the OpenCms database.*/
    private String m_groupName;

    /** The parent roles of this role. */
    private List m_parentRoles;

    /** The name of this role. */
    private String m_roleName;

    /** Indicates if this role is a system role or a user defined role. */
    private boolean m_systemRole;

    /**
     * Creates a user defined role.<p>
     * 
     * @param roleName the name of this role
     * @param groupName the name of the group the members of this role are stored in
     * @param parentRoles the parent roles of this role
     */
    public CmsRole(String roleName, String groupName, CmsRole[] parentRoles) {

        m_roleName = roleName;
        m_groupName = groupName;
        m_parentRoles = Collections.unmodifiableList(Arrays.asList(parentRoles));
        m_systemRole = false;
        initialize();
    }

    /**
     * Creates a system role.<p>
     * 
     * @param roleName the name of this role
     * @param parentRoles the parent roles of this role
     */
    private CmsRole(String roleName, CmsRole[] parentRoles) {

        m_roleName = roleName;
        m_parentRoles = Collections.unmodifiableList(Arrays.asList(parentRoles));
        m_systemRole = true;
    }

    /**
     * Returns the list of system defined roles (instances of <code>{@link CmsRole}</code>).<p> 
     * 
     * @return the list of system defined roles
     */
    public static List getSystemRoles() {

        return m_systemRoles;
    }

    /**
     * Initializes the system roles with the configured OpenCms system group names.<p>
     * 
     * This is done automatically during the system startup phase, any manual calls 
     * later will result in an Exception.<p>
     * 
     * @param defaultUsers the OpenCms default users
     * 
     * @throws CmsSecurityException if called outside the system startup phase
     */
    public static void initialize(CmsDefaultUsers defaultUsers) throws CmsSecurityException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
            // this method can be called only during the system startup phase
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_STARTUP_FINISHED_0));
        }

        // set the configured group names for the system roles
        ADMINISTRATOR.m_groupName = defaultUsers.getGroupAdministrators();
        PROJECT_MANAGER.m_groupName = defaultUsers.getGroupProjectmanagers();
        WORKPLACE_USER.m_groupName = defaultUsers.getGroupUsers();

        // TODO: Don't base all roles only on the "Administrator" group
        MODULE_MANAGER.m_groupName = defaultUsers.getGroupAdministrators();
        USER_MANAGER.m_groupName = defaultUsers.getGroupAdministrators();
        EXPORT_DATABASE.m_groupName = defaultUsers.getGroupAdministrators();
        IMPORT_DATABASE.m_groupName = defaultUsers.getGroupAdministrators();
        DEVELOPER.m_groupName = defaultUsers.getGroupAdministrators();
        SCHEDULER_MANAGER.m_groupName = defaultUsers.getGroupAdministrators();
        VFS_MANAGER.m_groupName = defaultUsers.getGroupAdministrators();
        RESOURCE_TYPE_MANAGER.m_groupName = defaultUsers.getGroupAdministrators();
        HISTORY_MANAGER.m_groupName = defaultUsers.getGroupAdministrators();
        PROPERTY_MANAGER.m_groupName = defaultUsers.getGroupAdministrators();
        ROOT_FOLDER_ACCESS.m_groupName = defaultUsers.getGroupAdministrators();
        WORKPLACE_MANAGER.m_groupName = defaultUsers.getGroupAdministrators();
        SYSTEM_USER.m_groupName = defaultUsers.getGroupUsers();

        // create a lookup list for the system roles
        m_systemRoles = Collections.unmodifiableList(Arrays.asList(new CmsRole[] {
            ADMINISTRATOR,
            PROJECT_MANAGER,
            WORKPLACE_USER,
            MODULE_MANAGER,
            USER_MANAGER,
            EXPORT_DATABASE,
            IMPORT_DATABASE,
            DEVELOPER,
            SCHEDULER_MANAGER,
            VFS_MANAGER,
            RESOURCE_TYPE_MANAGER,
            HISTORY_MANAGER,
            PROPERTY_MANAGER,
            ROOT_FOLDER_ACCESS,
            WORKPLACE_MANAGER,
            SYSTEM_USER}));

        // now initilaize all system roles
        for (int i = 0; i < m_systemRoles.size(); i++) {
            ((CmsRole)m_systemRoles.get(i)).initialize();
        }
    }

    /**
     * Returns <code>true</code> if the role group of this role (not the groups from the parent roles) 
     * matches a name of one of the given groups.<p>
     * 
     * This check is required only to find out if a user is a direct member of the role group of 
     * this role. It should never be used for permission checks. For all permission checks, use
     * <code>{@link #hasRole(List)}</code>.<p>
     * 
     * @param groups a List of <code>{@link CmsGroup}</code> instances to match this role group against
     * @return <code>true</code> if the role group of this role (not the groups from the parent roles) 
     *      matches a name of one of the given groups
     */
    public boolean checkDirectAccess(List groups) {

        for (int i = 0; i < groups.size(); i++) {
            if (m_groupName.equals(((CmsGroup)groups.get(i)).getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a role violation exception configured with a localized, role specific message 
     * for this role.<p>
     * 
     * @param context the current users OpenCms request context
     * 
     * @return a role violation exception configured with a localized, role specific message 
     *      for this role
     */
    public CmsRoleViolationException createRoleViolationException(CmsRequestContext context) {

        String roleName;
        if (m_systemRole) {
            // localize role names for system roles
            roleName = Messages.get().key(context.getLocale(), "GUI_ROLENAME_" + m_roleName + "_0", null);
        } else {
            roleName = getRoleName();
        }
        return new CmsRoleViolationException(Messages.get().container(
            Messages.ERR_NOT_IN_ROLE_2,
            context.currentUser().getName(),
            roleName));
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
     * Returns the (unmodifialble) List of parent roles of this role (instances of <code>{@link CmsRole}</code>.<p>
     *
     * @return the (unmodifialble) List of parent roles of this role
     */
    public List getParentRoles() {

        return m_parentRoles;
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
     * Returns <code>true</code> if at last one of the given <code>{@link CmsGroup}</code> instances is 
     * equal to a group of this role.<p>
     * 
     * This checks the given list against the role group of this role as well as against the role group 
     * of all parent roles.<p>
     * 
     * @param groups a List of <code>{@link CmsGroup}</code> instances to match the role groups against
     * @return <code>true</code> if at last one of the given group names is equal to a group name
     *      of this role
     */
    public boolean hasRole(List groups) {

        String[] groupNames = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            groupNames[i] = ((CmsGroup)groups.get(i)).getName();
        }
        return hasRole(groupNames);
    }

    /**
     * Returns <code>true</code> if at last one of the given group names is equal to a group name
     * of this role.<p>
     * 
     * This checks the given list against the role group of this role as well as against the role group 
     * of all parent roles.<p>
     * 
     * @param groupNames the group names to match the role groups against
     * @return <code>true</code> if at last one of the given group names is equal to a group name
     *      of this role
     */
    public boolean hasRole(String[] groupNames) {

        for (int i = 0; i < m_distictGroupNames.length; i++) {
            for (int j = 0; j < groupNames.length; j++) {
                if (m_distictGroupNames[i].equals(groupNames[j])) {
                    return true;
                }
            }
        }
        return false;
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
        ArrayList distinctGroups = new ArrayList();
        distinctGroups.add(getGroupName());
        for (int i = 0; i < m_parentRoles.size(); i++) {
            String name = ((CmsRole)m_parentRoles.get(i)).getGroupName();
            if (!distinctGroups.contains(name)) {
                distinctGroups.add(name);
            }
        }
        m_distictGroupNames = distinctGroups.toArray();
    }
}