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
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/** Test cases for @{link org.opencms.widgets.serialdate.CmsSerialDateBeanMonthly}. */
public class TestSerialDateBeanMonthlyWeeks extends OpenCmsTestCase {

    /** empty sorted set of dates. */
    private static final SortedSet<Date> EMPTY_SORTED_SET_DATES = new TreeSet<>();

    /** Test for every 5th Saturday each and each second month. */
    public void testGetDatesWeekDayComplex() {

        Calendar startDate = new GregorianCalendar(2017, 05, 28, 15, 05); // 28.06.2017 15:05 Wed
        Calendar endDate = new GregorianCalendar(2017, 05, 28, 16, 35); // 28.06.2017 16:35 Wed
        Calendar serialEndDate = new GregorianCalendar(2017, 7, 31, 01, 35); // 31.07.2017 01:35 Thu
        Calendar date1 = new GregorianCalendar(2017, 5, 29, 15, 05); // 30.06.2017 15:05 Thu
        Calendar date2 = new GregorianCalendar(2017, 6, 6, 15, 05); // 07.07.2017 15:05 Thu
        Calendar date3 = new GregorianCalendar(2017, 6, 27, 15, 05); // 28.07.2017 15:05 Thu
        Calendar date4 = new GregorianCalendar(2017, 7, 3, 15, 05); // 03.07.2017 15:05 Thu
        Calendar date5 = new GregorianCalendar(2017, 7, 24, 15, 05); // 24.07.2017 15:05 Thu
        Calendar date6 = new GregorianCalendar(2017, 7, 31, 15, 05); // 31.07.2017 15:05 Thu
        List<Calendar> dates = new ArrayList<>(6);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);
        dates.add(date5);
        dates.add(date6);

        // the tested bean
        CmsSerialDateBeanMonthlyWeeks bean = null;
        //the dates the bean yields
        SortedSet<Long> beanDates = null;
        SortedSet<WeekOfMonth> weeks = new TreeSet<>();
        weeks.add(WeekOfMonth.FIRST);
        weeks.add(WeekOfMonth.FOURTH);
        weeks.add(WeekOfMonth.LAST);
        WeekDay weekday = WeekDay.THURSDAY;

        // every 1st, 4th and 5th Thursday of every month till end date
        bean = new CmsSerialDateBeanMonthlyWeeks(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            1, //interval
            weeks, //weeks of month
            weekday); //weekday
        beanDates = bean.getDatesAsLong();
        Iterator<Long> it = beanDates.iterator();
        assertEquals("There should be six dates", 6, beanDates.size());
        for (int i = 0; i < 6; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 1st, 4th and 5th Thursday of every second month till end date
        bean = new CmsSerialDateBeanMonthlyWeeks(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            2, //interval
            weeks, //weeks of month
            weekday); //weekday
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(3).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(4).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(5).getTimeInMillis(), it.next().longValue());

        // every 5th Saturday 1st, 4th and 5th Thursday of every month, 6 occurrences
        bean = new CmsSerialDateBeanMonthlyWeeks(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            6, //occurrences
            EMPTY_SORTED_SET_DATES,
            1, //interval
            weeks, //weeks of month
            weekday); //weekday
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be six dates", 6, beanDates.size());
        for (int i = 0; i < 6; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every  1st, 4th and 5th Thursday of every second month, 4 occurrences
        bean = new CmsSerialDateBeanMonthlyWeeks(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            4, //occurrences
            EMPTY_SORTED_SET_DATES,
            2, //interval
            weeks, //weeks of month
            weekday); //weekday
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(3).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(4).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(5).getTimeInMillis(), it.next().longValue());

    }

    /** Test for every 2nd Saturday. */
    public void testGetDatesWeekDaySimple() {

        Calendar startDate = new GregorianCalendar(2017, 05, 28, 15, 05); // 28.06.2017 15:05 Mi
        Calendar endDate = new GregorianCalendar(2017, 05, 28, 16, 35); // 28.06.2017 16:35 Mi
        Calendar serialEndDate = new GregorianCalendar(2017, 10, 9, 01, 35); // 09.11.2017 01:35 Mo
        Calendar date1 = new GregorianCalendar(2017, 6, 8, 15, 05); // 08.07.2017 15:05 Sa
        Calendar date2 = new GregorianCalendar(2017, 7, 12, 15, 05); // 12.08.2017 15:05 Sa
        Calendar date3 = new GregorianCalendar(2017, 8, 9, 15, 05); // 09.09.2017 15:05 Sa
        Calendar date4 = new GregorianCalendar(2017, 9, 14, 15, 05); // 14.10.2017 15:05 Sa
        List<Calendar> dates = new ArrayList<>(4);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);

        // the tested bean
        CmsSerialDateBeanMonthlyWeeks bean = null;
        //the dates the bean yields
        SortedSet<Long> beanDates = null;

        SortedSet<WeekOfMonth> weeks = new TreeSet<>();
        weeks.add(WeekOfMonth.SECOND);

        WeekDay weekday = WeekDay.SATURDAY;

        // every 2nd Saturday of every month till end date
        bean = new CmsSerialDateBeanMonthlyWeeks(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            1, //interval
            weeks, //day of month
            weekday); //weekday
        beanDates = bean.getDatesAsLong();
        assertEquals("There should be four dates", 4, beanDates.size());
        Iterator<Long> it = beanDates.iterator();
        for (int i = 0; i < 4; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 2nd Saturday of every second month till end date
        bean = new CmsSerialDateBeanMonthlyWeeks(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            2, //interval
            weeks, //weeks of month
            weekday); //weekday
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be two dates", 2, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(2).getTimeInMillis(), it.next().longValue());

        // every 2nd Saturday of every month, 4 occurrences
        bean = new CmsSerialDateBeanMonthlyWeeks(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            4, //occurrences
            EMPTY_SORTED_SET_DATES,
            1, //interval
            weeks, //weeks of month
            weekday); //weekday
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 2nd Saturday of every second month, 2 occurrences
        bean = new CmsSerialDateBeanMonthlyWeeks(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            2, //occurrences
            EMPTY_SORTED_SET_DATES,
            2, //interval
            weeks, //weeks of month
            weekday); //weekday
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be two dates", 2, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(2).getTimeInMillis(), it.next().longValue());

    }

}
