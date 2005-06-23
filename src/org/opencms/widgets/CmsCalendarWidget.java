/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsCalendarWidget.java,v $
 * Date   : $Date: 2005/06/23 11:11:23 $
 * Version: $Revision: 1.10 $
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
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.text.ParseException;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Provides a DHTML calendar widget, for use on a widget dialog.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsCalendarWidget extends A_CmsWidget {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCalendarWidget.class);

    /**
     * Creates a new calendar widget.<p>
     */
    public CmsCalendarWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new calendar widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsCalendarWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return widgetDialog.calendarIncludes();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(16);
        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        result.append("<input class=\"xmlInputSmall");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
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
            org.opencms.workplace.Messages.GUI_CALENDAR_CHOOSE_DATE_0,
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append(widgetDialog.calendarInit(id, id + ".calendar", "cR", false, false, true, null, true));

        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsCalendarWidget(getConfiguration());
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
            long dateTime;
            try {
                dateTime = Long.valueOf(param.getStringValue(cms)).longValue();
            } catch (NumberFormatException e) {
                dateTime = 0;
            }
            String dateTimeValue = values[0].trim();
            if (CmsStringUtil.isNotEmpty(dateTimeValue)) {
                try {
                    dateTime = widgetDialog.getCalendarDate(dateTimeValue, true);
                } catch (ParseException e) {
                    // TODO: Better exception handling
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().container(Messages.ERR_PARSE_DATETIME_1, dateTimeValue), e);
                    }
                }
            } else {
                dateTime = 0;
            }
            param.setStringValue(cms, String.valueOf(dateTime));
        }
    }
}