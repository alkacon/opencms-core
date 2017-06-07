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

import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.workplace.CmsWidgetDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates a CSV file for a given list.<p>
 *
 * @since 6.0.0
 */
public class CmsRfsFileDisposalDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The file to download. */
    private File m_downloadFile;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRfsFileDisposalDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRfsFileDisposalDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        // empty
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#dialogButtonsCustom()
     */
    @Override
    public String dialogButtonsCustom() {

        return dialogButtons(new int[] {BUTTON_CLOSE}, new String[1]);
    }

    /**
     * Generates the output.<p>
     *
     * @throws IOException if something goes wrong
     */
    public void generateOutput() throws IOException {

        HttpServletResponse res = CmsFlexController.getController(getJsp().getRequest()).getTopResponse();
        res.setContentType("application/octet-stream");
        res.setHeader(
            "Content-Disposition",
            new StringBuffer("attachment; filename=\"").append(getDownloadFile().getName()).append("\"").toString());
        res.setContentLength((int)getDownloadFile().length());

        // getOutputStream() throws IllegalStateException if the jsp directive buffer="none" is set.
        ServletOutputStream outStream = res.getOutputStream();
        InputStream in = new BufferedInputStream(new FileInputStream(getDownloadFile()));

        try {
            // don't write the last '-1'
            int bit = in.read();
            while ((bit) >= 0) {
                outStream.write(bit);
                bit = in.read();
            }
        } catch (SocketException soe) {
            // this is the case for ie if cancel in download window is chosen:
            // "Connection reset by peer: socket write error". But not for firefox -> don't care
        } finally {
            if (outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                } catch (SocketException soe) {
                    // ignore
                }
            }
            in.close();
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#checkRole()
     */
    @Override
    protected void checkRole() throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.WORKPLACE_MANAGER);
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
            result.append("<script type=\"text/javascript\">\n");
            result.append("function download(){\n");
            result.append("\twindow.open(\"").append(getJsp().link(getDownloadPath())).append("\", \"rfsfile\");\n");
            result.append("}\n");
            result.append("window.setTimeout(\"download()\",500);\n");
            result.append("</script>\n");
            result.append(
                dialogBlockStart(
                    key(
                        Messages.GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_HEADER_1,
                        new Object[] {getDownloadFile().getName()})));
            result.append(key(Messages.GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_MESSAGE_0));
            result.append(" <a href='javascript:download()'>");
            result.append(key(Messages.GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_LINKTXT_0));
            result.append("</a>.");
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

        // empty
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
     * Returns the download path.<p>
     *
     * @return the download path
     */
    protected String getDownloadPath() {

        return "/system/workplace/admin/workplace/logfileview/downloadTrigger.jsp";
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

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.WORKPLACE_MANAGER);
    }
}
