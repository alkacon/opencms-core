/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsCompatibleCheck.java,v $
* Date   : $Date: 2003/07/31 13:19:37 $
* Version: $Revision: 1.11 $
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

package com.opencms.file;

import com.opencms.core.I_CmsConstants;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.workplace.I_CmsWpConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Checks if the resource sticks to the rules for templates and bodys if it is in
 * the correlative path.
 *
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsCompatibleCheck {


    public CmsCompatibleCheck() {
    }

    /**
     * checks if the resource sticks to the rules for templates and bodys if it is in
     * the correlative path. If this method is called from the CmsImport the resource
     * does not exist in opencms therefor the content is part of the parameter.
     *
     * @param name The absolute path of the resource in OpenCms.
     * @param content The content of the resource.
     * @param type The resource type.
     * @return true if the resource is ok.
     */
    public boolean isTemplateCompatible(String name, byte[] content, String type){

        // dont check folders
        if (CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME.equals(type)){
            return true;
        }
        if ( name == null){
            return false;
        }
        if (name.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES)){
            // this is a body file
            if (!CmsResourceTypePlain.C_RESOURCE_TYPE_NAME.equals(type)){
                // only plain files allowed in content/bodys
                return false;
            }
            // to check the rest we have to parse the content
            try{
                org.w3c.dom.Document xmlDoc = A_CmsXmlContent.getXmlParser().parse(
                                                new java.io.StringReader(new String(content)));
                 for(Node n = xmlDoc.getFirstChild(); n != null; n = treeWalker(xmlDoc, n)) {
                    short ntype = n.getNodeType();
                    if(((ntype > Node.CDATA_SECTION_NODE) && ntype < Node.DOCUMENT_TYPE_NODE)
                                        || (ntype == Node.ATTRIBUTE_NODE)){
                        return false;
                    }
                    if(n.getNodeType() == Node.ELEMENT_NODE){
                        String tagName = n.getNodeName();
                        if(!("template".equalsIgnoreCase(tagName) || "xmltemplate".equalsIgnoreCase(tagName))){
                            return false;
                        }
                    }
                }
            }catch(Exception e){
                return false;
            }

        } else if (name.startsWith(I_CmsWpConstants.C_VFS_PATH_DEFAULT_TEMPLATES)
                    || (name.startsWith(I_CmsWpConstants.C_VFS_PATH_MODULES) && name.indexOf("/" + I_CmsWpConstants.C_VFS_DIR_TEMPLATES) > -1)){
            // this is a template file
            if (!CmsResourceTypePlain.C_RESOURCE_TYPE_NAME.equals(type)){
                // only plain templates are allowed
                return false;
            }
            // to check the rest we have to parse the content
            try{
                org.w3c.dom.Document xmlDoc = A_CmsXmlContent.getXmlParser().parse(
                                                new java.io.StringReader(new String(content)));
                // we check the sub nodes from <xmltemplate>
                // there should be the two elementdefs, one template and some empty text nodes
                NodeList list = xmlDoc.getChildNodes();
                list = (list.item(0)).getChildNodes();
                int counterEldefs = 0;
                int counterTeplate = 0;
                for(int i=0; i<list.getLength(); i++){
                    Node n = list.item(i);
                    short nodeType = n.getNodeType();
                    if(nodeType == Node.ELEMENT_NODE){
                        // allowed is the Elementdef or the template tag
                        String nodeName = n.getNodeName();
                        if ("elementdef".equalsIgnoreCase(nodeName)){
                            // check the rules for the elementdefinitions
                            if(!isElementDefOk((Element)n)){
                                return false;
                            }
                            counterEldefs++;
                        }else if("template".equalsIgnoreCase(nodeName)){
                            // check if the template node is ok.
                            if(!isTemplateTagOk((Element)n)){
                                return false;
                            }
                            counterTeplate++;
                        }else{
                            //this name is not allowed
                            return false;
                        }

                    }else if(nodeType == Node.TEXT_NODE){
                        // text node is only allowed if the value is empty
                        String nodeValue = n.getNodeValue();
                        if((nodeValue != null) && (nodeValue.trim().length() > 0)){
                            return false;
                        }
                    }else{
                        // this nodeType is not allowed
                        return false;
                    }
                }
                if(counterEldefs != 2 || counterTeplate != 1){
                    // there have to be exactly two elementdefs and one template tag
                    return false;
                }

            }catch(Exception e){
                return false;
            }
        }
        return true;
    }

    /**
     * Help method to walk through the DOM document tree.
     * First it will be looked for children of the given node.
     * If there are no children, the siblings and the siblings of our parents
     * are examined. This will be done by calling treeWalkerWidth.
     * @param n Node representing the actual position in the tree
     * @return next node
     */
    private Node treeWalker(Node root, Node n) {
        Node nextnode = null;
        if(n.hasChildNodes()) {
            // child has child notes itself
            // process these first in the next loop
            nextnode = n.getFirstChild();
        }else {
            // child has no subchild.
            // so we take the next sibling
            nextnode = treeWalkerWidth(root, n);
        }
        return nextnode;
    }

    /**
     * Help method to walk through the DOM document tree by a
     * width-first-order.
     * @param n Node representing the actual position in the tree
     * @return next node
     */
    private Node treeWalkerWidth(Node root, Node n) {
        if(n == root) {
            return null;
        }
        Node nextnode = null;
        Node parent = null;
        nextnode = n.getNextSibling();
        parent = n.getParentNode();
        while(nextnode == null && parent != null && parent != root) {
            // child has sibling
            // last chance: we take our parent's sibling
            // (or our grandparent's sibling...)
            nextnode = parent.getNextSibling();
            parent = parent.getParentNode();
        }
        return nextnode;
    }

    /**
     * helper for checking the templates from C_VFS_PATH_BODIES.
     * This helper checks a elementdef Node if it sticks to the rules.
     * @param el the Element to check.
     * @return true if the elementdef is ok.
     */
    private boolean isElementDefOk(Element el){
        // first the name
        String elementName = el.getAttribute("name");
        if(!("contenttemplate".equalsIgnoreCase(elementName)
                    || "frametemplate".equalsIgnoreCase(elementName) )){
            // no other elementdefinition allowed
            return false;
        }
        // now the templateclass only the standard class is allowed
        String elClass = el.getElementsByTagName("CLASS").item(0).getFirstChild().getNodeValue();
        if(! I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS.equals(elClass)){
            return false;
        }
        String elTemplate = el.getElementsByTagName("TEMPLATE").item(0).getFirstChild().getNodeValue();
        if(elTemplate == null || elTemplate.indexOf(elementName) < 1){
            // it must be in the path /content/"elementName"/ or in
            // the path /system/modules/"modulename"/"elementName"/
            return false;
        }
        return true;
    }

    /**
     * helper for checking the templates from C_VFS_PATH_BODIES.
     * This helper checks a template Node if it sticks to the rules.
     * @param el the Element to check.
     * @return true if the template is ok.
     */
    private boolean isTemplateTagOk(Element el){

        NodeList list = el.getChildNodes();
        if(list.getLength() > 3){
            // only the one template tag allowed (and the two empty text nodes)
            return false;
        }
        for(int i=0; i<list.getLength(); i++){
            Node n = list.item(i);
            short ntype = n.getNodeType();
            if(ntype == Node.TEXT_NODE){
                String nodeValue = n.getNodeValue();
                if((nodeValue != null) && (nodeValue.trim().length() > 0)){
                    return false;
                }
            }else if(ntype == Node.ELEMENT_NODE){
                // this should be <ELEMENT name="frametemplate"/>
                if(! "element".equalsIgnoreCase(n.getNodeName())){
                    return false;
                }
                if(!"frametemplate".equals(((Element)n).getAttribute("name"))){
                    return false;
                }
            }else{
                return false;
            }
        }
        return true;
    }
}