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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to display the current running publish job.<p>
 *
 * @since 6.5.5
 */
public class CmsPublishQueueLiveReportDialog extends CmsReport {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "publishreport";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsPublishQueueLiveReportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPublishQueueLiveReportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the publish report, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void displayReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_END:
            case ACTION_CANCEL:
                actionCloseDialog();
                break;
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);
                getJsp().include(FILE_REPORT_OUTPUT);
                break;
            case ACTION_REPORT_BEGIN:
            case ACTION_CONFIRMED:
            case ACTION_DEFAULT:
            default:
                CmsPublishJobRunning publishJob = OpenCms.getPublishManager().getCurrentPublishJob();
                if (publishJob != null) {
                    setParamAction(REPORT_UPDATE);
                    // set the new thread id and flag that no thread is following
                    setParamThread(publishJob.getThreadUUID().toString());
                    setParamThreadHasNext(CmsStringUtil.FALSE);
                } else {
                    setParamAction(REPORT_END);
                }
                getJsp().include(FILE_REPORT_OUTPUT);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        // test some preconditions
        try {
            CmsPublishJobRunning publishJob = OpenCms.getPublishManager().getCurrentPublishJob();
            if (publishJob == null) {
                throw new Exception();
            }
            if (!OpenCms.getRoleManager().hasRole(getCms(), CmsRole.ROOT_ADMIN)
                && !publishJob.getUserId().equals(getCms().getRequestContext().getCurrentUser().getId())) {
                throw new Exception();
            }
        } catch (Exception e) {
            // redirect to parent if parameters not available
            setAction(ACTION_CANCEL);
            try {
                actionCloseDialog();
            } catch (JspException e1) {
                // noop
            }
            return;
        }

        // set the action for the JSP switch
        if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);
        } else if (REPORT_BEGIN.equals(getParamAction())) {
            setAction(ACTION_REPORT_BEGIN);
        } else if (REPORT_END.equals(getParamAction())) {
            setAction(ACTION_REPORT_END);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);

            CmsPublishJobRunning publishJob = OpenCms.getPublishManager().getCurrentPublishJob();
            // set parameters depending on publishing type
            if (publishJob.isDirectPublish()) {
                // add the title for the direct publish dialog
                setDialogTitle(
                    org.opencms.workplace.commons.Messages.GUI_PUBLISH_RESOURCE_1,
                    org.opencms.workplace.commons.Messages.GUI_PUBLISH_MULTI_2);
            } else {
                // add the title for the publish project dialog
                setParamTitle(key(org.opencms.workplace.commons.Messages.GUI_PUBLISH_PROJECT_0));
            }
        }
    }
}
