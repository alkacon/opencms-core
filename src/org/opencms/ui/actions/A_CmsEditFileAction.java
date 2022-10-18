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
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsVfsService;
import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to edit a file given by the file param.<p>
 * Used within the ADE context only.<p>
 */
public abstract class A_CmsEditFileAction extends A_CmsWorkplaceAction implements I_CmsADEAction {

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        // not supported
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getCommandClassName()
     */
    public String getCommandClassName() {

        return "org.opencms.gwt.client.ui.contextmenu.CmsEditFile";
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

        Map<String, String> params = new HashMap<String, String>();
        params.put("reload", Boolean.TRUE.toString());
        params.put("immediateReload", Boolean.TRUE.toString());
        params.put("filename", getFileParam());
        return params;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#isAdeSupported()
     */
    public boolean isAdeSupported() {

        return true;
    }

    /**
     * Checks whether the file specified by the file param and the given context exists and is visible.<p>
     *
     * @param context the dialog context
     *
     * @return <code>true</code> in case the file exists and is visible
     */
    protected boolean existsFile(I_CmsDialogContext context) {

        CmsResource res = context.getResources().get(0);
        if (OpenCms.getADEManager().isEditorRestricted(context.getCms(), res)) {
            return false;
        }
        String sitePath = CmsVfsService.prepareFileNameForEditor(context.getCms(), res, getFileParam());
        return context.getCms().existsResource(sitePath, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
    }

    /**
     * Returns the file parameter.<p>
     *
     * @return parameter of the file to edit
     */
    protected abstract String getFileParam();
}
