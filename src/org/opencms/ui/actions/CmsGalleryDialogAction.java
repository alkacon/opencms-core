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
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.Messages;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The gallery dialog action.<p>
 */
public class CmsGalleryDialogAction extends A_CmsWorkplaceAction {

    /** The action id. */
    public static final String ACTION_ID = "gallery";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = CmsStandardVisibilityCheck.VISIBLE;

    /** The folder types this action is available for. */
    private static final String[] GALLERY_TYPES = new String[] {"imagegallery", "downloadgallery", "linkgallery"};

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(final I_CmsDialogContext context) {

        try {
            CmsGwtDialogExtension dialogExtension = new CmsGwtDialogExtension(
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
            dialogExtension.openGalleryDialog(context.getResources().get(0));
        } catch (Exception e) {
            context.error(e);
        }
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

        return getWorkplaceMessage(Messages.GUI_EXPLORER_CONTEXT_OPENGALLERY_0);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        if (resources.size() == 1) {
            CmsResource res = resources.get(0);
            if (res.isFolder()) {
                String type = OpenCms.getResourceManager().getResourceType(res).getTypeName();
                if (Arrays.asList(GALLERY_TYPES).contains(type)) {
                    return VISIBILITY.getVisibility(cms, resources);
                }
            }
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }
}
