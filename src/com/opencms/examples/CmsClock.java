/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/Attic/CmsClock.java,v $
 * Date   : $Date: 2000/03/01 15:56:33 $
 * Version: $Revision: 1.1 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.examples;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import java.util.*;

/**
 * Template class for displaying the processed contents of a example-clock. The result of
 * this class is not cacheable. The output-format can freely be changed by editing the
 * xml-templatefile. This template dosen't need it's own content-definition. <P>
 * 
 * In the xml-templatefile the following entrys can be done for the time-output:<P>
 * 
 * <blockquote>
 *         <code> &lt;method name="get"&gt;year&lt;/method&gt; </code>
 *         <code> &lt;method name="get"&gt;month&lt;/method&gt; </code>
 *         <code> &lt;method name="get"&gt;day_of_month&lt;/method&gt; </code>
 *         <code> &lt;method name="get"&gt;hour&lt;/method&gt; </code>
 *         <code> &lt;method name="get"&gt;minute&lt;/method&gt; </code>
 *         <code> &lt;method name="get"&gt;second&lt;/method&gt; </code>
 * </blockquote>
 * 
 * You can format the output by adding html-code between this method "get"-calls. You can
 * also use a subset of this calls to generate e.g. only the date and not the time information.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/03/01 15:56:33 $
 */
public class CmsClock extends CmsXmlTemplate {

    /**
     * Handles any occurence of this user method.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object get(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
		throws CmsException {
		
		// get the current time
		Calendar now = new GregorianCalendar();

		if("year".equals(tagcontent.toLowerCase())) {
			// return the year
			return now.get(Calendar.YEAR) + "";	
		} else if("month".equals(tagcontent.toLowerCase())) {
			// return the month
			return twoDigit(now.get(Calendar.MONTH) + 1 );	
		} else if("day_of_month".equals(tagcontent.toLowerCase())) {
			// return the day of month
			return twoDigit(now.get(Calendar.DAY_OF_MONTH ) );	
		} else if("hour".equals(tagcontent.toLowerCase())) {
			// return the hour
			return twoDigit( now.get(Calendar.HOUR_OF_DAY) );	
		} else if("minute".equals(tagcontent.toLowerCase())) {
			// return the minute
			return twoDigit( now.get(Calendar.MINUTE) );	
		} else if("second".equals(tagcontent.toLowerCase())) {
			// return the seconds
			return twoDigit( now.get(Calendar.SECOND) );	
		} else {
			return "?" + tagcontent + "?";
		}
	}

	/**
	 * Private method to format an integer into a two-digit string.
	 * @param value the int-value to format.
	 * @return the formated string.
	 */
	private String twoDigit(int value) {
         String ret= "0" + value;   
         if (ret.length()==3) {
             ret=ret.substring(1,3);
         }
		 return ret;
	}
	
    /**
     * Indicates if the results of this class are cacheable.
     * <P>
     * This result is NOT cacheable, because a clock is dynamicaly.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
		// not cacheable
		return false;
    }
}
