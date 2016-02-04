/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.explorer.menu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsMenuCommandParameters;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPermissionSet;
import org.opencms.workplace.explorer.CmsExplorerContextMenuItem;
import org.opencms.workplace.explorer.CmsResourceUtil;

import org.apache.commons.logging.Log;

/**
 * A menu item rule which checks whether the file given by the 'filename' attribute in the CmsExplorerContextMenuItem's
 * parameters (after macro expansion) exists and is writable by the current user.<p>
 */
public class CmsMirSitemapActiveAndFileAvailable extends CmsMirSitemapActive {

    /** The logger instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsMirSitemapActiveAndFileAvailable.class);

    /**
     * @see org.opencms.workplace.explorer.menu.A_CmsMenuItemRule#getVisibility(org.opencms.file.CmsObject, org.opencms.workplace.explorer.CmsResourceUtil[], org.opencms.workplace.explorer.CmsExplorerContextMenuItem)
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(
        CmsObject cms,
        CmsResourceUtil[] resourceUtil,
        CmsExplorerContextMenuItem menuItem) {

        String origFileName = menuItem.getParamsMap().get(CmsMenuCommandParameters.PARAM_FILENAME);
        if (origFileName == null) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
        String fileName = CmsVfsService.prepareFileNameForEditor(cms, resourceUtil[0].getResource(), origFileName);
        try {
            CmsResource res = cms.readResource(fileName);
            if (!cms.hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.DEFAULT)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
        } catch (CmsVfsResourceNotFoundException e) {
            LOG.debug("Requested edit resource was not found.", e);
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
    }
}
