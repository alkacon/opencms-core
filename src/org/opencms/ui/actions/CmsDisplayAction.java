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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.lists.CmsListManager;
import org.opencms.ui.apps.lists.CmsListManagerConfiguration;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilitySingleOnly;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.util.CmsStringUtil;

import java.util.List;

/**
 * The display action. Renders the selected resource.<p>
 */
public class CmsDisplayAction extends A_CmsWorkplaceAction implements I_CmsDefaultAction {

    /** The name of the online version window. */
    public static final String ONLINE_WINDOW_NAME = "_blank";

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
            CmsResource resource = context.getResources().get(0);
            if (OpenCms.getResourceManager().getResourceType(resource).getTypeName().equals(
                CmsListManager.RES_TYPE_LIST_CONFIG)) {
                CmsAppWorkplaceUi.get().showApp(
                    OpenCms.getWorkplaceAppManager().getAppConfiguration(CmsListManagerConfiguration.APP_ID),
                    A_CmsWorkplaceApp.addParamToState(
                        "",
                        CmsEditor.RESOURCE_ID_PREFIX,
                        resource.getStructureId().toString()));
            } else {

                CmsObject cms = context.getCms();
                try {
                    cms = OpenCms.initCmsObject(cms);
                    cms.getRequestContext().setUri(cms.getSitePath(resource));
                } catch (CmsException e) {
                    // It's ok, we stick to the original context.
                }

                boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
                String link;
                if (isOnline
                    && !(CmsStringUtil.isEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot())
                        || OpenCms.getSiteManager().isSharedFolder(cms.getRequestContext().getSiteRoot()))) {
                    // use the online link only in case the current site is not the root site or the shared folder
                    link = OpenCms.getLinkManager().getOnlineLink(cms, cms.getSitePath(resource));
                } else {
                    link = OpenCms.getLinkManager().substituteLink(cms, resource);
                }
                if (isOnline
                    || !(OpenCms.getResourceManager().getResourceType(resource) instanceof CmsResourceTypeXmlContent)) {
                    A_CmsUI.get().openPageOrWarn(link, ONLINE_WINDOW_NAME);
                } else {
                    A_CmsUI.get().getPage().setLocation(link);
                }
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

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getTitleKey()
     */
    @Override
    protected String getTitleKey() {

        return Messages.GUI_ACTION_DISPLAY_0;
    }
}
