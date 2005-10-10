/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsUserTransferList.java,v $
 * Date   : $Date: 2005/10/10 16:11:03 $
 * Version: $Revision: 1.2 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Allows to select an user to transfer the permissions and attributes from list of previous selected users.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsUserTransferList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_TRANSFER = "at";

    /** list column id constant. */
    public static final String LIST_COLUMN_EMAIL = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTLOGIN = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOGIN = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_TRANSFER = "ct";

    /** list action id constant. */
    public static final String LIST_DEFACTION_TRANSFER = "dt";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ADDRESS = "da";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_GROUPS = "dg";

    /** List id constant. */
    public static final String LIST_ID = "lut";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Stores the value of the request parameter for the user id, could be a list of ids. */
    private String m_paramUserid;

    /** Stores the value of the users name, could be a list of names. */
    private String m_userName;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUserTransferList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
    }

    /**
     * Protected constructor.<p>
     * 
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsUserTransferList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_USERS_TRANSFER_LIST_NAME_0),
            LIST_COLUMN_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserTransferList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(2048);
        result.append(dialogBlockStart(Messages.get().container(Messages.GUI_USERS_TRANSFER_NOTICE_0).key(getLocale())));
        result.append("\n");
        if (getCurrentToolPath().indexOf("/edit/") < 0) {
            result.append(key(Messages.GUI_USER_DEPENDENCIES_SELECTED_USERS_0));
            result.append(":<br>\n");
            List users = CmsStringUtil.splitAsList(getUserName(), CmsHtmlList.ITEM_SEPARATOR, true);
            result.append("<ul>\n");
            Iterator it = users.iterator();
            while (it.hasNext()) {
                String name = (String)it.next();
                result.append("<li>");
                result.append(name);
                result.append("</li>\n");
            }
            result.append("</ul>\n");
        } 
        result.append(key(Messages.GUI_USERS_TRANSFER_NOTICE_TEXT_0));
        result.append(dialogBlockEnd());
        return result.toString();
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
    public void executeListSingleActions() throws IOException, ServletException {

        if (getParamListAction().equals(LIST_ACTION_TRANSFER) || getParamListAction().equals(LIST_DEFACTION_TRANSFER)) {
            // execute the delete action
            try {
                Iterator it = CmsStringUtil.splitAsList(getParamUserid(), CmsHtmlList.ITEM_SEPARATOR, true).iterator();
                while (it.hasNext()) {
                    CmsUUID id = new CmsUUID((String)it.next());
                    getCms().deleteUser(id, new CmsUUID(getSelectedItem().getId()));
                }
                CmsRequestUtil.forwardRequest(getParamCloseLink(), getJsp().getRequest(), getJsp().getResponse());
                setForwarded(true);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_TRANSFER_USER_1,
                    getSelectedItem().get(LIST_COLUMN_NAME)), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
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
     * Returns the users Name.<p>
     *
     * @return the users Name
     */
    public String getUserName() {

        return m_userName;
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
                if (detailId.equals(LIST_DETAIL_ADDRESS)) {
                    CmsUser user = getCms().readUser(userName);
                    // address
                    html.append(user.getAddress());
                    if (user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_TOWN) != null) {
                        html.append("<br>");
                        if (user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ZIPCODE) != null) {
                            html.append(user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_ZIPCODE));
                            html.append(" ");
                        }
                        html.append(user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_TOWN));
                    }
                    if (user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_COUNTRY) != null) {
                        html.append("<br>");
                        html.append(user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_COUNTRY));
                    }
                } else if (detailId.equals(LIST_DETAIL_GROUPS)) {
                    // groups
                    Iterator itGroups = getCms().getGroupsOfUser(userName).iterator();
                    while (itGroups.hasNext()) {
                        html.append(((CmsGroup)itGroups.next()).getName());
                        if (itGroups.hasNext()) {
                            html.append("<br>");
                        }
                        html.append("\n");
                    }
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
        Set selUsers = new HashSet(CmsStringUtil.splitAsList(getParamUserid(), CmsHtmlList.ITEM_SEPARATOR, true));
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsUser user = (CmsUser)itUsers.next();
            if (selUsers.contains(user.getId().toString())) {
                continue;
            }
            CmsListItem item = getList().newItem(user.getId().toString());
            item.set(LIST_COLUMN_LOGIN, user.getName());
            item.set(LIST_COLUMN_NAME, user.getFullName());
            item.set(LIST_COLUMN_EMAIL, user.getEmail());
            item.set(LIST_COLUMN_LASTLOGIN, new Date(user.getLastlogin()));
            ret.add(item);
        }
        return ret;
    }

    /**
     * Returns the list of users to display.<p>
     * 
     * @return the list of users to display
     * 
     * @throws CmsException if something goes wrong
     */
    protected List getUsers() throws CmsException {

        return CmsUser.filterCore(getCms().getUsers());
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

        // create column for transfer
        CmsListColumnDefinition transferCol = new CmsListColumnDefinition(LIST_COLUMN_TRANSFER);
        transferCol.setName(Messages.get().container(Messages.GUI_USERS_TRANSFER_LIST_COLS_TRANSFER_0));
        transferCol.setHelpText(Messages.get().container(Messages.GUI_USERS_TRANSFER_LIST_COLS_TRANSFER_HELP_0));
        transferCol.setWidth("20");
        transferCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        transferCol.setSorteable(false);

        // add transfer action
        setTransferAction(transferCol);

        // add it to the list definition
        metadata.addColumn(transferCol);

        // create column for login
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN);
        loginCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LOGIN_0));
        loginCol.setWidth("20%");

        // create default transfer action
        CmsListDefaultAction defTransferAction = new CmsListDefaultAction(LIST_DEFACTION_TRANSFER);
        defTransferAction.setName(Messages.get().container(Messages.GUI_USERS_TRANSFER_LIST_DEFACTION_TRANSFER_NAME_0));
        defTransferAction.setHelpText(Messages.get().container(
            Messages.GUI_USERS_TRANSFER_LIST_DEFACTION_TRANSFER_HELP_0));
        loginCol.addDefaultAction(defTransferAction);

        // add it to the list definition
        metadata.addColumn(loginCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_USERNAME_0));
        nameCol.setWidth("30%");
        metadata.addColumn(nameCol);

        // add column for email
        CmsListColumnDefinition emailCol = new CmsListColumnDefinition(LIST_COLUMN_EMAIL);
        emailCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_EMAIL_0));
        emailCol.setWidth("30%");
        metadata.addColumn(emailCol);

        // add column for last login date
        CmsListColumnDefinition lastLoginCol = new CmsListColumnDefinition(LIST_COLUMN_LASTLOGIN);
        lastLoginCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LASTLOGIN_0));
        lastLoginCol.setWidth("20%");
        CmsListDateMacroFormatter lastLoginFormatter = new CmsListDateMacroFormatter(Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_LASTLOGIN_FORMAT_1), Messages.get().container(
            Messages.GUI_USERS_LIST_COLS_LASTLOGIN_NEVER_0));
        lastLoginCol.setFormatter(lastLoginFormatter);
        metadata.addColumn(lastLoginCol);
    }

    /**
     * Sets the icon actions for the transfer list.<p>
     * 
     * @param transferCol the column to set the action
     */
    protected void setTransferAction(CmsListColumnDefinition transferCol) {

        CmsListDirectAction transferAction = new CmsListDirectAction(LIST_ACTION_TRANSFER);
        transferAction.setName(Messages.get().container(Messages.GUI_USERS_TRANSFER_LIST_ACTION_TRANSFER_NAME_0));
        transferAction.setHelpText(Messages.get().container(Messages.GUI_USERS_TRANSFER_LIST_ACTION_TRANSFER_HELP_0));
        transferAction.setIconPath(PATH_BUTTONS + "user.png");
        transferCol.addDirectAction(transferAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add user address details
        CmsListItemDetails userAddressDetails = new CmsListItemDetails(LIST_DETAIL_ADDRESS);
        userAddressDetails.setAtColumn(LIST_COLUMN_LOGIN);
        userAddressDetails.setVisible(false);
        userAddressDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ADDRESS_NAME_0));
        userAddressDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_ADDRESS_HELP_0));
        userAddressDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ADDRESS_NAME_0));
        userAddressDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_ADDRESS_HELP_0));
        userAddressDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_ADDRESS_NAME_0));
        userAddressDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_ADDRESS_NAME_0)));
        metadata.addItemDetails(userAddressDetails);

        // add user groups details
        CmsListItemDetails userGroupsDetails = new CmsListItemDetails(LIST_DETAIL_GROUPS);
        userGroupsDetails.setAtColumn(LIST_COLUMN_LOGIN);
        userGroupsDetails.setVisible(false);
        userGroupsDetails.setShowActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_NAME_0));
        userGroupsDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_GROUPS_HELP_0));
        userGroupsDetails.setHideActionName(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_NAME_0));
        userGroupsDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_GROUPS_HELP_0));
        userGroupsDetails.setName(Messages.get().container(Messages.GUI_USERS_DETAIL_GROUPS_NAME_0));
        userGroupsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_USERS_DETAIL_GROUPS_NAME_0)));
        metadata.addItemDetails(userGroupsDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_LOGIN));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_NAME));
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        m_userName = "";
        Iterator itUsers = CmsStringUtil.splitAsList(getParamUserid(), CmsHtmlList.ITEM_SEPARATOR, true).iterator();
        while (itUsers.hasNext()) {
            CmsUUID id = new CmsUUID(itUsers.next().toString());
            m_userName += getCms().readUser(id).getName();
            if (itUsers.hasNext()) {
                m_userName += CmsHtmlList.ITEM_SEPARATOR;
            }
        }
    }
}