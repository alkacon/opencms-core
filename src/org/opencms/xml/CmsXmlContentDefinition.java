/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlContentDefinition.java,v $
 * Date   : $Date: 2005/06/12 11:18:21 $
 * Version: $Revision: 1.26 $
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

package org.opencms.xml;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlLocaleValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

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

/**
 * Describes the structure definition of an XML content object.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.26 $
 * @since 5.5.0
 */
public class CmsXmlContentDefinition implements Cloneable {

    /**
     * Simple data structure to describe a type seqnence in an XML schema.<p>
     */
    private final class CmsXmlComplexTypeSequence {

        /** Indicates if this type sequence has a language attribute. */
        protected boolean m_hasLanguageAttribute;

        /** The name of the complex type seqnence. */
        protected String m_name;

        /** The type sequence elements. */
        protected List m_sequence;

        /**
         * Creates a new complex type sequence data structure.<p>
         * 
         * @param name the name of the sequence
         * @param sequence the type sequence element list
         * @param hasLanguageAttribute indicates if a "language" attribute is present
         */
        protected CmsXmlComplexTypeSequence(String name, List sequence, boolean hasLanguageAttribute) {

            m_name = name;
            m_sequence = sequence;
            m_hasLanguageAttribute = hasLanguageAttribute;
        }
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
    private Set m_includes;

    /** The inner element name of the content definition (type sequence). */
    private String m_innerName;

    /** The outer element name of the content definition (languange sequence). */
    private String m_outerName;

    /** The location from which the XML schema was read (XML system id). */
    private String m_schemaLocation;

    /** The main type name of this XML content definition. */
    private String m_typeName;

    /** The Map of configured types. */
    private Map m_types;

    /** The type sequence. */
    private List m_typeSequence;

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
     * @param outerName the outer element name to use for the content definiton
     * @param innerName the inner element name to use for the content definiton
     * @param schemaLocation the location from which the XML schema was read (system id)
     */
    public CmsXmlContentDefinition(String outerName, String innerName, String schemaLocation) {

        m_outerName = outerName;
        m_innerName = innerName;
        setInnerName(innerName);
        m_typeSequence = new ArrayList();
        m_types = new HashMap();
        m_includes = new HashSet();
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
     * @param resolver the XML entitiy resolver to use
     * @return a XML content definition instance unmarshalled from the byte array
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(byte[] xmlData, String schemaLocation, EntityResolver resolver)
    throws CmsXmlException {

        return unmarshal(CmsXmlUtils.unmarshalHelper(xmlData, resolver), schemaLocation);
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
        String systemId = CmsXmlEntityResolver.OPENCMS_SCHEME.concat(resourcename.substring(1));
        InputSource source = resolver.resolveEntity(null, systemId);

        return unmarshal(source, systemId, resolver);
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from a XML document.<p>
     * 
     * This method does additional validation to ensure the document has the required
     * XML structure for a OpenCms content definition schema.<p>
     * 
     * @param document the XML document to generate a XML content definition from
     * @param schemaLocation the location from which the XML schema was read (system id)
     * @return a XML content definition instance unmarshalled from the XML document
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(Document document, String schemaLocation) throws CmsXmlException {

        // analyze the document and generate the XML content type definition        
        Element root = document.getRootElement();
        if (!XSD_NODE_SCHEMA.equals(root.getQName())) {
            // schema node is required
            throw new CmsXmlException(Messages.get().container(Messages.ERR_CD_NO_SCHEMA_NODE_0));
        }

        List includes = root.elements(XSD_NODE_INCLUDE);
        if (includes.size() < 1) {
            // one include is required
            throw new CmsXmlException(Messages.get().container(Messages.ERR_CD_ONE_INCLUDE_REQUIRED_0));
        }

        Element include = (Element)includes.get(0);
        String target = validateAttribute(include, XSD_ATTRIBUTE_SCHEMA_LOCATION, null);
        if (!XSD_INCLUDE_OPENCMS.equals(target)) {
            // the first include must point to the default OpenCms standard schema include
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_CD_FIRST_INCLUDE_2,
                XSD_INCLUDE_OPENCMS,
                target));
        }

        Set nestedDefinitions = new HashSet();
        if (includes.size() > 1) {
            // resolve additional, nested include calls
            for (int i = 1; i < includes.size(); i++) {

                Element inc = (Element)includes.get(i);
                String schemaLoc = validateAttribute(inc, XSD_ATTRIBUTE_SCHEMA_LOCATION, null);
                EntityResolver resolver = document.getEntityResolver();
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

        List elements = root.elements(XSD_NODE_ELEMENT);
        if (elements.size() != 1) {
            // only one root element is allowed
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_CD_ROOT_ELEMENT_COUNT_1,
                XSD_INCLUDE_OPENCMS,
                new Integer(elements.size())));
        }

        // collect the data from the root element node
        Element main = (Element)elements.get(0);
        String name = validateAttribute(main, XSD_ATTRIBUTE_NAME, null);

        // now process the complex types
        List complexTypes = root.elements(XSD_NODE_COMPLEXTYPE);
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

        List complexTypeData = new ArrayList();
        Iterator ct = complexTypes.iterator();
        while (ct.hasNext()) {
            Element e = (Element)ct.next();
            CmsXmlComplexTypeSequence sequence = validateComplexTypeSequence(e, nestedDefinitions, result);
            complexTypeData.add(sequence);
        }

        // get the outer element sequence, this must be the first element 
        CmsXmlComplexTypeSequence outerSequence = (CmsXmlComplexTypeSequence)complexTypeData.get(0);
        CmsXmlNestedContentDefinition outer = (CmsXmlNestedContentDefinition)outerSequence.m_sequence.get(0);

        // make sure the inner and outer element names are as required
        String outerTypeName = createTypeName(name);
        String innerTypeName = createTypeName(outer.getName());
        validateAttribute((Element)complexTypes.get(0), XSD_ATTRIBUTE_NAME, outerTypeName);
        validateAttribute((Element)complexTypes.get(1), XSD_ATTRIBUTE_NAME, innerTypeName);
        validateAttribute(main, XSD_ATTRIBUTE_TYPE, outerTypeName);

        // the inner name is the element name set in the outer sequence
        result.setInnerName(outer.getName());

        // get the inner element sequence, this must be the second element 
        CmsXmlComplexTypeSequence innerSequence = (CmsXmlComplexTypeSequence)complexTypeData.get(1);

        // add the types from the main sequence node
        Iterator it = innerSequence.m_sequence.iterator();
        while (it.hasNext()) {
            result.addType((I_CmsXmlSchemaType)it.next());
        }

        // resolve the XML content handler information
        List annotations = root.elements(XSD_NODE_ANNOTATION);
        I_CmsXmlContentHandler contentHandler = null;
        Element appInfoElement = null;

        if (annotations.size() > 0) {
            List appinfos = ((Element)annotations.get(0)).elements(XSD_NODE_APPINFO);

            if (appinfos.size() > 0) {
                // the first appinfo node contains the specific XML content data 
                appInfoElement = (Element)appinfos.get(0);

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
        return result;
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from a XML InputSource.<p>
     * 
     * @param source the XML InputSource to use
     * @param schemaLocation the location from which the XML schema was read (system id)
     * @param resolver the XML entitiy resolver to use
     * @return a XML content definition instance unmarshalled from the InputSource
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(InputSource source, String schemaLocation, EntityResolver resolver)
    throws CmsXmlException {

        return unmarshal(CmsXmlUtils.unmarshalHelper(source, resolver), schemaLocation);
    }

    /**
     * Factory method to unmarshal (read) a XML content definition instance from a String
     * that contains XML data.<p>
     * 
     * @param xmlData the XML data in a String
     * @param schemaLocation the location from which the XML schema was read (system id)
     * @param resolver the XML entitiy resolver to use
     * @return a XML content definition instance unmarshalled from the byte array
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(String xmlData, String schemaLocation, EntityResolver resolver)
    throws CmsXmlException {

        return unmarshal(CmsXmlUtils.unmarshalHelper(xmlData, resolver), schemaLocation);
    }

    /**
     * Creates the name of the type attribute from the given content name.<p>
     * 
     * @param name the name to use
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
     * If the required value is not <code>null</code>, the attribute must have excatly this 
     * value set.<p> 
     * 
     * If no value is required, some simple validation is performed on the attribute value,
     * like a check that the value does not have leading or trainling white spaces.<p>
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
     * Validates if a gicen element has exactly the required attributes set.<p>
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

        List attributes = element.attributes();

        for (int i = 0; i < requiredAttributes.length; i++) {
            String attributeName = requiredAttributes[i];
            if (element.attribute(attributeName) == null) {
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_EL_MISSING_ATTRIBUTE_2,
                    element.getUniquePath(),
                    attributeName));
            }
        }

        List rA = Arrays.asList(requiredAttributes);
        List oA = Arrays.asList(optionalAttributes);

        for (int i = 0; i < attributes.size(); i++) {
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
     * @param definition the content definition the complex type seqnence belongs to 
     * 
     * @return a data structure containing the validated complex type seqnence data 
     * 
     * @throws CmsXmlException if the validation fails
     */
    protected static CmsXmlComplexTypeSequence validateComplexTypeSequence(
        Element element,
        Set includes,
        CmsXmlContentDefinition definition) throws CmsXmlException {

        validateAttributesExists(element, new String[] {XSD_ATTRIBUTE_NAME}, new String[0]);

        String name = validateAttribute(element, XSD_ATTRIBUTE_NAME, null);

        // now check the type definition list
        List mainElements = element.elements();
        if ((mainElements.size() != 1) && (mainElements.size() != 2)) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_TS_SUBELEMENT_COUNT_2,
                element.getUniquePath(),
                new Integer(mainElements.size())));
        }

        boolean hasLanguageAttribute = false;
        if (mainElements.size() == 2) {
            // two elements in the master list: the second must be the "language" attribute definition

            Element typeAttribute = (Element)mainElements.get(1);
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
        Element typeSequence = (Element)mainElements.get(0);
        if (!XSD_NODE_SEQUENCE.equals(typeSequence.getQName())) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_CD_ELEMENT_NAME_3,
                typeSequence.getUniquePath(),
                XSD_NODE_SEQUENCE.getQualifiedName(),
                typeSequence.getQName().getQualifiedName()));
        }

        // check the type definition sequence
        List typeSequenceElements = typeSequence.elements();
        if (typeSequenceElements.size() < 1) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_TS_SUBELEMENT_TOOFEW_3,
                typeSequence.getUniquePath(),
                new Integer(1),
                new Integer(typeSequenceElements.size())));
        }

        // now add all type definitions from the schema
        List sequence = new ArrayList();

        if (hasLanguageAttribute) {
            // only generate types for sequence node with language attribute

            CmsXmlContentTypeManager typeManager = OpenCms.getXmlContentTypeManager();
            Iterator i = typeSequenceElements.iterator();
            while (i.hasNext()) {
                sequence.add(typeManager.getContentType((Element)i.next(), includes));
            }
        } else {
            // generate a nested content definition for the main type sequence

            Element e = (Element)typeSequenceElements.get(0);
            String typeName = validateAttribute(e, XSD_ATTRIBUTE_NAME, null);
            String minOccurs = validateAttribute(e, XSD_ATTRIBUTE_MIN_OCCURS, XSD_ATTRIBUTE_VALUE_ZERO);
            String maxOccurs = validateAttribute(e, XSD_ATTRIBUTE_MAX_OCCURS, XSD_ATTRIBUTE_VALUE_UNBOUNDED);
            validateAttribute(e, XSD_ATTRIBUTE_TYPE, createTypeName(typeName));

            CmsXmlNestedContentDefinition cd = new CmsXmlNestedContentDefinition(null, typeName, minOccurs, maxOccurs);
            sequence.add(cd);
        }

        // return a data structure with the collected values
        return definition.new CmsXmlComplexTypeSequence(name, sequence, hasLanguageAttribute);
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
     * @throws CmsXmlException in case an unregisterd type is added
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

        Element root = doc.addElement(getInnerName() + "s");
        root.add(I_CmsXmlSchemaType.XSI_NAMESPACE);
        root.addAttribute(I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION, getSchemaLocation());

        createLocale(cms, document, root, locale);
        return doc;
    }

    /**
     * Generates a valid locale (language) element fot the XML schema of this content definition.<p>
     * 
     * @param cms the current users OpenCms context
     * @param document the OpenCms XML document the XML is created for
     * @param root the root node of the document where to append the locale to
     * @param locale the locale to create the default element in the document with
     * 
     * @return a valid XML element for the locale according to the XML schema of this content definition
     */
    public Element createLocale(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        Element element = root.addElement(getInnerName());
        element.addAttribute(XSD_ATTRIBUTE_VALUE_LANGUAGE, locale.toString());

        Iterator i = m_typeSequence.iterator();
        while (i.hasNext()) {
            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
            for (int j = 0; j < type.getMinOccurs(); j++) {
                type.generateXml(cms, document, element, locale);
            }
        }

        return element;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
    public synchronized void freeze() {

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
    public Set getIncludes() {

        return m_includes;
    }

    /**
     * Returns the inner element name of this content definiton.<p>
     *
     * @return the inner element name of this content definiton
     */
    public String getInnerName() {

        return m_innerName;
    }

    /**
     * Returns the outer element name of this content definiton.<p>
     *
     * @return the outer element name of this content definiton
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

        Document schema = DocumentHelper.createDocument();

        Element root = schema.addElement(XSD_NODE_SCHEMA);
        root.addAttribute(XSD_ATTRIBUTE_ELEMENT_FORM_DEFAULT, XSD_ATTRIBUTE_VALUE_QUALIFIED);

        Element include = root.addElement(XSD_NODE_INCLUDE);
        include.addAttribute(XSD_ATTRIBUTE_SCHEMA_LOCATION, XSD_INCLUDE_OPENCMS);

        if (m_includes.size() > 0) {
            Iterator i = m_includes.iterator();
            while (i.hasNext()) {
                CmsXmlContentDefinition definition = (CmsXmlContentDefinition)i.next();
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

        Iterator i = m_typeSequence.iterator();
        while (i.hasNext()) {
            I_CmsXmlSchemaType schemaType = (I_CmsXmlSchemaType)i.next();
            schemaType.appendXmlSchema(mainSequence);
        }

        Element language = main.addElement(XSD_NODE_ATTRIBUTE);
        language.addAttribute(XSD_ATTRIBUTE_NAME, XSD_ATTRIBUTE_VALUE_LANGUAGE);
        language.addAttribute(XSD_ATTRIBUTE_TYPE, CmsXmlLocaleValue.TYPE_NAME);
        language.addAttribute(XSD_ATTRIBUTE_USE, XSD_ATTRIBUTE_VALUE_REQUIRED);

        return schema;
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

        I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)m_types.get(path);
        if (type == null) {
            // no node with the given path defined in schema
            return null;
        }

        // check if recursion is required to get value from a nested schema
        if (type.isSimpleType() || !CmsXmlUtils.isDeepXpath(elementPath)) {
            // no recusion required
            return type;
        }

        // recusion required since the path is an Xpath
        CmsXmlNestedContentDefinition nestedDefinition = (CmsXmlNestedContentDefinition)type;
        path = CmsXmlUtils.removeFirstXpathElement(elementPath);
        return nestedDefinition.getNestedContentDefinition().getSchemaType(path);
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
     * Returns the type sequence.<p>
     *
     * @return the type sequence
     */
    public List getTypeSequence() {

        return m_typeSequence;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return getInnerName().hashCode();
    }

    /**
     * Sets the inner element name to use for the content definiton.<p>
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
     * Sets the outer element name to use for the content definiton.<p>
     *
     * @param outerName the outer element name to set
     */
    protected void setOuterName(String outerName) {

        m_outerName = outerName;
    }
}