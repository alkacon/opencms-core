package com.opencms.template;

import java.io.*;
import org.w3c.dom.*;

/**
 * Common interface for OpenCms XML parsers.
 * Classes and interfaces for each customized parser type
 * have to be implemtented.
 * 
 * @author Alexander Kandzior
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/02/11 19:00:00 $
 */
public interface I_CmsXmlParser {
    
    /**
     * Parses the given text.
     * @param in Reader with the input text.
     * @return Parsed text as DOM document.
     * @exception Exception
     */
    public Document parse(Reader in) throws Exception;
    
    /**
     * Creates an empty DOM XML document.
     * @return Empty document.
     */
    public Document createEmptyDocument(String docNod) throws Exception;
    
    /**
     * Used to import a node from a foreign document.
     * @param doc Destination document that should import the node.
     * @param node Node to be imported.
     * @return New node that belongs to the document <code>doc</code>
     */
    public Node importNode(Document doc, Node node);    
    
    /**
     * Gets a description of the parser.
     * @return Parser description.
     */
    public String toString();     
    
    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out Writer to print to.
     */
    public void getXmlText(Document doc, Writer out);

    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out OutputStream to print to.
     */
    public void getXmlText(Document doc, OutputStream out);
}
