/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlVfsFileValue.java,v $
 * Date   : $Date: 2005/05/13 13:35:38 $
 * Version: $Revision: 1.13 $
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

package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
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
 * @version $Revision: 1.13 $
 * @since 5.5.2
 */
public class CmsXmlVfsFileValue extends A_CmsXmlContentValue {

    /** Value to mark that no link is defined, "none". */
    public static final String C_NO_LINK = "none";

    /** The name of this type as used in the XML schema. */
    public static final String C_TYPE_NAME = "OpenCmsVfsFile";

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
        if (!C_NO_LINK.equals(m_element.getText())) {
            CmsLink link = new CmsLink("link0", "vfs", m_element.getText(), true);
            linkTable.addLink(link);
        }
        return linkTable;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        return "<xsd:simpleType name=\"" + C_TYPE_NAME + "\"><xsd:restriction base=\"xsd:string\" /></xsd:simpleType>";
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject)
     */
    public String getStringValue(CmsObject cms) throws CmsRuntimeException {

        if (cms != null && CmsStringUtil.isNotEmpty(m_stringValue) && !C_NO_LINK.equals(m_stringValue)) {
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
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        if (cms != null && !C_NO_LINK.equals(value)) {
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
}