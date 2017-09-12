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
import org.opencms.acacia.shared.I_CmsSerialDateValue.DateType;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.widgets.serialdate.CmsSerialDateBeanFactory;
import org.opencms.widgets.serialdate.CmsSerialDateValue;
import org.opencms.widgets.serialdate.I_CmsSerialDateBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

import com.google.common.base.Objects;

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

    /** The series definition. */
    I_CmsSerialDateValue m_seriesDefinition;

    /** The locale to use for rendering dates. */
    private CmsJspContentAccessValueWrapper m_value;

    /** The parent series. */
    CmsJspDateSeriesBean m_parentSeries;

    /** Flag, indicating if the single events last over night. */
    private Boolean m_isMultiDay;

    /** The locale to use for displaying dates. */
    private Locale m_locale;

    /**
     * Constructor for the date series bean.
     * @param value the content value wrapper for the element that stores the series definition.
     * @param locale the locale in which dates should be rendered. This can differ from the content locale, if e.g.
     *          on a German page a content that is only present in English is rendered.
     */
    public CmsJspDateSeriesBean(CmsJspContentAccessValueWrapper value, Locale locale) {
        m_value = value;
        m_locale = null == locale ? m_value.getLocale() : locale;
        String seriesDefinitionString = value.getStringValue();
        m_seriesDefinition = new CmsSerialDateValue(seriesDefinitionString);
        if (m_seriesDefinition.isValid()) {
            I_CmsSerialDateBean bean = CmsSerialDateBeanFactory.createSerialDateBean(m_seriesDefinition);
            m_dates = bean.getDates();
            m_duration = bean.getEventDuration();
        } else {
            LOG.error("Could not read series definition: " + seriesDefinitionString);
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
     * Returns a flag, indicating if the series is extracted from another series.
     * @return a flag, indicating if the series is extracted from another series.
     */
    public boolean getIsExtractedDate() {

        return Objects.equal(m_seriesDefinition.getDateType(), DateType.EXTRACTED);
    }

    /**
     * Returns a flag, indicating if the series is defined via a pattern, i.e., not just as via single date.
     * @return a flag, indicating if the series is defined via a pattern, i.e., not just as via single date.
     */
    public boolean getIsSeries() {

        return Objects.equal(m_seriesDefinition.getDateType(), DateType.SERIES);
    }

    /**
     * Returns a flag, indicating if the series is defined by only a single date and not extracted from another series.
     * @return a flag, indicating if the series is defined by only a single date and not extracted from another series.
     */
    public boolean getIsSingleDate() {

        return Objects.equal(m_seriesDefinition.getDateType(), DateType.SINGLE);
    }

    /**
     * Returns the locale to use for rendering dates.
     * @return the locale to use for rendering dates.
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the parent series, if it is present, otherwise <code>null</code>.
     * @return the parent series, if it is present, otherwise <code>null</code>.
     */
    public CmsJspDateSeriesBean getParentSeries() {

        if ((m_parentSeries == null) && getIsExtractedDate()) {
            CmsObject cms = m_value.getCmsObject();
            try {
                CmsResource res = cms.readResource(m_seriesDefinition.getParentSeriesId());
                CmsJspContentAccessBean content = new CmsJspContentAccessBean(cms, m_value.getLocale(), res);
                CmsJspContentAccessValueWrapper value = content.getValue().get(m_value.getPath());
                return new CmsJspDateSeriesBean(value, m_locale);
            } catch (CmsException e) {
                LOG.warn("Parent series with id " + m_seriesDefinition.getParentSeriesId() + " could not be read.", e);
            }

        }
        return null;
    }

    /**
     * Returns the gallery title of the series content.
     * @return the gallery title of the series content.
     */
    public String getTitle() {

        CmsGallerySearchResult result;
        try {
            result = CmsGallerySearch.searchById(
                m_value.getCmsObject(),
                m_value.getContentValue().getDocument().getFile().getStructureId(),
                m_value.getLocale());
            return result.getTitle();
        } catch (CmsException e) {
            LOG.error("Could not retrieve title of series content.", e);
            return "";
        }

    }

    /**
     * Returns a flag, indicating if the single events last over night.
     * @return <code>true</code> if the event ends on another day than it starts, <code>false</code> if it ends on the same day.
     */
    public boolean isMultiDay() {

        if (m_isMultiDay != null) {
            return m_isMultiDay.booleanValue();
        }
        if ((null == getInstanceDuration())
            || (getInstanceDuration().longValue() > I_CmsSerialDateValue.DAY_IN_MILLIS)) {
            m_isMultiDay = Boolean.TRUE;
            return true;
        }
        if (isWholeDay() && (getInstanceDuration().longValue() <= I_CmsSerialDateValue.DAY_IN_MILLIS)) {
            m_isMultiDay = Boolean.FALSE;
            return false;
        }
        Calendar start = new GregorianCalendar();
        start.setTime(m_seriesDefinition.getStart());
        Calendar end = new GregorianCalendar();
        end.setTime(m_seriesDefinition.getEnd());
        if (start.get(Calendar.DAY_OF_MONTH) == end.get(Calendar.DAY_OF_MONTH)) {
            m_isMultiDay = Boolean.FALSE;
            return false;
        }
        m_isMultiDay = Boolean.TRUE;
        return true;
    }

    /**
     * Returns a flag, indicating if the events in the series last whole days.
     * @return a flag, indicating if the events in the series last whole days.
     */
    public boolean isWholeDay() {

        return m_seriesDefinition.isWholeDay();
    }
}
