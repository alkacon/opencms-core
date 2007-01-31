/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/A_CmsOrgUnitsList.java,v $
 * Date   : $Date: 2007/01/31 15:44:17 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

/**
 * Main organization unit management view.<p>
 * 
 * @author Raphael Schnuck  
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.5.6 
 */
public abstract class A_CmsOrgUnitsList extends A_CmsListDialog {

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "ad";

    /** list action id constant. */
    public static final String LIST_ACTION_DEACTIVE = "add";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_GROUP = "ag";

    /** list action id constant. */
    public static final String LIST_ACTION_USER = "au";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cb";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_GROUP = "cg";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_USER = "cu";

    /** list action id constant. */
    public static final String LIST_DEFACTION_OVERVIEW = "do";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_USERS = "du";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_GROUPS = "dg";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_RESOURCES = "dr";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public A_CmsOrgUnitsList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName, LIST_COLUMN_NAME, CmsListOrderEnum.ORDER_ASCENDING, null);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        Iterator itItems = getSelectedItems().iterator();
        CmsListItem item = null;

        if (getParamListAction().equals(LIST_MACTION_DELETE)) {
            try {
                while (itItems.hasNext()) {
                    item = (CmsListItem)itItems.next();
                    String ouFqn = item.get(LIST_COLUMN_NAME).toString();
                    OpenCms.getOrgUnitManager().deleteOrganizationalUnit(getCms(), ouFqn);
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_DELETE_ORGUNIT_1,
                    (item == null) ? (Object)"?" : item.get(LIST_COLUMN_NAME)), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        String ouFqn = getSelectedItem().get(LIST_COLUMN_NAME).toString();
        Map params = new HashMap();
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, ouFqn);
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit", params);
        } else if (getParamListAction().equals(LIST_ACTION_USER)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/users", params);
        } else if (getParamListAction().equals(LIST_ACTION_GROUP)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/groups", params);
        } else if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/delete", params);
        } else if (getParamListAction().equals(LIST_DEFACTION_OVERVIEW)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath(), params);
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
        List orgUnits = getList().getAllContent();
        Iterator itOrgUnits = orgUnits.iterator();
        while (itOrgUnits.hasNext()) {
            CmsListItem item = (CmsListItem)itOrgUnits.next();
            String ouFqn = item.get(LIST_COLUMN_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_USERS)) {
                    List usersOrgUnit = OpenCms.getOrgUnitManager().getUsers(getCms(), ouFqn, false);
                    Iterator itUsersOrgUnit = usersOrgUnit.iterator();
                    while (itUsersOrgUnit.hasNext()) {
                        CmsUser user = (CmsUser)itUsersOrgUnit.next();
                        html.append(user.getFullName());
                        if (itUsersOrgUnit.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_GROUPS)) {
                    List groupsOrgUnit = OpenCms.getOrgUnitManager().getGroups(getCms(), ouFqn, false);
                    Iterator itGroupsOrgUnit = groupsOrgUnit.iterator();
                    while (itGroupsOrgUnit.hasNext()) {
                        CmsGroup group = (CmsGroup)itGroupsOrgUnit.next();
                        html.append(group.getSimpleName());
                        if (itGroupsOrgUnit.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_RESOURCES)) {
                    List resourcesOrgUnit = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                        getCms(),
                        ouFqn);
                    Iterator itResourcesOrgUnit = resourcesOrgUnit.iterator();
                    while (itResourcesOrgUnit.hasNext()) {
                        CmsResource resource = (CmsResource)itResourcesOrgUnit.next();
                        html.append(resource.getRootPath());
                        if (itResourcesOrgUnit.hasNext()) {
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
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();
        List orgUnits = OpenCms.getRoleManager().getManageableOrgUnits(getCms(), "", true);
        Iterator itOrgUnits = orgUnits.iterator();
        while (itOrgUnits.hasNext()) {
            CmsOrganizationalUnit childOrgUnit = (CmsOrganizationalUnit)itOrgUnits.next();
            CmsListItem item = getList().newItem(childOrgUnit.getName());
            item.set(LIST_COLUMN_NAME, childOrgUnit.getName());
            item.set(LIST_COLUMN_DESCRIPTION, childOrgUnit.getDescription());
            ret.add(item);
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // add edit action
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_EDIT_HELP_0));
        editAction.setIconPath(getEditIcon());
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for user
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_USER_0));
        userCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_USER_HELP_0));
        userCol.setWidth("20");
        userCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        userCol.setSorteable(false);
        // add user action
        CmsListDirectAction userAction = new CmsListDirectAction(LIST_ACTION_USER);
        userAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_ACTION_USER_NAME_0));
        userAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_USER_HELP_0));
        userAction.setIconPath(getUserIcon());
        userCol.addDirectAction(userAction);
        // add it to the list definition
        metadata.addColumn(userCol);

        // create column for group
        CmsListColumnDefinition groupCol = new CmsListColumnDefinition(LIST_COLUMN_GROUP);
        groupCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_GROUP_0));
        groupCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_GROUP_HELP_0));
        groupCol.setWidth("20");
        groupCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        groupCol.setSorteable(false);
        // add group action
        CmsListDirectAction groupAction = new CmsListDirectAction(LIST_ACTION_GROUP);
        groupAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_ACTION_GROUP_NAME_0));
        groupAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_GROUP_HELP_0));
        groupAction.setIconPath(getGroupIcon());
        groupCol.addDirectAction(groupAction);
        // add it to the list definition
        metadata.addColumn(groupCol);

        // create column for delete
        CmsListColumnDefinition deleteCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        deleteCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_DELETE_0));
        deleteCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_DELETE_HELP_0));
        deleteCol.setWidth("20");
        deleteCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        deleteCol.setSorteable(false);
        // add delete action
        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            public boolean isEnabled() {

                if (getItem() != null) {
                    String ouFqn = getItem().get(LIST_COLUMN_NAME).toString();
                    try {
                        if (OpenCms.getOrgUnitManager().getUsers(getCms(), ouFqn, true).size() > 0) {
                            return false;
                        }
                        if (OpenCms.getOrgUnitManager().getGroups(getCms(), ouFqn, true).size() > 0) {
                            List groups = OpenCms.getOrgUnitManager().getGroups(getCms(), ouFqn, true);
                            Iterator itGroups = groups.iterator();
                            while (itGroups.hasNext()) {
                                CmsGroup group = (CmsGroup)itGroups.next();
                                if (!OpenCms.getDefaultUsers().isDefaultGroup(group.getName())) {
                                    return false;
                                }
                            }
                        }
                        if (OpenCms.getOrgUnitManager().getOrganizationalUnits(getCms(), ouFqn, true).size() > 0) {
                            return false;
                        }
                        return true;
                    } catch (CmsException e) {
                        return true;
                    }
                }
                return super.isVisible();
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            public CmsMessageContainer getHelpText() {

                if (!isEnabled()) {
                    return Messages.get().container(Messages.GUI_ORGUNIT_ADMIN_TOOL_DISABLED_DELETE_HELP_0);
                }
                return super.getHelpText();
            }
        };
        deleteAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_DELETE_HELP_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);

        // add it to the list definition
        metadata.addColumn(deleteCol);

        // create column for description
        CmsListColumnDefinition descCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_DESCRIPTION_0));
        descCol.setWidth("60%");
        descCol.setSorteable(true);
        // create default overview action
        CmsListDefaultAction defOverviewAction = new CmsListDefaultAction(LIST_DEFACTION_OVERVIEW);
        defOverviewAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_DEFACTION_OVERVIEW_NAME_0));
        defOverviewAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_DEFACTION_OVERVIEW_HELP_0));
        descCol.addDefaultAction(defOverviewAction);
        // add it to the list definition
        metadata.addColumn(descCol);

        // create column for name / path
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_NAME_0));
        nameCol.setWidth("40%");
        // add it to the list definition
        metadata.addColumn(nameCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add users details
        CmsListItemDetails usersDetails = new CmsListItemDetails(LIST_DETAIL_USERS);
        usersDetails.setAtColumn(LIST_COLUMN_DESCRIPTION);
        usersDetails.setVisible(false);
        usersDetails.setShowActionName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_SHOW_USERS_NAME_0));
        usersDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_SHOW_USERS_HELP_0));
        usersDetails.setHideActionName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_HIDE_USERS_NAME_0));
        usersDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_HIDE_USERS_HELP_0));
        usersDetails.setName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_USERS_NAME_0));
        usersDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_ORGUNITS_DETAIL_USERS_NAME_0)));
        metadata.addItemDetails(usersDetails);

        // add groups details
        CmsListItemDetails groupsDetails = new CmsListItemDetails(LIST_DETAIL_GROUPS);
        groupsDetails.setAtColumn(LIST_COLUMN_DESCRIPTION);
        groupsDetails.setVisible(false);
        groupsDetails.setShowActionName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_SHOW_GROUPS_NAME_0));
        groupsDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_SHOW_GROUPS_HELP_0));
        groupsDetails.setHideActionName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_HIDE_GROUPS_NAME_0));
        groupsDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_HIDE_GROUPS_HELP_0));
        groupsDetails.setName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_GROUPS_NAME_0));
        groupsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_ORGUNITS_DETAIL_GROUPS_NAME_0)));
        metadata.addItemDetails(groupsDetails);

        // add resources details
        CmsListItemDetails resourcesDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCES);
        resourcesDetails.setAtColumn(LIST_COLUMN_DESCRIPTION);
        resourcesDetails.setVisible(false);
        resourcesDetails.setShowActionName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_SHOW_RESOURCES_NAME_0));
        resourcesDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_ORGUNITS_DETAIL_SHOW_RESOURCES_HELP_0));
        resourcesDetails.setHideActionName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_HIDE_RESOURCES_NAME_0));
        resourcesDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_ORGUNITS_DETAIL_HIDE_RESOURCES_HELP_0));
        resourcesDetails.setName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_RESOURCES_NAME_0));
        resourcesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_ORGUNITS_DETAIL_RESOURCES_NAME_0)));
        metadata.addItemDetails(resourcesDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_NAME));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_DESCRIPTION));
        metadata.setSearchAction(searchAction);
    }

    /** list action id constant. */
    public static final String LIST_MACTION_DELETE = "md";

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add delete multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_MACTION_DELETE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_MACTION_DELETE_HELP_0));
        deleteMultiAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_ORGUNITS_LIST_MACTION_DELETE_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_DELETE);
        metadata.addMultiAction(deleteMultiAction);
    }

    /**
     * Returns the path of the edit icon.<p>
     * 
     * @return the path of the edit icon
     */
    public String getEditIcon() {

        return PATH_BUTTONS + "orgunit.png";
    }

    /**
     * Returns the path of the group icon.<p>
     * 
     * @return the path of the group icon
     */
    public String getGroupIcon() {

        return PATH_BUTTONS + "group.png";
    }

    /**
     * Returns the path of the user icon.<p>
     * 
     * @return the path of the user icon
     */
    public String getUserIcon() {

        return PATH_BUTTONS + "user.png";
    }

}
