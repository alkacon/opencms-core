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

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the <code>{@link org.opencms.file.wrapper.CmsResourceWrapperUtils#createPropertyFile(org.opencms.file.CmsObject, org.opencms.file.CmsResource, String)}</code>
 * method.<p>
 */
public class TestPropertyFile extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestPropertyFile(String arg0) {

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
        suite.setName(TestPropertyFile.class.getName());

        suite.addTest(new TestPropertyFile("testEscapedCharacters"));
        suite.addTest(new TestPropertyFile("testReadUnicodeChars"));
        suite.addTest(new TestPropertyFile("testWriteUnicodeChars"));

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
     * Test if line breaks in the values of properties appears correctly in the generated property file.<p>
     *
     * @throws Throwable if an error occurs while the test is running
     */
    public void testEscapedCharacters() throws Throwable {

        String resourcename = "/folder1/subfolder11/index.html";
        getCmsObject().lockResource(resourcename);

        String propertyValue = "Start \n \t \r \" \' \\ \u2297 \\\" \\\' End";

        // set a property with a line break
        CmsProperty property = new CmsProperty();
        property.setName("Description");
        property.setStructureValue(propertyValue);
        getCmsObject().writePropertyObject(resourcename, property);

        // create the property file
        CmsResource res = getCmsObject().readResource(resourcename);
        CmsFile propFile = CmsResourceWrapperUtils.createPropertyFile(
            getCmsObject(),
            res,
            res.getRootPath() + ".properties");

        String content = CmsEncoder.createString(propFile.getContents(), CmsEncoder.ENCODING_UTF_8);

        // find correct line in the content
        int pos = content.indexOf("Description.i");
        assertTrue(pos >= 0);

        int start = content.indexOf("=", pos) + 1;
        String expected = "Start \\n \\t \\r \" \' \\ \u2297 \\\" \\\' End";
        assertEquals(expected, content.substring(start, start + expected.length()));
    }

    /**
     * Test if unicode characters are read correctly in the property files.<p>
     *
     * @throws Throwable if an error occurs while the test is running
     */
    public void testReadUnicodeChars() throws Throwable {

        String resourcename = "/folder1/subfolder11/index.html";
        getCmsObject().lockResource(resourcename);

        String propertyValue = "\u00E4\u00F6\u00FC \u00C4\u00D6\u00DC \u00DF\u00DF\u00DF \u20AC\u20AC\u20AC";

        // set a property with a line break
        CmsProperty property = new CmsProperty();
        property.setName("Description");
        property.setStructureValue(propertyValue);
        getCmsObject().writePropertyObject(resourcename, property);

        // create the property file
        CmsResource res = getCmsObject().readResource(resourcename);
        CmsFile propFile = CmsResourceWrapperUtils.createPropertyFile(
            getCmsObject(),
            res,
            res.getRootPath() + ".properties");

        String content = CmsEncoder.createString(propFile.getContents(), CmsEncoder.ENCODING_UTF_8);

        // find correct line in the content
        int pos = content.indexOf("Description.i");
        assertTrue(pos >= 0);

        int start = content.indexOf("=", pos) + 1;
        assertEquals(propertyValue, content.substring(start, start + propertyValue.length()));
    }

    /**
     * Test if unicode characters are written correctly in the property files.<p>
     *
     * @throws Throwable if an error occurs while the test is running
     */
    public void testWriteUnicodeChars() throws Throwable {

        String resourcename = "/folder1/subfolder11/index.html";
        String propValue = "\u00E4\u00F6\u00FC \u00C4\u00D6\u00DC \u00DF\u00DF\u00DF \u20AC\u20AC\u20AC";

        // create property file with one entry
        StringBuffer content = new StringBuffer();
        content.append("Description.i=");
        content.append(propValue);

        // write the property file to the resource
        CmsResourceWrapperUtils.writePropertyFile(
            getCmsObject(),
            resourcename,
            content.toString().getBytes(CmsEncoder.ENCODING_UTF_8));

        // read property
        CmsProperty prop = getCmsObject().readPropertyObject(resourcename, "Description", false);
        assertEquals(propValue, prop.getStructureValue());
    }
}
