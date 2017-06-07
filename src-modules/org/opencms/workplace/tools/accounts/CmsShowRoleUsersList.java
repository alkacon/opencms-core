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
import org.opencms.file.CmsUserSearchParameters;
import org.opencms.file.CmsUserSearchParameters.SortKey;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListState;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.common.collect.Lists;

/**
 * Role users list view.<p>
 *
 * @since 6.5.6
 */
public class CmsShowRoleUsersList extends A_CmsRoleUsersList {

    /** list id constant. */
    public static final String LIST_ID = "lsru";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsShowRoleUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsShowRoleUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsShowRoleUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_ROLEUSERS_LIST_NAME_0), true);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param lazy the lazy flag
     */
    public CmsShowRoleUsersList(CmsJspActionElement jsp, boolean lazy) {

        this(jsp, LIST_ID, lazy);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     * @param lazy the lazy flag
     */
    public CmsShowRoleUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res, boolean lazy) {

        this(new CmsJspActionElement(context, req, res), lazy);
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     * @param lazy the lazy flag
     */
    protected CmsShowRoleUsersList(CmsJspActionElement jsp, String listId, boolean lazy) {

        super(jsp, listId, Messages.get().container(Messages.GUI_ROLEUSERS_LIST_NAME_0), true, lazy);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    public String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRoleUsersList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    public void setColumns(CmsListMetadata meta) {

        if (m_lazy) {
            meta.setSelfManaged(true);
        }
        super.setColumns(meta);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRoleUsersList#getUsers(boolean)
     */
    @Override
    protected List<CmsUser> getUsers(boolean withOtherOus) throws CmsException {

        return OpenCms.getRoleManager().getUsersOfRole(
            getCms(),
            CmsRole.valueOf(getCms().readGroup(getParamRole())),
            withOtherOus,
            false);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRoleUsersList#getListItems()
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
                CmsListItem item = makeUserItem(user);
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
        if (!otherOrgUnitsVisible()) {
            CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn());
            params.setOrganizationalUnit(ou);
        }
        params.setPaging(getList().getMaxItemsPerPage(), state.getPage());
        CmsRole role = CmsRole.valueOf(getCms().readGroup(getParamRole()));
        Set<CmsGroup> roleGroups = OpenCms.getRoleManager().getRoleGroups(getCms(), role, false);
        params.setAnyGroups(roleGroups);
        params.setSorting(getSortKey(state.getColumn()), state.getOrder().equals(CmsListOrderEnum.ORDER_ASCENDING));
        params.setFilterByGroupOu(false);
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
        if (column.equals(LIST_COLUMN_FULLNAME)) {
            return SortKey.fullName;
        } else if (column.equals(LIST_COLUMN_NAME)) {
            return SortKey.loginName;
        } else if (column.equals(LIST_COLUMN_ORGUNIT)) {
            return SortKey.orgUnit;
        }
        return null;
    }

}
