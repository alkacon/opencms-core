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
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.workplace.explorer.CmsResourceUtil;

import org.apache.commons.logging.Log;

/**
 * Defines a menu item rule for the multi context menu that checks that the parent folder not new is.<p>
 *
 * @since 7.0.2
 */
public class CmsMirMultiDirPublishStandard extends A_CmsMenuItemRule {

    /** The name of the standard rule set used for multi context menu entries. */
    public static final String RULE_NAME = "multipubstandard";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMirMultiDirPublishStandard.class);

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#getVisibility(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_ONLINEPROJECT_0);
        }
        try {
            CmsResource parent = cms.readFolder(
                CmsResource.getParentFolder(cms.getSitePath(resourceUtil[0].getResource())));
            if (parent.getState().isNew()) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE.addMessageKey(
                    Messages.GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_PARENTFOLDER_0);
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
    }

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#matches(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    public boolean matches(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        return true;
    }

}
