/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/jobs/Attic/CmsJobsAdminTool.java,v $
 * Date   : $Date: 2005/05/10 15:59:00 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.jobs;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.CmsSchedulerException;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.workplace.list.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main scheduled jobs management list view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com) 
 * @version $Revision: 1.9 $
 * @since 5.7.3
 */
public class CmsJobsAdminTool extends A_CmsListDialog {

    /** List action activate. */
    public static final String LIST_ACTION_ACTIVATE = "activate";
    
    /** List action copy. */
    public static final String LIST_ACTION_COPY = "copy";
    
    /** List action deactivate. */
    public static final String LIST_ACTION_DEACTIVATE = "deactivate";
    
    /** List action delete. */
    public static final String LIST_ACTION_DELETE = "delete";
    
    /** List action edit. */
    public static final String LIST_ACTION_EDIT = "edit";
    
    /** List action multi activate. */
    public static final String LIST_ACTION_MACTIVATE = "mactivate";
    
    /** List action multi deactivate. */
    public static final String LIST_ACTION_MDEACTIVATE = "mdeactivate";
    
    /** List action multi delete. */
    public static final String LIST_ACTION_MDELETE = "mdelete";
    
    /** List column activate. */
    public static final String LIST_COLUMN_ACTIVATE = "activate";
    
    /** List column class. */
    public static final String LIST_COLUMN_CLASS = "class";
    
    /** List column copy. */
    public static final String LIST_COLUMN_COPY = "copy";
    
    /** List column delete. */
    public static final String LIST_COLUMN_DELETE = "delete";
    
    /** List column last execution. */
    public static final String LIST_COLUMN_LASTEXE = "lastexe";
    
    /** List column name. */
    public static final String LIST_COLUMN_NAME = "name";
    
    /** List column next execution. */
    public static final String LIST_COLUMN_NEXTEXE = "nextexe";
    
    /** List detail context info. */
    public static final String LIST_DETAIL_CONTEXTINFO = "contextinfo";
    
    /** List detail parameter. */
    public static final String LIST_DETAIL_PARAMETER = "parameter";
    
    /** List ID. */
    public static final String LIST_ID = "jobs";
    
    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/jobs/buttons/";
    
    /** Path to the list icons. */
    public static final String PATH_ICONS = "tools/jobs/icons/";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsJobsAdminTool(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, new CmsMessageContainer(Messages.get(), Messages.GUI_JOBS_LIST_NAME_0), LIST_COLUMN_NAME, LIST_COLUMN_NAME);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsJobsAdminTool(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     */
    public void executeListMultiActions() {

        if (getParamListAction().equals(LIST_ACTION_MDELETE)) {
            // execute the delete multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    OpenCms.getScheduleManager().unscheduleJob(getCms(), listItem.getId());
                    getList().removeItem(listItem.getId());
                }
            } catch (CmsException e) {
                throw new RuntimeException(e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_MACTIVATE) || getParamListAction().equals(LIST_ACTION_MDEACTIVATE)) {
                // execute the activate or deactivate multiaction
                try {
                    Iterator itItems = getSelectedItems().iterator();
                    boolean activate = getParamListAction().equals(LIST_ACTION_MACTIVATE);
                    while (itItems.hasNext()) {
                        CmsListItem listItem = (CmsListItem)itItems.next();
                        
                        CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(listItem.getId()).clone();
                        job.setActive(activate);
                        OpenCms.getScheduleManager().scheduleJob(getCms(), job);
                    }
                    refreshList();
                } catch (CmsException e) {
                    throw new RuntimeException(e);
                }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * This method should handle every defined list single action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     */
    public void executeListSingleActions() {

        if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            // edit a job from the list
            String jobId = getSelectedItem().getId();
            try {
                // forward to the edit job screen
                Map params = new HashMap();
                params.put("jobId", jobId);          
                getToolManager().jspRedirectTool(this, "/jobs/edit", params);
            } catch (IOException e) {
                // should never happen
                throw new RuntimeException(e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_COPY)) {
            // copy a job from the list
            String jobId = getSelectedItem().getId();
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(jobId).clone();
            job.setActive(true);
            // copy action has to be implemented!!
            int warn = 0;
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // activate a job from the list
            String jobId = getSelectedItem().getId();
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(jobId).clone();
            job.setActive(true);
            try {
                OpenCms.getScheduleManager().scheduleJob(getCms(), job);
                refreshList();
            } catch (CmsSchedulerException e) {
                // TODO: exception handling
            } catch (CmsRoleViolationException e) {
                // TODO: exception handling
            }
        } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
            // deactivate a job from the list
            String jobId = getSelectedItem().getId();
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)OpenCms.getScheduleManager().getJob(jobId).clone();
            job.setActive(false);
            try {
                OpenCms.getScheduleManager().scheduleJob(getCms(), job);
                refreshList();
            } catch (CmsSchedulerException e) {
                // TODO: exception handling
            } catch (CmsRoleViolationException e) {
                // TODO: exception handling
            }
        } else if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // delete a job from the list
            String jobId = getSelectedItem().getId();        
            try {
                OpenCms.getScheduleManager().unscheduleJob(getCms(), jobId);
                getList().removeItem(jobId);
            } catch (CmsRoleViolationException e) {
                // TODO: exception handling
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List items = new ArrayList();
        
        Iterator i = OpenCms.getScheduleManager().getJobs().iterator();
        while (i.hasNext()) {
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)i.next();
            CmsListItem item = getList().newItem(job.getId().toString());
            item.set(LIST_COLUMN_NAME, job.getJobName());
            item.set(LIST_COLUMN_CLASS, job.getClassName());
            item.set(LIST_COLUMN_LASTEXE, job.getExecutionTimePrevious());
            item.set(LIST_COLUMN_NEXTEXE, job.getExecutionTimeNext());
            // details: context info
            item.set(LIST_DETAIL_CONTEXTINFO, job.getContextInfo());
            // details: parameter
            StringBuffer params = new StringBuffer(32);
            Iterator paramIt = job.getParameters().keySet().iterator();
            while (paramIt.hasNext()) {
                String param = (String)paramIt.next();
                String value = (String)job.getParameters().get(param);
                params.append(param).append(": ");
                params.append(value).append("<br>");
            }
            item.set(LIST_DETAIL_PARAMETER, params);
            
            items.add(item);
        }
        
        return items;
    }

    
    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // add column for activate/deactivate action
        CmsListColumnDefinition activateCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        activateCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_ACTIVE_0));
        activateCol.setWidth(null);
        activateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        activateCol.setSorteable(false);
        // create direct action to activate/deactivate job
        CmsActionActivateJob activateJob = new CmsActionActivateJob(
            LIST_ID,
            LIST_ACTION_ACTIVATE,
            getCms());
        CmsListDirectAction userActAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_ACTIVATE);
        userActAction.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_NAME_0));
        userActAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_CONF_0));
        userActAction.setIconPath("buttons/apply_in.gif");
        userActAction.setEnabled(true);
        userActAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_HELP_0));
        activateJob.setFirstAction(userActAction);
        CmsListDirectAction userDeactAction = new CmsListDirectAction(LIST_ID, LIST_ACTION_DEACTIVATE);
        userDeactAction.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DEACTIVATE_NAME_0));
        userDeactAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_JOBS_LIST_ACTION_DEACTIVATE_CONF_0));
        userDeactAction.setIconPath("buttons/apply.gif");
        userDeactAction.setEnabled(true);
        userDeactAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DEACTIVATE_HELP_0));
        activateJob.setSecondAction(userDeactAction);
        activateCol.addDirectAction(activateJob);
        metadata.addColumn(activateCol);
        
        // add column for copy action
        CmsListColumnDefinition copyCol = new CmsListColumnDefinition(LIST_COLUMN_COPY);
        copyCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_COPY_0));
        copyCol.setWidth(null);
        copyCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        copyCol.setSorteable(false);
        // create direct action
        CmsListDirectAction copyJob = new CmsListDirectAction(LIST_ID, LIST_COLUMN_COPY);
        copyJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_COPY_NAME_0));
        copyJob.setConfirmationMessage(Messages.get().container(
            Messages.GUI_JOBS_LIST_ACTION_COPY_CONF_0));
        copyJob.setIconPath("buttons/apply.gif");
        copyJob.setEnabled(true);
        copyJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_COPY_HELP_0));
        copyCol.addDirectAction(copyJob);
        metadata.addColumn(copyCol);
        
        // add column for delete action
        CmsListColumnDefinition delCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        delCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_DELETE_0));
        delCol.setWidth(null);
        delCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        delCol.setSorteable(false);
        // create direct action
        CmsListDirectAction delJob = new CmsListDirectAction(LIST_ID, LIST_ACTION_DELETE);
        delJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DELETE_NAME_0));
        delJob.setConfirmationMessage(Messages.get().container(
            Messages.GUI_JOBS_LIST_ACTION_DELETE_CONF_0));
        delJob.setIconPath("buttons/apply.gif");
        delJob.setEnabled(true);
        delJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DELETE_HELP_0));
        delCol.addDirectAction(delJob);
        metadata.addColumn(delCol);
        
        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_NAME_0));
        nameCol.setWidth("20%");
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        // create default edit action for name column
        CmsListDefaultAction nameColAction = new CmsListDefaultAction (LIST_ID, LIST_ACTION_EDIT);
        nameColAction.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0));
        nameColAction.setIconPath(null);
        nameColAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_HELP_0));
        nameColAction.setEnabled(true);
        nameColAction.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_CONF_0));
        // set action for the name column
        nameCol.setDefaultAction(nameColAction);
        metadata.addColumn(nameCol);
        
        // add column for class
        CmsListColumnDefinition classCol = new CmsListColumnDefinition(LIST_COLUMN_CLASS);
        classCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_CLASS_0));
        classCol.setWidth("15%");
        classCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(classCol);
        
        // add column for last execution time
        CmsListColumnDefinition lastExecCol = new CmsListColumnDefinition(LIST_COLUMN_LASTEXE);
        lastExecCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_LASTEXE_0));
        lastExecCol.setWidth("22.5%");
        lastExecCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        // create date formatter for last execution time
        CmsListDateMacroFormatter listDateFormatter =  new CmsListDateMacroFormatter(Messages.get().container(
            Messages.GUI_JOBS_LIST_COL_LASTEXE_FORMAT_1), Messages.get().container(
            Messages.GUI_JOBS_LIST_COL_LASTEXE_NEVER_0));
        lastExecCol.setFormatter(listDateFormatter);
        metadata.addColumn(lastExecCol);
        
        // add column for next execution time
        CmsListColumnDefinition nextExecCol = new CmsListColumnDefinition(LIST_COLUMN_NEXTEXE);
        nextExecCol.setName(Messages.get().container(Messages.GUI_JOBS_LIST_COL_NEXTEXE_0));
        nextExecCol.setWidth("22.5%");
        nextExecCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        // create date formatter for next execution time
        listDateFormatter = new CmsListDateMacroFormatter(Messages.get().container(
            Messages.GUI_JOBS_LIST_COL_NEXTEXE_FORMAT_1), Messages.get().container(
                Messages.GUI_JOBS_LIST_COL_NEXTEXE_NEVER_0));
        nextExecCol.setFormatter(listDateFormatter);
        metadata.addColumn(nextExecCol);

    }
    
    
    
    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add default reload button
        metadata.addIndependentAction(CmsListIndependentAction.getDefaultRefreshListAction(LIST_ID));
        
        // add independent job context info button
        
        // create show context info action
        CmsListIndependentAction showContextInfoAction = new CmsListIndependentAction(LIST_ID, LIST_DETAIL_CONTEXTINFO);
        showContextInfoAction.setName(Messages.get().container(Messages.GUI_JOBS_DETAIL_SHOW_CONTEXTINFO_NAME_0));
        showContextInfoAction.setIconPath("buttons/properties.gif");
        showContextInfoAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_DETAIL_SHOW_CONTEXTINFO_HELP_0));
        showContextInfoAction.setEnabled(true);
        showContextInfoAction.setConfirmationMessage(null);
        // create hide context info action
        CmsListIndependentAction hideContextInfoAction = new CmsListIndependentAction(LIST_ID, LIST_DETAIL_CONTEXTINFO);
        hideContextInfoAction.setName(Messages.get().container(Messages.GUI_JOBS_DETAIL_HIDE_CONTEXTINFO_NAME_0));
        hideContextInfoAction.setIconPath("buttons/properties.gif");
        hideContextInfoAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_DETAIL_HIDE_CONTEXTINFO_HELP_0));
        hideContextInfoAction.setEnabled(true);
        hideContextInfoAction.setConfirmationMessage(null);
        // create list item
        CmsListItemDetails jobsContextInfoDetails = new CmsListItemDetails(LIST_DETAIL_CONTEXTINFO);
        jobsContextInfoDetails.setAtColumn(LIST_COLUMN_NAME);
        jobsContextInfoDetails.setVisible(false);
        jobsContextInfoDetails.setShowAction(showContextInfoAction);
        jobsContextInfoDetails.setHideAction(hideContextInfoAction);
        CmsContextInfoDetailsFormatter contextFormatter = new CmsContextInfoDetailsFormatter();
        contextFormatter.setUserMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_USER_0));
        contextFormatter.setProjectMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_PROJECT_0));
        contextFormatter.setLocaleMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_LOCALE_0));
        contextFormatter.setRootSiteMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_ROOTSITE_0));
        contextFormatter.setEncodingMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_ENCODING_0));
        contextFormatter.setRemoteAddrMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_REMADR_0));
        contextFormatter.setRequestedURIMessage(Messages.get().container(Messages.GUI_JOBS_DETAIL_CONTEXTINFO_REQURI_0));
        jobsContextInfoDetails.setFormatter(contextFormatter);
        // add item to meta data
        metadata.addItemDetails(jobsContextInfoDetails);
        
        // add independent job parameter button
        
        // create show parameter button
        CmsListIndependentAction showParameterAction = new CmsListIndependentAction(LIST_ID, LIST_DETAIL_PARAMETER);
        showParameterAction.setName(Messages.get().container(Messages.GUI_JOBS_DETAIL_SHOW_PARAMETER_NAME_0));
        showParameterAction.setIconPath("buttons/details.gif");
        showParameterAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_DETAIL_SHOW_PARAMETER_HELP_0));
        showParameterAction.setEnabled(true);
        showParameterAction.setConfirmationMessage(null);
        // create hide parameter button
        CmsListIndependentAction hideParameterAction = new CmsListIndependentAction(LIST_ID, LIST_DETAIL_PARAMETER);
        hideParameterAction.setName(Messages.get().container(Messages.GUI_JOBS_DETAIL_HIDE_PARAMETER_NAME_0));
        hideParameterAction.setIconPath("buttons/details.gif");
        hideParameterAction.setHelpText(Messages.get().container(Messages.GUI_JOBS_DETAIL_HIDE_PARAMETER_HELP_0));
        hideParameterAction.setEnabled(true);
        hideParameterAction.setConfirmationMessage(null);
        // create list item
        CmsListItemDetails jobsParameterDetails = new CmsListItemDetails(LIST_DETAIL_PARAMETER);
        jobsParameterDetails.setAtColumn(LIST_COLUMN_NAME);
        jobsParameterDetails.setVisible(false);
        jobsParameterDetails.setShowAction(showParameterAction);
        jobsParameterDetails.setHideAction(hideParameterAction);         
        jobsParameterDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_JOBS_DETAIL_PARAMETER_FORMAT_0)));
        metadata.addItemDetails(jobsParameterDetails);

    }
    
    
    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {
        
        // add the activate job multi action
        CmsListMultiAction activateJob = new CmsListMultiAction(LIST_ID, LIST_ACTION_MACTIVATE);
        activateJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MACTIVATE_NAME_0));
        activateJob.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MACTIVATE_CONF_0));
        activateJob.setIconPath("buttons/apply.gif");
        activateJob.setEnabled(true);
        activateJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MACTIVATE_HELP_0));
        metadata.addMultiAction(activateJob);
        
        // add the deactivate job multi action
        CmsListMultiAction deactivateJob = new CmsListMultiAction(LIST_ID, LIST_ACTION_MDEACTIVATE);
        deactivateJob.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEACTIVATE_NAME_0));
        deactivateJob.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEACTIVATE_CONF_0));
        deactivateJob.setIconPath("buttons/apply_in.gif");
        deactivateJob.setEnabled(true);
        deactivateJob.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEACTIVATE_HELP_0));
        metadata.addMultiAction(deactivateJob);
        
        // add the delete job multi action
        CmsListMultiAction deleteJobs = new CmsListMultiAction(LIST_ID, LIST_ACTION_MDELETE);
        deleteJobs.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDELETE_NAME_0));
        deleteJobs.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDELETE_CONF_0));
        deleteJobs.setIconPath("list/delete.gif");
        deleteJobs.setEnabled(true);
        deleteJobs.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDELETE_HELP_0));
        metadata.addMultiAction(deleteJobs);
    }

}