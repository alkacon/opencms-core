package com.opencms.util;

import java.io.*;
import java.util.*;
import java.net.*;


import javax.servlet.*;
import javax.servlet.http.*;


/**
 * The Encoder provies static methods to decode and encode data. <br>
 * The de- and encoding uses the same coding mechanism as JavaScript, special characters are
 * replaxed with <code>%hex</code> where hex is a two digit hex number.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/02/14 14:19:06 $
 */
public class Encoder { 

  /**
   * Encodes a textstring that is compatible with the JavaScript escape function
   * @param Source The textstring to be encoded.
   * @return The JavaScript escaped string.
   */
  public static String escape(String source) {
	  StringBuffer ret=new StringBuffer();
      // URLEncode the text string. This produces a very similar encoding to JavaSscript
      // encoding, except the blak whic is not encoded into a %20.
	  String enc=URLEncoder.encode(source);
	  StringTokenizer t=new StringTokenizer(enc,"+");
      while (t.hasMoreTokens()) {
            ret.append(t.nextToken());
            ret.append("%20");
      }
	  return ret.toString();
  }

   /**
   * Decodes a textstring that is compatible with the JavaScript unescape function.
   * @param Source The textstring to be decoded.
   * @return The JavaScript unescaped string.
   */
 public static String unescape(String source) {
	StringBuffer unescaped=new StringBuffer();
	String token="";
	String hex="";
	int code;
	Byte bytecode;
	byte bytestorage[]=new byte[1];
	String stringcode;
	
    // the flag signals if the character conversion should be skipped.
	boolean flag=false;
    // check if the escaped string starts with an escaped character
    if (!source.startsWith("%")){
		flag=true;
	}
	
    // convert all %hex in the texttring
	StringTokenizer t=new StringTokenizer(source,"%");

    while (t.hasMoreTokens()){
	    token = t.nextToken();
        // skip conversion if token is only one character
        if (token.length()<2) {
            flag=true;
        }
        if (!flag) {
            // get the real character from the hex code and append it to the already converted
            // result
            hex= token.substring(0,2);
		    token=token.substring(2);
			try {
			    code=Integer.parseInt(hex,16);
			}catch(Exception e) {
			    code=32;
				token=" ";
			}			
			bytecode = new Byte(new Integer(code).byteValue());	       
			bytestorage[0]=bytecode.byteValue();
			stringcode=new String(bytestorage);	
			unescaped.append(stringcode);
            unescaped.append(token);
        } else {
            // only add the token to the result, do not convert it
		    flag=false;
			unescaped.append(token);
		}
			
	}
	return unescaped.toString();
  }
}
