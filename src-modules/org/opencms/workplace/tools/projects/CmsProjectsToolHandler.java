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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.projects;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.administration.CmsAdminDialog;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.tools.A_CmsToolHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Users management tool handler that hides the tool if the current user
 * has not the needed privileges.<p>
 *
 * @since 6.0.0
 */
public class CmsProjectsToolHandler extends A_CmsToolHandler {

    private static final String PROJECT_ID = "projectid";
    private static final String PROJECT_NAME = "projectname";
    private static final String PROJECT_OVERVIEW_FILE = "/system/workplace/admin/projects/project_overview.jsp";

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#getParameters(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    public Map getParameters(CmsWorkplace wp) {

        if (OpenCms.getRoleManager().hasRole(wp.getCms(), CmsRole.PROJECT_MANAGER)) {
            return super.getParameters(wp);
        } else {
            Map argMap = new HashMap();
            argMap.put(PROJECT_ID, wp.getCms().getRequestContext().getCurrentProject().getUuid().toString());
            argMap.put(PROJECT_NAME, wp.getCms().getRequestContext().getCurrentProject().getName());
            if (wp instanceof CmsProjectFilesDialog) {
                argMap.put(A_CmsListExplorerDialog.PARAM_SHOW_EXPLORER, "false");
            }
            if (wp instanceof CmsAdminDialog) {
                argMap.put(A_CmsListExplorerDialog.PARAM_SHOW_EXPLORER, "true");
            }
            return argMap;
        }
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        if (OpenCms.getRoleManager().hasRole(cms, CmsRole.PROJECT_MANAGER)) {
            return !getLink().equals(PROJECT_OVERVIEW_FILE);
        } else {
            return getLink().equals(PROJECT_OVERVIEW_FILE);
        }
    }
}
