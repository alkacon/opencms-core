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

package org.opencms.workplace.tools.publishqueue;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobFinished;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
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

import org.apache.commons.logging.Log;

/**
 * Provides a list for finished publish reports of the current user.<p>
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
    public static final String LIST_ACTION_STATE_ERR = "ate";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE_OK = "ato";

    /** list action id constant. */
    public static final String LIST_ACTION_VIEW = "av";

    /** list id constant. */
    public static final String LIST_ID = "lppq";

    /** list column id constant. */
    private static final String LIST_ACTION_USER = "au";

    /** list column id constant. */
    private static final String LIST_COLUMN_ENDTIME = "ce";

    /** list column id constant. */
    private static final String LIST_COLUMN_ERRORS = "cse";

    /** list column id constant. */
    private static final String LIST_COLUMN_ID = "ci";

    /** list column id constant. */
    private static final String LIST_COLUMN_PROJECT = "cp";

    /** list column id constant. */
    private static final String LIST_COLUMN_RESCOUNT = "cr";

    /** list column id constant. */
    private static final String LIST_COLUMN_STARTTIME = "cs";

    /** list column id constant. */
    private static final String LIST_COLUMN_STATE = "ct";

    /** list column id constant. */
    private static final String LIST_COLUMN_STATE_ICON = "csi";

    /** list column id constant. */
    private static final String LIST_COLUMN_USER = "cu";

    /** list column id constant. */
    private static final String LIST_COLUMN_VIEW = "cv";

    /** list column id constant. */
    private static final String LIST_COLUMN_WARNINGS = "csw";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishQueuePersonalList.class);

    /** The path to the publish report state error icon. */
    private static final String PUBLISHQUEUE_ERROR_ICON = "tools/publishqueue/buttons/state_error.png";

    /** The path to the publish report state ok icon. */
    private static final String PUBLISHQUEUE_OK_ICON = "tools/publishqueue/buttons/state_ok.png";

    /** The path to the publish report view icon. */
    private static final String PUBLISHQUEUE_VIEW_BUTTON = "tools/publishqueue/buttons/publish_view.png";

    /** The path to the publish report state warning icon. */
    private static final String PUBLISHQUEUE_WARN_ICON = "tools/publishqueue/buttons/state_warning.png";

    /** Publish job state constant. */
    private static final String STATE_ERROR = "error";

    /** Publish job state constant. */
    private static final String STATE_OK = "ok";

    /** Publish job state constant. */
    private static final String STATE_WARNING = "warning";

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
    @Override
    public String defaultActionHtmlStart() {

        return new StringBuffer(getList().listJs()).append(dialogContentStart(getParamTitle())).toString();
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
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        String publishJobId = getSelectedItem().get(LIST_COLUMN_ID).toString();

        Map params = new HashMap();
        // set id parameter
        params.put(CmsPublishQueueHistoricalReportDialog.PARAM_ID, publishJobId);
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_ACTION_COUNT)
            || getParamListAction().equals(LIST_ACTION_END)
            || getParamListAction().equals(LIST_ACTION_START)
            || getParamListAction().equals(LIST_ACTION_STATE_OK)
            || getParamListAction().equals(LIST_ACTION_STATE_ERR)
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
    @Override
    protected void fillDetails(String detailId) {

        //noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List getListItems() {

        List ret = new ArrayList();

        List queue;
        if (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.ROOT_ADMIN)) {
            queue = OpenCms.getPublishManager().getPublishHistory();
        } else {
            queue = OpenCms.getPublishManager().getPublishHistory(getCms().getRequestContext().getCurrentUser());
        }
        Iterator iter = queue.iterator();
        while (iter.hasNext()) {
            CmsPublishJobFinished publishJob = (CmsPublishJobFinished)iter.next();
            CmsListItem item = getList().newItem(new Long(publishJob.getStartTime()).toString());
            Map state = getState(publishJob);
            item.set(LIST_COLUMN_PROJECT, publishJob.getProjectName());
            item.set(LIST_COLUMN_STARTTIME, new Date(publishJob.getStartTime()));
            item.set(LIST_COLUMN_ENDTIME, new Date(publishJob.getFinishTime()));
            item.set(LIST_COLUMN_STATE, state.get(LIST_COLUMN_STATE));
            item.set(LIST_COLUMN_RESCOUNT, new Integer(publishJob.getSize()));
            item.set(LIST_COLUMN_ID, publishJob.getPublishHistoryId());
            item.set(LIST_COLUMN_USER, publishJob.getUserName(getCms()));
            item.set(LIST_COLUMN_WARNINGS, state.get(LIST_COLUMN_WARNINGS));
            item.set(LIST_COLUMN_ERRORS, state.get(LIST_COLUMN_ERRORS));
            ret.add(item);
        }
        // set the user column visibility
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_USER).setVisible(
            OpenCms.getRoleManager().hasRole(getCms(), CmsRole.PROJECT_MANAGER));
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
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

        // create state icon column
        CmsListColumnDefinition stateIconCol = new CmsListColumnDefinition(LIST_COLUMN_STATE_ICON);
        stateIconCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_STATE_0));
        stateIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateIconCol.setWidth("20");
        // add state ok action
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_STATE_OK) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                String state = (String)getItem().get(LIST_COLUMN_STATE);
                return STATE_OK.equals(state);
            }
        };
        stateAction.setName(Messages.get().container(Messages.GUI_PUBLISH_JOB_FINISHED_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_HELP_0));
        stateAction.setIconPath(PUBLISHQUEUE_OK_ICON);
        stateIconCol.addDirectAction(stateAction);
        // add state err action
        CmsListDirectAction stateErrAction = new CmsListDirectAction(LIST_ACTION_STATE_ERR) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                String state = (String)getItem().get(LIST_COLUMN_STATE);
                if (STATE_ERROR.equals(state)) {
                    return PUBLISHQUEUE_ERROR_ICON;
                }
                return PUBLISHQUEUE_WARN_ICON;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            @Override
            public CmsMessageContainer getName() {

                Integer warns = (Integer)getItem().get(LIST_COLUMN_WARNINGS);
                Integer errors = (Integer)getItem().get(LIST_COLUMN_ERRORS);
                return Messages.get().container(Messages.GUI_PUBLISH_JOB_FINISHED_WITH_WARNS_2, warns, errors);
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                String state = (String)getItem().get(LIST_COLUMN_STATE);
                return !STATE_OK.equals(state);
            }
        };
        stateErrAction.setHelpText(Messages.get().container(Messages.GUI_PERSONALQUEUE_ACTION_VIEW_HELP_0));
        stateIconCol.addDirectAction(stateErrAction);
        metadata.addColumn(stateIconCol);

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
        startCol.setFormatter(
            new CmsListDateMacroFormatter(
                Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_WITH_SECONDS_1),
                Messages.get().container(org.opencms.workplace.list.Messages.GUI_LIST_DATE_FORMAT_NEVER_0)));
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
        endCol.setFormatter(
            new CmsListDateMacroFormatter(
                Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_WITH_SECONDS_1),
                Messages.get().container(org.opencms.workplace.list.Messages.GUI_LIST_DATE_FORMAT_NEVER_0)));
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

        // create hidden column for job id
        CmsListColumnDefinition idCol = new CmsListColumnDefinition(LIST_COLUMN_ID);
        idCol.setName(Messages.get().container(Messages.GUI_PERSONALQUEUE_COLS_ID_0));
        idCol.setSorteable(false);
        idCol.setVisible(false);
        metadata.addColumn(idCol);

        // create state error column
        CmsListColumnDefinition errCol = new CmsListColumnDefinition(LIST_COLUMN_ERRORS);
        errCol.setVisible(false);
        metadata.addColumn(errCol);

        // create state warning column
        CmsListColumnDefinition warnCol = new CmsListColumnDefinition(LIST_COLUMN_WARNINGS);
        warnCol.setVisible(false);
        metadata.addColumn(warnCol);

        // create state warning column
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setVisible(false);
        metadata.addColumn(stateCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        //noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        //noop
    }

    /**
     * Returns the state of the given publish job.<p>
     *
     * @param publishJob the publish job to get the state for
     *
     * @return the state of the given publish job
     */
    private Map getState(CmsPublishJobFinished publishJob) {

        Map result = new HashMap();
        byte[] reportBytes = null;
        try {
            reportBytes = OpenCms.getPublishManager().getReportContents(publishJob);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage());
            }
            result.put(LIST_COLUMN_STATE, STATE_OK);
        }
        if ((reportBytes != null) && (result.get(LIST_COLUMN_STATE) == null)) {
            String report = new String(reportBytes);
            // see org.opencms.report.CmsHtmlReport#print(String, int)
            if (report.indexOf("<span class='err'>") > -1) {
                result.put(LIST_COLUMN_STATE, STATE_ERROR);
                result.put(
                    LIST_COLUMN_ERRORS,
                    new Integer(CmsStringUtil.splitAsList(report, "<span class='err'>").size() - 1));
                result.put(
                    LIST_COLUMN_WARNINGS,
                    new Integer(CmsStringUtil.splitAsList(report, "<span class='warn'>").size() - 1));
            } else if (report.indexOf("<span class='warn'>") > -1) {
                result.put(LIST_COLUMN_STATE, STATE_WARNING);
                result.put(
                    LIST_COLUMN_WARNINGS,
                    new Integer(CmsStringUtil.splitAsList(report, "<span class='warn'>").size() - 1));
            } else {
                result.put(LIST_COLUMN_STATE, STATE_OK);
            }
        }

        if (result.get(LIST_COLUMN_WARNINGS) == null) {
            result.put(LIST_COLUMN_WARNINGS, new Integer(0));
        }
        if (result.get(LIST_COLUMN_ERRORS) == null) {
            result.put(LIST_COLUMN_ERRORS, new Integer(0));
        }

        return result;
    }
}
