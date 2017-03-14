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
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.extensions.CmsJSPBrowserFrameExtension;

import java.util.List;

/**
 * Abstract class for actions to display a JSP file in a vaadin window.<p>
 *
 * How to pass resources to and from JSP:
 * -GET request parameter "resource" returns an array of UUIDs as String.<p>
 * -JavaScript function "window.parent.changedResources(resources);" closes the window and returns the String array "resources" to the server<p>
 */
public abstract class A_CmsJSPAction extends A_CmsWorkplaceAction {

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        String link = OpenCms.getLinkManager().substituteLinkForRootPath(A_CmsUI.getCmsObject(), getJSPPath())
            + getRequestString(context.getResources());
        CmsJSPBrowserFrameExtension.showExtendedBrowserFrame(link, context);
    }

    /**
     * Sets the absolute path (in the vfs) to a jsp file used for the action.<p>
     *
     * @return path of jsp file
     */
    public abstract String getJSPPath();

    /**
     * Creates string for get—request with given list of resources.<p>
     *
     * @param resources to be transmitted
     * @return valid string for get-request
     */
    protected String getRequestString(List<CmsResource> resources) {

        String res = "?";

        for (CmsResource resource : resources) {
            res += "resources=" + resource.getStructureId().getStringValue() + "&";
        }
        return res.substring(0, res.length() - 1); //Remove last "&"
    }
}
