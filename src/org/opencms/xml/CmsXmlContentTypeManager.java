/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlContentTypeManager.java,v $
 * Date   : $Date: 2005/09/29 12:48:27 $
 * Version: $Revision: 1.29.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidget;
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
import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Manager class for registered OpenCms XML content types and content collectors.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.29.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsXmlContentTypeManager {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentTypeManager.class);

    /** Stores the initialized XML content handlers. */
    private Map m_contentHandlers;

    /** Stores the registered content widgets. */
    private Map m_defaultWidgets;

    /** Stores the registered content types. */
    private Map m_registeredTypes;

    /** Stores the registed content widgegts by class name. */
    private Map m_registeredWidgets;

    /** The alias names for the widgets. */
    private Map m_widgetAliases;

    /**
     * Creates a new content type manager.<p> 
     */
    public CmsXmlContentTypeManager() {

        // use the fast hash map implementation since there will be far more read then write accesses

        m_registeredTypes = new HashMap();
        m_defaultWidgets = new HashMap();
        m_registeredWidgets = new HashMap();
        m_widgetAliases = new HashMap();

        FastHashMap fastMap = new FastHashMap();
        fastMap.setFast(true);
        m_contentHandlers = fastMap;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_START_CONTENT_CONFIG_0));
        }
    }

    /**
     * Returns a statically initialized instance of an XML content type manager (for test cases only).<p>
     * 
     * @return a statically initialized instance of an XML content type manager
     */
    public static CmsXmlContentTypeManager createTypeManagerForTestCases() {

        CmsXmlContentTypeManager typeManager = new CmsXmlContentTypeManager();

        typeManager.addWidget("org.opencms.widgets.CmsCalendarWidget", null);
        typeManager.addWidget("org.opencms.widgets.CmsHtmlWidget", null);
        typeManager.addWidget("org.opencms.widgets.CmsInputWidget", null);

        typeManager.addSchemaType("org.opencms.xml.types.CmsXmlDateTimeValue", "org.opencms.widgets.CmsCalendarWidget");
        typeManager.addSchemaType("org.opencms.xml.types.CmsXmlHtmlValue", "org.opencms.widgets.CmsHtmlWidget");
        typeManager.addSchemaType("org.opencms.xml.types.CmsXmlLocaleValue", "org.opencms.widgets.CmsInputWidget");
        typeManager.addSchemaType("org.opencms.xml.types.CmsXmlStringValue", "org.opencms.widgets.CmsInputWidget");

        try {
            typeManager.initialize(null);
        } catch (CmsRoleViolationException e) {
            // this should never happen
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_INIT_TYPE_MANAGER_0));
        }
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
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_XCC_TYPE_REGISTERED_0));
        } catch (IllegalAccessException e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_XCC_TYPE_REGISTERED_0));
        } catch (ClassCastException e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_XCC_TYPE_REGISTERED_0));
        }
        m_registeredTypes.put(type.getTypeName(), type);
        return type;
    }

    /**
     * Adds a new XML content type schema class and XML widget to the manager by class names.<p>
     * 
     * @param className class name of the XML content schema type class to add
     * @param defaultWidget class name of the default XML widget class for the added XML content type
     */
    public void addSchemaType(String className, String defaultWidget) {

        Class classClazz;
        // init class for schema type
        try {
            classClazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOG.error(Messages.get().key(Messages.LOG_XML_CONTENT_SCHEMA_TYPE_CLASS_NOT_FOUND_1, className), e);
            return;
        }

        // create the schema type and add it to the internal list
        I_CmsXmlSchemaType type;
        try {
            type = addContentType(classClazz);
        } catch (Exception e) {
            LOG.error(
                Messages.get().key(Messages.LOG_INIT_XML_CONTENT_SCHEMA_TYPE_CLASS_ERROR_1, classClazz.getName()),
                e);
            return;
        }

        // add the editor widget for the schema type        
        I_CmsWidget widget = getWidget(defaultWidget);
        if (widget == null) {
            LOG.error(Messages.get().key(
                Messages.LOG_INIT_DEFAULT_WIDGET_FOR_CONTENT_TYPE_2,
                defaultWidget,
                type.getTypeName()));
            return;
        }

        // store the registered default widget
        m_defaultWidgets.put(type.getTypeName(), widget);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_ADD_ST_USING_WIDGET_2,
                type.getTypeName(),
                widget.getClass().getName()));
        }
    }

    /**
     * Adds a XML content editor widget class, making this widget available for the XML content editor.<p>
     * 
     * @param className the widget class to add
     * @param aliasName the (optional) alias name to use for the widget class
     */
    public void addWidget(String className, String aliasName) {

        Class widgetClazz;
        I_CmsWidget widget;
        try {
            widgetClazz = Class.forName(className);
            widget = (I_CmsWidget)widgetClazz.newInstance();
        } catch (Exception e) {
            LOG.error(Messages.get().key(Messages.LOG_XML_WIDGET_INITIALIZING_ERROR_1, className), e);
            return;
        }

        m_registeredWidgets.put(widgetClazz.getName(), widget);

        if (aliasName != null) {
            m_widgetAliases.put(aliasName, widgetClazz.getName());
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            if (aliasName != null) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_ADD_WIDGET_1, widgetClazz.getName()));
            } else {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_ADD_WIDGET_ALIAS_2, widgetClazz.getName(), aliasName));
            }
        }
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
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_CONTENT_HANDLER_1, key));
        } catch (IllegalAccessException e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_CONTENT_HANDLER_1, key));
        } catch (ClassCastException e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_CONTENT_HANDLER_1, key));
        } catch (ClassNotFoundException e) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_CONTENT_HANDLER_1, key));
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
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_CD_SCHEMA_STRUCTURE_0));
        }
        if (typeElement.elements().size() > 0) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_CD_SCHEMA_STRUCTURE_0));
        }

        int todo = 0;
        // TODO: Use validation methods from CmsXmlContentDefinition here

        String elementName = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_NAME);
        String typeName = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_TYPE);
        String defaultValue = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_DEFAULT);
        String maxOccrs = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_MAX_OCCURS);
        String minOccrs = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_MIN_OCCURS);

        if (CmsStringUtil.isEmpty(elementName) || CmsStringUtil.isEmpty(typeName)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_INVALID_CD_SCHEMA_STRUCTURE_0));
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
                throw new CmsXmlException(Messages.get().container(Messages.ERR_UNKNOWN_SCHEMA_1, typeName));
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
     * Retruns an alphabetically sorted list of all configured XML content schema types.<p>
     * 
     * @return an alphabetically sorted list of all configured XML content schema types
     */
    public List getRegisteredSchemaTypes() {

        List result = new ArrayList(m_registeredTypes.values());
        Collections.sort(result);
        return result;
    }

    /**
     * Returns the alias for the given Widget class name, may be <code>null</code> if no alias is defined for 
     * the class.<p>
     * 
     * @param className the name of the widget
     * @return the alias for the given Widget class name, may be <code>null</code> if no alias is defined for 
     * the class
     */
    public String getRegisteredWidgetAlias(String className) {

        // this implementation could be improved for performance, 
        // but since it's very seldom used it's currently just a straight map iteration 
        Iterator i = m_widgetAliases.keySet().iterator();
        while (i.hasNext()) {
            String aliasName = (String)i.next();
            String clazzName = (String)m_widgetAliases.get(aliasName);
            if (clazzName.equals(className)) {
                // the alias mapping was found
                return aliasName;
            }
        }
        return null;
    }

    /** 
     * Retruns an alphabetically sorted list of the class names of all configured XML widgets.<p>
     * 
     * @return an alphabetically sorted list of the class names of all configured XML widgets
     */
    public List getRegisteredWidgetNames() {

        List result = new ArrayList(m_registeredWidgets.keySet());
        Collections.sort(result);
        return result;
    }

    /**
     * Returns an initialized widget class by it's class name or by it's alias.<p>
     *  
     * @param name the class name or alias name to get the widget for
     * @return the widget instance for the class name
     */
    public I_CmsWidget getWidget(String name) {

        // first look up by class name
        I_CmsWidget result = (I_CmsWidget)m_registeredWidgets.get(name);
        if (result == null) {
            // not found by class name, look up an alias
            String className = (String)m_widgetAliases.get(name);
            if (className != null) {
                result = (I_CmsWidget)m_registeredWidgets.get(className);
            }
        }
        if (result != null) {
            result = result.newInstance();
        }
        return result;
    }

    /**
     * Returns the editor widget for the specified XML content type.<p>
     * 
     * @param typeName the name of the XML content type to get the widget for
     * @return the editor widget for the specified XML content type
     */
    public I_CmsWidget getWidgetDefault(String typeName) {

        I_CmsWidget result = (I_CmsWidget)m_defaultWidgets.get(typeName);
        if (result != null) {
            result = result.newInstance();
        }
        return result;
    }

    /**
     * Initializes XML content types managed in this XML content type manager.<p>
     * 
     * @param cms an initialized OpenCms user context with "Administrator" role permissions
     * 
     * @throws CmsRoleViolationException in case the provided OpenCms user context doea not have "Administrator" role permissions
     */
    public synchronized void initialize(CmsObject cms) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {

            // simple test cases don't require this check
            cms.checkRole(CmsRole.ADMINISTRATOR);
        }

        // create and cache the special system id for the XML content type entity resolver
        CmsXmlEntityResolver.cacheSystemId(CmsXmlContentDefinition.XSD_INCLUDE_OPENCMS, getSchemaBytes());

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_NUM_ST_INITIALIZED_1,
                new Integer(m_registeredTypes.size())));
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
            schemaStr = CmsXmlUtils.marshal(doc, CmsEncoder.ENCODING_UTF_8);
        } catch (CmsXmlException e) {
            // should not ever happen
            LOG.error(Messages.get().key(Messages.LOG_PRETTY_PRINT_SCHEMA_BYTES_ERROR_0), e);
        }

        try {
            return schemaStr.getBytes(CmsEncoder.ENCODING_UTF_8);
        } catch (UnsupportedEncodingException e) {
            // should not happen since the default encoding of UTF-8 is always valid
            LOG.error(Messages.get().key(Messages.LOG_CONVERTING_SCHEMA_BYTES_ERROR_0), e);
        }
        return null;
    }
}