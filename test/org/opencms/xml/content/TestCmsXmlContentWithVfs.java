/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/content/TestCmsXmlContentWithVfs.java,v $
 * Date   : $Date: 2005/04/10 11:00:14 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.xmlwidgets.CmsXmlBooleanWidget;
import org.opencms.workplace.xmlwidgets.CmsXmlHtmlWidget;
import org.opencms.workplace.xmlwidgets.I_CmsXmlWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlStringValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the link resolver for XML contents.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.23 $
 */
public class TestCmsXmlContentWithVfs extends OpenCmsTestCase {

    private static final String C_SCHEMA_SYSTEM_ID_2 = "http://www.opencms.org/test2.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_3 = "http://www.opencms.org/test3.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_4 = "http://www.opencms.org/test4.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_5 = "http://www.opencms.org/test5.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_6 = "http://www.opencms.org/test6.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_7 = "http://www.opencms.org/test7.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_8 = "http://www.opencms.org/test8.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_9 = "http://www.opencms.org/test9.xsd";
    
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

        suite.addTest(new TestCmsXmlContentWithVfs("testAddRemoveElements"));
        suite.addTest(new TestCmsXmlContentWithVfs("testContentHandler"));
        suite.addTest(new TestCmsXmlContentWithVfs("testDefaultNested"));
        suite.addTest(new TestCmsXmlContentWithVfs("testNestedSchema"));
        suite.addTest(new TestCmsXmlContentWithVfs("testAddRemoveNestedElements"));
        suite.addTest(new TestCmsXmlContentWithVfs("testAccessNestedElements"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValueIndex"));
        suite.addTest(new TestCmsXmlContentWithVfs("testLayoutWidgetMapping"));
        suite.addTest(new TestCmsXmlContentWithVfs("testLinkResolver"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValidation"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValidationExtended"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValidationLocale"));        
        suite.addTest(new TestCmsXmlContentWithVfs("testMappings"));
        suite.addTest(new TestCmsXmlContentWithVfs("testResourceBundle"));
        suite.addTest(new TestCmsXmlContentWithVfs("testMacros"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-3.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_3, resolver);

        I_CmsXmlContentHandler contentHandler;
        
        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), TestXmlContentHandler.class.getName());
        assertNull(contentHandler.getMessages(Locale.ENGLISH));
        
        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-8.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_8, resolver);
        
        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());
        
        CmsMessages messages = contentHandler.getMessages(Locale.ENGLISH);
        assertNotNull(messages);
        assertEquals("The following errors occurred when validating the form:", messages.key("editor.xmlcontent.validation.error.title"));             
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-7.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.C_UTF8_ENCODING);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

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
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.C_UTF8_ENCODING, resolver);
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
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.C_UTF8_ENCODING, resolver);

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
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.C_UTF8_ENCODING, resolver);

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
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.C_UTF8_ENCODING, resolver);

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
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.C_UTF8_ENCODING, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        level0Sequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(1, level0Sequence.getElementCount());
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-6.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_6, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-6.xml", CmsEncoder.C_UTF8_ENCODING);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

        CmsXmlContentValueSequence titleSequence;

        titleSequence = xmlcontent.getValueSequence("Title", Locale.ENGLISH);
        assertEquals("Title", titleSequence.getElementName());
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
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

        // ensure the document structure is as expected
        titleSequence = xmlcontent.getValueSequence("Title", Locale.ENGLISH);
        assertEquals("Title", titleSequence.getElementName());
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
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

        // ensure the document structure is as expected
        titleSequence = xmlcontent.getValueSequence("Title", Locale.ENGLISH);
        assertEquals("Title", titleSequence.getElementName());
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
        assertEquals("Option", optionSequence.getElementName());
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
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

        optionSequence = xmlcontent.getValueSequence("Option", Locale.ENGLISH);
        assertEquals("Option", optionSequence.getElementName());
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
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-7.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.C_UTF8_ENCODING);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

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
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
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
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
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
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        deepNestedSequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(0, deepNestedSequence.getElementCount());

        nestedSequence = xmlcontent.getValueSequence("Cascade", Locale.ENGLISH);
        assertEquals(1, nestedSequence.getElementCount());
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-3.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_3, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_3, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-3.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

        assertTrue(xmlcontent.hasValue("Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("VfsLink", Locale.ENGLISH));
        assertSame(definition.getContentHandler().getClass().getName(), TestXmlContentHandler.class.getName());
    }
    
    /**
     * Test default values in the appinfo node using a nested XML content schema.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testDefaultNested() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing for  default values in nested XML content schemas");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-4.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_4, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-4.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
        System.out.println(xmlcontent.toString());

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        I_CmsXmlContentValue value1;

        value1 = xmlcontent.addValue(cms, "Cascade[1]/Option", Locale.ENGLISH, 0);
        assertEquals("Default value from the XML", value1.getStringValue(cms));   
        assertEquals("Default value from the appinfos", value1.getContentDefinition().getContentHandler().getDefault(cms, value1, Locale.ENGLISH));
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-5.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_5, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_5, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-5.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);
        
        I_CmsXmlWidget widget;        

        // make sure the selected widgets are of the configured "non-standard" type
        widget = definition.getContentHandler().getWidget(xmlcontent.getValue("Title", Locale.ENGLISH));
        assertNotNull(widget);
        assertEquals(CmsXmlBooleanWidget.class.getName(), widget.getClass().getName());
        
        // make sure the alias name works
        widget = definition.getContentHandler().getWidget(xmlcontent.getValue("Test", Locale.ENGLISH));
        assertNotNull(widget);
        assertEquals(CmsXmlHtmlWidget.class.getName(), widget.getClass().getName());
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-2.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_2, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_2, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-2.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

        assertTrue(xmlcontent.hasValue("Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("VfsLink", Locale.ENGLISH));
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsXmlHtmlValue htmlValue = (CmsXmlHtmlValue)xmlcontent.getValue("Html", Locale.ENGLISH);
        CmsXmlVfsFileValue vfsValue = (CmsXmlVfsFileValue)xmlcontent.getValue("VfsLink", Locale.ENGLISH);

        // must set the value again to ensure link table is properly initialized
        htmlValue.setStringValue(cms, htmlValue.getStringValue(cms));
        vfsValue.setStringValue(cms, vfsValue.getStringValue(cms));

        Iterator i;
        CmsLinkTable table;

        table = htmlValue.getLinkTable();
        assertEquals(3, table.size());

        i = table.iterator();
        int result = 0;
        while (i.hasNext()) {
            // iterate all links and check if the required values are found
            CmsLink link = (CmsLink)i.next();
            if (link.getTarget().equals("/sites/default/index.html") && link.isInternal()) {
                result++;
            } else if (link.getTarget().equals("http://www.alkacon.com") && !link.isInternal()) {
                result++;
            } else if (link.getTarget().equals("/sites/default/folder1/index.html")
                && link.getQuery().equals("a=b&c=d")
                && link.getAnchor().equals("anchor")
                && link.isInternal()) {
                result++;
            }
        }
        assertEquals(3, result);

        table = vfsValue.getLinkTable();
        assertEquals(1, table.size());
        CmsLink link = (CmsLink)table.iterator().next();
        assertEquals("/sites/default/index.html", link.getTarget());
        assertTrue(link.isInternal());
        assertEquals("/index.html", vfsValue.getStringValue(cms));
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

        String iso = "ISO-8859-1";
        
        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-8.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_8, content.getBytes(iso));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", iso);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, iso, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        String resourcename = "/mappingtext.html";
        // create a file in the VFS with this content (required for mappings to work)
        cms.createResource(
            resourcename, 
            OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId(), 
            content.getBytes(iso), 
            Collections.EMPTY_LIST);

        CmsFile file = cms.readFile(resourcename);
        xmlcontent = CmsXmlContentFactory.unmarshal(cms, file);
        
        CmsProperty titleProperty;        
        titleProperty = cms.readPropertyObject(resourcename, I_CmsConstants.C_PROPERTY_TITLE, false);
        assertSame(titleProperty, CmsProperty.getNullProperty());
        
        String titleStr = "This must be the Title";
        I_CmsXmlContentValue value;
        value = xmlcontent.addValue(cms, "String", Locale.ENGLISH, 0);
        value.setStringValue(cms, titleStr);

        file.setContents(xmlcontent.toString().getBytes(iso));
        cms.writeFile(file);
        
        titleProperty = cms.readPropertyObject(resourcename, I_CmsConstants.C_PROPERTY_TITLE, false);
        assertEquals(titleStr, titleProperty.getValue());
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-4.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, C_SCHEMA_SYSTEM_ID_4, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_4, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-4.xml", CmsEncoder.C_UTF8_ENCODING);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-7.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.C_UTF8_ENCODING);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

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
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.C_UTF8_ENCODING, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        value1 = xmlcontent.getValue("DeepCascade[1]/Cascade[1]/VfsLink", Locale.ENGLISH);
        value1.setStringValue(cms, "/system/workplace/warning");

        value1 = xmlcontent.getValue("DeepCascade[1]/Cascade[1]/Html", Locale.ENGLISH);
        value1.setStringValue(cms, "This HTML contains an error!");

        value1 = xmlcontent.addValue(cms, "DeepCascade[1]/Cascade[1]/Option", Locale.ENGLISH, 0);
        assertEquals("Default value from the appinfos", value1.getStringValue(cms));

        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.C_UTF8_ENCODING, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertEquals(2, errorHandler.getErrors(Locale.ENGLISH).size());
        assertEquals(3, errorHandler.getWarnings(Locale.ENGLISH).size());
    }

    /**
     * Tests the macros in messages and default values.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testMacros() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing macros in the XML content");

        CmsUser admin = cms.getRequestContext().currentUser();
        admin.setFirstname("Hans");
        admin.setLastname("Mustermann");
        admin.setEmail("hans.mustermann@germany.de");
        admin.setAddress("Heidestraße 17, München");
        cms.writeUser(admin);
        
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;
        CmsXmlContent xmlcontent;

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-9.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_9, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-9.xml", CmsEncoder.C_UTF8_ENCODING);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

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
        assertEquals("Author: Hans Mustermann (Admin), Heidestraße 17, München - hans.mustermann@germany.de", value1.getStringValue(cms));

        value1 = xmlcontent.addValue(cms, "Option", Locale.GERMAN, 0);
        assertEquals("Autor: Hans Mustermann (Admin), Heidestraße 17, München - hans.mustermann@germany.de", value1.getStringValue(cms));
        
        // output the current document
        System.out.println(xmlcontent.toString());
        // re-create the document
        xmlcontent = CmsXmlContentFactory.unmarshal(xmlcontent.toString(), CmsEncoder.C_UTF8_ENCODING, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        errorHandler = xmlcontent.validate(cms);
        assertTrue(errorHandler.hasErrors());
        assertTrue(errorHandler.hasWarnings());
        assertEquals(1, errorHandler.getErrors(Locale.ENGLISH).size());
        assertEquals(2, errorHandler.getWarnings(Locale.ENGLISH).size());
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-8.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_8, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", CmsEncoder.C_UTF8_ENCODING);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
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
        value.setStringValue(cms, "Dieser String enthällt einen Fehler (English: 'error') und eine Warnung!");

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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-8.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_8, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-8.xml", CmsEncoder.C_UTF8_ENCODING);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
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
        assertEquals("A valid HTML color value (e.g. #ffffff) is required", errorHandler.getErrors(Locale.ENGLISH).get(value.getPath()));
        
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
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-7.xsd",
            CmsEncoder.C_UTF8_ENCODING);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(C_SCHEMA_SYSTEM_ID_7, content.getBytes(CmsEncoder.C_UTF8_ENCODING));

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-7.xml", CmsEncoder.C_UTF8_ENCODING);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);

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
        assertSame(TestXmlContentHandler.class.getName(), value1.getContentDefinition().getContentHandler().getClass()
            .getName());

        value1 = xmlcontent.getValue("Cascade", Locale.ENGLISH);
        assertSame(
            TestXmlContentHandler.class.getName(), 
            value1.getContentDefinition().getContentHandler().getClass().getName());

        value1 = xmlcontent.getValue("Cascade/Title", Locale.ENGLISH);
        assertSame(
            CmsDefaultXmlContentHandler.class.getName(), 
            value1.getContentDefinition().getContentHandler().getClass().getName());
    }
}