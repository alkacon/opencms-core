/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlHtmlValue.java,v $
 * Date   : $Date: 2004/11/30 16:04:21 $
 * Version: $Revision: 1.17 $
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Iterator;
import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.htmlparser.util.ParserException;

/**
 * Describes the XML content type "OpenCmsHtml".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.17 $
 * @since 5.5.0
 */
public class CmsXmlHtmlValue extends A_CmsXmlContentValue implements I_CmsXmlContentValue {

    /** The name of this type as used in the XML schema. */
    public static final String C_TYPE_NAME = "OpenCmsHtml";

    /** The schema definition String is located in a text for easier editing. */
    private static String m_schemaDefinition;

    /** The String value of the element node. */
    private String m_stringValue;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsHtml".<p>
     */
    public CmsXmlHtmlValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsHtml".<p>
     * 
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     */
    public CmsXmlHtmlValue(Element element, Locale locale) {

        super(element, locale);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsHtml".<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlHtmlValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#appendDefaultXml(org.dom4j.Element, Locale)
     */
    public void appendDefaultXml(Element root, Locale locale) {

        Element element = root.addElement(getElementName());
        int index = element.getParent().elements(element.getQName()).indexOf(element);
        element.addAttribute(CmsXmlPage.ATTRIBUTE_NAME, getElementName() + index);
        element.addElement(CmsXmlPage.NODE_LINKS);
        element.addElement(CmsXmlPage.NODE_CONTENT);

        if (m_defaultValue != null) {
            try {
                I_CmsXmlContentValue value = createValue(element, locale);
                value.setStringValue(m_defaultValue);
            } catch (CmsXmlException e) {
                // should not happen if default value is correct
                OpenCms.getLog(this).error("Invalid default value '" + m_defaultValue + "' for XML content", e);
                element.clearContent();
            }
        }
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(Element element, Locale locale) {

        return new CmsXmlHtmlValue(element, locale);
    }

    /**
     * Returns the link table of this XML page element.<p>
     * 
     * @return the link table of this XML page element
     */
    public CmsLinkTable getLinkTable() {

        CmsLinkTable linkTable = new CmsLinkTable();

        Element links = m_element.element(CmsXmlPage.NODE_LINKS);

        if (links != null) {
            for (Iterator i = links.elementIterator(CmsXmlPage.NODE_LINK); i.hasNext();) {

                Element lelem = (Element)i.next();
                Attribute lname = lelem.attribute(CmsXmlPage.ATTRIBUTE_NAME);
                Attribute type = lelem.attribute(CmsXmlPage.ATTRIBUTE_TYPE);
                Attribute internal = lelem.attribute(CmsXmlPage.ATTRIBUTE_INTERNAL);

                Element target = lelem.element(CmsXmlPage.NODE_TARGET);
                Element anchor = lelem.element(CmsXmlPage.NODE_ANCHOR);
                Element query = lelem.element(CmsXmlPage.NODE_QUERY);

                CmsLink link = new CmsLink(
                    lelem,
                    lname.getValue(), 
                    type.getValue(), 
                    (target != null) ? target.getText(): null, 
                    (anchor != null) ? anchor.getText() : null, 
                    (query != null) ? query.getText() : null, 
                    Boolean.valueOf(internal.getValue()).booleanValue());

                linkTable.addLink(link);
            }
        }
        return linkTable;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getPlainText(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument)
     */
    public String getPlainText(CmsObject cms, I_CmsXmlDocument document) {

        try {
            CmsHtmlExtractor extractor = new CmsHtmlExtractor();
            return extractor.extractText(this.getStringValue(cms, document), document.getEncoding());
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        // the schema definition is located in a separate file for easier editing
        if (m_schemaDefinition == null) {
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlHtmlValue.xsd");
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument)
     */
    public String getStringValue(CmsObject cms, I_CmsXmlDocument document) {

        if (m_stringValue == null) {
            m_stringValue = createStringValue(cms, document);
        }

        return m_stringValue;
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

        return new CmsXmlHtmlValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, java.lang.String)
     */
    public void setStringValue(CmsObject cms, I_CmsXmlDocument document, String value) throws CmsXmlException {

        Element content = m_element.element(CmsXmlPage.NODE_CONTENT);
        Element links = m_element.element(CmsXmlPage.NODE_LINKS);
        CmsLinkProcessor linkProcessor = null;

        if (document != null) {
            // may be null in case of default value generation (i.e. setStringValue(String) was called)

            String encoding = document.getEncoding();
            linkProcessor = document.getLinkProcessor(cms, new CmsLinkTable());

            if (encoding != null) {
                // ensure all chars in the given content are valid chars for the selected charset
                value = CmsEncoder.adjustHtmlEncoding(value, encoding);
            }

            // remove unnecessary tags if required
            String contentConversion = document.getConversion();
            if (CmsHtmlConverter.isConversionEnabled(contentConversion)) {
                CmsHtmlConverter converter = new CmsHtmlConverter(encoding, contentConversion);
                value = converter.convertToStringSilent(value);
            }
        }

        if (linkProcessor != null) {
            try {
                // replace links in HTML by macros and fill link table      
                value = linkProcessor.replaceLinks(value);
            } catch (Exception exc) {
                throw new CmsXmlException("HTML data processing failed", exc);
            }
        }

        content.clearContent();
        links.clearContent();

        if (CmsStringUtil.isNotEmpty(value)) {
            content.addCDATA(value);
            if (linkProcessor != null) {
                // may be null in case of default value generation (i.e. setStringValue(String) was called)

                CmsLinkTable linkTable = linkProcessor.getLinkTable();
                for (Iterator i = linkTable.iterator(); i.hasNext();) {
                    CmsLink link = (CmsLink)i.next();
    
                    Element linkElement = links.addElement(CmsXmlPage.NODE_LINK)
                        .addAttribute(CmsXmlPage.ATTRIBUTE_NAME, link.getName())
                        .addAttribute(CmsXmlPage.ATTRIBUTE_TYPE, link.getType())
                        .addAttribute(CmsXmlPage.ATTRIBUTE_INTERNAL, Boolean.toString(link.isInternal()));
    
                    linkElement.addElement(CmsXmlPage.NODE_TARGET).addCDATA(link.getTarget());

                    if (link.getAnchor() != null) {
                        linkElement.addElement(CmsXmlPage.NODE_ANCHOR).addCDATA(link.getAnchor());
                    }

                    if (link.getQuery() != null) {
                        linkElement.addElement(CmsXmlPage.NODE_QUERY).addCDATA(link.getQuery());
                    }
                }
            }
        }

        // ensure the String value is re-calculated next time
        m_stringValue = null;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(java.lang.String)
     */
    public void setStringValue(String value) throws CmsXmlException {

        // we don't have any information available for link processing
        setStringValue(null, null, value);
    }

    /**
     * Creates the String value for this HTML value element.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param document the XML document this value belongs to
     * 
     * @return the String value for this HTML value element
     */
    private String createStringValue(CmsObject cms, I_CmsXmlDocument document) {

        Element data = m_element.element(CmsXmlPage.NODE_CONTENT);
        Attribute enabled = m_element.attribute(CmsXmlPage.ATTRIBUTE_ENABLED);

        String content = "";
        if (enabled == null || Boolean.valueOf(enabled.getText()).booleanValue()) {

            content = data.getText();

            CmsLinkTable linkTable = getLinkTable();
            if (!linkTable.isEmpty()) {

                // link processing: replace macros with links
                CmsLinkProcessor linkProcessor = document.getLinkProcessor(cms, linkTable);
                try {
                    content = linkProcessor.processLinks(content);
                } catch (ParserException e) {
                    // should better not happen
                    OpenCms.getLog(this).error("HTML link processing failed", e);
                }
            }
        }
        return content;
    }
}