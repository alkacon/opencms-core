package com.opencms.util;

import java.util.*;

/**
 * This is a general helper class.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/02/14 19:03:11 $
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
