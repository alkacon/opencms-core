/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input.datebox;

import org.opencms.gwt.client.Messages;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * This class is an Helper with mostly static methods that convert a given date object
 * or that convert a given String.<p> 
 */
public final class CmsDateConverter {

    /** A constant for am. */
    public static final String AM = "am";

    /** A constant for pm. */
    public static final String PM = "pm";

    /** The part of the 12 hour presentation which signals the am pm. */
    private static final String AMPM_PATTERN_PART = "aa";

    /** A pattern for date time representation. */
    private static final String DATETIME_PATTERN = Messages.get().key(Messages.GUI_DATEBOX_DATETIME_PATTERN_0);

    /** A pattern for date time representation in 12 hour presentation. */
    private static final String TIME_PATTERN = Messages.get().key(Messages.GUI_DATEBOX_TIME_PATTERN_0);

    /** The formatter for the date time format. */
    private static final DateTimeFormat Z_DATETIME_FORMAT = DateTimeFormat.getFormat(DATETIME_PATTERN);

    /** The formatter for the time format. */
    private static final DateTimeFormat Z_TIME_FORMAT = DateTimeFormat.getFormat(TIME_PATTERN);

    /**
     * Hiding constructor.<p>
     */
    private CmsDateConverter() {

        // noop
    }

    /**
     * Cuts the suffix (am or pm) from a given time String.<p>
     * 
     * If the given String has less than 5 characters an dosen't 
     * contains an am or pm in it the original String is returned.<p>
     * 
     * @param time the time String to cut the suffix from
     * 
     * @return the time String without the suffix or the original String if the format of the String is incorrect
     */
    public static String cutSuffix(String time) {

        String ret = time.toLowerCase();
        if (ret.toLowerCase().contains(CmsDateConverter.AM) || ret.toLowerCase().contains(CmsDateConverter.PM)) {
            if (ret.length() > 5) {
                ret = ret.replace(CmsDateConverter.AM, "");
                ret = ret.replace(CmsDateConverter.PM, "");
                ret = ret.trim();
            }
        }
        return ret;
    }

    /**
     * Merges a given Date object with a given time String.<p>
     * 
     * Returns a <code>null</code> if the given time format coudn't be parsed.<p>
     * 
     * The expected time String should include the am pm information if the time format is in 12 hour presentation.<p>
     * 
     * @param date the given Date object which time has to be set
     * @param time the given time String which should be inserted into the Date Object
     * 
     * @return the merged date Object
     */
    @SuppressWarnings("deprecation")
    public static Date getDateWithTime(Date date, String time) {

        Date result;
        try {
            result = Z_TIME_FORMAT.parse(time);
            result.setDate(date.getDate());
            result.setMonth(date.getMonth());
            result.setYear(date.getYear());
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    /**
     * Returns the short time format of a given date as String.<p>
     * 
     * @param date the date to get the short time format from
     * 
     * @return the short time format of a given date
     */
    public static String getTime(Date date) {

        return Z_TIME_FORMAT.format(date);
    }

    /**
     * Returns <code>true</code> if the current date format is in the 12 hour 
     * representation mode <code>false</code> otherwise.<p>
     * 
     * @return <code>true</code> if an am or a pm is in a new Date object <code>false</code> otherwise
     */
    public static boolean is12HourPresentation() {

        return DATETIME_PATTERN.toLowerCase().contains(AMPM_PATTERN_PART);
    }

    /**
     * Returns <code>true</code> if an am is in the given date object <code>false</code> otherwise.<p>
     * 
     * @param date the date to check
     * 
     * @return <code>true</code> if an am is in the given Date object <code>false</code> otherwise
     */
    public static boolean isAm(Date date) {

        String time = getTime(date);
        return time.toLowerCase().contains(AM);
    }

    /**
     * Parses the provided String as a date.<p>
     * 
     * First try to parse the String with the given time format.<p>
     * 
     * If that fails try to parse the date with the browser settings.<p>
     * 
     * @param dateText the string representing a date
     * 
     * @return the date created, or null if there was a parse error
     * 
     * @throws Exception 
     */
    public static Date toDate(final String dateText) throws Exception {

        Date date = null;
        if (dateText.length() > 0) {
            date = Z_DATETIME_FORMAT.parse(dateText.trim());
            if (!validateDate(date)) {
                throw new IllegalArgumentException();
            }
        }
        return date;
    }

    /**
     * Formats the provided date. Note, a null date is a possible input.
     * 
     * @param date the date to format
     * 
     * @return the formatted date as a string
     */
    public static String toString(final Date date) {

        String result;
        if (date == null) {
            result = "";
        } else {
            result = Z_DATETIME_FORMAT.format(date);
        }
        return result;
    }

    /**
     * Validates a time String if it matches one of the two regular expressions.<p>
     * 
     * Returns <code>true</code> if the given date matches to one of the regular 
     * expressions, <code>false</code> otherwise.<p> 
     * 
     * @param date the date String to check
     * 
     * @return <code>true</code> if the given time matches to one of the regular expressions, <code>false</code> otherwise
     */
    public static boolean validateDate(Date date) {

        String time = getTime(date);
        return validateTime(time);

    }

    /**
     * Validates a time String if it matches the regular expressions.<p>
     * 
     * Returns <code>true</code> if the given time matches the regular 
     * expressions, <code>false</code> otherwise.<p> 
     * 
     * @param time the time String to check
     * 
     * @return <code>true</code> if the given time matches the regular expressions, <code>false</code> otherwise
     */
    public static native boolean validateTime(String time) /*-{
        var hasMeridian = false;
        var re = /^\d{1,2}[:]\d{2}([:]\d{2})?( [aApP][mM]?)?$/;
        if (!re.test(time)) {
            return false;
        }
        if (time.toLowerCase().indexOf("p") != -1) {
            hasMeridian = true;
        }
        if (time.toLowerCase().indexOf("a") != -1) {
            hasMeridian = true;
        }
        var values = time.split(":");
        if ((parseFloat(values[0]) < 0) || (parseFloat(values[0]) > 23)) {
            return false;
        }
        if (hasMeridian) {
            if ((parseFloat(values[0]) < 1) || (parseFloat(values[0]) > 12)) {
                return false;
            }
        }
        if ((parseFloat(values[1]) < 0) || (parseFloat(values[1]) > 59)) {
            return false;
        }
        if (values.length > 2) {
            if ((parseFloat(values[2]) < 0) || (parseFloat(values[2]) > 59)) {
                return false;
            }
        }
        return true;
    }-*/;
}
