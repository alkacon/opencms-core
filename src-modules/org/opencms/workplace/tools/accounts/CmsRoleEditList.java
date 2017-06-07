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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User roles overview view.<p>
 *
 * @since 6.5.6
 */
public class CmsRoleEditList extends A_CmsRolesList {

    /** list action id constant. */
    public static final String LIST_ACTION_ACTIVATE = "aa";

    /** list action id constant. */
    public static final String LIST_ACTION_DEACTIVATE = "ac";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIVATE = "ca";

    /** list id constant. */
    public static final String LIST_ID = "lsre";

    /** list action id constant. */
    public static final String LIST_MACTION_ACTIVATE = "ma";

    /** list action id constant. */
    public static final String LIST_MACTION_DEACTIVATE = "mc";

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRoleEditList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     */
    public CmsRoleEditList(CmsJspActionElement jsp, String listId) {

        this(jsp, listId, Messages.get().container(Messages.GUI_ROLEEDIT_LIST_NAME_0));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     */
    public CmsRoleEditList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRoleEditList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                CmsUser user = getCms().readUser(new CmsUUID(getParamUserid()));
                Iterator<CmsListItem> itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = itItems.next();
                    CmsGroup group = getCms().readGroup((String)listItem.get(LIST_COLUMN_GROUP_NAME));
                    CmsRole role = CmsRole.valueOf(group);
                    if (!OpenCms.getRoleManager().hasRole(getCms(), user.getName(), role)) {
                        OpenCms.getRoleManager().addUserToRole(getCms(), role, user.getName());
                        getCms().writeUser(user);
                    }
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_SELECTED_USERS_0), e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
        } else if (getParamListAction().equals(LIST_MACTION_DEACTIVATE)) {
            // execute the activate multiaction
            try {
                CmsUser user = getCms().readUser(new CmsUUID(getParamUserid()));
                Iterator<CmsListItem> itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = itItems.next();
                    CmsGroup group = getCms().readGroup((String)listItem.get(LIST_COLUMN_GROUP_NAME));
                    CmsRole role = CmsRole.valueOf(group);
                    if (OpenCms.getRoleManager().hasRole(getCms(), user.getName(), role)) {
                        OpenCms.getRoleManager().removeUserFromRole(getCms(), role, user.getName());
                        getCms().writeUser(user);
                    }
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DEACTIVATE_SELECTED_USERS_0), e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws CmsRuntimeException {

        String roleName = getSelectedItem().getId();
        try {
            CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
            CmsUser user = getCms().readUser(new CmsUUID(getParamUserid()));

            if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
                // execute the activate action
                try {
                    OpenCms.getRoleManager().addUserToRole(getCms(), role, user.getName());
                    getCms().writeUser(user);
                } catch (CmsException e) {
                    throw new CmsRuntimeException(
                        Messages.get().container(Messages.ERR_ACTIVATE_ROLE_2, roleName, user.getName()),
                        e);
                }
            } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
                // execute the activate action
                if (!OpenCms.getRoleManager().getRolesOfUser(getCms(), user.getName(), "", true, true, true).contains(
                    role)) {
                    throw new CmsRuntimeException(
                        Messages.get().container(Messages.ERR_DEACTIVATE_INDIRECT_ROLE_2, roleName, user.getName()));
                }
                try {
                    OpenCms.getRoleManager().removeUserFromRole(getCms(), role, user.getName());
                    getCms().writeUser(user);
                } catch (CmsException e) {
                    throw new CmsRuntimeException(
                        Messages.get().container(Messages.ERR_DEACTIVATE_ROLE_2, roleName, user.getName()),
                        e);
                }
            } else {
                throwListUnsupportedActionException();
            }
            listSave();
        } catch (CmsException e) {
            // should never happen
        }
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#getIconPath(CmsListItem)
     */
    @Override
    public String getIconPath(CmsListItem item) {

        try {
            CmsRole role = CmsRole.valueOf(getCms().readGroup((String)item.get(LIST_COLUMN_GROUP_NAME)));
            if (OpenCms.getRoleManager().hasRole(
                getCms(),
                getCms().readUser(new CmsUUID(getParamUserid())).getName(),
                role)) {
                return PATH_BUTTONS + "role.png";
            }
        } catch (Exception e) {
            // ignore
        }
        return PATH_BUTTONS + "role_inactive.png";
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
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    protected String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsRolesList#getRoles()
     */
    @Override
    protected List<CmsRole> getRoles() throws CmsException {

        return OpenCms.getRoleManager().getRolesOfUser(
            getCms(),
            getCms().getRequestContext().getCurrentUser().getName(),
            getParamOufqn(),
            false,
            false,
            true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        // create column for activation/deactivation
        CmsListColumnDefinition actCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        actCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ACTIVATE_0));
        actCol.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ACTIVATE_HELP_0));
        actCol.setWidth("20");
        actCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        actCol.setListItemComparator(new CmsListItemActionIconComparator());

        // activate action
        CmsListDirectAction actAction = new CmsListDirectAction(LIST_ACTION_ACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    String roleName = getItem().getId();
                    try {
                        CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
                        if (OpenCms.getRoleManager().hasRole(
                            getCms(),
                            getCms().readUser(new CmsUUID(((CmsRoleEditList)getWp()).getParamUserid())).getName(),
                            role)) {
                            return false;
                        }
                        return true;
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        actAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_ACTIVATE_NAME_0));
        actAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_ACTIVATE_HELP_0));
        actAction.setIconPath(ICON_INACTIVE);
        actCol.addDirectAction(actAction);

        // deactivate action
        CmsListDirectAction deactAction = new CmsListDirectAction(LIST_ACTION_DEACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    String roleName = getItem().getId();
                    try {
                        CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
                        if (OpenCms.getRoleManager().hasRole(
                            getCms(),
                            getCms().readUser(new CmsUUID(((CmsRoleEditList)getWp()).getParamUserid())).getName(),
                            role)) {
                            return true;
                        }
                        return false;
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        deactAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_DEACTIVATE_NAME_0));
        deactAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_DEACTIVATE_HELP_0));
        deactAction.setIconPath(ICON_ACTIVE);
        actCol.addDirectAction(deactAction);

        // add it to the list definition
        metadata.addColumn(actCol, 1);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add description details
        CmsListItemDetails descriptionDetails = new CmsListItemDetails(LIST_DETAIL_DESCRIPTION);
        descriptionDetails.setAtColumn(LIST_COLUMN_NAME);
        descriptionDetails.setVisible(true);
        descriptionDetails.setShowActionName(
            Messages.get().container(Messages.GUI_ROLEEDIT_DETAIL_SHOW_DESCRIPTION_NAME_0));
        descriptionDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_ROLEEDIT_DETAIL_SHOW_DESCRIPTION_HELP_0));
        descriptionDetails.setHideActionName(
            Messages.get().container(Messages.GUI_ROLEEDIT_DETAIL_HIDE_DESCRIPTION_NAME_0));
        descriptionDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_ROLEEDIT_DETAIL_HIDE_DESCRIPTION_HELP_0));
        descriptionDetails.setName(Messages.get().container(Messages.GUI_ROLEEDIT_DETAIL_DESCRIPTION_NAME_0));
        descriptionDetails.setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            public String format(Object data, Locale locale) {

                StringBuffer html = new StringBuffer(512);
                html.append("<table border='0' cellspacing='0' cellpadding='0'>\n");
                html.append("\t<tr>\n");
                html.append("\t\t<td style='white-space:normal;' >\n");
                html.append("\t\t\t");
                html.append(data == null ? "" : data);
                html.append("\n");
                html.append("\t\t</td>\n");
                html.append("\t</tr>\n");
                html.append("</table>\n");
                return html.toString();
            }
        });
        metadata.addItemDetails(descriptionDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the activate role multi action
        CmsListMultiAction activateRole = new CmsListMultiAction(LIST_MACTION_ACTIVATE);
        activateRole.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_MACTION_ACTIVATE_NAME_0));
        activateRole.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_MACTION_ACTIVATE_HELP_0));
        activateRole.setIconPath(ICON_MULTI_ACTIVATE);
        metadata.addMultiAction(activateRole);

        // add the deactivate role multi action
        CmsListMultiAction deactivateRole = new CmsListMultiAction(LIST_MACTION_DEACTIVATE);
        deactivateRole.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_MACTION_DEACTIVATE_NAME_0));
        deactivateRole.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_MACTION_DEACTIVATE_HELP_0));
        deactivateRole.setIconPath(ICON_MULTI_DEACTIVATE);
        metadata.addMultiAction(deactivateRole);
    }
}
