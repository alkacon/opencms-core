package com.opencms.template;

import java.io.*;
import org.w3c.dom.*;

/**
 * Common interface for OpenCms XML parsers.
 * Classes and interfaces for each customized parser type
 * have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/13 17:50:48 $
 */
public interface I_CmsXmlParser {
    public Document parse(Reader in) throws Exception;
    public Document createEmptyDocument();
    public Node importNode(Document doc, Node node);    
    public String toString();     
    public void getXmlText(Document doc, Writer out);
    public void getXmlText(Document doc, OutputStream out);

}
