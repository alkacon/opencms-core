/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsShowUserRolesList.java,v $
 * Date   : $Date: 2007/01/31 15:57:03 $
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

import org.opencms.file.CmsGroup;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User roles overview view.<p>
 * 
 * @author Raphael Schnuck  
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.5.6 
 */
public class CmsShowUserRolesList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_HIDE_NAME = "chn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_PATH = "cp";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_DESCRIPTION = "dd";

    /** list id constant. */
    public static final String LIST_ID = "lsur";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Stores the value of the request parameter for the organizational unit. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsShowUserRolesList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     */
    public CmsShowUserRolesList(CmsJspActionElement jsp, String listId) {

        this(jsp, listId, Messages.get().container(Messages.GUI_USERROLES_LIST_NAME_0), false);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsShowUserRolesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
    protected CmsShowUserRolesList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        boolean searchable) {

        super(jsp, listId, listName, LIST_COLUMN_NAME, CmsListOrderEnum.ORDER_ASCENDING, searchable ? LIST_COLUMN_NAME
        : null);
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
    public void executeListSingleActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
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
     * Returns the User id parameter value.<p>
     *
     * @return the User id parameter value
     */
    public String getParamUserid() {

        return m_paramUserid;
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
     * Sets the User id parameter value.<p>
     * 
     * @param userid the userid to set
     */
    public void setParamUserid(String userid) {

        m_paramUserid = userid;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    protected String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
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
        List roles = OpenCms.getRoleManager().getRolesOfUser(
            getCms(),
            getCms().readUser(new CmsUUID(getParamUserid())).getName(),
            getParamOufqn(),
            false,
            false,
            true);

        int todo = -1;
        // sort list of roles that children will be displayed after their parent role
        //        List tempRoles = new ArrayList();
        //        Iterator itRoles = roles.iterator();
        //        while (itRoles.hasNext()) {
        //            CmsGroup group = (CmsGroup)itRoles.next();
        //            CmsRole role = CmsRole.valueOf(group.getName());
        //            if (role.getParentRole() == null) {
        //                tempRoles.add(0, role);
        //            } else if (!roles.contains(role.getParentRole())) {
        //                tempRoles.add(role);
        //            }
        //            role.getDistinctGroupNames()
        //        }

        Iterator itRoles = roles.iterator();
        while (itRoles.hasNext()) {
            CmsGroup role = (CmsGroup)itRoles.next();
            CmsListItem item = getList().newItem(role.getId().toString());
            item.set(LIST_COLUMN_NAME, CmsRole.valueOf(role).getName(getCms().getRequestContext().getLocale()));
            item.set(LIST_COLUMN_HIDE_NAME, role.getName());
            item.set(LIST_COLUMN_PATH, role.getOuFqn());
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
        iconCol.setName(Messages.get().container(Messages.GUI_ROLES_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_ROLES_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);

        // adds a role icon
        CmsListDirectAction dirAction = new CmsListDefaultAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
             */
            public String getIconPath() {

                List roles = getList().getAllContent();
                Iterator itRoles = roles.iterator();
                List roleObjects = new ArrayList();
                try {
                    while (itRoles.hasNext()) {
                        CmsListItem item = (CmsListItem)itRoles.next();
                        roleObjects.add(CmsRole.valueOf(getCms().readGroup((String)item.get(LIST_COLUMN_HIDE_NAME))));
                    }

                    CmsRole role = CmsRole.valueOf(getCms().readGroup((String)getItem().get(LIST_COLUMN_HIDE_NAME)));
                    if (role.getParentRole() != null && roleObjects.contains(role.getParentRole())) {
                        return PATH_BUTTONS + "role_child.png";
                    }
                } catch (CmsException e) {
                    return super.getIconPath();
                }
                int todo = -1;
                //define the return value for roles inherited from another ou

                return super.getIconPath();
            }
        };
        dirAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ICON_NAME_0));
        dirAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ICON_HELP_0));
        dirAction.setIconPath(PATH_BUTTONS + "role.png");
        dirAction.setEnabled(false);
        iconCol.addDirectAction(dirAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        // create column for hidden name
        CmsListColumnDefinition hideNameCol = new CmsListColumnDefinition(LIST_COLUMN_HIDE_NAME);
        hideNameCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(hideNameCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_ROLES_LIST_COLS_NAME_0));
        nameCol.setWidth("35%");
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for path
        CmsListColumnDefinition descCol = new CmsListColumnDefinition(LIST_COLUMN_PATH);
        descCol.setName(Messages.get().container(Messages.GUI_ROLES_LIST_COLS_PATH_0));
        descCol.setWidth("65%");
        descCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(descCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add role description
        CmsListItemDetails descDetails = new CmsListItemDetails(LIST_DETAIL_DESCRIPTION);
        descDetails.setAtColumn(LIST_COLUMN_NAME);
        descDetails.setVisible(false);
        descDetails.setShowActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_DESCRIPTION_NAME_0));
        descDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_DESCRIPTION_HELP_0));
        descDetails.setHideActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_DESCRIPTION_NAME_0));
        descDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_DESCRIPTION_HELP_0));
        descDetails.setName(Messages.get().container(Messages.GUI_ROLES_DETAIL_DESCRIPTION_NAME_0));
        descDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_ROLES_DETAIL_DESCRIPTION_NAME_0)));
        metadata.addItemDetails(descDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }
}
