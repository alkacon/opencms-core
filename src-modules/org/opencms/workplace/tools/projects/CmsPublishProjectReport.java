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

package org.opencms.workplace.tools.projects;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsHtmlReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.Messages;
import org.opencms.workplace.threads.CmsRelationsValidatorThread;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report for publishing a project.<p>
 *
 * @since 6.0.0
 */
public class CmsPublishProjectReport extends CmsReport {

    /** Request parameter name for the project id. */
    public static final String PARAM_PROJECTID = "projectid";

    /** list of project id. */
    private String m_paramProjectid;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsPublishProjectReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPublishProjectReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the dialog actions depending on the initialized action.<p>
     *
     * @throws JspException if dialog actions fail
     */
    public void displayReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_END:
                actionCloseDialog();
                break;
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
                try {
                    if (getCms().readProject(
                        new CmsUUID(getParamProjectid())).getType() == CmsProject.PROJECT_TYPE_TEMPORARY) {
                        // set the flag that this is a temporary project
                        setParamRefreshWorkplace(CmsStringUtil.TRUE);
                    }
                } catch (Exception e) {
                    // ignore
                }

                if (getParamProjectid() == null) {
                    return;
                }

                CmsPublishList list = null;
                try {
                    CmsProject currentProject = getCms().getRequestContext().getCurrentProject();
                    getCms().getRequestContext().setCurrentProject(
                        getCms().readProject(new CmsUUID(getParamProjectid())));
                    list = OpenCms.getPublishManager().getPublishList(getCms());
                    getCms().getRequestContext().setCurrentProject(currentProject);
                } catch (CmsException e) {
                    throw new CmsRuntimeException(e.getMessageContainer(), e);
                }

                // start validation check
                startValidationThread(list);
                getJsp().include(FILE_REPORT_OUTPUT);
        }
    }

    /**
     * Gets the project id parameter.<p>
     *
     * @return the project id parameter
     */
    public String getParamProjectid() {

        return m_paramProjectid;
    }

    /**
     * Sets the project id parameter.<p>
     *
     * @param projectId the project id parameter
     */
    public void setParamProjectid(String projectId) {

        m_paramProjectid = projectId;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);
        } else if (REPORT_BEGIN.equals(getParamAction())) {
            setAction(ACTION_REPORT_BEGIN);
        } else if (REPORT_END.equals(getParamAction())) {
            if (Boolean.valueOf(getParamThreadHasNext()).booleanValue()) {
                // after the link check start the publish thread
                startPublishThread();
            } else {
                // ends the publish thread
                setAction(ACTION_REPORT_END);
            }
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            // set the default action
            setAction(ACTION_DEFAULT);
        }
    }

    /**
     * Starts the publish thread for the project.<p>
     */
    private void startPublishThread() {

        // create a publish thread from the current publish list
        CmsPublishList publishList = getSettings().getPublishList();
        try {
            OpenCms.getPublishManager().publishProject(
                getCms(),
                new CmsHtmlReport(getLocale(), getCms().getRequestContext().getSiteRoot()),
                publishList);
        } catch (CmsException e) {
            throw new CmsRuntimeException(e.getMessageContainer());
        }
        setParamAction(REPORT_END);
        setAction(ACTION_REPORT_END);
        setParamThreadHasNext(CmsStringUtil.FALSE);
    }

    /**
     * Starts the link validation thread for the project.<p>
     *
     * @param publishList the list of resources to publish
     *
     * @throws JspException if something goes wrong
     */
    private void startValidationThread(CmsPublishList publishList) throws JspException {

        try {
            CmsRelationsValidatorThread thread = new CmsRelationsValidatorThread(getCms(), publishList, getSettings());
            thread.start();

            setParamThread(thread.getUUID().toString());
            setParamThreadHasNext(CmsStringUtil.TRUE);

            setParamAction(REPORT_BEGIN);

            // set the key name for the continue checkbox
            setParamReportContinueKey(Messages.GUI_PUBLISH_CONTINUE_BROKEN_LINKS_0);
        } catch (Throwable e) {
            // error while link validation, show error screen
            includeErrorpage(this, e);
        }
    }
}
