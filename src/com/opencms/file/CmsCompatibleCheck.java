package com.opencms.file;
/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsCompatibleCheck.java,v $
 * Date   : $Date: 2001/07/10 15:44:15 $
 * Version: $Revision: 1.1 $
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

import com.opencms.template.*;
import com.opencms.core.*;
import org.w3c.dom.*;


/**
 * checks if the resource sticks to the rules for templates and bodys if it is in
 * the correlative path.
 *
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsCompatibleCheck implements I_CmsConstants{


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
    if ( name == null){
        return false;
    }
    if (name.startsWith("/content/bodys/")){
        // this is a body file
        if (!C_TYPE_PLAIN_NAME.equals(type)){
            // only plain files allowed in content/bodys
            return false;
        }
        // to check the rest we have to parse the content
        try{
            org.w3c.dom.Document xmlDoc = A_CmsXmlContent.getXmlParser().parse(
                                            new java.io.StringReader(new String(content)));
             for(Node n = xmlDoc.getFirstChild(); n != null; n = treeWalker(xmlDoc, n)) {
                System.err.println("mgm--node:"+ n.getNodeName()+" type:"+n.getNodeType()+" value:"+n.getNodeValue());
                if((n.getNodeType() > n.CDATA_SECTION_NODE) || (n.getNodeType() == n.ATTRIBUTE_NODE)){
                    return false;
                }
                if(n.getNodeType() == n.ELEMENT_NODE){
                    String tagName = n.getNodeName();
                    if(!("template".equalsIgnoreCase(tagName) || "xmltemplate".equalsIgnoreCase(tagName))){
                        return false;
                    }
                }
            }
        }catch(Exception e){
            return false;
        }

    } else if (name.startsWith("/content/templates/")
                || (name.startsWith("/system/modules/") && name.indexOf("/templates/") > -1)){
        // this is a template file
        if (!C_TYPE_PLAIN_NAME.equals(type)){
            // only plain templates are allowed
            return false;
        }
        // to check the rest we have to parse the content
        try{
            org.w3c.dom.Document xmlDoc = A_CmsXmlContent.getXmlParser().parse(
                                            new java.io.StringReader(new String(content)));
            NodeList list = xmlDoc.getChildNodes();
            for(int i=0; i<list.getLength(); i++){
                Node n = list.item(i);
//mgm at work                System.err.println("mgm--t node:"+ n.getNodeName()+" type:"+n.getNodeType()+" value:"+n.getNodeValue());
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

}