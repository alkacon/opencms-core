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

import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsStringUtil;
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
 * Group selection dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsGroupSelectionList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list action id constant. */
    public static final String LIST_ACTION_SELECT = "js";

    /** list column id constant. */
    public static final String LIST_COLUMN_DISPLAY = "cdis";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list id constant. */
    public static final String LIST_ID = "lug";

    /** Stores the value of the request parameter for the flags. */
    private String m_paramFlags;

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the user name. */
    private String m_paramUser;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsGroupSelectionList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_NAME_0),
            LIST_COLUMN_DISPLAY,
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
    public CmsGroupSelectionList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamUser())) {
            html.append(
                Messages.get().getBundle(getLocale()).key(Messages.GUI_GROUPSELECTION_USER_TITLE_1, getParamUser()));
        } else {
            html.append(Messages.get().getBundle(getLocale()).key(Messages.GUI_GROUPSELECTION_INTRO_TITLE_0));
        }
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
     * Returns the flags parameter value.<p>
     *
     * @return the flags parameter value
     */
    public String getParamFlags() {

        return m_paramFlags;
    }

    /**
     * Returns the organizational unit fqn parameter.<p>
     *
     * @return the organizational unit fqn paramter
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Returns the user name parameter.<p>
     *
     * @return the user name paramter
     */
    public String getParamUser() {

        return m_paramUser;
    }

    /**
     * Sets the flags parameter value.<p>
     *
     * @param flags the flags parameter value to set
     */
    public void setParamFlags(String flags) {

        m_paramFlags = flags;
    }

    /**
     * Sets the organizational unit fqn.<p>
     *
     * @param ouFqn the organizational unit fqn to set
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * Sets the user name.<p>
     *
     * @param userName the user name to set
     */
    public void setParamUser(String userName) {

        m_paramUser = userName;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * Returns the groups to show for selection.<p>
     *
     * @return A list of group objects
     *
     * @throws CmsException if womething goes wrong
     */
    protected List<CmsGroup> getGroups() throws CmsException {

        List<CmsGroup> groups = new ArrayList<CmsGroup>();
        if (getParamUser() != null) {
            groups.addAll(getCms().getGroupsOfUser(getParamUser(), false));
        } else {
            groups.addAll(OpenCms.getRoleManager().getManageableGroups(getCms(), "", true));
        }
        if (getParamFlags() != null) {
            int flags = Integer.parseInt(getParamFlags());
            CmsPrincipal.filterFlag(groups, flags);
        }
        if ((getParamOufqn() != null) && !getParamOufqn().equals("null")) {
            Iterator<CmsGroup> groupsIter = groups.iterator();
            while (groupsIter.hasNext()) {
                CmsGroup group = groupsIter.next();
                if (!group.getOuFqn().startsWith(getParamOufqn())) {
                    groupsIter.remove();
                }
            }
        }
        return groups;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();

        // get content
        List<CmsGroup> groups = getGroups();
        Iterator<CmsGroup> itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = itGroups.next();
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set(LIST_COLUMN_NAME, group.getName());
            item.set(LIST_COLUMN_DISPLAY, OpenCms.getWorkplaceManager().translateGroupName(group.getName(), true));
            ret.add(item);
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
        iconCol.setName(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        // set icon action
        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
        iconAction.setName(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_ICON_HELP_0));
        iconAction.setIconPath("buttons/group.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_COLS_NAME_0));
        nameCol.setVisible(false);
        metadata.addColumn(nameCol);

        // create column for login
        CmsListColumnDefinition displayCol = new CmsListColumnDefinition(LIST_COLUMN_DISPLAY);
        displayCol.setName(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_COLS_NAME_0));
        displayCol.setWidth("100%");
        CmsListDefaultAction selectAction = new A_CmsListDefaultJsAction(LIST_ACTION_SELECT) {

            /**
             * @see org.opencms.workplace.list.A_CmsListDirectJsAction#jsCode()
             */
            @Override
            public String jsCode() {

                return "window.opener.setGroupFormValue('"
                    + getItem().get(LIST_COLUMN_NAME)
                    + "'); window.opener.focus(); window.close();";
            }
        };
        selectAction.setName(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_ACTION_SELECT_NAME_0));
        selectAction.setHelpText(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_ACTION_SELECT_HELP_0));
        displayCol.addDefaultAction(selectAction);
        // add it to the list definition
        metadata.addColumn(displayCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_DISPLAY));
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
            getCms().readUser(getParamUser()).getName();
        } catch (Exception e) {
            setParamUser(null);
        }
        try {
            Integer.valueOf(getParamFlags());
        } catch (Throwable e) {
            setParamFlags(null);
        }
    }
}
