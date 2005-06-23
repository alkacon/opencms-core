/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsComboWidget.java,v $
 * Date   : $Date: 2005/06/23 10:47:10 $
 * Version: $Revision: 1.8 $
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.workplace.CmsWorkplace;

import java.util.Iterator;
import java.util.List;

/**
 * Provides a HTML text input field with optional values to select in a combo box, for use on a widget dialog.<p>
 * 
 * Please see the documentation of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> for a description 
 * about the configuration String syntax for the select options.<p>
 * 
 * The combo widget does use the following select options:<ul>
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getValue()}</code> for the texts to be displayed in the combo selector
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#isDefault()}</code> to fill the input with a preselected text
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getHelp()}</code> to display an (optional) help text for the combo option
 * </ul>
 * 
 * @author Andreas Zahner 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsComboWidget extends A_CmsSelectWidget {

    /**
     * Creates a new combo widget.<p>
     */
    public CmsComboWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a combo widget with the select options specified in the given configuration List.<p>
     * 
     * The list elements must be of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     * 
     * @param configuration the configuration (possible options) for the select widget
     * 
     * @see CmsSelectWidgetOption
     */
    public CmsComboWidget(List configuration) {

        super(configuration);
    }

    /**
     * Creates a combo widget with the specified combo options.<p>
     * 
     * @param configuration the configuration (possible options) for the combo box
     */
    public CmsComboWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getDialogHtmlEnd(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogHtmlEnd(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(256);

        // get the select box options
        List options = parseSelectOptions(cms, widgetDialog, param);

        if (options.size() > 0) {
            // create combo div
            result.append("<div class=\"widgetcombo\" id=\"combo");
            result.append(id);
            result.append("\">\n");

            int count = 0;
            Iterator i = options.iterator();
            while (i.hasNext()) {
                CmsSelectWidgetOption option = (CmsSelectWidgetOption)i.next();
                String itemId = new StringBuffer(64).append("ci").append(id).append('.').append(count).toString();
                // create the link around value
                result.append("\t<a href=\"javascript:setComboValue(\'");
                result.append(id);
                result.append("\', \'");
                result.append(itemId);
                result.append("\')\" name=\"");
                result.append(itemId);
                result.append("\" id=\"");
                result.append(itemId);
                result.append("\"");
                if (option.getHelp() != null) {
                    // create help text mousevent attributes
                    result.append(getJsHelpMouseHandler(widgetDialog, itemId));
                }
                result.append(">");
                result.append(option.getValue());
                result.append("</a>\n");
                count++;
            }

            // close combo div
            result.append("</div>\n");

            // create help texts for the values
            count = 0;
            i = options.iterator();
            while (i.hasNext()) {
                CmsSelectWidgetOption option = (CmsSelectWidgetOption)i.next();
                if (option.getHelp() != null) {
                    // help text is optional
                    String itemId = new StringBuffer(64).append("ci").append(id).append('.').append(count).toString();
                    result.append("<div class=\"help\" id=\"help");
                    result.append(itemId);
                    result.append("\"");
                    result.append(getJsHelpMouseHandler(widgetDialog, itemId));
                    result.append(">");
                    result.append(option.getHelp());
                    result.append("</div>\n");
                    count++;
                }
            }
        }

        // return the icon help text from super class
        result.append(super.getDialogHtmlEnd(cms, widgetDialog, param));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/combobox.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "\tinitComboBox();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);
        result.append("<td class=\"xmlTd\">");

        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        // medium text input field
        result.append("<input type=\"text\" class=\"xmlInputMedium");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"");
        String selected = getSelectedValue(cms, param);
        if (selected != null) {
            // append the selection 
            result.append(" value=\"");
            result.append(CmsEncoder.escapeXml(selected));
            result.append("\"");
        }
        result.append(">");
        result.append("</td><td>");
        // button to open combo box
        result.append("<button name=\"test\" onclick=\"showCombo(\'").append(id).append("\', \'combo").append(id);
        result.append("\');return false;\" class=\"widgetcombobutton\">");
        result.append("<img src=\"");
        result.append(CmsWorkplace.getSkinUri()).append("components/widgets/combo.png");
        result.append("\" width=\"7\" height=\"12\" alt=\"\" border=\"0\">");
        result.append("</button></td></tr></table>");

        result.append("</td>");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsComboWidget(getConfiguration());
    }
}