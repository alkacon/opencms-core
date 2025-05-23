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

package org.opencms.ui.dialogs.embedded;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.actions.I_CmsWorkplaceAction;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;

import java.util.List;
import java.util.Locale;

/**
 * Action for the data view dialog.<p>
 */
public class CmsDataViewAction implements I_CmsWorkplaceAction {

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        context.start(getTitle(A_CmsUI.get().getLocale()), new CmsDataViewDialog(context), DialogWidth.max);
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return "dataview";
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getTitle(java.util.Locale)
     */
    public String getTitle(Locale locale) {

        return OpenCms.getWorkplaceManager().getMessages(locale).key(org.opencms.ui.Messages.GUI_DATAVIEW_HEADER_0);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        throw new UnsupportedOperationException("not supported");
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        throw new UnsupportedOperationException("not supported");
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#isActive(org.opencms.ui.I_CmsDialogContext)
     */
    public boolean isActive(I_CmsDialogContext context) {

        return true;
    }

}
