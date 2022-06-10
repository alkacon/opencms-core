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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;

/**
 * Tests the OpenCms XML contents with real VFS operations.<p>
 *
 */
public class TestCmsXmlContentVersions extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContentVersions(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestCmsXmlContentVersions.class, "simpletest", "/");
    }

    /**
     * Tests that the schema version is set in newly created contents.
     *
     * @throws Exception if something goes wrong
     */
    public void testNewFileVersion() throws Exception {

        CmsObject cms = getCmsObject();

        I_CmsResourceType plain = OpenCms.getResourceManager().getResourceType("plain");
        I_CmsResourceType xmlcontent = OpenCms.getResourceManager().getResourceType("xmlcontent");
        I_CmsResourceType folder = OpenCms.getResourceManager().getResourceType("folder");
        List<CmsProperty> noProperties = new ArrayList<>();
        String name = "testNewFileVersion";
        String base = "/system/" + name + "/";
        try {
            cms.createResource(base, folder);
            if (!cms.existsResource("/system/news.xsl")) {
                cms.createResource("/system/news.xsl", plain, readTestFile("news.xsl"), noProperties);
            }
            cms.createResource(base + "news.xsd", plain, readTestFile("news-v1.xsd"), noProperties);
            CmsResource contentRes = createContent(cms, base + "news.xml", base + "news.xsd");
            Locale en = Locale.ENGLISH;
            org.dom4j.Document doc = CmsXmlUtils.unmarshalHelper(
                cms.readFile(base + "news.xml").getContents(),
                new CmsXmlEntityResolver(cms));
            assertEquals("1", doc.getRootElement().attributeValue("version"));
        } finally {
            cms.deleteResource("/system/news.xsl", CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.deleteResource(base, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }

    }

    /**
     * Tests the version transformation.
     *
     * @throws Exception if something goes wrong
     */
    public void testVersionTransformation() throws Exception {

        CmsObject cms = getCmsObject();

        I_CmsResourceType plain = OpenCms.getResourceManager().getResourceType("plain");
        I_CmsResourceType folder = OpenCms.getResourceManager().getResourceType("folder");
        List<CmsProperty> noProperties = new ArrayList<>();
        String name = "testVersionTransformation";
        String base = "/system/" + name + "/";
        try {
            cms.createResource(base, folder);
            if (!cms.existsResource("/system/news.xsl")) {
                cms.createResource("/system/news.xsl", plain, readTestFile("news.xsl"), noProperties);
            }
            cms.createResource(base + "news.xsd", plain, readTestFile("news-v0.xsd"), noProperties);
            CmsResource contentRes = createContent(cms, base + "news.xml", base + "news.xsd");
            Locale en = Locale.ENGLISH;

            CmsXmlContent c = CmsXmlContentFactory.unmarshal(cms, cms.readFile(base + "news.xml"));
            String originalTitle = "This is the original title";
            String originalIntro = "This is the original intro";
            c.getValue("Title", Locale.ENGLISH).setStringValue(cms, originalTitle);
            c.getValue("Intro", Locale.ENGLISH).setStringValue(cms, originalIntro);
            updateFile(cms, base + "news.xml", c.marshal());

            CmsXmlContent c0 = CmsXmlContentFactory.unmarshal(cms, cms.readFile(base + "news.xml"));
            assertTrue("Original content must have 'Title'", c0.hasValue("Title", en));
            assertFalse("Original content must not have 'Heading'", c0.hasValue("Heading", en));

            updateFile(cms, base + "news.xsd", readTestFile("news-v1.xsd"));
            CmsXmlContent c1 = CmsXmlContentFactory.unmarshal(cms, cms.readFile(base + "news.xml"));
            assertEquals("Version must be 1 in transformed content object", 1, c1.getSchemaVersion());
            String title2 = c1.getValue("Heading", en).getStringValue(cms);
            assertFalse("Updated version must not have 'Title'", c1.hasValue("Title", en));
            assertTrue("Updated content must have 'Heading'", c1.hasValue("Heading", en));
            assertEquals("Heading must match original Title", originalTitle, title2);

            byte[] newData = c1.marshal();

            org.dom4j.Document doc = CmsXmlUtils.unmarshalHelper(newData, new CmsXmlEntityResolver(cms));
            assertEquals(
                "Version must be 1 in transformed content XML",
                "1",
                doc.getRootElement().attributeValue("version"));

            updateFile(cms, base + "news.xml", newData);
            c1 = CmsXmlContentFactory.unmarshal(cms, cms.readFile(base + "news.xml"));
            assertTrue(
                "Heading should be using CDATA in saved file",
                new String(cms.readFile(base + "news.xml").getContents(), "UTF-8").contains(
                    "<Heading><![CDATA[" + originalTitle + "]]></Heading>"));

            title2 = c1.getValue("Heading", en).getStringValue(cms);
            assertFalse("Updated version must not have 'Title'", c1.hasValue("Title", en));
            assertTrue("Updated content must have 'Heading'", c1.hasValue("Heading", en));
            assertEquals("Heading must match original Title", originalTitle, title2);
            assertEquals(
                "Intro should have been modified exactly once",
                "modified " + originalIntro,
                c1.getValue("Intro", Locale.ENGLISH).getStringValue(cms));
        } finally {
            cms.deleteResource("/system/news.xsl", CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.deleteResource(base, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }

    }

    /**
     * Tests that the version transformation works when creating a new content with a model resource.
     *
     * @throws Exception
     */
    public void testVersionTransformationWithModelResource() throws Exception {

        CmsObject cms = getCmsObject();

        I_CmsResourceType plain = OpenCms.getResourceManager().getResourceType("plain");
        I_CmsResourceType xmlcontent = OpenCms.getResourceManager().getResourceType("xmlcontent");
        I_CmsResourceType folder = OpenCms.getResourceManager().getResourceType("folder");
        List<CmsProperty> noProperties = new ArrayList<>();
        String name = "testVersionTransformationWithModelResource";
        String base = "/system/" + name + "/";
        try {
            cms.createResource(base, folder);
            if (!cms.existsResource("/system/news.xsl")) {
                cms.createResource("/system/news.xsl", plain, readTestFile("news.xsl"), noProperties);
            }
            cms.createResource(base + "news.xsd", plain, readTestFile("news-v0.xsd"), noProperties);
            CmsResource contentRes = createContent(cms, base + "model.xml", base + "news.xsd");
            Locale en = Locale.ENGLISH;

            CmsXmlContent c = CmsXmlContentFactory.unmarshal(cms, cms.readFile(base + "model.xml"));
            String originalTitle = "This is the original title";
            String originalIntro = "This is the original intro";
            c.getValue("Title", Locale.ENGLISH).setStringValue(cms, originalTitle);
            c.getValue("Intro", Locale.ENGLISH).setStringValue(cms, originalIntro);
            updateFile(cms, base + "model.xml", c.marshal());

            updateFile(cms, base + "news.xsd", readTestFile("news-v1.xsd"));

            cms.getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_MODEL, base + "model.xml");
            cms.createResource(base + "news.xml", xmlcontent, null, new ArrayList<>());
            byte[] newFileData = cms.readFile(base + "news.xml").getContents();
            String newFileDataStr = new String(newFileData, "UTF-8");
            assertTrue(
                "CDATA should have been used for Heading \n newFileData = " + newFileDataStr,
                newFileDataStr.contains("<Heading><![CDATA[" + originalTitle + "]]></Heading>"));

            CmsXmlContent c1 = CmsXmlContentFactory.unmarshal(cms, cms.readFile(base + "news.xml"));
            assertEquals(
                "Should have been the original title",
                originalTitle,
                c1.getValue("Heading", Locale.ENGLISH).getStringValue(cms));
        } finally {
            cms.deleteResource("/system/news.xsl", CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.deleteResource(base, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }

    }

    /**
     * Creates a new content in the VFS.
     *
     * @param cms the CMS context
     * @param filePath the VFS path
     * @param schemaPath the schema path
     * @return the newly created resource
     * @throws Exception if something goes wrong
     */
    private CmsResource createContent(CmsObject cms, String filePath, String schemaPath) throws Exception {

        CmsXmlContentDefinition contentDef = CmsXmlContentDefinition.unmarshal(cms, schemaPath);
        CmsXmlContent content = CmsXmlContentFactory.createDocument(cms, Locale.ENGLISH, "UTF-8", contentDef);
        return cms.createResource(
            filePath,
            OpenCms.getResourceManager().getResourceType("xmlcontent"),
            content.marshal(),
            Collections.emptyList());
    }

    /**
     * Reads data from a test file using the classloader.
     *
     * @param name the file name
     * @return the file data
     * @throws Exception if something goes wrong
     */
    private byte[] readTestFile(String name) throws Exception {

        return CmsFileUtil.readFully(getClass().getResourceAsStream(name), true);
    }

    /**
     * Helper method for writing a file.
     *
     * @param cms the CMS context
     * @param path the VFS path
     * @param content the new file content
     * @throws Exception if something goes wrong
     */
    private void updateFile(CmsObject cms, String path, byte[] content) throws Exception {

        CmsFile file = cms.readFile(path);
        file.setContents(content);
        cms.writeFile(file);

    }

}
