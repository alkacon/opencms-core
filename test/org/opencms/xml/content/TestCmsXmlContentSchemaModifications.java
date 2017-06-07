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
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for XML content schema changes.<p>
 */
public class TestCmsXmlContentSchemaModifications extends OpenCmsTestCase {

    /**
     * Simple schema data container structure.<p>
     */
    class SchemaDef {

        /** The schema id to use in the OpenCms schema cache. */
        private String m_id;

        /** The name of the schema file in in the RFS. */
        private String m_file;

        /**
         * Creates the schema data container structure.<p>
         *
         * @param id the schema id to use in the OpenCms schema cache
         * @param file the name of the schema file in in the RFS
         */
        public SchemaDef(String id, String file) {

            m_id = id;
            m_file = file;
        }

        /**
         * Returns the name of the schema file in in the RFS.
         *
         * @return the name of the schema file in in the RFS
         */
        public String getFile() {

            return m_file;
        }

        /**
         * Returns the schema id to use in the OpenCms schema cache.
         *
         * @return the schema id to use in the OpenCms schema cache
         */
        public String getId() {

            return m_id;
        }
    }

    /** The schema id. */
    private static final String SCHEMA_SYSTEM_ID_1 = "http://www.opencms.org/test1.xsd";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContentSchemaModifications(String arg0) {

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
        suite.setName(TestCmsXmlContentSchemaModifications.class.getName());

        suite.addTest(new TestCmsXmlContentSchemaModifications("testVfsFile"));
        suite.addTest(new TestCmsXmlContentSchemaModifications("testUsageDemo"));
        suite.addTest(new TestCmsXmlContentSchemaModifications("testAddSchemaNodes"));
        suite.addTest(new TestCmsXmlContentSchemaModifications("testRemoveSchemaNodes"));
        suite.addTest(new TestCmsXmlContentSchemaModifications("testReArrangeSchemaNodes"));
        suite.addTest(new TestCmsXmlContentSchemaModifications("testCombinedChangeSchemaNodes"));
        suite.addTest(new TestCmsXmlContentSchemaModifications("testNestedChangeSchemaNodes"));
        suite.addTest(new TestCmsXmlContentSchemaModifications("testXsdTranslation"));
        suite.addTest(new TestCmsXmlContentSchemaModifications("testMaintainOrderInChoiceAfterSchemaChange"));

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
     * Test adding new nodes to the XML schema.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testAddSchemaNodes() throws Exception {

        echo("Testing adding new nodes to XML schema");

        runTestWithChangedSchema(
            SCHEMA_SYSTEM_ID_1,
            "/testAddSchemaNodes.html",
            "xmlcontent-definition-1.xsd",
            "xmlcontent-1.xml",
            "xmlcontent-definition-1.mod1.xsd",
            "xmlcontent-1.mod1.xml");
    }

    /**
     * Combined modification test for simple (non-nested) XML schema.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testCombinedChangeSchemaNodes() throws Exception {

        echo("Combined modification test for simple (non-nested) XML schema");

        runTestWithChangedSchema(
            SCHEMA_SYSTEM_ID_1,
            "/testCombinedChangeSchemaNodesA.html",
            "xmlcontent-definition-1.xsd",
            "xmlcontent-1.xml",
            "xmlcontent-definition-1.mod4.xsd",
            "xmlcontent-1.mod4.xml");

        runTestWithChangedSchema(
            SCHEMA_SYSTEM_ID_1,
            "/testCombinedChangeSchemaNodesB.html",
            "xmlcontent-definition-1.mod4.xsd",
            "xmlcontent-1.mod4.xml",
            "xmlcontent-definition-1.mod5.xsd",
            "xmlcontent-1.mod5.xml");

        // test a schema change where all nodes become invalid
        runTestWithChangedSchema(
            SCHEMA_SYSTEM_ID_1,
            "/testCombinedChangeSchemaNodesC.html",
            "xmlcontent-definition-1.mod5.xsd",
            "xmlcontent-1.mod5.xml",
            "xmlcontent-definition-1.mod6.xsd",
            "xmlcontent-1.mod6.xml");
    }

    /**
     * Tests if changes in the XML schema structure maintains the order of an existing xsd:choice list.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testMaintainOrderInChoiceAfterSchemaChange() throws Exception {

        echo("Testing if changes in the XML schema structure maintains the order of an existing xsd:choice list.");

        List<SchemaDef> originalSchemas = new ArrayList<SchemaDef>();
        List<SchemaDef> changedSchemas = new ArrayList<SchemaDef>();

        originalSchemas.add(new SchemaDef("http://www.opencms.org/ChoiceElement.xsd", "choicetest-ori-element.xsd"));
        originalSchemas.add(new SchemaDef("http://www.opencms.org/ChoiceTest.xsd", "choicetest-ori.xsd"));

        changedSchemas.add(new SchemaDef("http://www.opencms.org/ChoiceElement.xsd", "choicetest-mod-element.xsd"));
        changedSchemas.add(new SchemaDef("http://www.opencms.org/ChoiceTest.xsd", "choicetest-mod.xsd"));

        runTestWithChangedSchemas(
            "/choicetest-content.xml",
            originalSchemas,
            "choicetest-data-ori.xml",
            changedSchemas,
            "choicetest-data-mod.xml");
    }

    /**
     * Combined modification test for a nested XML schema.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testNestedChangeSchemaNodes() throws Exception {

        echo("Combined modification test for a nested XML schema");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(getCmsObject());

        cacheSchema(
            resolver,
            "http://www.opencms.org/test3.xsd",
            "org/opencms/xml/content/xmlcontent-definition-3.xsd");

        cacheSchema(
            resolver,
            "http://www.opencms.org/test4.xsd",
            "org/opencms/xml/content/xmlcontent-definition-4.xsd");

        runTestWithChangedSchema(
            "http://www.opencms.org/test3.xsd",
            "/testCombinedNestedSchemaNodesA.html",
            "xmlcontent-definition-3.xsd",
            "xmlcontent-4.xml",
            "xmlcontent-definition-3.mod1.xsd",
            "xmlcontent-4.mod1.xml");

        runTestWithChangedSchema(
            "http://www.opencms.org/test3.xsd",
            "/testCombinedNestedSchemaNodesB.html",
            "xmlcontent-definition-3.mod1.xsd",
            "xmlcontent-4.mod1.xml",
            "xmlcontent-definition-3.mod2.xsd",
            "xmlcontent-4.mod2.xml");
    }

    /**
     * Test re-arranging nodes in the XML schema.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testReArrangeSchemaNodes() throws Exception {

        echo("Test re-arranging nodes in the XML schema");

        runTestWithChangedSchema(
            SCHEMA_SYSTEM_ID_1,
            "/testReArrangeSchemaNodes.html",
            "xmlcontent-definition-1.xsd",
            "xmlcontent-1.xml",
            "xmlcontent-definition-1.mod3.xsd",
            "xmlcontent-1.mod3.xml");
    }

    /**
     * Test removing nodes from the XML schema.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testRemoveSchemaNodes() throws Exception {

        echo("Testing removing nodes from an XML schema");

        runTestWithChangedSchema(
            SCHEMA_SYSTEM_ID_1,
            "/testRemoveSchemaNodes.html",
            "xmlcontent-definition-1.xsd",
            "xmlcontent-1.xml",
            "xmlcontent-definition-1.mod2.xsd",
            "xmlcontent-1.mod2.xml");
    }

    /**
     * Demo test for using the XML content correcting API.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testUsageDemo() throws Exception {

        CmsObject cms = getCmsObject();
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // setup steps for demo test 1:
        // filenames to use
        String changedSchema = "org/opencms/xml/content/xmlcontent-definition-1.mod4.xsd";
        String originalFile = "org/opencms/xml/content/xmlcontent-1.xml";
        String filename = "/testUsageDemo.html";
        // cache the changed content definition
        cacheSchema(resolver, SCHEMA_SYSTEM_ID_1, changedSchema);
        // read the XML content from the test directory, usually this would be from the VFS
        String content = CmsFileUtil.readFile(originalFile, CmsEncoder.ENCODING_UTF_8);

        // DEMO TEST 1:

        // assumtion: the schema definition of an existing XML content has changed
        // the XML content is unmarshaled (using the changed schema) and then validated
        // after validation fails, it is corrected using the API and then saved to the VFS

        // unmarshal the XML content (the schema is already "wrong")
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // output the XML content (unmodified version)
        System.out.println(xmlcontent.toString());

        // validate the XML structure - must be invalid because of the schema change
        Exception ex = null;
        try {
            xmlcontent.validateXmlStructure(resolver);
        } catch (CmsXmlException e) {
            ex = e;
        }
        // required exception must haven been thrown or the test is a failure
        assertNotNull(ex);

        // enable "auto correction mode" - this is required or the XML structure will not be fully corrected
        xmlcontent.setAutoCorrectionEnabled(true);
        // now correct the XML
        xmlcontent.correctXmlStructure(cms);

        // output the XML content (modified version)
        System.out.println(xmlcontent.toString());

        // check again if the XML is correct - this time it must work without exception
        xmlcontent.validateXmlStructure(resolver);
        // demo test 1 is finished

        // setup steps for demo test 2:

        // write the content to the VFS
        cms.createResource(
            filename,
            OpenCms.getResourceManager().getResourceType("xmlcontent"),
            xmlcontent.toString().getBytes(xmlcontent.getEncoding()),
            Collections.<CmsProperty> emptyList());
        // change the XML schema definition for the XML content
        // it's important that the schema is changed _before_ the content is unmarshaled
        // in a "real world" use case this should be no problem as the schema will have been changed in another request
        changedSchema = "org/opencms/xml/content/xmlcontent-definition-1.mod5.xsd";
        // update the XML schema cache
        cacheSchema(resolver, SCHEMA_SYSTEM_ID_1, changedSchema);

        // DEMO TEST 2:

        // assumption: a file is to be corrected automatically while writing it to the VFS
        // for this, a special OpenCms request context attribute has been introduced
        // if this is set to a Boolean.TRUE object, the XML content is always corrected while saving it to the VFS
        CmsFile file = cms.readFile(filename);

        // this is not normally required, but illustrates the differences in the API behaviour
        // trying to write the file now (with the changed schema but XML still using the old schema) is not possible
        // this is because of the build-in validation while writing
        ex = null;
        try {
            file = cms.writeFile(file);
        } catch (CmsXmlException e) {
            ex = e;
        }
        // required exception must haven been thrown or the test is a failure
        assertNotNull(ex);

        // now set the "automatic correction" request context attribute
        cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);

        // write the file again - the correction will now be done automatically while writing (no exceptions)
        file = cms.writeFile(file);

        // output the XML content (modified version)
        System.out.println(new String(file.getContents()));
        // demo test 2 is finished
    }

    /**
     * Demo test for using the XML content correcting API.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testVfsFile() throws Exception {

        CmsObject cms = getCmsObject();
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // filenames to use
        String changedSchema = "org/opencms/xml/content/xmlcontent-definition-2.mod.xsd";
        String originalFile = "org/opencms/xml/content/xmlcontent-2.mod.xml";
        String filename = "/testVfsFile.html";
        // cache the changed content definition
        cacheSchema(resolver, "http://www.opencms.org/test2.xsd", changedSchema);
        // read the XML content from the test directory, usually this would be from the VFS
        String content = CmsFileUtil.readFile(originalFile, CmsEncoder.ENCODING_UTF_8);

        // unmarshal the XML content (the schema is already using the "filereference")
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // output the XML content (unmodified version)
        System.out.println(xmlcontent.toString());

        // validate the XML structure - must be invalid because of the schema change
        try {
            xmlcontent.validateXmlStructure(resolver);
            fail("xml content should not be valid");
        } catch (CmsXmlException e) {
            // ignore
        }

        // enable "auto correction mode" - this is required or the XML structure will not be fully corrected
        xmlcontent.setAutoCorrectionEnabled(true);
        // now correct the XML
        xmlcontent.correctXmlStructure(cms);

        // output the XML content (modified version)
        System.out.println(xmlcontent.toString());

        // check again if the XML is correct - this time it must work without exception
        xmlcontent.validateXmlStructure(resolver);

        // write the content to the VFS
        cms.createResource(
            filename,
            OpenCms.getResourceManager().getResourceType("xmlcontent"),
            CmsFileUtil.readFile(originalFile),
            Collections.<CmsProperty> emptyList());

        // assumption: a file is to be corrected automatically while writing it to the VFS
        // for this, a special OpenCms request context attribute has been introduced
        // if this is set to a Boolean.TRUE object, the XML content is always corrected while saving it to the VFS
        CmsFile file = cms.readFile(filename);

        // this is not normally required, but illustrates the differences in the API behaviour
        // trying to write the file now (with the changed schema but XML still using the old schema) is not possible
        // this is because of the build-in validation while writing
        try {
            cms.writeFile(file);
            fail("should fail to write the old xml file");
        } catch (CmsXmlException e) {
            // ok, ignore
        }

        // now set the "automatic correction" request context attribute
        cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);

        // write the file again - the correction will now be done automatically while writing (no exceptions)
        file = cms.writeFile(file);

        // output the XML content (modified version)
        System.out.println(new String(file.getContents()));
    }

    /**
     * Test for using the XSD translation.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testXsdTranslation() throws Exception {

        CmsObject cms = getCmsObject();
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        CmsResourceTranslator oldXsdTranslator = OpenCms.getResourceManager().getXsdTranslator();

        String newSchema = "http://www.alkacon.com/changed-schema.xsd";

        CmsResourceTranslator xsdTranslator = new CmsResourceTranslator(
            new String[] {"s#^http://www\\.opencms\\.org/test1\\.xsd#" + newSchema + "#"},
            false);

        // set modified folder translator
        OpenCms.getResourceManager().setTranslators(
            OpenCms.getResourceManager().getFolderTranslator(),
            OpenCms.getResourceManager().getFileTranslator(),
            xsdTranslator);

        String schema = "org/opencms/xml/content/xmlcontent-definition-1.xsd";
        String rfsFile = "org/opencms/xml/content/xmlcontent-1.xml";
        // cache the content definition
        cacheSchema(resolver, SCHEMA_SYSTEM_ID_1, schema);

        // read the XML content from the test directory, usually this would be from the VFS
        String content = CmsFileUtil.readFile(rfsFile, CmsEncoder.ENCODING_UTF_8);

        // unmarshal the XML content
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        xmlcontent.correctXmlStructure(cms);

        String strContent = xmlcontent.toString();
        // output the XML content (modified version)
        System.out.println(strContent);

        assertTrue("Translated XSD schema not found", strContent.indexOf(newSchema) > 0);

        // restore original XSD translator
        OpenCms.getResourceManager().setTranslators(
            OpenCms.getResourceManager().getFolderTranslator(),
            OpenCms.getResourceManager().getFileTranslator(),
            oldXsdTranslator);
    }

    /**
     * Compares a given XML content with the contents of a file in the RFS and asserts the XML is identical.<p>
     *
     * @param xmlcontent the XML content to compare
     * @param filename the RFS name of the file to compare the XML content with
     * @param resolver the XML entitiy resolver to use
     *
     * @throws Exception if something goes wrong
     */
    private void assertXmlContent(CmsXmlContent xmlcontent, String filename, CmsXmlEntityResolver resolver)
    throws Exception {

        System.out.println(xmlcontent.toString());
        String content = CmsFileUtil.readFile(filename, CmsEncoder.ENCODING_UTF_8);
        assertEquals(
            CmsXmlUtils.unmarshalHelper(xmlcontent.toString(), resolver),
            CmsXmlUtils.unmarshalHelper(content, resolver));
    }

    /**
     * Updates the OpenCms XML entity resolver cache with a changed XML schema id.<p>
     *
     * @param resolver the OpenCms XML entity resolver to use
     * @param id the XML schema id to update in the resolver
     * @param filename the name of the file in the RFS where to read the new schema content from
     *
     * @throws Exception if something goes wrong
     */
    private void cacheSchema(CmsXmlEntityResolver resolver, String id, String filename) throws Exception {

        // fire "clear cache" event to clear up previously cached schemas
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>()));
        // read the XML from the given file and store it in the resolver
        String content = CmsFileUtil.readFile(filename, CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, id, resolver);
        CmsXmlEntityResolver.cacheSystemId(id, definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
    }

    /**
     * Reads an XML content with the original schema, then changes the schema and corrects the XML content
     * with the new schema.<p>
     *
     * @param schemaId the XML schema id for the schema cache
     * @param filename the filename to write the XML content to in the OpenCms VFS
     * @param originalSchema the location of the original schema in the RFS
     * @param originalFile the location of the original XML file in the RFS
     * @param changedSchema the location of the changed schema in the RFS
     * @param changedFile the location of the changed XML file in the RFS (for comparison)
     *
     * @throws Exception if something goes wrong
     */
    private void runTestWithChangedSchema(
        String schemaId,
        String filename,
        String originalSchema,
        String originalFile,
        String changedSchema,
        String changedFile) throws Exception {

        List<SchemaDef> originalSchemas = new ArrayList<SchemaDef>();
        List<SchemaDef> changedSchemas = new ArrayList<SchemaDef>();

        originalSchemas.add(new SchemaDef(schemaId, originalSchema));
        changedSchemas.add(new SchemaDef(schemaId, changedSchema));

        runTestWithChangedSchemas(filename, originalSchemas, originalFile, changedSchemas, changedFile);
    }

    /**
     * Reads an XML content with a list of original schemas, then changes the schemas and corrects the XML content
     * with the new schemas.<p>
     *
     * @param filename the filename to write the XML content to in the OpenCms VFS
     * @param originalSchemas the list of the original schemas (id, name in RFS)
     * @param originalFile the location of the original XML file in the RFS
     * @param changedSchemas the list of the changed schemas (id, name in RFS)
     * @param changedFile the location of the changed XML file in the RFS (for comparison)
     *
     * @throws Exception if something goes wrong
     */
    private void runTestWithChangedSchemas(
        String filename,
        List<SchemaDef> originalSchemas,
        String originalFile,
        List<SchemaDef> changedSchemas,
        String changedFile) throws Exception {

        CmsObject cms = getCmsObject();
        String rfsPrefix = "org/opencms/xml/content/";

        // append default path to the filenames
        originalFile = rfsPrefix + originalFile;
        changedFile = rfsPrefix + changedFile;

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        // cache the ORIGINAL content definitions
        for (SchemaDef schema : originalSchemas) {
            cacheSchema(resolver, schema.getId(), rfsPrefix + schema.getFile());
        }

        // now create the XML content
        String content = CmsFileUtil.readFile(originalFile, CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontentOri = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure of the original content
        CmsXmlUtils.validateXmlStructure(content.getBytes(CmsEncoder.ENCODING_UTF_8), resolver);

        // save the XML content to the VFS
        cms.createResource(
            filename,
            OpenCms.getResourceManager().getResourceType("xmlcontent"),
            xmlcontentOri.toString().getBytes(xmlcontentOri.getEncoding()),
            Collections.<CmsProperty> emptyList());

        CmsFile file = cms.readFile(filename);
        CmsXmlContent xmlcontentRead = CmsXmlContentFactory.unmarshal(cms, file);

        // enable the XML auto correction mode on save
        cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);

        // write the file, it should not be changed
        cms.writeFile(file);
        assertXmlContent(xmlcontentRead, originalFile, resolver);

        // cache the CHANGED content definitions
        for (SchemaDef schema : changedSchemas) {
            cacheSchema(resolver, schema.getId(), rfsPrefix + schema.getFile());
        }

        // validate the general XML structure of the modified content
        CmsXmlUtils.validateXmlStructure(CmsFileUtil.readFile(changedFile), resolver);

        // write the file again, it should be changed according to the changed schema
        cms.writeFile(file);

        file = cms.readFile(filename);
        CmsXmlContent xmlcontentUpdated = CmsXmlContentFactory.unmarshal(cms, file);
        assertXmlContent(xmlcontentUpdated, changedFile, resolver);
    }
}