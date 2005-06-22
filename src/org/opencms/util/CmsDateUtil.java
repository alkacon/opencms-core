/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsDateUtil.java,v $
 * Date   : $Date: 2005/06/22 14:58:54 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.i18n.CmsLocaleManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 
 * Utilities to get and set formated dates in OpenCms.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsDateUtil {

    /** The "GMT" time zone, used when formatting http headers. */
    protected static final TimeZone C_GMT_TIMEZONE = TimeZone.getTimeZone("GMT");

    /** The default format to use when formatting http headers. */
    protected static final DateFormat C_HEADER_DEFAULT = new SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        Locale.US);

    /** The default format to use when formatting old cookies. */
    protected static final DateFormat C_OLD_COOKIE = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);

    /**
     * Hides the public constructor.<p>
     */
    private CmsDateUtil() {

        // noop
    }

    /**
     * Returns a formated date String from a Date value,
     * the formatting based on the provided options.<p>
     * 
     * @param date the Date object to format as String
     * @param format the format to use, see {@link DateFormat} for possible values
     * @param locale the locale to use
     * @return the formatted date 
     */
    public static String getDate(Date date, int format, Locale locale) {

        DateFormat df = DateFormat.getDateInstance(format, locale);
        return df.format(date);
    }

    /**
     * Returns a formated date String form a timestamp value,
     * the formatting based on the OpenCms system default locale
     * and the {@link DateFormat#SHORT} date format.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date 
     */
    public static String getDateShort(long time) {

        return getDate(new Date(time), DateFormat.SHORT, CmsLocaleManager.getDefaultLocale());
    }

    /**
     * Returns a formated date and time String from a Date value,
     * the formatting based on the provided options.<p>
     * 
     * @param date the Date object to format as String
     * @param format the format to use, see {@link DateFormat} for possible values
     * @param locale the locale to use
     * @return the formatted date 
     */
    public static String getDateTime(Date date, int format, Locale locale) {

        DateFormat df = DateFormat.getDateInstance(format, locale);
        DateFormat tf = DateFormat.getTimeInstance(format, locale);
        StringBuffer buf = new StringBuffer();
        buf.append(df.format(date));
        buf.append(" ");
        buf.append(tf.format(date));
        return buf.toString();
    }

    /**
     * Returns a formated date and time String form a timestamp value,
     * the formatting based on the OpenCms system default locale
     * and the {@link DateFormat#SHORT} date format.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date 
     */
    public static String getDateTimeShort(long time) {

        return getDateTime(new Date(time), DateFormat.SHORT, CmsLocaleManager.getDefaultLocale());
    }

    /**
     * Returns a formated date and time String form a timestamp value based on the
     * HTTP-Header date format.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date 
     */
    public static String getHeaderDate(long time) {

        if (C_HEADER_DEFAULT.getTimeZone() != C_GMT_TIMEZONE) {
            // ensure GMT is used as time zone for the header generation
            C_HEADER_DEFAULT.setTimeZone(C_GMT_TIMEZONE);
        }

        return C_HEADER_DEFAULT.format(new Date(time));
    }

    /**
     * Returns a formated date and time String form a timestamp value based on the
     * (old) Netscape cookie date format.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date 
     */
    public static String getOldCookieDate(long time) {

        if (C_OLD_COOKIE.getTimeZone() != C_GMT_TIMEZONE) {
            // ensure GMT is used as time zone for the header generation
            C_OLD_COOKIE.setTimeZone(C_GMT_TIMEZONE);
        }

        return C_OLD_COOKIE.format(new Date(time));
    }

    /**
     * Parses a formated date and time string in HTTP-Header date format and returns the 
     * time value.<p>
     *  
     * @param timestamp the timestamp in HTTP-Header date format
     * @return time value as long
     * @throws ParseException if parsing fails
     */
    public static long parseHeaderDate(String timestamp) throws ParseException {

        return C_HEADER_DEFAULT.parse(timestamp).getTime();
    }

}