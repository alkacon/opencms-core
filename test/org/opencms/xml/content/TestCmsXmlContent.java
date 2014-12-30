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

import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.types.CmsXmlDateTimeValue;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlLocaleValue;
import org.opencms.xml.types.CmsXmlStringValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Locale;

/**
 * Tests for generating an XML content.<p>
 * 
 */
public class TestCmsXmlContent extends OpenCmsTestCase {

    /** The schema id. */
    private static final String SCHEMA_SYSTEM_ID_1 = "http://www.opencms.org/test1.xsd";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContent(String arg0) {

        super(arg0);
    }

    /**
     * Tests moving elements up and down in the XML content.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testMoveUpDown() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        String content;
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-1.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1, resolver);
        // store content definition in entitiy resolver
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-1.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_SYSTEM_ID_1,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        // now create the XML content
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        // this content comes from the file that has been read
        String nn = "String";
        CmsXmlStringValue stringValue = (CmsXmlStringValue)xmlcontent.getValue(nn, Locale.ENGLISH, 0);
        assertEquals("Multitest 1", stringValue.getStringValue(null));

        // add some more nodes to the content
        I_CmsXmlContentValue value = xmlcontent.addValue(null, nn, Locale.ENGLISH, 1);
        value.setStringValue(null, "Node 2");
        value = xmlcontent.addValue(null, nn, Locale.ENGLISH, 2);
        value.setStringValue(null, "Node 3");
        value = xmlcontent.addValue(null, nn, Locale.ENGLISH, 3);
        String node4 = "Node 4";
        value.setStringValue(null, node4);

        // we must have 4 "String" nodes now
        int maxIndex = xmlcontent.getValue(nn, Locale.ENGLISH).getMaxIndex();
        assertEquals(4, maxIndex);

        // now we have 4 nodes, check the last node
        I_CmsXmlContentValue checkValue = xmlcontent.getValue(nn, Locale.ENGLISH, maxIndex - 1);
        assertEquals(node4, checkValue.getStringValue(null));

        // move the node down 3 times, then it must be at the first position
        value.moveDown();
        value.moveDown();
        value.moveDown();
        System.out.println(xmlcontent.toString());
        checkValue = xmlcontent.getValue(nn, Locale.ENGLISH, 0);
        assertEquals(node4, checkValue.getStringValue(null));

        // one more move down should have no effect
        value.moveDown();
        checkValue = xmlcontent.getValue(nn, Locale.ENGLISH, 0);
        assertEquals(node4, checkValue.getStringValue(null));

        // now move the node up again
        value.moveUp();
        value.moveUp();
        value.moveUp();
        System.out.println(xmlcontent.toString());
        checkValue = xmlcontent.getValue(nn, Locale.ENGLISH, maxIndex - 1);
        assertEquals(node4, checkValue.getStringValue(null));

        // one more move up should have no effect
        value.moveUp();
        checkValue = xmlcontent.getValue(nn, Locale.ENGLISH, maxIndex - 1);
        assertEquals(node4, checkValue.getStringValue(null));
    }

    /**
     * Test unmarshalling an XML content from a String.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testUnmarshalFromString() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        String content;
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-definition-1.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1, resolver);
        // store content definition in entitiy resolver
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-1.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_SYSTEM_ID_1,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        // now create the XML content
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        assertTrue(xmlcontent.hasValue("String", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("DateTime", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Locale", Locale.ENGLISH));

        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsXmlStringValue stringValue = (CmsXmlStringValue)xmlcontent.getValue("String", Locale.ENGLISH);
        CmsXmlDateTimeValue dateTimeValue = (CmsXmlDateTimeValue)xmlcontent.getValue("DateTime", Locale.ENGLISH);
        CmsXmlHtmlValue htmlValue = (CmsXmlHtmlValue)xmlcontent.getValue("Html", Locale.ENGLISH);
        CmsXmlLocaleValue localeValue = (CmsXmlLocaleValue)xmlcontent.getValue("Locale", Locale.ENGLISH);

        assertEquals("Multitest 1", stringValue.getStringValue(null));
        assertEquals("-58254180000", dateTimeValue.getStringValue(null));
        assertEquals("<p>This is some Html</p>", htmlValue.getStringValue(null));
        assertEquals("en_EN", localeValue.getStringValue(null));
    }
}