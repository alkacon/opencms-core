/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.i18n;

import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * The OpenCms CmsEncoder class provides static methods to decode and encode data.<p>
 *
 * The methods in this class are substitutes for <code>java.net.URLEncoder.encode()</code> and
 * <code>java.net.URLDecoder.decode()</code>. Use the methods from this class in all OpenCms
 * core classes to ensure the encoding is always handled the same way.<p>
 *
 * The de- and encoding uses the same coding mechanism as JavaScript, special characters are
 * replaced with <code>%hex</code> where hex is a two digit hex number.<p>
 *
 * <b>Note:</b> On the client side (browser) instead of using the deprecated <code>escape</code>
 * and <code>unescape</code> JavaScript functions, always the use <code>encodeURIComponent</code> and
 * <code>decodeURIComponent</code> functions. Only these work properly with unicode characters.<p>
 *
 * @since 6.0.0
 */
public final class CmsEncoder {

    /** Non-alphanumeric characters used for Base64 encoding. */
    public static final String BASE64_EXTRA = "+/=";

    /** Characters used as replacements for non-alphanumeric Base64 characters when using Base64 for request parameters. */
    public static final String BASE64_EXTRA_REPLACEMENTS = "-_.";

    /** Constant for the standard <code>ISO-8859-1</code> encoding. */
    public static final String ENCODING_ISO_8859_1 = "ISO-8859-1";

    /** Constant for the standard <code>US-ASCII</code> encoding. */
    public static final String ENCODING_US_ASCII = "US-ASCII";

    /**
     * Constant for the standard <code>UTF-8</code> encoding.<p>
     *
     * Default encoding for JavaScript decodeUriComponent methods is <code>UTF-8</code> by w3c standard.
     */
    public static final String ENCODING_UTF_8 = "UTF-8";

    /** The regex pattern to match HTML entities. */
    private static final Pattern ENTITIY_PATTERN = Pattern.compile("\\&#\\d+;");

    /** The prefix for HTML entities. */
    private static final String ENTITY_PREFIX = "&#";

    /** The replacement for HTML entity prefix in parameters. */
    private static final String ENTITY_REPLACEMENT = "$$";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEncoder.class);

    /** A cache for encoding name lookup. */
    private static Map<String, String> m_encodingCache = new HashMap<String, String>(16);

    private static Random m_random = new Random();

    /** The plus entity. */
    private static final String PLUS_ENTITY = ENTITY_PREFIX + "043;";

    /**
     * Constructor.<p>
     */
    private CmsEncoder() {

        // empty
    }

    /**
     * Adjusts the given String by making sure all characters that can be displayed
     * in the given charset are contained as chars, whereas all other non-displayable
     * characters are converted to HTML entities.<p>
     *
     * Just calls {@link #decodeHtmlEntities(String, String)} first and feeds the result
     * to {@link #encodeHtmlEntities(String, String)}. <p>
     *
     * @param input the input to adjust the HTML encoding for
     * @param encoding the charset to encode the result with\
     *
     * @return the input with the decoded/encoded HTML entities
     */
    public static String adjustHtmlEncoding(String input, String encoding) {

        return encodeHtmlEntities(decodeHtmlEntities(input, encoding), encoding);
    }

    /**
     * Changes the encoding of a byte array that represents a String.<p>
     *
     * @param input the byte array to convert
     * @param oldEncoding the current encoding of the byte array
     * @param newEncoding the new encoding of the byte array
     *
     * @return the byte array encoded in the new encoding
     */
    public static byte[] changeEncoding(byte[] input, String oldEncoding, String newEncoding) {

        if ((oldEncoding == null) || (newEncoding == null)) {
            return input;
        }
        if (oldEncoding.trim().equalsIgnoreCase(newEncoding.trim())) {
            return input;
        }
        byte[] result = input;
        try {
            result = (new String(input, oldEncoding)).getBytes(newEncoding);
        } catch (UnsupportedEncodingException e) {
            // return value will be input value
        }
        return result;
    }

    /**
     * Converts the host of an URI to Punycode.<p>
     *
     * This is needed when we want to do redirects to hosts with host names containing international characters like umlauts.<p>
     *
     * @param uriString the URI
     * @return the converted URI
     */
    public static String convertHostToPunycode(String uriString) {

        if (uriString.indexOf(":") >= 0) {
            try {
                URI uri = new URI(uriString);
                String authority = uri.getAuthority(); // getHost won't work when we have special characters
                int colonPos = authority.indexOf(':');
                if (colonPos >= 0) {
                    authority = IDN.toASCII(authority.substring(0, colonPos)) + authority.substring(colonPos);
                } else {
                    authority = IDN.toASCII(authority);
                }
                URI uriWithCorrectedHost = new URI(
                    uri.getScheme(),
                    authority,
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment());
                uriString = uriWithCorrectedHost.toASCIIString();
            } catch (URISyntaxException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return uriString;
    }

    /**
     * Creates a String out of a byte array with the specified encoding, falling back
     * to the system default in case the encoding name is not valid.<p>
     *
     * Use this method as a replacement for <code>new String(byte[], encoding)</code>
     * to avoid possible encoding problems.<p>
     *
     * @param bytes the bytes to decode
     * @param encoding the encoding scheme to use for decoding the bytes
     *
     * @return the bytes decoded to a String
     */
    public static String createString(byte[] bytes, String encoding) {

        String enc = encoding.intern();
        if (enc != OpenCms.getSystemInfo().getDefaultEncoding()) {
            enc = lookupEncoding(enc, null);
        }
        if (enc != null) {
            try {
                return new String(bytes, enc);
            } catch (UnsupportedEncodingException e) {
                // this can _never_ happen since the charset was looked up first
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.ERR_UNSUPPORTED_VM_ENCODING_1, encoding));
            }
            enc = OpenCms.getSystemInfo().getDefaultEncoding();
            try {
                return new String(bytes, enc);
            } catch (UnsupportedEncodingException e) {
                // this can also _never_ happen since the default encoding is always valid
            }
        }
        // this code is unreachable in practice
        LOG.error(Messages.get().getBundle().key(Messages.ERR_ENCODING_ISSUES_1, encoding));
        return null;
    }

    /**
     * Decodes a String using UTF-8 encoding, which is the standard for http data transmission
     * with GET ant POST requests.<p>
     *
     * @param source the String to decode
     *
     * @return String the decoded source String
     */
    public static String decode(String source) {

        return decode(source, ENCODING_UTF_8);
    }

    /**
     * This method is a substitute for <code>URLDecoder.decode()</code>.
     * Use this in all OpenCms core classes to ensure the encoding is
     * always handled the same way.<p>
     *
     * In case you don't know what encoding to use, set the value of
     * the <code>encoding</code> parameter to <code>null</code>.
     * This method will then default to UTF-8 encoding, which is probably the right one.<p>
     *
     * @param source The string to decode
     * @param encoding The encoding to use (if null, the system default is used)
     *
     * @return The decoded source String
     */
    public static String decode(String source, String encoding) {

        if (source == null) {
            return null;
        }
        if (encoding != null) {
            try {
                return URLDecoder.decode(source, encoding);
            } catch (java.io.UnsupportedEncodingException e) {
                // will fallback to default
            }
        }
        // fallback to default decoding
        try {
            return URLDecoder.decode(source, ENCODING_UTF_8);
        } catch (java.io.UnsupportedEncodingException e) {
            // ignore
        }
        return source;
    }

    /**
     * Decodes HTML entity references like <code>&amp;#8364;</code> that are contained in the
     * String to a regular character, but only if that character is contained in the given
     * encodings charset.<p>
     *
     * @param input the input to decode the HTML entities in
     * @param encoding the charset to decode the input for
     * @return the input with the decoded HTML entities
     *
     * @see #encodeHtmlEntities(String, String)
     */
    public static String decodeHtmlEntities(String input, String encoding) {

        Matcher matcher = ENTITIY_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer(input.length());
        Charset charset = Charset.forName(encoding);
        CharsetEncoder encoder = charset.newEncoder();

        while (matcher.find()) {
            String entity = matcher.group();
            String value = entity.substring(2, entity.length() - 1);
            int c = Integer.valueOf(value).intValue();
            if (c < 128) {
                // first 128 chars are contained in almost every charset
                entity = new String(new char[] {(char)c});
                // this is intended as performance improvement since
                // the canEncode() operation appears quite CPU heavy
            } else if (encoder.canEncode((char)c)) {
                // encoder can encode this char
                entity = new String(new char[] {(char)c});
            }
            matcher.appendReplacement(result, entity);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Decodes a string used as parameter in an uri in a way independent of other encodings/decodings applied before.<p>
     *
     * @param input the encoded parameter string
     *
     * @return the decoded parameter string
     *
     * @see #encodeParameter(String)
     */
    public static String decodeParameter(String input) {

        String result = CmsStringUtil.substitute(input, ENTITY_REPLACEMENT, ENTITY_PREFIX);
        return CmsEncoder.decodeHtmlEntities(result, OpenCms.getSystemInfo().getDefaultEncoding());
    }

    /**
     * Decodes a parameter which has been encoded from a string list using encodeStringsAsBase64Parameter.<p>
     *
     * @param data the data to decode
     * @return the list of strings
     */
    public static List<String> decodeStringsFromBase64Parameter(String data) {

        data = StringUtils.replaceChars(data, BASE64_EXTRA_REPLACEMENTS, BASE64_EXTRA);
        byte[] bytes = deobfuscateBytes(Base64.decodeBase64(data));
        try {
            JSONArray json = new JSONArray(new String(bytes, "UTF-8"));
            List<String> result = Lists.newArrayList();
            for (int i = 0; i < json.length(); i++) {
                result.add(json.getString(i));
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            throw new IllegalArgumentException("Decoding failed: " + data, e);
        }
        return null;
    }

    /**
     * Encodes a String using UTF-8 encoding, which is the standard for http data transmission
     * with GET ant POST requests.<p>
     *
     * @param source the String to encode
     *
     * @return String the encoded source String
     */
    public static String encode(String source) {

        return encode(source, ENCODING_UTF_8);
    }

    /**
     * This method is a substitute for <code>URLEncoder.encode()</code>.
     * Use this in all OpenCms core classes to ensure the encoding is
     * always handled the same way.<p>
     *
     * In case you don't know what encoding to use, set the value of
     * the <code>encoding</code> parameter to <code>null</code>.
     * This method will then default to UTF-8 encoding, which is probably the right one.<p>
     *
     * @param source the String to encode
     * @param encoding the encoding to use (if null, the system default is used)
     *
     * @return the encoded source String
     */
    public static String encode(String source, String encoding) {

        if (source == null) {
            return null;
        }
        if (encoding != null) {
            try {
                return URLEncoder.encode(source, encoding);
            } catch (java.io.UnsupportedEncodingException e) {
                // will fallback to default
            }
        }
        // fallback to default encoding
        try {
            return URLEncoder.encode(source, ENCODING_UTF_8);
        } catch (java.io.UnsupportedEncodingException e) {
            // ignore
        }
        return source;
    }

    /**
     * Encodes all characters that are contained in the String which can not displayed
     * in the given encodings charset with HTML entity references
     * like <code>&amp;#8364;</code>.<p>
     *
     * This is required since a Java String is
     * internally always stored as Unicode, meaning it can contain almost every character, but
     * the HTML charset used might not support all such characters.<p>
     *
     * @param input the input to encode for HTML
     * @param encoding the charset to encode the result with
     *
     * @return the input with the encoded HTML entities
     *
     * @see #decodeHtmlEntities(String, String)
     */
    public static String encodeHtmlEntities(String input, String encoding) {

        StringBuffer result = new StringBuffer(input.length() * 2);
        CharBuffer buffer = CharBuffer.wrap(input.toCharArray());
        Charset charset = Charset.forName(encoding);
        CharsetEncoder encoder = charset.newEncoder();
        for (int i = 0; i < buffer.length(); i++) {
            int c = buffer.get(i);
            if (c < 128) {
                // first 128 chars are contained in almost every charset
                result.append((char)c);
                // this is intended as performance improvement since
                // the canEncode() operation appears quite CPU heavy
            } else if (encoder.canEncode((char)c)) {
                // encoder can encode this char
                result.append((char)c);
            } else {
                // append HTML entity reference
                result.append(ENTITY_PREFIX);
                result.append(c);
                result.append(";");
            }
        }
        return result.toString();
    }

    /**
     * Encodes all characters that are contained in the String which can not displayed
     * in the given encodings charset with Java escaping like <code>\u20ac</code>.<p>
     *
     * This can be used to escape values used in Java property files.<p>
     *
     * @param input the input to encode for Java
     * @param encoding the charset to encode the result with
     *
     * @return the input with the encoded Java entities
     */
    public static String encodeJavaEntities(String input, String encoding) {

        StringBuffer result = new StringBuffer(input.length() * 2);
        CharBuffer buffer = CharBuffer.wrap(input.toCharArray());
        Charset charset = Charset.forName(encoding);
        CharsetEncoder encoder = charset.newEncoder();
        for (int i = 0; i < buffer.length(); i++) {
            int c = buffer.get(i);
            if (c < 128) {
                // first 128 chars are contained in almost every charset
                result.append((char)c);
                // this is intended as performance improvement since
                // the canEncode() operation appears quite CPU heavy
            } else if (encoder.canEncode((char)c)) {
                // encoder can encode this char
                result.append((char)c);
            } else {
                // append Java entity reference
                result.append("\\u");
                String hex = Integer.toHexString(c);
                int pad = 4 - hex.length();
                for (int p = 0; p < pad; p++) {
                    result.append('0');
                }
                result.append(hex);
            }
        }
        return result.toString();
    }

    /**
     * Encodes a string used as parameter in an uri in a way independent of other encodings/decodings applied later.<p>
     *
     * Used to ensure that GET parameters are not wrecked by wrong or incompatible configuration settings.
     * In order to ensure this, the String is first encoded with html entities for any character that cannot encoded
     * in US-ASCII; additionally, the plus sign is also encoded to avoid problems with the white-space replacer.
     * Finally, the entity prefix is replaced with characters not used as delimiters in urls.<p>
     *
     * @param input the parameter string
     *
     * @return the encoded parameter string
     */
    public static String encodeParameter(String input) {

        String result = CmsEncoder.encodeHtmlEntities(input, CmsEncoder.ENCODING_US_ASCII);
        result = CmsStringUtil.substitute(result, "+", PLUS_ENTITY);
        return CmsStringUtil.substitute(result, ENTITY_PREFIX, ENTITY_REPLACEMENT);
    }

    /**
     * Encode a list of strings as base64 data to be used in a request parameter.<p>
     *
     * @param strings the strings to encode
     * @return the resulting base64 data
     */
    public static String encodeStringsAsBase64Parameter(List<String> strings) {

        JSONArray array = new JSONArray();
        for (String string : strings) {
            array.put(string);
        }
        byte[] bytes;
        try {
            // use obfuscateBytes here to to make the output look more random
            bytes = obfuscateBytes(array.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // should never happen
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        String result = Base64.encodeBase64String(bytes);
        result = StringUtils.replaceChars(result, BASE64_EXTRA, BASE64_EXTRA_REPLACEMENTS);
        return result;
    }

    /**
     * Encodes a String in a way similar to the JavaScript "encodeURIcomponent" function,
     * using "UTF-8" for character encoding encoding.<p>
     *
     * JavaScript "decodeURIcomponent" can decode Strings that have been encoded using this method.<p>
     *
     * <b>Directly exposed for JSP EL<b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param source The text to be encoded
     *
     * @return The encoded string
     *
     * @see #escape(String, String)
     */
    public static String escape(String source) {

        return escape(source, ENCODING_UTF_8);
    }

    /**
     * Encodes a String in a way similar to the JavaScript "encodeURIcomponent" function.<p>
     *
     * JavaScript "decodeURIcomponent" can decode Strings that have been encoded using this method,
     * provided "UTF-8" has been used as encoding.<p>
     *
     * <b>Directly exposed for JSP EL<b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param source The text to be encoded
     * @param encoding the encoding type
     *
     * @return The encoded string
     */
    public static String escape(String source, String encoding) {

        // the blank is encoded into "+" not "%20" when using standard encode call
        return CmsStringUtil.substitute(encode(source, encoding), "+", "%20");
    }

    /**
     * Escapes special characters in a HTML-String with their number-based
     * entity representation, for example &amp; becomes &amp;#38;.<p>
     *
     * A character <code>num</code> is replaced if<br>
     * <code>((ch != 32) && ((ch > 122) || (ch < 48) || (ch == 60) || (ch == 62)))</code><p>
     *
     * @param source the String to escape
     *
     * @return String the escaped String
     *
     * @see #escapeXml(String)
     */
    public static String escapeHtml(String source) {

        if (source == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(source.length() * 2);
        for (int i = 0; i < source.length(); i++) {
            int ch = source.charAt(i);
            // avoid escaping already escaped characters
            if (ch == 38) {
                int terminatorIndex = source.indexOf(";", i);
                if (terminatorIndex > 0) {
                    if (source.substring(i + 1, terminatorIndex).matches("#[0-9]+|lt|gt|amp|quote")) {
                        result.append(source.substring(i, terminatorIndex + 1));
                        // Skip remaining chars up to (and including) ";"
                        i = terminatorIndex;
                        continue;
                    }
                }
            }
            if ((ch != 32) && ((ch > 122) || (ch < 48) || (ch == 60) || (ch == 62))) {
                result.append(ENTITY_PREFIX);
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
     *
     * @return String the escaped String
     *
     * @see #escapeXml(String)
     */
    public static String escapeNonAscii(String source) {

        if (source == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(source.length() * 2);
        for (int i = 0; i < source.length(); i++) {
            int ch = source.charAt(i);
            if (ch > 255) {
                result.append(ENTITY_PREFIX);
                result.append(ch);
                result.append(";");
            } else {
                result.append((char)ch);
            }
        }
        return new String(result);
    }

    /**
     * A simple method to avoid injection.<p>
     *
     * Replaces all single quotes to double single quotes in the value parameter of the SQL statement.<p>
     *
     * @param source the String to escape SQL from
     * @return the escaped value of the parameter source
     */
    public static String escapeSql(String source) {

        return source.replaceAll("'", "''");
    }

    /**
     * Escapes the wildcard characters in a string which will be used as the pattern for a SQL LIKE clause.<p>
     *
     * @param pattern the pattern
     * @param escapeChar the character which should be used as the escape character
     *
     * @return the escaped pattern
     */
    public static String escapeSqlLikePattern(String pattern, char escapeChar) {

        char[] special = new char[] {escapeChar, '%', '_'};
        String result = pattern;
        for (char charToEscape : special) {
            result = result.replaceAll("" + charToEscape, "" + escapeChar + charToEscape);
        }
        return result;
    }

    /**
     * Encodes a String in a way similar JavaScript "encodeURIcomponent" function.<p>
     *
     * Multiple blanks are encoded _multiply_ with <code>%20</code>.<p>
     *
     * @param source The text to be encoded
     * @param encoding the encoding type
     *
     * @return The encoded String
     */
    public static String escapeWBlanks(String source, String encoding) {

        if (CmsStringUtil.isEmpty(source)) {
            return source;
        }
        StringBuffer ret = new StringBuffer(source.length() * 2);

        // URLEncode the text string
        // this produces a very similar encoding to JavaSscript encoding,
        // except the blank which is not encoded into "%20" instead of "+"

        String enc = encode(source, encoding);
        for (int z = 0; z < enc.length(); z++) {
            char c = enc.charAt(z);
            if (c == '+') {
                ret.append("%20");
            } else {
                ret.append(c);
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
     * </ul><p>
     *
     * @param source the string to escape
     *
     * @return the escaped string
     *
     * @see #escapeHtml(String)
     */
    public static String escapeXml(String source) {

        return escapeXml(source, false);
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
     * </ul><p>
     *
     * @param source the string to escape
     * @param doubleEscape if <code>false</code>, all entities that already are escaped are left untouched
     *
     * @return the escaped string
     *
     * @see #escapeHtml(String)
     */
    public static String escapeXml(String source, boolean doubleEscape) {

        if (source == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(source.length() * 2);

        for (int i = 0; i < source.length(); ++i) {
            char ch = source.charAt(i);
            switch (ch) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    // don't escape already escaped international and special characters
                    if (!doubleEscape) {
                        int terminatorIndex = source.indexOf(";", i);
                        if (terminatorIndex > 0) {
                            if (source.substring(i + 1, terminatorIndex).matches("#[0-9]+")) {
                                result.append(ch);
                                break;
                            }
                        }
                    }
                    // note that to other "break" in the above "if" block
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(ch);
            }
        }
        return new String(result);
    }

    /**
     * Checks if a given encoding name is actually supported, and if so
     * resolves it to it's canonical name, if not it returns the given fallback
     * value.<p>
     *
     * Charsets have a set of aliases. For example, valid aliases for "UTF-8"
     * are "UTF8", "utf-8" or "utf8". This method resolves any given valid charset name
     * to it's "canonical" form, so that simple String comparison can be used
     * when checking charset names internally later.<p>
     *
     * Please see <a href="http://www.iana.org/assignments/character-sets">http://www.iana.org/assignments/character-sets</a>
     * for a list of valid charset alias names.<p>
     *
     * @param encoding the encoding to check and resolve
     * @param fallback the fallback encoding scheme
     *
     * @return the resolved encoding name, or the fallback value
     */
    public static String lookupEncoding(String encoding, String fallback) {

        String result = m_encodingCache.get(encoding);
        if (result != null) {
            return result;
        }

        try {
            result = Charset.forName(encoding).name();
            m_encodingCache.put(encoding, result);
            return result;
        } catch (Throwable t) {
            // we will use the default value as fallback
        }

        return fallback;
    }

    /**
     * Re-decodes a String that has not been correctly decoded and thus has scrambled
     * character bytes.<p>
     *
     * This is an equivalent to the JavaScript "decodeURIComponent" function.
     * It converts from the default "UTF-8" to the currently selected system encoding.<p>
     *
     * @param input the String to convert
     *
     * @return String the converted String
     */
    public static String redecodeUriComponent(String input) {

        if (input == null) {
            return input;
        }
        return new String(
            changeEncoding(input.getBytes(), ENCODING_UTF_8, OpenCms.getSystemInfo().getDefaultEncoding()));
    }

    /**
     * Decodes a String in a way similar to the JavaScript "decodeURIcomponent" function,
     * using "UTF-8" for character encoding.<p>
     *
     * This method can decode Strings that have been encoded in JavaScript with "encodeURIcomponent".<p>
     *
     * <b>Directly exposed for JSP EL<b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param source The String to be decoded
     *
     * @return The decoded String
     */
    public static String unescape(String source) {

        return unescape(source, ENCODING_UTF_8);
    }

    /**
     * Decodes a String in a way similar to the JavaScript "decodeURIcomponent" function.<p>
     *
     * This method can decode Strings that have been encoded in JavaScript with "encodeURIcomponent",
     * provided "UTF-8" is used as encoding.<p>
     *
     * <b>Directly exposed for JSP EL<b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param source The String to be decoded
     * @param encoding the encoding type
     *
     * @return The decoded String
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
     * Decrypts a byte array obfuscated with 'obfuscateBytes'.<p>
     *
     * @param source the source
     * @return the resuvlt
     */
    private static byte[] deobfuscateBytes(byte[] source) {

        byte[] result = new byte[source.length - 1];
        System.arraycopy(source, 1, result, 0, source.length - 1);
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte)(0xFF & (result[i] ^ source[0]));
        }
        return result;
    }

    /**
     * Simple "obfuscation" for byte arrays using random numbers.<p>
     *
     * @param source the source array
     * @return the result
     */
    private static byte[] obfuscateBytes(byte[] source) {

        byte[] s = new byte[1];
        m_random.nextBytes(s);
        byte[] result = new byte[source.length + 1];
        System.arraycopy(source, 0, result, 1, source.length);
        result[0] = s[0];
        for (int i = 1; i < result.length; i++) {
            result[i] = (byte)(0xFF & (result[i] ^ s[0]));
        }
        return result;
    }

}