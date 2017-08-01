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

package org.opencms.acacia.client.widgets.serialdate;

import org.opencms.acacia.shared.I_CmsSerialDateValue;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Client-side implementation of {@link I_CmsSerialDateValue}.
 * The implementation additionally has setters for the various values of the serial date specification.
 */
public class CmsSerialDateValueWrapper implements I_CmsObservableSerialDateValue {

    /** Start date and time of the first event in the series. */
    private Date m_start;
    /** End date and time of the first event in the series. */
    private Date m_end;
    /** Last day events of the series should take place. */
    private Date m_seriesEndDate;
    /** Maximal number of occurrences of the event. */
    private int m_seriesOccurrences;
    /** The interval between two events (e.g., number of days, weeks, month, years). */
    private int m_interval;
    /** The day of the month when the event should happen. */
    private int m_dayOfMonth;
    /** The weekdays at which the event should happen. */
    private final SortedSet<WeekDay> m_weekDays = new TreeSet<>();
    /** The recursion pattern of the event series. */
    private PatternType m_patterntype;
    /** The weeks in a month where the event should happen. */
    private final SortedSet<WeekOfMonth> m_weeksOfMonth = new TreeSet<>();
    /** Dates in the event series, where the event is not taking place. */
    private final SortedSet<Date> m_exceptions = new TreeSet<>();
    /** Individual dates, where the event takes place. */
    private final SortedSet<Date> m_individualDates = new TreeSet<>();
    /** Flag, indicating if the event should take place on every working day. */
    private boolean m_isEveryWorkingDay;
    /** Flag, indicating if the event lasts all the day. */
    private boolean m_isWholeDay;
    /** Month in which the event takes place. */
    private Month m_month = Month.JANUARY;
    /** The list of value change observers. */
    Collection<I_CmsSerialDateValueChangeObserver> m_valueChangeObservers = new HashSet<>();
    /** The end type of the series. */
    private EndType m_endType;

    /** Default constructor, setting the default state of the the serial date widget. */
    public CmsSerialDateValueWrapper() {
        setDefaultValue();
    }

    /**
     * Add a week of month.
     * @param week the week to add.
     */
    public void addWeekOfMonth(WeekOfMonth week) {

        m_weeksOfMonth.add(week);
    }

    /**
     * Clear the exceptions.
     */
    public void clearExceptions() {

        m_exceptions.clear();
    }

    /**
     * Clear the individual dates.
     */
    public void clearIndividualDates() {

        m_individualDates.clear();

    }

    /**
     * Clear the week days.
     */
    public void clearWeekDays() {

        m_weekDays.clear();

    }

    /**
     * Clear the weeks of month.
     */
    public void clearWeeksOfMonth() {

        m_weeksOfMonth.clear();

    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        if (o instanceof I_CmsSerialDateValue) {
            I_CmsSerialDateValue val = (I_CmsSerialDateValue)o;
            return (val.getDayOfMonth() == this.getDayOfMonth())
                && (val.isEveryWorkingDay() == this.isEveryWorkingDay())
                && (val.isWholeDay() == this.isWholeDay())
                && Objects.equals(val.getEnd(), this.getEnd())
                && Objects.equals(val.getEndType(), this.getEndType())
                && Objects.equals(val.getExceptions(), this.getExceptions())
                && Objects.equals(val.getIndividualDates(), this.getIndividualDates())
                && (val.getInterval() == this.getInterval())
                && Objects.equals(val.getMonth(), this.getMonth())
                && (val.getOccurrences() == this.getOccurrences())
                && Objects.equals(val.getPatternType(), this.getPatternType())
                && Objects.equals(val.getSeriesEndDate(), this.getSeriesEndDate())
                && Objects.equals(val.getStart(), this.getStart())
                && Objects.equals(val.getWeekDay(), this.getWeekDay())
                && Objects.equals(val.getWeekDays(), this.getWeekDays())
                && Objects.equals(val.getWeekOfMonth(), this.getWeekOfMonth())
                && Objects.equals(val.getWeeksOfMonth(), this.getWeeksOfMonth());
        }
        return false;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getDayOfMonth()
     */
    public int getDayOfMonth() {

        return m_dayOfMonth;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getEnd()
     */
    public Date getEnd() {

        return m_end;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getEndType()
     */
    public EndType getEndType() {

        return m_endType;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getExceptions()
     */
    public SortedSet<Date> getExceptions() {

        return new TreeSet<>(m_exceptions);
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getIndividualDates()
     */
    public SortedSet<Date> getIndividualDates() {

        return new TreeSet<>(m_individualDates);
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getInterval()
     */
    public int getInterval() {

        return m_interval;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getMonth()
     */
    public Month getMonth() {

        return m_month;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getOccurrences()
     */
    public int getOccurrences() {

        return m_seriesOccurrences;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getPatternType()
     */
    public PatternType getPatternType() {

        return m_patterntype;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getSeriesEndDate()
     */
    public Date getSeriesEndDate() {

        return m_seriesEndDate;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getStart()
     */
    public Date getStart() {

        return m_start;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeekDay()
     */
    public WeekDay getWeekDay() {

        if (m_weekDays.size() > 0) {
            return m_weekDays.first();
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeekDays()
     */
    public SortedSet<WeekDay> getWeekDays() {

        return new TreeSet<>(m_weekDays);
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeekOfMonth()
     */
    public WeekOfMonth getWeekOfMonth() {

        if (m_weeksOfMonth.size() > 0) {
            return m_weeksOfMonth.iterator().next();
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeeksOfMonth()
     */
    @Override
    public SortedSet<WeekOfMonth> getWeeksOfMonth() {

        return new TreeSet<>(m_weeksOfMonth);
    }

    /**
     * Returns a flag, indicating if exceptions are present.
     * @return a flag, indicating if exceptions are present.
     */
    public boolean hasExceptions() {

        return !getExceptions().isEmpty();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return Objects.hash(
            Boolean.valueOf(this.isEveryWorkingDay()),
            Boolean.valueOf(this.isWholeDay()),
            Integer.valueOf(this.getDayOfMonth()),
            this.getEnd(),
            this.getEndType(),
            this.getExceptions(),
            this.getIndividualDates(),
            Integer.valueOf(this.getInterval()),
            this.getMonth(),
            Integer.valueOf(this.getOccurrences()),
            this.getPatternType(),
            this.getSeriesEndDate(),
            this.getStart(),
            this.getWeekDay(),
            this.getWeekDays(),
            this.getWeekOfMonth(),
            this.getWeeksOfMonth());

    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isEveryWorkingDay()
     */
    public boolean isEveryWorkingDay() {

        return m_isEveryWorkingDay;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isWholeDay()
     */
    public boolean isWholeDay() {

        return m_isWholeDay;
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsObservableSerialDateValue#registerValueChangeObserver(org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver)
     */
    public void registerValueChangeObserver(I_CmsSerialDateValueChangeObserver obs) {

        m_valueChangeObservers.add(obs);

    }

    /**
     * Remove a week of month.
     * @param week the week to remove.
     */
    public void removeWeekOfMonth(WeekOfMonth week) {

        m_weeksOfMonth.remove(week);
    }

    /**
     * Set the day of the month, the event should take place.
     * @param dayOfMonth the day of the month to set.
     */
    public void setDayOfMonth(int dayOfMonth) {

        m_dayOfMonth = dayOfMonth;
    }

    /**
     * Set the end time for the event.
     * @param date the end time to set.
     */
    public void setEnd(Date date) {

        m_end = date;
    }

    /**
     * Set the end type of the series.
     * @param endType the end type to set.
     */
    public void setEndType(EndType endType) {

        m_endType = endType;
    }

    /**
     * Set the flag, indicating if the event should take place every working day.
     * @param isEveryWorkingDay the flag, indicating if the event should take place every working day.
     */
    public void setEveryWorkingDay(boolean isEveryWorkingDay) {

        m_isEveryWorkingDay = isEveryWorkingDay;

    }

    /**
     * Set dates where the event should not take place, even if they are part of the series.
     * @param dates dates to set.
     */
    public void setExceptions(SortedSet<Date> dates) {

        m_exceptions.clear();
        if (null != dates) {
            m_exceptions.addAll(dates);
        }

    }

    /**
     * Set the individual dates where the event should take place.
     * @param dates the dates to set.
     */
    public void setIndividualDates(SortedSet<Date> dates) {

        m_individualDates.clear();
        if (null != dates) {
            m_individualDates.addAll(dates);
        }
        for (Date d : getExceptions()) {
            if (!m_individualDates.contains(d)) {
                m_exceptions.remove(d);
            }
        }

    }

    /**
     * Set the pattern type specific interval between two events, e.g., number of days, weeks, month, years.
     * @param interval the interval to set.
     */
    public void setInterval(int interval) {

        m_interval = interval;
    }

    /**
     * Set the month in which the event should take place.
     * @param month the month to set.
     */
    public void setMonth(Month month) {

        m_month = null == month ? Month.JANUARY : month;

    }

    /**
     * Set the number of occurrences of the event.
     * @param occurrences the number of occurrences to set.
     */
    public void setOccurrences(int occurrences) {

        m_seriesEndDate = null; // important, otherwise the end date is still assumed as criteria to end the series.
        m_seriesOccurrences = occurrences;

    }

    /**
     * Set the pattern type of the event series.<p>
     *
     * All pattern specific values are reset.
     *
     * @param type the pattern type to set.
     */
    public void setPatternType(PatternType type) {

        m_patterntype = type;
    }

    /**
     * Set the last day events of the series should occur.
     * @param date the day to set.
     */
    public void setSeriesEndDate(Date date) {

        m_seriesEndDate = date;

    }

    /**
     * Set the start time of the events. Unless you specify a single event, the day information is discarded.
     * @param date the time to set.
     */
    public void setStart(Date date) {

        m_start = date;
    }

    /**
     * Set the value as provided.
     * @param value the serial date value as JSON string.
     */
    public final void setValue(String value) {

        if ((null == value) || value.isEmpty()) {
            setDefaultValue();
        } else {
            try {
                tryToSetParsedValue(value);
            } catch (@SuppressWarnings("unused") Exception e) {
                setDefaultValue();
            }
        }
        notifyOnValueChange();
    }

    /**
     * Set the week day the events should occur.
     * @param weekDay the week day to set.
     */
    public void setWeekDay(WeekDay weekDay) {

        SortedSet<WeekDay> wds = new TreeSet<>();
        if (null != weekDay) {
            wds.add(weekDay);
        }
        setWeekDays(wds);

    }

    /**
     * Set the week days the events should occur.
     * @param weekDays the week days to set.
     */
    public void setWeekDays(SortedSet<WeekDay> weekDays) {

        m_weekDays.clear();
        if (null != weekDays) {
            m_weekDays.addAll(weekDays);
        }
    }

    /**
     * Set the week of the month the events should occur.
     * @param weekOfMonth the week of month to set (first to fifth, where fifth means last).
     */
    public void setWeekOfMonth(WeekOfMonth weekOfMonth) {

        SortedSet<WeekOfMonth> woms = new TreeSet<>();
        if (null != weekOfMonth) {
            woms.add(weekOfMonth);
        }
        setWeeksOfMonth(woms);
    }

    /**
     * Set the weeks of the month the events should occur.
     * @param weeksOfMonth the weeks of month to set (first to fifth, where fifth means last).
     */
    public void setWeeksOfMonth(SortedSet<WeekOfMonth> weeksOfMonth) {

        m_weeksOfMonth.clear();
        if (null != weeksOfMonth) {
            m_weeksOfMonth.addAll(weeksOfMonth);
        }

    }

    /**
     * Set the flag, indicating if the event last the whole day/whole days.
     * @param isWholeDay the flag to set
     */
    public void setWholeDay(Boolean isWholeDay) {

        m_isWholeDay = (null != isWholeDay) && isWholeDay.equals(Boolean.TRUE);

    }

    /**
     * Convert the information from the wrapper to a JSON object.
     * @return the serial date information as JSON.
     */
    public JSONValue toJson() {

        JSONObject result = new JSONObject();
        result.put(JsonKey.START, dateToJson(getStart()));
        result.put(JsonKey.END, dateToJson(getEnd()));
        if (m_isWholeDay) {
            result.put(JsonKey.WHOLE_DAY, JSONBoolean.getInstance(true));
        }
        JSONObject pattern = patternToJson();
        result.put(JsonKey.PATTERN, pattern);
        SortedSet<Date> exceptions = getExceptions();
        if (exceptions.size() > 0) {
            result.put(JsonKey.EXCEPTIONS, datesToJsonArray(exceptions));
        }
        switch (getEndType()) {
            case DATE:
                result.put(JsonKey.SERIES_ENDDATE, dateToJson(getSeriesEndDate()));
                break;
            case TIMES:
                result.put(JsonKey.SERIES_OCCURRENCES, new JSONString(String.valueOf(getOccurrences())));
                break;
            case SINGLE:
            default:
                break;
        }
        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        JSONValue json = toJson();
        return json.toString();
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsObservableSerialDateValue#unregisterValueChangeObserver(org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver)
     */
    public void unregisterValueChangeObserver(I_CmsSerialDateValueChangeObserver obs) {

        m_valueChangeObservers.remove(obs);

    }

    /**
     * Converts a collection of dates to a JSON array with the long representation of the dates as strings.
     * @param dates the list to convert.
     * @return JSON array with long values of dates as string
     */
    private JSONValue datesToJsonArray(Collection<Date> dates) {

        if (null != dates) {
            JSONArray result = new JSONArray();
            for (Date d : dates) {
                result.set(result.size(), dateToJson(d));
            }
            return result;
        }
        return null;
    }

    /**
     * Convert a date to the String representation we use in the JSON.
     * @param d the date to convert
     * @return the String representation we use in the JSON.
     */
    private JSONValue dateToJson(Date d) {

        return null != d ? new JSONString(Long.toString(d.getTime())) : null;
    }

    /**
     * Notifies all observers on value changes.
     */
    private void notifyOnValueChange() {

        for (I_CmsSerialDateValueChangeObserver obs : m_valueChangeObservers) {
            obs.onValueChange();
        }

    }

    /**
     * Generates the JSON object storing the pattern information.
     * @return the JSON object storing the pattern information.
     */
    private JSONObject patternToJson() {

        JSONObject pattern = new JSONObject();
        pattern.put(JsonKey.PATTERN_TYPE, new JSONString(getPatternType().toString()));
        switch (getPatternType()) {
            case DAILY:
                if (isEveryWorkingDay()) {
                    pattern.put(JsonKey.PATTERN_EVERYWORKINGDAY, JSONBoolean.getInstance(true));
                } else {
                    pattern.put(JsonKey.PATTERN_INTERVAL, new JSONString(String.valueOf(getInterval())));
                }
                break;
            case WEEKLY:
                pattern.put(JsonKey.PATTERN_INTERVAL, new JSONString(String.valueOf(getInterval())));
                pattern.put(JsonKey.PATTERN_WEEKDAYS, toJsonStringList(getWeekDays()));
                break;
            case MONTHLY:
                pattern.put(JsonKey.PATTERN_INTERVAL, new JSONString(String.valueOf(getInterval())));
                if (null != getWeekDay()) {
                    pattern.put(JsonKey.PATTERN_WEEKS_OF_MONTH, toJsonStringList(getWeeksOfMonth()));
                    pattern.put(JsonKey.PATTERN_WEEKDAYS, toJsonStringList(getWeekDays()));
                } else {
                    pattern.put(JsonKey.PATTERN_DAY_OF_MONTH, new JSONString(String.valueOf(getDayOfMonth())));
                }
                break;
            case YEARLY:
                pattern.put(JsonKey.PATTERN_MONTH, new JSONString(getMonth().toString()));
                if (null != getWeekDay()) {
                    pattern.put(JsonKey.PATTERN_WEEKS_OF_MONTH, toJsonStringList(getWeeksOfMonth()));
                    pattern.put(JsonKey.PATTERN_WEEKDAYS, toJsonStringList(getWeekDays()));
                } else {
                    pattern.put(JsonKey.PATTERN_DAY_OF_MONTH, new JSONString(String.valueOf(getDayOfMonth())));
                }
                break;
            case INDIVIDUAL:
                pattern.put(JsonKey.PATTERN_DATES, datesToJsonArray(getIndividualDates()));
                break;
            case NONE:
            default:
                break;
        }
        return pattern;
    }

    /**
     * Extracts the dates from a JSON array.
     * @param json the JSON array where the dates are stored in.
     * @return list of the extracted dates.
     */
    private SortedSet<Date> readDates(JSONValue json) {

        JSONArray array = null == json ? null : json.isArray();
        if (null != array) {
            SortedSet<Date> result = new TreeSet<>();
            for (int i = 0; i < array.size(); i++) {
                Long l = readOptionalLong(array.get(i), null);
                if (null != l) {
                    result.add(new Date(l.longValue()));
                }
            }
            return result;
        }
        return new TreeSet<>();
    }

    /**
     * Read an optional boolean value form a JSON value.
     * @param val the JSON value that should represent the boolean.
     * @param defaultValue the default value, to be returned if the boolean can not be read from the JSON value.
     * @return the boolean from the JSON or the default value if reading the boolean fails.
     */
    private boolean readOptionalBoolean(JSONValue val, boolean defaultValue) {

        JSONBoolean b = null == val ? null : val.isBoolean();
        if (b != null) {
            return b.booleanValue();
        }
        return defaultValue;
    }

    /**
     * Read an optional int value form a JSON value.
     * @param val the JSON value that should represent the int.
     * @param defaultValue the default value, to be returned if the int can not be read from the JSON value.
     * @return the int from the JSON or the default value if reading the int fails.
     */
    private int readOptionalInt(JSONValue val, int defaultValue) {

        JSONString str = null == val ? null : val.isString();
        if (str != null) {
            try {
                return Integer.valueOf(str.stringValue()).intValue();
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                // Do nothing, return default value
            }
        }
        return defaultValue;
    }

    /**
     * Read an optional Long value form a JSON value.
     * @param val the JSON value that should represent the Long.
     * @param defaultValue the default value, to be returned if the Long can not be read from the JSON value.
     * @return the Long from the JSON or the default value if reading the Long fails.
     */
    private Long readOptionalLong(JSONValue val, Long defaultValue) {

        JSONString str = null == val ? null : val.isString();
        if (str != null) {
            try {
                return Long.valueOf(str.stringValue());
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                // do nothing - return the default value
            }
        }
        return defaultValue;
    }

    /**
     * Read an optional month value form a JSON value.
     * @param val the JSON value that should represent the month.
     * @param defaultValue the default value, to be returned if the month can not be read from the JSON value.
     * @return the month from the JSON or the default value if reading the month fails.
     */
    private Month readOptionalMonth(JSONValue val, Month defaultValue) {

        String str = readOptionalString(val, null);
        if (null != str) {
            try {
                return Month.valueOf(str);
            } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                // Do nothing -return the default value
            }
        }
        return defaultValue;
    }

    /**
     * Read an optional string value form a JSON value.
     * @param val the JSON value that should represent the string.
     * @param defaultValue the default value, to be returned if the string can not be read from the JSON value.
     * @return the string from the JSON or the default value if reading the string fails.
     */
    private String readOptionalString(JSONValue val, String defaultValue) {

        JSONString str = null == val ? null : val.isString();
        if (str != null) {
            return str.stringValue();
        }
        return defaultValue;
    }

    /**
     * Read pattern information from the provided JSON object.
     * @param patternJson the JSON object containing the pattern information.
     */
    private void readPattern(JSONObject patternJson) {

        setPatternType(readPatternType(patternJson.get(JsonKey.PATTERN_TYPE)));
        setInterval(readOptionalInt(patternJson.get(JsonKey.PATTERN_INTERVAL), 0));
        setWeekDays(readWeekDays(patternJson.get(JsonKey.PATTERN_WEEKDAYS)));
        setDayOfMonth(readOptionalInt(patternJson.get(JsonKey.PATTERN_DAY_OF_MONTH), 0));
        setEveryWorkingDay(readOptionalBoolean(patternJson.get(JsonKey.PATTERN_EVERYWORKINGDAY), false));
        setWeeksOfMonth(readWeeksOfMonth(patternJson.get(JsonKey.PATTERN_WEEKS_OF_MONTH)));
        setIndividualDates(readDates(patternJson.get(JsonKey.PATTERN_DATES)));
        setMonth(readOptionalMonth(patternJson.get(JsonKey.PATTERN_MONTH), null));
        m_endType = (getPatternType().equals(PatternType.NONE) || getPatternType().equals(PatternType.INDIVIDUAL))
        ? EndType.SINGLE
        : getSeriesEndDate() != null ? EndType.DATE : EndType.TIMES;

    }

    /**
     * Reads the pattern type from the provided JSON. Defaults to type NONE.
     * @param val the JSON object containing the pattern type information.
     * @return the pattern type.
     */
    private PatternType readPatternType(JSONValue val) {

        PatternType patterntype;
        try {
            String str = readOptionalString(val, "");
            patterntype = PatternType.valueOf(str);
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            patterntype = PatternType.NONE;
        }
        return patterntype;
    }

    /**
     * Read a single weekday from the provided JSON value.
     * @param val the value to read the week day from.
     * @return the week day read
     * @throws IllegalArgumentException thrown if the provided JSON value is not the representation of a week day.
     */
    private WeekDay readWeekDay(JSONValue val) throws IllegalArgumentException {

        String str = readOptionalString(val, null);
        if (null != str) {
            return WeekDay.valueOf(str);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Reads the weekday information.
     * @param val the JSON object containing the weekday information.
     * @return the weekdays extracted, defaults to the empty list, if no information is found.
     */
    private SortedSet<WeekDay> readWeekDays(JSONValue val) {

        JSONArray array = null == val ? null : val.isArray();
        if (null != array) {
            SortedSet<WeekDay> result = new TreeSet<>();
            for (int i = 0; i < array.size(); i++) {
                try {
                    result.add(readWeekDay(array.get(i)));
                } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                    // Just skip
                }
            }
            return result;
        }
        return new TreeSet<>();
    }

    /**
     * Read "weeks of month" information from JSON.
     * @param json the JSON where information is read from.
     * @return the list of weeks read, defaults to the empty list.
     */
    private SortedSet<WeekOfMonth> readWeeksOfMonth(JSONValue json) {

        JSONArray array = null == json ? null : json.isArray();
        if (null != array) {
            SortedSet<WeekOfMonth> result = new TreeSet<>();
            for (int i = 0; i < array.size(); i++) {
                String weekStr = readOptionalString(array.get(i), null);
                try {
                    WeekOfMonth week = WeekOfMonth.valueOf(weekStr);
                    result.add(week);
                } catch (@SuppressWarnings("unused") IllegalArgumentException | NullPointerException e) {
                    // Just skip
                }
            }
            return result;
        }
        return new TreeSet<>();
    }

    /**
     * Sets the value to a default.
     */
    private void setDefaultValue() {

        Date now = new Date();
        m_start = now;
        m_end = now;
        m_patterntype = PatternType.NONE;
        m_dayOfMonth = 0;
        m_exceptions.clear();
        m_individualDates.clear();
        m_interval = 0;
        m_isEveryWorkingDay = false;
        m_isWholeDay = false;
        m_month = Month.JANUARY;
        m_seriesEndDate = null;
        m_seriesOccurrences = 0;
        m_weekDays.clear();
        m_weeksOfMonth.clear();
        m_valueChangeObservers.clear();
        m_endType = EndType.SINGLE;

    }

    /**
     * Convert a list of objects to a JSON array with the string representations of that objects.
     * @param list the list of objects.
     * @return the JSON array with the string representations.
     */
    private JSONValue toJsonStringList(Collection<? extends Object> list) {

        if (null != list) {
            JSONArray array = new JSONArray();
            for (Object o : list) {
                array.set(array.size(), new JSONString(o.toString()));
            }
            return array;
        } else {
            return null;
        }
    }

    /**
     * Try to set the value from the provided Json string.
     * @param value the value to set.
     * @throws Exception thrown if parsing fails.
     */
    private void tryToSetParsedValue(String value) throws Exception {

        JSONObject json = JSONParser.parseStrict(value).isObject();
        JSONValue val = json.get(JsonKey.START);
        m_start = new Date(readOptionalLong(val, Long.valueOf(0)).longValue());
        val = json.get(JsonKey.END);
        m_end = new Date(readOptionalLong(val, Long.valueOf(0)).longValue());
        m_isWholeDay = readOptionalBoolean(json.get(JsonKey.WHOLE_DAY), false);
        JSONObject patternJson = json.get(JsonKey.PATTERN).isObject();
        readPattern(patternJson);
        m_exceptions.clear();
        m_exceptions.addAll(readDates(json.get(JsonKey.EXCEPTIONS)));
        Long seriesEnd = readOptionalLong(json.get(JsonKey.SERIES_ENDDATE), null);
        if (null != seriesEnd) {
            m_seriesEndDate = new Date(seriesEnd.longValue());
        }
        m_seriesOccurrences = readOptionalInt(json.get(JsonKey.SERIES_OCCURRENCES), 0);
        m_endType = getPatternType().equals(PatternType.NONE) || getPatternType().equals(PatternType.INDIVIDUAL)
        ? EndType.SINGLE
        : null != getSeriesEndDate() ? EndType.DATE : EndType.TIMES;

    }
}
