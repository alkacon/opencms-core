/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlContentDefinition.java,v $
 * Date   : $Date: 2004/09/27 17:13:03 $
 * Version: $Revision: 1.2 $
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
package org.opencms.xml;

import org.opencms.xml.types.CmsXmlLocaleValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Describes the structure of an xml content object.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.5.0
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
    
    /** Constant for the XML schema attribute value "qualified". */
    public static final String XSD_ATTRIBUTE_VALUE_QUALIFIED = "qualified";       
    
    /** Constant for the XML schema attribute value "required". */
    public static final String XSD_ATTRIBUTE_VALUE_REQUIRED = "required";  
    
    /** Constant for the XML schema attribute value "unbounded". */
    public static final String XSD_ATTRIBUTE_VALUE_UNBOUNDED = "unbounded";
    
    /** Constant for the XML schema attribute value "0". */
    public static final String XSD_ATTRIBUTE_VALUE_ZERO = "0";
    
    /** The opencms default type definition include. */
    public static final String XSD_INCLUDE_OPENCMS = CmsXmlEntityResolver.C_OPENCMS_SCHEME + "opencms-xmlcontent.xsd";        
    
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
   
    /** The element mappings (defined in the annotations). */
    private Map m_elementMappings;
    
    /** The name of the content definition. */
    private String m_name;
    
    /** The location from which the XML schema was read (XML system id). */
    private String m_schemaLocation;
    
    /** The Map of configured types. */
    private Map m_types;
    
    /** The type sequence. */
    private List m_typeSequence;
    
    /**
     * Creates a new XML content definition.<p> 
     * 
     * @param name the name to use for the content type
     * @param schemaLocation the location from which the XML schema was read (system id)
     */    
    public CmsXmlContentDefinition(String name, String schemaLocation) {
        
        m_name = name;
        m_typeSequence = new ArrayList();
        m_types = new HashMap();
        m_elementMappings = new HashMap();
        m_schemaLocation = schemaLocation;
    }
    
    /**
     * Freezes this content definition, making all internal data structures
     * unmodifiable.<p>
     * 
     * This is required to prevent modification of a cached content definition.<p>
     */
    public void freeze() {
        
        m_types = Collections.unmodifiableMap(m_types);
        m_elementMappings = Collections.unmodifiableMap(m_elementMappings);
        m_typeSequence = Collections.unmodifiableList(m_typeSequence);
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
    public static CmsXmlContentDefinition unmarshal(InputSource source, String schemaLocation, EntityResolver resolver) throws CmsXmlException {
        
        return unmarshal(CmsXmlUtils.unmarshalHelper(source, resolver), schemaLocation);
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
    public static CmsXmlContentDefinition unmarshal(byte[] xmlData, String schemaLocation, EntityResolver resolver) throws CmsXmlException {
        
        return unmarshal(CmsXmlUtils.unmarshalHelper(xmlData, resolver), schemaLocation);
    }
        
    /**
     * Factory method to unmarshal (read) a XML content definition instance from a Stringy
     * that contains XML data.<p>
     * 
     * @param xmlData the XML data in a String
     * @param schemaLocation the location from which the XML schema was read (system id)
     * @param resolver the XML entitiy resolver to use
     * @return a XML content definition instance unmarshalled from the byte array
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlContentDefinition unmarshal(String xmlData, String schemaLocation, EntityResolver resolver) throws CmsXmlException {
        
        return unmarshal(CmsXmlUtils.unmarshalHelper(xmlData, resolver), schemaLocation);
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
        
        // TODO: why not use a XML schmema for the validation?
        
        // now analyze the document and generate the XML content type definition        
        Element root = document.getRootElement();
        if (! XSD_NODE_SCHEMA.equals(root.getQName())) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        
        List includes = root.elements(XSD_NODE_INCLUDE);
        if (includes.size() != 1) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        Element include = (Element)includes.get(0);
        Attribute target = include.attribute(XSD_ATTRIBUTE_SCHEMA_LOCATION);
        if (! XSD_INCLUDE_OPENCMS.equals(target.getValue())) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        
        List elements = root.elements(XSD_NODE_ELEMENT);
        if (includes.size() != 1) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        } 
        Element main = (Element)elements.get(0);
        Attribute nameAttr = main.attribute(XSD_ATTRIBUTE_NAME);
        Attribute typeAttr = main.attribute(XSD_ATTRIBUTE_TYPE);
        if ((nameAttr == null) || (typeAttr == null)) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        
        String name = nameAttr.getValue();
        if (! name.endsWith("s")) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        name = name.substring(0, name.length()-1);
        if (name.length() == 0) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }   
        
        String listName = createListName(name);
        String typeName = createTypeName(name);
        
        if (! listName.equals(typeAttr.getValue())) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }

        // OpenCms XML content definitions require exactly 2 complex types
        List complexTypes = root.elements(XSD_NODE_COMPLEXTYPE);
        if (complexTypes.size() != 2) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        } 
        
        Element complex1 = (Element)complexTypes.get(0);
        Element complex2 = (Element)complexTypes.get(1);
        
        String name1 = complex1.attributeValue(XSD_ATTRIBUTE_NAME);
        String name2 = complex2.attributeValue(XSD_ATTRIBUTE_NAME);
        
        if (! (listName.equals(name1) || listName.equals(name2))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        if (! (typeName.equals(name1) || typeName.equals(name2))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        
        // determine which is the "list" and which is the "type" element
        Element listElement;
        Element typeElement;
        
        if (listName.equals(name1)) {
            listElement = complex1;
            typeElement = complex2;
        } else {            
            listElement = complex2;
            typeElement = complex1;
        }                   
        
        // check if the list element is defined correctly
        Element listSequence = (Element)listElement.elements().get(0);
        if (! XSD_NODE_SEQUENCE.equals(listSequence.getQName())) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }        
        Element listSequenceElement = (Element)listSequence.elements().get(0);
        if (! XSD_NODE_ELEMENT.equals(listSequenceElement.getQName())) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }        
        if (! name.equals(listSequenceElement.attributeValue(XSD_ATTRIBUTE_NAME))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        if (! typeName.equals(listSequenceElement.attributeValue(XSD_ATTRIBUTE_TYPE))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        if (! XSD_ATTRIBUTE_VALUE_ZERO.equals(listSequenceElement.attributeValue(XSD_ATTRIBUTE_MIN_OCCURS))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }        
        if (! XSD_ATTRIBUTE_VALUE_UNBOUNDED.equals(listSequenceElement.attributeValue(XSD_ATTRIBUTE_MAX_OCCURS))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        
        // now check the type definition list
        List typeElements = typeElement.elements();
        if ((typeElements.size() != 2) && (typeElements.size() != 3)) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }         
        
        Element type1 = (Element)typeElements.get(0);
        Element type2 = (Element)typeElements.get(1);
        
        Element typeSequence = null; 
        Element typeAttribute = null;
            
        if (XSD_NODE_SEQUENCE.equals(type1.getQName())) {
            typeSequence = type1;
        }
        if (XSD_NODE_ATTRIBUTE.equals(type2.getQName())) {
            typeAttribute = type2;
        }     
        
        // check the "language" attribute 
        if ((typeSequence == null) || (typeAttribute == null)) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        } 
        if (! XSD_ATTRIBUTE_VALUE_LANGUAGE.equals(typeAttribute.attributeValue(XSD_ATTRIBUTE_NAME))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        if (! CmsXmlLocaleValue.C_TYPE_NAME.equals(typeAttribute.attributeValue(XSD_ATTRIBUTE_TYPE))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        if (! XSD_ATTRIBUTE_VALUE_REQUIRED.equals(typeAttribute.attributeValue(XSD_ATTRIBUTE_USE))) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }        
        
        // check the type definition sequence
        List typeSequenceElements = typeSequence.elements();
        if (typeSequenceElements.size() < 1) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }

        // generate the XML content definition
        CmsXmlContentTypeManager typeManager = CmsXmlContentTypeManager.getTypeManager();
        CmsXmlContentDefinition result = new CmsXmlContentDefinition(name, schemaLocation);
        Iterator i = typeSequenceElements.iterator();
        while (i.hasNext()) {
            result.addType(typeManager.getContentType((Element)i.next()));
        }
        
        // add element mappings
        List annotations = root.elements(XSD_NODE_ANNOTATION);
        if (annotations.size() > 0) {
            List appinfos = ((Element)annotations.get(0)).elements(XSD_NODE_APPINFO);
            if (appinfos.size() > 0) {
                i = ((Element)appinfos.get(0)).elements().iterator();
                while (i.hasNext()) {
                    Element appinfo = (Element)i.next();
                    if (appinfo.getName().equals("mapping")) {
                        String key = appinfo.attributeValue("element");
                        String value = appinfo.attributeValue("mapto");
                        if ((key != null) && (value != null)) {
                            result.addMapping(key, value);
                        }
                    }
                }
            }
        }
        
        result.freeze();
        return result;
    }    
    
    /**
     * Creates the name of the list attribute from the given content name.<p>
     * 
     * @param name the name to use
     * @return the name of the list attribute
     */    
    private static String createListName(String name) {
        
        StringBuffer result = new StringBuffer(32);
        result.append(createTypeName(name));
        result.append("s");
        return result.toString();       
    }
    
    /**
     * Creates the name of the type attribute from the given content name.<p>
     * 
     * @param name the name to use
     * @return the name of the type attribute
     */
    private static String createTypeName(String name) {
        
        StringBuffer result = new StringBuffer(32);
        result.append("OpenCms");
        result.append(name.substring(0, 1).toUpperCase());
        if (name.length() > 1) {
            result.append(name.substring(1));
        }
        return result.toString();   
    }
    
    /**
     * Adds an element mapping.<p>
     * 
     * @param elementName the element name to map
     * @param mapping the mapping to use
     * @throws CmsXmlException in case an unknown element name is used
     */
    public void addMapping(String elementName, String mapping) throws CmsXmlException {
        
        if (getSchemaType(elementName) == null) {
            throw new CmsXmlException("Unregistered XML content type used for mapping");            
        }
        
        m_elementMappings.put(elementName, mapping);        
    }
    
    /**
     * Adds the given content type.<p>
     * 
     * @param type the content type to add
     * @throws CmsXmlException in case an unregisterd type is added
     */
    public void addType(I_CmsXmlSchemaType type) throws CmsXmlException {
        
        // check if the type to add actually exists in the type manager
        CmsXmlContentTypeManager typeManager = CmsXmlContentTypeManager.getTypeManager();        
        if (typeManager.getContentType(type.getTypeName()) == null) {
            throw new CmsXmlException("Unregistered XML content type added");
        }
        
        m_typeSequence.add(type);
        m_types.put(type.getNodeName(), type);
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        
        if (o == null) {
            return false;
        }
        if (! (o instanceof CmsXmlContentDefinition)) {
            return false;
        }
        CmsXmlContentDefinition other = (CmsXmlContentDefinition)o;
        if (! getName().equals(other.getName())) {
            return false;
        }
        return m_typeSequence.equals(other.m_typeSequence);
    }

    /**
     * Returns the mapping defined for the given element name.<p>
     * 
     * @param elementName the element name to use
     * @return the mapping defined for the given element name
     */
    public String getMapping(String elementName) {
        
        return (String)m_elementMappings.get(elementName);
    }
    
    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
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
        
        String listName = createListName(getName());
        String typeName = createTypeName(getName());
        String contentName = getName() + "s";
        
        Element content = root.addElement(XSD_NODE_ELEMENT);
        content.addAttribute(XSD_ATTRIBUTE_NAME, contentName);
        content.addAttribute(XSD_ATTRIBUTE_TYPE, listName);
        
        Element list = root.addElement(XSD_NODE_COMPLEXTYPE);
        list.addAttribute(XSD_ATTRIBUTE_NAME, listName);
        
        Element listSequence = list.addElement(XSD_NODE_SEQUENCE);
        Element listElement = listSequence.addElement(XSD_NODE_ELEMENT);
        listElement.addAttribute(XSD_ATTRIBUTE_NAME, getName());
        listElement.addAttribute(XSD_ATTRIBUTE_TYPE, typeName);
        listElement.addAttribute(XSD_ATTRIBUTE_MIN_OCCURS, XSD_ATTRIBUTE_VALUE_ZERO);
        listElement.addAttribute(XSD_ATTRIBUTE_MAX_OCCURS, XSD_ATTRIBUTE_VALUE_UNBOUNDED);
        
        Element main = root.addElement(XSD_NODE_COMPLEXTYPE);
        main.addAttribute(XSD_ATTRIBUTE_NAME, typeName);
        
        Element mainSequence = main.addElement(XSD_NODE_SEQUENCE);
        
        Iterator i = m_typeSequence.iterator();
        while (i.hasNext()) {
            I_CmsXmlSchemaType schemaType = (I_CmsXmlSchemaType)i.next();
            schemaType.appendXmlSchema(mainSequence);
        }
        
        Element language = main.addElement(XSD_NODE_ATTRIBUTE);
        language.addAttribute(XSD_ATTRIBUTE_NAME, XSD_ATTRIBUTE_VALUE_LANGUAGE);
        language.addAttribute(XSD_ATTRIBUTE_TYPE, CmsXmlLocaleValue.C_TYPE_NAME);        
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
     * Returns the scheme type for the given key, or <code>null</code> if no 
     * node is defined for this key.<p>
     * 
     * @param keyName the key to look up the type for
     * @return the type for the given key, or <code>null</code> if no 
     *      node is defined for this key
     */
    public I_CmsXmlSchemaType getSchemaType(String keyName) {
        
        return (I_CmsXmlSchemaType)m_types.get(keyName);
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
     * Generates a valid XML document according to the XML schema of this content definition.<p>
     * 
     * @param locale the locale to create the default element in the document with
     * @return a valid XML document according to the XML schema of this content definition
     */    
    public Document createDocument(Locale locale) {
        
        Document doc = DocumentHelper.createDocument();
        
        Element root = doc.addElement(getName() + "s"); 
        root.add(I_CmsXmlSchemaType.XSI_NAMESPACE);        
        root.addAttribute(I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION, getSchemaLocation());
        
        Element node = root.addElement(getName());
        node.addAttribute(XSD_ATTRIBUTE_VALUE_LANGUAGE, locale.toString());
        
        Iterator i = m_typeSequence.iterator();
        while (i.hasNext()) {
            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
            for (int j=0; j<type.getMinOccurs(); j++) {
                type.appendDefaultXml(node, j);
            }
        }
        
        return doc;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        
        return getName().hashCode();
    }
        
    /**
     * Creates a clone of this XML content definition.<p> 
     * 
     * @return a clone of this XML content definition
     */
    public Object clone() {

        CmsXmlContentDefinition result = new CmsXmlContentDefinition(m_name, m_schemaLocation);
        result.m_typeSequence = m_typeSequence;
        result.m_elementMappings = m_elementMappings;
        result.m_types = m_types;
        return result;
    }
}

