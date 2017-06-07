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
import org.opencms.jsp.CmsJspTagEnableAde;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilitySingleOnly;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * The display action. Renders the selected resource.<p>
 */
public class CmsDisplayAction extends A_CmsWorkplaceAction implements I_CmsDefaultAction {

    /** The name of the online version window. */
    public static final String ONLINE_WINDOW_NAME = "opencmsOnline";

    /** The action id. */
    public static final String ACTION_ID = "display";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = new CmsMenuItemVisibilitySingleOnly(
        CmsStandardVisibilityCheck.EDIT);

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        if (context.getResources().size() == 1) {

            HttpServletRequest req = CmsVaadinUtils.getRequest();
            CmsJspTagEnableAde.removeDirectEditFlagFromSession(req.getSession());
            if (context.getCms().getRequestContext().getCurrentProject().isOnlineProject()) {
                String link = OpenCms.getLinkManager().getOnlineLink(
                    context.getCms(),
                    context.getCms().getSitePath(context.getResources().get(0)));
                A_CmsUI.get().openPageOrWarn(link, ONLINE_WINDOW_NAME);
            } else {
                String link = OpenCms.getLinkManager().substituteLink(context.getCms(), context.getResources().get(0));
                A_CmsUI.get().getPage().setLocation(link);
            }
        }
    }

    /**
     * @see org.opencms.ui.actions.I_CmsDefaultAction#getDefaultActionRank(org.opencms.ui.I_CmsDialogContext)
     */
    public int getDefaultActionRank(I_CmsDialogContext context) {

        return 10;
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

        return getWorkplaceMessage(Messages.GUI_ACTION_DISPLAY_0);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        if ((resources.size() == 1)
            && resources.get(0).isFile()
            && !CmsResourceTypeXmlContainerPage.isContainerPage(resources.get(0))) {
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        } else {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }
}
