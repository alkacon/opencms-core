/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlColorValue.java,v $
 * Date   : $Date: 2004/11/30 16:04:21 $
 * Version: $Revision: 1.5 $
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

import java.util.Locale;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsColor".<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.5.2
 */
public class CmsXmlColorValue extends A_CmsXmlValueTextBase {

    /** The name of this type as used in the XML schema. */
    public static final String C_TYPE_NAME = "OpenCmsColor";

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsColor".<p>
     */
    public CmsXmlColorValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsColor".<p>
     * 
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     */
    public CmsXmlColorValue(Element element, Locale locale) {

        super(element, locale);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsColor".<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlColorValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(Element element, Locale locale) {

        return new CmsXmlColorValue(element, locale);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getDefault(Locale)
     */
    public String getDefault(Locale locale) {

        if (m_defaultValue != null) {
            return m_defaultValue;
        }
        return "#000000";
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        return "<xsd:simpleType name=\""
            + C_TYPE_NAME
            + "\"><xsd:restriction base=\"xsd:string\">"
            + "<xsd:pattern value=\""
            + "#?([a-f]|[A-F]|[0-9]){3}(([a-f]|[A-F]|[0-9]){3})?"
            + "\" /></xsd:restriction></xsd:simpleType>";
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

        return new CmsXmlColorValue(name, minOccurs, maxOccurs);
    }
}