
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/I_CmsXmlContent.java,v $
* Date   : $Date: 2001/01/24 09:42:40 $
* Version: $Revision: 1.7 $
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

import com.opencms.file.*;
import com.opencms.core.*;
import java.io.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * Common interface for OpenCms XML content classes.
 * Classes for each customized content type have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.7 $ $Date: 2001/01/24 09:42:40 $
 */
public interface I_CmsXmlContent extends I_CmsContent {
    
    /**
     * Creates a clone of this object.
     * @return cloned object
     * @exception CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException;
    
    /**
     * Gets the absolute filename of the XML file represented by this content class
     * @return Absolute filename
     */
    public String getAbsoluteFilename();
    
    /**
     * Gets a short filename (without path) of the XML file represented by this content class
     * of the template file.
     * @return filename
     */
    public String getFilename();
    
    /**
     * Prints the XML parsed content to a String
     * @return String with XML content
     */
    public String getXmlText();
    
    /**
     * Prints the XML parsed content of this template file
     * to the given Writer.
     * 
     * @param out Writer to print to.
     */
    public void getXmlText(Writer out);
    
    /**
     * Prints the XML parsed content of the given Node and
     * its subnodes to the given Writer.
     * 
     * @param out Writer to print to.
     * @param n Node that should be printed.
     */
    public void getXmlText(Writer out, Node n);
    
    /**
     * Prints the XML parsed content of a given node and 
     * its subnodes to a String
     * @param n Node that should be printed.
     * @return String with XML content
     */
    public String getXmlText(Node n);
    
    /**
     * Initialize the XML content class.
     * Load and parse the content of the given CmsFile object.
     * @param cms CmsObject Object for accessing resources.
     * @param file CmsFile object of the file to be loaded and parsed.
     * @exception CmsException
     */
    public void init(CmsObject cms, CmsFile file) throws CmsException;
    
    /**
     * Initialize the XML content class.
     * Load and parse the file given by filename,
     * @param cms CmsObject Object for accessing resources.
     * @param filename Filename of the file to be loaded.
     * @exception CmsException
     */
    public void init(CmsObject cms, String filename) throws CmsException;
    
    /**
     * Initialize the class with the given parsed XML DOM document.
     * @param cms CmsObject Object for accessing system resources.
     * @param document DOM document object containing the parsed XML file.
     * @param filename OpenCms filename of the XML file.
     * @exception CmsException
     */
    public void init(CmsObject cms, Document content, String filename) throws CmsException;
    
    /**
     * Parses the given file and stores it in the internal list of included files and
     * appends the relevant data structures of the new file to its own structures.
     * 
     * @param include Filename of the XML file to be included
     * @exception CmsException
     */
    public A_CmsXmlContent readIncludeFile(String filename) throws CmsException;
    
    /**
     * Writes the XML document back to the OpenCms system. 
     * @exception CmsException  
     */
    public void write() throws CmsException;
}
