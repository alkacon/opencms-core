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

import org.opencms.acacia.shared.A_CmsSerialDateValue;
import org.opencms.acacia.shared.CmsSerialDateUtil;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.util.CmsJspElFunctions;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

/** Server-side implementation of {@link org.opencms.acacia.shared.I_CmsSerialDateValue}. */
public class CmsSerialDateValue extends A_CmsSerialDateValue {

    /** Logger for the class. */
    private static final Log LOG = CmsLog.getLog(CmsSerialDateValue.class);

    /** Flag, indicating if parsing the provided string value failed. */
    private boolean m_parsingFailed;

    /** Default constructor, setting the default state of the the serial date widget. */
    public CmsSerialDateValue() {

        setDefaultValue();
    }

    /**
     * Wraps the JSON specification of the serial date.
     *
     * @param value JSON representation of the serial date as string.
     */
    public CmsSerialDateValue(String value) {
        if ((null != value) && !value.isEmpty()) {
            try {
                JSONObject json = new JSONObject(value);
                setStart(readOptionalDate(json, JsonKey.START));
                setEnd(readOptionalDate(json, JsonKey.END));
                setWholeDay(readOptionalBoolean(json, JsonKey.WHOLE_DAY));
                JSONObject patternJson = json.getJSONObject(JsonKey.PATTERN);
                readPattern(patternJson);
                setExceptions(readDates(readOptionalArray(json, JsonKey.EXCEPTIONS)));
                setSeriesEndDate(readOptionalDate(json, JsonKey.SERIES_ENDDATE));
                setOccurrences(readOptionalInt(json, JsonKey.SERIES_OCCURRENCES));
                setDerivedEndType();
                setCurrentTillEnd(readOptionalBoolean(json, JsonKey.CURRENT_TILL_END));
                setParentSeriesId(readOptionalUUID(json, JsonKey.PARENT_SERIES));
            } catch (JSONException e) {
                setDefaultValue();
                Date d = CmsJspElFunctions.convertDate(value);
                if (d.getTime() == 0) {
                    m_parsingFailed = true;
                } else {
                    setStart(d);
                }
            }
        } else {
            setDefaultValue();
        }
    }

    /**
     * Convert the information from the wrapper to a JSON object.
     * @return the serial date information as JSON.
     */
    public JSONObject toJson() {

        try {
            JSONObject result = new JSONObject();
            if (null != getStart()) {
                result.put(JsonKey.START, dateToJson(getStart()));
            }
            if (null != getEnd()) {
                result.put(JsonKey.END, dateToJson(getEnd()));
            }
            if (isWholeDay()) {
                result.put(JsonKey.WHOLE_DAY, true);
            }
            JSONObject pattern = patternToJson();
            result.put(JsonKey.PATTERN, pattern);
            SortedSet<Date> exceptions = getExceptions();
            if (!exceptions.isEmpty()) {
                result.put(JsonKey.EXCEPTIONS, datesToJson(exceptions));
            }
            switch (getEndType()) {
                case DATE:
                    result.put(JsonKey.SERIES_ENDDATE, dateToJson(getSeriesEndDate()));
                    break;
                case TIMES:
                    result.put(JsonKey.SERIES_OCCURRENCES, String.valueOf(getOccurrences()));
                    break;
                case SINGLE:
                default:
                    break;
            }
            if (!isCurrentTillEnd()) {
                result.put(JsonKey.CURRENT_TILL_END, false);
            }
            if (null != getParentSeriesId()) {
                result.put(JsonKey.PARENT_SERIES, getParentSeriesId().getStringValue());
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
     * Validates the wrapped value and returns a localized error message in case of invalid values.
     * @return <code>null</code> if the value is valid, a suitable localized error message otherwise.
     */
    public CmsMessageContainer validateWithMessage() {

        if (m_parsingFailed) {
            return Messages.get().container(Messages.ERR_SERIALDATE_INVALID_VALUE_0);
        }
        if (!isStartSet()) {
            return Messages.get().container(Messages.ERR_SERIALDATE_START_MISSING_0);
        }
        if (!isEndValid()) {
            return Messages.get().container(Messages.ERR_SERIALDATE_END_BEFORE_START_0);
        }
        String key = validatePattern();
        if (null != key) {
            return Messages.get().container(key);
        }
        key = validateDuration();
        if (null != key) {
            return Messages.get().container(key);
        }
        if (hasTooManyEvents()) {
            return Messages.get().container(
                Messages.ERR_SERIALDATE_TOO_MANY_EVENTS_1,
                Integer.valueOf(CmsSerialDateUtil.getMaxEvents()));
        }
        return null;
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
     * Returns a flag, indicating if the series has too many events.
     * @return a flag, indicating if the series has too many events.
     */
    private boolean hasTooManyEvents() {

        return CmsSerialDateBeanFactory.createSerialDateBean(this).hasTooManyDates();

    }

    /**
     * Generates the JSON object storing the pattern information.
     * @return the JSON object storing the pattern information.
     * @throws JSONException if JSON creation fails.
     */
    private JSONObject patternToJson() throws JSONException {

        JSONObject pattern = new JSONObject();
        if (null != getPatternType()) {
            pattern.putOpt(JsonKey.PATTERN_TYPE, getPatternType().toString());
            switch (getPatternType()) {
                case DAILY:
                    if (isEveryWorkingDay()) {
                        pattern.put(JsonKey.PATTERN_EVERYWORKINGDAY, true);
                    } else {
                        pattern.putOpt(JsonKey.PATTERN_INTERVAL, String.valueOf(getInterval()));
                    }
                    break;
                case WEEKLY:
                    pattern.putOpt(JsonKey.PATTERN_INTERVAL, String.valueOf(getInterval()));
                    pattern.putOpt(JsonKey.PATTERN_WEEKDAYS, toJsonStringArray(getWeekDays()));
                    break;
                case MONTHLY:
                    pattern.putOpt(JsonKey.PATTERN_INTERVAL, String.valueOf(getInterval()));
                    if (null != getWeekDay()) {
                        pattern.putOpt(JsonKey.PATTERN_WEEKS_OF_MONTH, toJsonStringArray(getWeeksOfMonth()));
                        pattern.putOpt(JsonKey.PATTERN_WEEKDAYS, toJsonStringArray(getWeekDays()));
                    } else {
                        pattern.putOpt(JsonKey.PATTERN_DAY_OF_MONTH, "" + getDayOfMonth());
                    }
                    break;
                case YEARLY:
                    pattern.put(JsonKey.PATTERN_MONTH, getMonth().toString());
                    if (null != getWeekDay()) {
                        pattern.putOpt(JsonKey.PATTERN_WEEKS_OF_MONTH, toJsonStringArray(getWeeksOfMonth()));
                        pattern.putOpt(JsonKey.PATTERN_WEEKDAYS, toJsonStringArray(getWeekDays()));
                    } else {
                        pattern.putOpt(JsonKey.PATTERN_DAY_OF_MONTH, "" + getDayOfMonth());
                    }
                    break;
                case INDIVIDUAL:
                    pattern.putOpt(JsonKey.PATTERN_DATES, datesToJson(getIndividualDates()));
                    break;
                case NONE:
                default:
                    break;
            }
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
     * @return the array or null if reading the array fails.
     */
    private JSONArray readOptionalArray(JSONObject json, String key) {

        try {
            return json.getJSONArray(key);
        } catch (JSONException e) {
            LOG.debug("Reading optional JSON array failed. Default to provided default value.", e);
        }
        return null;
    }

    /**
     * Read an optional boolean value form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the boolean value in the provided JSON object.
     * @return the boolean or null if reading the boolean fails.
     */
    private Boolean readOptionalBoolean(JSONObject json, String key) {

        try {
            return Boolean.valueOf(json.getBoolean(key));
        } catch (JSONException e) {
            LOG.debug("Reading optional JSON boolean failed. Default to provided default value.", e);
        }
        return null;
    }

    /**
     * Read an optional Date value (stored as string) form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the Long value in the provided JSON object.
     * @return the Date or null if reading the Date fails.
     */
    private Date readOptionalDate(JSONObject json, String key) {

        try {
            String str = json.getString(key);
            return new Date(Long.parseLong(str));
        } catch (NumberFormatException | JSONException e) {
            LOG.debug("Reading optional JSON Long failed. Default to provided default value.", e);
        }
        return null;
    }

    /**
     * Read an optional int value (stored as string) form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the int value in the provided JSON object.
     * @return the int or 0 if reading the int fails.
     */
    private int readOptionalInt(JSONObject json, String key) {

        try {
            String str = json.getString(key);
            return Integer.valueOf(str).intValue();
        } catch (NumberFormatException | JSONException e) {
            LOG.debug("Reading optional JSON int failed. Default to provided default value.", e);
        }
        return 0;
    }

    /**
     * Read an optional month value (stored as string) form a JSON Object.
     * @param json the JSON object to read from.
     * @param key the key for the month value in the provided JSON object.
     * @return the month or null if reading the month fails.
     */
    private Month readOptionalMonth(JSONObject json, String key) {

        try {
            String str = json.getString(key);
            return Month.valueOf(str);
        } catch (JSONException | IllegalArgumentException e) {
            LOG.debug("Reading optional JSON month failed. Default to provided default value.", e);
        }
        return null;
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
     * Read an optional uuid stored as JSON string.
     * @param json the JSON object to readfrom.
     * @param key the key for the UUID in the provided JSON object.
     * @return the uuid, or <code>null</code> if the uuid can not be read.
     */
    private CmsUUID readOptionalUUID(JSONObject json, String key) {

        String id = readOptionalString(json, key, null);
        if (null != id) {
            try {
                CmsUUID uuid = CmsUUID.valueOf(id);
                return uuid;
            } catch (NumberFormatException e) {
                LOG.debug("Reading optional UUID failed. Could not convert \"" + id + "\" to a valid UUID.");
            }
        }
        return null;
    }

    /**
     * Read pattern information from the provided JSON object.
     * @param patternJson the JSON object containing the pattern information.
     */
    private void readPattern(JSONObject patternJson) {

        setPatternType(readPatternType(patternJson));
        setInterval(readOptionalInt(patternJson, JsonKey.PATTERN_INTERVAL));
        setWeekDays(readWeekDays(patternJson));
        setDayOfMonth(readOptionalInt(patternJson, JsonKey.PATTERN_DAY_OF_MONTH));
        setEveryWorkingDay(readOptionalBoolean(patternJson, JsonKey.PATTERN_EVERYWORKINGDAY));
        setWeeksOfMonth(readWeeksOfMonth(patternJson));
        setIndividualDates(readDates(readOptionalArray(patternJson, JsonKey.PATTERN_DATES)));
        setMonth(readOptionalMonth(patternJson, JsonKey.PATTERN_MONTH));

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

    /**
     * Check if the day of month is valid.
     * @return <code>null</code> if the day of month is valid, the key of a suitable error message otherwise.
     */
    private String validateDayOfMonth() {

        return (isDayOfMonthValid()) ? null : Messages.ERR_SERIALDATE_INVALID_DAY_OF_MONTH_0;
    }

    /**
     * Checks if the provided duration information is valid.
     * @return <code>null</code> if the information is valid, the key of the suitable error message otherwise.
     */
    private String validateDuration() {

        if (!isValidEndTypeForPattern()) {
            return Messages.ERR_SERIALDATE_INVALID_END_TYPE_FOR_PATTERN_0;
        }
        switch (getEndType()) {
            case DATE:
                return (getStart().getTime() < (getSeriesEndDate().getTime() + DAY_IN_MILLIS))
                ? null
                : Messages.ERR_SERIALDATE_SERIES_END_BEFORE_START_0;
            case TIMES:
                return getOccurrences() > 0 ? null : Messages.ERR_SERIALDATE_INVALID_OCCURRENCES_0;
            default:
                return null;
        }

    }

    /**
     * Check if the interval is valid.
     * @return <code>null</code> if the interval is valid, a suitable error message key otherwise.
     */
    private String validateInterval() {

        return isIntervalValid() ? null : Messages.ERR_SERIALDATE_INVALID_INTERVAL_0;
    }

    /**
     * Check, if the month is set.
     * @return <code>null</code> if a month is set, a suitable error message key otherwise.
     */
    private String validateMonthSet() {

        return isMonthSet() ? null : Messages.ERR_SERIALDATE_NO_MONTH_SET_0;
    }

    /**
     * Check, if all values used for calculating the series for a specific pattern are valid.
     * @return <code>null</code> if the pattern is valid, a suitable error message otherwise.
     */
    private String validatePattern() {

        String error = null;
        switch (getPatternType()) {
            case DAILY:
                error = isEveryWorkingDay() ? null : validateInterval();
                break;
            case WEEKLY:
                error = validateInterval();
                if (null == error) {
                    error = validateWeekDaySet();
                }
                break;
            case MONTHLY:
                error = validateInterval();
                if (null == error) {
                    error = validateMonthSet();
                    if (null == error) {
                        error = isWeekDaySet() ? validateWeekOfMonthSet() : validateDayOfMonth();
                    }
                }
                break;
            case YEARLY:
                error = isWeekDaySet() ? validateWeekOfMonthSet() : validateDayOfMonth();
                break;
            case INDIVIDUAL:
            case NONE:
            default:
        }
        return error;
    }

    /**
     * Validate if a weekday is set, otherwise return the key for a suitable error message.
     * @return <code>null</code> if a weekday is set, the key for a suitable error message otherwise.
     */
    private String validateWeekDaySet() {

        return isWeekDaySet() ? null : Messages.ERR_SERIALDATE_NO_WEEKDAY_SPECIFIED_0;
    }

    /**
     * Check if a week of month is set.
     * @return <code>null</code> if a week of month is set, the key for a suitable error message otherwise.
     */
    private String validateWeekOfMonthSet() {

        return isWeekOfMonthSet() ? null : Messages.ERR_SERIALDATE_NO_WEEK_OF_MONTH_SPECIFIED_0;
    }

}
