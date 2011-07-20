/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsStringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides convenient access to configuration parameters.<p>
 * 
 * Usually the parameters are configured in some sort of String based file,
 * either in an XML configuration, or in a .property file. 
 * This wrapper allows accessing such String values directly 
 * as <code>int</code>, <code>boolean</code> or other data types, without 
 * worrying about the type conversion.<p>
 * 
 * It can also read a configuration from a special property file format,
 * which is explained here:
 *
 * <ul>
 *  <li>
 *   Each parameter in the file has the syntax <code>key = value</code>
 *  </li>
 *  <li>
 *   The <i>key</i> may use any character but the equal sign '='.
 *  </li>
 *  <li>
 *   <i>value</i> may be separated on different lines if a backslash
 *   is placed at the end of the line that continues below.
 *  </li>
 *  <li>
 *   If <i>value</i> is a list of strings, each token is separated
 *   by a comma ','.
 *  </li>
 *  <li>
 *   Commas in each token are escaped placing a backslash right before
 *   the comma.
 *  </li>
 *  <li>
 *   Backslashes are escaped by using two consecutive backslashes i.e. \\.
 *   Note: Unlike in regular Java properties files, you don't need to escape Backslashes. 
 *  </li>
 *  <li>
 *   If a <i>key</i> is used more than once, the values are appended
 *   as if they were on the same line separated with commas.
 *  </li>
 *  <li>
 *   Blank lines and lines starting with character '#' are skipped.
 *  </li>
 * </ul>
 *
 * Here is an example of a valid parameter properties file:<p>
 *
 * <pre>
 *      # lines starting with # are comments
 *
 *      # This is the simplest property
 *      key = value
 *
 *      # A long property may be separated on multiple lines
 *      longvalue = aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
 *                  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
 *
 *      # This is a property with many tokens
 *      tokens_on_a_line = first token, second token
 *
 *      # This sequence generates exactly the same result
 *      tokens_on_multiple_lines = first token
 *      tokens_on_multiple_lines = second token
 *
 *      # commas may be escaped in tokens
 *      commas.escaped = Hi\, what'up?
 * </pre>
 */
public class CmsConfigurationParameter {

    /**
     * Used to read parameter lines from a property file.<p>  
     * 
     * The lines do not terminate with new-line chars but rather when there is no
     * backslash sign a the end of the line. This is used to
     * concatenate multiple lines for readability in the input file.<p>
     */
    protected static class ParameterReader extends LineNumberReader {

        /**
         * Constructor.<p>
         *
         * @param reader a reader
         */
        public ParameterReader(Reader reader) {

            super(reader);
        }

        /**
         * Reads a parameter line.<p>
         *
         * @return the parameter line read
         * 
         * @throws IOException in case of IO errors
         */
        public String readParameter() throws IOException {

            StringBuffer buffer = new StringBuffer();
            String line = readLine();
            while (line != null) {
                line = line.trim();
                if ((line.length() != 0) && (line.charAt(0) != '#')) {
                    if (endsWithSlash(line)) {
                        line = line.substring(0, line.length() - 1);
                        buffer.append(line);
                    } else {
                        buffer.append(line);
                        return buffer.toString(); // normal method end
                    }
                }
                line = readLine();
            }
            return null; // EOF reached
        }
    }

    /**
     * This class divides property value into tokens separated by ",".<p>
     * 
     * Commas in the property value that are wanted
     * can be escaped using the backslash in front like this "\,".
     */
    protected static class ParameterTokenizer extends StringTokenizer {

        /** The property delimiter used while parsing (a comma). */
        static final String COMMA = ",";

        /**
         * Constructor.<p>
         *
         * @param string the String to break into tokens
         */
        public ParameterTokenizer(String string) {

            super(string, COMMA);
        }

        /**
         * Returns the next token.<p>
         *
         * @return  the next token
         */
        @Override
        public String nextToken() {

            StringBuffer buffer = new StringBuffer();

            while (hasMoreTokens()) {
                String token = super.nextToken();
                if (endsWithSlash(token)) {
                    buffer.append(token.substring(0, token.length() - 1));
                    buffer.append(COMMA);
                } else {
                    buffer.append(token);
                    break;
                }
            }

            return buffer.toString().trim();
        }
    }

    /** The parsed map of parameters where the Strings may have become Objects. */
    private Map<String, Object> m_configurationObjects;

    /** The initial map of parameters. */
    private Map<String, String> m_configurationStrings;

    /**
     * Create an empty configuration parameter wrapper.<p>
     */
    public CmsConfigurationParameter() {

        m_configurationObjects = new LinkedHashMap<String, Object>();
        m_configurationStrings = new LinkedHashMap<String, String>();
    }

    /**
     * Creates a parameter wrapper from an input stream.<p>
     * 
     * @param in the input stream to create the parameter wrapper from
     * 
     * @throws IOException in case of errors loading the parameters from the input stream
     */
    public CmsConfigurationParameter(InputStream in)
    throws IOException {

        load(in);
    }

    /**
     * Creates a parameter wrapper from a Map of Strings.<p>
     * 
     * @param configuration the map of Strings to create the parameter wrapper from
     */
    public CmsConfigurationParameter(Map<String, String> configuration) {

        m_configurationObjects = new LinkedHashMap<String, Object>(configuration.size());
        m_configurationStrings = new LinkedHashMap<String, String>(configuration.size());

        for (String key : configuration.keySet()) {

            String value = configuration.get(key);
            addParameter(key, value);
        }
    }

    /**
     * Creates a parameter wrapper by loading the parameters from the specified property file.<p>
     *
     * @param file the path of the file to load
     * 
     * @throws IOException in case of errors loading the parameters from the specified property file
     */
    public CmsConfigurationParameter(String file)
    throws IOException {

        m_configurationObjects = new LinkedHashMap<String, Object>();
        m_configurationStrings = new LinkedHashMap<String, String>();

        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            this.load(in);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore error on close() only
            }
        }
    }

    /**
     * Counts the number of successive times 'ch' appears in the
     * 'line' before the position indicated by the 'index'.<p>
     * 
     * @param line the line to count
     * @param index the index position to start
     * @param ch the character to count
     * 
     * @return the number of successive times 'ch' appears in the 'line' 
     *      before the position indicated by the 'index'
     */
    protected static int countPreceding(String line, int index, char ch) {

        int i;
        for (i = index - 1; i >= 0; i--) {
            if (line.charAt(i) != ch) {
                break;
            }
        }
        return index - 1 - i;
    }

    /**
     * Checks if the line ends with odd number of backslashes.<p>
     * 
     * @param line the line to check
     * 
     * @return <code>true</code> if the line ends with odd number of backslashes
     */
    protected static boolean endsWithSlash(String line) {

        if (!line.endsWith("\\")) {
            return false;
        }
        return ((countPreceding(line, line.length() - 1, '\\') % 2) == 0);
    }

    /**
     * Replaces escaped char sequences in the input value.<p>
     * 
     * @param value the value to unescape
     * 
     * @return the unescaped String
     */
    protected static String unescape(String value) {

        value = CmsStringUtil.substitute(value, "\\,", ",");
        value = CmsStringUtil.substitute(value, "\\=", "=");
        value = CmsStringUtil.substitute(value, "\\\\", "\\");

        return value;
    }

    /**
     * Add a parameter to this configuration.<p>
     * 
     * If the parameter already exists then the value will be added
     * to the existing configuration entry and a List will be created for the values.<p>
     *
     * @param parameter the parameter to add
     * @param value the value to add
     */
    public void addParameter(String parameter, String value) {

        if (value.indexOf(ParameterTokenizer.COMMA) > 0) {
            // token contains commas, so must be split apart then added
            ParameterTokenizer tokenizer = new ParameterTokenizer(value);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                addParameterInternal(parameter, unescape(token));
            }
        } else {
            // token contains no commas, so can be simply added
            addParameterInternal(parameter, unescape(value));
        }
    }

    /**
     * Clears a parameter from this configuration.
     *
     * @param parameter the parameter to clear
     */
    public void clearParameter(String parameter) {

        if (m_configurationObjects.containsKey(parameter)) {
            m_configurationObjects.remove(parameter);
        }
        if (m_configurationStrings.containsKey(parameter)) {
            m_configurationStrings.remove(parameter);
        }
    }

    /**
     * Returns the boolean associated with the given parameter, 
     * or the default value in case there is no boolean value for this parameter.<p> 
     *
     * @param parameter the parameter to look up the value for
     * @param defaultValue the default value
     * 
     * @return the boolean associated with the given parameter, 
     *      or the default value in case there is no boolean value for this parameter
     */
    public boolean getBoolean(String parameter, boolean defaultValue) {

        Object value = m_configurationObjects.get(parameter);

        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();

        } else if (value instanceof String) {
            Boolean b = Boolean.valueOf((String)value);
            m_configurationObjects.put(parameter, b);
            return b.booleanValue();

        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the integer associated with the given parameter, 
     * or the default value in case there is no integer value for this parameter.<p> 
     *
     * @param parameter the parameter to look up the value for
     * @param defaultValue the default value
     * 
     * @return the integer associated with the given parameter, 
     *      or the default value in case there is no integer value for this parameter
     */
    public int getInteger(String parameter, int defaultValue) {

        Object value = m_configurationObjects.get(parameter);

        if (value instanceof Integer) {
            return ((Integer)value).intValue();

        } else if (value instanceof String) {
            Integer i = new Integer((String)value);
            m_configurationObjects.put(parameter, i);
            return i.intValue();

        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the List of Strings associated with the given parameter, 
     * or an empty List in case there is no List of Strings for this parameter.<p> 
     *
     * The list returned is a copy of the internal data of this object, and as
     * such you may alter it freely.<p>
     *
     * @param parameter the parameter to look up the value for
     * 
     * @return the List of Strings associated with the given parameter, 
     *      or an empty List in case there is no List of Strings for this parameter
     */
    public List<String> getList(String parameter) {

        return getList(parameter, null);
    }

    /**
     * Returns the List of Strings associated with the given parameter, 
     * or the default value in case there is no List of Strings for this parameter.<p> 
     *
     * The list returned is a copy of the internal data of this object, and as
     * such you may alter it freely.<p>
     *
     * @param parameter the parameter to look up the value for
     * @param defaultValue the default value
     * 
     * @return the List of Strings associated with the given parameter, 
     *      or the default value in case there is no List of Strings for this parameter
     */
    public List<String> getList(String parameter, List<String> defaultValue) {

        Object value = m_configurationObjects.get(parameter);

        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> result = (List<String>)value;
            return new ArrayList<String>(result);

        } else if (value instanceof String) {
            List<String> values = new ArrayList<String>(1);
            values.add((String)value);
            m_configurationObjects.put(parameter, values);
            return values;

        } else {
            if (defaultValue == null) {
                return new ArrayList<String>();
            } else {
                return defaultValue;
            }
        }
    }

    /**
     * Returns the raw Object associated with the given parameter, 
     * or <code>null</code> in case there is no Object for this parameter.<p> 
     *
     * @param parameter the parameter to look up the value for
     * 
     * @return the raw Object associated with the given parameter, 
     *      or <code>null</code> in case there is no Object for this parameter.<p> 
     */
    public Object getObject(String parameter) {

        return m_configurationObjects.get(parameter);
    }

    /**
     * Returns a Map for this configuration where all values are represented as Strings.<p>
     * 
     * The Map returned is a copy of the internal data of this object, and as
     * such you may alter it freely.<p>
     * 
     * @return a Map for this configuration where all values are represented as Strings
     */
    public Map<String, String> getParameterMap() {

        return new LinkedHashMap<String, String>(m_configurationStrings);
    }

    /**
     * Returns the parameter Set for this configuration.<p>
     * 
     * The Set returned is a copy of the internal data of this object, and as
     * such you may alter it freely.<p>
     * 
     * @return the "pure" String configuration of this parameter wrapper
     */
    public Set<String> getParameterSet() {

        return new HashSet<String>(m_configurationObjects.keySet());
    }

    /**
     * Returns the String associated with the given parameter, 
     * or <code>null</code> in case there is no value for this parameter.<p> 
     *
     * @param parameter the parameter to look up the value for
     * 
     * @return the String associated with the given parameter, 
     *      or <code>null</code> in case there is no value for this parameter.<p> 
     */
    public String getString(String parameter) {

        return m_configurationStrings.get(parameter);
    }

    /**
     * Returns the String associated with the given parameter, 
     * or the given default value in case there is no value for this parameter.<p> 
     *
     * @param parameter the parameter to look up the value for
     * @param defaultValue the default value
     * 
     * @return the String associated with the given parameter, 
     *      or the given default value in case there is no value for this parameter.<p> 
     */
    public String getString(String parameter, String defaultValue) {

        String result = m_configurationStrings.get(parameter);
        return result == null ? defaultValue : result;
    }

    /**
     * Returns <code>true</code> in case there is a value for the given parameter.<p>
     * 
     * @param parameter the parameter name to check
     * 
     * @return <code>true</code> in case there is a value for the given parameter
     */
    public boolean hasParameter(String parameter) {

        return m_configurationObjects.containsKey(parameter);
    }

    /**
     * Load the parameters from the given input stream, which must be in property file formaet.<p>
     *
     * @param input the stream to load the input from
     * 
     * @throws IOException in case of IO errors reading from the stream
     */
    public void load(InputStream input) throws IOException {

        ParameterReader reader = null;

        try {
            reader = new ParameterReader(new InputStreamReader(input, CmsEncoder.ENCODING_ISO_8859_1));

        } catch (UnsupportedEncodingException ex) {

            reader = new ParameterReader(new InputStreamReader(input));
        }

        while (true) {
            String line = reader.readParameter();
            if (line == null) {
                return; // EOF
            }
            int equalSign = line.indexOf('=');

            if (equalSign > 0) {
                String key = line.substring(0, equalSign).trim();
                String value = line.substring(equalSign + 1).trim();

                if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                    continue;
                }

                addParameter(key, value);
            }
        }
    }

    /**
     * Set a parameter for this configuration.<p>
     * 
     * If the parameter already exists then the existing value will be replaced.<p>
     *
     * @param parameter the parameter to set
     * @param value the value to set
     */
    public void setParameter(String parameter, String value) {

        clearParameter(parameter);
        addParameter(parameter, value);
    }

    /**
     * Returns the number of parameters in this configuration.<p>
     * 
     * @return the number of parameters in this configuration
     */
    public int size() {

        return m_configurationObjects.size();
    }

    /**
     * Adds a parameter, parsing the value if required.<p>
     * 
     * @param parameter the parameter to add
     * @param value the value of the parameter
     */
    private void addParameterInternal(String parameter, String value) {

        Object currentObj = m_configurationObjects.get(parameter);
        String currentStr = m_configurationStrings.get(parameter);

        if (currentObj instanceof String) {
            // one object already in map - convert it to a list
            List<String> values = new ArrayList<String>(2);
            values.add(currentStr);
            values.add(value);
            m_configurationObjects.put(parameter, values);
            m_configurationStrings.put(parameter, currentStr + ParameterTokenizer.COMMA + value);
        } else if (currentObj instanceof List) {
            // already a list - just add the new token
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>)currentObj;
            list.add(value);
            m_configurationStrings.put(parameter, currentStr + ParameterTokenizer.COMMA + value);
        } else {
            m_configurationObjects.put(parameter, value);
            m_configurationStrings.put(parameter, value);
        }
    }
}
