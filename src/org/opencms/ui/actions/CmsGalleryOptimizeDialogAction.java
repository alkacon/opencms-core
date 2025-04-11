/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.actions;

import org.opencms.file.CmsResource;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.dialogs.CmsGalleryOptimizeDialog;
import org.opencms.workplace.explorer.Messages;

/**
 * The gallery optimize dialog action.<p>
 */
public class CmsGalleryOptimizeDialogAction extends A_CmsGalleryDialogAction {

    /** The action id. */
    public static final String ACTION_ID = "galleryoptimize";

    /** The folder types this action is available for. */
    private static final String[] GALLERY_TYPES = new String[] {"imagegallery", "downloadgallery"};

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        if (!hasBlockingLocks(context)) {
            CmsResource gallery = getGallery(context);
            CmsGalleryOptimizeDialog galleryOptimizeDialog = new CmsGalleryOptimizeDialog(context, gallery);
            openDialog(galleryOptimizeDialog, context, CmsBasicDialog.DialogWidth.max);
        }

    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return ACTION_ID;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsGalleryDialogAction#getSupportedGalleryTypes()
     */
    @Override
    protected String[] getSupportedGalleryTypes() {

        return GALLERY_TYPES;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getTitleKey()
     */
    @Override
    protected String getTitleKey() {

        return Messages.GUI_EXPLORER_CONTEXT_OPTIMIZEGALLERY_0;
    }

}
