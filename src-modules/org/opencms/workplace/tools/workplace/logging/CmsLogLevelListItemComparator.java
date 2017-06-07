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

package org.opencms.workplace.tools.workplace.logging;

import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.I_CmsListItemComparator;

import java.util.Comparator;
import java.util.Locale;

import org.apache.log4j.Level;

/**
 * Help function to select the comparator. <p>
 * Returns the comparator for the requested Column.<p>
 *
 * */

public class CmsLogLevelListItemComparator implements I_CmsListItemComparator {

    /** Constant for the DEBUG level. */
    public static final String DEBUG = "DEBUG";

    /** Constant for the INFO level. */
    public static final String INFO = "INFO";

    /** Constant for the WARN level. */
    public static final String WARN = "WARN";

    /** Constant for the ERROR level. */
    public static final String ERROR = "ERROR";

    /** Constant for the FATAL level. */
    public static final String FATAL = "FATAL";

    /** Constant for the OFF level. */
    public static final String OFF = "OFF";

    /**
     * @see org.opencms.workplace.list.I_CmsListItemComparator#getComparator(java.lang.String, java.util.Locale)
     */
    public Comparator<CmsListItem> getComparator(String columnId, Locale locale) {

        Comparator<CmsListItem> compa = null;
        // returns the Comparator for the "DEBUG" column
        if (columnId.equals(CmsLog4JAdminDialog.COLUMN_DEBUG)) {
            compa = new Comparator<CmsListItem>() {

                public int compare(CmsListItem o1, CmsListItem o2) {

                    int test = 0;
                    Level o1temp = Level.toLevel((String)o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    Level o2temp = Level.toLevel((String)o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    // returns 0 if both rows have the loglevel "Debug"
                    if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(DEBUG)
                        && o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(DEBUG)) {
                        test = 0;
                    } else {
                        // returns < 0 if the first rows have the loglevel "Debug"
                        if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(DEBUG)) {
                            test = -1;
                        } else
                            // returns > 0 if the second rows have the loglevel "Debug"
                            if (o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(DEBUG)) {
                            test = 1;
                        } else {
                            // sort other values by level value
                            if (o1temp.toInt() == o2temp.toInt()) {
                                test = 0;
                            } else if (o1temp.toInt() < o2temp.toInt()) {
                                test = -1;
                            } else {
                                test = 1;
                            }
                        }
                    }
                    return test;
                }

            };
        }
        // returns the Comparator for the "INFO" column
        if (columnId.equals(CmsLog4JAdminDialog.COLUMN_INFO)) {
            compa = new Comparator<CmsListItem>() {

                public int compare(CmsListItem o1, CmsListItem o2) {

                    int test = 0;
                    Level o1temp = Level.toLevel((String)o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    Level o2temp = Level.toLevel((String)o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    // returns 0 if both rows have the loglevel "INFO"
                    if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(INFO)
                        && o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(INFO)) {
                        test = 0;
                    } else {
                        // returns < 0 if the first rows have the loglevel "INFO"
                        if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(INFO)) {
                            test = -1;
                        } else
                            // returns > 0 if the second rows have the loglevel "INFO"
                            if (o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(INFO)) {
                            test = 1;
                        } else {
                            // sort other values by level value
                            if (o1temp.toInt() == o2temp.toInt()) {
                                test = 0;
                            } else if (o1temp.toInt() < o2temp.toInt()) {
                                test = -1;
                            } else {
                                test = 1;
                            }
                        }
                    }
                    return test;
                }

            };
        }
        // returns the Comparator for the "WARN" column
        if (columnId.equals(CmsLog4JAdminDialog.COLUMN_WARN)) {
            compa = new Comparator<CmsListItem>() {

                public int compare(CmsListItem o1, CmsListItem o2) {

                    int test = 0;
                    Level o1temp = Level.toLevel((String)o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    Level o2temp = Level.toLevel((String)o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    // returns 0 if both rows have the loglevel "WARN"
                    if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(WARN)
                        && o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(WARN)) {
                        test = 0;
                    } else {
                        // returns < 0 if the first rows have the loglevel "WARN"
                        if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(WARN)) {
                            test = -1;
                        } else
                            // returns > 0 if the second rows have the loglevel "WARN"
                            if (o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(WARN)) {
                            test = 1;
                        } else {
                            // sort other values by level value
                            if (o1temp.toInt() == o2temp.toInt()) {
                                test = 0;
                            } else if (o1temp.toInt() < o2temp.toInt()) {
                                test = -1;
                            } else {
                                test = 1;
                            }
                        }
                    }
                    return test;
                }

            };
        }
        // returns the Comparator for the "ERROR" column
        if (columnId.equals(CmsLog4JAdminDialog.COLUMN_ERROR)) {
            compa = new Comparator<CmsListItem>() {

                public int compare(CmsListItem o1, CmsListItem o2) {

                    int test = 0;
                    Level o1temp = Level.toLevel((String)o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    Level o2temp = Level.toLevel((String)o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    // returns 0 if both rows have the loglevel "ERROR"
                    if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(ERROR)
                        && o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(ERROR)) {
                        test = 0;
                    } else {
                        // returns < 0 if the first rows have the loglevel "ERROR"
                        if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(ERROR)) {
                            test = -1;
                        } else
                            // returns > 0 if the second rows have the loglevel "ERROR"
                            if (o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(ERROR)) {
                            test = 1;
                        } else {
                            // sort other values by level value
                            if (o1temp.toInt() == o2temp.toInt()) {
                                test = 0;
                            } else if (o1temp.toInt() < o2temp.toInt()) {
                                test = -1;
                            } else {
                                test = 1;
                            }
                        }
                    }
                    return test;
                }

            };
        }
        // returns the Comparator for the "FATAL" column
        if (columnId.equals(CmsLog4JAdminDialog.COLUMN_FATAL)) {
            compa = new Comparator<CmsListItem>() {

                public int compare(CmsListItem o1, CmsListItem o2) {

                    int test = 0;
                    Level o1temp = Level.toLevel((String)o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    Level o2temp = Level.toLevel((String)o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    // returns 0 if both rows have the loglevel "FATAL"
                    if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(FATAL)
                        && o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(FATAL)) {
                        test = 0;
                    } else {
                        // returns < 0 if the first rows have the loglevel "FATAL"
                        if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(FATAL)) {
                            test = -1;
                        } else
                            // returns > 0 if the second rows have the loglevel "FATAL"
                            if (o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(FATAL)) {
                            test = 1;
                        } else {
                            // sort other values by level value
                            if (o1temp.toInt() == o2temp.toInt()) {
                                test = 0;
                            } else if (o1temp.toInt() < o2temp.toInt()) {
                                test = -1;
                            } else {
                                test = 1;
                            }
                        }
                    }
                    return test;
                }

            };
        }
        // returns the Comparator for the "OFF" column
        if (columnId.equals(CmsLog4JAdminDialog.COLUMN_OFF)) {
            compa = new Comparator<CmsListItem>() {

                public int compare(CmsListItem o1, CmsListItem o2) {

                    int test = 0;
                    Level o1temp = Level.toLevel((String)o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    Level o2temp = Level.toLevel((String)o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL));
                    // returns 0 if both rows have the loglevel "Off"
                    if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(OFF)
                        && o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(OFF)) {
                        test = 0;
                    } else {
                        // returns < 0 if the first rows have the loglevel "Off"
                        if (o1.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(OFF)) {
                            test = -1;
                        } else
                            // returns > 0 if the second rows have the loglevel "Off"
                            if (o2.get(CmsLog4JAdminDialog.COLUMN_LOG_LEVEL).equals(OFF)) {
                            test = 1;
                        } else {
                            // sort other values by level value
                            if (o1temp.toInt() == o2temp.toInt()) {
                                test = 0;
                            } else if (o1temp.toInt() < o2temp.toInt()) {
                                test = -1;
                            } else {
                                test = 1;
                            }
                        }
                    }
                    return test;
                }

            };
        }

        return compa;
    }

}
