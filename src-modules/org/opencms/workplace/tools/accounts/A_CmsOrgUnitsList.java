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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
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
 * @since 6.5.6
 */
public abstract class A_CmsOrgUnitsList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_DEACTIVE = "add";

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "ad";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_GROUP = "ag";

    /** list action id constant. */
    public static final String LIST_ACTION_USER = "au";

    /** list column id constant. */
    public static final String LIST_COLUMN_ADMIN = "ca";

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

    /** list column id constant. */
    public static final String LIST_COLUMN_WEBUSER = "cw";

    /** list action id constant. */
    public static final String LIST_DEFACTION_OVERVIEW = "do";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_GROUPS = "dg";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_RESOURCES = "dr";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_USERS = "du";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETE = "md";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Cached list of OUs. */
    private List<CmsOrganizationalUnit> m_ous;

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
    @Override
    public void executeListMultiActions() {

        Iterator<CmsListItem> itItems = getSelectedItems().iterator();

        if (getParamListAction().equals(LIST_MACTION_DELETE)) {
            while (itItems.hasNext()) {
                CmsListItem item = itItems.next();
                String ouFqn = item.get(LIST_COLUMN_NAME).toString();
                try {
                    OpenCms.getOrgUnitManager().deleteOrganizationalUnit(getCms(), ouFqn.substring(1));
                } catch (CmsException e) {
                    throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_ORGUNIT_1, ouFqn), e);
                }
            }
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

        String ouFqn = getSelectedItem().get(LIST_COLUMN_NAME).toString();
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {ouFqn.substring(1)});
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/orgunit/mgmt/edit", params);
        } else if (getParamListAction().equals(LIST_ACTION_USER)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/orgunit/users", params);
        } else if (getParamListAction().equals(LIST_ACTION_GROUP)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/orgunit/groups", params);
        } else if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/orgunit/mgmt/delete", params);
        } else if (getParamListAction().equals(LIST_DEFACTION_OVERVIEW)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/orgunit", params);
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

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List<CmsListItem> orgUnits = getList().getAllContent();
        Iterator<CmsListItem> itOrgUnits = orgUnits.iterator();
        while (itOrgUnits.hasNext()) {
            CmsListItem item = itOrgUnits.next();
            String ouFqn = item.get(LIST_COLUMN_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_USERS)) {
                    List<CmsUser> usersOrgUnit = OpenCms.getOrgUnitManager().getUsers(getCms(), ouFqn, false);
                    Iterator<CmsUser> itUsersOrgUnit = usersOrgUnit.iterator();
                    while (itUsersOrgUnit.hasNext()) {
                        CmsUser user = itUsersOrgUnit.next();
                        html.append(user.getFullName());
                        if (itUsersOrgUnit.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_GROUPS)) {
                    List<CmsGroup> groupsOrgUnit = OpenCms.getOrgUnitManager().getGroups(getCms(), ouFqn, false);
                    Iterator<CmsGroup> itGroupsOrgUnit = groupsOrgUnit.iterator();
                    while (itGroupsOrgUnit.hasNext()) {
                        CmsGroup group = itGroupsOrgUnit.next();
                        String niceGroupName = OpenCms.getWorkplaceManager().translateGroupName(group.getName(), false);
                        html.append(niceGroupName);
                        if (itGroupsOrgUnit.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_RESOURCES)) {
                    List<CmsResource> resourcesOrgUnit = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                        getCms(),
                        ouFqn);
                    Iterator<CmsResource> itResourcesOrgUnit = resourcesOrgUnit.iterator();
                    while (itResourcesOrgUnit.hasNext()) {
                        CmsResource resource = itResourcesOrgUnit.next();
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
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        List<CmsOrganizationalUnit> orgUnits = getOrgUnits();
        Iterator<CmsOrganizationalUnit> itOrgUnits = orgUnits.iterator();
        while (itOrgUnits.hasNext()) {
            CmsOrganizationalUnit childOrgUnit = itOrgUnits.next();
            CmsListItem item = getList().newItem(childOrgUnit.getName());
            item.set(LIST_COLUMN_NAME, CmsOrganizationalUnit.SEPARATOR + childOrgUnit.getName());
            item.set(LIST_COLUMN_DESCRIPTION, childOrgUnit.getDescription(getLocale()));
            item.set(LIST_COLUMN_ADMIN, Boolean.valueOf(
                OpenCms.getRoleManager().hasRole(getCms(), CmsRole.ADMINISTRATOR.forOrgUnit(childOrgUnit.getName()))));
            item.set(LIST_COLUMN_WEBUSER, Boolean.valueOf(childOrgUnit.hasFlagWebuser()));
            ret.add(item);
        }
        return ret;
    }

    /**
     * Returns the organizational units to display.<p>
     *
     * @return the organizational units
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsOrganizationalUnit> getOrgUnits() throws CmsException {

        if (m_ous == null) {
            m_ous = OpenCms.getRoleManager().getOrgUnitsForRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(""), true);
        }
        return m_ous;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // add edit action
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            @Override
            public CmsMessageContainer getHelpText() {

                if (!isEnabled()) {
                    return Messages.get().container(Messages.GUI_ORGUNIT_ADMIN_TOOL_DISABLED_EDIT_HELP_0);
                }
                return super.getHelpText();
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                if (getItem() != null) {
                    if (((Boolean)getItem().get(LIST_COLUMN_WEBUSER)).booleanValue()) {
                        return PATH_BUTTONS + "webuser_ou.png";
                    }
                }
                return super.getIconPath();
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            @Override
            public boolean isEnabled() {

                if (getItem() != null) {
                    return ((Boolean)getItem().get(LIST_COLUMN_ADMIN)).booleanValue();
                }
                return super.isVisible();
            }
        };
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

        // create column for manageable flag
        CmsListColumnDefinition adminCol = new CmsListColumnDefinition(LIST_COLUMN_ADMIN);
        adminCol.setVisible(false);
        metadata.addColumn(adminCol);

        // create column for webuser flag
        CmsListColumnDefinition webuserCol = new CmsListColumnDefinition(LIST_COLUMN_WEBUSER);
        webuserCol.setVisible(false);
        metadata.addColumn(webuserCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
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
        usersDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_USERS_NAME_0)));
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
        groupsDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_GROUPS_NAME_0)));
        metadata.addItemDetails(groupsDetails);

        // add resources details
        CmsListItemDetails resourcesDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCES);
        resourcesDetails.setAtColumn(LIST_COLUMN_DESCRIPTION);
        resourcesDetails.setVisible(false);
        resourcesDetails.setShowActionName(
            Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_SHOW_RESOURCES_NAME_0));
        resourcesDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_SHOW_RESOURCES_HELP_0));
        resourcesDetails.setHideActionName(
            Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_HIDE_RESOURCES_NAME_0));
        resourcesDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_HIDE_RESOURCES_HELP_0));
        resourcesDetails.setName(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_RESOURCES_NAME_0));
        resourcesDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_ORGUNITS_DETAIL_RESOURCES_NAME_0)));
        metadata.addItemDetails(resourcesDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_NAME));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_DESCRIPTION));
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

}
