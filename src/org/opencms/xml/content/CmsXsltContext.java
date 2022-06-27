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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;

import org.dom4j.DocumentException;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides extension functions for use in XSLT version transformation files.
 *
 * <p>An instance of this class is meant to be used for only a single content conversion.
 */
public class CmsXsltContext {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXsltContext.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** The document builder factory. */
    private DocumentBuilderFactory m_documentBuilderFactory;

    /** The document builder. */
    private DocumentBuilder m_documentBuilder;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     */
    public CmsXsltContext(CmsObject cms) {

        m_cms = cms;
        m_documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            m_documentBuilder = m_documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * XSLT extension function that converts the XML for a value between two different OpenCms content value types.
     *
     * @param value a node list that is expected to contain exactly one element, which should represent an XML content value
     * @param sourceTypeName the original type of the value
     * @param targetTypeName the type which the value should be converted to
     * @param elementName the name that should be used
     *
     * @return a node list containing the converted XML value
     */
    public NodeList convertType(NodeList value, String sourceTypeName, String targetTypeName, String elementName) {

        if (value.getLength() != 1) {
            throw new RuntimeException("convertType must be passed exactly one node.");
        }

        // this is somewhat convoluted - XSLT uses the org.w3c.dom classes, while OpenCms XML contents use dom4j DOM classes.
        // We create a dom4j document containing only the relevant XML for the value, then interpret it as an OpenCms value
        // on which we can call getStringValue(). Then we create a new value in the dom4j document for the new type, and set its
        // string value to the previously read string value. In the end, we have to translate everything back to org.w3c.dom format again.

        Document doc = m_documentBuilder.newDocument();
        Node copiedNode = doc.importNode(value.item(0), true);
        Element w3cRoot = doc.createElement("root");
        doc.appendChild(w3cRoot);
        w3cRoot.appendChild(copiedNode);
        org.dom4j.io.DOMReader reader = new DOMReader();
        org.dom4j.Document dom4jDoc = reader.read(doc);
        org.dom4j.Element dom4jRoot = dom4jDoc.getRootElement();
        org.dom4j.Element dom4jValue = dom4jRoot.elements().get(0);
        final CmsDefaultXmlContentHandler handler = new CmsDefaultXmlContentHandler();

        // We need a dummy content with a content handler because creating a new value require a reference to a content,
        // and the content handler is asked for a default value. This dummy content is probably unusable for anything else.
        I_CmsXmlDocument dummyContent = new CmsXmlContent() {

            public I_CmsXmlContentHandler getHandler() {

                return handler;
            }

        };
        I_CmsXmlContentValue sourceType = (I_CmsXmlContentValue)OpenCms.getXmlContentTypeManager().getContentType(
            sourceTypeName);

        // We must use newInstance here so the element name is set
        I_CmsXmlContentValue targetType = (I_CmsXmlContentValue)OpenCms.getXmlContentTypeManager().getContentType(
            targetTypeName).newInstance(elementName, "0", "1");

        I_CmsXmlContentValue sourceValue = sourceType.createValue(dummyContent, dom4jValue, Locale.ENGLISH);
        String valueString = sourceValue.getStringValue(m_cms);

        // once we have the string value, we don't need the XML structure for the original value anymore - throw it away
        dom4jValue.detach();

        org.dom4j.Element dom4jNewValue = targetType.generateXml(m_cms, dummyContent, dom4jRoot, Locale.ENGLISH);
        I_CmsXmlContentValue newValue = targetType.createValue(dummyContent, dom4jNewValue, Locale.ENGLISH);
        newValue.setStringValue(m_cms, valueString);
        List<Node> result = new ArrayList<>();
        org.w3c.dom.Document newW3cDoc;
        try {
            newW3cDoc = new DOMWriter().write(dom4jDoc);
            result.add(newW3cDoc.getDocumentElement().getFirstChild());
        } catch (DocumentException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return new NodeList() {

            public int getLength() {

                return result.size();
            }

            public Node item(int index) {

                return result.get(index);
            }

        };
    }
}
