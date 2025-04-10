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

import org.opencms.acacia.shared.I_CmsSerialDateValue.EndType;

import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Implementation of @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean}
 * that handles series' specified on a daily base.
 */
public class CmsSerialDateBeanWorkingDays extends A_CmsSerialDateBean {

    /** The non-working days. */
    private static final int[] NON_WORKING_DAYS = {Calendar.SATURDAY, Calendar.SUNDAY};

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
     */
    public CmsSerialDateBeanWorkingDays(
        Date startDate,
        Date endDate,
        boolean isWholeDay,
        EndType endType,
        Date serialEndDate,
        int occurrences,
        SortedSet<Date> exceptions) {
        super(startDate, endDate, isWholeDay, endType, serialEndDate, occurrences, exceptions);

    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#getFirstDate()
     */
    @Override
    protected Calendar getFirstDate() {

        Calendar date = (Calendar)getStartDate().clone();
        if (!isWorkingDay(date)) {
            toNextDate(date);
        }
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

        do {
            date.add(Calendar.DATE, 1);
        } while (!isWorkingDay(date));

    }

    /**
     * Check, if the day for the provided date is a working day (i.e., not Saturday or Sunday).
     * @param date the date to check.
     * @return <code>true</code> if the provided date represents a working day, <code>false</code> otherwise.
     */
    private boolean isWorkingDay(Calendar date) {

        return !ArrayUtils.contains(NON_WORKING_DAYS, date.get(Calendar.DAY_OF_WEEK));
    }

}
