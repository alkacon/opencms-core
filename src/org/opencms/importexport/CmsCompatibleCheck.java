/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/Attic/CmsCompatibleCheck.java,v $
 * Date   : $Date: 2004/02/13 13:45:33 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.importexport;

import org.opencms.file.CmsResourceTypeFolder;
import org.opencms.file.CmsResourceTypePlain;
import org.opencms.main.I_CmsConstants;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.template.A_CmsXmlContent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks path information on vfs resources.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.3 $
 */
public class CmsCompatibleCheck {

    /**
     * Constructor, does nothing.<p> 
     */
    public CmsCompatibleCheck() {
        // nothing to do here 
    }

    /**
     * Helper for checking the templates from C_VFS_PATH_BODIES.<p>
     * 
     * @param el the Element to check
     * @return true if the element definition is ok
     */
    private boolean checkElementDefOk(Element el) {
        // first the name
        String elementName = el.getAttribute("name");
        if (!("contenttemplate".equalsIgnoreCase(elementName) || "frametemplate".equalsIgnoreCase(elementName))) {
            // no other elementdefinition allowed
            return false;
        }
        // now the templateclass only the standard class is allowed
        String elClass = el.getElementsByTagName("CLASS").item(0).getFirstChild().getNodeValue();
        if (!I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS.equals(elClass)) {
            return false;
        }
        String elTemplate = el.getElementsByTagName("TEMPLATE").item(0).getFirstChild().getNodeValue();
        if (elTemplate == null || elTemplate.indexOf(elementName) < 1) {
            // it must be in the path /content/"elementName"/ or in
            // the path /system/modules/"modulename"/"elementName"/
            return false;
        }
        return true;
    }

    /**
     * Helper for checking the templates from C_VFS_PATH_BODIES.<p>
     * 
     * @param el the Element to check
     * @return true if the template is ok
     */
    private boolean checkTemplateTagOk(Element el) {

        NodeList list = el.getChildNodes();
        if (list.getLength() > 3) {
            // only the one template tag allowed (and the two empty text nodes)
            return false;
        }
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            short ntype = n.getNodeType();
            if (ntype == Node.TEXT_NODE) {
                String nodeValue = n.getNodeValue();
                if ((nodeValue != null) && (nodeValue.trim().length() > 0)) {
                    return false;
                }
            } else if (ntype == Node.ELEMENT_NODE) {
                // this should be <ELEMENT name="frametemplate"/>
                if (!"element".equalsIgnoreCase(n.getNodeName())) {
                    return false;
                }
                if (!"frametemplate".equals(((Element)n).getAttribute("name"))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the resource path information fullfills the rules for template and body paths.<p>
     *
     * @param name the absolute path of the resource in the VFS
     * @param content the content of the resource.
     * @param type the resource type
     * @return true if the resource is ok
     */
    public boolean isTemplateCompatible(String name, byte[] content, String type) {

        // dont check folders
        if (CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME.equals(type)) {
            return true;
        }
        if (name == null) {
            return false;
        }
        if (name.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES)) {
            // this is a body file
            if (!CmsResourceTypePlain.C_RESOURCE_TYPE_NAME.equals(type)) {
                // only plain files allowed in content/bodys
                return false;
            }
            // to check the rest we have to parse the content
            try {
                org.w3c.dom.Document xmlDoc = A_CmsXmlContent.getXmlParser().parse(new java.io.StringReader(new String(content)));
                for (Node n = xmlDoc.getFirstChild(); n != null; n = treeWalker(xmlDoc, n)) {
                    short ntype = n.getNodeType();
                    if (((ntype > Node.CDATA_SECTION_NODE) && ntype < Node.DOCUMENT_TYPE_NODE) || (ntype == Node.ATTRIBUTE_NODE)) {
                        return false;
                    }
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        String tagName = n.getNodeName();
                        if (!("template".equalsIgnoreCase(tagName) || "xmltemplate".equalsIgnoreCase(tagName))) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                return false;
            }

        } else if (name.startsWith(I_CmsWpConstants.C_VFS_PATH_DEFAULT_TEMPLATES) || (name.startsWith(I_CmsWpConstants.C_VFS_PATH_MODULES) && name.indexOf("/" + I_CmsWpConstants.C_VFS_DIR_TEMPLATES) > -1)) {
            // this is a template file
            if (!CmsResourceTypePlain.C_RESOURCE_TYPE_NAME.equals(type)) {
                // only plain templates are allowed
                return false;
            }
            // to check the rest we have to parse the content
            try {
                org.w3c.dom.Document xmlDoc = A_CmsXmlContent.getXmlParser().parse(new java.io.StringReader(new String(content)));
                // we check the sub nodes from <xmltemplate>
                // there should be the two elementdefs, one template and some empty text nodes
                NodeList list = xmlDoc.getChildNodes();
                list = (list.item(0)).getChildNodes();
                int counterEldefs = 0;
                int counterTeplate = 0;
                for (int i = 0; i < list.getLength(); i++) {
                    Node n = list.item(i);
                    short nodeType = n.getNodeType();
                    if (nodeType == Node.ELEMENT_NODE) {
                        // allowed is the Elementdef or the template tag
                        String nodeName = n.getNodeName();
                        if ("elementdef".equalsIgnoreCase(nodeName)) {
                            // check the rules for the elementdefinitions
                            if (!checkElementDefOk((Element)n)) {
                                return false;
                            }
                            counterEldefs++;
                        } else if ("template".equalsIgnoreCase(nodeName)) {
                            // check if the template node is ok.
                            if (!checkTemplateTagOk((Element)n)) {
                                return false;
                            }
                            counterTeplate++;
                        } else {
                            //this name is not allowed
                            return false;
                        }

                    } else if (nodeType == Node.TEXT_NODE) {
                        // text node is only allowed if the value is empty
                        String nodeValue = n.getNodeValue();
                        if ((nodeValue != null) && (nodeValue.trim().length() > 0)) {
                            return false;
                        }
                    } else {
                        // this nodeType is not allowed
                        return false;
                    }
                }
                if (counterEldefs != 2 || counterTeplate != 1) {
                    // there have to be exactly two elementdefs and one template tag
                    return false;
                }

            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Help method to walk through the DOM document tree.<p>
     *
     * @param root the root Node
     * @param n a Node representing the current position in the tree
     * @return next node
     */
    private Node treeWalker(Node root, Node n) {
        Node nextnode = null;
        if (n.hasChildNodes()) {
            // child has child notes itself
            // process these first in the next loop
            nextnode = n.getFirstChild();
        } else {
            // child has no subchild.
            // so we take the next sibling
            nextnode = treeWalkerWidth(root, n);
        }
        return nextnode;
    }

    /**
     * Help method to walk through the DOM document tree by a width-first-order.<p>
     * 
     * @param root the root Node
     * @param n a Node representing the current position in the tree
     * @return next node
     */
    private Node treeWalkerWidth(Node root, Node n) {
        if (n == root) {
            return null;
        }
        Node nextnode = null;
        Node parent = null;
        nextnode = n.getNextSibling();
        parent = n.getParentNode();
        while (nextnode == null && parent != null && parent != root) {
            // child has sibling
            // last chance: we take our parent's sibling
            // (or our grandparent's sibling...)
            nextnode = parent.getNextSibling();
            parent = parent.getParentNode();
        }
        return nextnode;
    }
}