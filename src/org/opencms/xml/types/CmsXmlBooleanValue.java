/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlBooleanValue.java,v $
 * Date   : $Date: 2004/12/01 12:01:20 $
 * Version: $Revision: 1.8 $
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
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsBoolean".<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.8 $
 * @since 5.5.2
 */
public class CmsXmlBooleanValue extends A_CmsXmlValueTextBase {

    /** The name of this type as used in the XML schema. */
    public static final String C_TYPE_NAME = "OpenCmsBoolean";

    /** The boolean value of the element node. */
    private boolean m_boolean;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsBoolean".<p>
     */
    public CmsXmlBooleanValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsBoolean".<p>
     * 
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlBooleanValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
        m_boolean = Boolean.valueOf(m_stringValue).booleanValue();
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsBoolean".<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlBooleanValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * Returns the boolean value of the given XML content value.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param value the XML content value to get the boolean value of
     * 
     * @return the boolean value of the given XML content value
     * @throws CmsXmlException if something goes wrong
     */
    public static boolean getBooleanValue(CmsObject cms, I_CmsXmlContentValue value) throws CmsXmlException {

        boolean result;
        if (value instanceof CmsXmlBooleanValue) {
            // this is a "native" boolean type
            result = ((CmsXmlBooleanValue)value).getBooleanValue();
        } else {
            // get the boolean value from the String value
            result = Boolean.valueOf(value.getStringValue(cms)).booleanValue();
        }
        return result;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlBooleanValue(document, element, locale, this);
    }

    /**
     * Returns the boolean value as a boolean type.<p>
     * 
     * @return the boolean value as a boolean type
     */
    public boolean getBooleanValue() {

        return m_boolean;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getDefault(Locale)
     */
    public String getDefault(Locale locale) {

        if (m_defaultValue != null) {
            return m_defaultValue;
        }
        return "false";
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        return "<xsd:simpleType name=\"" + C_TYPE_NAME + "\"><xsd:restriction base=\"xsd:boolean\" /></xsd:simpleType>";
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

        return new CmsXmlBooleanValue(name, minOccurs, maxOccurs);
    }
}