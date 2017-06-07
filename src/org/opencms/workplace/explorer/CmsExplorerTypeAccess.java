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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Explorer type access object, encapsulates access control entries and lists of a explorer type.<p>
 *
 * @since 6.0.0
 */
public class CmsExplorerTypeAccess {

    /** Principal key name for the default permission settings. */
    public static final String PRINCIPAL_DEFAULT = "DEFAULT";

    /** The listener used to flush cached access settings. */
    protected static CmsExplorerTypeAccessFlushListener flushListener;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerTypeAccess.class);

    static {
        flushListener = new CmsExplorerTypeAccessFlushListener();
        flushListener.install();
    }

    /** The map of configured access control entries. */
    private Map<String, String> m_accessControl;

    /** The acl based on the map of configured access control entries. */
    private CmsAccessControlList m_accessControlList;

    /** Cached permissions based on roles. */
    private Map<String, CmsPermissionSetCustom> m_permissionsCache;

    /**
     * Constructor, creates an empty, CmsExplorerTypeAccess object.<p>
     */
    public CmsExplorerTypeAccess() {

        m_accessControl = new HashMap<String, String>();
        flushListener.add(this);
    }

    /**
     * Adds a single access entry to the map of access entries of the explorer type setting.<p>
     *
     * This stores the configuration data in a map which is used in the initialize process
     * to create the access control list.<p>
     *
     * @param key the principal of the ace
     * @param value the permissions for the principal
     */
    public void addAccessEntry(String key, String value) {

        m_accessControl.put(key, value);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_ACCESS_ENTRY_2, key, value));
        }
    }

    /**
     * Creates the access control list from the temporary map.<p>
     *
     * @param resourceType the name of the resource type
     *
     * @throws CmsException if something goes wrong
     */
    public void createAccessControlList(String resourceType) throws CmsException {

        if (OpenCms.getRunLevel() < OpenCms.RUNLEVEL_2_INITIALIZING) {
            // we don't need this for simple test cases
            return;
        }
        if (m_permissionsCache == null) {
            m_permissionsCache = CmsMemoryMonitor.createLRUCacheMap(2048);
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + resourceType, m_permissionsCache);
        } else {
            m_permissionsCache.clear();
        }

        m_accessControlList = new CmsAccessControlList();
        Iterator<String> i = m_accessControl.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            if (!PRINCIPAL_DEFAULT.equals(key)) {
                String value = m_accessControl.get(key);
                // get the principal name from the principal String
                String principal = key.substring(key.indexOf('.') + 1, key.length());

                // create an OpenCms user context with "Guest" permissions
                CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());

                CmsUUID principalId = null;
                if (key.startsWith(I_CmsPrincipal.PRINCIPAL_GROUP)) {
                    // read the group
                    principal = OpenCms.getImportExportManager().translateGroup(principal);
                    try {
                        principalId = cms.readGroup(principal).getId();
                    } catch (CmsException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                } else if (key.startsWith(I_CmsPrincipal.PRINCIPAL_USER)) {
                    // read the user
                    principal = OpenCms.getImportExportManager().translateUser(principal);
                    try {
                        principalId = cms.readUser(principal).getId();
                    } catch (CmsException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                } else {
                    // read the role with role name
                    CmsRole role = CmsRole.valueOfRoleName(principal);
                    if (role == null) {
                        // try to read the role in the old fashion with group name
                        role = CmsRole.valueOfGroupName(principal);
                    }
                    principalId = role.getId();
                }
                if (principalId != null) {
                    // create a new entry for the principal
                    CmsAccessControlEntry entry = new CmsAccessControlEntry(null, principalId, value);
                    m_accessControlList.add(entry);
                }
            }
        }
    }

    /**
     * Returns the computed access Control List.<p>
     *
     * @return the computed access Control List
     */
    public CmsAccessControlList getAccessControlList() {

        return m_accessControlList;
    }

    /**
     * Returns the map of access entries of the explorer type setting.<p>
     *
     * @return the map of access entries of the explorer type setting
     */
    public Map<String, String> getAccessEntries() {

        return m_accessControl;
    }

    /**
     * Calculates the permissions for this explorer type settings
     * for the user in the given OpenCms user context.<p>
     *
     * @param cms the OpenCms user context to calculate the permissions for
     * @param resource the resource to check the permissions for
     *
     * @return the permissions for this explorer type settings for the user in the given OpenCms user context
     */
    public CmsPermissionSet getPermissions(CmsObject cms, CmsResource resource) {

        String cacheKey = getPermissionsCacheKey(cms, resource);
        CmsPermissionSetCustom permissions;
        if (cacheKey != null) {
            permissions = m_permissionsCache.get(cacheKey);
            if (permissions != null) {
                return permissions;
            }
        }
        CmsAccessControlList acl = (CmsAccessControlList)m_accessControlList.clone();

        CmsUser user = cms.getRequestContext().getCurrentUser();
        List<CmsGroup> groups = null;
        try {
            groups = cms.getGroupsOfUser(user.getName(), false);
        } catch (CmsException e) {
            // error reading the groups of the current user
            LOG.error(Messages.get().getBundle().key(Messages.LOG_READ_GROUPS_OF_USER_FAILED_1, user.getName()), e);
        }
        List<CmsRole> roles = null;
        try {
            roles = OpenCms.getRoleManager().getRolesForResource(cms, user, resource);
        } catch (CmsException e) {
            // error reading the roles of the current user
            LOG.error(Messages.get().getBundle().key(Messages.LOG_READ_GROUPS_OF_USER_FAILED_1, user.getName()), e);
        }
        String defaultPermissions = m_accessControl.get(PRINCIPAL_DEFAULT);
        // add the default permissions to the acl
        if ((defaultPermissions != null) && !user.isGuestUser()) {
            boolean found = false;
            if (acl.getPermissions(user.getId()) != null) {
                // acl already contains the user, no need for default
                found = true;
            }
            if (!found && (groups != null)) {
                // look up all groups to see if we need the default
                Iterator<CmsGroup> itGroups = groups.iterator();
                while (itGroups.hasNext()) {
                    CmsGroup group = itGroups.next();
                    if (acl.getPermissions(group.getId()) != null) {
                        // acl already contains the group, no need for default
                        found = true;
                        break;
                    }
                }
            }
            if (!found && (roles != null)) {
                // look up all roles to see if we need the default
                Iterator<CmsRole> itRoles = roles.iterator();
                while (itRoles.hasNext()) {
                    CmsRole role = itRoles.next();
                    if (acl.getPermissions(role.getId()) != null) {
                        // acl already contains the group, no need for default
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                // add default access control settings for current user
                CmsAccessControlEntry entry = new CmsAccessControlEntry(null, user.getId(), defaultPermissions);
                acl.add(entry);
            }
        }
        permissions = acl.getPermissions(user, groups, roles);

        if (cacheKey != null) {
            m_permissionsCache.put(cacheKey, permissions);
        }
        return permissions;
    }

    /**
     * Tests if there are any access information stored.<p>
     *
     * @return true or false
     */
    public boolean isEmpty() {

        return m_accessControl.isEmpty();
    }

    /**
     * Flushes the permission cache.<p>
     */
    protected void flushCache() {

        if (m_permissionsCache != null) {
            m_permissionsCache.clear();
        }
    }

    /**
     * Returns the cache key for the roles and groups of the current user and the given resource.<p>
     *
     * In this way, it does not matter if the resource and/or user permissions changes, so we never need to clean the cache.<p>
     *
     * And since the cache is a LRU map, old trash entries will be automatically removed.<p>
     *
     * @param cms the current cms context
     * @param resource the resource
     *
     * @return the cache key
     */
    private String getPermissionsCacheKey(CmsObject cms, CmsResource resource) {

        try {
            String userName = cms.getRequestContext().getCurrentUser().getName();
            StringBuffer key = new StringBuffer(256);
            key.append(resource.getRootPath()).append("_");
            Iterator<?> itGroups = cms.getGroupsOfUser(userName, true).iterator();
            while (itGroups.hasNext()) {
                CmsGroup group = (CmsGroup)itGroups.next();
                key.append(group.getName()).append("_");
            }
            Iterator<?> itRoles = OpenCms.getRoleManager().getRolesOfUser(
                cms,
                userName,
                "",
                true,
                true,
                false).iterator();
            while (itRoles.hasNext()) {
                CmsRole role = (CmsRole)itRoles.next();
                key.append(role.getGroupName()).append("_");
            }
            return key.toString();
        } catch (CmsException e) {
            return null;
        }
    }
}