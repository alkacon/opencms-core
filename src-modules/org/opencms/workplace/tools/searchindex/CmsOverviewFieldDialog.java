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

package org.opencms.workplace.tools.searchindex;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 *
 * Widget dialog for an overview of a <code>{@link org.opencms.search.fields.CmsLuceneField}</code>.<p>
 *
 * @since 6.5.5
 */
public class CmsOverviewFieldDialog extends A_CmsFieldDialog {

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp the jsp action element
     */
    public CmsOverviewFieldDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsOverviewFieldDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns the String value of the indexed value.<p>
     *
     * @return String value of the indexed value
     */
    public String getIndexed() {

        if ((m_field != null) && (m_field.getIndexed() != null)) {
            return m_field.getIndexed();
        }
        return "";
    }

    /**
     * Sets the indexed value of the field.<p>
     *
     * @param indexed String value of the indexed value
     */
    public void setIndexed(String indexed) {

        m_field.setIndexed(indexed);
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
            result.append(dialogBlockStart(key(Messages.GUI_LABEL_FIELD_BLOCK_SETTINGS_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 6));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defaultActionHtmlEnd()
     */
    @Override
    protected String defaultActionHtmlEnd() {

        return "";
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        super.defineWidgets();

        addWidget(new CmsWidgetDialogParameter(m_field, "name", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_field, "displayName", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_field, "stored", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "indexed", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_field, "inExcerpt", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_field, "boost", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_field, "defaultValue", PAGES[0], new CmsDisplayWidget()));
    }
}
