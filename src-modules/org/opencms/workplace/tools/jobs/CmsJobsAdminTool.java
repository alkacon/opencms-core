/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/jobs/Attic/CmsJobsAdminTool.java,v $
 * Date   : $Date: 2005/05/10 12:14:41 $
 * Version: $Revision: 1.7 $
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
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDialog;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsSearchAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

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
 * Main scheduler jobs management view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.7 $
 * @since 5.7.3
 */
public class CmsJobsAdminTool extends CmsListDialog {

    /**
     * list action to activate a job, that can be used as direct action
     * as also multi action.<p>
     * 
     * @author Michael Moossen (m.moossen@alkacon.com) 
     * @version $Revision: 1.7 $
     * @since 5.7.3
     */
    private class ActivateJobAction extends CmsListDirectAction {

        /**
         * Default Constructor.<p>
         * 
         * @param listId The id of the associated list
         */
        public ActivateJobAction(String listId) {

            super(
                listId,
                LIST_ACTION_ACTIVATE,
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_NAME_0),
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_HELP_0),
                "buttons/user_sm.gif",
                true,
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_CONF_0));
        }

        /**
         * @see org.opencms.workplace.list.I_CmsListAction#getConfirmationMessage()
         */
        public CmsMessageContainer getConfirmationMessage() {

            if (getItem() != null) {
                String jobId = getItem().getId();
                CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(jobId);

                if (!job.isActive()) {
                    return Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_ACTCONF_0);
                }
                return Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_DESCONF_0);
            }
            return super.getConfirmationMessage();
        }

        /**
         * @see org.opencms.workplace.list.I_CmsHtmlIconButton#getHelpText()
         */
        public CmsMessageContainer getHelpText() {

            if (getItem() != null) {
                String jobId = getItem().getId();
                CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(jobId);

                if (!job.isActive()) {
                    return Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_ACTHELP_0);
                }
                return Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_ACTIVATE_DESHELP_0);
            }
            return super.getHelpText();
        }

        /**
         * @see org.opencms.workplace.list.I_CmsHtmlIconButton#getIconPath()
         */
        public String getIconPath() {

            if (getItem() != null) {
                String jobId = getItem().getId();
                CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(jobId);

                if (!job.isActive()) {
                    return "buttons/apply_in.gif";
                }
                return "buttons/apply.gif";
            }
            return super.getIconPath();
        }

        /**
         * @see org.opencms.workplace.list.I_CmsHtmlIconButton#isEnabled()
         */
        public boolean isEnabled() {

            if (getItem() != null) {
                try {
                    String jobName = getItem().get(LIST_COLUMN_NAME).toString();
                    CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(jobName);
                    return job.getExecutionTimeNext() != null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return super.isEnabled();
        }
    }

    /** list action id constant. */
    public static final String LIST_ACTION_ACTIVATE = "activate";

    /** list action id constant. */
    public static final String LIST_ACTION_COPY = "copy";

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "delete";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "edit";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIONS = "actions";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTEXE = "lastexe";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "name";

    /** list column id constant. */
    public static final String LIST_COLUMN_NEXTEXE = "nextexe";

    /** list column id constant. */
    public static final String LIST_COLUMN_TYPE = "type";

    /** list id constant. */
    public static final String LIST_ID = "jobs";

    /** metadata for the list used in this dialog. */
    private static CmsListMetadata metadata;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsJobsAdminTool(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, LIST_COLUMN_NAME);
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

        if (getParamListAction().equals(LIST_ACTION_DELETE)) {
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
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    String jobId = listItem.getId();
                    CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(jobId);
                    if (job.isActive()) {
                        job.setActive(false);
                    } else {
                        job.setActive(true);
                    }
                    OpenCms.getScheduleManager().scheduleJob(getCms(), job);
                }
            } catch (CmsException e) {
                throw new RuntimeException(e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
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
        } else if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
            // execute the activate action
            CmsListItem listItem = getSelectedItem();
            try {
                String jobId = listItem.getId();
                CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(jobId);
                if (job.isActive()) {
                    job.setActive(false);
                } else {
                    job.setActive(true);
                }
                OpenCms.getScheduleManager().scheduleJob(getCms(), job);
            } catch (CmsException e) {
                throw new RuntimeException(e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.CmsListDialog#createList()
     */
    protected CmsHtmlList createList() {

        if (metadata == null) {
            metadata = new CmsListMetadata();

            metadata.addIndependentAction(CmsListIndependentAction.getDefaultRefreshListAction(LIST_ID));

            // add column for direct actions
            CmsListColumnDefinition actionsCol = new CmsListColumnDefinition(
                LIST_COLUMN_ACTIONS,
                Messages.get().container(Messages.GUI_JOBS_LIST_COLS_ACTIONS_0),
                "", // no width
                CmsListColumnAlignEnum.ALIGN_CENTER);
            actionsCol.setSorteable(false);

            I_CmsListDirectAction activateJob = new ActivateJobAction(LIST_ID);

            CmsListDirectAction deleteAction = new CmsListDirectAction(
                LIST_ID,
                LIST_ACTION_DELETE,
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DELETE_NAME_0),
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DELETE_HELP_0),
                "list/delete.gif",
                true, // enabled
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_DELETE_CONF_0));

            CmsListDirectAction copyAction = new CmsListDirectAction(
                LIST_ID,
                LIST_ACTION_COPY,
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_COPY_NAME_0),
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_COPY_HELP_0),
                "list/copy.gif",
                true, // enabled
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_COPY_CONF_0));

            actionsCol.addDirectAction(activateJob);
            actionsCol.addDirectAction(copyAction);
            actionsCol.addDirectAction(deleteAction);
            metadata.addColumn(actionsCol);

            // add column for name and default action
            CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME, Messages.get().container(
                Messages.GUI_JOBS_LIST_COLS_NAME_0), "", // no width
                CmsListColumnAlignEnum.ALIGN_LEFT);
            nameCol.setDefaultAction(new CmsListDefaultAction(LIST_ID, LIST_ACTION_EDIT, Messages.get().container(
                Messages.GUI_JOBS_LIST_ACTION_EDIT_NAME_0), Messages.get().container(
                Messages.GUI_JOBS_LIST_ACTION_EDIT_HELP_0), null, // no icon
                true, // enabled
                Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_EDIT_CONF_0)));
            metadata.addColumn(nameCol);

            // add column for type
            CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE, Messages.get().container(
                Messages.GUI_JOBS_LIST_COLS_TYPE_0), "", // no width
                CmsListColumnAlignEnum.ALIGN_LEFT);
            metadata.addColumn(typeCol);

            // add column for last execution time
            CmsListColumnDefinition lastExeCol = new CmsListColumnDefinition(
                LIST_COLUMN_LASTEXE,
                Messages.get().container(Messages.GUI_JOBS_LIST_COLS_LASTEXE_0),
                "", // no width
                CmsListColumnAlignEnum.ALIGN_LEFT);
            lastExeCol.setFormatter(new CmsListDateMacroFormatter(Messages.get().container(
                Messages.GUI_JOBS_LIST_COLS_LASTEXE_FORMAT_0), Messages.get().container(
                Messages.GUI_JOBS_LIST_COLS_LASTEXE_NEVER_0)));
            metadata.addColumn(lastExeCol);

            // add column for next execution time
            CmsListColumnDefinition nextExeCol = new CmsListColumnDefinition(
                LIST_COLUMN_NEXTEXE,
                Messages.get().container(Messages.GUI_JOBS_LIST_COLS_NEXTEXE_0),
                "", // no width
                CmsListColumnAlignEnum.ALIGN_LEFT);
            nextExeCol.setFormatter(new CmsListDateMacroFormatter(Messages.get().container(
                Messages.GUI_JOBS_LIST_COLS_NEXTEXE_FORMAT_0), Messages.get().container(
                Messages.GUI_JOBS_LIST_COLS_NEXTEXE_NEVER_0)));
            metadata.addColumn(nextExeCol);

            // add multi actions
            // metadata.addDirectMultiAction(deleteAction);
            // reuse the activate job action as a multi action
            // metadata.addDirectMultiAction(activateJob);

            // made the list searchable by name
            CmsSearchAction searchAction = new CmsSearchAction(LIST_ID, nameCol);
            searchAction.useDefaultShowAllAction();
            metadata.setSearchAction(searchAction);

        }
        return new CmsHtmlList(
            LIST_ID,
            new CmsMessageContainer(Messages.get(), Messages.GUI_JOBS_LIST_NAME_0),
            metadata);
    }

    /**
     * @see org.opencms.workplace.list.CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List ret = new ArrayList();
        // fill list
        List jobs = OpenCms.getScheduleManager().getJobs();
        Iterator itJobs = jobs.iterator();
        while (itJobs.hasNext()) {
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)itJobs.next();
            CmsListItem item = getList().newItem(job.getId());
            item.set(LIST_COLUMN_NAME, job.getJobName());
            item.set(LIST_COLUMN_TYPE, job.getClassName());
            item.set(LIST_COLUMN_LASTEXE, job.getExecutionTimePrevious());
            item.set(LIST_COLUMN_NEXTEXE, job.getExecutionTimeNext());
            ret.add(ret);
        }

        return ret;
    }

}