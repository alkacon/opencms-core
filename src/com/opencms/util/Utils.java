/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Utils.java,v $
 * Date   : $Date: 2000/02/19 10:32:16 $
 * Version: $Revision: 1.4 $
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

package com.opencms.util;

import java.util.*;

/**
 * This is a general helper class.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.4 $ $Date: 2000/02/19 10:32:16 $
 */
public class Utils {
	/**
	 * This method splits a overgiven string into substrings. 
	 * 
	 * @param toSplit the String to split.
	 * @param at the delimeter.
	 * 
	 * @return an Array of Strings.
	 */
	public static final String[] split(String toSplit, String at) {
		Vector parts = new Vector();
		int index = 0;
		int nextIndex = toSplit.indexOf(at);
		
		while(nextIndex != -1) {
			parts.addElement( (Object) toSplit.substring(index, nextIndex) );
			index = nextIndex + at.length();
			nextIndex = toSplit.indexOf(at, index);
		}
		parts.addElement( (Object) toSplit.substring(index) );
		
		String partsArray[] = new String[parts.size()];
		parts.copyInto((Object[]) partsArray );
		return(partsArray);
	}


     /**
      * Gets a formated time string form a long time value.
      * @param time The time value as a long.
      * @return Formated time string.
      */
     public static String getNiceDate(long time) {
         StringBuffer niceTime=new StringBuffer();
         
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(new Date(time));
         String day="0"+new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();        
         String month="0"+new Integer(cal.get(Calendar.MONTH)+1).intValue(); 
         String year=new Integer(cal.get(Calendar.YEAR)).toString();
         String hour="0"+new Integer(cal.get(Calendar.HOUR)+12*cal.get(Calendar.AM_PM)).intValue();   
         String minute="0"+new Integer(cal.get(Calendar.MINUTE));   
         if (day.length()==3) {
             day=day.substring(1,3);
         }
         if (month.length()==3) {
             month=month.substring(1,3);
         }
         if (hour.length()==3) {
             hour=hour.substring(1,3);
         }
         if (minute.length()==3) {
             minute=minute.substring(1,3);
         }
         niceTime.append(day+".");
         niceTime.append(month+".");  
         niceTime.append(year+" ");
         niceTime.append(hour+":");
         niceTime.append(minute);
         return niceTime.toString();
     }

     /**
      * Gets a formated time string form a long time value.
      * @param time The time value as a long.
      * @return Formated time string.
      */
     public static String getNiceShortDate(long time) {
         StringBuffer niceTime=new StringBuffer();
         
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(new Date(time));
         String day="0"+new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();        
         String month="0"+new Integer(cal.get(Calendar.MONTH)+1).intValue(); 
         String year=new Integer(cal.get(Calendar.YEAR)).toString();
         if (day.length()==3) {
             day=day.substring(1,3);
         }
         if (month.length()==3) {
             month=month.substring(1,3);
         }
         niceTime.append(day+".");
         niceTime.append(month+".");  
         niceTime.append(year+" ");
         return niceTime.toString();
     }
}
