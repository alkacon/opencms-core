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

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Parses the configuration for various select widgets, including multi-select and combo-box.<p>
 *
 * It expects the canonical format of the configuration options as it is produced
 * by {@link org.opencms.widgets.CmsSelectWidgetOption#createConfigurationString(List)}
 */
public class CmsSelectConfigurationParser {

    /** Delimiter at the end of a value. */
    private static final char VALUE_DELIMITER = '\'';

    /** Key prefix for the 'default'. */
    private static final String KEY_DEFAULT = "default='";

    /** Key prefix for the 'help' text. */
    private static final String KEY_HELP = "help='";

    /** Key prefix for the 'option' text. */
    private static final String KEY_OPTION = "option='";

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
     * Splits the options string at every unescaped input delimiter, i.e., every unescaped "|".
     * @param input the options string
     * @return the array with the various options
     */
    public static int findNextUnescapedSingleQuote(String input) {

        //Note that we use a regex matching all "|" characters not prefixed by "\"
        //Since we define a regex for matching, the input delimiter "|" needs to be escaped, as well as "\",
        //which is even double-escaped - one escaping is due to the String, one due to the regex.
        RegExp regex = RegExp.compile("(?<!\\\\)\\" + VALUE_DELIMITER);
        MatchResult match = regex.exec(input);
        return match.getIndex();
    }

    /**
     * Splits the options string at every unescaped input delimiter, i.e., every unescaped "|".
     * @param input the options string
     * @return the array with the various options
     */
    public static String[] splitOptions(String input) {

        //Note that we use a regex matching all "|" characters not prefixed by "\"

        //Since we define a regex for matching, the input delimiter "|" needs to be escaped, as well as "\",
        //which is even double-escaped - one escaping is due to the String, one due to the regex.

        // emulate missing lookbehinds in JS regexes by first reversing the input,
        // then using a split with lookaheads, and finally reversing the parts resulting
        // from the split
        String reverse = reverse(input);
        String[] parts = reverse.split("\\|(?!\\\\)");
        String[] finalParts = new String[parts.length];
        int lastIndex = parts.length - 1;
        for (int i = 0; i < parts.length; i++) {
            finalParts[lastIndex - i] = reverse(parts[i]);
        }
        return finalParts;

    }

    /**
     * Reverses a string.<p>
     *
     * @param input the input string
     * @return the reversed string
     */
    private static String reverse(String input) {

        return new StringBuilder(input).reverse().toString();
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
     * Parses the configuration string if provided in canonical format.<p>
     *
     * At the client side, the string has always to be in the canonical format "value='...' default='...' option='...' help='...'|..."
     * where only value is mandatory, the other things are optional.
     *
     * The format is produced by {@link org.opencms.widgets.CmsSelectWidgetOption#createConfigurationString(List)}.
     */
    private void parseConfiguration() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_configuration)) {
            //split the configuration in single strings to handle every string single.
            String[] selectOptions = splitOptions(m_configuration);

            for (int i = 0; i < selectOptions.length; i++) {
                try {
                    String value;
                    String label = "";
                    boolean isDefault = false;
                    String help = "";
                    String option = selectOptions[i];
                    option = option.replace("\\|", "|");

                    int valuePos = option.indexOf(KEY_VALUE);
                    int defaultPos = option.indexOf(KEY_DEFAULT);
                    int optionPos = option.indexOf(KEY_OPTION);
                    int helpPos = option.indexOf(KEY_HELP);
                    if (valuePos != 0) {
                        throw new Exception("Invalid select widget configuration \"" + option + "\" is ignored.");
                    }
                    int end = defaultPos >= 0
                    ? defaultPos
                    : optionPos >= 0 ? optionPos : helpPos >= 0 ? helpPos : option.length();
                    value = option.substring((valuePos + KEY_VALUE.length()) - 1, end).trim();
                    value = value.substring(1, value.length() - 1);

                    isDefault = (defaultPos >= 0)
                        && option.substring(defaultPos + KEY_DEFAULT.length()).startsWith("true");

                    if (optionPos >= 0) {
                        end = helpPos >= 0 ? helpPos : option.length();
                        label = option.substring((optionPos + KEY_OPTION.length()) - 1, end).trim();
                        label = label.substring(1, label.length() - 1);
                    } else {
                        label = value;
                    }
                    if (helpPos >= 0) {
                        help = option.substring((helpPos + KEY_HELP.length()) - 1, option.length()).trim();
                        help = help.substring(1, help.length() - 1);
                    }
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
