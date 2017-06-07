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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Group account view over all manageable organizational units.<p>
 *
 * @since 6.5.6
 */
public class CmsGroupsAllOrgUnitsList extends A_CmsGroupsList {

    /** list action id constant. */
    public static final String LIST_ACTION_OVERVIEW = "ao";

    /** list column id constant. */
    public static final String LIST_COLUMN_ORGUNIT = "co";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ORGUNIT_DESC = "dd";

    /** list id constant. */
    public static final String LIST_ID = "lgaou";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsGroupsAllOrgUnitsList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_GROUPS_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGroupsAllOrgUnitsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        String groupId = getSelectedItem().getId();
        String groupName = getSelectedItem().get(LIST_COLUMN_NAME).toString();

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsEditGroupDialog.PARAM_GROUPID, new String[] {groupId});
        params.put(A_CmsEditGroupDialog.PARAM_GROUPNAME, new String[] {groupName});
        params.put(
            A_CmsOrgUnitDialog.PARAM_OUFQN,
            new String[] {getSelectedItem().get(LIST_COLUMN_ORGUNIT).toString().substring(1)});
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});

        if (getParamListAction().equals(LIST_ACTION_OVERVIEW)) {
            // forward
            getToolManager().jspForwardTool(this, "/accounts/orgunit/groups/edit", params);
        } else if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/orgunit/groups/edit", params);
        } else {
            super.executeListSingleActions();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        super.fillDetails(detailId);

        // get content
        List<CmsListItem> groups = getList().getAllContent();
        Iterator<CmsListItem> itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsListItem item = itGroups.next();
            String groupName = item.get(LIST_COLUMN_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_ORGUNIT_DESC)) {
                    CmsGroup group = getCms().readGroup(groupName);
                    html.append(
                        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), group.getOuFqn()).getDescription(
                            getLocale()));
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
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#getGroups()
     */
    @Override
    protected List<CmsGroup> getGroups() throws CmsException {

        return CmsPrincipal.filterCoreGroups(OpenCms.getRoleManager().getManageableGroups(getCms(), "", true));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> listItems = super.getListItems();
        Iterator<CmsListItem> itListItems = listItems.iterator();
        while (itListItems.hasNext()) {
            CmsListItem item = itListItems.next();
            CmsGroup group = getCms().readGroup(new CmsUUID(item.getId()));
            item.set(LIST_COLUMN_ORGUNIT, CmsOrganizationalUnit.SEPARATOR + group.getOuFqn());
        }

        return listItems;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        metadata.getColumnDefinition(LIST_COLUMN_USERS).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_ACTIVATE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_DELETE).setVisible(false);

        metadata.getColumnDefinition(LIST_COLUMN_DISPLAY).setWidth("25%");
        metadata.getColumnDefinition(LIST_COLUMN_DESCRIPTION).setWidth("50%");

        // add column for orgunit
        CmsListColumnDefinition orgUnitCol = new CmsListColumnDefinition(LIST_COLUMN_ORGUNIT);
        orgUnitCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_ORGUNIT_0));
        orgUnitCol.setWidth("25%");
        metadata.addColumn(
            orgUnitCol,
            metadata.getColumnDefinitions().indexOf(metadata.getColumnDefinition(LIST_COLUMN_DESCRIPTION)));

    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#setDeleteAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#setEditAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setEditAction(CmsListColumnDefinition editCol) {

        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_OVERVIEW);
        editAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_EDIT_HELP_0));
        editAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "group.png");
        editCol.addDirectAction(editAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        super.setIndependentActions(metadata);

        // add orgunit description details
        CmsListItemDetails orgUnitDescDetails = new CmsListItemDetails(LIST_DETAIL_ORGUNIT_DESC);
        orgUnitDescDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        orgUnitDescDetails.setVisible(false);
        orgUnitDescDetails.setShowActionName(
            Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ORGUNIT_DESC_NAME_0));
        orgUnitDescDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ORGUNIT_DESC_HELP_0));
        orgUnitDescDetails.setHideActionName(
            Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ORGUNIT_DESC_NAME_0));
        orgUnitDescDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ORGUNIT_DESC_HELP_0));
        orgUnitDescDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_ORGUNIT_DESC_NAME_0));
        orgUnitDescDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_USERS_DETAIL_ORGUNIT_DESC_NAME_0)));
        metadata.addItemDetails(orgUnitDescDetails);

        metadata.getSearchAction().addColumn(metadata.getColumnDefinition(LIST_COLUMN_DESCRIPTION));
        metadata.getSearchAction().addColumn(metadata.getColumnDefinition(LIST_COLUMN_ORGUNIT));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupsList#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // no param check needed
    }
}
