/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/workflow/Attic/CmsWorkflowList.java,v $
 * Date   : $Date: 2006/08/21 17:04:18 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.workflow;

import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Workflow list.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.0 
 */
public class CmsWorkflowList extends A_CmsListDialog {

    /** list column id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_OWNER = "co";

    /** list column id constant. */
    public static final String LIST_COLUMN_STATE = "cs";

    /** list column id constant. */
    public static final String LIST_COLUMN_TYPE = "ct";

    /** list column id constant. */
    public static final String LIST_COLUMN_USER_CREATED = "cd";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_RESOURCES = "dr";

    /** list id constant. */
    public static final String LIST_ID = "lwf";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsWorkflowList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_WORKFLOW_LIST_NAME_0), null, null, null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsWorkflowList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListIndepActions()
     */
    public void executeListIndepActions() {

        super.executeListIndepActions();
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
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // get content
        List users = getList().getAllContent();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsListItem item = (CmsListItem)itUsers.next();
            int projectId = Integer.parseInt(item.getId());
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_RESOURCES)) {
                    // resources
                    Iterator itResources = getCms().readProjectResources(getCms().readProject(projectId)).iterator();
                    while (itResources.hasNext()) {
                        html.append(itResources.next());
                        if (itResources.hasNext()) {
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
        // get content
        List workflows = OpenCms.getWorkflowManager().getTasks();
        Iterator itWorkflows = workflows.iterator();
        while (itWorkflows.hasNext()) {
            CmsProject workflow = (CmsProject)itWorkflows.next();
            CmsListItem item = getList().newItem("" + workflow.getId());
            item.set(LIST_COLUMN_NAME, workflow.getName());
            item.set(LIST_COLUMN_USER_CREATED, OpenCms.getWorkflowManager().getTaskOwner(workflow).getName());
            item.set(LIST_COLUMN_OWNER, OpenCms.getWorkflowManager().getTaskAgent(workflow).getName());
            item.set(LIST_COLUMN_STATE, OpenCms.getWorkflowManager().getTaskState(workflow, getLocale()));
            item.set(LIST_COLUMN_TYPE, OpenCms.getWorkflowManager().getTaskType(workflow, getLocale()));
            ret.add(item);
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_ICON_0));
        iconCol.setWidth("30");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);

        // icon action
        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
        iconAction.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_ACTION_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_WORKFLOW_LIST_ACTION_ICON_HELP_0));
        iconAction.setIconPath("tools/workflow/buttons/workflow.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_NAME_0));
        nameCol.setWidth("20%");
        nameCol.setSorteable(true);
        metadata.addColumn(nameCol);

        // add column for user created
        CmsListColumnDefinition userCreatedCol = new CmsListColumnDefinition(LIST_COLUMN_USER_CREATED);
        userCreatedCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_USER_CREATED_0));
        userCreatedCol.setWidth("20%");
        userCreatedCol.setSorteable(true);
        metadata.addColumn(userCreatedCol);

        // add column for user owner
        CmsListColumnDefinition ownerCol = new CmsListColumnDefinition(LIST_COLUMN_OWNER);
        ownerCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_OWNER_0));
        ownerCol.setWidth("20%");
        ownerCol.setSorteable(true);
        metadata.addColumn(ownerCol);

        // add column for state
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_STATE_0));
        stateCol.setWidth("20%");
        stateCol.setSorteable(true);
        metadata.addColumn(stateCol);

        // add column for type
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_TYPE_0));
        typeCol.setWidth("20%");
        typeCol.setSorteable(true);
        metadata.addColumn(typeCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // no ias
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // no ma
    }
}