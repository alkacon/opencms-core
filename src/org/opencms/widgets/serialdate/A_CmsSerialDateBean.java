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

package org.opencms.widgets.serialdate;

import org.opencms.acacia.shared.CmsSerialDateUtil;
import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SortedSet;
import java.util.TreeSet;

/** Abstract base class for serial date beans.
 * It deals with information common for all serial dates and already provides part of the implementation
 * for calculating all dates of the series.
 */
public abstract class A_CmsSerialDateBean implements I_CmsSerialDateBean {

    /** The maximal number of occurrences that is allowed. */
    public static final int MAX_OCCURRENCES = 100;
    /** The start date and time of the (potentially) first event of the series. */
    protected Calendar m_startDate;
    /** The end date and time of the (potentially) first event of the series. */
    protected Calendar m_endDate;
    /** The maximal number of occurrences of the event. */
    protected int m_occurrences;
    /** The date of the last day, the event should occur. */
    protected Calendar m_serialEndDate;
    /** The exact time the event should occur latest (in milliseconds). */
    protected long m_endMillis;
    /** The end type of the series. */
    protected EndType m_endType = null;
    /** Variable for caching the dates of the event after lazy calculation. */
    protected SortedSet<Date> m_dates;
    /** Variable for caching the dates of the event after lazy calculation. */
    protected SortedSet<Date> m_allDates;
    /** Variable for caching the dates as long. */
    protected SortedSet<Long> m_datesInMillis;
    /** The list of exceptions. */
    protected final SortedSet<Date> m_exceptions = new TreeSet<>();
    /** A flag, indicating if the configuration specifies too many occurrences. */
    private Boolean m_hasTooManyOccurrences;

    /** Constructor for the abstract class for serial date beans.
     * It takes all the arguments that are common for serial dates and should be called from each sub-class.
     *
     * @param startDate the start date of the series as provided by the serial date widget.
     * @param endDate the end date of the series as provided by the serial date widget.
     * @param isWholeDay flag, indicating if the event lasts the whole day.
     * @param endType the end type of the series as provided by the serial date widget.
     * @param serialEndDate the end date of the series as provided by the serial date widget.
     * @param occurrences the maximal number of occurrences of the event as provided by the serial date widget.
     *        If endType is DATE, this parameter is ignored.
     * @param exceptions the dates not part of the list.
     */
    public A_CmsSerialDateBean(
        Date startDate,
        Date endDate,
        boolean isWholeDay,
        EndType endType,
        Date serialEndDate,
        int occurrences,
        SortedSet<Date> exceptions) {

        m_startDate = new GregorianCalendar();
        m_endDate = new GregorianCalendar();
        m_startDate.setTime(startDate);
        m_endDate.setTime(endDate == null ? startDate : endDate);
        if (isWholeDay) {
            m_startDate.set(Calendar.HOUR_OF_DAY, 0);
            m_startDate.set(Calendar.MINUTE, 0);
            m_startDate.set(Calendar.SECOND, 0);
            m_startDate.set(Calendar.MILLISECOND, 0);
            m_endDate.set(Calendar.HOUR_OF_DAY, 0);
            m_endDate.set(Calendar.MINUTE, 0);
            m_endDate.set(Calendar.SECOND, 0);
            m_endDate.set(Calendar.MILLISECOND, 0);
            m_endDate.add(Calendar.DATE, 1);
        }
        m_endType = endType;
        switch (m_endType) {
            case DATE:
                m_serialEndDate = new GregorianCalendar();
                m_serialEndDate.setTime(serialEndDate);
                Calendar dayAfterEnd = new GregorianCalendar(
                    m_serialEndDate.get(Calendar.YEAR),
                    m_serialEndDate.get(Calendar.MONTH),
                    m_serialEndDate.get(Calendar.DATE));
                dayAfterEnd.add(Calendar.DATE, 1);
                m_endMillis = dayAfterEnd.getTimeInMillis();
                break;
            case TIMES:
                m_occurrences = occurrences;
                break;
            case SINGLE:
                m_occurrences = 1;
                break;
            default:
                break;
        }
        if (null != exceptions) {
            m_exceptions.addAll(exceptions);
        }
    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#getDates()
     */
    @Override
    public SortedSet<Date> getDates() {

        if (null == m_dates) {
            m_dates = filterExceptions(calculateDates());
        }
        return m_dates;
    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#getDatesAsLong()
     */
    @Override
    public SortedSet<Long> getDatesAsLong() {

        if (null == m_datesInMillis) {
            SortedSet<Date> dates = getDates();
            m_datesInMillis = new TreeSet<>();
            for (Date d : dates) {
                m_datesInMillis.add(Long.valueOf(d.getTime()));
            }
        }
        return m_datesInMillis;
    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#getEventDuration()
     */
    @Override
    public Long getEventDuration() {

        return (null != m_endDate) && (null != m_startDate)
        ? Long.valueOf(m_endDate.getTimeInMillis() - m_startDate.getTimeInMillis())
        : null;
    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#getExceptions()
     */
    @Override
    public SortedSet<Date> getExceptions() {

        return m_exceptions;
    }

    /**
     * Returns the occurrences of a defined series interval, used for the series end type.<p>
     *
     * @return the occurrences of a defined series interval, used for the series end type
     */
    public int getOccurrences() {

        return Math.min(m_occurrences, CmsSerialDateUtil.getMaxEvents());
    }

    /**
     * Returns the serial end date if the series is of type: ending at specific date.<p>
     *
     * @return the serial end date if the series is of type: ending at specific date
     */
    public Calendar getSerialEndDate() {

        return m_serialEndDate;
    }

    /**
     * Returns the end type of the date series (never, n times, specific date).<p>
     *
     * @return the end type of the date series
     */
    public EndType getSerialEndType() {

        return m_endType;
    }

    /**
     * Returns the date provided as the earliest date the event should take place.
     * The time is set to the starting time of the event.
     *
     * @return date where the event should take place earliest with time set to the date's starting time.
     */
    public Calendar getStartDate() {

        return m_startDate;
    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#hasTooManyDates()
     */
    @Override
    public boolean hasTooManyDates() {

        if (null == m_hasTooManyOccurrences) {
            switch (getSerialEndType()) {
                case SINGLE:
                    m_hasTooManyOccurrences = Boolean.FALSE;
                    break;
                case TIMES:
                    m_hasTooManyOccurrences = Boolean.valueOf(m_occurrences > CmsSerialDateUtil.getMaxEvents());
                    break;
                case DATE:
                    m_hasTooManyOccurrences = Boolean.FALSE;
                    calculateDates(); // this will set the value automatically to TRUE in case there are too many dates.
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        return m_hasTooManyOccurrences.booleanValue();
    }

    /**
     * Generates the first date of the series.
     *
     * @return the first date of the series.
     */
    abstract protected Calendar getFirstDate();

    /**
     * Check, if the series can have at least one event/date.
     * @return <code>true</code> if the series can be non-empty, <code>false</code> otherwise.
     */
    abstract protected boolean isAnyDatePossible();

    /**
     * Check if the provided date or any date after it are part of the series.
     * @param nextDate the current date to check.
     * @param previousOccurrences the number of events of the series that took place before the date to check.
     * @return <code>true</code> if more dates (including the provided one) could be in the series, <code>false</code> otherwise.
     */
    protected boolean showMoreEntries(Calendar nextDate, int previousOccurrences) {

        switch (getSerialEndType()) {
            case DATE:
                boolean moreByDate = nextDate.getTimeInMillis() < m_endMillis;
                boolean moreByOccurrences = previousOccurrences < CmsSerialDateUtil.getMaxEvents();
                if (moreByDate && !moreByOccurrences) {
                    m_hasTooManyOccurrences = Boolean.TRUE;
                }
                return moreByDate && moreByOccurrences;
            case TIMES:
            case SINGLE:
                return previousOccurrences < getOccurrences();
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Starting with a date that's in the series, the next date is created.
     * @param date the current event date for a event in the series, which is adjusted to the next date potentially in the series.
     */
    abstract protected void toNextDate(Calendar date);

    /**
     * Calculates all dates of the series.
     * @return all dates of the series in milliseconds.
     */
    private SortedSet<Date> calculateDates() {

        if (null == m_allDates) {
            SortedSet<Date> result = new TreeSet<>();
            if (isAnyDatePossible()) {
                Calendar date = getFirstDate();
                int previousOccurrences = 0;
                while (showMoreEntries(date, previousOccurrences)) {
                    result.add(date.getTime());
                    toNextDate(date);
                    previousOccurrences++;
                }
            }
            m_allDates = result;
        }
        return m_allDates;
    }

    /**
     * Filters all exceptions from the provided dates.
     * @param dates the dates to filter.
     * @return the provided dates, except the ones that match some exception.
     */
    private SortedSet<Date> filterExceptions(SortedSet<Date> dates) {

        SortedSet<Date> result = new TreeSet<Date>();
        for (Date d : dates) {
            if (!m_exceptions.contains(d)) {
                result.add(d);
            }
        }
        return result;
    }

}
