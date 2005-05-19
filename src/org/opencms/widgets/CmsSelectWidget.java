/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsSelectWidget.java,v $
 * Date   : $Date: 2005/05/19 16:35:47 $
 * Version: $Revision: 1.3 $
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
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.util.StringTokenizer;

/**
 * Provides a standard HTML form select box widget, for use on a widget dialog.<p>
 * 
 * The select box options have to be written in the "default" appinfo node and use the following syntax:<br>
 * valueattribute1*:displayed text 1|valueattribute2:displayed text 2<br>
 * The asterisk marks the preselected value when creating a new value, the displayed text is optional.
 * You can use localized keys for the displayed text like ${key.keyname}.<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.5.3
 */
public class CmsSelectWidget extends A_CmsWidget {

    /** The delimiter that separates the value attribute from the displayed option text. */
    public static final char DELIM_ATTRS = ':';

    /** The delimiter that separates the option entries of the select box to create. */
    public static final String DELIM_OPTIONS = "|";

    /** The character that marks the preselected option of the select box. */
    public static final char PRESELECTED = '*';

    /** The possible options for the select box. */
    private String m_selectOptions;

    /**
     * Creates a new select widget.<p>
     */
    public CmsSelectWidget() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a select widget with the specified select options.<p>
     * 
     * @param configuration the configuration (possible options) for the select box
     */
    public CmsSelectWidget(String configuration) {

        m_configuration = configuration;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);

        result.append("<td class=\"xmlTd\"><select class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">");

        // get select box options from default value String
        String defaultValue = getSelectOptions(cms, widgetDialog, param);
        if (CmsStringUtil.isEmpty(defaultValue)) {
            defaultValue = "";
        }

        // tokenize the found String
        StringTokenizer t = new StringTokenizer(defaultValue, DELIM_OPTIONS);
        boolean isPreselected;
        String val;
        String label;
        String selected;
        int delimPos;
        while (t.hasMoreTokens()) {
            // generate the option tags
            String part = t.nextToken();
            // check preselection of current option
            isPreselected = part.indexOf(PRESELECTED) != -1;
            delimPos = part.indexOf(DELIM_ATTRS);
            if (delimPos != -1) {
                // a special label text is given
                val = part.substring(0, delimPos);
                label = part.substring(delimPos + 1);
            } else {
                // no special label text present, use complete String
                val = part;
                label = val;
            }

            if (isPreselected) {
                // remove eventual preselected flag markers from Strings
                String preSelected = "" + PRESELECTED;
                val = CmsStringUtil.substitute(val, preSelected, "");
                label = CmsStringUtil.substitute(label, preSelected, "");
            }

            // check if current option is selected
            String fieldValue = param.getStringValue(cms);
            if ((isPreselected && (CmsStringUtil.isEmpty(fieldValue) || defaultValue.equals(fieldValue)))
                || val.equals(fieldValue)) {
                selected = " selected=\"selected\"";
            } else {
                selected = "";
            }

            // create the option
            result.append("<option value=\"");
            result.append(val);
            result.append("\"");
            result.append(selected);
            result.append(">");
            result.append(label);
            result.append("</option>");
        }

        result.append("</select>");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsSelectWidget(m_configuration);
    }

    /**
     * Returns the possible options for the select box.<p>
     * 
     * In case the select Options have not been directly set, 
     * the default value of the given widget parameter is used.<p>
     * 
     * @param cms the current users OpenCms context
     * @param param the current widget parameter
     *   
     * @return the possible options for the select box
     */
    private String getSelectOptions(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        if (m_selectOptions == null) {
            if (m_configuration == null) {
                // use the default value
                m_selectOptions = param.getDefault(cms);
            } else {
                // use the configuration value, with processed macros
                m_selectOptions = CmsMacroResolver.resolveMacros(m_configuration, cms, widgetDialog.getMessages());
            }
        }
        return m_selectOptions;
    }
}