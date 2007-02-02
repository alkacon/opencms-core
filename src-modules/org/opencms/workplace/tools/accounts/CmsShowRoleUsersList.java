/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsShowRoleUsersList.java,v $
 * Date   : $Date: 2007/02/02 12:04:48 $
 * Version: $Revision: 1.1.2.2 $
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
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Role users list view.<p>
 * 
 * @author Raphael Schnuck
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.5.6
 */
public class CmsShowRoleUsersList extends A_CmsRoleUsersList {

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ORGUNIT = "dou";

    /** list id constant. */
    public static final String LIST_ID = "lsru";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsShowRoleUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsShowRoleUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsShowRoleUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_ROLEUSERS_LIST_NAME_0), true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    public String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
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
            item.set(LIST_COLUMN_NAME, user.getSimpleName());
            item.set(LIST_COLUMN_FULLNAME, user.getFullName());
            ret.add(item);
        }

        return ret;
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
     * @see org.opencms.workplace.tools.accounts.A_CmsRoleUsersList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);

        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
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
            
            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_HIDE;
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
            
            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_SHOW;
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

        // noop
    }
}
