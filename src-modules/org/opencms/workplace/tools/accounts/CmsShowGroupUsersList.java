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
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListState;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.common.collect.Lists;

/**
 * User groups overview view.<p>
 *
 * @since 6.0.0
 */
public class CmsShowGroupUsersList extends A_CmsGroupUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTLOGIN = "cl";

    /** list id constant. */
    public static final String LIST_ID = "lsgu";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsShowGroupUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param lazy <code>true</code> for lazy initialization
     */
    public CmsShowGroupUsersList(CmsJspActionElement jsp, boolean lazy) {

        this(jsp, LIST_ID, lazy);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsShowGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     * @param lazy the lazy flag
     */
    public CmsShowGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res, boolean lazy) {

        this(new CmsJspActionElement(context, req, res), lazy);
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsShowGroupUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_GROUPUSERS_LIST_NAME_0), false);
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     * @param lazy the lazy flag
     */
    protected CmsShowGroupUsersList(CmsJspActionElement jsp, String listId, boolean lazy) {

        super(jsp, listId, Messages.get().container(Messages.GUI_GROUPUSERS_LIST_NAME_0), false, lazy);
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
    public void executeListSingleActions() throws IOException, ServletException {

        String userId = getSelectedItem().getId();

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
        params.put(A_CmsEditUserDialog.PARAM_USERID, new String[] {userId});
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {getParamOufqn()});

        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            getToolManager().jspForwardTool(
                this,
                getCurrentToolPath().substring(0, getCurrentToolPath().indexOf('/', 2)) + "/orgunit/users/edit/user",
                params);
        } else {
            throwListUnsupportedActionException();
        }
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#getListItems()
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
                CmsListItem item = makeListItem(user);
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
        if (!hasOuDetail()) {
            params.setOrganizationalUnit(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()));
        }
        params.setPaging(getList().getMaxItemsPerPage(), state.getPage());
        params.setSorting(getSortKey(state.getColumn()), state.getOrder().equals(CmsListOrderEnum.ORDER_ASCENDING));
        CmsGroup group = getCms().readGroup(new CmsUUID(getParamGroupid()));
        params.setGroup(group);
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
        }
        return null;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#getUsers(boolean)
     */
    @Override
    protected List<CmsUser> getUsers(boolean withOtherOus) throws CmsException {

        return getCms().getUsersOfGroup(getParamGroupname(), withOtherOus);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata meta) {

        if (m_lazy) {
            meta.setSelfManaged(true);
        }
        super.setColumns(meta);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setDefaultAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setDefaultAction(CmsListColumnDefinition loginCol) {

        CmsListDefaultAction editAction = new CmsListDefaultAction(LIST_ACTION_EDIT) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            @Override
            public boolean isEnabled() {

                return getItem().get(LIST_COLUMN_ORGUNIT).equals(
                    CmsOrganizationalUnit.SEPARATOR + ((CmsShowGroupUsersList)getWp()).getParamOufqn());
            }
        };
        editAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_HELP_0));
        loginCol.addDefaultAction(editAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setIconAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setIconAction(CmsListColumnDefinition iconCol) {

        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return ((A_CmsGroupUsersList)getWp()).getIconPath(getItem());
            }
        };
        iconAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_INGROUP_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_INGROUP_HELP_0));
        iconAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "user.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setStateActionCol(CmsListMetadata metadata) {

        // no-op
    }
}
