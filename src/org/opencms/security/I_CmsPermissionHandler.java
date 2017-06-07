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
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.util.A_CmsModeIntEnumeration;

/**
 * Permission handler interface.<p>
 *
 * @since 7.0.2
 *
 * @see org.opencms.db.CmsSecurityManager#hasPermissions(org.opencms.file.CmsRequestContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)
 */
public interface I_CmsPermissionHandler {

    /**
     *  Enumeration class for the results of {@link I_CmsPermissionHandler#hasPermissions(CmsDbContext, CmsResource, CmsPermissionSet, boolean, CmsResourceFilter)}.<p>
     */
    public static final class CmsPermissionCheckResult extends A_CmsModeIntEnumeration {

        /** Indicates allowed permissions. */
        protected static final CmsPermissionCheckResult ALLOWED = new CmsPermissionCheckResult(1);

        /** Indicates denied permissions. */
        protected static final CmsPermissionCheckResult DENIED = new CmsPermissionCheckResult(2);

        /** Indicates a resource was filtered during permission check. */
        protected static final CmsPermissionCheckResult FILTERED = new CmsPermissionCheckResult(3);

        /** Indicates a resource was not locked for a write / control operation. */
        protected static final CmsPermissionCheckResult NOTLOCKED = new CmsPermissionCheckResult(4);

        /** Version id required for safe serialization. */
        private static final long serialVersionUID = 2398277834335860916L;

        /**
         * Private constructor.<p>
         *
         * @param mode the copy mode integer representation
         */
        private CmsPermissionCheckResult(int mode) {

            super(mode);
        }

        /**
         * Checks if this permission is allowed or not.<p>
         *
         * @return <code>true</code> if allowed
         */
        public boolean isAllowed() {

            return (this == ALLOWED);
        }
    }

    /** Indicates allowed permissions. */
    CmsPermissionCheckResult PERM_ALLOWED = CmsPermissionCheckResult.ALLOWED;
    /** Indicates denied permissions. */
    CmsPermissionCheckResult PERM_DENIED = CmsPermissionCheckResult.DENIED;
    /** Indicates a resource was filtered during permission check. */
    CmsPermissionCheckResult PERM_FILTERED = CmsPermissionCheckResult.FILTERED;
    /** Indicates a resource was not locked for a write / control operation. */
    CmsPermissionCheckResult PERM_NOTLOCKED = CmsPermissionCheckResult.NOTLOCKED;

    /**
     * Performs a non-blocking permission check on a resource.<p>
     *
     * This test will not throw an exception in case the required permissions are not
     * available for the requested operation. Instead, it will return one of the
     * following values:<ul>
     * <li><code>{@link #PERM_ALLOWED}</code></li>
     * <li><code>{@link #PERM_FILTERED}</code></li>
     * <li><code>{@link #PERM_DENIED}</code></li></ul><p>
     *
     * Despite of the fact that the results of this method are cached, this method should
     * be as fast as possible since it is called really often.<p>
     *
     * @param dbc the current database context
     * @param resource the resource on which permissions are required
     * @param requiredPermissions the set of permissions required for the operation
     * @param checkLock if true, a lock for the current user is required for
     *      all write operations, if false it's ok to write as long as the resource
     *      is not locked by another user
     * @param filter the resource filter to use
     *
     * @return <code>{@link #PERM_ALLOWED}</code> if the user has sufficient permissions on the resource
     *      for the requested operation
     *
     * @throws CmsException in case of i/o errors (NOT because of insufficient permissions)
     */
    CmsPermissionCheckResult hasPermissions(
        CmsDbContext dbc,
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException;

    /**
     * Initializes internal variables needed to work.<p>
     *
     * @param driverManager the driver manager
     * @param systemConfiguration the system configuration instance
     */
    void init(CmsDriverManager driverManager, CmsSystemConfiguration systemConfiguration);
}