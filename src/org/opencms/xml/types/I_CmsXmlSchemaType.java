/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/I_CmsXmlSchemaType.java,v $
 * Date   : $Date: 2005/02/17 12:45:12 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * Describes a type in an OpenCms XML schema based content definition.<p>
 *
 * A XML content definition in OpenCms basically consists of a sequence of
 * nodes in the following format:<p>
 * 
 * <code>&lt;xsd:element name="title" type="cmsStringType" minOccurs="0" maxOccurs="unbounded" default="Some text" /&gt;</code>.<p>
 *
 * Internally, each configured element in a XML schema is represented by an instance of 
 * this interface. This allows for constructing or changing the XML schema through the 
 * provided API.<p>
 * 
 * Note that this class only <i>describes the definition</i> of a value in the XML schema.
 * It is not the representation of an actual value from a XML file,
 * for this you need an instance of a {@link org.opencms.xml.types.I_CmsXmlContentValue}.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.16 $
 * @since 5.5.0
 * 
 * @see org.opencms.xml.types.I_CmsXmlContentValue
 */
public interface I_CmsXmlSchemaType extends Comparable {

    /** The schema instance namespace. */
    Namespace XSI_NAMESPACE = Namespace.get("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    /** Constant for the XML schema attribute "noNamespaceSchemaLocation" in the XML schema instance namespace. */
    QName XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION = QName.get("noNamespaceSchemaLocation", XSI_NAMESPACE);

    /**
     * Appends an XML representation of this schema type to the given XML element.<p>
     * 
     * This is used to dynamically build a XML schema from an instance of a
     * {@link org.opencms.xml.CmsXmlContentDefinition} class.<p>
     * 
     * @param root the element to append the XML to
     */
    void appendXmlSchema(Element root);

    /**
     * Creates a XML content value object for this type.<p>
     * 
     * @param document the XML content instance this value belongs to
     * @param element the XML element to create the value from
     * @param locale the locale to create the value for
     * 
     * @return the created XML content value object
     */
    I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale);

    /**
     * Appends an XML for a new, empty node of this schema type to the given root element.<p>
     * 
     * This is used to dynamically build a vaild XML content object from an initialized
     * {@link org.opencms.xml.CmsXmlContentDefinition} class.<p>
     * 
     * Important: This method can only be used during initialization of a XML content object,
     * not to add values to an already initialized XML content. To add values after initialization,
     * use {@link org.opencms.xml.content.CmsXmlContent#addValue(CmsObject, String, Locale, int)}.<p>
     * 
     * @param cms the current users OpenCms context
     * @param document the document the XML is generated for
     * @param root the element to append the XML to
     * @param locale the locale to generate the element default content for
     * 
     * @return the generated XML element
     */
    Element generateXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale);

    /**
     * Returns the content definition this schema type belongs to.<p> 
     * 
     * Note that for nested schemas, the content definition of a nested 
     * value is not neccessarily equal to the content defintion of the document
     * returned by {@link I_CmsXmlContentValue#getDocument()}. If the value belongs to a nested
     * content, then the content definiton of the value also is the nested 
     * content defintion.<p>
     * 
     * @return the content definition this schema type belongs to
     */
    CmsXmlContentDefinition getContentDefinition();

    /**
     * Returns the default value for a node of this type in the current schema.<p>
     * 
     * @param locale the locale to generate the default value for
     * 
     * @return the default value for a node of this type in the current schema
     * 
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDefault(CmsObject, I_CmsXmlSchemaType, Locale)
     */
    String getDefault(Locale locale);

    /**
     * Returns the XML element node name of this type in the current schema.<p>
     *
     * The XML element node name can be configured in the schema.
     * For example, the node name could be <code>"Title"</code>,
     * <code>"Teaser"</code> or <code>"Text"</code>. The XML schema controls 
     * what node names are allowed.<p> 
     *
     * @return the XML node name of this type in the current schema
     */
    String getElementName();

    /**
     * Returns the maximum occurences of this type in the current schema.<p>
     *
     * @return the maximum occurences of this type in the current schema
     */
    int getMaxOccurs();

    /**
     * Returns the minimum occurences of this type in the current schema.<p>
     *
     * @return the minimum occurences of this type in the current schema
     */
    int getMinOccurs();

    /**
     * Returns a String representation of the XML definition for this schema type.<p>  
     * 
     * @return a String representation of the XML definition for this schema type
     */
    String getSchemaDefinition();

    /**
     * Returns the schema type name.<p>
     *
     * By convention, a XML schema type name has the form 
     * <code>"OpenCms + ${name}"</code>. Examples are
     * <code>"OpenCmsString"</code> or <code>"OpenCmsBoolean"</code>.<p>
     * 
     * The schema type name is fixed by the implementation.<p> 
     *
     * @return the schema type name
     */
    String getTypeName();

    /**
     * Returns <code>true</code> if this is a simple type, or <code>false</code>
     * if this type is a nested schema.<p>
     * 
     * If a value is a nested schema, it must be an instance of {@link CmsXmlNestedContentDefinition}.<p> 
     * 
     * @return true if this is  a simple type, or false if this type is a nested schema
     * 
     * @see CmsXmlNestedContentDefinition
     */
    boolean isSimpleType();

    /**
     * Creates a new instance of this XML schema type initialized with the given values.<p>
     * 
     * @param name the name to use in the xml document
     * @param minOccurs minimum number of occurences
     * @param maxOccurs maximum number of occurences
     * 
     * @return a new instance of this XML content type initialized with the given values
     */
    I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs);

    /**
     * Sets the content definition this schema type belongs to.<p>
     * 
     * This is done automatically when the scheme type is added
     * to a content definition. Usually there is no need to call this 
     * method from the application.<p>
     * 
     * @param contentDefinition the content definition to set
     */
    void setContentDefinition(CmsXmlContentDefinition contentDefinition);

    /**
     * Sets the default value for a node of this type in the current schema.<p>
     * 
     * @param defaultValue the default value to set
     */
    void setDefault(String defaultValue);

    /**
     * Checks if a given value is valid according to the validation rule (regular expression) used for validation
     * of this schema type in the XML schema.<p>
     * 
     * To have a more refined validation according to the special requirements of the
     * content type, use custom validation rules in the appinfo which are
     * processed with {@link org.opencms.xml.content.I_CmsXmlContentHandler#resolveValidation(CmsObject, I_CmsXmlContentValue, org.opencms.xml.content.CmsXmlContentErrorHandler)}.<p>
     * 
     * @param value the value to validate
     * 
     * @return the validation rule (regular expression) used for this schema type in the XML schema
     * 
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#resolveValidation(CmsObject, I_CmsXmlContentValue, org.opencms.xml.content.CmsXmlContentErrorHandler)
     */
    boolean validateValue(String value);
}