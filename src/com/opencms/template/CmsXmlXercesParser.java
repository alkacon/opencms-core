/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlXercesParser.java,v $
 * Date   : $Date: 2000/02/17 18:39:58 $
 * Version: $Revision: 1.5 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.template;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import org.apache.xerces.parsers.*;
import org.apache.xerces.dom.*;

import source.org.openxml.printer.*;

import com.opencms.core.*;

/**
 * Implementation of the OpenCms XML parser interface for
 * the Xerces parser.
 * 
 * @author Alexander Kandzior
 * @author Alexander Lucas
 * @version $Revision: 1.5 $ $Date: 2000/02/17 18:39:58 $
 */
public class CmsXmlXercesParser implements I_CmsXmlParser, I_CmsLogChannels {
    
    /**
     * Parses the given text with the Xerces parser.
     * @param in Reader with the input text.
     * @return Parsed text as DOM document.
     * @exception Exception
     */
    public Document parse(Reader in) throws Exception { 
        //return DOMFactory.createParser(in, null).parseDocument();
        DOMParser parser = new DOMParser();
        InputSource input = new InputSource(in);
        parser.parse(input);
        return parser.getDocument();
    }    

    /**
     * Creates an empty DOM XML document.
     * Workarround caus original method is corruped
     * 
     * @author Michaela Schleich
     * @param docNod first Node in empty  XML document
     * @return Empty document.
     */
    public Document createEmptyDocument(String docNod) throws Exception {
        String docXml = new String("<?xml version=\"1.0\" encoding=\"UTF8\"?>");
		docXml = docXml+"<"+docNod+">"+"</"+docNod+">";
		StringReader reader = new StringReader(docXml);

		return parse(reader);
		//return (Document)(new DocumentImpl(null));
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
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out Writer to print to.
     */
    public void getXmlText(Document doc, Writer out) {
        OutputFormat of = new OutputFormat(doc, OutputFormat.DEFAULT_ENCODING, true);
        
        Printer printer = Printer.makePrinter(out, of);
        try {
            System.out.println(doc);
			System.out.println(printer);
			System.out.println(out);
			printer.print(doc);   
			
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlXerxesParser] " + e);
            }
        }
    }

    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out OutputStream to print to.
     */
    public void getXmlText(Document doc, OutputStream out) {
        OutputFormat of = new OutputFormat(doc, OutputFormat.DEFAULT_ENCODING, true);
        
        try {
            Printer printer = Printer.makePrinter(out, of);
            printer.print(doc);      
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlXerxesParser] " + e);
            }
        }
    }
    
    /**
     * Gets a description of the parser.
     * @return Parser description.
     */
    public String toString() {
        return "Apache Xerces XML Parser";        
    }
}
