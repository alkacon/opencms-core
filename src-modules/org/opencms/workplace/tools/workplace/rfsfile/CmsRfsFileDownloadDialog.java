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

package org.opencms.workplace.tools.workplace.rfsfile;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.CmsToolDialog;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Shows useful information about the current file chosen within the
 * <code>{@link org.opencms.util.CmsRfsFileViewer}</code> and offers
 * a direct download link. <p>
 *
 * @since 6.0.0
 */
public class CmsRfsFileDownloadDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "workplace.download";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The file to download. */
    private File m_downloadFile;

    /** The file date. */
    private String m_filedate;

    /** The file name. */
    private String m_filename;

    /** The file path. */
    private String m_filepath;

    /** The file size. */
    private String m_filesize;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRfsFileDownloadDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRfsFileDownloadDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List<Throwable> errors = new ArrayList<Throwable>();
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(CmsDialog.PARAM_CLOSELINK, new String[] {getParamCloseLink()});
        params.put(CmsToolDialog.PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        getToolManager().jspForwardPage(this, "/system/workplace/admin/workplace/logfileview/dodownload.jsp", params);

        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#dialogButtonsCustom()
     */
    @Override
    public String dialogButtonsCustom() {

        return dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL}, new String[2]);
    }

    /**
     * Returns the file date.<p>
     *
     * @return the file date
     */
    public String getFiledate() {

        return m_filedate;
    }

    /**
     * Returns the file name.<p>
     *
     * @return the file name
     */
    public String getFilename() {

        return m_filename;
    }

    /**
     * Returns the file path.<p>
     *
     * @return the file path
     */
    public String getFilepath() {

        return m_filepath;
    }

    /**
     * Returns the file size.<p>
     *
     * @return the file size
     */
    public String getFilesize() {

        return m_filesize;
    }

    /**
     * Sets the file date.<p>
     *
     * @param filedate the file date to set
     */
    public void setFiledate(String filedate) {

        m_filedate = filedate;
    }

    /**
     * Sets the file name.<p>
     *
     * @param filename the file name to set
     */
    public void setFilename(String filename) {

        m_filename = filename;
    }

    /**
     * Sets the file path.<p>
     *
     * @param filepath the file path to set
     */
    public void setFilepath(String filepath) {

        m_filepath = filepath;
    }

    /**
     * Sets the file size.<p>
     *
     * @param filesize the file size to set
     */
    public void setFilesize(String filesize) {

        m_filesize = filesize;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_MSG_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 3));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        setKeyPrefix(KEY_PREFIX);

        setFilename(getDownloadFile().getName());
        setFilesize("" + getDownloadFile().length());
        setFilepath(getDownloadFile().getAbsolutePath());
        setFiledate(
            CmsDateUtil.getDateTime(new Date(getDownloadFile().lastModified()), DateFormat.MEDIUM, getLocale()));

        addWidget(new CmsWidgetDialogParameter(this, "filename", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "filesize", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "filepath", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "filedate", PAGES[0], new CmsDisplayWidget()));
    }

    /**
     * Returns the file that will be downloaded upon clicking the download button
     * generated in this form by <code>{@link #dialogButtonsOkCancel()}</code>.<p>
     *
     * @return the file that will be downloaded upon clicking the download button
     *         generated in this form by <code>{@link #dialogButtonsOkCancel()}</code>
     *
     * @throws CmsRuntimeException if access to the chosen file to download fails
     */
    protected File getDownloadFile() throws CmsRuntimeException {

        if (m_downloadFile == null) {
            // no clone needed: we just read here.
            CmsRfsFileViewer fileView = OpenCms.getWorkplaceManager().getFileViewSettings();
            m_downloadFile = new File(fileView.getFilePath());
            try {
                // 2nd check: it is impossible to set an invalid path to that class.
                m_downloadFile = m_downloadFile.getCanonicalFile();
            } catch (IOException ioex) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_FILE_ACCESS_0), ioex);
            }
        }
        return m_downloadFile;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        super.initMessages();
        addMessages(org.opencms.workplace.tools.workplace.rfsfile.Messages.get().getBundleName());
    }
}