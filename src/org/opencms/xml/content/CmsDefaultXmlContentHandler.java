/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsDefaultXmlContentHandler.java,v $
 * Date   : $Date: 2004/12/01 17:36:03 $
 * Version: $Revision: 1.6 $
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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.xmlwidgets.I_CmsXmlWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Element;

/**
 * Default implementation for the XML content handler, will be used by all XML contents that do not
 * provide their own handler.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 * @since 5.5.4
 */
public class CmsDefaultXmlContentHandler implements I_CmsXmlContentHandler {

    /** The element mappings (defined in the annotations). */
    protected Map m_elementMappings;

    /** The widgets used for the elements (defined in the annotations). */
    protected Map m_elementWidgets;

    /** Indicates if <code>{@link #freeze()}</code> has alreaby been called on this instance. */
    protected boolean m_frozen;

    /**
     * Creates a new instance of the default XML content handler.<p>  
     */
    public CmsDefaultXmlContentHandler() {

        m_elementMappings = new HashMap();
        m_elementWidgets = new HashMap();
    }

    /**
     * Adds an element mapping.<p>
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to map
     * @param mapping the mapping to use
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    public void addMapping(CmsXmlContentDefinition contentDefinition, String elementName, String mapping)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException("Unregistered XML content type used for mapping");
        }

        m_elementMappings.put(elementName, mapping);
    }

    /**
     * Adds a GUI widget for a soecified element.<p> 
     * 
     * @param elementName the element name to map
     * @param className the name of the widget class to use as GUI for the element
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    public void addWidget(String elementName, String className) throws CmsXmlException {

        I_CmsXmlWidget widget = OpenCms.getXmlContentTypeManager().getEditorWidgetByClassName(className);

        if (widget == null) {
            throw new CmsXmlException("Unregistered XML widget '"
                + className
                + "'configureed as GUI for element "
                + elementName);
        }

        m_elementWidgets.put(elementName, widget);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#analyzeAppInfo(org.dom4j.Element, org.opencms.xml.CmsXmlContentDefinition)
     */
    public synchronized void analyzeAppInfo(Element appInfoElement, CmsXmlContentDefinition contentDefinition)
    throws CmsXmlException {

        if (m_frozen || appInfoElement == null) {
            // already frozen, or no appinfo provided, so no mapping is required
            return;
        }

        Iterator i = appInfoElement.elements().iterator();
        while (i.hasNext()) {
            // iterate all elements in the appinfo node
            Element appinfo = (Element)i.next();
            String nodeName = appinfo.getName();
            if (nodeName.equals("mapping")) {
                // this is a mapping node
                String elementName = appinfo.attributeValue("element");
                String value = appinfo.attributeValue("mapto");
                if ((elementName != null) && (value != null)) {
                    // add the element mapping 
                    addMapping(contentDefinition, elementName, value);
                }
            } else if (nodeName.equals("gui")) {
                // this is a gui widget node
                String elementName = appinfo.attributeValue("element");
                String className = appinfo.attributeValue("widget");
                if ((elementName != null) && (className != null)) {
                    // add the GUI widget
                    addWidget(elementName, className);
                }
            }
        }
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#freeze()
     */
    public synchronized void freeze() {

        // indicate this XML content handler is already frozen
        m_frozen = true;
        // make the element mappings unmodifiable
        m_elementMappings = Collections.unmodifiableMap(m_elementMappings);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDefaultValue(org.opencms.xml.types.I_CmsXmlSchemaType, java.util.Locale)
     */
    public String getDefaultValue(I_CmsXmlSchemaType type, Locale locale) {

        // default implementation currently just uses the "getDefault" mehod of the given value
        return type.getDefault(locale);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getEditorWidget(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public I_CmsXmlWidget getEditorWidget(I_CmsXmlContentValue value) {

        // try the specific widget settings first
        I_CmsXmlWidget result = (I_CmsXmlWidget)m_elementWidgets.get(value.getElementName());
        if (result != null) {
            return result;
        }

        // use default widget mappings
        return OpenCms.getXmlContentTypeManager().getEditorWidget(value.getTypeName());
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
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getPreviewUri(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent)
     */
    public String getPreviewUri(CmsObject cms, CmsXmlContent content) {

        // the default implementation currently does not support a preview URI
        // TODO: read some node from schema appinfo and create a link based on that information
        return null;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#resolveAppInfo(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent)
     */
    public void resolveAppInfo(CmsObject cms, CmsXmlContent content) throws CmsException {

        // get the original VFS file from the content
        CmsFile file = content.getFile();
        if (file == null) {
            throw new CmsXmlException("File not available to resolve element mappings");
        }

        // get filename and locale
        String filename = cms.getSitePath(content.getFile());
        Locale locale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(cms, filename).get(0);

        List typeSequence = content.getContentDefinition().getTypeSequence();
        Iterator i = typeSequence.iterator();
        while (i.hasNext()) {

            // walk through all the possible values in the XML content definition
            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
            String nodeName = type.getElementName();
            int indexCount = content.getIndexCount(nodeName, locale);
            // ensure there's at last one value available in the XML content
            if (indexCount > 0) {

                // get the mapping for the node name
                String mapping = getMapping(nodeName);

                if (CmsStringUtil.isNotEmpty(mapping)) {

                    // this value is mapped (the mapping is set in the XML schema)                    
                    I_CmsXmlContentValue value = content.getValue(nodeName, locale, 0);
                    // get the string value of the current node
                    String stringValue = value.getStringValue(cms);

                    if (mapping.startsWith(C_MAPTO_PROPERTY)) {

                        // this is a property mapping
                        String property = mapping.substring(C_MAPTO_PROPERTY.length());
                        // just store the string value in the selected property
                        cms.writePropertyObject(filename, new CmsProperty(property, stringValue, null));

                    } else if (mapping.startsWith(C_MAPTO_ATTRIBUTE)) {

                        // this is an attribute mapping                        
                        String attribute = mapping.substring(C_MAPTO_ATTRIBUTE.length());
                        switch (C_ATTRIBUTES_LIST.indexOf(attribute)) {
                            case 0: // datereleased
                                long date;
                                date = Long.valueOf(stringValue).longValue();
                                if (date == 0) {
                                    date = CmsResource.DATE_RELEASED_DEFAULT;
                                }
                                file.setDateReleased(date);
                                break;
                            case 1: // dateexpired
                                date = Long.valueOf(stringValue).longValue();
                                if (date == 0) {
                                    date = CmsResource.DATE_EXPIRED_DEFAULT;
                                }
                                file.setDateExpired(date);
                                break;
                            default:
                        // TODO: handle invalid mapto values                                
                        }
                    }
                }
            }
        }
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#validateValue(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlContentValue, org.opencms.xml.content.CmsXmlContentErrorHandler)
     */
    public CmsXmlContentErrorHandler validateValue(
        CmsObject cms,
        I_CmsXmlContentValue value,
        CmsXmlContentErrorHandler errorHandler) {

        // the default implementation currently just returns an empty error handler (i.e. indicated no errors)
        // TODO: read some node from schema appinfo and create validation rules for this (regex - based)
        // TODO: use same regex logic for XML schema and editor validation if possible        
        if (errorHandler == null) {
            return new CmsXmlContentErrorHandler();
        }
        return errorHandler;
    }
}