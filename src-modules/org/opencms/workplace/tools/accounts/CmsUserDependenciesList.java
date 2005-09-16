/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsUserDependenciesList.java,v $
 * Date   : $Date: 2005/09/16 13:11:12 $
 * Version: $Revision: 1.1.2.1 $
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

import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsIdentifiableObjectContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * User dependencies list view. <p>
 * 
 * displays the dependencies of a user or a list of user.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsUserDependenciesList extends A_CmsListDialog {

    /** Value for the delete action. */
    public static final int ACTION_DELETE = 121;

    /** Value for the transfer action. */
    public static final int ACTION_TRANSFER = 122;

    /** Request parameter value for the delete action. */
    public static final String DELETE_ACTION = "delete";

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_CREATED = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTMODIFIED = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_PERMISSIONS = "cp";

    /** List id constant. */
    public static final String LIST_ID = "lud";

    /** Request parameter name for the user id, could be a list of ids. */
    public static final String PARAM_USERID = "userid";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Request parameter value for the transfer action. */
    public static final String TRANSFER_ACTION = "transfer";

    /** Stores the value of the request parameter for the user id, could be a list of ids. */
    private String m_paramUserid;

    /** Stores the value of the user name, could be a list of names. */
    private String m_userName;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUserDependenciesList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserDependenciesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * 
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsUserDependenciesList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_NAME_0),
            LIST_COLUMN_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            LIST_COLUMN_NAME);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    public void actionDialog() throws JspException, ServletException, IOException {

        switch (getAction()) {
            case ACTION_DELETE:
                Iterator it = CmsStringUtil.splitAsList(getUserName(), CmsHtmlList.ITEM_SEPARATOR, true).iterator();
                while (it.hasNext()) {
                    String name = (String)it.next();
                    try {
                        getCms().deleteUser(name);
                    } catch (CmsException e) {
                        throw new CmsRuntimeException(e.getMessageContainer(), e);
                    }
                }
                setAction(ACTION_CANCEL);
                actionCloseDialog();
                break;
            case ACTION_TRANSFER:
                Map params = new HashMap();
                // set action parameter to initial dialog call
                params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
                // forward to the select replacement screen
                params.put(PARAM_USERID, getParamUserid());
                getToolManager().jspForwardPage(
                    this,
                    getJsp().getRequestContext().getFolderUri() + "user_transfer.html",
                    params);
                break;

            default:
                super.actionDialog();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlContent()
     */
    public String defaultActionHtmlContent() {

        if (getList().getTotalSize() > 0) {
            return super.defaultActionHtmlContent();
        }
        return "";
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
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
     * Returns the user Name.<p>
     *
     * @return the user Name
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
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlEnd()
     */
    protected String customHtmlEnd() {

        StringBuffer result = new StringBuffer(512);
        result.append(super.customHtmlEnd());
        result.append("<form name='actions' method='post' action='");
        result.append(getDialogRealUri());
        result.append("' class='nomargin' onsubmit=\"return submitAction('ok', null, 'actions');\">\n");
        result.append(allParamsAsHidden());
        result.append(dialogButtonRow(HTML_START));
        result.append("<input name='");
        result.append(DELETE_ACTION);
        result.append("' type='button' value='");
        result.append(Messages.get().container(Messages.GUI_DEPENDENCIES_BUTTON_DELETE_0).key(getLocale()));
        result.append("' onclick=\"submitAction('");
        result.append(DELETE_ACTION);
        result.append("', form);\" class='dialogbutton'>\n");
        if (getList().getTotalSize() > 0) {
            result.append("<input name='");
            result.append(TRANSFER_ACTION);
            result.append("' type='button' value='");
            result.append(Messages.get().container(Messages.GUI_DEPENDENCIES_BUTTON_TRANSFER_0).key(getLocale()));
            result.append("' onclick=\"submitAction('");
            result.append(TRANSFER_ACTION);
            result.append("', form);\" class='dialogbutton'>\n");
        }
        dialogButtonsHtml(result, BUTTON_CANCEL, "");
        result.append(dialogButtonRow(HTML_END));
        result.append("</form>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(512);
        result.append(dialogBlockStart(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_NOTICE_0).key(
            getLocale())));
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
        if (getList().getTotalSize() > 0) {
            result.append(key(Messages.GUI_USER_DEPENDENCIES_NOTICE_TEXT_0));
        } else {
            result.append(key(Messages.GUI_USER_DEPENDENCIES_DELETE_0));
        }
        result.append(dialogBlockEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        CmsIdentifiableObjectContainer ret = new CmsIdentifiableObjectContainer(true, false);
        Iterator itUsers = CmsStringUtil.splitAsList(getParamUserid(), CmsHtmlList.ITEM_SEPARATOR, true).iterator();
        getCms().getRequestContext().saveSiteRoot();
        getCms().getRequestContext().setSiteRoot("/");
        while (itUsers.hasNext()) {
            CmsUser user = getCms().readUser(new CmsUUID(itUsers.next().toString()));
            // get content
            List resources = getCms().getResourcesForPrincipal(user.getId(), null, true);
            Iterator itRes = resources.iterator();
            while (itRes.hasNext()) {
                CmsResource resource = (CmsResource)itRes.next();
                CmsListItem item = (CmsListItem)ret.getObject(resource.getResourceId().toString());
                if (item == null) {
                    item = getList().newItem(resource.getResourceId().toString());
                    item.set(LIST_COLUMN_NAME, resource.getRootPath());
                    item.set(LIST_COLUMN_CREATED, getCms().readUser(resource.getUserCreated()).getFullName());
                    item.set(LIST_COLUMN_LASTMODIFIED, getCms().readUser(resource.getUserLastModified()).getFullName());
                    Iterator itAces = getCms().getAccessControlEntries(resource.getRootPath(), false).iterator();
                    while (itAces.hasNext()) {
                        CmsAccessControlEntry ace = (CmsAccessControlEntry)itAces.next();
                        if (ace.getPrincipal().equals(user.getId())) {
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ace.getPermissions().getPermissionString())) {
                                item.set(LIST_COLUMN_PERMISSIONS, user.getName()
                                    + ": "
                                    + ace.getPermissions().getPermissionString());
                            }
                            break;
                        }
                    }
                    ret.addIdentifiableObject(item.getId(), item);
                } else {
                    String oldData = (String)item.get(LIST_COLUMN_PERMISSIONS);
                    Iterator itAces = getCms().getAccessControlEntries(resource.getRootPath(), false).iterator();
                    while (itAces.hasNext()) {
                        CmsAccessControlEntry ace = (CmsAccessControlEntry)itAces.next();
                        if (ace.getPrincipal().equals(user.getId())) {
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ace.getPermissions().getPermissionString())) {
                                String data = user.getName() + ": " + ace.getPermissions().getPermissionString();
                                if (oldData != null) {
                                    data = oldData + ", " + data;
                                }
                                item.set(LIST_COLUMN_PERMISSIONS, data);
                            }
                            break;
                        }
                    }
                }
            }
        }
        getCms().getRequestContext().restoreSiteRoot();
        return ret.elementList();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add cms dialog resource bundle
        addMessages(org.opencms.workplace.Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (DELETE_ACTION.equals(getParamAction())) {
            setAction(ACTION_DELETE);
        } else if (TRANSFER_ACTION.equals(getParamAction())) {
            setAction(ACTION_TRANSFER);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_ICON_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add icon action
        CmsListDirectAction iconAction = new CmsDependencyIconAction(
            LIST_ACTION_ICON,
            CmsDependencyIconActionType.RESOURCE,
            getCms());
        iconAction.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_ACTION_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_ACTION_ICON_HELP_0));
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);

        // add it to the list definition
        metadata.addColumn(iconCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_NAME_0));
        nameCol.setWidth("50%");
        metadata.addColumn(nameCol);

        // add column for created by
        CmsListColumnDefinition createdCol = new CmsListColumnDefinition(LIST_COLUMN_CREATED);
        createdCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_CREATED_0));
        createdCol.setWidth("20%");
        metadata.addColumn(createdCol);

        // add column for last modified by
        CmsListColumnDefinition lastModifiedCol = new CmsListColumnDefinition(LIST_COLUMN_LASTMODIFIED);
        lastModifiedCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_LASTMODIFIED_0));
        lastModifiedCol.setWidth("20%");
        metadata.addColumn(lastModifiedCol);

        // add column for permissions
        CmsListColumnDefinition permissionsCol = new CmsListColumnDefinition(LIST_COLUMN_PERMISSIONS);
        permissionsCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_PERMISSIONS_0));
        permissionsCol.setWidth("20%");
        metadata.addColumn(permissionsCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // no-op       
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

        // test the needed parameters
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