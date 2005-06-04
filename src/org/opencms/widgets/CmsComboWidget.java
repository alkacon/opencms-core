/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsComboWidget.java,v $
 * Date   : $Date: 2005/06/04 08:11:29 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Provides a HTML text input field with optional values to select in a combo box, for use on a widget dialog.<p>
 * 
 * The combo box options have to be written in the "default" appinfo node and use the following syntax:<br>
 * value1*:displayed help text 1|value2:displayed help text 2<br>
 * You can use localized keys for the displayed values and text like ${key.keyname}.<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 * @since 5.5.3
 */
public class CmsComboWidget extends A_CmsWidget {

    /** The delimiter that separates the values from the displayed help text. */
    public static final char DELIM_ATTRS = ':';

    /** The delimiter that separates the option entries of the combo box to create. */
    public static final String DELIM_OPTIONS = "|";

    /** The possible options for the combo box. */
    private String m_comboOptions;

    /**
     * Creates a new combo widget.<p>
     */
    public CmsComboWidget() {

        // empty constructor is required for class registration
        this("");
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

        // get select box options from configuration String
        String defaultValue = getComboOptions(cms, widgetDialog);

        // tokenize the found String
        StringTokenizer t = new StringTokenizer(defaultValue, DELIM_OPTIONS);
        String val;
        String helpText;
        int delimPos;

        if (t.hasMoreTokens()) {
            // create combo div
            result.append("<div class=\"widgetcombo\" id=\"combo").append(id).append("\">\n");
            int count = 0;
            Map helpTexts = new HashMap(t.countTokens());
            while (t.hasMoreTokens()) {
                // generate the combo options
                String itemId = "ci" + id + "." + count;
                String part = t.nextToken();
                delimPos = part.indexOf(DELIM_ATTRS);
                if (delimPos != -1) {
                    // a special help text is given
                    val = part.substring(0, delimPos);
                    helpText = part.substring(delimPos + 1);
                } else {
                    // no special help text present
                    val = part;
                    helpText = null;
                }
                // create the link around value
                result.append("\t<a href=\"javascript:setComboValue(\'");
                result.append(id);
                result.append("\', \'");
                result.append(itemId);
                result.append("\')\" name=\"").append(itemId).append("\" id=\"").append(itemId).append("\"");
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
                    // create help text mousevent attributes
                    helpTexts.put(itemId, helpText);
                    result.append(getJsHelpMouseHandler(widgetDialog, itemId));
                }
                result.append(">");
                result.append(val);
                result.append("</a>\n");
                count++;
            }

            // close combo div
            result.append("</div>\n");

            // create help texts for the values
            Iterator i = helpTexts.keySet().iterator();
            while (i.hasNext()) {
                String key = (String)i.next();
                result.append("<div class=\"help\" id=\"help");
                result.append(key);
                result.append("\"");
                result.append(getJsHelpMouseHandler(widgetDialog, key));
                result.append(">");
                result.append(helpTexts.get(key));
                result.append("</div>\n");
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
        result.append("\" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\">");
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

    /**
     * Returns the possible options for the combo box.<p>
     * 
     * In case the combo options have not been directly set, 
     * the default value of the given widget parameter is used.<p>
     * @param cms the current users OpenCms context
     * 
     * @return the possible options for the combo box
     */
    private String getComboOptions(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        if (m_comboOptions == null) {
            // use the configuration value, with processed macros
            m_comboOptions = CmsMacroResolver.resolveMacros(getConfiguration(), cms, widgetDialog.getMessages());
        }
        if (CmsStringUtil.isEmpty(m_comboOptions)) {
            m_comboOptions = "";
        }
        return m_comboOptions;
    }
}