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

import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;

import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

/**
 * Implementation of @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean}
 * that handles series' specified on a monthly base.
 */
public class CmsSerialDateBeanMonthly extends A_CmsSerialDateBean {

    /** The number of months till the next event. */
    private int m_interval;
    /** The number of the day or week day of the month the event should occur. */
    private int m_dayOfMonth;

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
     * @param dayOfMonth the day of the month the event should occur, if the month does not have that particular day
     *   the event takes place at the last day of the month.
     */
    public CmsSerialDateBeanMonthly(
        Date startDate,
        Date endDate,
        boolean isWholeDay,
        EndType endType,
        Date serialEndDate,
        int occurrences,
        SortedSet<Date> exceptions,
        int interval,
        int dayOfMonth) {
        super(startDate, endDate, isWholeDay, endType, serialEndDate, occurrences, exceptions);
        m_interval = interval;
        m_dayOfMonth = dayOfMonth;
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#getFirstDate()
     */
    @Override
    protected Calendar getFirstDate() {

        Calendar date = (Calendar)getStartDate().clone();
        if (date.get(Calendar.DAY_OF_MONTH) > m_dayOfMonth) {
            date.set(Calendar.DAY_OF_MONTH, 1);
            date.add(Calendar.MONTH, 1);
        }
        setCorrectDay(date);
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

        date.set(Calendar.DAY_OF_MONTH, 1); // To not get in trouble when we are at the 31st or a like.
        date.add(Calendar.MONTH, m_interval);
        setCorrectDay(date);
    }

    /**
     * Checks, if the the current month does have less than the searched day.
     * @param date the date where year and month are set correctly
     * @return <code>true</code> if the month has less days than the day searched for, <code>false</code> otherwise.
     */
    private boolean monthHasNotDay(Calendar date) {

        return date.getActualMaximum(Calendar.DAY_OF_MONTH) < m_dayOfMonth;
    }

    /**
     * Set the correct day for the date with year and month already fixed.
     * @param date the date, where year and month are already correct.
     */
    private void setCorrectDay(Calendar date) {

        if (monthHasNotDay(date)) {
            date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else {
            date.set(Calendar.DAY_OF_MONTH, m_dayOfMonth);
        }
    }

}
