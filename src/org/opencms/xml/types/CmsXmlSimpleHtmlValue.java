/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/Attic/CmsXmlSimpleHtmlValue.java,v $
 * Date   : $Date: 2004/10/23 06:50:36 $
 * Version: $Revision: 1.4 $
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
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.I_CmsXmlDocument;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsSimpleHtml".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 * @since 5.5.0
 */
public class CmsXmlSimpleHtmlValue extends A_CmsXmlContentValue implements I_CmsXmlContentValue {

    /** The name of this type as used in the XML schema. */
    public static final String C_TYPE_NAME = "OpenCmsSimpleHtml";

    /** The String value of the element node. */
    private String m_stringValue;

    /**
     * Creates a new String type definition.<p>
     */
    public CmsXmlSimpleHtmlValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value.<p>
     * 
     * @param element the XML element that contains the value
     * @param name the node name of this value in the source XML document
     * @param index the index of the XML element in the source document
     */
    public CmsXmlSimpleHtmlValue(Element element, String name, int index) {

        m_element = element;
        m_name = name;
        m_index = index;
        m_stringValue = element.getText();
    }

    /**
     * Creates a new HTML type which must occur exaclty once.<p>
     * 
     * @param name the name of the element
     */
    public CmsXmlSimpleHtmlValue(String name) {

        m_name = name;
        m_minOccurs = 1;
        m_maxOccurs = 1;
    }

    /**
     * Creates a new HTML type.<p>
     * 
     * @param name the name of the element
     * @param minOccurs minimum number of occurences
     * @param maxOccurs maximum number of occurences
     */
    public CmsXmlSimpleHtmlValue(String name, int minOccurs, int maxOccurs) {

        m_name = name;
        m_minOccurs = minOccurs;
        m_maxOccurs = maxOccurs;
    }

    /**
     * Creates a new HTML type.<p>
     * 
     * @param name the name of the element
     * @param minOccurs minimum number of occurences
     * @param maxOccurs maximum number of occurences
     */
    public CmsXmlSimpleHtmlValue(String name, String minOccurs, String maxOccurs) {

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

        return new CmsXmlSimpleHtmlValue(element, name, index);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        return "<xsd:simpleType name=\"" + C_TYPE_NAME + "\"><xsd:restriction base=\"xsd:string\" /></xsd:simpleType>";
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject, I_CmsXmlDocument)
     */
    public String getStringValue(CmsObject cms, I_CmsXmlDocument document) {

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

        return new CmsXmlSimpleHtmlValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(java.lang.String)
     */
    public void setStringValue(String value) {

        m_element.clearContent();
        if (CmsStringUtil.isNotEmpty(value)) { 
            m_element.addCDATA(value);
        }
    }
}