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

package org.opencms.ui.actions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.ui.dialogs.CmsCopyMoveDialog;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * The copy move dialog action.<p>
 */
public class CmsCopyDialogAction extends A_CmsWorkplaceAction {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCopyDialogAction.class);

    /** The action id. */
    public static final String ACTION_ID = "copy";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = CmsStandardVisibilityCheck.DEFAULT;

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        if (!hasBlockingLocks(context)) {
            CmsCopyMoveDialog dialog = new CmsCopyMoveDialog(context, CmsCopyMoveDialog.DialogMode.copy);
            if (!context.getResources().isEmpty()) {
                CmsResource res = context.getResources().get(0);
                CmsResource parent;
                try {
                    parent = context.getCms().readParentFolder(res.getStructureId());

                    dialog.setTargetForlder(parent);
                } catch (CmsException e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
            openDialog(dialog, context);
        }
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getDialogTitle()
     */
    @Override
    public String getDialogTitle() {

        return getWorkplaceMessage(org.opencms.ui.Messages.GUI_DIALOGTITLE_COPY_0);
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return ACTION_ID;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getTitle()
     */
    public String getTitle() {

        return getWorkplaceMessage(org.opencms.ui.Messages.GUI_DIALOGTITLE_COPY_0);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return VISIBILITY.getVisibility(cms, resources);
    }
}
