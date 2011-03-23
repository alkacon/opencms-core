/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsRoleManager.java,v $
 * Date   : $Date: 2011/03/23 14:51:14 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This manager provide access to the role related operations.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.10 $
 * 
 * @since 6.5.6
 */
public class CmsRoleManager {

    /** The security manager. */
    private final CmsSecurityManager m_securityManager;

    /**
     * Default constructor.<p>
     * 
     * @param securityManager the security manager
     */
    public CmsRoleManager(CmsSecurityManager securityManager) {

        m_securityManager = securityManager;
    }

    /**
     * Adds a user to the given role.<p>
     * 
     * @param cms the opencms context
     * @param role the role
     * @param username the name of the user that is to be added to the role
     * 
     * @throws CmsException if something goes wrong
     */
    public void addUserToRole(CmsObject cms, CmsRole role, String username) throws CmsException {

        m_securityManager.addUserToGroup(cms.getRequestContext(), username, role.getGroupName(), true);
    }

    /**
     * Checks if the user of this OpenCms context is a member of the given role
     * for the given organizational unit.<p>
     * 
     * The user must have the given role in at least one parent organizational unit.<p>
     * 
     * @param cms the opencms context
     * @param role the role to check
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     */
    public void checkRole(CmsObject cms, CmsRole role) throws CmsRoleViolationException {

        m_securityManager.checkRole(cms.getRequestContext(), role);
    }

    /**
     * Checks if the user of this OpenCms context is a member of the given role
     * for the given resource.<p>
     * 
     * The user must have the given role in at least one organizational unit to which this resource belongs.<p>
     * 
     * @param cms the opencms context
     * @param role the role to check
     * @param resourceName the name of the resource to check the role for
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     * @throws CmsException if something goes wrong, while reading the resource
     */
    public void checkRoleForResource(CmsObject cms, CmsRole role, String resourceName)
    throws CmsException, CmsRoleViolationException {

        CmsResource resource = cms.readResource(resourceName);
        m_securityManager.checkRoleForResource(cms.getRequestContext(), role, resource);
    }

    /**
     * Returns all groups of organizational units for which the current user 
     * has the {@link CmsRole#ACCOUNT_MANAGER} role.<p>
     * 
     * @param cms the current cms context
     * @param ouFqn the fully qualified name of the organizational unit
     * @param includeSubOus if sub organizational units should be included in the search 
     *  
     * @return a list of {@link org.opencms.file.CmsGroup} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getManageableGroups(CmsObject cms, String ouFqn, boolean includeSubOus) throws CmsException {

        List groups = new ArrayList();
        Iterator it = getOrgUnitsForRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ouFqn), includeSubOus).iterator();
        while (it.hasNext()) {
            CmsOrganizationalUnit orgUnit = (CmsOrganizationalUnit)it.next();
            groups.addAll(OpenCms.getOrgUnitManager().getGroups(cms, orgUnit.getName(), false));
        }
        return groups;
    }

    /**
     * Returns all resources of organizational units for which the current user has 
     * the given role role.<p>
     * 
     * @param cms the current cms context
     * @param role the role to check
     *  
     * @return a list of {@link org.opencms.file.CmsResource} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getManageableResources(CmsObject cms, CmsRole role) throws CmsException {

        return m_securityManager.getManageableResources(cms.getRequestContext(), role);
    }

    /**
     * Returns all users of organizational units for which the current user has 
     * the {@link CmsRole#ACCOUNT_MANAGER} role.<p>
     * 
     * @param cms the current cms context
     * @param ouFqn the fully qualified name of the organizational unit
     * @param includeSubOus if sub organizational units should be included in the search 
     *  
     * @return a list of {@link org.opencms.file.CmsUser} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getManageableUsers(CmsObject cms, String ouFqn, boolean includeSubOus) throws CmsException {
        
        return getManageableUsers(cms, ouFqn, includeSubOus, false);
    }

    /**
     * Returns all users of organizational units for which the current user has 
     * the {@link CmsRole#ACCOUNT_MANAGER} role.<p>
     * 
     * @param cms the current cms context
     * @param ouFqn the fully qualified name of the organizational unit
     * @param includeSubOus if sub organizational units should be included in the search
     * @param includeWebusers if webuser organizational units should be included in the search
     *  
     * @return a list of {@link org.opencms.file.CmsUser} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getManageableUsers(CmsObject cms, String ouFqn, boolean includeSubOus, boolean includeWebusers) throws CmsException {

        List users = new ArrayList();
        Iterator it = getOrgUnitsForRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ouFqn), includeSubOus).iterator();
        while (it.hasNext()) {
            CmsOrganizationalUnit orgUnit = (CmsOrganizationalUnit)it.next();
            if (!includeWebusers && orgUnit.hasFlagWebuser()) {
                // webuser are never manageable
                continue;
            }
            users.addAll(OpenCms.getOrgUnitManager().getUsers(cms, orgUnit.getName(), false));
        }
        return users;
    }

    /**
     * Returns all the organizational units for which the current user has the given role.<p>
     * 
     * @param cms the current cms context
     * @param role the role to check
     * @param includeSubOus if sub organizational units should be included in the search 
     *  
     * @return a list of {@link org.opencms.security.CmsOrganizationalUnit} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getOrgUnitsForRole(CmsObject cms, CmsRole role, boolean includeSubOus) throws CmsException {

        return m_securityManager.getOrgUnitsForRole(cms.getRequestContext(), role, includeSubOus);
    }

    /**
     * Returns all roles, in the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit of the role
     * @param includeSubOus include roles of child organizational units
     * 
     * @return a list of all <code>{@link org.opencms.file.CmsGroup}</code> objects
     *
     * @throws CmsException if operation was not successful
     */
    public List getRoles(CmsObject cms, String ouFqn, boolean includeSubOus) throws CmsException {

        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ouFqn);
        List groups = m_securityManager.getGroups(cms.getRequestContext(), ou, includeSubOus, true);
        List roles = new ArrayList(groups.size());
        Iterator itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            roles.add(CmsRole.valueOf(group));
        }
        return roles;
    }

    /**
     * Returns all roles the given user has over the given resource.<p>
     * 
     * @param cms the current cms context
     * @param userFqn the user name to check
     * @param resourceName the resource name
     * 
     * @return a list of {@link CmsRole} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getRolesForResource(CmsObject cms, String userFqn, String resourceName) throws CmsException {

        CmsUser user = cms.readUser(userFqn);
        CmsResource resource = cms.readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.getRolesForResource(cms.getRequestContext(), user, resource);
    }

    /**
     * Returns all roles the given user belongs to, in the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param username the name of the user to get all roles for
     * @param ouFqn the fully qualified name of the organizational unit to restrict the search to
     * @param includeChildOus include roles of child organizational units
     * @param directRolesOnly if set only the direct assigned roles will be returned, if not also indirect roles
     * @param recursive if this is set, also roles of higher organizational unit are considered
     * 
     * @return a list of <code>{@link org.opencms.security.CmsRole}</code> objects
     *
     * @throws CmsException if operation was not successful
     */
    public List getRolesOfUser(
        CmsObject cms,
        String username,
        String ouFqn,
        boolean includeChildOus,
        boolean directRolesOnly,
        boolean recursive) throws CmsException {

        List groups;
        ouFqn = CmsOrganizationalUnit.removeLeadingSeparator(ouFqn);
        if (!recursive) {
            groups = m_securityManager.getGroupsOfUser(
                cms.getRequestContext(),
                username,
                ouFqn,
                includeChildOus,
                true,
                directRolesOnly,
                cms.getRequestContext().getRemoteAddress());
        } else {
            groups = new ArrayList();
            Iterator itAllGroups = m_securityManager.getGroupsOfUser(
                cms.getRequestContext(),
                username,
                "",
                true,
                true,
                directRolesOnly,
                cms.getRequestContext().getRemoteAddress()).iterator();
            while (itAllGroups.hasNext()) {
                CmsGroup role = (CmsGroup)itAllGroups.next();
                if (!includeChildOus && role.getOuFqn().equals(ouFqn)) {
                    groups.add(role);
                }
                if (includeChildOus && role.getOuFqn().startsWith(ouFqn)) {
                    groups.add(role);
                }
            }
        }
        List roles = new ArrayList(groups.size());
        Iterator itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            roles.add(CmsRole.valueOf(group));
        }
        return roles;
    }

    /**
     * Returns all direct users of a given role, in the given organizational unit.<p>
     *
     * Users that are "indirectly" in the role are not returned in the result.<p>
     *
     * @param cms the opencms context
     * @param role the role to get all users for
     * @param includeOtherOuUsers include users of other organizational units
     * @param directUsersOnly if set only the direct assigned users will be returned, 
     *                          if not also indirect users, ie. members of child groups
     * 
     * @return all <code>{@link org.opencms.file.CmsUser}</code> objects in the group
     *
     * @throws CmsException if operation was not successful
     */
    public List getUsersOfRole(CmsObject cms, CmsRole role, boolean includeOtherOuUsers, boolean directUsersOnly)
    throws CmsException {

        return m_securityManager.getUsersOfGroup(
            cms.getRequestContext(),
            role.getGroupName(),
            includeOtherOuUsers,
            directUsersOnly,
            true);
    }

    /**
     * Checks if the given context user has the given role in the given organizational unit.<p>
     *  
     * @param cms the opencms context
     * @param role the role to check
     * 
     * @return <code>true</code> if the given context user has the given role in the given organizational unit
     */
    public boolean hasRole(CmsObject cms, CmsRole role) {

        return m_securityManager.hasRole(cms.getRequestContext(), cms.getRequestContext().currentUser(), role);
    }

    /**
     * Checks if the given user has the given role in the given organizational unit.<p> 
     *  
     * @param cms the opencms context
     * @param userName the name of the user to check the role for
     * @param role the role to check
     * 
     * @return <code>true</code> if the given user has the given role in the given organizational unit
     */
    public boolean hasRole(CmsObject cms, String userName, CmsRole role) {

        CmsUser user;
        try {
            user = cms.readUser(userName);
        } catch (CmsException e) {
            // ignore
            return false;
        }
        return m_securityManager.hasRole(cms.getRequestContext(), user, role);
    }

    /**
     * Checks if the given context user has the given role for the given resource.<p>
     *  
     * @param cms the opencms context
     * @param role the role to check
     * @param resourceName the name of the resource to check
     * 
     * @return <code>true</code> if the given context user has the given role for the given resource
     */
    public boolean hasRoleForResource(CmsObject cms, CmsRole role, String resourceName) {

        CmsResource resource;
        try {
            resource = cms.readResource(resourceName, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            // ignore
            return false;
        }
        return m_securityManager.hasRoleForResource(
            cms.getRequestContext(),
            cms.getRequestContext().currentUser(),
            role,
            resource);
    }

    /**
     * Checks if the given context user has the given role for the given resource.<p>
     *  
     * @param cms the opencms context
     * @param userName the name of the user to check the role for
     * @param role the role to check
     * @param resourceName the name of the resource to check
     * 
     * @return <code>true</code> if the given context user has the given role for the given resource
     */
    public boolean hasRoleForResource(CmsObject cms, String userName, CmsRole role, String resourceName) {

        CmsResource resource;
        try {
            resource = cms.readResource(resourceName);
        } catch (CmsException e) {
            // ignore
            return false;
        }
        CmsUser user;
        try {
            user = cms.readUser(userName);
        } catch (CmsException e) {
            // ignore
            return false;
        }
        return m_securityManager.hasRoleForResource(cms.getRequestContext(), user, role, resource);
    }

    /**
     * Removes a user from a role, in the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param role the role to remove the user from
     * @param username the name of the user that is to be removed from the group
     * 
     * @throws CmsException if operation was not successful
     */
    public void removeUserFromRole(CmsObject cms, CmsRole role, String username) throws CmsException {

        m_securityManager.removeUserFromGroup(cms.getRequestContext(), username, role.getGroupName(), true);
    }

}