/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Utils.java,v $
* Date   : $Date: 2003/09/16 12:06:10 $
* Version: $Revision: 1.56 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.defaults.I_CmsLifeCycle;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRegistry;
import com.opencms.file.CmsUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

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
    }
    
    /**
     * Returns a string representation of the full name of a user.<p>
     * 
     * @param user the user to get the full name from
     * @return a string representation of the user fullname
     */
    public static String getFullName(CmsUser user) {
        String retValue = "";
        if (user != null) {
            retValue += user.getFirstname() + " ";
            retValue += user.getLastname() + " (";
            retValue += user.getName() + ")";
        }
        return retValue;
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
    
    /**
     * Converts date string to a long value.<p>
     * 
     * @param dateString the date as a String
     * @return long value of date
     */
    public static long splitDate(String dateString) {
        long result = 0;
        if (dateString != null && !"".equals(dateString)) {
            String splittetDate[] = Utils.split(dateString, ".");
            GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]),
                    Integer.parseInt(splittetDate[1]) - 1, Integer.parseInt(splittetDate[0]), 0, 0, 0);
            result = cal.getTime().getTime();
        }
        return result;
    }    

    /**
     * Returns all publish methods of the configured modules.<p>
     *
     * @param cms the current cms context object
     * @param changedLinks will be filled with the links (as String) that have changed during the publishing
     * @throws CmsException if something goes wrong
     */
    public static void getModulPublishMethods(CmsObject cms, Vector changedLinks) throws CmsException {
        // now publish the module masters
        Vector publishModules = new Vector();
        cms.getRegistry().getModulePublishables(publishModules, I_CmsConstants.C_PUBLISH_METHOD_LINK);

        for (int i = 0; i < publishModules.size(); i++) {
            // call the publishProject method of the class with parameters:
            // cms, changedLinks
            try {
                Class.forName((String)publishModules.elementAt(i)).getMethod("publishLinks",
                                        new Class[] {CmsObject.class, Vector.class}).invoke(
                                        null, new Object[] {cms, changedLinks});
            } catch (Exception ex) {
            ex.printStackTrace();
                if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                    OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, "Error when publish data of module "+(String)publishModules.elementAt(i)+"!: "+ex.getMessage());
                }
            }
        }
    }

    /**
     * Calls the startup method on all module classes that are registerd in the registry.<p>
     *
     * @param cms the current cms context object
     * @throws CmsException if something goes wrong
     */
    public static void getModulStartUpMethods(CmsObject cms) throws CmsException {
        Vector startUpModules = new Vector();
        cms.getRegistry().getModuleLifeCycle(startUpModules);
        for (int i = 0; i < startUpModules.size(); i++) {
            try {
                I_CmsLifeCycle lifeClass = (I_CmsLifeCycle)Class.forName((String)startUpModules.elementAt(i)).getConstructor(new Class[] {}).newInstance(new Class[] {});
                lifeClass.startUp(cms);
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Calls the shutdown method on all module classes that are registerd in the registry.<p>
     *
     * @param reg the current registry
     * @throws CmsException if something goes wrong
     */
    public static void getModulShutdownMethods(CmsRegistry reg) throws CmsException {
        Vector startUpModules = new Vector();
        reg.getModuleLifeCycle(startUpModules);
        for (int i = 0; i < startUpModules.size(); i++) {
            try {
                I_CmsLifeCycle lifeClass = (I_CmsLifeCycle)Class.forName((String)startUpModules.elementAt(i)).getConstructor(new Class[] {}).newInstance(new Class[] {});
                lifeClass.shutDown();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Gets the stack-trace of a exception, and returns it as a string.
     * @param e The exception to get the stackTrace from.
     * @return the stackTrace of the exception.
     */
    public static String getStackTrace(Throwable e) {

        // print the stack-trace into a writer, to get its content
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        e.printStackTrace(writer);
        if (e instanceof CmsException) {
            CmsException cmsException = (CmsException)e;
            if (cmsException.getException() != null) {
                cmsException.getException().printStackTrace(writer);
            }
        }
        try {
            writer.close();
            stringWriter.close();
        } catch (Exception err) {
            // ignore
        }
        return stringWriter.toString();
    }

    /**
     * Replaces all line breaks in a given string object by
     * white spaces, also all lines will be trimed to
     * delete all unnecessary white spaces.<p>
     * 
     * @param s Input string
     * @return Output String
     * @throws CmsException if something goes wrong
     */
    public static String removeLineBreaks(String s) throws CmsException {
        StringBuffer result = new StringBuffer();
        BufferedReader br = new BufferedReader(new StringReader(s));
        String lineStr = null;
        try {
            while ((lineStr = br.readLine()) != null) {
                result.append(lineStr.trim());
                result.append(" ");
            }
        } catch (IOException e) {
            throw new CmsException("Error while reading input stream in com.opencms.util.Utils.removeLineBreaks: " + e);
        }
        return result.toString();
    }

    /**
     * This method splits a String into substrings.
     *
     * @param toSplit the String to split
     * @param at the delimeter to split at
     *
     * @return an array of Strings
     */
    public static final String[] split(String toSplit, String at) {
        List parts = new ArrayList();
        int index = 0;
        int nextIndex = toSplit.indexOf(at);
        while (nextIndex != -1) {
            parts.add(toSplit.substring(index, nextIndex));
            index = nextIndex + at.length();
            nextIndex = toSplit.indexOf(at, index);
        }
        parts.add(toSplit.substring(index));
        return (String[])parts.toArray(new String[parts.size()]);
    }

    /**
     * Sorts two vectors using bubblesort.<p>
     * 
     * This is a quick hack to display templates sorted by title instead of
     * by name in the template dropdown, because it is the title that is shown in the dropdown.
     *
     * @param names the vector to sort
     * @param data vector with data that accompanies names
     */
    public static void bubblesort(Vector names, Vector data) {
        for (int i = 0; i < names.size() - 1; i++) {
            int len = names.size() - i - 1;
            for (int j = 0; j < len; j++) {
                String a = (String)names.elementAt(j);
                String b = (String)names.elementAt(j + 1);
                if (a.toLowerCase().compareTo(b.toLowerCase()) > 0) {
                    names.setElementAt(a, j + 1);
                    names.setElementAt(b, j);
                    a = (String)data.elementAt(j);
                    data.setElementAt(data.elementAt(j + 1), j);
                    data.setElementAt(a, j + 1);
                }
            }
        }
    }
}
