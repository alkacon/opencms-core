/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/Attic/CmsXmlVfsFileReferenceValue.java,v $
 * Date   : $Date: 2006/07/13 14:56:32 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsVfsReferenceFile".<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.0.0 
 */
public class CmsXmlVfsFileReferenceValue extends A_CmsXmlContentValue {

    /** Value to mark that no link is defined, "none". */
    public static final String NO_LINK = "none";

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsVfsFileReference";

    /** The schema definition String is located in a text for easier editing. */
    private static String m_schemaDefinition;

    /** The String value of the element node. */
    private String m_stringValue;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsVfsFileReference".<p>
     */
    public CmsXmlVfsFileReferenceValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsVfsFileReference".<p>
     * 
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlVfsFileReferenceValue(
        I_CmsXmlDocument document,
        Element element,
        Locale locale,
        I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsVfsFile".<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlVfsFileReferenceValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlVfsFileReferenceValue(document, element, locale, this);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#generateXml(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, org.dom4j.Element, java.util.Locale)
     */
    public Element generateXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        Element element = root.addElement(getName());

        // get the default value from the content handler
        String defaultValue = document.getContentDefinition().getContentHandler().getDefault(cms, this, locale);
        if (defaultValue != null) {
            I_CmsXmlContentValue value = createValue(document, element, locale);
            value.setStringValue(cms, defaultValue);
        }
        return element;
    }

    /**
     * Returns the link table of this XML page element.<p>
     * 
     * @return the link table of this XML page element
     */
    public CmsLink getLink() {

        Element linkElement = m_element.element(CmsXmlPage.NODE_LINK);
        if (linkElement == null) {
            return null;
        }
        Element uuid = linkElement.element(CmsXmlPage.NODE_UUID);
        Element target = linkElement.element(CmsXmlPage.NODE_TARGET);
        Element anchor = linkElement.element(CmsXmlPage.NODE_ANCHOR);
        Element query = linkElement.element(CmsXmlPage.NODE_QUERY);

        if (target == null || CmsStringUtil.isEmptyOrWhitespaceOnly(target.getText())) {
            return null;
        }
        CmsLink link = new CmsLink(
            m_element,
            "link0",
            "VFSREF",
            (uuid != null) ? new CmsUUID(uuid.getText()) : null,
            target.getText(),
            (anchor != null) ? anchor.getText() : null,
            (query != null) ? query.getText() : null,
            true);

        return link;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getPlainText(org.opencms.file.CmsObject)
     */
    public String getPlainText(CmsObject cms) {

        return getStringValue(cms);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        // the schema definition is located in a separate file for easier editing
        if (m_schemaDefinition == null) {
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlVfsFileReferenceValue.xsd");
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject)
     */
    public String getStringValue(CmsObject cms) throws CmsRuntimeException {

        if (m_stringValue == null) {
            m_stringValue = createStringValue();
        }
        return m_stringValue;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getTypeName()
     */
    public String getTypeName() {

        return TYPE_NAME;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlVfsFileReferenceValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        m_element.clearContent();
        if (value == null) {            
            return;
        }
        if (cms != null && value != null) {
            value = CmsLinkManager.getSitePath(cms, null, value);
        }
        CmsLink link = new CmsLink("link0", "VFSREF", value, true);
        Element linkElement = m_element.addElement(CmsXmlPage.NODE_LINK);

        if (link.getStructureId() != null) {
            linkElement.addElement(CmsXmlPage.NODE_UUID).addCDATA(link.getStructureId().toString());
        }
        linkElement.addElement(CmsXmlPage.NODE_TARGET).addCDATA(link.getTarget());

        if (link.getAnchor() != null) {
            linkElement.addElement(CmsXmlPage.NODE_ANCHOR).addCDATA(link.getAnchor());
        }

        if (link.getQuery() != null) {
            linkElement.addElement(CmsXmlPage.NODE_QUERY).addCDATA(link.getQuery());
        }

        // ensure the String value is re-calculated next time
        m_stringValue = null;

    }

    /**
     * Creates the String value for this vfs file reference value element.<p>
     * 
     * @return the String value for this vfs file reference value element
     */
    private String createStringValue() {

        Attribute enabled = m_element.attribute(CmsXmlPage.ATTRIBUTE_ENABLED);

        String content = "";
        if (getLink() != null && (enabled == null || Boolean.valueOf(enabled.getText()).booleanValue())) {
            content = getLink().getVfsUri();
        }
        return content;
    }
}
