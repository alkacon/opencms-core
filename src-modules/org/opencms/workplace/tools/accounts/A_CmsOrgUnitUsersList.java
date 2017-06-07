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
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generalized organizational unit users view.<p>
 *
 * @since 6.5.6
 */
public abstract class A_CmsOrgUnitUsersList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE = "as";

    /** list column id constant. */
    public static final String LIST_COLUMN_FULLNAME = "cf";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOGIN = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ORGUNIT = "co";

    /** list column id constant. */
    public static final String LIST_COLUMN_STATE = "cs";

    /** Constant for session attribute. */
    public static final String NOT_ORGUNIT_USERS = "not_orgunit_users";

    /** Constant for session attribute. */
    public static final String ORGUNIT_USERS = "orgunit_users";

    /** Stores the users not in the current ou.*/
    private List<CmsUser> m_notOuUsers;

    /** Stores the users of the the current ou.*/
    private List<CmsUser> m_ouUsers;

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     * @param searchable searchable flag
     */
    protected A_CmsOrgUnitUsersList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        boolean searchable) {

        super(
            jsp,
            listId,
            listName,
            LIST_COLUMN_LOGIN,
            CmsListOrderEnum.ORDER_ASCENDING,
            searchable ? LIST_COLUMN_NAME : null);
    }

    /**
     * Returns the notOuUsers.<p>
     *
     * @return the notOuUsers
     */
    public List<CmsUser> getNotOuUsers() {

        return m_notOuUsers;
    }

    /**
     * Returns the ouUsers.<p>
     *
     * @return the ouUsers
     */
    public List<CmsUser> getOuUsers() {

        return m_ouUsers;
    }

    /**
     * Returns the right icon path for the given list item.<p>
     *
     * @param item the list item to get the icon path for
     *
     * @return the icon path for the given role
     */
    public String getIconPath(CmsListItem item) {

        try {
            CmsUser user = getCms().readUser((String)item.get(LIST_COLUMN_LOGIN));
            if (user.getOuFqn().equals(getParamOufqn())) {
                return A_CmsUsersList.PATH_BUTTONS + "user.png";
            } else {
                return A_CmsUsersList.PATH_BUTTONS + "user_other_ou.png";
            }
        } catch (CmsException e) {
            return A_CmsUsersList.PATH_BUTTONS + "user.png";
        }
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
     * Sets the notOuUsers.<p>
     *
     * @param notOuUsers the notOuUsers to set
     */
    public void setNotOuUsers(List<CmsUser> notOuUsers) {

        m_notOuUsers = notOuUsers;
        getJsp().getRequest().getSession().setAttribute(A_CmsOrgUnitUsersList.NOT_ORGUNIT_USERS, m_notOuUsers);
    }

    /**
     * Sets the ouUsers.<p>
     *
     * @param ouUsers the ouUsers to set
     */
    public void setOuUsers(List<CmsUser> ouUsers) {

        m_ouUsers = ouUsers;
        getJsp().getRequest().getSession().setAttribute(A_CmsOrgUnitUsersList.ORGUNIT_USERS, m_ouUsers);
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

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();

        // get content
        List<CmsUser> users = getUsers();
        Iterator<CmsUser> itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsUser user = itUsers.next();
            CmsListItem item = getList().newItem(user.getId().toString());
            item.set(LIST_COLUMN_LOGIN, user.getName());
            item.set(LIST_COLUMN_NAME, user.getSimpleName());
            item.set(LIST_COLUMN_ORGUNIT, CmsOrganizationalUnit.SEPARATOR + user.getOuFqn());
            item.set(LIST_COLUMN_FULLNAME, user.getFullName());
            ret.add(item);
        }

        return ret;
    }

    /**
     * Returns a list of users to display.<p>
     *
     * @return a list of <code><{@link CmsUser}</code>s
     *
     * @throws CmsException if something goes wrong
     */
    protected abstract List<CmsUser> getUsers() throws CmsException;

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        // set icon action
        setIconAction(iconCol);
        // add it to the list definition
        metadata.addColumn(iconCol);

        setStateActionCol(metadata);

        // create column for login
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN);
        loginCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(loginCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LOGIN_0));
        nameCol.setWidth("20%");
        setDefaultAction(nameCol);
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for organizational unit
        CmsListColumnDefinition orgUnitCol = new CmsListColumnDefinition(LIST_COLUMN_ORGUNIT);
        orgUnitCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ORGUNIT_0));
        orgUnitCol.setWidth("40%");
        // add it to the list definition
        metadata.addColumn(orgUnitCol);

        // create column for fullname
        CmsListColumnDefinition fullnameCol = new CmsListColumnDefinition(LIST_COLUMN_FULLNAME);
        fullnameCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_FULLNAME_0));
        fullnameCol.setWidth("40%");
        fullnameCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(fullnameCol);
    }

    /**
     * Sets the optional login default action.<p>
     *
     * @param loginCol the login column
     */
    protected abstract void setDefaultAction(CmsListColumnDefinition loginCol);

    /**
     * Sets the needed icon action(s).<p>
     *
     * @param iconCol the list column for edition.
     */
    protected abstract void setIconAction(CmsListColumnDefinition iconCol);

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * Sets the optional state change action column.<p>
     *
     * @param metadata the list metadata object
     */
    protected abstract void setStateActionCol(CmsListMetadata metadata);

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), m_paramOufqn).getName();
    }
}
