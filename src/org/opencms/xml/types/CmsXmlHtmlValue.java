/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlHtmlValue.java,v $
 * Date   : $Date: 2004/10/20 10:54:08 $
 * Version: $Revision: 1.9 $
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
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.htmlparser.util.ParserException;

/**
 * Describes the XML content type "OpenCmsHtml".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.9 $
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
     * Creates a new Locale type definition.<p>
     */
    public CmsXmlHtmlValue() {
        // empty constructor is required for class registration
    }
    
    /**
     * Creates a new XML content value.<p>
     * 
     * @param element the XML element that contains the value
     * @param name the node name of this value in the source XML document
     * @param index the index of the XML element in the source document
     */
    public CmsXmlHtmlValue(Element element, String name, int index) {
        
        m_element = element;
        m_name = name;
        m_index = index;
    }
    
    /**
     * Creates a new Locale type which must occur exaclty once.<p>
     * 
     * @param name the name of the element
     */
    public CmsXmlHtmlValue(String name) {
        m_name = name;
        m_minOccurs = 1;
        m_maxOccurs = 1;
    }
    
    /**
     * Creates a new Locale type.<p>
     * 
     * @param name the name of the element
     * @param minOccurs minimum number of occurences
     * @param maxOccurs maximum number of occurences
     */
    public CmsXmlHtmlValue(String name, int minOccurs, int maxOccurs) {
        m_name = name;
        m_minOccurs = minOccurs;
        m_maxOccurs = maxOccurs;
    }

    /**
     * Creates a new Locale type.<p>
     * 
     * @param name the name of the element
     * @param minOccurs minimum number of occurences
     * @param maxOccurs maximum number of occurences
     */
    public CmsXmlHtmlValue(String name, String minOccurs, String maxOccurs) {
        m_name = name;
        m_minOccurs = 1;
        if (CmsStringUtil.isNotEmpty(minOccurs)) {
            try {
                m_minOccurs = Integer.valueOf(minOccurs).intValue();
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        m_maxOccurs = 1;
        if (CmsStringUtil.isNotEmpty(maxOccurs)) {
            if (CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_UNBOUNDED.equals(maxOccurs)) {
                m_maxOccurs = Integer.MAX_VALUE;
            } else {
                try {
                    m_maxOccurs = Integer.valueOf(maxOccurs).intValue();
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
    }    
    
    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#appendDefaultXml(org.dom4j.Element, int)
     */
    public void appendDefaultXml(Element root, int index) {

        Element sub = root.addElement(getNodeName());
        sub.addAttribute(CmsXmlPage.ATTRIBUTE_NAME, getNodeName() + index);
        sub.addElement(CmsXmlPage.NODE_LINKS);
        sub.addElement(CmsXmlPage.NODE_CONTENT);
        
//        if (m_defaultValue != null) {
//            try {
//                I_CmsXmlContentValue value = createValue(sub, getNodeName(), index);
//                int todo = 0;
//                // TODO: check "double null" dilemma here...
//                value.setStringValue(null, null, m_defaultValue);
//            } catch (CmsXmlException e) {
//                // should not happen if default value is correct
//                OpenCms.getLog(this).error("Invalid default value '" + m_defaultValue + "' for XML content", e);
//                sub.clearContent();
//            }
//        }
    }
    
    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(org.dom4j.Element, java.lang.String, int)
     */
    public I_CmsXmlContentValue createValue(Element element, String name, int index) {
        
        return new CmsXmlHtmlValue(element, name, index);
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
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {
        
        // the schema definition is located in a separate file for easier editing
        if (m_schemaDefinition == null) {            
            try {
                m_schemaDefinition = CmsFileUtil.readFile("org/opencms/xml/types/HtmlValue.xsd", CmsEncoder.C_UTF8_ENCODING);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject, A_CmsXmlDocument)
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
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, org.opencms.xml.A_CmsXmlDocument, java.lang.String)
     */
    public void setStringValue(CmsObject cms, I_CmsXmlDocument document, String value) throws CmsXmlException {

        Element content = m_element.element(CmsXmlPage.NODE_CONTENT);
        Element links = m_element.element(CmsXmlPage.NODE_LINKS);

        int todo = 0;
        // TODO: solve Interface vs. abstract issue
        A_CmsXmlDocument xmlDoc = (A_CmsXmlDocument)document;
        
        String encoding = xmlDoc.getEncoding();
        CmsLinkProcessor linkProcessor = xmlDoc.getLinkProcessor(cms, new CmsLinkTable());

        if (encoding != null) {
            // ensure all chars in the given content are valid chars for the selected charset
            value = CmsEncoder.adjustHtmlEncoding(value, encoding);
        }
        
        // do some processing to remove unnecessary tags if necessary
        String contentConversion = xmlDoc.getConversion();    
        if (CmsHtmlConverter.isConversionEnabled(contentConversion)) {
            CmsHtmlConverter converter = new CmsHtmlConverter(encoding, contentConversion);
            value = converter.convertToStringSilent(value);
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
        content.addCDATA(value);

        CmsLinkTable linkTable = linkProcessor.getLinkTable();
        links.setContent(null);
        for (Iterator i = linkTable.iterator(); i.hasNext();) {
            CmsLink link = linkTable.getLink((String)i.next());

            Element linkElement = links.addElement(CmsXmlPage.NODE_LINK).addAttribute(
                CmsXmlPage.ATTRIBUTE_NAME,
                link.getName()).addAttribute(CmsXmlPage.ATTRIBUTE_TYPE, link.getType()).addAttribute(
                CmsXmlPage.ATTRIBUTE_INTERNAL,
                Boolean.toString(link.isInternal()));

            linkElement.addElement(CmsXmlPage.NODE_TARGET).addCDATA(link.getTarget());

            if (link.getAnchor() != null) {
                linkElement.addElement(CmsXmlPage.NODE_ANCHOR).addCDATA(link.getAnchor());
            }

            if (link.getQuery() != null) {
                linkElement.addElement(CmsXmlPage.NODE_QUERY).addCDATA(link.getQuery());
            }
        }
        
        // ensure the String value is re-calculated next time
        m_stringValue = null;
    }
    
    /**
     * Creates the String value for this XML page element.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param document the XML document this value belongs to
     * 
     * @return the String value for this XML page element
     */
    private String createStringValue(CmsObject cms, I_CmsXmlDocument document) {
        
        Element data = m_element.element(CmsXmlPage.NODE_CONTENT);
        Attribute enabled = m_element.attribute(CmsXmlPage.ATTRIBUTE_ENABLED);
        
        int todo = 0;
        // TODO: solve Interface vs. abstract issue
        A_CmsXmlDocument xmlDoc = (A_CmsXmlDocument)document;
        
        String content = "";
        if (enabled == null || Boolean.valueOf(enabled.getText()).booleanValue()) {
        
            content = data.getText();

            CmsLinkTable linkTable = getLinkTable();
            if (!linkTable.isEmpty()) {
                
                int todo2 = 0;
                // TODO: boolean value for "editor mode" currently always set to "false", see XML page handling of this
                
                // replace macros with links
                CmsLinkProcessor linkProcessor = new CmsLinkProcessor(cms, linkTable, xmlDoc.getEncoding(), null);                
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
