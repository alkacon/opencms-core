/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/A_CmsGroupUsersList.java,v $
 * Date   : $Date: 2005/06/19 10:57:06 $
 * Version: $Revision: 1.5 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;

/**
 * Generalized user groups view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.5 $
 * @since 5.7.3
 */
public abstract class A_CmsGroupUsersList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "action_icon";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE = "action_state";

    /** list column id constant. */
    public static final String LIST_COLUMN_FULLNAME = "column_fullname";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "column_icon";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOGIN = "column_name";

    /** list column id constant. */
    public static final String LIST_COLUMN_STATE = "column_state";

    /** Stores the value of the request parameter for the user id. */
    private String m_paramGroupid;

    /** Stores the value of the request parameter for the user name. */
    private String m_paramGroupname;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     * @param searchable searchable flag
     */
    protected A_CmsGroupUsersList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        boolean searchable) {

        super(jsp, listId, listName, LIST_COLUMN_LOGIN, searchable ? LIST_COLUMN_LOGIN : null);
    }

    /**
     * Action method for two list dialogs.<p>
     * 
     * @param list1 first list
     * @param list2 second list
     * 
     * @throws JspException if dialog actions fail
     * @throws IOException if writing to the JSP out fails, or in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public static void actionDialog(A_CmsGroupUsersList list1, A_CmsGroupUsersList list2)
    throws JspException, IOException, ServletException {

        A_CmsGroupUsersList wp1 = (list1.isActive() ? (A_CmsGroupUsersList)list1 : (A_CmsGroupUsersList)list2);
        A_CmsGroupUsersList wp2 = (!list1.isActive() ? (A_CmsGroupUsersList)list1 : (A_CmsGroupUsersList)list2);

        // perform the active list actions
        wp1.updateGroupList();
        wp1.actionDialog();
        wp1.refreshList();
        wp2.refreshList();
    }

    /**
     * Returns the user id parameter value.<p>
     * 
     * @return the user id parameter value
     */
    public String getParamGroupid() {

        return m_paramGroupid;
    }

    /**
     * Returns the user name parameter value.<p>
     * 
     * @return the user name parameter value
     */
    public String getParamGroupname() {

        return m_paramGroupname;
    }

    /**
     * Sets the user id parameter value.<p>
     * 
     * @param userId the user id parameter value
     */
    public void setParamGroupid(String userId) {

        m_paramGroupid = userId;
    }

    /**
     * Sets the user name parameter value.<p>
     * 
     * @param userName the user name parameter value
     */
    public void setParamGroupname(String userName) {

        m_paramGroupname = userName;
    }

    /**
     * Updates the main user list.<p>
     */
    public void updateGroupList() {

        Map objects = (Map)getSettings().getListObject();
        if (objects != null) {
            objects.remove(CmsGroupsList.class.getName());
            objects.remove(CmsUsersList.class.getName());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();

        // get content        
        List users = getUsers();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsUser user = (CmsUser)itUsers.next();
            CmsListItem item = getList().newItem(user.getId().toString());
            item.set(LIST_COLUMN_LOGIN, user.getName());
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
    protected abstract List getUsers() throws CmsException;

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        // state action
        CmsListDirectAction iconAction = new CmsListDirectAction(getListId(), LIST_ACTION_ICON);
        if (!getListId().equals(CmsNotUserGroupsList.LIST_ID)) {
            iconAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_INGROUP_NAME_0));
            iconAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_INGROUP_HELP_0));
        } else {
            iconAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_AVAILABLE_NAME_0));
            iconAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_AVAILABLE_HELP_0));
        }
        iconAction.setIconPath(CmsUsersList.PATH_BUTTONS + "user.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        if (!getListId().equals(CmsShowGroupUsersList.LIST_ID)) {
            // create column for state change
            CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
            stateCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_STATE_0));
            stateCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_STATE_HELP_0));
            stateCol.setWidth("20");
            stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
            stateCol.setSorteable(false);
            // add it to the list definition
            metadata.addColumn(stateCol);
        }

        // create column for login
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN);
        loginCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LOGIN_0));
        loginCol.setWidth("35%");
        // add it to the list definition
        metadata.addColumn(loginCol);

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
    protected void setIndependentActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        getCms().readGroup(getParamGroupname());
        getCms().readGroup(new CmsUUID(getParamGroupid()));
    }
}