/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsWorkplaceEditorConfiguration.java,v $
 * Date   : $Date: 2004/02/04 15:48:16 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editor;


/**
 * Single editor configuration object.<p>
 * 
 * Holds all necessary information about an OpenCms editor which is stored in the
 * "editor_configuration.xml" file in the editor folder.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.1
 */
public class CmsWorkplaceEditorConfiguration {
    
    /** Name of the root document node */
    private static final String C_DOCUMENT_NODE = "editor";
    /** Name of the resourcetypes node */
    public static final String C_NODE_RESOURCETYPES = "resourcetypes";   
    /** Name of the resource type node */
    public static final String C_NODE_TYPE = "type";
    /** Name of the resource type subnode name */
    public static final String C_NODE_NAME = "name";
    /** Name of the resource type subnode ranking */
    public static final String C_NODE_RANKING = "ranking";
    /** Name of the resource type subnode mapto */
    public static final String C_NODE_MAPTO = "mapto";
    /** Name of the useragents node */
    public static final String C_NODE_USERAGENTS = "useragents";
    /** Name of the single user agent node */
    public static final String C_NODE_AGENT = "agent";
    
//    private Document m_document;
//    
//    private Map m_resTypes;
//    private List m_userAgents;
    
    /**
     * Constructor with xml data String.<p>
     * 
     * @param xmlData the XML data String containing the information about the editor
     */
    public CmsWorkplaceEditorConfiguration(String xmlData) {
        C_DOCUMENT_NODE.equals(xmlData);
//        SAXReader reader = new SAXReader();
//        try {
//            m_document = reader.read(new StringReader(xmlData));
//            initialize();
//            
//        } catch (DocumentException e) {
//            // cannot read page
//        }
    }
    
}
