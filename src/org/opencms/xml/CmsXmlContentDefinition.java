/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlContentDefinition.java,v $
 * Date   : $Date: 2011/03/23 14:53:13 $
 * Version: $Revision: 1.48 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlLocaleValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Describes the structure definition of an XML content object.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.48 $ 
 * 
 * @since 6.0.0 
 */
public class CmsXmlContentDefinition implements Cloneable {

    /** Constant for the XML schema attribute "mapto". */
    public static final String XSD_ATTRIBUTE_DEFAULT = "default";

    /** Constant for the XML schema attribute "elementFormDefault". */
    public static final String XSD_ATTRIBUTE_ELEMENT_FORM_DEFAULT = "elementFormDefault";

    /** Constant for the XML schema attribute "maxOccurs". */
    public static final String XSD_ATTRIBUTE_MAX_OCCURS = "maxOccurs";

    /** Constant for the XML schema attribute "minOccurs". */
    public static final String XSD_ATTRIBUTE_MIN_OCCURS = "minOccurs";

    /** Constant for the XML schema attribute "name". */
    public static final String XSD_ATTRIBUTE_NAME = "name";

    /** Constant for the XML schema attribute "schemaLocation". */
    public static final String XSD_ATTRIBUTE_SCHEMA_LOCATION = "schemaLocation";

    /** Constant for the XML schema attribute "type". */
    public static final String XSD_ATTRIBUTE_TYPE = "type";

    /** Constant for the XML schema attribute "use". */
    public static final String XSD_ATTRIBUTE_USE = "use";

    /** Constant for the XML schema attribute value "language". */
    public static final String XSD_ATTRIBUTE_VALUE_LANGUAGE = "language";

    /** Constant for the XML schema attribute value "optional". */
    public static final String XSD_ATTRIBUTE_VALUE_OPTIONAL = "optional";

    /** Constant for the XML schema attribute value "qualified". */
    public static final String XSD_ATTRIBUTE_VALUE_QUALIFIED = "qualified";

    /** Constant for the XML schema attribute value "required". */
    public static final String XSD_ATTRIBUTE_VALUE_REQUIRED = "required";

    /** Constant for the XML schema attribute value "unbounded". */
    public static final String XSD_ATTRIBUTE_VALUE_UNBOUNDED = "unbounded";

    /** Constant for the XML schema attribute value "0". */
    public static final String XSD_ATTRIBUTE_VALUE_ZERO = "0";

    /** The opencms default type definition include. */
    public static final String XSD_INCLUDE_OPENCMS = CmsXmlEntityResolver.OPENCMS_SCHEME + "opencms-xmlcontent.xsd";

    /** The schema definition namespace. */
    public static final Namespace XSD_NAMESPACE = Namespace.get("xsd", "http://www.w3.org/2001/XMLSchema");

    /** Constant for the "annotation" node in the XML schema namespace. */
    public static final QName XSD_NODE_ANNOTATION = QName.get("annotation", XSD_NAMESPACE);

    /** Constant for the "appinfo" node in the XML schema namespace. */
    public static final QName XSD_NODE_APPINFO = QName.get("appinfo", XSD_NAMESPACE);

    /** Constant for the "attribute" node in the XML schema namespace. */
    public static final QName XSD_NODE_ATTRIBUTE = QName.get("attribute", XSD_NAMESPACE);

    /** Constant for the "complexType" node in the XML schema namespace. */
    public static final QName XSD_NODE_COMPLEXTYPE = QName.get("complexType", XSD_NAMESPACE);

    /** Constant for the "element" node in the XML schema namespace. */
    public static final QName XSD_NODE_ELEMENT = QName.get("element", XSD_NAMESPACE);

    /** Constant for the "include" node in the XML schema namespace. */
    public static final QName XSD_NODE_INCLUDE = QName.get("include", XSD_NAMESPACE);

    /** Constant for the "schema" node in the XML schema namespace. */
    public static final QName XSD_NODE_SCHEMA = QName.get("schema", XSD_NAMESPACE);

    /** Constant for the "sequence" node in the XML schema namespace. */
    public static final QName XSD_NODE_SEQUENCE = QName.get("sequence", XSD_NAMESPACE);

    /** The XML content handler. */
    private I_CmsXmlContentHandler m_contentHandler;

    /** The set of included additional XML content definitions. */
    private Set<CmsXmlContentDefinition> m_includes;

    /** The inner element name of the content definition (type sequence). */
    private String m_innerName;

    /** The outer element name of the content definition (language sequence). */
    private String m_outerName;

    /** The location from which the XML schema was read (XML system id). */
    private String m_schemaLocation;

    /** The main type name of this XML content definition. */
    private String m_typeName;

    /** The Map of configured types. */
    private Map<String, I_CmsXmlSchemaType> m_types;

    /** The type sequence. */
    private List<I_CmsXmlSchemaType> m_typeSequence;

    /** The XML document from which the schema was unmarshalled. */
    private Document m_schemaDocument;

    /**
     * Creates a new XML content definition.<p> 
     * 
     * @param innerName the inner element name to use for the content definiton
     * @param schemaLocation the location from which the XML schema was read (system id)
     */
    public CmsXmlContentDefinition(String innerName, String schemaLocation) {

        // TODO: this constructor can be removed or made 'protected', schemas should be only created from files   
        this(innerName + "s", innerName, schemaLocation);
    }

    /**
     * Creates a new XML content definition.<p> 
     * 
     * @param outerName the outer element name to use for the content definition
     * @param innerName the inner element name to use for the content definition
     * @param schemaLocation the location from which the XML schema was read (system id)
     */
    public CmsXmlContentDefinition(String outerName, String innerName, String schemaLocation) {

        // TODO: this constructor can be removed or made 'protected', schemas should be only created from files   
        m_outerName = outerName;
        m_innerName = innerName;
        setInnerName(innerName);
        m_typeSequence = new ArrayList<I_CmsXmlSchemaType>();
        m_types = new HashMap<String, I_CmsXmlSchemaType>();
        m_includes = new HashSet<CmsXmlContentDefinition>();
        m_schemaLocation = schemaLocation;
        m_contentHandler = new CmsDefaultXmlContentHandler();
    }

    /**
     * Required empty constructor for clone operation.<p>
     */
    protected CmsXmlContentDefinition() {

        // noop, required for clone operation
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from a byte array
     * that contains XML data.<p>
     * 
     * @param xmlData the XML data in a byte array
     * @param schemaLocation the location from which the XML schema was read (system id)
     * @param resolver the XML entity resolver to use
     * 
     * @return a XML content definition instance unmarshalled from the byte array
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(byte[] xmlData, String schemaLocation, EntityResolver resolver)
    throws CmsXmlException {

        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            result = unmarshalInternal(CmsXmlUtils.unmarshalHelper(xmlData, resolver), schemaLocation, resolver);
        }
        return result;
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from the OpenCms VFS resource name.<p>
     * 
     * @param cms the current users CmsObject
     * @param resourcename the resource name to unmarshal the XML content definition from
     * 
     * @return a XML content definition instance unmarshalled from the VFS resource
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(CmsObject cms, String resourcename) throws CmsXmlException {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        String schemaLocation = CmsXmlEntityResolver.OPENCMS_SCHEME.concat(resourcename.substring(1));
        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            InputSource source = resolver.resolveEntity(null, schemaLocation);
            result = unmarshalInternal(CmsXmlUtils.unmarshalHelper(source, resolver), schemaLocation, resolver);
        }
        return result;
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from a XML document.<p>
     * 
     * This method does additional validation to ensure the document has the required
     * XML structure for a OpenCms content definition schema.<p>
     * 
     * @param document the XML document to generate a XML content definition from
     * @param schemaLocation the location from which the XML schema was read (system id)
     * 
     * @return a XML content definition instance unmarshalled from the XML document
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(Document document, String schemaLocation) throws CmsXmlException {

        EntityResolver resolver = document.getEntityResolver();
        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            result = unmarshalInternal(document, schemaLocation, resolver);
        }
        return result;
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from a XML InputSource.<p>
     * 
     * @param source the XML InputSource to use
     * @param schemaLocation the location from which the XML schema was read (system id)
     * @param resolver the XML entity resolver to use
     * 
     * @return a XML content definition instance unmarshalled from the InputSource
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(InputSource source, String schemaLocation, EntityResolver resolver)
    throws CmsXmlException {

        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            result = unmarshalInternal(CmsXmlUtils.unmarshalHelper(source, resolver), schemaLocation, resolver);
        }
        return result;
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from a given XML schema location.<p>
     * 
     * The XML content definition data to unmarshal will be read from the provided schema location using
     * an XML InputSource.<p>
     * 
     * @param schemaLocation the location from which to read the XML schema (system id)
     * @param resolver the XML entity resolver to use
     * 
     * @return a XML content definition instance unmarshalled from the InputSource
     * 
     * @throws CmsXmlException if something goes wrong
     * @throws SAXException if the XML schema location could not be converted to an XML InputSource
     * @throws IOException if the XML schema location could not be converted to an XML InputSource
     */
    public static CmsXmlContentDefinition unmarshal(String schemaLocation, EntityResolver resolver)
    throws CmsXmlException, SAXException, IOException {

        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            InputSource source = resolver.resolveEntity(null, schemaLocation);
            result = unmarshalInternal(CmsXmlUtils.unmarshalHelper(source, resolver), schemaLocation, resolver);
        }
        return result;
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from a String
     * that contains XML data.<p>
     * 
     * @param xmlData the XML data in a String
     * @param schemaLocation the location from which the XML schema was read (system id)
     * @param resolver the XML entitiy resolver to use
     * 
     * @return a XML content definition instance unmarshalled from the byte array
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(String xmlData, String schemaLocation, EntityResolver resolver)
    throws CmsXmlException {

        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            result = unmarshalInternal(CmsXmlUtils.unmarshalHelper(xmlData, resolver), schemaLocation, resolver);
        }
        return result;
    }

    /**
     * Creates the name of the type attribute from the given content name.<p>
     * 
     * @param name the name to use
     * 
     * @return the name of the type attribute
     */
    protected static String createTypeName(String name) {

        StringBuffer result = new StringBuffer(32);
        result.append("OpenCms");
        result.append(name.substring(0, 1).toUpperCase());
        if (name.length() > 1) {
            result.append(name.substring(1));
        }
        return result.toString();
    }

    /**
     * Validates if a given attribute exists at the given element with an (optional) specified value.<p>
     * 
     * If the required value is not <code>null</code>, the attribute must have exactly this 
     * value set.<p> 
     * 
     * If no value is required, some simple validation is performed on the attribute value,
     * like a check that the value does not have leading or trailing white spaces.<p>
     * 
     * @param element the element to validate
     * @param attributeName the attribute to check for
     * @param requiredValue the required value of the attribute, or <code>null</code> if any value is allowed
     * 
     * @return the value of the attribute
     * 
     * @throws CmsXmlException if the element does not have the required attribute set, or if the validation fails
     */
    protected static String validateAttribute(Element element, String attributeName, String requiredValue)
    throws CmsXmlException {

        Attribute attribute = element.attribute(attributeName);
        if (attribute == null) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_EL_MISSING_ATTRIBUTE_2,
                element.getUniquePath(),
                attributeName));
        }
        String value = attribute.getValue();

        if (requiredValue == null) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(value) || !value.equals(value.trim())) {
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_EL_BAD_ATTRIBUTE_WS_3,
                    element.getUniquePath(),
                    attributeName,
                    value));
            }
        } else {
            if (!requiredValue.equals(value)) {
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_EL_BAD_ATTRIBUTE_VALUE_4,
                    new Object[] {element.getUniquePath(), attributeName, requiredValue, value}));
            }
        }
        return value;
    }

    /**
     * Validates if a given element has exactly the required attributes set.<p>
     * 
     * @param element the element to validate
     * @param requiredAttributes the list of required attributes
     * @param optionalAttributes the list of optional attributes
     * 
     * @throws CmsXmlException if the validation fails 
     */
    protected static void validateAttributesExists(
        Element element,
        String[] requiredAttributes,
        String[] optionalAttributes) throws CmsXmlException {

        if (element.attributeCount() < requiredAttributes.length) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_EL_ATTRIBUTE_TOOFEW_3,
                element.getUniquePath(),
                new Integer(requiredAttributes.length),
                new Integer(element.attributeCount())));
        }

        if (element.attributeCount() > (requiredAttributes.length + optionalAttributes.length)) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_EL_ATTRIBUTE_TOOMANY_3,
                element.getUniquePath(),
                new Integer(requiredAttributes.length + optionalAttributes.length),
                new Integer(element.attributeCount())));
        }

        for (int i = 0; i < requiredAttributes.length; i++) {
            String attributeName = requiredAttributes[i];
            if (element.attribute(attributeName) == null) {
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_EL_MISSING_ATTRIBUTE_2,
                    element.getUniquePath(),
                    attributeName));
            }
        }

        List<String> rA = Arrays.asList(requiredAttributes);
        List<String> oA = Arrays.asList(optionalAttributes);

        for (int i = 0; i < element.attributes().size(); i++) {
            String attributeName = element.attribute(i).getName();
            if (!rA.contains(attributeName) && !oA.contains(attributeName)) {
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_EL_INVALID_ATTRIBUTE_2,
                    element.getUniquePath(),
                    attributeName));
            }
        }
    }

    /**
     * Validates the given element as a complex type sequence.<p>
     * 
     * @param element the element to validate
     * @param includes the XML schema includes
     * 
     * @return a data structure containing the validated complex type sequence data 
     * 
     * @throws CmsXmlException if the validation fails
     */
    protected static CmsXmlComplexTypeSequence validateComplexTypeSequence(
        Element element,
        Set<CmsXmlContentDefinition> includes) throws CmsXmlException {

        validateAttributesExists(element, new String[] {XSD_ATTRIBUTE_NAME}, new String[0]);

        String name = validateAttribute(element, XSD_ATTRIBUTE_NAME, null);

        // now check the type definition list
        List<Element> mainElements = CmsXmlGenericWrapper.elements(element);
        if ((mainElements.size() != 1) && (mainElements.size() != 2)) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_TS_SUBELEMENT_COUNT_2,
                element.getUniquePath(),
                new Integer(mainElements.size())));
        }

        boolean hasLanguageAttribute = false;
        if (mainElements.size() == 2) {
            // two elements in the master list: the second must be the "language" attribute definition

            Element typeAttribute = mainElements.get(1);
            if (!XSD_NODE_ATTRIBUTE.equals(typeAttribute.getQName())) {
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_CD_ELEMENT_NAME_3,
                    typeAttribute.getUniquePath(),
                    XSD_NODE_ATTRIBUTE.getQualifiedName(),
                    typeAttribute.getQName().getQualifiedName()));
            }
            validateAttribute(typeAttribute, XSD_ATTRIBUTE_NAME, XSD_ATTRIBUTE_VALUE_LANGUAGE);
            validateAttribute(typeAttribute, XSD_ATTRIBUTE_TYPE, CmsXmlLocaleValue.TYPE_NAME);
            try {
                validateAttribute(typeAttribute, XSD_ATTRIBUTE_USE, XSD_ATTRIBUTE_VALUE_REQUIRED);
            } catch (CmsXmlException e) {
                validateAttribute(typeAttribute, XSD_ATTRIBUTE_USE, XSD_ATTRIBUTE_VALUE_OPTIONAL);
            }
            // no error: then the language attribute is valid
            hasLanguageAttribute = true;
        }

        // check the main element type sequence
        Element typeSequence = mainElements.get(0);
        if (!XSD_NODE_SEQUENCE.equals(typeSequence.getQName())) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_CD_ELEMENT_NAME_3,
                typeSequence.getUniquePath(),
                XSD_NODE_SEQUENCE.getQualifiedName(),
                typeSequence.getQName().getQualifiedName()));
        }

        // check the type definition sequence
        List<Element> typeSequenceElements = CmsXmlGenericWrapper.elements(typeSequence);
        if (typeSequenceElements.size() < 1) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_TS_SUBELEMENT_TOOFEW_3,
                typeSequence.getUniquePath(),
                new Integer(1),
                new Integer(typeSequenceElements.size())));
        }

        // now add all type definitions from the schema
        List<I_CmsXmlSchemaType> sequence = new ArrayList<I_CmsXmlSchemaType>();

        if (hasLanguageAttribute) {
            // only generate types for sequence node with language attribute

            CmsXmlContentTypeManager typeManager = OpenCms.getXmlContentTypeManager();
            Iterator<Element> i = typeSequenceElements.iterator();
            while (i.hasNext()) {
                sequence.add(typeManager.getContentType(i.next(), includes));
            }
        } else {
            // generate a nested content definition for the main type sequence

            Element e = typeSequenceElements.get(0);
            String typeName = validateAttribute(e, XSD_ATTRIBUTE_NAME, null);
            String minOccurs = validateAttribute(e, XSD_ATTRIBUTE_MIN_OCCURS, XSD_ATTRIBUTE_VALUE_ZERO);
            String maxOccurs = validateAttribute(e, XSD_ATTRIBUTE_MAX_OCCURS, XSD_ATTRIBUTE_VALUE_UNBOUNDED);
            validateAttribute(e, XSD_ATTRIBUTE_TYPE, createTypeName(typeName));

            CmsXmlNestedContentDefinition cd = new CmsXmlNestedContentDefinition(null, typeName, minOccurs, maxOccurs);
            sequence.add(cd);
        }

        // return a data structure with the collected values
        return new CmsXmlComplexTypeSequence(name, sequence, hasLanguageAttribute);
    }

    /**
     * Looks up the given XML content definition system id in the internal content definition cache.<p> 
     * 
     * @param schemaLocation the system id of the XML content definition to look up
     * @param resolver the XML entity resolver to use (contains the cache)
     * 
     * @return the XML content definition found, or null if no definition is cached for the given system id
     */
    private static CmsXmlContentDefinition getCachedContentDefinition(String schemaLocation, EntityResolver resolver) {

        if (resolver instanceof CmsXmlEntityResolver) {
            // check for a cached version of this content definition
            CmsXmlEntityResolver cmsResolver = (CmsXmlEntityResolver)resolver;
            return cmsResolver.getCachedContentDefinition(schemaLocation);
        }
        return null;
    }

    /**
     * Internal method to unmarshal (read) a XML content definition instance from a XML document.<p>
     * 
     * It is assumed that the XML content definition cache has already been tested and the document 
     * has not been found in the cache. After the XML content definition has been successfully created, 
     * it is placed in the cache.<p>
     * 
     * @param document the XML document to generate a XML content definition from
     * @param schemaLocation the location from which the XML schema was read (system id)
     * @param resolver the XML entity resolver used by the given XML document
     * 
     * @return a XML content definition instance unmarshalled from the XML document
     * 
     * @throws CmsXmlException if something goes wrong
     */
    private static CmsXmlContentDefinition unmarshalInternal(
        Document document,
        String schemaLocation,
        EntityResolver resolver) throws CmsXmlException {

        // analyze the document and generate the XML content type definition        
        Element root = document.getRootElement();
        if (!XSD_NODE_SCHEMA.equals(root.getQName())) {
            // schema node is required
            throw new CmsXmlException(Messages.get().container(Messages.ERR_CD_NO_SCHEMA_NODE_0));
        }

        List<Element> includes = CmsXmlGenericWrapper.elements(root, XSD_NODE_INCLUDE);
        if (includes.size() < 1) {
            // one include is required
            throw new CmsXmlException(Messages.get().container(Messages.ERR_CD_ONE_INCLUDE_REQUIRED_0));
        }

        Element include = includes.get(0);
        String target = validateAttribute(include, XSD_ATTRIBUTE_SCHEMA_LOCATION, null);
        if (!XSD_INCLUDE_OPENCMS.equals(target)) {
            // the first include must point to the default OpenCms standard schema include
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_CD_FIRST_INCLUDE_2,
                XSD_INCLUDE_OPENCMS,
                target));
        }

        Set<CmsXmlContentDefinition> nestedDefinitions = new HashSet<CmsXmlContentDefinition>();
        if (includes.size() > 1) {
            // resolve additional, nested include calls
            for (int i = 1; i < includes.size(); i++) {

                Element inc = includes.get(i);
                String schemaLoc = validateAttribute(inc, XSD_ATTRIBUTE_SCHEMA_LOCATION, null);
                InputSource source = null;
                try {
                    source = resolver.resolveEntity(null, schemaLoc);
                } catch (Exception e) {
                    throw new CmsXmlException(Messages.get().container(Messages.ERR_CD_BAD_INCLUDE_1, schemaLoc));
                }
                CmsXmlContentDefinition xmlContentDefinition = unmarshal(source, schemaLoc, resolver);
                nestedDefinitions.add(xmlContentDefinition);
            }
        }

        List<Element> elements = CmsXmlGenericWrapper.elements(root, XSD_NODE_ELEMENT);
        if (elements.size() != 1) {
            // only one root element is allowed
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_CD_ROOT_ELEMENT_COUNT_1,
                XSD_INCLUDE_OPENCMS,
                new Integer(elements.size())));
        }

        // collect the data from the root element node
        Element main = elements.get(0);
        String name = validateAttribute(main, XSD_ATTRIBUTE_NAME, null);

        // now process the complex types
        List<Element> complexTypes = CmsXmlGenericWrapper.elements(root, XSD_NODE_COMPLEXTYPE);
        if (complexTypes.size() != 2) {
            // exactly two complex types are required
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_CD_COMPLEX_TYPE_COUNT_1,
                new Integer(complexTypes.size())));
        }

        // generate the result XML content definition
        CmsXmlContentDefinition result = new CmsXmlContentDefinition(name, null, schemaLocation);

        // set the nested definitions
        result.m_includes = nestedDefinitions;
        // set the schema document
        result.m_schemaDocument = document;

        List<CmsXmlComplexTypeSequence> complexTypeData = new ArrayList<CmsXmlComplexTypeSequence>();
        Iterator<Element> ct = complexTypes.iterator();
        while (ct.hasNext()) {
            Element e = ct.next();
            CmsXmlComplexTypeSequence sequence = validateComplexTypeSequence(e, nestedDefinitions);
            complexTypeData.add(sequence);
        }

        // get the outer element sequence, this must be the first element 
        CmsXmlComplexTypeSequence outerSequence = complexTypeData.get(0);
        CmsXmlNestedContentDefinition outer = (CmsXmlNestedContentDefinition)outerSequence.getSequence().get(0);

        // make sure the inner and outer element names are as required
        String outerTypeName = createTypeName(name);
        String innerTypeName = createTypeName(outer.getName());
        validateAttribute(complexTypes.get(0), XSD_ATTRIBUTE_NAME, outerTypeName);
        validateAttribute(complexTypes.get(1), XSD_ATTRIBUTE_NAME, innerTypeName);
        validateAttribute(main, XSD_ATTRIBUTE_TYPE, outerTypeName);

        // the inner name is the element name set in the outer sequence
        result.setInnerName(outer.getName());

        // get the inner element sequence, this must be the second element 
        CmsXmlComplexTypeSequence innerSequence = complexTypeData.get(1);

        // add the types from the main sequence node
        Iterator<I_CmsXmlSchemaType> it = innerSequence.getSequence().iterator();
        while (it.hasNext()) {
            result.addType(it.next());
        }

        // resolve the XML content handler information
        List<Element> annotations = CmsXmlGenericWrapper.elements(root, XSD_NODE_ANNOTATION);
        I_CmsXmlContentHandler contentHandler = null;
        Element appInfoElement = null;

        if (annotations.size() > 0) {
            List<Element> appinfos = CmsXmlGenericWrapper.elements(annotations.get(0), XSD_NODE_APPINFO);

            if (appinfos.size() > 0) {
                // the first appinfo node contains the specific XML content data 
                appInfoElement = appinfos.get(0);

                // check for a special content handler in the appinfo node
                Element handlerElement = appInfoElement.element("handler");
                if (handlerElement != null) {
                    String className = handlerElement.attributeValue("class");
                    if (className != null) {
                        contentHandler = OpenCms.getXmlContentTypeManager().getContentHandler(className, schemaLocation);
                    }
                }
            }
        }

        if (contentHandler == null) {
            // if no content handler is defined, the default handler is used
            contentHandler = OpenCms.getXmlContentTypeManager().getContentHandler(
                CmsDefaultXmlContentHandler.class.getName(),
                name);
        }

        // analyze the app info node with the selected XML content handler
        contentHandler.initialize(appInfoElement, result);
        result.m_contentHandler = contentHandler;

        result.freeze();

        if (resolver instanceof CmsXmlEntityResolver) {
            // put the generated content definition in the cache
            ((CmsXmlEntityResolver)resolver).cacheContentDefinition(schemaLocation, result);
        }

        return result;
    }

    /**
     * Adds the missing default XML according to this content definition to the given document element.<p>  
     * 
     * In case the root element already contains sub nodes, only missing sub nodes are added.<p>
     * 
     * @param cms the current users OpenCms context
     * @param document the document where the XML is added in (required for default XML generation)
     * @param root the root node to add the missing XML for
     * @param locale the locale to add the XML for
     * 
     * @return the given root element with the missing content added
     */
    public Element addDefaultXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        Iterator<I_CmsXmlSchemaType> i = m_typeSequence.iterator();
        int currentPos = 0;
        List<Element> allElements = CmsXmlGenericWrapper.elements(root);

        while (i.hasNext()) {
            I_CmsXmlSchemaType type = i.next();

            // check how many elements of this type already exist in the XML
            String elementName = type.getName();
            List<Element> elements = CmsXmlGenericWrapper.elements(root, elementName);

            currentPos += elements.size();
            for (int j = elements.size(); j < type.getMinOccurs(); j++) {
                // append the missing elements
                Element typeElement = type.generateXml(cms, document, root, locale);
                // need to check for default value again because the of appinfo "mappings" node
                I_CmsXmlContentValue value = type.createValue(document, typeElement, locale);
                String defaultValue = document.getContentDefinition().getContentHandler().getDefault(cms, value, locale);
                if (defaultValue != null) {
                    // only if there is a default value available use it to overwrite the initial default
                    value.setStringValue(cms, defaultValue);
                }

                // re-sort elements as they have been appended to the end of the XML root, not at the correct position
                typeElement.detach();
                allElements.add(currentPos, typeElement);
                currentPos++;
            }
        }

        return root;
    }

    /**
     * Adds a nested (included) XML content definition.<p>
     * 
     * @param nestedSchema the nested (included) XML content definition to add
     */
    public void addInclude(CmsXmlContentDefinition nestedSchema) {

        m_includes.add(nestedSchema);
    }

    /**
     * Adds the given content type.<p>
     * 
     * @param type the content type to add
     * 
     * @throws CmsXmlException in case an unregistered type is added
     */
    public void addType(I_CmsXmlSchemaType type) throws CmsXmlException {

        // check if the type to add actually exists in the type manager
        CmsXmlContentTypeManager typeManager = OpenCms.getXmlContentTypeManager();
        if (type.isSimpleType() && (typeManager.getContentType(type.getTypeName()) == null)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_UNREGISTERED_TYPE_1, type.getTypeName()));
        }

        // add the type to the internal type sequence and lookup table
        m_typeSequence.add(type);
        m_types.put(type.getName(), type);

        // store reference to the content definition in the type
        type.setContentDefinition(this);
    }

    /**
     * Creates a clone of this XML content definition.<p> 
     * 
     * @return a clone of this XML content definition
     */
    @Override
    public Object clone() {

        CmsXmlContentDefinition result = new CmsXmlContentDefinition();
        result.m_innerName = m_innerName;
        result.m_schemaLocation = m_schemaLocation;
        result.m_typeSequence = m_typeSequence;
        result.m_types = m_types;
        result.m_contentHandler = m_contentHandler;
        result.m_typeName = m_typeName;
        result.m_includes = m_includes;
        return result;
    }

    /**
     * Generates the default XML content for this content definition, and append it to the given root element.<p>
     * 
     * Please note: The default values for the annotations are read from the content definition of the given
     * document. For a nested content definitions, this means that all defaults are set in the annotations of the 
     * "outer" or "main" content definition.<p>
     * 
     * @param cms the current users OpenCms context
     * @param document the OpenCms XML document the XML is created for
     * @param root the node of the document where to append the generated XML to
     * @param locale the locale to create the default element in the document with
     * 
     * @return the default XML content for this content definition, and append it to the given root element
     */
    public Element createDefaultXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        Iterator<I_CmsXmlSchemaType> i = m_typeSequence.iterator();
        while (i.hasNext()) {
            I_CmsXmlSchemaType type = i.next();
            for (int j = 0; j < type.getMinOccurs(); j++) {
                Element typeElement = type.generateXml(cms, document, root, locale);
                // need to check for default value again because of the appinfo "mappings" node
                I_CmsXmlContentValue value = type.createValue(document, typeElement, locale);
                String defaultValue = document.getContentDefinition().getContentHandler().getDefault(cms, value, locale);
                if (defaultValue != null) {
                    // only if there is a default value available use it to overwrite the initial default
                    value.setStringValue(cms, defaultValue);
                }
            }
        }

        return root;
    }

    /**
     * Generates a valid XML document according to the XML schema of this content definition.<p>
     * 
     * @param cms the current users OpenCms context
     * @param document the OpenCms XML document the XML is created for
     * @param locale the locale to create the default element in the document with
     * 
     * @return a valid XML document according to the XML schema of this content definition
     */
    public Document createDocument(CmsObject cms, I_CmsXmlDocument document, Locale locale) {

        Document doc = DocumentHelper.createDocument();

        Element root = doc.addElement(getOuterName());

        root.add(I_CmsXmlSchemaType.XSI_NAMESPACE);
        root.addAttribute(I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION, getSchemaLocation());

        createLocale(cms, document, root, locale);
        return doc;
    }

    /**
     * Generates a valid locale (language) element for the XML schema of this content definition.<p>
     * 
     * @param cms the current users OpenCms context
     * @param document the OpenCms XML document the XML is created for
     * @param root the root node of the document where to append the locale to
     * @param locale the locale to create the default element in the document with
     * 
     * @return a valid XML element for the locale according to the XML schema of this content definition
     */
    public Element createLocale(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        // add an element with a "locale" attribute to the given root node
        Element element = root.addElement(getInnerName());
        element.addAttribute(XSD_ATTRIBUTE_VALUE_LANGUAGE, locale.toString());

        // now generate the default XML for the element
        return createDefaultXml(cms, document, element, locale);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CmsXmlContentDefinition)) {
            return false;
        }
        CmsXmlContentDefinition other = (CmsXmlContentDefinition)obj;
        if (!getInnerName().equals(other.getInnerName())) {
            return false;
        }
        if (!getOuterName().equals(other.getOuterName())) {
            return false;
        }
        return m_typeSequence.equals(other.m_typeSequence);
    }

    /**
     * Freezes this content definition, making all internal data structures
     * unmodifiable.<p>
     * 
     * This is required to prevent modification of a cached content definition.<p>
     */
    public void freeze() {

        m_types = Collections.unmodifiableMap(m_types);
        m_typeSequence = Collections.unmodifiableList(m_typeSequence);
    }

    /**
     * Returns the selected XML content handler for this XML content definition.<p>
     *
     * If no specific XML content handler was provided in the "appinfo" node of the
     * XML schema, the default XML content handler <code>{@link CmsDefaultXmlContentHandler}</code> is used.<p>
     *
     * @return the contentHandler
     */
    public I_CmsXmlContentHandler getContentHandler() {

        return m_contentHandler;
    }

    /**
     * Returns the set of nested (included) XML content definitions.<p>
     * 
     * @return the set of nested (included) XML content definitions
     */
    public Set<CmsXmlContentDefinition> getIncludes() {

        return m_includes;
    }

    /**
     * Returns the inner element name of this content definition.<p>
     *
     * @return the inner element name of this content definition
     */
    public String getInnerName() {

        return m_innerName;
    }

    /**
     * Returns the outer element name of this content definition.<p>
     *
     * @return the outer element name of this content definition
     */
    public String getOuterName() {

        return m_outerName;
    }

    /**
     * Generates an XML schema for the content definition.<p>
     * 
     * @return the generated XML schema
     */
    public Document getSchema() {

        Document result;

        if (m_schemaDocument == null) {
            result = DocumentHelper.createDocument();
            Element root = result.addElement(XSD_NODE_SCHEMA);
            root.addAttribute(XSD_ATTRIBUTE_ELEMENT_FORM_DEFAULT, XSD_ATTRIBUTE_VALUE_QUALIFIED);

            Element include = root.addElement(XSD_NODE_INCLUDE);
            include.addAttribute(XSD_ATTRIBUTE_SCHEMA_LOCATION, XSD_INCLUDE_OPENCMS);

            if (m_includes.size() > 0) {
                Iterator<CmsXmlContentDefinition> i = m_includes.iterator();
                while (i.hasNext()) {
                    CmsXmlContentDefinition definition = i.next();
                    root.addElement(XSD_NODE_INCLUDE).addAttribute(
                        XSD_ATTRIBUTE_SCHEMA_LOCATION,
                        definition.m_schemaLocation);
                }
            }

            String outerTypeName = createTypeName(getOuterName());
            String innerTypeName = createTypeName(getInnerName());

            Element content = root.addElement(XSD_NODE_ELEMENT);
            content.addAttribute(XSD_ATTRIBUTE_NAME, getOuterName());
            content.addAttribute(XSD_ATTRIBUTE_TYPE, outerTypeName);

            Element list = root.addElement(XSD_NODE_COMPLEXTYPE);
            list.addAttribute(XSD_ATTRIBUTE_NAME, outerTypeName);

            Element listSequence = list.addElement(XSD_NODE_SEQUENCE);
            Element listElement = listSequence.addElement(XSD_NODE_ELEMENT);
            listElement.addAttribute(XSD_ATTRIBUTE_NAME, getInnerName());
            listElement.addAttribute(XSD_ATTRIBUTE_TYPE, innerTypeName);
            listElement.addAttribute(XSD_ATTRIBUTE_MIN_OCCURS, XSD_ATTRIBUTE_VALUE_ZERO);
            listElement.addAttribute(XSD_ATTRIBUTE_MAX_OCCURS, XSD_ATTRIBUTE_VALUE_UNBOUNDED);

            Element main = root.addElement(XSD_NODE_COMPLEXTYPE);
            main.addAttribute(XSD_ATTRIBUTE_NAME, innerTypeName);

            Element mainSequence = main.addElement(XSD_NODE_SEQUENCE);

            Iterator<I_CmsXmlSchemaType> i = m_typeSequence.iterator();
            while (i.hasNext()) {
                I_CmsXmlSchemaType schemaType = i.next();
                schemaType.appendXmlSchema(mainSequence);
            }

            Element language = main.addElement(XSD_NODE_ATTRIBUTE);
            language.addAttribute(XSD_ATTRIBUTE_NAME, XSD_ATTRIBUTE_VALUE_LANGUAGE);
            language.addAttribute(XSD_ATTRIBUTE_TYPE, CmsXmlLocaleValue.TYPE_NAME);
            language.addAttribute(XSD_ATTRIBUTE_USE, XSD_ATTRIBUTE_VALUE_OPTIONAL);
        } else {
            result = (Document)m_schemaDocument.clone();
        }
        return result;
    }

    /**
     * Returns the location from which the XML schema was read (XML system id).<p>
     *
     * @return the location from which the XML schema was read (XML system id)
     */
    public String getSchemaLocation() {

        return m_schemaLocation;
    }

    /**
     * Returns the scheme type for the given element name, or <code>null</code> if no 
     * node is defined with this name.<p>
     * 
     * @param elementPath the element path to look up the type for
     * @return the type for the given element name, or <code>null</code> if no 
     *      node is defined with this name
     */
    public I_CmsXmlSchemaType getSchemaType(String elementPath) {

        String path = CmsXmlUtils.getFirstXpathElement(elementPath);

        I_CmsXmlSchemaType type = m_types.get(path);
        if (type == null) {
            // no node with the given path defined in schema
            return null;
        }

        // check if recursion is required to get value from a nested schema
        if (type.isSimpleType() || !CmsXmlUtils.isDeepXpath(elementPath)) {
            // no recursion required
            return type;
        }

        // recursion required since the path is an xpath and the type must be a nested content definition
        CmsXmlNestedContentDefinition nestedDefinition = (CmsXmlNestedContentDefinition)type;
        path = CmsXmlUtils.removeFirstXpathElement(elementPath);
        return nestedDefinition.getNestedContentDefinition().getSchemaType(path);
    }

    /**
     * Returns the internal set of schema type names.<p>
     * 
     * @return the internal set of schema type names
     */
    public Set<String> getSchemaTypes() {

        return m_types.keySet();
    }

    /**
     * Returns the main type name of this XML content definition.<p>
     * 
     * @return the main type name of this XML content definition
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * Returns the type sequence, contains instances of {@link I_CmsXmlSchemaType}.<p>
     *
     * @return the type sequence, contains instances of {@link I_CmsXmlSchemaType}
     */
    public List<I_CmsXmlSchemaType> getTypeSequence() {

        return m_typeSequence;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getInnerName().hashCode();
    }

    /**
     * Sets the inner element name to use for the content definition.<p>
     *
     * @param innerName the inner element name to set
     */
    protected void setInnerName(String innerName) {

        m_innerName = innerName;
        if (m_innerName != null) {
            m_typeName = createTypeName(innerName);
        }
    }

    /**
     * Sets the outer element name to use for the content definition.<p>
     *
     * @param outerName the outer element name to set
     */
    protected void setOuterName(String outerName) {

        m_outerName = outerName;
    }
}