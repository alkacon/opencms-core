/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsCronEntry.java,v $
* Date   : $Date: 2003/07/21 11:07:17 $
* Version: $Revision: 1.3 $
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

package com.opencms.core;

import java.util.*;

import com.opencms.flex.util.CmsStringSubstitution;
import com.opencms.util.*;

class CmsCronEntry {

	/**
	 * The minute, when to lauch.
	 */
	private int m_minute;

	/**
	 * The hour, when to lauch.
	 */
	private int m_hour;

	/**
	 * The day of a month, when to lauch.
	 */
	private int m_dayOfMonth;

	/**
	 * The month, when to lauch.
	 */
	private int m_month;

	/**
	 * The day of a week, when to lauch.
	 */
	private int m_dayOfWeek;

	/**
	 * The module to lauch at its time.
	 */
	private String m_moduleToLaunch;

	/**
	 * The parameter fot the module to lauch.
	 */
	private String m_moduleParameter;

	/**
	 * The user with its rights to lauch the module.
	 */
	private String m_user;

	/**
	 * The group with its rights to lauch the module.
	 */
	private String m_group;

	/**
	 * The asterix-Value this value is set where no check has to be performed.
	 */
	public static final int C_ASTERIX = -1;

	/**
	 * This string indicates, where to split the parameterstring.
	 */
	private static final String C_SPLITSTRING = " ";

	/**
	 * A constructor for this Table-Entry.
	 *
	 * @param paramstring a string which indicates the complete table-entry.
	 * This string will be split into the parameters for the table-entry. The
	 * format for the string is
	 * "min hour day-of-month month day-of-week module username".
	 */
	CmsCronEntry(String paramstring)
		throws CmsException	{
        paramstring = CmsStringSubstitution.substitute(paramstring, "*", C_ASTERIX + "");
		try {
			String params[] =  Utils.split(paramstring, C_SPLITSTRING);
			m_minute =			Integer.parseInt(params[0]);
			m_hour =			Integer.parseInt(params[1]);
			m_dayOfMonth =		Integer.parseInt(params[2]);
			m_month =			Integer.parseInt(params[3]);
			m_dayOfWeek =		Integer.parseInt(params[4]);
			m_user =			new String(params[5]);
			m_group =			new String(params[6]);
			m_moduleToLaunch =	new String(params[7]);
            if(params.length > 8) {
    			m_moduleParameter =	new String(params[8]);
            } else {
                m_moduleParameter = null;
            }
		} catch(Exception exc) {
			throw new CmsException("Invalid parameterstring. Exception: " + exc.toString());
		}
	}

	/**
	 * This method returns the paramstring for this table-entry. It is a string with all
	 * fields conected via C_SPLITSTRING
	 *
	 * @return the parameterstring.
	 */
	public String getParamstring() {
		return new String( ((m_minute == C_ASTERIX) ? "*" : m_minute + "") + C_SPLITSTRING +
						   ((m_hour == C_ASTERIX) ? "*" : m_hour + "") + C_SPLITSTRING +
						   ((m_dayOfMonth == C_ASTERIX) ? "*" : m_dayOfMonth + "") + C_SPLITSTRING +
						   ((m_month == C_ASTERIX) ? "*" : m_month + "") + C_SPLITSTRING +
						   ((m_dayOfWeek == C_ASTERIX) ? "*" : m_dayOfWeek + "")+ C_SPLITSTRING +
                           m_user + C_SPLITSTRING +
                           m_group + C_SPLITSTRING +
						   m_moduleToLaunch +
						   (m_moduleParameter == null ? "" : C_SPLITSTRING + m_moduleParameter));
	}

	/**
	 * Checks this schedule-entry.
	 *
	 * @param lastTime the date of the last checkScheduleTable-run.
	 * @param now the date of this checkScheduleTable-run.
	 *
	 * @return true, if the module has to be launched or false, if not.
	 */
	boolean check(Calendar lastTime, Calendar now) {
        if(m_minute != C_ASTERIX) // check the minute
    		if(!isBetween(lastTime.get(Calendar.MINUTE), m_minute, now.get(Calendar.MINUTE)))
	    		return false;

		if(m_hour != C_ASTERIX)	// check the hour
			if(m_hour != now.get(Calendar.HOUR_OF_DAY))
				return false;

		if(m_dayOfMonth != C_ASTERIX)	// check the dayOfMonth
			if(m_dayOfMonth != now.get(Calendar.DAY_OF_MONTH))
				return false;

		if(m_month != C_ASTERIX)	// check the month
			if(m_month != now.get(Calendar.MONTH))
				return false;

		if(m_dayOfWeek != C_ASTERIX)	// check the dayOfWeek
			if(m_dayOfWeek != now.get(Calendar.DAY_OF_WEEK))
				return false;

		// all checks are ok - signal to launch the module
		return true;
	}

	/**
	 * Checks, if the value is inbetween min and max.
	 *
	 * @param min.
	 * @param value.
	 * @param max.
	 */
	private boolean isBetween(int min, int value, int max) {
		if((min < value) && (value <= max))
			return true;
		else
			return false;
	}

	/**
	 * Gets the name of the module.
	 *
	 * @return the module-name for this entry.
	 */
	public String getModuleName() {
		return m_moduleToLaunch;
	}

	/**
	 * Gets the parameter for the module.
	 *
	 * @return the module-parameter for this entry.
	 */
	public String getModuleParameter() {
		return m_moduleParameter;
	}

	/**
	 * Gets the name of the user.
	 *
	 * @return the user-name for this entry.
	 */
	public String getUserName() {
		return m_user;
	}

	/**
	 * Gets the name of the group.
	 *
	 * @return the group-name for this entry.
	 */
	public String getGroupName() {
		return m_group;
	}

	public int getMinute() {
		return m_minute;
	}

	public int getHour() {
		return m_hour;
	}

	public int getDayOfMonth() {
		return m_dayOfMonth;
	}

	public int getMonth() {
		return m_month;
	}

	public int getDayOfWeek() {
		return m_dayOfWeek;
	}

    public String toString() {
        return getClass().getName() + "{" + getParamstring() + "}";
    }
}
