/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/I_CmsWidgetDialog.java,v $
 * Date   : $Date: 2005/02/17 12:44:32 $
 * Version: $Revision: 1.2 $
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
 
package org.opencms.workplace.xmlwidgets;

import org.opencms.workplace.CmsWorkplaceSettings;

import java.text.ParseException;
import java.util.Locale;


/**
 * Describes a widget enabled dialog.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.5.2
 */
public interface I_CmsWidgetDialog {
    
    /**
     * Generates a button for the OpenCms workplace.<p>
     * 
     * @param href the href link for the button, if none is given the button will be disabled
     * @param target the href link target for the button, if none is given the target will be same window
     * @param image the image name for the button, skin path will be automattically added as prefix
     * @param label the label for the text of the button 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * 
     * @return a button for the OpenCms workplace
     */
    String button(String href, String target, String image, String label, int type);
    
    /**
     * Returns the html for an invisible spacer between button bar contents like buttons, labels, etc.<p>
     * 
     * @param width the width of the invisible spacer
     * @return the html for the invisible spacer
     */
    String buttonBarSpacer(int width);
    
    /**
     * Displays a javascript calendar element with the standard "opencms" style.<p>
     * 
     * Creates the HTML javascript and stylesheet includes for the head of the page.<p>
     * 
     * @return the necessary HTML code for the js and stylesheet includes
     */
    String calendarIncludes();
    
    /**
     * Initializes a javascript calendar element to be shown on a page.<p>
     * 
     * This method must be called at the end of a HTML page, e.g. before the closing &lt;body&gt; tag.<p>
     * 
     * @param inputFieldId the ID of the input field where the date is pasted to
     * @param triggerButtonId the ID of the button which triggers the calendar
     * @param align initial position of the calendar popup element
     * @param singleClick if true, a single click selects a date and closes the calendar, otherwise calendar is closed by doubleclick
     * @param weekNumbers show the week numbers in the calendar or not
     * @param mondayFirst show monday as first day of week
     * @param dateStatusFunc name of the function which determines if/how a date should be disabled
     * @param showTime true if the time selector should be shown, otherwise false
     * @return the HTML code to initialize a calendar poup element
     */
    String calendarInit(String inputFieldId, String triggerButtonId, String align, boolean singleClick, boolean weekNumbers, boolean mondayFirst, String dateStatusFunc, boolean showTime);
    
    /**
     * Creates the time in milliseconds from the given parameter.<p>
     * 
     * @param dateString the String representation of the date
     * @param useTime true if the time should be parsed, too, otherwise false
     * @return the time in milliseconds
     * @throws ParseException if something goes wrong
     */
    long getCalendarDate(String dateString, boolean useTime) throws ParseException;
    
    /**
     * Returns the given timestamp as String formatted in a localized pattern.<p>
     * 
     * @param timestamp the time to format
     * @return the given timestamp as String formatted in a localized pattern
     */
    String getCalendarLocalizedTime(long timestamp);
    
    /**
     * Returns the current element locale.<p>
     * 
     * @return the current element locale
     */
    Locale getElementLocale();
    
    /**
     * Returns the current workplace encoding.<p>
     * 
     * @return the current workplace encoding
     */
    String getEncoding();
    
    /**
     * Returns the current users locale setting.<p>
     * 
     * This is a convenience method that just 
     * executes the following code: 
     * <code>getCms().getRequestContext().getLocale()</code>.<p>
     * 
     * @return the current users locale setting
     */
    Locale getLocale();
    
    /**
     * Returns the current users workplace settings.<p>
     * 
     * @return the current users workplace settings
     */
    CmsWorkplaceSettings getSettings();
    
    /**
     * Get a localized key value for the workplace.<p>
     * 
     * @param keyName name of the key
     * @return a localized key value
     */
    String key(String keyName);
    
}
