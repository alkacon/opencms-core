/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsUsersList.java,v $
 * Date   : $Date: 2007/01/08 14:02:56 $
 * Version: $Revision: 1.15.4.5 $
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

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsSessionManager;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main system user account management view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.15.4.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsUsersList extends A_CmsUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_SWITCH = "as";

    /** list action id constant. */
    public static final String LIST_COLUMN_SWITCH = "cs";

    /** list id constant. */
    public static final String LIST_ID = "lsu";

    /** a set of action id's to use for deletion. */
    private static Set m_deleteActionIds = new HashSet();

    /** a set of action id's to use for edition. */
    private static Set m_editActionIds = new HashSet();

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUsersList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_USERS_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Calls the switch user method of the SessionManager.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void actionSwitchUser() throws CmsException {

        try {
            CmsSessionManager sessionManager = OpenCms.getSessionManager();
            sessionManager.switchUser(getCms(), getJsp().getRequest(), getCms().readUser(
                new CmsUUID(getJsp().getRequest().getParameter("userid"))));
        } catch (CmsException e) {
            String toolPath = getCurrentToolPath().substring(0, getCurrentToolPath().lastIndexOf("/"));
            getToolManager().setCurrentToolPath(this, toolPath);
            throw e;
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        String userId = getSelectedItem().getId();
        String userName = getSelectedItem().get(LIST_COLUMN_LOGIN).toString();

        Map params = new HashMap();
        params.put(A_CmsEditUserDialog.PARAM_USERID, userId);
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit", params);
        } else if (m_editActionIds.contains(getParamListAction())) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/user", params);
        } else if (getParamListAction().equals(LIST_ACTION_GROUPS)) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/groups", params);
        } else if (getParamListAction().equals(LIST_ACTION_SWITCH)) {
            // forward to switch user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/switch", params);
        } else if (getParamListAction().equals(LIST_ACTION_ROLES)) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/roles", params);
        } else if (m_deleteActionIds.contains(getParamListAction())) {
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit/delete", params);
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            try {
                CmsUser user = readUser(userName);
                user.setEnabled(true);
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_USER_1, userName), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
            // execute the activate action
            try {
                CmsUser user = readUser(userName);
                user.setEnabled(false);
                getCms().writeUser(user);
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DEACTIVATE_USER_1, userName), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getGroupIcon()
     */
    protected String getGroupIcon() {

        return PATH_BUTTONS + "group.png";
    }

    /**
     * Returns the path the switch user icon.<p>
     * 
     * @return the path to the switch user icon
     */
    protected String getSwitchIcon() {

        return PATH_BUTTONS + "user_switch.png";
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getUsers()
     */
    protected List getUsers() throws CmsException {

        return CmsPrincipal.filterCore(getCms().getUsers());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#readUser(java.lang.String)
     */
    protected CmsUser readUser(String name) throws CmsException {

        return getCms().readUser(name);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);

        // add edit action
        setEditAction(editCol);
        m_editActionIds.addAll(editCol.getDirectActionIds());
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for group edition
        CmsListColumnDefinition groupCol = new CmsListColumnDefinition(LIST_COLUMN_GROUPS);
        groupCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_GROUPS_0));
        groupCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_GROUPS_HELP_0));
        groupCol.setWidth("20");
        groupCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        groupCol.setSorteable(false);
        // add groups action
        CmsListDirectAction groupAction = new CmsListDirectAction(LIST_ACTION_GROUPS);
        groupAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_GROUPS_NAME_0));
        groupAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_GROUPS_HELP_0));
        groupAction.setIconPath(getGroupIcon());
        groupCol.addDirectAction(groupAction);
        // add it to the list definition
        metadata.addColumn(groupCol);

        // create column for switch user
        CmsListColumnDefinition switchCol = new CmsListColumnDefinition(LIST_COLUMN_SWITCH);
        switchCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_SWITCH_0));
        switchCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_SWITCH_HELP_0));
        switchCol.setWidth("20");
        switchCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        switchCol.setSorteable(false);
        // add switch action
        CmsListDirectAction switchAction = new CmsListDirectAction(LIST_ACTION_SWITCH);
        switchAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_SWITCH_NAME_0));
        switchAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_SWITCH_HELP_0));
        switchAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_SWITCH_CONF_0));
        switchAction.setIconPath(getSwitchIcon());
        switchCol.addDirectAction(switchAction);
        // add it to the list definition
        metadata.addColumn(switchCol);

        // create column for activation/deactivation
        CmsListColumnDefinition actCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        actCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_0));
        actCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ACTIVATE_HELP_0));
        actCol.setWidth("20");
        actCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        actCol.setListItemComparator(new CmsListItemActionIconComparator());

        // activate action
        CmsListDirectAction actAction = new CmsListDirectAction(LIST_ACTION_ACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                if (getItem() != null) {
                    String usrId = getItem().getId();
                    try {
                        return !getCms().readUser(new CmsUUID(usrId)).isEnabled();
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        actAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_NAME_0));
        actAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_HELP_0));
        actAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_CONF_0));
        actAction.setIconPath(ICON_INACTIVE);
        actCol.addDirectAction(actAction);

        // deactivate action
        CmsListDirectAction deactAction = new CmsListDirectAction(LIST_ACTION_DEACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                if (getItem() != null) {
                    String usrId = getItem().getId();
                    try {
                        return getCms().readUser(new CmsUUID(usrId)).isEnabled();
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        deactAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_NAME_0));
        deactAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_HELP_0));
        deactAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DEACTIVATE_CONF_0));
        deactAction.setIconPath(ICON_ACTIVE);
        actCol.addDirectAction(deactAction);

        // add it to the list definition
        metadata.addColumn(actCol);

        // create column for deletion
        CmsListColumnDefinition deleteCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        deleteCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_DELETE_0));
        deleteCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_DELETE_HELP_0));
        deleteCol.setWidth("20");
        deleteCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        deleteCol.setSorteable(false);
        // add delete action
        setDeleteAction(deleteCol);
        m_deleteActionIds.addAll(deleteCol.getDirectActionIds());

        // add it to the list definition
        metadata.addColumn(deleteCol);

        // create column for login
        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_LOGIN);
        loginCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_LOGIN_0));
        loginCol.setWidth("20%");

        // create default edit action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_DEFACTION_EDIT);
        defEditAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_EDIT_HELP_0));
        loginCol.addDefaultAction(defEditAction);

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
        lastLoginCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(lastLoginCol);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setDeleteAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {

        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_DELETE_HELP_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setEditAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setEditAction(CmsListColumnDefinition editCol) {

        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_HELP_0));
        editAction.setIconPath(PATH_BUTTONS + "user.png");
        editCol.addDirectAction(editAction);
    }
}