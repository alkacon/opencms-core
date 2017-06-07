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

package org.opencms.workplace.search;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Workplace class for /system/workplace/explorer/search/edit.jsp explorer tool.<p>
 *
 * @since 6.0.0
 */
public class CmsSearchEditorWrapper extends CmsDialog {

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSearchEditorWrapper(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSearchEditorWrapper(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     *
     * @throws Exception if writing to the JSP out fails
     */
    public void displayDialog() throws Exception {

        initAdminTool();

        JspWriter out = getJsp().getJspContext().getOut();
        out.print(htmlStart());
        out.print(bodyStart(null));
        out.print("<form name='editor' method='post' target='_top' action='");
        out.print(getJsp().link("/system/workplace/editors/editor.jsp"));
        out.print("'>\n");
        out.print(paramsAsHidden());
        out.print("</form>\n");
        out.print("<script type='text/javascript'>\n");
        out.print("document.forms['editor'].submit();\n");
        out.print("</script>\n");
        out.print(bodyEnd());
        out.print(htmlEnd());
    }
}