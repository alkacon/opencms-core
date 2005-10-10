/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsSelectWidgetOption.java,v $
 * Date   : $Date: 2005/10/10 16:11:03 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.widgets;

import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * An option of a select type widget.<p>
 * 
 * If options are passed from XML content schema definitions as widget configuration options,
 * the following syntax is used for defining the option values:<p>
 * 
 * <code>value='{text}' default='{true|false}' option='{text}' help='{text}|{more option definitions}</code><p>
 * 
 * For example:<p>  
 * 
 * <code>value='value1' default='true' option='option1' help='help1'|value='value2' option='option2' help='help2'</code><p>
 * 
 * The elements <code>default</code>, <code>option</code> and <code>help</code> are all optional, only a 
 * <code>value</code> must be present in the input. 
 * There should be only one <code>default</code> set to <code>true</code>
 * in the input, if more than one is detected, only the first <code>default</code> found is actually used. 
 * If no <code>option</code> is given, the value of <code>option</code> defaults to the value of the given <code>value</code>. 
 * If no <code>help</code> is given, the default is <code>null</code>.<p> 
 * 
 * Shortcut syntax options:<p>
 * 
 * If you don't specify the <code>value</code> key, the value is assumed to start at the first position of an
 * option definition. In this case the value must not be surrounded by the <code>'</code> chars. 
 * Example: <code>value='some value' default='true'</code> can also be written as <code>some value default='true'</code>.<p>
 * 
 * Only if you use the short value definition as described above, a default value can be marked with a <code>*</code>
 * at the end of the value definition.
 * Example: <code>value='some value' default='true'</code> can also be written as <code>some value*</code>.<p>
 * 
 * Only if you use the short value definition as described above, you can also append the <code>option</code>
 * to the <code>value</code> using a <code>:</code>. In this case no <code>'</code> must surround the <code>option</code>.
 * Please keep in mind that in this case the value 
 * itself can not longer contain a <code>:</code> char, since it would then be interpreted as a delimiter.
 * Example: <code>value='some value' option='some option'</code> can also be written as <code>some value:some option</code>.<p>
 * 
 * Any combinations of the above described shortcuts are allowed in the configuration option String.
 * Here are some more examples of valid configuration option Strings:<p>
 * 
 * <code>1*|2|3|4|5|6|7</code><br>
 * <code>1 default='true'|2|3|4|5|6|7</code><br>
 * <code>value='1' default='true'|value='2'|value='3'</code><br>
 * <code>value='1'|2*|value='3'</code><br>
 * <code>1*:option text|2|3|4</code><br>
 * <code>1* option='option text' help='some'|2|3|4</code><p>
 * 
 * Please note: If an entry in the configuration String is malformed, this error is silently ignored (but written 
 * to the log channel of this class at <code>INFO</code>level.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSelectWidgetOption {

    /** Optional shortcut default marker. */
    private static final char DEFAULT_MARKER = '*';

    /** Delimiter between option sets. */
    private static final char INPUT_DELIMITER = '|';

    /** Key prefix for the 'default'. */
    private static final String KEY_DEFAULT = "default='";

    /** Key prefix for the 'help' text. */
    private static final String KEY_HELP = "help='";

    /** Key prefix for the 'option' text. */
    private static final String KEY_OPTION = "option='";

    /** Key prefix for the 'value'. */
    private static final String KEY_VALUE = "value='";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSelectWidgetOption.class);

    /** Optional shortcut option delimiter. */
    private static final char OPTION_DELIMITER = ':';

    /** Indicates if this is the default value of the selection. */
    private boolean m_default;

    /** The hashcode of this object. */
    private int m_hashcode;

    /** The (optional) help text of this select option. */
    private String m_help;

    /** The (optional) display text of this select option. */
    private String m_option;

    /** The value of this select option. */
    private String m_value;

    /**
     * Creates a new select option for the given value.<p>
     *  
     * @param value the value of this select option
     */
    public CmsSelectWidgetOption(String value) {

        this(value, false, null, null);
    }

    /**
     * Creates a new select option form the given values.<p>
     * 
     * @param value the value of this select option
     * @param isDefault indicates if this is the default value of the selection (default is <code>false</code>)
     */
    public CmsSelectWidgetOption(String value, boolean isDefault) {

        this(value, isDefault, null, null);
    }

    /**
     * Creates a new select option form the given values.<p>
     * 
     * @param value the value of this select option
     * @param isDefault indicates if this is the default value of the selection (default is <code>false</code>)
     * @param optionText the (optional) display text of this select option
     */
    public CmsSelectWidgetOption(String value, boolean isDefault, String optionText) {

        this(value, isDefault, optionText, null);
    }

    /**
     * Creates a new select option form the given values.<p>
     * 
     * @param value the value of this select option
     * @param isDefault indicates if this is the default value of the selection (default is <code>false</code>)
     * @param optionText the (optional) display text of this select option
     * @param helpText the (optional) help text of this select option
     */
    public CmsSelectWidgetOption(String value, boolean isDefault, String optionText, String helpText) {

        m_default = isDefault;
        m_value = value;
        m_option = optionText;
        m_help = helpText;
    }

    /**
     * Returns a select widget configuration String created from the given list of select options.<p>
     * 
     * If an element found in the given list is not of type 
     * <code>{@link CmsSelectWidgetOption}</code>, it is ignored.<p>
     * 
     * @param options the list of select options to create the configuration String for
     * 
     * @return a select widget configuration String created from the given list of select options
     */
    public static String createConfigurationString(List options) {

        if ((options == null) || (options.size() == 0)) {
            return "";
        }
        StringBuffer result = new StringBuffer(256);
        boolean first = true;
        for (int i = 0; i < options.size(); i++) {
            Object o = options.get(i);
            if (o instanceof CmsSelectWidgetOption) {
                if (!first) {
                    result.append(CmsSelectWidgetOption.INPUT_DELIMITER);
                } else {
                    first = false;
                }
                result.append(o.toString());
            }
        }
        return result.toString();
    }

    /**
     * Returns the default option from the given list of select options, 
     * or <code>null</code> in case there is no default option in the given list.<p> 
     * 
     * If an element found in the given list is not of type 
     * <code>{@link CmsSelectWidgetOption}</code>, this is ignored.<p>
     * 
     * @param options the list of select options to get the default from
     * 
     * @return the default option from the given list of select options, or <code>null</code> in case there is no default option
     */
    public static CmsSelectWidgetOption getDefaultOption(List options) {

        if ((options == null) || (options.size() == 0)) {
            return null;
        }
        for (int i = 0; i < options.size(); i++) {
            Object o = options.get(i);
            if (o instanceof CmsSelectWidgetOption) {
                CmsSelectWidgetOption option = (CmsSelectWidgetOption)o;
                if (option.isDefault()) {
                    return option;
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of default options from the given list of select options.<p> 
     * 
     * If an element found in the given list is not of type 
     * <code>{@link CmsSelectWidgetOption}</code>, this is ignored.<p>
     * 
     * @param options the list of select options to get the default from
     * 
     * @return a list of <code>{@link CmsSelectWidgetOption}</code> objects 
     */
    public static List getDefaultOptions(List options) {

        List defaults = new ArrayList();
        if ((options == null) || (options.size() == 0)) {
            return defaults;
        }
        for (int i = 0; i < options.size(); i++) {
            Object o = options.get(i);
            if (o instanceof CmsSelectWidgetOption) {
                CmsSelectWidgetOption option = (CmsSelectWidgetOption)o;
                if (option.isDefault()) {
                    defaults.add(option);
                }
            }
        }
        return defaults;
    }

    /**
     * Parses a widget configuration String for select option values.<p> 
     * 
     * If the input is <code>null</code> or empty, a <code>{@link Collections#EMPTY_LIST}</code>
     * is returned.<p>
     * 
     * Please note: No exception is thrown in case the input is malformed, all malformed entries are silently ignored.<p>
     * 
     * @param input the widget input string to parse
     * 
     * @return a List of <code>{@link CmsSelectWidgetOption}</code> elements
     */
    public static List parseOptions(String input) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(input)) {
            // default result for empty input
            return Collections.EMPTY_LIST;
        }

        // cut along the delimiter
        String[] parts = CmsStringUtil.splitAsArray(input, INPUT_DELIMITER);
        List result = new ArrayList();

        // indicates if a default of 'true' was already set in this result list
        boolean foundDefault = false;

        for (int i = 0; i < parts.length; i++) {

            String part = parts[i].trim();
            if (part.length() == 0) {
                // skip empty parts
                continue;
            }

            try {

                String value = null;
                String option = null;
                String help = null;
                boolean isDefault = false;

                int posValue = part.indexOf(KEY_VALUE);
                int posDefault = part.indexOf(KEY_DEFAULT);
                int posOption = part.indexOf(KEY_OPTION);
                int posHelp = part.indexOf(KEY_HELP);

                boolean shortValue = false;
                if (posValue < 0) {
                    // shortcut syntax, value key must be at first position
                    if ((posDefault == 0) || (posOption == 0) || (posHelp == 0)) {
                        // malformed part - no value given
                        throw new CmsWidgetException(Messages.get().container(Messages.ERR_MALFORMED_SELECT_OPTIONS_0));
                    }
                    posValue = 0;
                    shortValue = true;
                }

                // a 'value' must be always present
                int end = part.length();
                // check where the 'value' ends
                if (posHelp > posValue) {
                    end = posHelp;
                }
                if ((posDefault > posValue) && (posDefault < end)) {
                    end = posDefault;
                }
                if ((posOption > posValue) && (posOption < end)) {
                    end = posOption;
                }
                if (shortValue) {
                    // no explicit setting using the key, value must be at the first position
                    value = part.substring(0, end).trim();
                } else {
                    value = part.substring(posValue + KEY_VALUE.length(), end).trim();
                    // cut of trailing '
                    value = value.substring(0, value.length() - 1);
                }

                boolean shortOption = false;
                // check if the option is appended using the ':' shortcut
                if ((shortValue) && (posOption < 0)) {
                    int pos = value.indexOf(OPTION_DELIMITER);
                    if (pos >= 0) {
                        // shortcut syntax is used
                        posOption = pos;
                        shortOption = true;
                        value = value.substring(0, pos);
                    }
                }

                if (posDefault >= 0) {
                    // there was an explicit 'default' setting using the key, check where it ends
                    end = part.length();
                    if (posHelp > posDefault) {
                        end = posHelp;
                    }
                    if ((posOption > posDefault) && (posOption < end)) {
                        end = posOption;
                    }
                    if ((posValue > posDefault) && (posValue < end)) {
                        end = posValue;
                    }
                    String sub = part.substring(posDefault + KEY_DEFAULT.length(), end).trim();
                    // cut of trailing '
                    sub = sub.substring(0, sub.length() - 1);
                    isDefault = Boolean.valueOf(sub).booleanValue();
                } else {
                    // check for shortcut syntax, value must end with a '*'
                    if (value.charAt(value.length() - 1) == DEFAULT_MARKER) {
                        isDefault = true;
                        value = value.substring(0, value.length() - 1);
                    }
                }

                if (posOption >= 0) {
                    // an 'option' setting is available, check where it ends
                    end = part.length();
                    if (posHelp > posOption) {
                        end = posHelp;
                    }
                    if ((posDefault > posOption) && (posDefault < end)) {
                        end = posDefault;
                    }
                    if ((posValue > posOption) && (posValue < end)) {
                        end = posValue;
                    }
                    if (shortOption) {
                        // shortcut syntax used for option with ':' appended to value
                        option = part.substring(posOption + 1, end).trim();
                    } else {
                        option = part.substring(posOption + KEY_OPTION.length(), end).trim();
                        // cut of trailing '
                        option = option.substring(0, option.length() - 1);
                    }
                }

                if (posHelp >= 0) {
                    // a 'help' setting is available, check where it ends
                    end = part.length();
                    if (posOption > posHelp) {
                        end = posOption;
                    }
                    if ((posDefault > posHelp) && (posDefault < end)) {
                        end = posDefault;
                    }
                    if ((posValue > posHelp) && (posValue < end)) {
                        end = posValue;
                    }
                    help = part.substring(posHelp + KEY_HELP.length(), end).trim();
                    // cut of trailing '
                    help = help.substring(0, help.length() - 1);
                }

                // check if there was already a 'true' default, if so all other entries are 'false'
                if (foundDefault) {
                    isDefault = false;
                } else if (isDefault) {
                    foundDefault = true;
                }

                result.add(new CmsSelectWidgetOption(value, isDefault, option, help));

            } catch (Exception e) {
                // malformed part
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().key(Messages.ERR_MALFORMED_SELECT_OPTIONS_0), e);
                }
            }
        }

        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsSelectWidgetOption)) {
            return false;
        }
        CmsSelectWidgetOption other = (CmsSelectWidgetOption)obj;
        if (m_default != other.m_default) {
            return false;
        }
        if (m_value == null) {
            if (other.m_value != null) {
                return false;
            }
        } else if (!m_value.equals(other.m_value)) {
            return false;
        }
        if (m_option == null) {
            if (other.m_option != null) {
                return false;
            }
        } else if (!m_option.equals(other.m_option)) {
            return false;
        }
        if (m_help == null) {
            if (other.m_help != null) {
                return false;
            }
        } else if (!m_help.equals(other.m_help)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the (optional) help text of this select option.<p>
     *
     * @return the (optional) help text of this select option
     */
    public String getHelp() {

        return m_help;
    }

    /**
     * Returns the option text of this select option.<p>
     *
     * If this has not been set, the result of <code>{@link #getValue()}</code> is returned,
     * there will always be a result other than <code>null</code> returned.<p>
     *
     * @return the option text of this select option
     */
    public String getOption() {

        if (m_option == null) {
            return getValue();
        }
        return m_option;
    }

    /**
     * Returns the value of this select option.<p>
     *
     * @return the value of this select option
     */
    public String getValue() {

        return m_value;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        if (m_hashcode == 0) {
            StringBuffer hash = new StringBuffer(128);
            hash.append(m_value);
            hash.append('|');
            hash.append(m_default);
            hash.append('|');
            hash.append(m_option);
            hash.append('|');
            hash.append(m_help);
            m_hashcode = hash.toString().hashCode();
        }
        return m_hashcode;
    }

    /**
     * Returns <code>true</code> if this is the default value of the selection.<p>
     *
     * @return <code>true</code> if this is the default value of the selection
     */
    public boolean isDefault() {

        return m_default;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer(128);

        result.append(KEY_VALUE);
        result.append(m_value);
        result.append('\'');
        if (m_default) {
            result.append(' ');
            result.append(KEY_DEFAULT);
            result.append(m_default);
            result.append('\'');
        }
        if (m_option != null) {
            result.append(' ');
            result.append(KEY_OPTION);
            result.append(m_option);
            result.append('\'');
        }
        if (m_help != null) {
            result.append(' ');
            result.append(KEY_HELP);
            result.append(m_help);
            result.append('\'');
        }
        return result.toString();
    }
}