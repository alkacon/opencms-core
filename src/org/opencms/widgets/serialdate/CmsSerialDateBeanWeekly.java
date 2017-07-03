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

import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

/**
 * Implementation of @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean}
 * that handles series' specified on a weekly base.
 */
public class CmsSerialDateBeanWeekly extends A_CmsSerialDateBean {

    /** The number of weeks till the next event occurs. */
    private int m_interval;
    /** The weekdays the event occurs. */
    private SortedSet<WeekDay> m_weekDays;

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
     * @param interval the number of weeks to the next event.
     * @param weekDays the weekdays the event occurs.
     */
    public CmsSerialDateBeanWeekly(
        Date startDate,
        Date endDate,
        boolean isWholeDay,
        EndType endType,
        Date serialEndDate,
        int occurrences,
        SortedSet<Date> exceptions,
        int interval,
        SortedSet<WeekDay> weekDays) {
        super(startDate, endDate, isWholeDay, endType, serialEndDate, occurrences, exceptions);
        m_interval = interval;
        m_weekDays = weekDays;
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#getFirstDate()
     */
    @Override
    protected Calendar getFirstDate() {

        Calendar date = (Calendar)getStartDate().clone();
        WeekDay currentWeekDay = WeekDay.fromInt(date.get(Calendar.DAY_OF_WEEK));
        if (!m_weekDays.contains(currentWeekDay)) {
            toNextDate(date);
        }
        return date;

    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#isAnyDatePossible()
     */
    @Override
    protected boolean isAnyDatePossible() {

        return !m_weekDays.isEmpty();
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#toNextDate(java.util.Calendar)
     */
    @Override
    protected void toNextDate(Calendar date) {

        WeekDay currentWeekDay = WeekDay.fromInt(date.get(Calendar.DAY_OF_WEEK));
        int daysToNextMatch = getDaysToNextMatch(currentWeekDay);
        date.add(Calendar.DATE, daysToNextMatch);

    }

    /**
     * Returns the number of days from the given weekday to the next weekday the event should occur.
     * @param weekDay the current weekday.
     * @return the number of days to the next weekday an event could occur.
     */
    private int getDaysToNextMatch(WeekDay weekDay) {

        for (WeekDay wd : m_weekDays) {
            if (wd.compareTo(weekDay) > 0) {
                return wd.toInt() - weekDay.toInt();
            }
        }
        return (m_weekDays.iterator().next().toInt() + (m_interval * I_CmsSerialDateValue.NUM_OF_WEEKDAYS))
            - weekDay.toInt();
    }

}
