/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/I_CmsXmlParser.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
* Version: $Revision: 1.16 $
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

import java.io.*;
import org.w3c.dom.*;

/**
 * Common interface for OpenCms XML parsers.
 * Classes and interfaces for each customized parser type
 * have to be implemtented.
 * 
 * @author Alexander Kandzior
 * @author Alexander Lucas
 * @version $Revision: 1.16 $ $Date: 2005/02/18 15:18:52 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public interface I_CmsXmlParser {
    
    /** Line width used for XML documents */
    public final static int C_XML_LINE_WIDTH = 80;
    
    /**
     * Creates an empty DOM XML document.
     * @return Empty document.
     */
    public Document createEmptyDocument(String docNod) throws Exception;
    
    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out OutputStream to print to.
     * @param encoding the character encoding to be used while serializing
     * document, if null - original or default encoding will be used
     */
    public void getXmlText(Document doc, OutputStream out, String encoding);

    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out Writer to print to.
     * @param encoding the character encoding to be used while serializing
     */
    public void getXmlText(Document doc, Writer out, String encoding);
        
    /**
     * Calls a XML printer for converting a XML DOM document
     * to a String.
     * @param doc Document to be printed.
     * @param out Writer to print to.
     */
    public void getXmlText(Document doc, Writer out);
    
    /**
     * Used to import a node from a foreign document.
     * @param doc Destination document that should import the node.
     * @param node Node to be imported.
     * @return New node that belongs to the document <code>doc</code>
     */
    public Node importNode(Document doc, Node node);
    
    /**
     * Parses the given text.
     * @param in Reader with the input text.
     * @return Parsed text as DOM document.
     * @throws Exception
     */
    public Document parse(Reader in) throws Exception;

    public Document parse(InputStream in) throws Exception;

    public String getOriginalEncoding(Document doc);

    /**
     * Gets a description of the parser.
     * @return Parser description.
     */
    public String toString();
}
