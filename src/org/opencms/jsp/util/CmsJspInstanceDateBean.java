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
import org.opencms.i18n.CmsMessages;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/** Bean for easy access to information for single events. */
public class CmsJspInstanceDateBean {

    /** Formatting options for dates. */
    public static enum CmsDateFormatOption {
        /** Short date and time format. */
        SHORT,
        /** Short date and time format with weekday. */
        SHORT_DAY,
        /** Long date and short time format. */
        LONG,
        /** Long date and short time format with weekday. */
        LONG_DAY
    }

    /** Transformer from formatting options to formatted dates. */
    public class CmsDateFormatTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public String transform(Object formatOption) {

            CmsDateFormatOption option = null;
            try {
                option = CmsDateFormatOption.valueOf(formatOption.toString());
            } catch (@SuppressWarnings("unused") Exception e) {
                // Use default option
                option = CmsDateFormatOption.SHORT;
            }
            return getFormattedDate(option);
        }

    }

    /** The separator between start and end date to use when formatting dates. */
    private static final String DATE_SEPARATOR = " - ";

    /** Message key prefix for time patterns. */
    private static final String PATTERN_PREFIX_TIME = "GUI_PATTERN_TIME_";

    /** Message key prefix for date patterns. */
    private static final String PATTERN_PREFIX_DATE = "GUI_PATTERN_DATE_";

    /** Message key prefix for date and time patterns. */
    private static final String PATTERN_PREFIX_DATE_TIME = "GUI_PATTERN_DATE_TIME_";

    /** Beginning of the event. */
    private Date m_start;

    /** End of the event. */
    private Date m_end;

    /** The series the event is part of. */
    private CmsJspDateSeriesBean m_series;

    /** The dates of the event formatted locale specific in long style. */
    private String m_formatLong;

    /** The dates of the event formatted locale specific in short style. */
    private String m_formatShort;

    /** The formatted dates as lazy map. */
    private Map<String, String> m_formattedDates;

    /** The message bundle to use for the date patterns. */
    private org.opencms.i18n.CmsMessages m_messages;

    /** Constructor taking start and end time for the single event.
     * @param start the start time of the event.
     * @param series the series, the event is part of.
     */
    public CmsJspInstanceDateBean(Date start, CmsJspDateSeriesBean series) {
        m_start = start;
        m_series = series;
    }

    /**
     * Returns the end time of the event.
     * @return the end time of the event.
     */
    public Date getEnd() {

        if ((null == m_end) && (m_series.getInstanceDuration() != null)) {
            m_end = new Date(m_start.getTime() + m_series.getInstanceDuration().longValue());
        }
        return m_end;
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
            m_formattedDates = CmsCollectionsGenericWrapper.createLazyMap(new CmsDateFormatTransformer());
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

        return m_start;
    }

    /**
     * Returns a flag, indicating if the event last over night.
     * @return <code>true</code> if the event ends on another day than it starts, <code>false</code> if it ends on the same day.
     */
    public boolean isMultiDay() {

        return m_series.isMultiDay();
    }

    /**
     * Returns a flag, indicating if the event lasts whole days.
     * @return a flag, indicating if the event lasts whole days.
     */
    public boolean isWholeDay() {

        return m_series.isWholeDay();
    }

    /**
     * Returns the start and end dates/times as "start - end" in the provided date/time format specific for the request locale.
     * @param formatOption the format to use for date and time.
     * @return the formatted date/time string.
     */
    String getFormattedDate(CmsDateFormatOption formatOption) {

        DateFormat df;
        String result;
        if (isWholeDay()) {
            df = getDateFormat(formatOption);
            result = df.format(getStart());
            if (getLastDay().after(getStart())) {
                result += DATE_SEPARATOR + df.format(getLastDay());
            }
        } else {
            df = getDateTimeFormat(formatOption);
            result = df.format(getStart());
            if (getEnd().after(getStart())) {
                if (isMultiDay()) {
                    result += DATE_SEPARATOR + df.format(getEnd());
                } else {
                    df = getTimeFormat(formatOption);
                    result += DATE_SEPARATOR + df.format(getEnd());
                }
            }
        }

        return result;
    }

    /**
     * Returns the correct (locale specific) date format for the provided formatting option.
     * @param option the formatting option for the date.
     * @return date format for the provided formatting option.
     */
    private DateFormat getDateFormat(CmsDateFormatOption option) {

        return new SimpleDateFormat(getMessages().key(PATTERN_PREFIX_DATE + option), m_series.getLocale());
    }

    /**
     * Returns the correct (locale specific) date and time format for the provided formatting option.
     * @param option the formatting option for the date and time.
     * @return date and time format for the provided formatting option.
     */
    private DateFormat getDateTimeFormat(CmsDateFormatOption option) {

        return new SimpleDateFormat(getMessages().key(PATTERN_PREFIX_DATE_TIME + option), m_series.getLocale());
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
     * Returns the messages to use for formatting dates.
     * @return the messages to use for formatting dates.
     */
    private CmsMessages getMessages() {

        if (null == m_messages) {
            m_messages = Messages.get().getBundle(m_series.getLocale());
        }
        return m_messages;
    }

    /**
     * Returns the correct (locale specific) time format for the provided formatting option.
     * @param option the formatting option for the time.
     * @return time format for the provided formatting option.
     */
    private DateFormat getTimeFormat(CmsDateFormatOption option) {

        return new SimpleDateFormat(getMessages().key(PATTERN_PREFIX_TIME + option), m_series.getLocale());
    }
}