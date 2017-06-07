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

package org.opencms.db;

import org.opencms.test.OpenCmsTestCase;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests that checks the "query.properties" files used by the various drivers for correct
 * format.
 * <p>
 *
 * This is no code functionality test but a configuration validation that was inspired by a 1 hour
 * debug session caused by a query.properties file with a trailing tab after the "escape linebreak"
 * backslash.
 * <p>
 *
 * Currently the following checks are made:
 * <ul>
 * <li> <b>Invalid linebreak escape</b><br>
 * If a linebreak is escaped by a backslash there must not follow any character in that line. </li>
 * <li> <b>Invalid comment line</b><br>
 * If a comment character is found it has to be the first character of the line or
 * <ul>
 * <li> The characters before it have to be fully "trimmable" (Unicode General Category Separator,
 * Space [Zs]). </li>
 * <li> The characters before it are a valid key - value pair in properties notation. </li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 */
public class TestQueryProperties extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.
     * <p>
     *
     * @param arg0 JUnit parameters
     */
    public TestQueryProperties(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.
     * <p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestQueryProperties.class.getName());

        suite.addTest(new TestQueryProperties("testQueryPropertiesGeneric"));
        suite.addTest(new TestQueryProperties("testQueryPropertiesMssql"));
        suite.addTest(new TestQueryProperties("testQueryPropertiesMysql"));
        suite.addTest(new TestQueryProperties("testQueryPropertiesOracle"));
        suite.addTest(new TestQueryProperties("testQueryPropertiesPostgresql"));

        return suite;
    }

    /**
     * Test the generic query.properties file within the workspace for format errors.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testQueryPropertiesGeneric() throws Exception {

        // generic
        parseQueryProperties("org/opencms/db/generic/query.properties");

    }

    /**
     * Test the mssql query.properties file within the workspace for format errors.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testQueryPropertiesMssql() throws Exception {

        // mssql
        parseQueryProperties("org/opencms/db/mssql/query.properties");
    }

    /**
     * Test the mysql query.properties file within the workspace for format errors.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testQueryPropertiesMysql() throws Exception {

        // mysql
        parseQueryProperties("org/opencms/db/mysql/query.properties");
    }

    /**
     * Test the oracle query.properties file within the workspace for format errors.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testQueryPropertiesOracle() throws Exception {

        // oracle8
        parseQueryProperties("org/opencms/db/oracle/query.properties");
    }

    /**
     * Test the postgresql query.properties file within the workspace for format errors.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testQueryPropertiesPostgresql() throws Exception {

        // postgresql
        parseQueryProperties("org/opencms/db/postgresql/query.properties");
    }

    /**
     * Parses the key value pair string.<p>
     *
     * @param keyValue the key value pair string
     *
     * @throws ParseException if the key value pair string doesn't match the pattern 'key =value'
     */
    private void parseKeyValue(String keyValue) throws ParseException {

        StringTokenizer tokenizer = new StringTokenizer(keyValue, "=:     ", false);
        if (tokenizer.countTokens() != 2) {
            throw new ParseException("Illegal key value pair " + keyValue, 0);
        }
    }

    /**
     * Implementation of the checks to perform.<p>
     *
     * @param fileName the property file name
     *
     * @throws Exception if something goes wrong
     */
    private void parseQueryProperties(String fileName) throws Exception {

        LineNumberReader reader = new LineNumberReader(
            new InputStreamReader(
                TestQueryProperties.class.getClassLoader().getResourceAsStream(fileName),
                Charset.forName("ISO-8859-1")));
        String read;
        int len;
        int count = 0;
        int lastEscape = 0;
        while ((read = reader.readLine()) != null) {
            len = read.length();
            count++;
            lastEscape = read.lastIndexOf('\\');

            if (read.trim().length() > 0) {
                // filter comments
                int firstSharp = read.indexOf('#');
                if (firstSharp > -1) {
                    String prefix = read.substring(0, firstSharp).trim();
                    if (prefix.length() > 0) {
                        // check 1: invalid key value pair before comment char
                        try {
                            parseKeyValue(prefix);
                        } catch (ParseException pe) {
                            throw new ParseException(
                                "Bad format in file " + fileName + ", line " + count + ": " + pe.getMessage(),
                                count);
                        }
                    } else {
                        // valid comment - only line
                        continue;
                    }
                } else {
                    // no omment line
                }
            }

            // check 2: invalid attempt to escape a line break, something follows
            if (lastEscape != -1) {
                if (lastEscape != (len - 1)) {
                    throw new ParseException(
                        "Bad format in file "
                            + fileName
                            + ", line "
                            + count
                            + ": Line termination escape '\\' is followed by further characters.",
                        count);
                }
            }
            // further checks if desired
        }
        reader.close();
    }
}