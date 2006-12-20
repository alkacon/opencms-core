/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/publishqueue/CmsPublishQueuePersonalList.java,v $
 * Date   : $Date: 2006/12/20 14:01:20 $
 * Version: $Revision: 1.1.2.3 $
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

package org.opencms.workplace.tools.publishqueue;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobFinished;
import org.opencms.security.CmsRole;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a list for finished publish reports of the current user.<p> 
 *
 * @author Raphael Schnuck
 * 
 * @version $Revision: 1.1.2.3 $ 
 * 
 * @since 6.5.5
 */
public class CmsPublishQueuePersonalList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_COUNT = "ac";

    /** list action id constant. */
    public static final String LIST_ACTION_END = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_PROJECT = "ap";

    /** list action id constant. */
    public static final String LIST_ACTION_START = "as";

    /** list action id constant. */
    public static final String LIST_ACTION_VIEW = "av";

    /** list id constant. */
    public static final String LIST_ID = "lppq";

    /** list column id constant. */
    private static final String LIST_ACTION_USER = "au";

    /** list column id constant. */
    private static final String LIST_COLUMN_ENDTIME = "ce";

    /** list column id constant. */
    private static final String LIST_COLUMN_FILE = "cf";

    /** list column id constant. */
    private static final String LIST_COLUMN_PROJECT = "cp";

    /** list column id constant. */
    private static final String LIST_COLUMN_RESCOUNT = "cr";

    /** list column id constant. */
    private static final String LIST_COLUMN_STARTTIME = "ct";

    /** list column id constant. */
    private static final String LIST_COLUMN_USER = "cu";

    /** list column id constant. */
    private static final String LIST_COLUMN_VIEW = "cv";

    /** The path to the publish report view icon. */
    private static final String PUBLISHQUEUE_VIEW_BUTTON = "tools/publishqueue/buttons/publish_view.png";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPublishQueuePersonalList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPublishQueuePersonalList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsPublishQueuePersonalList(CmsJspActionElement jsp, String listId) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_PERSONALQUEUE_LIST_NAME_0),
            LIST_COLUMN_STARTTIME,
            CmsListOrderEnum.ORDER_DESCENDING,
            null);
    }

    /**
     * Overrides the implementation to skip generation of gray header. <p>
     * 
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    public String defaultActionHtmlStart() {

        return new StringBuffer(getList().listJs()).append(dialogContentStart(getParamTitle())).toString();
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
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        String fileName = getSelectedItem().get(LIST_COLUMN_FILE).toString();

        Map params = new HashMap();
        // set file name parameter
        params.put(CmsPublishQueueHistoricalReportDialog.PARAM_FILENAME, fileName);
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_ACTION_COUNT)
            || getParamListAction().equals(LIST_ACTION_END)
            || getParamListAction().equals(LIST_ACTION_START)
            || getParamListAction().equals(LIST_ACTION_USER)
            || getParamListAction().equals(LIST_ACTION_PROJECT)
            || getParamListAction().equals(LIST_ACTION_VIEW)) {
            // forward to the view publish report screen
            getToolManager().jspForwardTool(this, getCurrentToolPath() + "/view", params);
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        //noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List ret = new ArrayList();

        List queue;
        if (getCms().hasRole(CmsRole.ADMINISTRATOR)) {
            queue = OpenCms.getPublishManager().getPublishHistory();
        } else {
            queue = OpenCms.getPublishManager().getPublishHistory(getCms().getRequestContext().currentUser());
        }
        Iterator iter = queue.iterator();
        while (iter.hasNext()) {
            CmsPublishJobFinished publishJob = (CmsPublishJobFinished)iter.next();
            CmsListItem item = getList().newItem(new Long(publishJob.getStartTime()).toString());
            item.set(LIST_COLUMN_PROJECT, publishJob.getProjectName(getLocale()));
            item.set(LIST_COLUMN_STARTTIME, new Date(publishJob.getStartTime()));
            item.set(LIST_COLUMN_ENDTIME, new Date(publishJob.getFinishTime()));
            item.set(LIST_COLUMN_RESCOUNT, new Integer(publishJob.getSize()));
            item.set(LIST_COLUMN_FILE, publishJob.getReportFilePath());
            item.set(LIST_COLUMN_USER, publishJob.getUserName());
            ret.add(item);
        }
        // set the user column visibility
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_USER).setVisible(
            getCms().hasRole(CmsRole.PROJECT_MANAGER));
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create view column
        CmsListColumnDefinition viewCol = new CmsListColumnDefinition(LIST_COLUMN_VIEW);
        viewCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_VIEW_0));
        viewCol.setWidth("20");
        viewCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        viewCol.setSorteable(false);
        // add view action
        CmsListDirectAction viewAction = new CmsListDirectAction(LIST_ACTION_VIEW);
        viewAction.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_NAME_0));
        viewAction.setHelpText(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_HELP_0));
        viewAction.setIconPath(PUBLISHQUEUE_VIEW_BUTTON);
        viewCol.addDirectAction(viewAction);
        // add it to the list definition
        metadata.addColumn(viewCol);

        // create project column
        CmsListColumnDefinition projectCol = new CmsListColumnDefinition(LIST_COLUMN_PROJECT);
        projectCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_PROJECT_0));
        projectCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        projectCol.setWidth("30%");
        // add default action
        CmsListDefaultAction projectAction = new CmsListDefaultAction(LIST_ACTION_PROJECT);
        projectAction.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_NAME_0));
        projectAction.setHelpText(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_HELP_0));
        projectCol.addDefaultAction(projectAction);
        metadata.addColumn(projectCol);

        // create start time column
        CmsListColumnDefinition startCol = new CmsListColumnDefinition(LIST_COLUMN_STARTTIME);
        startCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_STARTPUBLISHING_0));
        startCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        startCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        startCol.setWidth("20%");
        // add default action 
        CmsListDefaultAction startAction = new CmsListDefaultAction(LIST_ACTION_START);
        startAction.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_NAME_0));
        startAction.setHelpText(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_HELP_0));
        startCol.addDefaultAction(startAction);
        metadata.addColumn(startCol);

        // create end time column
        CmsListColumnDefinition endCol = new CmsListColumnDefinition(LIST_COLUMN_ENDTIME);
        endCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_STOPPUBLISHING_0));
        endCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        endCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        endCol.setWidth("20%");
        // add default action
        CmsListDefaultAction endAction = new CmsListDefaultAction(LIST_ACTION_END);
        endAction.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_NAME_0));
        endAction.setHelpText(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_HELP_0));
        endCol.addDefaultAction(endAction);
        metadata.addColumn(endCol);

        // create user column
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_USER_0));
        userCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        userCol.setWidth("25%");
        // add default action
        CmsListDefaultAction userAction = new CmsListDefaultAction(LIST_ACTION_USER);
        userAction.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_NAME_0));
        userAction.setHelpText(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_HELP_0));
        userCol.addDefaultAction(userAction);
        metadata.addColumn(userCol);

        // create resource count column
        CmsListColumnDefinition countCol = new CmsListColumnDefinition(LIST_COLUMN_RESCOUNT);
        countCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_RESCOUNT_0));
        countCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        countCol.setWidth("5%");
        // add default action
        CmsListDefaultAction countAction = new CmsListDefaultAction(LIST_ACTION_COUNT);
        countAction.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_NAME_0));
        countAction.setHelpText(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_HELP_0));
        countCol.addDefaultAction(countAction);
        metadata.addColumn(countCol);

        // create hidden column file name
        CmsListColumnDefinition fileCol = new CmsListColumnDefinition(LIST_COLUMN_FILE);
        fileCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_FILE_0));
        fileCol.setSorteable(false);
        fileCol.setVisible(false);
        metadata.addColumn(fileCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        //noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        //noop
    }
}
