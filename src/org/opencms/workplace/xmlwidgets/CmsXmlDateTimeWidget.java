/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlDateTimeWidget.java,v $
 * Date   : $Date: 2005/05/10 09:24:02 $
 * Version: $Revision: 1.14 $
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

package org.opencms.workplace.xmlwidgets;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.CmsXmlDateTimeValue;

import java.text.ParseException;
import java.util.Map;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlDateTimeValue}.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.14 $
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
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return widgetDialog.calendarIncludes();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogWidget(org.opencms.file.CmsObject, I_CmsWidgetDialog, I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) throws CmsXmlException {

        StringBuffer result = new StringBuffer(16);
        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        result.append("<input class=\"xmlInputSmall\" value=\"");
        String dateTimeValue = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(dateTimeValue) && !"0".equals(dateTimeValue)) {
            try {
                dateTimeValue = widgetDialog.getCalendarLocalizedTime(Long.parseLong(dateTimeValue));
            } catch (NumberFormatException e) {
                dateTimeValue = "";    
            }
        } else {
            dateTimeValue = "";    
        }

        String id = param.getId();

        result.append(dateTimeValue);
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td>");
        result.append("<table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"");
        result.append(id);
        result.append(".calendar\"><tr>");
        result.append(widgetDialog.button(
            "#",
            null,
            "calendar",
            "calendar.input.choosedate",
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append(widgetDialog.calendarInit(id, id + ".calendar", "cR", false, false, true, null, true));

        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, I_CmsWidgetDialog, I_CmsWidgetParameter)
     */
    public void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) throws CmsXmlException {

        String[] values = (String[])formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            CmsXmlDateTimeValue castValue = (CmsXmlDateTimeValue)param;
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
            param.setStringValue(cms, String.valueOf(dateTime));
        }
    }
}