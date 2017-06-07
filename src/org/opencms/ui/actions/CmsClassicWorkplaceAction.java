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
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsQuickLaunchLocationCache;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

/**
 * Action for showing locked resources by opening the GWT lock report.<p>
 */
public final class CmsClassicWorkplaceAction extends A_CmsWorkplaceAction {

    /** The name of the classic workplace window. */
    public static final String WORKPLACE_WINDOW_NAME = "workplaceClassic";

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(final I_CmsDialogContext context) {

        CmsObject cms = context.getCms();
        String initPath = CmsQuickLaunchLocationCache.getLocationCache(
            A_CmsUI.get().getHttpSession()).getFileExplorerLocation(cms.getRequestContext().getSiteRoot());

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(initPath)) {
            initPath = cms.getRequestContext().addSiteRoot(initPath);
        } else {
            initPath = CmsFileUtil.addTrailingSeparator(cms.getRequestContext().getSiteRoot());
        }
        String link = CmsWorkplace.getWorkplaceExplorerLink(cms, initPath);
        A_CmsUI.get().openPageOrWarn(link, WORKPLACE_WINDOW_NAME);
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return "oldworkplace";
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getTitle()
     */
    public String getTitle() {

        return CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_EXPLORER_CONTEXT_OLD_WORKPLACE_0);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        boolean visible = ((resources == null) || resources.isEmpty())
            && cms.existsResource(CmsWorkplace.JSP_WORKPLACE_URI);
        return visible ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }
}