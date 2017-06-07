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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;
import org.opencms.workplace.tools.CmsToolMacroResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Displays a print preview of a given list.<p>
 *
 * @since 6.0.0
 */
public class CmsListPrintDialog extends CmsDialog {

    /** List class parameter name constant. */
    public static final String PARAM_LISTCLASS = "listclass";

    /** The list to print. */
    private final CmsHtmlList m_list;

    /** List class paramater value. */
    private String m_paramListclass;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     *
     * @throws ClassNotFoundException if the list dialog class is not found
     */
    public CmsListPrintDialog(CmsJspActionElement jsp)
    throws ClassNotFoundException {

        super(jsp);
        setParamStyle(STYLE_NEW);
        m_list = A_CmsListDialog.getListObject(Class.forName(getParamListclass()), getSettings());
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     *
     * @throws ClassNotFoundException if the list dialog class is not found
     */
    public CmsListPrintDialog(PageContext context, HttpServletRequest req, HttpServletResponse res)
    throws ClassNotFoundException {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.CmsToolDialog#dialogTitle()
     */
    @Override
    public String dialogTitle() {

        // build title
        StringBuffer html = new StringBuffer(512);
        CmsMessages message = Messages.get().getBundle(getLocale());
        html.append("<div class='screenTitle'>\n");
        html.append("\t<table width='100%' cellspacing='0'>\n");
        html.append("\t\t<tr>\n");
        html.append("\t\t\t<td>\n");
        html.append(m_list.getName().key(getLocale()));
        html.append("\n\t\t\t</td>");
        html.append("\t\t\t<td class='uplevel'>\n\t\t\t\t");
        html.append(
            A_CmsHtmlIconButton.defaultButtonHtml(
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                "id-print",
                message.key(Messages.GUI_ACTION_PRINT_NAME_0),
                message.key(Messages.GUI_ACTION_PRINT_HELP_0),
                true,
                "list/print.png",
                null,
                "print();"));
        html.append("\n\t\t\t</td>\n");
        html.append("\t\t</tr>\n");
        html.append("\t</table>\n");
        html.append("</div>\n");

        return CmsToolMacroResolver.resolveMacros(html.toString(), this);
    }

    /**
     * Generates the printable output for the given list.<p>
     *
     * @return html code
     */
    public String generateHtml() {

        StringBuffer result = new StringBuffer(2048);
        result.append(htmlStart(null));
        result.append(CmsListExplorerColumn.getExplorerStyleDef());
        result.append(bodyStart("dialog", null));
        result.append(dialogStart());
        result.append(dialogContentStart(getParamTitle()));
        result.append(m_list.printableHtml());
        result.append(dialogContentEnd());
        result.append(dialogEnd());
        result.append(bodyEnd());
        result.append(htmlEnd());
        return result.toString();
    }

    /**
     * Returns the value for the List class parameter.<p>
     *
     * @return the value for the List class parameter
     */
    public String getParamListclass() {

        return m_paramListclass;
    }

    /**
     * Sets the value for the List class parameter.<p>
     *
     * @param listclass the value for the List class parameter to set
     */
    public void setParamListclass(String listclass) {

        m_paramListclass = listclass;
    }
}