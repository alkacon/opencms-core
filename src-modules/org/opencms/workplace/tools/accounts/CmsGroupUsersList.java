/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsGroupUsersList.java,v $
 * Date   : $Date: 2005/06/23 10:47:20 $
 * Version: $Revision: 1.5 $
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
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User groups view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsGroupUsersList extends A_CmsGroupUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE = "ar";

    /** list action id constant. */
    public static final String LIST_DEFACTION_REMOVE = "dr";

    /** list id constant. */
    public static final String LIST_ID = "lgu";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE = "mr";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsGroupUsersList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_GROUPUSERS_LIST_NAME_0), true);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
                String userName = (String)listItem.get(LIST_COLUMN_LOGIN);
                try {
                    getCms().removeUserFromGroup(userName, getParamGroupname());
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
    public void executeListSingleActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_DEFACTION_REMOVE) || getParamListAction().equals(LIST_ACTION_REMOVE)) {
            CmsListItem listItem = getSelectedItem();
            try {
                getCms().removeUserFromGroup((String)listItem.get(LIST_COLUMN_LOGIN), getParamGroupname());
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
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#getUsers()
     */
    protected List getUsers() throws CmsException {

        return getCms().getUsersOfGroup(getParamGroupname());
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

        super.setColumns(metadata);
        // get column for state
        CmsListColumnDefinition stateCol = metadata.getColumnDefinition(LIST_COLUMN_STATE);
        // add remove action
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_REMOVE);
        stateAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_HELP_0));
        stateAction.setIconPath(ICON_MINUS);
        stateCol.addDirectAction(stateAction);
        // get column for login
        CmsListColumnDefinition loginCol = metadata.getColumnDefinition(LIST_COLUMN_LOGIN);
        // add default remove action
        CmsListDefaultAction removeAction = new CmsListDefaultAction(LIST_DEFACTION_REMOVE);
        removeAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_HELP_0));
        loginCol.setDefaultAction(removeAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add remove multi action
        CmsListMultiAction removeMultiAction = new CmsListMultiAction(LIST_MACTION_REMOVE);
        removeMultiAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_NAME_0));
        removeMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_HELP_0));
        removeMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_CONF_0));
        removeMultiAction.setIconPath(ICON_MULTI_MINUS);
        metadata.addMultiAction(removeMultiAction);
    }
}
