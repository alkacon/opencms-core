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
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/** Test cases for @{link org.opencms.widgets.serialdate.CmsSerialDateBeanWeekly}. */
public class TestSerialDateBeanWeekly extends OpenCmsTestCase {

    /** empty sorted set of dates. */
    private static final SortedSet<Date> EMPTY_SORTED_SET_DATES = new TreeSet<>();

    /** Several tests for @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean#getDates}. */
    public void testGetDates() {

        Calendar startDate = new GregorianCalendar(2017, 05, 28, 15, 05); // 28.06.2017 15:05 Mi
        Calendar endDate = new GregorianCalendar(2017, 05, 28, 16, 35); // 28.06.2017 16:35 Mi
        Calendar serialEndDate = new GregorianCalendar(2017, 06, 27, 01, 35); // 27.07.2017 01:35 Do
        Calendar date1 = new GregorianCalendar(2017, 05, 29, 15, 05); // 29.06.2017 15:05 Do
        Calendar date2 = new GregorianCalendar(2017, 06, 04, 15, 05); // 04.07.2017 15:05 Di
        Calendar date3 = new GregorianCalendar(2017, 06, 06, 15, 05); // 06.07.2017 15:05 Do
        Calendar date4 = new GregorianCalendar(2017, 06, 11, 15, 05); // 11.07.2017 15:05 Di
        Calendar date5 = new GregorianCalendar(2017, 06, 13, 15, 05); // 13.07.2017 15:05 Do
        Calendar date6 = new GregorianCalendar(2017, 06, 18, 15, 05); // 18.07.2017 15:05 Di
        Calendar date7 = new GregorianCalendar(2017, 06, 20, 15, 05); // 20.07.2017 15:05 Do
        Calendar date8 = new GregorianCalendar(2017, 06, 25, 15, 05); // 25.07.2017 15:05 Di
        Calendar date9 = new GregorianCalendar(2017, 06, 27, 15, 05); // 27.07.2017 15:05 Do
        List<Calendar> dates = new ArrayList<>(6);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);
        dates.add(date5);
        dates.add(date6);
        dates.add(date7);
        dates.add(date8);
        dates.add(date9);
        SortedSet<WeekDay> weekDays = new TreeSet<WeekDay>();
        weekDays.add(WeekDay.TUESDAY);
        weekDays.add(WeekDay.THURSDAY);

        // the tested bean
        CmsSerialDateBeanWeekly bean = null;
        //the dates the bean yields
        SortedSet<Long> beanDates = null;

        // every Tuesday and Thursday till Thursday, 27.07.2017
        bean = new CmsSerialDateBeanWeekly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0,
            EMPTY_SORTED_SET_DATES,
            1,
            weekDays);
        beanDates = bean.getDatesAsLong();
        Iterator<Long> it = beanDates.iterator();
        assertEquals("There should be nine dates", 9, beanDates.size());
        for (int i = 0; i < 9; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // Tuesday and Thursday every two weeks till Thursday, 27.07.2017
        bean = new CmsSerialDateBeanWeekly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0,
            EMPTY_SORTED_SET_DATES,
            2,
            weekDays);
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be five dates", 5, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(3).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(4).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(7).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(8).getTimeInMillis(), it.next().longValue());

        // every Tuesday and Thursday nine occurrences
        bean = new CmsSerialDateBeanWeekly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            9,
            EMPTY_SORTED_SET_DATES,
            1,
            weekDays);
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be nine dates", 9, beanDates.size());
        for (int i = 0; i < 9; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // Tuesday and Thursday every two weeks five occurrences
        bean = new CmsSerialDateBeanWeekly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            5,
            EMPTY_SORTED_SET_DATES,
            2,
            weekDays);
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be five dates", 5, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(3).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(4).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(7).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(8).getTimeInMillis(), it.next().longValue());

    }
}
