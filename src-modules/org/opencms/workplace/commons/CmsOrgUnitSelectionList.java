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

package org.opencms.workplace.commons;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.workplace.list.A_CmsListDefaultJsAction;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;
import org.opencms.workplace.tools.CmsToolMacroResolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Organizational unit selection dialog.<p>
 *
 * @since 6.5.6
 */
public class CmsOrgUnitSelectionList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list action id constant. */
    public static final String LIST_ACTION_SELECT = "js";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_PATH = "cp";

    /** list id constant. */
    public static final String LIST_ID = "lous";

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the role group name to use to filter the selection. */
    private String m_paramRole;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsOrgUnitSelectionList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_NAME_0),
            LIST_COLUMN_PATH,
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
    public CmsOrgUnitSelectionList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.CmsToolDialog#dialogTitle()
     */
    @Override
    public String dialogTitle() {

        // build title
        StringBuffer html = new StringBuffer(512);
        html.append("<div class='screenTitle'>\n");
        html.append("\t<table width='100%' cellspacing='0'>\n");
        html.append("\t\t<tr>\n");
        html.append("\t\t\t<td>\n");
        html.append(key(Messages.GUI_ORGUNITSELECTION_INTRO_TITLE_0));
        html.append("\n\t\t\t</td>");
        html.append("\t\t</tr>\n");
        html.append("\t</table>\n");
        html.append("</div>\n");
        return CmsToolMacroResolver.resolveMacros(html.toString(), this);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * Returns the organizational unit fqn parameter value.<p>
     *
     * @return the organizational unit fqn parameter value
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Returns the value of the role parameter.<p>
     *
     * @return the value of the role parameter
     */
    public String getParamRole() {

        return m_paramRole;
    }

    /**
     * Sets the organizational unit fqn parameter value.<p>
     *
     * @param ouFqn the organizational unit fqn parameter value
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * Sets the value of the role parameter.<p>
     *
     * @param paramRole the value to set
     */
    public void setParamRole(String paramRole) {

        m_paramRole = paramRole;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        Iterator<CmsOrganizationalUnit> itOrgUnits = getOrgUnits().iterator();
        while (itOrgUnits.hasNext()) {
            CmsOrganizationalUnit orgUnit = itOrgUnits.next();
            CmsListItem item = getList().newItem(orgUnit.getName());
            item.set(LIST_COLUMN_DESCRIPTION, orgUnit.getDescription(getLocale()));
            item.set(LIST_COLUMN_PATH, CmsOrganizationalUnit.SEPARATOR + orgUnit.getName());
            ret.add(item);
        }

        return ret;
    }

    /**
     * Returns the list of organizational units for selection.<p>
     *
     * @return a list of organizational units
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsOrganizationalUnit> getOrgUnits() throws CmsException {

        List<CmsOrganizationalUnit> ret = new ArrayList<CmsOrganizationalUnit>();
        CmsRole role = null;
        if (getParamRole() != null) {
            role = CmsRole.valueOfGroupName(getParamRole());
        }
        String ou = getParamOufqn();
        if (ou == null) {
            ou = "";
        }
        if (role != null) {
            ret.addAll(OpenCms.getRoleManager().getOrgUnitsForRole(getCms(), role.forOrgUnit(ou), true));
        } else {
            ret.addAll(OpenCms.getOrgUnitManager().getOrganizationalUnits(getCms(), ou, true));
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        // set icon action
        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
        iconAction.setName(Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_ICON_HELP_0));
        iconAction.setIconPath("buttons/orgunit.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        // create column for description
        CmsListColumnDefinition descCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descCol.setName(Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_COLS_DESCRIPTION_0));
        descCol.setWidth("60%");
        CmsListDefaultAction selectAction = new A_CmsListDefaultJsAction(LIST_ACTION_SELECT) {

            /**
             * @see org.opencms.workplace.list.A_CmsListDirectJsAction#jsCode()
             */
            @Override
            public String jsCode() {

                return "window.opener.setOrgUnitFormValue('"
                    + getItem().get(LIST_COLUMN_PATH)
                    + "'); window.opener.focus(); window.close();";
            }
        };
        selectAction.setName(Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_ACTION_SELECT_NAME_0));
        selectAction.setHelpText(Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_ACTION_SELECT_HELP_0));
        descCol.addDefaultAction(selectAction);
        // add it to the list definition
        metadata.addColumn(descCol);

        // create column for path
        CmsListColumnDefinition pathCol = new CmsListColumnDefinition(LIST_COLUMN_PATH);
        pathCol.setName(Messages.get().container(Messages.GUI_ORGUNITSELECTION_LIST_COLS_PATH_0));
        pathCol.setWidth("40%");
        pathCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(pathCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_PATH));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_DESCRIPTION));
        searchAction.setCaseInSensitive(true);
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        try {
            OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn());
        } catch (Exception e) {
            setParamOufqn(null);
        }
    }
}
