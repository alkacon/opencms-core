/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsMessages.java,v $
 * Date   : $Date: 2005/04/11 17:44:39 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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

import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Reads localized resource Strings from a <code>java.util.ResourceBundle</code> 
 * and provides convenience methods to access the Strings from a template.<p>
 * 
 * This class is frequently used from JSP templates. Because of that, throwing of 
 * exceptions related to the access of the resource bundle are suppressed
 * so that a template always execute. The class provides an {@link #isInitialized()} method
 * that can be checked to see if the instance was properly initialized.
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.11 $
 * 
 * @since 5.0 beta 2
 */
public class CmsMessages {

    /** The suffix of a "short" localized key name. */
    public static final String C_KEY_SHORT_SUFFIX = ".short";

    /** Prefix / Suffix for unknown keys. */
    public static final String C_UNKNOWN_KEY_EXTENSION = "???";

    /** The resource bundle this message object was initialized with. */
    protected ResourceBundle m_bundle;

    /** The locale to use for looking up the messages from the bundle. */
    protected Locale m_locale;

    /**
     * Constructor for the messages with an initialized <code>java.util.Locale</code>.
     * 
     * @param baseName the base ResourceBundle name
     * @param locale the m_locale to use, eg. "de", "en" etc.
     */
    public CmsMessages(String baseName, Locale locale) {

        try {
            m_locale = locale;
            m_bundle = ResourceBundle.getBundle(baseName, m_locale);
        } catch (MissingResourceException e) {
            m_bundle = null;
        }
    }

    /**
     * Constructor for the messages with a language string.<p>
     * 
     * The <code>language</code> is a 2 letter language ISO code, e.g. <code>"EN"</code>.<p>
     * 
     * The Locale for the messages will be created like this:<br>
     * <code>new Locale(language, "", "")</code>.<p>
     * 
     * @param baseName the base ResourceBundle name
     * @param language ISO language indentificator for the m_locale of the bundle
     */
    public CmsMessages(String baseName, String language) {

        this(baseName, language, "", "");
    }

    /**
     * Constructor for the messages with language and country code strings.<p>
     * 
     * The <code>language</code> is a 2 letter language ISO code, e.g. <code>"EN"</code>.
     * The <code>country</code> is a 2 letter country ISO code, e.g. <code>"us"</code>.<p>
     * 
     * The Locale for the messages will be created like this:<br>
     * <code>new Locale(language, country, "")</code>.
     * 
     * @param baseName the base ResourceBundle name
     * @param language ISO language indentificator for the m_locale of the bundle
     * @param country ISO 2 letter country code for the m_locale of the bundle 
     */
    public CmsMessages(String baseName, String language, String country) {

        this(baseName, language, country, "");
    }

    /**
     * Constructor for the messages with language, country code and variant strings.<p>
     * 
     * The <code>language</code> is a 2 letter language ISO code, e.g. <code>"EN"</code>.
     * The <code>country</code> is a 2 letter country ISO code, e.g. <code>"us"</code>.
     * The <code>variant</code> is a vendor or browser-specific code, e.g. <code>"POSIX"</code>.<p>
     * 
     * The Locale for the messages will be created like this:<br>
     * <code>new Locale(language, country, variant)</code>.
     * 
     * @param baseName the base ResourceBundle name
     * @param language language indentificator for the m_locale of the bundle
     * @param country 2 letter country code for the m_locale of the bundle 
     * @param variant a vendor or browser-specific variant code
     */
    public CmsMessages(String baseName, String language, String country, String variant) {

        this(baseName, new Locale(language, country, variant));
    }

    /**
     * Formats an unknown key.<p>
     * 
     * @param keyName the key to format
     * @return the formatted unknown key
     * 
     * @see #isUnknownKey(String)
     */
    public static String formatUnknownKey(String keyName) {

        StringBuffer buf = new StringBuffer(64);
        buf.append(C_UNKNOWN_KEY_EXTENSION);
        buf.append(" ");
        buf.append(keyName);
        buf.append(" ");
        buf.append(C_UNKNOWN_KEY_EXTENSION);
        return buf.toString();
    }

    /**
     * Returns <code>true</code> if the provided value matches the scheme 
     * <code>"??? " + keyName + " ???"</code>, that is the value appears to be an unknown key.<p>
     * 
     * Also returns <code>true</code> if the given value is <code>null</code>.<p>
     * 
     * @param value the value to check
     * @return true if the value is matches the scheme for unknown keys
     * 
     * @see #formatUnknownKey(String)
     */
    public static boolean isUnknownKey(String value) {

        return (value == null) || (value.startsWith(C_UNKNOWN_KEY_EXTENSION));
    }

    /**
     * Returns a formated date String from a Date value,
     * the format being {@link DateFormat#SHORT} and the locale
     * based on this instance.<p>
     * 
     * @param date the Date object to format as String
     * @return the formatted date 
     */
    public String getDate(Date date) {

        return CmsDateUtil.getDate(date, DateFormat.SHORT, m_locale);
    }

    /**
     * Returns a formated date String from a Date value,
     * the formatting based on the provided option and the locale
     * based on this instance.<p>
     * 
     * @param date the Date object to format as String
     * @param format the format to use, see {@link CmsMessages} for possible values
     * @return the formatted date 
     */
    public String getDate(Date date, int format) {

        return CmsDateUtil.getDate(date, format, m_locale);
    }

    /**
     * Returns a formated date String from a timestamp value,
     * the format being {@link DateFormat#SHORT} and the locale
     * based on this instance.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date 
     */
    public String getDate(long time) {

        return CmsDateUtil.getDate(new Date(time), DateFormat.SHORT, m_locale);
    }

    /**
     * Returns a formated date and time String from a Date value,
     * the format being {@link DateFormat#SHORT} and the locale
     * based on this instance.<p>
     * 
     * @param date the Date object to format as String
     * @return the formatted date and time
     */
    public String getDateTime(Date date) {

        return CmsDateUtil.getDateTime(date, DateFormat.SHORT, m_locale);
    }

    /**
     * Returns a formated date and time String from a Date value,
     * the formatting based on the provided option and the locale
     * based on this instance.<p>
     * 
     * @param date the Date object to format as String
     * @param format the format to use, see {@link CmsMessages} for possible values
     * @return the formatted date and time
     */
    public String getDateTime(Date date, int format) {

        return CmsDateUtil.getDateTime(date, format, m_locale);
    }

    /**
     * Returns a formated date and time String from a timestamp value,
     * the format being {@link DateFormat#SHORT} and the locale
     * based on this instance.<p>
     * 
     * @param time the time value to format as date
     * @return the formatted date and time
     */
    public String getDateTime(long time) {

        return CmsDateUtil.getDateTime(new Date(time), DateFormat.SHORT, m_locale);
    }

    /**
     * Directly calls the getString(String) method of the wrapped ResourceBundle.<p>
     * 
     * If you use this this class on a template, you should consider using 
     * the {@link #key(String)} method to get the value from the ResourceBundle because it
     * handles the exception for you in a convenient way. 
     * 
     * @param keyName the key  
     * @return the resource string for the given key
     * @throws MissingResourceException in case the key is not found of the bundle is not initialized
     */
    public String getString(String keyName) throws MissingResourceException {

        if (m_bundle != null) {
            return m_bundle.getString(keyName);
        } else {
            throw new MissingResourceException("ResourceBundle not initialized", this.getClass().getName(), keyName);
        }
    }

    /**
     * Checks if the bundle was properly initialized.
     * 
     * @return <code>true</code> if bundle was initialized, <code>false</code> otherwise
     */
    public boolean isInitialized() {

        return (m_bundle != null);
    }

    /**
     * Returns the localized resource string for a given message key.<p>
     * 
     * If the key was not found in the bundle, the return value is
     * <code>"??? " + keyName + " ???"</code>. This will also be returned 
     * if the bundle was not properly initialized first.
     * 
     * @param keyName the key for the desired string 
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
     * @return the resource string for the given key 
     */
    public String key(String keyName, boolean allowNull) {

        try {
            if (m_bundle != null) {
                return m_bundle.getString(keyName);
            }
        } catch (MissingResourceException e) {
            // not found, return warning
            if (allowNull) {
                return null;
            }
        }
        return formatUnknownKey(keyName);
    }

    /**
     * Returns the localized resource string for a given message key,
     * with the provided replacement parameters.<p>
     * 
     * If the key was found in the bundle, it will be formatted using
     * a <code>{@link MessageFormat}</code> using the provided parameters.<p>
     * 
     * If the key was not found in the bundle, the return value is
     * <code>"??? " + keyName + " ???"</code>. This will also be returned 
     * if the bundle was not properly initialized first.
     * 
     * @param keyName the key for the desired string 
     * @param params the parameters to use for formatting
     * @return the resource string for the given key 
     */
    public String key(String keyName, Object[] params) {

        if ((params == null) || (params.length == 0)) {
            // no parameters available, use simple key method
            return key(keyName);
        }

        String result = key(keyName, true);
        if (result == null) {
            // key was not found
            result = formatUnknownKey(keyName);
        } else {
            // key was found in the bundle - create and apply the formatter
            MessageFormat formatter = new MessageFormat(result, m_locale);
            result = formatter.format(params);
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
     * @return the resource string for the given key it it exists, or the given default if not 
     */
    public String key(String keyName, String defaultValue) {

        String result = key(keyName, true);
        return (result == null) ? defaultValue : result;
    }

    /**
     * Returns the localized resource string for a given message key,
     * treating all values appended with "|" as replacement parameters.<p>
     * 
     * If the key was found in the bundle, it will be formatted using
     * a <code>{@link MessageFormat}</code> using the provided parameters.
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
     * @param keyName the key for the desired string, optinally containing parameters appended with a "|"
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
            String[] values = CmsStringUtil.splitAsArray(keyName, '|');
            String cutKeyName = values[0];
            String[] params = new String[values.length - 1];
            System.arraycopy(values, 1, params, 0, params.length);
            return key(cutKeyName, params);
        }
    }
}