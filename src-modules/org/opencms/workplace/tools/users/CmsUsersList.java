/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/users/Attic/CmsUsersList.java,v $
 * Date   : $Date: 2005/05/31 12:52:06 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.users;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main user account management view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public class CmsUsersList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ACTIVATE = "action_activate";

    /** list action id constant. */
    public static final String LIST_ACTION_DEACTIVATE = "action_deactivate";

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "action_delete";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "action_edit";

    /** list action id constant. */
    public static final String LIST_ACTION_GROUPS = "action_groups";

    /** list action id constant. */
    public static final String LIST_ACTION_ROLES = "action_roles";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIVATE = "column_activate";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "column_delete";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "column_edit";

    /** list column id constant. */
    public static final String LIST_COLUMN_EMAIL = "column_email";

    /** list column id constant. */
    public static final String LIST_COLUMN_GROUPS = "column_groups";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTLOGIN = "column_lastlogin";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOGIN = "column_login";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "column_name";

    /** list column id constant. */
    public static final String LIST_COLUMN_ROLES = "column_roles";

    /** list action id constant. */
    public static final String LIST_DEFACTION_EDIT = "defaction_edit";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ADDRESS = "detail_address";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_GROUPS = "detail_groups";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ROLES = "detail_roles";

    /** list id constant. */
    public static final String LIST_ID = "users";

    /** list action id constant. */
    public static final String LIST_MACTION_ACTIVATE = "maction_activate";

    /** list action id constant. */
    public static final String LIST_MACTION_DEACTIVATE = "maction_deactivate";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/users/buttons/";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUsersList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_USERS_LIST_NAME_0), LIST_COLUMN_LOGIN, null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     * 
     * @throws CmsRuntimeException to signal that an action is not supported
     * 
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // execute the delete multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    getCms().deleteUser(new CmsUUID(listItem.getId()));
                    getList().removeItem(listItem.getId());
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_SELECTED_USERS_0), e);
            }
        } else if (getParamListAction().equals(LIST_MACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = getCms().readUser(usrName);
                    if (user.getDisabled()) {
                        user.setEnabled();
                        getCms().writeUser(user);
                    }
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_SELECTED_USERS_0), e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
        } else if (getParamListAction().equals(LIST_MACTION_DEACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = getCms().readUser(usrName);
                    if (!user.getDisabled()) {
                        user.setDisabled();
                        getCms().writeUser(user);
                    }
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DEACTIVATE_SELECTED_USERS_0), e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * This method should handle every defined list single action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     * 
     * @throws CmsRuntimeException to signal that an action is not supported or in case an action failed
     * 
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            String userName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();
            String userId = getSelectedItem().getId();
            try {
                // forward to the edit user screen
                Map params = new HashMap();
                params.put(CmsEditUserDialog.PARAM_USERNAME, userName);
                params.put(CmsEditUserDialog.PARAM_USERID, userId);
                // set action parameter to initial dialog call
                params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
                getToolManager().jspRedirectTool(this, "/users/edit", params);
            } catch (IOException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_EDIT_USER_0), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            String userName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();
            String userId = getSelectedItem().getId();
            try {
                // forward to the edit user screen
                Map params = new HashMap();
                params.put(CmsEditUserDialog.PARAM_USERNAME, userName);
                params.put(CmsEditUserDialog.PARAM_USERID, userId);
                // set action parameter to initial dialog call
                params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
                getToolManager().jspRedirectTool(this, "/users/edit/user", params);
            } catch (IOException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_EDIT_USER_0), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_GROUPS)) {
            String userName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();
            String userId = getSelectedItem().getId();
            try {
                // forward to the edit user screen
                Map params = new HashMap();
                params.put(CmsEditUserDialog.PARAM_USERNAME, userName);
                params.put(CmsEditUserDialog.PARAM_USERID, userId);
                // set action parameter to initial dialog call
                params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
                getToolManager().jspRedirectTool(this, "/users/edit/groups", params);
            } catch (IOException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_EDIT_GROUPS_0), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_ROLES)) {
            String userName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();
            String userId = getSelectedItem().getId();
            try {
                // forward to the edit user screen
                Map params = new HashMap();
                params.put(CmsEditUserDialog.PARAM_USERNAME, userName);
                params.put(CmsEditUserDialog.PARAM_USERID, userId);
                // set action parameter to initial dialog call
                params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
                getToolManager().jspRedirectTool(this, "/users/edit/roles", params);
            } catch (IOException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_EDIT_ROLES_0), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // execute the delete action
            CmsListItem listItem = getSelectedItem();
            String userName = listItem.get(LIST_COLUMN_LOGIN).toString();
            try {
                getCms().deleteUser(new CmsUUID(listItem.getId()));
                getList().removeItem(listItem.getId());
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_USER_1, userName), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            CmsListItem listItem = getSelectedItem();
            String usrName = "";
            try {
                usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                CmsUser user = getCms().readUser(usrName);
                user.setEnabled();
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_USER_1, usrName), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
            // execute the activate action
            CmsListItem listItem = getSelectedItem();
            String usrName = "";
            try {
                usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                CmsUser user = getCms().readUser(usrName);
                user.setDisabled();
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DEACTIVATE_USER_1, usrName), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List ret = new ArrayList();
        // get content
        try {
            List users = getCms().getUsers();
            Iterator itUsers = users.iterator();
            while (itUsers.hasNext()) {
                CmsUser user = (CmsUser)itUsers.next();
                CmsListItem item = getList().newItem(user.getId().toString());
                item.set(LIST_COLUMN_LOGIN, user.getName());
                item.set(LIST_COLUMN_NAME, user.getFullName());
                item.set(LIST_COLUMN_EMAIL, user.getEmail());
                item.set(LIST_COLUMN_LASTLOGIN, new Date(user.getLastlogin()));
                // address
                StringBuffer html = new StringBuffer(512);
                html.append(user.getAddress());
                if (user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_TOWN) != null) {
                    html.append("<br>");
                    if (user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_ZIPCODE) != null) {
                        html.append(user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_ZIPCODE));
                        html.append(" ");
                    }
                    html.append(user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_TOWN));
                }
                if (user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_COUNTRY) != null) {
                    html.append("<br>");
                    html.append(user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_COUNTRY));
                }
                item.set(LIST_DETAIL_ADDRESS, html.toString());
                // groups
                Iterator itGroups = getCms().getGroupsOfUser(user.getName()).iterator();
                html = new StringBuffer(512);
                while (itGroups.hasNext()) {
                    html.append(((CmsGroup)itGroups.next()).getName());
                    if (itGroups.hasNext()) {
                        html.append("<br>");
                    }
                    html.append("\n");
                }
                item.set(LIST_DETAIL_GROUPS, html.toString());
                // roles
                Iterator itRoles = getCms().getGroupsOfUser(user.getName()).iterator();
                html = new StringBuffer(512);
                while (itRoles.hasNext()) {
                    html.append(((CmsGroup)itRoles.next()).getName());
                    if (itGroups.hasNext()) {
                        html.append("<br>");
                    }
                    html.append("\n");
                }
                item.set(LIST_DETAIL_ROLES, html.toString());
                ret.add(item);
            }
        } catch (CmsException e) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_CREATE_LIST_1,
                this.getCms().getRequestContext().getUri()));
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // add edit action
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_HELP_0));
        editAction.setIconPath(PATH_BUTTONS + "edit.png");
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for group edition
        CmsListColumnDefinition groupCol = new CmsListColumnDefinition(LIST_COLUMN_GROUPS);
        groupCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_GROUPS_0));
        groupCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_GROUPS_HELP_0));
        groupCol.setWidth("20");
        groupCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        groupCol.setSorteable(false);
        // add groups action
        CmsListDirectAction groupAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_GROUPS);
        groupAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_GROUPS_NAME_0));
        groupAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_GROUPS_HELP_0));
        groupAction.setIconPath(PATH_BUTTONS + "groups.png");
        groupCol.addDirectAction(groupAction);
        // add it to the list definition
        metadata.addColumn(groupCol);

        // create column for role edition
        CmsListColumnDefinition roleCol = new CmsListColumnDefinition(LIST_COLUMN_ROLES);
        roleCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ROLES_0));
        roleCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ROLES_HELP_0));
        roleCol.setWidth("20");
        roleCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        roleCol.setSorteable(false);
        // add roles action
        CmsListDirectAction roleAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_ROLES);
        roleAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ROLES_NAME_0));
        roleAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ROLES_HELP_0));
        roleAction.setIconPath(PATH_BUTTONS + "roles.png");
        roleCol.addDirectAction(roleAction);
        // add it to the list definition
        metadata.addColumn(roleCol);

        // create column for activation/deactivation
        CmsListColumnDefinition actCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        actCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_0));
        actCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_HELP_0));
        actCol.setWidth("20");
        actCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        actCol.setListItemComparator(new CmsListItemActionIconComparator());
        // activate action
        CmsListDirectAction actAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_ACTIVATE);
        actAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_NAME_0));
        actAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_HELP_0));
        actAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_CONF_0));
        actAction.setIconPath(ICON_INACTIVE);
        // deactivate action
        CmsListDirectAction deactAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_DEACTIVATE);
        deactAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_NAME_0));
        deactAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_HELP_0));
        deactAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_CONF_0));
        deactAction.setIconPath(ICON_ACTIVE);
        // adds an activate/deactivate direct action
        CmsUserActivateAction userAction = new CmsUserActivateAction(LIST_ID, LIST_ACTION_ACTIVATE, getCms());
        userAction.setFirstAction(actAction);
        userAction.setSecondAction(deactAction);
        actCol.addDirectAction(userAction);
        // add it to the list definition
        metadata.addColumn(actCol);

        // create column for deletion
        CmsListColumnDefinition deleteCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        deleteCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_DELETE_0));
        deleteCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_DELETE_HELP_0));
        deleteCol.setWidth("20");
        deleteCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        deleteCol.setSorteable(false);
        // add delete action
        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_HELP_0));
        deleteAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_CONF_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
        // add it to the list definition
        metadata.addColumn(deleteCol);

        // create column for login
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN);
        loginCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LOGIN_0));
        loginCol.setWidth("20%");
        // create default edit action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_ID, LIST_DEFACTION_EDIT);
        defEditAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_HELP_0));
        loginCol.setDefaultAction(defEditAction);
        // add it to the list definition
        metadata.addColumn(loginCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_USERNAME_0));
        nameCol.setWidth("30%");
        metadata.addColumn(nameCol);

        // add column for email
        CmsListColumnDefinition emailCol = new CmsListColumnDefinition(LIST_COLUMN_EMAIL);
        emailCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_EMAIL_0));
        emailCol.setWidth("30%");
        metadata.addColumn(emailCol);

        // add column for last login date
        CmsListColumnDefinition lastLoginCol = new CmsListColumnDefinition(LIST_COLUMN_LASTLOGIN);
        lastLoginCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LASTLOGIN_0));
        lastLoginCol.setWidth("20%");
        CmsListDateMacroFormatter lastLoginFormatter = new CmsListDateMacroFormatter(Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_LASTLOGIN_FORMAT_1), Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_LASTLOGIN_NEVER_0));
        lastLoginCol.setFormatter(lastLoginFormatter);
        metadata.addColumn(lastLoginCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add user address details
        CmsListItemDetails userAddressDetails = new CmsListItemDetails(LIST_ID, LIST_DETAIL_ADDRESS);
        userAddressDetails.setAtColumn(LIST_COLUMN_LOGIN);
        userAddressDetails.setVisible(false);
        userAddressDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ADDRESS_NAME_0));
        userAddressDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ADDRESS_HELP_0));
        userAddressDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ADDRESS_NAME_0));
        userAddressDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ADDRESS_HELP_0));
        userAddressDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_ADDRESS_NAME_0));
        userAddressDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_ADDRESS_NAME_0)));
        metadata.addItemDetails(userAddressDetails);

        // add user groups details
        CmsListItemDetails userGroupsDetails = new CmsListItemDetails(LIST_ID, LIST_DETAIL_GROUPS);
        userGroupsDetails.setAtColumn(LIST_COLUMN_LOGIN);
        userGroupsDetails.setVisible(true);
        userGroupsDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_NAME_0));
        userGroupsDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_HELP_0));
        userGroupsDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_NAME_0));
        userGroupsDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_HELP_0));
        userGroupsDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_GROUPS_NAME_0));
        userGroupsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_GROUPS_NAME_0)));
        metadata.addItemDetails(userGroupsDetails);

        // add user roles details
        CmsListItemDetails userRolesDetails = new CmsListItemDetails(LIST_ID, LIST_DETAIL_ROLES);
        userRolesDetails.setAtColumn(LIST_COLUMN_LOGIN);
        userRolesDetails.setVisible(true);
        userRolesDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ROLES_NAME_0));
        userRolesDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ROLES_HELP_0));
        userRolesDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ROLES_NAME_0));
        userRolesDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ROLES_HELP_0));
        userRolesDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_ROLES_NAME_0));
        userRolesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_ROLES_NAME_0)));
        metadata.addItemDetails(userRolesDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(
            LIST_ID,
            metadata.getColumnDefinition(LIST_COLUMN_LOGIN));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_NAME));
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add delete multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_ID, LIST_ACTION_DELETE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_HELP_0));
        deleteMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_DELETE);
        metadata.addMultiAction(deleteMultiAction);

        // add the activate user multi action
        CmsListMultiAction activateUser = new CmsListMultiAction(LIST_ID, LIST_MACTION_ACTIVATE);
        activateUser.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ACTIVATE_NAME_0));
        activateUser.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ACTIVATE_HELP_0));
        activateUser.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ACTIVATE_CONF_0));
        activateUser.setIconPath(ICON_MULTI_ACTIVATE);
        metadata.addMultiAction(activateUser);

        // add the activate user multi action
        CmsListMultiAction deactivateUser = new CmsListMultiAction(LIST_ID, LIST_MACTION_DEACTIVATE);
        deactivateUser.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DEACTIVATE_NAME_0));
        deactivateUser.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DEACTIVATE_HELP_0));
        deactivateUser.setConfirmationMessage(Messages.get().container(
            Messages.GUI_USERS_LIST_MACTION_DEACTIVATE_CONF_0));
        deactivateUser.setIconPath(ICON_MULTI_DEACTIVATE);
        metadata.addMultiAction(deactivateUser);
    }

}
