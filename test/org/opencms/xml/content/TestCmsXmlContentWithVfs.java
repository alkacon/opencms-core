/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/content/TestCmsXmlContentWithVfs.java,v $
 * Date   : $Date: 2004/11/30 14:23:51 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.xmlwidgets.CmsXmlBooleanWidget;
import org.opencms.workplace.xmlwidgets.I_CmsXmlWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlStringValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the link resolver for XML contents.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.6 $
 */
public class TestCmsXmlContentWithVfs extends OpenCmsTestCase {

    private static final String C_SCHEMA_SYSTEM_ID_2 = "http://www.opencms.org/test2.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_3 = "http://www.opencms.org/test3.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_4 = "http://www.opencms.org/test4.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_5 = "http://www.opencms.org/test5.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_6 = "http://www.opencms.org/test6.xsd";
    private static final String C_SCHEMA_SYSTEM_ID_7 = "http://www.opencms.org/test7.xsd";

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
        suite.addTest(new TestCmsXmlContentWithVfs("testNestedSchema"));
        suite.addTest(new TestCmsXmlContentWithVfs("testAddRemoveNestedElements"));
        suite.addTest(new TestCmsXmlContentWithVfs("testValueIndex"));
        suite.addTest(new TestCmsXmlContentWithVfs("testGuiWidgetMapping"));
        suite.addTest(new TestCmsXmlContentWithVfs("testLinkResolver"));
        
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

        for (int i=0; i<2; i++) {
            I_CmsXmlContentValue value = toastSequence.addValue(0);
            value.setStringValue("Added toast value " + i);
        }

        // output the current document
        System.out.println(xmlcontent.toString());
        
        assertEquals(toastSequence.getElementCount(), 3);
        for (int i=0; i<3; i++) {
            I_CmsXmlContentValue value = toastSequence.getValue(i);
            assertEquals(i, value.getIndex());
        } 
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
        newValue = nestedSequence.addValue(0);
        assertNotNull(newValue);
        assertFalse(newValue.isSimpleType());
        assertEquals(CmsXmlNestedContentDefinition.class.getName(), newValue.getClass().getName());
        
        // re-create the XML content
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
        
        nestedSequence = xmlcontent.getValueSequence("Cascade", Locale.ENGLISH);
        assertEquals(2, nestedSequence.getElementCount());
        
        CmsXmlContentValueSequence deepNestedSequence;
        deepNestedSequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(0, deepNestedSequence.getElementCount());        
        
        newValue = deepNestedSequence.addValue(0);
        assertNotNull(newValue);
        assertFalse(newValue.isSimpleType());
        assertEquals(CmsXmlNestedContentDefinition.class.getName(), newValue.getClass().getName());
        
        // re-create the XML content
        content = xmlcontent.toString();
        System.out.println(content);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.C_UTF8_ENCODING, resolver);
        
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
        
        deepNestedSequence = xmlcontent.getValueSequence("DeepCascade", Locale.ENGLISH);
        assertEquals(0, deepNestedSequence.getElementCount());
        
        nestedSequence = xmlcontent.getValueSequence("Cascade", Locale.ENGLISH);
        assertEquals(1, nestedSequence.getElementCount());                        
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
        assertEquals(
            "This is just a modification test", 
            ((CmsXmlStringValue)titleSequence.getValues().get(0)).getStringValue(cms, xmlcontent));

        CmsXmlStringValue newValue;

        newValue = (CmsXmlStringValue)titleSequence.addValue(0);
        assertEquals(2, titleSequence.getElementCount());
        assertEquals(newValue, titleSequence.getValues().get(0));
        newValue.setStringValue("This is another Value!");

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
        assertEquals(
            "This is another Value!", 
            ((CmsXmlStringValue)titleSequence.getValues().get(0)).getStringValue(cms, xmlcontent));
        assertEquals(
            "This is just a modification test", 
            ((CmsXmlStringValue)titleSequence.getValues().get(1)).getStringValue(cms, xmlcontent));

        // add an element at the last position
        newValue = (CmsXmlStringValue)titleSequence.addValue(2);
        newValue.setStringValue("This is the last value.");
        assertEquals(newValue, titleSequence.getValues().get(2));
        // add another element at the 2nd position
        newValue = (CmsXmlStringValue)titleSequence.addValue(1);
        newValue.setStringValue("This is the 2nd value.");
        assertEquals(newValue, titleSequence.getValues().get(1));
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
        assertEquals("This is another Value!", ((CmsXmlStringValue)titleSequence.getValues().get(0)).getStringValue(
            cms,
            xmlcontent));
        assertEquals("This is the 2nd value.", ((CmsXmlStringValue)titleSequence.getValues().get(1)).getStringValue(
            cms,
            xmlcontent));
        assertEquals("This is just a modification test", ((CmsXmlStringValue)titleSequence.getValues().get(2))
            .getStringValue(cms, xmlcontent));
        assertEquals("This is the last value.", ((CmsXmlStringValue)titleSequence.getValues().get(3)).getStringValue(
            cms,
            xmlcontent));

        // now the optional element
        CmsXmlContentValueSequence optionSequence;

        optionSequence = xmlcontent.getValueSequence("Option", Locale.ENGLISH);
        assertEquals("Option", optionSequence.getElementName());
        assertEquals(0, optionSequence.getElementCount());
        assertEquals(0, optionSequence.getMinOccurs());
        assertEquals(2, optionSequence.getMaxOccurs());

        // add an element for the optional element
        newValue = (CmsXmlStringValue)optionSequence.addValue(0);
        newValue.setStringValue("Optional value 1");
        assertEquals(newValue, optionSequence.getValues().get(0));
        // add another element
        newValue = (CmsXmlStringValue)optionSequence.addValue(0);
        newValue.setStringValue("Optional value 0");
        assertEquals(newValue, optionSequence.getValues().get(0));
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

        assertEquals("Optional value 0", ((CmsXmlStringValue)optionSequence.getValues().get(0)).getStringValue(
            cms,
            xmlcontent));
        assertEquals("Optional value 1", ((CmsXmlStringValue)optionSequence.getValues().get(1)).getStringValue(
            cms,
            xmlcontent));
        
        optionSequence.removeValue(1);
        assertEquals(1, optionSequence.getElementCount());
        assertEquals("Optional value 0", ((CmsXmlStringValue)optionSequence.getValues().get(0)).getStringValue(
            cms,
            xmlcontent));
        
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
        assertEquals("This is the 2nd value.", ((CmsXmlStringValue)titleSequence.getValues().get(0)).getStringValue(
            cms,
            xmlcontent));
        assertEquals("This is just a modification test", ((CmsXmlStringValue)titleSequence.getValues().get(1))
            .getStringValue(cms, xmlcontent));

        // now re-create the XML content from the XML document
        content = xmlcontent.toString();
        System.out.println(content);
    }

    /**
     * Test using the GUI widget mapping appinfo nodes.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testGuiWidgetMapping() throws Exception {

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

        // make sure the selected widgets are of the configured "non-standard" type
        I_CmsXmlWidget widget = definition.getContentHandler().getEditorWidget(
            xmlcontent.getValue("Title", Locale.ENGLISH),
            xmlcontent,
            definition);
        assertNotNull(widget);
        assertEquals(CmsXmlBooleanWidget.class.getName(), widget.getClass().getName());
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

        assertTrue(xmlcontent.hasValue("Title", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Title[0]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[0]/Html[0]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[0]/VfsLink[0]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[0]/VfsLink[1]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[1]/Html[0]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[1]/VfsLink[0]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[1]/Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade[1]/VfsLink", Locale.ENGLISH));

        //        assertTrue(xmlcontent.hasValue("Cascade", Locale.ENGLISH));
        //        assertTrue(xmlcontent.hasValue("Cascade[0]", Locale.ENGLISH));
        //        assertTrue(xmlcontent.hasValue("Cascade[1]", Locale.ENGLISH));

        assertTrue(xmlcontent.hasValue("Cascade/Html", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade/Html[0]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink[0]", Locale.ENGLISH));
        assertTrue(xmlcontent.hasValue("Cascade/VfsLink[1]", Locale.ENGLISH));

        assertSame(definition.getContentHandler().getClass().getName(), TestXmlContentHandler.class.getName());
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
        htmlValue.setStringValue(cms, xmlcontent, htmlValue.getStringValue(cms, xmlcontent));
        vfsValue.setStringValue(cms, xmlcontent, vfsValue.getStringValue(cms, xmlcontent));

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
        assertEquals("/index.html", vfsValue.getStringValue(cms, xmlcontent));
    }
}