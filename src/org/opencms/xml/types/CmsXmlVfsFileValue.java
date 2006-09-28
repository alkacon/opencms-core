/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlVfsFileValue.java,v $
 * Date   : $Date: 2006/09/28 07:53:12 $
 * Version: $Revision: 1.18.8.2 $
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
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsLinkUpdateUtil;
import org.opencms.relations.CmsRelationType;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsVfsFile".<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.18.8.2 $ 
 * 
 * @since 7.0.0 
 */
public class CmsXmlVfsFileValue extends A_CmsXmlContentValue {

    /** Value to mark that no link is defined, "none". */
    public static final String NO_LINK = "none";

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsVfsFile";

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
     * @param cms the cms context, can be <code>null</code> but no link check is performed
     * 
     * @return the link table of this XML page element
     */
    public CmsLink getLink(CmsObject cms) {

        Element linkElement = m_element.element(CmsXmlPage.NODE_LINK);
        if (linkElement == null) {
            String uri = m_element.getText();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(uri)) {
                setStringValue(cms, uri);
            }
            linkElement = m_element.element(CmsXmlPage.NODE_LINK);
            if (linkElement == null) {
                return null;
            }
        }
        CmsLinkUpdateUtil.updateType(linkElement, getContentDefinition().getContentHandler().getRelationType(this));
        CmsLink link = new CmsLink(linkElement);
        // link management check
        link.checkConsistency(cms);

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
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlVfsFileValue.xsd");
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject)
     */
    public String getStringValue(CmsObject cms) throws CmsRuntimeException {

        if (m_stringValue == null) {
            m_stringValue = createStringValue(cms);
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

        return new CmsXmlVfsFileValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        m_element.clearContent();
        if (value == null) {
            return;
        }
        String path = value;
        if (cms != null) {
            // remove the site root, because the next call will append it anyway
            if (path.startsWith(cms.getRequestContext().getSiteRoot())) {
                path = path.substring(cms.getRequestContext().getSiteRoot().length());
            }
            // get the site path
            path = CmsLinkManager.getSitePath(cms, null, path);
        }
        if (path == null) {
            return;
        }
        CmsRelationType type = getContentDefinition().getContentHandler().getRelationType(this);
        CmsLink link = new CmsLink("link0", type, path, true);
        // link management check
        link.checkConsistency(cms);
        // update xml node
        CmsLinkUpdateUtil.updateXmlForVfsFile(link, m_element.addElement(CmsXmlPage.NODE_LINK));
        // ensure the String value is re-calculated next time
        m_stringValue = null;
    }

    /**
     * Creates the String value for this vfs file value element.<p>
     * 
     * @param cms the cms context
     * 
     * @return the String value for this vfs file value element
     */
    private String createStringValue(CmsObject cms) {

        Attribute enabled = m_element.attribute(CmsXmlPage.ATTRIBUTE_ENABLED);

        String content = "";
        if ((enabled == null) || Boolean.valueOf(enabled.getText()).booleanValue()) {
            CmsLink link = getLink(cms);
            if (link != null) {
                content = link.getUri();
                if (cms != null) {
                    content = cms.getRequestContext().removeSiteRoot(link.getUri());
                }
            }
        }
        return content;
    }
}
