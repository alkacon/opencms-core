/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
 * Describes the XML content type "OpenCmsColor".<p>
 *
 * @since 6.0.0
 */
public class CmsXmlColorValue extends A_CmsXmlValueTextBase {

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsColor";

    /** The validation rule used for this schema type. */
    public static final String TYPE_RULE = "(#([a-f]|[A-F]|[0-9]){3}(([a-f]|[A-F]|[0-9]){3})?)|ActiveBorder|ActiveCaption|ActiveCaptionText|AppWorkspace|Background|ButtonFace|ButtonHighlight|ButtonShadow|ButtonText|CaptionText|GrayText|Highlight|HighlightText|InactiveBorder|InactiveCaption|InactiveCaptionText|InfoBackground|InfoBackground|InfoText|MenuText|Menu|ScrollBar|ThreeDDarkShadow|ThreeDFace|ThreeDHighlight|ThreeDLightShadow|ThreeDShadow|Window|WindowFrame|WindowText";

    /** Pre-compiled regular expression pattern for this rule. */
    private static final Pattern TYPE_PATTERN = Pattern.compile(TYPE_RULE);

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsColor".<p>
     */
    public CmsXmlColorValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsColor".<p>
     *
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlColorValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsColor".<p>
     *
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlColorValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlColorValue(document, element, locale, this);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getDefault(Locale)
     */
    @Override
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
     * @see org.opencms.xml.types.A_CmsXmlContentValue#isSearchable()
     */
    @Override
    public boolean isSearchable() {

        // there is no point in searching color values
        return false;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlColorValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#validateValue(java.lang.String)
     */
    @Override
    public boolean validateValue(String value) {

        return TYPE_PATTERN.matcher(value).matches();
    }
}