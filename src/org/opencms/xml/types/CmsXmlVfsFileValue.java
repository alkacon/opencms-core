/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlVfsFileValue.java,v $
 * Date   : $Date: 2004/10/18 13:04:55 $
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
package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsVfsFile".<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.2
 */
public class CmsXmlVfsFileValue extends A_CmsXmlContentValue {
    
    /** The name of this type as used in the XML schema. */
    public static final String C_TYPE_NAME = "OpenCmsVfsFile";
    
    /** The String value of the element node. */
    private String m_stringValue;     
    
    /**
     * Creates a new VFS file type definition.<p>
     */
    public CmsXmlVfsFileValue() {
        // empty constructor is required for class registration
    }
    
    /**
     * Creates a new XML content value.<p>
     * 
     * @param element the XML element that contains the value
     * @param name the node name of this value in the source XML document
     * @param index the index of the XML element in the source document
     */
    public CmsXmlVfsFileValue(Element element, String name, int index) {
        m_element = element;
        m_name = name;
        m_index = index;
        m_stringValue = element.getText();
    }
    
    /**
     * Creates a new VFS file type which must occur exaclty once.<p>
     * 
     * @param name the name of the element
     */
    public CmsXmlVfsFileValue(String name) {
        m_name = name;
        m_minOccurs = 1;
        m_maxOccurs = 1;
    }
    
    /**
     * Creates a new VFS file type.<p>
     * 
     * @param name the name of the element
     * @param minOccurs minimum number of occurences
     * @param maxOccurs maximum number of occurences
     */
    public CmsXmlVfsFileValue(String name, int minOccurs, int maxOccurs) {
        m_name = name;
        m_minOccurs = minOccurs;
        m_maxOccurs = maxOccurs;
    }

    /**
     * Creates a new VFS file type.<p>
     * 
     * @param name the name of the element
     * @param minOccurs minimum number of occurences
     * @param maxOccurs maximum number of occurences
     */
    public CmsXmlVfsFileValue(String name, String minOccurs, String maxOccurs) {
        m_name = name;
        m_minOccurs = 1;
        if (CmsStringUtil.isNotEmpty(minOccurs)) {
            try {
                m_minOccurs = Integer.valueOf(minOccurs).intValue();
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        m_maxOccurs = 1;
        if (CmsStringUtil.isNotEmpty(maxOccurs)) {
            if (CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_UNBOUNDED.equals(maxOccurs)) {
                m_maxOccurs = Integer.MAX_VALUE;
            } else {
                try {
                    m_maxOccurs = Integer.valueOf(maxOccurs).intValue();
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(org.dom4j.Element, java.lang.String, int)
     */
    public I_CmsXmlContentValue createValue(Element element, String name, int index) {
        
        return new CmsXmlVfsFileValue(element, name, index);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {
        
        return "<xsd:simpleType name=\""
            + C_TYPE_NAME
            + "\"><xsd:restriction base=\"xsd:string\" /></xsd:simpleType>";   
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject, A_CmsXmlDocument)
     */
    public String getStringValue(CmsObject cms, A_CmsXmlDocument document) {

        return m_stringValue;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getTypeName()
     */
    public String getTypeName() {

        return C_TYPE_NAME;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlVfsFileValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, org.opencms.xml.A_CmsXmlDocument, java.lang.String)
     */
    public void setStringValue(CmsObject cms, A_CmsXmlDocument document, String value) {

        m_element.clearContent();
        m_element.addCDATA(value);
    }  
}
