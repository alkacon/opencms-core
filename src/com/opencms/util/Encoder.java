/*
 * File   : Encoder.java
 * Author : Michael Emmerich
 *           
 * Version 0.1
 *
 * Date   : 30.09.1999 Created
 *
 * This class is the core of the MhtCms System. From here, all other operations are invoked
 *
 * Copyright (c) 1999 MindFact interaktive Medien AG.   All Rights Reserved.
 *
 * THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 * To use this software you must purchease a licencse from BKM Online.
 * In order to use this source code, you need written permission from BKM Online.
 * Redistribution of this source code, in modified or unmodified form,
 * is not allowed.
 *
 * BKM ONLINE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. BKM ONLINE SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.opencms.util;

import java.io.*;
import java.util.*;
import java.net.*;


import javax.servlet.*;
import javax.servlet.http.*;


/**
 * The Encoder provies static methods to decode and encode data.
 */
public class Encoder 

{
  /**
   * Constructor
   */
  public void Encoder()
  {
  }

  /**
   * Encodes a textstring that is compatible with the JavaScript escape function
   * @param Source The textstring to be encoded.
   * @return The JavaScript escaped string.
   */
  public static String escape(String source)
  {

	  String ret="";
	  String enc=URLEncoder.encode(source);
	  StringTokenizer t=new StringTokenizer(enc,"+");
		while (t.hasMoreTokens())
		{
			ret=ret+t.nextToken()+"%20";
		}
	
		return ret;
  }

   /**
   * Decodes a textstring that is compatible with the JavaScript unescape function.
   * @param Source The textstring to be decoded.
   * @return The JavaScript unescaped string.
   */
  public static String unescape(String source)
  {
	String unescaped="";
	String token="";
	String hex="";
	int code;
	Byte bytecode;
	byte bytestorage[]=new byte[1];
	String stringcode;
	
	boolean flag=false;
	if (!source.startsWith("%"))
	{
		flag=true;
	}
	
	StringTokenizer t=new StringTokenizer(source,"%");

	

	while (t.hasMoreTokens())
	{
		token = t.nextToken();
		hex= token.substring(0,2);
		if (!flag)
		{
			token=token.substring(2);
			code=Integer.parseInt(hex,16);
			bytecode = new Byte(new Integer(code).byteValue());	       
			bytestorage[0]=bytecode.byteValue();
			stringcode=new String(bytestorage);	
			unescaped=unescaped+stringcode+token;
		}
		else
		{
			flag=false;
			unescaped=unescaped+token;
		}
			
	}

	return unescaped;
  }
}
