/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsMultiSelectWidget.java,v $
 * Date   : $Date: 2005/10/10 16:11:03 $
 * Version: $Revision: 1.2 $
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides a widget for a standard HTML form multi select list or a group of check boxes.<p>
 * 
 * Please see the documentation of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> for a description 
 * about the configuration String syntax for the select options.<p>
 *
 * The multi select widget does use the following select options:<ul>
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getValue()}</code> for the value of the option
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#isDefault()}</code> for pre-selecting a specific value 
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getOption()}</code> for the display name of the option
 * </ul>
 * <p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMultiSelectWidget extends A_CmsSelectWidget {

    /** Indicates if used html code is a multi selection list or a list of checkboxes. */
    private boolean m_asCheckBoxes;

    /**
     * Creates a new select widget.<p>
     */
    public CmsMultiSelectWidget() {

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
    public CmsMultiSelectWidget(List configuration) {

        this(configuration, false);
    }

    /**
     * Creates a select widget with the select options specified in the given configuration List.<p>
     * 
     * The list elements must be of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     * 
     * @param configuration the configuration (possible options) for the select widget
     * @param asCheckboxes indicates if used html code is a multi selection list or a list of checkboxes
     * 
     * @see CmsSelectWidgetOption
     */
    public CmsMultiSelectWidget(List configuration, boolean asCheckboxes) {

        super(configuration);
        m_asCheckBoxes = asCheckboxes;
    }

    /**
     * Creates a select widget with the specified select options.<p>
     * 
     * @param configuration the configuration (possible options) for the select box
     */
    public CmsMultiSelectWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = (String[])formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            StringBuffer value = new StringBuffer(128);
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    value.append(',');
                }
                value.append(values[i]);
            }
            // set the value
            param.setStringValue(cms, value.toString());
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);

        List options = parseSelectOptions(cms, widgetDialog, param);
        result.append("<td class=\"xmlTd\">");
        if (!m_asCheckBoxes) {
            result.append("<select multiple size='");
            result.append(options.size());
            result.append("' class=\"xmlInput");
            if (param.hasError()) {
                result.append(" xmlInputError");
            }
            result.append("\" name=\"");
            result.append(id);
            result.append("\" id=\"");
            result.append(id);
            result.append("\">");
        }

        // get select box options from default value String
        List selected = getSelectedValues(cms, param);
        Iterator i = options.iterator();
        while (i.hasNext()) {
            CmsSelectWidgetOption option = (CmsSelectWidgetOption)i.next();
            // create the option
            if (!m_asCheckBoxes) {
                result.append("<option value=\"");
                result.append(option.getValue());
                result.append("\"");
                if (selected.contains(option.getValue())) {
                    result.append(" selected=\"selected\"");
                }
                result.append(">");
                result.append(option.getOption());
                result.append("</option>");
            } else {
                result.append("<input type='checkbox' name='");
                result.append(id);
                result.append("' value='");
                result.append(option.getValue());
                result.append("'");
                if (selected.contains(option.getValue())) {
                    result.append(" checked");
                }
                result.append(">");
                result.append(option.getOption());
                result.append("<br>");
            }
        }
        if (!m_asCheckBoxes) {
            result.append("</select>");
        }
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsMultiSelectWidget(getConfiguration());
    }
}