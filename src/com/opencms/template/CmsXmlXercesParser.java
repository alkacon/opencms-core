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
 * @version $Revision: 1.3 $ $Date: 2000/02/11 19:00:00 $
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
        OutputFormat of = new OutputFormat(doc, OutputFormat.DEFAULT_ENCODING, false);
        
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
