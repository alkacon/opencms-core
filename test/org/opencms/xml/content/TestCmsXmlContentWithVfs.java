/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsHtmlWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlStringValue;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms XML contents with real VFS operations.<p>
 *
 */
public class TestCmsXmlContentWithVfs extends OpenCmsTestCase {

    private static final String SCHEMA_SYSTEM_ID_1L1 = "http://www.opencms.org/test1_localized1.xsd";
    private static final String SCHEMA_SYSTEM_ID_1L2 = "http://www.opencms.org/test1_localized2.xsd";
    private static final String SCHEMA_SYSTEM_ID_1L4 = "http://www.opencms.org/test1_localized4.xsd";
    private static final String SCHEMA_SYSTEM_ID_2 = "http://www.opencms.org/test2.xsd";
    private static final String SCHEMA_SYSTEM_ID_3 = "http://www.opencms.org/test3.xsd";
    private static final String SCHEMA_SYSTEM_ID_3B = "http://www.opencms.org/test3b.xsd";
    private static final String SCHEMA_SYSTEM_ID_4 = "http://www.opencms.org/test4.xsd";
    private static final String SCHEMA_SYSTEM_ID_4B = "http://www.opencms.org/test4b.xsd";
    private static final String SCHEMA_SYSTEM_ID_5 = "http://www.opencms.org/test5.xsd";
    private static final String SCHEMA_SYSTEM_ID_6 = "http://www.opencms.org/test6.xsd";
    private static final String SCHEMA_SYSTEM_ID_7 = "http://www.opencms.org/test7.xsd";
    private static final String SCHEMA_SYSTEM_ID_8 = "http://www.opencms.org/test8.xsd";
    private static final String SCHEMA_SYSTEM_ID_9 = "http://www.opencms.org/test9.xsd";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContentWithVfs(String arg0) {

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
        suite.setName(TestCmsXmlContentWithVfs.class.getName());

        suite.addTest(new TestCmsXmlContentWithVfs("testAutoXsd"));
        suite.addTest(new TestCmsXmlContentWithVfs("testAddRemoveElements"));
        suite.addTest(new TestCmsXmlContentWithVfs("testContentHandler"));
        suite.addTest(new TestCmsXmlContentWithVfs("testDefaultOnCreation"));
        suite.addTest(new TestCmsXmlContentWithVfs("testDefaultOnCreationWithNested"));
        suite.addTest(new TestCmsXmlContentWithVfs("testDefaultNested"));
        suite.addTest(new TestCmsXmlContentWithVfs("testNestedSchema"));
        suite.addTest(new TestCmsXmlContentWithVfs("testAddRemoveNestedElements"));
        suite.addTest(new TestCmsXmlContentWithVfs("testAccessNestedElements"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValueIndex"));
        suite.addTest(new TestCmsXmlContentWithVfs("testLayoutWidgetMapping"));
        suite.addTest(new TestCmsXmlContentWithVfs("testLinkResolver"));
        suite.addTest(new TestCmsXmlContentWithVfs("testVarLinkResolver"));
        suite.addTest(new TestCmsXmlContentWithVfs("testEmptyLocale"));
        suite.addTest(new TestCmsXmlContentWithVfs("testCopyMoveRemoveLocale"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValidation"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValidationExtended"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValidationLocale"));
        suite.addTest(new TestCmsXmlContentWithVfs("testMappings"));
        suite.addTest(new TestCmsXmlContentWithVfs("testMappingsWithManyLocales"));
        suite.addTest(new TestCmsXmlContentWithVfs("testMappingsOfNestedContent"));
        suite.addTest(new TestCmsXmlContentWithVfs("testMappingsAsList"));
        suite.addTest(new TestCmsXmlContentWithVfs("testResourceBundle"));
        suite.addTest(new TestCmsXmlContentWithVfs("testResourceBundleFromXml"));
        suite.addTest(new TestCmsXmlContentWithVfs("testResourceBundleFromXmlWithDefault"));
        suite.addTest(new TestCmsXmlContentWithVfs("testResourceMultiBundle"));
        suite.addTest(new TestCmsXmlContentWithVfs("testMacros"));
        suite.addTest(new TestCmsXmlContentWithVfs("testAddFileReference"));
        suite.addTest(new TestCmsXmlContentWithVfs("testXmlContentCreate"));

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
     * Test accessing elements in nested schemas.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testAccessNestedElements() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing access to nested schema values in XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-7.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // add a deep cascaded node
        xmlcontent.addValue(cms, "DeepCascade", Locale.ENGLISH, 0);
        CmsXmlContentValueSequence level0Sequence = xmlcontent.getValueSequence("DeepCascade[1]", Locale.ENGLISH);
        assertEquals(1, level0Sequence.getElementCount());

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // check nested cascade level 1
        CmsXmlContentValueSequence level1Sequence = xmlcontent.getValueSequence(
            "DeepCascade[1]/Cascade",
            Locale.ENGLISH);
        assertEquals(1, level1Sequence.getElementCount());

        // check nested cascade level 2
        CmsXmlContentValueSequence level2Sequence = xmlcontent.getValueSequence(
            "DeepCascade[1]/Cascade[1]/VfsLink",
            Locale.ENGLISH);
        assertEquals(1, level2Sequence.getElementCount());

        // now append an element to the nested element 
        level1Sequence.addValue(cms, 1);
        assertEquals(2, level1Sequence.getElementCount());

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        level0Sequence = xmlcontent.getValueSequence("DeepCascade[1]", Locale.ENGLISH);
        assertEquals(1, level0Sequence.getElementCount());

        // check nested cascade level 1
        level1Sequence = xmlcontent.getValueSequence("DeepCascade[1]/Cascade", Locale.ENGLISH);
        assertEquals(2, level1Sequence.getElementCount());

        // check nested cascade level 2 (for the NEW element)
        level2Sequence = xmlcontent.getValueSequence("DeepCascade[1]/Cascade[2]/VfsLink", Locale.ENGLISH);
        assertEquals(1, level2Sequence.getElementCount());

        // add some values to the level 2 sequence
        level2Sequence.addValue(cms, 0);
        level2Sequence.addValue(cms, 2);
        level2Sequence.addValue(cms, 1);
        assertEquals(4, level2Sequence.getElementCount());

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // check nested cascade level 2 (for the NEW element)
        level2Sequence = xmlcontent.getValueSequence("DeepCascade[1]/Cascade[2]/VfsLink", Locale.ENGLISH);
        assertEquals(4, level2Sequence.getElementCount());

        // now add an optional, deep nested node that has no current value
        level2Sequence = xmlcontent.getValueSequence("DeepCascade[1]/Cascade[2]/Option", Locale.ENGLISH);
        assertEquals(0, level2Sequence.getElementCount());
        level2Sequence.addValue(cms, 0);
        level2Sequence.addValue(cms, 1);
        assertEquals(2, level2Sequence.getElementCount());

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        level2Sequence = xmlcontent.getValueSequence("DeepCascade[1]/Cascade[2]/Option", Locale.ENGLISH);
        assertEquals(2, level2Sequence.getElementCount());

        // now remove the deep cascaded sequence and create a new one
        level0Sequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(1, level0Sequence.getElementCount());
        level0Sequence.removeValue(0);
        assertEquals(0, level0Sequence.getElementCount());

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        level0Sequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(0, level0Sequence.getElementCount());

        // add a new value for the deep cascade
        level0Sequence.addValue(cms, 0);
        assertEquals(1, level0Sequence.getElementCount());

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        level0Sequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(1, level0Sequence.getElementCount());
    }

    /**
     * Test adding a file reference value to an existing xmlcontent.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testAddFileReference() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing adding a file reference value to an existing xmlcontent");

        String filename = "/xmlcontent/article_0001.html";
        String filename2 = "/xmlcontent/article_0002.html";
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        // now read the XML content
        CmsFile file = cms.readFile(filename);
        // needs to be written to assure link id correctness
        cms.lockResource(filename);
        file = cms.writeFile(file);
        String content = new String(file.getContents());
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        xmlcontent.addValue(cms, "Homepage", Locale.ENGLISH, 0);
        CmsXmlVfsFileValue value = (CmsXmlVfsFileValue)xmlcontent.getValue("Homepage", Locale.ENGLISH);
        value.setStringValue(cms, filename + "?a=b&c=d#e");
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        CmsLink link = value.getLink(cms);
        assertEquals(link.getTarget(), cms.getRequestContext().addSiteRoot(filename));
        assertTrue(link.isInternal());
        assertEquals(link.getQuery(), "a=b&c=d");
        assertEquals(link.getAnchor(), "e");
        assertEquals(link.getStructureId(), file.getStructureId());
        assertEquals(value.getStringValue(cms), filename + "?a=b&c=d#e");

        CmsResource res2 = cms.readResource(filename2);
        CmsResource schema = cms.readResource("/xmlcontent/article.xsd");
        List<CmsRelation> links = cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS);
        assertEquals(links.size(), 2);
        assertRelation(new CmsRelation(file, schema, CmsRelationType.XSD), links.get(0));
        assertRelation(new CmsRelation(file, res2, CmsRelationType.HYPERLINK), links.get(1));

        file.setContents(xmlcontent.toString().getBytes());
        cms.lockResource(filename);
        cms.writeFile(file);

        links = cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS);
        assertEquals(links.size(), 3);
        assertRelation(new CmsRelation(file, schema, CmsRelationType.XSD), links.get(0));
        assertRelation(new CmsRelation(file, file, CmsRelationType.XML_WEAK), links.get(1));
        assertRelation(new CmsRelation(file, res2, CmsRelationType.HYPERLINK), links.get(2));

        links = cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS.filterType(CmsRelationType.XML_WEAK));
        assertEquals(links.size(), 1);
        assertRelation(new CmsRelation(file, file, CmsRelationType.XML_WEAK), links.get(0));

        links = cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS.filterType(CmsRelationType.HYPERLINK));
        assertEquals(links.size(), 1);
        assertRelation(new CmsRelation(file, res2, CmsRelationType.HYPERLINK), links.get(0));

        file = cms.readFile(filename);
        content = new String(file.getContents());
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        CmsXmlHtmlValue value2 = (CmsXmlHtmlValue)xmlcontent.getValue("Text", Locale.ENGLISH, 0);
        link = value2.getLinkTable().getLink("link0");
        assertEquals(link.getTarget(), res2.getRootPath());
        assertTrue(link.isInternal());
        assertNull(link.getQuery());
        assertNull(link.getAnchor());
        assertEquals(link.getStructureId(), res2.getStructureId());
    }

    /**
     * Test adding and removing elements from an XML content.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testAddRemoveElements() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing adding and removing elements from an XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-6.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_6, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-6.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        CmsXmlContentValueSequence titleSequence;

        titleSequence = xmlcontent.getValueSequence("Title", Locale.ENGLISH);
        assertEquals("Title", titleSequence.getValue(0).getName());
        assertEquals(1, titleSequence.getElementCount());
        assertEquals(1, titleSequence.getMinOccurs());
        assertEquals(5, titleSequence.getMaxOccurs());
        assertEquals("This is just a modification test", titleSequence.getValue(0).getStringValue(cms));

        CmsXmlStringValue newValue;

        newValue = (CmsXmlStringValue)titleSequence.addValue(cms, 0);
        assertEquals(2, titleSequence.getElementCount());
        assertEquals(newValue, titleSequence.getValue(0));
        newValue.setStringValue(cms, "This is another Value!");

        // now re-create the XML content from the XML document
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // ensure the document structure is as expected
        titleSequence = xmlcontent.getValueSequence("Title", Locale.ENGLISH);
        assertEquals("Title", titleSequence.getValue(0).getName());
        assertEquals(2, titleSequence.getElementCount());
        assertEquals(1, titleSequence.getMinOccurs());
        assertEquals(5, titleSequence.getMaxOccurs());
        assertEquals("This is another Value!", titleSequence.getValue(0).getStringValue(cms));
        assertEquals("This is just a modification test", titleSequence.getValue(1).getStringValue(cms));

        // add an element at the last position
        newValue = (CmsXmlStringValue)titleSequence.addValue(cms, 2);
        newValue.setStringValue(cms, "This is the last value.");
        assertEquals(newValue, titleSequence.getValue(2));
        // add another element at the 2nd position
        newValue = (CmsXmlStringValue)titleSequence.addValue(cms, 1);
        newValue.setStringValue(cms, "This is the 2nd value.");
        assertEquals(newValue, titleSequence.getValue(1));
        assertEquals(4, titleSequence.getElementCount());

        // now re-create the XML content from the XML document
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // ensure the document structure is as expected
        titleSequence = xmlcontent.getValueSequence("Title", Locale.ENGLISH);
        assertEquals("Title", titleSequence.getValue(0).getName());
        assertEquals(4, titleSequence.getElementCount());
        assertEquals(1, titleSequence.getMinOccurs());
        assertEquals(5, titleSequence.getMaxOccurs());
        assertEquals("This is another Value!", titleSequence.getValue(0).getStringValue(cms));
        assertEquals("This is the 2nd value.", titleSequence.getValue(1).getStringValue(cms));
        assertEquals("This is just a modification test", titleSequence.getValue(2).getStringValue(cms));
        assertEquals("This is the last value.", titleSequence.getValue(3).getStringValue(cms));

        // now the optional element
        CmsXmlContentValueSequence optionSequence;

        optionSequence = xmlcontent.getValueSequence("Option", Locale.ENGLISH);
        assertEquals(0, optionSequence.getElementCount());
        assertEquals(0, optionSequence.getMinOccurs());
        assertEquals(2, optionSequence.getMaxOccurs());

        // add an element for the optional element
        newValue = (CmsXmlStringValue)optionSequence.addValue(cms, 0);
        newValue.setStringValue(cms, "Optional value 1");
        assertEquals(newValue, optionSequence.getValue(0));
        // add another element
        newValue = (CmsXmlStringValue)optionSequence.addValue(cms, 0);
        newValue.setStringValue(cms, "Optional value 0");
        assertEquals(newValue, optionSequence.getValue(0));
        assertEquals(2, optionSequence.getElementCount());

        // now re-create the XML content from the XML document
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        optionSequence = xmlcontent.getValueSequence("Option", Locale.ENGLISH);
        assertEquals("Option", optionSequence.getValue(0).getName());
        assertEquals(2, optionSequence.getElementCount());
        assertEquals(0, optionSequence.getMinOccurs());
        assertEquals(2, optionSequence.getMaxOccurs());

        assertEquals("Optional value 0", optionSequence.getValue(0).getStringValue(cms));
        assertEquals("Optional value 1", optionSequence.getValue(1).getStringValue(cms));

        optionSequence.removeValue(1);
        assertEquals(1, optionSequence.getElementCount());
        assertEquals("Optional value 0", optionSequence.getValue(0).getStringValue(cms));

        optionSequence.removeValue(0);
        assertEquals(0, optionSequence.getElementCount());

        // now re-create the XML content from the XML document
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        titleSequence = xmlcontent.getValueSequence("Title", Locale.ENGLISH);
        assertEquals(4, titleSequence.getElementCount());

        titleSequence.removeValue(0);
        titleSequence.removeValue(2);
        assertEquals(2, titleSequence.getElementCount());
        assertEquals("This is the 2nd value.", titleSequence.getValue(0).getStringValue(cms));
        assertEquals("This is just a modification test", titleSequence.getValue(1).getStringValue(cms));

        // now re-create the XML content from the XML document
        content = xmlcontent.toString();
        System.out.println(content);
    }

    /**
     * Test adding and removing elements from an XML content, including nested elements.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testAddRemoveNestedElements() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing adding and removing nested elements from an XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-7.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        CmsXmlContentValueSequence nestedSequence;

        nestedSequence = xmlcontent.getValueSequence("Cascade", Locale.ENGLISH);
        assertEquals(1, nestedSequence.getElementCount());
        I_CmsXmlContentValue newValue;
        newValue = nestedSequence.addValue(cms, 0);
        assertNotNull(newValue);
        assertFalse(newValue.isSimpleType());
        assertEquals(CmsXmlNestedContentDefinition.class.getName(), newValue.getClass().getName());

        // re-create the XML content
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        nestedSequence = xmlcontent.getValueSequence("Cascade", Locale.ENGLISH);
        assertEquals(2, nestedSequence.getElementCount());

        CmsXmlContentValueSequence deepNestedSequence;
        deepNestedSequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(0, deepNestedSequence.getElementCount());

        newValue = deepNestedSequence.addValue(cms, 0);
        assertNotNull(newValue);
        assertFalse(newValue.isSimpleType());
        assertEquals(CmsXmlNestedContentDefinition.class.getName(), newValue.getClass().getName());

        // re-create the XML content
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        deepNestedSequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(1, deepNestedSequence.getElementCount());

        nestedSequence = xmlcontent.getValueSequence("Cascade", Locale.ENGLISH);
        assertEquals(2, nestedSequence.getElementCount());

        nestedSequence.removeValue(1);
        deepNestedSequence.removeValue(0);

        // re-create the XML content
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        deepNestedSequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(0, deepNestedSequence.getElementCount());

        nestedSequence = xmlcontent.getValueSequence("Cascade", Locale.ENGLISH);
        assertEquals(1, nestedSequence.getElementCount());
    }

    /**
     * Tests creation of the automatic XML schema XSD.
     * 
     * @throws Exception in case something goes wrong
     */
    public void testAutoXsd() throws Exception {

        org.opencms.xml.CmsXmlEntityResolver resolver = new org.opencms.xml.CmsXmlEntityResolver(null);
        org.xml.sax.InputSource source = resolver.resolveEntity(
            null,
            org.opencms.xml.CmsXmlContentDefinition.XSD_INCLUDE_OPENCMS);
        byte[] bytes = org.opencms.util.CmsFileUtil.readFully(source.getByteStream());
        String string = org.opencms.i18n.CmsEncoder.createString(bytes, "UTF-8");
        System.out.println(string);
    }

    /**
     * Test using a different XML content handler then the default handler.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testContentHandler() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing individual content handler for XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-3.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_3, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_3, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-3.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        assertTrue(xmlcontent.hasValue("Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("VfsLink", Locale.ENGLISH));
        assertSame(definition.getContentHandler().getClass().getName(), TestXmlContentHandler.class.getName());
    }

    /**
     * Tests locale copy, move and remove operation on an XML content.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testCopyMoveRemoveLocale() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing locale copy, move and remove operation on an XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String iso = "ISO-8859-1";

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-8.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_8, content.getBytes(iso));

        // read an existing (empty) XML content with just one locale node
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", iso);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, iso, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);
        List<Locale> locales = xmlcontent.getLocales();
        assertEquals(1, locales.size());
        assertEquals(Locale.ENGLISH, locales.get(0));

        xmlcontent.copyLocale(Locale.ENGLISH, Locale.GERMANY);
        assertEquals(2, xmlcontent.getLocales().size());
        assertTrue(xmlcontent.hasLocale(Locale.ENGLISH));
        assertTrue(xmlcontent.hasLocale(Locale.GERMANY));

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        xmlcontent.moveLocale(Locale.GERMANY, Locale.FRENCH);
        assertEquals(2, xmlcontent.getLocales().size());
        assertTrue(xmlcontent.hasLocale(Locale.ENGLISH));
        assertTrue(xmlcontent.hasLocale(Locale.FRENCH));
        assertFalse(xmlcontent.hasLocale(Locale.GERMANY));

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        xmlcontent.removeLocale(Locale.ENGLISH);
        assertEquals(1, xmlcontent.getLocales().size());
        assertTrue(xmlcontent.hasLocale(Locale.FRENCH));
        assertFalse(xmlcontent.hasLocale(Locale.ENGLISH));
        assertFalse(xmlcontent.hasLocale(Locale.GERMANY));
        assertEquals(Locale.FRENCH, xmlcontent.getLocales().get(0));

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);
    }

    /**
     * Test default values in the appinfo node using a nested XML content schema.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testDefaultNested() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing for default values in nested XML content schemas");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-4.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_4, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-4.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        System.out.println(xmlcontent.toString());

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        I_CmsXmlContentValue value1;

        value1 = xmlcontent.addValue(cms, "Title", Locale.ENGLISH, 0);
        assertEquals("Test", value1.getStringValue(cms));

        value1 = xmlcontent.addValue(cms, "Cascade[1]/Option", Locale.ENGLISH, 0);
        assertEquals("Default value from the XML", value1.getStringValue(cms));

        // check exact default mappings for nested content
        value1 = xmlcontent.addValue(cms, "Cascade[1]/Option", Locale.ENGLISH, 1);
        assertEquals("Default value from outer content definition", value1.getStringValue(cms));

        // check generic default mappings for nested content
        value1 = xmlcontent.addValue(cms, "Cascade[1]/VfsLink", Locale.ENGLISH, 1);
        assertEquals("/default/for/all/from/outer.txt", value1.getStringValue(cms));

        value1 = xmlcontent.addValue(cms, "Cascade[1]/VfsLink", Locale.ENGLISH, 2);
        assertEquals("/default/for/all/from/outer.txt", value1.getStringValue(cms));

    }

    /**
     * Test default values after a new XML content has been created.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testDefaultOnCreation() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing default values when creating an XML content resource");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // create a new xml content article
        String xmlContentFile = "/xmlcontent/article_0005.html";
        cms.createResource(xmlContentFile, OpenCmsTestCase.ARTICLE_TYPEID);

        CmsFile file = cms.readFile(xmlContentFile);
        String content = new String(file.getContents(), CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        String value = xmlcontent.getStringValue(cms, "Title", Locale.ENGLISH);
        assertEquals("Default title value", value);

        value = xmlcontent.getStringValue(cms, "Release", Locale.ENGLISH);
        assertEquals("1114525380000", value);

        value = xmlcontent.getStringValue(cms, "Author", Locale.ENGLISH);
        assertEquals("This is the Description", value);
    }

    /**
     * Test default values in the appinfo node using a nested XML content schema when creating a new content.<p>
     * 
     * The nested content definition must be non-optional, and must have non-optional element.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testDefaultOnCreationWithNested() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing for default values in nested XML content schemas when creating a new content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definitions
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-3b.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_3B, content.getBytes(CmsEncoder.ENCODING_UTF_8));
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-4b.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_4B, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // create the content definition
        CmsXmlContentDefinition cd = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_4B, resolver);

        CmsXmlContent xmlcontent = CmsXmlContentFactory.createDocument(cms, Locale.ENGLISH, content, cd);

        String value = xmlcontent.getStringValue(cms, "Title", Locale.ENGLISH);
        assertEquals("Test", value);

        value = xmlcontent.getStringValue(cms, "Cascade/Option", Locale.ENGLISH);
        assertEquals("Default value from outer content definition", value);

        value = xmlcontent.getStringValue(cms, "Cascade/Option[2]", Locale.ENGLISH);
        assertEquals("Default value from outer content definition (for option node 2)", value);

        value = xmlcontent.getStringValue(cms, "Cascade/VfsLink", Locale.ENGLISH);
        assertEquals("/default/for/all/from/outer.txt", value);
    }

    /**
     * Tests the Locale settings of XMLContents with only optional elements and no element present.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testEmptyLocale() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing handling of empty locale nodes in XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String iso = "ISO-8859-1";

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-8.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_8, content.getBytes(iso));
        CmsXmlContentDefinition cd1 = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_8, resolver);

        // read an existing (empty) XML content with just one locale node
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", iso);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, iso, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);
        List<Locale> locales = xmlcontent.getLocales();
        assertEquals(1, locales.size());
        assertEquals(Locale.ENGLISH, locales.get(0));

        // create a fresh XML content based on the schema and try again  
        xmlcontent = CmsXmlContentFactory.createDocument(null, Locale.ENGLISH, CmsEncoder.ENCODING_UTF_8, cd1);
        xmlcontent.validateXmlStructure(resolver);

        locales = xmlcontent.getLocales();
        assertEquals(1, locales.size());
        assertEquals(Locale.ENGLISH, locales.get(0));
    }

    /**
     * Test using the GUI widget mapping appinfo nodes.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testLayoutWidgetMapping() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing mapping of the XML content GUI to different widgets");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-5.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_5, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_5, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-5.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        I_CmsWidget widget;
        I_CmsXmlContentHandler handler = definition.getContentHandler();

        // make sure the selected widgets are of the configured "non-standard" type
        widget = handler.getWidget(xmlcontent.getValue("Title", Locale.ENGLISH));
        assertNotNull(widget);
        assertEquals(CmsCheckboxWidget.class.getName(), widget.getClass().getName());
        assertEquals("Configuration for Title", handler.getConfiguration(xmlcontent.getValue("Title", Locale.ENGLISH)));

        // make sure the alias name works
        widget = handler.getWidget(xmlcontent.getValue("Test", Locale.ENGLISH));
        assertNotNull(widget);
        assertEquals(CmsHtmlWidget.class.getName(), widget.getClass().getName());
        assertEquals("Configuration for Test", handler.getConfiguration(xmlcontent.getValue("Test", Locale.ENGLISH)));

        // make sure the custom class name works
        widget = handler.getWidget(xmlcontent.getValue("Toast", Locale.ENGLISH));
        assertNotNull(widget);
        assertEquals(TestCustomInputWidgetImpl.class.getName(), widget.getClass().getName());
        assertEquals("Configuration for Toast", handler.getConfiguration(xmlcontent.getValue("Toast", Locale.ENGLISH)));
        // custom widget configuration has extended the handler String
        assertEquals("Configuration for Toast[some addition here]", widget.getConfiguration());
    }

    /**
     * Test resolving the links from an XML content.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testLinkResolver() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing link resolver for XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-2.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_2, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_2, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-2.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        assertTrue(xmlcontent.hasValue("Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("VfsLink", Locale.ENGLISH));
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsXmlHtmlValue htmlValue = (CmsXmlHtmlValue)xmlcontent.getValue("Html", Locale.ENGLISH);
        CmsXmlVfsFileValue vfsValue = (CmsXmlVfsFileValue)xmlcontent.getValue("VfsLink", Locale.ENGLISH);

        // must set the value again to ensure link table is properly initialized
        htmlValue.setStringValue(cms, htmlValue.getStringValue(cms));
        vfsValue.setStringValue(cms, vfsValue.getStringValue(cms));

        Iterator<CmsLink> i;
        CmsLinkTable table;

        String retranslatedOutput = htmlValue.getStringValue(cms);
        assertEquals("Incorrect links in resulting output", "<a href=\"http://www.alkacon.com\">Alkacon</a>\n"
            + "<a href=\"/data/opencms/index.html\">Index page</a>\n"
            + "<a href=\"/data/opencms/folder1/index.html?a=b&amp;c=d#anchor\">Index page</a>\n"
            + "<a href=\"/data/opencms/folder1/index.html?a2=b2&amp;c2=d2\">Index page with unescaped ampersand</a>",
        // note that the & in the links appear correctly escaped here
            retranslatedOutput.trim());

        table = htmlValue.getLinkTable();
        assertEquals(4, table.size());

        i = table.iterator();
        int result = 0;
        while (i.hasNext()) {
            // iterate all links and check if the required values are found
            CmsLink link = i.next();
            if (link.getTarget().equals("/sites/default/index.html") && link.isInternal()) {
                result++;
            } else if (link.getTarget().equals("http://www.alkacon.com") && !link.isInternal()) {
                result++;
            } else if (link.getTarget().equals("/sites/default/folder1/index.html")
                && link.getQuery().equals("a=b&c=d") // at this point the & in the link should be unescaped
                && link.getAnchor().equals("anchor")
                && link.isInternal()) {
                result++;
            } else if (link.getTarget().equals("/sites/default/folder1/index.html")
                && link.getQuery().equals("a2=b2&c2=d2") // at this point the & in the link should be unescaped
                && link.isInternal()) {
                result++;
            }
        }

        assertEquals(4, result);

        CmsLink link = vfsValue.getLink(cms);
        assertEquals("/sites/default/index.html", link.getTarget());
        assertTrue(link.isInternal());
        assertEquals("/index.html", vfsValue.getStringValue(cms));
    }

    /**
     * Tests the macros in messages and default values.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testMacros() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing macros in the XML content");

        CmsUser admin = cms.getRequestContext().getCurrentUser();
        admin.setFirstname("Hans");
        admin.setLastname("Mustermann");
        admin.setEmail("hans.mustermann@germany.de");
        admin.setAddress("Heidestra�e 17, M�nchen");
        cms.writeUser(admin);

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-9.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_9, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-9.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        CmsXmlContentErrorHandler errorHandler;

        I_CmsXmlContentValue value1;

        value1 = xmlcontent.getValue("Test", Locale.ENGLISH);
        value1.setStringValue(cms, "This produces a warning!");

        errorHandler = xmlcontent.validate(cms);
        assertFalse(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());

        value1.setStringValue(cms, "This produces a warning and an error!");

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors().size());
        assertEquals(1, errorHandler.getWarnings().size());

        value1 = xmlcontent.getValue("Toast", Locale.ENGLISH);
        value1.setStringValue(cms, "This produces a warning but no error!");

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());
        assertEquals(2, errorHandler.getWarnings(Locale.ENGLISH).size());

        value1 = xmlcontent.addValue(cms, "Option", Locale.ENGLISH, 0);
        assertEquals(
            "The author is: Hans Mustermann (Admin), Heidestra�e 17, M�nchen - hans.mustermann@germany.de",
            value1.getStringValue(cms));

        value1 = xmlcontent.addValue(cms, "Option", Locale.GERMAN, 0);
        assertEquals(
            "Der Autor ist: Hans Mustermann (Admin), Heidestra�e 17, M�nchen - hans.mustermann@germany.de",
            value1.getStringValue(cms));

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());
        assertEquals(2, errorHandler.getWarnings(Locale.ENGLISH).size());
    }

    /**
     * Tests the element mappings from the appinfo node.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testMappings() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing mapping of values in the XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-8.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_8, content.getBytes(CmsEncoder.ENCODING_ISO_8859_1));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", CmsEncoder.ENCODING_ISO_8859_1);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_ISO_8859_1, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        String resourcename = "/mappingtext.html";
        // create a file in the VFS with this content (required for mappings to work)
        cms.createResource(
            resourcename,
            OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId(),
            content.getBytes(CmsEncoder.ENCODING_ISO_8859_1),
            Collections.<CmsProperty> emptyList());

        CmsFile file = cms.readFile(resourcename);
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, file);

        CmsProperty titleProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertSame(titleProperty, CmsProperty.getNullProperty());

        // check mapping for cms:info tag
        String titleInfo = xmlcontent.getHandler().getTitleMapping(cms, xmlcontent, Locale.ENGLISH);
        assertNull(titleInfo);

        CmsProperty localeProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_LOCALE, false);
        assertSame(localeProperty, CmsProperty.getNullProperty());

        CmsProperty navImageProperty = cms.readPropertyObject(
            resourcename,
            CmsPropertyDefinition.PROPERTY_NAVIMAGE,
            false);
        assertSame(navImageProperty, CmsProperty.getNullProperty());

        CmsProperty navInfoProperty = cms.readPropertyObject(
            resourcename,
            CmsPropertyDefinition.PROPERTY_NAVINFO,
            false);
        assertSame(navInfoProperty, CmsProperty.getNullProperty());

        String titleStr = "This must be the Title";
        String navImageStr = "This is the String with xpath String[2]";
        String navInfoStr = "Here we have the String with xpath String[3]";

        I_CmsXmlContentValue value;
        value = xmlcontent.addValue(cms, "String", Locale.ENGLISH, 0);
        value.setStringValue(cms, titleStr);

        // set values for Title[2] and Title[3]
        CmsXmlContentValueSequence seq = xmlcontent.getValueSequence("String", Locale.ENGLISH);
        assertEquals(1, seq.getElementCount());
        value = seq.addValue(cms, 1);
        value.setStringValue(cms, navImageStr);
        value = seq.addValue(cms, 2);
        value.setStringValue(cms, navInfoStr);

        String localeStr = "en";
        value = xmlcontent.addValue(cms, "Locale", Locale.ENGLISH, 0);
        value.setStringValue(cms, localeStr);

        file.setContents(xmlcontent.toString().getBytes(CmsEncoder.ENCODING_ISO_8859_1));
        cms.writeFile(file);

        titleProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertEquals(titleStr, titleProperty.getValue());
        assertEquals(titleStr, titleProperty.getStructureValue());
        assertNull(titleProperty.getResourceValue());

        // check mapping for cms:info tag
        titleInfo = xmlcontent.getContentDefinition().getContentHandler().getTitleMapping(
            cms,
            xmlcontent,
            Locale.ENGLISH);
        assertNotNull(titleInfo);
        assertEquals(titleInfo, titleStr);

        // check multiple mappings
        CmsProperty myTitleProperty = cms.readPropertyObject(resourcename, "MyTitle", false);
        assertEquals(titleStr, myTitleProperty.getValue());
        assertEquals(titleStr, myTitleProperty.getStructureValue());
        assertNull(myTitleProperty.getResourceValue());

        navImageProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_NAVIMAGE, false);
        assertEquals(navImageStr, navImageProperty.getValue());
        assertEquals(navImageStr, navImageProperty.getResourceValue());
        assertNull(navImageProperty.getStructureValue());

        navInfoProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_NAVINFO, false);
        assertEquals(navInfoStr, navInfoProperty.getValue());
        assertEquals(navInfoStr, navInfoProperty.getStructureValue());
        assertNull(navInfoProperty.getResourceValue());

        localeProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_LOCALE, false);
        assertEquals(localeStr, localeProperty.getValue());
        assertEquals(localeStr, localeProperty.getResourceValue());
        assertNull(localeProperty.getStructureValue());
    }

    /**
     * Tests element mappings fom XML content to a property list.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testMappingsAsList() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing element mappings fom XML content to a property list");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // please note: XML schema 8 already in the cache from previous tests

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", CmsEncoder.ENCODING_ISO_8859_1);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_ISO_8859_1, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        String resourcename = "/mappinglist.html";
        // create a file in the VFS with this content (required for mappings to work)
        cms.createResource(
            resourcename,
            OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId(),
            content.getBytes(CmsEncoder.ENCODING_ISO_8859_1),
            Collections.<CmsProperty> emptyList());

        CmsFile file = cms.readFile(resourcename);
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, file);

        CmsProperty prop;
        prop = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        assertSame(prop, CmsProperty.getNullProperty());

        I_CmsXmlContentValue value;
        CmsXmlContentValueSequence seq = xmlcontent.getValueSequence("VfsFile", Locale.ENGLISH);
        assertEquals(0, seq.getElementCount());

        String res1 = "/index.html";
        String res2 = "/xmlcontent/";
        String res3 = "/xmlcontent/article_0001.html";
        String res4 = "/folder1/index.html";

        String sr = cms.getRequestContext().getSiteRoot();
        String propValue = sr + res1 + "|" + sr + res2 + "|" + sr + res3 + "|" + sr + res4;

        value = seq.addValue(cms, 0);
        value.setStringValue(cms, res1);
        value = seq.addValue(cms, 1);
        value.setStringValue(cms, res2);
        value = seq.addValue(cms, 2);
        value.setStringValue(cms, res3);
        value = seq.addValue(cms, 3);
        value.setStringValue(cms, res4);

        assertEquals(4, seq.getElementCount());
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        file.setContents(xmlcontent.toString().getBytes(CmsEncoder.ENCODING_ISO_8859_1));
        cms.writeFile(file);

        // check for written property values as list
        prop = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        List<String> list = prop.getValueList();
        assertNotNull(list);
        assertEquals(4, list.size());
        assertEquals(sr + res1, list.get(0));
        assertEquals(sr + res2, list.get(1));
        assertEquals(sr + res3, list.get(2));
        assertEquals(sr + res4, list.get(3));
        assertEquals(propValue, prop.getValue());
        assertEquals(propValue, prop.getStructureValue());
        assertNull(prop.getResourceValue());

        CmsProperty prop2;
        prop2 = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_KEYWORDS, false);
        assertSame(prop2, CmsProperty.getNullProperty());

        I_CmsXmlContentValue value2;
        CmsXmlContentValueSequence seq2 = xmlcontent.getValueSequence("VfsFile2", Locale.ENGLISH);
        assertEquals(0, seq2.getElementCount());

        value2 = seq2.addValue(cms, 0);
        value2.setStringValue(cms, res1);
        value2 = seq2.addValue(cms, 1);
        value2.setStringValue(cms, res2);
        value2 = seq2.addValue(cms, 2);
        value2.setStringValue(cms, res3);
        value2 = seq2.addValue(cms, 3);
        value2.setStringValue(cms, res4);

        assertEquals(4, seq2.getElementCount());
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        file.setContents(xmlcontent.toString().getBytes(CmsEncoder.ENCODING_ISO_8859_1));
        cms.writeFile(file);

        // check for written property values as list
        prop2 = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_KEYWORDS, false);
        List<String> list2 = prop.getValueList();
        assertNotNull(list2);
        assertEquals(4, list2.size());
        assertEquals(sr + res1, list2.get(0));
        assertEquals(sr + res2, list2.get(1));
        assertEquals(sr + res3, list2.get(2));
        assertEquals(sr + res4, list2.get(3));
        assertEquals(propValue, prop2.getValue());
        assertEquals(propValue, prop2.getResourceValue());
        assertNull(prop2.getStructureValue());

        CmsProperty prop3;
        prop3 = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_NAVTEXT, false);
        assertSame(prop3, CmsProperty.getNullProperty());

        I_CmsXmlContentValue value3;
        CmsXmlContentValueSequence seq3 = xmlcontent.getValueSequence("VfsFile3", Locale.ENGLISH);
        assertEquals(0, seq3.getElementCount());

        value3 = seq3.addValue(cms, 0);
        value3.setStringValue(cms, res1);
        value3 = seq3.addValue(cms, 1);
        value3.setStringValue(cms, res2);
        value3 = seq3.addValue(cms, 2);
        value3.setStringValue(cms, res3);
        value3 = seq3.addValue(cms, 3);
        value3.setStringValue(cms, res4);

        assertEquals(4, seq3.getElementCount());
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        file.setContents(xmlcontent.toString().getBytes(CmsEncoder.ENCODING_ISO_8859_1));
        cms.writeFile(file);

        // check for written property values as list
        prop3 = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_NAVTEXT, false);
        List<String> list3 = prop.getValueList();
        assertNotNull(list3);
        assertEquals(4, list3.size());
        assertEquals(sr + res1, list3.get(0));
        assertEquals(sr + res2, list3.get(1));
        assertEquals(sr + res3, list3.get(2));
        assertEquals(sr + res4, list3.get(3));
        assertEquals(propValue, prop3.getValue());
        assertEquals(propValue, prop3.getStructureValue());
        assertNull(prop3.getResourceValue());
    }

    /**
     * Tests the element mappings from the appinfo node for nested XML content.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testMappingsOfNestedContent() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing mapping of values in the XML content with nested elements");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-7.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        String resourcename = "/mappingtest_nested.html";
        // create a file in the VFS with this content (required for mappings to work)
        cms.createResource(
            resourcename,
            OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId(),
            content.getBytes(CmsEncoder.ENCODING_ISO_8859_1),
            Collections.<CmsProperty> emptyList());

        CmsFile file = cms.readFile(resourcename);
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, file);

        CmsProperty titleProperty;
        titleProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertSame(titleProperty, CmsProperty.getNullProperty());

        String titleStr = "This must be the Title (not nested)";
        I_CmsXmlContentValue value;
        value = xmlcontent.getValue("Test", Locale.ENGLISH);
        assertEquals(value.getStringValue(cms), "Another Test");
        value.setStringValue(cms, titleStr);

        String descStr = "This must be the Description (which IS nested)";
        value = xmlcontent.getValue("Cascade/Toast", Locale.ENGLISH);
        assertEquals(value.getStringValue(cms), "Toast");
        value.setStringValue(cms, descStr);

        file.setContents(xmlcontent.toString().getBytes(CmsEncoder.ENCODING_ISO_8859_1));
        cms.writeFile(file);

        titleProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertEquals(titleStr, titleProperty.getValue());
        titleProperty = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        assertEquals(descStr, titleProperty.getValue());
    }

    /**
     * Tests the element mappings from the appinfo node if there is more then one locale.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testMappingsWithManyLocales() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing mapping of values in the XML content with locales");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-8.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_8, content.getBytes(CmsEncoder.ENCODING_ISO_8859_1));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", CmsEncoder.ENCODING_ISO_8859_1);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_ISO_8859_1, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // create "en" property
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, Locale.ENGLISH.toString(), null));

        String resourcenameEn = "/mappingtext_en.html";
        String resourcenameDe = "/mappingtext_de.html";
        // create a file in the VFS with this content (required for mappings to work)
        cms.createResource(
            resourcenameEn,
            OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId(),
            content.getBytes(CmsEncoder.ENCODING_ISO_8859_1),
            properties);

        // copy the resource as a sibling to "de"
        cms.copyResource(resourcenameEn, resourcenameDe, CmsResource.COPY_AS_SIBLING);
        // now lock the "DE" sibling
        cms.changeLock(resourcenameDe);
        // add the "DE" locale property to the german version
        cms.writePropertyObject(
            resourcenameDe,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, Locale.GERMAN.toString(), null));

        CmsFile file = cms.readFile(resourcenameDe);
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, file);

        xmlcontent.addLocale(cms, Locale.GERMAN);
        if (!xmlcontent.hasLocale(Locale.ENGLISH)) {
            xmlcontent.addLocale(cms, Locale.ENGLISH);
        }

        // add EN property
        String titleStrEn = "This must be the Title in EN";
        I_CmsXmlContentValue value;
        value = xmlcontent.addValue(cms, "String", Locale.ENGLISH, 0);
        value.setStringValue(cms, titleStrEn);
        // add EN release date
        long timeEN = System.currentTimeMillis() - 1000;
        value = xmlcontent.addValue(cms, "DateTime", Locale.ENGLISH, 0);
        value.setStringValue(cms, "" + timeEN);

        // add DE property
        String titleStrDe = "Das ist der Title in DE";
        value = xmlcontent.addValue(cms, "String", Locale.GERMAN, 0);
        value.setStringValue(cms, titleStrDe);
        // add DE release date
        long timeDE = System.currentTimeMillis() - 5000;
        value = xmlcontent.addValue(cms, "DateTime", Locale.GERMAN, 0);
        value.setStringValue(cms, "" + timeDE);

        file.setContents(xmlcontent.toString().getBytes(CmsEncoder.ENCODING_ISO_8859_1));
        cms.writeFile(file);
        // finally unlock the resource
        cms.unlockResource(resourcenameDe);

        // now check if the properties have been assigned as required to the locales
        CmsProperty titlePropertyEn = cms.readPropertyObject(
            resourcenameEn,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false);
        assertEquals(titleStrEn, titlePropertyEn.getValue());
        assertDateReleased(cms, resourcenameEn, timeEN);

        CmsProperty titlePropertyDe = cms.readPropertyObject(
            resourcenameDe,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false);
        assertEquals(titleStrDe, titlePropertyDe.getValue());
        assertDateReleased(cms, resourcenameDe, timeDE);
    }

    /**
     * Test using a nested XML content schema.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testNestedSchema() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing for nested XML content schemas");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-4.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_4, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_4, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-4.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        System.out.println(xmlcontent.toString());

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        assertTrue(xmlcontent.hasValue("Title", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Title[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[1]/Html[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[1]/VfsLink[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[1]/VfsLink[2]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[2]/Html[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[2]/VfsLink[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[2]/Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[2]/VfsLink", Locale.ENGLISH));

        assertTrue(xmlcontent.hasValue("Cascade", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[2]", Locale.ENGLISH));

        assertTrue(xmlcontent.hasValue("Cascade/Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade/Html[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink[2]", Locale.ENGLISH));

        // ensure Xpath index is based on 1, not 0
        assertFalse(xmlcontent.hasValue("Title[0]", Locale.ENGLISH));
        assertFalse(xmlcontent.hasValue("Cascade[0]", Locale.ENGLISH));

        I_CmsXmlContentValue value1;
        I_CmsXmlContentValue value2;

        value1 = xmlcontent.getValue("Title", Locale.ENGLISH);
        value2 = xmlcontent.getValue("Title[1]", Locale.ENGLISH);
        assertSame(value1, value2);

        value2 = xmlcontent.getValue("Title", Locale.ENGLISH, 0);
        assertSame(value1, value2);

        String xpath = "Cascade[1]/VfsLink[2]";
        value1 = xmlcontent.getValue(xpath, Locale.ENGLISH);
        assertEquals(xpath, value1.getPath());

        xpath = "Title[1]";
        value1 = xmlcontent.getValue(xpath, Locale.ENGLISH);
        assertEquals(xpath, value1.getPath());

        xpath = "Cascade/Html";
        value1 = xmlcontent.getValue(xpath, Locale.ENGLISH);
        assertEquals(CmsXmlUtils.createXpath(xpath, 1), value1.getPath());

        xpath = "Cascade";
        value1 = xmlcontent.getValue(xpath, Locale.ENGLISH);
        assertEquals(CmsXmlUtils.createXpath(xpath, 1), value1.getPath());

        assertSame(definition.getContentHandler().getClass().getName(), TestXmlContentHandler.class.getName());
    }

    /**
     * Test if the resource bundle in the schema definition is properly initialized.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testResourceBundle() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing using different resource bundles in content handler for XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContentDefinition definition;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-3.xsd", CmsEncoder.ENCODING_UTF_8);
        definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_3, resolver);

        I_CmsXmlContentHandler contentHandler;

        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), TestXmlContentHandler.class.getName());
        assertNull(contentHandler.getMessages(Locale.ENGLISH));

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-8.xsd", CmsEncoder.ENCODING_UTF_8);
        definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_8, resolver);

        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsMessages messages = contentHandler.getMessages(Locale.ENGLISH);
        assertNotNull(messages);
        assertEquals(
            "The following errors occurred when validating the form:",
            messages.key(org.opencms.xml.content.Messages.GUI_EDITOR_XMLCONTENT_VALIDATION_ERROR_TITLE_0));
    }

    /**
     * Test the resource bundles defined in XML content.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testResourceBundleFromXml() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing resource bundles defined in XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContentDefinition definition;
        I_CmsXmlContentHandler contentHandler;

        // unmarshal content definition with localization in properties and XML
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-1_localized1.xsd",
            CmsEncoder.ENCODING_UTF_8);
        definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1L1, resolver);

        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsMessages messagesEN = contentHandler.getMessages(Locale.ENGLISH);
        assertNotNull(messagesEN.getResourceBundle());

        assertEquals("The author is", messagesEN.key("label.author"));
        assertEquals(
            "Bad value \"Arg0\" according to rule Arg1",
            messagesEN.key("editor.xmlcontent.validation.warning", "Arg0", "Arg1"));

        CmsMessages messagesDE = contentHandler.getMessages(Locale.GERMAN);
        assertNotNull(messagesDE.getResourceBundle());

        assertEquals("Der Autor ist", messagesDE.key("label.author"));
        assertEquals(
            "Bad value \"Arg0\" according to rule Arg1",
            messagesDE.key("editor.xmlcontent.validation.warning", "Arg0", "Arg1"));

        // get a Locale / language variation and see if this works
        CmsMessages messagesDEde = contentHandler.getMessages(Locale.GERMANY);
        assertNotNull(messagesDEde.getResourceBundle());

        // from DE locale (properties)
        assertEquals("Der Autor ist", messagesDEde.key("label.author"));
        // from EN locale (properties)
        assertEquals(
            "The following errors occurred when validating the form:",
            messagesDEde.key("editor.xmlcontent.validation.error.title"));
        // from DE_de locale (XML)
        assertEquals("Warnung aus dem XML", messagesDEde.key("editor.xmlcontent.validation.warning"));

        // unmarshal content definition with localization in XML only
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-1_localized2.xsd",
            CmsEncoder.ENCODING_UTF_8);
        definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1L2, resolver);

        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        messagesEN = contentHandler.getMessages(Locale.ENGLISH);
        assertNotNull(messagesEN.getResourceBundle());

        assertEquals("The author is NOW", messagesEN.key("label.author"));
        assertEquals(
            "VERY Bad value \"Arg0\" according to rule Arg1",
            messagesEN.key("editor.xmlcontent.validation.warning", "Arg0", "Arg1"));

        messagesDE = contentHandler.getMessages(Locale.GERMAN);
        assertNotNull(messagesDE.getResourceBundle());

        assertEquals("Der Autor ist JETZT", messagesDE.key("label.author"));
        assertEquals(
            "VERY Bad value \"Arg0\" according to rule Arg1",
            messagesDE.key("editor.xmlcontent.validation.warning", "Arg0", "Arg1"));

        // get a Locale / language variation and see if this works
        messagesDEde = contentHandler.getMessages(Locale.GERMANY);
        assertNotNull(messagesDEde.getResourceBundle());

        // from DE_de locale
        assertEquals("Der Autor ist JETZT", messagesDEde.key("label.author"));
        // from DE locale
        assertEquals(
            "ECHT schlechter Wert \"Arg0\" wegen Regel Arg1",
            messagesDEde.key("editor.xmlcontent.validation.warning", "Arg0", "Arg1"));
        // from EN locale
        assertEquals(
            "The following errors occurred when validating the form:",
            messagesDEde.key("editor.xmlcontent.validation.error.title"));

        // no test with a "real" XSD schema to make sure flushing of the caches does not "kill" the localization
        String filename = "/xmlcontent/article_0001.html";
        // now read the XML content
        CmsFile file = cms.readFile(filename);
        CmsXmlContent article = CmsXmlContentFactory.unmarshal(cms, file);

        // get a Locale / language variation and see if this works
        messagesDEde = article.getHandler().getMessages(Locale.GERMANY);
        assertNotNull(messagesDEde.getResourceBundle());
        assertEquals("Lokalisierung im XML Schema", messagesDEde.key("from.xml"));

        // fire a clear cache event
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, null));

        // the messages that where just injected must not be null because the permanent cache is NOT cleared
        messagesEN = contentHandler.getMessages(Locale.ENGLISH);
        assertNotNull(messagesEN.getResourceBundle());
        assertEquals("The author is NOW", messagesEN.key("label.author"));

        // if a content is unmarshalled, the content definition will be re-read after clear cache, 
        // but we better not depend on that as "old" references to content objects may be held when the clear cache is done 
        article = CmsXmlContentFactory.unmarshal(cms, file);
        messagesDEde = article.getHandler().getMessages(Locale.GERMANY);
        assertNotNull(messagesDEde);
        assertEquals("Lokalisierung im XML Schema", messagesDEde.key("from.xml"));
    }

    /**
     * Test the resource bundles defined in XML content with default values.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testResourceBundleFromXmlWithDefault() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing resource bundles defined in XML content with default values");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContentDefinition definition;

        // unmarshal content definition with localization in XML only
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-1_localized2.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // create the content definition
        definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1L2, resolver);
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        // store content definition in entity resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_1L2, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        CmsXmlContent xmlcontentDE = CmsXmlContentFactory.createDocument(cms, Locale.GERMAN, content, definition);

        assertEquals("Dies ist etwas Text EINS äöüÄÖÜß€", xmlcontentDE.getStringValue(cms, "StringOne", Locale.GERMAN));
        assertEquals("Dies ist etwas Text ZWEI äöüÄÖÜß€", xmlcontentDE.getStringValue(cms, "StringTwo", Locale.GERMAN));

        CmsXmlContent xmlcontentEN = CmsXmlContentFactory.createDocument(cms, Locale.ENGLISH, content, definition);

        assertEquals("This is some text ONE", xmlcontentEN.getStringValue(cms, "StringOne", Locale.ENGLISH));
        assertEquals("This is some text TWO", xmlcontentEN.getStringValue(cms, "StringTwo", Locale.ENGLISH));

        // now try to "change" the XSD 
        // unmarshal content definition with localization in XML only
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-1_localized3.xsd",
            CmsEncoder.ENCODING_UTF_8);

        // clear the caches of the XML entity resolver
        resolver.uncacheSystemId(SCHEMA_SYSTEM_ID_1L2);
        // create the content definition USING EXISTING SCHEMA NAME!!!        
        definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1L2, resolver);
        // store content definition in entity resolver USING EXISTING SCHEMA NAME!!!
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_1L2, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        xmlcontentDE = CmsXmlContentFactory.createDocument(cms, Locale.GERMAN, content, definition);
        String specialChars = C_AUML_LOWER
            + C_OUML_LOWER
            + C_UUML_LOWER
            + C_AUML_UPPER
            + C_OUML_UPPER
            + C_UUML_UPPER
            + C_SHARP_S
            + C_EURO;
        assertEquals(
            "Dies ist etwas Text EINS NEU " + specialChars,
            xmlcontentDE.getStringValue(cms, "StringOne", Locale.GERMAN));
        assertEquals(
            "Dies ist etwas Text ZWEI NEU " + specialChars,
            xmlcontentDE.getStringValue(cms, "StringTwo", Locale.GERMAN));

        xmlcontentEN = CmsXmlContentFactory.createDocument(cms, Locale.ENGLISH, content, definition);

        assertEquals("This is some text NEW ONE", xmlcontentEN.getStringValue(cms, "StringOne", Locale.ENGLISH));
        assertEquals("This is some text NEW TWO", xmlcontentEN.getStringValue(cms, "StringTwo", Locale.ENGLISH));
    }

    /**
     * Test if a multiple resource bundle in the schema definition is properly initialized.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testResourceMultiBundle() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing a multiple resource bundle in content handler for XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContentDefinition definition;

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-1_localized4.xsd",
            CmsEncoder.ENCODING_UTF_8);
        definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1L4, resolver);

        I_CmsXmlContentHandler contentHandler;

        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsMessages messages = contentHandler.getMessages(Locale.ENGLISH);
        assertNotNull(messages);
        assertEquals(
            "The following errors occurred when validating the form:",
            messages.key(org.opencms.xml.content.Messages.GUI_EDITOR_XMLCONTENT_VALIDATION_ERROR_TITLE_0));
        assertEquals(
            "Error while converting old xmlPage content.",
            messages.key(org.opencms.xml.page.Messages.ERR_XML_PAGE_CONVERT_CONTENT_0));

        // get a Locale / language variation and see if this works
        CmsMessages messagesDEde = contentHandler.getMessages(Locale.GERMANY);
        assertTrue(messagesDEde instanceof CmsMultiMessages);

        // from DE locale
        assertEquals(
            "ECHT schlechter Wert \"Arg0\" wegen Regel Arg1",
            messagesDEde.key("editor.xmlcontent.validation.warning", "Arg0", "Arg1"));
        // from EN locale
        assertEquals(
            "The following errors occurred when validating the form:",
            messagesDEde.key("editor.xmlcontent.validation.error.title"));

        // from DE_de locale
        assertEquals("Der Autor ist JETZT", messagesDEde.key("label.newauthor"));
    }

    /**
     * Test the validation of the value elements.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testValidation() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the validation for values in the XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-7.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // add 2 deep cascaded nodes
        xmlcontent.addValue(cms, "DeepCascade", Locale.ENGLISH, 0);
        xmlcontent.addValue(cms, "DeepCascade", Locale.ENGLISH, 1);
        xmlcontent.addLocale(cms, Locale.GERMAN);
        xmlcontent.addValue(cms, "DeepCascade", Locale.GERMAN, 0);
        // output the current document
        System.out.println(xmlcontent.toString());

        CmsXmlContentErrorHandler errorHandler;

        // perform a first validation - the must be no errors or warnings reported
        errorHandler = xmlcontent.validate(cms);
        assertFalse(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());

        I_CmsXmlContentValue value1;

        value1 = xmlcontent.getValue("Test", Locale.ENGLISH);
        value1.setStringValue(cms, "This produces a warning!");

        errorHandler = xmlcontent.validate(cms);
        assertFalse(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());

        value1.setStringValue(cms, "This produces a warning and an error!");

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors().size());
        assertEquals(1, errorHandler.getWarnings().size());

        value1 = xmlcontent.getValue("Toast", Locale.ENGLISH);
        value1.setStringValue(cms, "This produces a warning but no error!");

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());
        assertEquals(2, errorHandler.getWarnings(Locale.ENGLISH).size());

        value1 = xmlcontent.addValue(cms, "Option", Locale.ENGLISH, 0);
        assertEquals("Default value from the appinfos", value1.getStringValue(cms));

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        value1 = xmlcontent.getValue("DeepCascade[1]/Cascade[1]/VfsLink", Locale.ENGLISH);
        value1.setStringValue(cms, "/system/workplace/warning");

        value1 = xmlcontent.getValue("DeepCascade[1]/Cascade[1]/Html", Locale.ENGLISH);
        value1.setStringValue(cms, "This HTML contains an error!");

        value1 = xmlcontent.addValue(cms, "DeepCascade[1]/Cascade[1]/Option", Locale.ENGLISH, 0);
        assertEquals("Default value from the XML", value1.getStringValue(cms));

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertEquals(3, errorHandler.getErrors(Locale.ENGLISH).size());
        assertEquals(2, errorHandler.getWarnings(Locale.ENGLISH).size());
    }

    /**
     * Extended test for the validation of the value elements.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testValidationExtended() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Extended test for the validation of values in the XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-8.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_8, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        CmsXmlContentValueSequence sequence;
        I_CmsXmlContentValue value;

        sequence = xmlcontent.getValueSequence("String", Locale.ENGLISH);
        value = sequence.addValue(cms, 0);
        value.setStringValue(cms, "This is a String that contains an error and a warning!");

        // validate the XML structure (no error caused here)
        xmlcontent.validateXmlStructure(resolver);

        CmsXmlContentErrorHandler errorHandler;

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertTrue(errorHandler.hasErrors(Locale.ENGLISH));
        assertFalse(errorHandler.hasErrors(Locale.GERMAN));
        assertFalse(errorHandler.hasErrors(Locale.FRENCH));
        assertEquals(1, errorHandler.getErrors().size());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());
        assertEquals(1, errorHandler.getWarnings(Locale.ENGLISH).size());

        value.setStringValue(cms, "This is a nice String");
        errorHandler = xmlcontent.validate(cms);
        assertFalse(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());

        sequence = xmlcontent.getValueSequence("DateTime", Locale.ENGLISH);
        value = sequence.addValue(cms, 0);
        value.setStringValue(cms, "invalid!");

        boolean error = true;
        try {
            xmlcontent.validateXmlStructure(resolver);
        } catch (Exception e) {
            error = false;
        }
        if (error) {
            fail("Invalid value was possible for DateTime");
        }

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());

        value.setStringValue(cms, String.valueOf(System.currentTimeMillis()));
        xmlcontent.validateXmlStructure(resolver);
        errorHandler = xmlcontent.validate(cms);
        assertFalse(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());

        sequence = xmlcontent.getValueSequence("Color", Locale.ENGLISH);
        value = sequence.addValue(cms, 0);
        value.setStringValue(cms, "invalid!");

        error = true;
        try {
            xmlcontent.validateXmlStructure(resolver);
        } catch (Exception e) {
            error = false;
        }
        if (error) {
            fail("Invalid value was possible for Color");
        }

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());

        value.setStringValue(cms, "#fff");
        xmlcontent.validateXmlStructure(resolver);
        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());

        // test custom error message
        assertEquals(
            "A valid HTML color value (e.g. #ffffff) is required",
            errorHandler.getErrors(Locale.ENGLISH).get(value.getPath()));

        value.setStringValue(cms, "#ffffff");
        xmlcontent.validateXmlStructure(resolver);
        errorHandler = xmlcontent.validate(cms);
        assertFalse(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());

        sequence = xmlcontent.getValueSequence("Locale", Locale.ENGLISH);
        value = sequence.addValue(cms, 0);
        value.setStringValue(cms, "invalid!");

        error = true;
        try {
            xmlcontent.validateXmlStructure(resolver);
        } catch (Exception e) {
            error = false;
        }
        if (error) {
            fail("Invalid value was possible for Locale");
        }

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());

        value.setStringValue(cms, Locale.GERMANY.toString());
        xmlcontent.validateXmlStructure(resolver);
        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());

        value.setStringValue(cms, Locale.GERMAN.toString());
        xmlcontent.validateXmlStructure(resolver);
        errorHandler = xmlcontent.validate(cms);
        assertFalse(errorHandler.hasErrors());
        assertFalse(errorHandler.hasWarnings());

        // output the current document
        System.out.println(xmlcontent.toString());
    }

    /**
     * Test for the validation with different locales.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testValidationLocale() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Test for the validation of multiple locale values in the XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-8.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_8, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        CmsXmlContentValueSequence sequence;
        I_CmsXmlContentValue value;

        xmlcontent.addLocale(cms, Locale.GERMAN);

        sequence = xmlcontent.getValueSequence("String", Locale.ENGLISH);
        value = sequence.addValue(cms, 0);
        value.setStringValue(cms, "This is a String that contains an error and a warning!");

        sequence = xmlcontent.getValueSequence("String", Locale.GERMAN);
        value = sequence.addValue(cms, 0);
        value.setStringValue(cms, "Dieser String enth�llt einen Fehler (English: 'error') und eine Warnung!");

        // validate the XML structure (no error caused here)
        xmlcontent.validateXmlStructure(resolver);

        CmsXmlContentErrorHandler errorHandler;

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertTrue(errorHandler.hasErrors(Locale.ENGLISH));
        assertTrue(errorHandler.hasErrors(Locale.GERMAN));
        assertFalse(errorHandler.hasErrors(Locale.FRENCH));
        assertEquals(2, errorHandler.getErrors().size());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());
        assertEquals(1, errorHandler.getWarnings(Locale.ENGLISH).size());
        assertEquals(1, errorHandler.getErrors(Locale.GERMAN).size());
        assertEquals(1, errorHandler.getWarnings(Locale.GERMAN).size());

        // output the current document
        System.out.println(xmlcontent.toString());
    }

    /**
     * Test the index of the value elements.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testValueIndex() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the value index for nodes in the XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-7.xsd", CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // "fill up" the XML content with some values
        CmsXmlContentValueSequence toastSequence = xmlcontent.getValueSequence("Toast", Locale.ENGLISH);

        for (int i = 0; i < 2; i++) {
            I_CmsXmlContentValue value = toastSequence.addValue(cms, 0);
            value.setStringValue(cms, "Added toast value " + i);
        }

        // output the current document
        System.out.println(xmlcontent.toString());

        assertEquals(toastSequence.getElementCount(), 3);
        for (int i = 0; i < 3; i++) {
            I_CmsXmlContentValue value = toastSequence.getValue(i);
            assertEquals(i, value.getIndex());
        }

        // test min / max occurs values for value instances
        I_CmsXmlContentValue toastValue = toastSequence.getValue(1);
        assertEquals(1, toastValue.getMinOccurs());
        assertEquals(3, toastValue.getMaxOccurs());

        // check content handlers for nested elements
        I_CmsXmlContentValue value1 = xmlcontent.getValue("Test", Locale.ENGLISH);
        assertSame(
            TestXmlContentHandler.class.getName(),
            value1.getContentDefinition().getContentHandler().getClass().getName());

        value1 = xmlcontent.getValue("Cascade", Locale.ENGLISH);
        assertSame(
            TestXmlContentHandler.class.getName(),
            value1.getContentDefinition().getContentHandler().getClass().getName());

        value1 = xmlcontent.getValue("Cascade/Title", Locale.ENGLISH);
        assertSame(
            CmsDefaultXmlContentHandler.class.getName(),
            value1.getContentDefinition().getContentHandler().getClass().getName());
    }

    /**
     * Test resolving a {@link CmsXmlVarLinkValue} in an XML content.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testVarLinkResolver() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing link CmsXmlVarLinkValue in an XML content");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-VarLink-definition-1.xsd",
            CmsEncoder.ENCODING_UTF_8);
        String schemaId = "http://www.opencms.org/testVarLink1.xsd";
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, schemaId, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(schemaId, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-VarLink-1.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        assertTrue(xmlcontent.hasValue("VfsLink", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("VarLink", Locale.ENGLISH));
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsXmlVfsFileValue vfsValue = (CmsXmlVfsFileValue)xmlcontent.getValue("VfsLink", Locale.ENGLISH);
        CmsXmlVarLinkValue varValue1 = (CmsXmlVarLinkValue)xmlcontent.getValue("VarLink", Locale.ENGLISH, 0);
        CmsXmlVarLinkValue varValue2 = (CmsXmlVarLinkValue)xmlcontent.getValue("VarLink", Locale.ENGLISH, 1);

        // make sure the XML unmarshals as expected
        CmsLink link = vfsValue.getLink(cms);
        assertEquals("/sites/default/index.html", link.getTarget());
        assertTrue(link.isInternal());
        assertEquals("/index.html", vfsValue.getStringValue(cms));

        CmsLink varLink1 = varValue1.getLink(cms);
        assertEquals("/sites/default/index.html", varLink1.getTarget());
        assertTrue(varLink1.isInternal());
        assertEquals("/index.html", varValue1.getStringValue(cms));

        CmsLink varLink2 = varValue2.getLink(cms);
        assertEquals("http://www.alkacon.com", varLink2.getTarget());
        assertFalse(varLink2.isInternal());

        // now set some VarLinks with different types of targets

        // simple external link
        CmsXmlVarLinkValue varVal;
        CmsLink varLink;

        varVal = (CmsXmlVarLinkValue)xmlcontent.addValue(cms, "VarLink", Locale.ENGLISH, 2);
        varVal.setStringValue(cms, "http://www.opencms.org");
        varLink = varVal.getLink(cms);
        assertEquals("http://www.opencms.org", varLink.getTarget());
        assertFalse(varLink.isInternal());

        // internal link to an existing file
        varVal = (CmsXmlVarLinkValue)xmlcontent.addValue(cms, "VarLink", Locale.ENGLISH, 3);
        varVal.setStringValue(cms, "/folder1/page1.html");
        varLink = varVal.getLink(cms);
        assertEquals("/sites/default/folder1/page1.html", varLink.getTarget());
        assertTrue(varLink.isInternal());
        assertEquals("/folder1/page1.html", varVal.getStringValue(cms));

        // internal link to a not existing file
        varVal = (CmsXmlVarLinkValue)xmlcontent.addValue(cms, "VarLink", Locale.ENGLISH, 4);
        varVal.setStringValue(cms, "/folder_notexist/page_i_dont_exist.html");
        varLink = varVal.getLink(cms);
        assertEquals("/sites/default/folder_notexist/page_i_dont_exist.html", varLink.getTarget());
        assertTrue(varLink.isInternal());
        assertEquals("/folder_notexist/page_i_dont_exist.html", varVal.getStringValue(cms));

        // internal link using the server prefix to an existing file
        varVal = (CmsXmlVarLinkValue)xmlcontent.addValue(cms, "VarLink", Locale.ENGLISH, 5);
        varVal.setStringValue(cms, "http://localhost:8080/folder1/page1.html");
        varLink = varVal.getLink(cms);
        assertEquals("/sites/default/folder1/page1.html", varLink.getTarget());
        assertTrue(varLink.isInternal());
        assertEquals("/folder1/page1.html", varVal.getStringValue(cms));

        // internal link using the server prefix to a not existing file
        varVal = (CmsXmlVarLinkValue)xmlcontent.addValue(cms, "VarLink", Locale.ENGLISH, 6);
        varVal.setStringValue(cms, "http://localhost:8080/folder_notexist/page_i_dont_exist.html");
        varLink = varVal.getLink(cms);
        assertEquals("/sites/default/folder_notexist/page_i_dont_exist.html", varLink.getTarget());
        assertTrue(varLink.isInternal());
        assertEquals("/folder_notexist/page_i_dont_exist.html", varVal.getStringValue(cms));

        // output the XML content after modifications
        echo("XML Content after VarLink modification:");
        echo(xmlcontent.toString());
        echo("-----------------");

        // create the content definition
        CmsXmlContentDefinition cd = CmsXmlContentDefinition.unmarshal(content, schemaId, resolver);
        CmsXmlContent newContent = CmsXmlContentFactory.createDocument(
            cms,
            Locale.ENGLISH,
            CmsEncoder.ENCODING_UTF_8,
            cd);

        echo("New XML Content for VarLink:");
        echo(newContent.toString());
        echo("-----------------");

        // validate the XML of the created XML content
        xmlcontent.validateXmlStructure(resolver);

        // validate the XML of the created XML content
        newContent.validateXmlStructure(resolver);
    }

    /**
     * Tests creating a XMl page with the API.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlContentCreate() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing creation of an XML Content:\n");
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String filename = "xmlcontent.html";
        CmsResource res = cms.createResource(filename, OpenCmsTestCase.ARTICLE_TYPEID);
        CmsFile file = cms.readFile(res);
        String content = new String(file.getContents(), CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        assertTrue(xmlcontent.hasLocale(Locale.ENGLISH));
        try {
            xmlcontent.addLocale(cms, Locale.ENGLISH);
            fail("where is the default locale!?");
        } catch (Exception e) {
            // should fail
        }
        xmlcontent.getValue("Author", Locale.ENGLISH).setStringValue(cms, "Alkacon Software GmbH");
        file.setContents(xmlcontent.marshal());
        cms.writeFile(file);
    }
}