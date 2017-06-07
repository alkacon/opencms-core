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

import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Roles overview view.<p>
 *
 * @since 6.5.6
 */
public class CmsRolesList extends A_CmsRolesList {

    /** list action id constant. */
    public static final String LIST_DEFACTION_OVERVIEW = "do";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_USERS = "du";

    /** list id constant. */
    public static final String LIST_ID = "lsrs";

    /** Request parameter name for the role name. */
    public static final String PARAM_ROLE = "role";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRolesList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     */
    public CmsRolesList(CmsJspActionElement jsp, String listId) {

        this(jsp, listId, Messages.get().container(Messages.GUI_ROLEEDIT_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRolesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     */
    protected CmsRolesList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        String roleName = getSelectedItem().get(LIST_COLUMN_GROUP_NAME).toString();
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {getParamOufqn()});
        params.put(PARAM_ROLE, new String[] {roleName});
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
        if (getParamListAction().equals(LIST_ACTION_ICON)) {
            try {
                if (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.valueOf(getCms().readGroup(roleName)))) {
                    // forward to the edit user screen
                    getToolManager().jspForwardTool(this, getCurrentToolPath() + "/overview/edit", params);
                } else {
                    getToolManager().jspForwardTool(this, getCurrentToolPath() + "/overview", params);
                }
            } catch (CmsException e) {
                // noop
            }
        } else if (getParamListAction().equals(LIST_DEFACTION_OVERVIEW)) {
            // forward to the overview screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/overview", params);
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * Returns the path of the edit icon.<p>
     *
     * @return the path of the edit icon
     */
    public String getEditIcon() {

        return PATH_BUTTONS + "user.png";
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#getIconPath(org.opencms.workplace.list.CmsListItem)
     */
    @Override
    public String getIconPath(CmsListItem item) {

        return PATH_BUTTONS + "role.png";
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        if (!detailId.equals(LIST_DETAIL_USERS)) {
            super.fillDetails(detailId);
            return;
        }
        // get content
        List<CmsListItem> roles = getList().getAllContent();
        Iterator<CmsListItem> itRoles = roles.iterator();
        while (itRoles.hasNext()) {
            CmsListItem item = itRoles.next();
            String roleName = item.get(LIST_COLUMN_GROUP_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_USERS)) {
                    CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
                    List<CmsUser> users = OpenCms.getRoleManager().getUsersOfRole(getCms(), role, true, true);
                    Iterator<CmsUser> itUsers = users.iterator();
                    while (itUsers.hasNext()) {
                        CmsUser user = itUsers.next();
                        if (user.getOuFqn().equals(getParamOufqn())) {
                            html.append(user.getSimpleName());
                        } else {
                            html.append(user.getDisplayName(getCms(), getLocale()));
                        }
                        if (itUsers.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                // noop
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#getRoles()
     */
    @Override
    protected List<CmsRole> getRoles() throws CmsException {

        List<CmsRole> roles = new ArrayList<CmsRole>(
            OpenCms.getRoleManager().getRoles(getCms(), getParamOufqn(), false));
        // ensure the role sorting matches the system roles order
        CmsRole.applySystemRoleOrder(roles);
        return roles;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#includeOuDetails()
     */
    @Override
    protected boolean includeOuDetails() {

        return false;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        // create default overview action
        CmsListDefaultAction defOverviewAction = new CmsListDefaultAction(LIST_DEFACTION_OVERVIEW);
        defOverviewAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_DEFACTION_OVERVIEW_NAME_0));
        defOverviewAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_DEFACTION_OVERVIEW_HELP_0));
        metadata.getColumnDefinition(LIST_COLUMN_NAME).addDefaultAction(defOverviewAction);

        // activate icon action and set a more descriptive help text
        metadata.getColumnDefinition(LIST_COLUMN_ICON).getDirectAction(LIST_ACTION_ICON).setEnabled(true);
        metadata.getColumnDefinition(LIST_COLUMN_ICON).setHelpText(
            Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_EDIT_HELP_0));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        super.setIndependentActions(metadata);

        // add users details
        CmsListItemDetails usersDetails = new CmsListItemDetails(LIST_DETAIL_USERS);
        usersDetails.setAtColumn(LIST_COLUMN_NAME);
        usersDetails.setVisible(false);
        usersDetails.setShowActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_USERS_NAME_0));
        usersDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_USERS_HELP_0));
        usersDetails.setHideActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_USERS_NAME_0));
        usersDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_USERS_HELP_0));
        usersDetails.setName(Messages.get().container(Messages.GUI_ROLES_DETAIL_USERS_NAME_0));
        usersDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_ROLES_DETAIL_USERS_NAME_0)));
        metadata.addItemDetails(usersDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }
}
