/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsAccountsToolHandler.java,v $
 * Date   : $Date: 2007/02/02 08:28:38 $
 * Version: $Revision: 1.8.4.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsDefaultToolHandler;

import java.util.Iterator;
import java.util.List;

/**
 * Users management tool handler that hides the tool if the current user
 * has not the needed privileges.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.8.4.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAccountsToolHandler extends CmsDefaultToolHandler {

    /** Assign users file path constant. */
    private static final String ASSIGN_FILE = "/system/workplace/admin/accounts/user_assign.jsp";

    /** Delete file path constant. */
    private static final String DELETE_FILE = "/system/workplace/admin/accounts/unit_delete.jsp";

    /** Edit file path constant. */
    private static final String EDIT_FILE = "/system/workplace/admin/accounts/unit_edit.jsp";

    /** New file path constant. */
    private static final String NEW_FILE = "/system/workplace/admin/accounts/unit_new.jsp";

    /** Overview file path constant. */
    private static final String OVERVIEW_FILE = "/system/workplace/admin/accounts/unit_overview.jsp";

    /** Visibility flag module parameter name. */
    private static final String PARAM_VISIBILITY_FLAG = "visibility";

    /** Parent file path constant. */
    private static final String PARENT_FILE = "/system/workplace/admin/accounts/unit_parent.jsp";

    /** Visibility parameter value constant. */
    private static final String VISIBILITY_ALL = "all";

    /** Visibility parameter value constant. */
    private static final String VISIBILITY_NONE = "none";

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#getDisabledHelpText()
     */
    public String getDisabledHelpText() {

        if (super.getDisabledHelpText().equals(DEFAULT_DISABLED_HELPTEXT)) {
            return "${key." + Messages.GUI_ORGUNIT_ADMIN_TOOL_DISABLED_DELETE_HELP_0 + "}";
        }
        return super.getDisabledHelpText();
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isEnabled(org.opencms.workplace.CmsWorkplace)
     */
    public boolean isEnabled(CmsWorkplace wp) {

        if (getLink().equals(DELETE_FILE)) {
            String ouFqn = wp.getJsp().getRequest().getParameter(A_CmsOrgUnitDialog.PARAM_OUFQN);
            if (ouFqn == null) {
                ouFqn = wp.getCms().getRequestContext().getOuFqn();
            }
            try {
                if (OpenCms.getOrgUnitManager().getUsers(wp.getCms(), ouFqn, true).size() > 0) {
                    return false;
                }
                if (OpenCms.getOrgUnitManager().getGroups(wp.getCms(), ouFqn, true).size() > 0) {
                    List groups = OpenCms.getOrgUnitManager().getGroups(wp.getCms(), ouFqn, true);
                    Iterator itGroups = groups.iterator();
                    while (itGroups.hasNext()) {
                        CmsGroup group = (CmsGroup)itGroups.next();
                        if (!OpenCms.getDefaultUsers().isDefaultGroup(group.getName())) {
                            return false;
                        }
                    }
                }
                if (OpenCms.getOrgUnitManager().getOrganizationalUnits(wp.getCms(), ouFqn, true).size() > 0) {
                    return false;
                }
            } catch (CmsException e) {
                // noop
            }
        }
        return true;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.workplace.CmsWorkplace)
     */
    public boolean isVisible(CmsWorkplace wp) {

        if (getVisibilityFlag().equals(VISIBILITY_NONE)) {
            return false;
        }
        if (getVisibilityFlag().equals(VISIBILITY_ALL)) {
            CmsObject cms = wp.getCms();
            String ouFqn = wp.getJsp().getRequest().getParameter(A_CmsOrgUnitDialog.PARAM_OUFQN);
            if (ouFqn == null) {
                ouFqn = cms.getRequestContext().getOuFqn();
            }
            String parentOu = CmsOrganizationalUnit.getParentFqn(ouFqn);

            if (getLink().equals(OVERVIEW_FILE)) {
                if (parentOu != null) {
                    return (OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER) && !OpenCms.getRoleManager().hasRole(
                        cms,
                        CmsRole.ADMINISTRATOR.forOrgUnit(parentOu)));
                }
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER);
            } else if (getLink().equals(EDIT_FILE)) {
                if (parentOu != null) {
                    return (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR) && OpenCms.getRoleManager().hasRole(
                        cms,
                        CmsRole.ADMINISTRATOR.forOrgUnit(parentOu)));
                }
                return false;
            } else if (getLink().equals(NEW_FILE)) {
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR);
            } else if (getLink().equals(PARENT_FILE)) {
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(parentOu));
            } else if (getLink().equals(DELETE_FILE)) {
                if (parentOu != null) {
                    return (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR) && OpenCms.getRoleManager().hasRole(
                        cms,
                        CmsRole.ADMINISTRATOR.forOrgUnit(parentOu)));
                }
                return false;
            } else if (getLink().equals(ASSIGN_FILE)) {
                try {
                    List orgUnits = OpenCms.getRoleManager().getOrgUnitsForRole(
                        cms,
                        CmsRole.ADMINISTRATOR.forOrgUnit(""),
                        true);
                    if (orgUnits.size() > 1) {
                        return true;
                    }
                    return false;
                } catch (CmsException e) {
                    return super.isVisible(cms);
                }
            } else {
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER);
            }
        }
        return true;
    }

    /**
     * Returns the visibility flag module parameter value.<p>
     * 
     * @return the visibility flag module parameter value
     */
    private String getVisibilityFlag() {

        CmsModule module = OpenCms.getModuleManager().getModule(this.getClass().getPackage().getName());
        if (module == null) {
            return VISIBILITY_ALL;
        }
        return module.getParameter(PARAM_VISIBILITY_FLAG, VISIBILITY_ALL);
    }
}
