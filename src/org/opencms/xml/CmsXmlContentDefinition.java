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

package org.opencms.xml;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlDynamicCategoryValue;
import org.opencms.xml.types.CmsXmlLocaleValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlStringValue;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

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
 * @since 6.0.0
 */
public class CmsXmlContentDefinition implements Cloneable {

    /**
     * Enumeration of possible sequence types in a content definition.
     */
    public enum SequenceType {
        /** A <code>xsd:choice</code> where the choice elements can appear more than once in a mix. */
        MULTIPLE_CHOICE,
        /** A simple <code>xsd:sequence</code>. */
        SEQUENCE,
        /** A <code>xsd:choice</code> where only one choice element can be selected. */
        SINGLE_CHOICE
    }

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

    /** Constant for the XML schema attribute value "1". */
    public static final String XSD_ATTRIBUTE_VALUE_ONE = "1";

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

    /** Constant for the "choice" node in the XML schema namespace. */
    public static final QName XSD_NODE_CHOICE = QName.get("choice", XSD_NAMESPACE);

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

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentDefinition.class);

    /** Null schema type value, required for map lookups. */
    private static final I_CmsXmlSchemaType NULL_SCHEMA_TYPE = new CmsXmlStringValue("NULL", "0", "0");

    /** Max occurs value for xsd:choice definitions. */
    private int m_choiceMaxOccurs;

    /** The XML content handler. */
    private I_CmsXmlContentHandler m_contentHandler;

    /** The Map of configured types indexed by the element xpath. */
    private Map<String, I_CmsXmlSchemaType> m_elementTypes;

    /** The set of included additional XML content definitions. */
    private Set<CmsXmlContentDefinition> m_includes;

    /** The inner element name of the content definition (type sequence). */
    private String m_innerName;

    /** The outer element name of the content definition (language sequence). */
    private String m_outerName;

    /** The XML document from which the schema was unmarshalled. */
    private Document m_schemaDocument;

    /** The location from which the XML schema was read (XML system id). */
    private String m_schemaLocation;

    /** Indicates the sequence type of this content definition. */
    private SequenceType m_sequenceType;

    /** The main type name of this XML content definition. */
    private String m_typeName;

    /** The Map of configured types. */
    private Map<String, I_CmsXmlSchemaType> m_types;

    /** The type sequence. */
    private List<I_CmsXmlSchemaType> m_typeSequence;

    /**
     * Creates a new XML content definition.<p>
     *
     * @param innerName the inner element name to use for the content definiton
     * @param schemaLocation the location from which the XML schema was read (system id)
     */
    public CmsXmlContentDefinition(String innerName, String schemaLocation) {

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

        m_outerName = outerName;
        m_innerName = innerName;
        setInnerName(innerName);
        m_typeSequence = new ArrayList<I_CmsXmlSchemaType>();
        m_types = new HashMap<String, I_CmsXmlSchemaType>();
        m_includes = new HashSet<CmsXmlContentDefinition>();
        m_schemaLocation = schemaLocation;
        m_contentHandler = new CmsDefaultXmlContentHandler();
        m_sequenceType = SequenceType.SEQUENCE;
        m_elementTypes = new ConcurrentHashMap<String, I_CmsXmlSchemaType>();
    }

    /**
     * Required empty constructor for clone operation.<p>
     */
    protected CmsXmlContentDefinition() {

        // noop, required for clone operation
    }

    /**
     * Factory method that returns the XML content definition instance for a given resource.<p>
     *
     * @param cms the cms-object
     * @param resource the resource
     *
     * @return the XML content definition
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsXmlContentDefinition getContentDefinitionForResource(CmsObject cms, CmsResource resource)
    throws CmsException {

        CmsXmlContentDefinition contentDef = null;
        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
        String schema = resType.getConfiguration().get(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
        if (schema != null) {
            try {
                // this wont in most cases read the file content because of caching
                contentDef = unmarshal(cms, schema);
            } catch (CmsException e) {
                // this should never happen, unless the configured schema is different than the schema in the XML
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        if (contentDef == null) {
            // could still be empty since it is not mandatory to configure the resource type in the XML configuration
            // try through the XSD relation
            List<CmsRelation> relations = cms.getRelationsForResource(
                resource,
                CmsRelationFilter.TARGETS.filterType(CmsRelationType.XSD));
            if ((relations != null) && !relations.isEmpty()) {
                CmsXmlEntityResolver entityResolver = new CmsXmlEntityResolver(cms);
                String xsd = cms.getSitePath(relations.get(0).getTarget(cms, CmsResourceFilter.ALL));
                contentDef = entityResolver.getCachedContentDefinition(xsd);
            }
        }
        if (contentDef == null) {
            // could still be empty if the XML content has been saved with an OpenCms before 8.0.0
            // so, to unmarshal is the only possibility left
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
            contentDef = content.getContentDefinition();
        }

        return contentDef;
    }

    /**
     * Reads the content definition which is configured for a resource type.<p>
     *
     * @param cms the current CMS context
     * @param typeName the type name
     *
     * @return the content definition
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsXmlContentDefinition getContentDefinitionForType(CmsObject cms, String typeName)
    throws CmsException {

        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(typeName);
        String schema = resType.getConfiguration().get(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
        CmsXmlContentDefinition contentDef = null;
        if (schema == null) {
            return null;
        }
        contentDef = unmarshal(cms, schema);
        return contentDef;
    }

    /**
     * Returns a content handler instance for the given resource.<p>
     *
     * @param cms the cms-object
     * @param resource the resource
     *
     * @return the content handler
     *
     * @throws CmsException if something goes wrong
     */
    public static I_CmsXmlContentHandler getContentHandlerForResource(CmsObject cms, CmsResource resource)
    throws CmsException {

        return getContentDefinitionForResource(cms, resource).getContentHandler();
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

        schemaLocation = translateSchema(schemaLocation);
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
        schemaLocation = translateSchema(schemaLocation);
        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            InputSource source = null;
            try {
                source = resolver.resolveEntity(null, schemaLocation);
                result = unmarshalInternal(CmsXmlUtils.unmarshalHelper(source, resolver), schemaLocation, resolver);
            } catch (IOException e) {
                throw new CmsXmlException(
                    Messages.get().container(
                        Messages.ERR_UNMARSHALLING_XML_SCHEMA_NOT_FOUND_2,
                        resourcename,
                        schemaLocation));
            }
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

        schemaLocation = translateSchema(schemaLocation);
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

        schemaLocation = translateSchema(schemaLocation);
        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            if (null == source) {
                throw new CmsXmlException(
                    Messages.get().container(
                        Messages.ERR_UNMARSHALLING_XML_DOC_1,
                        String.format("schemaLocation: '%s'. source: null!", schemaLocation)));
            }
            Document doc = CmsXmlUtils.unmarshalHelper(source, resolver);
            result = unmarshalInternal(doc, schemaLocation, resolver);
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

        schemaLocation = translateSchema(schemaLocation);
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
     * @param resolver the XML entity resolver to use
     *
     * @return a XML content definition instance unmarshalled from the byte array
     *
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(String xmlData, String schemaLocation, EntityResolver resolver)
    throws CmsXmlException {

        schemaLocation = translateSchema(schemaLocation);
        CmsXmlContentDefinition result = getCachedContentDefinition(schemaLocation, resolver);
        if (result == null) {
            // content definition was not found in the cache, unmarshal the XML document
            try {
                Document doc = CmsXmlUtils.unmarshalHelper(xmlData, resolver);
                result = unmarshalInternal(doc, schemaLocation, resolver);
            } catch (CmsXmlException e) {
                throw new CmsXmlException(
                    Messages.get().container(
                        Messages.ERR_UNMARSHALLING_XML_DOC_1,
                        String.format("schemaLocation: '%s'. xml: '%s'", schemaLocation, xmlData)),
                    e);
            }
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
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_EL_MISSING_ATTRIBUTE_2, element.getUniquePath(), attributeName));
        }
        String value = attribute.getValue();

        if (requiredValue == null) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(value) || !value.equals(value.trim())) {
                throw new CmsXmlException(
                    Messages.get().container(
                        Messages.ERR_EL_BAD_ATTRIBUTE_WS_3,
                        element.getUniquePath(),
                        attributeName,
                        value));
            }
        } else {
            if (!requiredValue.equals(value)) {
                throw new CmsXmlException(
                    Messages.get().container(
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
        String[] optionalAttributes)
    throws CmsXmlException {

        if (element.attributeCount() < requiredAttributes.length) {
            throw new CmsXmlException(
                Messages.get().container(
                    Messages.ERR_EL_ATTRIBUTE_TOOFEW_3,
                    element.getUniquePath(),
                    Integer.valueOf(requiredAttributes.length),
                    Integer.valueOf(element.attributeCount())));
        }

        if (element.attributeCount() > (requiredAttributes.length + optionalAttributes.length)) {
            throw new CmsXmlException(
                Messages.get().container(
                    Messages.ERR_EL_ATTRIBUTE_TOOMANY_3,
                    element.getUniquePath(),
                    Integer.valueOf(requiredAttributes.length + optionalAttributes.length),
                    Integer.valueOf(element.attributeCount())));
        }

        for (int i = 0; i < requiredAttributes.length; i++) {
            String attributeName = requiredAttributes[i];
            if (element.attribute(attributeName) == null) {
                throw new CmsXmlException(
                    Messages.get().container(
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
                throw new CmsXmlException(
                    Messages.get().container(
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
        Set<CmsXmlContentDefinition> includes)
    throws CmsXmlException {

        validateAttributesExists(element, new String[] {XSD_ATTRIBUTE_NAME}, new String[0]);

        String name = validateAttribute(element, XSD_ATTRIBUTE_NAME, null);

        // now check the type definition list
        List<Element> mainElements = CmsXmlGenericWrapper.elements(element);
        List<Element> attributes = mainElements.stream().filter(
            elem -> XSD_NODE_ATTRIBUTE.equals(elem.getQName())).collect(Collectors.toList());

        boolean hasLanguageAttribute = false;

        // two elements in the master list: the second must be the "language" attribute definition

        Element languageAttribute = attributes.stream().filter(
            elem -> elem.attribute(XSD_ATTRIBUTE_NAME).getValue().equals(
                XSD_ATTRIBUTE_VALUE_LANGUAGE)).findFirst().orElse(null);
        if (languageAttribute != null) {

            validateAttribute(languageAttribute, XSD_ATTRIBUTE_TYPE, CmsXmlLocaleValue.TYPE_NAME);
            try {
                validateAttribute(languageAttribute, XSD_ATTRIBUTE_USE, XSD_ATTRIBUTE_VALUE_REQUIRED);
            } catch (CmsXmlException e) {
                validateAttribute(languageAttribute, XSD_ATTRIBUTE_USE, XSD_ATTRIBUTE_VALUE_OPTIONAL);
            }
            // no error: then the language attribute is valid
            hasLanguageAttribute = true;
        }

        // the type of the sequence
        SequenceType sequenceType;
        int choiceMaxOccurs = 0;

        // check the main element type sequence
        Element typeSequenceElement = mainElements.get(0);
        if (!XSD_NODE_SEQUENCE.equals(typeSequenceElement.getQName())) {
            if (!XSD_NODE_CHOICE.equals(typeSequenceElement.getQName())) {
                throw new CmsXmlException(
                    Messages.get().container(
                        Messages.ERR_CD_ELEMENT_NAME_4,
                        new Object[] {
                            typeSequenceElement.getUniquePath(),
                            XSD_NODE_SEQUENCE.getQualifiedName(),
                            XSD_NODE_CHOICE.getQualifiedName(),
                            typeSequenceElement.getQName().getQualifiedName()}));
            } else {
                // this is a xsd:choice, check if this is single or multiple choice
                String minOccursStr = typeSequenceElement.attributeValue(XSD_ATTRIBUTE_MIN_OCCURS);
                int minOccurs = 1;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(minOccursStr)) {
                    try {
                        minOccurs = Integer.parseInt(minOccursStr.trim());
                    } catch (NumberFormatException e) {
                        throw new CmsXmlException(
                            Messages.get().container(
                                Messages.ERR_EL_BAD_ATTRIBUTE_3,
                                element.getUniquePath(),
                                XSD_ATTRIBUTE_MIN_OCCURS,
                                minOccursStr == null ? "1" : minOccursStr));
                    }
                }
                String maxOccursStr = typeSequenceElement.attributeValue(XSD_ATTRIBUTE_MAX_OCCURS);
                choiceMaxOccurs = 1;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(maxOccursStr)) {
                    if (CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_UNBOUNDED.equals(maxOccursStr.trim())) {
                        choiceMaxOccurs = Integer.MAX_VALUE;
                    } else {
                        try {
                            choiceMaxOccurs = Integer.parseInt(maxOccursStr.trim());
                        } catch (NumberFormatException e) {
                            throw new CmsXmlException(
                                Messages.get().container(
                                    Messages.ERR_EL_BAD_ATTRIBUTE_3,
                                    element.getUniquePath(),
                                    XSD_ATTRIBUTE_MAX_OCCURS,
                                    maxOccursStr));
                        }
                    }
                }
                if ((minOccurs == 0) && (choiceMaxOccurs == 1)) {
                    // minOccurs 0 and maxOccurs 1, this is a single choice sequence
                    sequenceType = SequenceType.SINGLE_CHOICE;
                } else {
                    // this is a multiple choice sequence
                    if (minOccurs > choiceMaxOccurs) {
                        throw new CmsXmlException(
                            Messages.get().container(
                                Messages.ERR_EL_BAD_ATTRIBUTE_3,
                                element.getUniquePath(),
                                XSD_ATTRIBUTE_MIN_OCCURS,
                                minOccursStr == null ? "1" : minOccursStr));
                    }
                    sequenceType = SequenceType.MULTIPLE_CHOICE;
                }
            }
        } else {
            // this is a simple sequence
            sequenceType = SequenceType.SEQUENCE;
        }

        // check the type definition sequence
        List<Element> typeSequenceElements = CmsXmlGenericWrapper.elements(typeSequenceElement);
        if (typeSequenceElements.size() < 1) {
            throw new CmsXmlException(
                Messages.get().container(
                    Messages.ERR_TS_SUBELEMENT_TOOFEW_3,
                    typeSequenceElement.getUniquePath(),
                    Integer.valueOf(1),
                    Integer.valueOf(typeSequenceElements.size())));
        }

        // now add all type definitions from the schema
        List<I_CmsXmlSchemaType> sequence = new ArrayList<I_CmsXmlSchemaType>();

        if (hasLanguageAttribute) {
            // only generate types for sequence node with language attribute

            CmsXmlContentTypeManager typeManager = OpenCms.getXmlContentTypeManager();
            Iterator<Element> i = typeSequenceElements.iterator();
            while (i.hasNext()) {
                Element typeElement = i.next();
                if (sequenceType != SequenceType.SEQUENCE) {
                    // in case of xsd:choice, need to make sure "minOccurs" for all type elements is 0
                    String minOccursStr = typeElement.attributeValue(XSD_ATTRIBUTE_MIN_OCCURS);
                    int minOccurs = 1;
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(minOccursStr)) {
                        try {
                            minOccurs = Integer.parseInt(minOccursStr.trim());
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                    // minOccurs must be "0"
                    if (minOccurs != 0) {
                        throw new CmsXmlException(
                            Messages.get().container(
                                Messages.ERR_EL_BAD_ATTRIBUTE_3,
                                typeElement.getUniquePath(),
                                XSD_ATTRIBUTE_MIN_OCCURS,
                                minOccursStr == null ? "1" : minOccursStr));
                    }
                }
                // create the type with the type manager
                I_CmsXmlSchemaType type = typeManager.getContentType(typeElement, includes);

                if (type.getTypeName().equals(CmsXmlDynamicCategoryValue.TYPE_NAME)
                    && ((type.getMaxOccurs() > 1) || (type.getMinOccurs() > 1))) {
                    throw new CmsXmlException(
                        Messages.get().container(
                            Messages.ERR_EL_OF_TYPE_MUST_OCCUR_AT_MOST_ONCE_2,
                            typeElement.getUniquePath(),
                            type.getTypeName()));
                }

                if (sequenceType == SequenceType.MULTIPLE_CHOICE) {
                    // if this is a multiple choice sequence,
                    // all elements must have "minOccurs" 0 or 1 and "maxOccurs" of 1
                    if ((type.getMinOccurs() < 0) || (type.getMinOccurs() > 1) || (type.getMaxOccurs() != 1)) {
                        throw new CmsXmlException(
                            Messages.get().container(
                                Messages.ERR_EL_BAD_ATTRIBUTE_3,
                                typeElement.getUniquePath(),
                                XSD_ATTRIBUTE_MAX_OCCURS,
                                typeElement.attributeValue(XSD_ATTRIBUTE_MAX_OCCURS)));
                    }
                }
                sequence.add(type);
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
        return new CmsXmlComplexTypeSequence(name, sequence, hasLanguageAttribute, sequenceType, choiceMaxOccurs);
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
     * Translates the XSD schema location.<p>
     *
     * @param schemaLocation the location to translate
     *
     * @return the translated schema location
     */
    private static String translateSchema(String schemaLocation) {

        if (OpenCms.getRepositoryManager() != null) {
            return OpenCms.getResourceManager().getXsdTranslator().translateResource(schemaLocation);
        }
        return schemaLocation;
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
        EntityResolver resolver)
    throws CmsXmlException {

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
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_CD_FIRST_INCLUDE_2, XSD_INCLUDE_OPENCMS, target));
        }

        boolean recursive = false;
        Set<CmsXmlContentDefinition> nestedDefinitions = new HashSet<CmsXmlContentDefinition>();
        if (includes.size() > 1) {
            // resolve additional, nested include calls
            for (int i = 1; i < includes.size(); i++) {

                Element inc = includes.get(i);
                String schemaLoc = validateAttribute(inc, XSD_ATTRIBUTE_SCHEMA_LOCATION, null);
                if (!(schemaLoc.equals(schemaLocation))) {
                    InputSource source = null;
                    try {
                        source = resolver.resolveEntity(null, schemaLoc);
                    } catch (Exception e) {
                        throw new CmsXmlException(
                            Messages.get().container(
                                Messages.ERR_CD_BAD_INCLUDE_3,
                                schemaLoc,
                                schemaLocation,
                                document.asXML()),
                            e);
                    }
                    // Couldn't resolve the entity?
                    if (null == source) {
                        throw new CmsXmlException(
                            Messages.get().container(
                                Messages.ERR_CD_BAD_INCLUDE_3,
                                schemaLoc,
                                schemaLocation,
                                document.asXML()));
                    }
                    CmsXmlContentDefinition xmlContentDefinition = unmarshal(source, schemaLoc, resolver);
                    nestedDefinitions.add(xmlContentDefinition);
                } else {
                    // recursion
                    recursive = true;
                }
            }
        }

        List<Element> elements = CmsXmlGenericWrapper.elements(root, XSD_NODE_ELEMENT);
        if (elements.size() != 1) {
            // only one root element is allowed
            throw new CmsXmlException(
                Messages.get().container(
                    Messages.ERR_CD_ROOT_ELEMENT_COUNT_1,
                    XSD_INCLUDE_OPENCMS,
                    Integer.valueOf(elements.size())));
        }

        // collect the data from the root element node
        Element main = elements.get(0);
        String name = validateAttribute(main, XSD_ATTRIBUTE_NAME, null);

        // now process the complex types
        List<Element> complexTypes = CmsXmlGenericWrapper.elements(root, XSD_NODE_COMPLEXTYPE);
        if (complexTypes.size() != 2) {
            // exactly two complex types are required
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_CD_COMPLEX_TYPE_COUNT_1, Integer.valueOf(complexTypes.size())));
        }

        // get the outer element sequence, this must be the first element
        CmsXmlComplexTypeSequence outerSequence = validateComplexTypeSequence(complexTypes.get(0), nestedDefinitions);
        CmsXmlNestedContentDefinition outer = (CmsXmlNestedContentDefinition)outerSequence.getSequence().get(0);

        // make sure the inner and outer element names are as required
        String outerTypeName = createTypeName(name);
        String innerTypeName = createTypeName(outer.getName());
        validateAttribute(complexTypes.get(0), XSD_ATTRIBUTE_NAME, outerTypeName);
        validateAttribute(complexTypes.get(1), XSD_ATTRIBUTE_NAME, innerTypeName);
        validateAttribute(main, XSD_ATTRIBUTE_TYPE, outerTypeName);

        // generate the result XML content definition
        CmsXmlContentDefinition result = new CmsXmlContentDefinition(name, null, schemaLocation);

        // set the nested definitions
        result.m_includes = nestedDefinitions;
        // set the schema document
        result.m_schemaDocument = document;

        // the inner name is the element name set in the outer sequence
        result.setInnerName(outer.getName());
        if (recursive) {
            nestedDefinitions.add(result);
        }

        // get the inner element sequence, this must be the second element
        CmsXmlComplexTypeSequence innerSequence = validateComplexTypeSequence(complexTypes.get(1), nestedDefinitions);

        // add the types from the main sequence node
        Iterator<I_CmsXmlSchemaType> it = innerSequence.getSequence().iterator();
        while (it.hasNext()) {
            result.addType(it.next());
        }

        // store if this content definition contains a xsd:choice sequence
        result.m_sequenceType = innerSequence.getSequenceType();
        result.m_choiceMaxOccurs = innerSequence.getChoiceMaxOccurs();

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
                        contentHandler = OpenCms.getXmlContentTypeManager().getFreshContentHandler(className);
                    }
                }
            }
        }

        if (contentHandler == null) {
            // if no content handler is defined, the default handler is used
            contentHandler = OpenCms.getXmlContentTypeManager().getFreshContentHandler(
                CmsDefaultXmlContentHandler.class.getName());
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
                String defaultValue = document.getHandler().getDefault(cms, value, locale);
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
        result.m_sequenceType = m_sequenceType;
        result.m_choiceMaxOccurs = m_choiceMaxOccurs;
        result.m_elementTypes = m_elementTypes;
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
                String defaultValue = document.getHandler().getDefault(cms, value, locale);
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
        int version = getVersion();
        if (version != 0) {
            root.addAttribute(CmsXmlContent.A_VERSION, "" + version);
        }
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
     * Iterates over all schema types along a given xpath, starting from a root content definition.<p>
     *
     * @param path the path
     * @param consumer a handler that consumes both the schema type and the remaining suffix of the path, relative to the schema type
     *
     * @return true if for all path components a schema type could be found
     */
    public boolean findSchemaTypesForPath(String path, BiConsumer<I_CmsXmlSchemaType, String> consumer) {

        path = CmsXmlUtils.removeAllXpathIndices(path);
        List<String> pathComponents = CmsXmlUtils.splitXpath(path);
        List<I_CmsXmlSchemaType> result = new ArrayList<>();
        CmsXmlContentDefinition currentContentDef = this;
        for (int i = 0; i < pathComponents.size(); i++) {
            String pathComponent = pathComponents.get(i);

            if (currentContentDef == null) {
                return false;
            }
            I_CmsXmlSchemaType schemaType = currentContentDef.getSchemaType(pathComponent);
            if (schemaType == null) {
                return false;
            } else {
                String remainingPath = CmsStringUtil.listAsString(
                    pathComponents.subList(i + 1, pathComponents.size()),
                    "/");
                consumer.accept(schemaType, remainingPath);
                result.add(schemaType);
                if (schemaType instanceof CmsXmlNestedContentDefinition) {
                    currentContentDef = ((CmsXmlNestedContentDefinition)schemaType).getNestedContentDefinition();
                } else {
                    currentContentDef = null; //
                }
            }
        }
        return true;
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
     * Returns the maxOccurs value for the choice in case this is a <code>xsd:choice</code> content definition.<p>
     *
     * This content definition is a <code>xsd:choice</code> sequence if the returned value is larger then 0.<p>
     *
     * @return the maxOccurs value for the choice in case this is a <code>xsd:choice</code> content definition
     */
    public int getChoiceMaxOccurs() {

        return m_choiceMaxOccurs;
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

            Element mainSequence;
            if (m_sequenceType == SequenceType.SEQUENCE) {
                mainSequence = main.addElement(XSD_NODE_SEQUENCE);
            } else {
                mainSequence = main.addElement(XSD_NODE_CHOICE);
                if (getChoiceMaxOccurs() > 1) {
                    mainSequence.addAttribute(XSD_ATTRIBUTE_MAX_OCCURS, String.valueOf(getChoiceMaxOccurs()));
                } else {
                    mainSequence.addAttribute(XSD_ATTRIBUTE_MIN_OCCURS, XSD_ATTRIBUTE_VALUE_ZERO);
                    mainSequence.addAttribute(XSD_ATTRIBUTE_MAX_OCCURS, XSD_ATTRIBUTE_VALUE_ONE);
                }
            }

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
     * Returns the schema type for the given element name, or <code>null</code> if no
     * node is defined with this name.<p>
     *
     * @param elementPath the element xpath to look up the type for
     * @return the type for the given element name, or <code>null</code> if no
     *      node is defined with this name
     */
    public I_CmsXmlSchemaType getSchemaType(String elementPath) {

        String path = CmsXmlUtils.removeXpath(elementPath);
        I_CmsXmlSchemaType result = m_elementTypes.get(path);
        if (result == null) {
            result = getSchemaTypeRecusive(path);
            if (result != null) {
                m_elementTypes.put(path, result);
            } else {
                m_elementTypes.put(path, NULL_SCHEMA_TYPE);
            }
        } else if (result == NULL_SCHEMA_TYPE) {
            result = null;
        }
        return result;
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
     * Returns the sequence type of this content definition.<p>
     *
     * @return the sequence type of this content definition
     */
    public SequenceType getSequenceType() {

        return m_sequenceType;
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
     * Gets the version.
     *
     * @return the version number
     */
    public int getVersion() {

        return CmsXmlUtils.getSchemaVersion(m_schemaDocument);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getInnerName().hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return CmsXmlContentDefinition.class.getSimpleName() + " " + m_schemaLocation;
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

    /**
     * Calculates the schema type for the given element name by recursing into the schema structure.<p>
     *
     * @param elementPath the element xpath to look up the type for
     * @return the type for the given element name, or <code>null</code> if no
     *      node is defined with this name
     */
    private I_CmsXmlSchemaType getSchemaTypeRecusive(String elementPath) {

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

}
