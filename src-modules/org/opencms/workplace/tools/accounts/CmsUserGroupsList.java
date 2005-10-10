/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsUserGroupsList.java,v $
 * Date   : $Date: 2006/03/27 14:52:49 $
 * Version: $Revision: 1.10 $
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

import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User groups view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsUserGroupsList extends A_CmsUserGroupsList {

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE = "ar";

    /** list action id constant. */
    public static final String LIST_DEFACTION_REMOVE = "dr";

    /** list id constant. */
    public static final String LIST_ID = "lug";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE = "mr";

    /** a set of action id's to use for removing. */
    protected static Set m_removeActionIds = new HashSet();
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUserGroupsList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     */
    protected CmsUserGroupsList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_USERGROUPS_LIST_NAME_0), true);
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
                String groupName = (String)listItem.get(LIST_COLUMN_NAME);
                boolean directGroup = false;
                try {
                    Iterator it = getCms().getDirectGroupsOfUser(getParamUsername()).iterator();
                    while (it.hasNext()) {
                        CmsGroup group = (CmsGroup)it.next();
                        if (group.getName().equals(groupName)) {
                            directGroup = true;
                            break;
                        }
                    }
                    if (directGroup) {
                        getCms().removeUserFromGroup(getParamUsername(), groupName);
                    }
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

        if (m_removeActionIds.contains(getParamListAction())) {
            CmsListItem listItem = getSelectedItem();
            try {
                getCms().removeUserFromGroup(getParamUsername(), (String)listItem.get(LIST_COLUMN_NAME));
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
     * @see org.opencms.workplace.list.A_CmsListDialog#getList()
     */
    public CmsHtmlList getList() {

        // assure we have the right username
        CmsHtmlList list = super.getList();
        if (list != null) {
            CmsListColumnDefinition col = list.getMetadata().getColumnDefinition(LIST_COLUMN_STATE);
            if (col != null) {
                Iterator itDirectActions = col.getDirectActions().iterator();
                while (itDirectActions.hasNext()) {
                    I_CmsListDirectAction action = (I_CmsListDirectAction)itDirectActions.next();
                    if (action != null && action instanceof CmsGroupRemoveAction) {
                        ((CmsGroupRemoveAction)action).setUserName(getParamUsername());
                    }
                }
            }
            CmsListColumnDefinition col2 = list.getMetadata().getColumnDefinition(LIST_COLUMN_NAME);
            if (col2 != null) {
                Iterator itDefaultActions = col2.getDefaultActions().iterator();
                while (itDefaultActions.hasNext()) {
                    I_CmsListDirectAction action = (I_CmsListDirectAction)itDefaultActions.next();
                    if (action != null && action instanceof CmsGroupRemoveAction) {
                        ((CmsGroupRemoveAction)action).setUserName(getParamUsername());
                    }
                }
            }
        }
        return list;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#getGroups()
     */
    protected List getGroups() throws CmsException {

        return getCms().getGroupsOfUser(getParamUsername());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#setDefaultAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setDefaultAction(CmsListColumnDefinition nameCol) {

        // add default remove action for direct groups
        CmsGroupRemoveAction removeAction = new CmsGroupRemoveAction(LIST_DEFACTION_REMOVE, getCms(), true);
        removeAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_REMOVE_HELP_0));
        nameCol.addDefaultAction(removeAction);

        // add default remove action for indirect groups
        CmsGroupRemoveAction indirRemoveAction = new CmsGroupRemoveAction(LIST_DEFACTION_REMOVE + "i", getCms(), false);
        indirRemoveAction.setName(Messages.get().container(Messages.GUI_USERGROUPS_LIST_ACTION_STATE_DISABLED_NAME_0));
        indirRemoveAction.setHelpText(Messages.get().container(
            Messages.GUI_USERGROUPS_LIST_ACTION_STATE_DISABLED_HELP_0));
        indirRemoveAction.setEnabled(false);
        nameCol.addDefaultAction(indirRemoveAction);

        // keep the ids
        m_removeActionIds.add(removeAction.getId());
        m_removeActionIds.add(indirRemoveAction.getId());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#setIconAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setIconAction(CmsListColumnDefinition iconCol) {

        // adds a direct group icon
        CmsListDirectAction dirAction = new CmsGroupStateAction(LIST_ACTION_ICON_DIRECT, getCms(), true);
        dirAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DIRECT_NAME_0));
        dirAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DIRECT_HELP_0));
        dirAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "group.png");
        dirAction.setEnabled(false);
        iconCol.addDirectAction(dirAction);

        // adds an indirect group icon
        CmsListDirectAction indirAction = new CmsGroupStateAction(LIST_ACTION_ICON_INDIRECT, getCms(), false);
        indirAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_INDIRECT_NAME_0));
        indirAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_INDIRECT_HELP_0));
        indirAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "group_indirect.png");
        indirAction.setEnabled(false);
        iconCol.addDirectAction(indirAction);

        iconCol.setListItemComparator(new CmsListItemActionIconComparator());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add remove multi action
        CmsListMultiAction removeMultiAction = new CmsListMultiAction(LIST_MACTION_REMOVE);
        removeMultiAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_REMOVE_NAME_0));
        removeMultiAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_MACTION_REMOVE_HELP_0));
        removeMultiAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_GROUPS_LIST_MACTION_REMOVE_CONF_0));
        removeMultiAction.setIconPath(ICON_MULTI_MINUS);
        metadata.addMultiAction(removeMultiAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setStateActionCol(CmsListMetadata metadata) {

        // create column for state change
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_STATE_0));
        stateCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_STATE_HELP_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        // add it to the list definition
        metadata.addColumn(stateCol);

        // add remove action for direct groups
        CmsGroupRemoveAction dirStateAction = new CmsGroupRemoveAction(LIST_ACTION_REMOVE, getCms(), true);
        dirStateAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_REMOVE_NAME_0));
        dirStateAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DEFACTION_REMOVE_HELP_0));
        dirStateAction.setIconPath(ICON_MINUS);
        stateCol.addDirectAction(dirStateAction);

        // add remove action for indirect groups
        CmsGroupRemoveAction indirStateAction = new CmsGroupRemoveAction(LIST_ACTION_REMOVE + "i", getCms(), false);
        indirStateAction.setName(Messages.get().container(Messages.GUI_USERGROUPS_LIST_ACTION_STATE_DISABLED_NAME_0));
        indirStateAction.setHelpText(Messages.get().container(Messages.GUI_USERGROUPS_LIST_ACTION_STATE_DISABLED_HELP_0));
        indirStateAction.setIconPath(A_CmsListDialog.ICON_DISABLED);
        indirStateAction.setEnabled(false);
        stateCol.addDirectAction(indirStateAction);

        stateCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add it to the list definition
        metadata.addColumn(stateCol);
        // keep the ids
        m_removeActionIds.add(dirStateAction.getId());
        m_removeActionIds.add(indirStateAction.getId());
    }
}
