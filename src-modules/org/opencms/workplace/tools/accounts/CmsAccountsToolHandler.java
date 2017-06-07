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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceUserInfoManager;
import org.opencms.workplace.tools.CmsDefaultToolHandler;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Users management tool handler that hides the tool if the current user
 * has not the needed privileges.<p>
 *
 * @since 6.0.0
 */
public class CmsAccountsToolHandler extends CmsDefaultToolHandler {

    /** Path for the kill session tool. */
    public static final String PATH_KILL_SESSIONS = "/accounts/orgunit/users/edit/kill_sessions";

    /** Path for the unlock tool. */
    public static final String PATH_UNLOCK = "/accounts/orgunit/users/edit/unlock";

    /** Visibility parameter value constant. */
    protected static final String VISIBILITY_ALL = "all";

    /** Account manager file path constant. */
    private static final String ACCMAN_FILE = "account_managers.jsp";

    /** All additional info file path constant. */
    private static final String ALLINFO_FILE = "user_allinfo.jsp";

    /** Assign users file path constant. */
    private static final String ASSIGN_FILE = "user_assign.jsp";

    /** Delete file path constant. */
    private static final String DELETE_FILE = "unit_delete.jsp";

    /** Group users file path constant. */
    private static final String GROUP_USERS_FILE = "group_users.jsp";

    /** Edit group users file path constant. */
    private static final String GROUPUSERS_FILE = "group_users.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAccountsToolHandler.class);

    /** New file path constant. */
    private static final String NEW_FILE = "unit_new.jsp";

    /** Organizational unit edit file path constant. */
    private static final String OU_EDIT_FILE = "unit_edit.jsp";

    /** Organizational unit roles file path constant. */
    private static final String OUROLES_FILE = "roles_list.jsp";

    /** Overview file path constant. */
    private static final String OVERVIEW_FILE = "unit_overview.jsp";

    /** Visibility flag module parameter name. */
    private static final String PARAM_VISIBILITY_FLAG = "visibility";

    /** Parent file path constant. */
    private static final String PARENT_FILE = "unit_parent.jsp";

    /** Role users edit file path constant. */
    private static final String ROLEUSERS_EDIT_FILE = "role_users.jsp";

    /** Switch user file path constant. */
    private static final String SWITCHUSER_FILE = "user_switch.jsp";

    /** User roles file path constant. */
    private static final String USERROLE_FILE = "user_role.jsp";

    /** Visibility parameter value constant. */
    private static final String VISIBILITY_NONE = "none";

    /** Flag to indicate if the current ou is a webuser ou. */
    private boolean m_webuserOu;

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#getDisabledHelpText()
     */
    @Override
    public String getDisabledHelpText() {

        if (super.getDisabledHelpText().equals(DEFAULT_DISABLED_HELPTEXT)) {
            if (getLink().equals(getPath(GROUPUSERS_FILE))) {
                return "${key." + Messages.GUI_VIRTUAL_GROUP_DISABLED_EDITION_HELP_0 + "}";
            }
            if (getLink().equals(getPath(ROLEUSERS_EDIT_FILE))) {
                return "${key." + Messages.GUI_ROLEUSERS_EDIT_DISABLED_HELP_0 + "}";
            }
            return "${key." + Messages.GUI_ORGUNIT_ADMIN_TOOL_DISABLED_DELETE_HELP_0 + "}";
        }
        return super.getDisabledHelpText();
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isEnabled(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    public boolean isEnabled(CmsWorkplace wp) {

        if (getLink().equals(getPath(GROUPUSERS_FILE))) {
            String groupId = CmsRequestUtil.getNotEmptyDecodedParameter(
                wp.getJsp().getRequest(),
                A_CmsEditGroupDialog.PARAM_GROUPID);
            try {
                return !wp.getCms().readGroup(new CmsUUID(groupId)).isVirtual();
            } catch (Exception e) {
                return false;
            }
        }
        if (!getLink().equals(ASSIGN_FILE)) {
            wp.getSession().removeAttribute(A_CmsOrgUnitUsersList.ORGUNIT_USERS);
            wp.getSession().removeAttribute(A_CmsOrgUnitUsersList.NOT_ORGUNIT_USERS);
        }

        if (getLink().equals(DELETE_FILE)) {
            String ouFqn = CmsRequestUtil.getNotEmptyDecodedParameter(
                wp.getJsp().getRequest(),
                A_CmsOrgUnitDialog.PARAM_OUFQN);
            if (ouFqn == null) {
                ouFqn = wp.getCms().getRequestContext().getOuFqn();
            }
            try {
                if (OpenCms.getOrgUnitManager().getUsers(wp.getCms(), ouFqn, true).size() > 0) {
                    return false;
                }
                if (OpenCms.getOrgUnitManager().getGroups(wp.getCms(), ouFqn, true).size() > 0) {
                    List<CmsGroup> groups = OpenCms.getOrgUnitManager().getGroups(wp.getCms(), ouFqn, true);
                    Iterator<CmsGroup> itGroups = groups.iterator();
                    while (itGroups.hasNext()) {
                        CmsGroup group = itGroups.next();
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

        if (getLink().equals(getPath(ROLEUSERS_EDIT_FILE))) {
            String roleName = CmsRequestUtil.getNotEmptyDecodedParameter(
                wp.getJsp().getRequest(),
                CmsRolesList.PARAM_ROLE);
            if (roleName == null) {
                return false;
            }
            if (!OpenCms.getRoleManager().hasRole(wp.getCms(), CmsRole.valueOfGroupName(roleName))) {
                return false;
            }
        } else if (getPath().indexOf("/users/edit/") > -1) {
            // check if the current user is the root administrator
            if (OpenCms.getRoleManager().hasRole(wp.getCms(), CmsRole.ROOT_ADMIN)) {
                return true;
            }

            String paramId = CmsRequestUtil.getNotEmptyDecodedParameter(
                wp.getJsp().getRequest(),
                A_CmsEditUserDialog.PARAM_USERID);
            if (paramId == null) {
                return false;
            }
            CmsUUID userId = new CmsUUID(paramId);
            try {
                CmsUser user = wp.getCms().readUser(userId);
                // check if the user to change is root administrator
                if (OpenCms.getRoleManager().hasRole(wp.getCms(), user.getName(), CmsRole.ROOT_ADMIN)) {
                    return false;
                }

                // check if the current user is an administrator
                if (OpenCms.getRoleManager().hasRole(wp.getCms(), CmsRole.ADMINISTRATOR)) {
                    return true;
                }
                // check if the user to change is an administrator
                return !OpenCms.getRoleManager().hasRole(wp.getCms(), user.getName(), CmsRole.ADMINISTRATOR);
            } catch (CmsException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return false;
        }

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.CmsDefaultToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    @Override
    public boolean isVisible(CmsObject cms) {

        if (getVisibilityFlag().equals(VISIBILITY_NONE)) {
            return false;
        }

        if (getLink().equals(getPath(ALLINFO_FILE))) {
            CmsWorkplaceUserInfoManager manager = OpenCms.getWorkplaceManager().getUserInfoManager();
            if ((manager == null) || (manager.getBlocks() == null) || manager.getBlocks().isEmpty()) {
                return false;
            }
        }

        if (!OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER)) {
            return false;
        }
        return true;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    public boolean isVisible(CmsWorkplace wp) {

        CmsObject cms = wp.getCms();
        if (!isVisible(cms)) {
            return false;
        }

        String ouFqn = CmsRequestUtil.getNotEmptyDecodedParameter(
            wp.getJsp().getRequest(),
            A_CmsOrgUnitDialog.PARAM_OUFQN);
        if (ouFqn == null) {
            ouFqn = cms.getRequestContext().getOuFqn();
        }
        String parentOu = CmsOrganizationalUnit.getParentFqn(ouFqn);
        try {
            m_webuserOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ouFqn).hasFlagWebuser();
        } catch (CmsException e) {
            // ignore
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        if (getLink().equals(getPath(OVERVIEW_FILE))) {
            if (parentOu != null) {
                return !OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(parentOu));
            }
            return true;
        } else if (getLink().equals(getPath(OU_EDIT_FILE))) {
            if (parentOu != null) {
                return (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR)
                    && OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(parentOu)));
            } else {
                return false;
            }
        } else if (getLink().equals(getPath(NEW_FILE))) {
            if (m_webuserOu) {
                return false;
            }
            return OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR);
        } else if (getLink().equals(getPath(PARENT_FILE))) {
            if (parentOu != null) {
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(parentOu));
            } else {
                return false;
            }
        } else if (getLink().equals(getPath(DELETE_FILE))) {
            if (parentOu != null) {
                return (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR)
                    && OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(parentOu)));
            } else {
                return false;
            }
        } else if (getLink().equals(getPath(ASSIGN_FILE))) {
            try {
                List<CmsOrganizationalUnit> orgUnits = OpenCms.getRoleManager().getOrgUnitsForRole(
                    cms,
                    CmsRole.ACCOUNT_MANAGER.forOrgUnit(""),
                    true);
                if (orgUnits.size() == 1) {
                    return false;
                }
                return !m_webuserOu;
            } catch (CmsException e) {
                // ignore
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        } else if (getLink().equals(getPath(OUROLES_FILE))) {
            return !m_webuserOu;
        } else if (getLink().equals(getPath(SWITCHUSER_FILE))) {
            boolean visible = false;
            CmsUUID userId = new CmsUUID(
                CmsRequestUtil.getNotEmptyDecodedParameter(wp.getJsp().getRequest(), A_CmsEditUserDialog.PARAM_USERID));
            try {
                CmsUser user = cms.readUser(userId);
                visible = OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(user.getOuFqn()));
                visible &= OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ELEMENT_AUTHOR);
            } catch (CmsException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return visible;
        } else if (getPath().indexOf("/users/edit/") > -1) {
            // check if the current user is the root administrator
            if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN) && !PATH_UNLOCK.equals(getPath())) {
                return true;
            }

            if (PATH_KILL_SESSIONS.equals(getPath())) {
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER);
            }
            CmsUUID userId = new CmsUUID(
                CmsRequestUtil.getNotEmptyDecodedParameter(wp.getJsp().getRequest(), A_CmsEditUserDialog.PARAM_USERID));
            try {
                CmsUser user = cms.readUser(userId);
                if (PATH_UNLOCK.equals(getPath())) {
                    return OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER)
                        && OpenCms.getLoginManager().isUserLocked(user);
                }

                // check if the user to change is root administrator
                if (OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ROOT_ADMIN)) {
                    return false;
                }
                // check if the current user is an administrator
                if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR)) {
                    return true;
                }
                // check if the user to change is an administrator
                return !OpenCms.getRoleManager().hasRole(cms, user.getName(), CmsRole.ADMINISTRATOR);
            } catch (CmsException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return false;
        } else if (getLink().equals(getPath(USERROLE_FILE)) || getLink().equals(getPath(GROUP_USERS_FILE))) {
            String userId = CmsRequestUtil.getNotEmptyDecodedParameter(
                wp.getJsp().getRequest(),
                A_CmsEditUserDialog.PARAM_USERID);
            if (userId == null) {
                return false;
            }
            try {
                return !cms.readUser(new CmsUUID(userId)).isWebuser();
            } catch (Exception e) {
                // ignore
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        } else if (getLink().equals(getPath(ACCMAN_FILE))) {
            return m_webuserOu;
        }
        return true;
    }

    /**
     * Returns the path to the jsp.<p>
     *
     * @param jspName the jsp name
     *
     * @return the full path
     */
    protected String getPath(String jspName) {

        return "/system/workplace/admin/accounts/" + jspName;
    }

    /**
     * Returns the visibility flag module parameter value.<p>
     *
     * @return the visibility flag module parameter value
     */
    protected String getVisibilityFlag() {

        CmsModule module = OpenCms.getModuleManager().getModule(this.getClass().getPackage().getName());
        if (module == null) {
            return VISIBILITY_ALL;
        }
        return module.getParameter(PARAM_VISIBILITY_FLAG, VISIBILITY_ALL);
    }
}
