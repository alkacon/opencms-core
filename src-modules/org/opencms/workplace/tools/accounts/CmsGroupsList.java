/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsGroupsList.java,v $
 * Date   : $Date: 2005/06/21 15:54:15 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;

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
 * Main user account management view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsGroupsList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ACTIVATE = "aa";

    /** list action id constant. */
    public static final String LIST_ACTION_DEACTIVATE = "ac";

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "ad";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_USERS = "au";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIVATE = "ca";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_PARENT = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_USERS = "cu";

    /** list action id constant. */
    public static final String LIST_DEFACTION_EDIT = "de";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_CHILDS = "dc";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_USERS = "du";

    /** list id constant. */
    public static final String LIST_ID = "lg";

    /** list action id constant. */
    public static final String LIST_MACTION_ACTIVATE = "ma";

    /** list action id constant. */
    public static final String LIST_MACTION_DEACTIVATE = "mc";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETE = "md";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsGroupsList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_GROUPS_LIST_NAME_0),
            LIST_COLUMN_NAME,
            LIST_COLUMN_NAME);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Deletes the selected group and returns.
     * 
     * @throws Exception is something goes wrong
     */
    public void actionDeleteGroup() throws Exception {

        String groupName = getJsp().getRequest().getParameter(CmsEditGroupDialog.PARAM_GROUPNAME);
        getCms().deleteGroup(groupName);
        refreshList();
        actionCloseDialog();
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
            List removedItems = new ArrayList();
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    getCms().deleteGroup(listItem.get(LIST_COLUMN_NAME).toString());
                    removedItems.add(listItem.getId());
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_SELECTED_GROUPS_0), e);
            } finally {
                getList().removeAllItems(removedItems, getLocale());
            }
        } else if (getParamListAction().equals(LIST_MACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    String groupName = listItem.get(LIST_COLUMN_NAME).toString();
                    CmsGroup group = getCms().readGroup(groupName);
                    if (group.getDisabled()) {
                        group.setEnabled();
                        getCms().writeGroup(group);
                    }
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_SELECTED_GROUPS_0), e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
        } else if (getParamListAction().equals(LIST_MACTION_DEACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    String groupName = listItem.get(LIST_COLUMN_NAME).toString();
                    CmsGroup group = getCms().readGroup(groupName);
                    if (!group.getDisabled()) {
                        group.setDisabled();
                        getCms().writeGroup(group);
                    }
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DEACTIVATE_SELECTED_GROUPS_0), e);
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
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        String groupId = getSelectedItem().getId();
        String groupName = getSelectedItem().get(LIST_COLUMN_NAME).toString();

        Map params = new HashMap();
        params.put(CmsEditGroupDialog.PARAM_GROUPNAME, groupName);
        params.put(CmsEditGroupDialog.PARAM_GROUPID, groupId);
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/groups/edit", params);
        } else if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            getToolManager().jspForwardTool(this, "/accounts/groups/edit/group", params);
        } else if (getParamListAction().equals(LIST_ACTION_USERS)) {
            getToolManager().jspForwardTool(this, "/accounts/groups/edit/users", params);
        } else if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // execute the delete action
            try {
                getCms().deleteGroup(groupName);
                getList().removeItem(groupId, getLocale());
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_GROUP_1, groupName), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            try {
                CmsGroup group = getCms().readGroup(groupName);
                group.setEnabled();
                getCms().writeGroup(group);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_GROUP_1, groupName), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
            // execute the activate action
            try {
                CmsGroup group = getCms().readGroup(groupName);
                group.setDisabled();
                getCms().writeGroup(group);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DEACTIVATE_GROUP_1, groupName), e);
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
        List groups = getList().getAllContent();
        Iterator itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsListItem item = (CmsListItem)itGroups.next();
            String groupName = item.get(LIST_COLUMN_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_USERS)) {
                    // users
                    Iterator itUsers = getCms().getUsersOfGroup(groupName).iterator();
                    while (itUsers.hasNext()) {
                        html.append(((CmsUser)itUsers.next()).getFullName());
                        if (itUsers.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_CHILDS)) {
                    // childs
                    Iterator itChilds = getCms().getChild(groupName).iterator();
                    while (itChilds.hasNext()) {
                        html.append(((CmsGroup)itChilds.next()).getName());
                        if (itChilds.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                // ignore
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();
        // get content
        List groups = getCms().getGroups();
        Iterator itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set(LIST_COLUMN_NAME, group.getName());
            item.set(LIST_COLUMN_DESCRIPTION, group.getDescription());
            try {
                item.set(LIST_COLUMN_PARENT, getCms().readGroup(group.getParentId()).getName());
            } catch (Exception e) {
                // ignore
            }
            ret.add(item);
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
        editCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // add edit action
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_EDIT_HELP_0));
        editAction.setIconPath(CmsUsersList.PATH_BUTTONS + "group.png");
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for group edition
        CmsListColumnDefinition usersCol = new CmsListColumnDefinition(LIST_COLUMN_USERS);
        usersCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_USERS_0));
        usersCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_USERS_HELP_0));
        usersCol.setWidth("20");
        usersCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        usersCol.setSorteable(false);
        // add groups action
        CmsListDirectAction usersAction = new CmsListDirectAction(LIST_ACTION_USERS);
        usersAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_USERS_NAME_0));
        usersAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_USERS_HELP_0));
        usersAction.setIconPath(CmsUsersList.PATH_BUTTONS + "user.png");
        usersCol.addDirectAction(usersAction);
        // add it to the list definition
        metadata.addColumn(usersCol);

        // create column for activation/deactivation
        CmsListColumnDefinition actCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        actCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_ACTIVATE_0));
        actCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_ACTIVATE_HELP_0));
        actCol.setWidth("20");
        actCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        actCol.setListItemComparator(new CmsListItemActionIconComparator());
        // activate action
        CmsListDirectAction actAction = new CmsListDirectAction(LIST_ACTION_ACTIVATE);
        actAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_ACTIVATE_NAME_0));
        actAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_ACTIVATE_HELP_0));
        actAction.setConfirmationMessage(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_ACTIVATE_CONF_0));
        actAction.setIconPath(ICON_INACTIVE);
        // deactivate action
        CmsListDirectAction deactAction = new CmsListDirectAction(LIST_ACTION_DEACTIVATE);
        deactAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DEACTIVATE_NAME_0));
        deactAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DEACTIVATE_HELP_0));
        deactAction.setConfirmationMessage(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DEACTIVATE_CONF_0));
        deactAction.setIconPath(ICON_ACTIVE);
        // adds an activate/deactivate direct action
        CmsGroupActivateAction activateAction = new CmsGroupActivateAction(LIST_ACTION_ACTIVATE, getCms());
        activateAction.setFirstAction(actAction);
        activateAction.setSecondAction(deactAction);
        actCol.addDirectAction(activateAction);
        // add it to the list definition
        metadata.addColumn(actCol);

        // create column for deletion
        CmsListColumnDefinition deleteCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        deleteCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_DELETE_0));
        deleteCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_DELETE_HELP_0));
        deleteCol.setWidth("20");
        deleteCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        deleteCol.setSorteable(false);
        // add delete action
        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DELETE_HELP_0));
        deleteAction.setConfirmationMessage(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DELETE_CONF_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
        // add it to the list definition
        metadata.addColumn(deleteCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_NAME_0));
        nameCol.setWidth("20%");
        // create default edit action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_DEFACTION_EDIT);
        defEditAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_EDIT_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_EDIT_HELP_0));
        nameCol.setDefaultAction(defEditAction);
        // add it to the list definition
        metadata.addColumn(nameCol);

        // add column for description
        CmsListColumnDefinition descriptionCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descriptionCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_DESCRIPTION_0));
        descriptionCol.setWidth("60%");
        metadata.addColumn(descriptionCol);

        // add column for parent
        CmsListColumnDefinition parentCol = new CmsListColumnDefinition(LIST_COLUMN_PARENT);
        parentCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_PARENT_0));
        parentCol.setWidth("20%");
        metadata.addColumn(parentCol);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add user users details
        CmsListItemDetails usersDetails = new CmsListItemDetails(LIST_DETAIL_USERS);
        usersDetails.setAtColumn(LIST_COLUMN_NAME);
        usersDetails.setVisible(false);
        usersDetails.setShowActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_USERS_NAME_0));
        usersDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_USERS_HELP_0));
        usersDetails.setHideActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_USERS_NAME_0));
        usersDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_USERS_HELP_0));
        usersDetails.setName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_USERS_NAME_0));
        usersDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_GROUPS_DETAIL_USERS_NAME_0)));
        metadata.addItemDetails(usersDetails);

        // add user childs details
        CmsListItemDetails childDetails = new CmsListItemDetails(LIST_DETAIL_CHILDS);
        childDetails.setAtColumn(LIST_COLUMN_NAME);
        childDetails.setVisible(false);
        childDetails.setShowActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_CHILDS_NAME_0));
        childDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_CHILDS_HELP_0));
        childDetails.setHideActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_CHILDS_NAME_0));
        childDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_CHILDS_HELP_0));
        childDetails.setName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_CHILDS_NAME_0));
        childDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_GROUPS_DETAIL_CHILDS_NAME_0)));
        metadata.addItemDetails(childDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add delete multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_DELETE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_DELETE_HELP_0));
        deleteMultiAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_GROUPS_LIST_MACTION_DELETE_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_DELETE);
        metadata.addMultiAction(deleteMultiAction);

        // add the activate user multi action
        CmsListMultiAction activateUser = new CmsListMultiAction(LIST_MACTION_ACTIVATE);
        activateUser.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_ACTIVATE_NAME_0));
        activateUser.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_ACTIVATE_HELP_0));
        activateUser.setConfirmationMessage(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_ACTIVATE_CONF_0));
        activateUser.setIconPath(ICON_MULTI_ACTIVATE);
        metadata.addMultiAction(activateUser);

        // add the deactivate user multi action
        CmsListMultiAction deactivateUser = new CmsListMultiAction(LIST_MACTION_DEACTIVATE);
        deactivateUser.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_DEACTIVATE_NAME_0));
        deactivateUser.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_DEACTIVATE_HELP_0));
        deactivateUser.setConfirmationMessage(Messages.get().container(
            Messages.GUI_GROUPS_LIST_MACTION_DEACTIVATE_CONF_0));
        deactivateUser.setIconPath(ICON_MULTI_DEACTIVATE);
        metadata.addMultiAction(deactivateUser);
    }
}