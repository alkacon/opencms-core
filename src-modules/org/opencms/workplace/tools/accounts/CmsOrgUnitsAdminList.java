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
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListMetadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Admin organization unit management view.<p>
 *
 * @since 6.5.6
 */
public class CmsOrgUnitsAdminList extends A_CmsOrgUnitsList {

    /** list id constant. */
    public static final String LIST_ID = "lsoua";

    /** list action id constant. */
    protected static final String LIST_ACTION_OVERVIEW = "ao";

    /** list column id constant. */
    protected static final String LIST_COLUMN_OVERVIEW = "co";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsOrgUnitsAdminList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_ADMINORGUNITS_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsOrgUnitsAdminList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     *
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtml()
     */
    @Override
    public String defaultActionHtml() {

        if ((getList() != null) && getList().getAllContent().isEmpty()) {
            // TODO: check the need for this
            refreshList();
        }
        StringBuffer result = new StringBuffer(2048);
        result.append(defaultActionHtmlStart());
        result.append(customHtmlStart());
        try {

            if (hasMoreAdminOUs()) {
                result.append(defaultActionHtmlContent());
            }
        } catch (CmsException e) {
            // noop
        }
        result.append(customHtmlEnd());
        result.append(defaultActionHtmlEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        String ouFqn = getSelectedItem().get(LIST_COLUMN_NAME).toString();
        if (ouFqn == null) {
            ouFqn = "";
        }
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {ouFqn.substring(1)});
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
        if (getParamListAction().equals(LIST_ACTION_OVERVIEW)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/orgunit", params);
        } else if (getParamListAction().equals(LIST_ACTION_USER)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getUsersToolPath(), params);
        } else if (getParamListAction().equals(LIST_ACTION_GROUP)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getGroupsToolPath(), params);
        } else if (getParamListAction().equals(LIST_DEFACTION_OVERVIEW)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/orgunit", params);
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * Performs a forward to the overview of the single organizational unit the current user
     * is allowed to administrate.<p>
     *
     * @throws ServletException in case of errors during forwarding
     * @throws IOException in case of errors during forwarding
     * @throws CmsException in case of errors during getting orgunits
     */
    public void forwardToSingleAdminOU() throws ServletException, IOException, CmsException {

        List<CmsOrganizationalUnit> orgUnits = getOrgUnits();

        if (orgUnits.isEmpty()) {
            OpenCms.getWorkplaceManager().getToolManager().jspForwardTool(this, "/", null);
            return;
        }

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {orgUnits.get(0).getName()});
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});

        OpenCms.getWorkplaceManager().getToolManager().jspForwardTool(this, getForwardToolPath(), params);
    }

    /**
     * Returns the path of the overview icon.<p>
     *
     * @return the path of the overview icon
     */
    public String getOverviewIcon() {

        return PATH_BUTTONS + "orgunit.png";
    }

    /**
     * Checks if the user has more then one organizational unit to administrate.<p>
     *
     * @return true if the user has more then then one organizational unit to administrate
     *         otherwise false
     * @throws CmsException if the organizational units can not be read
     */
    public boolean hasMoreAdminOUs() throws CmsException {

        List<CmsOrganizationalUnit> orgUnits = getOrgUnits();

        if (orgUnits == null) {
            return false;
        }
        if (orgUnits.size() <= 1) {
            return false;
        }
        return true;
    }

    /**
     * Returns the tool path to forward if there is only one single organizational unit.<p>
     *
     * @return the tool path to forward
     */
    protected String getForwardToolPath() {

        return "/accounts/orgunit";
    }

    /**
     * Returns the tool path of the groups management tool.<p>
     *
     * @return the tool path of the groups management tool
     */
    protected String getGroupsToolPath() {

        return getCurrentToolPath() + "/orgunit/groups";
    }

    /**
     * Returns the tool path of the users management tool.<p>
     *
     * @return the tool path of the users management tool
     */
    protected String getUsersToolPath() {

        return getCurrentToolPath() + "/orgunit/users";
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for overview
        CmsListColumnDefinition overviewCol = new CmsListColumnDefinition(LIST_COLUMN_OVERVIEW);
        overviewCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_OVERVIEW_0));
        overviewCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_OVERVIEW_HELP_0));
        overviewCol.setWidth("20");
        overviewCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        overviewCol.setSorteable(false);
        // add overview action
        CmsListDirectAction overviewAction = new CmsListDirectAction(LIST_ACTION_OVERVIEW) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                if (getItem() != null) {
                    if (((Boolean)getItem().get(LIST_COLUMN_WEBUSER)).booleanValue()) {
                        return PATH_BUTTONS + "webuser_ou.png";
                    }
                }
                return super.getIconPath();
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getName()
             */
            @Override
            public CmsMessageContainer getName() {

                if (getItem() != null) {
                    if (((Boolean)getItem().get(LIST_COLUMN_WEBUSER)).booleanValue()) {
                        return Messages.get().container(Messages.GUI_WEBOUS_LIST_ACTION_OVERVIEW_NAME_0);
                    }
                }
                return super.getName();
            }
        };
        overviewAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_ACTION_OVERVIEW_NAME_0));
        overviewAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_OVERVIEW_HELP_0));
        overviewAction.setIconPath(getOverviewIcon());
        overviewCol.addDirectAction(overviewAction);
        // add it to the list definition
        metadata.addColumn(overviewCol);

        // create column for user
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_USER_0));
        userCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_USER_HELP_0));
        userCol.setWidth("20");
        userCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        userCol.setSorteable(false);
        // add user action
        CmsListDirectAction userAction = new CmsListDirectAction(LIST_ACTION_USER);
        userAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_ACTION_USER_NAME_0));
        userAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_USER_HELP_0));
        userAction.setIconPath(getUserIcon());
        userCol.addDirectAction(userAction);
        // add it to the list definition
        metadata.addColumn(userCol);

        // create column for group
        CmsListColumnDefinition groupCol = new CmsListColumnDefinition(LIST_COLUMN_GROUP);
        groupCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_GROUP_0));
        groupCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_GROUP_HELP_0));
        groupCol.setWidth("20");
        groupCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        groupCol.setSorteable(false);
        // add group action
        CmsListDirectAction groupAction = new CmsListDirectAction(LIST_ACTION_GROUP);
        groupAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_ACTION_GROUP_NAME_0));
        groupAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_GROUP_HELP_0));
        groupAction.setIconPath(getGroupIcon());
        groupCol.addDirectAction(groupAction);
        // add it to the list definition
        metadata.addColumn(groupCol);

        // create column for description
        CmsListColumnDefinition descCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_DESCRIPTION_0));
        descCol.setWidth("60%");
        descCol.setSorteable(true);
        // create default overview action
        CmsListDefaultAction defOverviewAction = new CmsListDefaultAction(LIST_DEFACTION_OVERVIEW);
        defOverviewAction.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_DEFACTION_OVERVIEW_NAME_0));
        defOverviewAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_OVERVIEW_HELP_0));
        descCol.addDefaultAction(defOverviewAction);
        // add it to the list definition
        metadata.addColumn(descCol);

        // create column for name / path
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_ORGUNITS_LIST_COLS_NAME_0));
        nameCol.setWidth("40%");
        nameCol.setSorteable(true);
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for manageable flag
        CmsListColumnDefinition adminCol = new CmsListColumnDefinition(LIST_COLUMN_ADMIN);
        adminCol.setVisible(false);
        metadata.addColumn(adminCol);

        // create column for webuser flag
        CmsListColumnDefinition webuserCol = new CmsListColumnDefinition(LIST_COLUMN_WEBUSER);
        webuserCol.setVisible(false);
        metadata.addColumn(webuserCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (Boolean.parseBoolean(getParamForce())) {
            if (!hasMoreAdminOUs()) {
                // jump one level higher
                throw new Exception();
            }
        }
    }
}
