/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/Attic/CmsCompatibleCheck.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.12 $
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

package org.opencms.importexport;

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Checks path information on vfs resources.<p>
 *
 * @author Alexander Kandzior 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.12 $
 */
public class CmsCompatibleCheck {

    /** Default class for templates. */
    public static final String XML_CONTROL_DEFAULT_CLASS = "com.opencms.template.CmsXmlTemplate";

    /**
     * Constructor, does nothing.<p> 
     */
    public CmsCompatibleCheck() {

        // nothing to do here 
    }

    /**
     * Checks if the resource path information fulfills the rules for template and body paths.<p>
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
            if (!CmsResourceTypePlain.getStaticTypeName().equals(type)) {
                // only plain files allowed in content/bodys
                return false;
            }
            // to check the rest we have to parse the content
            try {
                Document xmlDoc = DocumentHelper.parseText(new String(content));
                for (Node n = (Node)xmlDoc.content().get(0); n != null; n = treeWalker(xmlDoc, n)) {
                    short ntype = n.getNodeType();
                    if (((ntype > Node.CDATA_SECTION_NODE) && ntype < Node.DOCUMENT_TYPE_NODE)
                        || (ntype == Node.ATTRIBUTE_NODE)) {
                        return false;
                    }
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        String tagName = n.getName();
                        if (!("template".equalsIgnoreCase(tagName) || "xmltemplate".equalsIgnoreCase(tagName))) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                return false;
            }

        } else if (name.startsWith(I_CmsWpConstants.C_VFS_PATH_DEFAULT_TEMPLATES)
            || (name.startsWith(I_CmsWpConstants.C_VFS_PATH_MODULES) && name.indexOf("/"
                + I_CmsWpConstants.C_VFS_DIR_TEMPLATES) > -1)) {
            // this is a template file
            if (!CmsResourceTypePlain.getStaticTypeName().equals(type)) {
                // only plain templates are allowed
                return false;
            }
            // to check the rest we have to parse the content
            try {
                Document xmlDoc = DocumentHelper.parseText(new String(content));

                // we check the sub nodes from <xmltemplate>
                // there should be the two elementdefs, one template and some empty text nodes
                List list = xmlDoc.getRootElement().content();
                list = ((Element)list.get(0)).content();
                int counterEldefs = 0;
                int counterTeplate = 0;
                for (int i = 0; i < list.size(); i++) {
                    Node n = (Node)list.get(i);
                    short nodeType = n.getNodeType();
                    if (nodeType == Node.ELEMENT_NODE) {
                        // allowed is the Elementdef or the template tag
                        String nodeName = n.getName();
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
                        String nodeValue = n.getText();
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
     * Helper for checking the templates from C_VFS_PATH_BODIES.<p>
     * 
     * @param el the Element to check
     * @return true if the element definition is ok
     */
    private boolean checkElementDefOk(Element el) {

        // first the name
        String elementName = el.attribute("name").getText();
        if (!("contenttemplate".equalsIgnoreCase(elementName) || "frametemplate".equalsIgnoreCase(elementName))) {
            // no other elementdefinition allowed
            return false;
        }
        // now the templateclass only the standard class is allowed
        String elClass = CmsImport.getChildElementTextValue(el, "CLASS");
        if (!CmsCompatibleCheck.XML_CONTROL_DEFAULT_CLASS.equals(elClass)) {
            return false;
        }
        String elTemplate = CmsImport.getChildElementTextValue(el, "TEMPLATE");
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

        List list = el.elements();
        if (list.size() > 3) {
            // only the one template tag allowed (and the two empty text nodes)
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            Node n = (Node)list.get(i);
            short ntype = n.getNodeType();
            if (ntype == Node.TEXT_NODE) {
                String nodeValue = n.getText();
                if ((nodeValue != null) && (nodeValue.trim().length() > 0)) {
                    return false;
                }
            } else if (ntype == Node.ELEMENT_NODE) {
                // this should be <ELEMENT name="frametemplate"/>
                if (!"element".equalsIgnoreCase(n.getName())) {
                    return false;
                }
                if (!"frametemplate".equals(((Element)n).attribute("name").getText())) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the next sibling of a node.<p>
     * 
     * @param node the node
     * @return the next sibling, or null
     */
    private Node getNextSibling(Node node) {

        Node parent = null;
        Node sibling = null;
        List content = null;
        int i = 0;

        if ((parent = node.getParent()) != null) {
            content = ((Element)parent).content();
            i = content.indexOf(node);
            if (i < content.size() - 1) {
                sibling = (Node)content.get(i + 1);
            }
        }

        return sibling;
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
        if (n.hasContent()) {
            // child has child notes itself
            // process these first in the next loop
            nextnode = (Node)((Element)n).content().get(0);
        } else {
            // child has no subchild.
            // so we take the next sibling
            nextnode = treeWalkerBreadth(root, n);
        }
        return nextnode;
    }

    /**
     * Help method to walk through the document tree by a breadth-first-order.<p>
     * 
     * @param root the root Node
     * @param n a Node representing the current position in the tree
     * @return next node
     */
    private Node treeWalkerBreadth(Node root, Node n) {

        if (n == root) {
            return null;
        }
        Node nextnode = null;
        Node parent = null;
        nextnode = getNextSibling(n);
        parent = n.getParent();
        while (nextnode == null && parent != null && parent != root) {
            // child has sibling
            // last chance: we take our parent's sibling
            // (or our grandparent's sibling...)
            nextnode = getNextSibling(parent);
            parent = parent.getParent();
        }
        return nextnode;
    }

}