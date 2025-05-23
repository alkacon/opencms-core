/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.modules;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Edit class to edit an exiting module.<p>
 *
 * @since 6.0.0
 */
public class CmsModulesEditResources extends CmsModulesEditBase {

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsModulesEditResources(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesEditResources(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.modules.CmsModulesEditBase#actionCommit()
     */
    @Override
    public void actionCommit() {

        try {
            // validate the module reouces
            m_module.checkResources(getCms());
        } catch (CmsRuntimeException e) {
            addCommitError(e);
        }

        // now do the committing in the base class
        super.actionCommit();
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            result.append(dialogBlockStart(key("label.resource")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        if (dialog.equals(PAGES[0])) {
            result.append(dialogBlockStart(key("label.excluderesource")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(1, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }
        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        super.defineWidgets();

        addWidget(new CmsWidgetDialogParameter(m_module, "resources", PAGES[0], new CmsVfsFileWidget()));
        addWidget(new CmsWidgetDialogParameter(m_module, "excludeResources", PAGES[0], new CmsVfsFileWidget()));
    }
}
