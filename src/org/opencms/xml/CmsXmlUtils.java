/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlUtils.java,v $
 * Date   : $Date: 2005/06/22 10:38:16 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml;

import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Provides some basic XML handling utilities.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.16 $
 * @since 5.3.5
 */
public final class CmsXmlUtils {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlUtils.class);  
    
    /**
     * Prevents instances of this class from being generated.<p> 
     */
    private CmsXmlUtils() {

        // noop
    }

    /**
     * Concats two Xpath expressions, ensuring that exactly one slash "/" is between them.<p>  
     * 
     * Use this method if it's uncertain if the given arguments are starting or ending with 
     * a slash "/".<p>
     * 
     * Examples:<br> 
     * <code>"title", "subtitle"</code> becomes <code>title/subtitle</code><br>
     * <code>"title[1]/", "subtitle"</code> becomes <code>title[1]/subtitle</code><br>
     * <code>"title[1]/", "/subtitle[1]"</code> becomes <code>title[1]/subtitle[1]</code><p>
     * 
     * @param prefix the prefix Xpath
     * @param suffix the suffix Xpath
     * 
     * @return the concated Xpath build from prefix and suffix
     */
    public static String concatXpath(String prefix, String suffix) {

        if (suffix == null) {
            // ensure suffix is not null
            suffix = "";
        } else {
            if ((suffix.length() > 0) && (suffix.charAt(0) == '/')) {
                // remove leading '/' form suffix
                suffix = suffix.substring(1);
            }
        }
        if (prefix != null) {
            StringBuffer result = new StringBuffer(32);
            result.append(prefix);
            if (!CmsResource.isFolder(prefix)) {
                result.append('/');
            }
            result.append(suffix);
            return result.toString();
        }
        return suffix;
    }

    /**
     * Translates a simple lookup path to the simplified Xpath format used for 
     * the internal bookmarks.<p>
     * 
     * Examples:<br> 
     * <code>title</code> becomes <code>title[1]</code><br>
     * <code>title[1]</code> is left untouched<br>
     * <code>title/subtitle</code> becomes <code>title[1]/subtitle[1]</code><br>
     * <code>title/subtitle[1]</code> becomes <code>title[1]/subtitle[1]</code><p>
     * 
     * Note: If the name already has the format <code>title[1]</code> then provided index parameter 
     * is ignored.<p> 
     * 
     * @param path the path to get the simplified Xpath for
     * @param index the index to append (if required)
     * 
     * @return the simplified Xpath for the given name
     */
    public static String createXpath(String path, int index) {
        
        if (path.indexOf('/') > -1) {
            // this is a complex path over more then 1 node
            StringBuffer result = new StringBuffer(path.length() + 32);
            
            // split the path into subelements
            List elements = CmsStringUtil.splitAsList(path, '/');
            int end = elements.size() - 1;
            for (int i = 0; i <= end; i++) {
                // append [i] to path element if required 
                result.append(createXpathElementCheck((String)elements.get(i), (i == end) ? index : 1));
                if (i < end) {
                    // append path delimiter if not final path element
                    result.append('/');
                }
            }
            return result.toString();
        }

        // this path has only 1 node, append [index] if required
        return createXpathElementCheck(path, index);
    }
    
    /**
     * Simplifies an Xpath by removing a leading and a trailing slash from the given path.<p> 
     * 
     * Examples:<br> 
     * <code>title/</code> becomes <code>title</code><br>
     * <code>/title[1]/</code> becomes <code>title[1]</code><br>
     * <code>/title/subtitle/</code> becomes <code>title/subtitle</code><br>
     * <code>/title/subtitle[1]/</code> becomes <code>title/subtitle[1]</code><p>
     * 
     * @param path the path to process
     * @return the input with a leading and a trailing slash removed
     */
    public static String simplifyXpath(String path) {
        
        StringBuffer result = new StringBuffer(path);
        if (result.charAt(0) == '/') {
            result.deleteCharAt(0);
        }
        int pos = result.length()-1;
        if (result.charAt(pos) == '/') {
            result.deleteCharAt(pos);
        }
        return result.toString();
    }
    
    /**
     * Removes all Xpath index information from the given input path.<p>
     * 
     * Examples:<br> 
     * <code>title</code> is left untouched<br>
     * <code>title[1]</code> becomes <code>title</code><br>
     * <code>title/subtitle</code> is left untouched<br>
     * <code>title/subtitle[1]</code> becomes <code>title/subtitle</code><p>
     * 
     * @param path the path to remove the Xpath index information from
     * 
     * @return the simplified Xpath for the given name
     */
    public static String removeXpath(String path) {
        
        if (path.indexOf('/') > -1) {
            // this is a complex path over more then 1 node
            StringBuffer result = new StringBuffer(path.length() + 32);

            // split the path into subelements
            List elements = CmsStringUtil.splitAsList(path, '/');
            int end = elements.size() - 1;
            for (int i = 0; i <= end; i++) {
                // append [i] to path element if required 
                result.append(removeXpathIndex((String)elements.get(i)));
                if (i < end) {
                    // append path delimiter if not final path element
                    result.append('/');
                }
            }
            return result.toString();
        }

        // this path has only 1 node, remove last index if required
        return removeXpathIndex(path);
    }

    /**
     * Appends the provided index parameter in square brackets to the given name,
     * like <code>path[index]</code>.<p>
     * 
     * This method is used if it's clear that some path does not have 
     * a square bracket already appended.<p>
     * 
     * @param path the path append the index to
     * @param index the index to append
     * 
     * @return the simplified Xpath for the given name
     */
    public static String createXpathElement(String path, int index) {

        StringBuffer result = new StringBuffer(path.length() + 5);
        result.append(path);
        result.append('[');
        result.append(index);
        result.append(']');
        return result.toString();
    }

    /**
     * Ensures that a provided simplified Xpath has the format <code>title[1]</code>.<p>
     * 
     * This method is used if it's uncertain if some path does have 
     * a square bracket already appended or not.<p>
     * 
     * Note: If the name already has the format <code>title[1]</code>, then provided index parameter 
     * is ignored.<p> 
     * 
     * @param path the path to get the simplified Xpath for
     * @param index the index to append (if required)
     * 
     * @return the simplified Xpath for the given name
     */
    public static String createXpathElementCheck(String path, int index) {

        if (path.charAt(path.length() - 1) == ']') {
            // path is already in the form "title[1]"
            // ignore provided index and return the path "as is"
            return path;
        }

        // append index in square brackets
        return createXpathElement(path, index);
    }

    /**
     * Returns the first Xpath element from the provided path, 
     * without the index value.<p>
     * 
     * Examples:<br> 
     * <code>title</code> is left untouched<br>
     * <code>title[1]</code> becomes <code>title</code><br>
     * <code>title/subtitle</code> becomes <code>title</code><br>
     * <code>title[1]/subtitle[1]</code> becomes <code>title</code><p>
     * 
     * @param path the path to get the first Xpath element from
     * 
     * @return the first Xpath element from the provided path
     */
    public static String getFirstXpathElement(String path) {

        int pos = path.indexOf('/');
        if (pos >= 0) {
            path = path.substring(0, pos);
        }

        return CmsXmlUtils.removeXpathIndex(path);
    }

    /**
     * Returns the last Xpath element from the provided path, 
     * without the index value.<p>
     * 
     * Examples:<br> 
     * <code>title</code> is left untouched<br>
     * <code>title[1]</code> becomes <code>title</code><br>
     * <code>title/subtitle</code> becomes <code>subtitle</code><br>
     * <code>title[1]/subtitle[1]</code> becomes <code>subtitle</code><p>
     * 
     * @param path the path to get the last Xpath element from
     * 
     * @return the last Xpath element from the provided path
     */
    public static String getLastXpathElement(String path) {

        int pos = path.lastIndexOf('/');
        if (pos >= 0) {
            path = path.substring(pos + 1);
        }

        return CmsXmlUtils.removeXpathIndex(path);
    }

    /**
     * Returns <code>true</code> if the given path is a Xpath with 
     * at last 2 elements.<p>
     * 
     * Examples:<br> 
     * <code>title</code> returns <code>false</code><br>
     * <code>title[1]</code> returns <code>false</code><br>
     * <code>title/subtitle</code> returns <code>true</code><br>
     * <code>title[1]/subtitle[1]</code> returns <code>true</code><p>
     * 
     * @param path the path to check
     * @return true if the given path is a Xpath with at last 2 elements
     */
    public static boolean isDeepXpath(String path) {

        return path.indexOf('/') > 0;
    }

    /**
     * Marshals (writes) an XML document into an output stream using XML pretty-print formatting.<p>
     * 
     * @param document the XML document to marshal
     * @param out the output stream to write to
     * @param encoding the encoding to use
     * @return the output stream with the xml content
     * @throws CmsXmlException if something goes wrong
     */
    public static OutputStream marshal(Document document, OutputStream out, String encoding) throws CmsXmlException {

        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding(encoding);

            XMLWriter writer = new XMLWriter(out, format);
            writer.setEscapeText(false);

            writer.write(document);
            writer.close();

        } catch (Exception e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_MARSHALLING_XML_DOC_0), e);
        }

        return out;
    }

    /**
     * Marshals (writes) an XML document to a String using XML pretty-print formatting.<p>
     * 
     * @param document the XML document to marshal
     * @param encoding the encoding to use
     * @return the marshalled XML document
     * @throws CmsXmlException if something goes wrong
     */
    public static String marshal(Document document, String encoding) throws CmsXmlException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshal(document, out, encoding);
        try {
            return out.toString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_MARSHALLING_XML_DOC_TO_STRING_0), e);
        }
    }

    /**
     * Removes the first Xpath element from the path.<p>
     * 
     * If the provided path does not contain a "/" character, 
     * it is returned unchanged.<p>
     * 
     * <p>Examples:<br> 
     * <code>title</code> is left untouched<br>
     * <code>title[1]</code> is left untouched<br>
     * <code>title/subtitle</code> becomes <code>subtitle</code><br>
     * <code>title[1]/subtitle[1]</code> becomes <code>subtitle[1]</code><p>
     * 
     * @param path the Xpath to remove the first element from
     * 
     * @return the path with the first element removed
     */
    public static String removeFirstXpathElement(String path) {

        int pos = path.indexOf('/');
        if (pos < 0) {
            return path;
        }

        return path.substring(pos + 1);
    }

    /**
     * Removes the last Xpath element from the path.<p>
     * 
     * If the provided path does not contain a "/" character, 
     * it is returned unchanged.<p>
     * 
     * <p>Examples:<br> 
     * <code>title</code> is left untouched<br>
     * <code>title[1]</code> is left untouched<br>
     * <code>title/subtitle</code> becomes <code>title</code><br>
     * <code>title[1]/subtitle[1]</code> becomes <code>title[1]</code><p>
     * 
     * @param path the Xpath to remove the last element from
     * 
     * @return the path with the last element removed
     */
    public static String removeLastXpathElement(String path) {

        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            return path;
        }

        return path.substring(0, pos);
    }
    
    /**
     * Removes the last Xpath index from the given path.<p>
     * 
     * Examples:<br> 
     * <code>title</code> is left untouched<br>
     * <code>title[1]</code> becomes <code>title</code><br>
     * <code>title/subtitle</code> is left untouched<br>
     * <code>title[1]/subtitle[1]</code> becomes <code>title[1]/subtitle</code><p>
     * 
     * @param path the path to remove the Xpath index from
     * 
     * @return the path with the last Xpath index removed
     */
    public static String removeXpathIndex(String path) {

        int pos1 = path.lastIndexOf('/');
        int pos2 = path.lastIndexOf('[');
        if ((pos2 < 0) || (pos1 > pos2)) {
            return path;
        }

        return path.substring(0, pos2);
    }
    
    /**
     * Returns the last Xpath index from the given path.<p>
     * 
     * Examples:<br> 
     * <code>title</code> returns them empty String<p>
     * <code>title[1]</code> returns <code>[1]</code><p>
     * <code>title/subtitle</code> returns them empty String<p>
     * <code>title[1]/subtitle[1]</code> returns <code>[1]</code><p>
     * 
     * @param path the path to remove the Xpath index from
     * 
     * @return the path with the last Xpath index removed
     */
    public static String getXpathIndex(String path) {
        
        int pos1 = path.lastIndexOf('/');
        int pos2 = path.lastIndexOf('[');
        if ((pos2 < 0) || (pos1 > pos2)) {
            return "";
        }

        return path.substring(pos2);
    }

    /**
     * Helper to unmarshal (read) xml contents from a byte array into a document.<p>
     * 
     * Using this method ensures that the OpenCms XML entitiy resolver is used.<p>
     * 
     * @param xmlData the XML data in a byte array
     * @param resolver the XML entitiy resolver to use
     * @return the base object initialized with the unmarshalled XML document
     * @throws CmsXmlException if something goes wrong
     * @see CmsXmlUtils#unmarshalHelper(InputSource, EntityResolver)
     */
    public static Document unmarshalHelper(byte[] xmlData, EntityResolver resolver) throws CmsXmlException {

        return CmsXmlUtils.unmarshalHelper(new InputSource(new ByteArrayInputStream(xmlData)), resolver);
    }

    /**
     * Helper to unmarshal (read) xml contents from an input source into a document.<p>
     * 
     * Using this method ensures that the OpenCms XML entitiy resolver is used.<p>
     * 
     * Important: The encoding provided will NOT be used uring unmarshalling,
     * the XML parser will do this on the base of the information in the source String.
     * The encoding is used for initializing the created instance of the document,
     * which means it will be used when marshalling the document again later.<p>
     *  
     * @param source the XML input source to use
     * @param resolver the XML entitiy resolver to use
     * @return the unmarshalled XML document
     * @throws CmsXmlException if something goes wrong
     */
    public static Document unmarshalHelper(InputSource source, EntityResolver resolver) throws CmsXmlException {

        try {
            SAXReader reader = new SAXReader();
            if (resolver != null) {
                reader.setEntityResolver(resolver);
            }
            reader.setMergeAdjacentText(true);
            return reader.read(source);
        } catch (DocumentException e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_UNMARSHALLING_XML_DOC_0), e);
        }
    }

    /**
     * Helper to unmarshal (read) xml contents from a String into a document.<p>
     * 
     * Using this method ensures that the OpenCms XML entitiy resolver is used.<p>
     * 
     * @param xmlData the xml data in a String 
     * @param resolver the XML entitiy resolver to use
     * @return the base object initialized with the unmarshalled XML document
     * @throws CmsXmlException if something goes wrong
     * @see CmsXmlUtils#unmarshalHelper(InputSource, EntityResolver)
     */
    public static Document unmarshalHelper(String xmlData, EntityResolver resolver) throws CmsXmlException {

        return CmsXmlUtils.unmarshalHelper(new InputSource(new StringReader(xmlData)), resolver);
    }

    /**
     * Validates the structure of a XML document contained in a byte array 
     * with the DTD or XML schema used by the document.<p>
     * 
     * @param xmlData a byte array containing a XML document that should be validated
     * @param encoding the encoding to use when marshalling the XML document (required)
     * @param resolver the XML entitiy resolver to use
     * 
     * @throws CmsXmlException if the validation fails
     */
    public static void validateXmlStructure(byte[] xmlData, String encoding, EntityResolver resolver)
    throws CmsXmlException {

        XMLReader reader;
        try {
            reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        } catch (SAXException e) {
            // xerces parser not available - no schema validation possible
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_VALIDATION_INIT_XERXES_SAX_READER_FAILED_0), e);
            }
            // no validation of the content is possible
            return;
        }
        // turn on validation
        try {
            reader.setFeature("http://xml.org/sax/features/validation", true);
            // turn on schema validation
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            // configure namespace support
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        } catch (SAXNotRecognizedException e) {
            // should not happen as Xerces 2 support this feature
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_SAX_READER_FEATURE_NOT_RECOGNIZED_0), e);
            }
            // no validation of the content is possible
            return;
        } catch (SAXNotSupportedException e) {
            // should not happen as Xerces 2 support this feature
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_SAX_READER_FEATURE_NOT_SUPPORTED_0), e);
            }
            // no validation of the content is possible
            return;
        }

        // add an error handler which turns any errors into XML
        CmsXmlValidationErrorHandler errorHandler = new CmsXmlValidationErrorHandler();
        reader.setErrorHandler(errorHandler);

        if (resolver != null) {
            // set the resolver for the "opencms://" URIs
            reader.setEntityResolver(resolver);
        }

        try {
            reader.parse(new InputSource(new ByteArrayInputStream(xmlData)));
        } catch (IOException e) {
            // should not happen since we read form a byte array
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_READ_XML_FROM_BYTE_ARR_FAILED_0), e);
            }
            return;
        } catch (SAXException e) {
            // should not happen since all errors are handled in the XML error handler
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_PARSE_SAX_EXC_0), e);
            }
            return;
        }

        if (errorHandler.getErrors().elements().size() > 0) {
            // there was at last one validation error, so throw an exception
            StringWriter out = new StringWriter(256);
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(out, format);
            try {
                writer.write(errorHandler.getErrors());
                writer.write(errorHandler.getWarnings());
                writer.close();
            } catch (IOException e) {
                // should not happen since we write to a StringWriter
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.LOG_STRINGWRITER_IO_EXC_0), e);
                }
            }
            // generate String from XML for display of document in error message
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XML_VALIDATION_1,
                out.toString()));
        }
    }

    /**
     * Validates the structure of a XML document with the DTD or XML schema used 
     * by the document.<p>
     * 
     * @param document a XML document that should be validated
     * @param encoding the encoding to use when marshalling the XML document (required)
     * @param resolver the XML entitiy resolver to use
     * 
     * @throws CmsXmlException if the validation fails
     */
    public static void validateXmlStructure(Document document, String encoding, EntityResolver resolver)
    throws CmsXmlException {

        // generate bytes from document
        byte[] xmlData = ((ByteArrayOutputStream)marshal(document, new ByteArrayOutputStream(512), encoding))
            .toByteArray();
        validateXmlStructure(xmlData, encoding, resolver);
    }
}