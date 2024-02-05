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
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
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
import org.opencms.workplace.list.I_CmsListFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

/**
 * Main user account management view.<p>
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
    public static final String LIST_ACTION_ROLE = "ar";

    /** list action id constant. */
    public static final String LIST_ACTION_ROLES = "ar";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIVATE = "ca";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_DISPLAY = "cdn";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_EMAIL = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_ENABLED = "ceb";

    /** list column id constant. */
    public static final String LIST_COLUMN_GROUPS = "cg";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTLOGIN = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOGIN = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list action id constant. */
    public static final String LIST_COLUMN_ROLE = "cr";

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
    private static Set<String> m_deleteActionIds = new HashSet<String>();

    /** a set of action id's to use for edition. */
    private static Set<String> m_editActionIds = new HashSet<String>();

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public A_CmsUsersList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName, LIST_COLUMN_DISPLAY, CmsListOrderEnum.ORDER_ASCENDING, null, false);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     * @param lazy the lazy flag
     */
    public A_CmsUsersList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName, boolean lazy) {

        super(jsp, listId, listName, LIST_COLUMN_DISPLAY, CmsListOrderEnum.ORDER_ASCENDING, null, lazy);
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id
     * of the action to execute.<p>
     *
     * @throws CmsRuntimeException to signal that an action is not supported
     *
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_DELETE)) {
            // execute the delete multiaction
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(A_CmsEditUserDialog.PARAM_USERID, new String[] {getParamSelItems()});
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});

            try {
                getToolManager().jspForwardTool(this, getCurrentToolPath() + "/delete", params);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_SELECTED_USERS_0), e);
            }
        } else if (getParamListAction().equals(LIST_MACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator<CmsListItem> itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = itItems.next();
                    String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = readUser(usrName);
                    if (!user.isEnabled()) {
                        user.setEnabled(true);
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
                Iterator<CmsListItem> itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = itItems.next();
                    String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = readUser(usrName);
                    if (user.isEnabled()) {
                        user.setEnabled(false);
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
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        String userId = getSelectedItem().getId();
        String userName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsEditUserDialog.PARAM_USERID, new String[] {userId});
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {getParamOufqn()});
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});

        if (getParamListAction().equals(LIST_ACTION_ROLE)) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/role", params);
        } else if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit", params);
        } else if (m_editActionIds.contains(getParamListAction())) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/user", params);
        } else if (getParamListAction().equals(LIST_ACTION_GROUPS)) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/groups", params);
        } else if (m_deleteActionIds.contains(getParamListAction())) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/delete", params);
        } else if (getParamListAction().startsWith(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            try {
                CmsUser user = readUser(userName);
                user.setEnabled(true);
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_USER_1, userName), e);
            }
        } else if (getParamListAction().startsWith(LIST_ACTION_DEACTIVATE)) {
            // execute the activate action
            try {
                CmsUser user = readUser(userName);
                user.setEnabled(false);
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
     * Returns the organizational unit fqn parameter value.<p>
     *
     * @return the organizational unit fqn parameter value
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Sets the organizational unit fqn parameter value.<p>
     *
     * @param ouFqn the organizational unit fqn parameter value
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List<CmsListItem> users = getList().getAllContent();
        Iterator<CmsListItem> itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsListItem item = itUsers.next();
            String userName = item.get(LIST_COLUMN_LOGIN).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_ADDRESS)) {
                    CmsUser user = readUser(userName);
                    // address
                    html.append(user.getAddress());
                    if (user.getCity() != null) {
                        html.append("<br>");
                        if (user.getZipcode() != null) {
                            html.append(user.getZipcode());
                            html.append(" ");
                        }
                        html.append(user.getCity());
                    }
                    if (user.getCountry() != null) {
                        html.append("<br>");
                        html.append(user.getCountry());
                    }
                } else if (detailId.equals(LIST_DETAIL_GROUPS)) {
                    // groups
                    List<CmsGroup> groups = getCms().getGroupsOfUser(userName, true, true);
                    Iterator<CmsGroup> itGroups = groups.iterator();
                    while (itGroups.hasNext()) {
                        CmsGroup group = itGroups.next();
                        if (group.getOuFqn().equals(getParamOufqn())) {
                            html.append(OpenCms.getWorkplaceManager().translateGroupName(group.getName(), false));
                        } else {
                            html.append(
                                group.getDisplayName(
                                    getCms(),
                                    getLocale(),
                                    OpenCms.getWorkplaceManager().getGroupNameTranslation()));
                        }
                        if (itGroups.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_ROLES)) {
                    // roles
                    boolean otherOuRole = false;
                    List<CmsRole> roles = OpenCms.getRoleManager().getRolesOfUser(
                        getCms(),
                        userName,
                        "/",
                        true,
                        true,
                        false);
                    Iterator<CmsRole> itRoles = roles.iterator();
                    while (itRoles.hasNext()) {
                        CmsRole role = itRoles.next();
                        if (!role.getOuFqn().equals(getParamOufqn())) {
                            otherOuRole = true;
                            break;
                        }
                    }
                    itRoles = roles.iterator();
                    while (itRoles.hasNext()) {
                        CmsRole role = itRoles.next();
                        if (!otherOuRole) {
                            html.append(role.getName(getLocale()));
                        } else {
                            html.append(role.getDisplayName(getCms(), getLocale()));
                        }
                        if (itRoles.hasNext()) {
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
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        // get content
        List<CmsUser> users = getUsers();
        Iterator<CmsUser> itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsUser user = itUsers.next();
            CmsListItem item = makeListItemForUser(user);
            ret.add(item);
        }
        CmsListColumnDefinition colDef = getList().getMetadata().getColumnDefinition(LIST_COLUMN_ROLE);
        if (colDef != null) {
            colDef.setVisible(
                !OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).hasFlagWebuser());
        }
        return ret;
    }

    /**
     * Returns the path the role edit icon.<p>
     *
     * @return the path to the role edit icon
     */
    protected String getRoleIcon() {

        return PATH_BUTTONS + "role.png";
    }

    /**
     * Returns a list of users.<p>
     *
     * @return the list of all users
     *
     * @throws CmsException if something goes wrong
     */
    protected abstract List<CmsUser> getUsers() throws CmsException;

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Makes a list item for a given user.<p>
     *
     * @param user the user
     *
     * @return the list item
     */
    protected CmsListItem makeListItemForUser(CmsUser user) {

        CmsListItem item = getList().newItem(user.getId().toString());
        setUserData(user, item);
        return item;
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
    @Override
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

        // create column for edit role
        CmsListColumnDefinition roleCol = new CmsListColumnDefinition(LIST_COLUMN_ROLE);
        roleCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ROLE_0));
        roleCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ROLE_HELP_0));
        roleCol.setWidth("20");
        roleCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        roleCol.setSorteable(false);
        // add role action
        CmsListDirectAction roleAction = new CmsListDirectAction(LIST_ACTION_ROLE);
        roleAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ROLE_NAME_0));
        roleAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ROLE_HELP_0));
        roleAction.setIconPath(getRoleIcon());
        roleCol.addDirectAction(roleAction);
        // add it to the list definition
        metadata.addColumn(roleCol);

        CmsListDirectAction enableAction = createActivateAction(LIST_ACTION_ACTIVATE);
        CmsListDirectAction deactAction = createDeactivateAction(LIST_ACTION_DEACTIVATE);
        addActivateColumn(metadata, enableAction, deactAction);

        CmsListDirectAction enableAction2 = createActivateAction(LIST_ACTION_ACTIVATE + "_2");
        CmsListDirectAction deactAction2 = createDeactivateAction(LIST_ACTION_DEACTIVATE + "_2");
        addEnabledColumn(metadata, enableAction2, deactAction2);

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
        metadata.addColumn(loginCol);
        loginCol.setVisible(false);

        // create column for display name
        CmsListColumnDefinition displayCol = new CmsListColumnDefinition(LIST_COLUMN_DISPLAY);
        displayCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LOGIN_0));
        displayCol.setWidth("20%");

        // create default edit action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_DEFACTION_EDIT);
        defEditAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_HELP_0));
        displayCol.addDefaultAction(defEditAction);

        // add it to the list definition
        metadata.addColumn(displayCol);

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
        lastLoginCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
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
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add user address details
        CmsListItemDetails userAddressDetails = new CmsListItemDetails(LIST_DETAIL_ADDRESS);
        userAddressDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        userAddressDetails.setVisible(false);
        userAddressDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ADDRESS_NAME_0));
        userAddressDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ADDRESS_HELP_0));
        userAddressDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ADDRESS_NAME_0));
        userAddressDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ADDRESS_HELP_0));
        userAddressDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_ADDRESS_NAME_0));
        userAddressDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_USERS_DETAIL_ADDRESS_NAME_0)));
        metadata.addItemDetails(userAddressDetails);

        // add user groups details
        CmsListItemDetails userGroupsDetails = new CmsListItemDetails(LIST_DETAIL_GROUPS);
        userGroupsDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        userGroupsDetails.setVisible(false);
        userGroupsDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_NAME_0));
        userGroupsDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_HELP_0));
        userGroupsDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_NAME_0));
        userGroupsDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_HELP_0));
        userGroupsDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_GROUPS_NAME_0));
        userGroupsDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_USERS_DETAIL_GROUPS_NAME_0)));
        metadata.addItemDetails(userGroupsDetails);

        // add user roles details
        CmsListItemDetails userRolesDetails = new CmsListItemDetails(LIST_DETAIL_ROLES);
        userRolesDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        userRolesDetails.setVisible(false);
        userRolesDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ROLES_NAME_0));
        userRolesDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ROLES_HELP_0));
        userRolesDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ROLES_NAME_0));
        userRolesDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ROLES_HELP_0));
        userRolesDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_ROLES_NAME_0));
        userRolesDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_USERS_DETAIL_ROLES_NAME_0)));
        metadata.addItemDetails(userRolesDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_DISPLAY));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_NAME));
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add delete multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_HELP_0));
        deleteMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DELETE_CONF_0));
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
        deactivateUser.setConfirmationMessage(
            Messages.get().container(Messages.GUI_USERS_LIST_MACTION_DEACTIVATE_CONF_0));
        deactivateUser.setIconPath(ICON_MULTI_DEACTIVATE);
        metadata.addMultiAction(deactivateUser);
    }

    /**
     * Sets all needed data of the user into the list item object.<p>
     *
     * @param user the user to set the data for
     * @param item the list item object to set the data into
     */
    protected void setUserData(CmsUser user, CmsListItem item) {

        item.set(LIST_COLUMN_LOGIN, user.getName());
        item.set(LIST_COLUMN_DISPLAY, user.getSimpleName());
        item.set(LIST_COLUMN_NAME, user.getFullName());
        item.set(LIST_COLUMN_EMAIL, user.getEmail());
        item.set(LIST_COLUMN_LASTLOGIN, new Date(user.getLastlogin()));
        item.set(LIST_COLUMN_ENABLED, Boolean.valueOf(user.isEnabled()));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getName();
    }

    /**
     * Adds an "activate" column.<p>
     *
     * @param metadata the list metadata
     * @param enable the action for enabling
     * @param deactivate the action for disabling
     */
    private void addActivateColumn(
        CmsListMetadata metadata,
        CmsListDirectAction enable,
        CmsListDirectAction deactivate) {

        // create column for activation/deactivation
        CmsListColumnDefinition actCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        actCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_0));
        actCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_HELP_0));
        actCol.setWidth("20");
        actCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        actCol.setListItemComparator(new CmsListItemActionIconComparator());
        actCol.addDirectAction(enable);
        actCol.addDirectAction(deactivate);
        // add it to the list definition
        metadata.addColumn(actCol);
    }

    /**
     * Adds an "enabled" column.<p>
     *
     * @param metadata the list metadata
     * @param enable the list action for enabling
     * @param deactivate the list action for disabling
     */
    private void addEnabledColumn(
        CmsListMetadata metadata,
        CmsListDirectAction enable,
        CmsListDirectAction deactivate) {

        // create column for activation/deactivation
        CmsListColumnDefinition enaCol = new CmsListColumnDefinition(LIST_COLUMN_ENABLED);
        enaCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_0));
        enaCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_HELP_0));
        enaCol.setWidth("20");
        enaCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        enaCol.setFormatter(new I_CmsListFormatter() {

            public String format(Object data, Locale locale) {

                return "";
            }
        });
        enaCol.addDirectAction(enable);
        enaCol.addDirectAction(deactivate);
        enaCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(enaCol);
    }

    /**
     * Creates an "activate" list action.<p>
     *
     * @param id the action id
     *
     * @return the list action
     */
    private CmsListDirectAction createActivateAction(String id) {

        // activate action
        CmsListDirectAction enableAction = new CmsListDirectAction(id) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() == null) {
                    return super.isVisible();
                }
                Boolean enabled = (Boolean)getItem().get(LIST_COLUMN_ENABLED);
                if (enabled == null) {
                    return super.isVisible();
                }
                return !enabled.booleanValue();
            }
        };
        enableAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_NAME_0));
        enableAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_HELP_0));
        enableAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_CONF_0));
        enableAction.setIconPath(ICON_INACTIVE);
        return enableAction;
    }

    /**
     * Creates a "deactivate" list action.<p>
     *
     * @param id the action id
     *
     * @return the list action
     */
    private CmsListDirectAction createDeactivateAction(String id) {

        CmsListDirectAction deactAction = new CmsListDirectAction(id) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() == null) {
                    return super.isVisible();
                }
                Boolean enabled = (Boolean)getItem().get(LIST_COLUMN_ENABLED);
                if (enabled == null) {
                    return super.isVisible();
                }
                return enabled.booleanValue();
            }
        };
        deactAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_NAME_0));
        deactAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_HELP_0));
        deactAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_CONF_0));
        deactAction.setIconPath(ICON_ACTIVE);
        return deactAction;
    }
}