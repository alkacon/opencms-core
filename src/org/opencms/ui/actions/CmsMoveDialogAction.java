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
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContextWithAdeContext;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.ui.dialogs.CmsCopyMoveDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * The copy move dialog action.<p>
 */
public class CmsMoveDialogAction extends A_CmsWorkplaceAction implements I_CmsADEAction {

    /** The action id. */
    public static final String ACTION_ID = "move";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = CmsStandardVisibilityCheck.DEFAULT;

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMoveDialogAction.class);

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        if (!hasBlockingLocks(context)) {
            CmsCopyMoveDialog dialog = new CmsCopyMoveDialog(context, CmsCopyMoveDialog.DialogMode.move);
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
     * @see org.opencms.ui.actions.I_CmsADEAction#getCommandClassName()
     */
    public String getCommandClassName() {

        return "org.opencms.gwt.client.ui.contextmenu.CmsEmbeddedAction";
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getDialogTitleKey()
     */
    @Override
    public String getDialogTitleKey() {

        return org.opencms.ui.Messages.GUI_DIALOGTITLE_MOVE_0;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return ACTION_ID;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getJspPath()
     */
    public String getJspPath() {

        return null;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getParams()
     */
    public Map<String, String> getParams() {

        Map<String, String> params = new HashMap<String, String>();
        params.put(CmsGwtConstants.ACTION_PARAM_DIALOG_ID, this.getClass().getName());
        return params;

    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return VISIBILITY.getVisibility(cms, resources);
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        if (context instanceof I_CmsDialogContextWithAdeContext) {
            AdeContext adeContext = ((I_CmsDialogContextWithAdeContext)context).getAdeContext();
            if (adeContext == AdeContext.resourceinfo) {
                if (OpenCms.getRoleManager().hasRole(context.getCms(), CmsRole.DEVELOPER)) {
                    return VISIBILITY.getVisibility(context.getCms(), context.getResources());
                }
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        } else {
            return VISIBILITY.getVisibility(context.getCms(), context.getResources());
        }
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#isAdeSupported()
     */
    public boolean isAdeSupported() {

        return true;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getTitleKey()
     */
    @Override
    protected String getTitleKey() {

        return org.opencms.ui.Messages.GUI_DIALOGTITLE_MOVE_0;
    }
}
