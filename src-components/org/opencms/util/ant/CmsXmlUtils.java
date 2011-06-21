/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util.ant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Provides some basic XML handling utilities.<p>
 * 
 * @since 6.0.0 
 */
public final class CmsXmlUtils {

    /**
     * Prevents instances of this class from being generated.<p> 
     */
    private CmsXmlUtils() {

        // noop
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

            // split the path into sub elements
            List<String> elements = CmsStringUtil.splitAsList(path, '/');
            int end = elements.size() - 1;
            for (int i = 0; i <= end; i++) {
                // append [i] to path element if required 
                result.append(createXpathElementCheck(elements.get(i), (i == end) ? index : 1));
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
     * Returns the last Xpath index from the given path.<p>
     * 
     * Examples:<br> 
     * <code>title</code> returns the empty String<p>
     * <code>title[1]</code> returns <code>[1]</code><p>
     * <code>title/subtitle</code> returns them empty String<p>
     * <code>title[1]/subtitle[1]</code> returns <code>[1]</code><p>
     * 
     * @param path the path to extract the Xpath index from
     * 
     * @return  the last Xpath index from the given path
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
     * Returns the last Xpath index from the given path as integer.<p>
     * 
     * Examples:<br> 
     * <code>title</code> returns 1<p>
     * <code>title[1]</code> returns 1<p>
     * <code>title/subtitle</code> returns 1<p>
     * <code>title[1]/subtitle[2]</code> returns 2<p>
     * 
     * @param path the path to extract the Xpath index from
     * 
     * @return the last Xpath index from the given path as integer
     */
    public static int getXpathIndexInt(String path) {

        int pos1 = path.lastIndexOf('/');
        int pos2 = path.lastIndexOf('[');
        if ((pos2 < 0) || (pos1 > pos2)) {
            return 1;
        }

        String idxStr = path.substring(pos2 + 1, path.lastIndexOf(']'));
        try {
            return Integer.parseInt(idxStr);
        } catch (NumberFormatException e) {
            // NOOP
        }
        return 1;
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
     * @throws Exception if something goes wrong
     */
    public static OutputStream marshal(Document document, OutputStream out, String encoding) throws Exception {

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(encoding);

        XMLWriter writer = new XMLWriter(out, format);
        writer.setEscapeText(false);

        writer.write(document);
        writer.close();

        return out;
    }

    /**
     * Marshals (writes) an XML document to a String using XML pretty-print formatting.<p>
     * 
     * @param document the XML document to marshal
     * @param encoding the encoding to use
     * @return the marshalled XML document
     * @throws Exception if something goes wrong
     */
    public static String marshal(Document document, String encoding) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshal(document, out, encoding);
        return out.toString(encoding);
    }

    /**
     * Marshals (writes) an XML node into an output stream using XML pretty-print formatting.<p>
     * 
     * @param node the XML node to marshal
     * @param encoding the encoding to use
     * 
     * @return the string with the xml content
     * 
     * @throws Exception if something goes wrong
     */
    public static String marshal(Node node, String encoding) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(encoding);
        format.setSuppressDeclaration(true);

        XMLWriter writer = new XMLWriter(out, format);
        writer.setEscapeText(false);

        writer.write(node);
        writer.close();
        return new String(out.toByteArray());
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
     * Removes the last complex Xpath element from the path.<p>
     * 
     * The same as {@link #removeLastXpathElement(String)} both it works with more complex xpaths.
     * 
     * <p>Example:<br> 
     * <code>system/backup[@date='23/10/2003']/resource[path='/a/b/c']</code> becomes <code>system/backup[@date='23/10/2003']</code><p>
     * 
     * @param path the Xpath to remove the last element from
     * 
     * @return the path with the last element removed
     */
    public static String removeLastComplexXpathElement(String path) {

        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            return path;
        }
        // count ' chars
        int p = pos;
        int count = -1;
        while (p > 0) {
            count++;
            p = path.indexOf("\'", p + 1);
        }
        String parentPath = path.substring(0, pos);
        if (count % 2 == 0) {
            // if substring is complete 
            return parentPath;
        }
        // if not complete
        p = parentPath.lastIndexOf("'");
        if (p >= 0) {
            // complete it if possible
            return removeLastComplexXpathElement(parentPath.substring(0, p));
        }
        return parentPath;
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
     * Removes all Xpath index information from the given input path.<p>
     * 
     * Examples:<br> 
     * <code>title</code> is left untouched<br>
     * <code>title[1]</code> becomes <code>title</code><br>
     * <code>title/subtitle</code> is left untouched<br>
     * <code>title[1]/subtitle[1]</code> becomes <code>title/subtitle</code><p>
     * 
     * @param path the path to remove the Xpath index information from
     * 
     * @return the simplified Xpath for the given name
     */
    public static String removeXpath(String path) {

        if (path.indexOf('/') > -1) {
            // this is a complex path over more then 1 node
            StringBuffer result = new StringBuffer(path.length() + 32);

            // split the path into sub-elements
            List<String> elements = CmsStringUtil.splitAsList(path, '/');
            int end = elements.size() - 1;
            for (int i = 0; i <= end; i++) {
                // remove [i] from path element if required 
                result.append(removeXpathIndex(elements.get(i)));
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
        int pos = result.length() - 1;
        if (result.charAt(pos) == '/') {
            result.deleteCharAt(pos);
        }
        return result.toString();
    }

    /**
     * Helper to unmarshal (read) xml contents from a byte array into a document.<p>
     * 
     * Using this method ensures that the OpenCms XML entity resolver is used.<p>
     * 
     * @param xmlData the XML data in a byte array
     * @param resolver the XML entity resolver to use
     * 
     * @return the base object initialized with the unmarshalled XML document
     * 
     * @throws Exception if something goes wrong
     * 
     * @see CmsXmlUtils#unmarshalHelper(InputSource, EntityResolver)
     */
    public static Document unmarshalHelper(byte[] xmlData, EntityResolver resolver) throws Exception {

        return CmsXmlUtils.unmarshalHelper(new InputSource(new ByteArrayInputStream(xmlData)), resolver);
    }

    /**
     * Helper to unmarshal (read) xml contents from a byte array into a document.<p>
     * 
     * Using this method ensures that the OpenCms XML entity resolver is used.<p>
     * 
     * @param xmlData the XML data in a byte array
     * @param resolver the XML entity resolver to use
     * @param validate if the reader should try to validate the xml code
     * 
     * @return the base object initialized with the unmarshalled XML document
     * 
     * @throws Exception if something goes wrong
     * 
     * @see CmsXmlUtils#unmarshalHelper(InputSource, EntityResolver)
     */
    public static Document unmarshalHelper(byte[] xmlData, EntityResolver resolver, boolean validate) throws Exception {

        return CmsXmlUtils.unmarshalHelper(new InputSource(new ByteArrayInputStream(xmlData)), resolver, validate);
    }

    /**
     * Helper to unmarshal (read) xml contents from an input source into a document.<p>
     * 
     * Using this method ensures that the OpenCms XML entity resolver is used.<p>
     * 
     * Important: The encoding provided will NOT be used during unmarshalling,
     * the XML parser will do this on the base of the information in the source String.
     * The encoding is used for initializing the created instance of the document,
     * which means it will be used when marshalling the document again later.<p>
     *  
     * @param source the XML input source to use
     * @param resolver the XML entity resolver to use
     * 
     * @return the unmarshalled XML document
     * 
     * @throws Exception if something goes wrong
     */
    public static Document unmarshalHelper(InputSource source, EntityResolver resolver) throws Exception {

        return unmarshalHelper(source, resolver, false);
    }

    /**
     * Helper to unmarshal (read) xml contents from an input source into a document.<p>
     * 
     * Using this method ensures that the OpenCms XML entity resolver is used.<p>
     * 
     * Important: The encoding provided will NOT be used during unmarshalling,
     * the XML parser will do this on the base of the information in the source String.
     * The encoding is used for initializing the created instance of the document,
     * which means it will be used when marshalling the document again later.<p>
     *  
     * @param source the XML input source to use
     * @param resolver the XML entity resolver to use
     * @param validate if the reader should try to validate the xml code
     * 
     * @return the unmarshalled XML document
     * 
     * @throws Exception if something goes wrong
     */
    public static Document unmarshalHelper(InputSource source, EntityResolver resolver, boolean validate)
    throws Exception {

        SAXReader reader = new SAXReader();
        if (resolver != null) {
            reader.setEntityResolver(resolver);
        }
        reader.setMergeAdjacentText(true);
        reader.setStripWhitespaceText(true);
        if (!validate) {
            reader.setValidation(false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        }
        return reader.read(source);
    }

    /**
     * Helper to unmarshal (read) xml contents from a String into a document.<p>
     * 
     * Using this method ensures that the OpenCms XML entitiy resolver is used.<p>
     * 
     * @param xmlData the xml data in a String 
     * @param resolver the XML entity resolver to use
     * @return the base object initialized with the unmarshalled XML document
     * @throws Exception if something goes wrong
     * @see CmsXmlUtils#unmarshalHelper(InputSource, EntityResolver)
     */
    public static Document unmarshalHelper(String xmlData, EntityResolver resolver) throws Exception {

        return CmsXmlUtils.unmarshalHelper(new InputSource(new StringReader(xmlData)), resolver);
    }
}