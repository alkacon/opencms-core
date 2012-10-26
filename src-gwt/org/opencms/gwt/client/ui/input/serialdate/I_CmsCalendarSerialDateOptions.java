/*
 * File   : $Source: /alkacon/cvs/alkacon/com.alkacon.opencms.v8.calendar/src/com/alkacon/opencms/v8/calendar/I_CmsCalendarSerialDateOptions.java,v $
 * Date   : $Date: 2009/02/05 09:49:31 $
 * Version: $Revision: 1.2 $
 *
 * This file is part of the Alkacon OpenCms Add-On Module Package
 *
 * Copyright (c) 2008 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * The Alkacon OpenCms Add-On Module Package is free software: 
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Alkacon OpenCms Add-On Module Package is distributed 
 * in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Alkacon OpenCms Add-On Module Package.  
 * If not, see http://www.gnu.org/licenses/.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com.
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org.
 */

package org.opencms.gwt.client.ui.input.serialdate;

import java.util.List;
import java.util.Map;

/**
 * The calendar serial date options provide a method to filter entries according to the given view dates.<p>
 * 
 * Additionally, the serial type (e.g. weekly or monthly series) has to be provided.<p>
 * 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.1
 */
public interface I_CmsCalendarSerialDateOptions {

    /** Configuration key name for the serial date day of month. */
    String CONFIG_DAY_OF_MONTH = "dayofmonth";

    /** Configuration key name for the serial date end type. */
    String CONFIG_END_TYPE = "endtype";

    /** Configuration key name for the serial date end date and time (sets duration together with start date). */
    String CONFIG_ENDDATE = "enddate";

    /** Configuration key name for the serial date daily configuration: every working day flag. */
    String CONFIG_EVERY_WORKING_DAY = "everyworkingday";

    /** Configuration key name for the serial date interval. */
    String CONFIG_INTERVAL = "interval";

    /** Configuration key name for the serial date month. */
    String CONFIG_MONTH = "month";

    /** Configuration key name for the serial date number of occurences. */
    String CONFIG_OCCURENCES = "occurences";

    /** Configuration key name for the serial date: series end date. */
    String CONFIG_SERIAL_ENDDATE = "serialenddate";

    /** Configuration key name for the serial date start date and time. */
    String CONFIG_STARTDATE = "startdate";

    /** Configuration key name for the serial date type. */
    String CONFIG_TYPE = "type";

    /** Configuration key name for the serial date week day(s). */
    String CONFIG_WEEKDAYS = "weekdays";

    /** Series end type: ends at specific date. */
    int END_TYPE_DATE = 3;

    /** Series end type: ends never. */
    int END_TYPE_NEVER = 1;

    /** Series end type: ends after n times. */
    int END_TYPE_TIMES = 2;

    /** Serial type: daily series. */
    int TYPE_DAILY = 1;

    /** Serial type: monthly series. */
    int TYPE_MONTHLY = 3;

    /** Serial type: weekly series. */
    int TYPE_WEEKLY = 2;

    /** Serial type: yearly series. */
    int TYPE_YEARLY = 4;

    /**
     * Returns the configuration values for the serial date as Map.<p>
     * 
     * This Map can be used to store the configured options as property value on VFS resources.<p>
     * 
     * @return the configuration values for the serial date as Map
     */
    Map getConfigurationValuesAsMap();

    /**
     * Returns a list with changes to the serial date containing {@link CmsCalendarSerialDateChange} objects.<p>
     * 
     * @return a list with changes to the serial date
     */
    List getSerialDateChanges();

    /**
     * Returns a list of interruptions to the serial date containing {@link CmsCalendarSerialDateInterruption} objects.<p>
     * 
     * @return a list with interruptions to the serial date
     */
    List getSerialDateInterruptions();

    /**
     * Returns the date serial type (e.g. daily, weekly, monthly, yearly).<p>
     * 
     * @return the date serial type
     */
    int getSerialType();

    /**
     * Sets the list with changes to the serial date.<p>
     * 
     * @param serialDateChanges the list with changes to the serial date
     */
    void setSerialDateChanges(List serialDateChanges);

    /**
     * Sets the list with interruptions of the serial date.<p>
     * 
     * @param serialDateInterruptions the list with interruptions of the serial date
     */
    void setSerialDateInterruptions(List serialDateInterruptions);

}