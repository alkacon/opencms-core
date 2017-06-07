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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User groups overview view.<p>
 *
 * @since 6.0.0
 */
public class CmsShowUserGroupsList extends A_CmsUserGroupsList {

    /** list id constant. */
    public static final String LIST_ID = "lsug";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsShowUserGroupsList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     */
    public CmsShowUserGroupsList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_USERGROUPS_LIST_NAME_0), false);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsShowUserGroupsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
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
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlEnd()
     */
    @Override
    protected String defaultActionHtmlEnd() {

        return "<div class=\"dialogspacer\" unselectable=\"on\">&nbsp;</div>";
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    protected String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#getGroups(boolean)
     */
    @Override
    protected List<CmsGroup> getGroups(boolean withOtherOus) throws CmsException {

        return getCms().getGroupsOfUser(getParamUsername(), false, withOtherOus);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#setDefaultAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setDefaultAction(CmsListColumnDefinition nameCol) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#setIconAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setIconAction(CmsListColumnDefinition iconCol) {

        // adds a direct group icon
        CmsListDirectAction dirAction = new CmsGroupStateAction(LIST_ACTION_ICON_DIRECT, true);
        dirAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_DIRECT_NAME_0));
        dirAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_DIRECT_HELP_0));
        dirAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "group.png");
        dirAction.setEnabled(false);
        iconCol.addDirectAction(dirAction);

        // adds an indirect group icon
        CmsListDirectAction indirAction = new CmsGroupStateAction(LIST_ACTION_ICON_INDIRECT, false);
        indirAction.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_INDIRECT_NAME_0));
        indirAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_INDIRECT_HELP_0));
        indirAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "group_indirect.png");
        indirAction.setEnabled(false);
        iconCol.addDirectAction(indirAction);

        iconCol.setListItemComparator(new CmsListItemActionIconComparator());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        super.setIndependentActions(metadata);

        metadata.getItemDetailDefinition(LIST_DETAIL_OTHEROU).setVisible(false);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserGroupsList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setStateActionCol(CmsListMetadata metadata) {

        // no-op
    }
}
