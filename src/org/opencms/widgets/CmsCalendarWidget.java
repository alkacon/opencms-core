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
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;

/**
 * Provides a DHTML calendar widget, for use on a widget dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsCalendarWidget extends A_CmsWidget implements I_CmsADEWidget {

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
     * Creates the HTML JavaScript and stylesheet includes required by the calendar for the head of the page.<p>
     *
     * The default <code>"opencms"</code> style is used.<p>
     *
     * @param locale the locale to use for the calendar
     *
     * @return the necessary HTML code for the js and stylesheet includes
     *
     * @see #calendarIncludes(Locale, String)
     */
    public static String calendarIncludes(Locale locale) {

        return calendarIncludes(locale, "opencms");
    }

    /**
     * Creates the HTML JavaScript and stylesheet includes required by the calendar for the head of the page.<p>
     *
     * @param locale the locale to use for the calendar
     * @param style the name of the used calendar style, e.g. "system", "blue"
     *
     * @return the necessary HTML code for the js and stylesheet includes
     */
    public static String calendarIncludes(Locale locale, String style) {

        StringBuffer result = new StringBuffer(512);
        String calendarPath = CmsWorkplace.getSkinUri() + "components/js_calendar/";
        if (CmsStringUtil.isEmpty(style)) {
            style = "system";
        }
        result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        result.append(calendarPath);
        result.append("calendar-");
        result.append(style);
        result.append(".css\">\n");
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(calendarPath);
        result.append("calendar.js\"></script>\n");
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(calendarPath);
        result.append("lang/calendar-");
        result.append(getLanguageSuffix(locale.getLanguage()));
        result.append(".js\"></script>\n");
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(calendarPath);
        result.append("calendar-setup.js\"></script>\n");
        return result.toString();
    }

    /**
     * Generates the HTML to initialize the JavaScript calendar element on the end of a page.<p>
     *
     * This method must be called at the end of a HTML page, e.g. before the closing &lt;body&gt; tag.<p>
     *
     * @param messages the messages to use (for date and time formats)
     * @param inputFieldId the ID of the input field where the date is pasted to
     * @param triggerButtonId the ID of the button which triggers the calendar
     * @param align initial position of the calendar popup element
     * @param singleClick if true, a single click selects a date and closes the calendar, otherwise calendar is closed by doubleclick
     * @param weekNumbers show the week numbers in the calendar or not
     * @param mondayFirst show monday as first day of week
     * @param dateStatusFunc name of the function which determines if/how a date should be disabled
     * @param showTime true if the time selector should be shown, otherwise false
     * @return the HTML code to initialize a calendar poup element
     */
    public static String calendarInit(
        CmsMessages messages,
        String inputFieldId,
        String triggerButtonId,
        String align,
        boolean singleClick,
        boolean weekNumbers,
        boolean mondayFirst,
        String dateStatusFunc,
        boolean showTime) {

        StringBuffer result = new StringBuffer(512);
        if (CmsStringUtil.isEmpty(align)) {
            align = "Bc";
        }
        result.append("<script type=\"text/javascript\">\n");
        result.append("<!--\n");
        result.append("\tCalendar.setup({\n");
        result.append("\t\tinputField     :    \"");
        result.append(inputFieldId);
        result.append("\",\n");
        result.append("\t\tifFormat       :    \"");
        result.append(messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0));
        if (showTime) {
            result.append(" ");
            result.append(messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIME_FORMAT_0));
        }
        result.append("\",\n");
        result.append("\t\tbutton         :    \"");
        result.append(triggerButtonId);
        result.append("\",\n");
        result.append("\t\talign          :    \"");
        result.append(align);
        result.append("\",\n");
        result.append("\t\tsingleClick    :    ");
        result.append(singleClick);
        result.append(",\n");
        result.append("\t\tweekNumbers    :    ");
        result.append(weekNumbers);
        result.append(",\n");
        result.append("\t\tmondayFirst    :    ");
        result.append(mondayFirst);
        result.append(",\n");
        result.append("\t\tshowsTime      :    " + showTime);
        if (showTime
            && (messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIMEFORMAT_0).toLowerCase().indexOf(
                "p") != -1)) {
            result.append(",\n\t\ttimeFormat     :    \"12\"");
        }
        if (CmsStringUtil.isNotEmpty(dateStatusFunc)) {
            result.append(",\n\t\tdateStatusFunc :    ");
            result.append(dateStatusFunc);
        }
        result.append("\n\t});\n");

        result.append("//-->\n");
        result.append("</script>\n");
        return result.toString();
    }

    /**
     * Creates the time in milliseconds from the given parameter.<p>
     *
     * @param messages the messages that contain the time format definitions
     * @param dateString the String representation of the date
     * @param useTime true if the time should be parsed, too, otherwise false
     *
     * @return the time in milliseconds
     *
     * @throws ParseException if something goes wrong
     */
    public static long getCalendarDate(CmsMessages messages, String dateString, boolean useTime) throws ParseException {

        long dateLong = 0;

        // substitute some chars because calendar syntax != DateFormat syntax
        String dateFormat = messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0);
        if (useTime) {
            dateFormat += " " + messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIME_FORMAT_0);
        }
        dateFormat = CmsCalendarWidget.getCalendarJavaDateFormat(dateFormat);

        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        dateLong = df.parse(dateString).getTime();
        return dateLong;
    }

    /**
     * Parses the JavaScript calendar date format to the java patterns of SimpleDateFormat.<p>
     *
     * @param dateFormat the dateformat String of the JS calendar
     * @return the parsed SimpleDateFormat pattern String
     */
    public static String getCalendarJavaDateFormat(String dateFormat) {

        dateFormat = CmsStringUtil.substitute(dateFormat, "%", ""); // remove all "%"
        dateFormat = CmsStringUtil.substitute(dateFormat, "m", "${month}");
        dateFormat = CmsStringUtil.substitute(dateFormat, "H", "${hour}");
        dateFormat = CmsStringUtil.substitute(dateFormat, "Y", "${4anno}");
        dateFormat = dateFormat.toLowerCase();
        dateFormat = CmsStringUtil.substitute(dateFormat, "${month}", "M");
        dateFormat = CmsStringUtil.substitute(dateFormat, "${hour}", "H");
        dateFormat = CmsStringUtil.substitute(dateFormat, "y", "yy");
        dateFormat = CmsStringUtil.substitute(dateFormat, "${4anno}", "yyyy");
        dateFormat = CmsStringUtil.substitute(dateFormat, "m", "mm"); // minutes with two digits
        dateFormat = dateFormat.replace('e', 'd'); // day of month
        dateFormat = dateFormat.replace('i', 'h'); // 12 hour format
        dateFormat = dateFormat.replace('p', 'a'); // pm/am String
        return dateFormat;
    }

    /**
     * Returns the given timestamp as String formatted in a localized pattern.<p>
     *
     * @param locale the locale for the time format
     * @param messages the messages that contain the time format definitions
     * @param timestamp the time to format
     *
     * @return the given timestamp as String formatted in a localized pattern
     */
    public static String getCalendarLocalizedTime(Locale locale, CmsMessages messages, long timestamp) {

        // get the current date & time
        TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(timestamp);
        // format it nicely according to the localized pattern
        DateFormat df = new SimpleDateFormat(
            CmsCalendarWidget.getCalendarJavaDateFormat(
                messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0)
                    + " "
                    + messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIME_FORMAT_0)));
        return df.format(cal.getTime());
    }

    /**
     * Returns the language suffix for the calendar-*.js localizations.<p>
     *
     * @param language the language from the locale
     *
     * @return the suffix to use for the calendar-*js localication file
     */
    private static String getLanguageSuffix(String language) {

        if (language.equals(Locale.JAPANESE.getLanguage())) {
            return "jp";
        } else {
            return language;
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        return getConfiguration();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.singleline;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return calendarIncludes(widgetDialog.getLocale());
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
        String dateTimeValue = getWidgetStringValue(cms, widgetDialog, param);
        result.append(dateTimeValue);
        String id = param.getId();
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
        result.append(
            widgetDialog.button(
                "#",
                null,
                "calendar",
                org.opencms.workplace.Messages.GUI_CALENDAR_CHOOSE_DATE_0,
                widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append(
            calendarInit(widgetDialog.getMessages(), id, id + ".calendar", "cR", false, false, true, null, true));

        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsCalendarWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getWidgetStringValue(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public String getWidgetStringValue(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String result = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !"0".equals(result)) {
            try {
                result = getCalendarLocalizedTime(
                    widgetDialog.getLocale(),
                    widgetDialog.getMessages(),
                    Long.parseLong(result));
            } catch (NumberFormatException e) {
                if (!CmsMacroResolver.isMacro(result, CmsMacroResolver.KEY_CURRENT_TIME)) {
                    // neither long nor macro, show empty value
                    result = "";
                }
            }
        } else {
            result = "";
        }
        return result;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return true;
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
    @Override
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String dateTimeValue = values[0].trim();
            if (CmsMacroResolver.isMacro(dateTimeValue, CmsMacroResolver.KEY_CURRENT_TIME)) {
                // a macro is used, redisplay it
                param.setStringValue(cms, dateTimeValue);
            } else {
                // a date value should be used
                long dateTime;
                try {
                    dateTime = Long.valueOf(param.getStringValue(cms)).longValue();
                } catch (NumberFormatException e) {
                    dateTime = 0;
                }
                if (CmsStringUtil.isNotEmpty(dateTimeValue)) {
                    try {
                        dateTime = getCalendarDate(widgetDialog.getMessages(), dateTimeValue, true);
                    } catch (ParseException e) {
                        // TODO: Better exception handling
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().getBundle().key(Messages.ERR_PARSE_DATETIME_1, dateTimeValue), e);
                        }
                    }
                } else {
                    dateTime = 0;
                }
                param.setStringValue(cms, String.valueOf(dateTime));
            }
        }
    }
}