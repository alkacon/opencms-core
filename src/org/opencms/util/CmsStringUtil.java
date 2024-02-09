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

package org.opencms.util;

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.awt.Color;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.google.common.base.Optional;

/**
 * Provides String utility functions.<p>
 *
 * @since 6.0.0
 */
public final class CmsStringUtil {

    /**
     * Compares two Strings according to the count of containing slashes.<p>
     *
     * If both Strings contain the same count of slashes the Strings are compared.<p>
     */
    public static class CmsSlashComparator implements Comparator<String> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(String a, String b) {

            int slashCountA = countChar(a, '/');
            int slashCountB = countChar(b, '/');

            if (slashCountA < slashCountB) {
                return 1;
            } else if (slashCountA == slashCountB) {
                return a.compareTo(b);
            } else {
                return -1;
            }
        }
    }

    /** Regular expression that matches the HTML body end tag. */
    public static final String BODY_END_REGEX = "<\\s*/\\s*body[^>]*>";

    /** Regular expression that matches the HTML body start tag. */
    public static final String BODY_START_REGEX = "<\\s*body[^>]*>";

    /** Constant for <code>"false"</code>. */
    public static final String FALSE = Boolean.toString(false);

    /** a convenient shorthand to the line separator constant. */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** Context macro. */
    public static final String MACRO_OPENCMS_CONTEXT = "${OpenCmsContext}";

    /** Pattern to determine a locale for suffixes like '_de' or '_en_US'. */
    public static final Pattern PATTERN_LOCALE_SUFFIX = Pattern.compile(
        "(.*)_([a-z]{2}(?:_[A-Z]{2})?)(?:\\.[^\\.]*)?$");

    /** Pattern to determine the document number for suffixes like '_0001'. */
    public static final Pattern PATTERN_NUMBER_SUFFIX = Pattern.compile("(.*)_(\\d+)(\\.[^\\.^\\n]*)?$");

    /** Pattern matching one or more slashes. */
    public static final Pattern PATTERN_SLASHES = Pattern.compile("/+");

    /** The place holder end sign in the pattern. */
    public static final String PLACEHOLDER_END = "}";

    /** The place holder start sign in the pattern. */
    public static final String PLACEHOLDER_START = "{";

    /** Contains all chars that end a sentence in the {@link #trimToSize(String, int, int, String)} method. */
    public static final char[] SENTENCE_ENDING_CHARS = {'.', '!', '?'};

    /** a convenient shorthand for tabulations.  */
    public static final String TABULATOR = "  ";

    /** Constant for <code>"true"</code>. */
    public static final String TRUE = Boolean.toString(true);

    /** Regex pattern that matches an end body tag. */
    private static final Pattern BODY_END_PATTERN = Pattern.compile(BODY_END_REGEX, Pattern.CASE_INSENSITIVE);

    /** Regex pattern that matches a start body tag. */
    private static final Pattern BODY_START_PATTERN = Pattern.compile(BODY_START_REGEX, Pattern.CASE_INSENSITIVE);

    /** Day constant. */
    private static final long DAYS = 1000 * 60 * 60 * 24;

    /** Multipliers used for duration parsing. */
    private static final long[] DURATION_MULTIPLIERS = {24L * 60 * 60 * 1000, 60L * 60 * 1000, 60L * 1000, 1000L, 1L};

    /** Number and unit pattern for duration parsing. */
    private static final Pattern DURATION_NUMBER_AND_UNIT_PATTERN = Pattern.compile("([0-9]+)([a-z]+)");

    /** Units used for duration parsing. */
    private static final String[] DURATION_UNTIS = {"d", "h", "m", "s", "ms"};

    /** Hour constant. */
    private static final long HOURS = 1000 * 60 * 60;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsStringUtil.class);

    /** OpenCms context replace String, static for performance reasons. */
    private static String m_contextReplace;

    /** OpenCms context search String, static for performance reasons. */
    private static String m_contextSearch;

    /** Minute constant. */
    private static final long MINUTES = 1000 * 60;

    /** Second constant. */
    private static final long SECONDS = 1000;

    /** Regex that matches an encoding String in an xml head. */
    private static final Pattern XML_ENCODING_REGEX = Pattern.compile(
        "encoding\\s*=\\s*[\"'].+[\"']",
        Pattern.CASE_INSENSITIVE);

    /** Regex that matches an xml head. */
    private static final Pattern XML_HEAD_REGEX = Pattern.compile("<\\s*\\?.*\\?\\s*>", Pattern.CASE_INSENSITIVE);

    /** Pattern matching sequences of non-slash characters. */
    private static final Pattern NOT_SLASHES = Pattern.compile("[^/]+");

    /**
     * Default constructor (empty), private because this class has only
     * static methods.<p>
     */
    private CmsStringUtil() {

        // empty
    }

    /**
     * Adds leading and trailing slashes to a path,
     * if the path does not already start or end with a slash.<p>
     *
     * <b>Directly exposed for JSP EL<b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param path the path to which add the slashes
     *
     * @return the path with added leading and trailing slashes
     */
    public static String addLeadingAndTrailingSlash(String path) {

        StringBuffer buffer1 = new StringBuffer();
        if (!path.startsWith("/")) {
            buffer1.append("/");
        }
        buffer1.append(path);
        if (!path.endsWith("/") && !path.isEmpty()) {
            buffer1.append("/");
        }
        return buffer1.toString();
    }

    /**
     * Returns a string representation for the given array using the given separator.<p>
     *
     * @param arg the array to transform to a String
     * @param separator the item separator
     *
     * @return the String of the given array
     */
    public static String arrayAsString(final String[] arg, String separator) {

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < arg.length; i++) {
            result.append(arg[i]);
            if ((i + 1) < arg.length) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    /**
     * Changes the given filenames suffix from the current suffix to the provided suffix.
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param filename the filename to be changed
     * @param suffix the new suffix of the file
     *
     * @return the filename with the replaced suffix
     */
    public static String changeFileNameSuffixTo(String filename, String suffix) {

        int dotPos = filename.lastIndexOf('.');
        if (dotPos != -1) {
            return filename.substring(0, dotPos + 1) + suffix;
        } else {
            // the string has no suffix
            return filename;
        }
    }

    /**
     * Checks if a given name is composed only of the characters <code>a...z,A...Z,0...9</code>
     * and the provided <code>constraints</code>.<p>
     *
     * If the check fails, an Exception is generated. The provided bundle and key is
     * used to generate the Exception. 4 parameters are passed to the Exception:<ol>
     * <li>The <code>name</code>
     * <li>The first illegal character found
     * <li>The position where the illegal character was found
     * <li>The <code>constraints</code></ol>
     *
     * @param name the name to check
     * @param constraints the additional character constraints
     * @param key the key to use for generating the Exception (if required)
     * @param bundle the bundle to use for generating the Exception (if required)
     *
     * @throws CmsIllegalArgumentException if the check fails (generated from the given key and bundle)
     */
    public static void checkName(String name, String constraints, String key, I_CmsMessageBundle bundle)
    throws CmsIllegalArgumentException {

        int l = name.length();
        for (int i = 0; i < l; i++) {
            char c = name.charAt(i);
            if (((c < 'a') || (c > 'z'))
                && ((c < '0') || (c > '9'))
                && ((c < 'A') || (c > 'Z'))
                && (constraints.indexOf(c) < 0)) {

                throw new CmsIllegalArgumentException(
                    bundle.container(key, new Object[] {name, Character.valueOf(c), Integer.valueOf(i), constraints}));
            }
        }
    }

    /**
     * Returns a string representation for the given collection using the given separator.<p>
     *
     * @param collection the collection to print
     * @param separator the item separator
     *
     * @return the string representation for the given collection
     */
    public static String collectionAsString(Collection<?> collection, String separator) {

        StringBuffer string = new StringBuffer(128);
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            string.append(it.next());
            if (it.hasNext()) {
                string.append(separator);
            }
        }
        return string.toString();
    }

    /**
     * Compares two paths, ignoring leading and trailing slashes.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param path1 the first path
     * @param path2 the second path
     *
     * @return true if the paths are equal (ignoring leading and trailing slashes)
     */
    public static boolean comparePaths(String path1, String path2) {

        return addLeadingAndTrailingSlash(path1).equals(addLeadingAndTrailingSlash(path2));
    }

    /**
     * Counts the occurrence of a given char in a given String.<p>
     *
     * @param s the string
     * @param c the char to count
     *
     * @return returns the count of occurrences of a given char in a given String
     */
    public static int countChar(String s, char c) {

        int counter = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Returns a String array representation for the given enum.<p>
     *
     * @param <T> the type of the enum
     * @param values the enum values
     *
     * @return the representing String array
     */
    public static <T extends Enum<T>> String[] enumNameToStringArray(T[] values) {

        int i = 0;
        String[] result = new String[values.length];
        for (T value : values) {
            result[i++] = value.name();
        }
        return result;
    }

    /**
     * Replaces line breaks to <code>&lt;br/&gt;</code> and HTML control characters
     * like <code>&lt; &gt; &amp; &quot;</code> with their HTML entity representation.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param source the String to escape
     *
     * @return the escaped String
     */
    public static String escapeHtml(String source) {

        if (source == null) {
            return null;
        }
        source = CmsEncoder.escapeXml(source);
        source = CmsStringUtil.substitute(source, "\r", "");
        source = CmsStringUtil.substitute(source, "\n", "<br/>\n");
        return source;
    }

    /**
     * Escapes a String so it may be used in JavaScript String definitions.<p>
     *
     * This method escapes
     * line breaks (<code>\r\n,\n</code>) quotation marks (<code>".'</code>)
     * and slash as well as backspace characters (<code>\,/</code>).<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param source the String to escape
     *
     * @return the escaped String
     */
    public static String escapeJavaScript(String source) {

        source = CmsStringUtil.substitute(source, "\\", "\\\\");
        source = CmsStringUtil.substitute(source, "\"", "\\\"");
        source = CmsStringUtil.substitute(source, "\'", "\\\'");
        source = CmsStringUtil.substitute(source, "\r\n", "\\n");
        source = CmsStringUtil.substitute(source, "\n", "\\n");

        // to avoid XSS (closing script tags) in embedded Javascript
        source = CmsStringUtil.substitute(source, "/", "\\/");
        return source;
    }

    /**
     * Escapes a String so it may be used as a Perl5 regular expression.<p>
     *
     * This method replaces the following characters in a String:<br>
     * <code>{}[]()\$^.*+/</code><p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param source the string to escape
     *
     * @return the escaped string
     */
    public static String escapePattern(String source) {

        if (source == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(source.length() * 2);
        for (int i = 0; i < source.length(); ++i) {
            char ch = source.charAt(i);
            switch (ch) {
                case '\\':
                    result.append("\\\\");
                    break;
                case '/':
                    result.append("\\/");
                    break;
                case '$':
                    result.append("\\$");
                    break;
                case '^':
                    result.append("\\^");
                    break;
                case '.':
                    result.append("\\.");
                    break;
                case '*':
                    result.append("\\*");
                    break;
                case '+':
                    result.append("\\+");
                    break;
                case '|':
                    result.append("\\|");
                    break;
                case '?':
                    result.append("\\?");
                    break;
                case '{':
                    result.append("\\{");
                    break;
                case '}':
                    result.append("\\}");
                    break;
                case '[':
                    result.append("\\[");
                    break;
                case ']':
                    result.append("\\]");
                    break;
                case '(':
                    result.append("\\(");
                    break;
                case ')':
                    result.append("\\)");
                    break;
                default:
                    result.append(ch);
            }
        }
        return new String(result);
    }

    /**
     * This method takes a part of a html tag definition, an attribute to extend within the
     * given text and a default value for this attribute; and returns a <code>{@link Map}</code>
     * with 2 values: a <code>{@link String}</code> with key <code>"text"</code> with the new text
     * without the given attribute, and another <code>{@link String}</code> with key <code>"value"</code>
     * with the new extended value for the given attribute, this value is surrounded by the same type of
     * quotation marks as in the given text.<p>
     *
     * @param text the text to search in
     * @param attribute the attribute to remove and extend from the text
     * @param defValue a default value for the attribute, should not have any quotation mark
     *
     * @return a map with the new text and the new value for the given attribute
     */
    public static Map<String, String> extendAttribute(String text, String attribute, String defValue) {

        Map<String, String> retValue = new HashMap<String, String>();
        retValue.put("text", text);
        retValue.put("value", "'" + defValue + "'");
        if ((text != null) && (text.toLowerCase().indexOf(attribute.toLowerCase()) >= 0)) {
            // this does not work for things like "att=method()" without quotations.
            String quotation = "\'";
            int pos1 = text.toLowerCase().indexOf(attribute.toLowerCase());
            // looking for the opening quotation mark
            int pos2 = text.indexOf(quotation, pos1);
            int test = text.indexOf("\"", pos1);
            if ((test > -1) && ((pos2 == -1) || (test < pos2))) {
                quotation = "\"";
                pos2 = test;
            }
            // assuming there is a closing quotation mark
            int pos3 = text.indexOf(quotation, pos2 + 1);
            // building the new attribute value
            String newValue = quotation + defValue + text.substring(pos2 + 1, pos3 + 1);
            // removing the onload statement from the parameters
            String newText = text.substring(0, pos1);
            if (pos3 < text.length()) {
                newText += text.substring(pos3 + 1);
            }
            retValue.put("text", newText);
            retValue.put("value", newValue);
        }
        return retValue;
    }

    /**
     * Extracts the content of a <code>&lt;body&gt;</code> tag in a HTML page.<p>
     *
     * This method should be pretty robust and work even if the input HTML does not contains
     * a valid body tag.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param content the content to extract the body from
     *
     * @return the extracted body tag content
     */
    public static String extractHtmlBody(String content) {

        Matcher startMatcher = BODY_START_PATTERN.matcher(content);
        Matcher endMatcher = BODY_END_PATTERN.matcher(content);

        int start = 0;
        int end = content.length();

        if (startMatcher.find()) {
            start = startMatcher.end();
        }

        if (endMatcher.find(start)) {
            end = endMatcher.start();
        }

        return content.substring(start, end);
    }

    /**
     * Extracts the xml encoding setting from an xml file that is contained in a String by parsing
     * the xml head.<p>
     *
     * This is useful if you have a byte array that contains a xml String,
     * but you do not know the xml encoding setting. Since the encoding setting
     * in the xml head is usually encoded with standard US-ASCII, you usually
     * just create a String of the byte array without encoding setting,
     * and use this method to find the 'true' encoding. Then create a String
     * of the byte array again, this time using the found encoding.<p>
     *
     * This method will return <code>null</code> in case no xml head
     * or encoding information is contained in the input.<p>
     *
     * @param content the xml content to extract the encoding from
     *
     * @return the extracted encoding, or null if no xml encoding setting was found in the input
     */
    public static String extractXmlEncoding(String content) {

        String result = null;
        Matcher xmlHeadMatcher = XML_HEAD_REGEX.matcher(content);
        if (xmlHeadMatcher.find()) {
            String xmlHead = xmlHeadMatcher.group();
            Matcher encodingMatcher = XML_ENCODING_REGEX.matcher(xmlHead);
            if (encodingMatcher.find()) {
                String encoding = encodingMatcher.group();
                int pos1 = encoding.indexOf('=') + 2;
                String charset = encoding.substring(pos1, encoding.length() - 1);
                if (Charset.isSupported(charset)) {
                    result = charset;
                }
            }
        }
        return result;
    }

    /**
     * Shortens a resource name or path so that it is not longer than the provided maximum length.<p>
     *
     * In order to reduce the length of the resource name, only
     * complete folder names are removed and replaced with ... successively,
     * starting with the second folder.
     * The first folder is removed only in case the result still does not fit
     * if all subfolders have been removed.<p>
     *
     * Example: <code>formatResourceName("/myfolder/subfolder/index.html", 21)</code>
     * returns <code>/myfolder/.../index.html</code>.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param name the resource name to format
     * @param maxLength the maximum length of the resource name (without leading <code>/...</code>)
     *
     * @return the formatted resource name
     */
    public static String formatResourceName(String name, int maxLength) {

        if (name == null) {
            return null;
        }

        if (name.length() <= maxLength) {
            return name;
        }

        int total = name.length();
        String[] names = CmsStringUtil.splitAsArray(name, "/");
        if (name.endsWith("/")) {
            names[names.length - 1] = names[names.length - 1] + "/";
        }
        for (int i = 1; (total > maxLength) && (i < (names.length - 1)); i++) {
            if (i > 1) {
                names[i - 1] = "";
            }
            names[i] = "...";
            total = 0;
            for (int j = 0; j < names.length; j++) {
                int l = names[j].length();
                total += l + ((l > 0) ? 1 : 0);
            }
        }
        if (total > maxLength) {
            names[0] = (names.length > 2) ? "" : (names.length > 1) ? "..." : names[0];
        }

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < names.length; i++) {
            if (names[i].length() > 0) {
                result.append("/");
                result.append(names[i]);
            }
        }

        return result.toString();
    }

    /**
     * Formats a runtime in the format hh:mm:ss, to be used e.g. in reports.<p>
     *
     * If the runtime is greater then 24 hours, the format dd:hh:mm:ss is used.<p>
     *
     * @param runtime the time to format
     *
     * @return the formatted runtime
     */
    public static String formatRuntime(long runtime) {

        long seconds = (runtime / SECONDS) % 60;
        long minutes = (runtime / MINUTES) % 60;
        long hours = (runtime / HOURS) % 24;
        long days = runtime / DAYS;
        StringBuffer strBuf = new StringBuffer();

        if (days > 0) {
            if (days < 10) {
                strBuf.append('0');
            }
            strBuf.append(days);
            strBuf.append(':');
        }

        if (hours < 10) {
            strBuf.append('0');
        }
        strBuf.append(hours);
        strBuf.append(':');

        if (minutes < 10) {
            strBuf.append('0');
        }
        strBuf.append(minutes);
        strBuf.append(':');

        if (seconds < 10) {
            strBuf.append('0');
        }
        strBuf.append(seconds);

        return strBuf.toString();
    }

    /**
     * Returns the color value (<code>{@link Color}</code>) for the given String value.<p>
     *
     * All parse errors are caught and the given default value is returned in this case.<p>
     *
     * @param value the value to parse as color
     * @param defaultValue the default value in case of parsing errors
     * @param key a key to be included in the debug output in case of parse errors
     *
     * @return the int value for the given parameter value String
     */
    public static Color getColorValue(String value, Color defaultValue, String key) {

        Color result;
        try {
            char pre = value.charAt(0);
            if (pre != '#') {
                value = "#" + value;
            }
            result = Color.decode(value);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_UNABLE_TO_PARSE_COLOR_2, value, key));
            }
            result = defaultValue;
        }
        return result;
    }

    /**
     * Returns the common parent path of two paths.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param first the first path
     * @param second the second path
     *
     * @return the common prefix path
     */
    public static String getCommonPrefixPath(String first, String second) {

        List<String> firstComponents = getPathComponents(first);
        List<String> secondComponents = getPathComponents(second);
        int minSize = Math.min(firstComponents.size(), secondComponents.size());
        StringBuffer resultBuffer = new StringBuffer();
        for (int i = 0; i < minSize; i++) {
            if (firstComponents.get(i).equals(secondComponents.get(i))) {
                resultBuffer.append("/");
                resultBuffer.append(firstComponents.get(i));
            } else {
                break;
            }
        }
        String result = resultBuffer.toString();
        if (result.length() == 0) {
            result = "/";
        }
        return result;
    }

    /**
     * Returns the Ethernet-Address of the locale host.<p>
     *
     * A dummy ethernet address is returned, if the ip is
     * representing the loopback address or in case of exceptions.<p>
     *
     * @return the Ethernet-Address
     */
    public static String getEthernetAddress() {

        try {
            InetAddress ip = InetAddress.getLocalHost();
            if (!ip.isLoopbackAddress()) {
                NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                byte[] mac = network.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", Byte.valueOf(mac[i]), (i < (mac.length - 1)) ? ":" : ""));
                }
                return sb.toString();
            }
        } catch (Throwable t) {
            // if an exception occurred return a dummy address
        }
        // return a dummy ethernet address, if the ip is representing the loopback address or in case of exceptions
        return CmsUUID.getDummyEthernetAddress();
    }

    /**
     * Returns the Integer (int) value for the given String value.<p>
     *
     * All parse errors are caught and the given default value is returned in this case.<p>
     *
     * @param value the value to parse as int
     * @param defaultValue the default value in case of parsing errors
     * @param key a key to be included in the debug output in case of parse errors
     *
     * @return the int value for the given parameter value String
     */
    public static int getIntValue(String value, int defaultValue, String key) {

        int result;
        try {
            result = Integer.valueOf(value).intValue();
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_UNABLE_TO_PARSE_INT_2, value, key));
            }
            result = defaultValue;
        }
        return result;
    }

    /**
     * Returns the closest Integer (int) value for the given String value.<p>
     *
     * All parse errors are caught and the given default value is returned in this case.<p>
     *
     * @param value the value to parse as int, can also represent a float value
     * @param defaultValue the default value in case of parsing errors
     * @param key a key to be included in the debug output in case of parse errors
     *
     * @return the closest int value for the given parameter value String
     */
    public static int getIntValueRounded(String value, int defaultValue, String key) {

        int result;
        try {
            result = Math.round(Float.parseFloat(value));
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_UNABLE_TO_PARSE_INT_2, value, key));
            }
            result = defaultValue;
        }
        return result;
    }

    /**
     * Returns a Locale calculated from the suffix of the given String, or <code>null</code> if no locale suffix is found.<p>
     *
     * The locale returned will include the optional country code if this was part of the suffix.<p>
     *
     * Calls {@link CmsResource#getName(String)} first, so the given name can also be a resource root path.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param name the name to get the locale for
     *
     * @return the locale, or <code>null</code>
     *
     * @see #getLocaleSuffixForName(String)
     */
    public static Locale getLocaleForName(String name) {

        String suffix = getLocaleSuffixForName(CmsResource.getName(name));
        if (suffix != null) {
            String laguageString = suffix.substring(0, 2);
            return suffix.length() == 5 ? new Locale(laguageString, suffix.substring(3, 5)) : new Locale(laguageString);
        }
        return null;
    }

    /**
     * Returns the locale for the given text based on the language detection library.<p>
     *
     * The result will be <code>null</code> if the detection fails or the detected locale is not configured
     * in the 'opencms-system.xml' as available locale.<p>
     *
     * @param text the text to retrieve the locale for
     *
     * @return the detected locale for the given text
     */
    public static Locale getLocaleForText(String text) {

        // try to detect locale by language detector
        if (isNotEmptyOrWhitespaceOnly(text)) {
            try {
                Detector detector = DetectorFactory.create();
                detector.append(text);
                String lang = detector.detect();
                Locale loc = new Locale(lang);
                if (OpenCms.getLocaleManager().getAvailableLocales().contains(loc)) {
                    return loc;
                }
            } catch (LangDetectException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    /**
     * Returns the locale suffix from the given String, or <code>null</code> if no locae suffix is found.<p>
     *
     * Uses the the {@link #PATTERN_LOCALE_SUFFIX} to find a language_country occurrence in the
     * given name and returns the first group of the match.<p>
     *
     * <b>Examples:</b>
     *
     * <ul>
     * <li><code>rabbit_en_EN.html -> Locale[en_EN]</code>
     * <li><code>rabbit_en_EN&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-> Locale[en_EN]</code>
     * <li><code>rabbit_en.html&nbsp;&nbsp;&nbsp;&nbsp;-> Locale[en]</code>
     * <li><code>rabbit_en&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-> Locale[en]</code>
     * <li><code>rabbit_en.&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-> Locale[en]</code>
     * <li><code>rabbit_enr&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-> null</code>
     * <li><code>rabbit_en.tar.gz&nbsp;&nbsp;-> null</code>
     * </ul>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param name the resource name to get the locale suffix for
     *
     * @return the locale suffix if found, <code>null</code> otherwise
     */
    public static String getLocaleSuffixForName(String name) {

        Matcher matcher = PATTERN_LOCALE_SUFFIX.matcher(name);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    /**
     * Returns the Long (long) value for the given String value.<p>
     *
     * All parse errors are caught and the given default value is returned in this case.<p>
     *
     * @param value the value to parse as long
     * @param defaultValue the default value in case of parsing errors
     * @param key a key to be included in the debug output in case of parse errors
     *
     * @return the long value for the given parameter value String
     */
    public static long getLongValue(String value, long defaultValue, String key) {

        long result;
        try {
            result = Long.valueOf(value).longValue();
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_UNABLE_TO_PARSE_INT_2, value, key));
            }
            result = defaultValue;
        }
        return result;
    }

    /**
     * Splits a path into its non-empty path components.<p>
     *
     * If the path is the root path, an empty list will be returned.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param path the path to split
     *
     * @return the list of non-empty path components
     */
    public static List<String> getPathComponents(String path) {

        List<String> result = CmsStringUtil.splitAsList(path, "/");
        Iterator<String> iter = result.iterator();
        while (iter.hasNext()) {
            String token = iter.next();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(token)) {
                iter.remove();
            }
        }
        return result;
    }

    /**
     * Converts the given path to a path relative to a base folder,
     * but only if it actually is a sub-path of the latter,
     * otherwise <code>null</code> is returned.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param base the base path
     * @param path the path which should be converted to a relative path
     *
     * @return 'path' converted to a path relative to 'base', or null if 'path' is not a sub-folder of 'base'
     */
    public static String getRelativeSubPath(String base, String path) {

        String result = null;
        base = CmsStringUtil.joinPaths(base, "/");
        path = CmsStringUtil.joinPaths(path, "/");
        if (path.startsWith(base)) {
            result = path.substring(base.length());
        }
        if (result != null) {
            if (result.endsWith("/")) {
                result = result.substring(0, result.length() - 1);
            }
            if (!result.startsWith("/")) {
                result = "/" + result;
            }
        }
        return result;
    }

    /**
     * Inserts the given number of spaces at the start of each line in the given text.
     * <p>This is useful when writing toString() methods for complex nested objects.</p>
     *
     * @param text the text to indent
     * @param numSpaces the number of spaces to insert before each line
     *
     * @return the indented text
     */
    public static String indentLines(String text, int numSpaces) {

        return text.replaceAll("(?m)^", StringUtils.repeat(" ", numSpaces));
    }

    /**
     * Returns <code>true</code> if the provided String is either <code>null</code>
     * or the empty String <code>""</code>.<p>
     *
     * @param value the value to check
     *
     * @return true, if the provided value is null or the empty String, false otherwise
     */
    public static boolean isEmpty(String value) {

        return (value == null) || (value.length() == 0);
    }

    /**
     * Returns <code>true</code> if the provided String is either <code>null</code>
     * or contains only white spaces.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param value the value to check
     *
     * @return true, if the provided value is null or contains only white spaces, false otherwise
     */
    public static boolean isEmptyOrWhitespaceOnly(String value) {

        return isEmpty(value) || (value.trim().length() == 0);
    }

    /**
     * Returns <code>true</code> if the provided Objects are either both <code>null</code>
     * or equal according to {@link Object#equals(Object)}.<p>
     *
     * @param value1 the first object to compare
     * @param value2 the second object to compare
     *
     * @return <code>true</code> if the provided Objects are either both <code>null</code>
     *              or equal according to {@link Object#equals(Object)}
     */
    public static boolean isEqual(Object value1, Object value2) {

        if (value1 == null) {
            return (value2 == null);
        }
        return value1.equals(value2);
    }

    /**
     * Returns <code>true</code> if the provided String is neither <code>null</code>
     * nor the empty String <code>""</code>.<p>
     *
     * @param value the value to check
     *
     * @return true, if the provided value is not null and not the empty String, false otherwise
     */
    public static boolean isNotEmpty(String value) {

        return (value != null) && (value.length() != 0);
    }

    /**
     * Returns <code>true</code> if the provided String is neither <code>null</code>
     * nor contains only white spaces.<p>
     *
     * @param value the value to check
     *
     * @return <code>true</code>, if the provided value is <code>null</code>
     *          or contains only white spaces, <code>false</code> otherwise
     */
    public static boolean isNotEmptyOrWhitespaceOnly(String value) {

        return (value != null) && (value.trim().length() > 0);
    }

    /**
     * Checks if the first path is a prefix of the second path.<p>
     *
     * This method is different compared to {@link String#startsWith},
     * because it considers <code>/foo/bar</code> to
     * be a prefix path of <code>/foo/bar/baz</code>,
     * but not of <code>/foo/bar42</code>.
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param firstPath the first path
     * @param secondPath the second path
     *
     * @return true if the first path is a prefix path of the second path
     */
    public static boolean isPrefixPath(String firstPath, String secondPath) {

        firstPath = CmsStringUtil.joinPaths(firstPath, "/");
        secondPath = CmsStringUtil.joinPaths(secondPath, "/");
        return secondPath.startsWith(firstPath);
    }

    /**
     * Checks if the first path is a prefix of the second path, but not equivalent to it.<p>
     *
     * @param firstPath the first path
     * @param secondPath the second path
     *
     * @return true if the first path is a prefix path of the second path, but not equivalent
     */
    public static boolean isProperPrefixPath(String firstPath, String secondPath) {

        firstPath = CmsStringUtil.joinPaths(firstPath, "/");
        secondPath = CmsStringUtil.joinPaths(secondPath, "/");
        return secondPath.startsWith(firstPath) && !firstPath.equals(secondPath);

    }

    /**
     * Checks if the given class name is a valid Java class name.<p>
     *
     * <b>Directly exposed for JSP EL</b>, not through {@link org.opencms.jsp.util.CmsJspElFunctions}.<p>
     *
     * @param className the name to check
     *
     * @return true if the given class name is a valid Java class name
     */
    public static boolean isValidJavaClassName(String className) {

        if (CmsStringUtil.isEmpty(className)) {
            return false;
        }
        int length = className.length();
        boolean nodot = true;
        for (int i = 0; i < length; i++) {
            char ch = className.charAt(i);
            if (nodot) {
                if (ch == '.') {
                    return false;
                } else if (Character.isJavaIdentifierStart(ch)) {
                    nodot = false;
                } else {
                    return false;
                }
            } else {
                if (ch == '.') {
                    nodot = true;
                } else if (Character.isJavaIdentifierPart(ch)) {
                    nodot = false;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Concatenates multiple paths and separates them with '/'.<p>
     *
     * Consecutive slashes will be reduced to a single slash in the resulting string.
     * For example, joinPaths("/foo/", "/bar", "baz") will return "/foo/bar/baz".
     *
     * @param paths the list of paths
     *
     * @return the joined path
     */
    public static String joinPaths(List<String> paths) {

        String result = listAsString(paths, "/");
        // result may now contain multiple consecutive slashes, so reduce them to single slashes
        result = PATTERN_SLASHES.matcher(result).replaceAll("/");
        return result;
    }

    /**
     * Concatenates multiple paths and separates them with '/'.<p>
     *
     * Consecutive slashes will be reduced to a single slash in the resulting string.
     * For example joinPaths("/foo/", "/bar", "baz") will return "/foo/bar/baz".<p>
     *
     * If one of the argument paths already contains a double "//" this will also be reduced to '/'.
     * For example joinPaths("/foo//bar/", "/baz") will return "/foo/bar/baz".
     *
     * @param paths the array of paths
     *
     * @return the joined path
     */
    public static String joinPaths(String... paths) {

        StringBuffer result = new StringBuffer(paths.length * 32);
        boolean noSlash = true;
        for (int i = 0; i < paths.length; i++) {
            for (int j = 0; j < paths[i].length(); j++) {
                char c = paths[i].charAt(j);
                if (c != '/') {
                    result.append(c);
                    noSlash = true;
                } else if (noSlash) {
                    result.append('/');
                    noSlash = false;
                }
            }
            if (noSlash && (i < (paths.length - 1))) {
                result.append('/');
                noSlash = false;
            }
        }
        return result.toString();
    }

    /**
     * Returns the last index of any of the given chars in the given source.<p>
     *
     * If no char is found, -1 is returned.<p>
     *
     * @param source the source to check
     * @param chars the chars to find
     *
     * @return the last index of any of the given chars in the given source, or -1
     */
    public static int lastIndexOf(String source, char[] chars) {

        // now try to find an "sentence ending" char in the text in the "findPointArea"
        int result = -1;
        for (int i = 0; i < chars.length; i++) {
            int pos = source.lastIndexOf(chars[i]);
            if (pos > result) {
                // found new last char
                result = pos;
            }
        }
        return result;
    }

    /**
     * Returns the last index a whitespace char the given source.<p>
     *
     * If no whitespace char is found, -1 is returned.<p>
     *
     * @param source the source to check
     *
     * @return the last index a whitespace char the given source, or -1
     */
    public static int lastWhitespaceIn(String source) {

        if (CmsStringUtil.isEmpty(source)) {
            return -1;
        }
        int pos = -1;
        for (int i = source.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(source.charAt(i))) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    /**
     * Returns a string representation for the given list using the given separator.<p>
     *
     * @param list the list to write
     * @param separator the item separator string
     *
     * @return the string representation for the given map
     */
    public static String listAsString(List<?> list, String separator) {

        StringBuffer string = new StringBuffer(128);
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            string.append(it.next());
            if (it.hasNext()) {
                string.append(separator);
            }
        }
        return string.toString();
    }

    /**
     * Encodes a map with string keys and values as a JSON string with the same keys/values.<p>
     *
     * @param map the input map
     * @return the JSON data containing the map entries
     */
    public static String mapAsJson(Map<String, String> map) {

        JSONObject obj = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                obj.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return obj.toString();
    }

    /**
     * Returns a string representation for the given map using the given separators.<p>
     *
     * @param <K> type of map keys
     * @param <V> type of map values
     * @param map the map to write
     * @param sepItem the item separator string
     * @param sepKeyval the key-value pair separator string
     *
     * @return the string representation for the given map
     */
    public static <K, V> String mapAsString(Map<K, V> map, String sepItem, String sepKeyval) {

        StringBuffer string = new StringBuffer(128);
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            string.append(entry.getKey());
            string.append(sepKeyval);
            string.append(entry.getValue());
            if (it.hasNext()) {
                string.append(sepItem);
            }
        }
        return string.toString();
    }

    /**
     * Applies white space padding to the left of the given String.<p>
     *
     * @param input the input to pad left
     * @param size the size of the padding
     *
     * @return the input padded to the left
     */
    public static String padLeft(String input, int size) {

        return (new PrintfFormat("%" + size + "s")).sprintf(input);
    }

    /**
     * Applies white space padding to the right of the given String.<p>
     *
     * @param input the input to pad right
     * @param size the size of the padding
     *
     * @return the input padded to the right
     */
    public static String padRight(String input, int size) {

        return (new PrintfFormat("%-" + size + "s")).sprintf(input);
    }

    /**
     * Parses a duration and returns the corresponding number of milliseconds.
     *
     * Durations consist of a space-separated list of components of the form {number}{time unit},
     * for example 1d 5m. The available units are d (days), h (hours), m (months), s (seconds), ms (milliseconds).<p>
     *
     * @param durationStr the duration string
     * @param defaultValue the default value to return in case the pattern does not match
     * @return the corresponding number of milliseconds
     */
    public static final long parseDuration(String durationStr, long defaultValue) {

        durationStr = durationStr.toLowerCase().trim();
        Matcher matcher = DURATION_NUMBER_AND_UNIT_PATTERN.matcher(durationStr);
        long millis = 0;
        boolean matched = false;
        while (matcher.find()) {
            long number = Long.valueOf(matcher.group(1)).longValue();
            String unit = matcher.group(2);
            long multiplier = 0;
            for (int j = 0; j < DURATION_UNTIS.length; j++) {
                if (unit.equals(DURATION_UNTIS[j])) {
                    multiplier = DURATION_MULTIPLIERS[j];
                    break;
                }
            }
            if (multiplier == 0) {
                LOG.warn("parseDuration: Unknown unit " + unit);
            } else {
                matched = true;
            }
            millis += number * multiplier;
        }
        if (!matched) {
            millis = defaultValue;
        }
        return millis;
    }

    /**
     * Reads a stringtemplate group from a stream.
     *
     * This will always return a group (empty if necessary), even if reading it from the stream fails.
     *
     * @param stream the stream to read from
     * @return the string template group
     */
    public static StringTemplateGroup readStringTemplateGroup(InputStream stream) {

        try {
            return new StringTemplateGroup(
                new InputStreamReader(stream, "UTF-8"),
                DefaultTemplateLexer.class,
                new StringTemplateErrorListener() {

                    @SuppressWarnings("synthetic-access")
                    public void error(String arg0, Throwable arg1) {

                        LOG.error(arg0 + ": " + arg1.getMessage(), arg1);
                    }

                    @SuppressWarnings("synthetic-access")
                    public void warning(String arg0) {

                        LOG.warn(arg0);

                    }
                });
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return new StringTemplateGroup("dummy");
        }
    }

    public static java.util.Optional<String> removePrefixPath(String prefix, String path) {

        prefix = CmsFileUtil.addTrailingSeparator(prefix);
        path = CmsFileUtil.addTrailingSeparator(path);
        if (path.startsWith(prefix)) {
            String result = path.substring(prefix.length() - 1);
            if (result.length() > 1) {
                result = CmsFileUtil.removeTrailingSeparator(result);
            }
            return java.util.Optional.of(result);
        } else {
            return java.util.Optional.empty();
        }

    }

    /**
     * Replaces a constant prefix with another string constant in a given text.<p>
     *
     * If the input string does not start with the given prefix, Optional.absent() is returned.<p>
     *
     * @param text the text for which to replace the prefix
     * @param origPrefix the original prefix
     * @param newPrefix the replacement prefix
     * @param ignoreCase if true, upper-/lower case differences will be ignored
     *
     * @return an Optional containing either the string with the replaced prefix, or an absent value if the prefix could not be replaced
     */
    public static Optional<String> replacePrefix(String text, String origPrefix, String newPrefix, boolean ignoreCase) {

        String prefixTestString = ignoreCase ? text.toLowerCase() : text;
        origPrefix = ignoreCase ? origPrefix.toLowerCase() : origPrefix;
        if (prefixTestString.startsWith(origPrefix)) {
            return Optional.of(newPrefix + text.substring(origPrefix.length()));
        } else {
            return Optional.absent();
        }
    }

    /**
     * Splits a String into substrings along the provided char delimiter and returns
     * the result as an Array of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     *
     * @return the Array of splitted Substrings
     */
    public static String[] splitAsArray(String source, char delimiter) {

        List<String> result = splitAsList(source, delimiter);
        return result.toArray(new String[result.size()]);
    }

    /**
     * Splits a String into substrings along the provided String delimiter and returns
     * the result as an Array of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     *
     * @return the Array of splitted Substrings
     */
    public static String[] splitAsArray(String source, String delimiter) {

        List<String> result = splitAsList(source, delimiter);
        return result.toArray(new String[result.size()]);
    }

    /**
     * Splits a String into substrings along the provided char delimiter and returns
     * the result as a List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     *
     * @return the List of splitted Substrings
     */
    public static List<String> splitAsList(String source, char delimiter) {

        return splitAsList(source, delimiter, false);
    }

    /**
     * Splits a String into substrings along the provided char delimiter and returns
     * the result as a List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     * @param trim flag to indicate if leading and trailing white spaces should be omitted
     *
     * @return the List of splitted Substrings
     */
    public static List<String> splitAsList(String source, char delimiter, boolean trim) {

        List<String> result = new ArrayList<String>();
        int i = 0;
        int l = source.length();
        int n = source.indexOf(delimiter);
        while (n != -1) {
            // zero - length items are not seen as tokens at start or end
            if ((i < n) || ((i > 0) && (i < l))) {
                result.add(trim ? source.substring(i, n).trim() : source.substring(i, n));
            }
            i = n + 1;
            n = source.indexOf(delimiter, i);
        }
        // is there a non - empty String to cut from the tail?
        if (n < 0) {
            n = source.length();
        }
        if (i < n) {
            result.add(trim ? source.substring(i).trim() : source.substring(i));
        }
        return result;
    }

    /**
     * Splits a String into substrings along the provided String delimiter and returns
     * the result as List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     *
     * @return the Array of splitted Substrings
     */
    public static List<String> splitAsList(String source, String delimiter) {

        return splitAsList(source, delimiter, false);
    }

    /**
     * Splits a String into substrings along the provided String delimiter and returns
     * the result as List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     * @param trim flag to indicate if leading and trailing white spaces should be omitted
     *
     * @return the Array of splitted Substrings
     */
    public static List<String> splitAsList(String source, String delimiter, boolean trim) {

        int dl = delimiter.length();
        if (dl == 1) {
            // optimize for short strings
            return splitAsList(source, delimiter.charAt(0), trim);
        }

        List<String> result = new ArrayList<String>();
        int i = 0;
        int l = source.length();
        int n = source.indexOf(delimiter);
        while (n != -1) {
            // zero - length items are not seen as tokens at start or end:  ",," is one empty token but not three
            if ((i < n) || ((i > 0) && (i < l))) {
                result.add(trim ? source.substring(i, n).trim() : source.substring(i, n));
            }
            i = n + dl;
            n = source.indexOf(delimiter, i);
        }
        // is there a non - empty String to cut from the tail?
        if (n < 0) {
            n = source.length();
        }
        if (i < n) {
            result.add(trim ? source.substring(i).trim() : source.substring(i));
        }
        return result;
    }

    /**
     * Splits a String into substrings along the provided <code>paramDelim</code> delimiter,
     * then each substring is treat as a key-value pair delimited by <code>keyValDelim</code>.<p>
     *
     * @param source the string to split
     * @param paramDelim the string to delimit each key-value pair
     * @param keyValDelim the string to delimit key and value
     *
     * @return a map of splitted key-value pairs
     */
    public static Map<String, String> splitAsMap(String source, String paramDelim, String keyValDelim) {

        int keyValLen = keyValDelim.length();
        // use LinkedHashMap to preserve the order of items
        Map<String, String> params = new LinkedHashMap<String, String>();
        Iterator<String> itParams = CmsStringUtil.splitAsList(source, paramDelim, true).iterator();
        while (itParams.hasNext()) {
            String param = itParams.next();
            int pos = param.indexOf(keyValDelim);
            String key = param;
            String value = "";
            if (pos > 0) {
                key = param.substring(0, pos);
                if ((pos + keyValLen) < param.length()) {
                    value = param.substring(pos + keyValLen);
                }
            }
            params.put(key, value);
        }
        return params;
    }

    /**
     * Specialized version of splitAsMap used for splitting option lists for widgets.
     *
     * <p>This used the separator characters (':' for key/value, '|' for entries), but also allows escaping of
     * these characters with backslashes ('\'), to enable use of colons/pipes in keys and values. Backslashes themselves
     * can also be escaped.
     *
     * @param optionsStr the string representing the option list
     * @return the options map
     */
    public static Map<String, String> splitOptions(String optionsStr) {

        // state machine with 4 states - combination of whether we are currently either in the key or the value of an entry, and whether the last character was the escape character '\' or not
        // (could be done more simply with java.util.regex.Pattern in the JVM, but we want to keep the implementation the same as in the GWT code, where we don't have it.)
        final int S_KEY = 0;
        final int S_VALUE = 1;
        final int S_KEY_ESC = 2;
        final int S_VALUE_ESC = 3;

        boolean nextEntry = false;
        StringBuilder keyBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();
        Map<String, String> result = new LinkedHashMap<>();
        int length = optionsStr.length();
        int state = S_KEY;
        // one more iteration than the number of characters, to flush the buffers
        for (int i = 0; i < (length + 1); i++) {
            nextEntry = false;
            char ch = 0;
            if (i < length) {
                ch = optionsStr.charAt(i);
            }
            switch (state) {
                case S_KEY:
                    if ((ch == '|') || (ch == 0)) {
                        nextEntry = true;
                        valueBuffer = null; // we have to keep track of whether a value was found to trim the key correctly, and we set the value buffer to null to do this
                        state = S_KEY;
                    } else if (ch == '\\') {
                        state = S_KEY_ESC;
                    } else if (ch == ':') {
                        state = S_VALUE;
                    } else {
                        keyBuffer.append(ch);
                    }
                    break;
                case S_KEY_ESC:
                    if (ch == 0) {
                        nextEntry = true;
                        valueBuffer = null;
                    } else {
                        keyBuffer.append(ch);
                        state = S_KEY;
                    }
                    break;
                case S_VALUE:
                    if ((ch == '|') || (ch == 0)) {
                        nextEntry = true;
                        state = S_KEY;
                    } else if (ch == '\\') {
                        state = S_VALUE_ESC;
                    } else {
                        if (valueBuffer != null) {
                            valueBuffer.append(ch);
                        }
                    }
                    break;
                case S_VALUE_ESC:
                    if (ch == 0) {
                        nextEntry = true;
                        state = S_KEY;
                    } else  {
                        if (valueBuffer != null) {
                            valueBuffer.append(ch);
                        }
                        state = S_VALUE;
                    }
                    break;
                default:
            }
            if (nextEntry) {
                nextEntry = false;
                String key = keyBuffer.toString();
                // trim leading whitespace in key and trailing whitespace in value, for compatibility with splitAsMap
                key = key.replaceFirst("^\\s+", "");
                String value;
                if (valueBuffer != null) {
                    value = valueBuffer.toString();
                    value = value.replaceFirst("\\s+$", "");
                } else {
                    // we just have a key, so we trim it on the right as well to get the same result as splitAsMap
                    value ="";
                    key = key.replaceFirst("\\s+$", "");
                }
                if (key.length() > 0) {
                    result.put(key, value);
                }
                keyBuffer = new StringBuilder();
                valueBuffer = new StringBuilder();
            }
        }
        return result;
    }

    /**
     * Substitutes a pattern in a string using a {@link I_CmsRegexSubstitution}.<p>
     *
     * @param pattern the pattern to substitute
     * @param text the text in which the pattern should be substituted
     * @param sub the substitution handler
     *
     * @return the transformed string
     */
    public static String substitute(Pattern pattern, String text, I_CmsRegexSubstitution sub) {

        if (text == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, sub.substituteMatch(text, matcher));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Replaces a set of <code>searchString</code> and <code>replaceString</code> pairs,
     * given by the <code>substitutions</code> Map parameter.<p>
     *
     * @param source the string to scan
     * @param substitions the map of substitutions
     *
     * @return the substituted String
     *
     * @see #substitute(String, String, String)
     */
    public static String substitute(String source, Map<String, String> substitions) {

        String result = source;
        Iterator<Map.Entry<String, String>> it = substitions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            result = substitute(result, entry.getKey(), entry.getValue().toString());
        }
        return result;
    }

    /**
     * Substitutes <code>searchString</code> in the given source String with <code>replaceString</code>.<p>
     *
     * This is a high-performance implementation which should be used as a replacement for
     * <code>{@link String#replaceAll(java.lang.String, java.lang.String)}</code> in case no
     * regular expression evaluation is required.<p>
     *
     * @param source the content which is scanned
     * @param searchString the String which is searched in content
     * @param replaceString the String which replaces <code>searchString</code>
     *
     * @return the substituted String
     */
    public static String substitute(String source, String searchString, String replaceString) {

        if (source == null) {
            return null;
        }

        if (isEmpty(searchString)) {
            return source;
        }

        if (replaceString == null) {
            replaceString = "";
        }
        int len = source.length();
        int sl = searchString.length();
        int rl = replaceString.length();
        int length;
        if (sl == rl) {
            length = len;
        } else {
            int c = 0;
            int s = 0;
            int e;
            while ((e = source.indexOf(searchString, s)) != -1) {
                c++;
                s = e + sl;
            }
            if (c == 0) {
                return source;
            }
            length = len - (c * (sl - rl));
        }

        int s = 0;
        int e = source.indexOf(searchString, s);
        if (e == -1) {
            return source;
        }
        StringBuffer sb = new StringBuffer(length);
        while (e != -1) {
            sb.append(source.substring(s, e));
            sb.append(replaceString);
            s = e + sl;
            e = source.indexOf(searchString, s);
        }
        e = len;
        sb.append(source.substring(s, e));
        return sb.toString();
    }

    /**
     * Substitutes the OpenCms context path (e.g. /opencms/opencms/) in a HTML page with a
     * special variable so that the content also runs if the context path of the server changes.<p>
     *
     * @param htmlContent the HTML to replace the context path in
     * @param context the context path of the server
     *
     * @return the HTML with the replaced context path
     */
    public static String substituteContextPath(String htmlContent, String context) {

        if (m_contextSearch == null) {
            m_contextSearch = "([^\\w/])" + context;
            m_contextReplace = "$1" + CmsStringUtil.escapePattern(CmsStringUtil.MACRO_OPENCMS_CONTEXT) + "/";
        }
        return substitutePerl(htmlContent, m_contextSearch, m_contextReplace, "g");
    }

    /**
     * Substitutes searchString in content with replaceItem.<p>
     *
     * @param content the content which is scanned
     * @param searchString the String which is searched in content
     * @param replaceItem the new String which replaces searchString
     * @param occurences must be a "g" if all occurrences of searchString shall be replaced
     *
     * @return String the substituted String
     */
    public static String substitutePerl(String content, String searchString, String replaceItem, String occurences) {

        String translationRule = "s#" + searchString + "#" + replaceItem + "#" + occurences;
        Perl5Util perlUtil = new Perl5Util();
        try {
            return perlUtil.substitute(translationRule, content);
        } catch (MalformedPerl5PatternException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_MALFORMED_TRANSLATION_RULE_1, translationRule),
                    e);
            }
        }
        return content;
    }

    /**
     * Returns the java String literal for the given String. <p>
     *
     * This is the form of the String that had to be written into source code
     * using the unicode escape sequence for special characters. <p>
     *
     * Example: "&Auml" would be transformed to "\\u00C4".<p>
     *
     * @param s a string that may contain non-ascii characters
     *
     * @return the java unicode escaped string Literal of the given input string
     */
    public static String toUnicodeLiteral(String s) {

        StringBuffer result = new StringBuffer();
        char[] carr = s.toCharArray();

        String unicode;
        for (int i = 0; i < carr.length; i++) {
            result.append("\\u");
            // append leading zeros
            unicode = Integer.toHexString(carr[i]).toUpperCase();
            for (int j = 4 - unicode.length(); j > 0; j--) {
                result.append("0");
            }
            result.append(unicode);
        }
        return result.toString();
    }

    /**
     * This method transformes a string which matched a format with one or more place holders into another format. The
     * other format also includes the same number of place holders. Place holders start with
     * {@link org.opencms.util.CmsStringUtil#PLACEHOLDER_START} and end with {@link org.opencms.util.CmsStringUtil#PLACEHOLDER_END}.<p>
     *
     * @param oldFormat the original format
     * @param newFormat the new format
     * @param value the value which matched the original format and which shall be transformed into the new format
     *
     * @return the new value with the filled place holder with the information in the parameter value
     */
    public static String transformValues(String oldFormat, String newFormat, String value) {

        if (!oldFormat.contains(CmsStringUtil.PLACEHOLDER_START)
            || !oldFormat.contains(CmsStringUtil.PLACEHOLDER_END)
            || !newFormat.contains(CmsStringUtil.PLACEHOLDER_START)
            || !newFormat.contains(CmsStringUtil.PLACEHOLDER_END)) {
            // no place holders are set in correct format
            // that is why there is nothing to calculate and the value is the new format
            return newFormat;
        }
        //initialize the arrays with the values where the place holders starts
        ArrayList<Integer> oldValues = new ArrayList<Integer>();
        ArrayList<Integer> newValues = new ArrayList<Integer>();

        // count the number of placeholders
        // for example these are three pairs:
        // old format: {.*}<b>{.*}</b>{.*}
        // new format: {}<strong>{}</strong>{}
        // get the number of place holders in the old format
        int oldNumber = 0;
        try {
            int counter = 0;
            Pattern pattern = Pattern.compile("\\{\\.\\*\\}");
            Matcher matcher = pattern.matcher(oldFormat);
            // get the number of matches
            while (matcher.find()) {
                counter += 1;
            }
            oldValues = new ArrayList<Integer>(counter);
            matcher = pattern.matcher(oldFormat);
            while (matcher.find()) {
                int start = matcher.start() + 1;
                oldValues.add(oldNumber, Integer.valueOf(start));
                oldNumber += 1;
            }
        } catch (PatternSyntaxException e) {
            // do nothing
        }
        // get the number of place holders in the new format
        int newNumber = 0;
        try {
            int counter = 0;
            Pattern pattern = Pattern.compile("\\{\\}");
            Matcher matcher = pattern.matcher(newFormat);
            // get the number of matches
            while (matcher.find()) {
                counter += 1;
            }
            newValues = new ArrayList<Integer>(counter);
            matcher = pattern.matcher(newFormat);
            while (matcher.find()) {
                int start = matcher.start() + 1;
                newValues.add(newNumber, Integer.valueOf(start));
                newNumber += 1;
            }
        } catch (PatternSyntaxException e) {
            // do nothing
        }
        // prove the numbers of place holders
        if (oldNumber != newNumber) {
            // not the same number of place holders in the old and in the new format
            return newFormat;
        }

        // initialize the arrays with the values between the place holders
        ArrayList<String> oldBetween = new ArrayList<String>(oldNumber + 1);
        ArrayList<String> newBetween = new ArrayList<String>(newNumber + 1);

        // get the values between the place holders for the old format
        // for this example with oldFormat: {.*}<b>{.*}</b>{.*}
        // this array is that:
        // ---------
        // | empty |
        // ---------
        // | <b>   |
        // |--------
        // | </b>  |
        // |--------
        // | empty |
        // |--------
        int counter = 0;
        Iterator<Integer> iter = oldValues.iterator();
        while (iter.hasNext()) {
            int start = iter.next().intValue();
            if (counter == 0) {
                // the first entry
                if (start == 1) {
                    // the first place holder starts at the beginning of the old format
                    // for example: {.*}<b>...
                    oldBetween.add(counter, "");
                } else {
                    // the first place holder starts NOT at the beginning of the old format
                    // for example: <a>{.*}<b>...
                    String part = oldFormat.substring(0, start - 1);
                    oldBetween.add(counter, part);
                }
            } else {
                // the entries between the first and the last entry
                int lastStart = oldValues.get(counter - 1).intValue();
                String part = oldFormat.substring(lastStart + 3, start - 1);
                oldBetween.add(counter, part);
            }
            counter += 1;
        }
        // the last element
        int lastElstart = oldValues.get(counter - 1).intValue();
        if ((lastElstart + 2) == (oldFormat.length() - 1)) {
            // the last place holder ends at the end of the old format
            // for example: ...</b>{.*}
            oldBetween.add(counter, "");
        } else {
            // the last place holder ends NOT at the end of the old format
            // for example: ...</b>{.*}</a>
            String part = oldFormat.substring(lastElstart + 3);
            oldBetween.add(counter, part);
        }

        // get the values between the place holders for the new format
        // for this example with newFormat: {}<strong>{}</strong>{}
        // this array is that:
        // ------------|
        // | empty     |
        // ------------|
        // | <strong>  |
        // |-----------|
        // | </strong> |
        // |-----------|
        // | empty     |
        // |-----------|
        counter = 0;
        iter = newValues.iterator();
        while (iter.hasNext()) {
            int start = iter.next().intValue();
            if (counter == 0) {
                // the first entry
                if (start == 1) {
                    // the first place holder starts at the beginning of the new format
                    // for example: {.*}<b>...
                    newBetween.add(counter, "");
                } else {
                    // the first place holder starts NOT at the beginning of the new format
                    // for example: <a>{.*}<b>...
                    String part = newFormat.substring(0, start - 1);
                    newBetween.add(counter, part);
                }
            } else {
                // the entries between the first and the last entry
                int lastStart = newValues.get(counter - 1).intValue();
                String part = newFormat.substring(lastStart + 1, start - 1);
                newBetween.add(counter, part);
            }
            counter += 1;
        }
        // the last element
        lastElstart = newValues.get(counter - 1).intValue();
        if ((lastElstart + 2) == (newFormat.length() - 1)) {
            // the last place holder ends at the end of the old format
            // for example: ...</b>{.*}
            newBetween.add(counter, "");
        } else {
            // the last place holder ends NOT at the end of the old format
            // for example: ...</b>{.*}</a>
            String part = newFormat.substring(lastElstart + 1);
            newBetween.add(counter, part);
        }

        // get the values in the place holders
        // for the example with:
        //   oldFormat: {.*}<b>{.*}</b>{.*}
        //   newFormat: {}<strong>{}</strong>{}
        //   value: abc<b>def</b>ghi
        // it is used the array with the old values between the place holders to get the content in the place holders
        // this result array is that:
        // ------|
        // | abc |
        // ------|
        // | def |
        // |-----|
        // | ghi |
        // |-----|
        ArrayList<String> placeHolders = new ArrayList<String>(oldNumber);
        String tmpValue = value;
        // loop over all rows with the old values between the place holders and take the values between them in the
        // current property value
        for (int placeCounter = 0; placeCounter < (oldBetween.size() - 1); placeCounter++) {
            // get the two next values with the old values between the place holders
            String content = oldBetween.get(placeCounter);
            String nextContent = oldBetween.get(placeCounter + 1);
            // check the position of the first of the next values in the current property value
            int contPos = 0;
            int nextContPos = 0;
            if ((placeCounter == 0) && CmsStringUtil.isEmpty(content)) {
                // the first value in the values between the place holders is empty
                // for example: {.*}<p>...
                contPos = 0;
            } else {
                // the first value in the values between the place holders is NOT empty
                // for example: bla{.*}<p>...
                contPos = tmpValue.indexOf(content);
            }
            // check the position of the second of the next values in the current property value
            if (((placeCounter + 1) == (oldBetween.size() - 1)) && CmsStringUtil.isEmpty(nextContent)) {
                // the last value in the values between the place holders is empty
                // for example: ...<p>{.*}
                nextContPos = tmpValue.length();
            } else {
                // the last value in the values between the place holders is NOT empty
                // for example: ...<p>{.*}bla
                nextContPos = tmpValue.indexOf(nextContent);
            }
            // every value must match the current value
            if ((contPos < 0) || (nextContPos < 0)) {
                return value;
            }
            // get the content of the current place holder
            String placeContent = tmpValue.substring(contPos + content.length(), nextContPos);
            placeHolders.add(placeCounter, placeContent);
            // cut off the currently visited part of the value
            tmpValue = tmpValue.substring(nextContPos);
        }

        // build the new format
        // with following vectors from above:
        // old values between the place holders:
        // ---------
        // | empty | (old.1)
        // ---------
        // | <b>   | (old.2)
        // |--------
        // | </b>  | (old.3)
        // |--------
        // | empty | (old.4)
        // |--------
        //
        // new values between the place holders:
        // ------------|
        // | empty     | (new.1)
        // ------------|
        // | <strong>  | (new.2)
        // |-----------|
        // | </strong> | (new.3)
        // |-----------|
        // | empty     | (new.4)
        // |-----------|
        //
        // content of the place holders:
        // ------|
        // | abc | (place.1)
        // ------|
        // | def | (place.2)
        // |-----|
        // | ghi | (place.3)
        // |-----|
        //
        // the result is calculated in that way:
        // new.1 + place.1 + new.2 + place.2 + new.3 + place.3 + new.4
        String newValue = "";
        // take the values between the place holders and add the content of the place holders
        for (int buildCounter = 0; buildCounter < newNumber; buildCounter++) {
            newValue = newValue + newBetween.get(buildCounter) + placeHolders.get(buildCounter);
        }
        newValue = newValue + newBetween.get(newNumber);
        // return the changed value
        return newValue;
    }

    /**
     * Translates all consecutive sequences of non-slash characters in a path using the given resource translator.
     *
     * @param translator the resource translator
     * @param path the path to translate
     * @return the translated path
     */
    public static String translatePathComponents(CmsResourceTranslator translator, String path) {

        String result = substitute(NOT_SLASHES, path, (text, matcher) -> {
            return translator.translateResource(matcher.group());
        });
        return result;
    }

    /**
     * Returns a substring of the source, which is at most length characters long.<p>
     *
     * This is the same as calling {@link #trimToSize(String, int, String)} with the
     * parameters <code>(source, length, " ...")</code>.<p>
     *
     * @param source the string to trim
     * @param length the maximum length of the string to be returned
     *
     * @return a substring of the source, which is at most length characters long
     */
    public static String trimToSize(String source, int length) {

        return trimToSize(source, length, length, " ...");
    }

    /**
     * Returns a substring of the source, which is at most length characters long, cut
     * in the last <code>area</code> chars in the source at a sentence ending char or whitespace.<p>
     *
     * If a char is cut, the given <code>suffix</code> is appended to the result.<p>
     *
     * @param source the string to trim
     * @param length the maximum length of the string to be returned
     * @param area the area at the end of the string in which to find a sentence ender or whitespace
     * @param suffix the suffix to append in case the String was trimmed
     *
     * @return a substring of the source, which is at most length characters long
     */
    public static String trimToSize(String source, int length, int area, String suffix) {

        if ((source == null) || (source.length() <= length)) {
            // no operation is required
            return source;
        }
        if (CmsStringUtil.isEmpty(suffix)) {
            // we need an empty suffix
            suffix = "";
        }
        // must remove the length from the after sequence chars since these are always added in the end
        int modLength = length - suffix.length();
        if (modLength <= 0) {
            // we are to short, return beginning of the suffix
            return suffix.substring(0, length);
        }
        int modArea = area + suffix.length();
        if ((modArea > modLength) || (modArea < 0)) {
            // area must not be longer then max length
            modArea = modLength;
        }

        // first reduce the String to the maximum allowed length
        String findPointSource = source.substring(modLength - modArea, modLength);

        String result;
        // try to find an "sentence ending" char in the text
        int pos = lastIndexOf(findPointSource, SENTENCE_ENDING_CHARS);
        if (pos >= 0) {
            // found a sentence ender in the lookup area, keep the sentence ender
            result = source.substring(0, (modLength - modArea) + pos + 1) + suffix;
        } else {
            // no sentence ender was found, try to find a whitespace
            pos = lastWhitespaceIn(findPointSource);
            if (pos >= 0) {
                // found a whitespace, don't keep the whitespace
                result = source.substring(0, (modLength - modArea) + pos) + suffix;
            } else {
                // not even a whitespace was found, just cut away what's to long
                result = source.substring(0, modLength) + suffix;
            }
        }

        return result;
    }

    /**
     * Returns a substring of the source, which is at most length characters long.<p>
     *
     * If a char is cut, the given <code>suffix</code> is appended to the result.<p>
     *
     * This is almost the same as calling {@link #trimToSize(String, int, int, String)} with the
     * parameters <code>(source, length, length*, suffix)</code>. If <code>length</code>
     * if larger then 100, then <code>length* = length / 2</code>,
     * otherwise <code>length* = length</code>.<p>
     *
     * @param source the string to trim
     * @param length the maximum length of the string to be returned
     * @param suffix the suffix to append in case the String was trimmed
     *
     * @return a substring of the source, which is at most length characters long
     */
    public static String trimToSize(String source, int length, String suffix) {

        int area = (length > 100) ? length / 2 : length;
        return trimToSize(source, length, area, suffix);
    }

    /**
     * Validates a value against a regular expression.<p>
     *
     * @param value the value to test
     * @param regex the regular expression
     * @param allowEmpty if an empty value is allowed
     *
     * @return <code>true</code> if the value satisfies the validation
     */
    public static boolean validateRegex(String value, String regex, boolean allowEmpty) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            return allowEmpty;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}