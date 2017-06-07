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

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobFinished;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWidgetDialog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a dialog to view a publish report selected out of the personal publish list.<p>
 *
 * @since 6.5.5
 */
public class CmsPublishQueueHistoricalReportDialog extends CmsWidgetDialog {

    /** The pages array for possible multi-page dialogs. This is a dummy. */
    private static final String[] PAGES = {"page1"};

    /** Request parameter name for the publish job id. */
    public static final String PARAM_ID = "id";

    /** The path to the underlying file. */
    protected String m_jobId;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp the CmsJspActionElement
     */
    public CmsPublishQueueHistoricalReportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPublishQueueHistoricalReportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
        setParamId(CmsEncoder.decodeParameter(req.getParameter(PARAM_ID)));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        // noop
    }

    /**
     * Returns the file path.<p>
     *
     * @return the file path
     */
    public String getParamFilename() {

        return m_jobId;
    }

    /**
     * Return the file content from the underlying file or an empty String.<p>
     *
     * @return file content from the underlying file or an empty String
     *
     * @throws CmsException if something goes wrong
     */
    public String readFileContent() throws CmsException {

        BufferedReader reader = null;
        try {
            CmsPublishJobFinished publishJob = (CmsPublishJobFinished)OpenCms.getPublishManager().getJobByPublishHistoryId(
                new CmsUUID(m_jobId));
            byte[] contents = OpenCms.getPublishManager().getReportContents(publishJob);
            StringBuffer result = new StringBuffer();

            if (contents != null) {
                reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents)));
                String read = reader.readLine();
                while (read != null) {
                    result.append(read).append("\n");
                    read = reader.readLine();
                }
            }
            return result.toString();
        } catch (IOException ioex) {
            throw new CmsException(Messages.get().container(Messages.ERR_FILE_ARG_ACCESS_1, m_jobId), ioex);
        } catch (CmsException ex) {
            throw new CmsException(Messages.get().container(Messages.ERR_FILE_ARG_ACCESS_1, m_jobId), ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }

            }
        }
    }

    /**
     * Set the job id value.<p>
     *
     * @param value the job id value
     */
    public void setParamId(String value) {

        m_jobId = value;
    }

    /**
     * Returns the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        String fileContentHeader;
        if (m_jobId == null) {
            fileContentHeader = key(Messages.GUI_PERSONALQUEUE_LIST_NAME_0);
        } else {
            Object[] params = new Object[3];
            CmsPublishJobFinished publishJob = (CmsPublishJobFinished)OpenCms.getPublishManager().getJobByPublishHistoryId(
                new CmsUUID(m_jobId));
            // project name
            params[0] = publishJob.getProjectName();
            // user name
            params[1] = publishJob.getUserName(getCms());
            // start date
            params[2] = Messages.get().getBundle(getLocale()).getDateTime(publishJob.getStartTime());
            // compose the title
            fileContentHeader = key(Messages.GUI_PUBLISH_REPORT_VIEW_TITLE_3, params);
        }

        result.append(createWidgetBlockStart(fileContentHeader));
        try {
            result.append("<iframe style=\"overflow: auto;\" src=\"");
            result.append(
                getJsp().link(
                    "/system/workplace/admin/publishqueue/publishreportshow.jsp?"
                        + CmsEncoder.encodeParameter(PARAM_ID)
                        + "="
                        + CmsEncoder.encodeParameter(m_jobId)));
            result.append("\" width=\"100%\" height=\"500\" border=\"0\" frameborder=\"0\"></iframe>");
        } catch (Exception e) {
            //noop
        }
        result.append(createWidgetBlockEnd());

        // close widget table
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        //noop
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_jobId)) {
            throw new Exception();
        }
    }
}