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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import com.google.gwt.i18n.client.Dictionary;

/**
 * Reads localized resource Strings from a <code>java.util.ResourceBundle</code>
 * and provides convenience methods to access the Strings from a template.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.i18n.CmsMessages
 */
public class CmsMessages {

    /** The suffix of a "short" localized key name. */
    public static final String KEY_SHORT_SUFFIX = ".short";

    /** Prefix / Suffix for unknown keys. */
    public static final String UNKNOWN_KEY_EXTENSION = "???";

    /** Cached dictionaries. */
    private static Map<String, Dictionary> m_dictionaries;

    /** The name of the resource bundle. */
    private String m_bundleName;

    /** The current dictionary. */
    private Dictionary m_dictionary;

    /**
     * Default constructor.<p>
     *
     * @param bundleName the localized bundle name
     */
    public CmsMessages(String bundleName) {

        if (m_dictionaries == null) {
            m_dictionaries = new HashMap<String, Dictionary>();
        }
        m_bundleName = bundleName;
        m_dictionary = m_dictionaries.get(m_bundleName);
        if (m_dictionary == null) {
            m_dictionary = Dictionary.getDictionary(m_bundleName.replace('.', '_'));
            m_dictionaries.put(m_bundleName, m_dictionary);
        }
    }

    /**
     * Helper method for formatting message parameters.<p>
     *
     * @param result the raw message containing placeholders like {0}
     * @param args the parameters to insert into the placeholders
     *
     * @return the formatted message
     */
    public static String formatMessage(String result, Object... args) {

        // key was found in the bundle - create and apply the formatter
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Date) {
                Date date = (Date)args[i];
                result = result.replace(getRegEx(i), CmsDateTimeUtil.getDateTime(date, CmsDateTimeUtil.Format.MEDIUM));
                result = result.replace(
                    getRegEx(i, "time"),
                    CmsDateTimeUtil.getTime(date, CmsDateTimeUtil.Format.MEDIUM));
                result = result.replace(
                    getRegEx(i, "time", "short"),
                    CmsDateTimeUtil.getTime(date, CmsDateTimeUtil.Format.SHORT));
                result = result.replace(
                    getRegEx(i, "time", "medium"),
                    CmsDateTimeUtil.getTime(date, CmsDateTimeUtil.Format.MEDIUM));
                result = result.replace(
                    getRegEx(i, "time", "long"),
                    CmsDateTimeUtil.getTime(date, CmsDateTimeUtil.Format.LONG));
                result = result.replace(
                    getRegEx(i, "time", "full"),
                    CmsDateTimeUtil.getTime(date, CmsDateTimeUtil.Format.FULL));
                result = result.replace(
                    getRegEx(i, "date"),
                    CmsDateTimeUtil.getDate(date, CmsDateTimeUtil.Format.MEDIUM));
                result = result.replace(
                    getRegEx(i, "date", "short"),
                    CmsDateTimeUtil.getDate(date, CmsDateTimeUtil.Format.SHORT));
                result = result.replace(
                    getRegEx(i, "date", "medium"),
                    CmsDateTimeUtil.getDate(date, CmsDateTimeUtil.Format.MEDIUM));
                result = result.replace(
                    getRegEx(i, "date", "long"),
                    CmsDateTimeUtil.getDate(date, CmsDateTimeUtil.Format.LONG));
                result = result.replace(
                    getRegEx(i, "date", "full"),
                    CmsDateTimeUtil.getDate(date, CmsDateTimeUtil.Format.FULL));
            } else {
                result = result.replace(getRegEx(i), String.valueOf(args[i]));
            }
        }
        return result;
    }

    /**
     * Formats an unknown key.<p>
     *
     * @param keyName the key to format
     *
     * @return the formatted unknown key
     *
     * @see #isUnknownKey(String)
     */
    public static String formatUnknownKey(String keyName) {

        StringBuffer buf = new StringBuffer(64);
        buf.append(UNKNOWN_KEY_EXTENSION);
        buf.append(" ");
        buf.append(keyName);
        buf.append(" ");
        buf.append(UNKNOWN_KEY_EXTENSION);
        return buf.toString();
    }

    /**
     * Returns <code>true</code> if the provided value matches the scheme
     * <code>"??? " + keyName + " ???"</code>, that is the value appears to be an unknown key.<p>
     *
     * Also returns <code>true</code> if the given value is <code>null</code>.<p>
     *
     * @param value the value to check
     *
     * @return true if the value is matches the scheme for unknown keys
     *
     * @see #formatUnknownKey(String)
     */
    public static boolean isUnknownKey(String value) {

        return (value == null) || (value.startsWith(UNKNOWN_KEY_EXTENSION));
    }

    /**
     * Returns a regular expression for replacement.<p>
     *
     * @param position the parameter number
     * @param options the optional options
     *
     * @return the regular expression for replacement
     */
    private static String getRegEx(int position, String... options) {

        String value = "" + position;
        for (int i = 0; i < options.length; i++) {
            value += "," + options[i];
        }
        return "{" + value + "}";
    }

    /**
     * Returns the localized message bundle wrapped in this instance.<p>
     *
     * Mainly for API compatibility with the core localization methods.<p>
     *
     * @return the localized message bundle wrapped in this instance
     */
    public CmsMessages getBundle() {

        return this;
    }

    /**
     * Returns the name of the resource bundle.<p>
     *
     * @return the name of the resource bundle
     */
    public String getBundleName() {

        return m_bundleName;
    }

    /**
     * Returns a formated date String from a Date value,
     * the format being {@link CmsDateTimeUtil.Format#SHORT} and the locale
     * based on this instance.<p>
     *
     * @param date the Date object to format as String
     *
     * @return the formatted date
     */
    public String getDate(Date date) {

        return CmsDateTimeUtil.getDate(date, CmsDateTimeUtil.Format.SHORT);
    }

    /**
     * Returns a formated date String from a Date value,
     * the formatting based on the provided option and the locale
     * based on this instance.<p>
     *
     * @param date the Date object to format as String
     * @param format the format to use, see {@link CmsDateTimeUtil.Format} for possible values
     *
     * @return the formatted date
     */
    public String getDate(Date date, CmsDateTimeUtil.Format format) {

        return CmsDateTimeUtil.getDate(date, format);
    }

    /**
     * Returns a formated date String from a timestamp value,
     * the format being {@link CmsDateTimeUtil.Format#SHORT} and the locale
     * based on this instance.<p>
     *
     * @param time the time value to format as date
     *
     * @return the formatted date
     */
    public String getDate(long time) {

        return CmsDateTimeUtil.getDate(new Date(time), CmsDateTimeUtil.Format.SHORT);
    }

    /**
     * Returns a formated date and time String from a Date value,
     * the format being {@link CmsDateTimeUtil.Format#SHORT} and the locale
     * based on this instance.<p>
     *
     * @param date the Date object to format as String
     * @return the formatted date and time
     */
    public String getDateTime(Date date) {

        return CmsDateTimeUtil.getDateTime(date, CmsDateTimeUtil.Format.SHORT);
    }

    /**
     * Returns a formated date and time String from a Date value,
     * the formatting based on the provided option and the locale
     * based on this instance.<p>
     *
     * @param date the Date object to format as String
     * @param format the format to use, see {@link CmsDateTimeUtil.Format} for possible values
     * @return the formatted date and time
     */
    public String getDateTime(Date date, CmsDateTimeUtil.Format format) {

        return CmsDateTimeUtil.getDateTime(date, format);
    }

    /**
     * Returns a formated date and time String from a timestamp value,
     * the format being {@link CmsDateTimeUtil.Format#SHORT} and the locale
     * based on this instance.<p>
     *
     * @param time the time value to format as date
     * @return the formatted date and time
     */
    public String getDateTime(long time) {

        return CmsDateTimeUtil.getDateTime(new Date(time), CmsDateTimeUtil.Format.SHORT);
    }

    /**
     * Returns the internal dictionary.<p>
     *
     * @return the internal dictionary
     */
    public Dictionary getDictionary() {

        return m_dictionary;
    }

    /**
     * Returns the localized resource string for a given message key.<p>
     *
     * If the key was not found in the bundle, the return value is
     * <code>"??? " + keyName + " ???"</code>. This will also be returned
     * if the bundle was not properly initialized first.
     *
     * @param keyName the key for the desired string
     *
     * @return the resource string for the given key
     */
    public String key(String keyName) {

        return key(keyName, false);
    }

    /**
     * Returns the localized resource string for a given message key.<p>
     *
     * If the key was not found in the bundle, the return value
     * depends on the setting of the allowNull parameter. If set to false,
     * the return value is always a String in the format
     * <code>"??? " + keyName + " ???"</code>.
     * If set to true, null is returned if the key is not found.
     * This will also be returned
     * if the bundle was not properly initialized first.
     *
     * @param keyName the key for the desired string
     * @param allowNull if true, 'null' is an allowed return value
     *
     * @return the resource string for the given key
     */
    public String key(String keyName, boolean allowNull) {

        try {
            if (m_dictionary != null) {
                return m_dictionary.get(keyName);
            }
        } catch (@SuppressWarnings("unused") MissingResourceException e) {
            // not found, return warning
            if (allowNull) {
                return null;
            }
        }
        return formatUnknownKey(keyName);
    }

    /**
     * Returns the selected localized message for the initialized resource bundle and locale.<p>
     *
     * If the key was found in the bundle, it will be formatted using
     * a <code>{@link java.text.MessageFormat}</code> using the provided parameters.<p>
     *
     * If the key was not found in the bundle, the return value is
     * <code>"??? " + keyName + " ???"</code>. This will also be returned
     * if the bundle was not properly initialized first.
     *
     * @param key the message key
     * @param args the message arguments
     *
     * @return the selected localized message for the initialized resource bundle and locale
     */
    public String key(String key, Object... args) {

        if ((args == null) || (args.length == 0)) {
            // no parameters available, use simple key method
            return key(key);
        }

        String result = key(key, true);
        if (result == null) {
            // key was not found
            result = formatUnknownKey(key);
        } else {
            result = formatMessage(result, args);
        }
        // return the result
        return result;
    }

    /**
     * Returns the localized resource string for a given message key.<p>
     *
     * If the key was not found in the bundle, the provided default value
     * is returned.<p>
     *
     * @param keyName the key for the desired string
     * @param defaultValue the default value in case the key does not exist in the bundle
     *
     * @return the resource string for the given key it it exists, or the given default if not
     */
    public String keyDefault(String keyName, String defaultValue) {

        String result = key(keyName, true);
        return (result == null) ? defaultValue : result;
    }

    /**
     * Returns the localized resource string for a given message key,
     * treating all values appended with "|" as replacement parameters.<p>
     *
     * If the key was found in the bundle, it will be formatted using
     * a <code>{@link java.text.MessageFormat}</code> using the provided parameters.
     * The parameters have to be appended to the key separated by a "|".
     * For example, the keyName <code>error.message|First|Second</code>
     * would use the key <code>error.message</code> with the parameters
     * <code>First</code> and <code>Second</code>. This would be the same as calling
     * <code>{@link CmsMessages#key(String, Object[])}</code>.<p>
     *
     * If no parameters are appended with "|", this is the same as calling
     * <code>{@link CmsMessages#key(String)}</code>.<p>
     *
     * If the key was not found in the bundle, the return value is
     * <code>"??? " + keyName + " ???"</code>. This will also be returned
     * if the bundle was not properly initialized first.
     *
     * @param keyName the key for the desired string, optionally containing parameters appended with a "|"
     *
     * @return the resource string for the given key
     *
     * @see #key(String, Object[])
     * @see #key(String)
     */
    public String keyWithParams(String keyName) {

        if (keyName.indexOf('|') == -1) {
            // no separator found, key has no parameters
            return key(keyName, false);
        } else {
            // this key contains parameters
            String[] values = CmsStringUtil.splitAsArray(keyName, "|");
            String cutKeyName = values[0];
            String[] params = new String[values.length - 1];
            System.arraycopy(values, 1, params, 0, params.length);
            return key(cutKeyName, (Object[])params);
        }
    }
}