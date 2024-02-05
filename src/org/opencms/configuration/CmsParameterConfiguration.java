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

package org.opencms.configuration;

import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsStringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.dom4j.Element;

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
public class CmsParameterConfiguration extends AbstractMap<String, String> implements Serializable {

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

    /**
     * An empty, immutable parameter configuration.<p>
     */
    public static final CmsParameterConfiguration EMPTY_PARAMETERS = new CmsParameterConfiguration(
        Collections.<String, String> emptyMap(),
        Collections.<String, Serializable> emptyMap());

    /** The serial version id. */
    private static final long serialVersionUID = 294679648036460877L;

    /** The parsed map of parameters where the Strings may have become Objects. */
    private transient Map<String, Serializable> m_configurationObjects;

    /** The original map of parameters that contains only String values. */
    private Map<String, String> m_configurationStrings;

    /**
     * Creates an empty parameter configuration.<p>
     */
    public CmsParameterConfiguration() {

        this(new TreeMap<String, String>(), new TreeMap<String, Serializable>());
    }

    /**
     * Creates a parameter configuration from an input stream.<p>
     *
     * @param in the input stream to create the parameter configuration from
     *
     * @throws IOException in case of errors loading the parameters from the input stream
     */
    public CmsParameterConfiguration(InputStream in)
    throws IOException {

        this();
        load(in);
    }

    /**
     * Creates a parameter configuration from a Map of Strings.<p>
     *
     * @param configuration the map of Strings to create the parameter configuration from
     */
    public CmsParameterConfiguration(Map<String, String> configuration) {

        this();

        for (String key : configuration.keySet()) {

            String value = configuration.get(key);
            add(key, value);
        }
    }

    /**
     * Creates a parameter wrapper by loading the parameters from the specified property file.<p>
     *
     * @param file the path of the file to load
     *
     * @throws IOException in case of errors loading the parameters from the specified property file
     */
    public CmsParameterConfiguration(String file)
    throws IOException {

        this();

        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            load(in);
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
     * Creates a parameter configuration from the given maps.<p>
     *
     * @param strings the String map
     * @param objects the object map
     */
    private CmsParameterConfiguration(Map<String, String> strings, Map<String, Serializable> objects) {

        m_configurationStrings = strings;
        m_configurationObjects = objects;
    }

    /**
     * Returns an unmodifiable version of this parameter configuration.<p>
     *
     * @param original the configuration to make unmodifiable
     *
     * @return an unmodifiable version of this parameter configuration
     */
    public static CmsParameterConfiguration unmodifiableVersion(CmsParameterConfiguration original) {

        return new CmsParameterConfiguration(
            Collections.unmodifiableMap(original.m_configurationStrings),
            original.m_configurationObjects);
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
     * String values separated by a comma "," will NOT be tokenized when this
     * method is used. To create a List of String values for a parameter, call this method
     * multiple times with the same parameter name.<p>
     *
     * @param key the parameter to add
     * @param value the value to add
     */
    public void add(String key, String value) {

        add(key, value, false);
    }

    /**
     * Serializes this parameter configuration for the OpenCms XML configuration.<p>
     *
     * For each parameter, a XML node like this<br>
     * <code>
     * &lt;param name="theName"&gt;theValue&lt;/param&gt;
     * </code><br>
     * is generated and appended to the provided parent node.<p>
     *
     * @param parentNode the parent node where the parameter nodes are appended to
     *
     * @return the parent node
     */
    public Element appendToXml(Element parentNode) {

        return appendToXml(parentNode, null);
    }

    /**
     * Serializes this parameter configuration for the OpenCms XML configuration.<p>
     *
     * For each parameter, a XML node like this<br>
     * <code>
     * &lt;param name="theName"&gt;theValue&lt;/param&gt;
     * </code><br>
     * is generated and appended to the provided parent node.<p>
     *
     * @param parentNode the parent node where the parameter nodes are appended to
     * @param parametersToIgnore if not <code>null</code>,
     *      all parameters in this list are not written to the XML
     *
     * @return the parent node
     */
    public Element appendToXml(Element parentNode, List<String> parametersToIgnore) {

        for (Map.Entry<String, Serializable> entry : m_configurationObjects.entrySet()) {
            String name = entry.getKey();
            // check if the parameter should be ignored
            if ((parametersToIgnore == null) || !parametersToIgnore.contains(name)) {
                // now serialize the parameter name and value
                Object value = entry.getValue();
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> values = (List<String>)value;
                    for (String strValue : values) {
                        // use the original String as value
                        Element paramNode = parentNode.addElement(I_CmsXmlConfiguration.N_PARAM);
                        // set the name attribute
                        paramNode.addAttribute(I_CmsXmlConfiguration.A_NAME, name);
                        // set the text of <param> node
                        paramNode.addText(strValue);
                    }
                } else {
                    // use the original String as value
                    String strValue = get(name);
                    Element paramNode = parentNode.addElement(I_CmsXmlConfiguration.N_PARAM);
                    // set the name attribute
                    paramNode.addAttribute(I_CmsXmlConfiguration.A_NAME, name);
                    // set the text of <param> node
                    paramNode.addText(strValue);
                }
            }
        }

        return parentNode;
    }

    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {

        m_configurationStrings.clear();
        m_configurationObjects.clear();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {

        return m_configurationStrings.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {

        return m_configurationStrings.containsValue(value) || m_configurationObjects.containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {

        return m_configurationStrings.entrySet();
    }

    /**
     * Returns the String associated with the given parameter.<p>
     *
     * @param key the parameter to look up the value for
     *
     * @return the String associated with the given parameter
     */
    @Override
    public String get(Object key) {

        return m_configurationStrings.get(key);
    }

    /**
     * Returns the boolean associated with the given parameter,
     * or the default value in case there is no boolean value for this parameter.<p>
     *
     * @param key the parameter to look up the value for
     * @param defaultValue the default value
     *
     * @return the boolean associated with the given parameter,
     *      or the default value in case there is no boolean value for this parameter
     */
    public boolean getBoolean(String key, boolean defaultValue) {

        Object value = m_configurationObjects.get(key);

        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();

        } else if (value instanceof String) {
            Boolean b = Boolean.valueOf((String)value);
            m_configurationObjects.put(key, b);
            return b.booleanValue();

        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the integer associated with the given parameter,
     * or the default value in case there is no integer value for this parameter.<p>
     *
     * @param key the parameter to look up the value for
     * @param defaultValue the default value
     *
     * @return the integer associated with the given parameter,
     *      or the default value in case there is no integer value for this parameter
     */
    public int getInteger(String key, int defaultValue) {

        Object value = m_configurationObjects.get(key);

        if (value instanceof Integer) {
            return ((Integer)value).intValue();

        } else if (value instanceof String) {
            Integer i = Integer.valueOf((String)value);
            m_configurationObjects.put(key, i);
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
     * @param key the parameter to look up the value for
     *
     * @return the List of Strings associated with the given parameter,
     *      or an empty List in case there is no List of Strings for this parameter
     */
    public List<String> getList(String key) {

        return getList(key, null);
    }

    /**
     * Returns the List of Strings associated with the given parameter,
     * or the default value in case there is no List of Strings for this parameter.<p>
     *
     * The list returned is a copy of the internal data of this object, and as
     * such you may alter it freely.<p>
     *
     * @param key the parameter to look up the value for
     * @param defaultValue the default value
     *
     * @return the List of Strings associated with the given parameter,
     *      or the default value in case there is no List of Strings for this parameter
     */
    public List<String> getList(String key, List<String> defaultValue) {

        Object value = m_configurationObjects.get(key);

        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> result = (List<String>)value;
            return new ArrayList<String>(result);

        } else if (value instanceof String) {
            ArrayList<String> values = new ArrayList<String>(1);
            values.add((String)value);
            m_configurationObjects.put(key, values);
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
     * @param key the parameter to look up the value for
     *
     * @return the raw Object associated with the given parameter,
     *      or <code>null</code> in case there is no Object for this parameter.<p>
     */
    public Object getObject(String key) {

        return m_configurationObjects.get(key);
    }

    /**
     * Creates a new <tt>Properties</tt> object from the existing configuration
     * extracting all key-value pars whose key are prefixed
     * with <tt>keyPrefix</tt>. <p>
     *
     * For this example config:
     *
     * <pre>
     *      # lines starting with # are comments
     *      db.pool.default.jdbcDriver=net.bull.javamelody.JdbcDriver
     *      db.pool.default.connectionProperties.driver=com.mysql.cj.jdbc.Driver
     * </pre>
     *
     * <tt>getPrefixedProperties("db.pool.default.connectionProperties")</tt>
     * will return a <tt>Properties</tt> object with one single entry:
     * <pre>
     *      key:"driver", value:"com.mysql.cj.jdbc.Driver"
     * </pre>
     *
     * @param keyPrefix prefix to match. If it isn't already, it will be
     *           terminated with a dot.  If <tt>null</tt>, it will return
     *           an empty <tt>Properties</tt> instance
     * @return a new <tt>Properties</tt> object with all the entries from this
     *          configuration whose keys math the prefix
     */
    public Properties getPrefixedProperties(String keyPrefix) {

        Properties props = new Properties();
        if (null == keyPrefix) {
            return props;
        }

        String dotTerminatedKeyPrefix = keyPrefix + (keyPrefix.endsWith(".") ? "" : ".");
        for (Map.Entry<String, String> e : entrySet()) {
            String key = e.getKey();
            if ((null != key) && key.startsWith(dotTerminatedKeyPrefix)) {
                String subKey = key.substring(dotTerminatedKeyPrefix.length());
                props.put(subKey, e.getValue());
            }
        }
        return props;
    }

    /**
     * Returns the String associated with the given parameter,
     * or the given default value in case there is no value for this parameter.<p>
     *
     * @param key the parameter to look up the value for
     * @param defaultValue the default value
     *
     * @return the String associated with the given parameter,
     *      or the given default value in case there is no value for this parameter.<p>
     */
    public String getString(String key, String defaultValue) {

        String result = get(key);
        return result == null ? defaultValue : result;
    }

    /**
     * @see java.util.Map#hashCode()
     */
    @Override
    public int hashCode() {

        return m_configurationStrings.hashCode();
    }

    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {

        return m_configurationStrings.keySet();
    }

    /**
     * Load the parameters from the given input stream, which must be in property file format.<p>
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

                add(key, value, true);
            }
        }
    }

    /**
     * Set a parameter for this configuration.<p>
     *
     * If the parameter already exists then the existing value will be replaced.<p>
     *
     * @param key the parameter to set
     * @param value the value to set
     *
     * @return the previous String value from the parameter map
     */
    @Override
    public String put(String key, String value) {

        String result = remove(key);
        add(key, value, false);
        return result;
    }

    /**
     * Merges this parameter configuration with the provided other parameter configuration.<p>
     *
     * The difference form a simple <code>Map&lt;String, String&gt;</code> is that for the parameter
     * configuration, the values of the keys in both maps are merged and kept in the Object store
     * as a List.<p>
     *
     * As result, <code>this</code> configuration will be altered, the other configuration will
     * stay unchanged.<p>
     *
     * @param other the other parameter configuration to merge this configuration with
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> other) {

        for (String key : other.keySet()) {
            boolean tokenize = false;
            if (other instanceof CmsParameterConfiguration) {
                Object o = ((CmsParameterConfiguration)other).getObject(key);
                if (o instanceof List) {
                    tokenize = true;
                }
            }
            add(key, other.get(key), tokenize);
        }
    }

    /**
     * Removes a parameter from this configuration.
     *
     * @param key the parameter to remove
     */
    @Override
    public String remove(Object key) {

        String result = m_configurationStrings.remove(key);
        m_configurationObjects.remove(key);
        return result;
    }

    /**
     * @see java.util.Map#toString()
     */
    @Override
    public String toString() {

        return m_configurationStrings.toString();
    }

    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<String> values() {

        return m_configurationStrings.values();
    }

    /**
     * Add a parameter to this configuration.<p>
     *
     * If the parameter already exists then the value will be added
     * to the existing configuration entry and a List will be created for the values.<p>
     *
     * @param key the parameter to add
     * @param value the value to add
     * @param tokenize decides if a String value should be tokenized or nor
     */
    private void add(String key, String value, boolean tokenize) {

        if (tokenize && (value.indexOf(ParameterTokenizer.COMMA) > 0)) {
            // token contains commas, so must be split apart then added
            ParameterTokenizer tokenizer = new ParameterTokenizer(value);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                addInternal(key, unescape(token));
            }
        } else if (tokenize) {
            addInternal(key, unescape(value));
        } else {
            // token contains no commas, so can be simply added
            addInternal(key, value);
        }
    }

    /**
     * Adds a parameter, parsing the value if required.<p>
     *
     * @param key the parameter to add
     * @param value the value of the parameter
     */
    private void addInternal(String key, String value) {

        Object currentObj = m_configurationObjects.get(key);
        String currentStr = get(key);

        if (currentObj instanceof String) {
            // one object already in map - convert it to a list
            ArrayList<String> values = new ArrayList<String>(2);
            values.add(currentStr);
            values.add(value);
            m_configurationObjects.put(key, values);
            m_configurationStrings.put(key, currentStr + ParameterTokenizer.COMMA + value);
        } else if (currentObj instanceof List) {
            // already a list - just add the new token
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>)currentObj;
            list.add(value);
            m_configurationStrings.put(key, currentStr + ParameterTokenizer.COMMA + value);
        } else {
            m_configurationObjects.put(key, value);
            m_configurationStrings.put(key, value);
        }
    }
}
