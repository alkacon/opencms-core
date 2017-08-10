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

import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * Wrapper for the value stored by the {@link org.opencms.widgets.CmsSerialDateWidget}
 * that provides easy access.
 */
public class CmsSerialDateBeanFactory {

    /** Logger for the class. */
    public static final Log LOG = CmsLog.getLog(CmsSerialDateBeanFactory.class);

    /**
     * Factory method for creating a serial date bean.
     * @param value the value for the series
     * @return the serial date bean.
     */
    public static I_CmsSerialDateBean createSerialDateBean(I_CmsSerialDateValue value) {

        if ((null == value) || !value.isValid()) {
            return null;
        }
        switch (value.getPatternType()) {
            case DAILY:
                if (value.isEveryWorkingDay()) {
                    return new CmsSerialDateBeanWorkingDays(
                        value.getStart(),
                        value.getEnd(),
                        value.isWholeDay(),
                        value.getEndType(),
                        value.getSeriesEndDate(),
                        value.getOccurrences(),
                        value.getExceptions());
                } else {
                    return new CmsSerialDateBeanDaily(
                        value.getStart(),
                        value.getEnd(),
                        value.isWholeDay(),
                        value.getEndType(),
                        value.getSeriesEndDate(),
                        value.getOccurrences(),
                        value.getExceptions(),
                        value.getInterval());
                }
            case WEEKLY:
                return new CmsSerialDateBeanWeekly(
                    value.getStart(),
                    value.getEnd(),
                    value.isWholeDay(),
                    value.getEndType(),
                    value.getSeriesEndDate(),
                    value.getOccurrences(),
                    value.getExceptions(),
                    value.getInterval(),
                    value.getWeekDays());
            case MONTHLY:
                if (null == value.getWeekDay()) {
                    return new CmsSerialDateBeanMonthly(
                        value.getStart(),
                        value.getEnd(),
                        value.isWholeDay(),
                        value.getEndType(),
                        value.getSeriesEndDate(),
                        value.getOccurrences(),
                        value.getExceptions(),
                        value.getInterval(),
                        value.getDayOfMonth());
                } else {
                    return new CmsSerialDateBeanMonthlyWeeks(
                        value.getStart(),
                        value.getEnd(),
                        value.isWholeDay(),
                        value.getEndType(),
                        value.getSeriesEndDate(),
                        value.getOccurrences(),
                        value.getExceptions(),
                        value.getInterval(),
                        value.getWeeksOfMonth(),
                        value.getWeekDay());
                }
            case YEARLY:
                if (null == value.getWeekDay()) {
                    return new CmsSerialDateBeanYearly(
                        value.getStart(),
                        value.getEnd(),
                        value.isWholeDay(),
                        value.getEndType(),
                        value.getSeriesEndDate(),
                        value.getOccurrences(),
                        value.getExceptions(),
                        value.getDayOfMonth(),
                        value.getMonth());
                } else {
                    return new CmsSerialDateBeanYearlyWeekday(
                        value.getStart(),
                        value.getEnd(),
                        value.isWholeDay(),
                        value.getEndType(),
                        value.getSeriesEndDate(),
                        value.getOccurrences(),
                        value.getExceptions(),
                        value.getWeekOfMonth(),
                        value.getMonth(),
                        value.getWeekDay());
                }
            case INDIVIDUAL:
                return new CmsSerialDateBeanIndividual(
                    value.getStart(),
                    value.getEnd(),
                    value.isWholeDay(),
                    value.getEndType(),
                    value.getSeriesEndDate(),
                    value.getOccurrences(),
                    value.getExceptions(),
                    value.getIndividualDates());
            case NONE:
                return new CmsSerialDateBeanSingle(value.getStart(), value.getEnd(), value.isWholeDay());
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Factory method for creating a serial date bean.
     * @param widgetValue the value for the series as stored by the {@link org.opencms.widgets.CmsSerialDateWidget}
     * @return the serial date bean.
     */
    public static I_CmsSerialDateBean createSerialDateBean(String widgetValue) {

        I_CmsSerialDateValue value;
        value = new CmsSerialDateValue(widgetValue);
        return createSerialDateBean(value);
    }
}
