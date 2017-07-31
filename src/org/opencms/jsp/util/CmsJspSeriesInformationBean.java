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
import org.opencms.json.JSONException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.widgets.serialdate.CmsSerialDateBeanFactory;
import org.opencms.widgets.serialdate.CmsSerialDateValueWrapper;
import org.opencms.widgets.serialdate.I_CmsSerialDateBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/** Bean for easy access to information of an event series. */
public class CmsJspSeriesInformationBean {

    /** Bean for easy access to information for single events. */
    public static class CmsJspSingleEventInformationBean {

        /** Beginning of the event. */
        private Date m_start;
        /** End of the event. */
        private Date m_end;
        /** Duration of the event. */
        private long m_duration;
        /** Flag, indicating if the event lasts whole days. */
        private boolean m_isWholeDay;

        /** Constructor taking start and end time for the single event.
         * @param start the start time of the event.
         * @param duration the duration of the event.
         * @param isWholeDay a flag, indicating if the event lasts whole days.
         */
        public CmsJspSingleEventInformationBean(Date start, long duration, boolean isWholeDay) {
            m_start = start;
            m_duration = duration;
            m_isWholeDay = isWholeDay;
        }

        /**
         * Returns the end time of the event.
         * @return the end time of the event.
         */
        public Date getEnd() {

            if (null == m_end) {
                m_end = new Date(m_start.getTime() + m_duration);
            }
            return m_end;
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

            return m_isWholeDay;
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
                return new CmsJspSingleEventInformationBean((Date)d.clone(), m_duration, m_isWholeDay);
            }
            return null;
        }
    }

    /** Logger for the class. */
    private static final Log LOG = CmsLog.getLog(CmsJspSeriesInformationBean.class);

    /** Lazy map from start dates (provided as Long, long value as string or date) to informations on the single event. */
    private Map<Object, CmsJspSingleEventInformationBean> m_singleEvents;

    /** The dates of the series. */
    SortedSet<Date> m_dates;

    /** The duration of a single event. */
    long m_duration;

    /** Flag, indicating if the event lasts whole days. */
    boolean m_isWholeDay;

    /**
     * Constructor for the series information bean.
     * @param seriesDefinition string with the series definition.
     */
    public CmsJspSeriesInformationBean(String seriesDefinition) {
        try {
            CmsSerialDateValueWrapper serialDateValue = new CmsSerialDateValueWrapper(seriesDefinition);
            I_CmsSerialDateBean bean = CmsSerialDateBeanFactory.createSerialDateBean(serialDateValue);
            m_dates = bean.getDates();
            m_duration = bean.getEventDuration();
            m_isWholeDay = serialDateValue.isWholeDay();
        } catch (JSONException e) {
            LOG.error("Could not read series definition: " + seriesDefinition, e);
            m_dates = new TreeSet<>();
        }
    }

    /**
     * Returns the list of dates the event takes place.
     * @return the list of dates the event takes place.
     */
    public List<Date> getEventDates() {

        return new ArrayList<>(m_dates);
    }

    /**
     * Returns a lazy map from the start time of a single event of the series to the date information on the single event.<p>
     *
     * Start time can be provided as Long, as a String representation of the long value or as Date.<p>
     *
     * @return a lazy map from the start time of a single event of the series to the date information on the single event.
     */
    public Map<Object, CmsJspSingleEventInformationBean> getEventInfo() {

        if (m_singleEvents == null) {
            m_singleEvents = CmsCollectionsGenericWrapper.createLazyMap(new CmsSeriesSingleEventTransformer());
        }
        return m_singleEvents;
    }
}
