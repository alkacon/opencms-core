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

import org.opencms.acacia.shared.CmsSerialDateUtil;
import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;

import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONString;

/** Server-side implementation of {@link org.opencms.acacia.shared.I_CmsSerialDateValue}. */
public class CmsSerialDateValueWrapper implements I_CmsSerialDateValue {

    /** Logger for the class. */
    private static final Log LOG = CmsLog.getLog(CmsSerialDateValueWrapper.class);

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
    private SortedSet<WeekDay> m_weekDays;
    /** The recursion pattern of the event series. */
    private PatternType m_patterntype;
    /** The weeks in a month where the event should happen. */
    private SortedSet<WeekOfMonth> m_weeksOfMonth;
    /** Dates in the event series, where the event is not taking place. */
    private SortedSet<Date> m_exceptions;
    /** Individual dates, where the event takes place. */
    private SortedSet<Date> m_individualDates;
    /** Flag, indicating if the event should take place on every working day. */
    private boolean m_isEveryWorkingDay;
    /** Flag, indicating if the event lasts all the day. */
    private boolean m_isWholeDay;
    /** Month in which the event takes place. */
    private Month m_month;

    /** Default constructor, setting the default state of the the serial date widget. */
    public CmsSerialDateValueWrapper() {
        Date now = new Date();
        m_start = now;
        m_end = now;
        m_patterntype = PatternType.NONE;
    }

    /**
     * Wraps the JSON specification of the serial date.
     * @param value JSON representation of the serial date as string.
     * @throws JSONException thrown if the representation is no correct JSON, or is not of the correct structure.s
     */
    public CmsSerialDateValueWrapper(String value)
    throws JSONException {
        this();
        if ((null != value) && !value.isEmpty()) {
            JSONObject json = new JSONObject(value);
            m_start = new Date(readOptionalLong(json, JsonKey.START, Long.valueOf(0)).longValue());
            m_end = new Date(readOptionalLong(json, JsonKey.END, Long.valueOf(0)).longValue());
            m_isWholeDay = readOptionalBoolean(json, JsonKey.WHOLE_DAY, false);
            JSONObject patternJson = json.getJSONObject(JsonKey.PATTERN);
            readPattern(patternJson);
            m_exceptions = readDates(readOptionalArray(json, JsonKey.EXCEPTIONS, null));
            Long seriesEnd = readOptionalLong(json, JsonKey.SERIES_ENDDATE, null);
            if (null != seriesEnd) {
                m_seriesEndDate = new Date(seriesEnd.longValue());
            }
            m_seriesOccurrences = readOptionalInt(json, JsonKey.SERIES_OCCURRENCES, 0);
        }
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

        if (getSeriesEndDate() != null) {
            return EndType.DATE;
        }
        if (getOccurrences() > 0) {
            return EndType.TIMES;
        }

        return EndType.SINGLE;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getExceptions()
     */
    public SortedSet<Date> getExceptions() {

        return m_exceptions;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getIndividualDates()
     */
    public SortedSet<Date> getIndividualDates() {

        return m_individualDates;
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

        if ((null != m_weekDays) && !m_weekDays.isEmpty()) {
            return m_weekDays.iterator().next();
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeekDays()
     */
    public SortedSet<WeekDay> getWeekDays() {

        return m_weekDays;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeekOfMonth()
     */
    public WeekOfMonth getWeekOfMonth() {

        if ((null != m_weeksOfMonth) && !m_weeksOfMonth.isEmpty()) {
            return m_weeksOfMonth.iterator().next();
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeeksOfMonth()
     */
    public SortedSet<WeekOfMonth> getWeeksOfMonth() {

        return m_weeksOfMonth;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isEveryWorkingDay()
     */
    public boolean isEveryWorkingDay() {

        return m_isEveryWorkingDay;
    }

    /**
     * Checks, if the value specifies a series of dates.
     * @return flag, indicating if the value really specifies a series of dates.
     */
    public boolean isValid() {

        return timesAreValid() && patternIsValid() && durationIsValid();
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isWholeDay()
     */
    public boolean isWholeDay() {

        return m_isWholeDay;
    }

    /**
     * Convert the information from the wrapper to a JSON object.
     * @return the serial date information as JSON.
     */
    public JSONObject toJson() {

        try {
            JSONObject result = new JSONObject();
            result.put(JsonKey.START, dateToJson(getStart()));
            result.put(JsonKey.END, dateToJson(getEnd()));
            if (m_isWholeDay) {
                result.put(JsonKey.WHOLE_DAY, JSONBoolean.getInstance(true));
            }
            JSONObject pattern = patternToJson();
            result.put(JsonKey.PATTERN, pattern);
            SortedSet<Date> exceptions = getExceptions();
            if (!exceptions.isEmpty()) {
                result.put(JsonKey.EXCEPTIONS, datesToJson(exceptions));
            }
            int occurrences = getOccurrences();
            if (occurrences > 0) {
                result.put(JsonKey.SERIES_OCCURRENCES, new JSONString("" + occurrences));
            }
            Date seriesEndDate = getSeriesEndDate();
            if (null != seriesEndDate) {
                result.put(JsonKey.SERIES_ENDDATE, dateToJson(seriesEndDate));
            }
            return result;
        } catch (JSONException e) {
            LOG.error("Could not convert Serial date value to JSON.", e);
            return null;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        JSONObject json;
        json = toJson();
        return null != json ? json.toString() : "";
    }

    /**
     * Converts a list of dates to a Json array with the long representation of the dates as strings.
     * @param individualDates the list to convert.
     * @return Json array with long values of dates as string
     */
    private JSONArray datesToJson(Collection<Date> individualDates) {

        if (null != individualDates) {
            JSONArray result = new JSONArray();
            for (Date d : individualDates) {
                result.put(dateToJson(d));
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
    private String dateToJson(Date d) {

        return Long.toString(d.getTime());
    }

    /**
     * Checks if the provided duration information is valid.
     * @return a flag, indicating if the duration information is valid.
     */
    private boolean durationIsValid() {

        switch (getEndType()) {
            case DATE:
                return (getStart().getTime() < (getSeriesEndDate().getTime() + DAY_IN_MILLIS))
                    && !CmsSerialDateBeanFactory.createSerialDateBean(this).hasTooManyDates();
            case TIMES:
                int o = getOccurrences();
                return (o > 0) && (o <= CmsSerialDateUtil.getMaxEvents());
            default:
                return true;
        }

    }

    /**
     * Check if the provided day of month is valid.
     * @return flag, indicating if the day of month is valid.
     */
    private boolean isDayOfMonthValid() {

        return (getDayOfMonth() > 0) && (getDayOfMonth() < 32);
    }

    /**
     * Check if the interval is valid.
     * @return flag, indicating if the interval is valid.
     */
    private boolean isIntervalValid() {

        return getInterval() > 0;
    }

    /**
     * Check if a month is set.
     * @return flag, indicating if a month is set.
     */
    private boolean isMonthSet() {

        return getMonth() != null;
    }

    /**
     * Check if a weekday is set.
     * @return flag, indicating if a weekday is set.
     */
    private boolean isWeekDaySet() {

        return getWeekDays().size() > 0;
    }

    /**
     * Check if a week of month is set.
     * @return flag, indicating if a week of month is set.
     */
    private boolean isWeekOfMonthSet() {

        return getWeeksOfMonth().size() > 0;
    }

    /**
     * Check, if all values used for calculating the series for a specific pattern are valid.
     * @return flag, indicating if the values are valid.
     */
    private boolean patternIsValid() {

        switch (getPatternType()) {
            case DAILY:
                return isIntervalValid() || isEveryWorkingDay();
            case WEEKLY:
                return isIntervalValid() && isWeekDaySet();
            case MONTHLY:
                return isIntervalValid() && (isWeekDaySet() ? isWeekOfMonthSet() : isDayOfMonthValid());
            case YEARLY:
                return isMonthSet() && (isWeekDaySet() ? isWeekOfMonthSet() : isDayOfMonthValid());
            case INDIVIDUAL:
                return getIndividualDates().size() > CmsSerialDateUtil.getMaxEvents();
            case NONE:
            default:
                return true;
        }
    }

    /**
     * Generates the JSON object storing the pattern information.
     * @return the JSON object storing the pattern information.
     * @throws JSONException if JSON creation fails.
     */
    private JSONObject patternToJson() throws JSONException {

        JSONObject pattern = new JSONObject();
        pattern.putOpt(JsonKey.PATTERN_TYPE, new JSONString(getPatternType().toString()));
        SortedSet<Date> dates = getIndividualDates();
        if (!dates.isEmpty()) {
            pattern.putOpt(JsonKey.PATTERN_DATES, datesToJson(dates));
        }
        int interval = getInterval();
        if (interval > 0) {
            pattern.putOpt(JsonKey.PATTERN_INTERVAL, new JSONString("" + interval));
        }
        SortedSet<WeekDay> weekdays = getWeekDays();
        if (!weekdays.isEmpty()) {
            pattern.putOpt(JsonKey.PATTERN_WEEKDAYS, toJsonStringArray(weekdays));
        }
        int day = getDayOfMonth();
        if (day > 0) {
            pattern.putOpt(JsonKey.PATTERN_DAY_OF_MONTH, new JSONString("" + day));
        }
        SortedSet<WeekOfMonth> weeks = getWeeksOfMonth();
        if (!weeks.isEmpty()) {
            pattern.putOpt(JsonKey.PATTERN_WEEKS_OF_MONTH, toJsonStringArray(weeks));
        }
        if (getPatternType().equals(PatternType.YEARLY)) {
            pattern.put(JsonKey.PATTERN_MONTH, new JSONString(getMonth().toString()));
        }
        if (isEveryWorkingDay()) {
            pattern.put(JsonKey.PATTERN_EVERYWORKINGDAY, JSONBoolean.getInstance(true));
        }
        return pattern;
    }

    /**
     * Extracts the dates from a JSON array.
     * @param array the JSON array where the dates are stored in.
     * @return list of the extracted dates.
     */
    private SortedSet<Date> readDates(JSONArray array) {

        if (null != array) {
            SortedSet<Date> result = new TreeSet<>();
            for (int i = 0; i < array.length(); i++) {
                try {
                    long l = Long.valueOf(array.getString(i)).longValue();
                    result.add(new Date(l));
                } catch (NumberFormatException | JSONException e) {
                    LOG.error("Could not read date from JSON array.", e);
                }
            }
            return result;
        }
        return new TreeSet<>();
    }

    /**
     * Read an optional JSON array.
     * @param json the JSON Object that has the array as element
     * @param key the key for the array in the provided JSON object
     * @param defaultValue the default value, to be returned if the array does not exist or can't be read.
     * @return the array or the default value if reading the array fails.
     */
    private JSONArray readOptionalArray(JSONObject json, String key, JSONArray defaultValue) {

        try {
            return json.getJSONArray(key);
        } catch (JSONException e) {
            LOG.debug("Reading optional JSON array failed. Default to provided default value.", e);
        }
        return defaultValue;
    }

    /**
     * Read an optional boolean value form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the boolean value in the provided JSON object.
     * @param defaultValue the default value, to be returned if the boolean can not be read from the JSON object.
     * @return the boolean or the default value if reading the boolean fails.
     */
    private boolean readOptionalBoolean(JSONObject json, String key, boolean defaultValue) {

        try {
            return json.getBoolean(key);
        } catch (JSONException e) {
            LOG.debug("Reading optional JSON boolean failed. Default to provided default value.", e);
        }
        return defaultValue;
    }

    /**
     * Read an optional int value (stored as string) form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the int value in the provided JSON object.
     * @param defaultValue the default value, to be returned if the int can not be read from the JSON object.
     * @return the int or the default value if reading the int fails.
     */
    private int readOptionalInt(JSONObject json, String key, int defaultValue) {

        try {
            String str = json.getString(key);
            return Integer.valueOf(str).intValue();
        } catch (NumberFormatException | JSONException e) {
            LOG.debug("Reading optional JSON int failed. Default to provided default value.", e);
        }
        return defaultValue;
    }

    /**
     * Read an optional Long value (stored as string) form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the Long value in the provided JSON object.
     * @param defaultValue the default value, to be returned if the Long can not be read from the JSON object.
     * @return the Long or the default value if reading the Long fails.
     */
    private Long readOptionalLong(JSONObject json, String key, Long defaultValue) {

        try {
            String str = json.getString(key);
            return Long.valueOf(str);
        } catch (NumberFormatException | JSONException e) {
            LOG.debug("Reading optional JSON Long failed. Default to provided default value.", e);
        }
        return defaultValue;
    }

    /**
     * Read an optional month value (stored as string) form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the month value in the provided JSON object.
     * @param defaultValue the default value, to be returned if the month can not be read from the JSON object.
     * @return the month or the default value if reading the month fails.
     */
    private Month readOptionalMonth(JSONObject json, String key, Month defaultValue) {

        try {
            String str = json.getString(key);
            return Month.valueOf(str);
        } catch (JSONException | IllegalArgumentException e) {
            LOG.debug("Reading optional JSON month failed. Default to provided default value.", e);
        }
        return defaultValue;
    }

    /**
     * Read an optional string value form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the string value in the provided JSON object.
     * @param defaultValue the default value, to be returned if the string can not be read from the JSON object.
     * @return the string or the default value if reading the string fails.
     */
    private String readOptionalString(JSONObject json, String key, String defaultValue) {

        try {
            String str = json.getString(key);
            if (str != null) {
                return str;
            }

        } catch (JSONException e) {
            LOG.debug("Reading optional JSON string failed. Default to provided default value.", e);
        }
        return defaultValue;
    }

    /**
     * Read pattern information from the provided JSON object.
     * @param patternJson the JSON object containing the pattern information.
     */
    private void readPattern(JSONObject patternJson) {

        m_patterntype = readPatternType(patternJson);
        m_interval = readOptionalInt(patternJson, JsonKey.PATTERN_INTERVAL, 0);
        m_weekDays = readWeekDays(patternJson);
        m_dayOfMonth = readOptionalInt(patternJson, JsonKey.PATTERN_DAY_OF_MONTH, 0);
        m_isEveryWorkingDay = readOptionalBoolean(patternJson, JsonKey.PATTERN_EVERYWORKINGDAY, false);
        m_weeksOfMonth = readWeeksOfMonth(patternJson);
        m_individualDates = readDates(readOptionalArray(patternJson, JsonKey.PATTERN_DATES, null));
        m_month = readOptionalMonth(patternJson, JsonKey.PATTERN_MONTH, null);

    }

    /**
     * Reads the pattern type from the provided JSON. Defaults to type NONE.
     * @param val the JSON object containing the pattern type information.
     * @return the pattern type.
     */
    private PatternType readPatternType(JSONObject val) {

        PatternType patterntype;
        try {
            String str = readOptionalString(val, JsonKey.PATTERN_TYPE, "");
            patterntype = PatternType.valueOf(str);
        } catch (IllegalArgumentException e) {
            LOG.debug("Could not read pattern type from JSON. Default to type NONE.", e);
            patterntype = PatternType.NONE;
        }
        return patterntype;
    }

    /**
     * Reads the weekday information.
     * @param val the JSON object containing the weekday information.
     * @return the weekdays extracted, defaults to the empty list, if no information is found.
     */
    private SortedSet<WeekDay> readWeekDays(JSONObject val) {

        try {
            JSONArray array = val.getJSONArray(JsonKey.PATTERN_WEEKDAYS);
            if (null != array) {
                SortedSet<WeekDay> result = new TreeSet<>();
                for (int i = 0; i < array.length(); i++) {
                    try {
                        result.add(WeekDay.valueOf(array.getString(i)));
                    } catch (JSONException | IllegalArgumentException e) {
                        LOG.error("Could not read weekday from JSON. Skipping that day.", e);
                    }
                }
                return result;
            }
        } catch (JSONException e) {
            LOG.debug("Could not read weekdays from JSON", e);
        }
        return new TreeSet<>();

    }

    /**
     * Read "weeks of month" information from JSON.
     * @param json the JSON where information is read from.
     * @return the list of weeks read, defaults to the empty list.
     */
    private SortedSet<WeekOfMonth> readWeeksOfMonth(JSONObject json) {

        try {
            JSONArray array = json.getJSONArray(JsonKey.PATTERN_WEEKS_OF_MONTH);
            if (null != array) {
                SortedSet<WeekOfMonth> result = new TreeSet<>();
                for (int i = 0; i < array.length(); i++) {
                    try {
                        WeekOfMonth week = WeekOfMonth.valueOf(array.getString(i));
                        result.add(week);
                    } catch (JSONException e) {
                        LOG.debug("Could not read week of month from JSON. Skipping that particular week of month.", e);
                    }
                }
                return result;
            }
        } catch (JSONException e) {
            LOG.debug("Could not read week of month information from JSON", e);
        }
        return new TreeSet<>();
    }

    /**
     * Check if the times (start and end of the single event) are valid.
     * @return flag, indicating if times are valid.
     */
    private boolean timesAreValid() {

        return !getEnd().before(getStart());
    }

    /**
     * Convert a collection of objects to a JSON array with the string representations of that objects.
     * @param collection the collection of objects.
     * @return the JSON array with the string representations.
     */
    private JSONArray toJsonStringArray(Collection<? extends Object> collection) {

        if (null != collection) {
            JSONArray array = new JSONArray();
            for (Object o : collection) {
                array.put("" + o);
            }
            return array;
        } else {
            return null;
        }
    }

}
