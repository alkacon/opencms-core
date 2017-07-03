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

import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Implementation of @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean}
 * that handles single events.
 */
public class CmsSerialDateBeanSingle implements I_CmsSerialDateBean {

    /** Singleton list with the start date of the event. */
    SortedSet<Date> m_dates;
    /** Singleton list with the start date of the event in milliseconds. */
    SortedSet<Long> m_datesAsLong;

    /**
     * Constructor for the serial date bean for single events.
     * @param startDate the start date of the single event.
     */
    public CmsSerialDateBeanSingle(Date startDate) {
        Objects.requireNonNull(startDate, "The start date should not be null.");
        m_dates = new TreeSet<>();
        m_dates.add(startDate);
        m_datesAsLong = new TreeSet<>();
        m_datesAsLong.add(Long.valueOf(startDate.getTime()));

    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#getDates()
     */
    public SortedSet<Date> getDates() {

        return m_dates;
    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#getDatesAsLong()
     */
    public SortedSet<Long> getDatesAsLong() {

        return m_datesAsLong;
    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#getExceptions()
     */
    public SortedSet<Date> getExceptions() {

        return new TreeSet<>();
    }

    /**
     * @see org.opencms.widgets.serialdate.I_CmsSerialDateBean#hasTooManyDates()
     */
    public boolean hasTooManyDates() {

        return false;
    }

}
