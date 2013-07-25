/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;

import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** 
 * Test cases for resource bundles in schemas with a different configured default locale.<p>
 */
public class TestCmsXmlContentResourceBundlesGerman extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContentResourceBundlesGerman(String arg0) {

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
        suite.setName(TestCmsXmlContentResourceBundlesGerman.class.getName());
        suite.addTest(new TestCmsXmlContentResourceBundlesGerman("testReadBundleMessages"));
        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms(
                    "simpletest",
                    "/",
                    getTestDataPath("WEB-INF/config." + getDbProduct() + "/"),
                    getTestDataPath("WEB-INF/config.de/"),
                    true);
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };
        return wrapper;
    }

    /** 
     * Tests whether the bundle messages are correct.<p>
     * 
     * @throws Exception
     */
    public void testReadBundleMessages() throws Exception {

        CmsObject cms = getCmsObject();
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        String content;
        CmsXmlContentDefinition definition;
        I_CmsXmlContentHandler contentHandler;

        // unmarshal content definition with localization in properties and XML
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-1_localized2.xsd",
            CmsEncoder.ENCODING_UTF_8);
        definition = CmsXmlContentDefinition.unmarshal(content, TestCmsXmlContentWithVfs.SCHEMA_SYSTEM_ID_1L1, resolver);

        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsMessages messagesDe = contentHandler.getMessages(Locale.GERMAN);
        String messageText = messagesDe.key("label.author");
        assertContains(messageText, "JETZT");

        CmsMessages messagesEn = contentHandler.getMessages(Locale.ENGLISH);
        messageText = messagesEn.key("label.author");
        assertContains(messageText, "NOW");
    }

}
