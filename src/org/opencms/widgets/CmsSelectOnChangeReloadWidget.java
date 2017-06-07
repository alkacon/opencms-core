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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.workplace.CmsDialog;

import java.util.Iterator;
import java.util.List;

/**
 * Provides a widget for a standard HTML form select box with. The jsp where this widget is used is
 * reloaded, when the select box value is changed.<p>
 *
 * Please see the documentation of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> for a description
 * about the configuration String syntax for the select options.<p>
 *
 * The select widget does use the following select options:<ul>
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getValue()}</code> for the <code>value</code> of the HTML select box
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#isDefault()}</code> for pre-selecting a specific value
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getOption()}</code> for the <code>option</code> of the HTML select box
 * </ul>
 * <p>
 *
 * @since 7.5.3
 */
public class CmsSelectOnChangeReloadWidget extends CmsSelectWidget {

    /**
     * Creates a new select widget.<p>
     */
    public CmsSelectOnChangeReloadWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a select widget with the select options specified in the given configuration List.<p>
     *
     * The list elements must be of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     *
     * @param configuration the configuration (possible options) for the select widget
     *
     * @see CmsSelectWidgetOption
     */
    public CmsSelectOnChangeReloadWidget(List<CmsSelectWidgetOption> configuration) {

        super(configuration);
    }

    /**
     * Creates a select widget with the specified select options.<p>
     *
     * @param configuration the configuration (possible options) for the select box
     */
    public CmsSelectOnChangeReloadWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);

        result.append("<td class=\"xmlTd\" style=\"height: 25px;\"><select class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"");
        result.append(
            " onChange=\"document.getElementsByName('"
                + CmsDialog.PARAM_ACTION
                + "')[0].value='"
                + CmsDialog.PARAM_ACTION_VALUE_FOR_CHANGED_INDEX
                + "';this.form.submit()\"");
        result.append(">");

        // get select box options from default value String
        List<CmsSelectWidgetOption> options = parseSelectOptions(cms, widgetDialog, param);
        String selected = getSelectedValue(cms, param);
        Iterator<CmsSelectWidgetOption> i = options.iterator();
        while (i.hasNext()) {
            CmsSelectWidgetOption option = i.next();
            // create the option
            result.append("<option value=\"");
            result.append(option.getValue());
            result.append("\"");
            if ((selected != null) && selected.equals(option.getValue())) {
                result.append(" selected=\"selected\"");
            }
            result.append(">");
            result.append(option.getOption());
            result.append("</option>");
        }

        result.append("</select>");
        result.append("</td>");

        return result.toString();
    }
}