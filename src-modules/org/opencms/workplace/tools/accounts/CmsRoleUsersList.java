/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsRoleUsersList.java,v $
 * Date   : $Date: 2007/01/31 14:23:18 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Role users view.<p>
 * 
 * @author Raphael Schnuck  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.6
 */
public class CmsRoleUsersList extends A_CmsRoleUsersList {

    /** list action id constant. */
    public static final String LIST_DEFACTION_REMOVE = "dr";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ORGUNIT = "dou";

    /** list id constant. */
    public static final String LIST_ID = "lru";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE = "mr";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsRoleUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRoleUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsRoleUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_ROLEUSERS_LIST_NAME_0), true);
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
                    OpenCms.getRoleManager().removeUserFromRole(
                        getCms(),
                        CmsRole.valueOf(getCms().readGroup(getParamRole())),
                        userName);
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

        if (m_removeActionIds.contains(getParamListAction())) {
            CmsListItem listItem = getSelectedItem();
            try {
                OpenCms.getRoleManager().removeUserFromRole(
                    getCms(),
                    CmsRole.valueOf(getCms().readGroup(getParamRole())),
                    (String)listItem.get(LIST_COLUMN_LOGIN));
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
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // get content
        List users = getList().getAllContent();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsListItem item = (CmsListItem)itUsers.next();
            String userName = item.get(LIST_COLUMN_LOGIN).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_ORGUNIT)) {
                    CmsUser user = getCms().readUser(userName);
                    // address
                    html.append(user.getOuFqn());
                } else {
                    continue;
                }
            } catch (Exception e) {
                // noop
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRoleUsersList#getUsers()
     */
    protected List getUsers() throws CmsException {

        return OpenCms.getRoleManager().getUsersOfRole(
            getCms(),
            CmsRole.valueOf(getCms().readGroup(getParamRole())),
            true,
            false);
    }

    /**
     * Sets the optional login default action.<p>
     * 
     * @param loginCol the user login column
     */
    protected void setDefaultAction(CmsListColumnDefinition loginCol) {

        // add default remove action
        CmsListDefaultAction removeAction = new CmsListDefaultAction(LIST_DEFACTION_REMOVE);
        removeAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_HELP_0));
        loginCol.addDefaultAction(removeAction);
        // keep the id
        m_removeActionIds.add(removeAction.getId());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add user address details
        CmsListItemDetails orgUnitDetails = new CmsListItemDetails(LIST_DETAIL_ORGUNIT);
        orgUnitDetails.setAtColumn(LIST_COLUMN_LOGIN);
        orgUnitDetails.setVisible(false);
        orgUnitDetails.setHideAction(new CmsListIndependentAction(LIST_DETAIL_ORGUNIT) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                List users = getList().getAllContent();
                Iterator itUsers = users.iterator();
                while (itUsers.hasNext()) {
                    CmsListItem item = (CmsListItem)itUsers.next();
                    String userName = item.get(LIST_COLUMN_LOGIN).toString();
                    try {
                        CmsUser user = getCms().readUser(userName);
                        if (!user.getOuFqn().equals(getParamOufqn())) {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }
        });
        orgUnitDetails.setShowAction(new CmsListIndependentAction(LIST_DETAIL_ORGUNIT) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                List users = getList().getAllContent();
                Iterator itUsers = users.iterator();
                while (itUsers.hasNext()) {
                    CmsListItem item = (CmsListItem)itUsers.next();
                    String userName = item.get(LIST_COLUMN_LOGIN).toString();
                    try {
                        CmsUser user = getCms().readUser(userName);
                        if (!user.getOuFqn().equals(getParamOufqn())) {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }
        });
        orgUnitDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ORGUNIT_NAME_0));
        orgUnitDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ORGUNIT_HELP_0));
        orgUnitDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ORGUNIT_NAME_0));
        orgUnitDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ORGUNIT_HELP_0));
        orgUnitDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_ORGUNIT_NAME_0));
        orgUnitDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_ORGUNIT_NAME_0)));
        metadata.addItemDetails(orgUnitDetails);
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
