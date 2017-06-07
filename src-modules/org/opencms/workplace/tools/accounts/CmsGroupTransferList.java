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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Allows to select a group to transfer the permissions and attributes from another one.<p>
 *
 * @since 6.0.0
 */
public class CmsGroupTransferList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_TRANSFER = "at";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_DISPLAY = "cdis";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_PARENT = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_TRANSFER = "ct";

    /** list column id constant. */
    public static final String LIST_COLUMN_USERS = "cu";

    /** list action id constant. */
    public static final String LIST_DEFACTION_TRANSFER = "dt";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_CHILDREN = "dc";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_USERS = "du";

    /** List id constant. */
    public static final String LIST_ID = "lgt";

    /** Stores the value of the group name, could be a list of names. */
    private String m_groupName;

    /** Stores the value of the request parameter for the group id, could be a list of ids. */
    private String m_paramGroupid;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsGroupTransferList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGroupTransferList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     *
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsGroupTransferList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_GROUPS_TRANSFER_LIST_NAME_0),
            LIST_COLUMN_DISPLAY,
            CmsListOrderEnum.ORDER_ASCENDING,
            LIST_COLUMN_DISPLAY);
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

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        if (getParamListAction().equals(LIST_DEFACTION_TRANSFER) || getParamListAction().equals(LIST_ACTION_TRANSFER)) {
            try {
                getCms().deleteGroup(new CmsUUID(getParamGroupid()), new CmsUUID(getSelectedItem().getId()));
                CmsRequestUtil.forwardRequest(getParamCloseLink(), getJsp().getRequest(), getJsp().getResponse());
                setForwarded(true);
            } catch (CmsException e) {
                throw new CmsRuntimeException(
                    Messages.get().container(Messages.ERR_TRANSFER_GROUP_1, getSelectedItem().get(LIST_COLUMN_NAME)),
                    e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * Returns the group Name.<p>
     *
     * @return the group Name
     */
    public String getGroupName() {

        return m_groupName;
    }

    /**
     * Returns the group id parameter value.<p>
     *
     * @return the group id parameter value
     */
    public String getParamGroupid() {

        return m_paramGroupid;
    }

    /**
     * Sets the group id parameter value.<p>
     *
     * @param groupId the group id parameter value
     */
    public void setParamGroupid(String groupId) {

        m_paramGroupid = groupId;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    @Override
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(2048);
        result.append(
            dialogBlockStart(Messages.get().container(Messages.GUI_GROUPS_TRANSFER_NOTICE_0).key(getLocale())));
        result.append("\n");
        if (getCurrentToolPath().indexOf("/edit/") < 0) {
            result.append(key(Messages.GUI_GROUP_DEPENDENCIES_SELECTED_GROUPS_0));
            result.append(":<br>\n");
            List<String> users = CmsStringUtil.splitAsList(getGroupName(), CmsHtmlList.ITEM_SEPARATOR, true);
            result.append("<ul>\n");
            Iterator<String> it = users.iterator();
            while (it.hasNext()) {
                String name = it.next();
                result.append("<li>");
                result.append(name);
                result.append("</li>\n");
            }
            result.append("</ul>\n");
        }
        result.append(key(Messages.GUI_GROUPS_TRANSFER_NOTICE_TEXT_0));
        result.append(dialogBlockEnd());
        return result.toString();
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
                    Iterator<CmsUser> itUsers = getCms().getUsersOfGroup(groupName).iterator();
                    while (itUsers.hasNext()) {
                        html.append(itUsers.next().getFullName());
                        if (itUsers.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
                } else if (detailId.equals(LIST_DETAIL_CHILDREN)) {
                    // children
                    Iterator<CmsGroup> itChildren = getCms().getChildren(groupName, false).iterator();
                    while (itChildren.hasNext()) {
                        html.append(itChildren.next().getName());
                        if (itChildren.hasNext()) {
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
     * Returns the list of groups to display.<p>
     *
     * @return the list of groups to display
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsGroup> getGroups() throws CmsException {

        return CmsPrincipal.filterCoreGroups(OpenCms.getOrgUnitManager().getGroups(getCms(), "", true));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        // get content
        List<CmsGroup> groups = getGroups();
        Set<String> selGroups = new HashSet<String>(
            CmsStringUtil.splitAsList(getParamGroupid(), CmsHtmlList.ITEM_SEPARATOR, true));
        Iterator<CmsGroup> itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = itGroups.next();
            if (selGroups.contains(group.getId().toString())) {
                continue;
            }
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set(LIST_COLUMN_NAME, group.getName());
            item.set(LIST_COLUMN_DISPLAY, OpenCms.getWorkplaceManager().translateGroupName(group.getName(), true));
            item.set(LIST_COLUMN_DESCRIPTION, group.getDescription(getLocale()));
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

        // create column for transfer
        CmsListColumnDefinition transferCol = new CmsListColumnDefinition(LIST_COLUMN_TRANSFER);
        transferCol.setName(Messages.get().container(Messages.GUI_GROUPS_TRANSFER_LIST_COLS_TRANSFER_0));
        transferCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_TRANSFER_LIST_COLS_TRANSFER_HELP_0));
        transferCol.setWidth("20");
        transferCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        transferCol.setSorteable(false);

        // add transfer action
        setTransferAction(transferCol);
        // add it to the list definition
        metadata.addColumn(transferCol);

        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_NAME_0));
        nameCol.setVisible(false);
        metadata.addColumn(nameCol);

        // create column for name
        CmsListColumnDefinition displayCol = new CmsListColumnDefinition(LIST_COLUMN_DISPLAY);
        displayCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_NAME_0));
        displayCol.setWidth("20%");

        // create default transfer action
        CmsListDefaultAction defTransferAction = new CmsListDefaultAction(LIST_DEFACTION_TRANSFER);
        defTransferAction.setName(
            Messages.get().container(Messages.GUI_GROUPS_TRANSFER_LIST_DEFACTION_TRANSFER_NAME_0));
        defTransferAction.setHelpText(
            Messages.get().container(Messages.GUI_GROUPS_TRANSFER_LIST_DEFACTION_TRANSFER_HELP_0));
        displayCol.addDefaultAction(defTransferAction);

        // add it to the list definition
        metadata.addColumn(displayCol);

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
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no-op
    }

    /**
     * Sets the icon actions for the transfer list.<p>
     *
     * @param transferCol the column to set the action
     */
    protected void setTransferAction(CmsListColumnDefinition transferCol) {

        CmsListDirectAction transferAction = new CmsListDirectAction(LIST_ACTION_TRANSFER);
        transferAction.setName(Messages.get().container(Messages.GUI_GROUPS_TRANSFER_LIST_ACTION_TRANSFER_NAME_0));
        transferAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_TRANSFER_LIST_ACTION_TRANSFER_HELP_0));
        transferAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "group.png");
        transferCol.addDirectAction(transferAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        m_groupName = "";
        Iterator<String> itGroups = CmsStringUtil.splitAsList(
            getParamGroupid(),
            CmsHtmlList.ITEM_SEPARATOR,
            true).iterator();
        while (itGroups.hasNext()) {
            CmsUUID id = new CmsUUID(itGroups.next());
            m_groupName += getCms().readGroup(id).getName();
            if (itGroups.hasNext()) {
                m_groupName += CmsHtmlList.ITEM_SEPARATOR;
            }
        }
    }
}