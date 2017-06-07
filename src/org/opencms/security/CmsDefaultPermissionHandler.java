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

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.CmsCacheSettings;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsSecurityManager;
import org.opencms.db.I_CmsCacheKey;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Iterator;

import org.apache.commons.logging.Log;

/**
 * Generic base driver interface.<p>
 *
 * @since 7.0.2
 */
public class CmsDefaultPermissionHandler implements I_CmsPermissionHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultPermissionHandler.class);

    /** Driver Manager instance. */
    protected CmsDriverManager m_driverManager;

    /** Security Manager instance. */
    protected CmsSecurityManager m_securityManager;

    /** The class used for cache key generation. */
    private I_CmsCacheKey m_keyGenerator;

    /**
     * @see org.opencms.security.I_CmsPermissionHandler#hasPermissions(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, org.opencms.security.CmsPermissionSet, boolean, org.opencms.file.CmsResourceFilter)
     */
    public CmsPermissionCheckResult hasPermissions(
        CmsDbContext dbc,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {

        // check if the resource is valid according to the current filter
        // if not, throw a CmsResourceNotFoundException
        if (!filter.isValid(dbc.getRequestContext(), resource)) {
            return I_CmsPermissionHandler.PERM_FILTERED;
        }

        // checking the filter is less cost intensive then checking the cache,
        // this is why basic filter results are not cached
        String cacheKey = m_keyGenerator.getCacheKeyForUserPermissions(
            filter.requireVisible() && checkLock
            ? "11"
            : (!filter.requireVisible() && checkLock ? "01" : (filter.requireVisible() && !checkLock ? "10" : "00")),
            dbc,
            resource,
            requiredPermissions);
        CmsPermissionCheckResult cacheResult = OpenCms.getMemoryMonitor().getCachedPermission(cacheKey);
        if (cacheResult != null) {
            return cacheResult;
        }

        int denied = 0;

        // if this is the online project, write is rejected
        if (dbc.currentProject().isOnlineProject()) {
            denied |= CmsPermissionSet.PERMISSION_WRITE;
        }

        // check if the current user is admin
        boolean canIgnorePermissions = m_securityManager.hasRoleForResource(
            dbc,
            dbc.currentUser(),
            CmsRole.VFS_MANAGER,
            resource);

        // check lock status
        boolean writeRequired = requiredPermissions.requiresWritePermission()
            || requiredPermissions.requiresControlPermission();

        // if the resource type is jsp
        // write is only allowed for administrators
        if (writeRequired && !canIgnorePermissions && (CmsResourceTypeJsp.isJsp(resource))) {
            if (!m_securityManager.hasRoleForResource(dbc, dbc.currentUser(), CmsRole.VFS_MANAGER, resource)) {
                denied |= CmsPermissionSet.PERMISSION_WRITE;
                denied |= CmsPermissionSet.PERMISSION_CONTROL;
            }
        }

        if (writeRequired && checkLock) {
            // check lock state only if required
            CmsLock lock = m_driverManager.getLock(dbc, resource);
            // if the resource is not locked by the current user, write and control
            // access must cause a permission error that must not be cached
            if (lock.isUnlocked() || !lock.isLockableBy(dbc.currentUser())) {
                return I_CmsPermissionHandler.PERM_NOTLOCKED;
            }
        }

        CmsPermissionSetCustom permissions;
        if (canIgnorePermissions) {
            // if the current user is administrator, anything is allowed
            permissions = new CmsPermissionSetCustom(~0);
        } else {
            // otherwise, get the permissions from the access control list
            permissions = m_driverManager.getPermissions(dbc, resource, dbc.currentUser());
        }

        // revoke the denied permissions
        permissions.denyPermissions(denied);

        if ((permissions.getPermissions() & CmsPermissionSet.PERMISSION_VIEW) == 0) {
            // resource "invisible" flag is set for this user
            if (!canIgnorePermissions && filter.requireVisible()) {
                // filter requires visible permission - extend required permission set
                requiredPermissions = new CmsPermissionSet(
                    requiredPermissions.getAllowedPermissions() | CmsPermissionSet.PERMISSION_VIEW,
                    requiredPermissions.getDeniedPermissions());
            } else {
                // view permissions can be ignored by filter
                permissions.setPermissions(
                    // modify permissions so that view is allowed
                    permissions.getAllowedPermissions() | CmsPermissionSet.PERMISSION_VIEW,
                    permissions.getDeniedPermissions() & ~CmsPermissionSet.PERMISSION_VIEW);
            }
        }

        if (requiredPermissions.requiresDirectPublishPermission()) {
            // direct publish permission is required
            if ((permissions.getPermissions() & CmsPermissionSet.PERMISSION_DIRECT_PUBLISH) == 0) {
                // but the user has no direct publish permission, so check if the user has the project manager role
                boolean canIgnorePublishPermission = m_securityManager.hasRoleForResource(
                    dbc,
                    dbc.currentUser(),
                    CmsRole.PROJECT_MANAGER,
                    resource);
                // if not, check the manageable projects
                if (!canIgnorePublishPermission) {
                    CmsUser user = dbc.currentUser();
                    Iterator<CmsProject> itProjects = m_driverManager.getAllManageableProjects(
                        dbc,
                        m_driverManager.readOrganizationalUnit(dbc, user.getOuFqn()),
                        true).iterator();
                    while (itProjects.hasNext()) {
                        CmsProject project = itProjects.next();
                        if (CmsProject.isInsideProject(m_driverManager.readProjectResources(dbc, project), resource)) {
                            canIgnorePublishPermission = true;
                            break;
                        }
                    }
                }

                if (canIgnorePublishPermission) {
                    // direct publish permission can be ignored
                    permissions.setPermissions(
                        // modify permissions so that direct publish is allowed
                        permissions.getAllowedPermissions() | CmsPermissionSet.PERMISSION_DIRECT_PUBLISH,
                        permissions.getDeniedPermissions() & ~CmsPermissionSet.PERMISSION_DIRECT_PUBLISH);
                }
            }
        }

        CmsPermissionCheckResult result;
        if ((requiredPermissions.getPermissions()
            & (permissions.getPermissions())) == requiredPermissions.getPermissions()) {
            result = I_CmsPermissionHandler.PERM_ALLOWED;
        } else {
            result = I_CmsPermissionHandler.PERM_DENIED;
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_NO_PERMISSION_RESOURCE_USER_4,
                        new Object[] {
                            dbc.getRequestContext().removeSiteRoot(resource.getRootPath()),
                            dbc.currentUser().getName(),
                            requiredPermissions.getPermissionString(),
                            permissions.getPermissionString()}));
            }
        }
        if (dbc.getProjectId().isNullUUID()) {
            OpenCms.getMemoryMonitor().cachePermission(cacheKey, result);
        }

        return result;
    }

    /**
     * @see org.opencms.security.I_CmsPermissionHandler#init(org.opencms.db.CmsDriverManager, CmsSystemConfiguration)
     */
    public void init(CmsDriverManager driverManager, CmsSystemConfiguration systemConfiguration) {

        m_driverManager = driverManager;
        m_securityManager = driverManager.getSecurityManager();

        CmsCacheSettings settings = systemConfiguration.getCacheSettings();

        String className = settings.getCacheKeyGenerator();
        try {
            // initialize the key generator
            m_keyGenerator = (I_CmsCacheKey)Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new CmsInitException(
                org.opencms.main.Messages.get().container(
                    org.opencms.main.Messages.ERR_CRITICAL_CLASS_CREATION_1,
                    className),
                e);
        }
    }
}
