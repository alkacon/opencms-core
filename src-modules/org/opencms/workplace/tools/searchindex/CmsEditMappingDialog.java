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

package org.opencms.workplace.tools.searchindex;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.search.fields.CmsSearchFieldMappingType;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 *
 * Dialog to edit new or existing mapping in the administration view.<p>
 *
 * @since 6.5.5
 */
public class CmsEditMappingDialog extends A_CmsMappingDialog {

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp the jsp action element
     */
    public CmsEditMappingDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditMappingDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
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
            result.append(createDialogRowsHtml(0, 2));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        super.defineWidgets();

        // widgets to display
        // new indexsource
        addWidget(
            new CmsWidgetDialogParameter(this, "type", PAGES[0], new CmsSelectWidget(getTypeWidgetConfiguration())));
        addWidget(new CmsWidgetDialogParameter(m_mapping, "param", "", PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_mapping, "defaultValue", "", PAGES[0], new CmsInputWidget(), 0, 1));
    }

    /**
     * Sets the mapping type of the mapping.<p>
     *
     * @param type String value of the mapping type
     */
    public void setType(String type) {

        m_mapping.setType(type);
    }

    /**
     * Returns the String value of the mapping type.<p>
     *
     * @return String value of the mapping type
     */
    public String getType() {

        if ((m_mapping != null) && (m_mapping.getType() != null)) {
            return m_mapping.getType().toString();
        }
        return "";
    }

    /**
     * Returns a list of CmsSearchFieldMappingTypes for the type select box.<p>
     *
     * @return a list of CmsSearchFieldMappingTypes
     */
    private List<CmsSelectWidgetOption> getTypeWidgetConfiguration() {

        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        result.add(new CmsSelectWidgetOption(CmsSearchFieldMappingType.CONTENT.toString(), true));
        result.add(new CmsSelectWidgetOption(CmsSearchFieldMappingType.PROPERTY.toString(), false));
        result.add(new CmsSelectWidgetOption(CmsSearchFieldMappingType.PROPERTY_SEARCH.toString(), false));
        result.add(new CmsSelectWidgetOption(CmsSearchFieldMappingType.ITEM.toString(), false));
        return result;
    }
}
