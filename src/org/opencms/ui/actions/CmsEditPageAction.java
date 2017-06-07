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
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

/**
 * The edit page action. Available for container pages.<p>
 */
public class CmsEditPageAction extends CmsDisplayAction {

    /** The action id. */
    @SuppressWarnings("hiding")
    public static final String ACTION_ID = "editpage";

    /**
     * @see org.opencms.ui.actions.I_CmsDefaultAction#getDefaultActionRank(org.opencms.ui.I_CmsDialogContext)
     */
    @Override
    public int getDefaultActionRank(I_CmsDialogContext context) {

        return 30;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    @Override
    public String getId() {

        return ACTION_ID;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getTitle()
     */
    @Override
    public String getTitle() {

        return getWorkplaceMessage(org.opencms.ui.Messages.GUI_ACTION_OPEN_PAGE_0);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        if ((resources.size() == 1) && CmsResourceTypeXmlContainerPage.isContainerPage(resources.get(0))) {
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        } else {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }
}
