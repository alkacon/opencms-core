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

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.json.JSONObject;
import org.opencms.widgets.serialdate.CmsSerialDateValue;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.xml2json.I_CmsJsonFormattableValue;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsSerialDate".<p>
 *
 * @since 11.0.0
 */
public class CmsXmlSerialDateValue extends A_CmsXmlValueTextBase
implements I_CmsXmlValidateWithMessage, I_CmsJsonFormattableValue {

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsSerialDate";

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsDateTime".<p>
     */
    public CmsXmlSerialDateValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsSerialDate".<p>
     *
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlSerialDateValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsDateTime".<p>
     *
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlSerialDateValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlSerialDateValue(document, element, locale, this);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getDefault(Locale)
     */
    @Override
    public String getDefault(Locale locale) {

        if (m_defaultValue != null) {
            return m_defaultValue;
        }
        return "";
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        return "<xsd:simpleType name=\"" + TYPE_NAME + "\"><xsd:restriction base=\"xsd:string\" /></xsd:simpleType>";
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

        // there is no point in searching date/time values
        return false;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlSerialDateValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonFormattableValue#toJson(org.opencms.file.CmsObject)
     */
    @SuppressWarnings("finally")
    public Object toJson(CmsObject cms) {

        JSONObject result = null;
        try {
            result = new JSONObject(getStringValue(cms));
        } catch (Exception e) {
            result = new JSONObject("{}");
        } finally {
            return result;
        }
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#validateValue(java.lang.String)
     */
    @Override
    public boolean validateValue(String value) {

        return null == validateWithMessage(value);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlValidateWithMessage#validateWithMessage(java.lang.String)
     */
    public CmsMessageContainer validateWithMessage(String value) {

        CmsSerialDateValue wrapper = new CmsSerialDateValue(value);
        return wrapper.validateWithMessage();
    }
}