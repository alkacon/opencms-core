package com.opencms.template;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/I_CmsXmlParser.java,v $
 * Date   : $Date: 2000/08/08 14:08:29 $
 * Version: $Revision: 1.6 $
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

import java.io.*;
import org.w3c.dom.*;

/**
 * Common interface for OpenCms XML parsers.
 * Classes and interfaces for each customized parser type
 * have to be implemtented.
 * 
 * @author Alexander Kandzior
 * @author Alexander Lucas
 * @version $Revision: 1.6 $ $Date: 2000/08/08 14:08:29 $
 */
public interface I_CmsXmlParser {

	/** Encoding used for XML documents */
	public final static String C_XML_ENCODING = "ISO-8859-1";
	
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
	 */
	public void getXmlText(Document doc, OutputStream out);
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
	 * @exception Exception
	 */
	public Document parse(Reader in) throws Exception;
	/**
	 * Gets a description of the parser.
	 * @return Parser description.
	 */
	public String toString();
}
