/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/Attic/A_CmsNotUserGroupsList.java,v $
 * Date   : $Date: 2005/06/29 09:24:47 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Not Usergroups view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.0.0 
 */
public class A_CmsNotUserGroupsList extends A_CmsUserGroupsList {

    /** list action id constant. */
    public static final String LIST_ACTION_ADD = "aa";

    /** list action id constant. */
    public static final String LIST_DEFACTION_ADD = "da";

    /** list action id constant. */
    public static final String LIST_MACTION_ADD = "ma";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     */
    public A_CmsNotUserGroupsList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_NOTUSERGROUPS_LIST_NAME_0), true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_ADD)) {
            // execute the remove multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    getCms().addUserToGroup(getParamUsername(), (String)listItem.get(LIST_COLUMN_NAME));
                }
            } catch (CmsException e) {
                // refresh the list
                Map objects = (Map)getSettings().getListObject();
                if (objects != null) {
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
    public void executeListSingleActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_DEFACTION_ADD) || getParamListAction().equals(LIST_ACTION_ADD)) {
            CmsListItem listItem = getSelectedItem();
            try {
                getCms().addUserToGroup(getParamUsername(), (String)listItem.get(LIST_COLUMN_NAME));
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
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#getGroups()
     */
    protected List getGroups() throws CmsException {

        List usergroups = getCms().getGroupsOfUser(getParamUsername());
        List groups = getCms().getGroups();
        groups.removeAll(usergroups);
        return groups;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        setActive(((getListId() + "-form").equals(request.getParameter(PARAM_FORMNAME))));
        super.initWorkplaceRequestValues(settings, request);
        setParamFormName(getListId() + "-form");
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);
        // get column for state
        CmsListColumnDefinition stateCol = metadata.getColumnDefinition(LIST_COLUMN_STATE);
        // add add action
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_ADD);
        stateAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_ADD_NAME_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_ADD_HELP_0));
        stateAction.setIconPath(ICON_ADD);
        stateCol.addDirectAction(stateAction);
        // get column for name
        CmsListColumnDefinition nameCol = metadata.getColumnDefinition(LIST_COLUMN_NAME);
        // add add action
        CmsListDefaultAction addAction = new CmsListDefaultAction(LIST_DEFACTION_ADD);
        addAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_ADD_NAME_0));
        addAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_ADD_HELP_0));
        nameCol.setDefaultAction(addAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction addMultiAction = new CmsListMultiAction(LIST_MACTION_ADD);
        addMultiAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_ADD_NAME_0));
        addMultiAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_ADD_HELP_0));
        addMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_ADD_CONF_0));
        addMultiAction.setIconPath(ICON_MULTI_ADD);
        metadata.addMultiAction(addMultiAction);
    }
}
