/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlVfsFileValue.java,v $
 * Date   : $Date: 2004/12/01 12:01:20 $
 * Version: $Revision: 1.8 $
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

package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsVfsFile".<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.8 $
 * @since 5.5.2
 */
public class CmsXmlVfsFileValue extends A_CmsXmlContentValue {

    /** The name of this type as used in the XML schema. */
    public static final String C_TYPE_NAME = "OpenCmsVfsFile";

    /** The schema definition String is located in a text for easier editing. */
    private static String m_schemaDefinition;

    /** The String value of the element node. */
    private String m_stringValue;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsVfsFile".<p>
     */
    public CmsXmlVfsFileValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsVfsFile".<p>
     * 
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlVfsFileValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
        m_stringValue = element.getText();
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsVfsFile".<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlVfsFileValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlVfsFileValue(document, element, locale, this);
    }

    /**
     * Returns the link table of this XML page element.<p>
     * 
     * @return the link table of this XML page element
     */
    public CmsLinkTable getLinkTable() {

        CmsLinkTable linkTable = new CmsLinkTable();
        CmsLink link = new CmsLink("link0", "vfs", m_element.getText(), true);
        linkTable.addLink(link);
        return linkTable;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        // the schema definition is located in a separate file for easier editing
        if (m_schemaDefinition == null) {
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlVfsFileValue.xsd");
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject)
     */
    public String getStringValue(CmsObject cms) {

        if (cms != null) {
            return cms.getRequestContext().removeSiteRoot(m_stringValue);
        } else {
            return m_stringValue;
        }
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getTypeName()
     */
    public String getTypeName() {

        return C_TYPE_NAME;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlVfsFileValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) {

        if (cms != null) {
            // add site path if required
            value = CmsLinkManager.getSitePath(cms, null, value);
        }

        // now update the XML node
        m_element.clearContent();
        if (CmsStringUtil.isNotEmpty(value)) {
            m_element.addText(value);
            m_stringValue = value;
        } else {
            m_stringValue = null;
        }
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(java.lang.String)
     */
    public void setStringValue(String value) {

        // we don't have any information available for link processing
        setStringValue(null, value);
    }
}