package com.opencms.util;

import java.util.*;

/**
 * This class has a static method to split a String into parts.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $
 */
public class Split
{

	/**
	 * This method splits a overgiven string into substrings. 
	 * 
	 * @param toSplit the String to split.
	 * @param at the delimeter.
	 * 
	 * @return an Array of Strings.
	 */
	public static String[] split(String toSplit, String at)
	{
		Vector parts = new Vector();
		int index = 0;
		int nextIndex = toSplit.indexOf(at);
		
		while(nextIndex != -1)
		{
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
