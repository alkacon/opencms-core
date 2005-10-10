/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/A_CmsUsersList.java,v $
 * Date   : $Date: 2005/10/10 16:11:03 $
 * Version: $Revision: 1.2 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

/**
 * Main user account management view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsUsersList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ACTIVATE = "aa";

    /** list action id constant. */
    public static final String LIST_ACTION_DEACTIVATE = "ac";

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "ad";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_GROUPS = "ag";

    /** list action id constant. */
    public static final String LIST_ACTION_ROLES = "ar";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIVATE = "ca";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_EMAIL = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_GROUPS = "cg";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTLOGIN = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOGIN = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ROLES = "cr";

    /** list action id constant. */
    public static final String LIST_DEFACTION_EDIT = "de";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ADDRESS = "da";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_GROUPS = "dg";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ROLES = "dr";

    /** list action id constant. */
    public static final String LIST_MACTION_ACTIVATE = "ma";

    /** list action id constant. */
    public static final String LIST_MACTION_DEACTIVATE = "mc";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETE = "md";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** a set of action id's to use for deletion. */
    private static Set m_deleteActionIds = new HashSet();

    /** a set of action id's to use for edition. */
    private static Set m_editActionIds = new HashSet();

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public A_CmsUsersList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName, LIST_COLUMN_LOGIN, CmsListOrderEnum.ORDER_ASCENDING, null);
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

        if (getParamListAction().equals(LIST_MACTION_DELETE)) {
            // execute the delete multiaction
            Map params = new HashMap();
            params.put(A_CmsEditUserDialog.PARAM_USERID, getParamSelItems());
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

            try {
                getToolManager().jspForwardTool(this, getCurrentToolPath() + "/delete", params);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_SELECTED_USERS_0), e);
            }
        } else if (getParamListAction().equals(LIST_MACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = readUser(usrName);
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
                    CmsUser user = readUser(usrName);
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
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        String userId = getSelectedItem().getId();
        String userName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();

        Map params = new HashMap();
        params.put(A_CmsEditUserDialog.PARAM_USERID, userId);
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit", params);
        } else if (m_editActionIds.contains(getParamListAction())) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/user", params);
        } else if (getParamListAction().equals(LIST_ACTION_GROUPS)) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/groups", params);
        } else if (getParamListAction().equals(LIST_ACTION_ROLES)) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/roles", params);
        } else if (m_deleteActionIds.contains(getParamListAction())) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/delete", params);
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            try {
                CmsUser user = readUser(userName);
                user.setEnabled();
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_USER_1, userName), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
            // execute the activate action
            try {
                CmsUser user = readUser(userName);
                user.setDisabled();
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DEACTIVATE_USER_1, userName), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // get content
        List users = getList().getAllContent();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsListItem item = (CmsListItem)itUsers.next();
            String userName = item.get(LIST_COLUMN_LOGIN).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_ADDRESS)) {
                    CmsUser user = readUser(userName);
                    // address
                    html.append(user.getAddress());
                    if (user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_TOWN) != null) {
                        html.append("<br>");
                        if (user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ZIPCODE) != null) {
                            html.append(user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ZIPCODE));
                            html.append(" ");
                        }
                        html.append(user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_TOWN));
                    }
                    if (user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_COUNTRY) != null) {
                        html.append("<br>");
                        html.append(user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_COUNTRY));
                    }
                } else if (detailId.equals(LIST_DETAIL_GROUPS)) {
                    // groups
                    Iterator itGroups = getCms().getGroupsOfUser(userName).iterator();
                    while (itGroups.hasNext()) {
                        html.append(((CmsGroup)itGroups.next()).getName());
                        if (itGroups.hasNext()) {
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
     * Returns the path the group icon.<p>
     * 
     * @return the path to the group icon
     */
    protected abstract String getGroupIcon();

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();
        // get content
        List users = getUsers();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsUser user = (CmsUser)itUsers.next();
            CmsListItem item = getList().newItem(user.getId().toString());
            item.set(LIST_COLUMN_LOGIN, user.getName());
            item.set(LIST_COLUMN_NAME, user.getFullName());
            item.set(LIST_COLUMN_EMAIL, user.getEmail());
            item.set(LIST_COLUMN_LASTLOGIN, new Date(user.getLastlogin()));
            ret.add(item);
        }
        return ret;
    }

    /**
     * Returns a list of users.<p>
     * 
     * @return the list of all users
     * 
     * @throws CmsException if something goes wrong
     */
    protected abstract List getUsers() throws CmsException;

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
     * Reads the user.<p>
     * 
     * @param name the name of the user to read
     * 
     * @return the user
     * 
     * @throws CmsException if something goes wrong
     */
    protected abstract CmsUser readUser(String name) throws CmsException;

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
        setEditAction(editCol);
        m_editActionIds.addAll(editCol.getDirectActionIds());
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
        CmsListDirectAction groupAction = new CmsListDirectAction(LIST_ACTION_GROUPS);
        groupAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_GROUPS_NAME_0));
        groupAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_GROUPS_HELP_0));
        groupAction.setIconPath(getGroupIcon());
        groupCol.addDirectAction(groupAction);
        // add it to the list definition
        metadata.addColumn(groupCol);

        // create column for activation/deactivation
        CmsListColumnDefinition actCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        actCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_0));
        actCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_HELP_0));
        actCol.setWidth("20");
        actCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        actCol.setListItemComparator(new CmsListItemActionIconComparator());

        // activate action
        CmsListDirectAction actAction = new CmsListDirectAction(LIST_ACTION_ACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                if (getItem() != null) {
                    String usrId = getItem().getId();
                    try {
                        return getCms().readUser(new CmsUUID(usrId)).getDisabled();
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        actAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_NAME_0));
        actAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_HELP_0));
        actAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_CONF_0));
        actAction.setIconPath(ICON_INACTIVE);
        actCol.addDirectAction(actAction);

        // deactivate action
        CmsListDirectAction deactAction = new CmsListDirectAction(LIST_ACTION_DEACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                if (getItem() != null) {
                    String usrId = getItem().getId();
                    try {
                        return getCms().readUser(new CmsUUID(usrId)).isEnabled();
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        deactAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_NAME_0));
        deactAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_HELP_0));
        deactAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_CONF_0));
        deactAction.setIconPath(ICON_ACTIVE);
        actCol.addDirectAction(deactAction);

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
        setDeleteAction(deleteCol);
        m_deleteActionIds.addAll(deleteCol.getDirectActionIds());

        // add it to the list definition
        metadata.addColumn(deleteCol);

        // create column for login
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN);
        loginCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LOGIN_0));
        loginCol.setWidth("20%");

        // create default edit action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_DEFACTION_EDIT);
        defEditAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_HELP_0));
        loginCol.addDefaultAction(defEditAction);

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
     * Sets the needed delete action(s).<p>
     * 
     * @param deleteCol the list column for deletion.
     */
    protected abstract void setDeleteAction(CmsListColumnDefinition deleteCol);

    /**
     * Sets the needed edit action(s).<p>
     * 
     * @param editCol the list column for edition.
     */
    protected abstract void setEditAction(CmsListColumnDefinition editCol);

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add user address details
        CmsListItemDetails userAddressDetails = new CmsListItemDetails(LIST_DETAIL_ADDRESS);
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
        CmsListItemDetails userGroupsDetails = new CmsListItemDetails(LIST_DETAIL_GROUPS);
        userGroupsDetails.setAtColumn(LIST_COLUMN_LOGIN);
        userGroupsDetails.setVisible(false);
        userGroupsDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_NAME_0));
        userGroupsDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_HELP_0));
        userGroupsDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_NAME_0));
        userGroupsDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_HELP_0));
        userGroupsDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_GROUPS_NAME_0));
        userGroupsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_GROUPS_NAME_0)));
        metadata.addItemDetails(userGroupsDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_LOGIN));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_NAME));
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add delete multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_HELP_0));
        deleteMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_DELETE);
        metadata.addMultiAction(deleteMultiAction);

        // add the activate user multi action
        CmsListMultiAction activateUser = new CmsListMultiAction(LIST_MACTION_ACTIVATE);
        activateUser.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ACTIVATE_NAME_0));
        activateUser.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ACTIVATE_HELP_0));
        activateUser.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ACTIVATE_CONF_0));
        activateUser.setIconPath(ICON_MULTI_ACTIVATE);
        metadata.addMultiAction(activateUser);

        // add the deactivate user multi action
        CmsListMultiAction deactivateUser = new CmsListMultiAction(LIST_MACTION_DEACTIVATE);
        deactivateUser.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DEACTIVATE_NAME_0));
        deactivateUser.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DEACTIVATE_HELP_0));
        deactivateUser.setConfirmationMessage(Messages.get().container(
            Messages.GUI_USERS_LIST_MACTION_DEACTIVATE_CONF_0));
        deactivateUser.setIconPath(ICON_MULTI_DEACTIVATE);
        metadata.addMultiAction(deactivateUser);
    }
}