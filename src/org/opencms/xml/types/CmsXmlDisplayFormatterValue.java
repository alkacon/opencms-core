/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.types;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;

import java.util.Locale;

import org.dom4j.Element;

/**
 * XML value type for display formatters.<p>
 */
public class CmsXmlDisplayFormatterValue extends A_CmsXmlValueTextBase {

    /** The value separator string. */
    public static final String SEPARATOR = ":";

    /** The XSD type name. */
    private static final String TYPE_NAME = "OpenCmsDisplayFormatter";

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsDisplayFormatter".<p>
     */
    public CmsXmlDisplayFormatterValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsDisplayFormatter".<p>
     *
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlDisplayFormatterValue(
        I_CmsXmlDocument document,
        Element element,
        Locale locale,
        I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsDisplayFormatter".<p>
     *
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlDisplayFormatterValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#createValue(org.opencms.xml.I_CmsXmlDocument, org.dom4j.Element, java.util.Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlDisplayFormatterValue(document, element, locale, this);
    }

    /**
     * Returns the display resource type name.<p>
     *
     * @return the display resource type
     */
    public String getDisplayType() {

        String value = getStringValue(null);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            return value.split(SEPARATOR)[0];
        }
        return null;
    }

    /**
     * Returns the formatter config id.<p>
     *
     * @return the formatter config id
     */
    public String getFormatterId() {

        String value = getStringValue(null);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            String[] parts = value.split(SEPARATOR);
            if (parts.length == 2) {
                return parts[1];
            }
        }
        return null;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        return "<xsd:simpleType name=\"" + TYPE_NAME + "\"><xsd:restriction base=\"xsd:string\" /></xsd:simpleType>";
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlValueTextBase#getStringValue(org.opencms.file.CmsObject)
     */
    @Override
    public String getStringValue(CmsObject cms) throws CmsRuntimeException {

        // always try to return the value with the formatter key (rather than id) if possible
        // (this matches the handling of options in the display formatter widget)

        I_CmsXmlDocument doc = getDocument();
        if ((doc != null) && (doc instanceof CmsXmlContent)) {
            CmsXmlContent content = (CmsXmlContent)doc;
            if (content.getFile() != null) {
                CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
                    cms,
                    content.getFile().getRootPath());
                String internalValue = super.getStringValue(cms);
                if (internalValue == null) {
                    return null;
                }
                int colonPos = internalValue.indexOf(':');
                if (colonPos == -1) {
                    return internalValue;
                }
                String keyOrId = internalValue.substring(colonPos + 1);
                I_CmsFormatterBean formatter = config.findFormatter(keyOrId);
                if (formatter != null) {
                    return internalValue.substring(0, colonPos + 1) + formatter.getKeyOrId();
                }
            }
        }
        return super.getStringValue(cms);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getTypeName()
     */
    public String getTypeName() {

        return TYPE_NAME;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlDisplayFormatterValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlValueTextBase#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public void setStringValue(CmsObject cms, String value) {

        I_CmsXmlDocument doc = getDocument();
        if (!CmsStringUtil.isEmpty(value)) {
            if ((doc != null) && (doc instanceof CmsXmlContent)) {
                CmsXmlContent content = (CmsXmlContent)doc;
                if (content.getFile() != null) {
                    CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
                        cms,
                        content.getFile().getRootPath());
                    int colonPos = value.indexOf(":");
                    if (colonPos > -1) {
                        String keyOrId = value.substring(colonPos + 1);
                        I_CmsFormatterBean formatter = config.findFormatter(keyOrId);
                        if (formatter != null) {
                            String newId = config.isUseFormatterKeys() ? formatter.getKeyOrId() : formatter.getId();
                            value = value.substring(0, colonPos) + ":" + newId;
                        }
                    }
                }
            }
        }
        super.setStringValue(cms, value);
    }

}
