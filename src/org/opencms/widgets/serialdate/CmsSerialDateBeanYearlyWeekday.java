/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.widgets.serialdate;

import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.Month;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;

import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

/**
 * Implementation of @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean}
 * that handles series' specified on a yearly base.
 */
public class CmsSerialDateBeanYearlyWeekday extends A_CmsSerialDateBean {

    /** The number of the week of the month the event should occur. */
    private WeekOfMonth m_weekOfMonth;
    /** The month in which the event should occur. */
    private Month m_month;
    /** The weekday the event should occur. Can be <code>null</code> if the weekday does not matter. */
    private WeekDay m_weekDay;

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
     * @param weekOfMonth if <code>weekDay</code> is <code>null</code> the day of the month the event should occur, otherwise the number of the specific week day in the month where event should occur.
     * @param month the month in which the event should occur
     * @param weekDay the weekday on which the event should occur
     */
    public CmsSerialDateBeanYearlyWeekday(
        Date startDate,
        Date endDate,
        boolean isWholeDay,
        EndType endType,
        Date serialEndDate,
        int occurrences,
        SortedSet<Date> exceptions,
        WeekOfMonth weekOfMonth,
        Month month,
        WeekDay weekDay) {

        super(startDate, endDate, isWholeDay, endType, serialEndDate, occurrences, exceptions);
        m_weekOfMonth = weekOfMonth;
        m_month = month;
        m_weekDay = weekDay;
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#getFirstDate()
     */
    @Override
    protected Calendar getFirstDate() {

        Calendar date = (Calendar)getStartDate().clone();
        int month = date.get(Calendar.MONTH);
        Calendar firstPossibleMatch = (Calendar)date.clone();
        setFittingWeekDay(firstPossibleMatch);
        if ((month > m_month.ordinal())
            || ((month == m_month.ordinal()) && (date.getTimeInMillis() > firstPossibleMatch.getTimeInMillis()))) {
            date.set(Calendar.MONTH, 0);
            date.set(Calendar.DAY_OF_MONTH, 1);
            date.add(Calendar.YEAR, 1);
        }
        date.set(Calendar.MONTH, m_month.ordinal());
        setFittingWeekDay(date);
        return date;

    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#isAnyDatePossible()
     */
    @Override
    protected boolean isAnyDatePossible() {

        return true;

    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#toNextDate(java.util.Calendar)
     */
    @Override
    protected void toNextDate(Calendar date) {

        date.set(Calendar.DAY_OF_MONTH, 1);
        date.add(Calendar.YEAR, 1);
        setFittingWeekDay(date);
    }

    /**
     * Adjusts the day in the provided month, that it fits the specified week day.
     * If there's no match for that provided month, the next possible month is checked.
     *
     * @param date the date to adjust, with the correct year and month already set.
     */
    private void setFittingWeekDay(Calendar date) {

        date.set(Calendar.DAY_OF_MONTH, 1);
        int weekDayFirst = date.get(Calendar.DAY_OF_WEEK);
        int firstFittingWeekDay = (((m_weekDay.toInt() + I_CmsSerialDateValue.NUM_OF_WEEKDAYS) - weekDayFirst)
            % I_CmsSerialDateValue.NUM_OF_WEEKDAYS) + 1;
        int fittingWeekDay = firstFittingWeekDay + (I_CmsSerialDateValue.NUM_OF_WEEKDAYS * m_weekOfMonth.ordinal());
        if (fittingWeekDay > date.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            fittingWeekDay -= I_CmsSerialDateValue.NUM_OF_WEEKDAYS;
        }
        date.set(Calendar.DAY_OF_MONTH, fittingWeekDay);
    }
}
