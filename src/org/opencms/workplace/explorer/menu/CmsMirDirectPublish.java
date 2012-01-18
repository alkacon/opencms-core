/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.explorer.menu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPermissionSet;
import org.opencms.workplace.explorer.CmsResourceUtil;

import org.apache.commons.logging.Log;

/**
 * Defines a menu item rule that sets the visibility to active
 * if the current resource can be directly published by the current user.<p>
 * 
 * @since 6.5.6
 */
public class CmsMirDirectPublish extends A_CmsMenuItemRule {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMirDirectPublish.class);

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#getVisibility(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        if (!resourceUtil[0].isInsideProject()) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_OTHERPROJECT_0);
        }

        CmsLock lock = resourceUtil[0].getLock();
        if (lock.isNullLock()
            || (lock.isExclusiveOwnedInProjectBy(
                cms.getRequestContext().getCurrentUser(),
                cms.getRequestContext().getCurrentProject()))) {
            // resource is not locked or exclusively locked by current user in current project

            try {
                if (cms.hasPermissions(resourceUtil[0].getResource(), CmsPermissionSet.ACCESS_DIRECT_PUBLISH)) {
                    // only activate if user has direct publish permissions
                    if (resourceUtil[0].getResource().isFolder()
                        || !resourceUtil[0].getResource().getState().isUnchanged()) {
                        // resource is a folder or not unchanged
                        CmsResource parent = cms.readFolder(CmsResource.getParentFolder(cms.getSitePath(resourceUtil[0].getResource())));
                        if (parent.getState().isNew()) {
                            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_PARENTFOLDER_0);
                        }
                        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
                    } else if (!resourceUtil[0].getResource().isFolder()
                        && resourceUtil[0].getResource().getState().isUnchanged()) {
                        return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_UNCHANGED_0);
                    }
                } else {
                    return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_PUBLISH_0);
                }
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        if (lock.isInherited()) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_INHERITED_0);
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_NOT_LOCKED_0);
    }

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#matches(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    public boolean matches(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        return true;
    }

}
