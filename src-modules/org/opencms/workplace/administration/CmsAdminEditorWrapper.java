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

package org.opencms.workplace.administration;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Workplace class for /system/workplace/views/admin/admin-editor.jsp .<p>
 *
 * @since 6.0.0
 */
public class CmsAdminEditorWrapper extends CmsDialog {

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsAdminEditorWrapper(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminEditorWrapper(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns all parameters of the current workplace class
     * as hidden field tags that can be inserted in a form.<p>
     *
     * @return all parameters of the current workplace class
     * as hidden field tags that can be inserted in a html form
     */
    @Override
    public String allParamsAsHidden() {

        StringBuffer result = new StringBuffer(512);
        Map<String, Object> params = allParamValues();
        Iterator<Entry<String, Object>> i = params.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, Object> entry = i.next();
            result.append("<input type=\"hidden\" name=\"");
            result.append(entry.getKey());
            result.append("\" value=\"");
            String encoded = CmsEncoder.escapeXml(entry.getValue().toString());
            result.append(encoded);
            result.append("\">\n");
        }
        return result.toString();
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     *
     * @throws Exception if writing to the JSP out fails
     */
    public void displayDialog() throws Exception {

        initAdminTool();
        getToolManager().setCurrentToolPath(this, getParentPath());

        JspWriter out = getJsp().getJspContext().getOut();
        out.print(htmlStart());
        out.print(bodyStart(null));
        out.print("<form name='editor' method='post' target='_top' action='");
        out.print(getJsp().link("/system/workplace/editors/editor.jsp"));
        out.print("'>\n");
        out.print(allParamsAsHidden());
        out.print("</form>\n");
        out.print("<script type='text/javascript'>\n");
        out.print("document.forms['editor'].submit();\n");
        out.print("</script>\n");
        out.print(dialogContentStart(getParamTitle()));
        out.print(dialogContentEnd());
        out.print(dialogEnd());
        out.print(bodyEnd());
        out.print(htmlEnd());
    }
}
