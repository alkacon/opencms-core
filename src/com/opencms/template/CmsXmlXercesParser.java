package com.opencms.template;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import org.apache.xerces.parsers.*;
import org.apache.xerces.dom.*;

import source.org.openxml.printer.*;

import com.opencms.core.*;

public class CmsXmlXercesParser implements I_CmsXmlParser, I_CmsLogChannels {
    
    public Document parse(Reader in) throws Exception { 
        //return DOMFactory.createParser(in, null).parseDocument();
        DOMParser parser = new DOMParser();
        InputSource input = new InputSource(in);
        parser.parse(input);
        return parser.getDocument();
    }    

    public Document createEmptyDocument() {
        return (Document)(new DocumentImpl(null));
    }    
    
    public Node importNode(Document doc, Node node) { 
        return ((org.apache.xerces.dom.DocumentImpl)doc).importNode(node, true);        
    }

    public void getXmlText(Document doc, Writer out) {
        OutputFormat of = new OutputFormat(doc, OutputFormat.DEFAULT_ENCODING, false);
        
        Printer printer = Printer.makePrinter(out, of);
        try {
            printer.print(doc);      
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlXerxesParser] " + e);
            }
        }
    }

    public void getXmlText(Document doc, OutputStream out) {
        OutputFormat of = new OutputFormat(doc, OutputFormat.DEFAULT_ENCODING, false);
        
        try {
            Printer printer = Printer.makePrinter(out, of);
            printer.print(doc);      
        } catch(Exception e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlXerxesParser] " + e);
            }
        }
    }

    
    public String toString() {
        return "Apache Xerces XML Parser";        
    }
}
