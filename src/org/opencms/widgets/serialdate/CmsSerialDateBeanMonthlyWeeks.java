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

import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * Implementation of @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean}
 * that handles series' specified on a monthly base.
 */
public class CmsSerialDateBeanMonthlyWeeks extends A_CmsSerialDateBean {

    /** The number of months till the next event. */
    private int m_interval;
    /** The number of the day or week day of the month the event should occur. */
    private SortedSet<WeekOfMonth> m_weeksOfMonth;
    /** The weekday the event should occur. Can be <code>null</code> if the weekday does not matter. */
    private WeekDay m_weekDay;
    /** The index of the current week from the weeks of the month */
    private Iterator<WeekOfMonth> m_weekOfMonthIterator;

    /**
     * Constructs the bean with all the information provided by the {@link org.opencms.widgets.CmsSerialDateWidget}.
     *
     * @param startDate the start date of the series as provided by the serial date widget.
     * @param endDate the end date of the series as provided by the serial date widget.
     * @param isWholeDay flag, indicating if the event lasts the whole day
     * @param endType the end type of the series as provided by the serial date widget.
     * @param serialEndDate the end date of the series as provided by the serial date widget.
     * @param occurrences the maximal number of occurrences of the event as provided by the serial date widget.
     * @param exceptions dates where the event does not take place, even if it is in the series.
     * @param interval the number of month to the next events.
     * @param weeksOfMonth the weeks in the month the event should take place, e.g., the first and the third week.
     * @param weekDay the weekday on which the event should occur
     */
    public CmsSerialDateBeanMonthlyWeeks(
        Date startDate,
        Date endDate,
        boolean isWholeDay,
        EndType endType,
        Date serialEndDate,
        int occurrences,
        SortedSet<Date> exceptions,
        int interval,
        SortedSet<WeekOfMonth> weeksOfMonth,
        WeekDay weekDay) {
        super(startDate, endDate, isWholeDay, endType, serialEndDate, occurrences, exceptions);
        m_interval = interval;
        m_weeksOfMonth = weeksOfMonth;
        m_weekDay = weekDay;
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#getFirstDate()
     */
    @Override
    protected Calendar getFirstDate() {

        m_weekOfMonthIterator = m_weeksOfMonth.iterator();
        Calendar date = (Calendar)getStartDate().clone();
        toCorrectDateWithDay(date, m_weekOfMonthIterator.next());
        while (date.getTimeInMillis() < getStartDate().getTimeInMillis()) {
            toNextDate(date, 1);
        }
        return date;

    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#isAnyDatePossible()
     */
    @Override
    protected boolean isAnyDatePossible() {

        return m_weeksOfMonth.size() > 0;
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#toNextDate(java.util.Calendar)
     */
    @Override
    protected void toNextDate(Calendar date) {

        toNextDate(date, m_interval);
    }

    /**
     * Sets the day of the month that matches the condition, i.e., the day of month of the 2nd Saturday.
     * If the day does not exist in the current month, the last possible date is set, i.e.,
     * instead of the fifth Saturday, the fourth is chosen.
     *
     * @param date date that has the correct year and month already set.
     * @param week the number of the week to choose.
     */
    private void toCorrectDateWithDay(Calendar date, WeekOfMonth week) {

        date.set(Calendar.DAY_OF_MONTH, 1);
        int daysToFirstWeekDayMatch = ((m_weekDay.toInt() + I_CmsSerialDateValue.NUM_OF_WEEKDAYS)
            - (date.get(Calendar.DAY_OF_WEEK))) % I_CmsSerialDateValue.NUM_OF_WEEKDAYS;
        date.add(Calendar.DAY_OF_MONTH, daysToFirstWeekDayMatch);
        int wouldBeDayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            + ((week.ordinal()) * I_CmsSerialDateValue.NUM_OF_WEEKDAYS);
        if (wouldBeDayOfMonth > date.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            date.set(Calendar.DAY_OF_MONTH, wouldBeDayOfMonth - I_CmsSerialDateValue.NUM_OF_WEEKDAYS);
        } else {
            date.set(Calendar.DAY_OF_MONTH, wouldBeDayOfMonth);
        }

    }

    /**
     * Calculates the next date, starting from the provided date.
     *
     * @param date the current date.
     * @param interval the number of month to add when moving to the next month.
     */
    private void toNextDate(Calendar date, int interval) {

        long previousDate = date.getTimeInMillis();
        if (!m_weekOfMonthIterator.hasNext()) {
            date.add(Calendar.MONTH, interval);
            m_weekOfMonthIterator = m_weeksOfMonth.iterator();
        }
        toCorrectDateWithDay(date, m_weekOfMonthIterator.next());
        if (previousDate == date.getTimeInMillis()) { // this can happen if the fourth and the last week are checked.
            toNextDate(date);
        }

    }

}
