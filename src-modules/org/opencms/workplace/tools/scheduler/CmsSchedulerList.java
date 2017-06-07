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

package org.opencms.workplace.tools.scheduler;

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDefaultComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main scheduled jobs management list view.<p>
 *
 * Defines the list columns and possible actions for scheduled jobs.<p>
 *
 * @since 6.0.0
 */
public class CmsSchedulerList extends A_CmsListDialog {

    /** List action activate. */
    public static final String LIST_ACTION_ACTIVATE = "aa";

    /** List action copy. */
    public static final String LIST_ACTION_COPY = "ac";

    /** List action deactivate. */
    public static final String LIST_ACTION_DEACTIVATE = "at";

    /** List action delete. */
    public static final String LIST_ACTION_DELETE = "ad";

    /** List action edit. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** List action execute. */
    public static final String LIST_ACTION_EXECUTE = "exec";

    /** List column activate. */
    public static final String LIST_COLUMN_ACTIVATE = "ca";

    /** List column class. */
    public static final String LIST_COLUMN_ACTIVE = "cac";

    /** List column class. */
    public static final String LIST_COLUMN_CLASS = "cs";

    /** List column copy. */
    public static final String LIST_COLUMN_COPY = "cc";

    /** List column delete. */
    public static final String LIST_COLUMN_DELETE = "cd";

    /** List column edit. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** List column execute. */
    public static final String LIST_COLUMN_EXECUTE = "c_exec";

    /** List column last execution. */
    public static final String LIST_COLUMN_LASTEXE = "cl";

    /** List column name. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** List column next execution. */
    public static final String LIST_COLUMN_NEXTEXE = "cx";

    /** List action edit. */
    public static final String LIST_DEFACTION_EDIT = "de";

    /** List detail context info. */
    public static final String LIST_DETAIL_CONTEXTINFO = "dc";

    /** List detail parameter. */
    public static final String LIST_DETAIL_PARAMETER = "dp";

    /** List ID. */
    public static final String LIST_ID = "lj";

    /** List action multi activate. */
    public static final String LIST_MACTION_ACTIVATE = "ma";

    /** List action multi deactivate. */
    public static final String LIST_MACTION_DEACTIVATE = "mc";

    /** List action multi delete. */
    public static final String LIST_MACTION_DELETE = "md";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/scheduler/buttons/";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSchedulerList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            new CmsMessageContainer(Messages.get(), Messages.GUI_JOBS_LIST_NAME_0),
            LIST_COLUMN_NAME,
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
    public CmsSchedulerList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id
     * of the action to execute.<p>
     *
     * @throws CmsRuntimeException to signal that an action is not supported
     *
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_DELETE)) {
            // execute the delete multiaction
            List<String> removedItems = new ArrayList<String>();
            Iterator<CmsListItem> itItems = getSelectedItems().iterator();
            while (itItems.hasNext()) {
                CmsListItem listItem = itItems.next();
                try {
                    OpenCms.getScheduleManager().unscheduleJob(getCms(), listItem.getId());
                    removedItems.add(listItem.getId());
                } catch (CmsException e) {
                    throw new CmsRuntimeException(
                        Messages.get().container(Messages.ERR_UNSCHEDULE_JOB_1, listItem.getId()),
                        e);
                }
            }
            // update the XML configuration
            writeConfiguration(false);
        } else if (getParamListAction().equals(LIST_MACTION_ACTIVATE)
            || getParamListAction().equals(LIST_MACTION_DEACTIVATE)) {
            // execute the activate or deactivate multiaction
            Iterator<CmsListItem> itItems = getSelectedItems().iterator();
            boolean activate = getParamListAction().equals(LIST_MACTION_ACTIVATE);
            while (itItems.hasNext()) {
                // toggle the active state of the selected item(s)
                CmsListItem listItem = itItems.next();
                try {
                    CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(
                        listItem.getId()).clone();
                    job.setActive(activate);
                    OpenCms.getScheduleManager().scheduleJob(getCms(), job);
                } catch (CmsException e) {
                    throw new CmsRuntimeException(
                        Messages.get().container(Messages.ERR_SCHEDULE_JOB_1, listItem.getId()),
                        e);
                }
            }
            // update the XML configuration
            writeConfiguration(true);
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        if (getParamListAction().equals(LIST_ACTION_EDIT) || getParamListAction().equals(LIST_DEFACTION_EDIT)) {
            // edit a job from the list
            String jobId = getSelectedItem().getId();
            // forward to the edit job screen with additional parameters
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsEditScheduledJobInfoDialog.PARAM_JOBID, new String[] {jobId});
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
            getToolManager().jspForwardTool(this, "/scheduler/edit", params);
        } else if (getParamListAction().equals(LIST_ACTION_COPY)) {
            // copy a job from the list
            String jobId = getSelectedItem().getId();
            // forward to the edit job screen with additional parameters
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsEditScheduledJobInfoDialog.PARAM_JOBID, new String[] {jobId});
            // set action parameter to copy job action
            params.put(CmsDialog.PARAM_ACTION, new String[] {CmsEditScheduledJobInfoDialog.DIALOG_COPYJOB});
            getToolManager().jspForwardTool(this, "/scheduler/new", params);
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // activate a job from the list
            String jobId = getSelectedItem().getId();
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(jobId).clone();
            job.setActive(true);
            try {
                OpenCms.getScheduleManager().scheduleJob(getCms(), job);
                // update the XML configuration
                writeConfiguration(true);
            } catch (CmsException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SCHEDULE_JOB_1, jobId), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
            // deactivate a job from the list
            String jobId = getSelectedItem().getId();
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(jobId).clone();
            job.setActive(false);
            try {
                OpenCms.getScheduleManager().scheduleJob(getCms(), job);
                // update the XML configuration
                writeConfiguration(true);
            } catch (CmsException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_UNSCHEDULE_JOB_1, jobId), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // delete a job from the list
            String jobId = getSelectedItem().getId();
            try {
                OpenCms.getScheduleManager().unscheduleJob(getCms(), jobId);
                // update the XML configuration
                writeConfiguration(false);
            } catch (CmsRoleViolationException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_JOB_1, jobId), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_EXECUTE)) {
            String jobId = getSelectedItem().getId();
            CmsScheduleManager scheduler = OpenCms.getScheduleManager();
            scheduler.executeDirectly(jobId);
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

        // get all scheduled jobs from manager
        Iterator<CmsListItem> i = getList().getAllContent().iterator();
        while (i.hasNext()) {
            CmsListItem item = i.next();
            CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(item.getId());
            if (detailId.equals(LIST_DETAIL_CONTEXTINFO)) {
                // job details: context info
                item.set(LIST_DETAIL_CONTEXTINFO, job.getContextInfo());
            } else if (detailId.equals(LIST_DETAIL_PARAMETER)) {
                // job details: parameter
                StringBuffer params = new StringBuffer(32);
                Iterator<String> paramIt = job.getParameters().keySet().iterator();
                while (paramIt.hasNext()) {
                    String param = paramIt.next();
                    String value = job.getParameters().get(param);
                    params.append(param).append("=");
                    params.append(value).append("<br>");
                }
                item.set(LIST_DETAIL_PARAMETER, params);
            } else {
                continue;
            }
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> items = new ArrayList<CmsListItem>();

        // get all scheduled jobs from manager
        Iterator<CmsScheduledJobInfo> i = OpenCms.getScheduleManager().getJobs().iterator();
        while (i.hasNext()) {
            CmsScheduledJobInfo job = i.next();
            CmsListItem item = getList().newItem(job.getId());
            // set the contents of the columns
            item.set(LIST_COLUMN_NAME, job.getJobName());
            item.set(LIST_COLUMN_CLASS, job.getClassName());
            item.set(LIST_COLUMN_LASTEXE, job.getExecutionTimePrevious());
            item.set(LIST_COLUMN_NEXTEXE, job.getExecutionTimeNext());
            item.set(LIST_COLUMN_ACTIVE, Boolean.valueOf(job.isActive()));
            items.add(item);
        }

        return items;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific messages
        addMessages(Messages.get().getBundleName());
        // add default messages
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // add column for edit action
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_COL_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // create default edit action for edit column: edit job
        CmsListDirectAction editColAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editColAction.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0));
        editColAction.setIconPath(PATH_BUTTONS + "edit.png");
        editColAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_HELP_0));
        editColAction.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_CONF_0));
        // set action for the edit column
        editCol.addDirectAction(editColAction);
        metadata.addColumn(editCol);

        // add column for activate/deactivate action
        CmsListColumnDefinition activateCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        activateCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_ACTIVE_0));
        activateCol.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_COL_ACTIVE_HELP_0));
        activateCol.setWidth("20");
        activateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        activateCol.setListItemComparator(new CmsListItemActionIconComparator());

        // direct action: activate job
        CmsListDirectAction jobActAction = new CmsListDirectAction(LIST_ACTION_ACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    return !((Boolean)getItem().get(LIST_COLUMN_ACTIVE)).booleanValue();
                }
                return super.isVisible();
            }
        };
        jobActAction.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_NAME_0));
        jobActAction.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_CONF_0));
        jobActAction.setIconPath(ICON_INACTIVE);
        jobActAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_HELP_0));
        activateCol.addDirectAction(jobActAction);

        // direct action: deactivate job
        CmsListDirectAction jobDeactAction = new CmsListDirectAction(LIST_ACTION_DEACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    return ((Boolean)getItem().get(LIST_COLUMN_ACTIVE)).booleanValue();
                }
                return super.isVisible();
            }
        };
        jobDeactAction.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DEACTIVATE_NAME_0));
        jobDeactAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DEACTIVATE_CONF_0));
        jobDeactAction.setIconPath(ICON_ACTIVE);
        jobDeactAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DEACTIVATE_HELP_0));
        activateCol.addDirectAction(jobDeactAction);

        metadata.addColumn(activateCol);

        // add column for copy action
        CmsListColumnDefinition copyCol = new CmsListColumnDefinition(LIST_COLUMN_COPY);
        copyCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_COPY_0));
        copyCol.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_COL_COPY_HELP_0));
        copyCol.setWidth("20");
        copyCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        copyCol.setListItemComparator(null);
        // direct action: copy job
        CmsListDirectAction copyJob = new CmsListDirectAction(LIST_ACTION_COPY);
        copyJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_COPY_NAME_0));
        copyJob.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_COPY_CONF_0));
        copyJob.setIconPath(PATH_BUTTONS + "copy.png");
        copyJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_COPY_HELP_0));
        copyCol.addDirectAction(copyJob);
        metadata.addColumn(copyCol);

        // add column for delete action
        CmsListColumnDefinition delCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        delCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_DELETE_0));
        delCol.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_COL_DELETE_HELP_0));
        delCol.setWidth("20");
        delCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        delCol.setListItemComparator(null);
        // direct action: delete job
        CmsListDirectAction delJob = new CmsListDirectAction(LIST_ACTION_DELETE);
        delJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DELETE_NAME_0));
        delJob.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DELETE_CONF_0));
        delJob.setIconPath(ICON_DELETE);
        delJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DELETE_HELP_0));
        delCol.addDirectAction(delJob);
        metadata.addColumn(delCol);

        // add column for delete action
        CmsListColumnDefinition execCol = new CmsListColumnDefinition(LIST_COLUMN_EXECUTE);
        execCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_EXECUTE_0));
        execCol.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_COL_EXECUTE_HELP_0));
        execCol.setWidth("20");
        execCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        execCol.setListItemComparator(null);
        CmsListDirectAction execJob = new CmsListDirectAction(LIST_ACTION_EXECUTE);
        execJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EXECUTE_NAME_0));
        execJob.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EXECUTE_CONF_0));
        execJob.setIconPath("list/rightarrow.png");
        execJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EXECUTE_HELP_0));
        execCol.addDirectAction(execJob);
        metadata.addColumn(execCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_NAME_0));
        nameCol.setWidth("30%");
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setListItemComparator(new CmsListItemDefaultComparator());
        // create default edit action for name column: edit job
        CmsListDefaultAction nameColAction = new CmsListDefaultAction(LIST_DEFACTION_EDIT);
        nameColAction.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0));
        nameColAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_HELP_0));
        nameColAction.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_CONF_0));
        // set action for the name column
        nameCol.addDefaultAction(nameColAction);
        metadata.addColumn(nameCol);

        // add column for class
        CmsListColumnDefinition classCol = new CmsListColumnDefinition(LIST_COLUMN_CLASS);
        classCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_CLASS_0));
        classCol.setWidth("20%");
        classCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        classCol.setListItemComparator(new CmsListItemDefaultComparator());
        metadata.addColumn(classCol);

        // add column for last execution time
        CmsListColumnDefinition lastExecCol = new CmsListColumnDefinition(LIST_COLUMN_LASTEXE);
        lastExecCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_LASTEXE_0));
        lastExecCol.setWidth("25%");
        lastExecCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        lastExecCol.setListItemComparator(new CmsListItemDefaultComparator());
        // create date formatter for last execution time
        lastExecCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(lastExecCol);

        // add column for next execution time
        CmsListColumnDefinition nextExecCol = new CmsListColumnDefinition(LIST_COLUMN_NEXTEXE);
        nextExecCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_NEXTEXE_0));
        nextExecCol.setWidth("25%");
        nextExecCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nextExecCol.setListItemComparator(new CmsListItemDefaultComparator());
        // add column for activation information
        CmsListColumnDefinition actInfoCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVE);
        actInfoCol.setVisible(false);
        metadata.addColumn(actInfoCol);

        // create date formatter for next execution time
        nextExecCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(nextExecCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add independent job context info button
        CmsListItemDetails jobsContextInfoDetails = new CmsListItemDetails(LIST_DETAIL_CONTEXTINFO);
        jobsContextInfoDetails.setAtColumn(LIST_COLUMN_NAME);
        jobsContextInfoDetails.setVisible(false);
        jobsContextInfoDetails.setShowActionName(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_SHOW_CONTEXTINFO_NAME_0));
        jobsContextInfoDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_SHOW_CONTEXTINFO_HELP_0));
        jobsContextInfoDetails.setHideActionName(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_HIDE_CONTEXTINFO_NAME_0));
        jobsContextInfoDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_HIDE_CONTEXTINFO_HELP_0));
        // create formatter to display context info
        CmsContextInfoDetailsFormatter contextFormatter = new CmsContextInfoDetailsFormatter();
        contextFormatter.setUserMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_USER_0));
        contextFormatter.setProjectMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_PROJECT_0));
        contextFormatter.setLocaleMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_LOCALE_0));
        contextFormatter.setRootSiteMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_ROOTSITE_0));
        contextFormatter.setEncodingMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_ENCODING_0));
        contextFormatter.setRemoteAddrMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_REMADR_0));
        contextFormatter.setRequestedURIMessage(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_REQURI_0));
        jobsContextInfoDetails.setFormatter(contextFormatter);
        // add context info item detail to meta data
        metadata.addItemDetails(jobsContextInfoDetails);

        // add independent job parameter button
        CmsListItemDetails jobsParameterDetails = new CmsListItemDetails(LIST_DETAIL_PARAMETER);
        jobsParameterDetails.setAtColumn(LIST_COLUMN_NAME);
        jobsParameterDetails.setVisible(false);
        jobsParameterDetails.setShowActionName(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_SHOW_PARAMETER_NAME_0));
        jobsParameterDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_SHOW_PARAMETER_HELP_0));
        jobsParameterDetails.setHideActionName(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_HIDE_PARAMETER_NAME_0));
        jobsParameterDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_JOBS_DETAIL_HIDE_PARAMETER_HELP_0));
        // create formatter to display parameters
        jobsParameterDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_JOBS_DETAIL_PARAMETER_FORMAT_0)));
        // add parameter item to metadata
        metadata.addItemDetails(jobsParameterDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the activate job multi action
        CmsListMultiAction activateJob = new CmsListMultiAction(LIST_MACTION_ACTIVATE);
        activateJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MACTIVATE_NAME_0));
        activateJob.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MACTIVATE_CONF_0));
        activateJob.setIconPath(ICON_MULTI_ACTIVATE);
        activateJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MACTIVATE_HELP_0));
        metadata.addMultiAction(activateJob);

        // add the deactivate job multi action
        CmsListMultiAction deactivateJob = new CmsListMultiAction(LIST_MACTION_DEACTIVATE);
        deactivateJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEACTIVATE_NAME_0));
        deactivateJob.setConfirmationMessage(
            Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEACTIVATE_CONF_0));
        deactivateJob.setIconPath(ICON_MULTI_DEACTIVATE);
        deactivateJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEACTIVATE_HELP_0));
        metadata.addMultiAction(deactivateJob);

        // add the delete job multi action
        CmsListMultiAction deleteJobs = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteJobs.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDELETE_NAME_0));
        deleteJobs.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDELETE_CONF_0));
        deleteJobs.setIconPath(ICON_MULTI_DELETE);
        deleteJobs.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDELETE_HELP_0));
        metadata.addMultiAction(deleteJobs);
    }

    /**
     * Writes the updated scheduled job info back to the XML configuration file and refreshes the complete list.<p>
     *
     * @param refresh if true, the list items are refreshed
     */
    protected void writeConfiguration(boolean refresh) {

        // update the XML configuration
        OpenCms.writeConfiguration(CmsSystemConfiguration.class);
        if (refresh) {
            refreshList();
        }
    }
}