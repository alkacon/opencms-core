/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsDefaultXmlContentHandler.java,v $
 * Date   : $Date: 2004/11/01 12:23:49 $
 * Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $
 * @since 5.5.4
 */
public class CmsDefaultXmlContentHandler implements I_CmsXmlContentHandler {

    /** The element mappings (defined in the annotations). */
    protected Map m_elementMappings;

    /** Indicates if <code>{@link #freeze()}</code> has alreaby been called on this instance. */
    protected boolean m_frozen;

    /**
     * Creates a new instance of the default XML content handler.<p>  
     */
    public CmsDefaultXmlContentHandler() {

        m_elementMappings = new HashMap();
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
            if (appinfo.getName().equals("mapping")) {
                // this is a mapping node
                String key = appinfo.attributeValue("element");
                String value = appinfo.attributeValue("mapto");
                if ((key != null) && (value != null)) {
                    // add the mapping to the XML content definition
                    addMapping(contentDefinition, key, value);
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
     * Returns the mapping defined for the given element name.<p>
     * 
     * @param elementName the element name to use
     * @return the mapping defined for the given element name
     */
    public String getMapping(String elementName) {

        return (String)m_elementMappings.get(elementName);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#resolveElementMappings(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.xml.CmsXmlContentDefinition)
     */
    public void resolveElementMappings(CmsObject cms, CmsXmlContent content, CmsXmlContentDefinition contentDefinition)
    throws CmsException {

        // get the original VFS file from the content
        CmsFile file = content.getFile();
        if (file == null) {
            throw new CmsXmlException("File not available to resolve element mappings");
        }

        // get filename and locale
        String filename = cms.getSitePath(content.getFile());
        Locale locale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(cms, filename).get(0);

        List typeSequence = contentDefinition.getTypeSequence();
        Iterator i = typeSequence.iterator();
        while (i.hasNext()) {

            // walk through all the possible values in the XML content definition
            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
            String nodeName = type.getNodeName();
            int indexCount = content.getIndexCount(nodeName, locale);
            // ensure there's at last one value available in the XML content
            if (indexCount > 0) {

                // get the mapping for the node name
                String mapping = getMapping(nodeName);

                if (CmsStringUtil.isNotEmpty(mapping)) {

                    // this value is mapped (the mapping is set in the XML schema)                    
                    I_CmsXmlContentValue value = content.getValue(nodeName, locale, 0);
                    // get the string value of the current node
                    String stringValue = value.getStringValue(cms, content);

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
}