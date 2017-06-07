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
import org.opencms.file.CmsUserSearchParameters.SortKey;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListState;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.common.collect.Lists;

/**
 * Main system user account management view.<p>
 *
 * @since 6.0.0
 */
public class CmsUsersList extends A_CmsUsersList {

    /** list id constant. */
    public static final String LIST_ID = "lsu";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUsersList(CmsJspActionElement jsp) {

        this(jsp, false);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param lazy the lazy flag
     */
    public CmsUsersList(CmsJspActionElement jsp, boolean lazy) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_USERS_LIST_NAME_0), lazy);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res), false);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res, boolean lazy) {

        this(new CmsJspActionElement(context, req, res), lazy);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getGroupIcon()
     */
    @Override
    protected String getGroupIcon() {

        return PATH_BUTTONS + "group.png";
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
        CmsUserSearchParameters params = new CmsUserSearchParameters();
        String searchFilter = state.getFilter();
        params.setSearchFilter(searchFilter);
        params.setFilterCore(true);
        params.setPaging(getList().getMaxItemsPerPage(), state.getPage());
        params.setSorting(getSortKey(state.getColumn()), state.getOrder().equals(CmsListOrderEnum.ORDER_ASCENDING));
        params.setOrganizationalUnit(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()));
        params.setRecursiveOrgUnits(false);
        return params;
    }

    /**
     * Gets the sort key for a column.<p>
     *
     * @param column a column
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
        }
        return null;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getUsers()
     */
    @Override
    protected List<CmsUser> getUsers() throws CmsException {

        return CmsPrincipal.filterCoreUsers(OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false));
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
        metadata.getColumnDefinition(LIST_COLUMN_ENABLED).setVisible(true);
        metadata.getColumnDefinition(LIST_COLUMN_ACTIVATE).setVisible(false);

    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setDeleteAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {

        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_HELP_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setEditAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setEditAction(CmsListColumnDefinition editCol) {

        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_HELP_0));
        editAction.setIconPath(PATH_BUTTONS + "user.png");
        editCol.addDirectAction(editAction);
    }

}