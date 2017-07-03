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
import org.opencms.acacia.shared.I_CmsSerialDateValue.Month;

import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

/**
 * Implementation of @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean}
 * that handles series' specified on a yearly base.
 */
public class CmsSerialDateBeanYearly extends A_CmsSerialDateBean {

    /** The number of the day or week day of the month the event should occur. */
    private int m_dayOfMonth;
    /** The month in which the event should occur. */
    private Month m_month;

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
     * @param dayOfMonth if <code>weekDay</code> is <code>null</code> the day of the month the event should occur, otherwise the number of the specific week day in the month where event should occur.
     * @param month the month in which the event should occur
     */
    public CmsSerialDateBeanYearly(
        Date startDate,
        Date endDate,
        boolean isWholeDay,
        EndType endType,
        Date serialEndDate,
        int occurrences,
        SortedSet<Date> exceptions,
        int dayOfMonth,
        Month month) {

        super(startDate, endDate, isWholeDay, endType, serialEndDate, occurrences, exceptions);
        m_dayOfMonth = dayOfMonth;
        m_month = month;
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#getFirstDate()
     */
    @Override
    protected Calendar getFirstDate() {

        Calendar date = (Calendar)getStartDate().clone();
        int month = date.get(Calendar.MONTH);
        if ((month > m_month.ordinal())
            || ((month == m_month.ordinal()) && (m_dayOfMonth < date.get(Calendar.DAY_OF_MONTH)))) {
            date.set(Calendar.MONTH, 0);
            date.set(Calendar.DAY_OF_MONTH, 1);
            date.add(Calendar.YEAR, 1);
        }
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.set(Calendar.MONTH, m_month.ordinal());
        setCorrectDay(date);
        return date;

    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#isAnyDatePossible()
     */
    @Override
    protected boolean isAnyDatePossible() {

        return m_month.getMaximalDay() >= m_dayOfMonth;

    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#toNextDate(java.util.Calendar)
     */
    @Override
    protected void toNextDate(Calendar date) {

        date.set(Calendar.DAY_OF_MONTH, 1);
        date.add(Calendar.YEAR, 1);
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
