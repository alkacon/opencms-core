/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlNestedContentDefinition.java,v $
 * Date   : $Date: 2004/11/30 14:23:51 $
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
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * A nested content XML definition that is included by another XML content definition.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.4
 */
public class CmsXmlNestedContentDefinition extends A_CmsXmlContentValue implements I_CmsXmlSchemaType {

    /** The nested content definition. */
    CmsXmlContentDefinition m_contentDefinition;

    /**
     * Creates a new nested content definition.<p>
     * 
     * @param contentDefinition the content definition to nest
     * @param name the type name of the content definition in the containing document
     * @param minOccurs the minimum occurences
     * @param maxOccurs the maximum occurences
     */
    public CmsXmlNestedContentDefinition(
        CmsXmlContentDefinition contentDefinition,
        String name,
        String minOccurs,
        String maxOccurs) {

        m_contentDefinition = contentDefinition;

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
     * Creates a new XML content value for the nested content definition.<p> 
     * 
     * @param element the parent element of the value
     * @param contentDefinition the nested XML content definition
     * @param name the node name of this value in the source XML document
     */
    public CmsXmlNestedContentDefinition(Element element, CmsXmlContentDefinition contentDefinition, String name) {

        m_element = element;
        m_name = name;
        m_contentDefinition = contentDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#appendDefaultXml(org.dom4j.Element, Locale)
     */
    public void appendDefaultXml(Element root, Locale locale) {

        // create a default XML document for the nested content definition
        Document doc = m_contentDefinition.createDocument(Locale.ENGLISH);
        // the first language nod contains the created values        
        Element element = (Element)doc.getRootElement().elements().get(0);
        // detach the created element
        element.detach();
        // clear the attributes (e.g. language)
        element.attributes().clear();
        // set the name of the main element node (otherwise it would be the default according to the nested schema) 
        element.setName(getElementName());
        // append XML to parent node
        root.add(element);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#createValue(org.dom4j.Element, java.lang.String, Locale)
     */
    public I_CmsXmlContentValue createValue(Element element, String name, Locale locale) {

        return new CmsXmlNestedContentDefinition(element, m_contentDefinition, name);
    }

    /**
     * Returns the nested content definition.<p>
     *
     * @return the nested content definition
     */
    public CmsXmlContentDefinition getContentDefinition() {

        return m_contentDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        throw new RuntimeException("Unable to get the schema definition of a nested XML content definition");
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument)
     */
    public String getStringValue(CmsObject cms, I_CmsXmlDocument document) {

        throw new RuntimeException("Unable to get the string value of a nested XML content definition");
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getTypeName()
     */
    public String getTypeName() {

        return m_contentDefinition.getTypeName();
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#isSimpleType()
     */
    public boolean isSimpleType() {

        // nested content definitions are never simple
        return false;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        throw new RuntimeException("Unable to create a new instance of a nested XML content definition");
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(java.lang.String)
     */
    public void setStringValue(String value) {

        throw new RuntimeException("Unable to set the string value of a nested XML content definition");
    }
}