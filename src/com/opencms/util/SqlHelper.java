package com.opencms.util;

import java.util.*;
import java.io.*;

/**
 * This class has several methos to add data to a SQL stament.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.1 $
 */
public class SqlHelper {

	/**
	 * This method splits a overgiven string into substrings. 
	 * 
	 * @param toSplit the String to split.
	 * @param at the delimeter.
	 * 
	 * @return an Array of Strings.
	 */
	public static String[] split(String toSplit, String at)	{
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
     * Converts a string so that it can be inserted into an SQL statement.
     * @param parameter The string to be converted.
     * @return Converted string.
     */
   public static String putString(String parameter) {
       String result=null;
       
       if(parameter == null) {
	  
	    StringBuffer buffer = new StringBuffer();
	    int i;
			        
	    buffer.append('\'');

	    for (i = 0 ; i < parameter.length() ; ++i) {
		    char c = parameter.charAt(i);		
		    if (c == '\\' || c == '\'' || c == '"') {
		        buffer.append((char)'\\');
		    }
		    buffer.append(c);
        }	
	    buffer.append('\'');
        result=buffer.toString();
      }
      return result;  
   }
   
     /**
     * Converts a byte array so that it can be inserted into an SQL statement.
     * @param parameter The string to be converted.
     * @return Converted string.
     */
   public static String putByteArray(byte[] parameter) {
      	ByteArrayOutputStream BytesOut = new ByteArrayOutputStream();
        int size=parameter.length;
      
    	BytesOut.write('\'');
   
    	for (int i=0;i<size;i++) {
	        byte b = parameter[i];
	        if (b == '\0') {
		        BytesOut.write('\\');
		        BytesOut.write('0');
	        } else {
		        if (b == '\\' || b == '\'' || b == '"') {
		            BytesOut.write('\\');
		        }
            }
		    BytesOut.write(b);
	    }
   
	    BytesOut.write('\'');
        
        return new String(BytesOut.toByteArray());
        
   }
}
