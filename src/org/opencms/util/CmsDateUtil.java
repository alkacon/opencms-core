/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsDateUtil.java,v $
 * Date   : $Date: 2004/06/07 12:44:05 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;

/**
 * 
 * Utilities to get and set formated dates in OpenCms.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.1 $
 */
public final class CmsDateUtil {
   
    /**
     * Hides the public constructor
     */
    private CmsDateUtil() {

        // noop
    }   

    /** The default format to use when formatting headers */
    public static final SimpleDateFormat C_HEADER_DEFAULT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    
    /**
     * Returns a formated date String from a Date value,
     * the formatting based on the provided options.<p>
     * 
     * @param date the Date object to format as String
     * @param format the format to use, see {@link CmsMessages} for possible values
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
     * and the {@link CmsMessages#SHORT} date format.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date 
     */
    public static String getDateShort(long time) {

        return getDate(new Date(time), CmsMessages.SHORT, OpenCms.getLocaleManager().getDefaultLocale());
    }

    /**
     * Returns a formated date and time String from a Date value,
     * the formatting based on the provided options.<p>
     * 
     * @param date the Date object to format as String
     * @param format the format to use, see {@link CmsMessages} for possible values
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
     * and the {@link CmsMessages#SHORT} date format.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date 
     */
    public static String getDateTimeShort(long time) {

        return getDateTime(new Date(time), CmsMessages.SHORT, OpenCms.getLocaleManager().getDefaultLocale());
    }
    
    
    /**
     * Returns a formated date and time String form a timestamp value based on the
     * HTTP-Header date format.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date 
     */
    public static String getHeaderDate(long time) {
        return C_HEADER_DEFAULT.format(new Date(time));
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