/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/A_CmsUserGroupsList.java,v $
 * Date   : $Date: 2005/06/22 10:38:20 $
 * Version: $Revision: 1.9 $
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

import org.opencms.file.CmsGroup;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListDirectAction;

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
 * @author Michael Moossen  
 * @version $Revision: 1.9 $
 * @since 5.7.3
 */
public abstract class A_CmsUserGroupsList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list action id constant. */
    public static final String LIST_ACTION_ICON_DIRECT = "aid";

    /** list action id constant. */
    public static final String LIST_ACTION_ICON_INDIRECT = "aii";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE_DIRECT = "asd";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE_INDIRECT = "asi";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_STATE = "cs";

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /** Stores the value of the request parameter for the user name. */
    private String m_paramUsername;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     * @param searchable searchable flag
     */
    protected A_CmsUserGroupsList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        boolean searchable) {

        super(jsp, listId, listName, LIST_COLUMN_NAME, searchable ? LIST_COLUMN_NAME : null);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    public void actionDialog() throws JspException, ServletException, IOException {

        updateUserList();
        super.actionDialog();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getList()
     */
    public CmsHtmlList getList() {

        // assure we have the right username
        CmsHtmlList list = super.getList();
        if (list != null) {
            CmsListColumnDefinition col = list.getMetadata().getColumnDefinition(LIST_COLUMN_ICON);
            if (col != null) {
                Iterator it = col.getDirectActions().iterator();
                while (it.hasNext()) {
                    I_CmsListDirectAction action = (I_CmsListDirectAction)it.next();
                    if (action instanceof CmsGroupDisabledStateAction) {
                        ((CmsGroupDisabledStateAction)action).setUserName(getParamUsername());
                    }
                }
            }
        }
        return list;
    }

    /**
     * Returns the user id parameter value.<p>
     * 
     * @return the user id parameter value
     */
    public String getParamUserid() {

        return m_paramUserid;
    }

    /**
     * Returns the user name parameter value.<p>
     * 
     * @return the user name parameter value
     */
    public String getParamUsername() {

        return m_paramUsername;
    }

    /**
     * Sets the user id parameter value.<p>
     * 
     * @param userId the user id parameter value
     */
    public void setParamUserid(String userId) {

        m_paramUserid = userId;
    }

    /**
     * Sets the user name parameter value.<p>
     * 
     * @param userName the user name parameter value
     */
    public void setParamUsername(String userName) {

        m_paramUsername = userName;
    }

    /**
     * Updates the main user list.<p>
     */
    public void updateUserList() {

        Map objects = (Map)getSettings().getListObject();
        if (objects != null) {
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
     * Returns a list of groups to display.<p>
     * 
     * @return a list of <code><{@link CmsGroup}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    protected abstract List getGroups() throws CmsException;

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();

        // get content        
        List groups = getGroups();
        Iterator itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set(LIST_COLUMN_NAME, group.getName());
            item.set(LIST_COLUMN_DESCRIPTION, group.getDescription());
            ret.add(item);
        }

        return ret;
    }

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
        iconCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        if (!getListId().equals(CmsNotUserGroupsList.LIST_ID)) {
            // state action
            CmsGroupDisabledStateAction iconAction = new CmsGroupDisabledStateAction(
                LIST_ACTION_ICON,
                getCms(),
                getParamUsername());
            // adds a direct group icon
            CmsListDirectAction dirAction = new CmsListDirectAction(LIST_ACTION_ICON_DIRECT);
            dirAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DIRECT_NAME_0));
            dirAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DIRECT_HELP_0));
            dirAction.setIconPath(CmsUsersList.PATH_BUTTONS + "group.png");
            iconAction.setFirstAction(dirAction);
            // adds an indirect group icon
            CmsListDirectAction indirAction = new CmsListDirectAction(LIST_ACTION_ICON_INDIRECT);
            indirAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_INDIRECT_NAME_0));
            indirAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_INDIRECT_HELP_0));
            indirAction.setIconPath(CmsUsersList.PATH_BUTTONS + "group_indirect.png");
            iconAction.setSecondAction(indirAction);
            iconCol.addDirectAction(iconAction);
        } else {
            // state action
            CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
            iconAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_AVAILABLE_NAME_0));
            iconAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_AVAILABLE_HELP_0));
            iconAction.setIconPath(CmsUsersList.PATH_BUTTONS + "group.png");
            iconAction.setEnabled(false);
            iconCol.addDirectAction(iconAction);
        }
        // add it to the list definition
        metadata.addColumn(iconCol);

        if (!getListId().equals(CmsShowUserGroupsList.LIST_ID)) {
            // create column for state change
            CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
            stateCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_STATE_0));
            stateCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_STATE_HELP_0));
            stateCol.setWidth("20");
            stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
            stateCol.setSorteable(false);
            // add it to the list definition
            metadata.addColumn(stateCol);
        }

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_NAME_0));
        nameCol.setWidth("35%");
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for description
        CmsListColumnDefinition descCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_DESCRIPTION_0));
        descCol.setWidth("65%");
        descCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(descCol);
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
        getCms().readUser(getParamUsername());
        getCms().readUser(new CmsUUID(getParamUserid()));
    }
}
