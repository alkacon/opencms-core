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

/** Test cases for @{link org.opencms.widgets.serialdate.CmsSerialDateBeanDaily}. */
public class TestSerialDateBeanDaily extends OpenCmsTestCase {

    /** empty sorted set of dates. */
    private static final SortedSet<Date> EMPTY_SORTED_SET_DATES = new TreeSet<>();

    /** Several tests for @{link org.opencms.widgets.serialdate.I_CmsSerialDateBean#getDates}. */
    public void testGetDates() {

        Calendar startDate = new GregorianCalendar(2017, 05, 28, 15, 05); // 28.06.2017 15:05 Mi
        Calendar endDate = new GregorianCalendar(2017, 05, 28, 16, 35); // 28.06.2017 16:35 Mi
        Calendar serialEndDate = new GregorianCalendar(2017, 06, 03, 01, 35); // 03.07.2017 01:35 Mo
        Calendar date1 = startDate;
        Calendar date2 = new GregorianCalendar(2017, 05, 29, 15, 05); // 29.06.2017 15:05 Do
        Calendar date3 = new GregorianCalendar(2017, 05, 30, 15, 05); // 30.06.2017 15:05 Fr
        Calendar date4 = new GregorianCalendar(2017, 06, 01, 15, 05); // 01.07.2017 15:05 Sa
        Calendar date5 = new GregorianCalendar(2017, 06, 02, 15, 05); // 02.07.2017 15:05 So
        Calendar date6 = new GregorianCalendar(2017, 06, 03, 15, 05); // 03.07.2017 15:05 Mo
        List<Calendar> dates = new ArrayList<>(6);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);
        dates.add(date5);
        dates.add(date6);

        // the tested bean
        CmsSerialDateBeanDaily dailyBean = null;
        //the dates the bean yields
        Collection<Long> beanDates = null;

        // every day till end date
        dailyBean = new CmsSerialDateBeanDaily(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0,
            EMPTY_SORTED_SET_DATES,
            1);
        beanDates = dailyBean.getDatesAsLong();
        assertEquals("There should be six dates", 6, beanDates.size());
        int i = 0;
        for (Long d : beanDates) {
            assertEquals(dates.get(i++).getTimeInMillis(), d.longValue());
        }

        // every 3rd day till end date
        dailyBean = new CmsSerialDateBeanDaily(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.DATE,
            serialEndDate.getTime(),
            0,
            EMPTY_SORTED_SET_DATES,
            3);
        beanDates = dailyBean.getDatesAsLong();
        Iterator<Long> it = beanDates.iterator();
        assertEquals("There should be two dates", 2, beanDates.size());
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(3).getTimeInMillis(), it.next().longValue());

        // every day max. 6 occurrences
        dailyBean = new CmsSerialDateBeanDaily(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            6,
            EMPTY_SORTED_SET_DATES,
            1);
        beanDates = dailyBean.getDatesAsLong();
        assertEquals("There should be six dates", 6, beanDates.size());
        i = 0;
        for (Long d : beanDates) {
            assertEquals(dates.get(i++).getTimeInMillis(), d.longValue());
        }

        // every 3rd day till end date
        dailyBean = new CmsSerialDateBeanDaily(
            startDate.getTime(),
            endDate.getTime(),
            false,
            EndType.TIMES,
            null,
            2,
            EMPTY_SORTED_SET_DATES,
            3);
        beanDates = dailyBean.getDatesAsLong();
        assertEquals("There should be two dates", 2, beanDates.size());
        it = beanDates.iterator();
        assertEquals(dates.get(0).getTimeInMillis(), it.next().longValue());
        assertEquals(dates.get(3).getTimeInMillis(), it.next().longValue());
    }
}
