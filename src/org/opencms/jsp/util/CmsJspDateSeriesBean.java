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
import org.opencms.widgets.serialdate.CmsSerialDateBeanFactory;
import org.opencms.widgets.serialdate.CmsSerialDateValue;
import org.opencms.widgets.serialdate.I_CmsSerialDateBean;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/** Bean for easy access to information of an event series. */
public class CmsJspDateSeriesBean {

    /** Bean for easy access to information for single events. */
    public static class CmsJspSeriesEventBean {

        /** The separator between start and end date to use when formatting dates. */
        private static final String DATE_SEPARATOR = " - ";

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

        /** Constructor taking start and end time for the single event.
         * @param start the start time of the event.
         * @param series the series, the event is part of.
         */
        public CmsJspSeriesEventBean(Date start, CmsJspDateSeriesBean series) {
            m_start = start;
            m_series = series;
        }

        /**
         * Returns the end time of the event.
         * @return the end time of the event.
         */
        public Date getEnd() {

            if (null == m_end) {
                m_end = new Date(m_start.getTime() + m_series.getInstanceDuration());
            }
            return m_end;
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
         * Returns a flag, indicating if the event lasts whole days.
         * @return a flag, indicating if the event lasts whole days.
         */
        public boolean isWholeDay() {

            return m_series.isWholeDay();
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
                    result += DATE_SEPARATOR + df.format(getEnd());
                }
            }

            return result;
        }
    }

    /**
     * Provides information on the single event when the start date is provided.<p>
     */
    public class CmsSeriesSingleEventTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object date) {

            Date d = null;
            if (null != date) {
                if (date instanceof Date) {
                    d = (Date)date;
                } else if (date instanceof Long) {
                    d = new Date(((Long)date).longValue());
                } else {
                    try {
                        long l = Long.parseLong(date.toString());
                        d = new Date(l);
                    } catch (@SuppressWarnings("unused") Exception e) {
                        // do nothing, just let d remain null
                    }
                }
            }
            if ((d != null) && m_dates.contains(d)) {
                return new CmsJspSeriesEventBean((Date)d.clone(), CmsJspDateSeriesBean.this);
            }
            return null;
        }
    }

    /** Logger for the class. */
    private static final Log LOG = CmsLog.getLog(CmsJspDateSeriesBean.class);

    /** Lazy map from start dates (provided as Long, long value as string or date) to informations on the single event. */
    private Map<Object, CmsJspSeriesEventBean> m_singleEvents;

    /** The dates of the series. */
    SortedSet<Date> m_dates;

    /** The duration of a single event. */
    long m_duration;

    /** Flag, indicating if the event lasts whole days. */
    boolean m_isWholeDay;

    /** The locale to use for rendering dates. */
    private Locale m_locale;

    /**
     * Constructor for the series information bean.
     * @param seriesDefinition string with the series definition.
     * @param locale the locale to use for rendering dates.
     */
    public CmsJspDateSeriesBean(String seriesDefinition, Locale locale) {
        CmsSerialDateValue serialDateValue = new CmsSerialDateValue(seriesDefinition);
        if (serialDateValue.isValid()) {
            I_CmsSerialDateBean bean = CmsSerialDateBeanFactory.createSerialDateBean(serialDateValue);
            m_dates = bean.getDates();
            m_duration = bean.getEventDuration();
            m_isWholeDay = serialDateValue.isWholeDay();
            m_locale = locale;
        } else {
            LOG.error("Could not read series definition: " + seriesDefinition);
            m_dates = new TreeSet<>();
        }
    }

    /**
     * Returns the list of start dates for all instances of the series.
     * @return the list of start dates for all instances of the series.
     */
    public List<Date> getDates() {

        return new ArrayList<>(m_dates);
    }

    /**
     * Returns the duration of a single instance in milliseconds.
     * @return the duration of a single instance in milliseconds.
     */
    public long getInstanceDuration() {

        return m_duration;
    }

    /**
     * Returns a lazy map from the start time of a single instance of the series to the date information on the single instance.<p>
     *
     * Start time can be provided as Long, as a String representation of the long value or as Date.<p>
     *
     * @return a lazy map from the start time of a single instance of the series to the date information on the single instance.
     */
    public Map<Object, CmsJspSeriesEventBean> getInstanceInfo() {

        if (m_singleEvents == null) {
            m_singleEvents = CmsCollectionsGenericWrapper.createLazyMap(new CmsSeriesSingleEventTransformer());
        }
        return m_singleEvents;
    }

    /**
     * Returns the locale to use for rendering dates.
     * @return the locale to use for rendering dates.
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns a flag, indicating if the events in the series last whole days.
     * @return a flag, indicating if the events in the series last whole days.
     */
    public boolean isWholeDay() {

        return m_isWholeDay;
    }
}
