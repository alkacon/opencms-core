/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/rfsfile/CmsRfsFileDownloadDialog.java,v $
 * Date   : $Date: 2005/06/26 13:07:56 $
 * Version: $Revision: 1.9 $
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

package org.opencms.workplace.tools.workplace.rfsfile;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Shows useful information about the current file chosen within the 
 * <code>{@link org.opencms.util.CmsRfsFileViewer}</code> and offers 
 * a direct download link. <p>
 *
 * @author  Achim Westermann 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsRfsFileDownloadDialog extends CmsDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "rfsfiledownload";

    private File m_downloadFile;

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
     * Returns the HTML for the history settings input form.<p>
     * 
     * @return the HTML code for the history settings input form
     */
    public String buildDownloadFileView() {

        StringBuffer result = new StringBuffer(512);

        result.append(dialogBlock(HTML_START, Messages.get().key(
            getLocale(),
            Messages.GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_MSG_0,
            null), false));

        result.append("\r\n");
        result.append("<!-- start buildDownloadFileView -->\r\n");
        result.append("<table border=\"0\" style=\"padding:10px;\"}>\r\n");

        result.append("  <tr>\r\n");
        result.append("    <td>");
        result.append(Messages.get().key(Messages.GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FNAME_0));
        result.append(":</td>\r\n");
        result.append("    <td >");
        result.append(" ").append(m_downloadFile.getName());
        result.append("</td>\r\n");
        result.append("  </tr>\r\n");

        result.append("  <tr>\r\n");
        result.append("    <td>");
        result.append(Messages.get().key(Messages.GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FSIZE_0));
        result.append(":</td>\n");
        result.append("    <td>");
        result.append(CmsFileUtil.formatFilesize(m_downloadFile.length(), getLocale()));
        result.append("</td>\r\n");
        result.append("  </tr>\r\n");

        result.append("  <tr>\r\n");
        result.append("    <td>");
        result.append(Messages.get().key(Messages.GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FPATH_0));
        result.append(":</td>\n");
        result.append("    <td>");
        result.append(m_downloadFile.getAbsolutePath());
        result.append("</td>\r\n");
        result.append("  </tr>\r\n");

        result.append("  <tr>\r\n");
        result.append("    <td>");
        result.append(Messages.get().key(Messages.GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FLMOD_0));
        result.append(":</td>\n");
        result.append("    <td>");
        result.append(CmsDateUtil.getDateTime(new Date(m_downloadFile.lastModified()), DateFormat.MEDIUM, getLocale()));
        result.append("</td>\r\n");
        result.append("  </tr>\r\n");
        result.append("</table>\r\n");
        result.append("<!-- end buildDownloadFileView -->\r\n");
        result.append("\r\n");

        result.append(dialogBlock(HTML_END, m_downloadFile.getName(), true));
        return result.toString();
    }

    /**
     * Returns the script used to open the download popup.<p>
     * 
     * Has to be called from the jsp to work propertly.<p> 
     * 
     * @return the script used to open the download popup
     */
    public String buildDownloadScript() {

        StringBuffer result = new StringBuffer(212);
        // the javasript for download button 
        result.append("<script type=\"text/javascript\">\r\n");
        result.append("function doDownload() {\r\n");
        result.append("  window.open(\"");
        result.append(getJsp().link("/system/workplace/admin/workplace/logfileview/dodownload.jsp?"));
        result.append("servletUrl=");
        result.append(getJsp().link("/system/workplace/admin/workplace/logfileview/downloadTrigger.jsp"));
        result.append("\", \"download\", \"width=300,height=130,left=100,top=100,menubar=no,status=no,toolbar=no\");\r\n");
        result.append("}\r\n");
        result.append("</script>\r\n");
        return result.toString();
    }

    /**
     * Returns the HTML for a button that triggers a file download with 
     * <code>{@link CmsRfsFileDownloadServlet}</code> and a button for the chancel action.<p>
     *  
     * @see org.opencms.workplace.CmsDialog#dialogButtonsOkCancel()
     * @return the HTML for a button that triggers a file download with 
     *         <code>{@link CmsRfsFileDownloadServlet}</code> and a button for the chancel action. 
     */
    public String dialogButtonsOkCancel() {

        StringBuffer result = new StringBuffer();
        result.append("<!-- button row start -->\r\n");
        result.append("<div class=\"dialogbuttons\" unselectable=\"on\">\r\n");
        result.append("  <p>\r\n    ");
        result.append("<input type=\"button\" value=\"").append(
            org.opencms.workplace.Messages.get().key(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_DOWNLOAD_0)).append(
            "\" onclick=\"doDownload();\" class=\"dialogbutton\">");

        result.append("<input name=\"back\" type=\"button\" value=\"");
        result.append(org.opencms.workplace.Messages.get().key(
            getLocale(),
            org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_BACK_0,
            null));
        result.append("\" onclick=\"submitAction(\'");
        result.append(CmsDialog.DIALOG_CANCEL);
        result.append("\', null, 'main');\" class=\"dialogbutton\">\r\n");
        result.append("</div>\r\n");
        result.append("<!-- button row end -->\r\n");

        return result.toString();
    }

    /**
     * Returns the file that will be downloaded upon clicking the download button 
     * generated in this form by <code>{@link #dialogButtonsOkCancel()}</code>.<p>
     * 
     * @return the file that will be downloaded upon clicking the download button 
     *         generated in this form by <code>{@link #dialogButtonsOkCancel()}</code>
     */
    public File getDownloadFile() {

        return m_downloadFile;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // triggered by the super-constructor so cannot be 
        // placed in the constructor here or nullpointer exception occurs.
        this.m_downloadFile = getDownloadFileInternal();

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);
        } else {
            // set the default action               
            setAction(ACTION_DEFAULT);
            setParamTitle(Messages.get().key(
                getLocale(),
                Messages.GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_TITLE_1,
                new Object[] {getDownloadFile().getName()}));
        }
    }

    /**
     * Returns the File to downoad from the <code>{@link org.opencms.workplace.WorkplaceManager}</code>'s 
     * setting {@link org.opencms.workplace.WorkplaceManager#getRfsFileViewSettings()}.<p>
     * 
     * @return the File to downoad from the <code>{@link org.opencms.workplace.WorkplaceManager}</code>'s 
     *         setting {@link org.opencms.workplace.WorkplaceManager#getRfsFileViewSettings()}
     * @throws CmsRuntimeException if access to the chosen file to download fails
     */
    private File getDownloadFileInternal() throws CmsRuntimeException {

        // no unfrozen clone needed: we just read here.
        CmsRfsFileViewer fileView = OpenCms.getWorkplaceManager().getFileViewSettings();
        File file = new File(fileView.getFilePath());
        try {
            // 2nd check: it is impossible to set an invalid path to that class. 
            return file.getCanonicalFile();
        } catch (IOException ioex) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_FILE_ACCESS_0), ioex);
        }
    }
}