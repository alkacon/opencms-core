/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/workflow/Attic/CmsWorkflowList.java,v $
 * Date   : $Date: 2006/08/25 11:01:42 $
 * Version: $Revision: 1.1.2.4 $
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
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.util.ArrayList;
import java.util.Date;
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
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.5.0 
 */
public class CmsWorkflowList extends A_CmsListDialog {

    /** list column id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_DATE = "cdc";

    /** list column id constant. */
    public static final String LIST_COLUMN_HIDDEN = "ch";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

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

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_WORKFLOW_LIST_NAME_0),
            LIST_COLUMN_OWNER,
            CmsListOrderEnum.ORDER_ASCENDING,
            LIST_COLUMN_NAME);
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
            item.set(LIST_COLUMN_NAME, OpenCms.getWorkflowManager().getTaskDescription(workflow));
            item.set(LIST_COLUMN_TYPE, OpenCms.getWorkflowManager().getTaskType(workflow, getLocale()));
            item.set(LIST_COLUMN_STATE, OpenCms.getWorkflowManager().getTaskState(workflow, getLocale()));
            item.set(LIST_COLUMN_DATE, new Date(OpenCms.getWorkflowManager().getTaskStartTime(workflow)));
            item.set(LIST_COLUMN_USER_CREATED, OpenCms.getWorkflowManager().getTaskOwner(workflow).getName());
            I_CmsPrincipal agent = OpenCms.getWorkflowManager().getTaskAgent(workflow);
            item.set(LIST_COLUMN_OWNER, agent.getName());
            CmsUser curUser = getCms().getRequestContext().currentUser();
            boolean myWf = false;
            if (agent.equals(curUser)) {
                // if the current user is the agent itself
                myWf = true;
            } else {
                try {
                    List groups = getCms().getGroupsOfUser(curUser.getName());
                    // if the current user is member of the agent group
                    myWf = groups.contains(agent);
                } catch (Exception e) {
                    // ignore any exception
                }
            }
            item.set(LIST_COLUMN_HIDDEN, Boolean.valueOf(myWf));
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
        metadata.addColumn(iconCol);

        // icon action
        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            public String getIconPath() {

                if (((Boolean)getItem().get(LIST_COLUMN_HIDDEN)).booleanValue()) {
                    return "tools/workflow/buttons/my_workflow.png";
                } else {
                    return "tools/workflow/buttons/other_workflow.png";
                }
            }
        };
        iconAction.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_ACTION_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_WORKFLOW_LIST_ACTION_ICON_HELP_0));
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_NAME_0));
        nameCol.setWidth("25%");
        nameCol.setSorteable(true);
        metadata.addColumn(nameCol);

        // add column for type
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_TYPE_0));
        typeCol.setWidth("15%");
        typeCol.setSorteable(true);
        metadata.addColumn(typeCol);

        // add column for state
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_STATE_0));
        stateCol.setWidth("15%");
        stateCol.setSorteable(true);
        metadata.addColumn(stateCol);

        // add column for date created
        CmsListColumnDefinition dateCol = new CmsListColumnDefinition(LIST_COLUMN_DATE);
        dateCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_DATE_0));
        dateCol.setWidth("15%");
        dateCol.setSorteable(true);
        dateCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(dateCol);

        // add column for user created
        CmsListColumnDefinition userCreatedCol = new CmsListColumnDefinition(LIST_COLUMN_USER_CREATED);
        userCreatedCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_USER_CREATED_0));
        userCreatedCol.setWidth("15%");
        userCreatedCol.setSorteable(true);
        metadata.addColumn(userCreatedCol);

        // add column for user owner
        CmsListColumnDefinition ownerCol = new CmsListColumnDefinition(LIST_COLUMN_OWNER);
        ownerCol.setName(Messages.get().container(Messages.GUI_WORKFLOW_LIST_COLS_OWNER_0));
        ownerCol.setWidth("15%");
        ownerCol.setSorteable(true);
        metadata.addColumn(ownerCol);

        // add column for hidden info
        CmsListColumnDefinition hiddenCol = new CmsListColumnDefinition(LIST_COLUMN_HIDDEN);
        hiddenCol.setVisible(false);
        metadata.addColumn(hiddenCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // create list item detail
        CmsListItemDetails resourcesDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCES);
        resourcesDetails.setAtColumn(LIST_COLUMN_NAME);
        resourcesDetails.setVisible(false);
        resourcesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_WORKFLOW_LABEL_RESOURCES_0)));
        resourcesDetails.setShowActionName(Messages.get().container(Messages.GUI_WORKFLOW_DETAIL_SHOW_RESOURCES_NAME_0));
        resourcesDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_WORKFLOW_DETAIL_SHOW_RESOURCES_HELP_0));
        resourcesDetails.setHideActionName(Messages.get().container(Messages.GUI_WORKFLOW_DETAIL_HIDE_RESOURCES_NAME_0));
        resourcesDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_WORKFLOW_DETAIL_HIDE_RESOURCES_HELP_0));

        // add resources info item detail to meta data
        metadata.addItemDetails(resourcesDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // no ma
    }
}