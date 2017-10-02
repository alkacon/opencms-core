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
import org.opencms.acacia.shared.I_CmsSerialDateValue.PatternType;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekDay;
import org.opencms.acacia.shared.I_CmsSerialDateValue.WeekOfMonth;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsUUID;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

/** Tests for the serial date wrapper, targeting on transformations from/to Strings. */
public class TestSerialDateValue extends OpenCmsTestCase {

    /**
     * Test for the "current till end" flag.
     */
    @Test
    public void testCurrentTillEnd() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"NONE\"}, \"currenttillend\":false}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        // check if the flag is read correctly
        assertFalse(wrapper.isCurrentTillEnd());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);

        patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"NONE\"}}";
        wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        // check if the flag is read correctly
        assertTrue(wrapper.isCurrentTillEnd());
        // re-wrap
        rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the daily pattern, whole day and defined on a daily base, as well as with exceptions and occurrences specified.
     */
    @Test
    public void testDailyEndTimesWithExceptions() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"wholeday\":true, \"pattern\":{\"type\":\"DAILY\", \"interval\":\"5\"}, \"exceptions\":[\"1491289200000\",\"1491462000000\"], \"occurrences\":\"3\"}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(true, wrapper.isWholeDay());
        // end
        assertEquals(EndType.TIMES, wrapper.getEndType());
        assertEquals(3, wrapper.getOccurrences());
        // pattern
        assertEquals(PatternType.DAILY, wrapper.getPatternType());
        assertEquals(false, wrapper.isEveryWorkingDay());
        assertEquals(5, wrapper.getInterval());
        // exceptions
        SortedSet<Date> exceptions = new TreeSet<>();
        exceptions.add(new Date(1491289200000L));
        exceptions.add(new Date(1491462000000L));
        assertEquals(exceptions, wrapper.getExceptions());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the daily pattern, not whole day and defintion on for workdays with a series end date specified.
     */
    @Test
    public void testDailyWorkingDayEndDate() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"DAILY\", \"everyworkingday\":true}, \"enddate\":\"1492207200000\"}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(false, wrapper.isWholeDay());
        // end
        assertEquals(EndType.DATE, wrapper.getEndType());
        assertEquals(1492207200000L, wrapper.getSeriesEndDate().getTime());
        // pattern
        assertEquals(PatternType.DAILY, wrapper.getPatternType());
        assertEquals(true, wrapper.isEveryWorkingDay());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the yearly pattern, specified by week of month and weekday.
     */
    @Test
    public void testDateOnlyInitialization() {

        String patternDefinition = "1491202800000";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(null, wrapper.getEnd());
        assertEquals(false, wrapper.isWholeDay());
        // end
        assertEquals(EndType.SINGLE, wrapper.getEndType());
        // pattern
        assertEquals(PatternType.NONE, wrapper.getPatternType());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the individual pattern.
     */
    @Test
    public void testIndividual() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"INDIVIDUAL\", \"dates\":[\"1501489020000\",\"1501748220000\"]}}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(false, wrapper.isWholeDay());
        // pattern
        assertEquals(PatternType.INDIVIDUAL, wrapper.getPatternType());
        SortedSet<Date> dates = new TreeSet<>();
        dates.add(new Date(1501489020000L));
        dates.add(new Date(1501748220000L));
        assertEquals(dates, wrapper.getIndividualDates());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test with missing to date.
     */
    @Test
    public void testMissingToDate() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"pattern\":{\"type\":\"NONE\"}}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertNull(wrapper.getEnd());
        assertEquals(false, wrapper.isWholeDay());
        // end
        assertEquals(EndType.SINGLE, wrapper.getEndType());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the monthly pattern, specified by day of month.
     */
    @Test
    public void testMonthlyDay() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"MONTHLY\", \"interval\":\"2\", \"day\":\"15\"}, \"occurrences\":\"3\"}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(false, wrapper.isWholeDay());
        // end
        assertEquals(EndType.TIMES, wrapper.getEndType());
        assertEquals(3, wrapper.getOccurrences());
        // pattern
        assertEquals(PatternType.MONTHLY, wrapper.getPatternType());
        assertEquals(15, wrapper.getDayOfMonth());
        assertEquals(null, wrapper.getWeekDay());
        assertEquals(2, wrapper.getInterval());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the monthly pattern, specified by day of month.
     */
    @Test
    public void testMonthlyWeeks() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"MONTHLY\", \"interval\":\"5\", \"weekdays\":[\"WEDNESDAY\"], \"weeks\":[\"SECOND\",\"LAST\"]}, \"occurrences\":\"3\"}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(false, wrapper.isWholeDay());
        // end
        assertEquals(EndType.TIMES, wrapper.getEndType());
        assertEquals(3, wrapper.getOccurrences());
        // pattern
        assertEquals(PatternType.MONTHLY, wrapper.getPatternType());
        assertEquals(WeekDay.WEDNESDAY, wrapper.getWeekDay());
        SortedSet<WeekOfMonth> weeks = new TreeSet<>();
        weeks.add(WeekOfMonth.SECOND);
        weeks.add(WeekOfMonth.LAST);
        assertEquals(weeks, wrapper.getWeeksOfMonth());
        assertEquals(5, wrapper.getInterval());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the single event.
     */
    @Test
    public void testNone() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"NONE\"}}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(false, wrapper.isWholeDay());
        // pattern
        assertEquals(PatternType.NONE, wrapper.getPatternType());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test if the parent pattern series id is read correctly.
     */
    @Test
    public void testParentSeriesId() {

        String parentId = "6d642ad9-5c78-11e5-96ab-0242ac11002b";
        String patternDefinitionWithParent = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"NONE\"}, \"parentseries\":\""
            + parentId
            + "\"}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinitionWithParent);
        assertTrue(wrapper.isValid());
        assertTrue(wrapper.isFromOtherSeries());
        assertEquals(new CmsUUID(parentId), wrapper.getParentSeriesId());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);

        String patternDefinitionWithoutParent = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"NONE\"}}";
        wrapper = new CmsSerialDateValue(patternDefinitionWithoutParent);
        assertFalse(wrapper.isFromOtherSeries());
        assertNull(wrapper.getParentSeriesId());
        // re-wrap
        rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the weekly pattern.
     */
    @Test
    public void testWeekly() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"WEEKLY\", \"interval\":\"5\", \"weekdays\":[\"TUESDAY\",\"THURSDAY\"]}, \"occurrences\":\"3\"}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(false, wrapper.isWholeDay());
        // end
        assertEquals(EndType.TIMES, wrapper.getEndType());
        assertEquals(3, wrapper.getOccurrences());
        // pattern
        assertEquals(PatternType.WEEKLY, wrapper.getPatternType());
        SortedSet<WeekDay> weekdays = new TreeSet<>();
        weekdays.add(WeekDay.TUESDAY);
        weekdays.add(WeekDay.THURSDAY);
        assertEquals(weekdays, wrapper.getWeekDays());
        assertEquals(5, wrapper.getInterval());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the yearly pattern, specified by day of month.
     */
    @Test
    public void testYearlyDay() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"YEARLY\", \"day\":\"31\", \"month\":\"JULY\"}, \"occurrences\":\"3\"}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(false, wrapper.isWholeDay());
        // end
        assertEquals(EndType.TIMES, wrapper.getEndType());
        assertEquals(3, wrapper.getOccurrences());
        // pattern
        assertEquals(PatternType.YEARLY, wrapper.getPatternType());
        assertEquals(null, wrapper.getWeekDay());
        assertEquals(null, wrapper.getWeekOfMonth());
        assertEquals(31, wrapper.getDayOfMonth());
        assertEquals(Month.JULY, wrapper.getMonth());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }

    /**
     * Test for the yearly pattern, specified by week of month and weekday.
     */
    @Test
    public void testYearlyWeeks() {

        String patternDefinition = "{\"from\":\"1491202800000\", \"to\":\"1491231600000\", \"pattern\":{\"type\":\"YEARLY\", \"weekdays\":[\"WEDNESDAY\"], \"weeks\":[\"SECOND\"], \"month\":\"JULY\"}, \"occurrences\":\"3\"}";
        CmsSerialDateValue wrapper = new CmsSerialDateValue(patternDefinition);
        // general
        assertTrue(wrapper.isValid());
        assertEquals(1491202800000L, wrapper.getStart().getTime());
        assertEquals(1491231600000L, wrapper.getEnd().getTime());
        assertEquals(false, wrapper.isWholeDay());
        // end
        assertEquals(EndType.TIMES, wrapper.getEndType());
        assertEquals(3, wrapper.getOccurrences());
        // pattern
        assertEquals(PatternType.YEARLY, wrapper.getPatternType());
        assertEquals(WeekDay.WEDNESDAY, wrapper.getWeekDay());
        assertEquals(WeekOfMonth.SECOND, wrapper.getWeekOfMonth());
        assertEquals(Month.JULY, wrapper.getMonth());
        // re-wrap
        CmsSerialDateValue rewrap = new CmsSerialDateValue(wrapper.toString());
        assertEquals(wrapper, rewrap);
    }
}