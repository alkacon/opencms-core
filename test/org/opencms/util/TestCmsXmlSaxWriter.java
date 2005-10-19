/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsXmlSaxWriter.java,v $
 * Date   : $Date: 2005/10/19 13:07:25 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXWriter;

/** 
 * Test cases for the class <code>{@link org.opencms.util.CmsXmlSaxWriter}</code>.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.0.0
 */
public class TestCmsXmlSaxWriter extends OpenCmsTestCase {

    private static final String TEXT_1 = " This is a simple text ";
    private static final String TEXT_2 = " This is a text with XML entities like <, > and & ";
    private static final String TEXT_3 = " This is a text containing international chars like \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\u20ac ";
    private static final String TEXT_3_ESC = " This is a text containing international chars like &#228;&#246;&#252;&#196;&#214;&#220;&#223;&#8364; ";
    private static final String TEXT_4 = " This is a text with \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\u20ac as well as <> and & ";
    private static final String TEXT_4_ESC = " This is a text with &#228;&#246;&#252;&#196;&#214;&#220;&#223;&#8364; as well as &lt;&gt; and &amp; ";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlSaxWriter(String arg0) {

        super(arg0);
    }

    /**
     * Test HTML escaping in XML using the <code>{@link org.opencms.util.CmsXmlSaxWriter}</code>.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testEntityExcapeInXml() throws Exception {

        // generate the SAX XML writer
        CmsXmlSaxWriter saxHandler = new CmsXmlSaxWriter(new StringWriter(4096), "US-ASCII");
        saxHandler.setEscapeXml(true);
        saxHandler.setEscapeUnknownChars(true);
        SAXWriter writer = new SAXWriter(saxHandler, saxHandler);

        // the XML document to write the XMl to
        Document doc = docTestGenerate();

        writer.write(doc);

        String result1 = saxHandler.getWriter().toString();
        System.out.println(result1);

        // ensure international chars have been replaced by entities
        assertTrue(result1.indexOf(TEXT_3_ESC) >= 0);
        assertTrue(result1.indexOf(TEXT_4_ESC) >= 0);

        // now unmarshal the document from the String
        Document doc1 = CmsXmlUtils.unmarshalHelper(result1, null);

        // the XML entities must have been replaced with their String representations
        docTestCheck(doc1);
    }

    /**
     * Test if disabling escaping in <code>{@link org.opencms.util.CmsXmlSaxWriter}</code> causes expected issues.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testWithoutEntityEscaping() throws Exception {

        // generate the SAX XML writer
        CmsXmlSaxWriter saxHandler = new CmsXmlSaxWriter(new StringWriter(4096), "US-ASCII");
        saxHandler.setEscapeXml(false);
        saxHandler.setEscapeUnknownChars(false);
        SAXWriter writer = new SAXWriter(saxHandler, saxHandler);

        // the XML document to write the XMl to
        Document doc = docTestGenerate();
        writer.write(doc);
        String result1 = saxHandler.getWriter().toString();
        System.out.println(result1);

        // now unmarshal the document from the String
        CmsXmlException error = null;
        try {
            // this must generate an error since the content was not escaped
            CmsXmlUtils.unmarshalHelper(result1, null);
        } catch (CmsXmlException e) {
            error = e;
        }
        assertNotNull("Expected Exception was not thrown", error);
        assertSame(org.opencms.xml.Messages.ERR_UNMARSHALLING_XML_DOC_0, error.getMessageContainer().getKey());
    }

    /**
     * Test round-trip generation of an XML using the <code>{@link org.opencms.util.CmsXmlSaxWriter}</code>.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testXmlRoundtrip() throws Exception {

        // generate the SAX XML writer
        CmsXmlSaxWriter saxHandler = new CmsXmlSaxWriter(new StringWriter(4096), CmsEncoder.ENCODING_ISO_8859_1);
        saxHandler.setEscapeXml(true);
        saxHandler.setEscapeUnknownChars(false);
        SAXWriter writer = new SAXWriter(saxHandler, saxHandler);

        // the XML document to write the XMl to
        Document doc = docTestGenerate();

        // write the document to a String using the SAX writer
        writer.write(doc);

        String result1 = saxHandler.getWriter().toString();
        System.out.println(result1);
        // generate document from String        
        Document doc1 = CmsXmlUtils.unmarshalHelper(result1, null);
        // check doc1 values
        docTestCheck(doc1);

        // generate another document 
        CmsXmlSaxWriter saxHandler2 = new CmsXmlSaxWriter(new StringWriter(4096), CmsEncoder.ENCODING_ISO_8859_1);
        saxHandler.setEscapeXml(true);
        saxHandler.setEscapeUnknownChars(false);
        SAXWriter writer2 = new SAXWriter(saxHandler2, saxHandler2);
        // use the document generated from the String as input
        writer2.write(doc1);
        String result2 = saxHandler2.getWriter().toString();
        System.out.println(result2);
        Document doc2 = CmsXmlUtils.unmarshalHelper(result2, null);

        // both docs must be equal, String and XML
        assertEquals(result1, result2);
        assertEquals(doc1, doc2);
        // check content of doc2
        docTestCheck(doc2);
    }

    /**
     * Asserts all values in the given focument match.<p>
     * 
     * @param doc the document to check
     */
    private void docTestCheck(Document doc) {

        assertEquals(TEXT_1, doc.getRootElement().element("texts").element("sub1").getText());
        assertEquals(TEXT_2, doc.getRootElement().element("texts").element("sub2").getText());
        assertEquals(TEXT_3, doc.getRootElement().element("texts").element("sub3").getText());
        assertEquals(TEXT_4, doc.getRootElement().element("texts").element("sub4").getText());
        assertEquals(TEXT_1, doc.getRootElement().element("cdatas").element("sub1").getText());
        assertEquals(TEXT_2, doc.getRootElement().element("cdatas").element("sub2").getText());
        assertEquals(TEXT_3, doc.getRootElement().element("cdatas").element("sub3").getText());
        assertEquals(TEXT_4, doc.getRootElement().element("cdatas").element("sub4").getText());
    }

    /**
     * Returns a test XML document.<p>
     * 
     * @return a test XML document
     */
    private Document docTestGenerate() {

        Document doc = DocumentHelper.createDocument();

        Element root = doc.addElement("testroot");
        Element texts = root.addElement("texts");
        texts.addElement("sub1").setText(TEXT_1);
        texts.addElement("sub2").setText(TEXT_2);
        texts.addElement("sub3").setText(TEXT_3);
        texts.addElement("sub4").setText(TEXT_4);
        Element cdatas = root.addElement("cdatas");
        cdatas.addElement("sub1").addCDATA(TEXT_1);
        cdatas.addElement("sub2").addCDATA(TEXT_2);
        cdatas.addElement("sub3").addCDATA(TEXT_3);
        cdatas.addElement("sub4").addCDATA(TEXT_4);

        return doc;
    }
}