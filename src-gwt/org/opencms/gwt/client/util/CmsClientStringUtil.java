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

package org.opencms.gwt.client.util;

import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Random;

/**
 * Additional string related helper methods.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.util.CmsStringUtil
 */
public final class CmsClientStringUtil {

    /**
     * Prevent instantiation.<p>
     */
    private CmsClientStringUtil() {

        // empty
    }

    /**
     * Returns the exception message.<p>
     *
     * @param t the exception to get the message for
     *
     * @return the exception message
     */
    public static String getMessage(Throwable t) {

        String message = t.getLocalizedMessage();
        if (message == null) {
            message = t.getMessage();
        }
        if (message == null) {
            message = t.getClass().getName();
        }
        return message;
    }

    /**
     * Returns the stack trace of the Throwable as a string.<p>
     *
     * @param t the Throwable for which the stack trace should be returned
     * @param separator the separator between the lines of the stack trace
     *
     * @return a string representing a stack trace
     */
    public static String getStackTrace(Throwable t, String separator) {

        Throwable cause = t;
        String result = "";
        while (cause != null) {
            result += getStackTraceAsString(cause.getStackTrace(), separator);
            cause = cause.getCause();
        }
        return result;
    }

    /**
     * Returns the stack trace as a string.<p>
     *
     * @param trace the stack trace
     * @param separator the separator between the lines of the stack trace
     *
     * @return a string representing a stack trace
     */
    public static String getStackTraceAsString(StackTraceElement[] trace, String separator) {

        String result = "";
        for (StackTraceElement elem : trace) {
            result += elem.toString();
            result += separator;
        }
        return result;
    }

    /**
     * The parseFloat() function parses a string and returns a float.<p>
     *
     * Only the first number in the string is returned. Leading and trailing spaces are allowed.
     *
     * @param str the string to be parsed
     *
     * @return the parsed number
     */
    public static native double parseFloat(String str) /*-{
        var ret = parseFloat(str, 10);
        if (isNaN(ret)) {
            return 0;
        }
        return ret;
    }-*/;

    /**
     * The parseInt() function parses a string and returns an integer.<p>
     *
     * Only the first number in the string is returned. Leading and trailing spaces are allowed.
     * If the first character cannot be converted to a number, parseInt() returns zero.<p>
     *
     * @param str the string to be parsed
     *
     * @return the parsed number
     */
    public static native int parseInt(String str) /*-{
        var ret = parseInt(str, 10);
        if (isNaN(ret)) {
            return 0;
        }
        return ret;
    }-*/;

    /**
     * Pushes a String into a javascript array.<p>
     *
     * @param array the array to push the String into
     * @param s the String to push into the array
     */
    public static native void pushArray(JavaScriptObject array, String s) /*-{
        array.push(s);
    }-*/;

    /**
     * Generates a purely random uuid.<p>
     *
     * @return the generated uuid
     */
    public static String randomUUID() {

        String base = CmsUUID.getNullUUID().toString();
        String hexDigits = "0123456789abcdef";
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < base.length(); i++) {
            char ch = base.charAt(i);
            if (ch == '-') {
                result.append(ch);
            } else if (ch == '0') {
                result.append(hexDigits.charAt(Random.nextInt(16)));
            }
        }
        return result.toString();
    }

    /**
     * Shortens the string to the given maximum length.<p>
     *
     * Will include HTML entity ellipses replacing the cut off text.<p>
     *
     * @param text the string to shorten
     * @param maxLength the maximum length
     *
     * @return the shortened string
     */
    public static String shortenString(String text, int maxLength) {

        if (text.length() <= maxLength) {
            return text;
        }
        String newText = text.substring(0, maxLength - 1);
        if (text.startsWith("/")) {
            // file name?
            newText = CmsStringUtil.formatResourceName(text, maxLength);
        } else if (maxLength > 2) {
            // enough space for ellipsis?
            newText += CmsDomUtil.Entity.hellip.html();
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(newText)) {
            // if empty, it could break the layout
            newText = CmsDomUtil.Entity.nbsp.html();
        }
        return newText;
    }

}