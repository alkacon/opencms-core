/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlDateTimeValue.java,v $
 * Date   : $Date: 2004/11/30 17:20:31 $
 * Version: $Revision: 1.9 $
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

import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsDateTime".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.9 $
 * @since 5.5.0
 */
public class CmsXmlDateTimeValue extends A_CmsXmlValueTextBase {

    /** The name of this type as used in the XML schema. */
    public static final String C_TYPE_NAME = "OpenCmsDateTime";

    /** The long value (timestamp). */
    private long m_dateTime;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsDateTime".<p>
     */
    public CmsXmlDateTimeValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsDateTime".<p>
     * 
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     */
    public CmsXmlDateTimeValue(I_CmsXmlDocument document, Element element, Locale locale) {

        super(document, element, locale);
        try {
            m_dateTime = Long.valueOf(m_stringValue).longValue();
        } catch (NumberFormatException e) {
            m_dateTime = 0;
        }
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsDateTime".<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlDateTimeValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlDateTimeValue(document, element, locale);
    }

    /**
     * Returns the date time value as a long.<p>
     * 
     * @return the date time value as a long
     */
    public long getDateTimeValue() {

        return m_dateTime;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getDefault(Locale)
     */
    public String getDefault(Locale locale) {

        if (m_defaultValue != null) {
            return m_defaultValue;
        }
        return "0";
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        return "<xsd:simpleType name=\"" + C_TYPE_NAME + "\"><xsd:restriction base=\"xsd:decimal\" /></xsd:simpleType>";
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

        return new CmsXmlDateTimeValue(name, minOccurs, maxOccurs);
    }
}