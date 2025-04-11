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
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContextWithAdeContext;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.components.extensions.CmsPropertyDialogExtension;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilitySingleOnly;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.Messages;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * The properties dialog action.<p>
 */
public class CmsPropertiesDialogAction extends A_CmsWorkplaceAction implements I_CmsADEAction {

    /** The action id. */
    public static final String ACTION_ID = "properties";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = new CmsMenuItemVisibilitySingleOnly(
        CmsStandardVisibilityCheck.VIEW);

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY_AUTHOR = new CmsMenuItemVisibilitySingleOnly(
        CmsStandardVisibilityCheck.VIEW_AUTHOR);

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(final I_CmsDialogContext context) {

        try {
            CmsPropertyDialogExtension dialogExtension = new CmsPropertyDialogExtension(
                A_CmsUI.get(),
                new I_CmsUpdateListener<String>() {

                    public void onUpdate(List<String> updatedItems) {

                        List<CmsUUID> updatedIds = Lists.newArrayList();
                        for (String item : updatedItems) {
                            updatedIds.add(new CmsUUID(item));

                        }
                        context.finish(updatedIds);
                    }
                });
            dialogExtension.editProperties(
                context.getResources().get(0).getStructureId(),
                context.getAllStructureIdsInView(),
                false);
        } catch (Exception e) {
            context.error(e);
        }
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getCommandClassName()
     */
    public String getCommandClassName() {

        return "org.opencms.gwt.client.ui.contextmenu.CmsEditProperties";
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

        return null;
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
            if (AdeContext.gallery.equals(adeContext)) {
                if (OpenCms.getWorkplaceManager().isAllowElementAuthorToWorkInGalleries()) {
                    return VISIBILITY_AUTHOR.getVisibility(context.getCms(), context.getResources());
                }
            }
        }
        return VISIBILITY.getVisibility(context.getCms(), context.getResources());

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

        return Messages.GUI_EXPLORER_CONTEXT_ADVANCED_PROPERTIES_0;
    }
}
