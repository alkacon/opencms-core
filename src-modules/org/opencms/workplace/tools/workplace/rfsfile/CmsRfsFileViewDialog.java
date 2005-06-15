/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/rfsfile/CmsRfsFileViewDialog.java,v $
 * Date   : $Date: 2005/06/15 12:51:24 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.workplace.rfsfile;

import org.opencms.jsp.CmsJspActionElement;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Displays a certain amount of lines starting from a certain starting line 
 * which are specified in the <code>{@link org.opencms.workplace.CmsWorkplaceManager}'s</code> 
 * <code>{@link org.opencms.util.CmsRfsFileViewer}</code>.<p>
 * 
 * @author  Achim Westermann (a.westermann@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 6.0
 * 
 */
public class CmsRfsFileViewDialog extends A_CmsRfsFileWidgetDialog {

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

        // wrap a box with scrollbars around the file content: 
        result.append(createFileContentBoxStart());
        try {
            result.append(m_logView.readFilePortion());
        } catch (Throwable f) {
            List commitErrors = getCommitErrors();
            if (commitErrors == null) {
                commitErrors = new LinkedList();
            }
            commitErrors.add(f);
            setCommitErrors(commitErrors);
        }
        result.append(createFileContentBoxEnd());
        // close widget table
        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Returns the ending <code>div</code> 
     * for wrapping the content of a file in a box with scrollbars.<p>
     * 
     * @return the ending <code>div</code> for wrapping the content of a file 
     *         in a box with scrollbars
     */
    protected String createFileContentBoxEnd() {

        StringBuffer result = new StringBuffer(127);
        result.append("  </pre>\r\n");
        result.append("</div>\r\n");
        return result.toString();
    }

    /**
     * Returns the starting <code>div</code> with custom style (new_admin.css -> logfilewidget) 
     * for wrapping the content of a file. <p>
     * 
     * @return the starting <code>div</code> with custom style (new_admin.css -> logfilewidget) 
     *         for wrapping the content of a file
     */
    protected String createFileContentBoxStart() {

        StringBuffer result = new StringBuffer(127);
        result.append("<span class=\"logfilewidgethead\" >\r\n");
        result.append("  ").append(m_logView.getFilePath().replace('\\', '/')).append("\r\n");
        result.append("</span>\r\n");
        result.append("<div id=\"logview\" class=\"logfilewidget\">\r\n");
        // the file path in the "border"
        result.append("  <pre>\r\n");
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