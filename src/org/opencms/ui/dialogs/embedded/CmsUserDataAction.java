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

package org.opencms.ui.dialogs.embedded;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.actions.I_CmsWorkplaceAction;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

public class CmsUserDataAction implements I_CmsWorkplaceAction {

    public void executeAction(I_CmsDialogContext context) {

        //        CmsUserDataDialog dialog = new CmsUserDataDialog(context);
        //context.start(dialog.getTitle(A_CmsUI.get().getLocale()), new CmsUserDataDialog(context));
    }

    public String getId() {

        return "userdata";
    }

    public String getTitle() {

        return "User data";
    }

    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        throw new UnsupportedOperationException("not supported");
    }

    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        throw new UnsupportedOperationException("not supported");
    }

    public boolean isActive(I_CmsDialogContext context) {

        return true;
    }

}
