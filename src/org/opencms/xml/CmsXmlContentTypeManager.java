/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlContentTypeManager.java,v $
 * Date   : $Date: 2004/08/18 11:53:19 $
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.xmlwidgets.CmsXmlDateTimeWidget;
import org.opencms.workplace.xmlwidgets.CmsXmlHtmlWidget;
import org.opencms.workplace.xmlwidgets.CmsXmlStringWidget;
import org.opencms.workplace.xmlwidgets.I_CmsXmlWidget;
import org.opencms.xml.types.CmsXmlDateTimeValue;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlLocaleValue;
import org.opencms.xml.types.CmsXmlSimpleHtmlValue;
import org.opencms.xml.types.CmsXmlStringValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;


/**
 * Manager class for registered OpenCms XML content types.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.5.0
 */
public class CmsXmlContentTypeManager {
    
    /** The static type manager singleton instance. */
    private static CmsXmlContentTypeManager m_instance;
  
    /** Stores the registered content types. */
    private Map m_registeredTypes;
    
    /** Stores the registered content widgets. */
    private Map m_registeredWidgets;
    
    /**
     * Creates a new content type manager.<p> 
     */
    public CmsXmlContentTypeManager() {
        
        m_registeredTypes = new HashMap();
        m_registeredWidgets = new HashMap();
    }
    
    /**
     * Returns the XML content type manager.<p>
     * 
     * @return the XML content type manager
     */
    public static CmsXmlContentTypeManager getTypeManager() {
        
        if (m_instance == null) {
            m_instance = new CmsXmlContentTypeManager();

            int todo = 0;
            // TODO: this is just a hack to get rolling
            try {
                m_instance.addContentType(CmsXmlStringValue.class);
                m_instance.addContentType(CmsXmlSimpleHtmlValue.class);
                m_instance.addContentType(CmsXmlLocaleValue.class);
                m_instance.addContentType(CmsXmlDateTimeValue.class);
                m_instance.addContentType(CmsXmlHtmlValue.class);
                
                m_instance.addEditorWidget(CmsXmlStringValue.C_TYPE_NAME, CmsXmlStringWidget.class);
                m_instance.addEditorWidget(CmsXmlSimpleHtmlValue.C_TYPE_NAME, CmsXmlHtmlWidget.class);
                m_instance.addEditorWidget(CmsXmlLocaleValue.C_TYPE_NAME, CmsXmlStringWidget.class);
                m_instance.addEditorWidget(CmsXmlDateTimeValue.C_TYPE_NAME, CmsXmlDateTimeWidget.class);
                m_instance.addEditorWidget(CmsXmlHtmlValue.C_TYPE_NAME, CmsXmlHtmlWidget.class);
            } catch (CmsXmlException e) {
                OpenCms.getLog(CmsXmlContentTypeManager.class).error("Error initializing XML content type manager", e);
            }
        }
        return m_instance;        
    }
    
    /**
     * Adds a content type class to the registerd XML content types.<p>
     * 
     * @param clazz the class type to add
     * @throws CmsXmlException in case the class is not an instance of {@link I_CmsXmlSchemaType}
     */
    public void addContentType(Class clazz) throws CmsXmlException {
        
        I_CmsXmlSchemaType type;
        try {
            type = (I_CmsXmlSchemaType)clazz.newInstance();
        } catch (Throwable t) {
            throw new CmsXmlException("Invalid XML content class type registered");
        }
        m_registeredTypes.put(type.getTypeName(), type);
        
        CmsXmlEntityResolver.cacheSystemId(CmsXmlContentDefinition.XSD_INCLUDE_OPENCMS, getSchemaBytes());
    }
    
    /**
     * Adds a editor widget class for a registerd XML content type.<p>
     * 
     * @param typeName the name of the XML content type to add the widget for
     * @param clazz the class type to add
     * @throws CmsXmlException in case the class is not an instance of {@link I_CmsXmlWidget}
     */
    public void addEditorWidget(String typeName, Class clazz) throws CmsXmlException {
        
        I_CmsXmlWidget widget;
        try {
            widget = (I_CmsXmlWidget)clazz.newInstance();
        } catch (InstantiationException e) {
            throw new CmsXmlException("Invalid XML content class type registered");
        } catch (IllegalAccessException e) {
            throw new CmsXmlException("Invalid XML content class type registered");
        } catch (ClassCastException e) {
            throw new CmsXmlException("Invalid XML content class type registered");
        }
        m_registeredWidgets.put(typeName, widget);
    }
    
    /**
     * Generates an initialized instance of a XML content type definition
     * from the given XML schema element.<p>
     * 
     * @param typeElement the element to generate the XML content type definition from
     * @return an initialized instance of a XML content type definition
     * @throws CmsXmlException in case the element does not describe a valid XML content type definition
     */
    public I_CmsXmlSchemaType getContentType(Element typeElement) throws CmsXmlException {
   
        if (! CmsXmlContentDefinition.XSD_NODE_ELEMENT.equals(typeElement.getQName())) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        if (typeElement.elements().size() > 0) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        
        String name = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_NAME);
        String type = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_TYPE);
        String defaultValue = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_DEFAULT);
        String maxOccrs = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_MAX_OCCURS);
        String minOccrs = typeElement.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_MIN_OCCURS);
            
        if (CmsStringUtil.isEmpty(name) || CmsStringUtil.isEmpty(type)) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }
        
        I_CmsXmlSchemaType schemaType = (I_CmsXmlSchemaType)m_registeredTypes.get(type);
        if (schemaType == null) {
            throw new CmsXmlException("Invalid OpenCms content definition XML schema structure");
        }                
        
        schemaType = schemaType.newInstance(name, minOccrs, maxOccrs);
        
        if (CmsStringUtil.isNotEmpty(defaultValue)) {
            schemaType.setDefault(defaultValue);
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
