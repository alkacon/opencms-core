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

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 */
public class TestCmsHtmlConverter extends OpenCmsTestCase {

    private static final String SIMPLE_HTML = "<h1>Test</h1><div><p>This is a test<p>some content<p>last line</div><pre>Some pre<br>\r\n   More pre\r\n</pre>Final line.";
    // some test Strings
    private static final String STRING_1 = "Test: &#228;&#246;&#252;&#196;&#214;&#220;&#223;";
    private static final String STRING_1_UTF8_RESULT = "Test: \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df";
    private static final String STRING_2 = "Test: &#228;&#246;&#252;&#196;&#214;&#220;&#223;&#8364;";

    private static final String STRING_2_UTF8_RESULT = "Test: \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\u20ac";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsHtmlConverter(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsHtmlConverter.class.getName());

        suite.addTest(new TestCmsHtmlConverter("testISO"));
        suite.addTest(new TestCmsHtmlConverter("testUTF8"));
        suite.addTest(new TestCmsHtmlConverter("testPrettyPrint"));
        suite.addTest(new TestCmsHtmlConverter("testRemoveWordTags"));
        suite.addTest(new TestCmsHtmlConverter("testHrefWhitespaceIssue"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests an issue with white space insertion after href tags.<p>
     *
     * Tidy sometimes inserts unwanted, visible whitespace in a href tag because of
     * the formatting used.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testHrefWhitespaceIssue() throws Exception {

        System.out.println("Testing href whitespace issue");
        CmsHtmlConverter converter = new CmsHtmlConverter(CmsEncoder.ENCODING_UTF_8, CmsHtmlConverter.PARAM_XHTML);
        String input = CmsFileUtil.readFile("org/opencms/util/testConverter_03.html", CmsEncoder.ENCODING_ISO_8859_1);
        // the input has the right (that is no) white-spacing between the tags
        assertContains(input, "</a></code>).");
        String output = converter.convertToString(input);
        System.out.println("----------------");
        System.out.println(output);
        System.out.println("----------------");
        // PARAM_XHTML will cause closing tags on new lines
        assertContains(output, "</a>" + System.getProperty("line.separator") + "</code>).");
    }

    /**
     * Tests conversion of ISO-encoded entities.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testISO() throws Exception {

        System.out.println("Testing US-ASCII conversion");
        CmsHtmlConverter converter = new CmsHtmlConverter(CmsEncoder.ENCODING_US_ASCII, CmsHtmlConverter.PARAM_WORD);
        String convertedHtml1 = converter.convertToString(STRING_1);
        String convertedHtml2 = converter.convertToString(STRING_2);

        assertEquals(STRING_1, convertedHtml1);
        assertEquals(STRING_2, convertedHtml2);
    }

    /**
     * Tests if simple pretty printing works.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testPrettyPrint() throws Exception {

        System.out.println("Testing HTML pretty printing");
        CmsHtmlConverter converter = new CmsHtmlConverter(
            CmsEncoder.ENCODING_ISO_8859_1,
            CmsHtmlConverter.PARAM_ENABLED);
        String result = converter.convertToString(SIMPLE_HTML);

        System.out.println("----------------");
        System.out.println(result);
        System.out.println("----------------");
    }

    /**
     * Tests if all word tags are removed.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testRemoveWordTags() throws Exception {

        System.out.println("Testing Word conversion");
        CmsHtmlConverter converter = new CmsHtmlConverter(
            CmsEncoder.ENCODING_ISO_8859_1,
            CmsHtmlConverter.PARAM_WORD + ";" + CmsHtmlConverter.PARAM_XHTML);

        // read a file and convert it
        File inputfile = new File(getTestDataPath("test2.html"));
        byte[] htmlInput = CmsFileUtil.readFile(inputfile);
        String outputContent = converter.convertToString(htmlInput);
        System.out.println(outputContent);
        // now check if all word specific tags are removed
        assertContainsNot(outputContent, "<o:p>");
        assertContainsNot(outputContent, "<o:smarttagtype");
        assertContainsNot(outputContent, "<?xml:namespace ");
    }

    /**
     * Tests conversion of UTF8-encoded entities.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testUTF8() throws Exception {

        System.out.println("Testing UTF-8 conversion");
        CmsHtmlConverter converter = new CmsHtmlConverter(CmsEncoder.ENCODING_UTF_8, CmsHtmlConverter.PARAM_WORD);
        String convertedHtml1 = converter.convertToString(STRING_1);
        String convertedHtml2 = converter.convertToString(STRING_2);

        assertEquals(STRING_1_UTF8_RESULT, convertedHtml1);
        assertEquals(STRING_2_UTF8_RESULT, convertedHtml2);
    }
}