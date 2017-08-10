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

import org.opencms.main.CmsLog;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.widgets.serialdate.CmsSerialDateBeanFactory;
import org.opencms.widgets.serialdate.CmsSerialDateValue;
import org.opencms.widgets.serialdate.I_CmsSerialDateBean;

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

    /**
     * Provides information on the single event when the start date is provided.<p>
     *
     * If no valid start date is provided, the information for the first event in the series is provided.
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
            if ((null != d) && m_dates.contains(d)) {
                return new CmsJspInstanceDateBean((Date)d.clone(), CmsJspDateSeriesBean.this);
            } else {
                if (!m_dates.isEmpty()) {
                    return new CmsJspInstanceDateBean((Date)m_dates.first().clone(), CmsJspDateSeriesBean.this);
                }
            }
            return null;
        }
    }

    /** Logger for the class. */
    private static final Log LOG = CmsLog.getLog(CmsJspDateSeriesBean.class);

    /** Lazy map from start dates (provided as Long, long value as string or date) to informations on the single event. */
    private Map<Object, CmsJspInstanceDateBean> m_singleEvents;

    /** The dates of the series. */
    SortedSet<Date> m_dates;

    /** The duration of a single event. */
    Long m_duration;

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
    public Long getInstanceDuration() {

        return m_duration;
    }

    /**
     * Returns a lazy map from the start time of a single instance of the series to the date information on the single instance.<p>
     *
     * Start time can be provided as Long, as a String representation of the long value or as Date.<p>
     *
     * If no event exists for the start time, the information for the first event of the series is returned.
     *
     * @return a lazy map from the start time of a single instance of the series to the date information on the single instance.
     */
    public Map<Object, CmsJspInstanceDateBean> getInstanceInfo() {

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
