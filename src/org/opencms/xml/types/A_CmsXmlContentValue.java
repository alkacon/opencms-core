/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/A_CmsXmlContentValue.java,v $
 * Date   : $Date: 2004/11/30 16:04:21 $
 * Version: $Revision: 1.11 $
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Base class for XML content value implementations.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.11 $
 * @since 5.5.0
 */
public abstract class A_CmsXmlContentValue implements I_CmsXmlContentValue {

    /** The default value for nodes of this value. */
    protected String m_defaultValue;

    /** The XML element node that contains this value. */
    protected Element m_element;

    /** The locale this value was generated for. */
    protected Locale m_locale;

    /** The maximum occurences of this value according to the parent schema. */
    protected int m_maxOccurs;

    /** The minimum occurences of this value according to the parent schema. */
    protected int m_minOccurs;

    /** The configured XML node name of this value. */
    protected String m_name;

    /**
     * Default constructor for a xml content type 
     * that initializes some internal values.<p> 
     */
    protected A_CmsXmlContentValue() {

        m_minOccurs = 0;
        m_maxOccurs = Integer.MAX_VALUE;
    }

    /**
     * Initializes the required members for this XML content value.<p>
     * 
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     */
    protected A_CmsXmlContentValue(Element element, Locale locale) {

        m_element = element;
        m_name = element.getName();
        m_locale = locale;
    }

    /**
     * Initializes the schema type descriptor values for this type descriptor.<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    protected A_CmsXmlContentValue(String name, String minOccurs, String maxOccurs) {

        m_name = name;
        m_minOccurs = 1;
        if (CmsStringUtil.isNotEmpty(minOccurs)) {
            try {
                m_minOccurs = Integer.valueOf(minOccurs).intValue();
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        m_maxOccurs = 1;
        if (CmsStringUtil.isNotEmpty(maxOccurs)) {
            if (CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_UNBOUNDED.equals(maxOccurs)) {
                m_maxOccurs = Integer.MAX_VALUE;
            } else {
                try {
                    m_maxOccurs = Integer.valueOf(maxOccurs).intValue();
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#appendDefaultXml(org.dom4j.Element, Locale)
     */
    public void appendDefaultXml(Element root, Locale locale) {

        Element element = root.addElement(getElementName());
        if (getDefault(locale) != null) {
            try {
                I_CmsXmlContentValue value = createValue(element, locale);
                value.setStringValue(getDefault(locale));
            } catch (CmsXmlException e) {
                // should not happen if default value is correct
                OpenCms.getLog(this).error("Invalid default value '" + getDefault(locale) + "' for XML content", e);
                element.clearContent();
            }
        }
    }

    /**
     * Appends an element XML representation of this type to the given root node.<p>
     * 
     * @param root the element to append the XML to
     */
    public void appendXmlSchema(Element root) {

        Element element = root.addElement(CmsXmlContentDefinition.XSD_NODE_ELEMENT);
        element.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_NAME, getElementName());
        element.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_TYPE, getTypeName());
        if ((getMinOccurs() > 1) || (getMinOccurs() == 0)) {
            element.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_MIN_OCCURS, String.valueOf(getMinOccurs()));
        }
        if (getMaxOccurs() > 1) {
            if (getMaxOccurs() == Integer.MAX_VALUE) {
                element.addAttribute(
                    CmsXmlContentDefinition.XSD_ATTRIBUTE_MAX_OCCURS,
                    CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_UNBOUNDED);
            } else {
                element.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_MAX_OCCURS, String.valueOf(getMaxOccurs()));
            }
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {

        if (!(o instanceof I_CmsXmlSchemaType)) {
            return 0;
        }
        return getTypeName().compareTo(((I_CmsXmlSchemaType)o).getTypeName());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (!(o instanceof I_CmsXmlSchemaType)) {
            return false;
        }
        I_CmsXmlSchemaType other = (I_CmsXmlSchemaType)o;
        return (getElementName().equals(other.getElementName())
            && getTypeName().equals(other.getTypeName())
            && (getMinOccurs() == other.getMinOccurs()) && (getMaxOccurs() == other.getMaxOccurs()));
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getDefault(java.util.Locale)
     */
    public String getDefault(Locale locale) {

        return m_defaultValue;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getElement()
     */
    public Element getElement() {

        return m_element;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getElementName() {

        return m_name;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getIndex()
     */
    public int getIndex() {

        return m_element.getParent().elements(m_element.getQName()).indexOf(m_element);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getLocale()
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the maximum occurences of this type.<p>
     *
     * @return the maximum occurences of this type
     */
    public int getMaxOccurs() {

        return m_maxOccurs;
    }

    /**
     * Returns the minimum occurences of this type.<p>
     *
     * @return the minimum occurences of this type
     */
    public int getMinOccurs() {

        return m_minOccurs;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getPlainText(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument)
     */
    public String getPlainText(CmsObject cms, I_CmsXmlDocument document) {

        return null;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return getTypeName().hashCode();
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#isSimpleType()
     */
    public boolean isSimpleType() {

        // the abstract base type should be used for simple types only
        return true;
    }

    /**
     * Sets the default value for a node of this type.<p>
     * 
     * @param defaultValue the default value to set
     */
    public void setDefault(String defaultValue) {

        m_defaultValue = defaultValue;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, java.lang.String)
     */
    public void setStringValue(CmsObject cms, I_CmsXmlDocument document, String value) throws CmsXmlException {

        // for most values, no special processing on the users OpenCms context is required
        setStringValue(value);
    }

    /**
     * Convenience method to loads the XML schema definition for this value type from an external file.<p>
     * 
     * @param schemaUri the schema uri to load the XML schema file from
     * 
     * @return the loaded XML schema
     */
    protected String readSchemaDefinition(String schemaUri) {

        // the schema definition is located in a separate file for easier editing
        String schemaDefinition;
        try {
            schemaDefinition = CmsFileUtil.readFile(schemaUri, CmsEncoder.C_UTF8_ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load external schema: " + schemaUri, e);
        }
        return schemaDefinition;
    }
}