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

package org.opencms.jsp.util;

import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/** Bean for easy access to information for single events. */
public class CmsJspInstanceDateBean {

    /** Formatting options for dates. */
    public static class CmsDateFormatOption {

        /** The date format. */
        SimpleDateFormat m_dateFormat;
        /** The time format. */
        SimpleDateFormat m_timeFormat;
        /** The date and time format. */
        SimpleDateFormat m_dateTimeFormat;

        /**
         * Create a new date format option.
         *
         * Examples (for date 19/06/82 11:17):
         * <ul>
         *   <li>"dd/MM/yy"
         *      <ul>
         *          <li>formatDate: "19/06/82"</li>
         *          <li>formatTime: ""</li>
         *          <li>formatDateTime: "19/06/82"</li>
         *      </ul>
         *   </li>
         *   <li>"dd/MM/yy|hh:mm"
         *      <ul>
         *          <li>formatDate: "19/06/82"</li>
         *          <li>formatTime: "11:17"</li>
         *          <li>formatDateTime: "19/06/82 11:17"</li>
         *      </ul>
         *   </li>
         *   <li>"dd/MM/yy|hh:mm|dd/MM/yy - hh:mm"
         *      <ul>
         *          <li>formatDate: "19/06/82"</li>
         *          <li>formatTime: "11:17"</li>
         *          <li>formatDateTime: "19/06/82 - 11:17"</li>
         *      </ul>
         *   </li>
         * @param configString the configuration string, should be structured as "datePattern|timePattern|dateTimePattern", where only datePattern is mandatory.
         * @param locale the locale to use for printing days of week, month names etc.
         * @throws IllegalArgumentException thrown if the configured patterns are invalid.
         */
        public CmsDateFormatOption(String configString, Locale locale)
        throws IllegalArgumentException {

            if (null != configString) {
                String[] config = configString.split("\\|");
                String datePattern = config[0];
                if (!datePattern.trim().isEmpty()) {
                    m_dateFormat = new SimpleDateFormat(datePattern, locale);
                }
                if (config.length > 1) {
                    String timePattern = config[1];
                    if (!timePattern.trim().isEmpty()) {
                        m_timeFormat = new SimpleDateFormat(timePattern, locale);
                    }
                    if (config.length > 2) {
                        String dateTimePattern = config[2];
                        if (!dateTimePattern.trim().isEmpty()) {
                            m_dateTimeFormat = new SimpleDateFormat(dateTimePattern, locale);
                        }
                    } else if ((null != m_dateFormat) && (null != m_timeFormat)) {
                        m_dateTimeFormat = new SimpleDateFormat(
                            m_dateFormat.toPattern() + " " + m_timeFormat.toPattern(),
                            locale);
                    }
                }
            }
        }

        /**
         * Returns the formatted date (without time).
         * @param d the {@link Date} to format.
         * @return the formatted date (without time).
         */
        String formatDate(Date d) {

            return null != m_dateFormat ? m_dateFormat.format(d) : "";
        }

        /**
         * Returns the formatted date (with time).
         * @param d the {@link Date} to format.
         * @return the formatted date (with time).
         */
        String formatDateTime(Date d) {

            return null != m_dateTimeFormat
            ? m_dateTimeFormat.format(d)
            : null != m_dateFormat ? m_dateFormat.format(d) : m_timeFormat != null ? m_timeFormat.format(d) : "";
        }

        /**
         * Returns the formatted time (without date).
         * @param d the {@link Date} to format.
         * @return the formatted time (without date).
         */
        String formatTime(Date d) {

            return null != m_timeFormat ? m_timeFormat.format(d) : "";
        }
    }

    /** Transformer from formatting options to formatted dates. */
    public class CmsDateFormatTransformer implements Transformer {

        /** The locale to use for formatting (e.g. for the names of month). */
        Locale m_locale;

        /**
         * Constructor for the date format transformer.
         * @param locale the locale to use for writing names of month or days of weeks etc.
         */
        public CmsDateFormatTransformer(Locale locale) {

            m_locale = locale;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public String transform(Object formatOption) {

            CmsDateFormatOption option = null;
            try {
                option = new CmsDateFormatOption(formatOption.toString(), m_locale);
            } catch (IllegalArgumentException e) {
                LOG.error(
                    "At least one of the provided date/time patterns are illegal. Defaulting to short default date format.",
                    e);
            }
            return getFormattedDate(option);
        }

    }

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsJspInstanceDateBean.class);

    /** The separator between start and end date to use when formatting dates. */
    private static final String DATE_SEPARATOR = " - ";

    /** Beginning of the event. */
    private Date m_start;

    /** End of the event. */
    private Date m_end;

    /** Explicitely set end of the single event. */
    private Date m_explicitEnd;

    /** Flag, indicating if the single event explicitely lasts the whole day. */
    private Boolean m_explicitWholeDay;

    /** The series the event is part of. */
    private CmsJspDateSeriesBean m_series;

    /** The dates of the event formatted locale specific in long style. */
    private String m_formatLong;

    /** The dates of the event formatted locale specific in short style. */
    private String m_formatShort;

    /** The formatted dates as lazy map. */
    private Map<String, String> m_formattedDates;

    /** Constructor taking start and end time for the single event.
     * @param start the start time of the event.
     * @param series the series, the event is part of.
     */
    public CmsJspInstanceDateBean(Date start, CmsJspDateSeriesBean series) {

        m_start = start;
        m_series = series;
    }

    /**
     * Constructor to wrap a single date as instance date.
     * This will allow to use the format options.
     *
     * @param date the date to wrap
     * @param locale the locale to use for formatting the date.
     *
     */
    public CmsJspInstanceDateBean(Date date, Locale locale) {

        this(date, new CmsJspDateSeriesBean(Long.toString(date.getTime()), locale));
    }

    /**
     * Returns the end time of the event.
     * @return the end time of the event.
     */
    public Date getEnd() {

        if (null != m_explicitEnd) {
            return isWholeDay() ? adjustForWholeDay(m_explicitEnd, true) : m_explicitEnd;
        }
        if ((null == m_end) && (m_series.getInstanceDuration() != null)) {
            m_end = new Date(m_start.getTime() + m_series.getInstanceDuration().longValue());
        }
        return isWholeDay() && !m_series.isWholeDay() ? adjustForWholeDay(m_end, true) : m_end;
    }

    /**
     * Returns a lazy map from date format options to dates.
     * Supported formats are the values of {@link CmsDateFormatOption}.<p>
     *
     * Each option must be backed up by four three keys in the message "bundle org.opencms.jsp.util.messages" for you locale:
     * GUI_PATTERN_DATE_{Option}, GUI_PATTERN_DATE_TIME_{Option} and GUI_PATTERN_TIME_{Option}.
     *
     * @return a lazy map from date patterns to dates.
     */
    public Map<String, String> getFormat() {

        if (null == m_formattedDates) {
            m_formattedDates = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsDateFormatTransformer(m_series.getLocale()));
        }
        return m_formattedDates;

    }

    /**
     * Returns the start and end dates/times as "start - end" in long date format and short time format specific for the request locale.
     * @return the formatted date/time string.
     */
    public String getFormatLong() {

        if (m_formatLong == null) {
            m_formatLong = getFormattedDate(DateFormat.LONG);
        }
        return m_formatLong;
    }

    /**
     * Returns the start and end dates/times as "start - end" in short date/time format specific for the request locale.
     * @return the formatted date/time string.
     */
    public String getFormatShort() {

        if (m_formatShort == null) {
            m_formatShort = getFormattedDate(DateFormat.SHORT);
        }
        return m_formatShort;
    }

    /**
     * Returns some time of the last day, the event takes place. </p>
     *
     * For whole day events the end date is adjusted by subtracting one day,
     * since it would otherwise be the 12 am of the first day, the event does not take place anymore.
     *
     * @return some time of the last day, the event takes place.
     */
    public Date getLastDay() {

        return isWholeDay() ? new Date(getEnd().getTime() - I_CmsSerialDateValue.DAY_IN_MILLIS) : getEnd();
    }

    /**
     * Returns the start time of the event.
     * @return the start time of the event.
     */
    public Date getStart() {

        // Adjust the start time for an explicitely whole day option that overwrites the series' whole day option.
        return isWholeDay() && !m_series.isWholeDay() ? adjustForWholeDay(m_start, false) : m_start;
    }

    /**
     * Returns a flag, indicating if the event last over night.
     * @return <code>true</code> if the event ends on another day than it starts, <code>false</code> if it ends on the same day.
     */
    public boolean isMultiDay() {

        if ((null != m_explicitEnd) || (null != m_explicitWholeDay)) {
            return isSingleMultiDay();
        } else {
            return m_series.isMultiDay();
        }
    }

    /**
     * Returns a flag, indicating if the event lasts whole days.
     * @return a flag, indicating if the event lasts whole days.
     */
    public boolean isWholeDay() {

        return null == m_explicitWholeDay ? m_series.isWholeDay() : m_explicitWholeDay.booleanValue();
    }

    /**
     * Explicitly set the end time of the event.
     *
     * If the provided date is <code>null</code> or a date before the start date, the end date defaults to the start date.
     *
     * @param endDate the end time of the event.
     */
    public void setEnd(Date endDate) {

        if ((null == endDate) || getStart().after(endDate)) {
            m_explicitEnd = null;
        } else {
            m_explicitEnd = endDate;
        }
    }

    /**
     * Explicitly set if the single event is whole day.
     *
     * @param isWholeDay flag, indicating if the single event lasts the whole day.
     *          If <code>null</code> the value defaults to the setting from the underlying date series.
     */
    public void setWholeDay(Boolean isWholeDay) {

        m_explicitWholeDay = isWholeDay;
    }

    /**
     * Returns the start and end dates/times as "start - end" in the provided date/time format specific for the request locale.
     * @param formatOption the format to use for date and time.
     * @return the formatted date/time string.
     */
    String getFormattedDate(CmsDateFormatOption formatOption) {

        if (null == formatOption) {
            return getFormattedDate(DateFormat.SHORT);
        }
        String result;
        if (isWholeDay()) {
            result = formatOption.formatDate(getStart());
            if (getLastDay().after(getStart())) {
                String to = formatOption.formatDate(getLastDay());
                if (!to.isEmpty()) {
                    result += DATE_SEPARATOR + to;
                }
            }
        } else {
            result = formatOption.formatDateTime(getStart());
            if (getEnd().after(getStart())) {
                String to;
                if (isMultiDay()) {
                    to = formatOption.formatDateTime(getEnd());
                } else {
                    to = formatOption.formatTime(getEnd());
                }
                if (!to.isEmpty()) {
                    result += DATE_SEPARATOR + to;
                }
            }
        }

        return result;
    }

    /**
     * Adjust the date according to the whole day options.
     *
     * @param date the date to adjust.
     * @param isEnd flag, indicating if the date is the end of the event (in contrast to the beginning)
     *
     * @return the adjusted date, which will be exactly the beginning or the end of the provide date's day.
     */
    private Date adjustForWholeDay(Date date, boolean isEnd) {

        Calendar result = new GregorianCalendar();
        result.setTime(date);
        result.set(Calendar.HOUR_OF_DAY, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);
        if (isEnd) {
            result.add(Calendar.DATE, 1);
        }

        return result.getTime();
    }

    /**
     * Returns the start and end dates/times as "start - end" in the provided date/time format specific for the request locale.
     * @param dateTimeFormat the format to use for date (time is always short).
     * @return the formatted date/time string.
     */
    private String getFormattedDate(int dateTimeFormat) {

        DateFormat df;
        String result;
        if (isWholeDay()) {
            df = DateFormat.getDateInstance(dateTimeFormat, m_series.getLocale());
            result = df.format(getStart());
            if (getLastDay().after(getStart())) {
                result += DATE_SEPARATOR + df.format(getLastDay());
            }
        } else {
            df = DateFormat.getDateTimeInstance(dateTimeFormat, DateFormat.SHORT, m_series.getLocale());
            result = df.format(getStart());
            if (getEnd().after(getStart())) {
                if (isMultiDay()) {
                    result += DATE_SEPARATOR + df.format(getEnd());
                } else {
                    df = DateFormat.getTimeInstance(DateFormat.SHORT, m_series.getLocale());
                    result += DATE_SEPARATOR + df.format(getEnd());
                }
            }
        }

        return result;
    }

    /**
     * Returns a flag, indicating if the current event is a multi-day event.
     * The method is only called if the single event has an explicitely set end date
     * or an explicitely changed whole day option.
     *
     * @return a flag, indicating if the current event takes lasts over more than one day.
     */
    private boolean isSingleMultiDay() {

        long duration = getEnd().getTime() - getStart().getTime();
        if (duration > I_CmsSerialDateValue.DAY_IN_MILLIS) {
            return true;
        }
        if (isWholeDay() && (duration <= I_CmsSerialDateValue.DAY_IN_MILLIS)) {
            return false;
        }
        Calendar start = new GregorianCalendar();
        start.setTime(getStart());
        Calendar end = new GregorianCalendar();
        end.setTime(getEnd());
        if (start.get(Calendar.DAY_OF_MONTH) == end.get(Calendar.DAY_OF_MONTH)) {
            return false;
        }
        return true;

    }
}