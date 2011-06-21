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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

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
public final class CmsSetupXmlHelper {

    /**
     * Default constructor.<p>
     * 
     * Uses no base path.<p>
     */
    private CmsSetupXmlHelper() {

        // ignore        
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
    @SuppressWarnings("unchecked")
    public static int setValue(Document document, String xPath, String value, String nodeToInsert) {

        int changes = 0;
        // be naive and try to find the node
        Iterator<Node> itNodes = document.selectNodes(xPath).iterator();

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
                        if (!it.hasNext() && !CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                            elem.setText(value);
                        }
                    } else {
                        // if node is attribute create it with given value
                        elem.addAttribute(nodeName.substring(1), value);
                    }
                    currentNode = elem;
                } else {
                    // should never happen
                    break;
                }
            }
            if (nodeToInsert == null) {
                // if not inserting we are done
                return 1;
            }
            // if inserting, we just created the insertion point, so continue
            itNodes = document.selectNodes(xPath).iterator();
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
                List<Node> list = parent.content();
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
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(childValue)) {
                        childElem.addText(childValue);
                    }
                }
            }
        }
        return elem;
    }
}