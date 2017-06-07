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

package org.opencms.acacia.client.widgets;

import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the configuration for various select widgets, including multi-select and combo-box.<p>
 *
 * Use following syntax for the configuration:<p>
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
 */
public class CmsSelectConfigurationParser {

    /** Optional shortcut default marker. */
    private static final String DEFAULT_MARKER = "*";

    /** Delimiter between option sets. */
    private static final String INPUT_DELIMITER = "|";

    /** Key prefix for the 'default'. */
    private static final String KEY_DEFAULT = "default='true'";

    /** Empty String to replaces unnecessary keys. */
    private static final String KEY_EMPTY = "";

    /** Key prefix for the 'help' text. */
    private static final String KEY_HELP = "help='";

    /** Key prefix for the 'option' text. */
    private static final String KEY_OPTION = "option='";

    /** Short key prefix for the 'option' text. */
    private static final String KEY_SHORT_OPTION = ":";

    /** Key suffix for the 'default' , 'help', 'option' text with following entrances.*/
    private static final String KEY_SUFFIX = "' ";

    /** Key suffix for the 'default' , 'help', 'option' text without following entrances.*/
    private static final String KEY_SUFFIX_SHORT = "'";

    /** Key prefix for the 'value'. */
    private static final String KEY_VALUE = "value='";

    /** The configuration to parse. */
    private String m_configuration;

    /** The default values. */
    private List<String> m_defaultValues;

    /** The help texts. */
    private Map<String, String> m_helpTexts;

    /** The options. */
    private Map<String, String> m_options;

    /**
     * Constructor. Will parse the given configuration string.<p>
     *
     * @param configuration the configuration
     */
    public CmsSelectConfigurationParser(String configuration) {

        m_configuration = configuration;
        m_options = new LinkedHashMap<String, String>();
        m_helpTexts = new LinkedHashMap<String, String>();
        m_defaultValues = new ArrayList<String>();
        parseConfiguration();
    }

    /**
     * Returns the default value.<p>
     *
     * @return the default value
     */
    public String getDefaultValue() {

        String value = null;
        if (!m_defaultValues.isEmpty()) {
            value = m_defaultValues.get(m_defaultValues.size() - 1);
        }
        return value;
    }

    /**
     * Returns the default value.<p>
     *
     * @return the default value
     */
    public List<String> getDefaultValues() {

        return m_defaultValues;
    }

    /**
     * Returns the help texts.<p>
     *
     * @return the help texts
     */
    public Map<String, String> getHelpTexts() {

        return m_helpTexts;
    }

    /**
     * Returns the options.<p>
     *
     * @return the options
     */
    public Map<String, String> getOptions() {

        return m_options;
    }

    /**
     * Parses the configuration string.<p>
     */
    private void parseConfiguration() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_configuration)) {
            //split the configuration in single strings to handle every string single.
            String[] selectOptions = m_configuration.split("\\" + INPUT_DELIMITER);

            for (int i = 0; i < selectOptions.length; i++) {
                //check if there are one or more parameters set in this substring.
                boolean test_default = (selectOptions[i].indexOf(KEY_DEFAULT) >= 0);
                boolean test_value = selectOptions[i].indexOf(KEY_VALUE) >= 0;
                boolean test_option = selectOptions[i].indexOf(KEY_OPTION) >= 0;
                boolean test_short_option = selectOptions[i].indexOf(KEY_SHORT_OPTION) >= 0;
                boolean test_help = selectOptions[i].indexOf(KEY_HELP) >= 0;
                boolean isDefault = false;
                String value = null;
                String label = null;
                String help = null;
                try {
                    //check if there is a default value set.
                    if ((selectOptions[i].indexOf(DEFAULT_MARKER) >= 0) || test_default) {
                        //remember the position in the array.
                        isDefault = true;
                        //remove the declaration parameters.
                        selectOptions[i] = selectOptions[i].replace(DEFAULT_MARKER, KEY_EMPTY);
                        selectOptions[i] = selectOptions[i].replace(KEY_DEFAULT, KEY_EMPTY);
                    }
                    //check for values (e.g.:"value='XvalueX' ") set in configuration.
                    if (test_value) {
                        String sub = KEY_EMPTY;
                        //check there are more parameters set, separated with space.
                        if (selectOptions[i].indexOf(KEY_SUFFIX) >= 0) {
                            //create substring e.g.:"value='XvalueX".
                            sub = selectOptions[i].substring(
                                selectOptions[i].indexOf(KEY_VALUE),
                                selectOptions[i].indexOf(KEY_SUFFIX));
                        }
                        //if there are no more parameters set.
                        else {
                            //create substring e.g.:"value='XvalueX".
                            sub = selectOptions[i].substring(
                                selectOptions[i].indexOf(KEY_VALUE),
                                selectOptions[i].length() - 1);
                        }
                        //transfer the extracted value to the value array.
                        value = sub.replace(KEY_VALUE, KEY_EMPTY);
                        //remove the parameter within the value.
                        selectOptions[i] = selectOptions[i].replace(KEY_VALUE + value + KEY_SUFFIX_SHORT, KEY_EMPTY);
                    }
                    //no value parameter is set.
                    else {
                        //check there are more parameters set.
                        if (test_short_option) {
                            //transfer the separated value to the value array.
                            value = selectOptions[i].substring(0, selectOptions[i].indexOf(KEY_SHORT_OPTION));
                        }
                        //no parameters set.
                        else {
                            //transfer the value set in configuration untreated to the value array.
                            value = selectOptions[i];
                        }
                    }
                    //check for options(e.g.:"option='XvalueX' ") set in configuration.
                    if (test_option) {
                        String sub = KEY_EMPTY;
                        //check there are more parameters set, separated with space.
                        if (selectOptions[i].indexOf(KEY_SUFFIX) >= 0) {
                            //create substring e.g.:"option='XvalueX".
                            sub = selectOptions[i].substring(
                                selectOptions[i].indexOf(KEY_OPTION),
                                selectOptions[i].indexOf(KEY_SUFFIX));
                        }
                        //if there are no more parameters set.
                        else {
                            //create substring e.g.:"option='XvalueX".
                            sub = selectOptions[i].substring(
                                selectOptions[i].indexOf(KEY_OPTION),
                                selectOptions[i].lastIndexOf(KEY_SUFFIX_SHORT));
                        }
                        //transfer the extracted value to the option array.
                        label = sub.replace(KEY_OPTION, KEY_EMPTY);
                        //remove the parameter within the value.
                        selectOptions[i] = selectOptions[i].replace(KEY_OPTION + label + KEY_SUFFIX_SHORT, KEY_EMPTY);
                    }
                    //check if there is a short form (e.g.:":XvalueX") of the option set in configuration.
                    else if (test_short_option) {
                        //transfer the extracted value to the option array.
                        label = selectOptions[i].substring(selectOptions[i].indexOf(KEY_SHORT_OPTION) + 1);
                    }
                    //there are no options set in configuration.
                    else {
                        //option value is the same like the name value so the name value is transfered to the option array.
                        label = value;
                    }
                    //check for help set in configuration.
                    if (test_help) {
                        String sub = KEY_EMPTY;
                        //check there are more parameters set, separated with space.
                        if (selectOptions[i].indexOf(KEY_SUFFIX) >= 0) {
                            sub = selectOptions[i].substring(
                                selectOptions[i].indexOf(KEY_HELP),
                                selectOptions[i].indexOf(KEY_SUFFIX));
                        }
                        //if there are no more parameters set.
                        else {
                            sub = selectOptions[i].substring(
                                selectOptions[i].indexOf(KEY_HELP),
                                selectOptions[i].indexOf(KEY_SUFFIX_SHORT));
                        }
                        //transfer the extracted value to the help array.
                        help = sub.replace(KEY_HELP, KEY_EMPTY);
                        //remove the parameter within the value.
                        selectOptions[i] = selectOptions[i].replace(KEY_HELP + help + KEY_SUFFIX_SHORT, KEY_EMPTY);

                    }
                    //copy value and option to the Map.
                    m_options.put(value, label);
                    m_helpTexts.put(value, help);
                    if (isDefault) {
                        m_defaultValues.add(value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
