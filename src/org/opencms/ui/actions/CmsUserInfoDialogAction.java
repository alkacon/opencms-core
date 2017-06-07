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
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.components.CmsUploadButton.I_UploadListener;
import org.opencms.ui.components.CmsUserInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.dialogs.CmsEmbeddedDialogContext;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

import com.google.common.collect.Multimap;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * User info dialog action, used only for sitemap and page editor toolbar.<p>
 */
public class CmsUserInfoDialogAction extends A_CmsWorkplaceAction {

    /** The action id. */
    public static final String ACTION_ID = "userInfo";

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(final I_CmsDialogContext context) {

        CmsUserInfo dialog = new CmsUserInfo(new I_UploadListener() {

            public void onUploadFinished(List<String> uploadedFiles) {

                handleUpload(uploadedFiles, context);
            }
        }, context);
        Multimap<String, String> params = A_CmsUI.get().getParameters();
        int top = 55;
        int left = 0;
        if (params.containsKey("left")) {
            String buttonLeft = params.get("left").iterator().next();
            left = Integer.parseInt(buttonLeft) - 290;
        }
        final Window window = new Window();
        window.setModal(false);
        window.setClosable(true);
        window.setResizable(false);
        window.setContent(dialog);
        context.setWindow(window);
        window.addStyleName(OpenCmsTheme.DROPDOWN);
        UI.getCurrent().addWindow(window);
        window.setPosition(left, top);
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getDialogTitle()
     */
    @Override
    public String getDialogTitle() {

        return "";
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

        return "";
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        if ((context instanceof CmsEmbeddedDialogContext) && (ContextType.appToolbar == context.getContextType())) {
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        } else {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * Handles the user image file upload.<p>
     *
     * @param uploadedFiles the uploaded file names
     * @param context the dialog context
     */
    void handleUpload(List<String> uploadedFiles, I_CmsDialogContext context) {

        CmsObject cms = context.getCms();
        boolean success = OpenCms.getWorkplaceAppManager().getUserIconHelper().handleImageUpload(cms, uploadedFiles);
        if (success) {
            context.reload();
        }
    }
}
