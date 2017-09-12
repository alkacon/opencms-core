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
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/** The base class for implementations of serial date values. */
public class A_CmsSerialDateValue implements I_CmsSerialDateValue {

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
    /** The end type of the series. */
    private EndType m_endType;
    /** The series content, the current value is extracted from. */
    private CmsUUID m_parentSeriesId;
    /** Flag, indicating if the events are "current" till their end. */
    private boolean m_currentTillEnd = true;

    /**
     * Add a date where the event should not take place, even if they are part of the series.
     * @param date the date to add as exception.
     */
    public void addException(Date date) {

        if (null != date) {
            m_exceptions.add(date);
        }

    }

    /**
     * Add a week of month.
     * @param week the week to add.
     */
    public final void addWeekOfMonth(WeekOfMonth week) {

        m_weeksOfMonth.add(week);
    }

    /**
     * Clear the exceptions.
     */
    public final void clearExceptions() {

        m_exceptions.clear();

    }

    /**
     * Clear the individual dates.
     */
    public final void clearIndividualDates() {

        m_individualDates.clear();

    }

    /**
     * Clear the week days.
     */
    public final void clearWeekDays() {

        m_weekDays.clear();

    }

    /**
     * Clear the weeks of month.
     */
    public final void clearWeeksOfMonth() {

        m_weeksOfMonth.clear();

    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#endsAtMidNight()
     */
    @SuppressWarnings("deprecation")
    public boolean endsAtMidNight() {

        Date end = getEnd();
        return (end != null)
            && (end.getHours() == 0)
            && (end.getMinutes() == 0)
            && (end.getSeconds() == 0)
            && ((end.getTime() % 1000) == 0);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    //TODO: Rework!
    @Override
    public final boolean equals(Object o) {

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
                && Objects.equals(val.getWeeksOfMonth(), this.getWeeksOfMonth())
                && Objects.equals(val.getParentSeriesId(), this.getParentSeriesId());
        }
        return false;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getDateType()
     */
    public DateType getDateType() {

        if (!Objects.equals(getPatternType(), PatternType.NONE)) {
            return DateType.SERIES;
        }
        if (isFromOtherSeries()) {
            return DateType.EXTRACTED;
        }
        return DateType.SINGLE;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getDayOfMonth()
     */
    public final int getDayOfMonth() {

        return m_dayOfMonth;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getEnd()
     */
    public final Date getEnd() {

        return m_end;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getEndType()
     */
    public final EndType getEndType() {

        return m_endType;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getExceptions()
     */
    public final SortedSet<Date> getExceptions() {

        return new TreeSet<>(m_exceptions);
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getIndividualDates()
     */
    public final SortedSet<Date> getIndividualDates() {

        return new TreeSet<>(m_individualDates);
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getInterval()
     */
    public final int getInterval() {

        return m_interval;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getMonth()
     */
    public final Month getMonth() {

        return m_month;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getOccurrences()
     */
    public final int getOccurrences() {

        return m_seriesOccurrences;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getParentSeriesId()
     */
    public CmsUUID getParentSeriesId() {

        return m_parentSeriesId;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getPatternType()
     */
    public final PatternType getPatternType() {

        return m_patterntype;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getSeriesEndDate()
     */
    public final Date getSeriesEndDate() {

        return m_seriesEndDate;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getStart()
     */
    public final Date getStart() {

        return m_start;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeekDay()
     */
    public final WeekDay getWeekDay() {

        if (m_weekDays.size() > 0) {
            return m_weekDays.first();
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeekDays()
     */
    public final SortedSet<WeekDay> getWeekDays() {

        return new TreeSet<>(m_weekDays);
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeekOfMonth()
     */
    public final WeekOfMonth getWeekOfMonth() {

        if (m_weeksOfMonth.size() > 0) {
            return m_weeksOfMonth.iterator().next();
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#getWeeksOfMonth()
     */
    @Override
    public final SortedSet<WeekOfMonth> getWeeksOfMonth() {

        return new TreeSet<>(m_weeksOfMonth);
    }

    /**
     * Returns a flag, indicating if exceptions are present.
     * @return a flag, indicating if exceptions are present.
     */
    public final boolean hasExceptions() {

        return !getExceptions().isEmpty();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    // TODO: Rework
    @Override
    public final int hashCode() {

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
            this.getWeeksOfMonth(),
            Boolean.valueOf(this.isCurrentTillEnd()),
            this.getParentSeriesId());

    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isCurrentTillEnd()
     */
    public boolean isCurrentTillEnd() {

        return m_currentTillEnd;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isEveryWorkingDay()
     */
    public final boolean isEveryWorkingDay() {

        return m_isEveryWorkingDay;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isFromOtherSeries()
     */
    @Override
    public boolean isFromOtherSeries() {

        return null != m_parentSeriesId;
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isValid()
     */
    public final boolean isValid() {

        return isStartSet() && isEndValid() && isPatternValid() && isDurationValid();
    }

    /**
     * @see org.opencms.acacia.shared.I_CmsSerialDateValue#isWholeDay()
     */
    public final boolean isWholeDay() {

        return m_isWholeDay;
    }

    /**
     * Remove a week of month.
     * @param week the week to remove.
     */
    public final void removeWeekOfMonth(WeekOfMonth week) {

        m_weeksOfMonth.remove(week);
    }

    /**
     * Set the flag, indicating if the event is treated as "current" till the end.
     * @param isCurrentTillEnd the flag, indicating if the event is treated as "current" till the end.
     */
    public final void setCurrentTillEnd(Boolean isCurrentTillEnd) {

        m_currentTillEnd = (null == isCurrentTillEnd) || isCurrentTillEnd.booleanValue();
    }

    /**
     * Set the day of month.
     * @param dayOfMonth the day of month to set.
     */
    public final void setDayOfMonth(int dayOfMonth) {

        m_dayOfMonth = dayOfMonth;
    }

    /**
     * Set the end time for the event.
     * @param date the end time to set.
     */
    public final void setEnd(Date date) {

        m_end = date;
    }

    /**
     * Set the end type of the series.
     * @param endType the end type to set.
     */
    public final void setEndType(EndType endType) {

        m_endType = null == endType ? EndType.SINGLE : endType;
    }

    /**
     * Set the flag, indicating if the event should take place every working day.
     * @param isEveryWorkingDay the flag, indicating if the event should take place every working day.
     */
    public final void setEveryWorkingDay(Boolean isEveryWorkingDay) {

        m_isEveryWorkingDay = null == isEveryWorkingDay ? false : isEveryWorkingDay.booleanValue();

    }

    /**
     * Set dates where the event should not take place, even if they are part of the series.
     * @param dates dates to set.
     */
    public final void setExceptions(SortedSet<Date> dates) {

        m_exceptions.clear();
        if (null != dates) {
            m_exceptions.addAll(dates);
        }

    }

    /**
     * Set the individual dates where the event should take place.
     * @param dates the dates to set.
     */
    public final void setIndividualDates(SortedSet<Date> dates) {

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
    public final void setInterval(int interval) {

        m_interval = interval;
    }

    /**
     * Set the month in which the event should take place.
     * @param month the month to set.
     */
    public final void setMonth(Month month) {

        m_month = null == month ? Month.JANUARY : month;

    }

    /**
     * Set the number of occurrences of the event.
     * @param occurrences the number of occurrences to set.
     */
    public final void setOccurrences(int occurrences) {

        m_seriesOccurrences = occurrences;

    }

    /**
     * Set the series, the current event (series) is extracted from.
     * @param structureId the structure id of the series content, the event is extracted from.
     */
    public final void setParentSeriesId(CmsUUID structureId) {

        m_parentSeriesId = structureId;

    }

    /**
     * Set the pattern type of the event series.<p>
     *
     * All pattern specific values are reset.
     *
     * @param type the pattern type to set.
     */
    public final void setPatternType(PatternType type) {

        m_patterntype = null == type ? PatternType.NONE : type;
    }

    /**
     * Set the last day events of the series should occur.
     * @param date the day to set.
     */
    public final void setSeriesEndDate(Date date) {

        m_seriesEndDate = date;

    }

    /**
     * Set the start time of the events. Unless you specify a single event, the day information is discarded.
     * @param date the time to set.
     */
    public final void setStart(Date date) {

        m_start = date;
    }

    /**
     * Set the week day the events should occur.
     * @param weekDay the week day to set.
     */
    public final void setWeekDay(WeekDay weekDay) {

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
    public final void setWeekDays(SortedSet<WeekDay> weekDays) {

        m_weekDays.clear();
        if (null != weekDays) {
            m_weekDays.addAll(weekDays);
        }
    }

    /**
     * Set the week of the month the events should occur.
     * @param weekOfMonth the week of month to set (first to fifth, where fifth means last).
     */
    public final void setWeekOfMonth(WeekOfMonth weekOfMonth) {

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
    public final void setWeeksOfMonth(SortedSet<WeekOfMonth> weeksOfMonth) {

        m_weeksOfMonth.clear();
        if (null != weeksOfMonth) {
            m_weeksOfMonth.addAll(weeksOfMonth);
        }

    }

    /**
     * Set the flag, indicating if the event last the whole day/whole days.
     * @param isWholeDay the flag to set
     */
    public final void setWholeDay(Boolean isWholeDay) {

        m_isWholeDay = (null != isWholeDay) && isWholeDay.equals(Boolean.TRUE);

    }

    /**
     * Checks, if a valid day of month is set.
     * @return a flag, indicating if the set day of month is valid.
     */
    protected final boolean isDayOfMonthValid() {

        return (getDayOfMonth() > 0) && (getDayOfMonth() <= (getMonth() == null ? 31 : getMonth().getMaximalDay()));
    }

    /**
     * Checks if the duration option is valid.
     *
     * NOTE: This does NOT check, if too many events are specified.
     *
     * @return a flag, indicating if the duration option is valid.
     */
    protected final boolean isDurationValid() {

        if (isValidEndTypeForPattern()) {
            switch (getEndType()) {
                case DATE:
                    return (getStart().getTime() < (getSeriesEndDate().getTime() + DAY_IN_MILLIS));
                case TIMES:
                    return getOccurrences() > 0;
                case SINGLE:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    /** Check, if the end date of the single event is valid, i.e., either not set or not before the start date.
     *
     * @return a flag, indicating if the end date is set.
     */
    protected final boolean isEndValid() {

        return (getEnd() == null) || !getEnd().before(getStart());
    }

    /** Checks, if a valid interval is specified.
     *
     * @return a flag, indicating if the specified interval is valid.
     */
    protected final boolean isIntervalValid() {

        return getInterval() > 0;
    }

    /**
     * Checks, if a month is specified.
     * @return flag, indicating if a month is specified.
     */
    protected final boolean isMonthSet() {

        return getMonth() != null;
    }

    /**
     * Checks, if all values necessary for a specific pattern are valid.
     * @return a flag, indicating if all values required for the pattern are valid.
     */
    protected final boolean isPatternValid() {

        switch (getPatternType()) {
            case DAILY:
                return isEveryWorkingDay() || isIntervalValid();
            case WEEKLY:
                return isIntervalValid() && isWeekDaySet();
            case MONTHLY:
                return isIntervalValid() && isWeekDaySet() ? isWeekOfMonthSet() : isDayOfMonthValid();
            case YEARLY:
                return isMonthSet() && isWeekDaySet() ? isWeekOfMonthSet() : isDayOfMonthValid();
            case INDIVIDUAL:
            case NONE:
                return true;
            default:
                return false;
        }
    }

    /** Check, if the start time stamp is set.
     *
     * @return a flag, indicating if a start date is set.
     */
    protected final boolean isStartSet() {

        return null != getStart();
    }

    /**
     * Checks, if the end type is valid for the set pattern type.
     * @return a flag, indicating if the end type is valid for the pattern type.
     */
    protected final boolean isValidEndTypeForPattern() {

        if (getEndType() == null) {
            return false;
        }
        switch (getPatternType()) {
            case DAILY:
            case WEEKLY:
            case MONTHLY:
            case YEARLY:
                return (getEndType().equals(EndType.DATE) || getEndType().equals(EndType.TIMES));
            case INDIVIDUAL:
            case NONE:
                return getEndType().equals(EndType.SINGLE);
            default:
                return false;
        }
    }

    /**
     * Checks if at least one weekday is specified.
     * @return a flag, indicating if at least one weekday is specified.
     */
    protected final boolean isWeekDaySet() {

        return !m_weekDays.isEmpty();
    }

    /**
     * Checks, if at least one week of month is set.
     * @return a flag, indicating if at least one week of month is set.
     */
    protected final boolean isWeekOfMonthSet() {

        return !m_weeksOfMonth.isEmpty();
    }

    /**
     * Sets the value to a default.
     */
    protected final void setDefaultValue() {

        m_start = null;
        m_end = null;
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
        m_endType = EndType.SINGLE;
        m_parentSeriesId = null;
    }

    /**
     * Set the end type as derived from other values.
     */
    protected final void setDerivedEndType() {

        m_endType = getPatternType().equals(PatternType.NONE) || getPatternType().equals(PatternType.INDIVIDUAL)
        ? EndType.SINGLE
        : null != getSeriesEndDate() ? EndType.DATE : EndType.TIMES;
    }
}
