/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/publishqueue/CmsPublishQueueHistoricalReportDialog.java,v $
 * Date   : $Date: 2006/11/29 14:54:02 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.tools.publishqueue;

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.publish.CmsPublishJobBase;
import org.opencms.util.CmsRfsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWidgetDialog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a dialog to view a publish report selected out of the personal publish list.<p> 
 *
 * @author Raphael Schnuck
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.5
 */
public class CmsPublishQueueHistoricalReportDialog extends CmsWidgetDialog {

    /** The pages array for possible multi-page dialogs. This is a dummy. */
    public static String[] PAGES = {"page1"};

    /** Request parameter name for the file name. */
    public static final String PARAM_FILENAME = "filename";

    /** The path to the underlying file. */
    protected String m_filePath = null;

    /** The file encoding. */
    private Charset m_fileEncoding;

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
        m_fileEncoding = Charset.forName(new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding());
        setParamFilename(req.getParameter(PARAM_FILENAME));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        // noop
    }

    /**
     * Returns the file path.<p>
     *  
     * @return the file path
     */
    public String getParamFilename() {

        return m_filePath;
    }

    /**
     * Return the file content from the underlying file or an empty String.<p>
     *
     * @return file content from the underlying file or an empty String
     * 
     * @throws CmsRfsException if something goes wrong
     */
    public String readFileContent() throws CmsRfsException {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(m_filePath), m_fileEncoding));

            StringBuffer result = new StringBuffer();

            String read = reader.readLine();
            while (read != null) {
                result.append(read).append("\n");
                read = reader.readLine();
            }
            return result.toString();
        } catch (IOException ioex) {
            CmsRfsException ex = new CmsRfsException(Messages.get().container(
                Messages.ERR_FILE_ARG_ACCESS_1,
                m_filePath), ioex);
            throw ex;
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
     * Set the file name value.<p> 
     * 
     * @param value the file name value
     */
    public void setParamFilename(String value) {

        m_filePath = value;
    }

    /**
     * Returns the dialog HTML for all defined widgets of the named dialog (page).<p>
     * 
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        String fileContentHeader;
        if (m_filePath == null) {
            fileContentHeader = key(Messages.GUI_PERSONALQUEUE_LIST_NAME_0);
        } else {
            Object[] params = new Object[3];
            String fileName = CmsResource.getName(m_filePath.replace('\\', '/'));
            // remove prefix and postfix
            fileName = fileName.substring(CmsPublishJobBase.REPORT_FILENAME_PREFIX.length());
            fileName = fileName.substring(0, fileName.length() - CmsPublishJobBase.REPORT_FILENAME_POSTFIX.length());
            // project name
            int sep = fileName.indexOf(CmsPublishJobBase.REPORT_FILENAME_SEPARATOR);
            params[0] = fileName.substring(0, sep);
            // user name
            int sep2 = fileName.indexOf(CmsPublishJobBase.REPORT_FILENAME_SEPARATOR, sep + 1);
            params[1] = fileName.substring(sep + 1, sep2);
            // start date
            long startTime = Long.parseLong(fileName.substring(sep2 + 1));
            params[2] = Messages.get().getBundle(getLocale()).getDateTime(startTime);
            // compose the title
            fileContentHeader = key(Messages.GUI_PUBLISH_REPORT_VIEW_TITLE_3, params);
        }

        result.append(createWidgetBlockStart(fileContentHeader));
        try {
            result.append("<iframe style=\"overflow: auto;\" src=\"");
            result.append(getJsp().link(
                "/system/workplace/admin/publishqueue/publishreportshow.jsp?"
                    + CmsEncoder.encode(PARAM_FILENAME)
                    + "="
                    + CmsEncoder.encode(m_filePath)));
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
    protected void defineWidgets() {

        //noop
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_filePath)) {
            throw new Exception();
        }
    }
}