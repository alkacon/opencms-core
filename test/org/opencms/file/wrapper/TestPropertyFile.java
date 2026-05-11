/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
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
import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit test for the <code>{@link org.opencms.file.wrapper.CmsResourceWrapperUtils#createPropertyFile(org.opencms.file.CmsObject, org.opencms.file.CmsResource, String)}</code>
 * method.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPropertyFile extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Test if line breaks in the values of properties appears correctly in the generated property file.<p>
     *
     * @throws Throwable if an error occurs while the test is running
     */
    @Test
    @Order(1)
    public void testEscapedCharacters() throws Throwable {

        String resourcename = "/folder1/subfolder11/index.html";
        getCmsObject().lockResource(resourcename);

        String propertyValue = "Start \n \t \r \" \' \\ \u2297 \\\" \\\' End";

        CmsProperty property = new CmsProperty();
        property.setName("Description");
        property.setStructureValue(propertyValue);
        getCmsObject().writePropertyObject(resourcename, property);

        CmsResource res = getCmsObject().readResource(resourcename);
        CmsFile propFile = CmsResourceWrapperUtils.createPropertyFile(
            getCmsObject(),
            res,
            res.getRootPath() + ".properties");

        String content = CmsEncoder.createString(propFile.getContents(), CmsEncoder.ENCODING_UTF_8);

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
    @Test
    @Order(2)
    public void testReadUnicodeChars() throws Throwable {

        String resourcename = "/folder1/subfolder11/index.html";
        getCmsObject().lockResource(resourcename);

        String propertyValue = "\u00E4\u00F6\u00FC \u00C4\u00D6\u00DC \u00DF\u00DF\u00DF \u20AC\u20AC\u20AC";

        CmsProperty property = new CmsProperty();
        property.setName("Description");
        property.setStructureValue(propertyValue);
        getCmsObject().writePropertyObject(resourcename, property);

        CmsResource res = getCmsObject().readResource(resourcename);
        CmsFile propFile = CmsResourceWrapperUtils.createPropertyFile(
            getCmsObject(),
            res,
            res.getRootPath() + ".properties");

        String content = CmsEncoder.createString(propFile.getContents(), CmsEncoder.ENCODING_UTF_8);

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
    @Test
    @Order(3)
    public void testWriteUnicodeChars() throws Throwable {

        String resourcename = "/folder1/subfolder11/index.html";
        String propValue = "\u00E4\u00F6\u00FC \u00C4\u00D6\u00DC \u00DF\u00DF\u00DF \u20AC\u20AC\u20AC";

        StringBuffer content = new StringBuffer();
        content.append("Description.i=");
        content.append(propValue);

        CmsResourceWrapperUtils.writePropertyFile(
            getCmsObject(),
            resourcename,
            content.toString().getBytes(CmsEncoder.ENCODING_UTF_8));

        CmsProperty prop = getCmsObject().readPropertyObject(resourcename, "Description", false);
        assertEquals(propValue, prop.getStructureValue());
    }
}
