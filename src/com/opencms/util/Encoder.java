/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/Encoder.java,v $
* Date   : $Date: 2003/11/03 09:05:52 $
* Version: $Revision: 1.31 $
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

import org.opencms.main.OpenCms;

import java.io.UnsupportedEncodingException;
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
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 */
public final class Encoder {
    
    /** Default encoding for JavaScript decodeUriComponent methods is UTF-8 by w3c standard */
    public static final String C_UTF8_ENCODING = "UTF-8";

    /**
     * Constructor
     */
    private Encoder() {
        // empty
    }
    
    /**
     * This method is a substitute for <code>URLEncoder.encode()</code>.
     * Use this in all OpenCms core classes to ensure the encoding is
     * always handled the same way.<p>
     * 
     * In case you don't know what encoding to use, set the value of 
     * the <code>encoding</code> parameter to <code>null</code>. 
     * This method will then default to UTF-8 encoding, which is propably the right one.<p>
     * 
     * @param source the String to encode
     * @param encoding the encoding to use (if null, the system default is used)
     * @return the encoded source String
     */
    public static String encode(String source, String encoding) {
        if (source == null) return null;
        if (encoding != null) {
            try {
                return URLEncoder.encode(source, encoding); 
            } catch (java.io.UnsupportedEncodingException e) {
                // will fallback to default
            } 
        }
        // fallback to default encoding
        try {
            return URLEncoder.encode(source, C_UTF8_ENCODING); 
        } catch (java.io.UnsupportedEncodingException e) {
            // ignore
        }
        return source;
    }
    
    /**
     * Encodes a String using UTF-8 encoding, which is the standard for http data transmission
     * with GET ant POST requests.<p>
     * 
     * @param source the String to encode
     * @return String the encoded source String
     */
    public static String encode(String source) {
        return encode(source, C_UTF8_ENCODING);
    }

    /**
     * This method is a substitute for <code>URLDecoder.decode()</code>.
     * Use this in all OpenCms core classes to ensure the encoding is
     * always handled the same way.<p>
     * 
     * In case you don't know what encoding to use, set the value of 
     * the <code>encoding</code> parameter to <code>null</code>. 
     * This method will then default to UTF-8 encoding, which is propably the right one.<p>
     * 
     * @param source The string to decode
     * @param encoding The encoding to use (if null, the system default is used)
     * @return The decoded source String
     */
    public static String decode(String source, String encoding) {
        if (source == null) return null;
        if (encoding != null) {
            try {
                return URLDecoder.decode(source, encoding); 
            } catch (java.io.UnsupportedEncodingException e) {
                // will fallback to default
            } 
        }
        // fallback to default decoding
        try {
            return URLDecoder.decode(source, C_UTF8_ENCODING); 
        } catch (java.io.UnsupportedEncodingException e) {
            // ignore
        }
        return source;     
    }
    
    /**
     * Decodes a String using UTF-8 encoding, which is the standard for http data transmission
     * with GET ant POST requests.<p>
     * 
     * @param source the String to decode
     * @return String the decoded source String
     */
    public static String decode(String source) {
        return decode(source, C_UTF8_ENCODING);
    }    

    /**
     * Encodes a String in a way that is compatible with the JavaScript escape function.
     * 
     * @param source The textstring to be encoded.
     * @param encoding the encoding type
     * @return The JavaScript escaped string.
     */
    public static String escape(String source, String encoding) {
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript
        // encoding, except the blank which is not encoded into a %20.
        String enc = encode(source, encoding);
        StringTokenizer t = new StringTokenizer(enc, "+");
        while (t.hasMoreTokens()) {
            ret.append(t.nextToken());
            if (t.hasMoreTokens()) {
                ret.append("%20");
            }
        }
        return ret.toString();
    }

    /**
     * Encodes a String in a way that is compatible with the JavaScript escape function.
     * Muliple blanks are encoded _multiply _with %20.
     * 
     * @param source The textstring to be encoded.
     * @param encoding the encoding type
     * @return The JavaScript escaped string.
     */
    public static String escapeWBlanks(String source, String encoding) {
        if (source == null) {
            return null;
        }
        StringBuffer ret = new StringBuffer();

        // URLEncode the text string. This produces a very similar encoding to JavaSscript
        // encoding, except the blank which is not encoded into a %20.
        String enc = encode(source, encoding);
        for (int z = 0; z < enc.length(); z++) {
            if (enc.charAt(z) == '+') {
                ret.append("%20");
            } else {
                ret.append(enc.charAt(z));
            }
        }
        return ret.toString();
    }

    /**
     * Escapes a String so it may be printed as text content or attribute
     * value in a HTML page or an XML file.<p>
     * 
     * This method replaces the following characters in a String:
     * <ul>
     * <li><b>&lt;</b> with &amp;lt;
     * <li><b>&gt;</b> with &amp;gt;
     * <li><b>&amp;</b> with &amp;amp;
     * <li><b>&quot;</b> with &amp;quot;
     * </ul>
     * 
     * @param source the string to escape
     * @return the escaped string
     * 
     * @see #escapeHtml(String)
     */
    public static String escapeXml(String source) {
        if (source == null) return null;
        StringBuffer result = new StringBuffer(source.length()*2);
        int terminatorIndex;
        for (int i = 0; i < source.length(); ++i) {
            char ch = source.charAt(i);
            switch (ch) {
                case '<' :
                    result.append("&lt;");
                    break;
                case '>' :
                    result.append("&gt;");
                    break;
                case '&' :
                    // Don't escape already escaped international and special characters
                    if ((terminatorIndex = source.indexOf(";", i)) > 0)
                        if (source.substring(i + 1, terminatorIndex).matches("#[0-9]+"))
                            result.append(ch);
                        else 
                            result.append("&amp;");
                    else
                        result.append("&amp;");
                    break;
                case '"' :
                    result.append("&quot;");
                    break;
                default :
                    result.append(ch);
            }
        }
        return new String(result);
    }   

    /**
     * Escapes special characters in a HTML-String with their number-based 
     * entity representation, for example &amp; becomes &amp;#38;.<p>
     * 
     * A character <code>num</code> is replaced if<br>
     * <code>((ch !=  32) && ((ch > 122) || (ch < 48) || (ch == 60) || (ch == 62)))</code><p>
     * 
     * @param source the String to escape
     * @return String the escaped String
     * 
     * @see #escapeXml(String)
     */
    public static String escapeHtml(String source) {
        int terminatorIndex;
        if (source == null) return null;
        StringBuffer result = new StringBuffer(source.length()*2);
        for (int i = 0; i < source.length(); i++) {
            int ch = source.charAt(i);
            // Avoid escaping already escaped characters;
            if ((ch == 38) && ((terminatorIndex = source.indexOf(";", i)) > 0)) {
                if (source.substring(i + 1, terminatorIndex).matches("#[0-9]+|lt|gt|amp|quote")) {
                    result.append(source.substring(i, terminatorIndex + 1));
                    // Skip remaining chars up to (and including) ";"
                    i = terminatorIndex;
                    continue;
                }
            }
            if ((ch !=  32) && ((ch > 122) || (ch < 48) || (ch == 60) || (ch == 62))) {
                result.append("&#");
                result.append(ch);
                result.append(";");
            } else {
                result.append((char)ch);
            }
        }
        return new String(result);
    }

    /**
     * Escapes non ASCII characters in a HTML-String with their number-based 
     * entity representation, for example &amp; becomes &amp;#38;.<p>
     * 
     * A character <code>num</code> is replaced if<br>
     * <code>(ch > 255)</code><p>
     * 
     * @param source the String to escape
     * @return String the escaped String
     * 
     * @see #escapeXml(String)
     */
    public static String escapeNonAscii(String source) {
        if (source == null) return null;
        StringBuffer result = new StringBuffer(source.length()*2);
        for (int i = 0; i < source.length(); i++) {
            int ch = source.charAt(i);
            if (ch > 255) {
                result.append("&#");
                result.append(ch);
                result.append(";");
            } else {
                result.append((char)ch);
            }
        }
        return new String(result);
    }
    
    /**
     * Decodes a String in a way that is compatible with the JavaScript 
     * unescape function.
     * 
     * @param source The String to be decoded.
     * @param encoding the encoding type
     * @return The JavaScript unescaped String.
     */
    public static String unescape(String source, String encoding) {
        if (source == null) {
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
    
    /**
     * Changes the encoding of a byte array that represents a String.<p>
     * 
     * @param input the byte array to convert
     * @param oldEncoding the current encoding of the byte array
     * @param newEncoding the new encoding of the byte array
     * @return byte[] the byte array encoded in the new encoding
     */
    public static byte[] changeEncoding(byte[] input, String oldEncoding, String newEncoding) {
        if ((oldEncoding == null) || (newEncoding == null)) return input;
        if (oldEncoding.trim().equalsIgnoreCase(newEncoding.trim())) return input;
        byte[] result = input;
        try {
            result = (new String(input, oldEncoding)).getBytes(newEncoding);
        } catch (UnsupportedEncodingException e) {
            // return value will be input value
        }
        return result;
    }
    
    /**
     * Re-decodes a String that has not been correctly decoded and thus has scrambled
     * character bytes.<p>
     * 
     * This is an equivalent to the JavaScript "decodeURIComponent" function.
     * It converts from the default "UTF-8" to the currently selected system encoding.<p>
     * 
     * @param input the String to convert
     * @return String the converted String
     */
    public static String redecodeUriComponent(String input) {
       if (input == null) return input;
       return new String(changeEncoding(input.getBytes(), C_UTF8_ENCODING, OpenCms.getDefaultEncoding())); 
    }
}
