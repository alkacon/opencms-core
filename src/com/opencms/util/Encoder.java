/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Encoder.java,v $
* Date   : $Date: 2002/09/02 07:29:09 $
* Version: $Revision: 1.15 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


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
 */

public class Encoder {

    /**
     * Constructor
     */

    public Encoder() {

    }

    /**
     * Encodes a textstring that is compatible with the JavaScript escape function
     * @param Source The textstring to be encoded.
     * @return The JavaScript escaped string.
     */

    public static String escape(String source) {
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript

        // encoding, except the blank which is not encoded into a %20.
        String enc = encode(source);
        StringTokenizer t = new StringTokenizer(enc, "+");
        while(t.hasMoreTokens()) {
            ret.append(t.nextToken());
            if(t.hasMoreTokens()) {
                ret.append("%20");
            }
        }
        return ret.toString();
    }
    
    /**
     * Method to call the (deprecated) encode function in 
     * the java.net.URLEncoder class, which will 
     * yield an annoying deprecation warning during compilation.
     * 
     * @param source The String to encode
     * @return The encoded String
     */
    public static String encode(String source) {
        // To prevent the deprecation warning, UTF-8 could be used here as default.
        // TODO: Must check if this is backwards compatible with Java 1.3
        return URLEncoder.encode(source);
    }

    /**
     * Encodes a textstring that is compatible with the JavaScript escape function.
     * Muliple blanks are encoded _multiply _with %20
     * @param Source The textstring to be encoded.
     * @return The JavaScript escaped string.
     */

    public static String escapeWBlanks(String source) {
        if(source == null) {
            return null;
        }
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript
        // encoding, except the blank which is not encoded into a %20.
        String enc = encode(source);
        for(int z = 0;z < enc.length();z++) {
            if(enc.charAt(z) == '+') {
                ret.append("%20");
            }
            else {
                ret.append(enc.charAt(z));
            }
        }
        return ret.toString();
    }

    /**
     * Escapes a string so it may be printed as text content or attribute
     * value. Non printable characters are escaped using character references.
     * Where the format specifies a deault entity reference, that reference
     * is used (e.g. <tt>&amp;lt;</tt>).
     *
     * @param source The string to escape
     * @return The escaped string
     */

    public static String escapeXml(String source) {
        StringBuffer result;
        int i;
        char ch;
        String charRef;
        result = new StringBuffer(source.length());
        for(i = 0;i < source.length();++i) {
            ch = source.charAt(i);
            charRef = getEntityRef(ch);
            if(charRef == null) {
                result.append(ch);
            }
            else {
                result.append('&').append(charRef).append(';');
            }
        }
        return result.toString();
    }

    /**
     * Encodes special XML characters into the equivalent character references.
     *
     * @param ch The character to encode
     * @return The encoded character as string
     */

    protected static String getEntityRef(char ch) {

        // These four entities have to be escaped by default.
        switch(ch) {
        case '<':
            return "lt";

        case '>':
            return "gt";

        case '&':
            return "amp";

        case '"':
            return "quot";
        }
        return null;
    }

    /**
     * Decodes a textstring that is compatible with the JavaScript unescape function.
     * @param Source The textstring to be decoded.
     * @return The JavaScript unescaped string.
     */

    public static String unescape(String source) {
        if(source == null){
            return null;
        }
        StringBuffer unescaped = new StringBuffer();
        String token = "";
        String hex = "";
        int code;
        Byte bytecode;
        byte bytestorage[] = new byte[1];
        String stringcode;

        // the flag signals if the character conversion should be skipped.
        boolean flag = false;

        // check if the escaped string starts with an escaped character
        if(!source.startsWith("%")) {
            flag = true;
        }

        // convert all %hex in the texttring
        StringTokenizer t = new StringTokenizer(source, "%");
        while(t.hasMoreTokens()) {
            token = t.nextToken();

            // skip conversion if token is only one character
            if(token.length() < 2) {
                flag = true;
            }
            if(!flag) {

                // get the real character from the hex code and append it to the already converted

                // result
                hex = token.substring(0, 2);
                token = token.substring(2);
                try {
                    code = Integer.parseInt(hex, 16);
                }
                catch(Exception e) {
                    code = 32;
                    token = " ";
                }
                bytecode = new Byte(new Integer(code).byteValue());
                bytestorage[0] = bytecode.byteValue();
                stringcode = new String(bytestorage);
                unescaped.append(stringcode);
                unescaped.append(token);
            }
            else {

                // only add the token to the result, do not convert it
                flag = false;
                unescaped.append(token);
            }
        }
        return unescaped.toString();
    }
}
