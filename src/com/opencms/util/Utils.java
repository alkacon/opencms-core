/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Utils.java,v $
 * Date   : $Date: 2000/02/15 17:44:01 $
 * Version: $Revision: 1.2 $
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
 * @version $Revision: 1.2 $ $Date: 2000/02/15 17:44:01 $
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
}
