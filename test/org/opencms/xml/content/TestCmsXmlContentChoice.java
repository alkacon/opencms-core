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

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.CmsXmlDateTimeValue;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlStringValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms XML content support for <code>xsd:choice</code>.<p>
 *
 */
public class TestCmsXmlContentChoice extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContentChoice(String arg0) {

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
        suite.setName(TestCmsXmlContentChoice.class.getName());

        suite.addTest(new TestCmsXmlContentChoice("testSimpleChoiceSchemaValidation"));
        suite.addTest(new TestCmsXmlContentChoice("testChoiceXmlContentDefinitionCreation"));
        suite.addTest(new TestCmsXmlContentChoice("testChoiceXmlContent"));
        suite.addTest(new TestCmsXmlContentChoice("testChoiceAdvancedXmlContentDefinitionCreation"));

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
     * Tests a simple XML file validation for a schema that contains xsd:choice.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testSimpleChoiceSchemaValidation() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing a simple XML file validation for a schema that contains xsd:choice");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        cacheXmlSchema(
            "org/opencms/xml/content/xmlcontent-choice-definition-1.xsd",
            "http://www.opencms.org/testChoice1.xsd");
        cacheXmlSchema(
            "org/opencms/xml/content/xmlcontent-choice-definition-1-subA.xsd",
            "http://www.opencms.org/choice-definition1-subA.xsd");
        cacheXmlSchema(
            "org/opencms/xml/content/xmlcontent-choice-definition-1-subB.xsd",
            "http://www.opencms.org/choice-definition1-subB.xsd");
        cacheXmlSchema(
            "org/opencms/xml/content/xmlcontent-choice-definition-1-subC.xsd",
            "http://www.opencms.org/choice-definition1-subC.xsd");

        // now read the XML content
        byte[] content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-choice-1.xml");

        // validate the XML structure
        CmsXmlUtils.validateXmlStructure(content, resolver);
    }

    /**
     * Read the given file and cache it's contents as XML schema with the given system id.
     *
     * @param fileName the file name to read
     * @param systemId the XML schema system id to use
     *
     * @throws IOException in case of errors reading the file
     */
    private void cacheXmlSchema(String fileName, String systemId) throws IOException {

        // read the XML schema
        byte[] schema = CmsFileUtil.readFile(fileName);
        // store the XML schema in the resolver
        CmsXmlEntityResolver.cacheSystemId(systemId, schema);
    }

    /**
     * Tests XML content definition object generation for a schema that contains xsd:choice.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testChoiceXmlContentDefinitionCreation() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing XML content definition object generation for a schema that contains xsd:choice");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // fire "clear cache" event to clear up previously cached schemas
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>()));
        // read and cache the sub-schemas
        cacheXmlSchema(
            "org/opencms/xml/content/xmlcontent-choice-definition-1-subA.xsd",
            "http://www.opencms.org/choice-definition1-subA.xsd");
        cacheXmlSchema(
            "org/opencms/xml/content/xmlcontent-choice-definition-1-subB.xsd",
            "http://www.opencms.org/choice-definition1-subB.xsd");
        cacheXmlSchema(
            "org/opencms/xml/content/xmlcontent-choice-definition-1-subC.xsd",
            "http://www.opencms.org/choice-definition1-subC.xsd");
        // now read the XML from the given file and store it in the resolver
        String schema = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-choice-definition-1.xsd",
            CmsEncoder.ENCODING_UTF_8);

        // the point of this test really is that there is no exception thrown here if xsd:choice is in the schema
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(
            schema,
            "http://www.opencms.org/testChoice1.xsd",
            resolver);

        System.out.println(definition.getSchema().asXML());
        CmsXmlEntityResolver.cacheSystemId(
            "http://www.opencms.org/testChoice1.xsd",
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));

        assertSame(
            "Content definition sequence not of required type SEQUENCE",
            definition.getSequenceType(),
            CmsXmlContentDefinition.SequenceType.SEQUENCE);
        assertTrue(
            "Content definition sequence choice maxOccurs is " + definition.getChoiceMaxOccurs() + " but must be 0",
            definition.getChoiceMaxOccurs() == 0);

        // now read the XML content
        byte[] content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-choice-1.xml");

        // validate the XML structure
        CmsXmlUtils.validateXmlStructure(content, resolver);

        // now create an XML content from the file with the xsd:choice content definition
        CmsXmlContentFactory.unmarshal(
            new String(content, CmsEncoder.ENCODING_UTF_8),
            CmsEncoder.ENCODING_UTF_8,
            resolver);

        CmsXmlNestedContentDefinition nestA = (CmsXmlNestedContentDefinition)definition.getSchemaType("ChoiceTestA");
        CmsXmlNestedContentDefinition nestB = (CmsXmlNestedContentDefinition)definition.getSchemaType("ChoiceTestB");
        CmsXmlNestedContentDefinition nestC = (CmsXmlNestedContentDefinition)definition.getSchemaType("ChoiceTestC");

        CmsXmlContentDefinition testA = nestA.getNestedContentDefinition();
        CmsXmlContentDefinition testB = nestB.getNestedContentDefinition();
        CmsXmlContentDefinition testC = nestC.getNestedContentDefinition();

        assertSame(
            "Choice sequence A not of required type MULTIPLE_CHOICE",
            testA.getSequenceType(),
            CmsXmlContentDefinition.SequenceType.MULTIPLE_CHOICE);
        assertTrue(
            "Choice sequence A maxOccurs is " + testA.getChoiceMaxOccurs() + " but must be 5",
            testA.getChoiceMaxOccurs() == 5);
        assertSame(
            "Choice sequence B not of required type MULTIPLE_CHOICE",
            testB.getSequenceType(),
            CmsXmlContentDefinition.SequenceType.MULTIPLE_CHOICE);
        assertTrue(
            "Choice sequence B maxOccurs is " + testB.getChoiceMaxOccurs() + " but must be 5",
            testB.getChoiceMaxOccurs() == 5);
        assertSame(
            "Choice sequence C not of required type SINGLE_CHOICE",
            testC.getSequenceType(),
            CmsXmlContentDefinition.SequenceType.SINGLE_CHOICE);
        assertTrue(
            "Choice sequence C maxOccurs is " + testC.getChoiceMaxOccurs() + " but must be 1",
            testC.getChoiceMaxOccurs() == 1);

    }

    /**
     * Tests XML content objects that contain a xsd:choice sequence definition.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testChoiceXmlContent() throws Exception {

        // please note: this test relies on the "testChoiceXmlContentDefinitionCreation" test
        // to cache the required XSD

        CmsObject cms = getCmsObject();
        echo("Testing XML content objects that contain a xsd:choice sequence definition");
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        // read the XML content
        byte[] content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-choice-1.xml");

        // now create the XML content from the file with the xsd:choice content definition
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(
            new String(content, CmsEncoder.ENCODING_UTF_8),
            CmsEncoder.ENCODING_UTF_8,
            resolver);

        I_CmsXmlContentValue v1 = xmlcontent.getValue("ChoiceTestA", Locale.ENGLISH);
        I_CmsXmlContentValue v2 = xmlcontent.getValue("ChoiceTestA/StringChoice", Locale.ENGLISH);
        I_CmsXmlContentValue v3 = xmlcontent.getValue("ChoiceTestA/DateTimeChoice", Locale.ENGLISH);
        I_CmsXmlContentValue v4 = xmlcontent.getValue("ChoiceTestA/StringChoice[2]", Locale.ENGLISH);

        assertNotNull("ChoiceTestA element must not be null", v1);
        assertNotNull("ChoiceTestA/StringChoice element must not be null", v2);
        assertNotNull("ChoiceTestA/DateTimeChoice element must not be null", v3);
        assertNotNull("ChoiceTestA/StringChoice[2] element must not be null", v4);

        xmlcontent.addValue(cms, "ChoiceTestA/StringChoice", Locale.ENGLISH, 3);
        I_CmsXmlContentValue v5 = xmlcontent.getValue("ChoiceTestA/StringChoice[3]", Locale.ENGLISH);
        assertNotNull("Value added at ChoiceTestA/StringChoice[3] must not be null", v5);

        CmsRuntimeException caught = null;
        try {
            xmlcontent.addValue(cms, "ChoiceTestA/StringChoice", Locale.ENGLISH, 8);
        } catch (CmsRuntimeException e) {
            caught = e;
        }
        assertNotNull("Required exception not thrown when adding an element beyond index end", caught);

        CmsXmlContentValueSequence sequence = xmlcontent.getValueSequence("ChoiceTestA/StringChoice", Locale.ENGLISH);
        assertNotNull("ChoiceTestA value sequence must not be null", sequence);

        assertTrue("Choice sequence A not recognized", xmlcontent.hasChoiceOptions("ChoiceTestA", Locale.ENGLISH));
        assertTrue("Choice sequence B not recognized", xmlcontent.hasChoiceOptions("ChoiceTestB", Locale.ENGLISH));
        assertTrue("Choice sequence C not recognized", xmlcontent.hasChoiceOptions("ChoiceTestC", Locale.ENGLISH));

        assertTrue(
            "Choice sequence A sub-options not recognized",
            xmlcontent.hasChoiceOptions("ChoiceTestA/DateTimeChoice", Locale.ENGLISH));
        assertTrue(
            "Choice sequence B sub-options not recognized",
            xmlcontent.hasChoiceOptions("ChoiceTestB/HtmlChoice", Locale.ENGLISH));
        assertFalse(
            "Choice sequence C sub-options wrongly recognized",
            xmlcontent.hasChoiceOptions("ChoiceTestC/NameChoiceC", Locale.ENGLISH));

        List<I_CmsXmlSchemaType> choices = xmlcontent.getChoiceOptions("ChoiceTestA", Locale.ENGLISH);
        assertTrue("Choice sequence A must have 2 choice options", choices.size() == 2);
        assertTrue(choices.get(0) instanceof CmsXmlStringValue);
        assertTrue(choices.get(1) instanceof CmsXmlDateTimeValue);

        choices = xmlcontent.getChoiceOptions("ChoiceTestB", Locale.ENGLISH);
        assertTrue("Choice sequence B must have 3 choice options", choices.size() == 3);
        assertTrue(choices.get(0) instanceof CmsXmlStringValue);
        assertTrue(choices.get(1) instanceof CmsXmlHtmlValue);
        assertTrue(choices.get(2) instanceof CmsXmlStringValue);

        xmlcontent.addValue(cms, "ChoiceTestC", Locale.ENGLISH, 1);
        choices = xmlcontent.getChoiceOptions("ChoiceTestC[2]", Locale.ENGLISH);
        assertTrue("Choice sequence C [2] must have 2 choice options", choices.size() == 2);
        assertTrue(choices.get(0) instanceof CmsXmlStringValue);
        assertTrue(choices.get(1) instanceof CmsXmlHtmlValue);

        choices = xmlcontent.getChoiceOptions("ChoiceTestC/NameChoiceC", Locale.ENGLISH);
        assertNull("ChoiceTestC/NameChoiceC choice list must be null", choices);

        xmlcontent.addValue(cms, "ChoiceTestA", Locale.ENGLISH, 2);
        xmlcontent.addValue(cms, "ChoiceTestA[3]/DateTimeChoice", Locale.ENGLISH, 0);
    }

    /**
     * Tests advanced XML content definition object generation for a schema that contains xsd:choice.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testChoiceAdvancedXmlContentDefinitionCreation() throws Exception {

        // please note: this test relies on the "testChoiceXmlContentDefinitionCreation" test
        // to cache the required XSD

        CmsObject cms = getCmsObject();
        echo("Testing XML content definition object generation for a schema that contains xsd:choice");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        // now test a content definition where the root node already is a xsd:choice
        String schema = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-choice-definition-2.xsd",
            CmsEncoder.ENCODING_UTF_8);

        // make sure that there is no exception thrown here if xsd:choice is in the schema root
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(
            schema,
            "http://www.opencms.org/testChoice2.xsd",
            resolver);

        System.out.println(CmsXmlUtils.marshal(definition.getSchema(), CmsEncoder.ENCODING_UTF_8));

        CmsXmlEntityResolver.cacheSystemId(
            "http://www.opencms.org/testChoice2.xsd",
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));

        CmsXmlContent content = CmsXmlContentFactory.createDocument(
            cms,
            Locale.ENGLISH,
            CmsEncoder.ENCODING_UTF_8,
            definition);

        System.out.println(content.toString());

        // make sure the XML is valid
        content.validateXmlStructure(resolver);

        int indexCount = content.getIndexCount("String", Locale.ENGLISH);
        assertTrue("Index count " + indexCount + " not as expected", indexCount == 0);

        content.addValue(cms, "ChoiceTestA", Locale.ENGLISH, 0);
        content.addValue(cms, "ChoiceTestA/DateTimeChoice", Locale.ENGLISH, 0);
        content.addValue(cms, "ChoiceTestA/StringChoice", Locale.ENGLISH, 1);
        System.out.println(content.toString());

        I_CmsXmlContentValue v1 = content.getValue("ChoiceTestA/StringChoice", Locale.ENGLISH);
        I_CmsXmlContentValue v2 = content.getValue("ChoiceTestA/StringChoice", Locale.ENGLISH, 0);
        assertTrue("StringChoice value must be available through xpath lookup", v1 != null);
        assertTrue("StringChoice value must be available through index lookup 1", v2 != null);
        assertSame("Value from index and xpath lookup must be the same", v1, v2);

        content.removeValue("ChoiceTestA/StringChoice", Locale.ENGLISH, 0);
        content.removeValue("ChoiceTestA/DateTimeChoice", Locale.ENGLISH, 0);
        System.out.println(content.toString());
    }
}