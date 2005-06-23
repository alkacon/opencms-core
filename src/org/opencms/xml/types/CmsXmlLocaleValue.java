/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlLocaleValue.java,v $
 * Date   : $Date: 2005/06/23 10:47:09 $
 * Version: $Revision: 1.14 $
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
import java.util.regex.Pattern;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsLocale".<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.0.0 
 */
public class CmsXmlLocaleValue extends A_CmsXmlValueTextBase {

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsLocale";

    /** The validation rule used for this schema type. */
    public static final String TYPE_RULE = "[a-z]{2}(_[A-Z]{2}(_[a-zA-Z0-9]+){0,1}){0,1}";

    /** Pre-compiled regular expression pattern for this rule. */
    private static final Pattern TYPE_PATTERN = Pattern.compile(TYPE_RULE);

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsLocale".<p>
     */
    public CmsXmlLocaleValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsLocale".<p>
     * 
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlLocaleValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsLocale".<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlLocaleValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlLocaleValue(document, element, locale, this);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        return "<xsd:simpleType name=\""
            + TYPE_NAME
            + "\"><xsd:restriction base=\"xsd:string\">"
            + "<xsd:pattern value=\""
            + TYPE_RULE
            + "\" /></xsd:restriction></xsd:simpleType>";
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getTypeName()
     */
    public String getTypeName() {

        return TYPE_NAME;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlLocaleValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#validateValue(java.lang.String)
     */
    public boolean validateValue(String value) {

        return TYPE_PATTERN.matcher(value).matches();
    }
}