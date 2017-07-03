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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Implementation of @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean}
 * that handles series' specified as individual dates.
 */
public class CmsSerialDateBeanIndividual extends A_CmsSerialDateBean {

    /** The individual dates. */
    private TreeSet<Date> m_individualDates;

    /**
     * @param startDate the start date of the series as provided by the serial date widget.
     * @param endDate the end date of the series as provided by the serial date widget.
     * @param isWholeDay flag, indicating if the event lasts the whole day
     * @param endType the end type of the series as provided by the serial date widget.
     * @param serialEndDate the end date of the series as provided by the serial date widget.
     * @param occurrences the maximal number of occurrences of the event as provided by the serial date widget.
     * @param exceptions dates where the event does not take place, even if it is in the series.
     * @param individualDates the individual dates of the series.
     */
    public CmsSerialDateBeanIndividual(
        Date startDate,
        Date endDate,
        boolean isWholeDay,
        EndType endType,
        Date serialEndDate,
        int occurrences,
        SortedSet<Date> exceptions,
        SortedSet<Date> individualDates) {
        super(startDate, endDate, isWholeDay, endType, serialEndDate, occurrences, exceptions);
        m_individualDates = null == individualDates ? new TreeSet<Date>() : new TreeSet<Date>(individualDates);

    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#getFirstDate()
     */
    @Override
    protected Calendar getFirstDate() {

        if (m_individualDates.isEmpty()) {
            return null;
        } else {
            Calendar result = new GregorianCalendar();
            result.setTime(m_individualDates.iterator().next());
            return result;
        }
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#isAnyDatePossible()
     */
    @Override
    protected boolean isAnyDatePossible() {

        return !m_individualDates.isEmpty();
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#showMoreEntries(java.util.Calendar, int)
     */
    @Override
    protected boolean showMoreEntries(Calendar nextDate, int previousOccurrences) {

        return previousOccurrences < m_individualDates.size();
    }

    /**
     * @see org.opencms.widgets.serialdate.A_CmsSerialDateBean#toNextDate(java.util.Calendar)
     */
    @Override
    protected void toNextDate(Calendar date) {

        Date d = date.getTime();
        boolean found = false;
        Iterator<Date> it = m_individualDates.iterator();
        while (it.hasNext()) {
            if (found) {
                date.setTime(it.next());
                return;
            } else if (d.equals(it.next())) {
                found = true;
            }
        }
    }

}
