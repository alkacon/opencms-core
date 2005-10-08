/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlNestedContentDefinition.java,v $
 * Date   : $Date: 2005/10/08 08:30:32 $
 * Version: $Revision: 1.13.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsRuntimeException;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * A nested content XML definition that is included by another XML content definition.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.13.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsXmlNestedContentDefinition extends A_CmsXmlContentValue implements I_CmsXmlSchemaType {

    /** The nested content definition. */
    private CmsXmlContentDefinition m_nestedContentDefinition;

    /**
     * Creates a new XML content value for the nested content definition.<p> 
     * 
     * @param document the XML content instance this value belongs to
     * @param contentDefinition the nested XML content definition
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlNestedContentDefinition(
        CmsXmlContentDefinition contentDefinition,
        I_CmsXmlDocument document,
        Element element,
        Locale locale,
        I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
        m_nestedContentDefinition = contentDefinition;
    }

    /**
     * Creates a new nested content definition.<p>
     * 
     * @param contentDefinition the content definition to nest
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlNestedContentDefinition(
        CmsXmlContentDefinition contentDefinition,
        String name,
        String minOccurs,
        String maxOccurs) {

        super(name, minOccurs, maxOccurs);
        m_nestedContentDefinition = contentDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlNestedContentDefinition(m_nestedContentDefinition, document, element, locale, this);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#generateXml(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, org.dom4j.Element, java.util.Locale)
     */
    public Element generateXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        // create the XML base node for the nested content definition
        Element element = root.addElement(getName());
        // create a default XML element for the nested content definition        
        element = m_nestedContentDefinition.createDefaultXml(cms, document, element, Locale.ENGLISH);
        // retrun the generated element
        return element;
    }

    /**
     * Returns the nested content definition.<p>
     *
     * @return the nested content definition
     */
    public CmsXmlContentDefinition getNestedContentDefinition() {

        return m_nestedContentDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        throw new CmsRuntimeException(Messages.get().container(Messages.ERR_NESTED_SCHEMA_0));
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(org.opencms.file.CmsObject)
     */
    public String getStringValue(CmsObject cms) throws CmsRuntimeException {

        throw new CmsRuntimeException(Messages.get().container(Messages.ERR_NESTED_GETVALUE_0));
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getTypeName()
     */
    public String getTypeName() {

        return m_nestedContentDefinition.getTypeName();
    }

    /**
     * Returns <code>false</code>, since nested content definitions are never simple.<p>
     *  
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#isSimpleType()
     */
    public boolean isSimpleType() {

        return false;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        throw new CmsRuntimeException(Messages.get().container(Messages.ERR_NESTED_NEWINSTANCE_0));
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) {

        throw new CmsRuntimeException(Messages.get().container(Messages.ERR_NESTED_SETVALUE_0));
    }
}