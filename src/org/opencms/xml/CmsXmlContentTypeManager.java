/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlContentTypeManager.java,v $
 * Date   : $Date: 2004/12/03 18:40:22 $
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

package org.opencms.xml;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.xmlwidgets.I_CmsXmlWidget;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.FastHashMap;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Manager class for registered OpenCms XML content types and content collectors.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.11 $
 * @since 5.5.0
 */
public class CmsXmlContentTypeManager {

    /** Stores the initialized XML content handlers. */
    private Map m_contentHandlers;

    /** Stores the registered content types. */
    private Map m_registeredTypes;

    /** Stores the registered content widgets. */
    private Map m_registeredWidgets;

    /** Stores the registed content widgegts by class name. */
    private Map m_registeredWidgetsByClassName;

    /**
     * Creates a new content type manager.<p> 
     */
    public CmsXmlContentTypeManager() {

        m_registeredTypes = new HashMap();
        m_registeredWidgets = new HashMap();
        m_registeredWidgetsByClassName = new HashMap();

        // use the fast hash map implementation since there will be far more read then write accesses
        FastHashMap fastMap = new FastHashMap();
        fastMap.setFast(true);
        m_contentHandlers = fastMap;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". XML content config   : starting");
        }
    }

    /**
     * Returns a statically initialized instance of an XML content type manager (for test cases only).<p>
     * 
     * @return a statically initialized instance of an XML content type manager
     */
    public static CmsXmlContentTypeManager createTypeManagerForTestCases() {

        CmsXmlContentTypeManager typeManager = new CmsXmlContentTypeManager();
        typeManager.addXmlContent(
            "org.opencms.xml.types.CmsXmlDateTimeValue",
            "org.opencms.workplace.xmlwidgets.CmsXmlDateTimeWidget");
        typeManager.addXmlContent(
            "org.opencms.xml.types.CmsXmlHtmlValue",
            "org.opencms.workplace.xmlwidgets.CmsXmlHtmlWidget");
        typeManager.addXmlContent(
            "org.opencms.xml.types.CmsXmlLocaleValue",
            "org.opencms.workplace.xmlwidgets.CmsXmlStringWidget");
        typeManager.addXmlContent(
            "org.opencms.xml.types.CmsXmlSimpleHtmlValue",
            "org.opencms.workplace.xmlwidgets.CmsXmlHtmlWidget");
        typeManager.addXmlContent(
            "org.opencms.xml.types.CmsXmlStringValue",
            "org.opencms.workplace.xmlwidgets.CmsXmlStringWidget");

        typeManager.initialize(null);
        return typeManager;
    }

    /**
     * Adds a XML content schema type class to the registerd XML content types.<p>
     * 
     * @param clazz the XML content schema type class to add
     * 
     * @return the created instance of the XML content schema type
     * 
     * @throws CmsXmlException in case the class is not an instance of {@link I_CmsXmlSchemaType}
     */
    public I_CmsXmlSchemaType addContentType(Class clazz) throws CmsXmlException {

        I_CmsXmlSchemaType type;
        try {
            type = (I_CmsXmlSchemaType)clazz.newInstance();
        } catch (InstantiationException e) {
            throw new CmsXmlException("Invalid XML content class type registered");
        } catch (IllegalAccessException e) {
            throw new CmsXmlException("Invalid XML content class type registered");
        } catch (ClassCastException e) {
            throw new CmsXmlException("Invalid XML content class type registered");
        }
        m_registeredTypes.put(type.getTypeName(), type);
        return type;
    }

    /**
     * Adds a XML content editor widget class for a registerd XML content schema type.<p>
     * 
     * @param typeName the name of the XML content schema type to add the widget for
     * @param clazz the widget class to add
     * 
     * @return the created instance of the XML content editor widget
     * 
     * @throws CmsXmlException in case the class is not an instance of {@link I_CmsXmlWidget}
     */
    public I_CmsXmlWidget addEditorWidget(String typeName, Class clazz) throws CmsXmlException {

        I_CmsXmlWidget widget;
        try {
            widget = (I_CmsXmlWidget)clazz.newInstance();
        } catch (InstantiationException e) {
            throw new CmsXmlException("Invalid XML content widget registered");
        } catch (IllegalAccessException e) {
            throw new CmsXmlException("Invalid XML content widget registered");
        } catch (ClassCastException e) {
            throw new CmsXmlException("Invalid XML content widget registered");
        }
        m_registeredWidgets.put(typeName, widget);
        m_registeredWidgetsByClassName.put(clazz.getName(), widget);
        return widget;
    }

    /**
     * Adds a new XML content type schema class and XML widget class to the manager.<p>
     * 
     * @param classClazz the XML content schema type class to add
     * @param widgetClazz the XML widget class for the added XML content type
     */
    public void addXmlContent(Class classClazz, Class widgetClazz) {

        // create the schema type and add it to the internal list
        I_CmsXmlSchemaType type;
        try {
            type = addContentType(classClazz);
        } catch (Exception e) {
            OpenCms.getLog(this).error("Error initializing XML content type class: " + classClazz.getName(), e);
            return;
        }

        // add the editor widget for the schema type        
        try {
            addEditorWidget(type.getTypeName(), widgetClazz);
        } catch (Exception e) {
            OpenCms.getLog(this).error("Error initializing XML widget class for content type: " + type.getTypeName());
            return;
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". XML content config   : added type '"
                    + type.getTypeName()
                    + "' using widget class '"
                    + widgetClazz.getName()
                    + "'");
        }
    }

    /**
     * Adds a new XML content type schema class and XML widget to the manager by class names.<p>
     * 
     * @param className class name of the XML content schema type class to add
     * @param widgetName class name of the XML widget class for the added XML content type
     */
    public void addXmlContent(String className, String widgetName) {

        Class classClazz;
        // init class for schema type
        try {
            classClazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            OpenCms.getLog(this).error("XML content type class not found: " + className, e);
            return;
        }

        Class widgetClazz;
        // init class for widget 
        try {
            widgetClazz = Class.forName(widgetName);
        } catch (ClassNotFoundException e) {
            OpenCms.getLog(this).error("XML widget class not found: " + widgetName, e);
            return;
        }

        // now add the classes 
        addXmlContent(classClazz, widgetClazz);
    }

    /**
     * Returns the XML content handler instance class for the specified class name.<p>
     * 
     * Only one instance of an XML content handler class per content definition name will be generated, 
     * and that instance will be cached and re-used for all operations.<p> 
     * 
     * @param className the name of the XML content handler to return
     * @param schemaLocation the schema location of the XML content definition that handler belongs to
     *  
     * @return the XML content handler class
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public I_CmsXmlContentHandler getContentHandler(String className, String schemaLocation) throws CmsXmlException {

        // create a unique key for the content deinition / class name combo
        StringBuffer buffer = new StringBuffer(128);
        buffer.append(schemaLocation);
        buffer.append('#');
        buffer.append(className);
        String key = buffer.toString();

        // look up the content handler from the cache
        I_CmsXmlContentHandler contentHandler = (I_CmsXmlContentHandler)m_contentHandlers.get(key);
        if (contentHandler != null) {
            return contentHandler;
        }

        // generate an instance for the content handler
        try {
            contentHandler = (I_CmsXmlContentHandler)Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            throw new CmsXmlException("Invalid XML content handler requested: " + key);
        } catch (IllegalAccessException e) {
            throw new CmsXmlException("Invalid XML content handler requested: " + key);
        } catch (ClassCastException e) {
            throw new CmsXmlException("Invalid XML content handler requested: " + key);
        } catch (ClassNotFoundException e) {
            throw new CmsXmlException("Invalid XML content handler requested: " + key);
        }

        // cache and return the content handler instance
        m_contentHandlers.put(key, contentHandler);
        return contentHandler;
    }

    /**
     * Generates an initialized instance of a XML content type definition
     * from the given XML schema element.<p>
     * 
     * @param typeElement the element to generate the XML content type definition from
     * @param nestedDefinitions the nested (included) XML content sub-definitions
     * 
     * @return an initialized instance of a XML content type definition
     * @throws CmsXmlException in case the element does not describe a valid XML content type definition
     */
    public I_CmsXmlSchemaType getContentType(Element typeElement, Set nestedDefinitions) throws CmsXmlException {

        if (!CmsXmlContentDefinition.XSD_NODE_ELEMENT.equals(typeElement.getQName())) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        if (typeElement.elements().size() > 0) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }

        String elementName = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_NAME);
        String typeName = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_TYPE);
        String defaultValue = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_DEFAULT);
        String maxOccrs = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_MAX_OCCURS);
        String minOccrs = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_MIN_OCCURS);

        if (CmsStringUtil.isEmpty(elementName) || CmsStringUtil.isEmpty(typeName)) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }

        boolean simpleType = true;
        I_CmsXmlSchemaType schemaType = (I_CmsXmlSchemaType)m_registeredTypes.get(typeName);
        if (schemaType == null) {

            // the name is not a simple type, try to resolve from the nested schemas
            Iterator i = nestedDefinitions.iterator();
            while (i.hasNext()) {

                CmsXmlContentDefinition cd = (CmsXmlContentDefinition)i.next();
                if (typeName.equals(cd.getTypeName())) {

                    simpleType = false;
                    return new CmsXmlNestedContentDefinition(cd, elementName, minOccrs, maxOccrs);
                }
            }

            if (simpleType) {
                throw new CmsXmlException("Invalid OpenCms content definition XML schema structure: Schema type '"
                    + typeName
                    + "' unknown");
            }
        }

        if (simpleType) {
            schemaType = schemaType.newInstance(elementName, minOccrs, maxOccrs);

            if (CmsStringUtil.isNotEmpty(defaultValue)) {
                schemaType.setDefault(defaultValue);
            }
        }

        return schemaType;
    }

    /**
     * Returns the content type registered with the given name, or <code>null</code>.<p>
     * 
     * @param typeName the name to look up the content type for
     * @return the content type registered with the given name, or <code>null</code>
     */
    public I_CmsXmlSchemaType getContentType(String typeName) {

        return (I_CmsXmlSchemaType)m_registeredTypes.get(typeName);
    }

    /**
     * Returns the editor widget for the specified XML content type.<p>
     * 
     * @param typeName the name of the XML content type to get the widget for
     * @return the editor widget for the specified XML content type
     */
    public I_CmsXmlWidget getEditorWidget(String typeName) {

        return (I_CmsXmlWidget)m_registeredWidgets.get(typeName);
    }

    /**
     * Returns an initialized widget class by it's class name.<p>
     *  
     * @param className the class name to get the widget for
     * @return the widget instance for the class name
     */
    public I_CmsXmlWidget getEditorWidgetByClassName(String className) {

        return (I_CmsXmlWidget)m_registeredWidgetsByClassName.get(className);
    }

    /** 
     * Retruns an alphabetically sorted list of all configured XML content schema types.<p>
     * 
     * @return an alphabetically sorted list of all configured XML content schema types
     */
    public List getRegisteredContentTypes() {

        List result = new ArrayList(m_registeredTypes.values());
        Collections.sort(result);
        return result;
    }

    /**
     * Initializes XML content types managed in this XML content type manager.<p>
     * 
     * @param adminCms an initialized CmsObject with "Admin" permissions
     */
    public synchronized void initialize(CmsObject adminCms) {

        if (((adminCms == null) && (OpenCms.getRunLevel() > 1)) || ((adminCms != null) && !adminCms.isAdmin())) {
            // null admin cms only allowed during test cases
            throw new RuntimeException("Admin permissions are required to initialize the XML content type manager");
        }

        // create and cache the special system id for the XML content type entity resolver
        CmsXmlEntityResolver.cacheSystemId(CmsXmlContentDefinition.XSD_INCLUDE_OPENCMS, getSchemaBytes());

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". XML content config   : " + m_registeredTypes.size() + " XML content schema types initialized");
        }
    }

    /**
     * Returns a byte array to be used as input source for the configured XML content types.<p> 
     * 
     * @return a byte array to be used as input source for the configured XML content types
     */
    private byte[] getSchemaBytes() {

        StringBuffer schema = new StringBuffer(512);
        schema.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        schema.append("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">");
        Iterator i = m_registeredTypes.values().iterator();
        while (i.hasNext()) {
            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
            schema.append(type.getSchemaDefinition());
        }
        schema.append("</xsd:schema>");
        String schemaStr = schema.toString();

        try {
            // pretty print the XML schema
            // this helps in debugging the auto-generated schema includes
            // since it makes them more human-readable
            Document doc = CmsXmlUtils.unmarshalHelper(schemaStr, null);
            schemaStr = CmsXmlUtils.marshal(doc, CmsEncoder.C_UTF8_ENCODING);
        } catch (CmsXmlException e) {
            // should not ever happen
            OpenCms.getLog(this).error("Error pretty-printing schema bytes", e);
        }

        try {
            return schemaStr.getBytes(CmsEncoder.C_UTF8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            // should not happen since the default encoding of UTF-8 is always valid
            OpenCms.getLog(this).error("Error converting schema bytes", e);
        }
        return null;
    }
}