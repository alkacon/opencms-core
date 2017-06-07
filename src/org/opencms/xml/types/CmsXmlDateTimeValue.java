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

import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;
import java.util.regex.Pattern;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsDateTime".<p>
 *
 * @since 6.0.0
 */
public class CmsXmlDateTimeValue extends A_CmsXmlValueTextBase {

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsDateTime";

    /** The validation rule used for this schema type. */
    public static final String TYPE_RULE = "-?\\p{Digit}+|"
        + CmsStringUtil.escapePattern(CmsMacroResolver.formatMacro(CmsMacroResolver.KEY_CURRENT_TIME));

    /** Pre-compiled regular expression pattern for this rule. */
    private static final Pattern TYPE_PATTERN = Pattern.compile(TYPE_RULE);

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
     * @param type the type instance to create the value for
     */
    public CmsXmlDateTimeValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
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
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlDateTimeValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#isSearchable()
     */
    @Override
    public boolean isSearchable() {

        // there is no point in searching date/time values
        // they are stored as long int anyway
        return false;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlDateTimeValue(document, element, locale, this);
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
    @Override
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

        StringBuffer result = new StringBuffer(256);
        // create a named decimal simpletype (for long values)
        result.append("<xsd:simpleType name=\"ocmsdatedec\"><xsd:restriction base=\"xsd:decimal\">");
        result.append("</xsd:restriction></xsd:simpleType>");
        // create a simpletype containing the "currenttime" macro
        result.append("<xsd:simpleType name=\"ocmsdatemacro\">");
        result.append("<xsd:restriction base=\"xsd:string\">");
        result.append("<xsd:enumeration value=\"");
        result.append(CmsMacroResolver.formatMacro(CmsMacroResolver.KEY_CURRENT_TIME));
        result.append("\"/>");
        result.append("</xsd:restriction></xsd:simpleType>");
        // unify the simpletypes for the datetime value
        result.append("<xsd:simpleType name=\"");
        result.append(TYPE_NAME);
        result.append("\"><xsd:union memberTypes=\"ocmsdatedec ocmsdatemacro\"/></xsd:simpleType>");

        return result.toString();
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

        return new CmsXmlDateTimeValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#validateValue(java.lang.String)
     */
    @Override
    public boolean validateValue(String value) {

        return TYPE_PATTERN.matcher(value).matches();
    }
}