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

package org.opencms.db;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.security.CmsPermissionSet;

/**
 * Generates the cache keys for the user and permission caches.<p>
 *
 * @since 6.0.0
 */
public class CmsCacheKey implements I_CmsCacheKey {

    /** Cache key for a list of sub-resources (files and folders) of a folder. */
    public static final String CACHE_KEY_SUBALL = "_all_";

    /** Cache key for a list of sub-files of a folder. */
    public static final String CACHE_KEY_SUBFILES = "_files_";

    /** Cache key for a list of sub-folders of a folder. */
    public static final String CACHE_KEY_SUBFOLDERS = "_folders_";

    /**
     * Constructor to create a new instance of CmsCacheKey.<p>
     */
    public CmsCacheKey() {

        // empty
    }

    /**
     * @see org.opencms.db.I_CmsCacheKey#getCacheKeyForGroupUsers(java.lang.String, org.opencms.db.CmsDbContext, org.opencms.file.CmsGroup)
     */
    public String getCacheKeyForGroupUsers(String prefix, CmsDbContext context, CmsGroup group) {

        if (!context.getProjectId().isNullUUID()) {
            return "";
        }
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append(prefix);
        cacheBuffer.append('_');
        cacheBuffer.append(group.getName());
        return cacheBuffer.toString();
    }

    /**
     * @see org.opencms.db.I_CmsCacheKey#getCacheKeyForUserGroups(java.lang.String, org.opencms.db.CmsDbContext, org.opencms.file.CmsUser)
     */
    public String getCacheKeyForUserGroups(String prefix, CmsDbContext context, CmsUser user) {

        if (!context.getProjectId().isNullUUID()) {
            return "";
        }
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append(prefix);
        cacheBuffer.append('_');
        cacheBuffer.append(user.getName());
        return cacheBuffer.toString();
    }

    /**
     * @see org.opencms.db.I_CmsCacheKey#getCacheKeyForUserPermissions(java.lang.String, org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, org.opencms.security.CmsPermissionSet)
     */
    public String getCacheKeyForUserPermissions(
        String prefix,
        CmsDbContext context,
        CmsResource resource,
        CmsPermissionSet requiredPermissions) {

        if (!context.getProjectId().isNullUUID()) {
            return "";
        }
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append(prefix);
        cacheBuffer.append('_');
        cacheBuffer.append(context.currentUser().getName());
        cacheBuffer.append(context.currentProject().isOnlineProject() ? "_0_" : "_1_");
        cacheBuffer.append(requiredPermissions.getPermissionString());
        cacheBuffer.append('_');
        cacheBuffer.append(resource.getStructureId().toString());
        return cacheBuffer.toString();
    }
}
