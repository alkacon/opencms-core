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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import com.google.common.collect.Lists;

/**
 * Not Usergroups view.<p>
 *
 * @since 6.0.0
 */
public class CmsNotGroupUsersList extends A_CmsGroupUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_ADD = "aa";

    /** list action id constant. */
    public static final String LIST_DEFACTION_ADD = "da";

    /** list id constant. */
    public static final String LIST_ID = "lngu";

    /** list action id constant. */
    public static final String LIST_MACTION_ADD = "ma";

    /** a set of action id's to use for adding. */
    protected static Set<String> m_addActionIds = new HashSet<String>();

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNotGroupUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param lazy the lazy flag
     */
    public CmsNotGroupUsersList(CmsJspActionElement jsp, boolean lazy) {

        this(jsp, LIST_ID, lazy);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNotGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
    public CmsNotGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res, boolean lazy) {

        this(new CmsJspActionElement(context, req, res), lazy);
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsNotGroupUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_NOTGROUPUSERS_LIST_NAME_0), true);
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     * @param lazy the lazy flag
     */
    protected CmsNotGroupUsersList(CmsJspActionElement jsp, String listId, boolean lazy) {

        super(jsp, listId, Messages.get().container(Messages.GUI_NOTGROUPUSERS_LIST_NAME_0), true, lazy);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_ADD)) {
            // execute the remove multiaction
            try {
                Iterator<CmsListItem> itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = itItems.next();
                    getCms().addUserToGroup((String)listItem.get(LIST_COLUMN_LOGIN), getParamGroupname());
                }
            } catch (CmsException e) {
                // refresh the list
                Map<?, ?> objects = (Map<?, ?>)getSettings().getListObject();
                if (objects != null) {
                    objects.remove(CmsGroupsList.class.getName());
                    objects.remove(A_CmsUsersList.class.getName());
                }
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ADD_SELECTED_GROUPS_0), e);
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

        if (m_addActionIds.contains(getParamListAction())) {
            CmsListItem listItem = getSelectedItem();
            try {
                getCms().addUserToGroup((String)listItem.get(LIST_COLUMN_LOGIN), getParamGroupname());
            } catch (CmsException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ADD_SELECTED_GROUP_0), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
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
        if (hasOuDetail()) {
            List<CmsOrganizationalUnit> ous = OpenCms.getRoleManager().getManageableOrgUnits(getCms(), "", true, false);
            params.setAllowedOus(ous);
        } else {
            params.setOrganizationalUnit(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()));
        }
        CmsGroup group = getCms().readGroup(getParamGroupname());
        params.setNotGroup(group);
        String searchFilter = state.getFilter();
        params.setSearchFilter(searchFilter);
        params.setPaging(getList().getMaxItemsPerPage(), state.getPage());
        params.setSorting(getSortKey(state.getColumn()), state.getOrder().equals(CmsListOrderEnum.ORDER_ASCENDING));
        return params;
    }

    /**
     * Gets the sort key for a column.<p>
     *
     * @param column a column
     *
     * @return the sort key
     */
    protected SortKey getSortKey(String column) {

        if (column == null) {
            return null;
        }
        if (column.equals(LIST_COLUMN_NAME)) {
            return SortKey.loginName;
        } else if (column.equals(LIST_COLUMN_FULLNAME)) {
            return SortKey.fullName;
        }
        return null;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#getUsers(boolean)
     */
    @Override
    protected List<CmsUser> getUsers(boolean withOtherOus) throws CmsException {

        List<CmsUser> groupusers = getCms().getUsersOfGroup(getParamGroupname(), withOtherOus);
        List<CmsUser> users;
        if (withOtherOus) {
            users = OpenCms.getRoleManager().getManageableUsers(getCms(), "", true);
        } else {
            users = OpenCms.getRoleManager().getManageableUsers(getCms(), getParamOufqn(), false);
        }
        users.removeAll(groupusers);
        return users;
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

        // add add action
        CmsListDefaultAction addAction = new CmsListDefaultAction(LIST_DEFACTION_ADD);
        addAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_ADD_NAME_0));
        addAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_ADD_HELP_0));
        loginCol.addDefaultAction(addAction);
        // keep the id
        m_addActionIds.add(addAction.getId());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setIconAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setIconAction(CmsListColumnDefinition iconCol) {

        CmsListDirectAction iconAction = new CmsListDefaultAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return ((A_CmsGroupUsersList)getWp()).getIconPath(getItem());
            }
        };
        iconAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_AVAILABLE_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_AVAILABLE_HELP_0));
        iconAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "user.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        super.setIndependentActions(metadata);

        metadata.getItemDetailDefinition(LIST_DETAIL_OTHEROU).setVisible(false);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction addMultiAction = new CmsListMultiAction(LIST_MACTION_ADD);
        addMultiAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ADD_NAME_0));
        addMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ADD_HELP_0));
        addMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_ADD_CONF_0));
        addMultiAction.setIconPath(ICON_MULTI_ADD);
        metadata.addMultiAction(addMultiAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setStateActionCol(CmsListMetadata metadata) {

        // create column for state change
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_STATE_0));
        stateCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_STATE_HELP_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        // add add action
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_ADD);
        stateAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_ADD_NAME_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_ADD_HELP_0));
        stateAction.setIconPath(ICON_ADD);
        stateCol.addDirectAction(stateAction);
        // add it to the list definition
        metadata.addColumn(stateCol);
        // keep the id
        m_addActionIds.add(stateAction.getId());
    }

}
