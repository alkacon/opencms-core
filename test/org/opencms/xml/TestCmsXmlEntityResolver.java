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

package org.opencms.xml;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms XML entity resolver.<p>
 *
 */
public class TestCmsXmlEntityResolver extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlEntityResolver(String arg0) {

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
        suite.setName(TestCmsXmlEntityResolver.class.getName());

        suite.addTest(new TestCmsXmlEntityResolver("testRemoveNestedSubschemaFromCacheIssue"));

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
     * Tests the "wrong version of nested subschema still cached after change in VFS" issue.<p>
     *
     * An issue was discovered in the conext of auto correction of XML content:
     * Cached content definition objects where not removed from the cache if
     * a nested subschema was changed.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRemoveNestedSubschemaFromCacheIssue() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the \"wrong version of nested subschema still cached after change in VFS\" issue");

        // this test replaces the predefined article schema with an "articlelist"
        // schema that contains the original article as nested subschema
        // this is require so that the original resource type for the article can be used
        String nestedSchemaUri = "/xmlcontent/subarticle.xsd";
        String schemaUri = "/xmlcontent/article.xsd";
        String xmlContentUri = "/xmlcontent/article_0005.html";

        // copy the original article XML schema
        cms.copyResource(schemaUri, nestedSchemaUri);

        // overwrite the schema with the new version read from the RFS
        String rfsname = "org/opencms/xml/xmlcontent-definition-1.xsd";
        String content = CmsFileUtil.readFile(rfsname, CmsEncoder.ENCODING_ISO_8859_1);
        // write the updated XML schema to the VFS
        CmsFile schemaFile = cms.readFile(schemaUri);
        schemaFile.setContents(content.getBytes(CmsEncoder.ENCODING_UTF_8));
        cms.lockResource(schemaUri);
        cms.writeFile(schemaFile);

        // create a new xml content article with the updated schema
        cms.createResource(xmlContentUri, OpenCmsTestCase.ARTICLE_TYPEID);
        CmsFile xmlContentFile = cms.readFile(xmlContentUri);
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, xmlContentFile);

        // write to stdout for visual control
        System.out.println(xmlContent.toString());

        // now overwrite the NESTED subschema content definition
        String rfsname2 = "org/opencms/xml/xmlcontent-definition-article-mod1.xsd";
        String content2 = CmsFileUtil.readFile(rfsname2, CmsEncoder.ENCODING_ISO_8859_1);
        CmsFile schemaFile2 = cms.readFile(nestedSchemaUri);
        schemaFile2.setContents(content2.getBytes(CmsEncoder.ENCODING_UTF_8));
        cms.writeFile(schemaFile2);

        // read the XML content file again - since the subschema has been changed, the XML must be invalid now
        CmsFile xmlContentFile2 = cms.readFile(xmlContentUri);
        CmsXmlContent xmlContent2 = CmsXmlContentFactory.unmarshal(cms, xmlContentFile2);
        CmsXmlException ex = null;
        try {
            xmlContent2.validateXmlStructure(new CmsXmlEntityResolver(cms));
        } catch (CmsXmlException e) {
            ex = e;
        }
        assertNotNull("Validation of XML did not fail even though nested subschema was changed", ex);

        // now set the "auto correct" runtime property and write the XML (which will auto correct it)
        cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);
        xmlContentFile2 = cms.writeFile(xmlContentFile2);
        xmlContent2 = CmsXmlContentFactory.unmarshal(cms, xmlContentFile2);

        // write to stdout for visual control
        System.out.println(xmlContent2.toString());

        if (xmlContent.toString().equals(xmlContent2.toString())) {
            fail("Content of XML files must not be equal after schema change");
        }
    }
}