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

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobBase;
import org.opencms.publish.CmsPublishJobEnqueued;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a list for the global publish queue.<p>
 *
 * @since 6.5.5
 */
public class CmsPublishQueueList extends A_CmsListDialog {

    /** list id constant. */
    public static final String LIST_ID = "lpq";

    /** The path to the publish report view icon. */
    public static final String PUBLISHQUEUE_CANCEL_BUTTON = "tools/publishqueue/buttons/cancel.png";

    /** The path to the publish report view icon. */
    public static final String PUBLISHQUEUE_STATE_PROCEED_BUTTON = "tools/publishqueue/buttons/publish_current.png";

    /** Constant for the state of a list entry. */
    public static final int STATE_OTHER = 1;

    /** Constant for the state of a list entry. */
    public static final int STATE_OWN = 0;

    /** Constant for the state of a list entry. */
    public static final int STATE_PROCEED = 2;

    /** list action id constant. */
    private static final String LIST_ACTION_CANCEL = "ac";

    /** list action id constant. */
    private static final String LIST_ACTION_NUMBER = "an";

    /** list action id constant. */
    private static final String LIST_ACTION_PROJECT = "ap";

    /** list action id constant. */
    private static final String LIST_ACTION_RESCOUNT = "ar";

    /** list action id constant. */
    private static final String LIST_ACTION_STARTTIME = "at";

    /** list action id constant. */
    private static final String LIST_ACTION_STATE = "as";

    /** list action id constant. */
    private static final String LIST_ACTION_USER = "au";

    /** list column id constant. */
    private static final String LIST_COLUMN_NUMBER = "cn";

    /** list column id constant. */
    private static final String LIST_COLUMN_PROJECT = "cp";

    /** list column id constant. */
    private static final String LIST_COLUMN_RESCOUNT = "cr";

    /** list column id constant. */
    private static final String LIST_COLUMN_STARTTIME = "ct";

    /** list column id constant. */
    private static final String LIST_COLUMN_STATE = "cs";

    /** list column id constant. */
    private static final String LIST_COLUMN_USER = "cu";

    /** list detail id constant. */
    private static final String LIST_DETAIL_RESOURCES = "dr";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsPublishQueueList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPublishQueueList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsPublishQueueList(CmsJspActionElement jsp, String listId) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_PUBLISHQUEUE_LIST_NAME_0),
            LIST_COLUMN_NUMBER,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
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

        if (getParamListAction().equals(LIST_ACTION_NUMBER)
            || getParamListAction().equals(LIST_ACTION_PROJECT)
            || getParamListAction().equals(LIST_ACTION_RESCOUNT)
            || getParamListAction().equals(LIST_ACTION_STATE)
            || getParamListAction().equals(LIST_ACTION_USER)
            || getParamListAction().equals(LIST_ACTION_STARTTIME)) {
            try {
                getToolManager().jspForwardTool(this, "/publishqueue/live", null);
            } catch (Exception e) {
                // Should never happen
            }
        } else if (getParamListAction().equals(LIST_ACTION_CANCEL)) {
            // search for the publish job to cancel
            String userName = (String)getSelectedItem().get(LIST_COLUMN_USER);
            long enqueueTime = ((Date)getSelectedItem().get(LIST_COLUMN_STARTTIME)).getTime();
            Iterator itJobs = OpenCms.getPublishManager().getPublishQueue().iterator();
            while (itJobs.hasNext()) {
                CmsPublishJobEnqueued publishJob = (CmsPublishJobEnqueued)itJobs.next();
                if (userName.equals(publishJob.getUserName(getCms())) && (enqueueTime == publishJob.getEnqueueTime())) {
                    try {
                        OpenCms.getPublishManager().abortPublishJob(getCms(), publishJob, true);
                    } catch (CmsException e) {
                        throw new CmsRuntimeException(e.getMessageContainer(), e);
                    }
                    break;
                }
            }
        } else {
            throwListUnsupportedActionException();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlEnd()
     */
    @Override
    protected String defaultActionHtmlEnd() {

        return "&nbsp;<br>";
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List publishJobs = getList().getAllContent();
        Iterator itPublishJobs = publishJobs.iterator();
        while (itPublishJobs.hasNext()) {
            CmsListItem item = (CmsListItem)itPublishJobs.next();
            int state = ((Integer)item.get(LIST_COLUMN_STATE)).intValue();
            boolean enabled = ((state == STATE_PROCEED)
                && (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.ROOT_ADMIN)
                    || getCms().getRequestContext().getCurrentUser().getName().equals(item.get(LIST_COLUMN_USER))));
            enabled = enabled
                || (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.ROOT_ADMIN) || (state == STATE_OWN));
            if (!enabled) {
                continue;
            }
            CmsUUID publishHistoryId = new CmsUUID(item.getId());
            CmsPublishJobBase publishJob = OpenCms.getPublishManager().getJobByPublishHistoryId(publishHistoryId);
            StringBuffer html = new StringBuffer(32);
            if ((publishJob != null) && detailId.equals(LIST_DETAIL_RESOURCES)) {
                // resources
                CmsPublishList publishList;
                if (publishJob instanceof CmsPublishJobEnqueued) {
                    publishList = ((CmsPublishJobEnqueued)publishJob).getPublishList();
                } else if (publishJob instanceof CmsPublishJobRunning) {
                    publishList = ((CmsPublishJobRunning)publishJob).getPublishList();
                } else {
                    continue;
                }
                List resources = new ArrayList(publishList.size());
                resources.addAll(publishList.getFolderList());
                resources.addAll(publishList.getFileList());
                resources.addAll(publishList.getDeletedFolderList());
                Iterator itResources = resources.iterator();
                while (itResources.hasNext()) {
                    CmsResource resource = (CmsResource)itResources.next();
                    html.append(getCms().getSitePath(resource));
                    html.append("<br>");
                }
            } else {
                continue;
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List getListItems() {

        List ret = new ArrayList();

        // number of jobs in list
        int number = 1;

        // get the current job to display it at the top of the publish queue
        if (OpenCms.getPublishManager().isRunning()) {
            CmsPublishJobRunning currentJob = OpenCms.getPublishManager().getCurrentPublishJob();
            if (currentJob != null) {
                CmsListItem item = getList().newItem(currentJob.getPublishList().getPublishHistoryId().toString());
                item.set(LIST_COLUMN_STATE, new Integer(STATE_PROCEED));
                item.set(LIST_COLUMN_NUMBER, new Integer(number));
                item.set(LIST_COLUMN_PROJECT, currentJob.getProjectName());
                item.set(LIST_COLUMN_STARTTIME, new Date(currentJob.getEnqueueTime()));
                item.set(LIST_COLUMN_USER, currentJob.getUserName(getCms()));
                item.set(LIST_COLUMN_RESCOUNT, new Integer(currentJob.getSize()));
                ret.add(item);
                number++;
            }
        }

        Iterator iter = OpenCms.getPublishManager().getPublishQueue().iterator();
        while (iter.hasNext()) {
            CmsPublishJobEnqueued publishJob = (CmsPublishJobEnqueued)iter.next();
            CmsListItem item = getList().newItem(publishJob.getPublishList().getPublishHistoryId().toString());
            // check the state
            int state = STATE_OWN;
            if (!publishJob.getUserId().equals(getCms().getRequestContext().getCurrentUser().getId())) {
                state = STATE_OTHER;
            }
            item.set(LIST_COLUMN_STATE, new Integer(state));
            item.set(LIST_COLUMN_NUMBER, new Integer(number));
            item.set(LIST_COLUMN_PROJECT, publishJob.getProjectName());
            item.set(LIST_COLUMN_STARTTIME, new Date(publishJob.getEnqueueTime()));
            item.set(LIST_COLUMN_USER, publishJob.getUserName(getCms()));
            item.set(LIST_COLUMN_RESCOUNT, new Integer(publishJob.getSize()));
            ret.add(item);
            number++;
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for state display
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_COLS_STATE_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);

        // add view action
        CmsListDirectAction viewDirectAction = new CmsListDirectAction(LIST_ACTION_STATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            @Override
            public boolean isEnabled() {

                return (OpenCms.getRoleManager().hasRole(getWp().getCms(), CmsRole.ROOT_ADMIN)
                    || getWp().getCms().getRequestContext().getCurrentUser().getName().equals(
                        getItem().get(LIST_COLUMN_USER)));
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                int state = ((Integer)getItem().get(LIST_COLUMN_STATE)).intValue();
                return (state == STATE_PROCEED);
            }
        };
        viewDirectAction.setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_ACTION_VIEW_NAME_0));
        viewDirectAction.setHelpText(Messages.get().container(Messages.GUI_PUBLISHQUEUE_ACTION_VIEW_HELP_0));
        viewDirectAction.setIconPath(PUBLISHQUEUE_STATE_PROCEED_BUTTON);
        stateCol.addDirectAction(viewDirectAction);

        // add cancel action
        CmsListDirectAction cancelAction = new CmsListDirectAction(LIST_ACTION_CANCEL) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            @Override
            public boolean isEnabled() {

                int state = ((Integer)getItem().get(LIST_COLUMN_STATE)).intValue();
                return (OpenCms.getRoleManager().hasRole(getWp().getCms(), CmsRole.ROOT_ADMIN) || (state == STATE_OWN));
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                int state = ((Integer)getItem().get(LIST_COLUMN_STATE)).intValue();
                return (state != STATE_PROCEED);
            }
        };
        cancelAction.setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_ACTION_CANCEL_NAME_0));
        cancelAction.setHelpText(Messages.get().container(Messages.GUI_PUBLISHQUEUE_ACTION_CANCEL_HELP_0));
        cancelAction.setConfirmationMessage(Messages.get().container(Messages.GUI_PUBLISHQUEUE_ACTION_CANCEL_CONF_0));
        cancelAction.setIconPath(PUBLISHQUEUE_CANCEL_BUTTON);

        stateCol.addDirectAction(cancelAction);
        stateCol.setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            public String format(Object data, Locale locale) {

                // prevent displaying the state number
                return "";
            }
        });
        metadata.addColumn(stateCol);

        // create column list number
        CmsListColumnDefinition numCol = new CmsListColumnDefinition(LIST_COLUMN_NUMBER);
        numCol.setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_COLS_NUMBER_0));
        numCol.setWidth("5%");
        numCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);

        /**
         *  Internal action class to manage the enabled state.<p>
         */
        final class CmsPublishQueueViewLiveReportAction extends CmsListDefaultAction {

            /**
             * Default constructor.<p>
             *
             * @param id the action id
             */
            CmsPublishQueueViewLiveReportAction(String id) {

                super(id);
                setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_ACTION_VIEW_NAME_0));
                setHelpText(Messages.get().container(Messages.GUI_PUBLISHQUEUE_ACTION_VIEW_HELP_0));
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            @Override
            public CmsMessageContainer getHelpText() {

                if (isEnabled()) {
                    return super.getHelpText();
                } else {
                    return EMPTY_MESSAGE;
                }
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
             */
            @Override
            public boolean isEnabled() {

                int state = ((Integer)getItem().get(LIST_COLUMN_STATE)).intValue();
                return ((state == STATE_PROCEED)
                    && (OpenCms.getRoleManager().hasRole(getWp().getCms(), CmsRole.ROOT_ADMIN)
                        || getWp().getCms().getRequestContext().getCurrentUser().getName().equals(
                            getItem().get(LIST_COLUMN_USER))));
            }
        }

        // add view action
        numCol.addDefaultAction(new CmsPublishQueueViewLiveReportAction(LIST_ACTION_NUMBER));
        metadata.addColumn(numCol);

        // create project column
        CmsListColumnDefinition projectCol = new CmsListColumnDefinition(LIST_COLUMN_PROJECT);
        projectCol.setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_COLS_PROJECT_0));
        projectCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        projectCol.setWidth("30%");
        // add view action
        projectCol.addDefaultAction(new CmsPublishQueueViewLiveReportAction(LIST_ACTION_PROJECT));
        metadata.addColumn(projectCol);

        // create column for in-queue-time
        CmsListColumnDefinition startCol = new CmsListColumnDefinition(LIST_COLUMN_STARTTIME);
        startCol.setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_COLS_INQUEUETIME_0));
        startCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        startCol.setFormatter(
            new CmsListDateMacroFormatter(
                Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_WITH_SECONDS_1),
                Messages.get().container(org.opencms.workplace.list.Messages.GUI_LIST_DATE_FORMAT_NEVER_0)));
        startCol.setWidth("30%");
        // add view action
        startCol.addDefaultAction(new CmsPublishQueueViewLiveReportAction(LIST_ACTION_STARTTIME));
        metadata.addColumn(startCol);

        // create column for user
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_COLS_USER_0));
        userCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        userCol.setWidth("30%");
        // add view action
        userCol.addDefaultAction(new CmsPublishQueueViewLiveReportAction(LIST_ACTION_USER));
        metadata.addColumn(userCol);

        // create column for resource count
        CmsListColumnDefinition countCol = new CmsListColumnDefinition(LIST_COLUMN_RESCOUNT);
        countCol.setName(Messages.get().container(Messages.GUI_PUBLISHQUEUE_COLS_RESCOUNT_0));
        countCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        countCol.setWidth("5%");
        // add view action
        countCol.addDefaultAction(new CmsPublishQueueViewLiveReportAction(LIST_ACTION_RESCOUNT));
        metadata.addColumn(countCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // create resources list item detail
        CmsListItemDetails resourcesDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCES);
        resourcesDetails.setAtColumn(LIST_COLUMN_NUMBER);
        resourcesDetails.setVisible(false);
        resourcesDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_PUBLISHQUEUE_DETAIL_LABEL_RESOURCES_0)));
        resourcesDetails.setShowActionName(
            Messages.get().container(Messages.GUI_PUBLISHQUEUE_DETAIL_SHOW_RESOURCES_NAME_0));
        resourcesDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_PUBLISHQUEUE_DETAIL_SHOW_RESOURCES_HELP_0));
        resourcesDetails.setHideActionName(
            Messages.get().container(Messages.GUI_PUBLISHQUEUE_DETAIL_HIDE_RESOURCES_NAME_0));
        resourcesDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_PUBLISHQUEUE_DETAIL_HIDE_RESOURCES_HELP_0));

        // add author info item detail to meta data
        metadata.addItemDetails(resourcesDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        //noop
    }
}
