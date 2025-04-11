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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.json.JSONArray;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.xml2json.I_CmsJsonFormattableValue;

import java.util.List;
import java.util.Locale;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsVfsFile".<p>
 *
 * This type allows links to internal VFS resources only.<p>
 *
 * @since 7.0.0
 */
public class CmsXmlDynamicCategoryValue extends A_CmsXmlContentValue implements I_CmsJsonFormattableValue {

    /** Temporary element used for storing the categories as a string in the XML for validation purposes. This is thrown out before the file is actually saved to the database. */
    public static final String N_CATEGORY_STRING = "category-string";

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsDynamicCategory";

    /** The schema definition String is located in a text for easier editing. */
    private static String m_schemaDefinition;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsCategoryValue".<p>
     */
    public CmsXmlDynamicCategoryValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsCategoryValue".<p>
     *
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlDynamicCategoryValue(
        I_CmsXmlDocument document,
        Element element,
        Locale locale,
        I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsCategoryValue".<p>
     *
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlDynamicCategoryValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlDynamicCategoryValue(document, element, locale, this);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#generateXml(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, org.dom4j.Element, java.util.Locale)
     */
    @Override
    public Element generateXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        Element element = root.addElement(getName());
        element.addComment("Categories are read dynamically");

        return element;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getPlainText(org.opencms.file.CmsObject)
     */
    @Override
    public String getPlainText(CmsObject cms) {

        return getStringValue(cms);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        if (m_schemaDefinition == null) {
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlDynamicCategoryValue.xsd");
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject)
     */
    public String getStringValue(CmsObject cms) throws CmsRuntimeException {

        Element categoryElement = categoryStringElem(false);
        if (categoryElement == null) {
            return "";
        }
        return categoryElement.getText();
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

        // there is no point in an empty node
        return false;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlDynamicCategoryValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        categoryStringElem(true).setText(value);
    }

    /**
     * Gets the category-string subelement, creating it if necessary.
     *
     * @param create if true, the category string element is created if it doesn't exist; if false, null is returned in that case.
     * @return the category-string subelement
     */
    Element categoryStringElem(boolean create) {

        Element result = m_element.element(N_CATEGORY_STRING);
        if ((result == null) && create) {
            result = m_element.addElement(N_CATEGORY_STRING);
            result.detach();
            m_element.elements().add(0, result);
        }
        return result;
    }

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonFormattableValue#toJson(org.opencms.file.CmsObject)
     */
    public Object toJson(CmsObject cms) {

        CmsXmlContent content = (CmsXmlContent)getDocument();
        JSONArray array = new JSONArray();
        try {
            CmsFile file = content.getFile();
            List<CmsCategory> categories = CmsCategoryService.getInstance().readResourceCategories(cms, file);
            for (CmsCategory cat : categories) {
                array.put(cat.getPath());
            }
            return array;
        } catch (Exception e) {
            return array;
        }
    }

}