/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Utils.java,v $
* Date   : $Date: 2004/02/04 17:18:07 $
* Version: $Revision: 1.63 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This is a general helper class.<p>
 *
 * @author Andreas Schouten
 * @author Alexander Lucas
 */
public final class Utils {

    /**
     * Hides the public constructor.<p>
     */
    private Utils() {
        // empty
    }
    
    /**
     * Returns a formated time String form a long time value, the
     * format being "dd.mm.yy hh:mm".<p>
     * 
     * @param time the time value as a long
     * @return a formated time String form a long time value
     */
    public static String getNiceDate(long time) {
        StringBuffer niceTime = new StringBuffer();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(time));
        String day = "0" + new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();
        String month = "0" + new Integer(cal.get(Calendar.MONTH) + 1).intValue();
        String year = new Integer(cal.get(Calendar.YEAR)).toString();
        String hour = "0" + new Integer(cal.get(Calendar.HOUR) + 12
                * cal.get(Calendar.AM_PM)).intValue();
        String minute = "0" + new Integer(cal.get(Calendar.MINUTE));
        if (day.length() == 3) {
            day = day.substring(1, 3);
        }
        if (month.length() == 3) {
            month = month.substring(1, 3);
        }
        if (hour.length() == 3) {
            hour = hour.substring(1, 3);
        }
        if (minute.length() == 3) {
            minute = minute.substring(1, 3);
        }
        niceTime.append(day + ".");
        niceTime.append(month + ".");
        niceTime.append(year + " ");
        niceTime.append(hour + ":");
        niceTime.append(minute);
        return niceTime.toString();
    }

    /**
     * Returns a formated time string form a long time value, the
     * format being "dd.mm.yy".<p>
     * 
     * @param time the time value as a long
     * @return a formated time String form a long time value
     */
    public static String getNiceShortDate(long time) {
        StringBuffer niceTime = new StringBuffer();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(time));
        String day = "0" + new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();
        String month = "0" + new Integer(cal.get(Calendar.MONTH) + 1).intValue();
        String year = new Integer(cal.get(Calendar.YEAR)).toString();
        if (day.length() == 3) {
            day = day.substring(1, 3);
        }
        if (month.length() == 3) {
            month = month.substring(1, 3);
        }
        niceTime.append(day + ".");
        niceTime.append(month + ".");
        niceTime.append(year);
        return niceTime.toString();
    }
}
