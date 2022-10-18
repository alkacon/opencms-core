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

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Action to logout.<p>
 * Used within the ADE context only.<p>
 */
public class CmsEditContentAction extends A_CmsEditFileAction {

    /** The action id. */
    public static final String ACTION_ID = "ade_editcontent";

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return ACTION_ID;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        Set<String> validContexts = Sets.newHashSet();
        for (AdeContext adecontext : Arrays.asList(AdeContext.gallery, AdeContext.resourceinfo)) {
            validContexts.add(adecontext.name());
        }
        if (!validContexts.contains(context.getAppId())) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
        List<CmsResource> resources = context.getResources();
        CmsResource resource = resources.get(0);
        if (OpenCms.getADEManager().isEditorRestricted(context.getCms(), resource)) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        if ((type instanceof CmsResourceTypeXmlContent) && !CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            return CmsStandardVisibilityCheck.DEFAULT.getSingleVisibility(context.getCms(), resource);
        } else {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * @see org.opencms.ui.actions.A_CmsEditFileAction#getFileParam()
     */
    @Override
    protected String getFileParam() {

        return "%(file)";
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getTitleKey()
     */
    @Override
    protected String getTitleKey() {

        return Messages.GUI_ACTION_EDIT_CONTENT_0;
    }
}
