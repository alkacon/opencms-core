/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.modules;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Edit class to edit an exiting module.<p>
 * 
 * @since 6.0.0 
 */
public class CmsModulesEdit extends CmsModulesEditBase {

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsModulesEdit(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesEdit(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));

    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>  
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            result.append(dialogBlockStart(key("label.moduleinformation")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 6));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key("label.modulecreator")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(7, 8));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            if (CmsStringUtil.isEmpty(m_module.getName())) {
                result.append(dialogBlockStart(key("label.modulefolder")));
                result.append(createWidgetTableStart());
                result.append(createDialogRowsHtml(9, 15));
                result.append(createWidgetTableEnd());
                result.append(dialogBlockEnd());
            }
        }

        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        super.defineWidgets();
        if (CmsStringUtil.isEmpty(m_module.getName())) {
            addWidget(new CmsWidgetDialogParameter(m_module, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_module, "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_module, "niceName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "description", PAGES[0], new CmsTextareaWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "version.version", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "group", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "actionClass", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "importScript", PAGES[0], new CmsTextareaWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "authorName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "authorEmail", PAGES[0], new CmsInputWidget()));
        // add the second page only when creating a new module
        if (CmsStringUtil.isEmpty(m_module.getName())) {
            addWidget(new CmsWidgetDialogParameter(m_module, "createModuleFolder", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_module, "createTemplateFolder", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_module, "createElementsFolder", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(
                m_module,
                "createFormattersFolder",
                PAGES[0],
                new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_module, "createResourcesFolder", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_module, "createSchemasFolder", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_module, "createClassesFolder", PAGES[0], new CmsCheckboxWidget()));
            addWidget(new CmsWidgetDialogParameter(m_module, "createLibFolder", PAGES[0], new CmsCheckboxWidget()));
        }
    }

}
