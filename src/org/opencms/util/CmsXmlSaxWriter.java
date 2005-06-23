/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsXmlSaxWriter.java,v $
 * Date   : $Date: 2005/06/23 10:47:10 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import java.io.IOException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple SAX event handler that generates a XML file from the events caught.<p>
 * 
 * This is to be used for writing large XML files where keeping a DOM structure 
 * in memory might cause out-of-memory issues, like e.g. when writing the 
 * OpenCms export files.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsXmlSaxWriter extends DefaultHandler implements LexicalHandler {

    /** The indentation to use. */
    private static final String C_INDENT_STR = "\t";

    /** The file encoding to use. */
    private String m_encoding;

    /** The indentation level. */
    private int m_indentLevel;

    /** The last element name written to the output. */
    private String m_lastElementName;

    /** Indicates if a CDATA node is still open. */
    private boolean m_openCdata;

    /** Indicates if an element tag is still open. */
    private boolean m_openTag;

    /** The Writer to write the output to. */
    private Writer m_writer;

    /**
     * A SAX event handler that generates XML Strings from the events caught and writes them
     * to the given Writer.<p>
     * 
     * @param writer the Writer to write to output to 
     * @param encoding the encoding for the XML output file header
     */
    public CmsXmlSaxWriter(Writer writer, String encoding) {

        m_writer = writer;
        m_encoding = encoding;
        m_indentLevel = 0;
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] buf, int offset, int len) throws SAXException {

        if (len == 0) {
            return;
        }
        if (m_openTag) {
            write(">");
            m_openTag = false;
        }
        if (m_openCdata) {
            write("<![CDATA[");
            m_openCdata = false;
        }
        write(new String(buf, offset, len));
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) {

        // NOOP
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {

        if (!m_openCdata) {
            write("]]>");
        }
        m_openCdata = false;
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {

        try {
            if (m_openTag) {
                write("/>");
                m_openTag = false;
            }
            writeNewLine();
            m_writer.flush();
        } catch (IOException e) {
            throw new SAXException(Messages.get().key(Messages.ERR_IOERROR_0), e);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() {

        // NOOP
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {

        String elementName = resolveName(localName, qualifiedName);
        if (m_openTag) {
            write("/>");
        } else {
            if (!elementName.equals(m_lastElementName)) {
                writeNewLine();
            }
            write("</");
            write(elementName);
            write(">");
        }
        m_openTag = false;
        m_indentLevel--;
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    public void endEntity(String name) {

        // NOOP
    }

    /**
     * Returns the Writer where the XML is written to.<p>
     * 
     * @return the Writer where the XML is written to
     */
    public Writer getWriter() {

        return m_writer;
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA() {

        m_openCdata = true;
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {

        write("<?xml version=\"1.0\" encoding=\"");
        write(m_encoding);
        write("\"?>");
        writeNewLine();
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
     */
    public void startDTD(String name, String publicId, String systemId) {

        // NOOP
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attributes)
    throws SAXException {

        if (m_openTag) {
            write(">");
            m_openTag = false;
        }
        // increase indent and write linebreak
        m_indentLevel++;
        writeNewLine();
        // get element name and write entry
        m_lastElementName = resolveName(localName, qualifiedName);
        write("<");
        write(m_lastElementName);
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                write(" ");
                write(resolveName(attributes.getLocalName(i), attributes.getQName(i)));
                write("=\"");
                write(attributes.getValue(i));
                write("\"");
            }
        }
        m_openTag = true;
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    public void startEntity(String name) {

        // NOOP
    }

    /**
     * Resolves the local vs. the qualified name.<p>
     * 
     * If the local name is the empty String "", the qualified name is used.<p>
     * 
     * @param localName the local name
     * @param qualifiedName the qualified XML 1.0 name
     * @return the resolved name to use 
     */
    private String resolveName(String localName, String qualifiedName) {

        if ((localName == null) || (localName.length() == 0)) {
            return qualifiedName;
        } else {
            return localName;
        }
    }

    /**
     * Writes s String to the output stream.<p>
     * 
     * @param s the String to write
     * @throws SAXException in case of I/O errors
     */
    private void write(String s) throws SAXException {

        try {
            m_writer.write(s);
        } catch (IOException e) {
            throw new SAXException(Messages.get().key(Messages.ERR_IOERROR_0), e);
        }
    }

    /**
     * Writes a linebreak to the output stream, also handles the indentation.<p>
     *  
     * @throws SAXException in case of I/O errors
     */
    private void writeNewLine() throws SAXException {

        try {
            // write new line
            m_writer.write('\n');
            // write indentation
            for (int i = 1; i < m_indentLevel; i++) {
                m_writer.write(C_INDENT_STR);
            }
            // flush the stream
            m_writer.flush();
        } catch (IOException e) {
            throw new SAXException(Messages.get().key(Messages.ERR_IOERROR_0), e);
        }
    }
}