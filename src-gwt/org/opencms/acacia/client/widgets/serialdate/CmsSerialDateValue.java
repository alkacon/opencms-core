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

import org.opencms.acacia.shared.A_CmsSerialDateValue;
import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
public class CmsSerialDateValue extends A_CmsSerialDateValue implements I_CmsObservableSerialDateValue {

    /** The list of value change observers. */
    Collection<I_CmsSerialDateValueChangeObserver> m_valueChangeObservers = new HashSet<>();

    /** Default constructor, setting the default state of the the serial date widget. */
    public CmsSerialDateValue() {
        setDefaultValue();
    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsObservableSerialDateValue#registerValueChangeObserver(org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDateValueChangeObserver)
     */
    public void registerValueChangeObserver(I_CmsSerialDateValueChangeObserver obs) {

        m_valueChangeObservers.add(obs);

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
                CmsDebugLog.consoleLog("Could not set invalid serial date value: " + value);
                setDefaultValue();
            }
        }
        notifyOnValueChange();
    }

    /**
     * Convert the information from the wrapper to a JSON object.
     * @return the serial date information as JSON.
     */
    public JSONValue toJson() {

        JSONObject result = new JSONObject();
        if (null != getStart()) {
            result.put(JsonKey.START, dateToJson(getStart()));
        }
        if (null != getEnd()) {
            result.put(JsonKey.END, dateToJson(getEnd()));
        }
        if (isWholeDay()) {
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
        if (!isCurrentTillEnd()) {
            result.put(JsonKey.CURRENT_TILL_END, JSONBoolean.getInstance(false));
        }
        if (getParentSeriesId() != null) {
            result.put(JsonKey.PARENT_SERIES, new JSONString(getParentSeriesId().getStringValue()));
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
        if (null != getPatternType()) {
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
                    pattern.put(JsonKey.PATTERN_MONTH, new JSONString(String.valueOf(getMonth())));
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
                Date d = readOptionalDate(array.get(i));
                if (null != d) {
                    result.add(d);
                }
            }
            return result;
        }
        return new TreeSet<>();
    }

    /**
     * Read an optional boolean value form a JSON value.
     * @param val the JSON value that should represent the boolean.
     * @return the boolean from the JSON or null if reading the boolean fails.
     */
    private Boolean readOptionalBoolean(JSONValue val) {

        JSONBoolean b = null == val ? null : val.isBoolean();
        if (b != null) {
            return Boolean.valueOf(b.booleanValue());
        }
        return null;
    }

    /**
     * Read an optional Date value form a JSON value.
     * @param val the JSON value that should represent the Date as long value in a string.
     * @return the Date from the JSON or null if reading the date fails.
     */
    private Date readOptionalDate(JSONValue val) {

        JSONString str = null == val ? null : val.isString();
        if (str != null) {
            try {
                return new Date(Long.parseLong(str.stringValue()));
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                // do nothing - return the default value
            }
        }
        return null;
    }

    /**
     * Read an optional int value form a JSON value.
     * @param val the JSON value that should represent the int.
     * @return the int from the JSON or 0 reading the int fails.
     */
    private int readOptionalInt(JSONValue val) {

        JSONString str = null == val ? null : val.isString();
        if (str != null) {
            try {
                return Integer.valueOf(str.stringValue()).intValue();
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                // Do nothing, return default value
            }
        }
        return 0;
    }

    /**
     * Read an optional month value form a JSON value.
     * @param val the JSON value that should represent the month.
     * @return the month from the JSON or null if reading the month fails.
     */
    private Month readOptionalMonth(JSONValue val) {

        String str = readOptionalString(val);
        if (null != str) {
            try {
                return Month.valueOf(str);
            } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                // Do nothing -return the default value
            }
        }
        return null;
    }

    /**
     * Read an optional string value form a JSON value.
     * @param val the JSON value that should represent the string.
     * @return the string from the JSON or null if reading the string fails.
     */
    private String readOptionalString(JSONValue val) {

        JSONString str = null == val ? null : val.isString();
        if (str != null) {
            return str.stringValue();
        }
        return null;
    }

    /**
     * Read an optional uuid stored as JSON string.
     * @param val the JSON value to read the uuid from.
     * @return the uuid, or <code>null</code> if the uuid can not be read.
     */
    private CmsUUID readOptionalUUID(JSONValue val) {

        String id = readOptionalString(val);
        if (null != id) {
            try {
                CmsUUID uuid = CmsUUID.valueOf(id);
                return uuid;
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                // Do nothing, just return null
            }
        }
        return null;
    }

    /**
     * Read pattern information from the provided JSON object.
     * @param patternJson the JSON object containing the pattern information.
     */
    private void readPattern(JSONObject patternJson) {

        setPatternType(readPatternType(patternJson.get(JsonKey.PATTERN_TYPE)));
        setInterval(readOptionalInt(patternJson.get(JsonKey.PATTERN_INTERVAL)));
        setWeekDays(readWeekDays(patternJson.get(JsonKey.PATTERN_WEEKDAYS)));
        setDayOfMonth(readOptionalInt(patternJson.get(JsonKey.PATTERN_DAY_OF_MONTH)));
        setEveryWorkingDay(readOptionalBoolean(patternJson.get(JsonKey.PATTERN_EVERYWORKINGDAY)));
        setWeeksOfMonth(readWeeksOfMonth(patternJson.get(JsonKey.PATTERN_WEEKS_OF_MONTH)));
        setIndividualDates(readDates(patternJson.get(JsonKey.PATTERN_DATES)));
        setMonth(readOptionalMonth(patternJson.get(JsonKey.PATTERN_MONTH)));

    }

    /**
     * Reads the pattern type from the provided JSON. Defaults to type NONE.
     * @param val the JSON object containing the pattern type information.
     * @return the pattern type.
     */
    private PatternType readPatternType(JSONValue val) {

        PatternType patterntype;
        try {
            String str = readOptionalString(val);
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

        String str = readOptionalString(val);
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
                String weekStr = readOptionalString(array.get(i));
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
        setStart(readOptionalDate(val));
        val = json.get(JsonKey.END);
        setEnd(readOptionalDate(val));
        setWholeDay(readOptionalBoolean(json.get(JsonKey.WHOLE_DAY)));
        JSONObject patternJson = json.get(JsonKey.PATTERN).isObject();
        readPattern(patternJson);
        setExceptions(readDates(json.get(JsonKey.EXCEPTIONS)));
        setSeriesEndDate(readOptionalDate(json.get(JsonKey.SERIES_ENDDATE)));
        setOccurrences(readOptionalInt(json.get(JsonKey.SERIES_OCCURRENCES)));
        setDerivedEndType();
        setCurrentTillEnd(readOptionalBoolean(json.get(JsonKey.CURRENT_TILL_END)));
        setParentSeriesId(readOptionalUUID(json.get(JsonKey.PARENT_SERIES)));

    }
}
