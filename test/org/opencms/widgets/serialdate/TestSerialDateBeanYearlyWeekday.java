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
import org.opencms.acacia.shared.I_CmsSerialDateValue.Month;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;
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

/** Test cases for @{link org.opencms.widgets.serialdate.CmsSerialDateBeanYearly}. */
public class TestSerialDateBeanYearlyWeekday extends OpenCmsTestCase {

    /** empty sorted set of dates. */
    private static final SortedSet<Date> EMPTY_SORTED_SET_DATES = new TreeSet<>();

    /** Test for 5th Monday in January. */
    public void testGetDatesWeekDayComplex() {

        Calendar startDate = new GregorianCalendar(2017, 05, 28, 15, 05); // 28.06.2017 15:05 Mi
        Calendar endDate = new GregorianCalendar(2017, 05, 28, 16, 35); // 28.06.2017 16:35 Mi
        Calendar serialEndDate = new GregorianCalendar(2021, 1, 15, 01, 35); // 15.02.2025 01:35
        Calendar date1 = new GregorianCalendar(2018, 0, 29, 15, 05); // 29.01.2018 15:05 Mo
        Calendar date2 = new GregorianCalendar(2019, 0, 28, 15, 05); // 28.01.2019 15:05 Mo
        Calendar date3 = new GregorianCalendar(2020, 0, 27, 15, 05); // 27.01.2020 15:05 Mo
        Calendar date4 = new GregorianCalendar(2021, 0, 25, 15, 05); // 25.01.2021 15:05 Mo
        List<Calendar> dates = new ArrayList<>(4);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);

        // the tested bean
        CmsSerialDateBeanYearlyWeekday bean = null;
        //the dates the bean yields
        Collection<Long> beanDates = null;

        // every 5th Monday in January till end date
        bean = new CmsSerialDateBeanYearlyWeekday(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            WeekOfMonth.LAST, //week of month
            Month.JANUARY, //month
            WeekDay.MONDAY); //weekday
        beanDates = bean.getDatesAsLong();
        Iterator<Long> it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 5th Monday in January, 4 occurrences
        bean = new CmsSerialDateBeanYearlyWeekday(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            4, //occurrences
            EMPTY_SORTED_SET_DATES,
            WeekOfMonth.LAST, //week of month
            Month.JANUARY, //month
            WeekDay.MONDAY); //weekday
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }
    }

    /** Test for 3rd Monday in January. */
    public void testGetDatesWeekDaySimple() {

        Calendar startDate = new GregorianCalendar(2017, 05, 28, 15, 05); // 28.06.2017 15:05 Mi
        Calendar endDate = new GregorianCalendar(2017, 05, 28, 16, 35); // 28.06.2017 16:35 Mi
        Calendar serialEndDate = new GregorianCalendar(2021, 1, 15, 01, 35); // 15.02.2021 01:35
        Calendar date1 = new GregorianCalendar(2018, 0, 15, 15, 05); // 15.01.2018 15:05 Mo
        Calendar date2 = new GregorianCalendar(2019, 0, 21, 15, 05); // 21.01.2019 15:05 Mo
        Calendar date3 = new GregorianCalendar(2020, 0, 20, 15, 05); // 20.01.2020 15:05 Mo
        Calendar date4 = new GregorianCalendar(2021, 0, 18, 15, 05); // 18.01.2021 15:05 Mo
        List<Calendar> dates = new ArrayList<>(4);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);

        // the tested bean
        CmsSerialDateBeanYearlyWeekday bean = null;
        //the dates the bean yields
        Collection<Long> beanDates = null;

        // every 3rd Monday in January till end date
        bean = new CmsSerialDateBeanYearlyWeekday(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0, //occurrences
            EMPTY_SORTED_SET_DATES,
            WeekOfMonth.THIRD, //week of month
            Month.JANUARY, //month
            WeekDay.MONDAY); //weekday
        beanDates = bean.getDatesAsLong();
        Iterator<Long> it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }

        // every 3rd Monday in January, 4 occurrences
        bean = new CmsSerialDateBeanYearlyWeekday(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            4, //occurrences
            EMPTY_SORTED_SET_DATES,
            WeekOfMonth.THIRD, //week of month
            Month.JANUARY, //month
            WeekDay.MONDAY); //weekday
        beanDates = bean.getDatesAsLong();
        it = beanDates.iterator();
        assertEquals("There should be four dates", 4, beanDates.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(dates.get(i).getTimeInMillis(), it.next().longValue());
        }
    }
}
