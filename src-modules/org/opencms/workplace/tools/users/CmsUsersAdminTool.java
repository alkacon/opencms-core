/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/users/Attic/CmsUsersAdminTool.java,v $
 * Date   : $Date: 2005/05/20 11:47:11 $
 * Version: $Revision: 1.20 $
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
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDefaultComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.I_CmsListAction;

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
 * @version $Revision: 1.20 $
 * @since 5.7.3
 */
public class CmsUsersAdminTool extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ACTIVATE = "activate";

    /** list action id constant. */
    public static final String LIST_ACTION_DEACTIVATE = "deactivate";

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "delete";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "edit";

    /** list action id constant. */
    public static final String LIST_ACTION_MACTIVATE = "mactivate";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIONS = "actions";

    /** list column id constant. */
    public static final String LIST_COLUMN_EMAIL = "email";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTLOGIN = "lastlogin";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOGIN = "login";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "name";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ADDRESS = "address";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_GROUPS = "groups";

    /** list id constant. */
    public static final String LIST_ID = "users";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUsersAdminTool(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_USERS_LIST_NAME_0),
            LIST_COLUMN_LOGIN,
            LIST_COLUMN_LOGIN);
    }

    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersAdminTool(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
                throw new RuntimeException(e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_MACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = getCms().readUser(usrName);
                    if (user.getDisabled()) {
                        user.setEnabled();
                    } else {
                        user.setDisabled();
                    }
                    getCms().writeUser(user);
                }
            } catch (CmsException e) {
                throw new RuntimeException(e);
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
     * @throws CmsRuntimeException to signal that an action is not supported
     * 
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            String usrName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();
            try {
                // forward to the edit user screen
                Map params = new HashMap();
                params.put("usrName", usrName);
                getToolManager().jspRedirectTool(this, "/users/edit", params);

            } catch (IOException e) {
                // should never happen
                throw new RuntimeException(e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            CmsListItem listItem = getSelectedItem();
            try {
                String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                CmsUser user = getCms().readUser(usrName);
                user.setEnabled();
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new RuntimeException(e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
            // execute the activate action
            CmsListItem listItem = getSelectedItem();
            try {
                String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                CmsUser user = getCms().readUser(usrName);
                user.setDisabled();
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new RuntimeException(e);
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
                item.set(LIST_DETAIL_ADDRESS, html);
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
                ret.add(item);
            }
        } catch (CmsException e) {
            throw new RuntimeException(e);
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
    
        // add column for direct actions
        CmsListColumnDefinition actionsCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIONS, Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_ACTIONS_0), "", // no width
            CmsListColumnAlignEnum.ALIGN_CENTER);
        actionsCol.setListItemComparator(new CmsListItemActionIconComparator());
        actionsCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIONS_HELP_0));
        // adds an activate/deactivate user action
        CmsUserActivateDeactivateAction userAction = new CmsUserActivateDeactivateAction(
            LIST_ID,
            LIST_ACTION_ACTIVATE,
            getCms());
        CmsListDirectAction userActAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_ACTIVATE);
        userActAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_NAME_0));
        userActAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_CONF_0));
        userActAction.setIconPath("tools/users/buttons/activate.gif");
        userActAction.setEnabled(true);
        userActAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_HELP_0));
        userAction.setFirstAction(userActAction);
        CmsListDirectAction userDeactAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_DEACTIVATE);
        userDeactAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_NAME_0));
        userDeactAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_CONF_0));
        userDeactAction.setIconPath("tools/users/buttons/deactivate.gif");
        userDeactAction.setEnabled(true);
        userDeactAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_HELP_0));
        userAction.setSecondAction(userDeactAction);
        actionsCol.addDirectAction(userAction);

        metadata.addColumn(actionsCol);

        // add column for login and default action
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN, Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_LOGIN_0), "", // no width
            CmsListColumnAlignEnum.ALIGN_LEFT);
        loginCol.setListItemComparator(new CmsListItemDefaultComparator());
        loginCol.setDefaultAction(new CmsListDefaultAction(LIST_ID, LIST_ACTION_EDIT, Messages.get().container(
            Messages.GUI_USERS_LIST_ACTION_EDITUSER_NAME_0), Messages.get().container(
            Messages.GUI_USERS_LIST_ACTION_EDITUSER_HELP_0), null, true, // enabled
            Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDITUSER_CONF_0)));
        metadata.addColumn(loginCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME, Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_USERNAME_0), "", // no width
            CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setListItemComparator(new CmsListItemDefaultComparator());
        metadata.addColumn(nameCol);

        // add column for email
        CmsListColumnDefinition emailCol = new CmsListColumnDefinition(LIST_COLUMN_EMAIL, Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_EMAIL_0), "", // no width
            CmsListColumnAlignEnum.ALIGN_LEFT);
        emailCol.setListItemComparator(new CmsListItemDefaultComparator());
        metadata.addColumn(emailCol);

        // add column for last login date
        CmsListColumnDefinition lastLoginCol = new CmsListColumnDefinition(
            LIST_COLUMN_LASTLOGIN,
            Messages.get().container(Messages.GUI_USERS_LIST_COLS_LASTLOGIN_0),
            "", // no width
            CmsListColumnAlignEnum.ALIGN_LEFT);
        lastLoginCol.setListItemComparator(new CmsListItemDefaultComparator());
        lastLoginCol.setFormatter(new CmsListDateMacroFormatter(Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_LASTLOGIN_FORMAT_1), Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_LASTLOGIN_NEVER_0)));
        metadata.addColumn(lastLoginCol);
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add user address details
        I_CmsListAction showAddressAction = new CmsListIndependentAction(
            LIST_ID,
            LIST_DETAIL_ADDRESS,
            Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ADDRESS_NAME_0),
            Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ADDRESS_HELP_0),
            "tools/users/buttons/address.gif",
            true,
            null);
        I_CmsListAction hideAddressAction = new CmsListIndependentAction(
            LIST_ID,
            LIST_DETAIL_ADDRESS,
            Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ADDRESS_NAME_0),
            Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ADDRESS_HELP_0),
            "tools/users/buttons/address.gif",
            true,
            null);
        CmsListItemDetails userAddressDetails = new CmsListItemDetails(
            LIST_DETAIL_ADDRESS,
            LIST_COLUMN_LOGIN,
            false,
            showAddressAction,
            hideAddressAction);
        userAddressDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_ADDRESS_FORMAT_0)));
        metadata.addItemDetails(userAddressDetails);

        // add user groups details
        I_CmsListAction showGroupsAction = new CmsListIndependentAction(
            LIST_ID,
            LIST_DETAIL_GROUPS,
            Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_NAME_0),
            Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_HELP_0),
            "tools/users/buttons/groups.gif",
            true,
            null);
        I_CmsListAction hideGroupsAction = new CmsListIndependentAction(
            LIST_ID,
            LIST_DETAIL_GROUPS,
            Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_NAME_0),
            Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_HELP_0),
            "tools/users/buttons/groups.gif",
            true,
            null);

        CmsListItemDetails userGroupsDetails = new CmsListItemDetails(
            LIST_DETAIL_GROUPS,
            LIST_COLUMN_LOGIN,
            true,
            showGroupsAction,
            hideGroupsAction);
        userGroupsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_GROUPS_FORMAT_0)));
        metadata.addItemDetails(userGroupsDetails);

        // adds a reload button
        metadata.addIndependentAction(CmsListIndependentAction.getDefaultRefreshListAction(LIST_ID));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add multi actions
        metadata.addMultiAction(new CmsListMultiAction(LIST_ID, LIST_ACTION_DELETE, Messages.get().container(
            Messages.GUI_USERS_LIST_ACTION_DELETE_NAME_0), "list/delete.gif", Messages.get().container(
            Messages.GUI_USERS_LIST_ACTION_DELETE_HELP_0), true, Messages.get().container(
            Messages.GUI_USERS_LIST_ACTION_DELETE_CONF_0)));

        // add the activate user multi action
        CmsListMultiAction activateUser = new CmsListMultiAction(LIST_ID, LIST_ACTION_MACTIVATE);
        activateUser.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_MACTIVATE_NAME_0));
        activateUser.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_MACTIVATE_CONF_0));
        activateUser.setIconPath("tools/users/buttons/multi_activate.gif");
        activateUser.setEnabled(true);
        activateUser.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_MACTIVATE_HELP_0));
        metadata.addMultiAction(activateUser);
    }

                    }
