/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlDateTimeWidget.java,v $
 * Date   : $Date: 2004/12/01 12:01:20 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.xmlwidgets;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.CmsXmlDateTimeValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.text.ParseException;
import java.util.Map;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlDateTimeValue}.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.10 $
 * @since 5.5.0
 */
public class CmsXmlDateTimeWidget extends A_CmsXmlWidget {

    /**
     * Creates a new editor widget.<p>
     */
    public CmsXmlDateTimeWidget() {

        // empty constructor is required for class registration
    }
    
    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.CmsXmlContentDefinition)
     */
    public String getDialogIncludes(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        CmsXmlContentDefinition contentDefinition) {
  
        return widgetDialog.calendarIncludes();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getDialogWidget(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlContentValue value) {

        CmsXmlDateTimeValue castValue = (CmsXmlDateTimeValue)value;

        StringBuffer result = new StringBuffer(128);
        result.append("<tr><td class=\"xmlLabel\">");        
        result.append(getMessage(widgetDialog, value.getDocument().getContentDefinition(), value.getElementName()));
        result.append(": </td>");
        result.append(getHelpBubble(cms, widgetDialog, value.getDocument().getContentDefinition(), value.getElementName()));
        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>"); 
        result.append("<input class=\"xmlInputSmall\" value=\"");
        String dateTimeValue = "";
        if (castValue.getDateTimeValue() > 0) {
            dateTimeValue = widgetDialog.getCalendarLocalizedTime(castValue.getDateTimeValue());
        }

        String id = getParameterName(value);

        result.append(dateTimeValue);
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td><td>");     
        
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"");
        result.append(id);
        result.append(".calendar\"><tr>");
        result.append(widgetDialog.buttonBarSpacer(1));
        result.append(widgetDialog.button("#", null, "calendar", "calendar.input.choosedate", widgetDialog.getSettings().getUserSettings().getEditorButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");
        
        result.append(widgetDialog.calendarInit(id, id + ".calendar", "cR", false, false, true, null, true));

        result.append("</td></tr>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlContentValue value) throws CmsXmlException {

        String[] values = (String[])formParameters.get(getParameterName(value));
        if ((values != null) && (values.length > 0)) {
            CmsXmlDateTimeValue castValue = (CmsXmlDateTimeValue)value;
            long dateTime = castValue.getDateTimeValue();
            String dateTimeValue = values[0].trim();
            if (CmsStringUtil.isNotEmpty(dateTimeValue)) {
                try {
                    dateTime = widgetDialog.getCalendarDate(dateTimeValue, true);
                } catch (ParseException e) {
                    // TODO: Better exception handling
                    if (OpenCms.getLog(this).isWarnEnabled()) {
                        OpenCms.getLog(this).warn("Error parsing calendar DateTime String", e);
                    }
                }
            } else {
                dateTime = 0;
            }
            value.setStringValue(cms, String.valueOf(dateTime));
        }
    }
}