/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Encoder.java,v $
* Date   : $Date: 2002/09/04 15:41:30 $
* Version: $Revision: 1.18 $
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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;

/**
 * The OpenCms Encoder class provides static methods to decode and encode data.<p>
 * 
 * The methods in this class are substitutes for <code>java.net.URLEncoder.encode()</code> and
 * <code>java.net.URLDecoder.decode()</code>. Use the methods from this class in all OpenCms 
 * core classes to ensure the encoding is always handled the same way.<p>
 * 
 * The de- and encoding uses the same coding mechanism as JavaScript, special characters are
 * replaxed with <code>%hex</code> where hex is a two digit hex number.<p>
 * 
 * <b>Note:</b> On the client side (browser) instead of using corresponding <code>escape</code>
 * and <code>unescape</code> JavaScript functions, better use <code>encodeURIComponent</code> and
 * <code>decodeURIComponent</code> functions wich are work properly with unicode characters.
 * These functions are supported in IE 5.5+ and NS 6+ only.
 *
 * @author Michael Emmerich
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Unknown guys from Gridnine AB
 */
public class Encoder {

    /**
     * Constructor
     */
    public Encoder() {}

    /**
     * This method is a substitute for <code>URLEncoder.encode()</code>.
     * Use this in all OpenCms core classes to ensure the encoding is
     * always handled the same way.<p>
     * 
     * In case you don't know what encoding to use, set the value of 
     * the <code>encoding</code> parameter to <code>null</code>. 
     * This will use the default encoding, which is propably the right one.<p>
     * 
     * It also solves a backward compatiblity issue between Java 1.3 and 1.4,
     * since 1.3 does not support an explicit encoding parameter and always uses
     * the default system encoding.<p>
     * 
     * @param source The string to encode
     * @param encoding The encoding to use (if null, the system default is used)
     * @return The encoded source String
     */
    public static String encode(String source, String encoding) {
        if (encoding != null) {
            try {
                String encodedSource = URLEncoder.encode(source, encoding); 
                return encodedSource;
            } 
            catch (java.io.UnsupportedEncodingException e) {}
            catch (java.lang.NoSuchMethodError n) {}
        }
        // Use default encoding
        // This is also important for Java 1.3 compatibility
        return URLEncoder.encode(source);
    }

    /**
     * This method is a substitute for <code>URLDecoder.decode()</code>.
     * Use this in all OpenCms core classes to ensure the encoding is
     * always handled the same way.<p>
     * 
     * In case you don't know what encoding to use, set the value of 
     * the <code>encoding</code> parameter to <code>null</code>. 
     * This will use the default encoding, which is propably the right one.<p>
     * 
     * It also solves a backward compatiblity issue between Java 1.3 and 1.4,
     * since 1.3 does not support an explicit encoding parameter and always uses
     * the default system encoding.<p>
     * 
     * @param source The string to decode
     * @param encoding The encoding to use (if null, the system default is used)
     * @return The decoded source String
     */
    public static String decode(String source, String encoding) {
        if (encoding != null) {
            try {
                return URLDecoder.decode(source, encoding);                
            }
            catch (java.io.UnsupportedEncodingException e) {}
            catch (java.lang.NoSuchMethodError n) {}            
        }
        // Use default encoding
        // This is also important for Java 1.3 compatibility
        return URLDecoder.decode(source);        
    }

    /**
     * Encodes a String in a way that is compatible with the JavaScript escape function.
     * 
     * @param Source The textstring to be encoded.
     * @return The JavaScript escaped string.
     */
    // Gridnine AB Aug 8, 2002
    // added support for different encodings
    public static String escape(String source, String encoding) {
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript
        // encoding, except the blank which is not encoded into a %20.
        String enc = encode(source, encoding);
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
     * Encodes a String in a way that is compatible with the JavaScript escape function.
     * Muliple blanks are encoded _multiply _with %20.
     * 
     * @param Source The textstring to be encoded.
     * @return The JavaScript escaped string.
     */
    // Gridnine AB Aug 8, 2002
    // added support for different encodings
    public static String escapeWBlanks(String source, String encoding) {
        if(source == null) {
            return null;
        }
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript
        // encoding, except the blank which is not encoded into a %20.
        String enc = encode(source, encoding);
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
     * Escapes a String so it may be printed as text content or attribute
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
                result.append(charRef);
            }
        }
        return result.toString();
    }

    /**
     * Encodes special XML characters into the equivalent character references.
     *
     * @param ch The character to encode
     * @return The encoded character as a String
     */
    private static String getEntityRef(char ch) {
        // These four entities have to be escaped by default.
        switch(ch) {
        case '<':
            return "&lt;";

        case '>':
            return "&gt;";

        case '&':
            return "&amp;";

        case '"':
            return "&quot;";
        }
        return null;
    }

    /**
     * Decodes a String in a way that is compatible with the JavaScript 
     * unescape function.
     * 
     * @param Source The String to be decoded.
     * @return The JavaScript unescaped String.
     */
    // Gridnine AB Aug 8, 2002
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
        return decode(preparedSource.toString(), encoding);
    }
}
