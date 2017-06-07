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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Client side implementation for {@link org.opencms.util.CmsDateUtil}.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.util.CmsDateUtil
 */
public final class CmsDateTimeUtil {

    /**
     * The standard formats.<p>
     *
     * @see java.text.DateFormat
     */
    public enum Format {

        /** @see java.text.DateFormat#FULL */
        FULL,

        /** @see java.text.DateFormat#LONG */
        LONG,

        /** @see java.text.DateFormat#MEDIUM */
        MEDIUM,

        /** @see java.text.DateFormat#SHORT */
        SHORT;
    }

    /**
     * Hides the public constructor.<p>
     */
    private CmsDateTimeUtil() {

        // noop
    }

    /**
     * Returns a formated date String from a Date value,
     * the formatting based on the provided options.<p>
     *
     * @param date the Date object to format as String
     * @param format the format to use
     *
     * @return the formatted date
     */
    public static String getDate(Date date, Format format) {

        DateTimeFormat df;
        switch (format) {
            case FULL:
                df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL);
                break;
            case LONG:
                df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_LONG);
                break;
            case MEDIUM:
                df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
                break;
            case SHORT:
                df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
                break;
            default:
                // can never happen, just to prevent stupid warning
                return "";
        }
        return df.format(date);
    }

    /**
     * Returns a formated date String form a timestamp value,
     * the formatting based on the OpenCms system default locale
     * and the {@link Format#SHORT} date format.<p>
     *
     * @param time the time value to format as date
     * @return the formatted date
     */
    public static String getDateShort(long time) {

        return getDate(new Date(time), Format.SHORT);
    }

    /**
     * Returns a formated date and time String from a Date value,
     * the formatting based on the provided options.<p>
     *
     * @param date the Date object to format as String
     * @param format the format to use, see {@link Format} for possible values
     * @return the formatted date
     */
    public static String getDateTime(Date date, Format format) {

        StringBuffer buf = new StringBuffer();
        buf.append(getDate(date, format));
        buf.append(" ");
        buf.append(getTime(date, format));
        return buf.toString();
    }

    /**
     * Returns a formated date and time String form a timestamp value,
     * the formatting based on the OpenCms system default locale
     * and the {@link Format#SHORT} date format.<p>
     *
     * @param time the time value to format as date
     * @return the formatted date
     */
    public static String getDateTimeShort(long time) {

        return getDateTime(new Date(time), Format.SHORT);
    }

    /**
     * Returns a formated time String from a Date value,
     * the formatting based on the provided options.<p>
     *
     * @param date the Date object to format as String
     * @param format the format to use
     *
     * @return the formatted time
     */
    public static String getTime(Date date, Format format) {

        DateTimeFormat df;
        switch (format) {
            case FULL:
                df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_FULL);
                break;
            case LONG:
                df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_LONG);
                break;
            case MEDIUM:
                df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_MEDIUM);
                break;
            case SHORT:
                df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_SHORT);
                break;
            default:
                // can never happen, just to prevent stupid warning
                return "";
        }
        return df.format(date);
    }
}