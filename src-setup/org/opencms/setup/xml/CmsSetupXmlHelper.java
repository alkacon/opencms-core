/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.xml;

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Helper class to modify xml files.<p>
 *
 * For more info about xpath see: <br>
 * <ul>
 * <li>http://www.w3.org/TR/xpath.html</li>
 * <li>http://www.zvon.org/xxl/XPathTutorial/General/examples.html</li>
 * </ul><p>
 *
 * @since 6.1.8
 */
public class CmsSetupXmlHelper {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSetupXmlHelper.class);

    /** Entity resolver to skip dtd validation. */
    private static final EntityResolver NO_ENTITY_RESOLVER = new EntityResolver() {

        /**
         * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
         */
        public InputSource resolveEntity(String publicId, String systemId) {

            return new InputSource(new StringReader(""));
        }
    };

    /** Optional base path. */
    private String m_basePath;

    /** Document cache. */
    private Map<String, Document> m_cache = new HashMap<String, Document>();

    /**
     * Default constructor.<p>
     *
     * Uses no base path.<p>
     */
    public CmsSetupXmlHelper() {

        // ignore
    }

    /**
     * Uses an optional base file path.<p>
     *
     * @param basePath the base file path to use;
     */
    public CmsSetupXmlHelper(String basePath) {

        m_basePath = basePath;
    }

    /**
     * Unmarshals (reads) an XML string into a new document.<p>
     *
     * @param xml the XML code to unmarshal
     *
     * @return the generated document
     *
     * @throws CmsXmlException if something goes wrong
     */
    public static String format(String xml) throws CmsXmlException {

        return CmsXmlUtils.marshal((Node)CmsXmlUtils.unmarshalHelper(xml, null), CmsEncoder.ENCODING_UTF_8);
    }

    /**
     * Returns the value in the given xpath of the given xml file.<p>
     *
     * @param document the xml document
     * @param xPath the xpath to read (should select a single node or attribute)
     *
     * @return the value in the given xpath of the given xml file, or <code>null</code> if no matching node
     */
    public static String getValue(Document document, String xPath) {

        Node node = document.selectSingleNode(xPath);
        if (node != null) {
            // return the value
            return node.getText();
        } else {
            return null;
        }
    }

    /**
     * Replaces a attibute's value in the given node addressed by the xPath.<p>
     *
     * @param document the document to replace the node attribute
     * @param xPath the xPath to the node
     * @param attribute the attribute to replace the value of
     * @param value the new value to set
     *
     * @return <code>true</code> if successful <code>false</code> otherwise
     */
    public static boolean setAttribute(Document document, String xPath, String attribute, String value) {

        Node node = document.selectSingleNode(xPath);
        Element e = (Element)node;
        @SuppressWarnings("unchecked")
        List<Attribute> attributes = e.attributes();
        for (Attribute a : attributes) {
            if (a.getName().equals(attribute)) {
                a.setValue(value);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the given value in all nodes identified by the given xpath of the given xml file.<p>
     *
     * If value is <code>null</code>, all nodes identified by the given xpath will be deleted.<p>
     *
     * If the node identified by the given xpath does not exists, the missing nodes will be created
     * (if <code>value</code> not <code>null</code>).<p>
     *
     * @param document the xml document
     * @param xPath the xpath to set
     * @param value the value to set (can be <code>null</code> for deletion)
     *
     * @return the number of successful changed or deleted nodes
     */
    public static int setValue(Document document, String xPath, String value) {

        return setValue(document, xPath, value, null);
    }

    /**
     * Sets the given value in all nodes identified by the given xpath of the given xml file.<p>
     *
     * If value is <code>null</code>, all nodes identified by the given xpath will be deleted.<p>
     *
     * If the node identified by the given xpath does not exists, the missing nodes will be created
     * (if <code>value</code> not <code>null</code>).<p>
     *
     * @param document the xml document
     * @param xPath the xpath to set
     * @param value the value to set (can be <code>null</code> for deletion)
     * @param nodeToInsert optional, if given it will be inserted after xPath with the given value
     *
     * @return the number of successful changed or deleted nodes
     */
    public static int setValue(Document document, String xPath, String value, String nodeToInsert) {

        int changes = 0;
        // be naive and try to find the node
        Iterator<Node> itNodes = CmsCollectionsGenericWrapper.<Node> list(document.selectNodes(xPath)).iterator();

        // if not found
        if (!itNodes.hasNext()) {
            if (value == null) {
                // if no node found for deletion
                return 0;
            }
            // find the node creating missing nodes in the way
            Iterator<String> it = CmsStringUtil.splitAsList(xPath, "/", false).iterator();
            Node currentNode = document;
            while (it.hasNext()) {
                String nodeName = it.next();
                // if a string condition contains '/'
                while ((nodeName.indexOf("='") > 0) && (nodeName.indexOf("']") < 0)) {
                    nodeName += "/" + it.next();
                }
                Node node = currentNode.selectSingleNode(nodeName);
                if (node != null) {
                    // node found
                    currentNode = node;
                    if (!it.hasNext()) {
                        currentNode.setText(value);
                    }
                } else if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element)currentNode;
                    if (!nodeName.startsWith("@")) {
                        elem = handleNode(elem, nodeName);
                        if (!it.hasNext() && CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                            elem.setText(value);
                        }
                    } else {
                        // if node is attribute create it with given value
                        elem.addAttribute(nodeName.substring(1), value);
                    }
                    currentNode = elem;
                } else {
                    // should never happen
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.ERR_XML_SET_VALUE_2, xPath, value));
                    }
                    break;
                }
            }
            if (nodeToInsert == null) {
                // if not inserting we are done
                return 1;
            }
            // if inserting, we just created the insertion point, so continue
            itNodes = CmsCollectionsGenericWrapper.<Node> list(document.selectNodes(xPath)).iterator();
        }

        // if found
        while (itNodes.hasNext()) {
            Node node = itNodes.next();
            if (nodeToInsert == null) {
                // if not inserting
                if (value != null) {
                    // if found, change the value
                    node.setText(value);
                } else {
                    // if node for deletion is found
                    node.getParent().remove(node);
                }
            } else {
                // first create the node to insert
                Element parent = node.getParent();
                Element elem = handleNode(parent, nodeToInsert);
                if (value != null) {
                    elem.setText(value);
                }
                // get the parent element list
                List<Node> list = CmsCollectionsGenericWrapper.<Node> list(parent.content());
                // remove the just created element
                list.remove(list.size() - 1);
                // insert it back to the right position
                int pos = list.indexOf(node);
                list.add(pos + 1, elem); // insert after
            }
            changes++;
        }
        return changes;
    }

    /**
     * Handles the xpath name, by creating the given node and its children.<p>
     *
     * @param parent the parent node to use
     * @param xpathName the xpathName, ie <code>a[@b='c'][d='e'][text()='f']</code>
     *
     * @return the new created element
     */
    private static Element handleNode(Element parent, String xpathName) {

        // if node is no attribute, create a new node
        String childrenPart = null;
        String nodeName;
        int pos = xpathName.indexOf("[");
        if (pos > 0) {
            childrenPart = xpathName.substring(pos + 1, xpathName.length() - 1);
            nodeName = xpathName.substring(0, pos);
        } else {
            nodeName = xpathName;
        }
        // create node
        Element elem = parent.addElement(nodeName);
        if (childrenPart != null) {
            pos = childrenPart.indexOf("[");
            if ((pos > 0) && (childrenPart.indexOf("]") > pos)) {
                handleNode(elem, childrenPart);
                return elem;
            }
            if (childrenPart.contains("=")) {
                Map<String, String> children = CmsStringUtil.splitAsMap(childrenPart, "][", "=");
                // handle child nodes
                for (Map.Entry<String, String> child : children.entrySet()) {
                    String childName = child.getKey();
                    String childValue = child.getValue();
                    if (childValue.startsWith("'")) {
                        childValue = childValue.substring(1);
                    }
                    if (childValue.endsWith("'")) {
                        childValue = childValue.substring(0, childValue.length() - 1);
                    }
                    if (childName.startsWith("@")) {
                        elem.addAttribute(childName.substring(1), childValue);
                    } else if (childName.equals("text()")) {
                        elem.setText(childValue);
                    } else if (!childName.contains("(")) {
                        Element childElem = elem.addElement(childName);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(childValue)) {
                            childElem.addText(childValue);
                        }
                    }
                }
            }
        }
        return elem;
    }

    /**
     * Discards the changes in the given file.<p>
     *
     * @param xmlFilename the xml config file (could be relative to the base path)
     */
    public void flush(String xmlFilename) {

        m_cache.remove(xmlFilename);
    }

    /**
     * Discards the changes in all files.<p>
     */
    public void flushAll() {

        m_cache.clear();
    }

    /**
     * Returns the base file Path.<p>
     *
     * @return the base file Path
     */
    public String getBasePath() {

        return m_basePath;
    }

    /**
     * Returns the document for the given filename.<p>
     * It can be new read or come from the document cache.<p>
     *
     * @param xmlFilename the filename to read
     *
     * @return the document for the given filename
     *
     * @throws CmsXmlException if something goes wrong while reading
     */
    public Document getDocument(String xmlFilename) throws CmsXmlException {

        // try to get it from the cache
        Document document = m_cache.get(xmlFilename);

        if (document == null) {
            try {
                document = CmsXmlUtils.unmarshalHelper(
                    new InputSource(new FileReader(getFile(xmlFilename))),
                    NO_ENTITY_RESOLVER);
            } catch (FileNotFoundException e) {
                LOG.error("Could not read file " + xmlFilename, e);
                throw new CmsXmlException(new CmsMessageContainer(null, e.toString()));

            } catch (Exception e) {
                LOG.error("Could not parse file " + xmlFilename, e);
                throw new CmsXmlException(
                    Messages.get().container(Messages.ERR_XML_COULD_NOT_PARSE_FILE_1, xmlFilename),
                    e);
            }
            // cache the doc
            m_cache.put(xmlFilename, document);
        }
        return document;
    }

    /**
     * Returns the value in the given xpath of the given xml file.<p>
     *
     * @param xmlFilename the xml config file (could be relative to the base path)
     * @param xPath the xpath to read (should select a single node or attribute)
     *
     * @return the value in the given xpath of the given xml file, or <code>null</code> if no matching node
     *
     * @throws CmsXmlException if something goes wrong while reading
     */
    public String getValue(String xmlFilename, String xPath) throws CmsXmlException {

        return getValue(getDocument(xmlFilename), xPath);
    }

    /**
     * Replaces a attibute's value in the given node addressed by the xPath.<p>
     *
     * @param xmlFilename the xml file name to get the document from
     * @param xPath the xPath to the node
     * @param attribute the attribute to replace the value of
     * @param value the new value to set
     *
     * @return <code>true</code> if successful <code>false</code> otherwise
     *
     * @throws CmsXmlException if the xml document coudn't be read
     */
    public boolean setAttribute(String xmlFilename, String xPath, String attribute, String value)
    throws CmsXmlException {

        return setAttribute(getDocument(xmlFilename), xPath, attribute, value);
    }

    /**
     * Sets the given value in all nodes identified by the given xpath of the given xml file.<p>
     *
     * If value is <code>null</code>, all nodes identified by the given xpath will be deleted.<p>
     *
     * If the node identified by the given xpath does not exists, the missing nodes will be created
     * (if <code>value</code> not <code>null</code>).<p>
     *
     * @param xmlFilename the xml config file (could be relative to the base path)
     * @param xPath the xpath to set
     * @param value the value to set (can be <code>null</code> for deletion)
     *
     * @return the number of successful changed or deleted nodes
     *
     * @throws CmsXmlException if something goes wrong
     */
    public int setValue(String xmlFilename, String xPath, String value) throws CmsXmlException {

        return setValue(getDocument(xmlFilename), xPath, value, null);
    }

    /**
     * Sets the given value in all nodes identified by the given xpath of the given xml file.<p>
     *
     * If value is <code>null</code>, all nodes identified by the given xpath will be deleted.<p>
     *
     * If the node identified by the given xpath does not exists, the missing nodes will be created
     * (if <code>value</code> not <code>null</code>).<p>
     *
     * @param xmlFilename the xml config file (could be relative to the base path)
     * @param xPath the xpath to set
     * @param value the value to set (can be <code>null</code> for deletion)
     * @param nodeToInsert optional, if given it will be inserted after xPath with the given value
     *
     * @return the number of successful changed or deleted nodes
     *
     * @throws CmsXmlException if something goes wrong
     */
    public int setValue(String xmlFilename, String xPath, String value, String nodeToInsert) throws CmsXmlException {

        return setValue(getDocument(xmlFilename), xPath, value, nodeToInsert);
    }

    /**
     * Writes the given file back to disk.<p>
     *
     * @param xmlFilename the xml config file (could be relative to the base path)
     *
     * @throws CmsXmlException if something wrong while writing
     */
    public void write(String xmlFilename) throws CmsXmlException {

        // try to get it from the cache
        Document document = m_cache.get(xmlFilename);

        if (document != null) {
            try {
                CmsXmlUtils.validateXmlStructure(document, CmsEncoder.ENCODING_UTF_8, new CmsXmlEntityResolver(null));
                OutputStream out = null;
                out = new FileOutputStream(getFile(xmlFilename));
                CmsXmlUtils.marshal(document, out, CmsEncoder.ENCODING_UTF_8);
            } catch (FileNotFoundException e) {
                throw new CmsXmlException(new CmsMessageContainer(null, e.toString()));
            } catch (CmsXmlException e) {
                // write invalid config files to the file system with a prefix of "invalid-" so they can be inspected for errors
                try {
                    OutputStream invalidOut = new FileOutputStream(getFile("invalid-" + xmlFilename));
                    CmsXmlUtils.marshal(document, invalidOut, CmsEncoder.ENCODING_UTF_8);
                } catch (IOException e2) {
                    // ignore

                }
                throw e;
            }
        }
    }

    /**
     * Flushes all cached documents.<p>
     *
     * @throws CmsXmlException if something wrong while writing
     */
    public void writeAll() throws CmsXmlException {

        Iterator<String> it = new ArrayList<String>(m_cache.keySet()).iterator();
        while (it.hasNext()) {
            String filename = it.next();
            write(filename);
        }
    }

    /**
     * Returns a file from a given filename.<p>
     *
     * @param xmlFilename the file name
     *
     * @return the file
     */
    private File getFile(String xmlFilename) {

        File file = new File(m_basePath + xmlFilename);
        if (!file.exists() || !file.canRead()) {
            file = new File(xmlFilename);
        }
        return file;
    }
}