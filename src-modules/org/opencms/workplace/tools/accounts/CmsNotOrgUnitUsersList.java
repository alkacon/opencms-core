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
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Not organizational unit users view.<p>
 *
 * @since 6.5.6
 */
public class CmsNotOrgUnitUsersList extends A_CmsOrgUnitUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_ADD = "aa";

    /** list action id constant. */
    public static final String LIST_DEFACTION_ADD = "da";

    /** list id constant. */
    public static final String LIST_ID = "lnouu";

    /** list action id constant. */
    public static final String LIST_MACTION_ADD = "ma";

    /** a set of action id's to use for adding. */
    protected static Set<String> m_addActionIds = new HashSet<String>();

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNotOrgUnitUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNotOrgUnitUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsNotOrgUnitUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_NOTORGUNITUSERS_LIST_NAME_0), true);
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

                    CmsUser user = getCms().readUser((String)listItem.get(LIST_COLUMN_LOGIN));
                    List<CmsUser> currentUsers = OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false);

                    boolean inOrgUnit = false;
                    Iterator<CmsUser> itCurrentUsers = currentUsers.iterator();
                    while (itCurrentUsers.hasNext()) {
                        CmsUser currentUser = itCurrentUsers.next();
                        if (currentUser.getSimpleName().equals(user.getSimpleName())) {
                            inOrgUnit = true;
                        }
                    }
                    if (!inOrgUnit) {
                        @SuppressWarnings("unchecked")
                        List<CmsUser> ouUsers = (ArrayList<CmsUser>)getJsp().getRequest().getSession().getAttribute(
                            A_CmsOrgUnitUsersList.ORGUNIT_USERS);
                        if (ouUsers == null) {
                            ouUsers = new ArrayList<CmsUser>();
                        }
                        ouUsers.add(user);
                        setOuUsers(ouUsers);

                        @SuppressWarnings("unchecked")
                        List<CmsUser> notOuUsers = (ArrayList<CmsUser>)getJsp().getRequest().getSession().getAttribute(
                            A_CmsOrgUnitUsersList.NOT_ORGUNIT_USERS);
                        notOuUsers.remove(user);
                        setNotOuUsers(notOuUsers);
                    }
                }
            } catch (CmsException e) {
                // noop
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
                CmsUser user = getCms().readUser((String)listItem.get(LIST_COLUMN_LOGIN));
                @SuppressWarnings("unchecked")
                List<CmsUser> ouUsers = (ArrayList<CmsUser>)getJsp().getRequest().getSession().getAttribute(
                    A_CmsOrgUnitUsersList.ORGUNIT_USERS);
                if (ouUsers == null) {
                    ouUsers = new ArrayList<CmsUser>();
                }
                ouUsers.add(user);
                setOuUsers(ouUsers);

                @SuppressWarnings("unchecked")
                List<CmsUser> notOuUsers = (ArrayList<CmsUser>)getJsp().getRequest().getSession().getAttribute(
                    A_CmsOrgUnitUsersList.NOT_ORGUNIT_USERS);
                notOuUsers.remove(user);
                setNotOuUsers(notOuUsers);
            } catch (CmsException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ADD_SELECTED_ORGUNITUSER_0), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsOrgUnitUsersList#getUsers()
     */
    @Override
    protected List<CmsUser> getUsers() throws CmsException {

        @SuppressWarnings("unchecked")
        List<CmsUser> notOuUsers = (ArrayList<CmsUser>)getJsp().getRequest().getSession().getAttribute(
            A_CmsOrgUnitUsersList.NOT_ORGUNIT_USERS);

        if (notOuUsers == null) {
            List<CmsUser> orgUnitsUser = OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false);
            List<CmsUser> notOrgUnitUsers = OpenCms.getRoleManager().getManageableUsers(getCms(), "", true);

            notOrgUnitUsers.removeAll(orgUnitsUser);
            setNotOuUsers(notOrgUnitUsers);
        } else {
            setNotOuUsers(notOuUsers);
        }

        return getNotOuUsers();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsOrgUnitUsersList#setDefaultAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setDefaultAction(CmsListColumnDefinition loginCol) {

        // add add action
        CmsListDefaultAction addAction = new CmsListDefaultAction(LIST_DEFACTION_ADD) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            @Override
            public CmsMessageContainer getHelpText() {

                if (!isEnabled()) {
                    return Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_DISABLED_DELETE_HELP_0);
                }
                return super.getHelpText();
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            @Override
            public boolean isEnabled() {

                if (getItem() != null) {
                    try {
                        String userName = getItem().get(LIST_COLUMN_NAME).toString();
                        List<CmsUser> currentUsers = OpenCms.getOrgUnitManager().getUsers(
                            getWp().getCms(),
                            ((A_CmsOrgUnitUsersList)getWp()).getParamOufqn(),
                            false);
                        Iterator<CmsUser> itCurrentUsers = currentUsers.iterator();
                        while (itCurrentUsers.hasNext()) {
                            CmsUser user = itCurrentUsers.next();
                            if (user.getSimpleName().equals(userName)) {
                                return false;
                            }
                            if (((A_CmsOrgUnitUsersList)getWp()).getCms().getGroupsOfUser(
                                getItem().get(LIST_COLUMN_LOGIN).toString(),
                                false).size() > 0) {
                                return false;
                            }
                        }
                        return true;
                    } catch (CmsException e) {
                        return super.isVisible();
                    }
                }
                return super.isVisible();
            }
        };
        addAction.setName(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_DEFACTION_ADD_NAME_0));
        addAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_DEFACTION_ADD_HELP_0));
        loginCol.addDefaultAction(addAction);
        // keep the id
        m_addActionIds.add(addAction.getId());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsOrgUnitUsersList#setIconAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setIconAction(CmsListColumnDefinition iconCol) {

        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return ((A_CmsOrgUnitUsersList)getWp()).getIconPath(getItem());
            }
        };
        iconAction.setName(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_AVAILABLE_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_AVAILABLE_HELP_0));
        iconAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "user.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction addMultiAction = new CmsListMultiAction(LIST_MACTION_ADD);
        addMultiAction.setName(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_MACTION_ADD_NAME_0));
        addMultiAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_MACTION_ADD_HELP_0));
        addMultiAction.setIconPath(ICON_MULTI_ADD);
        metadata.addMultiAction(addMultiAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsOrgUnitUsersList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setStateActionCol(CmsListMetadata metadata) {

        // create column for state change
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_COLS_STATE_0));
        stateCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_COLS_STATE_HELP_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        // add add action
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_ADD) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            @Override
            public CmsMessageContainer getHelpText() {

                if (!isEnabled()) {
                    return Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_DISABLED_DELETE_HELP_0);
                }
                return super.getHelpText();
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            @Override
            public boolean isEnabled() {

                if (getItem() != null) {
                    try {
                        String userName = getItem().get(LIST_COLUMN_NAME).toString();
                        List<CmsUser> currentUsers = OpenCms.getOrgUnitManager().getUsers(
                            getWp().getCms(),
                            ((A_CmsOrgUnitUsersList)getWp()).getParamOufqn(),
                            false);
                        Iterator<CmsUser> itCurrentUsers = currentUsers.iterator();
                        while (itCurrentUsers.hasNext()) {
                            CmsUser user = itCurrentUsers.next();
                            if (user.getSimpleName().equals(userName)) {
                                return false;
                            }
                            if (((A_CmsOrgUnitUsersList)getWp()).getCms().getGroupsOfUser(
                                getItem().get(LIST_COLUMN_LOGIN).toString(),
                                false).size() > 0) {
                                return false;
                            }
                        }
                        return true;
                    } catch (CmsException e) {
                        return super.isVisible();
                    }
                }
                return super.isVisible();
            }
        };
        stateAction.setName(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_DEFACTION_ADD_NAME_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITUSERS_LIST_DEFACTION_ADD_HELP_0));
        stateAction.setIconPath(ICON_ADD);
        stateCol.addDirectAction(stateAction);
        // add it to the list definition
        metadata.addColumn(stateCol);
        // keep the id
        m_addActionIds.add(stateAction.getId());
    }

}
