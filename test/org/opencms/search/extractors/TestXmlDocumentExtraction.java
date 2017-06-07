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

package org.opencms.search.extractors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.documents.CmsDocumentXmlContent;
import org.opencms.search.documents.CmsDocumentXmlPage;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the text extraction for <code>xmlpage</code> and <code>xmlcontent</code> resources.<p>
 *
 */
public class TestXmlDocumentExtraction extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestXmlDocumentExtraction(String arg0) {

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
        suite.setName(TestXmlDocumentExtraction.class.getName());

        suite.addTest(new TestXmlDocumentExtraction("textXmlPageExtraction"));
        suite.addTest(new TestXmlDocumentExtraction("textXmlContentExtraction"));

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
     * Tests the <code>xmlpage</code> content extraction.<p>
     *
     * @throws Exception if the test fails
     */
    public void textXmlPageExtraction() throws Exception {

        CmsDocumentXmlPage doc = new CmsDocumentXmlPage("xmlpage");
        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource("/folder1/page4.html");
        CmsSearchIndex index = new CmsSearchIndex();
        index.setLocale(Locale.ENGLISH);

        I_CmsExtractionResult extractionResult = doc.extractContent(cms, resource, index);
        Map<String, String> items = extractionResult.getContentItems();
        assertEquals(3, items.size());
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_CONTENT));
        assertTrue(items.containsKey("body"));
        assertTrue(items.containsKey("special"));
    }

    /**
     * Tests the <code>xmlcontent</code> content extraction.<p>
     *
     * @throws Exception if the test fails
     */
    public void textXmlContentExtraction() throws Exception {

        CmsDocumentXmlContent doc = new CmsDocumentXmlContent("xmlcontent");
        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource("/xmlcontent/article_0003.html");
        CmsSearchIndex index = new CmsSearchIndex();
        index.setLocale(Locale.ENGLISH);

        I_CmsExtractionResult extractionResult = doc.extractContent(cms, resource, index);
        Map<String, String> items = extractionResult.getContentItems();
        Iterator<Map.Entry<String, String>> i = items.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, String> entry = i.next();
            System.out.println(entry.getKey());
        }

        assertEquals(7, items.size());
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_CONTENT));
        assertTrue(items.containsKey("Title[1]"));
        assertTrue(items.containsKey("Teaser[1]"));
        assertTrue(items.containsKey("Teaser[2]"));
        assertTrue(items.containsKey("Teaser[3]"));
        assertTrue(items.containsKey("Text[1]"));
        assertTrue(items.containsKey("Author[1]"));
    }
}