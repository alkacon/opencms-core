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

package org.opencms.acacia.shared;

import org.opencms.util.CmsUUID;

import java.util.Date;
import java.util.SortedSet;

/** Interface to access serial date values easily. Used on client and server. */
public interface I_CmsSerialDateValue {

    /** Different types of serial dates. */
    public enum DateType {
        /** Only a single date is specified, which is not extracted from a series. */
        SINGLE,
        /** The date was extracted from a series. */
        EXTRACTED,
        /** A whole series of dates is specified. */
        SERIES;
    }

    /** Different types of conditions how serial dates can end. */
    public enum EndType {
        /** There is no end type, all dates are specified explicitly */
        SINGLE,
        /** An explicit end date is specified. */
        DATE,
        /** A number of occurrences for the event is specified */
        TIMES;
    }

    /** The JSON keys used in the JSON representation of serial date specifications. */
    static final class JsonKey {

        /** Start time of the first event. */
        public static final String START = "from";

        /** End time of the first event. */
        public static final String END = "to";

        /** Flag, indicating if the event last the whole day/whole days, or not. */
        public static final String WHOLE_DAY = "wholeday";

        /** Sub-object with the pattern related information. */
        public static final String PATTERN = "pattern";

        /** The pattern type. */
        public static final String PATTERN_TYPE = "type";

        /** The pattern specific interval. */
        public static final String PATTERN_INTERVAL = "interval";

        /** Flag, indicating if the event takes place every working day. */
        public static final String PATTERN_EVERYWORKINGDAY = "everyworkingday";

        /** Array with the weekdays, the event takes place. */
        public static final String PATTERN_WEEKDAYS = "weekdays";

        /** The day of the month where the event takes place. */
        public static final String PATTERN_DAY_OF_MONTH = "day";

        /** Array with the weeks of the month where the event takes place. */
        public static final String PATTERN_WEEKS_OF_MONTH = "weeks";

        /** Array with individual dates where the event takes place. */
        public static final String PATTERN_DATES = "dates";

        /** The month where the event takes place. */
        public static final String PATTERN_MONTH = "month";

        /** Array with dates where the event does not take place. */
        public static final String EXCEPTIONS = "exceptions";

        /** The last day the event can potentially take place. */
        public static final String SERIES_ENDDATE = "enddate";

        /** The maximal number of occurrences of the event. */
        public static final String SERIES_OCCURRENCES = "occurrences";

        /** The uuid of the series content, the event originally belonged to. */
        public static final String PARENT_SERIES = "parentseries";

        /** Flag, indicating if events are "current" events till the end, or only till the beginning. */
        public static final String CURRENT_TILL_END = "currenttillend";
    }

    /** Months as enumeration. */
    public enum Month {
        /** January */
        JANUARY,
        /** February */
        FEBRUARY,
        /** March */
        MARCH,
        /** April */
        APRIL,
        /** May */
        MAY,
        /** June */
        JUNE,
        /** July */
        JULY,
        /** August */
        AUGUST,
        /** September */
        SEPTEMBER,
        /** October */
        OCTOBER,
        /** November */
        NOVEMBER,
        /** December */
        DECEMBER;

        /**
         * Returns the (maximal) number of days the month can have.
         * I.e., for February 29 and for the other months either 30 or 31 depending on the month.
         * @return the (maximal) number of days the month can have.
         */
        public int getMaximalDay() {

            switch (this) {
                case JANUARY:
                    return 31;
                case FEBRUARY:
                    return 29;
                case MARCH:
                    return 31;
                case APRIL:
                    return 30;
                case MAY:
                    return 31;
                case JUNE:
                    return 30;
                case JULY:
                    return 31;
                case AUGUST:
                    return 31;
                case SEPTEMBER:
                    return 30;
                case OCTOBER:
                    return 31;
                case NOVEMBER:
                    return 30;
                case DECEMBER:
                    return 31;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    /** Type of the series. E.g., daily, weekly, etc. */
    public enum PatternType {
        /** No series at all. */
        NONE,
        /** Series is specified on a daily base. */
        DAILY,
        /** Series is specified on a weekly base. */
        WEEKLY,
        /** Series is specified on a monthly base. */
        MONTHLY,
        /** Series is specified on a yearly base. */
        YEARLY,
        /** Series is specified by individual dates. */
        INDIVIDUAL;
    }

    /** Enumeration representing the week days. */
    public enum WeekDay {
        /** Sunday */
        SUNDAY,
        /** Monday */
        MONDAY,
        /** Tuesday */
        TUESDAY,
        /** Wednesday */
        WEDNESDAY,
        /** Thursday */
        THURSDAY,
        /** Friday */
        FRIDAY,
        /** Saturday */
        SATURDAY;

        /** Conversion of integers to weekdays. The conversion is NOT the inverse of the ordinals.
         * The enumeration here starts with 1, i.e., fromInt(i).ordinal() == i-1.
         * The shift is added to get the numbers as used in {@link java.util.Calendar}.
         * @param i the number of the weekday (starting with 1 for Sunday)
         * @return the {@link WeekDay} that corresponds to the given number.
         */
        public static WeekDay fromInt(int i) {

            switch (i) {
                case 1:
                    return WeekDay.SUNDAY;
                case 2:
                    return WeekDay.MONDAY;
                case 3:
                    return WeekDay.TUESDAY;
                case 4:
                    return WeekDay.WEDNESDAY;
                case 5:
                    return WeekDay.THURSDAY;
                case 6:
                    return WeekDay.FRIDAY;
                case 7:
                    return WeekDay.SATURDAY;
                default:
                    throw new IllegalArgumentException();
            }
        }

        /**Object
         * Converts the {@link WeekDay} to it's corresponding number, i.e. it's ordinal plus 1.
         * The numbers correspond to the numbers for weekdays used by {@link java.util.Calendar}.
         *
         * @return the number corresponding to the weekday.
         */
        public int toInt() {

            return this.ordinal() + 1;
        }
    }

    /** Possible weeks of a month. */
    public enum WeekOfMonth {
        /** The first week of a month. */
        FIRST,
        /** The second week of a month. */
        SECOND,
        /** The third week of a month. */
        THIRD,
        /** The fourth week of a month. */
        FOURTH,
        /** The last week of a month. */
        LAST
    }

    /** Constant for the milliseconds of a day. */
    static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    /** The number of weekdays (seven). */
    static final int NUM_OF_WEEKDAYS = 7;

    /**
     * Returns a flag, indicating if the event ends at midnight.
     * @return a flag, indicating if the event ends at midnight.
     */
    boolean endsAtMidNight();

    /**
     * Returns the type of the specified date.
     * @return the type of the specified date.
     */
    DateType getDateType();

    /**
     * Returns the day of the month, the events should take place.
     * @return the day of the month, the events should take place.
     */
    int getDayOfMonth();

    /**
     * Returns the end time of the events.
     * @return the end time of the events.
     */
    Date getEnd();

    /**
     * Returns the end type of the event series.
     * @return the end type of the event series.
     */
    EndType getEndType();

    /**
     * Returns the dates, where the event should not take place.
     * @return the dates, where the event should not take place.
     */
    SortedSet<Date> getExceptions();

    /**
     * Returns the dates of an individual date series.
     * @return the dates of an individual date series.
     */
    SortedSet<Date> getIndividualDates();

    /**
     * Returns the pattern type specific interval of the event.
     * @return the pattern type specific interval of the event.
     */
    int getInterval();

    /**
     * Returns the month in which the events take place.
     * @return the month in which the events take place.
     */
    Month getMonth();

    /**
     * Returns the number of occurrences of the event.
     * @return the number of occurrences of the event.
     */
    int getOccurrences();

    /**
     * Returns the uuid of the content that holds the series, the current event was extracted from.
     * Or <code>null</code> if the current event series was not extracted from another one.
     * @return the uuid of the original series' content,
     *         or <code>null<code>, if the event is not extracted from another event series.
     */
    CmsUUID getParentSeriesId();

    /**
     * Returns the pattern type of the event series.
     * @return the pattern type of the event series.
     */
    PatternType getPatternType();

    /**
     * Returns the date of the last day, events of the series should take place.
     * @return the date of the last day, events of the series should take place.
     */
    Date getSeriesEndDate();

    /**
     * Returns the start time of the events.
     * @return the start time of the events.
     */
    Date getStart();

    /**
     * Returns the week day where the event should take place.
     * @return the week day where the event should take place.
     */
    WeekDay getWeekDay();

    /**
     * Returns the week days where the event should take place.
     * @return the week days where the event should take place.
     */
    SortedSet<WeekDay> getWeekDays();

    /**
     * Returns the week of the month, the event should take place.
     * @return the week of the month, the event should take place.
     */
    WeekOfMonth getWeekOfMonth();

    /**
     * Returns the weeks of the month, the event should take place.
     * @return the weeks of the month, the event should take place.
     */
    SortedSet<WeekOfMonth> getWeeksOfMonth();

    /**
     * Returns a flag, indicating if the events should be treated as "current" till they end (or only till they start).
     * @return <code>true</code> if the event is "current" till it ends, <code>false</code> if it is current till it starts.
     */
    boolean isCurrentTillEnd();

    /**
     * Returns a flag, indicating if the event should take place every working day.
     * @return a flag, indicating if the event should take place every working day.
     */
    boolean isEveryWorkingDay();

    /**
     * Returns a flag, indicating if the event is extracted from another series.
     * @return a flag, indicating if the event is extracted from another series.
     */
    boolean isFromOtherSeries();

    /**
     * Returns a flag, indicating if the value specifies a valid date (series).
     * @return a flag, indicating if the value specifies a valid date (series).
     */
    boolean isValid();

    /**
     * Returns a flag, indicating if the event last the whole day/whole days.
     * @return a flag, indicating if the event last the whole day/whole days.
     */
    boolean isWholeDay();

}
