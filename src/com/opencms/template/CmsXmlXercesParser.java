/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlXercesParser.java,v $
* Date   : $Date: 2003/09/17 14:30:14 $
* Version: $Revision: 1.25 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.template;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of the OpenCms XML parser interface for
 * the Xerces parser.
 * 
 * @author Alexander Kandzior
 * @author Alexander Lucas
 * @version $Revision: 1.25 $ $Date: 2003/09/17 14:30:14 $
 */
public class CmsXmlXercesParser implements I_CmsXmlParser {
    
    /** Prevents the parser from printing multiple error messages.*/
    private static boolean c_xercesWarning = false;
    
    /**
     * Creates an empty DOM XML document.
     * Workaround because the original method is not working as expected.
     * 
     * @param docNod first Node in empty  XML document
     * @return Empty document.
     */
    public Document createEmptyDocument(String docNod) throws Exception {
        String docXml = new String("<?xml version=\"1.0\" encoding=\"" + OpenCms.getDefaultEncoding() + "\"?>");
        docXml = docXml + "<" + docNod + ">" + "</" + docNod + ">";
        StringReader reader = new StringReader(docXml);
        return parse(reader);
    }
    
    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out OutputStream to print to.
     */
    public void getXmlText(Document doc, OutputStream out, String encoding) {
        OutputFormat outf =
            new OutputFormat(doc, (encoding == null) ? getOriginalEncoding(doc) : encoding, true);
        outf.setLineWidth(C_XML_LINE_WIDTH);
        outf.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(out, outf);
        try {
            DOMSerializer domSerializer = serializer.asDOMSerializer();
            domSerializer.serialize(doc);
        } catch (Exception e) {
            if (OpenCms.getLog(CmsLog.CHANNEL_TEMPLATE_XML).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_TEMPLATE_XML).error("Xml parsing error", e);
            }
        }
    }

    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out Writer to print to.
     * @param encoding the character encoding to be used while serializing
     */
    public void getXmlText(Document doc, Writer out, String encoding) {
        OutputFormat outf =
            new OutputFormat(doc, (encoding == null) ? getOriginalEncoding(doc) : encoding, true);
        outf.setLineWidth(C_XML_LINE_WIDTH);
        outf.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(out, outf);
        try {
            DOMSerializer domSerializer = serializer.asDOMSerializer();
            domSerializer.serialize(doc);
        } catch (Exception e) {
            if (OpenCms.getLog(CmsLog.CHANNEL_TEMPLATE_XML).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_TEMPLATE_XML).error("Xml parsing error", e);
            }
        }
    }

    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out Writer to print to.
     */
    public void getXmlText(Document doc, Writer out) {
        getXmlText(doc, out, getOriginalEncoding(doc));
    }
    
    /**
     * Used to import a node from a foreign document.
     * @param doc Destination document that should import the node.
     * @param node Node to be imported.
     * @return New node that belongs to the document <code>doc</code>
     */
    public Node importNode(Document doc, Node node) {
        return ((org.apache.xerces.dom.DocumentImpl)doc).importNode(node, true);
    }
    
    /**
     * Parses the given text with the Xerces parser.
     * @param in Reader with the input text.
     * @return Parsed text as DOM document.
     * @throws Exception
     */
    public Document parse(Reader in) throws Exception {
        return parse(new InputSource(in));
    }
    
    public Document parse(InputStream in) throws Exception {
        return parse(new InputSource(in));
    }
    
    /**
     * Common internal method to actually parse input.
     */ 
    protected Document parse(InputSource input) throws Exception {
        DOMParser parser = new DOMParser();
        try {
            parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
        }
        catch(SAXException e) {
            if(OpenCms.getLog(CmsLog.CHANNEL_TEMPLATE_XML).isInfoEnabled()  && !c_xercesWarning) {
                OpenCms.getLog(CmsLog.CHANNEL_TEMPLATE_XML).info("[CmsXmlXercesParser] Cannot set parser feature for apache xerces XML parser.");
                OpenCms.getLog(CmsLog.CHANNEL_TEMPLATE_XML).info("[CmsXmlXercesParser] This is NOT critical, but you should better use xerces 1.1.1 or higher.");
                c_xercesWarning = true;
            }
        }
        parser.parse(input);
        return parser.getDocument();
    }
    
    public void serialize(Document doc, OutputStream in) throws Exception {
    }

    /**
     * Gets a description of the parser.
     * @return Parser description.
     */
    public String toString() {
        return "Apache Xerces XML Parser";
    }

    public String getOriginalEncoding(Document doc) {
        // this functionality has experimental status in Apache Xerces parser 1.4.x and 2.x
        // as it implements DOM level 3 functionality not completly and W3C' DOM3 API
        // is in working draft stage
        if (doc instanceof org.apache.xerces.dom.CoreDocumentImpl) {
            String result = ((org.apache.xerces.dom.CoreDocumentImpl)doc).getEncoding();
            if ((result != null) && !"".equals(result.trim())) {
                return result;
            }
        }
        // in other cases we just return default encoding
        return OpenCms.getDefaultEncoding();
    }
}
