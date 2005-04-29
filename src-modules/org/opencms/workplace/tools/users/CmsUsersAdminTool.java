/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/users/Attic/CmsUsersAdminTool.java,v $
 * Date   : $Date: 2005/04/29 16:05:53 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.users;

import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDialog;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsSearchAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main user account management view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.6 $
 * @since 5.7.3
 */
public class CmsUsersAdminTool extends CmsListDialog {

    /**
     * This is an list action implementation, that can be used as direct action
     * as also multi action.<p>
     * 
     * @author Michael Moossen (m.moossen@alkacon.com) 
     * @version $Revision: 1.6 $
     * @since 5.7.3
     */
    private class ActivateUserAction extends CmsListDirectAction {

        /**
         * Default Constructor.<p>
         * 
         * @param listId The id of the associated list
         */
        public ActivateUserAction(String listId) {

            super(
                listId,
                LIST_ACTION_ACTIVATE,
                Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_NAME_0),
                "buttons/user_sm.gif",
                Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_HELP_0),
                true,
                Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_CONF_0));
        }

        /**
         * @see org.opencms.workplace.list.I_CmsListAction#getConfirmationMessage()
         */
        public CmsMessageContainer getConfirmationMessage() {

            if (getItem() != null) {
                try {
                    String usrName = getItem().get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = getCms().readUser(usrName);
                    if (user.getDisabled()) {
                        return Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_ACTCONF_0);
                    }
                    return Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_DESCONF_0);
                } catch (CmsException e) {
                    throw new RuntimeException(e);
                }
            }
            return super.getConfirmationMessage();
        }

        /**
         * @see org.opencms.workplace.list.I_CmsHtmlButton#getHelpText()
         */
        public CmsMessageContainer getHelpText() {

            if (getItem() != null) {
                try {
                    String usrName = getItem().get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = getCms().readUser(usrName);
                    if (user.getDisabled()) {
                        return Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_ACTHELP_0);
                    }
                    return Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_DESHELP_0);
                } catch (CmsException e) {
                    throw new RuntimeException(e);
                }
            }
            return super.getHelpText();
        }

        /**
         * @see org.opencms.workplace.list.I_CmsHtmlIconButton#getIconPath()
         */
        public String getIconPath() {

            if (getItem() != null) {
                try {
                    String usrName = getItem().get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = getCms().readUser(usrName);
                    if (user.getDisabled()) {
                        return "buttons/apply_in.gif";
                    }
                    return "buttons/apply.gif";
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return super.getIconPath();
        }

        /**
         * @see org.opencms.workplace.list.I_CmsHtmlButton#isEnabled()
         */
        public boolean isEnabled() {

            if (getItem() != null) {
                try {
                    String usrName = getItem().get(LIST_COLUMN_LOGIN).toString();
                    return !getCms().userInGroup(usrName, OpenCms.getDefaultUsers().getGroupAdministrators());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return super.isEnabled();
        }
    }

    /** list action id constant. */
    public static final String LIST_ACTION_ACTIVATE = "activate";

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "delete";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "edit";

    /** list action id constant. */
    public static final String LIST_ACTION_REFRESH = "refresh";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIONS = "actions";

    /** list column id constant. */
    public static final String LIST_COLUMN_EMAIL = "email";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTLOGIN = "lastlogin";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOGIN = "login";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "name";

    /** list id constant. */
    public static final String LIST_ID = "users";

    /** metadata for the list used in this dialog. */
    private static CmsListMetadata metadata;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUsersAdminTool(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, LIST_COLUMN_LOGIN);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersAdminTool(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * This method should handle every defined list independent action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     */
    public void executeListIndepActions() {

        if (getParamListAction().equals(LIST_ACTION_REFRESH)) {
            refreshList();
        } else {
            throwListUnsupportedActionException();
        }
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     */
    public void executeListMultiActions() {

        if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // execute the delete multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    getCms().deleteUser(new CmsUUID(listItem.getId()));
                    getList().removeItem(listItem.getId());
                }
            } catch (CmsException e) {
                throw new RuntimeException(e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                    CmsUser user = getCms().readUser(usrName);
                    if (user.getDisabled()) {
                        user.setEnabled();
                    } else {
                        user.setDisabled();
                    }
                    getCms().writeUser(user);
                }
            } catch (CmsException e) {
                throw new RuntimeException(e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * This method should handle every defined list single action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     */
    public void executeListSingleActions() {

        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            String usrName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();
            try {
                // forward to the edit user screen
                Map params = new HashMap();
                params.put("usrName", usrName);
                getToolManager().jspRedirectTool(getJsp(), "/users/edit", params);

            } catch (IOException e) {
                // should never happen
                throw new RuntimeException(e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            CmsListItem listItem = getSelectedItem();
            try {
                String usrName = listItem.get(LIST_COLUMN_LOGIN).toString();
                CmsUser user = getCms().readUser(usrName);
                if (user.getDisabled()) {
                    user.setEnabled();
                } else {
                    user.setDisabled();
                }
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new RuntimeException(e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.CmsListDialog#createList()
     */
    protected CmsHtmlList createList() {

        if (metadata == null) {
            metadata = new CmsListMetadata();

            // adds a reload button
            metadata.addIndependentAction(new CmsListIndependentAction(
                LIST_ID,
                LIST_ACTION_REFRESH,
                Messages.get().container(Messages.GUI_USERS_LIST_ACTION_REFRESH_NAME_0),
                "list/reload.gif",
                Messages.get().container(Messages.GUI_USERS_LIST_ACTION_REFRESH_HELP_0),
                true, // enabled
                Messages.get().container(Messages.GUI_USERS_LIST_ACTION_REFRESH_CONF_0)));

            // add column for direct actions
            CmsListColumnDefinition actionsCol = new CmsListColumnDefinition(
                LIST_COLUMN_ACTIONS,
                Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIONS_0),
                "", // no width
                CmsListColumnAlignEnum.ALIGN_CENTER);
            actionsCol.setSorteable(false);
            I_CmsListDirectAction activateUser = new ActivateUserAction(LIST_ID);
            actionsCol.addDirectAction(activateUser);
            metadata.addColumn(actionsCol);

            // add column for login and default action
            CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN, Messages.get().container(
                Messages.GUI_USERS_LIST_COLS_LOGIN_0), "", // no width
                CmsListColumnAlignEnum.ALIGN_LEFT);
            loginCol.setDefaultAction(new CmsListDefaultAction(LIST_ID, LIST_ACTION_EDIT, Messages.get().container(
                Messages.GUI_USERS_LIST_ACTION_EDITUSER_NAME_0), null, // no icon
                Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDITUSER_HELP_0), true, // enabled
                Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDITUSER_CONF_0)));
            metadata.addColumn(loginCol);

            // add column for name
            CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME, Messages.get().container(
                Messages.GUI_USERS_LIST_COLS_USERNAME_0), "", // no width
                CmsListColumnAlignEnum.ALIGN_LEFT);
            metadata.addColumn(nameCol);

            // add column for email
            CmsListColumnDefinition emailCol = new CmsListColumnDefinition(LIST_COLUMN_EMAIL, Messages.get().container(
                Messages.GUI_USERS_LIST_COLS_EMAIL_0), "", // no width
                CmsListColumnAlignEnum.ALIGN_LEFT);
            metadata.addColumn(emailCol);

            // add column for last login date
            CmsListColumnDefinition lastLoginCol = new CmsListColumnDefinition(
                LIST_COLUMN_LASTLOGIN,
                Messages.get().container(Messages.GUI_USERS_LIST_COLS_LASTLOGIN_0),
                "", // no width
                CmsListColumnAlignEnum.ALIGN_LEFT);
            lastLoginCol.setFormatter(new CmsListDateMacroFormatter(Messages.get().container(
                Messages.GUI_USERS_LIST_COLS_LASTLOGIN_FORMAT_0), Messages.get().container(
                Messages.GUI_USERS_LIST_COLS_LASTLOGIN_NEVER_0)));
            metadata.addColumn(lastLoginCol);

            // add multi actions
            metadata.addMultiAction(new CmsListMultiAction(LIST_ID, LIST_ACTION_DELETE, Messages.get().container(
                Messages.GUI_USERS_LIST_ACTION_DELETE_NAME_0), "list/delete.gif", Messages.get().container(
                Messages.GUI_USERS_LIST_ACTION_DELETE_HELP_0), true, Messages.get().container(
                Messages.GUI_USERS_LIST_ACTION_DELETE_CONF_0)));
            // reuse the activate user action as a multi action
            metadata.addDirectMultiAction(activateUser);

            // makes the list searchable by login
            CmsSearchAction searchAction = new CmsSearchAction(LIST_ID, loginCol);
            searchAction.useDefaultShowAllAction();
            metadata.setSearchAction(searchAction);
        }
        return new CmsHtmlList(LIST_ID, Messages.get().container(Messages.GUI_USERS_LIST_NAME_0), metadata);
    }

    /**
     * @see org.opencms.workplace.list.CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List ret = new ArrayList();
        // get content
        try {
            List users = getCms().getUsers();
            Iterator itUsers = users.iterator();
            while (itUsers.hasNext()) {
                CmsUser user = (CmsUser)itUsers.next();
                CmsListItem item = getList().newItem(user.getId().toString());
                item.set(LIST_COLUMN_LOGIN, user.getName());
                item.set(LIST_COLUMN_NAME, user.getFullName());
                item.set(LIST_COLUMN_EMAIL, user.getEmail());
                item.set(LIST_COLUMN_LASTLOGIN, new Date(user.getLastlogin()));
                ret.add(item);
            }
        } catch (CmsException e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

}