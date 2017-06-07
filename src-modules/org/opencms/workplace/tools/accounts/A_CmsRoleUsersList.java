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
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generalized role users view.<p>
 *
 * @since 6.5.6
 */
public abstract class A_CmsRoleUsersList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

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

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ORGUNIT = "dou";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Cached value. */
    private Boolean m_hasUsersInOtherOus;

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the role name. */
    private String m_paramRole;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     * @param searchable searchable flag
     */
    protected A_CmsRoleUsersList(
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
            searchable ? LIST_COLUMN_NAME : null,
            false);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     * @param searchable searchable flag
     * @param lazy the lazy flag
     */
    protected A_CmsRoleUsersList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        boolean searchable,
        boolean lazy) {

        super(
            jsp,
            listId,
            listName,
            LIST_COLUMN_LOGIN,
            CmsListOrderEnum.ORDER_ASCENDING,
            searchable ? LIST_COLUMN_NAME : null,
            lazy);
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
     * Returns the role name parameter value.<p>
     *
     * @return the role name parameter value
     */
    public String getParamRole() {

        return m_paramRole;
    }

    /**
     * Returns if the list of users has users of other organizational units.<p>
     *
     * @return if the list of users has users of other organizational units
     */
    public boolean hasUsersInOtherOus() {

        if (m_lazy) {
            // if we use database-side paging, we have to assume that there may be users from other OUs
            return true;
        }
        if (m_hasUsersInOtherOus == null) {
            // lazzy initialization
            m_hasUsersInOtherOus = Boolean.FALSE;
            try {
                Iterator<CmsUser> itUsers = getUsers(true).iterator();
                while (itUsers.hasNext()) {
                    CmsUser user = itUsers.next();
                    if (!user.getOuFqn().equals(getParamOufqn())) {
                        m_hasUsersInOtherOus = Boolean.TRUE;
                        break;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return m_hasUsersInOtherOus.booleanValue();
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
     * Sets the role name parameter value.<p>
     *
     * @param roleName the role name parameter value
     */
    public void setParamRole(String roleName) {

        m_paramRole = roleName;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * Checks if other OUs are visible.<p>
     *
     * @return true if other OUs are visible
     */
    protected boolean otherOrgUnitsVisible() {

        return getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_ORGUNIT).isVisible();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();

        boolean withOtherOus = hasUsersInOtherOus() && otherOrgUnitsVisible();

        // get content
        List<CmsUser> users = getUsers(withOtherOus);
        Iterator<CmsUser> itUsers = users.iterator();
        while (itUsers.hasNext()) {

            CmsUser user = itUsers.next();
            CmsListItem item = makeUserItem(user);
            ret.add(item);
        }

        return ret;
    }

    /**
     * Makes a list item from a user.<p>
     *
     * @param user the user
     *
     * @return the list item
     */
    protected CmsListItem makeUserItem(CmsUser user) {

        CmsListItem item = getList().newItem(user.getId().toString());
        item.set(LIST_COLUMN_LOGIN, user.getName());
        item.set(LIST_COLUMN_NAME, user.getSimpleName());
        item.set(LIST_COLUMN_ORGUNIT, CmsOrganizationalUnit.SEPARATOR + user.getOuFqn());
        item.set(LIST_COLUMN_FULLNAME, user.getFullName());
        return item;
    }

    /**
     * Returns a list of users to display.<p>
     *
     * @param withOtherOus if not set only users of the current ou should be returned
     *
     * @return a list of <code><{@link CmsUser}</code>s
     *
     * @throws CmsException if something goes wrong
     */
    protected abstract List<CmsUser> getUsers(boolean withOtherOus) throws CmsException;

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initializeDetail(java.lang.String)
     */
    @Override
    protected void initializeDetail(String detailId) {

        super.initializeDetail(detailId);
        if (detailId.equals(LIST_DETAIL_ORGUNIT)) {
            boolean visible = hasUsersInOtherOus()
                && getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_ORGUNIT).isVisible();
            getList().getMetadata().getColumnDefinition(LIST_COLUMN_ORGUNIT).setVisible(visible);
            getList().getMetadata().getColumnDefinition(LIST_COLUMN_ORGUNIT).setPrintable(visible);
        }
    }

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

        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.list.CmsListDirectAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
             */
            @Override
            public String buttonHtml(CmsWorkplace wp) {

                if (!isVisible()) {
                    return "";
                }
                return A_CmsHtmlIconButton.defaultButtonHtml(
                    resolveButtonStyle(),
                    getId() + getItem().getId(),
                    getId() + getItem().getId(),
                    resolveName(wp.getLocale()),
                    resolveHelpText(wp.getLocale()),
                    isEnabled(),
                    getIconPath(),
                    null,
                    resolveOnClic(wp.getLocale()),
                    false,
                    null);
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            @Override
            public CmsMessageContainer getHelpText() {

                try {
                    CmsUser user = getCms().readUser((String)getItem().get(LIST_COLUMN_LOGIN));
                    if (user.getOuFqn().equals(((A_CmsRoleUsersList)getWp()).getParamOufqn())) {
                        List<CmsRole> userRoles = OpenCms.getRoleManager().getRolesOfUser(
                            ((A_CmsRoleUsersList)getWp()).getCms(),
                            user.getName(),
                            ((A_CmsRoleUsersList)getWp()).getParamOufqn(),
                            false,
                            true,
                            true);
                        Iterator<CmsRole> itUserRoles = userRoles.iterator();
                        while (itUserRoles.hasNext()) {
                            CmsRole role = itUserRoles.next();
                            if (role.getGroupName().equals(((A_CmsRoleUsersList)getWp()).getParamRole())) {
                                return Messages.get().container(Messages.GUI_USERS_LIST_INROLE_HELP_0);
                            }
                        }
                        return Messages.get().container(Messages.GUI_USERS_LIST_INROLE_INDIRECT_HELP_0);

                    } else {
                        return Messages.get().container(Messages.GUI_USERS_LIST_INROLE_OTHEROU_HELP_0);
                    }
                } catch (CmsException e) {
                    return Messages.get().container(Messages.GUI_USERS_LIST_INROLE_HELP_0);
                }
            }

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                try {
                    CmsUser user = getCms().readUser((String)getItem().get(LIST_COLUMN_LOGIN));
                    if (user.getOuFqn().equals(((A_CmsRoleUsersList)getWp()).getParamOufqn())) {
                        List<CmsRole> userRoles = OpenCms.getRoleManager().getRolesOfUser(
                            ((A_CmsRoleUsersList)getWp()).getCms(),
                            user.getName(),
                            ((A_CmsRoleUsersList)getWp()).getParamOufqn(),
                            false,
                            true,
                            true);
                        Iterator<CmsRole> itUserRoles = userRoles.iterator();
                        while (itUserRoles.hasNext()) {
                            CmsRole role = itUserRoles.next();
                            if (role.getGroupName().equals(((A_CmsRoleUsersList)getWp()).getParamRole())) {
                                return A_CmsUsersList.PATH_BUTTONS + "user.png";
                            }
                        }
                        return A_CmsUsersList.PATH_BUTTONS + "user_indirect.png";

                    } else {
                        return A_CmsUsersList.PATH_BUTTONS + "user_other_ou.png";
                    }
                } catch (CmsException e) {
                    return A_CmsUsersList.PATH_BUTTONS + "user.png";
                }
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getName()
             */
            @Override
            public CmsMessageContainer getName() {

                try {
                    CmsUser user = getCms().readUser((String)getItem().get(LIST_COLUMN_LOGIN));
                    if (user.getOuFqn().equals(((A_CmsRoleUsersList)getWp()).getParamOufqn())) {
                        List<CmsRole> userRoles = OpenCms.getRoleManager().getRolesOfUser(
                            ((A_CmsRoleUsersList)getWp()).getCms(),
                            user.getName(),
                            ((A_CmsRoleUsersList)getWp()).getParamOufqn(),
                            false,
                            true,
                            true);
                        Iterator<CmsRole> itUserRoles = userRoles.iterator();
                        while (itUserRoles.hasNext()) {
                            CmsRole role = itUserRoles.next();
                            if (role.getGroupName().equals(((A_CmsRoleUsersList)getWp()).getParamRole())) {
                                return Messages.get().container(Messages.GUI_USERS_LIST_INROLE_NAME_0);
                            }
                        }
                        return Messages.get().container(Messages.GUI_USERS_LIST_INROLE_INDIRECT_NAME_0);

                    } else {
                        return Messages.get().container(Messages.GUI_USERS_LIST_INROLE_OTHEROU_NAME_0);
                    }
                } catch (CmsException e) {
                    return Messages.get().container(Messages.GUI_USERS_LIST_INROLE_NAME_0);
                }
            }
        };
        iconAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_INROLE_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_INROLE_HELP_0));
        iconAction.setIconPath(PATH_BUTTONS + "user.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        // create column for login
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN);
        loginCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(loginCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LOGIN_0));
        nameCol.setWidth("35%");
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for orgunit
        CmsListColumnDefinition orgunitCol = new CmsListColumnDefinition(LIST_COLUMN_ORGUNIT);
        orgunitCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ORGUNIT_0));
        orgunitCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(orgunitCol);

        // create column for fullname
        CmsListColumnDefinition fullnameCol = new CmsListColumnDefinition(LIST_COLUMN_FULLNAME);
        fullnameCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_FULLNAME_0));
        fullnameCol.setWidth("65%");
        fullnameCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(fullnameCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add other ou button
        CmsListItemDetails otherOuDetails = new CmsListItemDetails(LIST_DETAIL_ORGUNIT);
        otherOuDetails.setAtColumn(LIST_COLUMN_NAME);
        otherOuDetails.setVisible(false);
        otherOuDetails.setHideAction(new CmsListIndependentAction(LIST_DETAIL_ORGUNIT) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_HIDE;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                return ((A_CmsRoleUsersList)getWp()).hasUsersInOtherOus();
            }
        });
        otherOuDetails.setShowAction(new CmsListIndependentAction(LIST_DETAIL_ORGUNIT) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_SHOW;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                return ((A_CmsRoleUsersList)getWp()).hasUsersInOtherOus();
            }
        });
        otherOuDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_OTHEROU_NAME_0));
        otherOuDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_OTHEROU_HELP_0));
        otherOuDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_OTHEROU_NAME_0));
        otherOuDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_OTHEROU_HELP_0));
        otherOuDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_OTHEROU_NAME_0));
        otherOuDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_USERS_DETAIL_OTHEROU_NAME_0)));
        otherOuDetails.setVisible(true);
        metadata.addItemDetails(otherOuDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        CmsRole.valueOf(getCms().readGroup(getParamRole())).getRoleName();
    }

}
