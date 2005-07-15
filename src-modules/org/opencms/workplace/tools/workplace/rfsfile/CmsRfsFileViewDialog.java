/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/rfsfile/CmsRfsFileViewDialog.java,v $
 * Date   : $Date: 2005/07/15 10:34:03 $
 * Version: $Revision: 1.7 $
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Displays a certain amount of lines starting from a certain starting line 
 * which are specified in the <code>{@link org.opencms.workplace.CmsWorkplaceManager}'s</code> 
 * <code>{@link org.opencms.util.CmsRfsFileViewer}</code>.<p>
 * 
 * @author  Achim Westermann 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsRfsFileViewDialog extends A_CmsRfsFileWidgetDialog {

    /**
     * Boolean request parameter that switches between serving the content of the file 
     * to the iframe of the page that is generated if the switch is false. <p>
     */
    String m_paramShowlog;

    /**
     * Public constructor with JSP action element.<p> 
     * 
     * @param jsp the CmsJspActionElement
     */
    public CmsRfsFileViewDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRfsFileViewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#displayDialog()
     */
    public void displayDialog() throws JspException, IOException, ServletException {

        if (!Boolean.valueOf(getParamShowlog()).booleanValue()) {
            super.displayDialog();
        } else {
            StringBuffer result = new StringBuffer(1024);
            // wrap a box with scrollbars around the file content: 
            try {
                result.append("<pre>");
                result.append(m_logView.readFilePortion());
                result.append("</pre>");
            } catch (Throwable f) {
                List commitErrors = getCommitErrors();
                if (commitErrors == null) {
                    commitErrors = new LinkedList();
                }
                commitErrors.add(f);
                setCommitErrors(commitErrors);
            }
            JspWriter out = getJsp().getJspContext().getOut();
            out.print(result.toString());
        }
    }

    /**
     * Returns true wether the content of the file should be written to the response or false 
     * if the page content should be generated.<p>
     *  
     * @return true wether the content of the file should be written to the response or false 
     * if the page content should be generated
     */
    public String getParamShowlog() {

        return m_paramShowlog;
    }

    /**
     * Set the value to decide wether page content or the file content has to be shown to the response.<p> 
     * 
     * @param value the value to decide wether page content or the file content has to be shown to the response to set
     */
    public void setParamShowlog(String value) {

        m_paramShowlog = value;
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
        if (m_logView.getFilePath() == null) {
            fileContentHeader = Messages.get().key(getLocale(), Messages.GUI_WORKPLACE_LOGVIEW_NO_FILE_SELECTED_0, null);
        } else {
            fileContentHeader = m_logView.getFilePath().replace('\\', '/');
        }
        result.append(createWidgetBlockStart(fileContentHeader));
        result.append("<iframe style=\"overflow: auto;\" src=\"");
        result.append(getJsp().link("/system/workplace/admin/workplace/logfileview/index.html?showlog=true"));
        result.append("\" width=\"100%\" height=\"400\" border=\"0\" frameborder=\"0\"></iframe>");
        result.append(createWidgetBlockEnd());

        // result.append(createFileContentBoxEnd());
        // close widget table

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        super.defineWidgets();
        // no widgets as controls on the front page are just links to "Edit Settings"
    }
}