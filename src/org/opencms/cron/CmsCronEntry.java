/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cron/Attic/CmsCronEntry.java,v $
 * Date   : $Date: 2004/02/13 13:41:46 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cron;

import org.opencms.main.CmsException;
import org.opencms.util.CmsStringSubstitution;


import java.util.Calendar;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * @author Thomas Weckert (t.weckert@alkacon.com) 
 * @version $Revision: 1.5 $ $Date: 2004/02/13 13:41:46 $
 * @since 5.1.12
 */
public class CmsCronEntry extends Object {

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
     * A constructor for this Table-Entry.<p>
     *
     * @param paramstring a string which indicates the complete table-entry.
     * This string will be split into the parameters for the table-entry. The
     * format for the string is "min hour day-of-month month day-of-week module username".
     * @throws CmsException if something goes wrong
     */
    CmsCronEntry(String paramstring) throws CmsException {
        paramstring = CmsStringSubstitution.substitute(paramstring, "*", C_ASTERIX + "");
        try {
            String params[] = CmsStringSubstitution.split(paramstring, C_SPLITSTRING);
            m_minute = Integer.parseInt(params[0]);
            m_hour = Integer.parseInt(params[1]);
            m_dayOfMonth = Integer.parseInt(params[2]);
            m_month = Integer.parseInt(params[3]);
            m_dayOfWeek = Integer.parseInt(params[4]);
            m_user = new String(params[5]);
            m_group = new String(params[6]);
            m_moduleToLaunch = new String(params[7]);
            if (params.length > 8) {
                m_moduleParameter = new String(params[8]);
            } else {
                m_moduleParameter = null;
            }
        } catch (Exception exc) {
            throw new CmsException("Invalid cron entry: " + paramstring);
        }
    }
    
    /**
     * Creates a new crontab entry.<p>
     * 
     * @param min the minute when the cron job is executed
     * @param hour the hour when the cron job is executed
     * @param dayOfWeek the day of the week when the cron job is executed
     * @param month the month when the cron job is executed
     * @param dayOfMonth the day of the month when the cron job is executed
     * @param userName the user for the CmsObject instance when the cron job is executed
     * @param groupName the group for the CmsObject when the cron job is executed
     * @param className the class that gets executed when the cron job is executed
     * @param params optionaly parameters
     */
    CmsCronEntry(int min, int hour, int dayOfWeek, int month, int dayOfMonth, String userName, String groupName, String className, String params) {
        m_minute = min;
        m_hour = hour;
        m_dayOfMonth = dayOfMonth;
        m_month = month;
        m_dayOfWeek = dayOfWeek;
        m_user = userName;
        m_group = groupName;
        m_moduleToLaunch = className;
        m_moduleParameter = params;
    }

    /**
     * This method returns the paramstring for this table-entry. It is a string with all
     * fields conected via C_SPLITSTRING.<p>
     *
     * @return the parameterstring.
     */
    public String getParamstring() {
        return new String(((m_minute == C_ASTERIX) ? "*" : m_minute + "") + C_SPLITSTRING + ((m_hour == C_ASTERIX) ? "*" : m_hour + "") + C_SPLITSTRING + ((m_dayOfMonth == C_ASTERIX) ? "*" : m_dayOfMonth + "") + C_SPLITSTRING + ((m_month == C_ASTERIX) ? "*" : m_month + "") + C_SPLITSTRING + ((m_dayOfWeek == C_ASTERIX) ? "*" : m_dayOfWeek + "") + C_SPLITSTRING + m_user + C_SPLITSTRING + m_group + C_SPLITSTRING + m_moduleToLaunch + (m_moduleParameter == null ? "" : C_SPLITSTRING + m_moduleParameter));
    }

    /**
     * Checks this schedule-entry.<p>
     *
     * @param lastTime the date of the last checkScheduleTable-run.
     * @param now the date of this checkScheduleTable-run.
     *
     * @return true, if the module has to be launched or false, if not.
     */
    boolean check(Calendar lastTime, Calendar now) {
        // check the minute
        if ((m_minute != C_ASTERIX)             
            && (!isBetween(lastTime.get(Calendar.MINUTE), m_minute, now.get(Calendar.MINUTE)))) {            
                return false;
        }

        // check the hour
        if ((m_hour != C_ASTERIX) 
            && (m_hour != now.get(Calendar.HOUR_OF_DAY))) {
                return false;
        }

        // check the dayOfMonth
        if ((m_dayOfMonth != C_ASTERIX) 
            && (m_dayOfMonth != now.get(Calendar.DAY_OF_MONTH))) {
                return false;
        }

        // check the month
        if ((m_month != C_ASTERIX) 
           && (m_month != now.get(Calendar.MONTH))) {
                return false;
        }

        // check the dayOfWeek
        if ((m_dayOfWeek != C_ASTERIX) 
            && (m_dayOfWeek != now.get(Calendar.DAY_OF_WEEK))) {
                return false;
        }

        // all checks are ok - signal to launch the module
        return true;
    }

    /**
     * Checks, if the value is inbetween min and max.<p>
     *
     * @param min the minimum value
     * @param value the value
     * @param max the maximum value
     * @return true if (min < value) and (value <= max)
     */
    private boolean isBetween(int min, int value, int max) {
        if ((min < value) && (value <= max)) {
            return true;
        }

        return false;
    }

    /**
     * Gets the name of the module.<p>
     *
     * @return the module-name for this entry.
     */
    public String getModuleName() {
        return m_moduleToLaunch;
    }

    /**
     * Gets the parameter for the module.<p>
     *
     * @return the module-parameter for this entry.
     */
    public String getModuleParameter() {
        return m_moduleParameter;
    }

    /**
     * Gets the name of the user.<p>
     *
     * @return the user-name for this entry.
     */
    public String getUserName() {
        return m_user;
    }

    /**
     * Gets the name of the group.<p>
     *
     * @return the group-name for this entry.
     */
    public String getGroupName() {
        return m_group;
    }

    /**
     * Returns the minute when the job is launched.<p>
     * 
     * @return the minute when the job is launched
     */
    public int getMinute() {
        return m_minute;
    }

    /**
     * Returns the hour when the job is launched.<p>
     * 
     * @return the hour when the job is launched
     */
    public int getHour() {
        return m_hour;
    }

    /**
     * Returns the day of the month when the job is launched.<p>
     * 
     * @return the day of the month when the job is launched
     */
    public int getDayOfMonth() {
        return m_dayOfMonth;
    }

    /**
     * Returns the month when the job is launched.<p>
     * 
     * @return the month when the job is launched
     */
    public int getMonth() {
        return m_month;
    }

    /**
     * Retruns the day of the week when the job is launched.<p>
     * 
     * @return the day of the week when the job is launched
     */ 
    public int getDayOfWeek() {
        return m_dayOfWeek;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "[" + this.getClass().getName() + ": " + getParamstring() + "]";
    }
    
    /**
     * Returns a dom4j XML tree for this con entry.<p>
     * 
     * @return a dom4j XML tree for this con entry
     */
    public Element toXml() {
        Document document = DocumentHelper.createDocument();
        Element cronjob = document.addElement("cronjob");
        
        cronjob.addElement("min").addText(Integer.toString(getMinute()));
        cronjob.addElement("hour").addText(Integer.toString(getHour()));
        cronjob.addElement("dayofmonth").addText(Integer.toString(getDayOfMonth()));
        cronjob.addElement("month").addText(Integer.toString(getMonth()));
        cronjob.addElement("dayofweek").addText(Integer.toString(getDayOfWeek()));
        cronjob.addElement("class").addText(getModuleName());
        cronjob.addElement("user").addText(getUserName());
        
        return cronjob;        
    }
    
}
