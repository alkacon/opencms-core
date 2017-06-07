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
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
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
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

/**
 * Skeleton for a generic group list.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsGroupsList extends A_CmsListDialog {

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
    public static final String LIST_COLUMN_DISPLAY = "cdn";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_USERS = "cu";

    /** list action id constant. */
    public static final String LIST_DEFACTION_EDIT = "de";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_CHILDREN = "dc";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_PARENT = "dp";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_SET_PERM = "dsp";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_USERS = "du";

    /** list action id constant. */
    public static final String LIST_MACTION_ACTIVATE = "ma";

    /** list action id constant. */
    public static final String LIST_MACTION_DEACTIVATE = "mc";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETE = "md";

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
     * @param listName the name of the list
     */
    public A_CmsGroupsList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName, LIST_COLUMN_DISPLAY, CmsListOrderEnum.ORDER_ASCENDING, null);
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
            params.put(A_CmsEditGroupDialog.PARAM_GROUPID, new String[] {getParamSelItems()});
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
            try {
                getToolManager().jspForwardTool(this, getCurrentToolPath() + "/delete", params);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_SELECTED_GROUPS_0), e);
            }
        } else if (getParamListAction().equals(LIST_MACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator<CmsListItem> itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = itItems.next();
                    String groupName = listItem.get(LIST_COLUMN_NAME).toString();
                    CmsGroup group = getCms().readGroup(groupName);
                    if (!group.isEnabled()) {
                        group.setEnabled(true);
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
                Iterator<CmsListItem> itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = itItems.next();
                    String groupName = listItem.get(LIST_COLUMN_NAME).toString();
                    CmsGroup group = getCms().readGroup(groupName);
                    if (group.isEnabled()) {
                        group.setEnabled(false);
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
    @Override
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        String groupId = getSelectedItem().getId();
        String groupName = getSelectedItem().get(LIST_COLUMN_NAME).toString();

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsEditGroupDialog.PARAM_GROUPID, new String[] {groupId.toString()});
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {m_paramOufqn});
        params.put(A_CmsEditGroupDialog.PARAM_GROUPNAME, new String[] {groupName});
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});

        if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit", params);
        } else if (m_editActionIds.contains(getParamListAction())) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/group", params);
        } else if (getParamListAction().equals(LIST_ACTION_USERS)) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/users", params);
        } else if (m_deleteActionIds.contains(getParamListAction())) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/delete", params);
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            try {
                CmsGroup group = getCms().readGroup(groupName);
                group.setEnabled(true);
                getCms().writeGroup(group);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_GROUP_1, groupName), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
            // execute the activate action
            try {
                CmsGroup group = getCms().readGroup(groupName);
                group.setEnabled(false);
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
        List<CmsListItem> groups = getList().getAllContent();
        Iterator<CmsListItem> itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsListItem item = itGroups.next();
            String groupName = item.get(LIST_COLUMN_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_USERS)) {
                    // users
                    List<CmsUser> users = getCms().getUsersOfGroup(groupName, true);
                    Iterator<CmsUser> itUsers = users.iterator();
                    while (itUsers.hasNext()) {
                        CmsUser user = itUsers.next();
                        if (user.getOuFqn().equals(getParamOufqn())) {
                            html.append(user.getFullName());
                        } else {
                            html.append(user.getDisplayName(getCms(), getLocale()));
                        }
                        if (itUsers.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_CHILDREN)) {
                    // childen
                    Iterator<CmsGroup> itChildren = getCms().getChildren(groupName, false).iterator();
                    while (itChildren.hasNext()) {
                        CmsGroup group = itChildren.next();
                        if (group.getOuFqn().equals(getParamOufqn())) {
                            html.append(group.getSimpleName());
                        } else {
                            html.append(group.getDisplayName(getCms(), getLocale()));
                        }
                        if (itChildren.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_PARENT)) {
                    // parent
                    CmsGroup parent = getCms().readGroup(getCms().readGroup(groupName).getParentId());
                    html.append(parent.getName());
                } else if (detailId.equals(LIST_DETAIL_SET_PERM)) {
                    // folder permissions
                    String storedSiteRoot = getCms().getRequestContext().getSiteRoot();
                    try {
                        getCms().getRequestContext().setSiteRoot("/");
                        CmsGroup group = getCms().readGroup(groupName);
                        Iterator<CmsResource> itRes = getCms().getResourcesForPrincipal(
                            group.getId(),
                            null,
                            false).iterator();
                        while (itRes.hasNext()) {
                            CmsResource resource = itRes.next();
                            html.append(resource.getRootPath());

                            Iterator<CmsAccessControlEntry> itAces = getCms().getAccessControlEntries(
                                resource.getRootPath(),
                                false).iterator();
                            while (itAces.hasNext()) {
                                CmsAccessControlEntry ace = itAces.next();
                                if (ace.getPrincipal().equals(group.getId())) {
                                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                                        ace.getPermissions().getPermissionString())) {
                                        html.append(" (" + ace.getPermissions().getPermissionString() + ")");
                                    }
                                    break;
                                }
                            }

                            if (itRes.hasNext()) {
                                html.append("<br>");
                            }
                            html.append("\n");
                        }
                    } finally {
                        getCms().getRequestContext().setSiteRoot(storedSiteRoot);
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
     * Returns a list of groups.<p>
     *
     * @return the list of all groups
     *
     * @throws CmsException if something goes wrong
     */
    protected abstract List<CmsGroup> getGroups() throws CmsException;

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        // get content
        List<CmsGroup> groups = getGroups();
        Iterator<CmsGroup> itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = itGroups.next();
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set(LIST_COLUMN_NAME, group.getName());
            item.set(LIST_COLUMN_DISPLAY, OpenCms.getWorkplaceManager().translateGroupName(group.getName(), false));
            item.set(LIST_COLUMN_DESCRIPTION, group.getDescription(getLocale()));
            ret.add(item);
        }

        return ret;
    }

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
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);

        // add edit action
        setEditAction(editCol);
        m_editActionIds.addAll(editCol.getDirectActionIds());
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
        usersAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "user.png");
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
        CmsListDirectAction actAction = new CmsListDirectAction(LIST_ACTION_ACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    String groupId = getItem().getId();
                    try {
                        return !getCms().readGroup(new CmsUUID(groupId)).isEnabled();
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        actAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_ACTIVATE_NAME_0));
        actAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_ACTIVATE_HELP_0));
        actAction.setConfirmationMessage(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_ACTIVATE_CONF_0));
        actAction.setIconPath(ICON_INACTIVE);
        actCol.addDirectAction(actAction);

        // deactivate action
        CmsListDirectAction deactAction = new CmsListDirectAction(LIST_ACTION_DEACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    String groupId = getItem().getId();
                    try {
                        return getCms().readGroup(new CmsUUID(groupId)).isEnabled();
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        deactAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DEACTIVATE_NAME_0));
        deactAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DEACTIVATE_HELP_0));
        deactAction.setConfirmationMessage(Messages.get().container(Messages.GUI_GROUPS_LIST_ACTION_DEACTIVATE_CONF_0));
        deactAction.setIconPath(ICON_ACTIVE);
        actCol.addDirectAction(deactAction);
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
        setDeleteAction(deleteCol);
        m_deleteActionIds.addAll(deleteCol.getDirectActionIds());
        // add it to the list definition
        metadata.addColumn(deleteCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        // add it to the list definition
        metadata.addColumn(nameCol);
        nameCol.setVisible(false);

        // create column for display name
        CmsListColumnDefinition displayCol = new CmsListColumnDefinition(LIST_COLUMN_DISPLAY);
        displayCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_NAME_0));
        displayCol.setWidth("35%");

        // create default edit action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_DEFACTION_EDIT);
        defEditAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_EDIT_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_EDIT_HELP_0));
        displayCol.addDefaultAction(defEditAction);

        // add it to the list definition
        metadata.addColumn(displayCol);

        // add column for description
        CmsListColumnDefinition descriptionCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descriptionCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_DESCRIPTION_0));
        descriptionCol.setWidth("65%");
        metadata.addColumn(descriptionCol);
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

        // add user users details
        CmsListItemDetails usersDetails = new CmsListItemDetails(LIST_DETAIL_USERS);
        usersDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        usersDetails.setVisible(false);
        usersDetails.setShowActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_USERS_NAME_0));
        usersDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_USERS_HELP_0));
        usersDetails.setHideActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_USERS_NAME_0));
        usersDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_USERS_HELP_0));
        usersDetails.setName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_USERS_NAME_0));
        usersDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_GROUPS_DETAIL_USERS_NAME_0)));
        metadata.addItemDetails(usersDetails);

        // add user children details
        CmsListItemDetails childDetails = new CmsListItemDetails(LIST_DETAIL_CHILDREN);
        childDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        childDetails.setVisible(false);
        childDetails.setShowActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_CHILDREN_NAME_0));
        childDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_CHILDREN_HELP_0));
        childDetails.setHideActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_CHILDREN_NAME_0));
        childDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_CHILDREN_HELP_0));
        childDetails.setName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_CHILDREN_NAME_0));
        childDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_GROUPS_DETAIL_CHILDREN_NAME_0)));
        metadata.addItemDetails(childDetails);

        // add parent group details
        CmsListItemDetails parentDetails = new CmsListItemDetails(LIST_DETAIL_PARENT);
        parentDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        parentDetails.setVisible(false);
        parentDetails.setShowActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_PARENT_NAME_0));
        parentDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_PARENT_HELP_0));
        parentDetails.setHideActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_PARENT_NAME_0));
        parentDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_PARENT_HELP_0));
        parentDetails.setName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_PARENT_NAME_0));
        parentDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_GROUPS_DETAIL_PARENT_NAME_0)));
        metadata.addItemDetails(parentDetails);

        // add folder permission details
        CmsListItemDetails setPermDetails = new CmsListItemDetails(LIST_DETAIL_SET_PERM);
        setPermDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        setPermDetails.setVisible(false);
        setPermDetails.setShowActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_SET_PERM_NAME_0));
        setPermDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_SET_PERM_HELP_0));
        setPermDetails.setHideActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_SET_PERM_NAME_0));
        setPermDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_SET_PERM_HELP_0));
        setPermDetails.setName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SET_PERM_NAME_0));
        setPermDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SET_PERM_NAME_0)));
        metadata.addItemDetails(setPermDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_DISPLAY));
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add delete multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_DELETE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_DELETE_HELP_0));
        deleteMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_DELETE_CONF_0));
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
        deactivateUser.setConfirmationMessage(
            Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_DEACTIVATE_CONF_0));
        deactivateUser.setIconPath(ICON_MULTI_DEACTIVATE);
        metadata.addMultiAction(deactivateUser);
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
}