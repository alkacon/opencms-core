/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User roles overview view.<p>
 *
 * @since 6.5.6
 */
public class CmsShowUserRolesList extends A_CmsRolesList {

    /** list id constant. */
    public static final String LIST_ID = "lsur";

    /** Cached value. */
    private Boolean m_hasRolesInOtherOus;

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

        this(jsp, listId, Messages.get().container(Messages.GUI_USERROLES_LIST_NAME_0));
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
     */
    protected CmsShowUserRolesList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#getIconPath(CmsListItem)
     */
    @Override
    public String getIconPath(CmsListItem item) {

        List<CmsListItem> roles = getList().getAllContent();
        Iterator<CmsListItem> itRoles = roles.iterator();
        List<CmsRole> roleObjects = new ArrayList<CmsRole>();
        try {
            while (itRoles.hasNext()) {
                CmsListItem listItem = itRoles.next();
                roleObjects.add(CmsRole.valueOf(getCms().readGroup((String)listItem.get(LIST_COLUMN_GROUP_NAME))));
            }

            CmsRole role = CmsRole.valueOf(getCms().readGroup((String)item.get(LIST_COLUMN_GROUP_NAME)));
            if ((role.getParentRole() != null) && roleObjects.contains(role.getParentRole())) {
                if (role.getOuFqn().equals(getParamOufqn())) {
                    return PATH_BUTTONS + "role_child.png";
                } else {
                    return PATH_BUTTONS + "role_other_ou_inherit.png";
                }
            }
            if (role.getOuFqn().equals(getParamOufqn())) {
                return PATH_BUTTONS + "role.png";
            } else {
                return PATH_BUTTONS + "role_other_ou.png";
            }
        } catch (CmsException e) {
            return PATH_BUTTONS + "role.png";
        }
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
     * Sets the User id parameter value.<p>
     *
     * @param userid the userid to set
     */
    public void setParamUserid(String userid) {

        m_paramUserid = userid;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#writeDialog()
     */
    @Override
    public void writeDialog() throws IOException {

        try {
            if (!getCms().readUser(new CmsUUID(getParamUserid())).isWebuser()) {
                super.writeDialog();
            }
        } catch (CmsException e) {
            // ignore
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    protected String defaultActionHtmlStart() {

        return dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#getRoles()
     */
    @Override
    protected List<CmsRole> getRoles() throws CmsException {

        List<CmsRole> allRoles = OpenCms.getRoleManager().getRolesOfUser(
            getCms(),
            getCms().readUser(new CmsUUID(getParamUserid())).getName(),
            "",
            true,
            true,
            false);

        List<CmsRole> childRoles = OpenCms.getRoleManager().getRolesOfUser(
            getCms(),
            getCms().readUser(new CmsUUID(getParamUserid())).getName(),
            getParamOufqn(),
            false,
            false,
            false);

        Iterator<CmsRole> itChildRoles = childRoles.iterator();
        while (itChildRoles.hasNext()) {
            CmsRole role = itChildRoles.next();
            if (!allRoles.contains(role)) {
                allRoles.add(role);
            }
        }
        return allRoles;
    }

    /**
     * Returns true if the list of users has users of other organizational units.<p>
     *
     * @return <code>true</code> if the list of users has users of other organizational units
     */
    protected boolean hasRolesInOtherOus() {

        if (m_hasRolesInOtherOus == null) {
            m_hasRolesInOtherOus = Boolean.FALSE;
            try {
                Iterator<CmsRole> itRoles = getRoles().iterator();
                while (itRoles.hasNext()) {
                    CmsRole role = itRoles.next();
                    if (!role.getOuFqn().equals(getParamOufqn())) {
                        m_hasRolesInOtherOus = Boolean.TRUE;
                        break;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return m_hasRolesInOtherOus.booleanValue();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#includeOuDetails()
     */
    @Override
    protected boolean includeOuDetails() {

        return false;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        super.setIndependentActions(metadata);

        // add other ou button
        CmsListItemDetails pathDetails = new CmsListItemDetails(LIST_DETAIL_PATH);
        pathDetails.setAtColumn(LIST_COLUMN_NAME);
        pathDetails.setVisible(false);
        pathDetails.setHideAction(new CmsListIndependentAction(LIST_DETAIL_PATH) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_HIDE;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                return ((CmsShowUserRolesList)getWp()).hasRolesInOtherOus();
            }
        });
        pathDetails.setShowAction(new CmsListIndependentAction(LIST_DETAIL_PATH) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_SHOW;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                return ((CmsShowUserRolesList)getWp()).hasRolesInOtherOus();
            }
        });
        pathDetails.setShowActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_PATH_NAME_0));
        pathDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_PATH_HELP_0));
        pathDetails.setHideActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_PATH_NAME_0));
        pathDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_PATH_HELP_0));
        pathDetails.setName(Messages.get().container(Messages.GUI_ROLES_DETAIL_PATH_NAME_0));
        pathDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_ROLES_DETAIL_PATH_NAME_0)));
        metadata.addItemDetails(pathDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }
}