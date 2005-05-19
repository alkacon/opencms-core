/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsCheckboxWidget.java,v $
 * Date   : $Date: 2005/05/19 09:35:16 $
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
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.types.CmsXmlBooleanValue;

import java.util.Map;

/**
 * Provides a standard HTML form checkbox widget, for use on a widget dialog.<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.5.2
 */
public class CmsCheckboxWidget extends A_CmsWidget {

    /**
     * Creates a new editor widget.<p>
     */
    public CmsCheckboxWidget() {

        // empty constructor is required for class registration
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(16);

        result.append("<td class=\"xmlTd\">");
        result.append("<input type=\"checkbox\" name=\"");
        result.append(param.getId());
        result.append("\" value=\"true\"");
        boolean booleanValue = CmsXmlBooleanValue.getBooleanValue(cms, param);
        if (booleanValue) {
            result.append(" checked=\"checked\"");
        }
        result.append(">");
        result.append("<input type=\"hidden\" name=\"");
        result.append(param.getId());
        result.append(HIDDEN_SUFFIX);
        result.append("\" value=\"");
        result.append(booleanValue);
        result.append("\">");
        result.append("</td>");

        return result.toString();
    }
    
    /** Suffix for the hidden input that contains the original value. */
    public static final String HIDDEN_SUFFIX = ".value";

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

            // first get the current boolean value for the element
            boolean booleanValue = CmsXmlBooleanValue.getBooleanValue(cms, param);

            // now check if there's a new value in the form parameters
            String formValue = values[0].trim();
            if (CmsStringUtil.isNotEmpty(formValue)) {
                booleanValue = Boolean.valueOf(formValue).booleanValue();
            }

            // set the value
            param.setStringValue(cms, String.valueOf(booleanValue));

        } else {
            String value;
            values = (String[])formParameters.get(param.getId() + HIDDEN_SUFFIX);
            if ((values != null) && (values.length > 0)) {
                // found hidden value, so checkbox was not checked
                value = Boolean.FALSE.toString();
            } else {
                // no hidden value found, use default value
                value = param.getDefault(cms);
            }            
            param.setStringValue(cms, value);
        }
    }
}