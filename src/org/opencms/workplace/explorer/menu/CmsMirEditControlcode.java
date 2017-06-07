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

package org.opencms.workplace.explorer.menu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.workplace.explorer.CmsResourceUtil;

/**
 * Defines a menu item rule that sets the visibility for the "Edit controlcode" context menu entry,
 * depending on the project and lock state of the resource.<p>
 *
 * @since 6.9.2
 */
public class CmsMirEditControlcode extends A_CmsMenuItemRule {

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#getVisibility(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        try {
            if (resourceUtil[0].isInsideProject() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                // we are in the correct offline project and resource is not deleted
                CmsLock lock = resourceUtil[0].getLock();
                boolean lockedForPublish = resourceUtil[0].getProjectState().isLockedForPublishing();
                if (lock.isNullLock()) {
                    // resource is not locked, check autolock
                    if (!lockedForPublish
                        && OpenCms.getWorkplaceManager().autoLockResources()
                        && !resourceUtil[0].getResource().getState().isDeleted()) {
                        if (OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER)
                            && cms.hasPermissions(
                                resourceUtil[0].getResource(),
                                CmsPermissionSet.ACCESS_WRITE,
                                false,
                                CmsResourceFilter.ALL)) {
                            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
                        }
                    } else if (OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER)) {
                        if (resourceUtil[0].getResource().getState().isDeleted()) {
                            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                                Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_DELETED_0);
                        }
                        return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                            Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_WRITE_0);
                    }
                }
                if (!lockedForPublish
                    && !lock.isShared()
                    && lock.isOwnedInProjectBy(
                        cms.getRequestContext().getCurrentUser(),
                        cms.getRequestContext().getCurrentProject())) {
                    // resource is exclusively locked by the current user
                    if (OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER)) {
                        if (!resourceUtil[0].getResource().getState().isDeleted()
                            && cms.hasPermissions(
                                resourceUtil[0].getResource(),
                                CmsPermissionSet.ACCESS_WRITE,
                                false,
                                CmsResourceFilter.ALL)) {
                            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
                        } else {
                            if (resourceUtil[0].getResource().getState().isDeleted()) {
                                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                                    Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_DELETED_0);
                            }
                            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                                Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_WRITE_0);
                        }
                    }
                }

            }
        } catch (CmsException e) {
            // should not happen, anyway invisible is returned
        }
        // current user cannot see the entry
        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#matches(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    public boolean matches(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        // this rule always matches
        return true;
    }

}
