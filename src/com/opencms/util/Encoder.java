/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Encoder.java,v $
* Date   : $Date: 2002/09/03 11:57:06 $
* Version: $Revision: 1.16 $
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
 * <br><br>
 * On client side (browser) instead of using corresponding <code>escape</code>
 * and <code>unescape</code> JavaScript functions, use <code>encodeURIComponent</code> and
 * <code>decodeURIComponent</code> functions wich are proper work with unicode characters.
 * These functions are supported in IE 5.5+ and NS 6+
 *
 * @author Michael Emmerich
 */
//Gridnine AB Aug 9, 2002
// see JavaDoc comments above
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
    //Gridnine AB Aug 8, 2002
    // added support for different encodings
    public static String escape(String source, String encoding) {
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript

        // encoding, except the blank which is not encoded into a %20.
        String enc;
        try {
            enc = URLEncoder.encode(source, encoding);
        } catch (UnsupportedEncodingException uee) {
            enc = URLEncoder.encode(source);
        }
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
     * Encodes a textstring that is compatible with the JavaScript escape function.
     * Muliple blanks are encoded _multiply _with %20
     * @param Source The textstring to be encoded.
     * @return The JavaScript escaped string.
     */
    //Gridnine AB Aug 8, 2002
    // added support for different encodings
    public static String escapeWBlanks(String source, String encoding) {
        if(source == null) {
            return null;
        }
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript
        // encoding, except the blank which is not encoded into a %20.
        String enc;
        try {
            enc = URLEncoder.encode(source, encoding);
        } catch (UnsupportedEncodingException e) {
            enc = URLEncoder.encode(source);
        }
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
    //Gridnine AB Aug 8, 2002
    // changed to support different encodings
    public static String unescape(String source, String encoding) {
        if(source == null){
            return null;
        }
        int len = source.length();
        // to use standard decoder we need to replace '+' with "%20" (space)
        StringBuffer preparedSource = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            char c = source.charAt(i);
            if (c == '+') {
                preparedSource.append("%20");
            } else {
                preparedSource.append(c);
            }
        }
        try {
            return URLDecoder.decode(preparedSource.toString(), encoding);
        } catch (UnsupportedEncodingException e) {
            return URLDecoder.decode(preparedSource.toString());
        }
        /*
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
                if ((token.startsWith("u") || token.startsWith("U"))
                    && (token.length() > 2)) {
                    // skip leading Unicode mark
                    hex = token.substring(1, 5);
                    token = token.substring(5);
                } else {
                    hex = token.substring(0, 2);
                    token = token.substring(2);
                }
                try {
                    code = Integer.parseInt(hex, 16);
                }
                catch(Exception e) {
                    code = 32;
                    token = " ";
                }
                bytecode = new Byte(new Integer(code).byteValue());
                bytestorage[0] = bytecode.byteValue();
                try {
                    stringcode = new String(bytestorage, encoding);
                } catch (UnsupportedEncodingException e) {
                    stringcode = new String(bytestorage);
                }
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
        */
    }
}
