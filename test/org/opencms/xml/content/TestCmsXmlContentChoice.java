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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests the OpenCms XML content support for <code>xsd:choice</code>.<p>
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsXmlContentChoice extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests advanced XML content definition object generation for a schema that contains xsd:choice.<p>
     *
     * @throws Exception in case something goes wrong
     */
    @Test
    @Order(4)
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
        assertTrue(indexCount == 0, "Index count " + indexCount + " not as expected");

        content.addValue(cms, "ChoiceTestA", Locale.ENGLISH, 0);
        content.addValue(cms, "ChoiceTestA/DateTimeChoice", Locale.ENGLISH, 0);
        content.addValue(cms, "ChoiceTestA/StringChoice", Locale.ENGLISH, 1);
        System.out.println(content.toString());

        I_CmsXmlContentValue v1 = content.getValue("ChoiceTestA/StringChoice", Locale.ENGLISH);
        I_CmsXmlContentValue v2 = content.getValue("ChoiceTestA/StringChoice", Locale.ENGLISH, 0);
        assertTrue(v1 != null, "StringChoice value must be available through xpath lookup");
        assertTrue(v2 != null, "StringChoice value must be available through index lookup 1");
        assertSame(v1, v2, "Value from index and xpath lookup must be the same");

        content.removeValue("ChoiceTestA/StringChoice", Locale.ENGLISH, 0);
        content.removeValue("ChoiceTestA/DateTimeChoice", Locale.ENGLISH, 0);
        System.out.println(content.toString());
    }

    /**
     * Tests XML content objects that contain a xsd:choice sequence definition.<p>
     *
     * @throws Exception in case something goes wrong
     */
    @Test
    @Order(3)
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

        assertNotNull(v1, "ChoiceTestA element must not be null");
        assertNotNull(v2, "ChoiceTestA/StringChoice element must not be null");
        assertNotNull(v3, "ChoiceTestA/DateTimeChoice element must not be null");
        assertNotNull(v4, "ChoiceTestA/StringChoice[2] element must not be null");

        xmlcontent.addValue(cms, "ChoiceTestA/StringChoice", Locale.ENGLISH, 3);
        I_CmsXmlContentValue v5 = xmlcontent.getValue("ChoiceTestA/StringChoice[3]", Locale.ENGLISH);
        assertNotNull(v5, "Value added at ChoiceTestA/StringChoice[3] must not be null");

        CmsRuntimeException caught = null;
        try {
            xmlcontent.addValue(cms, "ChoiceTestA/StringChoice", Locale.ENGLISH, 8);
        } catch (CmsRuntimeException e) {
            caught = e;
        }
        assertNotNull(caught, "Required exception not thrown when adding an element beyond index end");

        CmsXmlContentValueSequence sequence = xmlcontent.getValueSequence("ChoiceTestA/StringChoice", Locale.ENGLISH);
        assertNotNull(sequence, "ChoiceTestA value sequence must not be null");

        assertTrue(xmlcontent.hasChoiceOptions("ChoiceTestA", Locale.ENGLISH), "Choice sequence A not recognized");
        assertTrue(xmlcontent.hasChoiceOptions("ChoiceTestB", Locale.ENGLISH), "Choice sequence B not recognized");
        assertTrue(xmlcontent.hasChoiceOptions("ChoiceTestC", Locale.ENGLISH), "Choice sequence C not recognized");

        assertTrue(
            xmlcontent.hasChoiceOptions("ChoiceTestA/DateTimeChoice", Locale.ENGLISH),
            "Choice sequence A sub-options not recognized");
        assertTrue(
            xmlcontent.hasChoiceOptions("ChoiceTestB/HtmlChoice", Locale.ENGLISH),
            "Choice sequence B sub-options not recognized");
        assertFalse(
            xmlcontent.hasChoiceOptions("ChoiceTestC/NameChoiceC", Locale.ENGLISH),
            "Choice sequence C sub-options wrongly recognized");

        List<I_CmsXmlSchemaType> choices = xmlcontent.getChoiceOptions("ChoiceTestA", Locale.ENGLISH);
        assertTrue(choices.size() == 2, "Choice sequence A must have 2 choice options");
        assertTrue(choices.get(0) instanceof CmsXmlStringValue);
        assertTrue(choices.get(1) instanceof CmsXmlDateTimeValue);

        choices = xmlcontent.getChoiceOptions("ChoiceTestB", Locale.ENGLISH);
        assertTrue(choices.size() == 3, "Choice sequence B must have 3 choice options");
        assertTrue(choices.get(0) instanceof CmsXmlStringValue);
        assertTrue(choices.get(1) instanceof CmsXmlHtmlValue);
        assertTrue(choices.get(2) instanceof CmsXmlStringValue);

        xmlcontent.addValue(cms, "ChoiceTestC", Locale.ENGLISH, 1);
        choices = xmlcontent.getChoiceOptions("ChoiceTestC[2]", Locale.ENGLISH);
        assertTrue(choices.size() == 2, "Choice sequence C [2] must have 2 choice options");
        assertTrue(choices.get(0) instanceof CmsXmlStringValue);
        assertTrue(choices.get(1) instanceof CmsXmlHtmlValue);

        choices = xmlcontent.getChoiceOptions("ChoiceTestC/NameChoiceC", Locale.ENGLISH);
        assertNull(choices, "ChoiceTestC/NameChoiceC choice list must be null");

        xmlcontent.addValue(cms, "ChoiceTestA", Locale.ENGLISH, 2);
        xmlcontent.addValue(cms, "ChoiceTestA[3]/DateTimeChoice", Locale.ENGLISH, 0);
    }

    /**
     * Tests XML content definition object generation for a schema that contains xsd:choice.<p>
     *
     * @throws Exception in case something goes wrong
     */
    @Test
    @Order(2)
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
            definition.getSequenceType(),
            CmsXmlContentDefinition.SequenceType.SEQUENCE,
            "Content definition sequence not of required type SEQUENCE");
        assertTrue(
            definition.getChoiceMaxOccurs() == 0,
            "Content definition sequence choice maxOccurs is " + definition.getChoiceMaxOccurs() + " but must be 0");

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
            testA.getSequenceType(),
            CmsXmlContentDefinition.SequenceType.MULTIPLE_CHOICE,
            "Choice sequence A not of required type MULTIPLE_CHOICE");
        assertTrue(
            testA.getChoiceMaxOccurs() == 5,
            "Choice sequence A maxOccurs is " + testA.getChoiceMaxOccurs() + " but must be 5");
        assertSame(
            testB.getSequenceType(),
            CmsXmlContentDefinition.SequenceType.MULTIPLE_CHOICE,
            "Choice sequence B not of required type MULTIPLE_CHOICE");
        assertTrue(
            testB.getChoiceMaxOccurs() == 5,
            "Choice sequence B maxOccurs is " + testB.getChoiceMaxOccurs() + " but must be 5");
        assertSame(
            testC.getSequenceType(),
            CmsXmlContentDefinition.SequenceType.SINGLE_CHOICE,
            "Choice sequence C not of required type SINGLE_CHOICE");
        assertTrue(
            testC.getChoiceMaxOccurs() == 1,
            "Choice sequence C maxOccurs is " + testC.getChoiceMaxOccurs() + " but must be 1");

    }

    /**
     * Tests a simple XML file validation for a schema that contains xsd:choice.<p>
     *
     * @throws Exception in case something goes wrong
     */
    @Test
    @Order(1)
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
}