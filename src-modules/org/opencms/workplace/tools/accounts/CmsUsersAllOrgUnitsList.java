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

import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserSearchParameters;
import org.opencms.file.CmsUserSearchParameters.SearchKey;
import org.opencms.file.CmsUserSearchParameters.SortKey;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.common.collect.Lists;

/**
 * User account view over all manageable organizational units.<p>
 *
 * @since 6.5.6
 */
public class CmsUsersAllOrgUnitsList extends A_CmsUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_OVERVIEW = "ao";

    /** list column id constant. */
    public static final String LIST_COLUMN_ORGUNIT = "co";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ORGUNIT_DESC = "dd";

    /** list id constant. */
    public static final String LIST_ID = "lsuaou";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUsersAllOrgUnitsList(CmsJspActionElement jsp) {

        this(jsp, false);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUsersAllOrgUnitsList(CmsJspActionElement jsp, boolean lazy) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_USERS_LIST_NAME_0), lazy);
    }

    /**
     * Public constructor.<p>
     *
     * @param context a page context
     * @param req a request
     * @param res a response
     */
    public CmsUsersAllOrgUnitsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(context, req, res, false);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     * @param lazy the lazy flag
     */
    public CmsUsersAllOrgUnitsList(PageContext context, HttpServletRequest req, HttpServletResponse res, boolean lazy) {

        this(new CmsJspActionElement(context, req, res), lazy);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        String userId = getSelectedItem().getId();

        Map params = new HashMap();
        params.put(A_CmsEditUserDialog.PARAM_USERID, userId);
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, getSelectedItem().get(LIST_COLUMN_ORGUNIT).toString().substring(1));
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_ACTION_OVERVIEW)) {
            // forward
            getToolManager().jspForwardTool(this, "/accounts/orgunit/users/edit", params);
        } else if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/accounts/orgunit/users/edit", params);
        } else {
            super.executeListSingleActions();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        super.fillDetails(detailId);

        List users = getList().getAllContent();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsListItem item = (CmsListItem)itUsers.next();
            String userName = item.get(LIST_COLUMN_LOGIN).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_ORGUNIT_DESC)) {
                    CmsUser user = readUser(userName);
                    html.append(
                        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), user.getOuFqn()).getDescription(
                            getLocale()));
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
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getGroupIcon()
     */
    @Override
    protected String getGroupIcon() {

        return null;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        if (!m_lazy) {
            return super.getListItems();
        } else {

            CmsUserSearchParameters params = getSearchParams();

            List<CmsUser> users = OpenCms.getOrgUnitManager().searchUsers(getCms(), params);
            int count = (int)OpenCms.getOrgUnitManager().countUsers(getCms(), params);
            getList().setSize(count);
            List<CmsListItem> result = Lists.newArrayList();
            for (CmsUser user : users) {
                CmsListItem item = makeListItemForUser(user);
                result.add(item);
            }
            return result;
        }
    }

    /**
     * Gets the search parameters.<p>
     *
     * @return the search parameters
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsUserSearchParameters getSearchParams() throws CmsException {

        CmsListState state = getListState();
        List<CmsOrganizationalUnit> ous = OpenCms.getRoleManager().getManageableOrgUnits(getCms(), "", true, false);
        CmsUserSearchParameters params = new CmsUserSearchParameters();
        params.setAllowedOus(ous);
        String searchFilter = state.getFilter();
        params.addSearch(SearchKey.email);
        params.addSearch(SearchKey.orgUnit);
        params.setSearchFilter(searchFilter);
        params.setFilterCore(true);
        params.setPaging(getList().getMaxItemsPerPage(), state.getPage());
        params.setSorting(getSortKey(state.getColumn()), state.getOrder().equals(CmsListOrderEnum.ORDER_ASCENDING));
        return params;
    }

    /**
     * Returns the sort key for the column.<p>
     *
     * @param column the list column
     * @return the sort key
     */
    protected SortKey getSortKey(String column) {

        if (column == null) {
            return null;
        }
        if (column.equals(LIST_COLUMN_ENABLED)) {
            return SortKey.activated;
        } else if (column.equals(LIST_COLUMN_LASTLOGIN)) {
            return SortKey.lastLogin;
        } else if (column.equals(LIST_COLUMN_DISPLAY)) {
            return SortKey.loginName;
        } else if (column.equals(LIST_COLUMN_NAME)) {
            return SortKey.fullName;
        } else if (column.equals(LIST_COLUMN_EMAIL)) {
            return SortKey.email;
        } else if (column.equals(LIST_COLUMN_ORGUNIT)) {
            return SortKey.orgUnit;
        }
        return null;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getUsers()
     */
    @Override
    protected List<CmsUser> getUsers() throws CmsException {

        return CmsPrincipal.filterCoreUsers(OpenCms.getRoleManager().getManageableUsers(getCms(), "", true));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#readUser(java.lang.String)
     */
    @Override
    protected CmsUser readUser(String name) throws CmsException {

        return getCms().readUser(name);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        if (m_lazy) {
            metadata.setSelfManaged(true);
        }
        super.setColumns(metadata);
        metadata.getColumnDefinition(LIST_COLUMN_GROUPS).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_ROLE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_ACTIVATE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_DELETE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_LASTLOGIN).setVisible(false);

        // add column for orgunit
        CmsListColumnDefinition orgUnitCol = new CmsListColumnDefinition(LIST_COLUMN_ORGUNIT);
        orgUnitCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ORGUNIT_0));
        orgUnitCol.setWidth("30%");
        metadata.addColumn(
            orgUnitCol,
            metadata.getColumnDefinitions().indexOf(metadata.getColumnDefinition(LIST_COLUMN_NAME)));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setDeleteAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setEditAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setEditAction(CmsListColumnDefinition editCol) {

        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_OVERVIEW);
        editAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_HELP_0));
        editAction.setIconPath(PATH_BUTTONS + "user.png");
        editCol.addDirectAction(editAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
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
        metadata.getSearchAction().addColumn(metadata.getColumnDefinition(LIST_COLUMN_EMAIL));
        metadata.getSearchAction().addColumn(metadata.getColumnDefinition(LIST_COLUMN_ORGUNIT));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setUserData(org.opencms.file.CmsUser, org.opencms.workplace.list.CmsListItem)
     */
    @Override
    protected void setUserData(CmsUser user, CmsListItem item) {

        super.setUserData(user, item);
        item.set(LIST_COLUMN_ORGUNIT, CmsOrganizationalUnit.SEPARATOR + user.getOuFqn());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // no param check needed
    }

}
