/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlXercesParser.java,v $
* Date   : $Date: 2002/12/06 23:16:51 $
* Version: $Revision: 1.18 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;

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
 * @version $Revision: 1.18 $ $Date: 2002/12/06 23:16:51 $
 */
public class CmsXmlXercesParser implements I_CmsXmlParser,I_CmsLogChannels {
    
    /** Prevents the parser from printing multiple error messages.*/
    private static boolean c_xercesWarning = false;
    
    /**
     * Creates an empty DOM XML document.
     * Workarround caus original method is corruped
     * 
     * @author Michaela Schleich
     * @param docNod first Node in empty  XML document
     * @return Empty document.
     */
    public Document createEmptyDocument(String docNod) throws Exception {
        String docXml = new String("<?xml version=\"1.0\" encoding=\"" + C_XML_ENCODING + "\"?>");
        docXml = docXml + "<" + docNod + ">" + "</" + docNod + ">";
        StringReader reader = new StringReader(docXml);
        return parse(reader);
    
    //return (Document)(new DocumentImpl(null));
    }
    
    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out OutputStream to print to.
     */
    //[modified by Gridnine AB, 2002-06-17]
    public void getXmlText(Document doc, OutputStream out, String encoding) {
        /* incorrectly used default system encoding to convert String to bytes
        OutputStreamWriter osw = new OutputStreamWriter(out);
        getXmlText(doc, osw);
        */
        OutputFormat outf = new OutputFormat(doc,
            (encoding == null) ? getOriginalEncoding(doc) : encoding, true);
        outf.setLineWidth(C_XML_LINE_WIDTH);
        outf.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(out, outf);
        try {
            DOMSerializer domSerializer = serializer.asDOMSerializer();
            domSerializer.serialize(doc);
        }
        catch(Exception e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlXercesParser] " + e);
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
        OutputFormat outf = new OutputFormat(doc, C_XML_ENCODING, true);
        outf.setLineWidth(C_XML_LINE_WIDTH);
        outf.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(out, outf);
        try {
            DOMSerializer domSerializer = serializer.asDOMSerializer();
            domSerializer.serialize(doc);
        }
        catch(Exception e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlXercesParser] " + e);
            }
        }
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
     * @exception Exception
     */
    //[modified by Gridnine AB, 2002-06-13]
    public Document parse(Reader in) throws Exception {
        //[removed by Gridnine AB, 2002-06-13]
        /*
        //return DOMFactory.createParser(in, null).parseDocument();
        DOMParser parser = new DOMParser();
        try {
            parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
        }
        catch(SAXException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()  && !c_xercesWarning) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlXercesParser] Cannot set parser feature for apache xerces XML parser.");
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlXercesParser] This is NOT critical, but you should better use xerces 1.1.1 or higher.");
                c_xercesWarning = true;
            }
        }
        InputSource input = new InputSource(in);
        parser.parse(input);
        return parser.getDocument();
        */
        return parse(new InputSource(in));
    }
    
    //[added by Gridnine AB, 2002-06-13]
    public Document parse(InputStream in) throws Exception {
        return parse(new InputSource(in));
    }
    
    //[added by Gridnine AB, 2002-06-13], common internal method to actually parse input
    protected Document parse(InputSource input) throws Exception {
        DOMParser parser = new DOMParser();
        try {
            parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
        }
        catch(SAXException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()  && !c_xercesWarning) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlXercesParser] Cannot set parser feature for apache xerces XML parser.");
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlXercesParser] This is NOT critical, but you should better use xerces 1.1.1 or higher.");
                c_xercesWarning = true;
            }
        }
        parser.parse(input);
        return parser.getDocument();
    }
    
    //[added by Gridnine AB, 2002-06-17]
    public void serialize(Document doc, OutputStream in) throws Exception {
    }

    /**
     * Gets a description of the parser.
     * @return Parser description.
     */
    public String toString() {
        return "Apache Xerces XML Parser";
    }

    //[added by Gridnine AB, 2002-06-17]
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
        return C_XML_ENCODING;
    }
}
