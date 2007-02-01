/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsRolesList.java,v $
 * Date   : $Date: 2007/02/01 15:00:52 $
 * Version: $Revision: 1.1.2.3 $
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
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Roles overview view.<p>
 * 
 * @author Raphael Schnuck  
 * 
 * @version $Revision: 1.1.2.3 $ 
 * 
 * @since 6.5.6 
 */
public class CmsRolesList extends A_CmsListDialog {

    /** list column id constant. */
    public static final String LIST_COLUMN_DEPENDENCY = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_HIDE_NAME = "chn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_DESCRIPTION = "dd";

    /** list action id constant. */
    public static final String LIST_DEFACTION_OVERVIEW = "do";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_USERS = "du";

    /** list id constant. */
    public static final String LIST_ID = "lsrs";

    /** Request parameter name for the role name. */
    public static final String PARAM_ROLE = "role";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Stores the value of the request parameter for the organizational unit. */
    private String m_paramOufqn;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsRolesList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     */
    public CmsRolesList(CmsJspActionElement jsp, String listId) {

        this(jsp, listId, Messages.get().container(Messages.GUI_ROLEEDIT_LIST_NAME_0), false);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRolesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     * @param searchable searchable flag
     */
    protected CmsRolesList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName, boolean searchable) {

        super(jsp, listId, listName, LIST_COLUMN_NAME, CmsListOrderEnum.ORDER_ASCENDING, searchable ? LIST_COLUMN_NAME
        : null);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        String roleName = getSelectedItem().get(LIST_COLUMN_HIDE_NAME).toString();
        Map params = new HashMap();
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, getParamOufqn());
        params.put(PARAM_ROLE, roleName);
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            try {
                if (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.valueOf(getCms().readGroup(roleName)))) {
                    // forward to the edit user screen
                    getToolManager().jspForwardTool(this, getCurrentToolPath() + "/edit", params);
                } else {
                    getToolManager().jspForwardTool(this, getCurrentToolPath() + "/overview", params);
                }
            } catch (CmsException e) {
                // noop
            }
        } else if (getParamListAction().equals(LIST_DEFACTION_OVERVIEW)) {
            // forward to the overview screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/overview", params);
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * Returns the path of the edit icon.<p>
     * 
     * @return the path of the edit icon
     */
    public String getEditIcon() {

        return PATH_BUTTONS + "user.png";
    }

    /**
     * Returns the organizational unit parameter value.<p>
     * 
     * @return the organizational unit parameter value
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Sets the user organizational unit value.<p>
     * 
     * @param ouFqn the organizational unit parameter value
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // get content
        List roles = getList().getAllContent();
        Iterator itRoles = roles.iterator();
        while (itRoles.hasNext()) {
            CmsListItem item = (CmsListItem)itRoles.next();
            String roleName = item.get(LIST_COLUMN_HIDE_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_DESCRIPTION)) {
                    CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
                    html.append(role.getDescription(getCms().getRequestContext().getLocale()));
                } else if (detailId.equals(LIST_DETAIL_USERS)) {
                    CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
                    List users = OpenCms.getRoleManager().getUsersOfRole(getCms(), role, false, false);
                    Iterator itUsers = users.iterator();
                    while (itUsers.hasNext()) {
                        CmsUser user = (CmsUser)itUsers.next();
                        html.append(user.getName());
                        if (itUsers.hasNext()) {
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
        List roles = OpenCms.getRoleManager().getRoles(getCms(), getParamOufqn(), false);

        Iterator itRoles = roles.iterator();
        while (itRoles.hasNext()) {
            CmsRole role = (CmsRole)itRoles.next();
            CmsListItem item = getList().newItem(role.getGroupName());
            Locale locale = getCms().getRequestContext().getLocale();
            item.set(LIST_COLUMN_NAME, role.getName(locale));
            item.set(LIST_COLUMN_HIDE_NAME, role.getGroupName());
            String dependency = "";
            while (role.getParentRole() != null) {
                dependency = dependency + role.getParentRole().getName(locale);
                role = role.getParentRole();
                if (role.getParentRole() != null) {
                    dependency = dependency + ", ";
                }
            }
            item.set(LIST_COLUMN_DEPENDENCY, dependency);
            ret.add(item);
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);

        // adds a role icon
        CmsListDirectAction dirAction = new CmsListDefaultAction(LIST_ACTION_ICON);
        dirAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ICON_NAME_0));
        dirAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ICON_HELP_0));
        dirAction.setIconPath(PATH_BUTTONS + "role.png");
        dirAction.setEnabled(false);
        iconCol.addDirectAction(dirAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // add edit action
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_EDIT_HELP_0));
        editAction.setIconPath(getEditIcon());
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_NAME_0));
        nameCol.setWidth("40%");
        // create default overview action
        CmsListDefaultAction defOverviewAction = new CmsListDefaultAction(LIST_DEFACTION_OVERVIEW);
        defOverviewAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_DEFACTION_OVERVIEW_NAME_0));
        defOverviewAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_DEFACTION_OVERVIEW_HELP_0));
        nameCol.addDefaultAction(defOverviewAction);
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for hidden name
        CmsListColumnDefinition hideNameCol = new CmsListColumnDefinition(LIST_COLUMN_HIDE_NAME);
        hideNameCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(hideNameCol);

        // create column for path
        CmsListColumnDefinition depCol = new CmsListColumnDefinition(LIST_COLUMN_DEPENDENCY);
        depCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_DEPENDENCY_0));
        depCol.setWidth("60%");
        depCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(depCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add description details
        CmsListItemDetails descriptionDetails = new CmsListItemDetails(LIST_DETAIL_DESCRIPTION);
        descriptionDetails.setAtColumn(LIST_COLUMN_NAME);
        descriptionDetails.setVisible(false);
        descriptionDetails.setShowActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_DESCRIPTION_NAME_0));
        descriptionDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_ROLES_DETAIL_SHOW_DESCRIPTION_HELP_0));
        descriptionDetails.setHideActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_DESCRIPTION_NAME_0));
        descriptionDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_ROLES_DETAIL_HIDE_DESCRIPTION_HELP_0));
        descriptionDetails.setName(Messages.get().container(Messages.GUI_ROLES_DETAIL_DESCRIPTION_NAME_0));
        descriptionDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_ROLES_DETAIL_DESCRIPTION_NAME_0)));
        metadata.addItemDetails(descriptionDetails);

        // add users details
        CmsListItemDetails usersDetails = new CmsListItemDetails(LIST_DETAIL_USERS);
        usersDetails.setAtColumn(LIST_COLUMN_NAME);
        usersDetails.setVisible(false);
        usersDetails.setShowActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_USERS_NAME_0));
        usersDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_USERS_HELP_0));
        usersDetails.setHideActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_USERS_NAME_0));
        usersDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_USERS_HELP_0));
        usersDetails.setName(Messages.get().container(Messages.GUI_ROLES_DETAIL_USERS_NAME_0));
        usersDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_ROLES_DETAIL_USERS_NAME_0)));
        metadata.addItemDetails(usersDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

}
