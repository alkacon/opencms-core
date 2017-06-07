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

package org.opencms.main;

import org.opencms.test.OpenCmsTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Unit tests the {@link org.opencms.main.OpenCmsUrlServletFilter}. */
public class TestOpenCmsUrlServletFilter extends OpenCmsTestCase {

    /** Servlet context. */
    static String SERVLETCONTEXT = "/opencms";
    /** Export path. */
    static String EXPORTPATH = "/export";
    /** Servlet name prefixed with /. */
    static String SERVLETPATH = "/opencms";
    /** resources folder. */
    private static String RESOURCES_FOLDER = "/resources/";
    /** VAADIN folder. */
    private static String VAADIN_FOLDER = "/VAADIN/";
    /** WebDav folder. */
    private static String WEBDAV_FOLDER = "/webdav";
    /** workplace folder. */
    private static String WORKPLACE_FOLDER = "/workplace";
    /** The default exclude prefixes. */
    private static String[] DEFAULT_EXCLUDE_PREFIXES = new String[] {
        EXPORTPATH,
        SERVLETPATH,
        RESOURCES_FOLDER,
        VAADIN_FOLDER,
        WEBDAV_FOLDER,
        WORKPLACE_FOLDER};
    /** Test url prefix. */
    private static String TESTPREFIX_ONE = "/test1";
    /** Test url prefix. */
    private static String TESTPREFIX_TWO = "/test2/";
    /** Test url prefix. */
    private static String TESTPREFIX_THREE = "/test2";
    /** Additional configuration as can be given via the filter's init-param. */
    private static String ADDITIONAL_CONFIG = "/test1|/test2/";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestOpenCmsUrlServletFilter(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestOpenCmsUrlServletFilter.class.getName());

        suite.addTest(new TestOpenCmsUrlServletFilter("testDefaultRegex"));
        suite.addTest(new TestOpenCmsUrlServletFilter("testRegexWithConfiguration"));

        return suite;
    }

    /**
     * Test default regex.
     */
    public void testDefaultRegex() {

        System.out.println("Testing the Regex for URL rewriting via the servlet filter");

        // Test
        String defaultRegex = OpenCmsUrlServletFilter.createRegex(SERVLETCONTEXT, DEFAULT_EXCLUDE_PREFIXES, null);

        // default prefixes
        matches(defaultRegex, SERVLETPATH, true);
        matches(defaultRegex, RESOURCES_FOLDER, true);
        matches(defaultRegex, WEBDAV_FOLDER, true);
        matches(defaultRegex, VAADIN_FOLDER, true);
        matches(defaultRegex, WORKPLACE_FOLDER, true);
        matches(defaultRegex, EXPORTPATH, true);

        // additional prefixes
        matches(defaultRegex, TESTPREFIX_ONE, false);
        matches(defaultRegex, TESTPREFIX_TWO, false);
        matches(defaultRegex, TESTPREFIX_THREE, false);

    }

    /**
     * Test regex build with extra configuration.<p>
     */
    public void testRegexWithConfiguration() {

        System.out.println("Testing the Regex for URL rewriting via the servlet filter with extra configuration");

        // Test
        String defaultRegex = OpenCmsUrlServletFilter.createRegex(
            SERVLETCONTEXT,
            DEFAULT_EXCLUDE_PREFIXES,
            ADDITIONAL_CONFIG);

        // default prefixes
        matches(defaultRegex, SERVLETPATH, true);
        matches(defaultRegex, RESOURCES_FOLDER, true);
        matches(defaultRegex, WEBDAV_FOLDER, true);
        matches(defaultRegex, VAADIN_FOLDER, true);
        matches(defaultRegex, WORKPLACE_FOLDER, true);
        matches(defaultRegex, EXPORTPATH, true);

        // additional prefixes
        matches(defaultRegex, TESTPREFIX_ONE, true);
        matches(defaultRegex, TESTPREFIX_TWO, true);
        matches(defaultRegex, TESTPREFIX_THREE, false);

    }

    /**
     * @param regex The regex.
     * @param prefix The folder (prefix) to check.
     * @param shouldMatch flag, indicating if the folder should match or not.
     */
    private void matches(String regex, String prefix, boolean shouldMatch) {

        String url = SERVLETCONTEXT + prefix;
        System.out.println(
            "Testing for url (with context): " + url + " (should " + (shouldMatch ? "" : " not ") + "match).");
        if (shouldMatch) {
            assertTrue("Folder " + prefix + " should be matched.", url.matches(regex));
        } else {
            assertFalse("Folder " + prefix + " should not be matched.", url.matches(regex));
        }

    }

}
