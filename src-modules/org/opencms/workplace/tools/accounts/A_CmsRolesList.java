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
import org.opencms.security.CmsRole;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * User roles overview view.<p>
 *
 * @since 6.5.6
 */
public abstract class A_CmsRolesList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_DEPENDENCY = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_GROUP_NAME = "cgn";

    /** list column id constant. */
    public static final String LIST_COLUMN_HIDDEN_NAME = "chn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_DESCRIPTION = "dd";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_PATH = "dp";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Stores the value of the request parameter for the organizational unit. */
    private String m_paramOufqn;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     */
    protected A_CmsRolesList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName, LIST_COLUMN_HIDDEN_NAME, CmsListOrderEnum.ORDER_ASCENDING, null);
    }

    /**
     * Returns the right icon path for the given list item.<p>
     *
     * @param item the list item to get the icon path for
     *
     * @return the icon path for the given role
     */
    public abstract String getIconPath(CmsListItem item);

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
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List<CmsListItem> roles = getList().getAllContent();
        Iterator<CmsListItem> itRoles = roles.iterator();
        while (itRoles.hasNext()) {
            CmsListItem item = itRoles.next();
            String roleName = item.get(LIST_COLUMN_GROUP_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_PATH)) {
                    html.append(
                        OpenCms.getOrgUnitManager().readOrganizationalUnit(
                            getCms(),
                            CmsOrganizationalUnit.getParentFqn(roleName)).getDisplayName(getLocale()));
                } else if (detailId.equals(LIST_DETAIL_DESCRIPTION)) {
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
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        List<CmsRole> roles = getRoles();
        Locale locale = getCms().getRequestContext().getLocale();
        Map<String, String> dependencies = new HashMap<String, String>();
        for (CmsRole role : roles) {
            for (CmsRole child : role.getChildren(true)) {
                String deps = dependencies.get(child.getRoleName());
                if (deps == null) {
                    deps = "";
                } else {
                    deps += ", ";
                }
                deps += role.getName(locale);
                dependencies.put(child.getRoleName(), deps);
            }
        }
        for (CmsRole role : roles) {
            CmsListItem item = getList().newItem(role.getGroupName());

            item.set(LIST_COLUMN_NAME, role.getName(locale));
            String dependency = dependencies.get(role.getRoleName());
            if (dependency == null) {
                dependency = "";
            }
            item.set(LIST_COLUMN_DEPENDENCY, dependency);
            item.set(LIST_COLUMN_HIDDEN_NAME, "" + (1000 + dependency.length()));
            item.set(LIST_COLUMN_GROUP_NAME, role.getGroupName());
            ret.add(item);
        }
        return ret;
    }

    /**
     * Returns all roles to display.<p>
     *
     * @return a list of {@link CmsRole} objects
     *
     * @throws CmsException if something goes wrong
     */
    protected abstract List<CmsRole> getRoles() throws CmsException;

    /**
     * Returns if the organizational unit details button should be displayed.<p>
     *
     * @return if the organizational unit details button should be displayed
     */
    protected boolean includeOuDetails() {

        return true;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("1%");
        iconCol.setSorteable(false);

        // adds a role icon
        CmsListDirectAction dirAction = new CmsListDefaultAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return ((A_CmsRolesList)getWp()).getIconPath(getItem());
            }

        };
        dirAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ICON_NAME_0));
        dirAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ICON_HELP_0));
        dirAction.setIconPath(PATH_BUTTONS + "role.png");
        dirAction.setEnabled(false);
        iconCol.addDirectAction(dirAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_NAME_0));
        nameCol.setWidth("40%");
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for path
        CmsListColumnDefinition depCol = new CmsListColumnDefinition(LIST_COLUMN_DEPENDENCY);
        depCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_DEPENDENCY_0));
        depCol.setWidth("60%");
        depCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(depCol);

        // create column for hidden name
        CmsListColumnDefinition hideNameCol = new CmsListColumnDefinition(LIST_COLUMN_HIDDEN_NAME);
        hideNameCol.setSorteable(true);
        hideNameCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(hideNameCol);
        hideNameCol.setPrintable(false);

        // create column for group name
        CmsListColumnDefinition groupNameCol = new CmsListColumnDefinition(LIST_COLUMN_GROUP_NAME);
        groupNameCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(groupNameCol);
        groupNameCol.setPrintable(false);
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

        if (includeOuDetails()) {
            // add role path
            CmsListItemDetails pathDetails = new CmsListItemDetails(LIST_DETAIL_PATH);
            pathDetails.setAtColumn(LIST_COLUMN_NAME);
            pathDetails.setVisible(false);
            pathDetails.setShowActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_PATH_NAME_0));
            pathDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_SHOW_PATH_HELP_0));
            pathDetails.setHideActionName(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_PATH_NAME_0));
            pathDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_ROLES_DETAIL_HIDE_PATH_HELP_0));
            pathDetails.setName(Messages.get().container(Messages.GUI_ROLES_DETAIL_PATH_NAME_0));
            pathDetails.setFormatter(
                new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_ROLES_DETAIL_PATH_NAME_0)));
            metadata.addItemDetails(pathDetails);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
    }
}