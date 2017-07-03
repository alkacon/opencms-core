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
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/** Test cases for @{link org.opencms.widgets.serialdate.CmsSerialDateBeanMonthly}. */
public class TestSerialDateBeanMonthly extends OpenCmsTestCase {

    /** empty sorted set of dates. */
    private static final SortedSet<Date> EMPTY_SORTED_SET_DATES = new TreeSet<>();

    /** Here we choose a day that's not present for some month. These month are just skipped then. */
    public void testGetDatesDayComplex() {

        Calendar startDate = new GregorianCalendar(2017, 05, 28, 15, 05); // 28.06.2017 15:05 Mi
        Calendar endDate = new GregorianCalendar(2017, 05, 28, 16, 35); // 28.06.2017 16:35 Mi
        Calendar serialEndDate = new GregorianCalendar(2018, 2, 31, 01, 35); // 31.03.2018 01:35
        Calendar date0 = new GregorianCalendar(2017, 5, 30, 15, 05); // 30.06.2017 15:05
        Calendar date1 = new GregorianCalendar(2017, 6, 31, 15, 05); // 31.07.2017 15:05
        Calendar date2 = new GregorianCalendar(2017, 7, 31, 15, 05); // 31.08.2017 15:05
        Calendar date3 = new GregorianCalendar(2017, 8, 30, 15, 05); // 30.09.2017 15:05
        Calendar date4 = new GregorianCalendar(2017, 9, 31, 15, 05); // 31.10.2017 15:05
        Calendar date5 = new GregorianCalendar(2017, 10, 30, 15, 05); // 30.11.2017 15:05
        Calendar date6 = new GregorianCalendar(2017, 11, 31, 15, 05); // 31.12.2017 15:05
        Calendar date7 = new GregorianCalendar(2018, 0, 31, 15, 05); // 31.01.2018 15:05
        Calendar date8 = new GregorianCalendar(2018, 1, 28, 15, 05); // 28.02.2018 15:05
        Calendar date9 = new GregorianCalendar(2018, 2, 31, 15, 05); // 31.03.2018 15:05
        List<Calendar> dates = new ArrayList<>(10);
        dates.add(date0);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);
        dates.add(date5);
        dates.add(date6);
        dates.add(date7);
        dates.add(date8);
        dates.add(date9);

        // the tested bean
        CmsSerialDateBeanMonthly bean = null;
        //the dates the bean yields
        Collection<Long> beanDates = null;

        // every 31th day of every month till end date
        bean = new CmsSerialDateBeanMonthly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            1, //interval
            31); //day of month
        beanDates = bean.getDatesAsLong();
        Iterator<Long> it = beanDates.iterator();
        assertEquals("There should be ten dates", dates.size(), beanDates.size());
        for (int i = 0; i < dates.size(); i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 31th day of every second month till end date
        bean = new CmsSerialDateBeanMonthly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            2, //interval
            31); //day of month
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be three dates", 5, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(2).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(4).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(6).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(8).getTimeInMillis(), it.next().longValue());

        // every 31th day of every month, 9 occurrences
        bean = new CmsSerialDateBeanMonthly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            serialEndDate.getTime(),
            dates.size(), //occurrences
            EMPTY_SORTED_SET_DATES,
            1, //interval
            31); //day of month
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be six dates", dates.size(), beanDates.size());
        for (int i = 0; i < 6; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 31th day of every second month, 5 occurrences
        bean = new CmsSerialDateBeanMonthly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            serialEndDate.getTime(),
            5, //occurrences
            EMPTY_SORTED_SET_DATES,
            2, //interval
            31); //day of month
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be three dates", 5, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(2).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(4).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(6).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(8).getTimeInMillis(), it.next().longValue());

    }

    /** Test for the 15th day of each and each second month. */
    public void testGetDatesDaySimple() {

        Calendar startDate = new GregorianCalendar(2017, 05, 28, 15, 05); // 28.06.2017 15:05 Mi
        Calendar endDate = new GregorianCalendar(2017, 05, 28, 16, 35); // 28.06.2017 16:35 Mi
        Calendar serialEndDate = new GregorianCalendar(2017, 10, 13, 01, 35); // 13.11.2017 01:35 Do
        Calendar date1 = new GregorianCalendar(2017, 6, 15, 15, 05); // 15.07.2017 15:05 Sa
        Calendar date2 = new GregorianCalendar(2017, 7, 15, 15, 05); // 15.08.2017 15:05 Di
        Calendar date3 = new GregorianCalendar(2017, 8, 15, 15, 05); // 15.09.2017 15:05 Fr
        Calendar date4 = new GregorianCalendar(2017, 9, 15, 15, 05); // 15.10.2017 15:05 So
        List<Calendar> dates = new ArrayList<>(4);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);

        // the tested bean
        CmsSerialDateBeanMonthly bean = null;
        //the dates the bean yields
        Collection<Long> beanDates = null;

        // every 15th day of every month till end date
        bean = new CmsSerialDateBeanMonthly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            1, //interval
            15); //day of month
        beanDates = bean.getDatesAsLong();
        Iterator<Long> it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 15th day of every second month till end date
        bean = new CmsSerialDateBeanMonthly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            2, //interval
            15); //day of month
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be two dates", 2, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(2).getTimeInMillis(), it.next().longValue());

        // every 15th day of every month, 4 occurrences
        bean = new CmsSerialDateBeanMonthly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            4, //occurrences
            EMPTY_SORTED_SET_DATES,
            1, //interval
            15); //day of month
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 15th day of every second month, 2 occurrences
        bean = new CmsSerialDateBeanMonthly(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            2, //occurrences
            EMPTY_SORTED_SET_DATES,
            2, //interval
            15); //day of month
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be two dates", 2, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(2).getTimeInMillis(), it.next().longValue());

    }
}
