/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/Attic/CmsUserRolesList.java,v $
 * Date   : $Date: 2005/06/08 16:44:19 $
 * Version: $Revision: 1.2 $
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User roles view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public class CmsUserRolesList extends A_CmsUserGroupsList {

    /** list action id constant. */
    public static final String LIST_ACTION_ADD = "action_add";

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE = "action_remove";

    /** list id constant. */
    public static final String LIST_ID = "userroles";

    /** list action id constant. */
    public static final String LIST_MACTION_ADD = "maction_add";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE = "maction_remove";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUserRolesList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_USERROLES_LIST_NAME_0), true);
        setCacheList(false);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserRolesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));        
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_REMOVE)) {
            // execute the remove multiaction
            Iterator itItems = getSelectedItems().iterator();
            while (itItems.hasNext()) {
                CmsListItem listItem = (CmsListItem)itItems.next();
                try {
                    getCms().removeUserFromGroup(getParamUsername(), (String)listItem.get(LIST_COLUMN_NAME));
                } catch (CmsException e) {
                    // could be an indirectly assigned group
                }
            }
        } else if (getParamListAction().equals(LIST_MACTION_ADD)) {
            // execute the remove multiaction
            Iterator itItems = getSelectedItems().iterator();
            while (itItems.hasNext()) {
                CmsListItem listItem = (CmsListItem)itItems.next();
                try {
                    getCms().addUserToGroup(getParamUsername(), (String)listItem.get(LIST_COLUMN_NAME));
                } catch (CmsException e) {
                    // could be an indirectly assigned group
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
    public void executeListSingleActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_ACTION_REMOVE)) {
            CmsListItem listItem = getSelectedItem();
            try {
                getCms().removeUserFromGroup(getParamUsername(), (String)listItem.get(LIST_COLUMN_NAME));
            } catch (CmsException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_REMOVE_SELECTED_GROUP_0), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_ADD)) {
            CmsListItem listItem = getSelectedItem();
            try {
                getCms().addUserToGroup(getParamUsername(), (String)listItem.get(LIST_COLUMN_NAME));
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
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#getGroups()
     */
    protected List getGroups() throws CmsException {

        return getCms().getGroups();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        setActive(((LIST_ID + "-form").equals(request.getParameter(PARAM_FORMNAME))));
        super.initWorkplaceRequestValues(settings, request);
        setParamFormName(LIST_ID + "-form");
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for activation/deactivation
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_USERROLES_LIST_COLS_STATE_0));
        stateCol.setHelpText(Messages.get().container(Messages.GUI_USERROLES_LIST_COLS_STATE_HELP_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setListItemComparator(new CmsListItemActionIconComparator());
        // activate action
        CmsListDirectAction addAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_ADD);
        addAction.setName(Messages.get().container(Messages.GUI_USERROLES_LIST_ACTION_ADD_NAME_0));
        addAction.setHelpText(Messages.get().container(Messages.GUI_USERROLES_LIST_ACTION_ADD_HELP_0));
        addAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERROLES_LIST_ACTION_ADD_CONF_0));
        addAction.setIconPath(ICON_INACTIVE);
        // deactivate action
        CmsListDirectAction removeAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_REMOVE);
        removeAction.setName(Messages.get().container(Messages.GUI_USERROLES_LIST_ACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_USERROLES_LIST_ACTION_REMOVE_HELP_0));
        removeAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERROLES_LIST_ACTION_REMOVE_CONF_0));
        removeAction.setIconPath(ICON_ACTIVE);
        // adds an activate/deactivate direct action
        CmsRoleActivateAction roleAction = new CmsRoleActivateAction(
            LIST_ID,
            LIST_ACTION_STATE,
            getCms(),
            getParamUsername());
        roleAction.setFirstAction(addAction);
        roleAction.setSecondAction(removeAction);
        stateCol.addDirectAction(roleAction);
        // add it to the list definition
        metadata.addColumn(stateCol);

        super.setColumns(metadata);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction addMultiAction = new CmsListMultiAction(LIST_ID, LIST_MACTION_ADD);
        addMultiAction.setName(Messages.get().container(Messages.GUI_USERROLES_LIST_MACTION_ADD_NAME_0));
        addMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERROLES_LIST_MACTION_ADD_HELP_0));
        addMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERROLES_LIST_MACTION_ADD_CONF_0));
        addMultiAction.setIconPath(ICON_MULTI_ACTIVATE);
        metadata.addMultiAction(addMultiAction);

        // add remove multi action
        CmsListMultiAction removeMultiAction = new CmsListMultiAction(LIST_ID, LIST_MACTION_REMOVE);
        removeMultiAction.setName(Messages.get().container(Messages.GUI_USERROLES_LIST_MACTION_REMOVE_NAME_0));
        removeMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERROLES_LIST_MACTION_REMOVE_HELP_0));
        removeMultiAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_USERROLES_LIST_MACTION_REMOVE_CONF_0));
        removeMultiAction.setIconPath(ICON_MULTI_DEACTIVATE);
        metadata.addMultiAction(removeMultiAction);
    }
}
