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
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListState;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.common.collect.Lists;

/**
 * Role users view.<p>
 *
 * @since 6.5.6
 */
public class CmsRoleUsersList extends A_CmsRoleUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE = "ar";

    /** list action id constant. */
    public static final String LIST_DEFACTION_REMOVE = "dr";

    /** list id constant. */
    public static final String LIST_ID = "lru";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE = "mr";

    /** a set of action id's to use for removing. */
    protected static Set<String> m_removeActionIds = new HashSet<String>();

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRoleUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRoleUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsRoleUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_ROLEUSERS_LIST_NAME_0), true);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param lazy the lazy flag
     */
    public CmsRoleUsersList(CmsJspActionElement jsp, boolean lazy) {

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
    public CmsRoleUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res, boolean lazy) {

        this(new CmsJspActionElement(context, req, res), lazy);
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     * @param lazy the lazy flag
     */
    protected CmsRoleUsersList(CmsJspActionElement jsp, String listId, boolean lazy) {

        super(jsp, listId, Messages.get().container(Messages.GUI_ROLEUSERS_LIST_NAME_0), true, lazy);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_REMOVE)) {
            // execute the remove multiaction
            Iterator<CmsListItem> itItems = getSelectedItems().iterator();
            while (itItems.hasNext()) {
                CmsListItem listItem = itItems.next();
                String userName = (String)listItem.get(LIST_COLUMN_LOGIN);
                try {
                    if (getCms().readUser(userName).getOuFqn().equals(getParamOufqn())) {
                        OpenCms.getRoleManager().removeUserFromRole(
                            getCms(),
                            CmsRole.valueOf(getCms().readGroup(getParamRole())),
                            userName);
                    }
                } catch (CmsException e) {
                    // noop
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
    public void executeListSingleActions() throws CmsRuntimeException {

        if (m_removeActionIds.contains(getParamListAction())) {
            CmsListItem listItem = getSelectedItem();
            try {
                OpenCms.getRoleManager().removeUserFromRole(
                    getCms(),
                    CmsRole.valueOf(getCms().readGroup(getParamRole())),
                    (String)listItem.get(LIST_COLUMN_LOGIN));
            } catch (CmsException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_REMOVE_SELECTED_GROUP_0), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
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
            true);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRoleUsersList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        if (m_lazy) {
            metadata.setSelfManaged(true);
        }
        super.setColumns(metadata);
        // create column for state change
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_STATE_0));
        stateCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_STATE_HELP_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        // add remove action
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_REMOVE);
        stateAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_ROLEUSERS_LIST_DEFACTION_REMOVE_HELP_0));
        stateAction.setIconPath(ICON_MINUS);
        stateCol.addDirectAction(stateAction);
        // add it to the list definition
        metadata.addColumn(stateCol, 1);
        // keep the id
        m_removeActionIds.add(stateAction.getId());
    }

    /**
     * Sets the optional login default action.<p>
     *
     * @param loginCol the user login column
     */
    protected void setDefaultAction(CmsListColumnDefinition loginCol) {

        // add default remove action
        CmsListDefaultAction removeAction = new CmsListDefaultAction(LIST_DEFACTION_REMOVE);
        removeAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_HELP_0));
        loginCol.addDefaultAction(removeAction);
        // keep the id
        m_removeActionIds.add(removeAction.getId());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add remove multi action
        CmsListMultiAction removeMultiAction = new CmsListMultiAction(LIST_MACTION_REMOVE);
        removeMultiAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_NAME_0));
        removeMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_HELP_0));
        removeMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_CONF_0));
        removeMultiAction.setIconPath(ICON_MULTI_MINUS);
        metadata.addMultiAction(removeMultiAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRoleUsersList#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        super.validateParamaters();
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.valueOf(getCms().readGroup(getParamRole())));
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
        getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_ORGUNIT).isVisible();
        params.setPaging(getList().getMaxItemsPerPage(), state.getPage());
        CmsRole role = CmsRole.valueOf(getCms().readGroup(getParamRole()));
        Set<CmsGroup> roleGroups = OpenCms.getRoleManager().getRoleGroups(getCms(), role, false);
        params.setAnyGroups(roleGroups);
        params.setSorting(getSortKey(state.getColumn()), state.getOrder().equals(CmsListOrderEnum.ORDER_ASCENDING));
        CmsGroup group = getCms().readGroup(getParamRole());
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
        } else if (column.equals(LIST_COLUMN_ORGUNIT)) {
            return SortKey.orgUnit;
        }
        return null;
    }
}
